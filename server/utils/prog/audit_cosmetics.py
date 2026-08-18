import os
import json
import glob

def audit_cosmetics():
    # Canonical list of expected micro categories per macro (from CosmeticItem.kt)
    taxonomy = {
        "PREP": ["CLEANSER", "TONER", "SERUM", "MOISTURIZER", "SPF", "PRIMER", "FACE_MASK", "EXFOLIANT", "EYE_CARE", "LIP_CARE"],
        "COMPLEXION": ["FOUNDATION", "BB_CC_CREAM", "CONCEALER", "COLOR_CORRECTOR", "SETTING_POWDER", "FACE_POWDER", "SETTING_SPRAY"],
        "DIMENSION": ["BLUSH", "BRONZER", "CONTOUR", "HIGHLIGHTER", "FRECKLE_TINT"],
        "EYES": ["EYESHADOW", "EYELINER", "MASCARA", "LASH_PRIMER", "BROW_PENCIL", "BROW_GEL", "FALSE_LASHES"],
        "LIPS": ["LIPSTICK", "LIP_GLOSS", "LIP_LINER", "LIP_TINT_STAIN", "LIP_BALM", "LIP_PLUMPER"],
        "NAILS": ["NAIL_POLISH"],
        "HAIR": ["SHAMPOO", "CONDITIONER", "HAIR_MASK", "HAIR_COLOR", "HAIR_STYLING", "HAIR_SPRAY", "SCALP_TREATMENT"],
        "HYGIENE": ["SOAP", "SHOWER_GEL", "BATH_PRODUCT", "DEODORANT", "ANTIPERSPIRANT", "INTIMATE_HYGIENE", "COTTON_PRODUCT", "HAND_CREAM"],
        "ORAL": ["TOOTHPASTE", "MOUTHWASH", "TOOTHBRUSH", "DENTAL_FLOSS"],
        "FRAGRANCE": ["PERFUME", "EAU_DE_PARFUM", "EAU_DE_TOILETTE", "COLOGNE", "BODY_MIST"],
        "GROOMING": ["SHAVING_CREAM", "AFTERSHAVE", "BEARD_CARE", "RAZOR"],
        "TOOLS": ["BRUSHES", "SPONGES", "EYELASH_CURLER", "ORGANIZERS", "OTHER"]
    }

    root_dir = "/Users/developer/AndroidStudioProjects/ProBase/server/package/input/KoColor"
    product_files = glob.glob(os.path.join(root_dir, "**/*.json"), recursive=True)

    populated_macros = set()
    populated_micros = set()

    for file_path in product_files:
        if file_path.endswith(".notes.json"):
            continue

        with open(file_path, 'r') as f:
            try:
                data = json.load(f)
            except:
                continue

        macro = data.get("macro_category")
        micro = data.get("micro_category")
        if macro:
            populated_macros.add(macro.upper())
        if micro:
            populated_micros.add(micro.upper())

    # Map file values to enum names if different
    mapping = {
        "STAIN": "LIP_TINT_STAIN",
        "BALM": "LIP_BALM",
        "POLISH": "NAIL_POLISH",
        "EYEBROW": "BROW_PENCIL"
    }

    missing_report = "# Missing Cosmetic Categories (V1.0 Audit)\n\n"
    missing_report += "The following high-fidelity categories are defined in the scientific taxonomy but currently have **zero items** in the inventory.\n\n"

    found_any_missing = False

    # Check for entirely missing Macros first
    macro_gaps = [m for m in taxonomy.keys() if m not in populated_macros]
    if macro_gaps:
        found_any_missing = True
        missing_report += "## 🚨 Entirely Missing Vertical Segments\n"
        missing_report += "These primary segments have NO items currently indexed:\n"
        for m in sorted(macro_gaps):
            missing_report += f"- [ ] {m.title()}\n"
        missing_report += "\n---\n\n"

    # Check for gaps within existing or missing Macros
    for macro, micros in taxonomy.items():
        missing_micros = []
        for m in micros:
            found = False
            if m in populated_micros:
                found = True
            else:
                for k, v in mapping.items():
                    if v == m and k in populated_micros:
                        found = True
                        break

            if not found:
                missing_micros.append(m)

        if missing_micros:
            found_any_missing = True
            missing_report += f"### {macro} Gaps\n"
            for m in missing_micros:
                missing_report += f"- [ ] {m.replace('_', ' ').title()}\n"
            missing_report += "\n"

    if not found_any_missing:
        missing_report += "✅ All cosmetic categories are currently populated."

    with open("/Users/developer/AndroidStudioProjects/ProBase/server/docs/Catalog/Missing_Categories.artifact.md", "w") as out:
        out.write(missing_report)

    print("✅ Comprehensive audit complete.")

if __name__ == "__main__":
    audit_cosmetics()
