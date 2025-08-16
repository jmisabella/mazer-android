
package com.jmisabella.mazer.screens.mazecomponents

import androidx.compose.foundation.Canvas
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.graphics.drawscope.translate
import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.jmisabella.mazer.layout.cellBackgroundColor
import com.jmisabella.mazer.models.Coordinates
import com.jmisabella.mazer.models.HeatMapPalette
import com.jmisabella.mazer.models.MazeCell
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

@Composable
fun UpsilonMazeScreen(
    cells: List<MazeCell>,
    squareSize: Float,
    octagonSize: Float,
    showSolution: Boolean,
    showHeatMap: Boolean,
    selectedPalette: HeatMapPalette,
    maxDistance: Int,
    defaultBackgroundColor: Color,
    optionalColor: Color?
) {
    val context = LocalContext.current
    val rows = (cells.maxOfOrNull { it.y } ?: 0) + 1
    val columns = (cells.maxOfOrNull { it.x } ?: 0) + 1
    val overlapDp = (octagonSize - squareSize) / 2f
    val negativeSpacing = -overlapDp

    val revealedSolutionPath = remember { mutableStateMapOf<Coordinates, Boolean>() }

    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator }
    val toneGenerator = remember { ToneGenerator(AudioManager.STREAM_MUSIC, 100) }

    DisposableEffect(Unit) {
        onDispose {
            toneGenerator.release()
        }
    }

    LaunchedEffect(showSolution) {
        if (showSolution) {
            revealedSolutionPath.clear()
            val pathCells = cells.filter { it.onSolutionPath && !it.isVisited }.sortedBy { it.distance }

            val totalAnimationTimeMs = 2000L
            val minDelayMs = 16L // approx 60fps
            val numSteps = (totalAnimationTimeMs / minDelayMs).toInt()
            val batchSize = max(1, (pathCells.size + numSteps - 1) / numSteps) // ceiling division

            var index = 0
            var lastFeedbackTime = 0L
            val minFeedbackInterval = 50L // Minimum time between feedback in ms

            while (index < pathCells.size) {
                val end = min(index + batchSize, pathCells.size)
                for (i in index until end) {
                    val cell = pathCells[i]
                    revealedSolutionPath[Coordinates(cell.x, cell.y)] = true
                }
                delay(minDelayMs)

                val currentTime = System.currentTimeMillis()
                if (currentTime - lastFeedbackTime >= minFeedbackInterval) {
//                    toneGenerator.startTone(ToneGenerator.TONE_PROP_PROMPT, 200)
                    vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                    lastFeedbackTime = currentTime
                }
                index += batchSize
            }
        } else {
            revealedSolutionPath.clear()
        }
    }

    val density = LocalDensity.current.density
    val octagonPx = octagonSize * density
    val squarePx = squareSize * density
    val spacingDp = (octagonSize + squareSize) / 2f
    val spacingPx = spacingDp * density
    val overlapPx = 2f  // Fixed pixel overlap to cover gaps reliably
    val strokePx = octagonPx / 16f  // Increased divisor for thinner walls on large cells

    val sortedCells = remember(cells) { cells.sortedBy { it.y * columns + it.x } }

    fun Offset.isLessThan(other: Offset): Boolean {
        return if (x < other.x) true else if (x > other.x) false else y < other.y
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val uniqueWalls = mutableSetOf<Pair<Offset, Offset>>()

        // Draw all backgrounds first (with translate for positioning)
        for (cell in sortedCells) {
            val cellXPx = cell.x * spacingPx
            val cellYPx = cell.y * spacingPx
            val coord = Coordinates(cell.x, cell.y)
            val fillColor = cellBackgroundColor(
                cell = cell,
                showSolution = showSolution,
                showHeatMap = showHeatMap,
                maxDistance = maxDistance,
                selectedPalette = selectedPalette,
                isRevealedSolution = revealedSolutionPath[coord] ?: false,
                defaultBackground = defaultBackgroundColor,
                totalRows = rows,
                optionalColor = optionalColor
            )

            translate(cellXPx, cellYPx) {
                if (cell.isSquare) {
                    val inset = (octagonPx - squarePx) / 2f
                    drawRect(
                        color = fillColor,
                        topLeft = Offset(inset - overlapPx, inset - overlapPx),
                        size = Size(squarePx + overlapPx * 2, squarePx + overlapPx * 2)
                    )
                } else {
                    val path = octagonPath(
                        rect = Rect(0f, 0f, octagonPx, octagonPx),
                        overlap = overlapPx
                    )
                    drawPath(path, fillColor, style = Fill)
                }
            }
        }

        // Collect unique wall segments (absolute rounded positions)
        for (cell in sortedCells) {
            val cellXPx = cell.x * spacingPx
            val cellYPx = cell.y * spacingPx
            val pts = if (cell.isSquare) {
                val i = (octagonPx - squarePx) / 2f
                listOf(
                    Offset(i, i),
                    Offset(i + squarePx, i),
                    Offset(i + squarePx, i + squarePx),
                    Offset(i, i + squarePx)
                )
            } else {
                val r = octagonPx / 2f
                val cx = r; val cy = r
                val k = 2 * r / (2 + sqrt(2f))
                listOf(
                    Offset(cx - r + k, cy - r),
                    Offset(cx + r - k, cy - r),
                    Offset(cx + r, cy - r + k),
                    Offset(cx + r, cy + r - k),
                    Offset(cx + r - k, cy + r),
                    Offset(cx - r + k, cy + r),
                    Offset(cx - r, cy + r - k),
                    Offset(cx - r, cy - r + k)
                )
            }

            val directions = if (cell.isSquare) mapOf(
                "Up" to (0 to 1),
                "Right" to (1 to 2),
                "Down" to (2 to 3),
                "Left" to (3 to 0)
            ) else mapOf(
                "Up" to (0 to 1),
                "UpperRight" to (1 to 2),
                "Right" to (2 to 3),
                "LowerRight" to (3 to 4),
                "Down" to (4 to 5),
                "LowerLeft" to (5 to 6),
                "Left" to (6 to 7),
                "UpperLeft" to (7 to 0)
            )

            for ((dir, pair) in directions) {
                if (!cell.linked.contains(dir)) {
                    val (startIdx, endIdx) = pair
                    val startAbs = Offset(cellXPx + pts[startIdx].x, cellYPx + pts[startIdx].y)
                    val endAbs = Offset(cellXPx + pts[endIdx].x, cellYPx + pts[endIdx].y)
                    val startRounded = Offset(startAbs.x.roundToInt().toFloat(), startAbs.y.roundToInt().toFloat())
                    val endRounded = Offset(endAbs.x.roundToInt().toFloat(), endAbs.y.roundToInt().toFloat())
                    val minPoint = if (startRounded.isLessThan(endRounded)) startRounded else endRounded
                    val maxPoint = if (startRounded.isLessThan(endRounded)) endRounded else startRounded
                    uniqueWalls.add(minPoint to maxPoint)
                }
            }
        }

        // Draw unique walls once (absolute positions, no translate needed)
        for ((start, end) in uniqueWalls) {
            drawLine(
                color = Color.Black,
                start = start,
                end = end,
                strokeWidth = strokePx,
                cap = StrokeCap.Butt
            )
        }
    }
}

