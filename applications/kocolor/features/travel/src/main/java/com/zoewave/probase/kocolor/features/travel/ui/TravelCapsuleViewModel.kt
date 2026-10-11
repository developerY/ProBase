package com.zoewave.probase.kocolor.features.travel.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.core.data.repository.weather.AtmosphericRepository
import com.zoewave.probase.core.model.ritual.FashionAdvice
import com.zoewave.probase.core.model.ritual.SeasonalType
import com.zoewave.probase.core.model.ritual.Undertone
import com.zoewave.probase.kocolor.data.FashionRepository
import com.zoewave.probase.kocolor.data.repository.CosmeticInventoryRepository
import com.zoewave.probase.kocolor.data.repository.WardrobeRepository
import com.zoewave.probase.kocolor.data.usecase.GenerateStyleResultUseCase
import com.zoewave.probase.kocolor.data.usecase.IntentAnalyzer
import com.zoewave.probase.kocolor.data.usecase.StyleRequestContext
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.max

enum class PackingCategory(val displayName: String) {
    DOCUMENTS("Documents & Essentials"),
    TOPS("Tops"),
    BOTTOMS("Bottoms"),
    OUTERWEAR("Outerwear"),
    UNDERGARMENTS("Socks & Underwear"),
    SHOES("Shoes"),
    COSMETICS("Cosmetics & Toiletries"),
    ACCESSORIES("Accessories")
}

data class PackingChecklistItem(
    val id: String,
    val name: String,
    val category: PackingCategory,
    val quantity: Int = 1,
    val isOwned: Boolean = true,
    val imageUrl: String? = null,
    val isChecked: Boolean = false
)

data class TravelCapsuleUiState(
    val destination: String = "",
    val occasion: String = "Vacation",
    val startDateMillis: Long? = null,
    val endDateMillis: Long? = null,
    val isInternational: Boolean = false,
    val isLoading: Boolean = false,
    val isGenerated: Boolean = false,
    val isSaved: Boolean = false,
    val passportReminder: Boolean = false,
    val weatherForecast: String = "",
    val rationale: String = "",
    val checklist: List<PackingChecklistItem> = emptyList(),
    val showStartDatePicker: Boolean = false,
    val showEndDatePicker: Boolean = false
)

sealed class TravelEvent {
    data class UpdateDestination(val dest: String) : TravelEvent()
    data class UpdateOccasion(val occasion: String) : TravelEvent()
    data class UpdateStartDate(val dateMillis: Long?) : TravelEvent()
    data class UpdateEndDate(val dateMillis: Long?) : TravelEvent()
    data class ToggleStartDatePicker(val show: Boolean) : TravelEvent()
    data class ToggleEndDatePicker(val show: Boolean) : TravelEvent()
    data class ToggleItemCheck(val id: String) : TravelEvent()
    data object GenerateCapsule : TravelEvent()
    data object SaveTrip : TravelEvent()
    data object Reset : TravelEvent()
}

