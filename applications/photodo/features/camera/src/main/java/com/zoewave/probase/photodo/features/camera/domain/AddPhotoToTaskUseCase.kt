package com.zoewave.probase.photodo.features.camera.domain

import com.zoewave.probase.applications.photodo.db.entity.PhotoEntity
import com.zoewave.probase.applications.photodo.db.repo.PhotoDoRepo
import javax.inject.Inject

class AddPhotoToTaskUseCase @Inject constructor(
    private val repo: PhotoDoRepo
) {
    // The operator fun allows you to call the class like a function!
    suspend operator fun invoke(projectId: Long, uriString: String) {
        val newPhoto = PhotoEntity(
            projectId = projectId,
            photoUri = uriString,
            timestamp = System.currentTimeMillis()
        )
        repo.upsertPhoto(newPhoto)
    }
}
