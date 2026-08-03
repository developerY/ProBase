pub mod cosmetics;
pub mod clothing;

use crate::{CosmeticItem, ClothingItem};

/// The Inventory Registry provides access to all individual items defined in the modular catalog.
pub struct InventoryRegistry;

impl InventoryRegistry {
    pub fn all_cosmetics() -> Vec<CosmeticItem> {
        vec![
            cosmetics::prep::purifying_gel_cleanser(),
            cosmetics::prep::luminescent_c_serum(),
            cosmetics::prep::anthelios_sunscreen(),
            cosmetics::lips::glow_catalyst_lip_stain(),
            cosmetics::lips::colourpop_blotted_lip(),
            cosmetics::complexion::seamless_silk_foundation(),
            cosmetics::dimension::petal_touch_blush(),
        ]
    }

    pub fn all_clothing() -> Vec<ClothingItem> {
        vec![
            clothing::tops::silk_drape_blouse(),
            clothing::tops::sculpted_blazer(),
        ]
    }

    /// Compose a custom pack by specific item IDs
    pub fn compose_pack(cosmetic_ids: Vec<&str>, clothing_ids: Vec<&str>) -> (Vec<CosmeticItem>, Vec<ClothingItem>) {
        let cosmetics = Self::all_cosmetics()
            .into_iter()
            .filter(|item| cosmetic_ids.contains(&item.id.as_str()))
            .collect();

        let clothing = Self::all_clothing()
            .into_iter()
            .filter(|item| clothing_ids.contains(&item.id.as_str()))
            .collect();

        (cosmetics, clothing)
    }
}
