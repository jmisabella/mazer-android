package com.jmisabella.mazer.screens

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.platform.LocalConfiguration
import com.jmisabella.mazer.layout.CellColors
import com.jmisabella.mazer.layout.CellSizes
import com.jmisabella.mazer.layout.computeCellSize
import com.jmisabella.mazer.layout.navigationMenuVerticalAdjustment
import com.jmisabella.mazer.models.*
import com.jmisabella.mazer.screens.mazecomponents.DeltaMazeScreen
import com.jmisabella.mazer.screens.mazecomponents.OrthogonalMazeScreen
import com.jmisabella.mazer.screens.mazecomponents.RhombicMazeScreen
import com.jmisabella.mazer.screens.mazecomponents.SigmaMazeScreen
import com.jmisabella.mazer.screens.mazecomponents.UpsilonMazeScreen
import kotlinx.coroutines.delay
import kotlin.math.min
import kotlin.math.sqrt

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun MazeGenerationAnimationScreen(
    generationSteps: List<List<MazeCell>>,
    mazeType: MazeType,
    cellSize: CellSize,
    isAnimatingGeneration: MutableState<Boolean>,
    mazeGenerated: MutableState<Boolean>,
    showSolution: MutableState<Boolean>,
    showHeatMap: MutableState<Boolean>,
    showControls: MutableState<Boolean>,
    selectedPalette: MutableState<HeatMapPalette>,
    defaultBackground: MutableState<Color>,
    showHelp: MutableState<Boolean>,
    mazeID: String,
    currentGrid: Long?,
    regenerateMaze: () -> Unit,
    cleanupMazeData: () -> Unit,
    cellSizes: CellSizes,
    optionalColor: Color?
) {
    val context = LocalContext.current
    val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)

    var currentStepIndex by remember { mutableStateOf(0) }

    // Helper functions (copied/adapted from MainActivity for use here)
    fun randomPaletteExcluding(current: HeatMapPalette, allPalettes: List<HeatMapPalette>): HeatMapPalette {
        val availablePalettes = allPalettes.filter { it != current }
        return availablePalettes.randomOrNull() ?: current
    }

    fun randomDefaultExcluding(current: Color, all: List<Color>): Color {
        val others = all.filter { it != current }
        return others.randomOrNull() ?: current
    }

    fun toggleHeatMap() {
        showHeatMap.value = !showHeatMap.value
        if (showHeatMap.value) {
            selectedPalette.value = randomPaletteExcluding(selectedPalette.value, allPalettes)
            defaultBackground.value = randomDefaultExcluding(defaultBackground.value, CellColors.defaultBackgroundColors)
        }
    }

    fun completeAnimation() {
        isAnimatingGeneration.value = false
        mazeGenerated.value = true
//        toneGenerator.startTone(ToneGenerator.TONE_PROP_PROMPT, 200)
        defaultBackground.value = randomDefaultExcluding(defaultBackground.value, CellColors.defaultBackgroundColors)
    }

    // Get status bar insets including display cutout
    val density = LocalDensity.current
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = statusBarPadding.calculateTopPadding() + navigationMenuVerticalAdjustment(mazeType, cellSize, context).dp,
                    start = statusBarPadding.calculateStartPadding(LocalLayoutDirection.current),
                    end = statusBarPadding.calculateEndPadding(LocalLayoutDirection.current)
                ),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = cleanupMazeData) {
                Icon(
                    Icons.Default.Menu,
                    contentDescription = "Back to maze settings",
                    tint = Color.Blue,
                    modifier = Modifier.size(32.dp)
                )
            }
            IconButton(onClick = {}, enabled = false) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Generate new maze",
                    tint = Color.Gray,
                    modifier = Modifier.size(32.dp)
                )
            }
            IconButton(onClick = { showSolution.value = !showSolution.value }) {
                Icon(
                    if (showSolution.value) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                    contentDescription = "Toggle solution path",
                    tint = if (showSolution.value) Color.Green else Color.Gray,
                    modifier = Modifier.size(32.dp)
                )
            }
            IconButton(onClick = { toggleHeatMap() }) {
                Icon(
                    if (showHeatMap.value) Icons.Filled.LocalFireDepartment else Icons.Outlined.LocalFireDepartment,
                    contentDescription = "Toggle heat map",
                    tint = if (showHeatMap.value) Color(0xFFFFA500) else Color.Gray,
                    modifier = Modifier.size(32.dp)
                )
            }
            IconButton(onClick = {}, enabled = false) {
                Icon(
                    Icons.Filled.MoreHoriz,
                    contentDescription = "Toggle navigation controls",
                    tint = Color.Gray,
                    modifier = Modifier.size(32.dp)
                )
            }
            IconButton(onClick = {}, enabled = false) {
                Icon(
                    Icons.Outlined.HelpOutline,
                    contentDescription = "Help instructions",
                    tint = Color.Gray,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        BoxWithConstraints(
            modifier = Modifier
//                .fillMaxSize()
                .fillMaxWidth() //+
                .weight(1f) //+
                .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)) //++
                .padding(bottom = 20.dp) // Added bottom padding to prevent cutoff at curved corners
        ) {
            if (currentStepIndex < generationSteps.size) {
                val currentCells = generationSteps[currentStepIndex]
                val localDensity = LocalDensity.current.density
                val availableHeightDp = constraints.maxHeight.toFloat() / localDensity
                val rows = (currentCells.maxOfOrNull { it.y } ?: 0) + 1

                val mazeContent = @Composable {
                    val maxDistance = currentCells.maxOfOrNull { it.distance } ?: 1
                    val configuration = LocalConfiguration.current
                    val screenWidthDp = configuration.screenWidthDp.toFloat()
                    val screenHeightDp = configuration.screenHeightDp.toFloat()
                    val cols = (currentCells.maxOfOrNull { it.x } ?: 0) + 1
                    val rows = (currentCells.maxOfOrNull { it.y } ?: 0) + 1
                    when (mazeType) {
                        MazeType.ORTHOGONAL -> OrthogonalMazeScreen(
                            selectedPalette = selectedPalette.value,
                            cells = currentCells,
                            showSolution = showSolution.value,
                            showHeatMap = showHeatMap.value,
                            defaultBackgroundColor = defaultBackground.value,
                            optionalColor = optionalColor,
                            cellSize = cellSize,
                            availableHeightDp = availableHeightDp
                        )
                        MazeType.DELTA -> {
                            val cellSizeValue = computeCellSize(currentCells, mazeType, cellSize, context, availableHeightDp)
                            DeltaMazeScreen(
                                cells = currentCells,
                                cellSize = cellSizeValue,
                                showSolution = showSolution.value,
                                showHeatMap = showHeatMap.value,
                                selectedPalette = selectedPalette.value,
//                                maxDistance = maxDistance,
                                defaultBackgroundColor = defaultBackground.value,
                                optionalColor = optionalColor
                            )
                        }
                        MazeType.SIGMA -> {
                            val cellSizeValue = computeCellSize(currentCells, mazeType, cellSize, context, availableHeightDp)
                            SigmaMazeScreen(
                                cells = currentCells,
                                cellSize = cellSizeValue,
                                showSolution = showSolution.value,
                                showHeatMap = showHeatMap.value,
                                selectedPalette = selectedPalette.value,
                                defaultBackgroundColor = defaultBackground.value,
                                optionalColor = optionalColor
                            )
                        }
                        MazeType.RHOMBIC -> {
                            val cellSizeValue = computeCellSize(currentCells, mazeType, cellSize, context, availableHeightDp)
                            RhombicMazeScreen(
                                selectedPalette = selectedPalette,
                                cells = currentCells,
                                cellSize = cellSizeValue,
                                showSolution = showSolution.value,
                                showHeatMap = showHeatMap.value,
                                defaultBackgroundColor = defaultBackground.value,
                                optionalColor = optionalColor
                            )
                        }
                        MazeType.UPSILON -> {
                            val c = sqrt(2f) - 1f
                            val f = (1f - c) / 2f
                            val denom = rows - (rows - 1) * f
                            val fitOctagon = availableHeightDp / denom
                            val octagon = min(cellSizes.octagon, fitOctagon)
                            val square = octagon * c
                            UpsilonMazeScreen(
                                cells = currentCells,
                                squareSize = square,
                                octagonSize = octagon,
                                showSolution = showSolution.value,
                                showHeatMap = showHeatMap.value,
                                selectedPalette = selectedPalette.value,
                                maxDistance = maxDistance,
                                defaultBackgroundColor = defaultBackground.value,
                                optionalColor = optionalColor
                            )
                        }
                        else -> Text("Unsupported maze type")
                    }
                }

                mazeContent()

                // Cancel button in upper right
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    IconButton(onClick = { completeAnimation() }) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Cancel maze generation animation",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier
                                .size(57.dp)
                                .background(Color.Black.copy(alpha = 0.5f), shape = CircleShape)
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        for (i in 1 until generationSteps.size) {
            delay(50)
            currentStepIndex = i
            if (i == generationSteps.size - 1) {
                completeAnimation()
            }
        }
    }
}
