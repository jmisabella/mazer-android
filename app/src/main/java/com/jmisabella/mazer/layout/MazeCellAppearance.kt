package com.jmisabella.mazer.layout

import androidx.compose.ui.graphics.Color
import kotlin.math.min
import kotlin.math.roundToInt
import com.jmisabella.mazer.models.MazeType
import com.jmisabella.mazer.models.MazeCell
import com.jmisabella.mazer.models.HeatMapPalette

fun String.toColor(): Color {
    val colorLong = this.trimStart('#').toLong(16)
    return Color(0xFF000000 or colorLong)
}

// Data class to hold color components (since Kotlin doesn't have built-in tuples)
data class ColorComponents(val red: Float, val green: Float, val blue: Float, val alpha: Float)

fun Color.components(): ColorComponents {
    return ColorComponents(red, green, blue, alpha)
}

fun interpolateColor(start: Color, end: Color, factor: Double): Color {
    val startComp = start.components()
    val endComp = end.components()
    val r = startComp.red + factor.toFloat() * (endComp.red - startComp.red)
    val g = startComp.green + factor.toFloat() * (endComp.green - startComp.green)
    val b = startComp.blue + factor.toFloat() * (endComp.blue - startComp.blue)
    val a = startComp.alpha + factor.toFloat() * (endComp.alpha - startComp.alpha)
    return Color(r, g, b, a)
}

fun wallStrokeWidth(mazeType: MazeType, cellSize: Float, density: Float): Float {
    val denominator: Float = when (mazeType) {
        MazeType.DELTA -> if (cellSize >= 18f) 3.65f else 2.35f
        MazeType.ORTHOGONAL -> if (cellSize >= 18f) 6f else 4.5f
        MazeType.SIGMA -> if (cellSize >= 18f) 1.8f else 1.6f
        MazeType.UPSILON -> if (cellSize >= 28f) 12f else 16f
//        MazeType.UPSILON -> if (cellSize >= 28f) 8f else 16f
        MazeType.RHOMBIC -> if (cellSize >= 28f) 2.25f else 1.8f
    }

    val raw = cellSize / denominator
    val snapped = (raw * density).roundToInt().toFloat() / density
    return if (mazeType == MazeType.DELTA) {
        val adjusted = snapped * 1.15f
        (adjusted * density).roundToInt().toFloat() / density
    } else {
        snapped
    }
}

fun cellBackgroundColor(
    cell: MazeCell,
    showSolution: Boolean,
    showHeatMap: Boolean,
    maxDistance: Int,
    selectedPalette: HeatMapPalette,
    isRevealedSolution: Boolean,
    defaultBackground: Color,
    totalRows: Int,
    optionalColor: Color?
): Color {
    if (cell.isStart) {
        return Color.Blue
    } else if (cell.isGoal) {
        return Color.Red
    } else if (cell.isVisited) {
        return CellColors.traversedPathColor
    } else if (isRevealedSolution) {
        return CellColors.solutionPathColor
    } else if (showHeatMap && maxDistance > 0) {
        val index = min(9, (cell.distance * 10) / maxDistance)
        return selectedPalette.shades[index].toColor()
    } else {
        if (totalRows > 1) {
            val startColor: Color = optionalColor?.let { color ->
                interpolateColor(defaultBackground, color, 0.17)
            } ?: interpolateColor(defaultBackground, Color.White, 0.9)
            val factor = cell.y.toDouble() / (totalRows - 1).toDouble()
            return interpolateColor(startColor, defaultBackground, factor)
        } else {
            return defaultBackground
        }
    }
}

object CellColors {
    val vividBlue = Color(104 / 255f, 232 / 255f, 255 / 255f)
    val solutionPathColor = Color(
        (104 + 128) / 2 / 255f,
        (232 + 128) / 2 / 255f,
        (255 + 128) / 2 / 255f
    )
    val traversedPathColor = Color(255 / 255f, 120 / 255f, 180 / 255f)
    val defaultCellBackgroundGray = Color(230 / 255f, 230 / 255f, 230 / 255f)
    val defaultCellBackgroundMint = Color(200 / 255f, 235 / 255f, 215 / 255f)
    val defaultCellBackgroundPeach = Color(255 / 255f, 215 / 255f, 200 / 255f)
    val defaultCellBackgroundLavender = Color(230 / 255f, 220 / 255f, 245 / 255f)
    val defaultCellBackgroundBlue = Color(215 / 255f, 230 / 255f, 255 / 255f)
    val solutionHighlight = Color(0xFF04D9FF)
    val offWhite = Color(0xFFFFF5E6)
    val orangeRed = Color(0xFFF66E6E)
    val lightGrey = Color(0xFF333333)
    val softOrange = Color(0xFFFFCCBC)
    val lightSkyBlue = Color(0xFFADD8E6)
    val lighterSky = Color(0xFFD6ECF3)
    val grayerSky = Color(0xFFDAEDEF)
    val lightModeSecondary = Color(0xFF333333)
    val barelyLavenderMostlyWhite = Color(0xFFFAF9FB)
    val softPastelPinkLight = Color(0xFFFADADD)
    val softPastelYellowLight = Color(0xFFFFFACD)
    val softPastelYellowishPink = Color(0xFFFFDAB9)
    val softPastelPurplishBlueLavender = Color(0xFFD7BDE2)
    val lighterSkyDarker = Color(0xFFA9DCED)
    val barelyLavenderMostlyWhiteDarker = Color(0xFFE0D7EA)
    val defaultCellBackgroundMintDarker = Color(0xFF9CE4BB)
    val defaultCellBackgroundPeachDarker = Color(0xFFFFB295)
    val defaultBackgroundColors: List<Color> = listOf(
        defaultCellBackgroundMint,
        defaultCellBackgroundPeach,
        offWhite,
        lighterSky,
        barelyLavenderMostlyWhite,
        lighterSkyDarker,
        barelyLavenderMostlyWhiteDarker,
        defaultCellBackgroundMintDarker,
        defaultCellBackgroundPeachDarker,
        softPastelPinkLight,
        softPastelYellowLight,
        softPastelYellowishPink,
        softPastelPurplishBlueLavender,
    )
}