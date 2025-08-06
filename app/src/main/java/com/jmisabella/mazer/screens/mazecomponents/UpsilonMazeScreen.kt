package com.jmisabella.mazer.screens.mazecomponents

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import com.jmisabella.mazer.layout.cellBackgroundColor
import com.jmisabella.mazer.models.Coordinates
import com.jmisabella.mazer.models.HeatMapPalette
import com.jmisabella.mazer.models.MazeCell
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.min

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

    val sortedCells = remember(cells) { cells.sortedBy { it.y * columns + it.x } }

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        horizontalArrangement = Arrangement.spacedBy(negativeSpacing.dp),
        verticalArrangement = Arrangement.spacedBy(negativeSpacing.dp),
        modifier = Modifier,
        contentPadding = PaddingValues(0.dp)
    ) {
        items(sortedCells) { cell ->
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
            UpsilonCell(
                cell = cell,
                gridCellSize = octagonSize,
                squareSize = squareSize,
                isSquare = cell.isSquare,
                fillColor = fillColor,
                optionalColor = optionalColor,
                modifier = Modifier.size(octagonSize.dp)
            )
        }
    }
}

//package com.jmisabella.mazer.screens.mazecomponents
//
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.PaddingValues
//import androidx.compose.foundation.lazy.grid.GridCells
//import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
//import androidx.compose.foundation.lazy.grid.items
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.unit.dp
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import com.jmisabella.mazer.layout.cellBackgroundColor
//import com.jmisabella.mazer.models.Coordinates
//import com.jmisabella.mazer.models.HeatMapPalette
//import com.jmisabella.mazer.models.MazeCell
//import kotlinx.coroutines.delay
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
//    val rows = (cells.maxOfOrNull { it.y } ?: 0) + 1
//    val columns = (cells.maxOfOrNull { it.x } ?: 0) + 1
//    val overlapDp = (octagonSize - squareSize) / 2f
//    val negativeSpacing = -overlapDp
//
//    var revealedSolutionPath by remember { mutableStateOf(setOf<Coordinates>()) }
//
//    LaunchedEffect(showSolution) {
//        if (showSolution) {
//            revealedSolutionPath = emptySet<Coordinates>()
//            val pathCells = cells.filter { it.onSolutionPath && !it.isVisited }.sortedBy { it.distance }
//            pathCells.forEachIndexed { index, cell ->
//                delay(15L * index.toLong())
//                revealedSolutionPath = revealedSolutionPath + Coordinates(cell.x, cell.y)
//            }
//        } else {
//            revealedSolutionPath = emptySet<Coordinates>()
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
//                isRevealedSolution = revealedSolutionPath.contains(coord),
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
//
