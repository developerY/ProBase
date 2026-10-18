Below is the complete end-to-end execution specification, the production `tag_pipeline.py` compiler script, and the corresponding Android Kotlin codebase updates. You can hand this entire document directly to your AI coding agent to execute the catalog enrichment, `.kpkg` recompilation, and Android client integration.

---

## Part 1: Master Prompt & Runbook for the Coding Agent

Copy and paste the block below as the top-level task prompt for your autonomous coding agent:

```markdown
# AGENT MISSION: Implement and Execute the KoColor Catalog Tagging Pipeline (v1.0.0)

You are tasked with upgrading the KoColor CDN Catalog (`server/Inventory/package/`) and the Android client (`com.zoewave.probase.kocolor`) to use the standardized **KoColor Catalog Metadata Taxonomy (`metadataVersion: 1`)**.

Execute the following steps in strict sequential order. Do not skip validation gates.

## STEP 1: Set Up the Server Tagging Pipeline
1. Navigate to `server/Inventory/package/`.
2. Install required Python dependencies:
   ```bash
   pip install google-genai pydantic zstandard pynacl

```

3. Create `server/Inventory/package/tag_pipeline.py` using the provided implementation.
4. Verify environment variables before running:
* `GEMINI_API_KEY`: Required for Phase 3 structured taxonomy extraction.
* `KOCOLOR_SIGNING_SEED_HEX` (optional 64-char hex seed for `atelier-dev-01` Ed25519 signing; if unset, the script uses a deterministic dev keypair and logs the public key).



## STEP 2: Discover, Enrich, and Compile All CDN Catalog Items

1. Run `tag_pipeline.py` against the raw catalog directory:
```bash
python3 server/Inventory/package/tag_pipeline.py \
  --input-dir server/Inventory/package/input/KoColor \
  --output-dir server/Inventory/package/dist \
  --model gemini-2.5-flash

```


2. Inspect `server/Inventory/package/dist/quality_report.json` and `quarantine/`:
* Ensure `quarantined_count == 0`.
* If any item is quarantined (missing `productType`, invalid hex, or missing `RULE_DERIVED`/`SOURCE_VERIFIED` thermal/occasion tags), inspect the quarantine reason, update the source `.json`/`.notes.json` or deterministic rule map, and re-run until 100% of items pass Phase 5.


3. Verify that all `.kpkg` bundles and `manifest.json` are generated in `server/Inventory/package/dist/` with valid `zstd` compression (level 19, without `--ultra`), `sha256` checksums, and `ed25519` signatures.

## STEP 3: Update the Android Data Models & Package Parser

1. Create/Update `domain/taxonomy/KoColorTaxonomy.kt` with all controlled vocabulary enums and `ProvenanceType` (`SOURCE_VERIFIED`, `RULE_DERIVED`, `LLM_SUGGESTED`, `HUMAN_REVIEWED`, `UNKNOWN`).
2. Create/Update `domain/model/CatalogRecord.kt` to deserialize the new `metadataVersion: 1` schema (`commonMetadata`, `wardrobeMetadata`, `cosmeticsMetadata`).
* Maintain backward compatibility in the JSON parser (`ignoreUnknownKeys = true`, `coerceInputValues = true`) so cached legacy items do not crash on app upgrade.


3. Update the `.kpkg` decompression and import repository (`fetchVerifiedPackage` / local Room DAO mappers) to persist the enriched `WardrobeMetadata` and `CosmeticsMetadata` fields.

## STEP 4: Update `DeterministicWardrobeEngine` & `StyleSimulatorEngine`

1. Update `engine/DeterministicWardrobeEngine.kt` to enforce the runtime rules:
* **Rule 4.1 (Provenance Policy):** Hard filters (Weather, Occasion, Activity, Category/Slot Isolation) may ONLY evaluate tags whose provenance is `SOURCE_VERIFIED`, `HUMAN_REVIEWED`, or `RULE_DERIVED`. Tags marked `LLM_SUGGESTED` must ONLY be used in Soft Preference Scoring.
* **Rule 4.2 (Preflight Candidate Check):** Group eligible items by `wardrobeMetadata.garmentSlot.value`. If any required slot (`TOP`, `BOTTOM`, `FOOTWEAR`) has `0` eligible candidates after hard filtering (or if the initial catalog is empty), immediately abort before calling Gemini and return `PruningResult.AbortedMissingSlots`.
* **Chromatic Isolation:** Filter out cosmetics with `chromaticBehavior` in `["TRANSLUCENT", "NON_CHROMATIC"]` (and categories `PREP`, `HAIR`, `COMPLEXION`) before CIELAB color harmony scoring and before constructing the Gemini cosmetic candidate manifest.


2. Update the Gemini Prompt Serializer so the `AVAILABLE CANDIDATES (COMPACT MANIFEST)` section outputs verified `material.composition` and `construction.features` for each candidate, satisfying Strict Grounding Rule #1.
3. Wire `BlueprintValidator.validateAiSelection()` immediately after Gemini returns JSON and before `[5] AESTHETIC CALIBRATION (FASHIONISTA)` runs. If validation fails or returns 0 items, do NOT compute a FASHIONISTA score.

## STEP 5: Build & Verify

1. Run `./gradlew testDebugUnitTest` and `./gradlew assembleDebug`.
2. Verify zero compilation or serialization errors.

```

---

## Part 2: Complete `tag_pipeline.py` Implementation

Save this script to `server/Inventory/package/tag_pipeline.py`. It implements all 7 phases of your architecture plan: file pairing, D65 2° CIELAB/LCh color telemetry, Gemini Structured Outputs via Pydantic, deterministic rule promotion, the Quality Gate & Quarantine reporter, RFC 8785 canonical JSON serialization, `zstd -19` compression, and `ed25519` package + envelope signing.

