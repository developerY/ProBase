package com.zoewave.probase.kocolor.features.starterpack.data.repository

import com.zoewave.probase.kocolor.db.KoColorDatabase
import com.zoewave.probase.kocolor.db.dao.ClothingDao
import com.zoewave.probase.kocolor.db.dao.CosmeticDao
import com.zoewave.probase.kocolor.db.dao.InstalledPackDao
import com.zoewave.probase.kocolor.db.dao.ShoppingCartDao
import com.zoewave.probase.kocolor.db.entity.ClothingUsageEntity
import com.zoewave.probase.kocolor.db.entity.InstalledPackEntity
import com.zoewave.probase.kocolor.db.entity.PackStatus
import com.zoewave.probase.kocolor.db.entity.ShoppingCartItemEntity
import com.zoewave.probase.kocolor.features.starterpack.data.StarterPackRepository
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.KcpsPayload
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.PackInfo
import com.zoewave.probase.kocolor.data.mapper.toEntity
import com.zoewave.probase.core.util.color.ColorQuantizer
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.ClothingItemDto
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.CosmeticItemDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PackSyncRepositoryImpl @Inject constructor(
    private val starterPackRepository: StarterPackRepository,
    private val database: KoColorDatabase,
    private val cosmeticDao: CosmeticDao,
    private val clothingDao: ClothingDao,
    private val installedPackDao: InstalledPackDao,
    private val shoppingCartDao: ShoppingCartDao
) : PackSyncRepository {

    override val cartProductIds: Flow<Set<String>> = 
        shoppingCartDao.getCartProductIdsFlow().map { it.toSet() }

    override val ownedProductIds: Flow<Set<String>> = combine(
        cosmeticDao.getOwnedCosmeticIds(),
        clothingDao.getOwnedClothingIds()
    ) { cosmetics, clothing ->
        (cosmetics + clothing).toSet()
    }

    override fun observeAllUsages(): Flow<List<ClothingUsageEntity>> {
        return database.garmentRotationDao.observeAllUsages()
    }

    override fun getInstalledPacks(): Flow<List<InstalledPackEntity>> {
        return installedPackDao.getAllInstalledPacks()
    }

    override suspend fun fetchManifest(): Result<List<PackInfo>> = runCatching {
        val envelope = starterPackRepository.getManifest()
        envelope.data.packs
    }

    override suspend fun ingestPack(pack: PackInfo): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            // 1. Mark as Downloading
            installedPackDao.insertPack(InstalledPackEntity(
                packId = pack.id,
                name = pack.name,
                description = pack.description,
                version = pack.version,
                status = PackStatus.DOWNLOADING,
                itemCount = pack.itemCount,
                sizeBytes = pack.compressedSizeBytes,
                hash = pack.sha256,
                packageHash = pack.sha256,
                heroImageUrl = pack.heroImageUrl,
                expiresAt = pack.expiresAt
            ))

            // 2. Fetch and Verify Package
            val payload = starterPackRepository.fetchVerifiedPackage(pack)
            
            // 3. Map and Persist
            importSelectedItems(pack.id, payload).getOrThrow()

            // 4. Finalize Installation
            installedPackDao.insertPack(InstalledPackEntity(
                packId = pack.id,
                name = pack.name,
                description = pack.description,
                version = pack.version,
                status = PackStatus.INSTALLED,
                itemCount = pack.itemCount,
                sizeBytes = pack.compressedSizeBytes,
                hash = pack.sha256,
                packageHash = pack.sha256,
                heroImageUrl = pack.heroImageUrl,
                expiresAt = pack.expiresAt
            ))
        }
    }

    override suspend fun importSelectedItems(packId: String, payload: KcpsPayload): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            // 1. Get the PackInfo from manifest
            val envelope = starterPackRepository.getManifest()
            val packInfo = envelope.data.packs.find { it.id == packId } 
                ?: throw Exception("Pack not found")

            // 2. Map to domain
            val (cosmeticItems, clothingItems) = starterPackRepository.mapToDomainItems(payload, packInfo)

            // 3. Map to entities
            val cosmeticEntities = cosmeticItems.map { item ->
                item.copy(colorFamily = ColorQuantizer.snapToFamily(item.colorHex)).toEntity()
            }
            val clothingEntities = clothingItems.map { item ->
                item.copy(colorFamily = ColorQuantizer.snapToFamily(item.colorHex)).toEntity()
            }
            
            // 4. Commit to DB
            cosmeticDao.insertCosmetics(cosmeticEntities)
            clothingDao.insertClothingList(clothingEntities)

            // 5. Update Installed Pack status
            installedPackDao.insertPack(InstalledPackEntity(
                packId = packInfo.id,
                name = packInfo.name,
                description = packInfo.description,
                version = packInfo.version,
                status = PackStatus.INSTALLED,
                itemCount = packInfo.itemCount,
                sizeBytes = packInfo.compressedSizeBytes,
                hash = packInfo.sha256,
                packageHash = packInfo.sha256,
                heroImageUrl = packInfo.heroImageUrl,
                expiresAt = packInfo.expiresAt
            ))

            // 6. Pre-fetch Images
            starterPackRepository.prefetchImages(payload)
        }
    }

    override suspend fun toggleCartItem(productId: String, packId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val existing = shoppingCartDao.getCartItem(productId)
            if (existing != null) {
                shoppingCartDao.removeFromCart(existing)
            } else {
                shoppingCartDao.addToCart(ShoppingCartItemEntity(productId, packId))
            }
        }
    }

    override suspend fun purchaseStagedProduct(productId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val cartItem = shoppingCartDao.getCartItem(productId) ?: throw Exception("Not in cart")
            
            val envelope = starterPackRepository.getManifest()
            val packInfo = envelope.data.packs.find { it.id == cartItem.packId } 
                ?: throw Exception("Pack not found")
                
            val items = starterPackRepository.getPackItems(cartItem.packId)
            val itemDto = items.find { it.id == productId } ?: throw Exception("Data not found")
            
            val payload = KcpsPayload(
                schemaVersion = 1,
                cosmetics = if (itemDto is CosmeticItemDto) listOf(itemDto) else emptyList(),
                clothing = if (itemDto is ClothingItemDto) listOf(itemDto) else emptyList()
            )
            
            val (cosmeticItems, clothingItems) = starterPackRepository.mapToDomainItems(payload, packInfo)
            val cosmeticEntities = cosmeticItems.map { it.copy(colorFamily = ColorQuantizer.snapToFamily(it.colorHex)).toEntity() }
            val clothingEntities = clothingItems.map { it.copy(colorFamily = ColorQuantizer.snapToFamily(it.colorHex)).toEntity() }

            database.purchaseStagedProduct(
                cosmeticEntities = cosmeticEntities,
                clothingEntities = clothingEntities,
                productId = productId
            )
        }
    }

    override suspend fun wipePack(packId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            cosmeticDao.deleteCosmeticsByPackId(packId)
            clothingDao.deleteClothingByPackId(packId)
            installedPackDao.deletePackRecord(packId)
        }
    }
}
