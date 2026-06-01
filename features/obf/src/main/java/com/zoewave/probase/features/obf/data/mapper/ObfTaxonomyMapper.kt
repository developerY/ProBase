package com.zoewave.probase.features.obf.data.mapper

import com.zoewave.probase.kocolor.model.MicroCategory

object ObfTaxonomyMapper {

    /**
     * Translates messy OBF category tags into strict KoColor MicroCategories.
     */
    fun extractMicroCategory(tags: List<String>?): MicroCategory {
        if (tags == null) return MicroCategory.AI_PENDING

        // Convert all tags to lowercase for safe matching
        val normalizedTags = tags.map { it.lowercase() }

        return when {
            // Makeup
            normalizedTags.any { it.contains("foundation") || it.contains("fond-de-teint") } -> MicroCategory.FOUNDATION
            normalizedTags.any { it.contains("concealer") || it.contains("anti-cernes") } -> MicroCategory.CONCEALER
            normalizedTags.any { it.contains("lipstick") || it.contains("rouge-a-levres") } -> MicroCategory.LIPSTICK
            normalizedTags.any { it.contains("mascara") } -> MicroCategory.MASCARA
            normalizedTags.any { it.contains("blush") || it.contains("fard à joues") } -> MicroCategory.BLUSH
            normalizedTags.any { it.contains("eyeshadow") || it.contains("fard à paupières") } -> MicroCategory.EYESHADOW
            normalizedTags.any { it.contains("eyeliner") } -> MicroCategory.EYELINER
            normalizedTags.any { it.contains("bb-and-cc-creams") || it.contains("bb-creams") } -> MicroCategory.BB_CC_CREAM
            normalizedTags.any { it.contains("face-powders") || it.contains("poudre") } -> MicroCategory.FACE_POWDER
            normalizedTags.any { it.contains("lip-glosses") || it.contains("brillant à lèvres") } -> MicroCategory.LIP_GLOSS
            normalizedTags.any { it.contains("eyebrow-makeup") || it.contains("sourcils") } -> MicroCategory.BROW_GEL
            
            // Skincare
            normalizedTags.any { it.contains("facial-cleansers") || it.contains("nettoyant visage") } -> MicroCategory.CLEANSER
            normalizedTags.any { it.contains("moisturizing-creams") || it.contains("crème hydratante") } -> MicroCategory.MOISTURIZER
            normalizedTags.any { it.contains("serums") || it.contains("sérum") } -> MicroCategory.SERUM
            normalizedTags.any { it.contains("sunscreen") || it.contains("solaire") || it.contains("sun-creams") } -> MicroCategory.SPF
            normalizedTags.any { it.contains("toners") || it.contains("tonique") } -> MicroCategory.TONER
            normalizedTags.any { it.contains("face-masks") || it.contains("masque visage") } -> MicroCategory.FACE_MASK
            normalizedTags.any { it.contains("exfoliants-and-scrubs") || it.contains("exfoliant") } -> MicroCategory.EXFOLIANT
            normalizedTags.any { it.contains("eye-contour-care") || it.contains("contour des yeux") } -> MicroCategory.EYE_CARE
            normalizedTags.any { it.contains("lip-care") || it.contains("soin des lèvres") } -> MicroCategory.LIP_CARE
            
            // Hair
            normalizedTags.any { it.contains("shampoos") || it.contains("shampooing") } -> MicroCategory.SHAMPOO
            normalizedTags.any { it.contains("conditioners") || it.contains("après-shampooing") } -> MicroCategory.CONDITIONER
            normalizedTags.any { it.contains("hair-masks") || it.contains("masque capillaire") } -> MicroCategory.HAIR_MASK
            normalizedTags.any { it.contains("hair-coloration") || it.contains("coloration") } -> MicroCategory.HAIR_COLOR
            normalizedTags.any { it.contains("hair-styling") } -> MicroCategory.HAIR_STYLING
            normalizedTags.any { it.contains("hairsprays") } -> MicroCategory.HAIR_SPRAY
            normalizedTags.any { it.contains("scalp-treatments") } -> MicroCategory.SCALP_TREATMENT
            
            // Hygiene
            normalizedTags.any { it.contains("soaps") || it.contains("savon") } -> MicroCategory.SOAP
            normalizedTags.any { it.contains("shower-gels") || it.contains("gel douche") } -> MicroCategory.SHOWER_GEL
            normalizedTags.any { it.contains("bath-products") || it.contains("bain") } -> MicroCategory.BATH_PRODUCT
            normalizedTags.any { it.contains("deodorants") } -> MicroCategory.DEODORANT
            normalizedTags.any { it.contains("antiperspirants") } -> MicroCategory.ANTIPERSPIRANT
            normalizedTags.any { it.contains("feminine-hygiene") } -> MicroCategory.INTIMATE_HYGIENE
            normalizedTags.any { it.contains("cotton-pads") } -> MicroCategory.COTTON_PRODUCT
            normalizedTags.any { it.contains("hand-creams") || it.contains("crème mains") } -> MicroCategory.HAND_CREAM
            
            // Oral
            normalizedTags.any { it.contains("toothpastes") || it.contains("dentifrice") } -> MicroCategory.TOOTHPASTE
            normalizedTags.any { it.contains("mouthwashes") || it.contains("bain de bouche") } -> MicroCategory.MOUTHWASH
            normalizedTags.any { it.contains("toothbrushes") || it.contains("brosse à dents") } -> MicroCategory.TOOTHBRUSH
            normalizedTags.any { it.contains("dental-floss") || it.contains("fil dentaire") } -> MicroCategory.DENTAL_FLOSS
            
            // Fragrance
            normalizedTags.any { it.contains("perfumes") || it.contains("parfum") } -> MicroCategory.PERFUME
            normalizedTags.any { it.contains("eau-de-parfum") } -> MicroCategory.EAU_DE_PARFUM
            normalizedTags.any { it.contains("eau-de-toilette") } -> MicroCategory.EAU_DE_TOILETTE
            normalizedTags.any { it.contains("colognes") } -> MicroCategory.COLOGNE
            normalizedTags.any { it.contains("body-mists") } -> MicroCategory.BODY_MIST
            
            // Grooming
            normalizedTags.any { it.contains("shaving-creams") || it.contains("rasage") } -> MicroCategory.SHAVING_CREAM
            normalizedTags.any { it.contains("aftershaves") } -> MicroCategory.AFTERSHAVE
            normalizedTags.any { it.contains("beard-care") } -> MicroCategory.BEARD_CARE
            normalizedTags.any { it.contains("razors") } -> MicroCategory.RAZOR
            
            // Tools
            normalizedTags.any { it.contains("nail-polishes") || it.contains("vernis") } -> MicroCategory.NAIL_POLISH
            
            else -> MicroCategory.AI_PENDING
        }
    }

