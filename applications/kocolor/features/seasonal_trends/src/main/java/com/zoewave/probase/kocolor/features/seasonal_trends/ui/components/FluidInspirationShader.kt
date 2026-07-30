package com.zoewave.probase.kocolor.features.seasonal_trends.ui.components

import android.graphics.RuntimeShader
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import org.intellij.lang.annotations.Language
import android.graphics.RenderEffect as AndroidRenderEffect

@Language("AGSL")
const val FLUID_DISTORTION_SHADER = """
    uniform shader image;
    uniform float2 resolution;
    uniform float time;

    float2 distort(float2 uv, float t) {
        float2 p = uv;
        p.x += 0.05 * sin(p.y * 10.0 + t);
        p.y += 0.05 * sin(p.x * 10.0 + t * 1.2);
        
        // Add more viscous swirling
        p.x += 0.02 * sin(p.y * 20.0 - t * 0.5);
        p.y += 0.02 * cos(p.x * 20.0 + t * 0.8);
        
        return p;
    }

    half4 main(float2 fragCoord) {
        float2 uv = fragCoord / resolution;
        float2 distortedUv = distort(uv, time);
        return image.eval(distortedUv * resolution);
    }
"""

fun Modifier.fluidDistortion(
    time: Float,
    width: Float,
    height: Float
): Modifier = this.graphicsLayer {
    if (width <= 0f || height <= 0f) return@graphicsLayer
    
    val shader = RuntimeShader(FLUID_DISTORTION_SHADER)
    shader.setFloatUniform("resolution", width, height)
    shader.setFloatUniform("time", time)
    
    renderEffect = AndroidRenderEffect.createRuntimeShaderEffect(shader, "image").asComposeRenderEffect()
}
