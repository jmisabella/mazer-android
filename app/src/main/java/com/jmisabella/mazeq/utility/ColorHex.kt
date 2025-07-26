package com.jmisabella.mazeq.utility

import androidx.compose.ui.graphics.Color

fun String.toColor(): Color {
    var hex = this.trim().replace("#", "")
    if (hex.length != 6) return Color.White // Fallback to white
    val intValue = hex.toIntOrNull(16) ?: return Color.White
    val r = (intValue shr 16) and 0xFF
    val g = (intValue shr 8) and 0xFF
    val b = intValue and 0xFF
    return Color(r, g, b)
}