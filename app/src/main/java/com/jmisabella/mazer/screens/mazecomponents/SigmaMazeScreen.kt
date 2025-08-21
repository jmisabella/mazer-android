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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.jmisabella.mazer.layout.cellBackgroundColor
import com.jmisabella.mazer.layout.wallStrokeWidth
import com.jmisabella.mazer.models.Coordinates
import com.jmisabella.mazer.models.HeatMapPalette
import com.jmisabella.mazer.models.HexDirection
import com.jmisabella.mazer.models.MazeCell
import com.jmisabella.mazer.models.MazeType
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

@Composable
fun SigmaMazeScreen(
    cells: List<MazeCell>,
    cellSize: Float,
    showSolution: Boolean,
    showHeatMap: Boolean,
    selectedPalette: HeatMapPalette,
    defaultBackgroundColor: Color,
    optionalColor: Color?
) {
    val context = LocalContext.current
    val cols = (cells.maxOfOrNull { it.x } ?: 0) + 1
    val rows = (cells.maxOfOrNull { it.y } ?: 0) + 1
    val totalRows = rows
    val hexHeight = sqrt(3f) * cellSize
    val totalWidth = cellSize * (1.5f * cols + 0.5f)
    val totalHeight = hexHeight * (rows.toFloat() + 0.5f)
    val maxDistance = cells.maxOfOrNull { it.distance } ?: 1
    val cellMap = cells.associateBy { Coordinates(it.x, it.y) }

    val revealedSolutionPath = remember { mutableStateMapOf<Coordinates, Boolean>() }
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator }
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
                    vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                    lastFeedbackTime = currentTime
                }
                index += batchSize
            }
        } else {
            revealedSolutionPath.clear()
        }
    }

    val density = LocalDensity.current

    val cellSizePx = remember(cellSize) { with(density) { cellSize.dp.toPx() } }
    val hexHeightPx = remember(cellSizePx) { sqrt(3f) * cellSizePx }
    val strokeWidth = remember(cellSize) { wallStrokeWidth(MazeType.SIGMA, cellSize, density.density) }
    val overlap = 1f
    val factor = remember(cellSizePx) { overlap / cellSizePx }

    val unitPoints = remember {
        listOf(
            Offset(0.5f, 0f),
            Offset(1.5f, 0f),
            Offset(2f, sqrt(3f) / 2),
            Offset(1.5f, sqrt(3f)),
            Offset(0.5f, sqrt(3f)),
            Offset(0f, sqrt(3f) / 2)
        )
    }

    val scaledPoints = remember(unitPoints, cellSizePx) {
        unitPoints.map { Offset(it.x * cellSizePx, it.y * cellSizePx) }
    }

    val c = remember(cellSizePx, hexHeightPx) { Offset(cellSizePx, hexHeightPx / 2) }

    val adjustedPoints = remember(scaledPoints, factor) {
        scaledPoints.map { p ->
            val dx = p.x - c.x
            val dy = p.y - c.y
            Offset(p.x + factor * dx, p.y + factor * dy)
        }
    }

    val relativeFillPath = remember(adjustedPoints) {
        Path().apply {
            moveTo(adjustedPoints[0].x, adjustedPoints[0].y)
            adjustedPoints.drop(1).forEach { lineTo(it.x, it.y) }
            close()
        }
    }

    val strokePaths = remember(cells, adjustedPoints) {
        cells.map { cell ->
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
            strokePath
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .size(totalWidth.dp, totalHeight.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            cells.forEachIndexed { index, cell ->
                val q = cell.x.toFloat()
                val r = cell.y.toFloat()
                val x = cellSize * 1.5f * q + cellSize
                val yOffset = if (cell.x % 2 == 0) 0f else hexHeight / 2f
                val y = hexHeight * r + hexHeight / 2f + yOffset

                val offsetX = with(density) { (x - cellSize).dp.toPx() }
                val offsetY = with(density) { (y - hexHeight / 2f).dp.toPx() }

                val fillColor = cellBackgroundColor(
                    cell = cell,
                    showSolution = showSolution,
                    showHeatMap = showHeatMap,
                    maxDistance = maxDistance,
                    selectedPalette = selectedPalette,
                    isRevealedSolution = revealedSolutionPath[Coordinates(cell.x, cell.y)] ?: false,
                    defaultBackground = defaultBackgroundColor,
                    totalRows = totalRows,
                    optionalColor = optionalColor
                )

                withTransform({
                    translate(offsetX, offsetY)
                }) {
                    drawPath(relativeFillPath, color = fillColor)
                    drawPath(strokePaths[index], color = Color.Black, style = Stroke(width = strokeWidth))
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
//import androidx.compose.ui.platform.LocalDensity
//import kotlin.math.roundToInt
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.offset
//import androidx.compose.foundation.layout.size
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.DisposableEffect
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.mutableStateMapOf
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.platform.LocalDensity
//import androidx.compose.ui.unit.IntOffset
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.platform.LocalDensity
//import kotlin.math.roundToInt
//import com.jmisabella.mazer.models.Coordinates
//import com.jmisabella.mazer.models.HeatMapPalette
//import com.jmisabella.mazer.models.MazeCell
//import kotlinx.coroutines.delay
//import kotlin.math.max
//import kotlin.math.min
//import kotlin.math.roundToInt
//import kotlin.math.sqrt
//
//@Composable
//fun SigmaMazeScreen(
//    cells: List<MazeCell>,
//    cellSize: Float,
//    showSolution: Boolean,
//    showHeatMap: Boolean,
//    selectedPalette: HeatMapPalette,
//    defaultBackgroundColor: Color,
//    optionalColor: Color?
//) {
//    val context = LocalContext.current
//    val cols = (cells.maxOfOrNull { it.x } ?: 0) + 1
//    val rows = (cells.maxOfOrNull { it.y } ?: 0) + 1
//    val totalRows = rows
//    val hexHeight = sqrt(3f) * cellSize
//    val totalWidth = cellSize * (1.5f * cols + 0.5f)
//    val totalHeight = hexHeight * (rows.toFloat() + 0.5f)
//    val maxDistance = cells.maxOfOrNull { it.distance } ?: 1
//    val cellMap = cells.associateBy { Coordinates(it.x, it.y) }
//
//    val revealedSolutionPath = remember { mutableStateMapOf<Coordinates, Boolean>() }
//    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator }
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
////                    toneGenerator.startTone(ToneGenerator.TONE_PROP_PROMPT, 200)
//                    vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
//                    lastFeedbackTime = currentTime
//                }
//                index += batchSize
//            }
//        } else {
//            revealedSolutionPath.clear()
//        }
//    }
//
//    Box(
//        modifier = Modifier.fillMaxSize(),
//        contentAlignment = Alignment.Center
//    ) {
//        Box(
//            modifier = Modifier
//                .size(totalWidth.dp, totalHeight.dp)
//        ) {
//            cells.forEach { cell ->
//                val q = cell.x.toFloat()
//                val r = cell.y.toFloat()
//                val x = cellSize * 1.5f * q + cellSize
//                val yOffset = if (cell.x % 2 == 0) 0f else hexHeight / 2f
//                val y = hexHeight * r + hexHeight / 2f + yOffset
//                val pos = Offset(x, y)
//
//                val centerToLeft = cellSize.dp
//                val centerToTop = (hexHeight / 2f).dp
//                val offsetDpX = pos.x.dp - centerToLeft
//                val offsetDpY = pos.y.dp - centerToTop
//
//                val density = LocalDensity.current
//                val offsetX = (offsetDpX.value * density.density).roundToInt()
//                val offsetY = (offsetDpY.value * density.density).roundToInt()
//
//                SigmaCellScreen(
//                    cell = cell,
//                    cellSize = cellSize,
//                    showSolution = showSolution,
//                    showHeatMap = showHeatMap,
//                    selectedPalette = selectedPalette,
//                    maxDistance = maxDistance,
//                    isRevealedSolution = revealedSolutionPath[Coordinates(cell.x, cell.y)] ?: false,
//                    defaultBackgroundColor = defaultBackgroundColor,
//                    optionalColor = optionalColor,
//                    totalRows = totalRows,
//                    cellMap = cellMap,
//                    modifier = Modifier.offset { IntOffset(offsetX, offsetY) }
//                )
//            }
//        }
//    }
//}

