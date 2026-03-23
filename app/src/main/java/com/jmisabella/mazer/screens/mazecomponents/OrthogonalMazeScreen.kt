package com.jmisabella.mazer.screens.mazecomponents

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.jmisabella.mazer.layout.CellColors
import com.jmisabella.mazer.layout.cellBackgroundColor
import com.jmisabella.mazer.layout.computeCellSize
import com.jmisabella.mazer.layout.wallStrokeWidth
import com.jmisabella.mazer.models.CellSize
import com.jmisabella.mazer.models.HeatMapPalette
import com.jmisabella.mazer.models.MazeCell
import com.jmisabella.mazer.models.MazeType
import kotlinx.coroutines.delay
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun OrthogonalMazeScreen(
    selectedPalette: HeatMapPalette,
    cells: List<MazeCell>,
    showSolution: Boolean,
    showHeatMap: Boolean,
    defaultBackgroundColor: Color,
    optionalColor: Color?,
    cellSize: CellSize,
    availableHeightDp: Float? = null
) {
    val context = LocalContext.current
    val columns = (cells.maxOfOrNull { it.x } ?: 0) + 1
    val rows = (cells.maxOfOrNull { it.y } ?: 0) + 1
    val maxDistance = cells.maxOfOrNull { it.distance } ?: 1
    val totalRows = rows

    val cellSizeDp = computeCellSize(cells, MazeType.ORTHOGONAL, cellSize, context, availableHeightDp).dp
    val strokeWidth = wallStrokeWidth(MazeType.ORTHOGONAL, cellSizeDp.value, LocalDensity.current.density).dp

    val borderWidth = strokeWidth
    val totalPadding = strokeWidth * 2

    val revealedSolutionPath = remember { mutableStateMapOf<Pair<Int, Int>, Boolean>() }
    val toneGenerator = remember { ToneGenerator(AudioManager.STREAM_MUSIC, 100) }
    val vibrator = remember { context.getSystemService(Vibrator::class.java) }

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
            val speedFactor = min(1.0, cellSizeDp.value.toDouble() / 50.0)
            val interval = (baseDelay * speedFactor * 1000.0).toLong()

            pathCoords.forEach { coord ->
                delay(interval)
                vibrator?.vibrate(
                    VibrationEffect.createOneShot(
                        50,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
                revealedSolutionPath[coord] = true
            }
        }
    }

    val density = LocalDensity.current.density

    fun snap(value: Float): Float = (value * density).roundToInt().toFloat() / density

    val cellSizePx = remember(cellSizeDp, density) { snap(cellSizeDp.value * density) }
    val strokeWidthPx = remember(strokeWidth, density) { snap(strokeWidth.value * density) }
    val totalPaddingPx = remember(totalPadding, density) { snap(totalPadding.value * density) }
    val totalWidthDp = cellSizeDp * columns + totalPadding
    val totalHeightDp = cellSizeDp * rows + totalPadding
    val totalWidthPx = cellSizePx * columns + totalPaddingPx
    val totalHeightPx = cellSizePx * rows + totalPaddingPx
    val overlapPx = 0.5f  // Small overlap to cover potential gaps

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (!isSystemInDarkTheme()) CellColors.offWhite else Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(totalWidthDp, totalHeightDp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Draw top border
                drawRect(
                    color = Color.Black,
                    topLeft = Offset(0f, 0f),
                    size = Size(totalWidthPx, strokeWidthPx)
                )
                // Draw bottom border
                drawRect(
                    color = Color.Black,
                    topLeft = Offset(0f, totalHeightPx - strokeWidthPx),
                    size = Size(totalWidthPx, strokeWidthPx)
                )

                for (row in 0 until rows) {
                    for (col in 0 until columns) {
                        val i = row * columns + col
                        val cell = cells[i]
                        val offsetXPx = snap(strokeWidthPx + col * cellSizePx)
                        val offsetYPx = snap(strokeWidthPx + row * cellSizePx)

                        val fillColor = cellBackgroundColor(
                            cell = cell,
                            showSolution = showSolution,
                            showHeatMap = showHeatMap,
                            maxDistance = maxDistance,
                            selectedPalette = selectedPalette,
                            isRevealedSolution = revealedSolutionPath[Pair(cell.x, cell.y)] ?: false,
                            defaultBackground = defaultBackgroundColor,
                            totalRows = totalRows,
                            optionalColor = optionalColor
                        )

                        withTransform({
                            translate(offsetXPx, offsetYPx)
                        }) {
                            drawRect(
                                color = fillColor,
                                topLeft = Offset(-overlapPx / 2, -overlapPx / 2),
                                size = Size(cellSizePx + overlapPx, cellSizePx + overlapPx)
                            )

                            // Draw left wall
                            if (!cell.linked.contains("Left")) {
                                drawLine(
                                    color = Color.Black,
                                    start = Offset(0f, 0f),
                                    end = Offset(0f, cellSizePx),
                                    strokeWidth = strokeWidthPx,
                                    cap = StrokeCap.Butt
                                )
                            }

                            // Draw right wall
                            if (!cell.linked.contains("Right")) {
                                drawLine(
                                    color = Color.Black,
                                    start = Offset(cellSizePx, 0f),
                                    end = Offset(cellSizePx, cellSizePx),
                                    strokeWidth = strokeWidthPx,
                                    cap = StrokeCap.Butt
                                )
                            }

                            // Draw top wall only if not top row (since solid border handles it)
                            if (row > 0 && !cell.linked.contains("Up")) {
                                drawLine(
                                    color = Color.Black,
                                    start = Offset(0f, 0f),
                                    end = Offset(cellSizePx, 0f),
                                    strokeWidth = strokeWidthPx,
                                    cap = StrokeCap.Butt
                                )
                            }

                            // Draw bottom wall only if not bottom row (since solid border handles it)
                            if (row < rows - 1 && !cell.linked.contains("Down")) {
                                drawLine(
                                    color = Color.Black,
                                    start = Offset(0f, cellSizePx),
                                    end = Offset(cellSizePx, cellSizePx),
                                    strokeWidth = strokeWidthPx,
                                    cap = StrokeCap.Butt
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