```python
#!/usr/bin/env python3
"""
KoColor Catalog Tagging & .kpkg Compiler Pipeline (v1.0.0)
Implements Phases 1-7:
  1. Discovery & Pairing (.json + .notes.json)
  2. Deterministic D65 CIELAB / LCh Color Telemetry
  3. Gemini Structured Taxonomy Extraction (Pydantic Schema)
  4. Deterministic Rule Promotion & Schema Merging
  5. Quality Gate, Provenance Enforcement & Quarantine
  6. RFC 8785 Canonical JSON Serialization & Zstandard (-19) Compression
  7. SHA-256 Digest, Ed25519 Package Signing & Manifest Envelope Signing
"""

import argparse
import hashlib
import json
import math
import os
import re
import sys
from datetime import datetime, timezone
from enum import Enum
from pathlib import Path
from typing import Any, Dict, List, Optional, Tuple

import zstandard as zstd
from google import genai
from google.genai import types
from nacl.signing import SigningKey
from pydantic import BaseModel, Field

# =====================================================================
# 1. CONTROLLED VOCABULARY ENUMS
# =====================================================================

class DomainType(str, Enum):
    WARDROBE = "WARDROBE"
    COSMETICS = "COSMETICS"

class ProvenanceType(str, Enum):
    SOURCE_VERIFIED = "SOURCE_VERIFIED"
    RULE_DERIVED = "RULE_DERIVED"
    LLM_SUGGESTED = "LLM_SUGGESTED"
    HUMAN_REVIEWED = "HUMAN_REVIEWED"
    UNKNOWN = "UNKNOWN"

class SustainabilityGrade(str, Enum):
    GRADE_A = "GRADE_A"
    GRADE_B = "GRADE_B"
    GRADE_C = "GRADE_C"
    UNRATED = "UNRATED"

class ChromaticBehavior(str, Enum):
    OPAQUE = "OPAQUE"
    SHEER = "SHEER"
    TRANSLUCENT = "TRANSLUCENT"
    NON_CHROMATIC = "NON_CHROMATIC"

class HueFamily(str, Enum):
    RED = "RED"
    ORANGE = "ORANGE"
    YELLOW = "YELLOW"
    GREEN = "GREEN"
    CYAN = "CYAN"
    BLUE = "BLUE"
    PURPLE = "PURPLE"
    MAGENTA = "MAGENTA"
    NEUTRAL = "NEUTRAL"

class ColorTemperature(str, Enum):
    WARM = "WARM"
    COOL = "COOL"
    NEUTRAL = "NEUTRAL"

class CatalogCategory(str, Enum):
    TOPS = "TOPS"
    BOTTOMS = "BOTTOMS"
    OUTERWEAR = "OUTERWEAR"
    ACTIVEWEAR = "ACTIVEWEAR"
    FOOTWEAR = "FOOTWEAR"
    ONE_PIECE = "ONE_PIECE"
    ACCESSORIES = "ACCESSORIES"

class GarmentSlot(str, Enum):
    TOP = "TOP"
    BOTTOM = "BOTTOM"
    OUTERWEAR = "OUTERWEAR"
    ONE_PIECE = "ONE_PIECE"
    FOOTWEAR = "FOOTWEAR"
    ACCESSORY = "ACCESSORY"

class ProductType(str, Enum):
    CROP_TOP = "CROP_TOP"
    T_SHIRT = "T_SHIRT"
    TANK_TOP = "TANK_TOP"
    BLOUSE = "BLOUSE"
    BUTTON_DOWN = "BUTTON_DOWN"
    POLO = "POLO"
    SWEATER = "SWEATER"
    CARDIGAN = "CARDIGAN"
    HOODIE = "HOODIE"
    SWEATSHIRT = "SWEATSHIRT"
    BODYSUIT = "BODYSUIT"
    TROUSERS = "TROUSERS"
    CHINOS = "CHINOS"
    JEANS = "JEANS"
    SHORTS = "SHORTS"
    SKIRT = "SKIRT"
    LEGGINGS = "LEGGINGS"
    JOGGERS = "JOGGERS"
    TRACK_PANTS = "TRACK_PANTS"
    DRESS = "DRESS"
    JUMPSUIT = "JUMPSUIT"
    ROMPER = "ROMPER"
    SNEAKERS = "SNEAKERS"
    RUNNING_SHOES = "RUNNING_SHOES"
    BOOTS = "BOOTS"
    ANKLE_BOOTS = "ANKLE_BOOTS"
    LOAFERS = "LOAFERS"
    OXFORDS = "OXFORDS"
    HEELS = "HEELS"
    FLATS = "FLATS"
    SANDALS = "SANDALS"
    SLIDES = "SLIDES"
    TRACK_JACKET = "TRACK_JACKET"
    WINDBREAKER = "WINDBREAKER"
    TRENCH_COAT = "TRENCH_COAT"
    LEATHER_JACKET = "LEATHER_JACKET"
    WOOL_OVERCOAT = "WOOL_OVERCOAT"
    BLAZER = "BLAZER"
    DENIM_JACKET = "DENIM_JACKET"
    BOMBER_JACKET = "BOMBER_JACKET"
    PUFFER = "PUFFER"
    PARKA = "PARKA"
    VEST = "VEST"
    BAG = "BAG"
    BELT = "BELT"
    HAT = "HAT"
    SCARF = "SCARF"
    SUNGLASSES = "SUNGLASSES"
    WATCH = "WATCH"
    JEWELRY = "JEWELRY"
    UNKNOWN = "UNKNOWN"

class OccasionTag(str, Enum):
    CASUAL = "CASUAL"
    EVERYDAY = "EVERYDAY"
    WORK = "WORK"
    EVENING = "EVENING"
    TRAVEL = "TRAVEL"
    FORMAL_EVENT = "FORMAL_EVENT"
    ACTIVE = "ACTIVE"

class FormalityLevel(str, Enum):
    CASUAL = "CASUAL"
    SMART_CASUAL = "SMART_CASUAL"
    BUSINESS = "BUSINESS"
    FORMAL = "FORMAL"
    GALA = "GALA"

class ThermalBand(str, Enum):
    HOT = "HOT"
    WARM = "WARM"
    MILD = "MILD"
    COOL = "COOL"
    COLD = "COLD"

class WeatherProtection(str, Enum):
    RAIN_RESISTANT = "RAIN_RESISTANT"
    WIND_RESISTANT = "WIND_RESISTANT"
    SUN_COVERAGE = "SUN_COVERAGE"
    THERMAL_RETENTION = "THERMAL_RETENTION"

class ThermalWeight(str, Enum):
    LIGHT = "LIGHT"
    MIDWEIGHT = "MIDWEIGHT"
    HEAVYWEIGHT = "HEAVYWEIGHT"
    UNKNOWN = "UNKNOWN"

class Breathability(str, Enum):
    LOW = "LOW"
    MEDIUM = "MEDIUM"
    HIGH = "HIGH"
    UNKNOWN = "UNKNOWN"

class InsulationLevel(str, Enum):
    NONE = "NONE"
    LIGHT = "LIGHT"
    MEDIUM = "MEDIUM"
    HEAVY = "HEAVY"
    UNKNOWN = "UNKNOWN"

class ConstructionFeature(str, Enum):
    RIBBED_CUFFS = "RIBBED_CUFFS"
    RIBBED_HEM = "RIBBED_HEM"
    ZIP_POCKETS = "ZIP_POCKETS"
    SIDE_POCKETS = "SIDE_POCKETS"
    PATCH_POCKETS = "PATCH_POCKETS"
    FULL_ZIP = "FULL_ZIP"
    HALF_ZIP = "HALF_ZIP"
    BUTTON_FRONT = "BUTTON_FRONT"
    DOUBLE_BREASTED = "DOUBLE_BREASTED"
    DRAWSTRING_WAIST = "DRAWSTRING_WAIST"
    ELASTIC_WAIST = "ELASTIC_WAIST"
    BELTED = "BELTED"
    HOODED = "HOODED"
    COLLARED = "COLLARED"
    CREWNECK = "CREWNECK"
    V_NECK = "V_NECK"
    MOCK_NECK = "MOCK_NECK"
    TURTLENECK = "TURTLENECK"
    PLEATED = "PLEATED"
    CUFFED_HEM = "CUFFED_HEM"
    WATER_REPELLENT_FINISH = "WATER_REPELLENT_FINISH"
    SEAM_SEALED = "SEAM_SEALED"
    VENTED = "VENTED"
    LINED = "LINED"
    UNLINED = "UNLINED"
    STRETCH_KNIT = "STRETCH_KNIT"

class LayeringRole(str, Enum):
    BASE_LAYER = "BASE_LAYER"
    MID_LAYER = "MID_LAYER"
    OUTER_LAYER = "OUTER_LAYER"
    STANDALONE = "STANDALONE"
    FOOTWEAR = "FOOTWEAR"
    ACCESSORY = "ACCESSORY"

class SilhouetteFit(str, Enum):
    FITTED = "FITTED"
    TAILORED = "TAILORED"
    RELAXED = "RELAXED"
    OVERSIZED = "OVERSIZED"
    UNKNOWN = "UNKNOWN"

class SeasonTag(str, Enum):
    SPRING = "SPRING"
    SUMMER = "SUMMER"
    AUTUMN = "AUTUMN"
    WINTER = "WINTER"

class ActivityTag(str, Enum):
    OFFICE = "OFFICE"
    WALKING = "WALKING"
    CYCLING = "CYCLING"
    EXERCISE = "EXERCISE"
    LOUNGE = "LOUNGE"
    DINING = "DINING"
    COMMUTE = "COMMUTE"
    SOCIAL = "SOCIAL"

class MaterialFiber(str, Enum):
    ORGANIC_COTTON = "ORGANIC_COTTON"
    COTTON = "COTTON"
    LINEN = "LINEN"
    SILK = "SILK"
    MERINO_WOOL = "MERINO_WOOL"
    WOOL = "WOOL"
    CASHMERE = "CASHMERE"
    ALPACA = "ALPACA"
    POLYESTER = "POLYESTER"
    TRICOT_POLYESTER = "TRICOT_POLYESTER"
    RECYCLED_POLYESTER = "RECYCLED_POLYESTER"
    NYLON = "NYLON"
    RECYCLED_NYLON = "RECYCLED_NYLON"
    SPANDEX = "SPANDEX"
    ELASTANE = "ELASTANE"
    VISCOSE = "VISCOSE"
    RAYON = "RAYON"
    TENCEL_LYOCELL = "TENCEL_LYOCELL"
    MODAL = "MODAL"
    ACRYLIC = "ACRYLIC"
    LEATHER = "LEATHER"
    SUEDE = "SUEDE"
    VEGAN_LEATHER = "VEGAN_LEATHER"
    GORE_TEX = "GORE_TEX"
    DENIM = "DENIM"
    CANVAS = "CANVAS"
    FLEECE = "FLEECE"
    DOWN = "DOWN"
    SYNTHETIC_FILL = "SYNTHETIC_FILL"
    RUBBER = "RUBBER"
    EVA_FOAM = "EVA_FOAM"
    MESH = "MESH"
    COSMETIC_FORMULATION = "COSMETIC_FORMULATION"
    UNKNOWN = "UNKNOWN"

class CosmeticCategory(str, Enum):
    EYE = "EYE"
    CHEEK = "CHEEK"
    LIP = "LIP"
    NAIL = "NAIL"
    PREP = "PREP"
    COMPLEXION = "COMPLEXION"
    HAIR = "HAIR"
    BODY = "BODY"

class CosmeticApplicationRole(str, Enum):
    LIPSTICK = "LIPSTICK"
    LIP_GLOSS = "LIP_GLOSS"
    LIP_BALM = "LIP_BALM"
    LIP_LINER = "LIP_LINER"
    EYESHADOW = "EYESHADOW"
    EYELINER = "EYELINER"
    MASCARA = "MASCARA"
    BROW = "BROW"
    BLUSH = "BLUSH"
    BRONZER = "BRONZER"
    HIGHLIGHTER = "HIGHLIGHTER"
    NAIL_LACQUER = "NAIL_LACQUER"
    PRIMER = "PRIMER"
    FOUNDATION = "FOUNDATION"
    CONCEALER = "CONCEALER"
    SETTING_SPRAY = "SETTING_SPRAY"
    CLEANSER = "CLEANSER"
    MOISTURIZER = "MOISTURIZER"
    SERUM = "SERUM"
    SUNSCREEN = "SUNSCREEN"
    AFTERSHAVE = "AFTERSHAVE"
    BEARD_OIL = "BEARD_OIL"
    DEODORANT = "DEODORANT"
    BODY_WASH = "BODY_WASH"
    OTHER = "OTHER"

class CosmeticFinish(str, Enum):
    MATTE = "MATTE"
    SATIN = "SATIN"
    NATURAL = "NATURAL"
    LUMINOUS = "LUMINOUS"
    HIGH_SHINE = "HIGH_SHINE"
    METALLIC = "METALLIC"
    SHEER = "SHEER"
    UNKNOWN = "UNKNOWN"


# =====================================================================
# 2. PYDANTIC SCHEMA FOR GEMINI STRUCTURED EXTRACTION (PHASE 3)
# =====================================================================

class MaterialComponentLLM(BaseModel):
    material: MaterialFiber
    percentage: int = Field(ge=1, le=100)

class LlmExtractionResult(BaseModel):
    domain: DomainType
    chromaticBehavior: ChromaticBehavior
    sustainabilityGrade: SustainabilityGrade
    materials: List[MaterialComponentLLM]

    # Wardrobe-specific fields (populated when domain == WARDROBE)
    catalogCategory: Optional[CatalogCategory] = None
    productType: Optional[ProductType] = None
    occasionTags: List[OccasionTag] = Field(default_factory=list)
    formality: Optional[FormalityLevel] = None
    thermalSuitability: List[ThermalBand] = Field(default_factory=list)
    weatherProtection: List[WeatherProtection] = Field(default_factory=list)
    thermalWeight: Optional[ThermalWeight] = None
    breathability: Optional[Breathability] = None
    insulation: Optional[InsulationLevel] = None
    features: List[ConstructionFeature] = Field(default_factory=list)
    primaryLayeringRole: Optional[LayeringRole] = None
    compatibleLayeringRoles: List[LayeringRole] = Field(default_factory=list)
    silhouette: Optional[SilhouetteFit] = None
    seasons: List[SeasonTag] = Field(default_factory=list)
    activities: List[ActivityTag] = Field(default_factory=list)

    # Cosmetics-specific fields (populated when domain == COSMETICS)
    cosmeticCategory: Optional[CosmeticCategory] = None
    applicationRole: Optional[CosmeticApplicationRole] = None
    shadeName: Optional[str] = None
    finish: Optional[CosmeticFinish] = None
    formulationAttributes: List[str] = Field(default_factory=list)


# =====================================================================
# 3. PHASE 2: DETERMINISTIC D65 CIELAB / LCH COLOR TELEMETRY
# =====================================================================

COLOR_ALGO_VERSION = "1.0.0"
COLOR_RULE_VERSION = "1.0.0"

def srgb_channel_to_linear(c: float) -> float:
    return c / 12.92 if c <= 0.04045 else ((c + 0.055) / 1.055) ** 2.4

def compute_cielab_d65(hex_str: str) -> Dict[str, Any]:
    """Deterministically converts #RRGGBB into D65 2-deg CIELAB and LCh."""
    clean_hex = hex_str.strip().lstrip("#").upper()
    if len(clean_hex) != 6 or not all(c in "0123456789ABCDEF" for c in clean_hex):
        raise ValueError(f"Invalid HEX color string: {hex_str}")

    r = srgb_channel_to_linear(int(clean_hex[0:2], 16) / 255.0)
    g = srgb_channel_to_linear(int(clean_hex[2:4], 16) / 255.0)
    b = srgb_channel_to_linear(int(clean_hex[4:6], 16) / 255.0)

    # sRGB to XYZ (D65 reference white)
    x = r * 0.4124564 + g * 0.3575761 + b * 0.1804375
    y = r * 0.2126729 + g * 0.7151522 + b * 0.0721750
    z = r * 0.0193339 + g * 0.1191920 + b * 0.9503041

    # Normalize by D65 White Point (Xn=0.95047, Yn=1.00000, Zn=1.08883)
    xr = x / 0.95047
    yr = y / 1.00000
    zr = z / 1.08883

    epsilon = 216.0 / 24389.0
    kappa = 24389.0 / 27.0

    def f(t: float) -> float:
        return t ** (1.0 / 3.0) if t > epsilon else (kappa * t + 16.0) / 116.0

    fx, fy, fz = f(xr), f(yr), f(zr)

    l_star = 116.0 * fy - 16.0
    a_star = 500.0 * (fx - fy)
    b_star = 200.0 * (fy - fz)

    c_star = math.sqrt(a_star ** 2 + b_star ** 2)
    h_rad = math.atan2(b_star, a_star)
    h_deg = math.degrees(h_rad) % 360.0

    # Deterministic Hue Family Classification
    if c_star < 8.5:
        hue_family = HueFamily.NEUTRAL
    elif h_deg < 25.0 or h_deg >= 345.0:
        hue_family = HueFamily.RED
    elif h_deg < 60.0:
        hue_family = HueFamily.ORANGE
    elif h_deg < 105.0:
        hue_family = HueFamily.YELLOW
    elif h_deg < 165.0:
        hue_family = HueFamily.GREEN
    elif h_deg < 210.0:
        hue_family = HueFamily.CYAN
    elif h_deg < 275.0:
        hue_family = HueFamily.BLUE
    elif h_deg < 315.0:
        hue_family = HueFamily.PURPLE
    else:
        hue_family = HueFamily.MAGENTA

    # Deterministic Color Temperature Classification
    if c_star < 6.0:
        color_temp = ColorTemperature.NEUTRAL
    elif 15.0 <= h_deg <= 115.0:
        color_temp = ColorTemperature.WARM
    elif 185.0 <= h_deg <= 305.0:
        color_temp = ColorTemperature.COOL
    else:
        color_temp = ColorTemperature.WARM if b_star > 4.0 else (
            ColorTemperature.COOL if b_star < -4.0 else ColorTemperature.NEUTRAL
        )

    return {
        "hex": f"#{clean_hex}",
        "hueFamily": {
            "value": hue_family.value,
            "provenance": ProvenanceType.RULE_DERIVED.value
        },
        "colorTemperature": {
            "value": color_temp.value,
            "provenance": ProvenanceType.RULE_DERIVED.value
        },
        "lStar": round(l_star, 2),
        "aStar": round(a_star, 2),
        "bStar": round(b_star, 2),
        "cStar": round(c_star, 2),
        "hDeg": round(h_deg, 1),
        "colorAlgorithmVersion": COLOR_ALGO_VERSION,
        "colorClassificationRuleVersion": COLOR_RULE_VERSION
    }


# =====================================================================
# 4. PHASE 4: DETERMINISTIC RULE PROMOTION MATRIX
# =====================================================================

PRODUCT_TYPE_TO_SLOT: Dict[ProductType, GarmentSlot] = {
    ProductType.CROP_TOP: GarmentSlot.TOP,
    ProductType.T_SHIRT: GarmentSlot.TOP,
    ProductType.TANK_TOP: GarmentSlot.TOP,
    ProductType.BLOUSE: GarmentSlot.TOP,
    ProductType.BUTTON_DOWN: GarmentSlot.TOP,
    ProductType.POLO: GarmentSlot.TOP,
    ProductType.SWEATER: GarmentSlot.TOP,
    ProductType.CARDIGAN: GarmentSlot.TOP,
    ProductType.HOODIE: GarmentSlot.TOP,
    ProductType.SWEATSHIRT: GarmentSlot.TOP,
    ProductType.BODYSUIT: GarmentSlot.TOP,
    ProductType.TROUSERS: GarmentSlot.BOTTOM,
    ProductType.CHINOS: GarmentSlot.BOTTOM,
    ProductType.JEANS: GarmentSlot.BOTTOM,
    ProductType.SHORTS: GarmentSlot.BOTTOM,
    ProductType.SKIRT: GarmentSlot.BOTTOM,
    ProductType.LEGGINGS: GarmentSlot.BOTTOM,
    ProductType.JOGGERS: GarmentSlot.BOTTOM,
    ProductType.TRACK_PANTS: GarmentSlot.BOTTOM,
    ProductType.DRESS: GarmentSlot.ONE_PIECE,
    ProductType.JUMPSUIT: GarmentSlot.ONE_PIECE,
    ProductType.ROMPER: GarmentSlot.ONE_PIECE,
    ProductType.SNEAKERS: GarmentSlot.FOOTWEAR,
    ProductType.RUNNING_SHOES: GarmentSlot.FOOTWEAR,
    ProductType.BOOTS: GarmentSlot.FOOTWEAR,
    ProductType.ANKLE_BOOTS: GarmentSlot.FOOTWEAR,
    ProductType.LOAFERS: GarmentSlot.FOOTWEAR,
    ProductType.OXFORDS: GarmentSlot.FOOTWEAR,
    ProductType.HEELS: GarmentSlot.FOOTWEAR,
    ProductType.FLATS: GarmentSlot.FOOTWEAR,
    ProductType.SANDALS: GarmentSlot.FOOTWEAR,
    ProductType.SLIDES: GarmentSlot.FOOTWEAR,
    ProductType.TRACK_JACKET: GarmentSlot.OUTERWEAR,
    ProductType.WINDBREAKER: GarmentSlot.OUTERWEAR,
    ProductType.TRENCH_COAT: GarmentSlot.OUTERWEAR,
    ProductType.LEATHER_JACKET: GarmentSlot.OUTERWEAR,
    ProductType.WOOL_OVERCOAT: GarmentSlot.OUTERWEAR,
    ProductType.BLAZER: GarmentSlot.OUTERWEAR,
    ProductType.DENIM_JACKET: GarmentSlot.OUTERWEAR,
    ProductType.BOMBER_JACKET: GarmentSlot.OUTERWEAR,
    ProductType.PUFFER: GarmentSlot.OUTERWEAR,
    ProductType.PARKA: GarmentSlot.OUTERWEAR,
    ProductType.VEST: GarmentSlot.OUTERWEAR,
    ProductType.BAG: GarmentSlot.ACCESSORY,
    ProductType.BELT: GarmentSlot.ACCESSORY,
    ProductType.HAT: GarmentSlot.ACCESSORY,
    ProductType.SCARF: GarmentSlot.ACCESSORY,
    ProductType.SUNGLASSES: GarmentSlot.ACCESSORY,
    ProductType.WATCH: GarmentSlot.ACCESSORY,
    ProductType.JEWELRY: GarmentSlot.ACCESSORY,
}

# Deterministic Thermal & Occasion baseline rules by ProductType
DETERMINISTIC_GARMENT_RULES: Dict[ProductType, Dict[str, Any]] = {
    ProductType.CROP_TOP: {
        "thermal": [ThermalBand.HOT, ThermalBand.WARM],
        "occasions": [OccasionTag.CASUAL, OccasionTag.EVERYDAY],
        "weight": ThermalWeight.LIGHT,
        "insulation": InsulationLevel.NONE,
        "layering": LayeringRole.STANDALONE,
    },
    ProductType.T_SHIRT: {
        "thermal": [ThermalBand.HOT, ThermalBand.WARM, ThermalBand.MILD],
        "occasions": [OccasionTag.CASUAL, OccasionTag.EVERYDAY],
        "weight": ThermalWeight.LIGHT,
        "insulation": InsulationLevel.NONE,
        "layering": LayeringRole.BASE_LAYER,
    },
    ProductType.TRACK_JACKET: {
        "thermal": [ThermalBand.MILD, ThermalBand.COOL],
        "occasions": [OccasionTag.ACTIVE, OccasionTag.CASUAL, OccasionTag.EVERYDAY],
        "weight": ThermalWeight.MIDWEIGHT,
        "insulation": InsulationLevel.NONE,
        "layering": LayeringRole.OUTER_LAYER,
    },
    ProductType.TRENCH_COAT: {
        "thermal": [ThermalBand.MILD, ThermalBand.COOL],
        "occasions": [OccasionTag.WORK, OccasionTag.EVERYDAY, OccasionTag.TRAVEL],
        "weight": ThermalWeight.MIDWEIGHT,
        "insulation": InsulationLevel.LIGHT,
        "layering": LayeringRole.OUTER_LAYER,
    },
    ProductType.LEATHER_JACKET: {
        "thermal": [ThermalBand.MILD, ThermalBand.COOL, ThermalBand.COLD],
        "occasions": [OccasionTag.CASUAL, OccasionTag.EVENING, OccasionTag.EVERYDAY],
        "weight": ThermalWeight.HEAVYWEIGHT,
        "insulation": InsulationLevel.LIGHT,
        "layering": LayeringRole.OUTER_LAYER,
    },
    ProductType.WOOL_OVERCOAT: {
        "thermal": [ThermalBand.COOL, ThermalBand.COLD],
        "occasions": [OccasionTag.WORK, OccasionTag.FORMAL_EVENT, OccasionTag.EVENING],
        "weight": ThermalWeight.HEAVYWEIGHT,
        "insulation": InsulationLevel.MEDIUM,
        "layering": LayeringRole.OUTER_LAYER,
    },
}


# =====================================================================
# 5. PHASE 3: GEMINI STRUCTURED EXTRACTION PROMPT
# =====================================================================

SYSTEM_INSTRUCTION = """
You are the KoColor Catalog Taxonomy & Metadata Compiler Agent.
Transform raw product descriptions into strictly validated enums for the KoColor styling engine.

STRICT GROUNDING RULES:
1. Extract ONLY materials and hardware features explicitly mentioned in the input text.
2. Separate merchandising catalogCategory (e.g., ACTIVEWEAR) from functional primaryLayeringRole (e.g., OUTER_LAYER).
3. For cosmetics that are transparent/clear primers, balms, oils, soaps, or deodorants, set chromaticBehavior to TRANSLUCENT or NON_CHROMATIC. For pigmented makeup (lipsticks, blushes, eyeshadows, foundations) and all clothing, set chromaticBehavior to OPAQUE or SHEER.
"""

def extract_with_gemini(
    client: genai.Client,
    model_name: str,
    raw_item: Dict[str, Any],
    notes_item: Dict[str, Any]
) -> LlmExtractionResult:
    user_payload = json.dumps({
        "raw": raw_item,
        "notes": notes_item
    }, indent=2)

    response = client.models.generate_content(
        model=model_name,
        contents=f"Classify this KoColor catalog item:\n{user_payload}",
        config=types.GenerateContentConfig(
            system_instruction=SYSTEM_INSTRUCTION,
            response_mime_type="application/json",
            response_schema=LlmExtractionResult,
            temperature=0.0,
        ),
    )
    return LlmExtractionResult.model_validate_json(response.text)


# =====================================================================
# 6. PHASE 4 & 5: RECORD BUILDER + QUALITY GATE
# =====================================================================

def build_and_validate_record(
    item_id: str,
    raw_item: Dict[str, Any],
    notes_item: Dict[str, Any],
    llm_res: LlmExtractionResult
) -> Tuple[Optional[Dict[str, Any]], List[str]]:
    """Merges deterministic color math, rule promotions, and LLM extraction."""
    violations: List[str] = []

    hex_code = raw_item.get("hex") or raw_item.get("colorHex") or "#808080"
    try:
        color_intel = compute_cielab_d65(hex_code)
    except ValueError as e:
        violations.append(str(e))
        return None, violations

    color_intel["colorName"] = raw_item.get("colorName") or raw_item.get("name", "Unknown")
    color_intel["chromaticBehavior"] = llm_res.chromaticBehavior.value

    common_meta = {
        "itemId": item_id,
        "name": raw_item.get("name", item_id),
        "source": "INTERNAL",
        "commercial": {
            "priceUsd": float(raw_item.get("price", raw_item.get("priceUsd", 0.0))),
            "volumeOrSize": str(raw_item.get("size", raw_item.get("volume", "Standard"))),
            "sustainabilityGrade": llm_res.sustainabilityGrade.value
        },
        "color": color_intel,
        "material": {
            "composition": [
                {"material": m.material.value, "percentage": m.percentage}
                for m in llm_res.materials
            ],
            "provenance": ProvenanceType.SOURCE_VERIFIED.value if raw_item.get("composition") else ProvenanceType.LLM_SUGGESTED.value
        },
        "metadataVersion": 1,
        "ruleVersion": "1.0.0"
    }

    if llm_res.domain == DomainType.WARDROBE:
        prod_type = llm_res.productType or ProductType.UNKNOWN
        if prod_type == ProductType.UNKNOWN:
            violations.append(f"Wardrobe item {item_id} resolved to UNKNOWN productType")

        slot = PRODUCT_TYPE_TO_SLOT.get(prod_type)
        if not slot:
            violations.append(f"No deterministic GarmentSlot mapping for {prod_type}")
            slot = GarmentSlot.ACCESSORY

        rule_spec = DETERMINISTIC_GARMENT_RULES.get(prod_type, {})
        rule_occasions = set(rule_spec.get("occasions", [OccasionTag.CASUAL, OccasionTag.EVERYDAY]))
        rule_thermals = set(rule_spec.get("thermal", llm_res.thermalSuitability))

        # Merge & tag provenance on OccasionTags (Rule-Derived vs LLM-Suggested)
        all_occasions = list(dict.fromkeys(list(rule_occasions) + llm_res.occasionTags))
        occasion_entries = [
            {
                "value": occ.value,
                "provenance": (
                    ProvenanceType.RULE_DERIVED.value
                    if occ in rule_occasions
                    else ProvenanceType.LLM_SUGGESTED.value
                )
            }
            for occ in all_occasions
        ]

        if not any(o["provenance"] in ("RULE_DERIVED", "SOURCE_VERIFIED") for o in occasion_entries):
            violations.append(f"Item {item_id} lacks a verified/rule-derived occasionTag for hard filtering")

        thermal_values = [t.value for t in (rule_thermals or llm_res.thermalSuitability)]
        if not thermal_values:
            violations.append(f"Item {item_id} has empty thermalSuitability")

        t_weight = rule_spec.get("weight") or llm_res.thermalWeight or ThermalWeight.MIDWEIGHT
        insul = rule_spec.get("insulation") or llm_res.insulation or InsulationLevel.NONE
        primary_layer = rule_spec.get("layering") or llm_res.primaryLayeringRole or LayeringRole.STANDALONE

        record = {
            "domain": DomainType.WARDROBE.value,
            "commonMetadata": common_meta,
            "wardrobeMetadata": {
                "catalogCategory": (llm_res.catalogCategory or CatalogCategory.TOPS).value,
                "productType": {
                    "value": prod_type.value,
                    "provenance": ProvenanceType.RULE_DERIVED.value
                },
                "garmentSlot": {
                    "value": slot.value,
                    "provenance": ProvenanceType.RULE_DERIVED.value
                },
                "occasionTags": occasion_entries,
                "formality": {
                    "value": (llm_res.formality or FormalityLevel.CASUAL).value,
                    "provenance": ProvenanceType.LLM_SUGGESTED.value
                },
                "thermalSuitability": {
                    "values": thermal_values,
                    "provenance": ProvenanceType.RULE_DERIVED.value,
                    "derivationMethod": "GARMENT_CONSTRUCTION",
                    "ruleVersion": "thermal-rules-1"
                },
                "weatherProtection": {
                    "values": [w.value for w in llm_res.weatherProtection],
                    "provenance": ProvenanceType.LLM_SUGGESTED.value
                },
                "construction": {
                    "thermalWeight": {
                        "value": t_weight.value,
                        "provenance": ProvenanceType.RULE_DERIVED.value
                    },
                    "breathability": {
                        "value": llm_res.breathability.value if llm_res.breathability and llm_res.breathability != Breathability.UNKNOWN else None,
                        "provenance": ProvenanceType.LLM_SUGGESTED.value if llm_res.breathability and llm_res.breathability != Breathability.UNKNOWN else ProvenanceType.UNKNOWN.value
                    },
                    "insulation": {
                        "value": insul.value,
                        "provenance": ProvenanceType.RULE_DERIVED.value
                    },
                    "features": [f.value for f in llm_res.features],
                    "derivationMethod": "GARMENT_CONSTRUCTION",
                    "ruleVersion": "thermal-rules-1"
                },
                "layeringRole": {
                    "primary": primary_layer.value,
                    "compatible": [r.value for r in (llm_res.compatibleLayeringRoles or [primary_layer])],
                    "provenance": ProvenanceType.RULE_DERIVED.value
                },
                "silhouette": {
                    "value": (llm_res.silhouette or SilhouetteFit.REGULAR if hasattr(SilhouetteFit, "REGULAR") else SilhouetteFit.TAILORED).value,
                    "provenance": ProvenanceType.LLM_SUGGESTED.value
                },
                "seasons": {
                    "values": [s.value for s in llm_res.seasons],
                    "provenance": ProvenanceType.LLM_SUGGESTED.value
                },
                "activities": {
                    "values": [a.value for a in llm_res.activities],
                    "provenance": ProvenanceType.LLM_SUGGESTED.value
                }
            }
        }
        return (record if not violations else None), violations

    else:
        cos_cat = llm_res.cosmeticCategory or CosmeticCategory.PREP
        app_role = llm_res.applicationRole or CosmeticApplicationRole.OTHER
        record = {
            "domain": DomainType.COSMETICS.value,
            "commonMetadata": common_meta,
            "cosmeticsMetadata": {
                "cosmeticCategory": cos_cat.value,
                "applicationRole": app_role.value,
                "shade": llm_res.shadeName or raw_item.get("shade", "Standard"),
                "finish": {
                    "value": (llm_res.finish or CosmeticFinish.NATURAL).value,
                    "provenance": ProvenanceType.LLM_SUGGESTED.value
                },
                "formulationAttributes": {
                    "values": llm_res.formulationAttributes,
                    "provenance": ProvenanceType.LLM_SUGGESTED.value
                }
            }
        }
        return (record if not violations else None), violations


# =====================================================================
# 7. PHASE 6 & 7: RFC 8785 CANONICAL JSON, ZSTD (-19), & ED25519 SIGNING
# =====================================================================

def canonical_json_bytes(obj: Any) -> bytes:
    """Serializes JSON with deterministic key sorting and zero whitespace (RFC 8785 style)."""
    return json.dumps(obj, sort_keys=True, separators=(",", ":"), ensure_ascii=False).encode("utf-8")

def compile_and_sign_kpkg(
    pack_id: str,
    records: List[Dict[str, Any]],
    output_dir: Path,
    signing_key: SigningKey
) -> Dict[str, Any]:
    # Sort deterministically by itemId
    sorted_records = sorted(records, key=lambda r: r["commonMetadata"]["itemId"])
    uncompressed_bytes = canonical_json_bytes(sorted_records)

    # Compress with Zstandard level 19 (no --ultra to keep mobile decompression window <= 64KB)
    compressor = zstd.ZstdCompressor(level=19)
    compressed_bytes = compressor.compress(uncompressed_bytes)

    kpkg_filename = f"{pack_id}.kpkg"
    kpkg_path = output_dir / kpkg_filename
    kpkg_path.write_bytes(compressed_bytes)

    sha256_hex = hashlib.sha256(compressed_bytes).hexdigest()
    signature_hex = signing_key.sign(compressed_bytes).signature.hex()

    return {
        "id": pack_id,
        "endpoint": kpkg_filename,
        "item_count": len(sorted_records),
        "compressed_size_bytes": len(compressed_bytes),
        "uncompressed_size_bytes": len(uncompressed_bytes),
        "sha256": sha256_hex,
        "signature": signature_hex,
        "compression_algorithm": "zstd",
        "hash_algorithm": "sha256",
        "hash_encoding": "hex",
        "signature_algorithm": "ed25519",
        "signature_encoding": "hex",
        "package_format_version": 1,
        "schema_version": 1,
        "taxonomy_version": "1.0.0",
        "color_algorithm_version": COLOR_ALGO_VERSION,
        "encryption": "none"
    }


def main() -> None:
    parser = argparse.ArgumentParser(description="KoColor Catalog Tagging & .kpkg Compiler")
    parser.add_argument("--input-dir", required=True, type=Path)
    parser.add_argument("--output-dir", required=True, type=Path)
    parser.add_argument("--model", default="gemini-2.5-flash")
    args = parser.parse_args()

    api_key = os.environ.get("GEMINI_API_KEY")
    if not api_key:
        print("ERROR: GEMINI_API_KEY environment variable is required.", file=sys.stderr)
        sys.exit(1)

    seed_hex = os.environ.get("KOCOLOR_SIGNING_SEED_HEX")
    signing_key = SigningKey(bytes.fromhex(seed_hex)) if seed_hex else SigningKey.generate()

    client = genai.Client(api_key=api_key)
    args.output_dir.mkdir(parents=True, exist_ok=True)
    quarantine_dir = args.output_dir / "quarantine"
    quarantine_dir.mkdir(exist_ok=True)

    approved_records: List[Dict[str, Any]] = []
    quarantined_items: List[Dict[str, Any]] = []

    # Phase 1: Discover all kc-*.json files (excluding .notes.json and .meta.json)
    for json_path in sorted(args.input_dir.rglob("kc-*.json")):
        if json_path.name.endswith(".notes.json") or json_path.name.endswith(".meta.json"):
            continue

        item_id = json_path.stem
        notes_path = json_path.with_name(f"{item_id}.notes.json")

        raw_data = json.loads(json_path.read_text(encoding="utf-8"))
        notes_data = json.loads(notes_path.read_text(encoding="utf-8")) if notes_path.exists() else {}

        print(f"[Enriching] {item_id}...")
        llm_res = extract_with_gemini(client, args.model, raw_data, notes_data)
        record, violations = build_and_validate_record(item_id, raw_data, notes_data, llm_res)

        if record and not violations:
            approved_records.append(record)
            meta_out = json_path.with_name(f"{item_id}.meta.json")
            meta_out.write_bytes(canonical_json_bytes(record))
        else:
            q_entry = {"itemId": item_id, "violations": violations}
            quarantined_items.append(q_entry)
            (quarantine_dir / f"{item_id}.quarantine.json").write_text(
                json.dumps(q_entry, indent=2), encoding="utf-8"
            )

    # Write Quality Gate Report
    report = {
        "generated_at": datetime.now(timezone.utc).isoformat(),
        "approved_count": len(approved_records),
        "quarantined_count": len(quarantined_items),
        "quarantined_items": quarantined_items,
    }
    (args.output_dir / "quality_report.json").write_text(json.dumps(report, indent=2), encoding="utf-8")

    if quarantined_items:
        print(f"WARNING: {len(quarantined_items)} items moved to quarantine. Inspect quality_report.json.")

    # Phase 6 & 7: Compile domain packs and sign master manifest envelope
    wardrobe_records = [r for r in approved_records if r["domain"] == "WARDROBE"]
    cosmetic_records = [r for r in approved_records if r["domain"] == "COSMETICS"]

    packs_meta = []
    if wardrobe_records:
        packs_meta.append(
            compile_and_sign_kpkg("com.kocolor.pack.fashion.complete", wardrobe_records, args.output_dir, signing_key)
        )
    if cosmetic_records:
        packs_meta.append(
            compile_and_sign_kpkg("com.kocolor.pack.cosmetics.complete", cosmetic_records, args.output_dir, signing_key)
        )

    manifest_data = {
        "manifest_version": 1,
        "generated_at": datetime.now(timezone.utc).isoformat(),
        "compiler_version": "1.0.0",
        "key_id": "atelier-dev-01",
        "packs": packs_meta
    }
    envelope_sig = signing_key.sign(canonical_json_bytes(manifest_data)).signature.hex()
    full_manifest = {
        "data": manifest_data,
        "signature": envelope_sig
    }
    (args.output_dir / "manifest.json").write_text(json.dumps(full_manifest, indent=2), encoding="utf-8")
    print(f"Pipeline complete. {len(approved_records)} records compiled into {len(packs_meta)} .kpkg bundles.")

if __name__ == "__main__":
    main()

```

