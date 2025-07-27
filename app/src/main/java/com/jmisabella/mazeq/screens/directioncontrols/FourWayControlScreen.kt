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
fun FourWayControlScreen(performMove: (String) -> Unit) {
    Box(
        modifier = Modifier
            .padding(12.dp)
            .background(Color.White.copy(alpha = 0.8f), shape = RoundedCornerShape(12.dp))
            .shadow(4.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DPadButton(systemImage = "arrow.left", action = "Left", performMove = performMove)
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DPadButton(systemImage = "arrow.up", action = "Up", performMove = performMove)
                DPadButton(systemImage = "arrow.down", action = "Down", performMove = performMove)
            }
            DPadButton(systemImage = "arrow.right", action = "Right", performMove = performMove)
        }
    }
}