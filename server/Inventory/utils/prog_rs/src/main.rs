use anyhow::{anyhow, Context, Result};
use chrono::Utc;
use clap::Parser;
use ed25519_dalek::{Signer, SigningKey};
use rand::rngs::OsRng;
use serde::{Deserialize, Serialize};
use serde_json::{json, Map, Value};
use sha2::{Digest, Sha256};
use std::collections::{BTreeMap, HashSet};
use std::env;
use std::fs;
use std::path::{Path, PathBuf};
use walkdir::WalkDir;

const COLOR_ALGO_VERSION: &str = "1.0.0";
const COLOR_RULE_VERSION: &str = "1.0.0";

const SYSTEM_INSTRUCTION: &str = r#"You are the KoColor Catalog Taxonomy & Metadata Compiler Agent.
Transform raw product descriptions into strictly validated enums for the KoColor styling engine.

STRICT GROUNDING RULES:
1. Extract ONLY materials and hardware features explicitly mentioned in the input text.
2. Separate merchandising catalogCategory (e.g., ACTIVEWEAR) from functional primaryLayeringRole (e.g., OUTER_LAYER).
3. For cosmetics that are transparent/clear primers, balms, oils, soaps, or deodorants, set chromaticBehavior to TRANSLUCENT or NON_CHROMATIC. For pigmented makeup (lipsticks, blushes, eyeshadows, foundations) and all clothing, set chromaticBehavior to OPAQUE or SHEER."#;

// =====================================================================
// 1. CONTROLLED VOCABULARY ENUMS
// =====================================================================

#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash, Serialize, Deserialize)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum DomainType {
    Wardrobe,
    Cosmetics,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash, Serialize, Deserialize)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum ProvenanceType {
    SourceVerified,
    RuleDerived,
    LlmSuggested,
    HumanReviewed,
    Unknown,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash, Serialize, Deserialize)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum SustainabilityGrade {
    GradeA,
    GradeB,
    GradeC,
    Unrated,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash, Serialize, Deserialize)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum ChromaticBehavior {
    Opaque,
    Sheer,
    Translucent,
    NonChromatic,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash, Serialize, Deserialize)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum HueFamily {
    Red,
    Orange,
    Yellow,
    Green,
    Cyan,
    Blue,
    Purple,
    Magenta,
    Neutral,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash, Serialize, Deserialize)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum ColorTemperature {
    Warm,
    Cool,
    Neutral,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash, Serialize, Deserialize)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum CatalogCategory {
    Tops,
    Bottoms,
    Outerwear,
    Activewear,
    Footwear,
    OnePiece,
    Accessories,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash, Serialize, Deserialize)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum GarmentSlot {
    Top,
    Bottom,
    Outerwear,
    OnePiece,
    Footwear,
    Accessory,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash, Serialize, Deserialize)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum ProductType {
    CropTop,
    TShirt,
    TankTop,
    Blouse,
    ButtonDown,
    Polo,
    Sweater,
    Cardigan,
    Hoodie,
    Sweatshirt,
    Bodysuit,
    Trousers,
    Chinos,
    Jeans,
    Shorts,
    Skirt,
    Leggings,
    Joggers,
    TrackPants,
    Dress,
    Jumpsuit,
    Romper,
    Sneakers,
    RunningShoes,
    Boots,
    AnkleBoots,
    Loafers,
    Oxfords,
    Heels,
    Flats,
    Sandals,
    Slides,
    TrackJacket,
    Windbreaker,
    TrenchCoat,
    LeatherJacket,
    WoolOvercoat,
    Blazer,
    DenimJacket,
    BomberJacket,
    Puffer,
    Parka,
    Vest,
    Bag,
    Belt,
    Hat,
    Scarf,
    Sunglasses,
    Watch,
    Jewelry,
    Unknown,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash, Serialize, Deserialize)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum OccasionTag {
    Casual,
    Everyday,
    Work,
    Evening,
    Travel,
    FormalEvent,
    Active,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash, Serialize, Deserialize)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum FormalityLevel {
    Casual,
    SmartCasual,
    Business,
    Formal,
    Gala,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash, Serialize, Deserialize)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum ThermalBand {
    Hot,
    Warm,
    Mild,
    Cool,
    Cold,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash, Serialize, Deserialize)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum WeatherProtection {
    RainResistant,
    WindResistant,
    SunCoverage,
    ThermalRetention,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash, Serialize, Deserialize)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum ThermalWeight {
    Light,
    Midweight,
    Heavyweight,
    Unknown,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash, Serialize, Deserialize)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum Breathability {
    Low,
    Medium,
    High,
    Unknown,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash, Serialize, Deserialize)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum InsulationLevel {
    None,
    Light,
    Medium,
    Heavy,
    Unknown,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash, Serialize, Deserialize)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum ConstructionFeature {
    RibbedCuffs,
    RibbedHem,
    ZipPockets,
    SidePockets,
    PatchPockets,
    FullZip,
    HalfZip,
    ButtonFront,
    DoubleBreasted,
    DrawstringWaist,
    ElasticWaist,
    Belted,
    Hooded,
    Collared,
    Crewneck,
    VNeck,
    MockNeck,
    Turtleneck,
    Pleated,
    CuffedHem,
    WaterRepellentFinish,
    SeamSealed,
    Vented,
    Lined,
    Unlined,
    StretchKnit,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash, Serialize, Deserialize)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum LayeringRole {
    BaseLayer,
    MidLayer,
    OuterLayer,
    Standalone,
    Footwear,
    Accessory,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash, Serialize, Deserialize)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum SilhouetteFit {
    Fitted,
    Tailored,
    Relaxed,
    Oversized,
    Unknown,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash, Serialize, Deserialize)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum SeasonTag {
    Spring,
    Summer,
    Autumn,
    Winter,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash, Serialize, Deserialize)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum ActivityTag {
    Office,
    Walking,
    Cycling,
    Exercise,
    Lounge,
    Dining,
    Commute,
    Social,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash, Serialize, Deserialize)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum MaterialFiber {
    OrganicCotton,
    Cotton,
    Linen,
    Silk,
    MerinoWool,
    Wool,
    Cashmere,
    Alpaca,
    Polyester,
    TricotPolyester,
    RecycledPolyester,
    Nylon,
    RecycledNylon,
    Spandex,
    Elastane,
    Viscose,
    Rayon,
    TencelLyocell,
    Modal,
    Acrylic,
    Leather,
    Suede,
    VeganLeather,
    GoreTex,
    Denim,
    Canvas,
    Fleece,
    Down,
    SyntheticFill,
    Rubber,
    EvaFoam,
    Mesh,
    CosmeticFormulation,
    Unknown,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash, Serialize, Deserialize)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum CosmeticCategory {
    Eye,
    Cheek,
    Lip,
    Nail,
    Prep,
    Complexion,
    Hair,
    Body,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash, Serialize, Deserialize)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum CosmeticApplicationRole {
    Lipstick,
    LipGloss,
    LipBalm,
    LipLiner,
    Eyeshadow,
    Eyeliner,
    Mascara,
    Brow,
    Blush,
    Bronzer,
    Highlighter,
    NailLacquer,
    Primer,
    Foundation,
    Concealer,
    SettingSpray,
    Cleanser,
    Moisturizer,
    Serum,
    Sunscreen,
    Aftershave,
    BeardOil,
    Deodorant,
    BodyWash,
    Other,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Hash, Serialize, Deserialize)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum CosmeticFinish {
    Matte,
    Satin,
    Natural,
    Luminous,
    HighShine,
    Metallic,
    Sheer,
    Unknown,
}

