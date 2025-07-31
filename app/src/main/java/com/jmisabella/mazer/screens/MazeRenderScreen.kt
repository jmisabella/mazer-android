package com.jmisabella.mazer.screens

import android.annotation.SuppressLint
import com.jmisabella.mazer.screens.mazecomponents.OrthogonalMazeScreen
import com.jmisabella.mazer.screens.directioncontrols.FourWayControlScreen
import com.jmisabella.mazer.screens.directioncontrols.FourWayDiagonalControlScreen
import com.jmisabella.mazer.screens.directioncontrols.EightWayControlScreen

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Vibrator
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.jmisabella.mazer.layout.CellSizes
import com.jmisabella.mazer.layout.navigationMenuHorizontalAdjustment
import com.jmisabella.mazer.layout.navigationMenuVerticalAdjustment
import com.jmisabella.mazer.models.*
import kotlin.math.*

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun MazeRenderScreen(
    mazeGenerated: MutableState<Boolean>,
    showSolution: MutableState<Boolean>,
    showHeatMap: MutableState<Boolean>,
    showControls: MutableState<Boolean>,
    padOffset: MutableState<Offset>,
    selectedPalette: MutableState<HeatMapPalette>,
    mazeID: String,
    defaultBackground: MutableState<Color>,
    showHelp: MutableState<Boolean>,
    mazeCells: List<MazeCell>,
    mazeType: MazeType,
    cellSize: CellSize,
    optionalColor: Color?,
    regenerateMaze: () -> Unit,
    moveAction: (String) -> Unit,
    cellSizes: CellSizes,
    toggleHeatMap: () -> Unit,
    cleanupMazeData: () -> Unit
) {
    val context = LocalContext.current
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
    val coroutineScope = rememberCoroutineScope()

    var lastLocation by remember { mutableStateOf<Offset?>(null) }
    var cumulativePathLength by remember { mutableStateOf(0f) }
    var directionVector by remember { mutableStateOf(Offset.Zero) }
    var lastMoveDirection by remember { mutableStateOf<String?>(null) }
    var performedMoves by remember { mutableStateOf(0) }
    var dragStartOffset by remember { mutableStateOf(Offset.Zero) }

    val performMove: (String) -> Unit = { dir ->
        showSolution.value = false
        moveAction(dir)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.offset(
                x = navigationMenuHorizontalAdjustment(mazeType, cellSize, context).dp,
                y = navigationMenuVerticalAdjustment(mazeType, cellSize, context).dp
            ),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(onClick = cleanupMazeData) {
                Icon(Icons.Default.Menu, contentDescription = "Back to maze settings", tint = Color.Blue)
            }
            IconButton(onClick = {
                showSolution.value = false
                regenerateMaze()
            }) {
                Icon(Icons.Default.Refresh, contentDescription = "Generate new maze", tint = Color(0xFF800080))
            }
            IconButton(onClick = { showSolution.value = !showSolution.value }) {
                Icon(
                    if (showSolution.value) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                    contentDescription = "Toggle solution path",
                    tint = if (showSolution.value) Color.Green else Color.Gray
                )
            }
            IconButton(onClick = toggleHeatMap) {
                Icon(
                    if (showHeatMap.value) Icons.Filled.LocalFireDepartment else Icons.Outlined.LocalFireDepartment,
                    contentDescription = "Toggle heat map",
                    tint = if (showHeatMap.value) Color(0xFFFFA500) else Color.Gray
                )
            }
            IconButton(onClick = { showControls.value = !showControls.value }) {
                Icon(
                    if (showControls.value) Icons.Filled.Close else Icons.Filled.MoreHoriz,
                    contentDescription = "Toggle navigation controls",
                    tint = Color.Gray
                )
            }
            IconButton(onClick = { showHelp.value = true }) {
                Icon(Icons.Outlined.HelpOutline, contentDescription = "Help instructions", tint = Color.Gray)
            }
        }

        val density = LocalDensity.current.density
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            if (lastLocation == null) {
                                lastLocation = change.position
                                return@detectDragGestures
                            }

                            val currentLocation = change.position
                            val deltaX = currentLocation.x - lastLocation!!.x
                            val deltaY = currentLocation.y - lastLocation!!.y
                            lastLocation = currentLocation

                            val delta_tx = deltaX
                            val delta_ty = -deltaY
                            val delta_mag = sqrt(delta_tx * delta_tx + delta_ty * delta_ty)
                            if (delta_mag == 0f) return@detectDragGestures

                            cumulativePathLength += delta_mag

                            val alpha = 0.3f
                            directionVector = if (directionVector == Offset.Zero) {
                                Offset(delta_tx, delta_ty)
                            } else {
                                Offset(
                                    directionVector.x * alpha + delta_tx * (1 - alpha),
                                    directionVector.y * alpha + delta_ty * (1 - alpha)
                                )
                            }

                            if (directionVector == Offset.Zero) return@detectDragGestures

                            val angle = atan2(directionVector.y.toDouble(), directionVector.x.toDouble()).toFloat()
                            var shifted = angle + (Math.PI.toFloat() / 8)
                            if (shifted < 0) shifted += (2 * Math.PI).toFloat()
                            val sector = floor(shifted / (Math.PI.toFloat() / 4)).toInt() % 8
                            val directions = listOf(
                                "Right", "UpperRight", "Up", "UpperLeft",
                                "Left", "LowerLeft", "Down", "LowerRight"
                            )
                            val currentDirection = directions[sector]
                            lastMoveDirection = currentDirection

                            val baseDimDp = com.jmisabella.mazer.layout.computeCellSize(mazeCells, mazeType, cellSize, context)
                            val baseDim = baseDimDp * density // convert dp to pixels
                            val dim = when (mazeType) {
                                MazeType.SIGMA -> baseDim * sqrt(3f)
                                MazeType.ORTHOGONAL -> baseDim
                                MazeType.DELTA -> baseDim / sqrt(3f)
                                else -> baseDim
                            }

                            val totalMovesNeeded = floor(cumulativePathLength / dim).toInt()
                            val movesToPerform = totalMovesNeeded - performedMoves

                            if (movesToPerform > 0) {
                                repeat(movesToPerform) {
                                    performMove(currentDirection)
                                }
                                performedMoves += movesToPerform
                            }
                        }
                    }
            ) {
                val mazeContent = @Composable {
                    when (mazeType) {
                        MazeType.ORTHOGONAL -> OrthogonalMazeScreen(
                            selectedPalette = selectedPalette,
                            cells = mazeCells,
                            showSolution = showSolution.value,
                            showHeatMap = showHeatMap.value,
                            defaultBackgroundColor = defaultBackground.value,
                            optionalColor = optionalColor
                        )
                        else -> Text("Unsupported maze type")
                    }
                }
                mazeContent()
            }

            if (showControls.value) {
                Column(modifier = Modifier.align(Alignment.BottomCenter)) {
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.8f))
                            .padding(16.dp)
                            .offset(x = padOffset.value.x.dp, y = padOffset.value.y.dp)
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    padOffset.value = Offset(
                                        padOffset.value.x + dragAmount.x,
                                        padOffset.value.y + dragAmount.y
                                    )
                                }
                            }
                    ) {
                        when (mazeType) {
                            MazeType.ORTHOGONAL -> FourWayControlScreen(performMove)
                            MazeType.SIGMA -> FourWayDiagonalControlScreen(performMove)
                            MazeType.DELTA -> EightWayControlScreen(performMove)
                            else -> Text("Controls not available for this maze type")
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        val sharedPrefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val key = "hasSeenHelpInstructions"
        val hasSeen = sharedPrefs.getBoolean(key, false)
        if (!hasSeen) {
            showHelp.value = true
            sharedPrefs.edit().putBoolean(key, true).apply()
        }
        toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 200)
    }
}

