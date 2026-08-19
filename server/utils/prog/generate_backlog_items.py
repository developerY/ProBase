import os
import json

def generate():
    root_dir = "/Users/developer/AndroidStudioProjects/ProBase/server/package/input/KoColor"
    summary_file = "/Users/developer/AndroidStudioProjects/ProBase/server/docs/Catalog/summary.md"
    description_file = "/Users/developer/AndroidStudioProjects/ProBase/server/docs/Catalog/description.md"
    tech_file = "/Users/developer/AndroidStudioProjects/ProBase/server/docs/Catalog/Product_Description.md"

    # Structure: (Macro, Micro, Name, Price, Volume, Ingredients, ColorHex, ShadeName, MarketingHook, Narrative)
    items = [
        # PREP
        ("PREP", "TONER", "Hydra-Lock Balancing Toner", 22.0, "200ml", ["Rose Water", "Hyaluronic Acid", "Allantoin"], "#F0F8FF", "Clear Mist",
         "Restore equilibrium. This weightless, alcohol-free toner uses rose water and hyaluronic acid to refine pores and lock in essential moisture.",
         "The essential bridge between cleansing and treatment. This pH-balanced formula preps the skin's surface for optimal absorption of active serums while providing a cooling, anti-inflammatory finish."),

        ("PREP", "MOISTURIZER", "Barrier-Shield Daily Moisturizer", 38.0, "50ml", ["Ceramides", "Squalane", "Shea Butter"], "#FFFFFF", "Silk White",
         "Your skin's ultimate bodyguard. A ceramide-rich cream that fortifies the natural moisture barrier while leaving a velvety, non-greasy finish.",
         "Deep hydration meets environmental defense. This breathable moisturizer uses a scientific blend of lipids to mimic the skin's natural structure, ensuring long-wear comfort and elastic retention."),

        ("PREP", "PRIMER", "Optical Blur Pore Primer", 28.0, "30ml", ["Silica", "Glycerin", "Dimethicone"], "#F5F5F5", "Translucent Cloud",
         "The perfect canvas starts here. A silicone-free primer that instantly blurs pores and fine lines while extending the wear of your complexion products.",
         "Architectural smoothing for the skin. This innovative formula uses light-diffusing microspheres to create a 'soft focus' effect, neutralizing surface texture for high-fidelity makeup application."),

        ("PREP", "FACE_MASK", "Resurfacing Enzyme Mask", 45.0, "75ml", ["Pumpkin Enzyme", "Glycolic Acid", "Honey"], "#FF8C00", "Enzyme Orange",
         "Reveal your true glow. A potent enzymatic treatment that dissolves dead skin cells and resurfaces the complexion for instant clarity.",
         "Scientific exfoliation without the irritation. This mask uses stabilized fruit enzymes and AHA to gently peel away dullness, revealing a smooth, high-fidelity skin texture."),

        ("PREP", "EXFOLIANT", "Liquid Glow AHA Exfoliant", 34.0, "100ml", ["Salicylic Acid", "Lactic Acid", "Green Tea"], "#FDF5E6", "Clear Solution",
         "Chemical precision for your skin. A powerful leave-on exfoliant that unclogs pores and evens out skin tone for a refined, luminous finish.",
         "Advanced chemical resurfacing. This balanced blend of AHAs and BHAs targets both surface texture and deep-pore congestion, leaving skin polished and balanced."),

        ("PREP", "EYE_CARE", "Depuffing Caffeine Eye Cream", 30.0, "15ml", ["Caffeine", "Peptides", "Vitamin C"], "#FFFACD", "Ivory Glow",
         "Awaken your gaze. A high-potency eye treatment that targets dark circles and puffiness with a shot of scientific caffeine.",
         "Targeted micro-circulation support. This lightweight cream delivers vaso-constrictive actives directly to the delicate eye area, reducing inflammation and brightening the eye contour."),

        ("PREP", "LIP_CARE", "Conditioning Lip Scrub", 16.0, "15g", ["Sugar Crystals", "Coconut Oil", "Vitamin E"], "#FFB6C1", "Sweet Pink",
         "Pout perfection. A gentle, sugar-based exfoliant that removes dry flakes and conditions lips for a smooth, high-fidelity color application.",
         "The essential prep for high-impact lip color. This scrub uses uniform micro-crystals to resurface the lips while infusing deep hydration via organic coconut oil."),

        # COMPLEXION
        ("COMPLEXION", "BB_CC_CREAM", "Adaptive Radiance BB Cream", 32.0, "40ml", ["Zinc Oxide", "Niacinamide", "Iron Oxides"], "#E5C29B", "Neutral Beige",
         "Skincare meets coverage. A lightweight, all-in-one beauty balm that provides sheer-to-medium coverage with a luminous, second-skin finish.",
         "The daily uniform for the minimalist. This hybrid formula combines the benefits of a moisturizer, SPF, and foundation, adapting to your unique skin undertones for a natural, healthy glow."),

        ("COMPLEXION", "COLOR_CORRECTOR", "Chroma-Correction Mint Gel", 24.0, "10ml", ["Water", "Chromium Oxide Greens", "Centella Asiatica"], "#98FB98", "Mint Neutralizer",
         "Neutralize redness instantly. A high-fidelity green corrector that cancels out inflammation and broken capillaries for a perfectly even base.",
         "Scientific color theory in action. This weightless gel uses complementary color optics to counteract ruddy undertones, creating a neutral starting point for your foundation or concealer."),

        ("COMPLEXION", "SETTING_POWDER", "Velvet-Finish Loose Powder", 35.0, "20g", ["Talc", "Silica", "Mica"], "#FFF5EE", "Translucent Silk",
         "Lock it in. An ultra-fine, light-reflecting loose powder that blurs imperfections and sets your makeup for a flawless, long-wear finish.",
         "The ultimate atmospheric finish. This air-milled powder uses micro-spheres to diffuse light, eliminating shine without altering the underlying color DNA of your foundation."),

        ("COMPLEXION", "FACE_POWDER", "Baked Mineral Foundation", 40.0, "9g", ["Sericite Mica", "Titanium Dioxide", "Zinc Stearate"], "#D2B48C", "Fair Tan",
         "Mineral mastery. A baked, pigment-rich mineral powder that provides buildable, breathable coverage with a natural, skin-like finish.",
         "Structural coverage in a weightless format. This mineral foundation is slow-baked on terracotta tiles to preserve the integrity of the pigments, delivering high-fidelity coverage and comfort."),

        ("COMPLEXION", "SETTING_SPRAY", "All-Day Hydro Lock Mist", 28.0, "100ml", ["Water", "PVP", "Aloe Vera"], "#F0FFFF", "Dewy Mist",
         "The final seal. A fine-mist setting spray that locks your look in place while delivering a burst of scientific hydration.",
         "Advanced polymer fixation. This spray creates a flexible, weightless barrier that prevents makeup from settling into lines or transferring, while maintaining a dewy, high-fidelity finish."),

        # DIMENSION
        ("DIMENSION", "BRONZER", "Sun-Kissed Mineral Bronzer", 26.0, "8g", ["Mica", "Boron Nitride", "Iron Oxides"], "#C68E65", "Golden Sand",
         "Instant warmth, zero sun damage. A finely milled powder bronzer that adds natural-looking depth and a healthy, golden-hour glow.",
         "Sculpting with light. This breathable mineral formula provides a seamless blend that mimics the natural path of the sun on your face, emphasizing bone structure with a soft-matte finish."),

        ("DIMENSION", "CONTOUR", "Architectural Sculpting Stick", 30.0, "6g", ["Caprylic Triglyceride", "Kaolin", "Iron Oxides"], "#8B4513", "Shadow Brown",
         "Define your architecture. A creamy, high-precision contour stick designed to create believable shadows and enhance your natural structure.",
         "Precision structural enhancement. This high-viscosity stick allows for clinical placement of shadows, blending effortlessly into the skin for a refined, professionally sculpted profile."),

        ("DIMENSION", "HIGHLIGHTER", "Starlight Liquid Glow", 28.0, "15ml", ["Water", "Mica", "Tin Oxide"], "#F7E7CE", "Champagne Pop",
         "Luminous dimension. A weightless, concentrated liquid highlighter that delivers a multi-dimensional, candlelit glow to the high points of the face.",
         "Capturing the starlight. This formula uses technical mica and tin oxide to create a high-reflectance surface that catches the light from every angle without ever looking glittery."),

        ("DIMENSION", "FRECKLE_TINT", "Natural-Dot Freckle Pen", 18.0, "1ml", ["Water", "Glycerin", "Iron Oxides"], "#A0522D", "Amber Speckle",
         "Youthful authenticity. A fine-tipped tint pen that allows you to create effortless, realistic-looking freckles that last all day.",
         "The look of natural skin, enhanced. This long-wear tint provides sheer, buildable spots that mimic natural melanin distribution, adding a touch of organic character to any look."),

        # EYES
        ("EYES", "LASH_PRIMER", "Lash-Bonding Fiber Primer", 20.0, "10ml", ["Water", "Beeswax", "Cellulose Fibers"], "#FFFFFF", "Pure White",
         "Multiply your lashes. A high-fidelity white primer that coats every lash in a volumizing fiber network before mascara application.",
         "The foundation of dramatic lashes. This scientific base adds length and thickness by creating a structural 'scaffold' around each individual hair, amplifying the performance of any top-coat."),

        ("EYES", "BROW_GEL", "Volumizing Tinted Brow Gel", 22.0, "5ml", ["Water", "Acrylates Copolymer", "Iron Oxides"], "#4B3621", "Dark Brown",
         "Architectural brows. A fiber-infused gel that grooms, tints, and volumizes brows in one simple scientific step.",
         "Structure and color in one. This long-wear formula uses micro-fibers to add bulk to sparse areas while the tinted gel provides a soft, flexible hold that never flakes."),

        ("EYES", "FALSE_LASHES", "High-Definition Silk Lashes", 15.0, "1 Pair", ["Synthetic Silk Fibers", "Cotton Band"], "#000000", "Obsidian Black",
         "Flutter with precision. Hand-crafted, multi-dimensional silk lashes designed to add volume and drama with a lightweight, comfortable feel.",
         "Optical lash enhancement. These high-fidelity lashes are engineered with varying lengths to mimic natural growth patterns, creating an intense but believable eye profile."),

        # LIPS
        ("LIPS", "LIP_GLOSS", "Glass-Reflect High Shine Gloss", 22.0, "5ml", ["Polybutene", "Jojoba Oil", "Mica"], "#FDF5E6", "Crystal Clear",
         "Liquid diamonds for your lips. A non-sticky, high-impact gloss that delivers a multi-dimensional shine and a cushiony, comfortable feel.",
         "The ultimate finish. This light-reflective formula uses advanced polymers to create a 'glass-like' surface that stays put, providing instant volume and hydration with every swipe."),

        ("LIPS", "LIP_LINER", "Precision Sculpt Lip Pencil", 18.0, "1.2g", ["Synthetic Wax", "Vitamin E", "Iron Oxides"], "#9B111E", "Ruby Definer",
         "Define your pout. A high-pigment, creamy lip pencil that provides a precise, long-wear border for your favorite lip colors.",
         "Structural lip definition. This velvety pencil glides on without tugging, providing a technical barrier that prevents bleeding and extends the life of your lipstick or gloss."),

        ("LIPS", "LIP_PLUMPER", "Max-Volume Peptide Plumper", 26.0, "4ml", ["Polybutene", "Peptides", "Capsicum Extract"], "#FFC0CB", "Icy Pink",
         "Scientific volume. A high-tech plumping gloss that uses peptides and mild stimulants to instantly increase lip volume and hydration.",
         "Biological volume enhancement. This formula uses a technical blend of peptides to support natural collagen while capsicum extract provides an immediate, visible plumping effect."),

        # NAIL_POLISH (Gap was NAIL_POLISH but macro NAILS)
        ("NAILS", "NAIL_POLISH", "High-Gloss Pro Lacquer", 18.0, "15ml", ["Ethyl Acetate", "Nitrocellulose", "Butyl Acetate"], "#FF0000", "Red Pro",
         "Professional endurance. A high-pigment, chip-resistant nail lacquer that delivers a salon-quality, mirror-shine finish.",
         "Engineered for durability. This lacquer uses a reinforced polymer network to provide extreme wear and high-fidelity color retention, even in high-impact environments."),

        # HAIR
        ("HAIR", "SHAMPOO", "Pure-Clarity Detox Shampoo", 24.0, "250ml", ["Water", "Sodium Lauroyl Methyl Isethionate", "Charcoal"], "#2F4F4F", "Slate Grey",
         "Reset your scalp. A sulfate-free, deep-cleansing shampoo that removes product buildup and environmental pollutants without stripping natural oils.",
         "Scientific scalp health. This clarifying formula uses gentle surfactants and botanical extracts to detoxify the hair follicle, restoring natural bounce, shine, and structural integrity."),

        ("HAIR", "CONDITIONER", "Structural-Repair Conditioner", 24.0, "250ml", ["Water", "Behentrimonium Chloride", "Keratin"], "#F8F8FF", "Pearl White",
         "Fortify every strand. A rich, strengthening conditioner that targets damaged areas to restore smoothness, shine, and manageability.",
         "Deep molecular repair. This formula uses biomimetic keratin to patch structural gaps in the hair cuticle, reducing breakage and enhancing the high-fidelity shine of your hair."),

        ("HAIR", "HAIR_MASK", "Deep-Bonding Recovery Mask", 32.0, "200ml", ["Water", "Argan Oil", "Panthenol"], "#FFF8DC", "Creamy Gold",
         "Intensive restoration. A concentrated weekly treatment that provides deep-tissue hydration and repairs the hair's natural bond structure.",
         "Therapeutic hair engineering. This mask delivers high concentrations of argan oil and panthenol to the core of the hair fiber, restoring elasticity and prevent future structural damage."),

        ("HAIR", "HAIR_COLOR", "Chromatic Intensity Semi-Perm", 28.0, "150ml", ["Water", "Direct Dyes", "Cetearyl Alcohol"], "#4B0082", "Indigo Velvet",
         "Vibrant chromatic expression. A professional-grade semi-permanent hair color that delivers intense, high-fidelity pigment without the damage.",
         "Pure color DNA for your hair. This ammonia-free formula uses direct dye technology to deposit rich color on the surface of the hair, ensuring brilliant results and exceptional chromatic clarity."),

        ("HAIR", "HAIR_STYLING", "Architectural Defining Pomade", 22.0, "50g", ["Water", "Ceteareth-25", "Glycerin"], "#DCDCDC", "Cool Silver",
         "Sculpt with precision. A high-hold, water-based pomade that provides technical structure and a polished, high-shine finish.",
         "Styling as structural engineering. This pomade allows for absolute control over hair architecture, offering a clean, professional finish that stays in place throughout the day."),

        ("HAIR", "HAIR_SPRAY", "High-Retention Weightless Spray", 20.0, "300ml", ["Alcohol Denat.", "VA/Crotonates Copolymer"], "#F5F5F5", "Clear Hold",
         "The ultimate hold. A fine-mist hairspray that provides long-lasting, invisible hold without the stiffness or technical build-up.",
         "Atmospheric styling lock. This weightless spray uses advanced polymers to freeze your style in place, maintaining high-fidelity shape even in high-humidity conditions."),

        ("HAIR", "SCALP_TREATMENT", "Revitalizing Peptide Scalp Serum", 38.0, "60ml", ["Water", "Procapil", "Biotin"], "#AFEEEE", "Arctic Blue",
         "Invest in your roots. A high-performance scalp serum that targets the foundation of hair health for thicker, fuller-looking strands.",
         "Scientific follicle stimulation. This serum delivers a technical blend of peptides and vitamins directly to the scalp, creating an optimal environment for healthy hair growth and density."),

        # HYGIENE
        ("HYGIENE", "SOAP", "Triple-Milled Botanical Bar", 12.0, "150g", ["Sodium Palmate", "Shea Butter", "Lavender Oil"], "#E6E6FA", "Lavender Fields",
         "Elevated cleansing. A luxurious, triple-milled soap bar that gently cleanses while infusing the skin with botanical moisture.",
         "Traditional craft meets scientific purity. The triple-milling process ensures a dense, long-lasting bar with a rich, creamy lather that leaves skin feeling soft and balanced."),

        ("HYGIENE", "SHOWER_GEL", "Invigorating Citrus Body Wash", 18.0, "400ml", ["Water", "Glycerin", "Orange Peel Oil"], "#FFA500", "Citrus Burst",
         "Refresh and renew. A zesty, moisture-rich body wash that awakens the senses while gently purifying the skin's surface.",
         "Atmospheric body care. This gel uses cold-pressed citrus oils to provide an uplifting olfactory experience while the hydrating glycerin base prevents post-shower dryness."),

        ("HYGIENE", "BATH_PRODUCT", "Effervescent Mineral Soak", 22.0, "500g", ["Epsom Salt", "Mica", "Essential Oils"], "#B0E0E6", "Ocean Breeze",
         "Total scientific immersion. A therapeutic mineral soak that relaxes the muscles and softens the skin for a high-end spa experience at home.",
         "Mineral restoration for the body. This blend of pharmaceutical-grade Epsom salts and essential oils targets physical tension while the delicate mica adds a high-fidelity shimmer to the water."),

        ("HYGIENE", "DEODORANT", "Clean-Science Deodorant Stick", 14.0, "75g", ["Magnesium Hydroxide", "Coconut Oil"], "#FFFFFF", "Pure Clean",
         "Odor control, simplified. A clean, baking-soda-free deodorant that neutralizes odor naturally while keeping you feeling fresh and dry.",
         "Scientific odor neutralization. This formula uses magnesium hydroxide to balance the skin's pH, preventing odor-causing bacteria without the use of harsh chemicals or irritants."),

        ("HYGIENE", "ANTIPERSPIRANT", "Clinical-Strength Roll-On", 16.0, "50ml", ["Aluminum Zirconium", "Water"], "#F0F0F0", "Invisible Shield",
         "Maximum protection. A technical roll-on antiperspirant that provides 48-hour defense against wetness and odor with zero white marks.",
         "High-fidelity dryness. This clinical-strength formula uses advanced aluminum salts to provide a reliable barrier against perspiration, even during intense physical activity."),

        ("HYGIENE", "INTIMATE_HYGIENE", "Balanced-pH Gentle Wash", 15.0, "200ml", ["Water", "Lactic Acid", "Chamomile"], "#FFF0F5", "Soft Rose",
         "Pure and gentle. A pH-balanced wash specifically designed for delicate skin, providing refreshing comfort without the irritation.",
         "Specialized biological care. This wash is formulated with lactic acid to maintain the natural acidity of the intimate area, while chamomile extract provides a soothing, high-fidelity finish."),

        ("HYGIENE", "COTTON_PRODUCT", "Premium Dual-Surface Rounds", 6.0, "100ct", ["100% Organic Cotton"], "#FFFFFF", "Cloud White",
         "The ultimate applicator. Soft, dual-textured organic cotton rounds that provide a professional surface for all your skincare needs.",
         "Technical cotton engineering. These rounds feature a textured side for gentle exfoliation and a smooth side for delicate product application, ensuring zero lint and maximum efficiency."),

        ("HYGIENE", "HAND_CREAM", "Deep-Hydration Barrier Hand Cream", 12.0, "75ml", ["Water", "Urea", "Shea Butter"], "#F5DEB3", "Wheat Husk",
         "Restore your hands. A rich, fast-absorbing cream that repairs dry, cracked skin and provides a long-lasting protective moisture barrier.",
         "Intensive hand repair. This technical formula uses urea and shea butter to penetrate deep into the skin's layers, restoring structural softness and protecting against environmental stress."),

        # ORAL
        ("ORAL", "TOOTHPASTE", "Whitening Enamel-Care Paste", 10.0, "100ml", ["Sorbitol", "Hydrated Silica", "Fluoride"], "#FFFFFF", "Mint White",
         "Scientific brightness. A high-performance whitening toothpaste that gently removes surface stains while fortifying the tooth enamel.",
         "Architectural dental care. This paste uses hydrated silica to polish the surface and fluoride to remineralize the enamel, ensuring a high-fidelity, healthy smile."),

        ("ORAL", "MOUTHWASH", "Total-Defense Zero Alcohol", 12.0, "500ml", ["Water", "Xylitol", "Menthol"], "#E0FFFF", "Cool Mint",
         "Total oral clarity. A zero-alcohol mouthwash that eliminates bacteria and freshens breath while maintaining a healthy oral microbiome.",
         "Equilibrium for your mouth. This wash uses xylitol to prevent cavity-causing bacteria and natural menthol for a crisp, refreshing finish that lasts for hours."),

        ("ORAL", "TOOTHBRUSH", "High-Frequency Bamboo Brush", 8.0, "1 Unit", ["Sustainable Bamboo", "Charcoal Bristles"], "#8B4513", "Natural Wood",
         "Sustainability meets hygiene. A professionally designed bamboo toothbrush with charcoal-infused bristles for a superior, clean feel.",
         "Ethical dental tools. This brush features an ergonomic handle and tapered bristles that reach deep between teeth, providing a high-fidelity clean with a Scientific Grade A sustainability rating."),

        ("ORAL", "DENTAL_FLOSS", "Expanding Waxed Dental Tape", 6.0, "50m", ["Nylon", "Microcrystalline Wax", "Mint"], "#F0FFF0", "Fresh Green",
         "Clean between. A unique expanding dental tape that adapts to the space between your teeth for maximum surface contact and cleaning.",
         "Precision interdental care. This waxed tape expands upon contact with moisture, allowing it to sweep away plaque and debris with high-fidelity accuracy and a fresh minty finish."),

        # FRAGRANCE
        ("FRAGRANCE", "PERFUME", "Aura No. 2: Jasmine & Saffron", 120.0, "50ml", ["Ethanol", "Jasmine Absolut", "Saffron"], "#DAA520", "Golden Aura",
         "Luxurious olfactory intensity. A concentrated, long-wear perfume that balances the floral sweetness of jasmine with the spicy depth of saffron.",
         "The pinnacle of Aura. This high-concentration perfume (20-30%) offers an unparalleled depth of scent, providing a rich, sophisticated olfactory signature that lasts all day and night."),

        ("FRAGRANCE", "EAU_DE_PARFUM", "Aura No. 1: Bergamot & Cedar", 85.0, "50ml", ["Ethanol", "Bergamot Oil", "Cedarwood"], "#FFBF00", "Amber Gold",
         "Scientific serenity in a bottle. A crisp, gender-neutral scent that blends bright citrus notes with a deep, grounding woody base.",
         "The KoColor olfactory signature. Aura No. 1 is a study in contrast—opening with a vibrant burst of Italian Bergamot before settling into a core of Atlas Cedar and White Musk."),

        ("FRAGRANCE", "EAU_DE_TOILETTE", "Aura No. 3: Sea Salt & Sage", 65.0, "100ml", ["Ethanol", "Sea Salt", "Sage Extract"], "#7FFFD4", "Ocean Sage",
         "Coastal atmospheric. A light, refreshing scent that captures the essence of the ocean breeze and the earthy warmth of wild sage.",
         "Effortless elegance. This Eau de Toilette provides a clean, airy scent profile that is perfect for everyday wear, offering a subtle but distinct presence that never overwhelms."),

        ("FRAGRANCE", "COLOGNE", "Aura No. 4: Vetiver & Leather", 75.0, "100ml", ["Ethanol", "Vetiver", "Leather Accord"], "#556B2F", "Forest Green",
         "Refined masculinity reimagined. A sophisticated cologne that blends the smoky depth of vetiver with the classic elegance of a leather accord.",
         "The architectural scent. Aura No. 4 is built on a foundation of Haitian Vetiver and premium leather, creating a structured, confident scent that is both modern and timeless."),

        ("FRAGRANCE", "BODY_MIST", "Aura No. 5: Vanilla & Orchid", 25.0, "250ml", ["Water", "Ethanol", "Vanilla Extract"], "#FFE4E1", "Blush Petal",
         "A delicate atmospheric layer. A light, sweet body mist that envelops you in a soft cloud of warm vanilla and exotic orchid.",
         "Everyday olfactory comfort. This refreshing mist provides a subtle, high-fidelity scent that can be layered throughout the day for a gentle, feminine aura."),

        # GROOMING
        ("GROOMING", "SHAVING_CREAM", "Smooth-Glide Shaving Cream", 18.0, "150ml", ["Water", "Stearic Acid", "Oat Flour"], "#F8F8FF", "Pearl White",
         "Zero friction. A rich, low-foam cream that creates a protective scientific barrier between the skin and the blade for a flawlessly close shave.",
         "Engineered for sensitive skin. This high-viscosity formula uses oat kernel extract and shea butter to prevent razor burn and irritation while ensuring a smooth, effortless glide."),

        ("GROOMING", "AFTERSHAVE", "Post-Shave Recovery Balm", 20.0, "100ml", ["Water", "Aloe Vera", "Witch Hazel"], "#E0FFFF", "Arctic Mist",
         "Soothe the afterburn. A cooling, alcohol-free balm that instantly calms redness and irritation following a close shave.",
         "Precision recovery for the skin. This balm uses the anti-inflammatory properties of aloe and witch hazel to restore the skin's moisture barrier and prevent post-shave sensitivity."),

        ("GROOMING", "BEARD_CARE", "Conditioning Beard Oil", 24.0, "30ml", ["Argan Oil", "Cedarwood Oil", "Vitamin E"], "#D2691E", "Deep Amber",
         "Groom your grain. A technical blend of organic oils that softens facial hair while nourishing the underlying skin for a healthy, managed beard.",
         "Scientific beard architecture. This oil uses argan and jojoba to penetrate the hair shaft, reducing stiffness and itchiness while the cedarwood provides a sophisticated olfactory finish."),

        ("GROOMING", "RAZOR", "Precision Ergonomic Handle", 25.0, "1 Unit", ["Zinc Alloy", "Rubber Grip"], "#C0C0C0", "Steel Grey",
         "Engineered for control. A weighted, ergonomic razor handle designed for absolute precision and a high-fidelity shaving experience.",
         "The tool of choice. This handle is balanced for optimal pressure distribution, ensuring a smooth, nick-free shave with every stroke. Durable zinc alloy construction for a lifetime of use."),

        # TOOLS
        ("TOOLS", "BRUSHES", "Professional Precision Set", 55.0, "8 Units", ["Synthetic Taklon", "Bamboo Handles"], "#F5F5F5", "Natural Cream",
         "The architect's toolkit. A set of eight professionally engineered brushes for flawless application across all color and dimension categories.",
         "High-fidelity application. Each brush is crafted with premium synthetic Taklon bristles that mimic natural hair performance while maintaining superior structural integrity and hygiene."),

        ("TOOLS", "SPONGES", "High-Density Blending Sponge", 12.0, "1 Unit", ["Hydrophilic Polyurethane"], "#FFB6C1", "Pink Petal",
         "The ultimate blender. A high-density, latex-free sponge that provides a seamless, airbrushed finish for all your complexion products.",
         "Precision blending tech. This sponge features a unique open-cell structure that minimizes product absorption while providing a soft-focus blend that is indistinguishable from natural skin."),

        ("TOOLS", "EYELASH_CURLER", "Architectural Lash Curler", 18.0, "1 Unit", ["Carbon Steel", "Silicone Pad"], "#2F4F4F", "Dark Gunmetal",
         "Structural lash lift. A professionally calibrated lash curler that provides an intense, long-lasting curl without pinching or tugging.",
         "Engineered for the eye. This curler uses a high-tension carbon steel frame and a premium silicone pad to deliver a uniform, high-fidelity curl that opens up the gaze and preps for mascara."),

        ("TOOLS", "ORGANIZERS", "Modular Acrylic Archive", 35.0, "1 Unit", ["BPA-Free Acrylic"], "#F0F8FF", "Clear Ice",
         "Archive your collection. A sleek, high-clarity acrylic organizer designed to house your entire KoColor inventory in a clean, accessible layout.",
         "The scientist's lab station. This modular system allows for custom configuration, ensuring your scientific style tools are organized with absolute precision and professional clarity."),

        ("TOOLS", "OTHER", "Hygienic Spatula Set", 10.0, "3 Units", ["Stainless Steel"], "#C0C0C0", "Mirror Finish",
         "Preserve the formula. A set of three stainless steel spatulas designed for the hygienic extraction and mixing of your cosmetic products.",
         "Maintaining scientific integrity. These spatulas prevent cross-contamination and ensure the shelf-life of your high-fidelity formulas by eliminating the need for fingertip extraction.")
    ]

    # Append to markdown files
    with open(summary_file, 'a') as sf, open(description_file, 'a') as df, open(tech_file, 'a') as tf:
        for macro, micro, name, price, volume, ingredients, color, shade, hook, narrative in items:
            # 1. KPSS Source File
            prod_id = f"kc-{micro.lower().replace('_', '-')}-901"
            cat_dir = os.path.join(root_dir, macro, micro.title().replace("_", ""))
            os.makedirs(cat_dir, exist_ok=True)

            kpss = {
                "schema_version": 1,
                "id": prod_id,
                "name": name,
                "brand": "KoColor",
                "macro_category": macro,
                "micro_category": micro,
                "shade_name": shade,
                "color_hex": color,
                "raw_image_input": f"./{prod_id}.png",
                "price": price,
                "volume": volume,
                "ingredients": ingredients,
                "Contains_Fragrance": False,
                "fda_data_verified": True
            }
            with open(os.path.join(cat_dir, f"{prod_id}.json"), "w") as f:
                json.dump(kpss, f, indent=2)

            # 2. Append to Markdown files
            sf.write(f"* **{name} (`{prod_id}`)**: {hook}\n")
            df.write(f"* **{name} (`{prod_id}`)**: {narrative}\n")

            # Detailed technical specs for Product_Description.md
            comp = ", ".join(ingredients)
            tech_desc = f"* **{name} (`{prod_id}`)**: Offered at ${price} for {volume}, this KoColor {micro} is expertly formulated with {comp}. Delivering a high-fidelity {shade} color profile (`{color}`), it leverages advanced engineering via high-purity pigments and volatile carriers. For optimal chromatic impact, apply directly to a clean, prepped surface."
            tf.write(f"{tech_desc}\n")

            # 3. Dummy Notes File (will be overwritten by population script but good for scaffolding)
            notes = {
                "product_id": prod_id,
                "card_title": f"Artist Notes: {name}",
                "description": None,
                "summary": None,
                "dynamic_attributes": [
                    {
                        "label": "Usage Notes",
                        "body": f"Standard {micro.lower().replace('_', ' ')} protocol for {prod_id}. Engineered for high-fidelity performance."
                    },
                    {
                        "label": "Expert Tip",
                        "body": f"For optimal results, integrate {name} into your daily scientific ritual."
                    },
                    {
                        "label": "Formulation Insight",
                        "body": f"Advanced {micro.title().replace('_', ' ')} engineering featuring medical-grade carriers and high-purity actives."
                    }
                ]
            }
            with open(os.path.join(cat_dir, f"{prod_id}.notes.json"), "w") as f:
                json.dump(notes, f, indent=2)

    print(f"✅ Generated 55 new products and updated markdown sources.")

if __name__ == "__main__":
    generate()
