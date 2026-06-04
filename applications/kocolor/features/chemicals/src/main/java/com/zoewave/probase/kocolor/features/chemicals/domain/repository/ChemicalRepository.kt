package com.zoewave.probase.kocolor.features.chemicals.domain.repository

import com.zoewave.probase.kocolor.features.chemicals.domain.model.ChemicalInfo

interface ChemicalRepository {
    suspend fun getChemicalInfo(name: String): Result<ChemicalInfo>
}
