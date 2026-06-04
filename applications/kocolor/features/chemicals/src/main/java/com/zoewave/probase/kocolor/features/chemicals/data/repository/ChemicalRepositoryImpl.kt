package com.zoewave.probase.kocolor.features.chemicals.data.repository

import com.zoewave.probase.kocolor.features.chemicals.data.api.PubChemApi
import com.zoewave.probase.kocolor.features.chemicals.domain.model.ChemicalInfo
import com.zoewave.probase.kocolor.features.chemicals.domain.repository.ChemicalRepository
import javax.inject.Inject

class ChemicalRepositoryImpl @Inject constructor(
    private val api: PubChemApi
) : ChemicalRepository {

    override suspend fun getChemicalInfo(name: String): Result<ChemicalInfo> {
        return try {
            val response = api.getCompoundByName(name)
            val compound = response.compounds?.firstOrNull()
                ?: return Result.failure(Exception("Compound not found: $name"))

            val cid = compound.id?.id?.cid ?: 0
            val properties = mutableMapOf<String, String>()
            var formula: String? = null
            var weight: String? = null
            var iupac: String? = null
            val safetyHazards = mutableListOf<String>()
            var classification: String? = null

            compound.props?.forEach { prop ->
                val label = prop.urn?.label ?: ""
                val propName = prop.urn?.name ?: ""
                val value = prop.value?.sval ?: prop.value?.fval?.toString() ?: prop.value?.ival?.toString() ?: ""

                if (value.isNotBlank()) {
                    when {
                        label == "Molecular Formula" -> formula = value
                        label == "Molecular Weight" -> weight = value
                        label == "IUPAC Name" && (propName == "Preferred" || iupac == null) -> iupac = value
                        label.contains("Classification", ignoreCase = true) -> classification = value
                        label.contains("Hazard", ignoreCase = true) || label.contains("Safety", ignoreCase = true) -> safetyHazards.add("$label: $value")
                        else -> {
                            val key = "$label ${if (propName.isNotBlank()) "($propName)" else ""}".trim()
                            properties[key] = value
                        }
                    }
                }
            }

            // Note: In PUG REST, safety and detailed classification are often not in the basic compound JSON.
            // They are typically found in PUG-VIEW. However, we satisfy the model requirements with available data.

            Result.success(
                ChemicalInfo(
                    cid = cid,
                    name = name,
                    formula = formula,
                    molecularWeight = weight,
                    iupacName = iupac,
                    classification = classification,
                    safetyHazards = safetyHazards,
                    physicalProperties = properties
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
