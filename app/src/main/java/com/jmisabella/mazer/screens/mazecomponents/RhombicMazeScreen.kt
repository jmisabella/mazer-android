package com.jmisabella.mazer.screens.mazecomponents

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.jmisabella.mazer.layout.cellBackgroundColor
import com.jmisabella.mazer.layout.wallStrokeWidth
import com.jmisabella.mazer.models.Coordinates
import com.jmisabella.mazer.models.HeatMapPalette
import com.jmisabella.mazer.models.MazeCell
import com.jmisabella.mazer.models.MazeType
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

data class DirInfo(val vertexIndices: Pair<Int, Int>, val offsetDelta: Pair<Int, Int>, val opposite: String)

@Composable
fun RhombicMazeScreen(
    selectedPalette: MutableState<HeatMapPalette>,
    cells: List<MazeCell>,
    cellSize: Float,
    showSolution: Boolean,
    showHeatMap: Boolean,
    defaultBackgroundColor: Color,
    optionalColor: Color?
) {
    val context = LocalContext.current
    val maxX = cells.maxOfOrNull { it.x } ?: 0
    val maxY = cells.maxOfOrNull { it.y } ?: 0
    val totalRows = maxY + 1
    val maxDistance = cells.maxOfOrNull { it.distance } ?: 1

    val sqrt2 = sqrt(2f)
    val diagonal = cellSize * sqrt2
    val halfDiagonal = diagonal / 2f

    val containerWidth = halfDiagonal * maxX + diagonal
    val containerHeight = halfDiagonal * maxY + diagonal

    val cellMap = cells.associateBy { Coordinates(it.x, it.y) }
    val revealedSolutionPath = remember { mutableStateMapOf<Coordinates, Boolean>() }
    val vibrator = remember { context.getSystemService(Vibrator::class.java) }
    val toneGenerator = remember { ToneGenerator(AudioManager.STREAM_MUSIC, 100) }

    DisposableEffect(Unit) {
        onDispose {
            toneGenerator.release()
        }
    }

    LaunchedEffect(showSolution, cells) {
        if (showSolution) {
            revealedSolutionPath.clear()
            val pathCells = cells
                .filter { it.onSolutionPath && !it.isVisited }
                .sortedBy { it.distance }

            val totalAnimationTimeMs = 2000L
            val minDelayMs = 16L
            val numSteps = (totalAnimationTimeMs / minDelayMs).toInt()
            val batchSize = max(1, (pathCells.size + numSteps - 1) / numSteps)

            var index = 0
            var lastFeedbackTime = 0L
            val minFeedbackInterval = 50L

            while (index < pathCells.size) {
                val end = min(index + batchSize, pathCells.size)
                for (i in index until end) {
                    val cell = pathCells[i]
                    revealedSolutionPath[Coordinates(cell.x, cell.y)] = true
                }
                delay(minDelayMs)

                val currentTime = System.currentTimeMillis()
                if (currentTime - lastFeedbackTime >= minFeedbackInterval) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                    lastFeedbackTime = currentTime
                }
                index += batchSize
            }
        } else {
            revealedSolutionPath.clear()
        }
    }

    val density = LocalDensity.current

    val halfDiagonalPx = remember(halfDiagonal) { with(density) { halfDiagonal.dp.toPx() } }
    val diagonalPx = remember(diagonal) { with(density) { diagonal.dp.toPx() } }
    val strokeWidth = remember(cellSize) { wallStrokeWidth(MazeType.RHOMBIC, cellSize, density.density) }

    val overlapPx = strokeWidth * (sqrt(2f) / 2f)  // ≈ 0.707 * strokeWidth

    val scaledPoints = remember(halfDiagonalPx, diagonalPx) {
        listOf(
            Offset(halfDiagonalPx, 0f), // top
            Offset(diagonalPx, halfDiagonalPx), // right
            Offset(halfDiagonalPx, diagonalPx), // bottom
            Offset(0f, halfDiagonalPx) // left
        )
    }

    val adjustedPoints = remember(scaledPoints, overlapPx) {
        listOf(
            Offset(scaledPoints[0].x, scaledPoints[0].y - overlapPx),
            Offset(scaledPoints[1].x + overlapPx, scaledPoints[1].y),
            Offset(scaledPoints[2].x, scaledPoints[2].y + overlapPx),
            Offset(scaledPoints[3].x - overlapPx, scaledPoints[3].y)
        )
    }

    val relativeFillPath = remember(adjustedPoints) {
        Path().apply {
            moveTo(adjustedPoints[0].x, adjustedPoints[0].y)
            adjustedPoints.drop(1).forEach { lineTo(it.x, it.y) }
            close()
        }
    }

    val dirMap = remember {
        mapOf(
            "UpperRight" to DirInfo(0 to 1, 1 to -1, "LowerLeft"),
            "LowerRight" to DirInfo(1 to 2, 1 to 1, "UpperLeft"),
            "LowerLeft" to DirInfo(2 to 3, -1 to 1, "UpperRight"),
            "UpperLeft" to DirInfo(3 to 0, -1 to -1, "LowerRight")
        )
    }

    val strokePaths = remember(cells, scaledPoints, dirMap) {
        cells.map { cell ->
            val strokePath = Path()
            dirMap.forEach { (dir, info) ->
                if (cell.linked.contains(dir)) return@forEach

                val (dq, dr) = info.offsetDelta
                val neighborCoord = Coordinates(cell.x + dq, cell.y + dr)
                val neighbor = cellMap[neighborCoord]

                val neighborLink = neighbor?.linked?.contains(info.opposite) ?: false
                if (neighborLink) return@forEach

                if (neighbor != null && cell.onSolutionPath && neighbor.onSolutionPath && abs(cell.distance - neighbor.distance) == 1) return@forEach

                val (i, j) = info.vertexIndices
                val start = scaledPoints[i]
                val end = scaledPoints[j]
                strokePath.moveTo(start.x, start.y)
                strokePath.lineTo(end.x, end.y)
            }
            strokePath
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .size(containerWidth.dp, containerHeight.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            cells.forEachIndexed { index, cell ->
                val offsetXPx = cell.x * halfDiagonalPx
                val offsetYPx = cell.y * halfDiagonalPx

                val fillColor = cellBackgroundColor(
                    cell = cell,
                    showSolution = showSolution,
                    showHeatMap = showHeatMap,
                    maxDistance = maxDistance,
                    selectedPalette = selectedPalette.value,
                    isRevealedSolution = revealedSolutionPath[Coordinates(cell.x, cell.y)] ?: false,
                    defaultBackground = defaultBackgroundColor,
                    totalRows = totalRows,
                    optionalColor = optionalColor
                )

                withTransform({
                    translate(offsetXPx, offsetYPx)
                }) {
                    drawPath(relativeFillPath, color = fillColor)
                    drawPath(strokePaths[index], color = Color.Black, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
                }
            }
        }
    }
}

//package com.jmisabella.mazer.screens.mazecomponents
//
//import android.content.Context
//import android.media.AudioManager
//import android.media.ToneGenerator
//import android.os.VibrationEffect
//import android.os.Vibrator
//import androidx.compose.foundation.Canvas
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.size
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.DisposableEffect
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.MutableState
//import androidx.compose.runtime.mutableStateMapOf
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.Path
//import androidx.compose.ui.graphics.StrokeCap
//import androidx.compose.ui.graphics.drawscope.Stroke
//import androidx.compose.ui.graphics.drawscope.withTransform
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.platform.LocalDensity
//import androidx.compose.ui.unit.dp
//import com.jmisabella.mazer.layout.cellBackgroundColor
//import com.jmisabella.mazer.layout.wallStrokeWidth
//import com.jmisabella.mazer.models.Coordinates
//import com.jmisabella.mazer.models.HeatMapPalette
//import com.jmisabella.mazer.models.MazeCell
//import com.jmisabella.mazer.models.MazeType
//import kotlinx.coroutines.delay
//import kotlin.math.abs
//import kotlin.math.max
//import kotlin.math.min
//import kotlin.math.sqrt
//
//data class DirInfo(val vertexIndices: Pair<Int, Int>, val offsetDelta: Pair<Int, Int>, val opposite: String)
//
//@Composable
//fun RhombicMazeScreen(
//    selectedPalette: MutableState<HeatMapPalette>,
//    cells: List<MazeCell>,
//    cellSize: Float,
//    showSolution: Boolean,
//    showHeatMap: Boolean,
//    defaultBackgroundColor: Color,
//    optionalColor: Color?
//) {
//    val context = LocalContext.current
//    val maxX = cells.maxOfOrNull { it.x } ?: 0
//    val maxY = cells.maxOfOrNull { it.y } ?: 0
//    val totalRows = maxY + 1
//    val maxDistance = cells.maxOfOrNull { it.distance } ?: 1
//
//    val sqrt2 = sqrt(2f)
//    val diagonal = cellSize * sqrt2
//    val halfDiagonal = diagonal / 2f
//
//    val containerWidth = halfDiagonal * maxX + diagonal
//    val containerHeight = halfDiagonal * maxY + diagonal
//
//    val cellMap = cells.associateBy { Coordinates(it.x, it.y) }
//    val revealedSolutionPath = remember { mutableStateMapOf<Coordinates, Boolean>() }
//    val vibrator = remember { context.getSystemService(Vibrator::class.java) }
//    val toneGenerator = remember { ToneGenerator(AudioManager.STREAM_MUSIC, 100) }
//
//    DisposableEffect(Unit) {
//        onDispose {
//            toneGenerator.release()
//        }
//    }
//
//    LaunchedEffect(showSolution, cells) {
//        if (showSolution) {
//            revealedSolutionPath.clear()
//            val pathCells = cells
//                .filter { it.onSolutionPath && !it.isVisited }
//                .sortedBy { it.distance }
//
//            val totalAnimationTimeMs = 2000L
//            val minDelayMs = 16L
//            val numSteps = (totalAnimationTimeMs / minDelayMs).toInt()
//            val batchSize = max(1, (pathCells.size + numSteps - 1) / numSteps)
//
//            var index = 0
//            var lastFeedbackTime = 0L
//            val minFeedbackInterval = 50L
//
//            while (index < pathCells.size) {
//                val end = min(index + batchSize, pathCells.size)
//                for (i in index until end) {
//                    val cell = pathCells[i]
//                    revealedSolutionPath[Coordinates(cell.x, cell.y)] = true
//                }
//                delay(minDelayMs)
//
//                val currentTime = System.currentTimeMillis()
//                if (currentTime - lastFeedbackTime >= minFeedbackInterval) {
//                    vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
//                    lastFeedbackTime = currentTime
//                }
//                index += batchSize
//            }
//        } else {
//            revealedSolutionPath.clear()
//        }
//    }
//
//    val density = LocalDensity.current
//
//    val halfDiagonalPx = remember(halfDiagonal) { with(density) { halfDiagonal.dp.toPx() } }
//    val diagonalPx = remember(diagonal) { with(density) { diagonal.dp.toPx() } }
//    val strokeWidth = remember(cellSize) { wallStrokeWidth(MazeType.RHOMBIC, cellSize, density.density) }
//
//    val overlapPx = strokeWidth * (sqrt(2f) / 2f)  // ≈ 0.707 * strokeWidth
////    val extensionLengthPx = strokeWidth / 2f
//    val extensionLengthPx = strokeWidth * (sqrt(2f) / 2f)
//
//    val scaledPoints = remember(halfDiagonalPx, diagonalPx) {
//        listOf(
//            Offset(halfDiagonalPx, 0f), // top
//            Offset(diagonalPx, halfDiagonalPx), // right
//            Offset(halfDiagonalPx, diagonalPx), // bottom
//            Offset(0f, halfDiagonalPx) // left
//        )
//    }
//
//    val adjustedPoints = remember(scaledPoints, overlapPx) {
//        listOf(
//            Offset(scaledPoints[0].x, scaledPoints[0].y - overlapPx),
//            Offset(scaledPoints[1].x + overlapPx, scaledPoints[1].y),
//            Offset(scaledPoints[2].x, scaledPoints[2].y + overlapPx),
//            Offset(scaledPoints[3].x - overlapPx, scaledPoints[3].y)
//        )
//    }
//
//    val relativeFillPath = remember(adjustedPoints) {
//        Path().apply {
//            moveTo(adjustedPoints[0].x, adjustedPoints[0].y)
//            adjustedPoints.drop(1).forEach { lineTo(it.x, it.y) }
//            close()
//        }
//    }
//
//    val dirMap = remember {
//        mapOf(
//            "UpperRight" to DirInfo(0 to 1, 1 to -1, "LowerLeft"),
//            "LowerRight" to DirInfo(1 to 2, 1 to 1, "UpperLeft"),
//            "LowerLeft" to DirInfo(2 to 3, -1 to 1, "UpperRight"),
//            "UpperLeft" to DirInfo(3 to 0, -1 to -1, "LowerRight")
//        )
//    }
//
//    val strokePaths = remember(cells, scaledPoints, dirMap) {
//        cells.map { cell ->
//            val strokePath = Path()
//            dirMap.forEach { (dir, info) ->
//                if (cell.linked.contains(dir)) return@forEach
//
//                val (dq, dr) = info.offsetDelta
//                val neighborCoord = Coordinates(cell.x + dq, cell.y + dr)
//                val neighbor = cellMap[neighborCoord]
//
//                val neighborLink = neighbor?.linked?.contains(info.opposite) ?: false
//                if (neighborLink) return@forEach
//
//                if (neighbor != null && cell.onSolutionPath && neighbor.onSolutionPath && abs(cell.distance - neighbor.distance) == 1) return@forEach
//
//                val (i, j) = info.vertexIndices
//                val start = scaledPoints[i]
//                val end = scaledPoints[j]
//                val (newStart, newEnd) = extendLine(start, end, extensionLengthPx)
//                strokePath.moveTo(newStart.x, newStart.y)
//                strokePath.lineTo(newEnd.x, newEnd.y)
//            }
//            strokePath
//        }
//    }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .size(containerWidth.dp, containerHeight.dp)
//    ) {
//        Canvas(modifier = Modifier.fillMaxSize()) {
//            cells.forEachIndexed { index, cell ->
//                val offsetXPx = cell.x * halfDiagonalPx
//                val offsetYPx = cell.y * halfDiagonalPx
//
//                val fillColor = cellBackgroundColor(
//                    cell = cell,
//                    showSolution = showSolution,
//                    showHeatMap = showHeatMap,
//                    maxDistance = maxDistance,
//                    selectedPalette = selectedPalette.value,
//                    isRevealedSolution = revealedSolutionPath[Coordinates(cell.x, cell.y)] ?: false,
//                    defaultBackground = defaultBackgroundColor,
//                    totalRows = totalRows,
//                    optionalColor = optionalColor
//                )
//
//                withTransform({
//                    translate(offsetXPx, offsetYPx)
//                }) {
//                    drawPath(relativeFillPath, color = fillColor)
//                    drawPath(strokePaths[index], color = Color.Black, style = Stroke(width = strokeWidth, cap = StrokeCap.Butt))
//                }
//            }
//        }
//    }
//}
//
//fun extendLine(start: Offset, end: Offset, extension: Float): Pair<Offset, Offset> {
//    val dx = end.x - start.x
//    val dy = end.y - start.y
//    val length = sqrt(dx * dx + dy * dy)
//    if (length == 0f) return Pair(start, end)
//    val unitDx = dx / length
//    val unitDy = dy / length
//    val extDx = unitDx * extension
//    val extDy = unitDy * extension
//    return Pair(
//        Offset(start.x - extDx, start.y - extDy),
//        Offset(end.x + extDx, end.y + extDy)
//    )
//}

