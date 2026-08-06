use crate::ClothingItem;

pub fn silk_drape_blouse() -> ClothingItem {
    ClothingItem {
        id: "kc-cloth-001".to_string(),
        name: "KoColor Silk Drape Executive Blouse".to_string(),
        brand: Some("KoColor Studio".to_string()),
        macro_category: "TOPS".to_string(),
        micro_category: "TOPS".to_string(),
        formality: "PROFESSIONAL".to_string(),
        color_hex: "#A81C28".to_string(),
        size: Some("M".to_string()),
        material: Some("100% Mulberry Silk".to_string()),
        price: Some(185.0),
        image_url: "https://cdn.kocolor.com/inventory/assets/silk_drape_blouse.webp".to_string(),
        thumbnail_url: "https://cdn.kocolor.com/inventory/assets/silk_drape_blouse_thumb.webp".to_string(),
        notes: Some("Essential professional silk blouse.".to_string()),
        dominant_hex: Some("#A81C28".to_string()),
        vibrant_hex: Some("#C82333".to_string()),
        muted_hex: Some("#8B1720".to_string()),
        palette_hexes: vec!["#A81C28".to_string(), "#C82333".to_string(), "#8B1720".to_string(), "#FFFFFF".to_string(), "#1F2421".to_string()],
        color_temperature: Some("NEUTRAL".to_string()),
        seasonal_palette: Some("WINTER".to_string()),
        contrast_level: Some("HIGH".to_string()),
        ko_color_group: Some("Crimson Velvet".to_string()),
    }
}

pub fn sculpted_blazer() -> ClothingItem {
    ClothingItem {
        id: "kc-cloth-002".to_string(),
        name: "KoColor Tailored Sculpted Blazer".to_string(),
        brand: Some("KoColor Studio".to_string()),
        macro_category: "TOPS".to_string(),
        micro_category: "TOPS".to_string(),
        formality: "PROFESSIONAL".to_string(),
        color_hex: "#2C3539".to_string(),
        size: Some("M".to_string()),
        material: Some("98% Virgin Wool, 2% Elastane".to_string()),
        price: Some(340.0),
        image_url: "https://cdn.kocolor.com/inventory/assets/tailored_sculpted_blazer.webp".to_string(),
        thumbnail_url: "https://cdn.kocolor.com/inventory/assets/tailored_sculpted_blazer_thumb.webp".to_string(),
        notes: Some("Sculpted tailored wool blazer.".to_string()),
        dominant_hex: Some("#2C3539".to_string()),
        vibrant_hex: Some("#3B474C".to_string()),
        muted_hex: Some("#1E2427".to_string()),
        palette_hexes: vec!["#2C3539".to_string(), "#3B474C".to_string(), "#1E2427".to_string(), "#E5E5E5".to_string(), "#A81C28".to_string()],
        color_temperature: Some("COOL".to_string()),
        seasonal_palette: Some("WINTER".to_string()),
        contrast_level: Some("HIGH".to_string()),
        ko_color_group: Some("Deep Charcoal".to_string()),
    }
}
