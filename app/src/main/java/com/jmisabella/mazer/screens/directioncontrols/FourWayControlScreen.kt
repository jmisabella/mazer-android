package com.jmisabella.mazer.screens.directioncontrols

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FourWayControlScreen(moveAction: (String) -> Unit) {
    val isDarkTheme = isSystemInDarkTheme()
    val alpha = if (isDarkTheme) 0.6f else 0.75f
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background.copy(alpha = alpha), RoundedCornerShape(64.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DPadButton(systemImage = "arrow.left", action = "Left", performMove = moveAction)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            DPadButton(systemImage = "arrow.up", action = "Up", performMove = moveAction)
            DPadButton(systemImage = "arrow.down", action = "Down", performMove = moveAction)
        }
        DPadButton(systemImage = "arrow.right", action = "Right", performMove = moveAction)
    }
}

