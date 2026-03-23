package com.zoewave.probase.photodo.mobile.features.tasks.domain

import com.zoewave.probase.applications.photodo.db.entity.PhotoEntity
import com.zoewave.probase.applications.photodo.db.repo.PhotoDoRepo
import javax.inject.Inject

class AddPhotoToTaskUseCase @Inject constructor(
    private val repo: PhotoDoRepo
) {
    // The operator fun allows you to call the class like a function!
    suspend operator fun invoke(listId: Long, uriString: String) {
        val newPhoto = PhotoEntity(
            listId = listId,
            photoUri = uriString,
            timestamp = System.currentTimeMillis()
        )
        repo.insertPhoto(newPhoto)
    }
}