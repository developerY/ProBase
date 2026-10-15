import com.google.firebase.ai.type.Schema

fun main() {
    val s = Schema.obj(
        mapOf(
            "selectedClothingIds" to Schema.array(Schema.str("Selected wardrobe item IDs")),
            "selectedCosmeticIds" to Schema.array(Schema.str("Selected cosmetic item IDs")),
            "rationale" to Schema.str("Harmonic synthesis rationale"),
            "recommendedPalette" to Schema.array(Schema.str("Hex color codes"))
        )
    )
}
