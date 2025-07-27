package com.jmisabella.mazeq.screens.directioncontrols

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

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

    val icon = when (systemImage) {
        "arrow.up" -> Icons.Default.ArrowDropUp
        "arrow.down" -> Icons.Default.ArrowDropDown
        "arrow.left" -> Icons.Default.ArrowLeft
        "arrow.right" -> Icons.Default.ArrowRight
        "arrow.up.left" -> Icons.Default.ArrowUpward // Approximate, as Compose doesn't have exact equivalents
        "arrow.up.right" -> Icons.Default.ArrowUpward // Adjust as needed
        "arrow.down.left" -> Icons.Default.ArrowDownward // Adjust as needed
        "arrow.down.right" -> Icons.Default.ArrowDownward // Adjust as needed
        else -> Icons.Default.ArrowForward // Fallback
    }

    Icon(
        imageVector = icon,
        contentDescription = "Move $action",
        modifier = modifier
            .size(44.dp)
            .background(Color.Gray, shape = CircleShape)
            .scale(scale)
            .clickable(interactionSource = interactionSource, indication = null) {
                performMove(action)
            },
        tint = Color.White
    )
}