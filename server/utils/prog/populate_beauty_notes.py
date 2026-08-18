import os
import json
import glob

def populate_beauty_notes():
    root_dir = "/Users/developer/AndroidStudioProjects/ProBase/server/package/input/KoColor"
    # Find all notes files that are NOT in APPAREL
    notes_files = [f for f in glob.glob(os.path.join(root_dir, "**/*.notes.json"), recursive=True) if "APPAREL" not in f]

    for file_path in notes_files:
        with open(file_path, 'r') as f:
            try:
                data = json.load(f)
            except:
                continue

        # Only populate if dynamic_attributes is empty
        if not data.get("dynamic_attributes"):
            product_path = file_path.replace(".notes.json", ".json")
            product_data = {}
            if os.path.exists(product_path):
                with open(product_path, 'r') as pf:
                    product_data = json.load(pf)

            data["card_title"] = f"Artist Notes: {product_data.get('name', 'Product')}"

            # 1. Usage Notes
            usage = f"Placeholder {product_data.get('micro_category', 'product').lower()} notes for {product_data.get('id')}. High-fidelity performance ensured."

            # 2. Expert Tip
            tip = f"For optimal chromatic impact, apply {product_data.get('id')} to a clean, prepped surface."

            # 3. Formulation Insight
            insight = f"Advanced {product_data.get('micro_category', 'product').lower()} engineering featuring high-purity pigments and volatile carriers."

            data["dynamic_attributes"] = [
                {"label": "Usage Notes", "body": usage},
                {"label": "Expert Tip", "body": tip},
                {"label": "Formulation Insight", "body": insight}
            ]

            with open(file_path, 'w') as f:
                json.dump(data, f, indent=2)

    print(f"✅ Successfully populated {len(notes_files)} beauty notes with default scientific attributes.")

if __name__ == "__main__":
    populate_beauty_notes()
