package com.zoewave.probase.features.health.nutrition.data

object NutritionDefaults {
    fun getUnifiedRoutine(): NutritionRoutine {
        return NutritionRoutine(
            stages = listOf(
                NutritionStage(
                    id = "stage_1",
                    title = "Stage 1: Intracellular Pre-Loading (06:00 - 07:00)",
                    subtitle = "Na+/K+-ATPase Pump Activation",
                    scientificBody = "Begin your day between 06:00 and 07:00 by consuming 16 to 20 ounces of water fortified with potassium and magnesium. In a fasted state, your cells rely heavily on the Na+/K+-ATPase pump to maintain their resting membrane potential, a massive energetic requirement that accounts for up to three-fourths of a neuron's total energy expenditure. This transmembrane enzyme utilizes ATP hydrolysis and absolutely requires magnesium as an obligatory cofactor to drive the essential conformational changes of its cytoplasmic headpiece. Pre-loading these cellular osmolytes prevents dangerous intracellular sodium accumulation and protects against mitochondrial calcium overload, ensuring immediate neurological and muscular excitability.",
                    suggestedMealTitle = "Stage 1 Meal: Intracellular Pre-Loading",
                    suggestedMealSubtitle = "Mineral-Fortified Hydration",
                    suggestedMealBody = "Upon waking, your body needs hydration and specific minerals to activate cellular energy pumps.\n\n**Suggested Options:**\n* 16–20 oz of water fortified with potassium and magnesium.\n* Consider utilizing the traditional \"Yin-Yang water\" method (filling a glass two-thirds with hot water, then topping it off with cold) to promote internal heat distribution and gently awaken the digestive system.",
                    startTime = "06:00",
                    endTime = "07:00"
                ),
                NutritionStage(
                    id = "stage_2",
                    title = "Stage 2: The Primary Metabolic Window (08:00)",
                    subtitle = "Circadian Insulin Sensitivity & mTOR Modulation",
                    scientificBody = "Consume your most carbohydrate-dense meal early in the morning, around 08:00. An optimal breakfast includes steel-cut oats or a strictly vetted granola (containing 5g or less of added sugar from a single source) topped with pumpkin seeds, chia seeds, walnuts, and soy or kefir milk. Dictated by the CLOCK/BMAL1 circadian network, your glucokinase activity, insulin synthesis, and skeletal muscle GLUT4 glucose transporters naturally peak in the morning, creating a biological window of maximal insulin sensitivity. Opting for whole-food plant proteins provides lower fractional concentrations of leucine. This prevents the chronic over-saturation of the intracellular Sestrin2 sensor, which binds leucine with a strict dissociation constant of 20 micromolar. This targeted protein delivery provides essential amino acids for tissue repair without triggering the hyperactivation of the mTORC1 aging pathway.",
                    suggestedMealTitle = "Stage 2 Meal: The Primary Metabolic Window",
                    suggestedMealSubtitle = "Low-Leucine, High-Fiber Breakfast",
                    suggestedMealBody = "Capitalize on your peak morning insulin sensitivity with complex carbohydrates and plant-based proteins.\n\n**Suggested Options:**\n* Steel-cut oats heavily topped with pumpkin seeds, chia seeds, and walnuts.\n* A strictly vetted granola (ensuring a maximum of 5g of added sugar and at least 7g of protein per serving).\n* Pair with unsweetened soy milk or a high-quality kefir to reach optimal protein thresholds without over-saturating the body with animal leucine.",
                    startTime = "08:00"
                ),
                NutritionStage(
                    id = "stage_3",
                    title = "Stage 3: Epigenetic Fortification (12:30 - 13:00)",
                    subtitle = "Microbiome Fermentation & HDAC Inhibition",
                    scientificBody = "Between 12:30 and 13:00, your midday meal must deliver a massive influx of diverse, intact plant fibers and unsaturated fats. An ideal lunch is a dense bowl of leafy greens, lentils or chickpeas, avocado, and an extra-virgin olive oil dressing. Colonic bacteria ferment these non-digestible carbohydrates to synthesize short-chain fatty acids, primarily butyrate. Butyrate acts as a powerful endogenous histone deacetylase (HDAC) inhibitor. By inhibiting HDACs within the cell nucleus, butyrate maintains local chromatin in an open state, directly upregulating the transcription of vital tight junction proteins like ZO-1 and claudin-1 to physically seal the gut barrier, while simultaneously suppressing the NF-κB inflammatory cascade.",
                    suggestedMealTitle = "Stage 3 Meal: Epigenetic Fortification",
                    suggestedMealSubtitle = "Microbiome-Fueling Lunch",
                    suggestedMealBody = "Provide your gut with a massive dose of fermentable fibers to produce protective short-chain fatty acids like butyrate.\n\n**Suggested Options:**\n* **Base:** A large serving of dark leafy greens, such as spinach or kale, which also provide the essential magnesium needed for your cellular pumps.\n* **Protein/Carb:** A heavy scoop of lentils, chickpeas, or beans.\n* **Fats:** Sliced avocado and a generous dressing made from extra-virgin olive oil.",
                    startTime = "12:30",
                    endTime = "13:00"
                ),
                NutritionStage(
                    id = "stage_4",
                    title = "Stage 4: The Circadian Downshift (17:00 - 18:00)",
                    subtitle = "Early Time-Restricted Feeding (eTRF)",
                    scientificBody = "Between 17:00 and 18:00, consume a lighter, easy-to-digest dinner, such as roasted root vegetables, asparagus, and a modest portion of wild-caught salmon, tempeh, or tofu. You must strictly cease all caloric intake 3 to 4 hours prior to sleep. As the evening progresses, the peripheral clocks in your muscle tissue naturally decrease insulin sensitivity, while hepatic glucose production rises to prepare the body for the fasting state of sleep. Consuming food during this phase disrupts this natural cycle, leading to significantly higher nocturnal glucose levels and hyperinsulinemia. Confining your food intake to daylight hours synchronizes peripheral feeding rhythms with the endogenous central clock, optimizing pancreatic β-cell exocytosis.",
                    suggestedMealTitle = "Stage 4 Meal: The Circadian Downshift",
                    suggestedMealSubtitle = "Light & Early Dinner",
                    suggestedMealBody = "Keep this meal easily digestible and finish eating 3 to 4 hours before bed so your body can safely lower glucose levels for the night.\n\n**Suggested Options:**\n* **Vegetables:** Roasted root vegetables (like sweet potatoes) or asparagus.\n* **Protein:** A modest portion of lean or plant-based protein such as wild-caught salmon, tempeh, or tofu.\n* **Preparation:** Cook with non-tropical plant oils (like olive or canola oil) and strictly avoid heavy, saturated animal fats like butter or beef tallow.",
                    startTime = "17:00",
                    endTime = "18:00"
                ),
                NutritionStage(
                    id = "stage_5",
                    title = "Stage 5: Autophagic Hormesis (18:00 - 08:00)",
                    subtitle = "AMPK Activation & NAD+ Synthesis",
                    scientificBody = "From 18:00 until 08:00 the next morning, maintain a strict 14-hour overnight fast consisting only of water. As extracellular nutrients drop, the systemic insulin/IGF-1 signaling (IIS) pathway quiets down, and the cellular AMP to ATP ratio rises, triggering the activation of the cellular energy sensor, AMPK. AMPK directly phosphorylates and inhibits mTORC1, signaling that nutrients are scarce, which halts cellular growth and initiates deep, restorative autophagic repair. Furthermore, this AMPK activation drives the production of NAD+, the essential biochemical fuel required for the sirtuin network (SIRT1-7) to remove acetyl markers from DNA, epigenetically silencing aging genes.",
                    suggestedMealTitle = "Stage 5 Meal: Autophagic Hormesis",
                    suggestedMealSubtitle = "The Fasting Window",
                    suggestedMealBody = "To trigger deep cellular repair pathways, completely avoid all caloric intake for 14 hours overnight.\n\n**Suggested Options:**\n* Plain water (still or sparkling).\n* Unsweetened, caffeine-free herbal teas.",
                    startTime = "18:00",
                    endTime = "08:00"
                )
            )
        )
    }
}
