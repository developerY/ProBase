package com.zoewave.probase.features.fda.data.remote

import com.google.gson.annotations.SerializedName

data class FdaResponse<T>(
    val meta: FdaMeta,
    val results: List<T>?
)

data class FdaMeta(
    val last_updated: String,
    val results: FdaResultsMeta
)

data class FdaResultsMeta(
    val skip: Int,
    val limit: Int,
    val total: Int
)

data class FdaEnforcementResult(
    val product_description: String,
    val status: String,
    val recall_number: String,
    val reason_for_recall: String,
    val center_classification_date: String
)

data class FdaEventResult(
    val reactions: List<String>,
    val products: List<FdaEventProduct>
)

data class FdaEventProduct(
    val product_name: String
)

data class FdaLabelResult(
    val active_ingredient: List<String>?,
    val purpose: List<String>?,
    val warnings: List<String>?
)
