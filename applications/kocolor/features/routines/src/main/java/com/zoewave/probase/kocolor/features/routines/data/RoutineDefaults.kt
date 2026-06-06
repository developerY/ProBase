package com.zoewave.probase.kocolor.features.routines.data

import com.zoewave.probase.kocolor.model.RoutineStep
import com.zoewave.probase.kocolor.model.RoutineTime

object RoutineDefaults {
    fun getMorningRoutine(): List<RoutineStep> = listOf(
        RoutineStep(
            id = "m1", 
            title = "Wake Up & Anchor", 
            subtitle = "Psychological Initialization",
            description = "The way you wake up dictates the neurocognitive trajectory of your entire day. Instead of a passive wake-up—where you might immediately fall into reactive habits like checking social media or emails—this step is about claiming immediate psychological control.\n\nThis approach is heavily inspired by the South Korean \"God-Saeng\" (갓생) movement, a cultural shift towards living a highly diligent, exemplary, and hyper-focused life. It serves as a proactive alternative to the YOLO (You Only Live Once) mindset by focusing on continuous self-development and micro-control over daily habits. A core part of this is the \"Miracle Morning\" practice, where individuals often wake before 6:00 AM to secure an immediate psychological victory before external demands begin.",
            actionLabel = "Don't just turn off your alarm. Use this moment to review your \"Daily Insight\" or set a single, immediate micro-goal. In the God-Saeng lifestyle, practitioners often use \"datafied visibility\"—such as logging their wake-up time or sharing a \"proof shot\". By checking off this very first action in the app, you trigger an immediate dopamine release that reinforces your self-efficacy and executive function for the complex steps ahead."
        ),
        RoutineStep("m2", "Hydrate", "Drink water (optionally with chlorophyll drops or lemon) to nourish the body."),
        RoutineStep("m3", "Move", "20-30 minutes of pilates, yoga, or a walk to boost circulation."),
        RoutineStep("m4", "Prep", "Make the bed for an instant sense of accomplishment."),
        RoutineStep("m5", "Skincare - Cleanse", "Cleanse your skin. Use a gua sha for depuffing if needed.", isRecommended = true),
        RoutineStep("m6", "Skincare - Serum", "Apply a vitamin C or hyaluronic acid serum.", isRecommended = true),
        RoutineStep("m7", "Skincare - Moisturizer", "Hydrate and seal in moisture.", isRecommended = true),
        RoutineStep("m8", "Skincare - SPF", "Crucial morning protection from UV rays.", isRecommended = true),
        RoutineStep("m9", "Style - Makeup", "Light makeup (tinted sunscreen, brow gel, mascara) to feel polished."),
        RoutineStep("m10", "Fuel - Breakfast", "A healthy breakfast (e.g., oatmeal with fruit or a smoothie).")
    )

    fun getEveningRoutine(): List<RoutineStep> = listOf(
        RoutineStep("e1", "Brush Teeth", "Nightly hygiene."),
        RoutineStep("e2", "Floss", "Essential for gum health."),
        RoutineStep("e3", "Double Cleanse", "Korean Skincare Step 1 & 2: Use oil then water cleanser to remove SPF and makeup.", isRecommended = true),
        RoutineStep("e4", "Exfoliator", "Korean Skincare Step 3: Use 1-2 times weekly to remove dead skin cells.", isRecommended = true),
        RoutineStep("e5", "Toner", "Korean Skincare Step 4: Prep skin for absorption.", isRecommended = true),
        RoutineStep("e6", "Essence", "Korean Skincare Step 5: Deep nightly hydration.", isRecommended = true),
        RoutineStep("e7", "Serum/Ampoule", "Korean Skincare Step 6: Intensive repair during sleep.", isRecommended = true),
        RoutineStep("e8", "Sheet Mask", "Korean Skincare Step 7: Use 2-3 times weekly for a moisture boost.", isRecommended = true),
        RoutineStep("e9", "Eye Cream", "Korean Skincare Step 8: Nightly care for eye area.", isRecommended = true),
        RoutineStep("e10", "Night Cream/Sleeping Mask", "Korean Skincare Step 9: Intense overnight recovery.", isRecommended = true)
    )

    val morningAdvice = listOf(
        "Always apply sunscreen, even on cloudy days!",
        "Vitamin C in the morning protects against daily pollutants.",
        "Pat your products in gently rather than rubbing.",
        "Drink a glass of water first thing for that natural morning glow.",
        "Apply products from thinnest consistency to thickest."
    )

    val eveningAdvice = listOf(
        "Double cleansing at night is the secret to clear Korean skin.",
        "Change your pillowcase weekly to avoid bacteria buildup.",
        "Sheet masks are best used for 15-20 minutes, don't let them dry out!",
        "Nighttime is for repair—focus on hydration and barrier support.",
        "Apply a thicker night cream to seal in all your active serums."
    )
}
