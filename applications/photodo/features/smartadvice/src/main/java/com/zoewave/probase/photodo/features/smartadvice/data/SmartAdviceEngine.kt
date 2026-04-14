package com.zoewave.probase.photodo.features.smartadvice.data

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.zoewave.probase.applications.photodo.db.entity.ProjectDetails
import com.zoewave.probase.photodo.features.smartadvice.domain.ProjectAdvice
import kotlinx.serialization.json.Json
import javax.inject.Inject

class SmartAdviceEngine @Inject constructor() {

    private val json = Json { 
        ignoreUnknownKeys = true 
        coerceInputValues = true
    }

    suspend fun getAdvice(
        project: ProjectDetails,
        categoryName: String,
        apiKey: String,
        modelName: String = "gemini-1.5-flash"
    ): ProjectAdvice {
        val generativeModel = GenerativeModel(
            modelName = modelName,
            apiKey = apiKey,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
            }
        )

        val taskList = project.tasks.joinToString("\n") { "- ${it.text} (${if (it.isChecked) "Completed" else "Pending"})" }
        
        val prompt = content {
            text("""
                You are an expert project management consultant. Analyze this project and provide helpful advice.
                
                PROJECT CONTEXT:
                - Name: ${project.project.name}
                - Category: $categoryName
                - Budget: $${project.project.projectBudget}
                - Duration Estimate: ${project.project.notes ?: "Not specified"}
                - Tasks:
                $taskList
                
                GOAL:
                Provide a structured analysis including a summary, actionable tips, potential risks, budget/time optimization advice, and 3-5 suggested tasks the user might have missed.
                
                Respond ONLY with a valid JSON object matching this exact schema:
                {
                  "summary": "string",
                  "tips": ["string"],
                  "potentialRisks": ["string"],
                  "budgetAdvice": "string or null",
                  "timeAdvice": "string or null",
                  "suggestedChecklistItems": ["string"]
                }
            """.trimIndent())
        }

        return try {
            val response = generativeModel.generateContent(prompt)
            val jsonText = response.text ?: throw IllegalStateException("Empty response from AI")
            json.decodeFromString<ProjectAdvice>(jsonText)
        } catch (e: Exception) {
            ProjectAdvice(
                summary = "Error getting advice: ${e.localizedMessage}",
                tips = listOf("Check your internet connection", "Ensure your API key is valid")
            )
        }
    }
}
