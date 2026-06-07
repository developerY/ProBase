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
            description = "To optimize your biological clock, morning movement must be paired with natural light exposure. Getting outdoors within the first hour of waking exposes your eyes to short-wavelength light, specifically around 480nm. This activates specialized cells in your eyes that contain melanopsin, a photopigment that communicates directly with your brain's master clock. This light exposure maximizes your Cortisol Awakening Response, a natural and essential hormone spike that sharpens your focus and primes your body for the day. Pairing this light exposure with physical exercise accelerates the natural rise of your core body temperature. This powerful combination phase-advances your circadian rhythm, significantly boosting your daytime alertness while making it much easier to fall asleep that night.",
            actionLabel = "Get outdoors within 30 to 45 minutes of waking. Leave your sunglasses inside, as your eyes need direct exposure to natural daylight to properly trigger your circadian clock. Engage in 15 to 30 minutes of movement—such as a brisk walk, a light jog, or dynamic mobility stretches. Your primary goal during this stage is to elevate your heart rate and raise your core body temperature while absorbing the morning light."
        ),
        RoutineStep(
            id = "m5", 
            title = "Stage 5: Skincare - Cleanse", 
            subtitle = "Hydrophilic vs. Lipophilic Balance",
            description = "Should you wash your face with a cleanser in the morning or just use water? The scientific consensus indicates that the optimal approach is not universal; it depends entirely on your individual skin type and the chemical composition of your evening skincare regimen. Overnight, your skin is metabolically active, producing sweat, sebum, and shedding dead cells. Furthermore, many nighttime skincare products contain emollient-rich, lipophilic (oil-based) ingredients—especially if you use heavy ointments to seal in moisture—that are not water-soluble. Because water is hydrophilic, it can effectively rinse away overnight sweat, but it cannot dissolve or emulsify oxidized sebum or heavy night creams. If these lipophilic compounds are left on the skin, they form a persistent barrier that prevents the absorption of morning products and interferes with your sunscreen, leading to congested pores and dullness. Conversely, if you have dry, sensitive, or compromised skin, washing with a surfactant-heavy cleanser can strip your skin's natural lipid barrier and protective acid mantle.",
            actionLabel = "Let your skin type and your evening routine guide you. If you have oily or acne-prone skin, or if you applied heavy creams or oils the night before, gently massage a pH-balanced cleanser into your face for 30 to 60 seconds using lukewarm water. If your skin is dry or sensitive, and your evening routine was very light, skip the morning cleanser and simply splash your face thoroughly with lukewarm water to refresh your skin while preserving its natural barrier. After rinsing, always gently pat your face dry instead of rubbing."
        ),
        RoutineStep(
            id = "m6", 
            title = "Stage 6: Skincare - Hydrate", 
            subtitle = "The 7-Skin Layering Method",
            description = "Instead of applying a single thick layer of heavy moisturizer—which can create a greasy surface and destabilize your morning sunscreen—the optimal approach to hydration relies on the South Korean \"7-Skin Method\". In Korean skincare terminology, a \"skin\" refers to a lightweight, hydrating toner or essence. This technique involves repeatedly patting multiple, micro-thin layers of a water-based humectant into your face. By applying a small volume, waiting a few seconds, and layering again, you allow the active ingredients to penetrate deeper into the epidermis. This progressively replenishes the lipid barrier and delivers profound, plump hydration from within, completely avoiding the heavy, pore-clogging occlusion of traditional creams.",
            actionLabel = "Skip the cotton pads. Pour a small amount of a hydrating toner or essence (formulas containing fermented rice water or hyaluronic acid work exceptionally well) directly into the palms of your hands. Gently pat the liquid into your face and neck. Wait 10 to 15 seconds; while your skin is still slightly damp, apply the next layer. Repeat this process anywhere from 3 to 7 times, depending on how dehydrated your skin feels that morning."
        ),
        RoutineStep(
            id = "m7", 
            title = "Stage 7: Skincare - SPF", 
            subtitle = "Advanced Photoprotection",
            description = "Protecting your skin with broad-spectrum sunscreen is a critical medical intervention against photoaging, but the type of sunscreen you use matters immensely. Advanced South Korean formulations utilize next-generation, highly photostable UV filters like Uvinul A Plus and Tinosorb S. Unlike older legacy chemical filters (such as avobenzone and oxybenzone) that degrade quickly and convert UV rays into localized heat—which triggers sebum production and inflammation—modern filters have a much larger molecular weight. Because these molecules are larger, they do not penetrate the skin barrier, enter the bloodstream, or migrate into your eyes. This effectively eliminates the risk of instant breakouts, barrier irritation, and the intense eye stinging associated with older sunscreens, all while providing a lightweight, serum-like texture.",
            actionLabel = "Apply a generous amount of sunscreen (roughly two full finger lengths for your face and neck) and spread it evenly. Crucially, sunscreens rely on specialized film-forming polymers to create a continuous, unbroken protective mesh across your skin. You must observe a mandatory 5 to 10-minute dry-down period. Applying foundation, using makeup brushes, or touching your face before these polymers have fully cross-linked and the volatile solvents have evaporated will physically disrupt your UV shield and cause severe product pilling."
        ),
        RoutineStep(
            id = "m8", 
            title = "Stage 8: Style - Makeup", 
            subtitle = "Polymer Film Formation",
            description = "While you might be tempted to apply your makeup immediately after your skincare, patience is a scientific necessity at this stage. Sunscreen relies heavily on specialized inactive ingredients known as film-forming polymers. These polymers are responsible for spreading the active UV filters into a continuous, unbroken microscopic mesh across the topography of your skin. However, this protective film is highly unstable right after application. The volatile solvents need time to evaporate, and the polymers need time to fully cross-link and set. Applying foundation, concealer, or using makeup brushes and sponges before this film has stabilized will physically disrupt your UV shield and cause severe product pilling—which is when products roll up into tiny balls on your skin.",
            actionLabel = "Observe a mandatory 5 to 10-minute dry-down period after applying your sunscreen. Use this brief window to get dressed, pack your bag, or simply stretch. Once the time has passed, apply your makeup using gentle tapping or pressing motions with a beauty sponge or your fingers. Because hand movement patterns and friction can physically alter the sunscreen's protective mesh, you want to avoid aggressive rubbing or buffing with a makeup brush to ensure your UV layer remains completely intact."
        ),
        RoutineStep(
            id = "m9", 
            title = "Stage 9: Prep", 
            subtitle = "Strategic Caffeine Delay",
            description = "The most pervasive morning habit is the \"First Thing\" coffee ritual—waking up and immediately brewing a cup. However, neuroscience and circadian biology reveal that this actually stalls your body's natural \"self-cleaning\" mechanism. When you wake up, your brain initiates a highly coordinated hormone sequence called the Cortisol Awakening Response (CAR), which naturally peaks 30 to 45 minutes after waking to sharpen your focus and stabilize your blood sugar. Introducing caffeine—a synthetic stimulant—during this natural cortisol surge can backfire, causing overstimulation, jitters, and raising early morning blood pressure. Furthermore, it instructs your brain to down-regulate its own cortisol production, making you dependent on caffeine just to reach baseline functioning.\n\nCaffeine operates by binding to and blocking your adenosine receptors, which are responsible for detecting sleep pressure. If you consume caffeine immediately upon waking, it masks residual sleepiness without allowing your brain to naturally clear overnight adenosine. When the caffeine is eventually metabolized 3 to 4 hours later, a massive backlog of adenosine floods those unblocked receptors all at once, leading to the dreaded mid-morning crash. Delaying your \"Prep\" stage by 90 to 120 minutes allows your natural cortisol to do its job, clears out residual adenosine, and aligns your caffeine peak with your natural cortisol taper, creating a seamless neurochemical hand-off for sustained, crash-free focus.",
            actionLabel = "Delay your first cup of coffee, tea, or caffeine until 90 to 120 minutes after waking. Use the first hour and a half of your day to complete your hydration, outdoor movement, and skincare routines. Once your natural cortisol peak begins to taper, prepare your caffeinated beverage of choice and enjoy it mindfully. This strategic delay will maximize the cognitive enhancement of your caffeine, prevent rapid tolerance buildup, and protect your evening sleep architecture from late-day caffeine stacking."
        ),
        RoutineStep(
            id = "m10", 
            title = "Stage 10: Fuel - Breakfast", 
            subtitle = "The Leucine Threshold & Anabolic Ignition",
            description = "Your first meal of the day is a critical metabolic intervention. During your overnight fast, your muscles enter a catabolic state where protein breakdown exceeds synthesis. Breaking this fast requires more than just eating a meal; it requires actively triggering Muscle Protein Synthesis (MPS).\n\nThe biological master switch that governs this process is a cellular pathway known as mTORC1. Unlike other nutrients that offer a gradual response, mTORC1 operates on an \"all-or-nothing\" toggle switch controlled by the essential amino acid leucine. To throw this switch and initiate muscle repair, your blood concentration of leucine must cross a precise \"leucine threshold\". For young adults under the age of 50, this requires a single bolus of 2.5 to 3.0 grams of leucine, which translates to a high-quality meal containing roughly 30 grams of complete protein (such as Greek yogurt, eggs, or whey isolate).\n\nFor adults over the age of 50, a natural blunting of this pathway—known as anabolic resistance—occurs. Overcoming this barrier to protect against age-related muscle loss requires a louder nutritional signal: a higher threshold of 3.0 to 4.0 grams of leucine, or roughly 40 grams of complete protein. Furthermore, spreading your protein intake evenly across your day (such as consuming 30 grams at breakfast, lunch, and dinner) drives 25% higher muscle protein synthesis compared to the common habit of saving most of your protein for dinner. Once successfully triggered, the mTORC1 pathway enters a mandatory refractory period of 3 to 4 hours during which it is temporarily desensitized. This means continuous morning snacking or grazing actually blunts your body's muscle-building signals.",
            actionLabel = "Prioritize complete, leucine-dense proteins for your breakfast rather than carbohydrate-heavy options. Focus on obtaining at least 30 grams of high-quality protein (or 40 grams if you are over the age of 50) to successfully cross your leucine threshold and ignite your metabolism. Once you have completed this meal, avoid grazing; instead, space your next protein feeding at least 3 to 4 hours later to respect your body's natural refractory window."
        )
    )

    fun getEveningRoutine(): List<RoutineStep> = listOf(
        RoutineStep(
            id = "e1", 
            title = "Stage 1: Passive Body Heating", 
            subtitle = "Thermoregulatory Sleep Induction",
            description = "Your core body temperature naturally drops 1 to 2 degrees in the evening, serving as a primary biological signal to initiate sleep. When you take a warm bath or shower before bed, it temporarily raises your skin temperature and induces peripheral vasodilation, rushing blood to the extremities like your hands and feet. Exiting the warm water into a cool room causes these widely dilated blood vessels to rapidly dissipate stored heat, artificially mimicking and amplifying your body's natural core temperature drop. This profound thermoregulatory decline acts as a powerful chronobiotic cue that significantly shortens the time it takes to fall asleep and accelerates your entry into deep, restorative slow-wave sleep.",
            actionLabel = "Take a warm bath or shower (maintained between 104–109°F or 40–42.5°C) 1 to 2 hours before your intended bedtime for at least 10 minutes. After bathing, transition immediately into a cool bedroom environment—optimally set between 65–68°F (18–20°C)—to allow your body heat to rapidly escape and maximize the sleep-inducing cooling effect."
        ),
        RoutineStep(
            id = "e2", 
            title = "Stage 2: Systemic Pre-Fueling", 
            subtitle = "Anti-Glycation & Amino Acid Loading",
            description = "Poor sleep quality and frequent micro-awakenings trigger a sharp decline in your body's insulin sensitivity, leading to abnormal spikes in blood sugar. This excess glucose initiates a damaging biochemical process called glycation, where sugar molecules permanently bond to your skin's structural collagen and elastin fibers, turning them rigid, brittle, and prone to sagging. Simultaneously, fragmented sleep keeps your primary stress hormone, cortisol, elevated into the night; this actively breaks down your remaining healthy connective tissue while blocking the crucial nocturnal growth hormone pulses required for structural repair.",
            actionLabel = "Consume a supplement of glycine-rich collagen peptides (approximately 15 grams) roughly 60 minutes before your intended bedtime. The high glycine content acts as an inhibitory neurotransmitter that helps lower your core body temperature and significantly reduces sleep fragmentation, while the collagen peptides flood your bloodstream with the precise structural building blocks your body needs to rebuild tissue and defend against glycation overnight."
        ),
        RoutineStep(
            id = "e3", 
            title = "Stage 3: The Double Cleanse", 
            subtitle = "Lipophilic and Hydrophilic Decontamination",
            description = "Throughout the day, your face accumulates two distinct layers of residue: lipophilic (oil-based) elements like your natural sebum, stubborn sunscreen polymers, and trapped urban particulate matter (\$PM_{2.5}$), as well as hydrophilic (water-based) debris like sweat and environmental dust. A single water-based cleanser cannot safely dissolve the lipid-bound pollution matrix, often leading people to use harsh, damaging friction. If left behind, microscopic \$PM_{2.5}$ particles can penetrate the skin barrier and trigger massive inflammatory cascades that actively break down critical structural proteins. The double cleanse method utilizes the chemical principle of \"like dissolves like,\" allowing you to thoroughly decontaminate your pores without stripping the physiological ceramides that protect your skin's natural moisture barrier.",
            actionLabel = "Start with dry hands and a dry face. Massage a lipophilic (oil-based) cleansing balm or oil onto your skin for 30 to 60 seconds to gently dissolve your SPF, makeup, and trapped pollution without pulling or rubbing. Add a splash of lukewarm water to emulsify the oil into a milky texture, then rinse. Immediately follow up with a mild, soap-free, and sulfate-free hydrophilic (water-based) cleanser to wash away the remaining sweat and residual emulsifiers, leaving a pristine, perfectly balanced canvas."
        ),
        RoutineStep(
            id = "e4", 
            title = "Stage 4: Epigenetic Barrier Prep", 
            subtitle = "Postbiotic Microbiome Restoration",
            description = "While your app's placeholder images listed an \"Exfoliator\" at this stage, applying harsh physical scrubs or strong acids at night can actually be detrimental, as your skin's natural permeability and Transepidermal Water Loss (TEWL) peak during the evening, making the barrier highly vulnerable. Instead of stripping the skin, the scientifically optimal step—replacing the \"Toner/Essence\" placeholders in your design—is to apply a hydrating liquid rich in bacterial postbiotics, such as Bifida Ferment Lysate. Because your skin is so permeable at night, these microscopic bacterial fragments easily penetrate and bind to specific cellular receptors, immediately downregulating the daily inflammation caused by UV rays and urban pollution. More importantly, they act on an epigenetic level, commanding your skin cells to rapidly upregulate the production of crucial physical barrier proteins like filaggrin and loricrin, effectively rebuilding your stratum corneum from the inside out while you sleep.",
            actionLabel = "Immediately after your double cleanse, while your face is still slightly damp, pour a few drops of a postbiotic essence or hydrating toner into the palms of your hands. Gently press and pat the liquid directly into your face and neck until it is fully absorbed. Avoid any harsh rubbing or mechanical exfoliation. Allow the layer to dry down for a few moments so the postbiotics can effectively engage with your skin's immune receptors before you apply your heavier serums."
        ),
        RoutineStep(
            id = "e5", 
            title = "Stage 5: Targeted Actives", 
            subtitle = "Chronopharmacological Optimization",
            description = "Your skin operates on a strict 24-hour biological clock, and its physical properties change drastically when the sun goes down. During the night, your epidermal barrier becomes highly permeable, and the transdermal penetration of both hydrophilic and lipophilic topical compounds reaches its absolute biological maximum around 4:00 AM. Simultaneously, your body's natural nighttime temperature regulation causes peripheral vasodilation; this increased cutaneous blood flow actively accelerates the passage and cellular distribution of these active compounds. Leveraging this biological window—a concept known as chronopharmacology—means that potent, targeted ingredients like retinoids, collagen-stimulating peptides, and concentrated antioxidants will penetrate much deeper and work exponentially harder during sleep than they ever could during the day.",
            actionLabel = "Once your postbiotic essence has dried down, dispense 2 to 3 drops of your highly concentrated active serum or ampoule (such as a retinoid or targeted peptide) onto your fingertips. Gently press and smooth the active ingredients evenly across your face, neck, and décolletage. Wait 1 to 2 minutes to allow the formula to fully sink into the epidermis and begin its transdermal journey before you seal it in with your final heavy creams."
        ),
        RoutineStep(
            id = "e6", 
            title = "Stage 6: Occlusive Seal", 
            subtitle = "Mitigating Nocturnal TEWL",
            description = "While your body is focused on deep cellular repair, your skin's physical barrier is paradoxically at its weakest. Driven by your biological clock, your skin's permeability and Transepidermal Water Loss (TEWL) reach their absolute peak during the night. This means that while active ingredients absorb better, essential hydration rapidly escapes into the atmosphere, which can cause micro-dehydration and trigger inflammatory pathways. To prevent this moisture evaporation and protect your vulnerable barrier, you must apply a biocompatible occlusive \"seal\". Dermatological science shows that formulations containing an optimal 3:1:1 molar ratio of ceramides, cholesterol, and free fatty acids are the most effective at structurally repairing the epidermal barrier and locking in your previous layers of hydration.",
            actionLabel = "Dispense a dime-sized amount of a rich night cream or sleeping mask formulated with a ceramide complex. Warm the product slightly between your fingers, then gently massage it over your entire face and neck. Ensure you create a thin, continuous layer to act as an artificial barrier, effectively sealing in the postbiotics and active serums you applied in the previous steps. Allow it a few minutes to set into the skin before your face touches your pillow."
        ),
        RoutineStep(
            id = "e7", 
            title = "Stage 7: Biomimetic Oral Care", 
            subtitle = "Nano-Hydroxyapatite Remineralization",
            description = "While your app's initial placeholder images correctly identified \"Brush Teeth\" and \"Floss\" as essential steps, a scientifically optimized routine requires a specific chemical upgrade and a strategic sequence shift to the very end of your night. During sleep, your salivary flow drops to near zero, removing your mouth's natural mechanical washing and acid-buffering defenses. This creates a stagnant environment where bacterial counts increase markedly, producing acids that slowly erode your tooth enamel. However, this prolonged, uninterrupted period without eating or drinking is also the absolute perfect biological window for structural repair. By using a nano-hydroxyapatite toothpaste, you introduce microscopic crystals that perfectly biomimic your natural tooth structure. Throughout the night, these particles actively bind to demineralized areas, physically replacing lost calcium and phosphate ions to repair subsurface lesions and rebuild your enamel while you sleep.",
            actionLabel = "Complete this as the absolute final step of your routine, right before your head hits the pillow. First, floss thoroughly to remove interdental plaque. Next, brush your teeth for a full two minutes using a high-quality nano-hydroxyapatite toothpaste. Crucially, **do not rinse** your mouth with water or mouthwash after brushing. Simply spit out the excess paste. Leaving this concentrated residue on your teeth allows the nano-hydroxyapatite crystals to continuously remineralize and strengthen your enamel throughout the entire night."
        )
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