private fun octagonPath(rect: Rect, overlap: Float): Path {
    val cx = rect.center.x
    val cy = rect.center.y
    val r = min(rect.width, rect.height) / 2f + overlap
    val k = (2f * r) / (2f + sqrt(2f))
    val points = listOf(
        Offset(cx - r + k, cy - r),
        Offset(cx + r - k, cy - r),
        Offset(cx + r, cy - r + k),
        Offset(cx + r, cy + r - k),
        Offset(cx + r - k, cy + r),
        Offset(cx - r + k, cy + r),
        Offset(cx - r, cy + r - k),
        Offset(cx - r, cy - r + k)
    )
    return Path().apply {
        moveTo(points[0].x, points[0].y)
        points.drop(1).forEach { lineTo(it.x, it.y) }
        close()
    }
}

//package com.jmisabella.mazer.screens.mazecomponents
//
//import android.content.Context
//import android.media.AudioManager
//import android.media.ToneGenerator
//import android.os.VibrationEffect
//import android.os.Vibrator
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.PaddingValues
//import androidx.compose.foundation.lazy.grid.GridCells
//import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
//import androidx.compose.foundation.lazy.grid.items
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.DisposableEffect
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.mutableStateMapOf
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.unit.dp
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.ui.graphics.graphicsLayer
//import com.jmisabella.mazer.layout.cellBackgroundColor
//import com.jmisabella.mazer.models.Coordinates
//import com.jmisabella.mazer.models.HeatMapPalette
//import com.jmisabella.mazer.models.MazeCell
//import kotlinx.coroutines.delay
//import kotlin.math.max
//import kotlin.math.min
//
//@Composable
//fun UpsilonMazeScreen(
//    cells: List<MazeCell>,
//    squareSize: Float,
//    octagonSize: Float,
//    showSolution: Boolean,
//    showHeatMap: Boolean,
//    selectedPalette: HeatMapPalette,
//    maxDistance: Int,
//    defaultBackgroundColor: Color,
//    optionalColor: Color?
//) {
//    val context = LocalContext.current
//    val rows = (cells.maxOfOrNull { it.y } ?: 0) + 1
//    val columns = (cells.maxOfOrNull { it.x } ?: 0) + 1
//    val overlapDp = (octagonSize - squareSize) / 2f
//    val negativeSpacing = -overlapDp
//
//    val revealedSolutionPath = remember { mutableStateMapOf<Coordinates, Boolean>() }
//
//    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator }
//    val toneGenerator = remember { ToneGenerator(AudioManager.STREAM_MUSIC, 100) }
//
//    DisposableEffect(Unit) {
//        onDispose {
//            toneGenerator.release()
//        }
//    }
//
//    LaunchedEffect(showSolution) {
//        if (showSolution) {
//            revealedSolutionPath.clear()
//            val pathCells = cells.filter { it.onSolutionPath && !it.isVisited }.sortedBy { it.distance }
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
//    val sortedCells = remember(cells) { cells.sortedBy { it.y * columns + it.x } }
//
//    LazyVerticalGrid(
//        columns = GridCells.Fixed(columns),
//        horizontalArrangement = Arrangement.spacedBy(negativeSpacing.dp),
//        verticalArrangement = Arrangement.spacedBy(negativeSpacing.dp),
//        modifier = Modifier,
//        contentPadding = PaddingValues(0.dp)
//    ) {
//        items(sortedCells) { cell ->
//            val coord = Coordinates(cell.x, cell.y)
//            val fillColor = cellBackgroundColor(
//                cell = cell,
//                showSolution = showSolution,
//                showHeatMap = showHeatMap,
//                maxDistance = maxDistance,
//                selectedPalette = selectedPalette,
//                isRevealedSolution = revealedSolutionPath[coord] ?: false,
//                defaultBackground = defaultBackgroundColor,
//                totalRows = rows,
//                optionalColor = optionalColor
//            )
//            UpsilonCell(
//                cell = cell,
//                gridCellSize = octagonSize,
//                squareSize = squareSize,
//                isSquare = cell.isSquare,
//                fillColor = fillColor,
//                optionalColor = optionalColor,
//                modifier = Modifier.size(octagonSize.dp)
//            )
//        }
//    }
//}

