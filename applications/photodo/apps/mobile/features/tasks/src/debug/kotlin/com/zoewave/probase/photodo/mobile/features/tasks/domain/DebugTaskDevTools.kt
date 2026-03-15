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

        // 1. Create a Root Category
        // We manually assign the ID here so we know exactly what to pass to the TaskList
        val mockCategoryId = (1..1000).random().toLong()
        val newCategory = CategoryEntity(
            categoryId = mockCategoryId,
            name = "Mock Category $mockCategoryId",
            description = "Auto-generated test data",
            imageUri = "content://media/external/images/media/${(1..50).random()}"
        )
        repo.insertCategory(newCategory)

        // 2. Create a Task List attached to the Category
        // Again, manual ID so we can link the TaskItems and Photos to it
        val mockListId = (1001..2000).random().toLong()
        val newList = TaskListEntity(
            listId = mockListId,
            categoryId = mockCategoryId, // FK REFERENCE to Category
            name = "Project: PreFab Home Setup"
        )
        repo.insertTaskList(newList)

        // 3. Create Multiple Task Items attached to the List
        for (i in 1..3) {
            val newTaskItem = TaskItemEntity(
                // Notice we DO NOT pass 'itemId'. It defaults to 0, and Room auto-generates it!
                listId = mockListId, // FK REFERENCE to TaskListEntity
                text = "Inspect foundation wiring phase $i",
                isChecked = i % 2 == 0 // Randomly complete alternating tasks
            )
            repo.insertTaskItem(newTaskItem)
        }

        // 4. Attach Photos to the Task List
        for (j in 1..2) {
            val newPhoto = PhotoEntity(
                // Notice we DO NOT pass 'photoId'. It defaults to 0, and Room auto-generates it!
                listId = mockListId, // FK REFERENCE to TaskListEntity
                photoUri = "content://media/external/images/media/${(100..999).random()}",
                caption = "Mock inspection photo $j",
                timestamp = timestamp
            )
            repo.insertPhoto(newPhoto)
        }
    }
}