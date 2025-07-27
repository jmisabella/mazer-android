package com.jmisabella.mazeq.screens.directioncontrols

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun FourWayDiagonalControlScreen(performMove: (String) -> Unit) {
    Box(
        modifier = Modifier
            .padding(12.dp)
            .background(Color.White.copy(alpha = 0.8f), shape = RoundedCornerShape(12.dp))
            .shadow(4.dp)
            .size(120.dp)
    ) {
        Box {
            DPadButton(
                systemImage = "arrow.up.right",
                action = "UpperRight",
                performMove = performMove,
                modifier = Modifier.offset(x = 28.dp, y = (-28).dp)
            )
            DPadButton(
                systemImage = "arrow.down.right",
                action = "LowerRight",
                performMove = performMove,
                modifier = Modifier.offset(x = 28.dp, y = 28.dp)
            )
            DPadButton(
                systemImage = "arrow.down.left",
                action = "LowerLeft",
                performMove = performMove,
                modifier = Modifier.offset(x = (-28).dp, y = 28.dp)
            )
            DPadButton(
                systemImage = "arrow.up.left",
                action = "UpperLeft",
                performMove = performMove,
                modifier = Modifier.offset(x = (-28).dp, y = (-28).dp)
            )
        }
    }
}