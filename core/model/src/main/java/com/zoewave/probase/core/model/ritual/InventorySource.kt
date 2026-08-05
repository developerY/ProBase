package com.zoewave.probase.core.model.ritual

import kotlinx.serialization.Serializable

@Serializable
enum class InventorySource {
    USER_SCAN,      // Manually captured by user
    STARTER_PACK,   // Official KoColor foundational data
    SAMPLE_PACK,    // Seasonal or trend-based miniatures
    PROMO_PACK,     // Brand-partnered limited collections
    GIFTED,         // Special unlocks or rewards
    CLONED,         // Created from a template item
    COMMUNITY,      // Peer-to-peer shared item
    UNKNOWN
}
