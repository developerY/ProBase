package com.zoewave.probase.kocolor.features.chemicals.domain.model

data class ChemicalInfo(
    val cid: Int,
    val name: String,
    val formula: String?,
    val molecularWeight: String?,
    val iupacName: String?,
    val classification: String?,
    val safetyHazards: List<String>,
    val physicalProperties: Map<String, String>
)
