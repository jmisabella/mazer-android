package com.jmisabella.mazeq.screens.directioncontrols

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun EightWayControlScreen(performMove: (String) -> Unit) {
    Box(
        modifier = Modifier
            .padding(16.dp)
            .background(Color.White.copy(alpha = 0.8f), shape = RoundedCornerShape(16.dp))
            .shadow(4.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DPadButton(systemImage = "arrow.up.left", action = "UpperLeft", performMove = performMove)
                DPadButton(systemImage = "arrow.up", action = "Up", performMove = performMove)
                DPadButton(systemImage = "arrow.up.right", action = "UpperRight", performMove = performMove)
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DPadButton(systemImage = "arrow.left", action = "Left", performMove = performMove)
                Spacer(Modifier.size(44.dp))
                DPadButton(systemImage = "arrow.right", action = "Right", performMove = performMove)
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DPadButton(systemImage = "arrow.down.left", action = "LowerLeft", performMove = performMove)
                DPadButton(systemImage = "arrow.down", action = "Down", performMove = performMove)
                DPadButton(systemImage = "arrow.down.right", action = "LowerRight", performMove = performMove)
            }
        }
    }
}