@HiltViewModel
class TravelCapsuleViewModel @Inject constructor(
    private val atmosphericRepository: AtmosphericRepository,
    private val generateStyleResultUseCase: GenerateStyleResultUseCase,
    private val wardrobeRepository: WardrobeRepository,
    private val cosmeticRepository: CosmeticInventoryRepository,
    private val fashionRepository: FashionRepository,
    private val intentAnalyzer: IntentAnalyzer
) : ViewModel() {

    private val _uiState = MutableStateFlow(TravelCapsuleUiState())
    val uiState: StateFlow<TravelCapsuleUiState> = _uiState

    init {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val tomorrow = today + 86400000L * 3 // Default 3 day trip
        _uiState.value = _uiState.value.copy(startDateMillis = today, endDateMillis = tomorrow)
    }

    fun onEvent(event: TravelEvent) {
        when (event) {
            is TravelEvent.UpdateDestination -> _uiState.value = _uiState.value.copy(destination = event.dest)
            is TravelEvent.UpdateOccasion -> _uiState.value = _uiState.value.copy(occasion = event.occasion)
            is TravelEvent.UpdateStartDate -> {
                _uiState.value = _uiState.value.copy(startDateMillis = event.dateMillis, showStartDatePicker = false)
            }
            is TravelEvent.UpdateEndDate -> {
                _uiState.value = _uiState.value.copy(endDateMillis = event.dateMillis, showEndDatePicker = false)
            }
            is TravelEvent.ToggleStartDatePicker -> _uiState.value = _uiState.value.copy(showStartDatePicker = event.show)
            is TravelEvent.ToggleEndDatePicker -> _uiState.value = _uiState.value.copy(showEndDatePicker = event.show)
            is TravelEvent.ToggleItemCheck -> {
                val current = _uiState.value.checklist
                val updated = current.map {
                    if (it.id == event.id) it.copy(isChecked = !it.isChecked) else it
                }
                _uiState.value = _uiState.value.copy(checklist = updated)
            }
            TravelEvent.GenerateCapsule -> generateCapsule()
            TravelEvent.SaveTrip -> saveTripToCollection()
            TravelEvent.Reset -> {
                val today = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                }.timeInMillis
                _uiState.value = TravelCapsuleUiState(startDateMillis = today, endDateMillis = today + 86400000L * 3)
            }
        }
    }

    private fun saveTripToCollection() {
        viewModelScope.launch {
            val profile = fashionRepository.getProfile().firstOrNull()
            val seasonalType = profile?.seasonalType ?: SeasonalType.SUMMER
            val undertone = profile?.undertone ?: Undertone.NEUTRAL

            val dest = _uiState.value.destination.ifBlank { "Destination" }
            val occ = _uiState.value.occasion.ifBlank { "Vacation" }
            val title = "Trip to $dest ($occ)"
            val summary = _uiState.value.rationale.ifBlank { "Destination Capsule for $dest ($occ)" }

            val advice = FashionAdvice(
                title = title,
                summary = summary,
                seasonalType = seasonalType,
                undertone = undertone,
                makeupSuggestions = emptyList(),
                outfitSuggestions = emptyList(),
                recommendedPalette = listOf("#1E88E5", "#22C55E", "#D4AF37")
            )

            fashionRepository.saveSuggestion(advice)
            _uiState.value = _uiState.value.copy(isSaved = true)
        }
    }

    private fun generateCapsule() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, isSaved = false)

            val destination = _uiState.value.destination.ifBlank { "Destination" }
            val occasion = _uiState.value.occasion.ifBlank { "Vacation" }
            val start = _uiState.value.startDateMillis ?: System.currentTimeMillis()
            val end = _uiState.value.endDateMillis ?: (start + 86400000L)

            val days = max(1, ((end - start) / 86400000L).toInt() + 1) // +1 to include both days

            // Check if international (heuristic)
            val isInternational = destination.contains(Regex("london|paris|tokyo|mexico|spain|italy|japan|france|uk|germany|canada|rome|barcelona|amsterdam|sydney", RegexOption.IGNORE_CASE))

            // Fetch forecast with safe fallback
            val forecast = try {
                atmosphericRepository.fetchTravelForecast(destination, start, end)
            } catch (e: Exception) {
                null
            }

            val temp = forecast?.weather?.main?.temp?.toFloat() ?: 22f
            val weatherDesc = forecast?.weather?.weather?.firstOrNull()?.main ?: "Clear"
            val uvIndex = forecast?.environmentalContext?.uvIndex ?: 3.0

            val intentProfile = intentAnalyzer.analyze("$occasion in $destination. Weather: $weatherDesc, Temp: $temp. Days: $days")

            val context = StyleRequestContext(
                intent = "$occasion in $destination ($days days)",
                occasion = occasion,
                intentProfile = intentProfile,
                weather = weatherDesc,
                weatherTempC = temp,
                uvIndex = uvIndex.toFloat()
            )

            val wardrobe = try { wardrobeRepository.getAllClothing().first() } catch (e: Exception) { emptyList() }
            val cosmetics = try { cosmeticRepository.getAllCosmetics().first() } catch (e: Exception) { emptyList() }

            val result = try {
                generateStyleResultUseCase.execute(wardrobe, cosmetics, context)
            } catch (e: Exception) {
                android.util.Log.e("TravelCapsuleVM", "AI execution exception, falling back to direct curation", e)
                null
            }

            val checklist = mutableListOf<PackingChecklistItem>()
            var idCounter = 0

            // 1. Documents & Essentials
            checklist.add(PackingChecklistItem("doc_${idCounter++}", "ID / Driver's License", PackingCategory.DOCUMENTS, isOwned = true))
            if (isInternational) {
                checklist.add(PackingChecklistItem("doc_${idCounter++}", "Passport", PackingCategory.DOCUMENTS, isOwned = true))
                checklist.add(PackingChecklistItem("doc_${idCounter++}", "Travel Visa (if required)", PackingCategory.DOCUMENTS, isOwned = true))
                checklist.add(PackingChecklistItem("doc_${idCounter++}", "Universal Power Adapter", PackingCategory.DOCUMENTS, isOwned = false))
            }
            checklist.add(PackingChecklistItem("doc_${idCounter++}", "Phone Charger & Cables", PackingCategory.DOCUMENTS, isOwned = true))

            // 2. Undergarments
            checklist.add(PackingChecklistItem("und_${idCounter++}", "Socks", PackingCategory.UNDERGARMENTS, quantity = days + 1, isOwned = true))
            checklist.add(PackingChecklistItem("und_${idCounter++}", "Underwear", PackingCategory.UNDERGARMENTS, quantity = days + 1, isOwned = true))

            // 3. Tops
            val aiTops = result?.selectedClothing?.filter { it.category.name == "TOPS" || it.category.name == "SHIRTS" } ?: emptyList()
            aiTops.forEach { checklist.add(PackingChecklistItem("w_${it.internalId}", it.name, PackingCategory.TOPS, imageUrl = it.imageUrl)) }

            val missingTops = max(0, days - aiTops.size)
            if (missingTops > 0) {
                val additionalTops = wardrobe.filter { it.category.name == "TOPS" || it.category.name == "SHIRTS" }
                    .filterNot { it in aiTops }
                    .shuffled().take(missingTops)
                additionalTops.forEach { checklist.add(PackingChecklistItem("w_${it.internalId}", it.name, PackingCategory.TOPS, imageUrl = it.imageUrl)) }

                val stillMissing = missingTops - additionalTops.size
                if (stillMissing > 0) {
                    checklist.add(PackingChecklistItem("buy_top_${idCounter++}", "T-Shirts / Tops", PackingCategory.TOPS, quantity = stillMissing, isOwned = false))
                }
            }

            // 4. Bottoms
            val aiBottoms = result?.selectedClothing?.filter { it.category.name == "BOTTOMS" || it.category.name == "PANTS" || it.category.name == "SHORTS" || it.category.name == "SKIRTS" } ?: emptyList()
            aiBottoms.forEach { checklist.add(PackingChecklistItem("w_${it.internalId}", it.name, PackingCategory.BOTTOMS, imageUrl = it.imageUrl)) }

            val bottomsNeeded = max(1, days / 2)
            val missingBottoms = max(0, bottomsNeeded - aiBottoms.size)
            if (missingBottoms > 0) {
                val additionalBottoms = wardrobe.filter { it.category.name == "BOTTOMS" || it.category.name == "PANTS" }
                    .filterNot { it in aiBottoms }
                    .shuffled().take(missingBottoms)
                additionalBottoms.forEach { checklist.add(PackingChecklistItem("w_${it.internalId}", it.name, PackingCategory.BOTTOMS, imageUrl = it.imageUrl)) }

                val stillMissing = missingBottoms - additionalBottoms.size
                if (stillMissing > 0) {
                    checklist.add(PackingChecklistItem("buy_bot_${idCounter++}", "Pants / Shorts", PackingCategory.BOTTOMS, quantity = stillMissing, isOwned = false))
                }
            }

            // 5. Outerwear
            val aiOuter = result?.selectedClothing?.filter { it.category.name == "OUTERWEAR" || it.category.name == "JACKETS" } ?: emptyList()
            aiOuter.forEach { checklist.add(PackingChecklistItem("w_${it.internalId}", it.name, PackingCategory.OUTERWEAR, imageUrl = it.imageUrl)) }
            if (temp < 18f && aiOuter.isEmpty()) {
                val jacket = wardrobe.find { it.category.name == "OUTERWEAR" }
                if (jacket != null) {
                    checklist.add(PackingChecklistItem("w_${jacket.internalId}", jacket.name, PackingCategory.OUTERWEAR, imageUrl = jacket.imageUrl))
                } else {
                    checklist.add(PackingChecklistItem("buy_out_${idCounter++}", "Light Jacket / Sweater", PackingCategory.OUTERWEAR, isOwned = false))
                }
            }

            // 6. Shoes
            val aiShoes = result?.selectedClothing?.filter { it.category.name == "SHOES" } ?: emptyList()
            aiShoes.forEach { checklist.add(PackingChecklistItem("w_${it.internalId}", it.name, PackingCategory.SHOES, imageUrl = it.imageUrl)) }
            if (aiShoes.isEmpty()) {
                val shoe = wardrobe.find { it.category.name == "SHOES" }
                if (shoe != null) {
                    checklist.add(PackingChecklistItem("w_${shoe.internalId}", shoe.name, PackingCategory.SHOES, imageUrl = shoe.imageUrl))
                } else {
                    checklist.add(PackingChecklistItem("buy_shoe_${idCounter++}", "Comfortable Walking Shoes", PackingCategory.SHOES, isOwned = false))
                }
            }

            // 7. Cosmetics & Toiletries
            checklist.add(PackingChecklistItem("cosm_${idCounter++}", "Toothbrush & Toothpaste", PackingCategory.COSMETICS, isOwned = true))
            checklist.add(PackingChecklistItem("cosm_${idCounter++}", "Deodorant", PackingCategory.COSMETICS, isOwned = true))
            checklist.add(PackingChecklistItem("cosm_${idCounter++}", "Travel Skincare (Cleanser/Moisturizer)", PackingCategory.COSMETICS, isOwned = true))

            if (uvIndex > 4.0 || weatherDesc.contains("Clear", true) || weatherDesc.contains("Sun", true)) {
                val spf = cosmetics.find { it.macroCategory.name == "PREP" && (it.notes?.contains("SPF") == true || it.name.contains("Sunscreen")) }
                if (spf != null) {
                    checklist.add(PackingChecklistItem("c_${spf.internalId}", spf.name, PackingCategory.COSMETICS, imageUrl = spf.imageUrl))
                } else {
                    checklist.add(PackingChecklistItem("buy_spf_${idCounter++}", "SPF Sunscreen (High UV)", PackingCategory.COSMETICS, isOwned = false))
                }
            }

            result?.selectedCosmetics?.forEach {
                checklist.add(PackingChecklistItem("c_${it.internalId}", it.name, PackingCategory.COSMETICS, imageUrl = it.imageUrl))
            }

            val rationaleText = result?.blueprint?.rationale ?: "Curated $days-day travel packing checklist for $destination ($occasion) based on forecast ($weatherDesc, ${temp.toInt()}°C) and your local wardrobe."

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isGenerated = true,
                isInternational = isInternational,
                passportReminder = isInternational,
                weatherForecast = "${temp.toInt()}°C, $weatherDesc (UV $uvIndex)",
                rationale = rationaleText,
                checklist = checklist
            )
        }
    }
}
