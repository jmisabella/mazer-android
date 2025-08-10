package com.jmisabella.mazer.screens.directioncontrols

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
fun DPadButton(
    systemImage: String,
    action: String,
    performMove: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.9f else 1.0f, label = "button scale")
    val opacity by animateFloatAsState(if (isPressed) 0.6f else 1.0f, label = "button opacity")

    val backgroundColor = Color.Gray

    val rotation = when (systemImage) {
        "arrow.right" -> 0f
        "arrow.down.right" -> 45f
        "arrow.down" -> 90f
        "arrow.down.left" -> 135f
        "arrow.left" -> 180f
        "arrow.up.left" -> 225f
        "arrow.up" -> 270f
        "arrow.up.right" -> 315f
        else -> 0f
    }

    Icon(
        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDescription = "Move $action",
        modifier = modifier
            .size(44.dp)
            .background(backgroundColor, shape = CircleShape)
            .scale(scale)
            .graphicsLayer {
                rotationZ = rotation
                alpha = opacity
            }
            .clickable(interactionSource = interactionSource, indication = null) {
                performMove(action)
            },
        tint = Color.White
    )
}

