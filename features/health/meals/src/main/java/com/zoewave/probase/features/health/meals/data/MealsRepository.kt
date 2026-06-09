package com.zoewave.probase.features.health.meals.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MealsRepository @Inject constructor() {
    private val _meals = MutableStateFlow<List<Meal>>(emptyList())
    val meals: Flow<List<Meal>> = _meals.asStateFlow()

    init {
        _meals.value = listOf(
            Meal(
                id = UUID.randomUUID().toString(),
                name = "mTOR Activation Breakfast",
                description = "High-protein initialization to trigger the Leucine Threshold and move the body from a catabolic overnight state to an anabolic state.",
                scientificFocus = "Leucine Threshold Activation",
                phase = MetabolicPhase.Morning,
                nutrition = NutritionInfo(450, 32f, 15f, 28f, 6f),
                ingredients = listOf(
                    Ingredient("Pasture-Raised Eggs", "3 large"),
                    Ingredient("Greek Yogurt (Full Fat)", "1/2 cup"),
                    Ingredient("Wild Blueberries", "1/4 cup"),
                    Ingredient("EVOO", "1 tbsp")
                ),
                steps = listOf(
                    MealStep(1, "Scramble eggs lightly in EVOO to preserve delicate fats."),
                    MealStep(2, "Top Greek yogurt with blueberries."),
                    MealStep(3, "Consume eggs first to maximize amino acid absorption.")
                )
            ),
            Meal(
                id = UUID.randomUUID().toString(),
                name = "Microbiome Homeostasis Lunch",
                description = "Focuses on fueling beneficial microbes via dietary fibers and polyphenols to drive SCFA biosynthesis.",
                scientificFocus = "SCFA Biosynthesis & Fiber Matrix",
                phase = MetabolicPhase.MidDay,
                nutrition = NutritionInfo(550, 28f, 45f, 22f, 12f),
                ingredients = listOf(
                    Ingredient("Wild Alaskan Salmon", "150g"),
                    Ingredient("Arugula & Dandelion Greens", "2 cups"),
                    Ingredient("Lentils (Cooled)", "1/2 cup"),
                    Ingredient("Lemon & Tahini Dressing", "2 tbsp")
                ),
                steps = listOf(
                    MealStep(1, "Steam or pan-sear salmon until just translucent."),
                    MealStep(2, "Toss greens with lentils and dressing."),
                    MealStep(3, "Ensure lentils are cooled to maximize resistant starch content.")
                )
            ),
            Meal(
                id = UUID.randomUUID().toString(),
                name = "Circadian Sunset Dinner",
                description = "High in tryptophan and easily digestible to support melatonin synthesis and vagal tone.",
                scientificFocus = "Melatonin Precursor Loading",
                phase = MetabolicPhase.Evening,
                nutrition = NutritionInfo(400, 25f, 50f, 12f, 8f),
                ingredients = listOf(
                    Ingredient("Pastured Turkey Breast", "120g"),
                    Ingredient("Roasted Sweet Potato", "1 medium"),
                    Ingredient("Bone Broth", "1 cup"),
                    Ingredient("Steamed Bok Choy", "1 cup")
                ),
                steps = listOf(
                    MealStep(1, "Warm the bone broth as a starter to prime enzymes."),
                    MealStep(2, "Roast sweet potato to provide complex carbs for tryptophan transport."),
                    MealStep(3, "Steam turkey and bok choy together for easy digestion.")
                )
            )
        )
    }

    fun addMeal(meal: Meal) {
        _meals.value = _meals.value + meal
    }
}
