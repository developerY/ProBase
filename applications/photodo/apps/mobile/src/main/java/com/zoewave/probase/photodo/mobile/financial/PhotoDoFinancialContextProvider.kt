package com.zoewave.probase.photodo.mobile.financial

import com.zoewave.probase.core.model.FinancialContextProvider
import com.zoewave.probase.applications.photodo.db.repo.PhotoDoRepo
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import javax.inject.Inject

class PhotoDoFinancialContextProvider @Inject constructor(
    private val repo: PhotoDoRepo
) : FinancialContextProvider {

    override suspend fun getFinancialContext(): String? {
        val categories = repo.getCategoriesWithProjectsAndTasks().first()
        if (categories.isEmpty()) return null
        
        return buildJsonObject {
            put("currency", "USD")
            putJsonObject("projects") {
                categories.forEach { categoryWithData ->
                    categoryWithData.projects.forEach { projectWithTasks ->
                        val project = projectWithTasks.project
                        put(project.name.lowercase(), project.projectBudget - project.currentSpend)
                    }
                }
            }
        }.toString()
    }
}
