package com.zoewave.probase.features.readers.ocr.data.model

import com.zoewave.probase.seaweed.model.finance.validation.OcrElement

data class OcrExtractionResult(
    val pageNumber: Int,
    val imageWidthPx: Int,
    val imageHeightPx: Int,
    val elements: List<OcrElement>,
    val singularTransformCount: Int
)
