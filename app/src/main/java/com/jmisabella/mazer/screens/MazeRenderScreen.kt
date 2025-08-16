package com.jmisabella.mazer.screens

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.RenderEffect
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Vibrator
import android.os.Build
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.WindowInsets
import com.jmisabella.mazer.layout.CellSizes
import com.jmisabella.mazer.layout.computeCellSize
import com.jmisabella.mazer.layout.navigationMenuHorizontalAdjustment
import com.jmisabella.mazer.layout.navigationMenuVerticalAdjustment
import com.jmisabella.mazer.models.*
import com.jmisabella.mazer.screens.directioncontrols.EightWayControlScreen
import com.jmisabella.mazer.screens.directioncontrols.FourWayControlScreen
import com.jmisabella.mazer.screens.directioncontrols.FourWayDiagonalControlScreen
import com.jmisabella.mazer.screens.mazecomponents.DeltaMazeScreen
import com.jmisabella.mazer.screens.mazecomponents.OrthogonalMazeScreen
import com.jmisabella.mazer.screens.mazecomponents.RhombicMazeScreen
import com.jmisabella.mazer.screens.mazecomponents.SigmaMazeScreen
import com.jmisabella.mazer.screens.mazecomponents.UpsilonMazeScreen
import kotlin.math.*

fun colorMatrixSaturation(saturation: Float): ColorMatrix {
    return ColorMatrix().apply { setSaturation(saturation) }
}

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
    moveAction: (String) -> Boolean,
    cellSizes: CellSizes,
    toggleHeatMap: () -> Unit,
    cleanupMazeData: () -> Unit,
    showCelebration: MutableState<Boolean>
) {
    val context = LocalContext.current
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    val toneGenerator = remember { ToneGenerator(AudioManager.STREAM_MUSIC, 30) }
    DisposableEffect(toneGenerator) {
        onDispose {
            toneGenerator.release()
        }
    }
    var lastLocation by remember { mutableStateOf<Offset?>(null) }
    var cumulativePathLength by remember { mutableStateOf(0f) }
    var directionVector by remember { mutableStateOf(Offset.Zero) }
    var lastMoveDirection by remember { mutableStateOf<String?>(null) }
    var performedMoves by remember { mutableStateOf(0) }

    val performMove: (String) -> Unit = { dir ->
        showSolution.value = false
        if (moveAction(dir)) {
//            toneGenerator.startTone(ToneGenerator.TONE_CDMA_CONFIRM, 200)
        }
    }

    val targetSaturation = if (showCelebration.value) 0f else 1f
    val saturation = animateFloatAsState(
        targetValue = targetSaturation,
        animationSpec = tween(650, easing = androidx.compose.animation.core.EaseInOut),
        label = "saturation"
    )

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
            IconButton(onClick = {
                showSolution.value = false
                regenerateMaze()
            }) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Generate new maze",
                    tint = Color(0xFF800080),
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
            IconButton(onClick = toggleHeatMap) {
                Icon(
                    if (showHeatMap.value) Icons.Filled.LocalFireDepartment else Icons.Outlined.LocalFireDepartment,
                    contentDescription = "Toggle heat map",
                    tint = if (showHeatMap.value) Color(0xFFFFA500) else Color.Gray,
                    modifier = Modifier.size(32.dp)
                )
            }
            IconButton(onClick = { showControls.value = !showControls.value }) {
                Icon(
                    if (showControls.value) Icons.Filled.Close else Icons.Filled.MoreHoriz,
                    contentDescription = "Toggle navigation controls",
                    tint = Color.Gray,
                    modifier = Modifier.size(32.dp)
                )
            }
            IconButton(onClick = { showHelp.value = true }) {
                Icon(
                    Icons.Outlined.HelpOutline,
                    contentDescription = "Help instructions",
                    tint = Color.Gray,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        val density = LocalDensity.current.density
        BoxWithConstraints(
            modifier = Modifier
//                .fillMaxSize()
                .fillMaxWidth() //+
                .weight(1f) //+
                .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)) //++
                .padding(bottom = 20.dp)
        ) {
            val localDensity = LocalDensity.current.density
            val availableHeightDp = constraints.maxHeight.toFloat() / localDensity
            val rows = (mazeCells.maxOfOrNull { it.y } ?: 0) + 1
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {  // API 31+
                            renderEffect = RenderEffect.createColorFilterEffect(
                                ColorMatrixColorFilter(colorMatrixSaturation(saturation.value))
                            ).asComposeRenderEffect()
                        }
                    }
            ) {
                val mazeContent = @Composable {
                    val maxDistance = mazeCells.maxOfOrNull { it.distance } ?: 1
                    val cellSizeValue = computeCellSize(mazeCells, mazeType, cellSize, context)
                    when (mazeType) {
                        MazeType.ORTHOGONAL -> OrthogonalMazeScreen(
                            selectedPalette = selectedPalette,
                            cells = mazeCells,
                            showSolution = showSolution.value,
                            showHeatMap = showHeatMap.value,
                            defaultBackgroundColor = defaultBackground.value,
                            optionalColor = optionalColor,
                            cellSize = cellSize
                        )
                        MazeType.DELTA -> DeltaMazeScreen(
                            cells = mazeCells,
                            cellSize = cellSizeValue,
                            showSolution = showSolution.value,
                            showHeatMap = showHeatMap.value,
                            selectedPalette = selectedPalette.value,
                            maxDistance = maxDistance,
                            defaultBackgroundColor = defaultBackground.value,
                            optionalColor = optionalColor
                        )
                        MazeType.SIGMA -> SigmaMazeScreen(
                            cells = mazeCells,
                            cellSize = cellSizeValue,
                            showSolution = showSolution.value,
                            showHeatMap = showHeatMap.value,
                            selectedPalette = selectedPalette.value,
                            defaultBackgroundColor = defaultBackground.value,
                            optionalColor = optionalColor
                        )
                        MazeType.RHOMBIC -> RhombicMazeScreen(
                            selectedPalette = selectedPalette,
                            cells = mazeCells,
                            cellSize = cellSizeValue,
                            showSolution = showSolution.value,
                            showHeatMap = showHeatMap.value,
                            defaultBackgroundColor = defaultBackground.value,
                            optionalColor = optionalColor
                        )
                        MazeType.UPSILON -> {
                            val c = sqrt(2f) - 1f
                            val f = (1f - c) / 2f
                            val denom = rows - (rows - 1) * f
                            val fitOctagon = availableHeightDp / denom
                            val octagon = min(cellSizes.octagon, fitOctagon)
                            val square = octagon * c
                            UpsilonMazeScreen(
                                cells = mazeCells,
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
//                        MazeType.UPSILON -> {
////                            val fitOctagon = availableHeightDp / rows.toFloat()
////                            val octagon = min(cellSizes.octagon, fitOctagon)
////                            val square = octagon / sqrt(2f).toFloat()
//                            UpsilonMazeScreen(
//                                cells = mazeCells,
//                                squareSize = cellSizes.square,
//                                octagonSize = cellSizes.octagon,
//                                showSolution = showSolution.value,
//                                showHeatMap = showHeatMap.value,
//                                selectedPalette = selectedPalette.value,
//                                maxDistance = maxDistance,
//                                defaultBackgroundColor = defaultBackground.value,
//                                optionalColor = optionalColor
//                            )
//                        }
                        else -> Text("Unsupported maze type")
                    }
                }
                mazeContent()

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Transparent)
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { /* Optional: vibrate or play tone */ },
                                onDragEnd = {
                                    val baseDimDp = computeCellSize(mazeCells, mazeType, cellSize, context)
                                    val baseDim = baseDimDp * density
                                    val dim = when (mazeType) {
                                        MazeType.SIGMA -> baseDim * sqrt(3f)
                                        MazeType.ORTHOGONAL -> baseDim
                                        MazeType.DELTA -> baseDim / sqrt(3f)
                                        MazeType.RHOMBIC -> baseDim
                                        else -> baseDim
                                    }
                                    val totalMoves = floor(cumulativePathLength / dim + 0.5f).toInt()
                                    val remainingMoves = totalMoves - performedMoves
                                    if (remainingMoves > 0 && lastMoveDirection != null) {
                                        repeat(remainingMoves) {
                                            performMove(lastMoveDirection!!)
                                        }
                                    }
                                    lastLocation = null
                                    cumulativePathLength = 0f
                                    directionVector = Offset.Zero
                                    lastMoveDirection = null
                                    performedMoves = 0
                                },
                                onDragCancel = {
                                    lastLocation = null
                                    cumulativePathLength = 0f
                                    directionVector = Offset.Zero
                                    lastMoveDirection = null
                                    performedMoves = 0
                                }
                            ) { change, dragAmount ->
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

                                val baseDimDp = computeCellSize(mazeCells, mazeType, cellSize, context)
                                val baseDim = baseDimDp * density
                                val dim = when (mazeType) {
                                    MazeType.SIGMA -> baseDim * sqrt(3f)
                                    MazeType.ORTHOGONAL -> baseDim
                                    MazeType.DELTA -> baseDim / sqrt(3f)
                                    MazeType.RHOMBIC -> baseDim
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
                )
            }

            if (showControls.value) {
                val configuration = LocalConfiguration.current
                val density = LocalDensity.current.density
                val screenWidth = configuration.screenWidthDp * density
                val screenHeight = configuration.screenHeightDp * density
                val maxX = screenWidth / 2 - (50 * density)
                val maxY = screenHeight / 2 - (50 * density)

                fun clamped(offset: Offset): Offset {
                    return Offset(
                        x = offset.x.coerceIn(-maxX, maxX),
                        y = offset.y.coerceIn(-maxY, maxY)
                    )
                }

                Column(modifier = Modifier.align(Alignment.BottomCenter)) {
                    Spacer(Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .offset(x = padOffset.value.x.dp, y = padOffset.value.y.dp)
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        padOffset.value = clamped(padOffset.value + Offset(dragAmount.x, dragAmount.y))
                                    },
                                    onDragEnd = {
                                        padOffset.value = clamped(padOffset.value)
                                    }
                                )
                            }
                    ) {
                        when (mazeType) {
                            MazeType.ORTHOGONAL -> FourWayControlScreen(performMove)
                            MazeType.SIGMA -> EightWayControlScreen(performMove)
                            MazeType.DELTA -> EightWayControlScreen(performMove)
                            MazeType.RHOMBIC -> FourWayDiagonalControlScreen(performMove)
                            MazeType.UPSILON -> EightWayControlScreen(performMove)
                            else -> Text("Controls not available for this maze type")
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(showControls.value) {
        if (showControls.value) {
            padOffset.value = Offset.Zero
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
//        toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK, 200)
    }
}

fun Modifier.noScroll(): Modifier = this.then(
    Modifier.pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent()
                event.changes.forEach { it.consume() }
            }
        }
    }
)

