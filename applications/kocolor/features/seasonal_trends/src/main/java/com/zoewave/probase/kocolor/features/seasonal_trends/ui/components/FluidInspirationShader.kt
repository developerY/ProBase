package com.zoewave.probase.kocolor.features.seasonal_trends.ui.components

import android.graphics.RuntimeShader
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import org.intellij.lang.annotations.Language
import android.graphics.RenderEffect as AndroidRenderEffect

@Language("AGSL")
const val FROSTED_GLASS_SHADER = """
    uniform shader image;
    uniform float2 resolution;
    uniform float time;
    uniform float frostAmount; // 0.0 (sharp) to 1.0 (frosted)

    // Pseudo-random noise for the frost grain
    float hash(float2 p) {
        p = fract(p * float2(123.34, 456.21));
        p += dot(p, p + 45.32);
        return fract(p.x * p.y);
    }

    half4 main(float2 fragCoord) {
        float2 uv = fragCoord / resolution;
        
        // 1. Tactile Jitter: Displace UVs slightly based on high-frequency noise
        // This creates the "etched" look of frosted glass.
        float2 noise = float2(
            hash(uv + fract(time * 0.1)), 
            hash(uv + fract(time * 0.1) + 1.0)
        ) - 0.5;
        
        // Subtle distortion for refraction effect
        float2 refractUv = uv + noise * 0.02 * frostAmount;
        
        half4 color = image.eval(refractUv * resolution);
        
        // 2. Grainy Overlay: Add a subtle shimmering grit
        float grain = hash(fragCoord + time) * 0.08 * frostAmount;
        color.rgb += half3(grain);
        
        // 3. Luminance Lift: Standard "Milky" look for frosted glass
        color.rgb = mix(color.rgb, half3(1.0), 0.15 * frostAmount);
        
        return color;
    }
"""

fun Modifier.frostedGlass(
    time: Float,
    frostAmount: Float,
    width: Float,
    height: Float
): Modifier = this.graphicsLayer {
    if (width <= 0f || height <= 0f) return@graphicsLayer
    
    val shader = RuntimeShader(FROSTED_GLASS_SHADER)
    shader.setFloatUniform("resolution", width, height)
    shader.setFloatUniform("time", time)
    shader.setFloatUniform("frostAmount", frostAmount)
    
    renderEffect = AndroidRenderEffect.createRuntimeShaderEffect(shader, "image").asComposeRenderEffect()
}