    /**
     * Extracts comma-separated INCI ingredients into a clean list.
     */
    fun parseIngredients(ingredientsText: String?): List<String> {
        if (ingredientsText.isNullOrBlank()) return emptyList()
        return ingredientsText
            .split(",")
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }
    }

    /**
     * Heuristically determines the Finish from product keywords.
     */
    fun extractFinish(keywords: List<String>?): com.zoewave.probase.kocolor.model.Finish {
        if (keywords == null) return com.zoewave.probase.kocolor.model.Finish.UNKNOWN
        val k = keywords.map { it.lowercase() }
        return when {
            k.contains("matte") || k.contains("ultramatte") -> com.zoewave.probase.kocolor.model.Finish.MATTE
            k.contains("satin") -> com.zoewave.probase.kocolor.model.Finish.SATIN
            k.contains("glossy") || k.contains("brillant") -> com.zoewave.probase.kocolor.model.Finish.GLOSSY
            k.contains("dewy") || k.contains("radiant") || k.contains("éclat") -> com.zoewave.probase.kocolor.model.Finish.RADIANT
            k.contains("metallic") -> com.zoewave.probase.kocolor.model.Finish.METALLIC
            k.contains("glitter") -> com.zoewave.probase.kocolor.model.Finish.GLITTER
            else -> com.zoewave.probase.kocolor.model.Finish.NATURAL
        }
    }

    /**
     * Heuristically determines the Formulation from product keywords.
     */
    fun extractFormulation(keywords: List<String>?): com.zoewave.probase.kocolor.model.Formulation {
        if (keywords == null) return com.zoewave.probase.kocolor.model.Formulation.UNKNOWN
        val k = keywords.map { it.lowercase() }
        return when {
            k.contains("liquid") || k.contains("liquide") || k.contains("ink") -> com.zoewave.probase.kocolor.model.Formulation.LIQUID
            k.contains("powder") || k.contains("poudre") -> com.zoewave.probase.kocolor.model.Formulation.POWDER
            k.contains("cream") || k.contains("crème") -> com.zoewave.probase.kocolor.model.Formulation.CREAM
            k.contains("stick") -> com.zoewave.probase.kocolor.model.Formulation.STICK
            k.contains("balm") || k.contains("baume") -> com.zoewave.probase.kocolor.model.Formulation.BALM
            k.contains("gel") -> com.zoewave.probase.kocolor.model.Formulation.GEL
            k.contains("spray") -> com.zoewave.probase.kocolor.model.Formulation.SPRAY
            else -> com.zoewave.probase.kocolor.model.Formulation.UNKNOWN
        }
    }

    /**
     * Determines Chemistry Base based on top ingredients.
     */
    fun extractChemistryBase(ingredients: List<String>): com.zoewave.probase.kocolor.model.ChemistryBase {
        val top = ingredients.firstOrNull()?.lowercase() ?: return com.zoewave.probase.kocolor.model.ChemistryBase.UNKNOWN
        return when {
            top.contains("water") || top.contains("aqua") -> com.zoewave.probase.kocolor.model.ChemistryBase.WATER
            top.contains("dimethicone") || top.contains("siloxane") || top.contains("silicone") -> com.zoewave.probase.kocolor.model.ChemistryBase.SILICONE
            top.contains("oil") || top.contains("huile") -> com.zoewave.probase.kocolor.model.ChemistryBase.OIL
            top.contains("wax") || top.contains("cire") -> com.zoewave.probase.kocolor.model.ChemistryBase.WAX
            else -> com.zoewave.probase.kocolor.model.ChemistryBase.UNKNOWN
        }
    }

    /**
     * Translates allergen tags into human-readable strings.
     */
    fun extractAllergens(tags: List<String>?): List<String> {
        if (tags == null) return emptyList()
        return tags.map { it.replace("en:", "").replace("-", " ").replaceFirstChar { c -> c.uppercase() } }
    }

    /**
     * Checks if the product is vegan based on analysis tags.
     */
    fun isVegan(tags: List<String>?): Boolean? {
        if (tags == null) return null
        return tags.any { it.contains("vegan") }
    }

    /**
     * Checks if the product is cruelty-free.
     */
    fun isCrueltyFree(keywords: List<String>?): Boolean? {
        if (keywords == null) return null
        return keywords.any { it.lowercase().contains("cruelty-free") || it.lowercase().contains("not-tested-on-animals") }
    }
}
