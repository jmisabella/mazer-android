// screens/mazecomponents/SigmaCellScreen.kt (add modifier param and pass to Canvas)
package com.jmisabella.mazer.screens.mazecomponents

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.jmisabella.mazer.layout.cellBackgroundColor
import com.jmisabella.mazer.layout.wallStrokeWidth
import com.jmisabella.mazer.models.Coordinates
import com.jmisabella.mazer.models.HexDirection
import com.jmisabella.mazer.models.HeatMapPalette
import com.jmisabella.mazer.models.MazeCell
import com.jmisabella.mazer.models.MazeType
import kotlin.math.abs
import kotlin.math.sqrt

@Composable
fun SigmaCellScreen(
    cell: MazeCell,
    cellSize: Float,
    showSolution: Boolean,
    showHeatMap: Boolean,
    selectedPalette: HeatMapPalette,
    maxDistance: Int,
    isRevealedSolution: Boolean,
    defaultBackgroundColor: Color,
    optionalColor: Color?,
    totalRows: Int,
    cellMap: Map<Coordinates, MazeCell>,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current.density
    val cellSizePx = cellSize * density
    val h = sqrt(3f)
    val hexHeightPx = h * cellSizePx
    val strokeWidth = wallStrokeWidth(MazeType.SIGMA, cellSize, density)
    val overlap = 1f
    val factor = overlap / cellSizePx

    val unitPoints = listOf(
        Offset(0.5f, 0f),
        Offset(1.5f, 0f),
        Offset(2f, h / 2),
        Offset(1.5f, h),
        Offset(0.5f, h),
        Offset(0f, h / 2)
    )

    val scaledPoints = unitPoints.map { Offset(it.x * cellSizePx, it.y * cellSizePx) }

    val c = Offset(cellSizePx, hexHeightPx / 2)
    val adjustedPoints = scaledPoints.map { p ->
        val dx = p.x - c.x
        val dy = p.y - c.y
        Offset(p.x + factor * dx, p.y + factor * dy)
    }

    val fillColor = cellBackgroundColor(
        cell = cell,
        showSolution = showSolution,
        showHeatMap = showHeatMap,
        maxDistance = maxDistance,
        selectedPalette = selectedPalette,
        isRevealedSolution = isRevealedSolution,
        defaultBackground = defaultBackgroundColor,
        totalRows = totalRows,
        optionalColor = optionalColor
    )

    Canvas(
        modifier = modifier
            .size((2 * cellSize).dp, (h * cellSize).dp)
    ) {
        val fillPath = Path().apply {
            moveTo(adjustedPoints[0].x, adjustedPoints[0].y)
            adjustedPoints.drop(1).forEach { lineTo(it.x, it.y) }
            close()
        }
        drawPath(fillPath, color = fillColor)

        val strokePath = Path()
        val isOddCol = cell.x % 2 == 1
        HexDirection.values().forEach { dir ->
            val linked = cell.linked.contains(dir.rawValue)
            val (dq, dr) = dir.offsetDelta(isOddCol)
            val neighborCoord = Coordinates(cell.x + dq, cell.y + dr)
            val neighbor = cellMap[neighborCoord] ?: return@forEach

            if (cell.onSolutionPath && neighbor.onSolutionPath && abs(cell.distance - neighbor.distance) == 1) return@forEach

            val neighborLink = neighbor.linked.contains(dir.opposite.rawValue)

            if (!linked && !neighborLink) {
                val (i, j) = dir.vertexIndices
                strokePath.moveTo(adjustedPoints[i].x, adjustedPoints[i].y)
                strokePath.lineTo(adjustedPoints[j].x, adjustedPoints[j].y)
            }
        }
        drawPath(strokePath, color = Color.Black, style = Stroke(width = strokeWidth))
    }
}

//@Composable
//fun SigmaCellScreen(
//    cell: MazeCell,
//    cellSize: Float,
//    showSolution: Boolean,
//    showHeatMap: Boolean,
//    selectedPalette: HeatMapPalette,
//    maxDistance: Int,
//    isRevealedSolution: Boolean,
//    defaultBackgroundColor: Color,
//    optionalColor: Color?,
//    totalRows: Int,
//    cellMap: Map<Coordinates, MazeCell>,
//    modifier: Modifier = Modifier
//) {
//    val density = LocalDensity.current.density
//    val strokeWidth = wallStrokeWidth(MazeType.SIGMA, cellSize, density)
//    val overlap = 1f / density
//    val factor = overlap / cellSize
//
//    val h = sqrt(3f)
//    val unitPoints = listOf(
//        Offset(0.5f, 0f),
//        Offset(1.5f, 0f),
//        Offset(2f, h / 2),
//        Offset(1.5f, h),
//        Offset(0.5f, h),
//        Offset(0f, h / 2)
//    )
//
//    val scaledPoints = unitPoints.map { Offset(it.x * cellSize, it.y * cellSize) }
//
//    val c = Offset(cellSize, (h / 2) * cellSize)
//    val adjustedPoints = scaledPoints.map { p ->
//        val dx = p.x - c.x
//        val dy = p.y - c.y
//        Offset(p.x + factor * dx, p.y + factor * dy)
//    }
//
//    val fillColor = cellBackgroundColor(
//        cell = cell,
//        showSolution = showSolution,
//        showHeatMap = showHeatMap,
//        maxDistance = maxDistance,
//        selectedPalette = selectedPalette,
//        isRevealedSolution = isRevealedSolution,
//        defaultBackground = defaultBackgroundColor,
//        totalRows = totalRows,
//        optionalColor = optionalColor
//    )
//
//    Canvas(
//        modifier = modifier
//            .size((2 * cellSize).dp, (h * cellSize).dp)
//    ) {
//        val fillPath = Path().apply {
//            moveTo(adjustedPoints[0].x, adjustedPoints[0].y)
//            adjustedPoints.drop(1).forEach { lineTo(it.x, it.y) }
//            close()
//        }
//        drawPath(fillPath, color = fillColor)
//
//        val strokePath = Path()
//        val isOddCol = cell.x % 2 == 1
//        HexDirection.values().forEach { dir ->
//            val linked = cell.linked.contains(dir.rawValue)
//            val (dq, dr) = dir.offsetDelta(isOddCol)
//            val neighborCoord = Coordinates(cell.x + dq, cell.y + dr)
//            val neighbor = cellMap[neighborCoord] ?: return@forEach
//
//            if (cell.onSolutionPath && neighbor.onSolutionPath && abs(cell.distance - neighbor.distance) == 1) return@forEach
//
//            val neighborLink = neighbor.linked.contains(dir.opposite.rawValue)
//
//            if (!linked && !neighborLink) {
//                val (i, j) = dir.vertexIndices
//                strokePath.moveTo(adjustedPoints[i].x, adjustedPoints[i].y)
//                strokePath.lineTo(adjustedPoints[j].x, adjustedPoints[j].y)
//            }
//        }
//        drawPath(strokePath, color = Color.Black, style = Stroke(width = strokeWidth))
//    }
//}