// =====================================================================
// 2. STRUCTURED EXTRACTION MODEL FOR GEMINI (PHASE 3)
// =====================================================================

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct MaterialComponentLLM {
    pub material: MaterialFiber,
    pub percentage: u8,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct LlmExtractionResult {
    pub domain: DomainType,
    pub chromatic_behavior: ChromaticBehavior,
    pub sustainability_grade: SustainabilityGrade,
    #[serde(default)]
    pub materials: Vec<MaterialComponentLLM>,

    // Wardrobe-specific fields
    pub catalog_category: Option<CatalogCategory>,
    pub product_type: Option<ProductType>,
    #[serde(default)]
    pub occasion_tags: Vec<OccasionTag>,
    pub formality: Option<FormalityLevel>,
    #[serde(default)]
    pub thermal_suitability: Vec<ThermalBand>,
    #[serde(default)]
    pub weather_protection: Vec<WeatherProtection>,
    pub thermal_weight: Option<ThermalWeight>,
    pub breathability: Option<Breathability>,
    pub insulation: Option<InsulationLevel>,
    #[serde(default)]
    pub features: Vec<ConstructionFeature>,
    pub primary_layering_role: Option<LayeringRole>,
    #[serde(default)]
    pub compatible_layering_roles: Vec<LayeringRole>,
    pub silhouette: Option<SilhouetteFit>,
    #[serde(default)]
    pub seasons: Vec<SeasonTag>,
    #[serde(default)]
    pub activities: Vec<ActivityTag>,

    // Cosmetics-specific fields
    pub cosmetic_category: Option<CosmeticCategory>,
    pub application_role: Option<CosmeticApplicationRole>,
    pub shade_name: Option<String>,
    pub finish: Option<CosmeticFinish>,
    #[serde(default)]
    pub formulation_attributes: Vec<String>,
}

// =====================================================================
// 3. PHASE 2: DETERMINISTIC D65 CIELAB / LCH COLOR TELEMETRY
// =====================================================================

fn srgb_channel_to_linear(c: f64) -> f64 {
    if c <= 0.04045 {
        c / 12.92
    } else {
        ((c + 0.055) / 1.055).powf(2.4)
    }
}

fn round_2(val: f64) -> f64 {
    (val * 100.0).round() / 100.0
}

fn round_1(val: f64) -> f64 {
    (val * 10.0).round() / 10.0
}

pub fn compute_cielab_d65(hex_str: &str) -> Result<Value> {
    let clean_hex = hex_str.trim().trim_start_matches('#').to_uppercase();
    if clean_hex.len() != 6 || !clean_hex.chars().all(|c| c.is_ascii_hexdigit()) {
        return Err(anyhow!("Invalid HEX color string: {}", hex_str));
    }

    let r_u8 = u8::from_str_radix(&clean_hex[0..2], 16)?;
    let g_u8 = u8::from_str_radix(&clean_hex[2..4], 16)?;
    let b_u8 = u8::from_str_radix(&clean_hex[4..6], 16)?;

    let r = srgb_channel_to_linear(r_u8 as f64 / 255.0);
    let g = srgb_channel_to_linear(g_u8 as f64 / 255.0);
    let b = srgb_channel_to_linear(b_u8 as f64 / 255.0);

    // sRGB to XYZ (D65 reference white, 2-degree observer)
    let x = r * 0.4124564 + g * 0.3575761 + b * 0.1804375;
    let y = r * 0.2126729 + g * 0.7151522 + b * 0.0721750;
    let z = r * 0.0193339 + g * 0.1191920 + b * 0.9503041;

    // Normalize by D65 White Point (Xn=0.95047, Yn=1.00000, Zn=1.08883)
    let xr = x / 0.95047;
    let yr = y / 1.00000;
    let zr = z / 1.08883;

    let epsilon = 216.0 / 24389.0;
    let kappa = 24389.0 / 27.0;

    let f = |t: f64| -> f64 {
        if t > epsilon {
            t.cbrt()
        } else {
            (kappa * t + 16.0) / 116.0
        }
    };

    let fx = f(xr);
    let fy = f(yr);
    let fz = f(zr);

    let l_star = 116.0 * fy - 16.0;
    let a_star = 500.0 * (fx - fy);
    let b_star = 200.0 * (fy - fz);

    let c_star = (a_star.powi(2) + b_star.powi(2)).sqrt();
    let h_rad = b_star.atan2(a_star);
    let h_deg = h_rad.to_degrees().rem_euclid(360.0);

    // Deterministic Hue Family Classification
    let hue_family = if c_star < 8.5 {
        HueFamily::Neutral
    } else if !(25.0..345.0).contains(&h_deg) {
        HueFamily::Red
    } else if h_deg < 60.0 {
        HueFamily::Orange
    } else if h_deg < 105.0 {
        HueFamily::Yellow
    } else if h_deg < 165.0 {
        HueFamily::Green
    } else if h_deg < 210.0 {
        HueFamily::Cyan
    } else if h_deg < 275.0 {
        HueFamily::Blue
    } else if h_deg < 315.0 {
        HueFamily::Purple
    } else {
        HueFamily::Magenta
    };

    // Deterministic Color Temperature Classification
    let color_temp = if c_star < 6.0 {
        ColorTemperature::Neutral
    } else if (15.0..=115.0).contains(&h_deg) {
        ColorTemperature::Warm
    } else if (185.0..=305.0).contains(&h_deg) {
        ColorTemperature::Cool
    } else if b_star > 4.0 {
        ColorTemperature::Warm
    } else if b_star < -4.0 {
        ColorTemperature::Cool
    } else {
        ColorTemperature::Neutral
    };

    Ok(json!({
        "hex": format!("#{}", clean_hex),
        "hueFamily": {
            "value": hue_family,
            "provenance": ProvenanceType::RuleDerived
        },
        "colorTemperature": {
            "value": color_temp,
            "provenance": ProvenanceType::RuleDerived
        },
        "lStar": round_2(l_star),
        "aStar": round_2(a_star),
        "bStar": round_2(b_star),
        "cStar": round_2(c_star),
        "hDeg": round_1(h_deg),
        "colorAlgorithmVersion": COLOR_ALGO_VERSION,
        "colorClassificationRuleVersion": COLOR_RULE_VERSION
    }))
}

// =====================================================================
// 4. PHASE 4: DETERMINISTIC RULE PROMOTION MATRIX
// =====================================================================

pub fn product_type_to_slot(pt: ProductType) -> Option<GarmentSlot> {
    use GarmentSlot::*;
    use ProductType::*;
    match pt {
        CropTop | TShirt | TankTop | Blouse | ButtonDown | Polo | Sweater | Cardigan | Hoodie
        | Sweatshirt | Bodysuit => Some(Top),
        Trousers | Chinos | Jeans | Shorts | Skirt | Leggings | Joggers | TrackPants => {
            Some(Bottom)
        }
        Dress | Jumpsuit | Romper => Some(OnePiece),
        Sneakers | RunningShoes | Boots | AnkleBoots | Loafers | Oxfords | Heels | Flats
        | Sandals | Slides => Some(Footwear),
        TrackJacket | Windbreaker | TrenchCoat | LeatherJacket | WoolOvercoat | Blazer
        | DenimJacket | BomberJacket | Puffer | Parka | Vest => Some(Outerwear),
        Bag | Belt | Hat | Scarf | Sunglasses | Watch | Jewelry => Some(Accessory),
        Unknown => None,
    }
}

pub struct GarmentRuleSpec {
    pub thermal: Vec<ThermalBand>,
    pub occasions: Vec<OccasionTag>,
    pub weight: ThermalWeight,
    pub insulation: InsulationLevel,
    pub layering: LayeringRole,
}

pub fn deterministic_garment_rules(pt: ProductType) -> Option<GarmentRuleSpec> {
    use LayeringRole::*;
    use OccasionTag::*;
    use ProductType::*;
    use ThermalBand::*;

    match pt {
        CropTop => Some(GarmentRuleSpec {
            thermal: vec![Hot, Warm],
            occasions: vec![Casual, Everyday],
            weight: ThermalWeight::Light,
            insulation: InsulationLevel::None,
            layering: Standalone,
        }),
        TShirt => Some(GarmentRuleSpec {
            thermal: vec![Hot, Warm, Mild],
            occasions: vec![Casual, Everyday],
            weight: ThermalWeight::Light,
            insulation: InsulationLevel::None,
            layering: BaseLayer,
        }),
        TrackJacket => Some(GarmentRuleSpec {
            thermal: vec![Mild, Cool],
            occasions: vec![Active, Casual, Everyday],
            weight: ThermalWeight::Midweight,
            insulation: InsulationLevel::None,
            layering: OuterLayer,
        }),
        TrenchCoat => Some(GarmentRuleSpec {
            thermal: vec![Mild, Cool],
            occasions: vec![Work, Everyday, Travel],
            weight: ThermalWeight::Midweight,
            insulation: InsulationLevel::Light,
            layering: OuterLayer,
        }),
        LeatherJacket => Some(GarmentRuleSpec {
            thermal: vec![Mild, Cool, Cold],
            occasions: vec![Casual, Evening, Everyday],
            weight: ThermalWeight::Heavyweight,
            insulation: InsulationLevel::Light,
            layering: OuterLayer,
        }),
        WoolOvercoat => Some(GarmentRuleSpec {
            thermal: vec![Cool, Cold],
            occasions: vec![Work, FormalEvent, Evening],
            weight: ThermalWeight::Heavyweight,
            insulation: InsulationLevel::Medium,
            layering: OuterLayer,
        }),
        _ => Option::None,
    }
}

// =====================================================================
// 5. PHASE 3: GEMINI STRUCTURED EXTRACTION CLIENT
// =====================================================================

fn gemini_response_schema() -> Value {
    json!({
        "type": "OBJECT",
        "required": ["domain", "chromaticBehavior", "sustainabilityGrade", "materials"],
        "properties": {
            "domain": { "type": "STRING", "enum": ["WARDROBE", "COSMETICS"] },
            "chromaticBehavior": { "type": "STRING", "enum": ["OPAQUE", "SHEER", "TRANSLUCENT", "NON_CHROMATIC"] },
            "sustainabilityGrade": { "type": "STRING", "enum": ["GRADE_A", "GRADE_B", "GRADE_C", "UNRATED"] },
            "materials": {
                "type": "ARRAY",
                "items": {
                    "type": "OBJECT",
                    "required": ["material", "percentage"],
                    "properties": {
                        "material": {
                            "type": "STRING",
                            "enum": [
                                "ORGANIC_COTTON", "COTTON", "LINEN", "SILK", "MERINO_WOOL", "WOOL",
                                "CASHMERE", "ALPACA", "POLYESTER", "TRICOT_POLYESTER", "RECYCLED_POLYESTER",
                                "NYLON", "RECYCLED_NYLON", "SPANDEX", "ELASTANE", "VISCOSE", "RAYON",
                                "TENCEL_LYOCELL", "MODAL", "ACRYLIC", "LEATHER", "SUEDE", "VEGAN_LEATHER",
                                "GORE_TEX", "DENIM", "CANVAS", "FLEECE", "DOWN", "SYNTHETIC_FILL",
                                "RUBBER", "EVA_FOAM", "MESH", "COSMETIC_FORMULATION", "UNKNOWN"
                            ]
                        },
                        "percentage": { "type": "INTEGER" }
                    }
                }
            },
            "catalogCategory": {
                "type": "STRING",
                "nullable": true,
                "enum": ["TOPS", "BOTTOMS", "OUTERWEAR", "ACTIVEWEAR", "FOOTWEAR", "ONE_PIECE", "ACCESSORIES"]
            },
            "productType": {
                "type": "STRING",
                "nullable": true,
                "enum": [
                    "CROP_TOP", "T_SHIRT", "TANK_TOP", "BLOUSE", "BUTTON_DOWN", "POLO", "SWEATER",
                    "CARDIGAN", "HOODIE", "SWEATSHIRT", "BODYSUIT", "TROUSERS", "CHINOS", "JEANS",
                    "SHORTS", "SKIRT", "LEGGINGS", "JOGGERS", "TRACK_PANTS", "DRESS", "JUMPSUIT",
                    "ROMPER", "SNEAKERS", "RUNNING_SHOES", "BOOTS", "ANKLE_BOOTS", "LOAFERS",
                    "OXFORDS", "HEELS", "FLATS", "SANDALS", "SLIDES", "TRACK_JACKET", "WINDBREAKER",
                    "TRENCH_COAT", "LEATHER_JACKET", "WOOL_OVERCOAT", "BLAZER", "DENIM_JACKET",
                    "BOMBER_JACKET", "PUFFER", "PARKA", "VEST", "BAG", "BELT", "HAT", "SCARF",
                    "SUNGLASSES", "WATCH", "JEWELRY", "UNKNOWN"
                ]
            },
            "occasionTags": {
                "type": "ARRAY",
                "items": {
                    "type": "STRING",
                    "enum": ["CASUAL", "EVERYDAY", "WORK", "EVENING", "TRAVEL", "FORMAL_EVENT", "ACTIVE"]
                }
            },
            "formality": {
                "type": "STRING",
                "nullable": true,
                "enum": ["CASUAL", "SMART_CASUAL", "BUSINESS", "FORMAL", "GALA"]
            },
            "thermalSuitability": {
                "type": "ARRAY",
                "items": { "type": "STRING", "enum": ["HOT", "WARM", "MILD", "COOL", "COLD"] }
            },
            "weatherProtection": {
                "type": "ARRAY",
                "items": {
                    "type": "STRING",
                    "enum": ["RAIN_RESISTANT", "WIND_RESISTANT", "SUN_COVERAGE", "THERMAL_RETENTION"]
                }
            },
            "thermalWeight": {
                "type": "STRING",
                "nullable": true,
                "enum": ["LIGHT", "MIDWEIGHT", "HEAVYWEIGHT", "UNKNOWN"]
            },
            "breathability": {
                "type": "STRING",
                "nullable": true,
                "enum": ["LOW", "MEDIUM", "HIGH", "UNKNOWN"]
            },
            "insulation": {
                "type": "STRING",
                "nullable": true,
                "enum": ["NONE", "LIGHT", "MEDIUM", "HEAVY", "UNKNOWN"]
            },
            "features": {
                "type": "ARRAY",
                "items": {
                    "type": "STRING",
                    "enum": [
                        "RIBBED_CUFFS", "RIBBED_HEM", "ZIP_POCKETS", "SIDE_POCKETS", "PATCH_POCKETS",
                        "FULL_ZIP", "HALF_ZIP", "BUTTON_FRONT", "DOUBLE_BREASTED", "DRAWSTRING_WAIST",
                        "ELASTIC_WAIST", "BELTED", "HOODED", "COLLARED", "CREWNECK", "V_NECK",
                        "MOCK_NECK", "TURTLENECK", "PLEATED", "CUFFED_HEM", "WATER_REPELLENT_FINISH",
                        "SEAM_SEALED", "VENTED", "LINED", "UNLINED", "STRETCH_KNIT"
                    ]
                }
            },
            "primaryLayeringRole": {
                "type": "STRING",
                "nullable": true,
                "enum": ["BASE_LAYER", "MID_LAYER", "OUTER_LAYER", "STANDALONE", "FOOTWEAR", "ACCESSORY"]
            },
            "compatibleLayeringRoles": {
                "type": "ARRAY",
                "items": {
                    "type": "STRING",
                    "enum": ["BASE_LAYER", "MID_LAYER", "OUTER_LAYER", "STANDALONE", "FOOTWEAR", "ACCESSORY"]
                }
            },
            "silhouette": {
                "type": "STRING",
                "nullable": true,
                "enum": ["FITTED", "TAILORED", "RELAXED", "OVERSIZED", "UNKNOWN"]
            },
            "seasons": {
                "type": "ARRAY",
                "items": { "type": "STRING", "enum": ["SPRING", "SUMMER", "AUTUMN", "WINTER"] }
            },
            "activities": {
                "type": "ARRAY",
                "items": {
                    "type": "STRING",
                    "enum": ["OFFICE", "WALKING", "CYCLING", "EXERCISE", "LOUNGE", "DINING", "COMMUTE", "SOCIAL"]
                }
            },
            "cosmeticCategory": {
                "type": "STRING",
                "nullable": true,
                "enum": ["EYE", "CHEEK", "LIP", "NAIL", "PREP", "COMPLEXION", "HAIR", "BODY"]
            },
            "applicationRole": {
                "type": "STRING",
                "nullable": true,
                "enum": [
                    "LIPSTICK", "LIP_GLOSS", "LIP_BALM", "LIP_LINER", "EYESHADOW", "EYELINER",
                    "MASCARA", "BROW", "BLUSH", "BRONZER", "HIGHLIGHTER", "NAIL_LACQUER",
                    "PRIMER", "FOUNDATION", "CONCEALER", "SETTING_SPRAY", "CLEANSER",
                    "MOISTURIZER", "SERUM", "SUNSCREEN", "AFTERSHAVE", "BEARD_OIL",
                    "DEODORANT", "BODY_WASH", "OTHER"
                ]
            },
            "shadeName": { "type": "STRING", "nullable": true },
            "finish": {
                "type": "STRING",
                "nullable": true,
                "enum": ["MATTE", "SATIN", "NATURAL", "LUMINOUS", "HIGH_SHINE", "METALLIC", "SHEER", "UNKNOWN"]
            },
            "formulationAttributes": {
                "type": "ARRAY",
                "items": { "type": "STRING" }
            }
        }
    })
}

async fn extract_with_gemini(
    http_client: &reqwest::Client,
    api_key: &str,
    model_name: &str,
    raw_item: &Value,
    notes_item: &Value,
) -> Result<LlmExtractionResult> {
    let url = format!(
        "https://generativelanguage.googleapis.com/v1beta/models/{}:generateContent?key={}",
        model_name, api_key
    );

    let user_payload = serde_json::to_string_pretty(&json!({
        "raw": raw_item,
        "notes": notes_item
    }))?;

    let req_body = json!({
        "systemInstruction": {
            "parts": [{ "text": SYSTEM_INSTRUCTION }]
        },
        "contents": [{
            "role": "user",
            "parts": [{ "text": format!("Classify this KoColor catalog item:\n{}", user_payload) }]
        }],
        "generationConfig": {
            "temperature": 0.0,
            "responseMimeType": "application/json",
            "responseSchema": gemini_response_schema()
        }
    });

    let resp = http_client
        .post(&url)
        .json(&req_body)
        .send()
        .await
        .context("Failed to call Gemini API")?;

    let status = resp.status();
    let body_text = resp.text().await?;
    if !status.is_success() {
        return Err(anyhow!("Gemini API HTTP {}: {}", status, body_text));
    }

    let resp_json: Value = serde_json::from_str(&body_text)?;
    let candidate_text = resp_json["candidates"][0]["content"]["parts"][0]["text"]
        .as_str()
        .ok_or_else(|| anyhow!("Missing candidate text in Gemini response: {}", body_text))?;

    let parsed: LlmExtractionResult = serde_json::from_str(candidate_text)
        .with_context(|| format!("Failed to parse LlmExtractionResult JSON: {}", candidate_text))?;

    Ok(parsed)
}

// =====================================================================
// 6. PHASE 4 & 5: RECORD BUILDER + QUALITY GATE
// =====================================================================

pub fn build_and_validate_record(
    item_id: &str,
    raw_item: &Value,
    _notes_item: &Value,
    llm_res: &LlmExtractionResult,
) -> (Option<Value>, Vec<String>) {
    let mut violations: Vec<String> = Vec::new();

    let hex_code = raw_item
        .get("hex")
        .or_else(|| raw_item.get("colorHex"))
        .and_then(|v| v.as_str())
        .unwrap_or("#808080");

    let mut color_intel = match compute_cielab_d65(hex_code) {
        Ok(c) => c,
        Err(e) => {
            violations.push(e.to_string());
            return (None, violations);
        }
    };

    let color_name = raw_item
        .get("colorName")
        .or_else(|| raw_item.get("name"))
        .and_then(|v| v.as_str())
        .unwrap_or("Unknown");

    if let Some(obj) = color_intel.as_object_mut() {
        obj.insert("colorName".to_string(), json!(color_name));
        obj.insert("chromaticBehavior".to_string(), json!(llm_res.chromatic_behavior));
    }

    let price_usd = raw_item
        .get("price")
        .or_else(|| raw_item.get("priceUsd"))
        .and_then(|v| v.as_f64())
        .unwrap_or(0.0);

    let volume_or_size = raw_item
        .get("size")
        .or_else(|| raw_item.get("volume"))
        .and_then(|v| v.as_str())
        .unwrap_or("Standard");

    let material_provenance = if raw_item.get("composition").is_some() {
        ProvenanceType::SourceVerified
    } else {
        ProvenanceType::LlmSuggested
    };

    let common_meta = json!({
        "itemId": item_id,
        "name": raw_item.get("name").and_then(|v| v.as_str()).unwrap_or(item_id),
        "source": "INTERNAL",
        "commercial": {
            "priceUsd": price_usd,
            "volumeOrSize": volume_or_size,
            "sustainabilityGrade": llm_res.sustainability_grade
        },
        "color": color_intel,
        "material": {
            "composition": llm_res.materials,
            "provenance": material_provenance
        },
        "metadataVersion": 1,
        "ruleVersion": "1.0.0"
    });

    if llm_res.domain == DomainType::Wardrobe {
        let prod_type = llm_res.product_type.unwrap_or(ProductType::Unknown);
        if prod_type == ProductType::Unknown {
            violations.push(format!(
                "Wardrobe item {} resolved to UNKNOWN productType",
                item_id
            ));
        }

        let slot = match product_type_to_slot(prod_type) {
            Some(s) => s,
            None => {
                violations.push(format!(
                    "No deterministic GarmentSlot mapping for {:?}",
                    prod_type
                ));
                GarmentSlot::Accessory
            }
        };

        let rule_spec = deterministic_garment_rules(prod_type);
        let rule_occasions: HashSet<OccasionTag> = rule_spec
            .as_ref()
            .map(|r| r.occasions.iter().copied().collect())
            .unwrap_or_else(|| {
                [OccasionTag::Casual, OccasionTag::Everyday]
                    .into_iter()
                    .collect()
            });

        let mut all_occasions: Vec<OccasionTag> = rule_occasions.iter().copied().collect();
        for occ in &llm_res.occasion_tags {
            if !all_occasions.contains(occ) {
                all_occasions.push(*occ);
            }
        }

        let occasion_entries: Vec<Value> = all_occasions
            .iter()
            .map(|occ| {
                let prov = if rule_occasions.contains(occ) {
                    ProvenanceType::RuleDerived
                } else {
                    ProvenanceType::LlmSuggested
                };
                json!({
                    "value": occ,
                    "provenance": prov
                })
            })
            .collect();

        let has_trusted_occasion = occasion_entries.iter().any(|o| {
            matches!(
                o["provenance"].as_str(),
                Some("RULE_DERIVED" | "SOURCE_VERIFIED")
            )
        });
        if !has_trusted_occasion {
            violations.push(format!(
                "Item {} lacks a verified/rule-derived occasionTag for hard filtering",
                item_id
            ));
        }

        let thermal_values: Vec<ThermalBand> = rule_spec
            .as_ref()
            .map(|r| r.thermal.clone())
            .filter(|v| !v.is_empty())
            .unwrap_or_else(|| llm_res.thermal_suitability.clone());

        if thermal_values.is_empty() {
            violations.push(format!("Item {} has empty thermalSuitability", item_id));
        }

        let t_weight = rule_spec
            .as_ref()
            .map(|r| r.weight)
            .or(llm_res.thermal_weight)
            .unwrap_or(ThermalWeight::Midweight);

        let insul = rule_spec
            .as_ref()
            .map(|r| r.insulation)
            .or(llm_res.insulation)
            .unwrap_or(InsulationLevel::None);

        let primary_layer = rule_spec
            .as_ref()
            .map(|r| r.layering)
            .or(llm_res.primary_layering_role)
            .unwrap_or(LayeringRole::Standalone);

        let compatible_layers = if llm_res.compatible_layering_roles.is_empty() {
            vec![primary_layer]
        } else {
            llm_res.compatible_layering_roles.clone()
        };

        let (breath_val, breath_prov) = match llm_res.breathability {
            Some(b) if b != Breathability::Unknown => (json!(b), ProvenanceType::LlmSuggested),
            _ => (Value::Null, ProvenanceType::Unknown),
        };

        let record = json!({
            "domain": DomainType::Wardrobe,
            "commonMetadata": common_meta,
            "wardrobeMetadata": {
                "catalogCategory": llm_res.catalog_category.unwrap_or(CatalogCategory::Tops),
                "productType": {
                    "value": prod_type,
                    "provenance": ProvenanceType::RuleDerived
                },
                "garmentSlot": {
                    "value": slot,
                    "provenance": ProvenanceType::RuleDerived
                },
                "occasionTags": occasion_entries,
                "formality": {
                    "value": llm_res.formality.unwrap_or(FormalityLevel::Casual),
                    "provenance": ProvenanceType::LlmSuggested
                },
                "thermalSuitability": {
                    "values": thermal_values,
                    "provenance": ProvenanceType::RuleDerived,
                    "derivationMethod": "GARMENT_CONSTRUCTION",
                    "ruleVersion": "thermal-rules-1"
                },
                "weatherProtection": {
                    "values": llm_res.weather_protection,
                    "provenance": ProvenanceType::LlmSuggested
                },
                "construction": {
                    "thermalWeight": {
                        "value": t_weight,
                        "provenance": ProvenanceType::RuleDerived
                    },
                    "breathability": {
                        "value": breath_val,
                        "provenance": breath_prov
                    },
                    "insulation": {
                        "value": insul,
                        "provenance": ProvenanceType::RuleDerived
                    },
                    "features": llm_res.features,
                    "derivationMethod": "GARMENT_CONSTRUCTION",
                    "ruleVersion": "thermal-rules-1"
                },
                "layeringRole": {
                    "primary": primary_layer,
                    "compatible": compatible_layers,
                    "provenance": ProvenanceType::RuleDerived
                },
                "silhouette": {
                    "value": llm_res.silhouette.unwrap_or(SilhouetteFit::Relaxed),
                    "provenance": ProvenanceType::LlmSuggested
                },
                "seasons": {
                    "values": llm_res.seasons,
                    "provenance": ProvenanceType::LlmSuggested
                },
                "activities": {
                    "values": llm_res.activities,
                    "provenance": ProvenanceType::LlmSuggested
                }
            }
        });

        if violations.is_empty() {
            (Some(record), violations)
        } else {
            (None, violations)
        }
    } else {
        let cos_cat = llm_res.cosmetic_category.unwrap_or(CosmeticCategory::Prep);
        let app_role = llm_res
            .application_role
            .unwrap_or(CosmeticApplicationRole::Other);
        let shade = llm_res
            .shade_name
            .clone()
            .or_else(|| {
                raw_item
                    .get("shade")
                    .and_then(|v| v.as_str())
                    .map(|s| s.to_string())
            })
            .unwrap_or_else(|| "Standard".to_string());

        let record = json!({
            "domain": DomainType::Cosmetics,
            "commonMetadata": common_meta,
            "cosmeticsMetadata": {
                "cosmeticCategory": cos_cat,
                "applicationRole": app_role,
                "shade": shade,
                "finish": {
                    "value": llm_res.finish.unwrap_or(CosmeticFinish::Natural),
                    "provenance": ProvenanceType::LlmSuggested
                },
                "formulationAttributes": {
                    "values": llm_res.formulation_attributes,
                    "provenance": ProvenanceType::LlmSuggested
                }
            }
        });

        if violations.is_empty() {
            (Some(record), violations)
        } else {
            (None, violations)
        }
    }
}

// =====================================================================
// 7. PHASE 6 & 7: RFC 8785 CANONICAL JSON, ZSTD (-19), & ED25519 SIGNING
// =====================================================================

fn canonicalize_value(val: &Value) -> Value {
    match val {
        Value::Object(map) => {
            let sorted: BTreeMap<String, Value> = map
                .iter()
                .map(|(k, v)| (k.clone(), canonicalize_value(v)))
                .collect();
            let mut new_map = Map::new();
            for (k, v) in sorted {
                new_map.insert(k, v);
            }
            Value::Object(new_map)
        }
        Value::Array(arr) => Value::Array(arr.iter().map(canonicalize_value).collect()),
        _ => val.clone(),
    }
}

pub fn canonical_json_bytes(val: &Value) -> Result<Vec<u8>> {
    let canonical = canonicalize_value(val);
    Ok(serde_json::to_vec(&canonical)?)
}

pub fn compile_and_sign_kpkg(
    pack_id: &str,
    records: &[Value],
    output_dir: &Path,
    signing_key: &SigningKey,
) -> Result<Value> {
    let mut sorted_records = records.to_vec();
    sorted_records.sort_by(|a, b| {
        let id_a = a["commonMetadata"]["itemId"].as_str().unwrap_or("");
        let id_b = b["commonMetadata"]["itemId"].as_str().unwrap_or("");
        id_a.cmp(id_b)
    });

    let uncompressed_bytes = canonical_json_bytes(&Value::Array(sorted_records.clone()))?;

    // Compress with Zstandard level 19 (keeps mobile decompression window <= 64KB)
    let compressed_bytes = zstd::encode_all(uncompressed_bytes.as_slice(), 19)
        .context("Zstandard compression failed")?;

    let kpkg_filename = format!("{}.kpkg", pack_id);
    let kpkg_path = output_dir.join(&kpkg_filename);
    fs::write(&kpkg_path, &compressed_bytes)?;

    let mut hasher = Sha256::new();
    hasher.update(&compressed_bytes);
    let sha256_hex = hex::encode(hasher.finalize());

    let signature = signing_key.sign(&compressed_bytes);
    let signature_hex = hex::encode(signature.to_bytes());

    Ok(json!({
        "id": pack_id,
        "endpoint": kpkg_filename,
        "item_count": sorted_records.len(),
        "compressed_size_bytes": compressed_bytes.len(),
        "uncompressed_size_bytes": uncompressed_bytes.len(),
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
    }))
}

// =====================================================================
// 8. CLI ORCHESTRATOR
// =====================================================================

#[derive(Parser, Debug)]
#[command(name = "kocolor-tag-pipeline", about = "KoColor Catalog Tagging & .kpkg Compiler")]
struct Args {
    #[arg(long)]
    input_dir: PathBuf,

    #[arg(long)]
    output_dir: PathBuf,

    #[arg(long, default_value = "gemini-3.8-flash")]
    model: String,

    #[arg(long)]
    resume: bool,
}

#[tokio::main]
async fn main() -> Result<()> {
    let args = Args::parse();

    let api_key = env::var("GEMINI_API_KEY")
        .context("ERROR: GEMINI_API_KEY environment variable is required.")?;

    let signing_key = match env::var("KOCOLOR_SIGNING_SEED_HEX") {
        Ok(seed_hex) if !seed_hex.trim().is_empty() => {
            let bytes = hex::decode(seed_hex.trim())
                .context("Invalid hex in KOCOLOR_SIGNING_SEED_HEX")?;
            let seed: [u8; 32] = bytes
                .try_into()
                .map_err(|_| anyhow!("KOCOLOR_SIGNING_SEED_HEX must be 32 bytes (64 hex chars)"))?;
            SigningKey::from_bytes(&seed)
        }
        _ => SigningKey::generate(&mut OsRng),
    };

    fs::create_dir_all(&args.output_dir)?;
    let quarantine_dir = args.output_dir.join("quarantine");
    fs::create_dir_all(&quarantine_dir)?;

    let http_client = reqwest::Client::new();
    let mut approved_records: Vec<Value> = Vec::new();
    let mut quarantined_items: Vec<Value> = Vec::new();

    // Collect and deterministically sort all kc-*.json files
    let mut json_paths: Vec<PathBuf> = WalkDir::new(&args.input_dir)
        .into_iter()
        .filter_map(|e| e.ok())
        .filter(|e| e.file_type().is_file())
        .map(|e| e.into_path())
        .filter(|p| {
            if let Some(name) = p.file_name().and_then(|n| n.to_str()) {
                name.starts_with("kc-")
                    && name.ends_with(".json")
                    && !name.ends_with(".notes.json")
                    && !name.ends_with(".meta.json")
            } else {
                false
            }
        })
        .collect();
    json_paths.sort();

    for json_path in json_paths {
        let item_id = match json_path.file_stem().and_then(|s| s.to_str()) {
            Some(s) => s.to_string(),
            None => continue,
        };
        let notes_path = json_path.with_file_name(format!("{}.notes.json", item_id));
        let meta_out = json_path.with_file_name(format!("{}.meta.json", item_id));

        // Skip extraction if --resume is active and the meta file already exists
        if args.resume && meta_out.exists() {
            println!("[Skipping] {} (already enriched)", item_id);
            let meta_data: Value = match fs::read_to_string(&meta_out).and_then(|s| Ok(serde_json::from_str(&s)?)) {
                Ok(v) => v,
                Err(_) => continue,
            };
            approved_records.push(meta_data);
            continue;
        }

        let raw_data: Value = match fs::read_to_string(&json_path)
            .and_then(|s| Ok(serde_json::from_str(&s)?))
        {
            Ok(v) => v,
            Err(e) => {
                eprintln!("Skipping {}: bad json {}", item_id, e);
                continue;
            }
        };

        let notes_data: Value = if notes_path.exists() {
            fs::read_to_string(&notes_path)
                .ok()
                .and_then(|s| serde_json::from_str(&s).ok())
                .unwrap_or_else(|| json!({}))
        } else {
            json!({})
        };

        println!("[Enriching] {}...", item_id);

        // Pause for 7 seconds to respect Google AI Studio rate limits and avoid HTTP 429
        tokio::time::sleep(std::time::Duration::from_secs(7)).await;

        match extract_with_gemini(&http_client, &api_key, &args.model, &raw_data, &notes_data).await
        {
            Ok(llm_res) => {
                let (record_opt, violations) =
                    build_and_validate_record(&item_id, &raw_data, &notes_data, &llm_res);
                if let Some(record) = record_opt {
                    if violations.is_empty() {
                        fs::write(&meta_out, canonical_json_bytes(&record)?)?;
                        approved_records.push(record);
                        continue;
                    }
                }
                let q_entry = json!({
                    "itemId": item_id,
                    "violations": violations
                });
                fs::write(
                    quarantine_dir.join(format!("{}.quarantine.json", item_id)),
                    serde_json::to_string_pretty(&q_entry)?,
                )?;
                quarantined_items.push(q_entry);
            }
            Err(e) => {
                eprintln!("Error extracting {}: {}", item_id, e);
                let q_entry = json!({
                    "itemId": item_id,
                    "violations": [e.to_string()]
                });
                fs::write(
                    quarantine_dir.join(format!("{}.quarantine.json", item_id)),
                    serde_json::to_string_pretty(&q_entry)?,
                )?;
                quarantined_items.push(q_entry);
            }
        }
    }

    let report = json!({
        "generated_at": Utc::now().to_rfc3339(),
        "approved_count": approved_records.len(),
        "quarantined_count": quarantined_items.len(),
        "quarantined_items": quarantined_items,
    });
    fs::write(
        args.output_dir.join("quality_report.json"),
        serde_json::to_string_pretty(&report)?,
    )?;

    if !quarantined_items.is_empty() {
        eprintln!(
            "WARNING: {} items moved to quarantine. Inspect quality_report.json.",
            quarantined_items.len()
        );
    }

    let wardrobe_records: Vec<Value> = approved_records
        .iter()
        .filter(|r| r["domain"].as_str() == Some("WARDROBE"))
        .cloned()
        .collect();

    let cosmetic_records: Vec<Value> = approved_records
        .iter()
        .filter(|r| r["domain"].as_str() == Some("COSMETICS"))
        .cloned()
        .collect();

    let mut packs_meta: Vec<Value> = Vec::new();
    if !wardrobe_records.is_empty() {
        packs_meta.push(compile_and_sign_kpkg(
            "com.kocolor.pack.fashion.complete",
            &wardrobe_records,
            &args.output_dir,
            &signing_key,
        )?);
    }
    if !cosmetic_records.is_empty() {
        packs_meta.push(compile_and_sign_kpkg(
            "com.kocolor.pack.cosmetics.complete",
            &cosmetic_records,
            &args.output_dir,
            &signing_key,
        )?);
    }

    let manifest_data = json!({
        "manifest_version": 1,
        "generated_at": Utc::now().to_rfc3339(),
        "compiler_version": "1.0.0",
        "key_id": "atelier-dev-01",
        "packs": packs_meta
    });

    let envelope_sig = hex::encode(
        signing_key
            .sign(&canonical_json_bytes(&manifest_data)?)
            .to_bytes(),
    );

    let full_manifest = json!({
        "data": manifest_data,
        "signature": envelope_sig
    });

    fs::write(
        args.output_dir.join("manifest.json"),
        serde_json::to_string_pretty(&full_manifest)?,
    )?;

    println!(
        "Pipeline complete. {} records compiled into {} .kpkg bundles.",
        approved_records.len(),
        packs_meta.len()
    );

    Ok(())
}
