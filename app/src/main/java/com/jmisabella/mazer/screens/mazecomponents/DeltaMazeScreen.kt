// New file: DeltaMazeScreen.kt
package com.jmisabella.mazer.screens.mazecomponents

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.jmisabella.mazer.layout.CellColors
import com.jmisabella.mazer.layout.computeCellSize
import com.jmisabella.mazer.models.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.math.max
import kotlin.math.min

@Composable
fun DeltaMazeScreen(
    cells: List<MazeCell>,
    cellSize: Float,
    showSolution: Boolean,
    showHeatMap: Boolean,
    selectedPalette: HeatMapPalette,
    maxDistance: Int,
    defaultBackgroundColor: Color,
    optionalColor: Color?
) {
    val context = LocalContext.current
    val density = androidx.compose.ui.platform.LocalDensity.current.density
    val columns = (cells.maxOfOrNull { it.x } ?: 0) + 1
    val rows = (cells.maxOfOrNull { it.y } ?: 0) + 1
    val totalRows = rows
    val cellSizeDp = cellSize.dp
    val triangleHeight = cellSize * sqrt(3f) / 2f
    val totalWidth = (cellSize * columns.toFloat() * 0.75f).dp
    val totalHeight = (triangleHeight * rows.toFloat()).dp

    fun snap(value: Float): Float = (value * density).roundToInt().toFloat() / density

    val cellMap: Map<Coordinates, MazeCell> = cells.associateBy { Coordinates(it.x, it.y) }
    val revealedSolutionPath = remember { mutableStateMapOf<Coordinates, Boolean>() }
    val coroutineScope = rememberCoroutineScope()
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
                    toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 200)
                    vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                    lastFeedbackTime = currentTime
                }
                index += batchSize
            }
        } else {
            revealedSolutionPath.clear()
        }
    }

    Box(
        modifier = Modifier
            .size(width = totalWidth, height = totalHeight)
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(snap(0f).dp)
        ) {
            repeat(rows) { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    horizontalArrangement = Arrangement.spacedBy(snap(-cellSize / 2f).dp)
                ) {
                    repeat(columns) { col ->
                        val coord = Coordinates(col, row)
                        val cell = cellMap[coord]
                        if (cell != null) {
                            DeltaCellScreen(
                                cell = cell,
                                cellSize = cellSizeDp,
                                showSolution = showSolution,
                                showHeatMap = showHeatMap,
                                selectedPalette = selectedPalette,
                                maxDistance = maxDistance,
                                isRevealedSolution = revealedSolutionPath[coord] ?: false,
                                defaultBackgroundColor = defaultBackgroundColor,
                                optionalColor = optionalColor,
                                totalRows = totalRows
                            )
                        } // No else clause needed; skip if null (matches Swift EmptyView)
                    }
                }
            }
        }

        // Top overlay
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .width(totalWidth * 0.9f)
                .height(2.dp)
                .background(Color.Black)
        )

        // Bottom overlay
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .width(totalWidth * 0.9f)
                .height(2.dp)
                .background(Color.Black)
        )
    }
}

