package com.jmisabella.mazer.screens.directioncontrols

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FourWayDiagonalControlScreen(moveAction: (String) -> Unit) {
    val isDarkTheme = isSystemInDarkTheme()
    val alpha = if (isDarkTheme) 0.6f else 0.75f
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background.copy(alpha = alpha), RoundedCornerShape(32.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            DPadButton(
                systemImage = "arrow.up.left",
                action = "UpperLeft",
                performMove = moveAction
            )
            DPadButton(
                systemImage = "arrow.up.right",
                action = "UpperRight",
                performMove = moveAction
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            DPadButton(
                systemImage = "arrow.down.left",
                action = "LowerLeft",
                performMove = moveAction
            )
            DPadButton(
                systemImage = "arrow.down.right",
                action = "LowerRight",
                performMove = moveAction
            )
        }
    }
}
