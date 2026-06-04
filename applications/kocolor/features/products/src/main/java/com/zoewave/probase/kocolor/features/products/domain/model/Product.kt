package com.zoewave.probase.kocolor.features.products.domain.model

data class Product(
    val id: String,
    val name: String,
    val brand: String,
    val imageUrl: String?,
    val fabricComposition: String?,
    val materials: List<String>,
    val origin: String?,
    val ecoScore: String?,
    val sustainabilityLabels: List<String>
)
