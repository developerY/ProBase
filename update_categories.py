import re

with open("applications/kocolor/features/starterpack/src/main/java/com/zoewave/probase/kocolor/features/starterpack/data/StarterPackRepository.kt", "r") as f:
    content = f.read()

target = """                "SHIRT" -> ClothingCategory.TOPS
                "PANTS" -> ClothingCategory.BOTTOMS
                "SHOES" -> ClothingCategory.SHOES
                "OUTERWEAR" -> ClothingCategory.OUTERWEAR
                "ACTIVEWEAR" -> ClothingCategory.ACTIVEWEAR
                "DRESS" -> ClothingCategory.DRESSES
                "ONE_PIECE" -> ClothingCategory.DRESSES
                "ACCESSORY" -> ClothingCategory.ACCESSORIES
                "BAG" -> ClothingCategory.ACCESSORIES"""

replacement = """                "SHIRT" -> ClothingCategory.TOPS
                "PANTS" -> ClothingCategory.BOTTOMS
                "SHOES" -> ClothingCategory.SHOES
                "OUTERWEAR" -> ClothingCategory.OUTERWEAR
                "ACTIVEWEAR" -> ClothingCategory.ACTIVEWEAR
                "DRESS" -> ClothingCategory.DRESSES
                "ONE_PIECE" -> ClothingCategory.DRESSES
                "ACCESSORY" -> ClothingCategory.ACCESSORIES
                "BAG" -> ClothingCategory.ACCESSORIES
                "HAT" -> ClothingCategory.ACCESSORIES
                "JEWELRY" -> ClothingCategory.ACCESSORIES"""

content = content.replace(target, replacement)

# also add it to macro check
target_macro = """                        if (dto.macroCategory.uppercase() == "APPAREL") ClothingCategory.TOPS
                        else ClothingCategory.OTHER"""
replacement_macro = """                        if (dto.macroCategory.uppercase() == "APPAREL") ClothingCategory.TOPS
                        else if (dto.macroCategory.uppercase() == "ACCESSORIES" || dto.macroCategory.uppercase() == "ACCESSORY") ClothingCategory.ACCESSORIES
                        else ClothingCategory.OTHER"""

content = content.replace(target_macro, replacement_macro)

with open("applications/kocolor/features/starterpack/src/main/java/com/zoewave/probase/kocolor/features/starterpack/data/StarterPackRepository.kt", "w") as f:
    f.write(content)
