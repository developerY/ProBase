package com.zoewave.probase.photodo.mobile.features.tasks.domain

import com.zoewave.probase.applications.photodo.db.entity.CategoryEntity
import com.zoewave.probase.applications.photodo.db.entity.PhotoEntity
import com.zoewave.probase.applications.photodo.db.entity.TaskItemEntity
import com.zoewave.probase.applications.photodo.db.entity.TaskListEntity
import com.zoewave.probase.applications.photodo.db.repo.PhotoDoRepo
import javax.inject.Inject

class DebugTaskDevTools @Inject constructor(
    private val repo: PhotoDoRepo
) : TaskDevTools {

    override suspend fun seedDatabase() {
        val timestamp = System.currentTimeMillis()

        // 1. Create Categories with manual IDs so we can link them
        val realEstateId = (1000..1999).random().toLong()
        val devId = (2000..2999).random().toLong()
        val businessId = (3000..3999).random().toLong()

        repo.insertCategory(CategoryEntity(categoryId = realEstateId, name = "Real Estate", description = "Property management"))
        repo.insertCategory(CategoryEntity(categoryId = devId, name = "Development", description = "Software projects"))
        repo.insertCategory(CategoryEntity(categoryId = businessId, name = "Business", description = "Brands and marketing"))

        // 2. Create Task Lists (Projects) with the NEW AI FLAGS
        val prefabId = (4000..4999).random().toLong()
        repo.insertTaskList(
            TaskListEntity(
                listId = prefabId,
                categoryId = realEstateId, // FK REFERENCE
                name = "Boxabl PreFab Home Build",
                notes = "Foundation and assembly",
                isUrgent = true,   // ❗ Will show the red exclamation
                isFavorite = true  // ❤️ Will show the filled heart
            )
        )

        repo.insertTaskList(
            TaskListEntity(
                listId = (5000..5999).random().toLong(),
                categoryId = devId, // FK REFERENCE
                name = "AshBike Mobile App",
                notes = "WatchOS and Android integration",
                isUrgent = false,
                isFavorite = true
            )
        )

        repo.insertTaskList(
            TaskListEntity(
                listId = (6000..6999).random().toLong(),
                categoryId = businessId, // FK REFERENCE
                name = "KoColor Brand Launch",
                notes = "Marketing and supply chain",
                isUrgent = true,
                isFavorite = false
            )
        )

        // 3. Create Multiple Task Items attached to the PreFab List
        val prefabTasks = listOf(
            "Inspect foundation wiring phase 1",
            "Inspect foundation wiring phase 2",
            "Schedule city plumbing sign-off"
        )

        prefabTasks.forEachIndexed { index, taskText ->
            val newTaskItem = TaskItemEntity(
                listId = prefabId, // FK REFERENCE to the specific TaskList
                text = taskText,
                isChecked = index % 2 == 0 // Randomly complete alternating tasks
            )
            repo.insertTaskItem(newTaskItem)
        }

        // 4. Attach Photos to the PreFab List
        for (j in 1..2) {
            val newPhoto = PhotoEntity(
                listId = prefabId, // FK REFERENCE
                photoUri = "content://media/external/images/media/${(100..999).random()}",
                caption = "Mock inspection photo $j",
                timestamp = timestamp
            )
            repo.insertPhoto(newPhoto)
        }
    }
}