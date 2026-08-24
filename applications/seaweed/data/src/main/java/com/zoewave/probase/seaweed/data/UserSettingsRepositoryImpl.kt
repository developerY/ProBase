package com.zoewave.probase.seaweed.data

import com.zoewave.probase.core.data.repository.SecureApiKeyRepository
import com.zoewave.probase.seaweed.database.UserSettingsDao
import com.zoewave.probase.seaweed.database.toDomain
import com.zoewave.probase.seaweed.database.toEntity
import com.zoewave.probase.seaweed.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserSettingsRepositoryImpl @Inject constructor(
    private val dao: UserSettingsDao,
    private val secureApiKeyRepository: SecureApiKeyRepository
) : UserSettingsRepository {

    override fun getUserSettings(): Flow<UserSettings> =
        dao.getUserSettings().map { it?.toDomain() ?: UserSettings() }

    override suspend fun saveUserSettings(settings: UserSettings) {
        dao.saveUserSettings(settings.toEntity())
    }

    // --- AiConfigurationSettings Implementation ---

    override val isAiEnabledFlow: Flow<Boolean> = getUserSettings().map { it.isAiEnabled }

    override suspend fun saveAiEnabled(enabled: Boolean) {
        val current = getUserSettings().first()
        saveUserSettings(current.copy(isAiEnabled = enabled))
    }

    override val aiModelFlow: Flow<String> = getUserSettings().map { it.aiModel }

    override suspend fun saveAiModel(model: String) {
        val current = getUserSettings().first()
        saveUserSettings(current.copy(aiModel = model))
    }

    override fun getGeminiApiKey(): String? = secureApiKeyRepository.getKey()

    override val isGeminiApiKeySetFlow: Flow<Boolean> = secureApiKeyRepository.isKeySetFlow

    override suspend fun saveGeminiApiKey(apiKey: String?) {
        if (apiKey == null) {
            secureApiKeyRepository.deleteKey()
        } else {
            secureApiKeyRepository.saveKey(apiKey)
        }
    }

    // --- SmartCaptureSettings Implementation ---

    override val userApiKeyFlow: Flow<String?> = isGeminiApiKeySetFlow.map { if (it) getGeminiApiKey() else null }
    override val userAiModelFlow: Flow<String> = aiModelFlow

    override val useFirebaseVertexAi: Flow<Boolean> = getUserSettings().map { it.useFirebaseVertexAi }

    override suspend fun saveUseFirebaseVertexAi(enabled: Boolean) {
        val current = getUserSettings().first()
        saveUserSettings(current.copy(useFirebaseVertexAi = enabled))
    }
}