---

## Part 3: Android Client Codebase Updates (Kotlin)

Your coding agent should update the following two files in the Android client so the app deserializes the new `CatalogRecord` format and enforces **Section 4.1 (Provenance Policy)** and **Section 4.2 (Preflight Candidate Checks)**.

### 1. Polymorphic Provenance-Aware Models (`domain/model/CatalogRecord.kt`)

```kotlin
package com.zoewave.probase.kocolor.domain.model

import com.zoewave.probase.kocolor.domain.taxonomy.*
import kotlinx.serialization.Serializable

@Serializable
enum class ProvenanceType {
    SOURCE_VERIFIED,
    RULE_DERIVED,
    LLM_SUGGESTED,
    HUMAN_REVIEWED,
    UNKNOWN;

    /**
     * Section 4.1 Provenance Policy:
     * Only verified, human-reviewed, or deterministically rule-derived tags
     * may be used for Hard Environmental or Occasion Exclusions.
     */
    val isTrustedForHardFilter: Boolean
        get() = this == SOURCE_VERIFIED || this == HUMAN_REVIEWED || this == RULE_DERIVED
}

@Serializable
data class ProvenanceField<T>(
    val value: T? = null,
    val provenance: ProvenanceType = ProvenanceType.UNKNOWN
)

@Serializable
data class ProvenanceListField<T>(
    val values: List<T> = emptyList(),
    val provenance: ProvenanceType = ProvenanceType.UNKNOWN,
    val derivationMethod: String? = null,
    val ruleVersion: String? = null
)

@Serializable
data class CommercialMeta(
    val priceUsd: Double = 0.0,
    val volumeOrSize: String = "",
    val sustainabilityGrade: String = "UNRATED"
)

@Serializable
data class ColorMeta(
    val hex: String,
    val colorName: String = "",
    val chromaticBehavior: String = "OPAQUE",
    val hueFamily: ProvenanceField<HueFamily>,
    val colorTemperature: ProvenanceField<ColorTemperature>,
    val lStar: Double,
    val aStar: Double = 0.0,
    val bStar: Double = 0.0,
    val cStar: Double,
    val hDeg: Double,
    val colorAlgorithmVersion: String = "1.0.0",
    val colorClassificationRuleVersion: String = "1.0.0"
)

@Serializable
data class MaterialEntry(
    val material: String,
    val percentage: Int
)

@Serializable
data class MaterialMeta(
    val composition: List<MaterialEntry> = emptyList(),
    val provenance: ProvenanceType = ProvenanceType.UNKNOWN
)

@Serializable
data class CommonMetadata(
    val itemId: String,
    val name: String,
    val source: String = "INTERNAL",
    val commercial: CommercialMeta = CommercialMeta(),
    val color: ColorMeta,
    val material: MaterialMeta = MaterialMeta(),
    val metadataVersion: Int = 1,
    val ruleVersion: String = "1.0.0"
)

@Serializable
data class ConstructionMeta(
    val thermalWeight: ProvenanceField<ThermalWeight>,
    val breathability: ProvenanceField<Breathability> = ProvenanceField(),
    val insulation: ProvenanceField<InsulationLevel>,
    val features: List<String> = emptyList(),
    val derivationMethod: String? = null,
    val ruleVersion: String? = null
)

@Serializable
data class LayeringRoleMeta(
    val primary: LayeringRole,
    val compatible: List<LayeringRole> = emptyList(),
    val provenance: ProvenanceType = ProvenanceType.RULE_DERIVED
)

@Serializable
data class WardrobeMetadata(
    val catalogCategory: String,
    val productType: ProvenanceField<ProductType>,
    val garmentSlot: ProvenanceField<String>, // TOP, BOTTOM, OUTERWEAR, ONE_PIECE, FOOTWEAR, ACCESSORY
    val occasionTags: List<ProvenanceField<OccasionTag>> = emptyList(),
    val formality: ProvenanceField<FormalityLevel>,
    val thermalSuitability: ProvenanceListField<ThermalBand>,
    val weatherProtection: ProvenanceListField<WeatherProtection> = ProvenanceListField(),
    val construction: ConstructionMeta,
    val layeringRole: LayeringRoleMeta,
    val silhouette: ProvenanceField<SilhouetteFit>,
    val seasons: ProvenanceListField<SeasonTag> = ProvenanceListField(),
    val activities: ProvenanceListField<ActivityTag> = ProvenanceListField()
)

@Serializable
data class CosmeticsMetadata(
    val cosmeticCategory: String, // EYE, CHEEK, LIP, NAIL, PREP, COMPLEXION, HAIR, BODY
    val applicationRole: String,
    val shade: String = "",
    val finish: ProvenanceField<String> = ProvenanceField(),
    val formulationAttributes: ProvenanceListField<String> = ProvenanceListField()
)

@Serializable
data class CatalogRecord(
    val domain: String, // "WARDROBE" or "COSMETICS"
    val commonMetadata: CommonMetadata,
    val wardrobeMetadata: WardrobeMetadata? = null,
    val cosmeticsMetadata: CosmeticsMetadata? = null
)

```

