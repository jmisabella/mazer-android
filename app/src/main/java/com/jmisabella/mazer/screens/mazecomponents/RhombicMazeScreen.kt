package com.jmisabella.mazer.screens.mazecomponents

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.jmisabella.mazer.layout.cellBackgroundColor
import com.jmisabella.mazer.models.Coordinates
import com.jmisabella.mazer.models.HeatMapPalette
import com.jmisabella.mazer.models.MazeCell
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

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
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(containerWidth.dp, containerHeight.dp)
                .background(Color.Transparent)
        ) {
            cells.forEach { cell ->
                val coord = Coordinates(cell.x, cell.y)
                RhombicCellScreen(
                    cell = cell,
                    cellSize = cellSize.dp,
                    showSolution = showSolution,
                    showHeatMap = showHeatMap,
                    selectedPalette = selectedPalette.value,
                    maxDistance = maxDistance,
                    isRevealedSolution = revealedSolutionPath[coord] ?: false,
                    defaultBackgroundColor = defaultBackgroundColor,
                    optionalColor = optionalColor,
                    totalRows = totalRows,
                    modifier = Modifier.offset((cell.x * halfDiagonal).dp, (cell.y * halfDiagonal).dp)
                )
            }
        }
    }
}