package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.random.Random

private data class ConfettiParticle(
    val startXFraction: Float,
    val startYFraction: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val size: Float,
    val rotationSpeed: Float,
    val isCircle: Boolean
)

/**
 * High-performance lightweight particle confetti burst using Compose Canvas.
 * Automatically triggers whenever triggerKey changes to a value > 0.
 */
@Composable
fun ConfettiOverlay(
    triggerKey: Long,
    modifier: Modifier = Modifier
) {
    if (triggerKey <= 0) return

    val progress = remember(triggerKey) { Animatable(0f) }

    // Generate confetti particles
    val particles = remember(triggerKey) {
        val colors = listOf(
            Color(0xFF34D399), // Mint
            Color(0xFFFBBF24), // Amber
            Color(0xFF38BDF8), // Cyan
            Color(0xFFFB7185), // Coral
            Color(0xFFA78BFA), // Purple
            Color(0xFFF43F5E), // Rose
            Color(0xFFFFD700)  // Gold
        )
        List(60) {
            ConfettiParticle(
                startXFraction = 0.5f + (Random.nextFloat() - 0.5f) * 0.4f,
                startYFraction = 0.35f,
                vx = (Random.nextFloat() - 0.5f) * 800f,
                vy = -Random.nextFloat() * 700f - 200f,
                color = colors[Random.nextInt(colors.size)],
                size = Random.nextFloat() * 10f + 8f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 720f,
                isCircle = Random.nextBoolean()
            )
        }
    }

    LaunchedEffect(triggerKey) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2200, easing = LinearEasing)
        )
    }

    if (progress.value < 1f) {
        Box(modifier = modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val t = progress.value
                val gravity = 980f // downward acceleration
                val alpha = (1f - t * 0.95f).coerceIn(0f, 1f)

                particles.forEach { p ->
                    val x = size.width * p.startXFraction + p.vx * t
                    // Physics motion: y = y0 + vy*t + 0.5*g*t^2
                    val y = size.height * p.startYFraction + (p.vy * t) + (0.5f * gravity * t * t)
                    val currentRotation = p.rotationSpeed * t

                    if (x in -50f..(size.width + 50f) && y in -50f..(size.height + 50f)) {
                        rotate(degrees = currentRotation, pivot = Offset(x, y)) {
                            if (p.isCircle) {
                                drawCircle(
                                    color = p.color.copy(alpha = alpha),
                                    radius = p.size / 2f,
                                    center = Offset(x, y)
                                )
                            } else {
                                drawRect(
                                    color = p.color.copy(alpha = alpha),
                                    topLeft = Offset(x - p.size / 2f, y - p.size / 3f),
                                    size = Size(p.size, p.size * 0.65f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
