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
import com.jmisabella.mazer.layout.cellBackgroundColor
import com.jmisabella.mazer.layout.wallStrokeWidth
import com.jmisabella.mazer.models.HeatMapPalette
import com.jmisabella.mazer.models.MazeCell
import com.jmisabella.mazer.models.MazeType
import kotlin.math.roundToInt
import kotlin.math.sqrt

@Composable
fun RhombicCellScreen(
    cell: MazeCell,
    cellSize: Dp,
    showSolution: Boolean,
    showHeatMap: Boolean,
    selectedPalette: HeatMapPalette,
    maxDistance: Int,
    isRevealedSolution: Boolean,
    defaultBackgroundColor: Color,
    optionalColor: Color?,
    totalRows: Int,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current.density
    val strokeWidthPx = wallStrokeWidth(MazeType.RHOMBIC, cellSize.value, density)
    val sidePx = cellSize.value * density
    val boxPx = sidePx * sqrt(2f)
    val overlapPx = 1f
    val extensionLengthPx = 0.5f

    fun snap(value: Float): Float = (value * density).roundToInt().toFloat() / density

    val points = listOf(
        Offset(snap(boxPx / 2f), snap(0f)),
        Offset(snap(boxPx), snap(boxPx / 2f)),
        Offset(snap(boxPx / 2f), snap(boxPx)),
        Offset(snap(0f), snap(boxPx / 2f))
    )

    val adjustedPoints = listOf(
        Offset(points[0].x, points[0].y - overlapPx),
        Offset(points[1].x + overlapPx, points[1].y),
        Offset(points[2].x, points[2].y + overlapPx),
        Offset(points[3].x - overlapPx, points[3].y)
    )

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
        modifier = modifier
            .size(cellSize * sqrt(2f), cellSize * sqrt(2f))
    ) {
        // Fill the rhombus
        val fillPath = Path().apply {
            moveTo(adjustedPoints[0].x, adjustedPoints[0].y)
            lineTo(adjustedPoints[1].x, adjustedPoints[1].y)
            lineTo(adjustedPoints[2].x, adjustedPoints[2].y)
            lineTo(adjustedPoints[3].x, adjustedPoints[3].y)
            close()
        }
        drawPath(fillPath, color = backgroundColor)

        // Stroke the walls
        val strokePath = Path()
        if (!cell.linked.contains("UpperRight")) {
            val (newStart, newEnd) = extendLine(points[0], points[1], extensionLengthPx)
            strokePath.moveTo(newStart.x, newStart.y)
            strokePath.lineTo(newEnd.x, newEnd.y)
        }
        if (!cell.linked.contains("LowerRight")) {
            val (newStart, newEnd) = extendLine(points[1], points[2], extensionLengthPx)
            strokePath.moveTo(newStart.x, newStart.y)
            strokePath.lineTo(newEnd.x, newEnd.y)
        }
        if (!cell.linked.contains("LowerLeft")) {
            val (newStart, newEnd) = extendLine(points[2], points[3], extensionLengthPx)
            strokePath.moveTo(newStart.x, newStart.y)
            strokePath.lineTo(newEnd.x, newEnd.y)
        }
        if (!cell.linked.contains("UpperLeft")) {
            val (newStart, newEnd) = extendLine(points[3], points[0], extensionLengthPx)
            strokePath.moveTo(newStart.x, newStart.y)
            strokePath.lineTo(newEnd.x, newEnd.y)
        }
        drawPath(
            strokePath,
            color = Color.Black,
            style = Stroke(
                width = strokeWidthPx,
                cap = StrokeCap.Square
            )
        )
    }
}