---

### 2. Provenance-Enforcing Engine & Compact Manifest Serializer (`engine/DeterministicCatalogEngine.kt`)

```kotlin
package com.zoewave.probase.kocolor.engine

import com.zoewave.probase.kocolor.domain.model.CatalogRecord
import com.zoewave.probase.kocolor.domain.taxonomy.*
import kotlin.math.abs

class DeterministicCatalogEngine {

    companion object {
        val ALLOWED_AI_COSMETIC_ROLES = setOf("EYE", "CHEEK", "LIP", "NAIL")
        val CHROMATIC_BEHAVIORS = setOf("OPAQUE", "SHEER")
    }

    /**
     * Filters and ranks Wardrobe & Cosmetic items while strictly enforcing:
     * 1. Provenance Policy (Hard filters only evaluate trusted provenance).
     * 2. Preflight Slot Completeness (Halts before calling Gemini if any required slot is empty).
     * 3. Category & Chromatic Isolation for Cosmetics.
     */
    fun prepareCandidatesForGemini(
        allRecords: List<CatalogRecord>,
        tempCelsius: Double,
        isRaining: Boolean,
        targetOccasion: OccasionTag,
        targetFormality: FormalityLevel,
        requiredWardrobeSlots: Set<String> = setOf("TOP", "BOTTOM", "FOOTWEAR"),
        topKPerSlot: Int = 6
    ): EnginePreflightResult {
        val activeBand = resolveThermalBand(tempCelsius)
        val allowOuterwear = activeBand !in setOf(ThermalBand.HOT, ThermalBand.WARM) || isRaining

        // --- 1. WARDROBE HARD FILTERING (Trusted Provenance Only) ---
        val eligibleWardrobe = allRecords
            .filter { it.domain == "WARDROBE" && it.wardrobeMetadata != null }
            .filter { record ->
                val w = record.wardrobeMetadata!!
                val slot = w.garmentSlot.value ?: return@filter false

                // Hard Rule A: Outerwear banned in hot/warm dry weather
                if (slot == "OUTERWEAR" && !allowOuterwear) return@filter false

                // Hard Rule B: Thermal suitability (only enforced if provenance is trusted)
                if (w.thermalSuitability.provenance.isTrustedForHardFilter) {
                    if (activeBand !in w.thermalSuitability.values) return@filter false
                }

                // Hard Rule C: Occasion check against trusted occasion tags
                val trustedOccasions = w.occasionTags
                    .filter { it.provenance.isTrustedForHardFilter }
                    .mapNotNull { it.value }
                if (trustedOccasions.isNotEmpty() && targetOccasion !in trustedOccasions) {
                    return@filter false
                }

                true
            }

        // --- 2. PREFLIGHT SLOT COMPLETENESS GATE ---
        val groupedBySlot = eligibleWardrobe.groupBy { it.wardrobeMetadata!!.garmentSlot.value!! }
        val missingSlots = requiredWardrobeSlots.filter { groupedBySlot[it].isNullOrEmpty() }.toSet()

        if (missingSlots.isNotEmpty()) {
            return EnginePreflightResult.AbortedMissingSlots(
                missingWardrobeSlots = missingSlots,
                initialWardrobeCount = allRecords.count { it.domain == "WARDROBE" }
            )
        }

        // --- 3. SOFT PREFERENCE RANKING (Includes LLM_SUGGESTED Tags) ---
        val rankedWardrobeBySlot = groupedBySlot.mapValues { (_, records) ->
            records.sortedByDescending { scoreWardrobeSoftPreferences(it, targetOccasion, targetFormality) }
                .take(topKPerSlot)
        }

        // --- 4. COSMETIC CATEGORY & CHROMATIC ISOLATION ---
        val eligibleCosmetics = allRecords
            .filter { it.domain == "COSMETICS" && it.cosmeticsMetadata != null }
            .filter { record ->
                val c = record.cosmeticsMetadata!!
                val behavior = record.commonMetadata.color.chromaticBehavior
                // Strictly forbid PREP, HAIR, COMPLEXION and non-chromatic items
                c.cosmeticCategory in ALLOWED_AI_COSMETIC_ROLES && behavior in CHROMATIC_BEHAVIORS
            }

        return EnginePreflightResult.Ready(
            wardrobeBySlot = rankedWardrobeBySlot,
            cosmeticsByRole = eligibleCosmetics.groupBy { it.cosmeticsMetadata!!.cosmeticCategory },
            compactManifestText = buildGroundedManifestPrompt(rankedWardrobeBySlot, eligibleCosmetics)
        )
    }

    private fun scoreWardrobeSoftPreferences(
        record: CatalogRecord,
        targetOccasion: OccasionTag,
        targetFormality: FormalityLevel
    ): Double {
        val w = record.wardrobeMetadata ?: return 0.0
        var score = 50.0

        // Soft Occasion boost (evaluates LLM_SUGGESTED tags)
        if (w.occasionTags.any { it.value == targetOccasion }) {
            score += 20.0
        }

        // Soft Formality alignment
        val itemFormality = w.formality.value ?: FormalityLevel.CASUAL
        val diff = abs(itemFormality.rank - targetFormality.rank)
        score += (20.0 - diff * 7.0).coerceAtLeast(0.0)

        return score
    }

    /**
     * Serializes the Compact Manifest with explicit physical materials and construction features
     * so Gemini obeys Strict Grounding Rule #1.
     */
    private fun buildGroundedManifestPrompt(
        wardrobeBySlot: Map<String, List<CatalogRecord>>,
        cosmetics: List<CatalogRecord>
    ): String = buildString {
        appendLine("WARDROBE:")
        wardrobeBySlot.values.flatten().forEach { r ->
            val c = r.commonMetadata
            val w = r.wardrobeMetadata!!
            val materials = c.material.composition.joinToString(", ") { "${it.percentage}% ${it.material}" }
            val features = w.construction.features.joinToString(", ")
            appendLine(
                "- ID: ${c.itemId} | Slot: ${w.garmentSlot.value} | Name: ${c.name} | " +
                "Hex: ${c.color.hex} (${c.color.hueFamily.value}) | " +
                "Materials: [$materials] | Features: [$features]"
            )
        }
        appendLine("\nCOSMETICS:")
        cosmetics.forEach { r ->
            val c = r.commonMetadata
            val cos = r.cosmeticsMetadata!!
            appendLine(
                "- ID: ${c.itemId} | Role: ${cos.cosmeticCategory} | Name: ${c.name} | " +
                "Shade: ${cos.shade} | Hex: ${c.color.hex} | Finish: ${cos.finish.value}"
            )
        }
    }

    private fun resolveThermalBand(tempC: Double): ThermalBand =
        ThermalBand.entries.firstOrNull { tempC >= it.minCelsius && tempC < it.maxCelsius }
            ?: ThermalBand.MILD
}

sealed class EnginePreflightResult {
    data class Ready(
        val wardrobeBySlot: Map<String, List<CatalogRecord>>,
        val cosmeticsByRole: Map<String, List<CatalogRecord>>,
        val compactManifestText: String
    ) : EnginePreflightResult()

    data class AbortedMissingSlots(
        val missingWardrobeSlots: Set<String>,
        val initialWardrobeCount: Int
    ) : EnginePreflightResult()
}

```