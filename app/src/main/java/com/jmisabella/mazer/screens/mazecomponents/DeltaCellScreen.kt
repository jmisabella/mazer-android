package com.jmisabella.mazer.screens.mazecomponents

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jmisabella.mazer.layout.cellBackgroundColor
import com.jmisabella.mazer.layout.wallStrokeWidth
import com.jmisabella.mazer.models.HeatMapPalette
import com.jmisabella.mazer.models.MazeCell
import com.jmisabella.mazer.models.MazeType
import kotlin.math.roundToInt
import kotlin.math.sqrt

@Composable
fun DeltaCellScreen(
    cell: MazeCell,
    cellSize: Dp,
    showSolution: Boolean,
    showHeatMap: Boolean,
    selectedPalette: HeatMapPalette,
    maxDistance: Int,
    isRevealedSolution: Boolean,
    defaultBackgroundColor: Color,
    optionalColor: Color?,
    totalRows: Int
) {
    val density = LocalDensity.current.density
    val strokeWidthPx = wallStrokeWidth(MazeType.DELTA, cellSize.value, density)
    val cellSizePx = cellSize.value * density
    val triangleHeightPx = cellSizePx * sqrt(3f) / 2f
    val overlapPx = 1f // 1 pixel overlap
    val extensionLengthPx = 0.5f // 0.5 pixel extension

    fun snap(value: Float): Float = (value * density).roundToInt().toFloat() / density

    val points: List<Offset> = if (cell.orientation.lowercase() == "normal") {
        listOf(
            Offset(snap(cellSizePx / 2f), snap(0f) - overlapPx),
            Offset(snap(0f) - overlapPx, snap(triangleHeightPx) + overlapPx),
            Offset(snap(cellSizePx) + overlapPx, snap(triangleHeightPx) + overlapPx)
        )
    } else {
        listOf(
            Offset(snap(0f) - overlapPx, snap(0f) - overlapPx),
            Offset(snap(cellSizePx) + overlapPx, snap(0f) - overlapPx),
            Offset(snap(cellSizePx / 2f), snap(triangleHeightPx) + overlapPx)
        )
    }

    fun extendLine(start: Offset, end: Offset, extension: Float): Pair<Offset, Offset> {
        val dx = end.x - start.x
        val dy = end.y - start.y
        val length = sqrt(dx * dx + dy * dy)
        if (length == 0f) return Pair(start, end)
        val unitDx = dx / length
        val unitDy = dy / length
        val extDx = unitDx * extension
        val extDy = unitDy * extension
        return Pair(
            Offset(start.x - extDx, start.y - extDy),
            Offset(end.x + extDx, end.y + extDy)
        )
    }

    val backgroundColor = cellBackgroundColor(
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
        modifier = Modifier
            .size(width = cellSize, height = (triangleHeightPx / density).dp)
    ) {
        // Fill the triangle
        val fillPath = Path().apply {
            moveTo(points[0].x, points[0].y)
            lineTo(points[1].x, points[1].y)
            lineTo(points[2].x, points[2].y)
            close()
        }
        drawPath(fillPath, color = backgroundColor)

        // Stroke the walls
        val strokePath = Path()
        if (cell.orientation.lowercase() == "normal") {
            if (!cell.linked.contains("UpperLeft")) {
                val (newStart, newEnd) = extendLine(points[0], points[1], extensionLengthPx)
                strokePath.moveTo(newStart.x, newStart.y)
                strokePath.lineTo(newEnd.x, newEnd.y)
            }
            if (!cell.linked.contains("UpperRight")) {
                val (newStart, newEnd) = extendLine(points[0], points[2], extensionLengthPx)
                strokePath.moveTo(newStart.x, newStart.y)
                strokePath.lineTo(newEnd.x, newEnd.y)
            }
            if (!cell.linked.contains("Down")) {
                val (newStart, newEnd) = extendLine(points[1], points[2], extensionLengthPx)
                strokePath.moveTo(newStart.x, newStart.y)
                strokePath.lineTo(newEnd.x, newEnd.y)
            }
        } else {
            if (!cell.linked.contains("Up")) {
                val (newStart, newEnd) = extendLine(points[0], points[1], extensionLengthPx)
                strokePath.moveTo(newStart.x, newStart.y)
                strokePath.lineTo(newEnd.x, newEnd.y)
            }
            if (!cell.linked.contains("LowerLeft")) {
                val (newStart, newEnd) = extendLine(points[0], points[2], extensionLengthPx)
                strokePath.moveTo(newStart.x, newStart.y)
                strokePath.lineTo(newEnd.x, newEnd.y)
            }
            if (!cell.linked.contains("LowerRight")) {
                val (newStart, newEnd) = extendLine(points[1], points[2], extensionLengthPx)
                strokePath.moveTo(newStart.x, newStart.y)
                strokePath.lineTo(newEnd.x, newEnd.y)
            }
        }
        drawPath(
            strokePath,
            color = Color.Black,
            style = Stroke(
                width = snap(strokeWidthPx) * 1.15f,
                cap = StrokeCap.Square
            )
        )
    }
}
