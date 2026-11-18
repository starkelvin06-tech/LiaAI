package com.Lia.assistant.ui.orb

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.Lia.assistant.ui.theme.ErrorRed
import com.Lia.assistant.ui.theme.IndigoMid
import com.Lia.assistant.ui.theme.IndigoNight
import com.Lia.assistant.ui.theme.Marigold
import com.Lia.assistant.ui.theme.PearlWhite
import com.Lia.assistant.voice.NovaOrbState
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * 3D GPU-shaded Pearl Orb.
 * Renders multiple radial gradients to simulate a spherical pearl of light
 * with state-driven breathing, voice amplitude reactivity, and edge glow.
 */
@Composable
fun LiaOrb(
    state: NovaOrbState,
    amplitude: Float,
    reducedMotion: Boolean = false,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "OrbTransitions")

    // Breathing pulse
    val breatheScale by transition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "OrbBreathe"
    )

    // Shimmer rotation angle for THINKING state
    val shimmerAngle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "OrbShimmer"
    )

    // Flash alpha for CONNECTING state
    val connectingAlpha by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "OrbConnecting"
    )

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = min(size.width, size.height) * 0.38f

            // Compute dynamic scale and glow based on state and reducedMotion
            val dynamicScale = when {
                reducedMotion -> 1.0f
                state == NovaOrbState.SPEAKING -> 1.0f + (amplitude.coerceIn(0f, 1f) * 0.28f)
                state == NovaOrbState.LISTENING -> 1.0f + (amplitude.coerceIn(0f, 1f) * 0.16f)
                state == NovaOrbState.IDLE -> breatheScale
                state == NovaOrbState.CONNECTING -> connectingAlpha
                else -> 1.0f
            }

            val sphereRadius = baseRadius * dynamicScale

            // Colors based on state
            val primaryGlowColor = when (state) {
                NovaOrbState.ERROR -> ErrorRed
                NovaOrbState.THINKING -> PearlWhite
                else -> Marigold
            }

            // 1. Wide Outer Atmospheric Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryGlowColor.copy(alpha = if (state == NovaOrbState.SPEAKING) 0.55f else 0.30f),
                        primaryGlowColor.copy(alpha = 0.10f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = sphereRadius * 1.85f
                ),
                radius = sphereRadius * 1.85f,
                center = center
            )

            // 2. Halo Corona Ring
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryGlowColor.copy(alpha = 0.70f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = sphereRadius * 1.25f
                ),
                radius = sphereRadius * 1.25f,
                center = center
            )

            // 3. Main 3D Pearl Sphere (Offset light source towards top-left)
            val lightSourceOffset = Offset(
                center.x - sphereRadius * 0.28f,
                center.y - sphereRadius * 0.32f
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        PearlWhite,
                        PearlWhite.copy(alpha = 0.95f),
                        primaryGlowColor.copy(alpha = 0.85f),
                        IndigoMid.copy(alpha = 0.95f),
                        IndigoNight
                    ),
                    center = lightSourceOffset,
                    radius = sphereRadius * 1.15f
                ),
                radius = sphereRadius,
                center = center
            )

            // 4. Specular Highlight Dot
            val specularOffset = Offset(
                center.x - sphereRadius * 0.34f,
                center.y - sphereRadius * 0.38f
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        Color.White.copy(alpha = 0.5f),
                        Color.Transparent
                    ),
                    center = specularOffset,
                    radius = sphereRadius * 0.22f
                ),
                radius = sphereRadius * 0.22f,
                center = specularOffset
            )

            // 5. Orbital Edge Ring (for THINKING / SPEAKING states)
            if (state == NovaOrbState.THINKING && !reducedMotion) {
                val rad = Math.toRadians(shimmerAngle.toDouble())
                val orbitOffset = Offset(
                    center.x + (sphereRadius * 1.15f * cos(rad)).toFloat(),
                    center.y + (sphereRadius * 1.15f * sin(rad)).toFloat()
                )
                drawCircle(
                    color = primaryGlowColor.copy(alpha = 0.85f),
                    radius = 8.dp.toPx(),
                    center = orbitOffset
                )
            }

            drawCircle(
                color = primaryGlowColor.copy(alpha = 0.45f),
                radius = sphereRadius * 1.08f,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}
