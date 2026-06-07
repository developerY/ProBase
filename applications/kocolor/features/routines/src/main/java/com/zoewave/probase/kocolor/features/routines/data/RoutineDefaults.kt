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
        RoutineStep(
            id = "m2", 
            title = "Stage 2: Mouth Hygiene", 
            subtitle = "Clear & Protect",
            description = "During sleep, your mouth acts as a detoxification zone, accumulating a biofilm of bacteria, dead cells, and debris on your tongue. Drinking water immediately upon waking washes these accumulated elements directly into your digestive tract. Beyond digestion, your oral microbiome is deeply connected to your cardiovascular health; specific beneficial bacteria on your tongue are responsible for converting dietary nitrates into nitric oxide, a molecule that is essential for maintaining healthy blood pressure. While harsh antibacterial mouthwashes can indiscriminately destroy these protective bacteria, mechanical cleaning with a tongue scraper safely reduces the volatile sulfur compounds that cause bad breath while preserving your beneficial microbiome.",
            actionLabel = "Brush and floss your teeth first so you do not redistribute debris from your teeth onto a freshly cleaned tongue. Then, using a tongue scraper, apply light pressure and gently pull it from the back of your tongue to the front in one smooth motion. Rinse the scraper under running water after each pass, repeating three to four times to completely clear the overnight buildup before you hydrate."
        ),
        RoutineStep(
            id = "m3", 
            title = "Stage 3: Hydrate", 
            subtitle = "Enteric Activation via Yin-Yang Water",
            description = "Drinking ice water first thing in the morning can shock your digestive system, slowing down digestion and fat breakdown, while plain hot water lacks dynamic energy. The biologically optimal way to hydrate relies on classic fluid dynamics and a traditional Korean practice known as \"Eum-yang-tang\" (음양탕), or Yin-Yang water. When you combine hot and cold water in a specific sequence, the hot water rises while the denser cold water sinks, creating a natural, active convection current inside your glass. Consuming water in this state of active thermal circulation promotes rapid internal heat distribution. This gently stimulates your enteric nervous system, warms your lower abdomen where vital digestive organs are located, and safely alleviates morning digestive sluggishness.",
            actionLabel = "Boil water in a kettle or pot. Fill your glass or mug two-thirds of the way with the freshly boiled water, then immediately top off the remaining one-third with cold or room-temperature water. The pour sequence is critical—always pour the hot water first, followed by the cold water to trigger the circulation. Drink the water while it is actively mixing to safely awaken your digestive tract and prime your body for the day."
        ),
        RoutineStep(
            id = "m4", 
            title = "Stage 4: Move", 
            subtitle = "Photic Entrainment & Core Temperature",
            description = "To optimize your biological clock, morning movement must be paired with natural light exposure. Getting outdoors within the first hour of waking exposes your eyes to short-wavelength light, which activates melanopsin receptors and maximizes your Cortisol Awakening Response. This endocrine spike is essential for daytime alertness and primes your brain's master clock. Pairing this light exposure with exercise accelerates your core body temperature rise and phase-advances your circadian rhythm for better sleep that night."
        ),
        RoutineStep(
            id = "m5", 
            title = "Stage 5: Skincare - Cleanse", 
            subtitle = "Hydrophilic vs. Lipophilic Balance",
            description = "Your morning cleanse should be dictated by your specific skin type and evening routine. If you have oily skin or use heavy, lipophilic (oil-based) occlusives at night, a gentle cleanser is necessary to remove the buildup and create a clean canvas. However, if you have dry or sensitive skin, splashing with lukewarm water is often sufficient to refresh the face without stripping the skin's natural lipid barrier."
        ),
        RoutineStep(
            id = "m6", 
            title = "Stage 6: Skincare - Hydrate", 
            subtitle = "The 7-Skin Layering Method",
            description = "Instead of applying a single heavy moisturizer that can destabilize your sunscreen, utilize the Korean \"7-Skin Method\". This technique involves repeatedly patting multiple thin layers of a hydrating toner or essence into the skin. Waiting a few seconds between each layer allows humectants to penetrate deeply, replenishing the lipid barrier and providing profound, plump hydration without greasy surface occlusion."
        ),
        RoutineStep(
            id = "m7", 
            title = "Stage 7: Skincare - SPF", 
            subtitle = "Advanced Photoprotection",
            description = "Protect your skin with next-generation photostable UV filters, such as Uvinul A Plus and Tinosorb S, which are standard in advanced South Korean formulations. Unlike older legacy filters, these modern molecules are much larger, meaning they do not penetrate the skin barrier, generate localized heat, or cause the intense eye stinging associated with traditional sunscreens. Apply generously for serum-like, broad-spectrum defense."
        ),
        RoutineStep(
            id = "m8", 
            title = "Stage 8: Style - Makeup", 
            subtitle = "Polymer Film Formation",
            description = "Sunscreen relies on specialized film-forming polymers to create a continuous, unbroken microscopic mesh across your skin. Before applying any makeup, you must observe a mandatory 5 to 10-minute dry-down period. Applying foundation or using brushes before these polymers have fully cross-linked and the volatile solvents have evaporated will physically disrupt your UV shield and cause severe product pilling."
        ),
        RoutineStep(
            id = "m9", 
            title = "Stage 9: Prep", 
            subtitle = "Strategic Caffeine Delay",
            description = "The most pervasive biological mistake is consuming caffeine immediately upon waking. Caffeine works by temporarily blocking adenosine (the sleepiness molecule) receptors. If consumed too early, it interrupts the natural clearing of residual adenosine and stacks on top of your natural cortisol spike, leading to overstimulation and an inevitable mid-morning crash. Delay your first cup of coffee until 90 minutes after waking to allow your body's natural energy mechanisms to settle first."
        ),
        RoutineStep(
            id = "m10", 
            title = "Stage 10: Fuel - Breakfast", 
            subtitle = "The Leucine Threshold",
            description = "Your first meal must transition your body from an overnight catabolic state into an active anabolic state by triggering muscle protein synthesis. The key to activating this biological pathway is hitting the \"leucine threshold\". This requires consuming 2.5 to 3.0 grams of the amino acid leucine, which translates to a single meal containing roughly 30 grams of high-quality protein (like eggs or Greek yogurt). Prioritize a robust, complete protein source to effectively initiate muscle repair."
        )
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
