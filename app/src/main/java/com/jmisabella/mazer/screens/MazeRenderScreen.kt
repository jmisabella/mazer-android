package com.jmisabella.mazer.screens

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.RenderEffect
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Vibrator
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
    val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)

    var lastLocation by remember { mutableStateOf<Offset?>(null) }
    var cumulativePathLength by remember { mutableStateOf(0f) }
    var directionVector by remember { mutableStateOf(Offset.Zero) }
    var lastMoveDirection by remember { mutableStateOf<String?>(null) }
    var performedMoves by remember { mutableStateOf(0) }
    var dragStartOffset by remember { mutableStateOf(Offset.Zero) }

    val performMove: (String) -> Unit = { dir ->
        showSolution.value = false
        if (moveAction(dir)) {
            toneGenerator.startTone(ToneGenerator.TONE_CDMA_CONFIRM, 200)
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
                .fillMaxSize()
                .padding(bottom = 20.dp)
        ) {
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

                            val baseDimDp = computeCellSize(mazeCells, mazeType, cellSize, context)
                            val baseDim = baseDimDp * density
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
                    .noScroll()
                    .graphicsLayer {
                        renderEffect = RenderEffect.createColorFilterEffect(
                            ColorMatrixColorFilter(colorMatrixSaturation(saturation.value))
                        ).asComposeRenderEffect()
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
                            optionalColor = optionalColor
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
                        MazeType.UPSILON -> UpsilonMazeScreen(
                            cells = mazeCells,
                            squareSize = cellSizes.square,
                            octagonSize = cellSizes.octagon,
                            showSolution = showSolution.value,
                            showHeatMap = showHeatMap.value,
                            selectedPalette = selectedPalette.value,
                            maxDistance = maxDistance,
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
                            MazeType.UPSILON -> EightWayControlScreen(performMove)
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

//package com.jmisabella.mazer.screens
//
//import android.annotation.SuppressLint
//import android.content.Context
//import android.graphics.ColorMatrixColorFilter
//import android.graphics.RenderEffect
//import android.media.AudioManager
//import android.media.ToneGenerator
//import android.os.Vibrator
//import androidx.compose.animation.core.animateFloatAsState
//import androidx.compose.animation.core.Animatable
//import androidx.compose.animation.core.EaseInQuad
//import androidx.compose.animation.core.EaseOutQuad
//import androidx.compose.animation.core.snap
//import androidx.compose.animation.core.tween
//import androidx.compose.foundation.background
//import androidx.compose.foundation.gestures.detectDragGestures
//import androidx.compose.foundation.layout.*
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.*
//import androidx.compose.material.icons.outlined.CheckCircle
//import androidx.compose.material.icons.outlined.HelpOutline
//import androidx.compose.material.icons.outlined.LocalFireDepartment
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.Text
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.asComposeRenderEffect
//import androidx.compose.ui.graphics.graphicsLayer
//import androidx.compose.ui.input.pointer.pointerInput
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.platform.LocalDensity
//import androidx.compose.ui.platform.LocalLayoutDirection
//import androidx.compose.ui.unit.dp
//import androidx.compose.foundation.layout.WindowInsets
//import com.jmisabella.mazer.layout.CellSizes
//import com.jmisabella.mazer.layout.computeCellSize
//import com.jmisabella.mazer.layout.navigationMenuHorizontalAdjustment
//import com.jmisabella.mazer.layout.navigationMenuVerticalAdjustment
//import com.jmisabella.mazer.models.*
//import com.jmisabella.mazer.screens.directioncontrols.EightWayControlScreen
//import com.jmisabella.mazer.screens.directioncontrols.FourWayControlScreen
//import com.jmisabella.mazer.screens.directioncontrols.FourWayDiagonalControlScreen
//import com.jmisabella.mazer.screens.mazecomponents.OrthogonalMazeScreen
//import com.jmisabella.mazer.screens.mazecomponents.DeltaMazeScreen
//import com.jmisabella.mazer.screens.mazecomponents.SigmaMazeScreen
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//import kotlin.math.*
//import kotlin.random.Random
//import android.graphics.ColorMatrix as AndroidColorMatrix
//
//private class Sparkle(
//    val x: Double,
//    val y: Double,
//    val size: Double,
//    val symbol: androidx.compose.ui.graphics.vector.ImageVector,
//    val color: Color
//) {
//    var phase by mutableStateOf(0)
//}
//
//@Composable
//private fun SparkleScreen(
//    count: Int = 60,
//    totalDuration: Float = 3f,
//    onFinished: () -> Unit = {}
//) {
//    val symbols = listOf(
//        Icons.Default.AutoAwesome,
//        Icons.Filled.Star,
//        Icons.Filled.Circle
//    )
//    val colors: List<Color> = listOf(Color.Yellow, Color.Magenta, Color(0xFF98FB98), Color(0xFFFFA500))
//
//    val sparkles = remember { mutableStateListOf<Sparkle>() }
//    val density = LocalDensity.current
//
//    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
//        val widthInDp = with(density) { constraints.maxWidth.toDp() }
//        val heightInDp = with(density) { constraints.maxHeight.toDp() }
//
//        sparkles.forEach { sparkle ->
//            val targetOpacity = when (sparkle.phase) {
//                1, 2 -> 1f
//                else -> 0f
//            }
//            val opacity = animateFloatAsState(
//                targetValue = targetOpacity,
//                animationSpec = if (sparkle.phase == 1) {
//                    tween<Float>((totalDuration * 0.12f * 1000).toInt(), easing = EaseOutQuad)
//                } else if (sparkle.phase == 3) {
//                    tween<Float>((totalDuration * 0.12f * 1000).toInt(), easing = EaseInQuad)
//                } else {
//                    snap()
//                },
//                label = "opacity"
//            )
//
//            val targetScale = if (sparkle.phase >= 1) 1f else 0.1f
//            val scale = animateFloatAsState(
//                targetValue = targetScale,
//                animationSpec = if (sparkle.phase == 1) {
//                    tween<Float>((totalDuration * 0.12f * 1000).toInt(), easing = EaseOutQuad)
//                } else {
//                    snap()
//                },
//                label = "scale"
//            )
//
//            Icon(
//                imageVector = sparkle.symbol,
//                contentDescription = null,
//                tint = sparkle.color,
//                modifier = Modifier
//                    .size(sparkle.size.dp)
//                    .graphicsLayer {
//                        alpha = opacity.value
//                        scaleX = scale.value
//                        scaleY = scale.value
//                    }
//                    .offset(
//                        x = (sparkle.x * widthInDp.value).dp,
//                        y = (sparkle.y * heightInDp.value).dp
//                    )
//            )
//        }
//    }
//
//    LaunchedEffect(Unit) {
//        val stagger = totalDuration * 0.5f / count
//        val newSparkles = (0 until count).map {
//            Sparkle(
//                x = Random.nextDouble(),
//                y = Random.nextDouble(),
//                size = Random.nextDouble(20.0, 50.0),
//                symbol = symbols.random(),
//                color = colors.random()
//            )
//        }
//        sparkles.addAll(newSparkles)
//
//        for ((i, sparkle) in newSparkles.withIndex()) {
//            delay((i * stagger * 1000f).toLong())
//            sparkle.phase = 1
//        }
//
//        delay(((totalDuration + 0.5f) * 1000f).toLong())
//        sparkles.clear()
//        onFinished()
//    }
//
//    sparkles.forEach { sparkle ->
//        LaunchedEffect(sparkle.phase) {
//            val fadeInDur = totalDuration * 0.12f
//            val fadeOutDur = totalDuration * 0.12f
//            val visibleDur = totalDuration * 0.5f
//            when (sparkle.phase) {
//                1 -> {
//                    delay((fadeInDur * 1000f).toLong())
//                    sparkle.phase = 2
//                }
//                2 -> {
//                    delay((visibleDur * 1000f).toLong())
//                    sparkle.phase = 3
//                }
//                3 -> {
//                    delay((fadeOutDur * 1000f).toLong())
//                    sparkle.phase = 4
//                }
//            }
//        }
//    }
//}
//
//fun colorMatrixSaturation(saturation: Float): AndroidColorMatrix {
//    return AndroidColorMatrix().apply { setSaturation(saturation) }
//}
//
//@SuppressLint("UnusedBoxWithConstraintsScope")
//@Composable
//fun MazeRenderScreen(
//    mazeGenerated: MutableState<Boolean>,
//    showSolution: MutableState<Boolean>,
//    showHeatMap: MutableState<Boolean>,
//    showControls: MutableState<Boolean>,
//    padOffset: MutableState<Offset>,
//    selectedPalette: MutableState<HeatMapPalette>,
//    mazeID: String,
//    defaultBackground: MutableState<Color>,
//    showHelp: MutableState<Boolean>,
//    mazeCells: List<MazeCell>,
//    mazeType: MazeType,
//    cellSize: CellSize,
//    optionalColor: Color?,
//    regenerateMaze: () -> Unit,
//    moveAction: (String) -> Boolean,  // Updated to return Boolean indicating if maze is completed after move
//    cellSizes: CellSizes,
//    toggleHeatMap: () -> Unit,
//    cleanupMazeData: () -> Unit
//) {
//    val context = LocalContext.current
//    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
//    val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
//    val coroutineScope = rememberCoroutineScope()
//
//    var lastLocation by remember { mutableStateOf<Offset?>(null) }
//    var cumulativePathLength by remember { mutableStateOf(0f) }
//    var directionVector by remember { mutableStateOf(Offset.Zero) }
//    var lastMoveDirection by remember { mutableStateOf<String?>(null) }
//    var performedMoves by remember { mutableStateOf(0) }
//    var dragStartOffset by remember { mutableStateOf(Offset.Zero) }
//    var showSparkles by remember { mutableStateOf(false) }
//    val saturationAnim = remember { Animatable(1f) }
//
//    val performMove: (String) -> Unit = { dir ->
//        showSolution.value = false
//        if (moveAction(dir)) {
//            // Trigger celebration on completion
//            toneGenerator.startTone(ToneGenerator.TONE_SUP_CONFIRM, 500)
//            showSparkles = true
//            coroutineScope.launch {
//                saturationAnim.animateTo(0f, tween(200, easing = EaseInQuad))
//                saturationAnim.animateTo(1f, tween(200, easing = EaseOutQuad))
//            }
//        }
//    }
//
//    // Get status bar insets including display cutout
//    val density = LocalDensity.current
//    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()
//
//    Column(modifier = Modifier.fillMaxSize()) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(
//                    top = statusBarPadding.calculateTopPadding() + navigationMenuVerticalAdjustment(mazeType, cellSize, context).dp,
//                    start = statusBarPadding.calculateStartPadding(LocalLayoutDirection.current),
//                    end = statusBarPadding.calculateEndPadding(LocalLayoutDirection.current)
//                ),
//            horizontalArrangement = Arrangement.Center,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            IconButton(onClick = cleanupMazeData) {
//                Icon(
//                    Icons.Default.Menu,
//                    contentDescription = "Back to maze settings",
//                    tint = Color.Blue,
//                    modifier = Modifier.size(32.dp)
//                )
//            }
//            IconButton(onClick = {
//                showSolution.value = false
//                regenerateMaze()
//            }) {
//                Icon(
//                    Icons.Default.Refresh,
//                    contentDescription = "Generate new maze",
//                    tint = Color(0xFF800080),
//                    modifier = Modifier.size(32.dp)
//                )
//            }
//            IconButton(onClick = { showSolution.value = !showSolution.value }) {
//                Icon(
//                    if (showSolution.value) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
//                    contentDescription = "Toggle solution path",
//                    tint = if (showSolution.value) Color.Green else Color.Gray,
//                    modifier = Modifier.size(32.dp)
//                )
//            }
//            IconButton(onClick = toggleHeatMap) {
//                Icon(
//                    if (showHeatMap.value) Icons.Filled.LocalFireDepartment else Icons.Outlined.LocalFireDepartment,
//                    contentDescription = "Toggle heat map",
//                    tint = if (showHeatMap.value) Color(0xFFFFA500) else Color.Gray,
//                    modifier = Modifier.size(32.dp)
//                )
//            }
//            IconButton(onClick = { showControls.value = !showControls.value }) {
//                Icon(
//                    if (showControls.value) Icons.Filled.Close else Icons.Filled.MoreHoriz,
//                    contentDescription = "Toggle navigation controls",
//                    tint = Color.Gray,
//                    modifier = Modifier.size(32.dp)
//                )
//            }
//            IconButton(onClick = { showHelp.value = true }) {
//                Icon(
//                    Icons.Outlined.HelpOutline,
//                    contentDescription = "Help instructions",
//                    tint = Color.Gray,
//                    modifier = Modifier.size(32.dp)
//                )
//            }
//        }
//
//        val density = LocalDensity.current.density
//        BoxWithConstraints(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(bottom = 20.dp) // Added bottom padding to prevent cutoff at curved corners
//        ) {
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .pointerInput(Unit) {
//                        detectDragGestures { change, dragAmount ->
//                            if (lastLocation == null) {
//                                lastLocation = change.position
//                                return@detectDragGestures
//                            }
//
//                            val currentLocation = change.position
//                            val deltaX = currentLocation.x - lastLocation!!.x
//                            val deltaY = currentLocation.y - lastLocation!!.y
//                            lastLocation = currentLocation
//
//                            val delta_tx = deltaX
//                            val delta_ty = -deltaY
//                            val delta_mag = sqrt(delta_tx * delta_tx + delta_ty * delta_ty)
//                            if (delta_mag == 0f) return@detectDragGestures
//
//                            cumulativePathLength += delta_mag
//
//                            val alpha = 0.3f
//                            directionVector = if (directionVector == Offset.Zero) {
//                                Offset(delta_tx, delta_ty)
//                            } else {
//                                Offset(
//                                    directionVector.x * alpha + delta_tx * (1 - alpha),
//                                    directionVector.y * alpha + delta_ty * (1 - alpha)
//                                )
//                            }
//
//                            if (directionVector == Offset.Zero) return@detectDragGestures
//
//                            val angle = atan2(directionVector.y.toDouble(), directionVector.x.toDouble()).toFloat()
//                            var shifted = angle + (Math.PI.toFloat() / 8)
//                            if (shifted < 0) shifted += (2 * Math.PI).toFloat()
//                            val sector = floor(shifted / (Math.PI.toFloat() / 4)).toInt() % 8
//                            val directions = listOf(
//                                "Right", "UpperRight", "Up", "UpperLeft",
//                                "Left", "LowerLeft", "Down", "LowerRight"
//                            )
//                            val currentDirection = directions[sector]
//                            lastMoveDirection = currentDirection
//
//                            val baseDimDp = computeCellSize(mazeCells, mazeType, cellSize, context)
//                            val baseDim = baseDimDp * density // convert dp to pixels
//                            val dim = when (mazeType) {
//                                MazeType.SIGMA -> baseDim * sqrt(3f)
//                                MazeType.ORTHOGONAL -> baseDim
//                                MazeType.DELTA -> baseDim / sqrt(3f)
//                                else -> baseDim
//                            }
//
//                            val totalMovesNeeded = floor(cumulativePathLength / dim).toInt()
//                            val movesToPerform = totalMovesNeeded - performedMoves
//
//                            if (movesToPerform > 0) {
//                                repeat(movesToPerform) {
//                                    performMove(currentDirection)
//                                }
//                                performedMoves += movesToPerform
//                            }
//                        }
//                    }
//                    .noScroll()
//                    .graphicsLayer {
//                        renderEffect = RenderEffect.createColorFilterEffect(
//                            ColorMatrixColorFilter(colorMatrixSaturation(saturationAnim.value))
//                        ).asComposeRenderEffect()
//                    }
//            ) {
//                val mazeContent = @Composable {
//                    val maxDistance = mazeCells.maxOfOrNull { it.distance } ?: 1
//                    val cellSizeValue = computeCellSize(mazeCells, mazeType, cellSize, context)
//                    when (mazeType) {
//                        MazeType.ORTHOGONAL -> OrthogonalMazeScreen(
//                            selectedPalette = selectedPalette,
//                            cells = mazeCells,
//                            showSolution = showSolution.value,
//                            showHeatMap = showHeatMap.value,
//                            defaultBackgroundColor = defaultBackground.value,
//                            optionalColor = optionalColor
//                        )
//                        MazeType.DELTA -> DeltaMazeScreen(
//                            cells = mazeCells,
//                            cellSize = cellSizeValue,
//                            showSolution = showSolution.value,
//                            showHeatMap = showHeatMap.value,
//                            selectedPalette = selectedPalette.value,
//                            maxDistance = maxDistance,
//                            defaultBackgroundColor = defaultBackground.value,
//                            optionalColor = optionalColor
//                        )
//                        MazeType.SIGMA -> SigmaMazeScreen(
//                            cells = mazeCells,
//                            cellSize = cellSizeValue,
//                            showSolution = showSolution.value,
//                            showHeatMap = showHeatMap.value,
//                            selectedPalette = selectedPalette.value,
//                            defaultBackgroundColor = defaultBackground.value,
//                            optionalColor = optionalColor
//                        )
//                        else -> Text("Unsupported maze type")
//                    }
//                }
//                mazeContent()
//            }
//
//            if (showControls.value) {
//                Column(modifier = Modifier.align(Alignment.BottomCenter)) {
//                    Box(
//                        modifier = Modifier
//                            .background(Color.White.copy(alpha = 0.8f))
//                            .padding(16.dp)
//                            .offset(x = padOffset.value.x.dp, y = padOffset.value.y.dp)
//                            .pointerInput(Unit) {
//                                detectDragGestures { change, dragAmount ->
//                                    padOffset.value = Offset(
//                                        padOffset.value.x + dragAmount.x,
//                                        padOffset.value.y + dragAmount.y
//                                    )
//                                }
//                            }
//                    ) {
//                        when (mazeType) {
//                            MazeType.ORTHOGONAL -> FourWayControlScreen(performMove)
//                            MazeType.SIGMA -> FourWayDiagonalControlScreen(performMove)
//                            MazeType.DELTA -> EightWayControlScreen(performMove)
//                            else -> Text("Controls not available for this maze type")
//                        }
//                    }
//                }
//            }
//
//            if (showSparkles) {
//                SparkleScreen(onFinished = { showSparkles = false })
//            }
//        }
//    }
//
//    LaunchedEffect(Unit) {
//        val sharedPrefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
//        val key = "hasSeenHelpInstructions"
//        val hasSeen = sharedPrefs.getBoolean(key, false)
//        if (!hasSeen) {
//            showHelp.value = true
//            sharedPrefs.edit().putBoolean(key, true).apply()
//        }
//        toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 200)
//    }
//}
//
//fun Modifier.noScroll(): Modifier = this.then(
//    Modifier.pointerInput(Unit) {
//        awaitPointerEventScope {
//            while (true) {
//                val event = awaitPointerEvent()
//                event.changes.forEach { it.consume() }
//            }
//        }
//    }
//)