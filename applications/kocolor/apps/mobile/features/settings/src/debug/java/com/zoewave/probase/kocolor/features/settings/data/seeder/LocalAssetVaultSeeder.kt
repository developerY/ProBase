package com.zoewave.probase.kocolor.features.settings.data.seeder

import android.content.Context
import com.zoewave.probase.kocolor.db.dao.ClothingDao
import com.zoewave.probase.kocolor.db.dao.CosmeticDao
import com.zoewave.probase.kocolor.features.settings.domain.seeder.VaultSeeder
import com.zoewave.probase.kocolor.features.settings.domain.seeder.model.CosmeticSeedDto
import com.zoewave.probase.kocolor.features.settings.domain.seeder.model.WardrobeSeedDto
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject

class LocalAssetVaultSeeder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cosmeticDao: CosmeticDao,
    private val clothingDao: ClothingDao
) : VaultSeeder {

    private val json = Json { 
        ignoreUnknownKeys = true 
        coerceInputValues = true
    }

    override suspend fun wipeAndSeedDatabase(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            // 1. Load Cosmetics
            val cosmeticsJson = context.assets.open("seed_cosmetics.json").bufferedReader().use { it.readText() }
            val cosmeticDtos = json.decodeFromString<List<CosmeticSeedDto>>(cosmeticsJson)
            
            // 2. Load Wardrobe
            val wardrobeJson = context.assets.open("seed_wardrobe.json").bufferedReader().use { it.readText() }
            val wardrobeDtos = json.decodeFromString<List<WardrobeSeedDto>>(wardrobeJson)

            // 3. Clear existing data
            cosmeticDao.deleteAllCosmetics()
            clothingDao.deleteAllClothing()

            // 4. Insert new data
            cosmeticDtos.forEach { cosmeticDao.insertCosmetic(it.toEntity()) }
            wardrobeDtos.forEach { clothingDao.insertClothing(it.toEntity()) }
        }
    }
}
