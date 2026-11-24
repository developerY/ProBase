package com.zoewave.probase.ashbike.mobile

import com.zoewave.probase.ashbike.features.main.navigation.AshBikeDestination
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class Nav3DestinationsSerializationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Test
    fun ashBikeDestination_Home_roundTripSerialization() {
        val original: AshBikeDestination = AshBikeDestination.Home
        val encoded = json.encodeToString(AshBikeDestination.serializer(), original)
        val decoded = json.decodeFromString(AshBikeDestination.serializer(), encoded)
        assertEquals(original, decoded)
    }

    @Test
    fun ashBikeDestination_Trips_roundTripSerialization() {
        val original: AshBikeDestination = AshBikeDestination.Trips
        val encoded = json.encodeToString(AshBikeDestination.serializer(), original)
        val decoded = json.decodeFromString(AshBikeDestination.serializer(), encoded)
        assertEquals(original, decoded)
    }

    @Test
    fun ashBikeDestination_Settings_roundTripSerialization() {
        val original: AshBikeDestination = AshBikeDestination.Settings(sectionToExpand = "app_prefs")
        val encoded = json.encodeToString(AshBikeDestination.serializer(), original)
        val decoded = json.decodeFromString(AshBikeDestination.serializer(), encoded)
        assertEquals(original, decoded)
    }

    @Test
    fun ashBikeDestination_AdvancedBikeSettings_roundTripSerialization() {
        val original: AshBikeDestination = AshBikeDestination.AdvancedBikeSettings
        val encoded = json.encodeToString(AshBikeDestination.serializer(), original)
        val decoded = json.decodeFromString(AshBikeDestination.serializer(), encoded)
        assertEquals(original, decoded)
    }

    @Test
    fun ashBikeDestination_RideDetail_roundTripSerialization() {
        val original: AshBikeDestination = AshBikeDestination.RideDetail(rideId = "ride-123-abc")
        val encoded = json.encodeToString(AshBikeDestination.serializer(), original)
        val decoded = json.decodeFromString(AshBikeDestination.serializer(), encoded)
        assertEquals(original, decoded)
    }
}
