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
import com.jmisabella.mazer.models.MazeCell
import com.jmisabella.mazer.models.MazeType
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

@Composable
fun DeltaMazeScreen(
    cells: List<MazeCell>,
    cellSize: Float,
    showSolution: Boolean,
    showHeatMap: Boolean,
    selectedPalette: HeatMapPalette,
    defaultBackgroundColor: Color,
    optionalColor: Color?
) {
    val context = LocalContext.current
    val columns = (cells.maxOfOrNull { it.x } ?: 0) + 1
    val rows = (cells.maxOfOrNull { it.y } ?: 0) + 1
    val totalRows = rows
    val maxDistance = cells.maxOfOrNull { it.distance } ?: 1
    val triangleHeight = cellSize * sqrt(3f) / 2f
    val totalWidth = cellSize * (columns + 1f) / 2f
    val totalHeight = triangleHeight * rows.toFloat()

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

    val density = LocalDensity.current.density

    val cellSizePx = remember(cellSize) { cellSize * density }
    val triangleHeightPx = remember(cellSizePx) { cellSizePx * sqrt(3f) / 2f }
    val strokeWidth = remember(cellSize) { wallStrokeWidth(MazeType.DELTA, cellSize, density) }
    val overlapPx = 1f
    val extensionLengthPx = 0.5f

    fun snap(value: Float): Float = (value * density).roundToInt().toFloat() / density

    val upPoints = remember(cellSizePx, triangleHeightPx, overlapPx) {
        listOf(
            Offset(snap(cellSizePx / 2f), snap(0f) - overlapPx),
            Offset(snap(0f) - overlapPx, snap(triangleHeightPx) + overlapPx),
            Offset(snap(cellSizePx) + overlapPx, snap(triangleHeightPx) + overlapPx)
        )
    }

    val downPoints = remember(cellSizePx, triangleHeightPx, overlapPx) {
        listOf(
            Offset(snap(0f) - overlapPx, snap(0f) - overlapPx),
            Offset(snap(cellSizePx) + overlapPx, snap(0f) - overlapPx),
            Offset(snap(cellSizePx / 2f), snap(triangleHeightPx) + overlapPx)
        )
    }

    val upDirs = remember {
        mapOf(
            "UpperLeft" to (0 to 1),
            "UpperRight" to (0 to 2),
            "Down" to (1 to 2)
        )
    }

    val downDirs = remember {
        mapOf(
            "Up" to (0 to 1),
            "LowerLeft" to (0 to 2),
            "LowerRight" to (1 to 2)
        )
    }

    val relativeFillPaths = remember(upPoints, downPoints) {
        mapOf(
            true to Path().apply {
                moveTo(upPoints[0].x, upPoints[0].y)
                lineTo(upPoints[1].x, upPoints[1].y)
                lineTo(upPoints[2].x, upPoints[2].y)
                close()
            },
            false to Path().apply {
                moveTo(downPoints[0].x, downPoints[0].y)
                lineTo(downPoints[1].x, downPoints[1].y)
                lineTo(downPoints[2].x, downPoints[2].y)
                close()
            }
        )
    }

    val strokePaths = remember(cells, upPoints, downPoints, upDirs, downDirs) {
        cells.map { cell ->
            val isPointUp = cell.orientation.lowercase() == "normal"
            val points = if (isPointUp) upPoints else downPoints
            val dirs = if (isPointUp) upDirs else downDirs
            val strokePath = Path()
            dirs.forEach { (dir, pair) ->
                if (!cell.linked.contains(dir)) {
                    val (i, j) = pair
                    val start = points[i]
                    val end = points[j]
                    val dx = end.x - start.x
                    val dy = end.y - start.y
                    val length = sqrt(dx * dx + dy * dy)
                    if (length > 0f) {
                        val unitDx = dx / length
                        val unitDy = dy / length
                        val extDx = unitDx * extensionLengthPx
                        val extDy = unitDy * extensionLengthPx
                        val newStart = Offset(start.x - extDx, start.y - extDy)
                        val newEnd = Offset(end.x + extDx, end.y + extDy)
                        strokePath.moveTo(newStart.x, newStart.y)
                        strokePath.lineTo(newEnd.x, newEnd.y)
                    }
                }
            }
            strokePath
        }
    }

    val finalStrokeWidth = remember(strokeWidth) { snap(strokeWidth) * 1.15f }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .size(totalWidth.dp, totalHeight.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            cells.forEachIndexed { index, cell ->
                val isPointUp = cell.orientation.lowercase() == "normal"
                val offsetXPx = cell.x * (cellSizePx / 2f)
                val offsetYPx = cell.y * triangleHeightPx

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
                    translate(offsetXPx, offsetYPx)
                }) {
                    drawPath(relativeFillPaths[isPointUp] ?: Path(), color = fillColor)
                    drawPath(
                        strokePaths[index],
                        color = Color.Black,
                        style = Stroke(
                            width = finalStrokeWidth,
                            cap = androidx.compose.ui.graphics.StrokeCap.Square
                        )
                    )
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
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.unit.dp
//import com.jmisabella.mazer.layout.CellColors
//import com.jmisabella.mazer.layout.computeCellSize
//import com.jmisabella.mazer.models.*
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//import kotlin.math.roundToInt
//import kotlin.math.sqrt
//import kotlin.math.max
//import kotlin.math.min
//
//@Composable
//fun DeltaMazeScreen(
//    cells: List<MazeCell>,
//    cellSize: Float,
//    showSolution: Boolean,
//    showHeatMap: Boolean,
//    selectedPalette: HeatMapPalette,
//    maxDistance: Int,
//    defaultBackgroundColor: Color,
//    optionalColor: Color?
//) {
//    val context = LocalContext.current
//    val density = androidx.compose.ui.platform.LocalDensity.current.density
//    val columns = (cells.maxOfOrNull { it.x } ?: 0) + 1
//    val rows = (cells.maxOfOrNull { it.y } ?: 0) + 1
//    val totalRows = rows
//    val cellSizeDp = cellSize.dp
//    val triangleHeight = cellSize * sqrt(3f) / 2f
//    val totalWidth = (cellSize * (columns + 1f) / 2f).dp
//    val totalHeight = (triangleHeight * rows.toFloat()).dp
//
//    fun snap(value: Float): Float = (value * density).roundToInt().toFloat() / density
//
//    val cellMap: Map<Coordinates, MazeCell> = cells.associateBy { Coordinates(it.x, it.y) }
//    val revealedSolutionPath = remember { mutableStateMapOf<Coordinates, Boolean>() }
//    val coroutineScope = rememberCoroutineScope()
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
//            val minDelayMs = 16L // approx 60fps
//            val numSteps = (totalAnimationTimeMs / minDelayMs).toInt()
//            val batchSize = max(1, (pathCells.size + numSteps - 1) / numSteps) // ceiling division
//
//            var index = 0
//            var lastFeedbackTime = 0L
//            val minFeedbackInterval = 50L // Minimum time between feedback in ms
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
//        contentAlignment = Alignment.TopCenter
//    ) {
//        Box(
//            modifier = Modifier
//                .size(totalWidth, totalHeight)
//                .background(Color.Transparent)
//        ) {
//            Column(
//                modifier = Modifier.fillMaxSize(),
//                verticalArrangement = Arrangement.spacedBy(snap(0f).dp)
//            ) {
//                repeat(rows) { row ->
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .wrapContentHeight(),
//                        horizontalArrangement = Arrangement.spacedBy(snap(-cellSize / 2f).dp)
//                    ) {
//                        repeat(columns) { col ->
//                            val coord = Coordinates(col, row)
//                            val cell = cellMap[coord]
//                            if (cell != null) {
//                                DeltaCellScreen(
//                                    cell = cell,
//                                    cellSize = cellSizeDp,
//                                    showSolution = showSolution,
//                                    showHeatMap = showHeatMap,
//                                    selectedPalette = selectedPalette,
//                                    maxDistance = maxDistance,
//                                    isRevealedSolution = revealedSolutionPath[coord] ?: false,
//                                    defaultBackgroundColor = defaultBackgroundColor,
//                                    optionalColor = optionalColor,
//                                    totalRows = totalRows
//                                )
//                            } // No else clause needed; skip if null (matches Swift EmptyView)
//                        }
//                    }
//                }
//            }
//
//        }
//    }
//}

