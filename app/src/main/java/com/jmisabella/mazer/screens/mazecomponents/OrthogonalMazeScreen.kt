package com.jmisabella.mazer.screens.mazecomponents

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.jmisabella.mazer.layout.CellColors
import com.jmisabella.mazer.layout.computeCellSize
import com.jmisabella.mazer.models.CellSize
import com.jmisabella.mazer.models.HeatMapPalette
import com.jmisabella.mazer.models.MazeCell
import com.jmisabella.mazer.models.MazeType
import kotlinx.coroutines.delay
import kotlin.math.min

@Composable
fun OrthogonalMazeScreen(
    selectedPalette: MutableState<HeatMapPalette>,
    cells: List<MazeCell>,
    showSolution: Boolean,
    showHeatMap: Boolean,
    defaultBackgroundColor: Color,
    optionalColor: Color?
) {
    val context = LocalContext.current
    val columns = (cells.maxOfOrNull { it.x } ?: 0) + 1
    val rows = (cells.maxOfOrNull { it.y } ?: 0) + 1
    val maxDistance = cells.maxOfOrNull { it.distance } ?: 1
    val totalRows = rows

    val cellSize = computeCellSize(cells, MazeType.ORTHOGONAL, CellSize.MEDIUM, context).dp

    val revealedSolutionPath = remember { mutableStateMapOf<Pair<Int, Int>, Boolean>() }
    val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    LaunchedEffect(showSolution, cells) {
        revealedSolutionPath.clear()
        if (showSolution) {
            val pathCoords = cells
                .filter { it.onSolutionPath && !it.isVisited }
                .sortedBy { it.distance }
                .map { Pair(it.x, it.y) }

            val baseDelay = 0.015
            val speedFactor = min(1.0, cellSize.value.toDouble() / 50.0)
            val interval = (baseDelay * speedFactor * 1000.0).toLong()

            pathCoords.forEachIndexed { i, coord ->
                delay((i * interval).toLong())
                toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 200)
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                revealedSolutionPath[coord] = true
            }
        }
    }

    Box(
        modifier = Modifier
            .size(cellSize * columns, cellSize * rows)
            .background(if (!isSystemInDarkTheme()) CellColors.offWhite else Color.Black)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier.fillMaxSize()
        ) {
            items(cells.size) { i ->
                val cell = cells[i]
                OrthogonalCellScreen(
                    cell = cell,
                    cellSize = cellSize,
                    showSolution = showSolution,
                    showHeatMap = showHeatMap,
                    selectedPalette = selectedPalette.value,
                    maxDistance = maxDistance,
                    isRevealedSolution = revealedSolutionPath[Pair(cell.x, cell.y)] ?: false,
                    defaultBackgroundColor = defaultBackgroundColor,
                    optionalColor = optionalColor,
                    totalRows = totalRows
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(4.dp, Color.Black)
        )
    }
}