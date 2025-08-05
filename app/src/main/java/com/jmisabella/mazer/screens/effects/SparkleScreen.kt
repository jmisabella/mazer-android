package com.jmisabella.mazer.screens.effects

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.EaseInQuad
import androidx.compose.animation.core.EaseOutQuad
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import kotlinx.coroutines.delay
import kotlin.random.Random

class Sparkle(
    val id: Int,
    val x: Double,
    val y: Double,
    val size: Double,
    val symbol: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color
) {
    var phase by mutableStateOf(0)
}

@Composable
fun SparkleScreen(
    count: Int = 60,
    totalDuration: Float = 3f,
    onFinished: () -> Unit = {}
) {
    val symbols = listOf(
        Icons.Default.AutoAwesome,
        Icons.Filled.Star,
        Icons.Filled.Circle
    )
    val colors: List<Color> = listOf(
        Color.Yellow,
        Color.Magenta,
        Color(0xFF98FB98), // Mint
        Color(0xFFFFA500)  // Orange
    )
    val sparkles = remember { mutableStateListOf<Sparkle>() }
    val density = LocalDensity.current

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val widthInDp = with(density) { constraints.maxWidth.toDp().value }
        val heightInDp = with(density) { constraints.maxHeight.toDp().value }

        sparkles.forEach { sparkle ->
            val targetOpacity by animateFloatAsState(
                targetValue = when (sparkle.phase) {
                    1, 2 -> 1f
                    else -> 0f
                },
                animationSpec = when (sparkle.phase) {
                    1 -> tween((totalDuration * 0.12f * 1000).toInt(), easing = EaseOutQuad)
                    3 -> tween((totalDuration * 0.12f * 1000).toInt(), easing = EaseInQuad)
                    else -> snap()
                },
                label = "opacity_${sparkle.id}"
            )

            val targetScale by animateFloatAsState(
                targetValue = if (sparkle.phase >= 1) 1f else 0.1f,
                animationSpec = if (sparkle.phase == 1) {
                    tween((totalDuration * 0.12f * 1000).toInt(), easing = EaseOutQuad)
                } else {
                    snap()
                },
                label = "scale_${sparkle.id}"
            )

            // Calculate position with bounds checking
            val xPos = (sparkle.x * widthInDp).coerceIn(0.0, (widthInDp - sparkle.size).coerceAtLeast(0.0))
            val yPos = (sparkle.y * heightInDp).coerceIn(0.0, (heightInDp - sparkle.size).coerceAtLeast(0.0))

            Icon(
                imageVector = sparkle.symbol,
                contentDescription = null,
                tint = sparkle.color,
                modifier = Modifier
                    .size(sparkle.size.dp)
                    .graphicsLayer {
                        alpha = targetOpacity
                        scaleX = targetScale
                        scaleY = targetScale
                    }
                    .offset(x = xPos.dp, y = yPos.dp)
            )
        }
    }

    LaunchedEffect(count, totalDuration) {
        sparkles.clear() // Clear existing sparkles to prevent duplicates
        val stagger = totalDuration * 0.5f / count.coerceAtLeast(1)
        val newSparkles = (0 until count).map { i ->
            Sparkle(
                id = i,
                x = Random.nextDouble(0.0, 1.0),
                y = Random.nextDouble(0.0, 1.0),
                size = Random.nextDouble(30.0, 60.0),
                symbol = symbols.random(),
                color = colors.random()
            )
        }
        sparkles.addAll(newSparkles)

        newSparkles.forEachIndexed { i, sparkle ->
            delay((i * stagger * 1000f).toLong())
            sparkle.phase = 1
        }

        delay(((totalDuration + 0.5f) * 1000f).toLong())
        sparkles.clear()
        onFinished()
    }

    sparkles.forEach { sparkle ->
        LaunchedEffect(sparkle.phase) {
            val fadeInDur = totalDuration * 0.12f
            val fadeOutDur = totalDuration * 0.12f
            val visibleDur = totalDuration * 0.5f
            when (sparkle.phase) {
                1 -> {
                    delay((fadeInDur * 1000f).toLong())
                    sparkle.phase = 2
                }
                2 -> {
                    delay((visibleDur * 1000f).toLong())
                    sparkle.phase = 3
                }
                3 -> {
                    delay((fadeOutDur * 1000f).toLong())
                    sparkle.phase = 4
                }
            }
        }
    }
}