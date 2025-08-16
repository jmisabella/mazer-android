// OrthogonalMazeScreen.kt
package com.jmisabella.mazer.screens.mazecomponents

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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
    optionalColor: Color?,
    cellSize: CellSize
) {
    val context = LocalContext.current
    val columns = (cells.maxOfOrNull { it.x } ?: 0) + 1
    val rows = (cells.maxOfOrNull { it.y } ?: 0) + 1
    val maxDistance = cells.maxOfOrNull { it.distance } ?: 1
    val totalRows = rows

    // Compute cell size
//    val cellSize = computeCellSize(cells, MazeType.ORTHOGONAL, CellSize.MEDIUM, context).dp
    val cellSize = computeCellSize(cells, MazeType.ORTHOGONAL, cellSize, context).dp
    val strokeWidth = when (cellSize.value) {
        in 0f..20f -> 2.dp
        in 20f..40f -> 3.dp
        else -> 4.dp
    }
    val borderWidth = strokeWidth // Set to strokeWidth to fill the padding area and avoid gaps
    val totalPadding = strokeWidth * 2 // Account for walls on both sides

//    println("Maze: cols=$columns, rows=$rows, cellSize=${cellSize.value}dp, totalWidth=${(cellSize * columns + totalPadding).value}dp, totalHeight=${(cellSize * rows + totalPadding).value}dp")

    val revealedSolutionPath = remember { mutableStateMapOf<Pair<Int, Int>, Boolean>() }
    val toneGenerator = remember { ToneGenerator(AudioManager.STREAM_MUSIC, 100) }
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator }

    DisposableEffect(Unit) {
        onDispose {
            toneGenerator.release()
        }
    }

    LaunchedEffect(showSolution, cells) {
        revealedSolutionPath.clear()
        if (showSolution) {
            val pathCoords = cells
                .filter { it.onSolutionPath && !it.isVisited }
                .sortedBy { it.distance }
                .map { Pair(it.x, it.y) }

            val baseDelay = 0.005
            val speedFactor = min(1.0, cellSize.value.toDouble() / 50.0)
            val interval = (baseDelay * speedFactor * 1000.0).toLong()

            pathCoords.forEach { coord ->
                delay(interval)
//                toneGenerator.startTone(ToneGenerator.TONE_PROP_PROMPT, 200)
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        50,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
                revealedSolutionPath[coord] = true
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (!isSystemInDarkTheme()) CellColors.offWhite else Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(cellSize * columns + totalPadding)
                .wrapContentHeight()
        ) {
            Column(modifier = Modifier.align(Alignment.Center)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(borderWidth)
                        .background(Color.Black)
                )
                Column(modifier = Modifier.padding(horizontal = strokeWidth)) {
                    repeat(rows) { row ->
                        Row {
                            repeat(columns) { col ->
                                val i = row * columns + col
                                val cell = cells[i]
                                OrthogonalCellScreen(
                                    cell = cell,
                                    cellSize = cellSize,
                                    showSolution = showSolution,
                                    showHeatMap = showHeatMap,
                                    selectedPalette = selectedPalette.value,
                                    maxDistance = maxDistance,
                                    isRevealedSolution = revealedSolutionPath[Pair(cell.x, cell.y)]
                                        ?: false,
                                    defaultBackgroundColor = defaultBackgroundColor,
                                    optionalColor = optionalColor,
                                    totalRows = totalRows
                                )
                            }
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(borderWidth)
                        .background(Color.Black)
                )
            }
        }
    }
}
