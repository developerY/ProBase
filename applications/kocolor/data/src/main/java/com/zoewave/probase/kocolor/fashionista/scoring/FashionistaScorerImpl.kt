package com.zoewave.probase.kocolor.fashionista.scoring

import com.zoewave.probase.kocolor.fashionista.domain.FashionistaFeatureVector
import com.zoewave.probase.kocolor.fashionista.domain.FashionistaObservation
import com.zoewave.probase.kocolor.fashionista.domain.FashionistaScore
import com.zoewave.probase.kocolor.fashionista.domain.FashionistaScorer
import com.zoewave.probase.kocolor.fashionista.extraction.ColorFeatureExtractor
import com.zoewave.probase.kocolor.fashionista.extraction.CompositionFeatureExtractor
import com.zoewave.probase.kocolor.fashionista.extraction.HierarchyFeatureExtractor
import com.zoewave.probase.kocolor.fashionista.extraction.SilhouetteFeatureExtractor
import com.zoewave.probase.kocolor.fashionista.extraction.TextureFeatureExtractor
import com.zoewave.probase.kocolor.fashionista.integration.OutfitIntegrationEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FashionistaScorerImpl @Inject constructor(
    private val colorExtractor: ColorFeatureExtractor,
    private val compositionExtractor: CompositionFeatureExtractor,
    private val silhouetteExtractor: SilhouetteFeatureExtractor,
    private val textureExtractor: TextureFeatureExtractor,
    private val hierarchyExtractor: HierarchyFeatureExtractor,
    private val integrationEngine: OutfitIntegrationEngine,
    private val deterministicScorer: DeterministicScorer,
    private val calibrationCurve: CalibrationCurve,
    private val calibration: FashionistaCalibration = FashionistaCalibration()
) : FashionistaScorer {

    override suspend fun score(outfit: FashionistaObservation): FashionistaScore = withContext(Dispatchers.Default) {
        // 1. Extract 6 Perceptual System FeatureValues
        val featureVector = FashionistaFeatureVector(
            composition = compositionExtractor.extract(outfit),
            colorHarmony = colorExtractor.extract(outfit),
            silhouette = silhouetteExtractor.extract(outfit),
            textureHarmony = textureExtractor.extract(outfit),
            visualHierarchy = hierarchyExtractor.extract(outfit),
            presentationIntegration = integrationEngine.evaluate(outfit)
        )

        // 2. Deterministic Scoring Model
        val result = deterministicScorer.calculateQ(featureVector, calibration)

        // Zero-Availability Fail-Safe Short-Circuit
        if (result.coverage == 0.0) {
            return@withContext FashionistaScore(
                score = 0.0,
                coverage = 0.0,
                standardId = calibration.standardId,
                standardVersion = calibration.version,
                breakdown = featureVector
            )
        }

        // 3. Calibration Curve Logistic Scaling
        val finalScore = calibrationCurve.mapToScore(result.q, calibration)

        FashionistaScore(
            score = finalScore,
            coverage = result.coverage,
            standardId = calibration.standardId,
            standardVersion = calibration.version,
            breakdown = featureVector
        )
    }
}
