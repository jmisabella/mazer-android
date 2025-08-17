package com.jmisabella.mazer

import android.os.Build
import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.jmisabella.mazer.layout.CellColors
import com.jmisabella.mazer.models.*
import com.jmisabella.mazer.screens.MazeRenderScreen
import com.jmisabella.mazer.screens.MazeRequestScreen
import com.jmisabella.mazer.ui.theme.MazerTheme
import com.jmisabella.mazer.layout.computeCellSizes
import com.jmisabella.mazer.screens.HelpModal
import com.jmisabella.mazer.screens.MazeGenerationAnimationScreen
import com.jmisabella.mazer.screens.effects.LoadingOverlay
import com.jmisabella.mazer.screens.effects.SparkleScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID
import kotlin.math.floor
import kotlin.math.sqrt

@Serializable
data class MazeRequest(
    @SerialName("maze_type") val mazeType: String,
    @SerialName("width") val width: Int,
    @SerialName("height") val height: Int,
    @SerialName("algorithm") val algorithm: String,
    @SerialName("capture_steps") val captureSteps: Boolean
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MazerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ContentScreen()
                }
            }
        }
    }
}

@Composable
fun ContentScreen() {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    val toneGenerator = remember { ToneGenerator(AudioManager.STREAM_MUSIC, 30) }
    DisposableEffect(toneGenerator) {
        onDispose {
            toneGenerator.release()
        }
    }

    // State declarations
    val ffiIntegrationTestResultState = remember { mutableStateOf(0) }
    val mazeCellsState = remember { mutableStateOf<List<MazeCell>>(emptyList()) }
    val mazeTypeState = remember { mutableStateOf(MazeType.ORTHOGONAL) }
    val mazeGeneratedState = remember { mutableStateOf(false) }
    val errorMessageState = remember { mutableStateOf<String?>(null) }
    val selectedSizeState = remember { mutableStateOf(CellSize.LARGE) }
    val selectedMazeTypeState = remember { mutableStateOf(MazeType.ORTHOGONAL) }
    val selectedAlgorithmState = remember { mutableStateOf(MazeAlgorithm.RECURSIVE_BACKTRACKER) }
    val showSolutionState = remember { mutableStateOf(false) }
    val showHeatMapState = remember { mutableStateOf(false) }
    val showControlsState = remember { mutableStateOf(false) }
    val padOffsetState = remember { mutableStateOf(Offset.Zero) }
    val showCelebrationState = remember { mutableStateOf(false) }
    val selectedPaletteState = remember { mutableStateOf(allPalettes.randomOrNull() ?: turquoisePalette) }
    val mazeIDState = remember { mutableStateOf(UUID.randomUUID().toString()) }
    val currentGridState = remember { mutableStateOf<Long?>(null) }
    val defaultBackgroundColorState = remember { mutableStateOf<Color>(CellColors.defaultBackgroundColors.randomOrNull() ?: Color.White) }
    val didInitialRandomizationState = remember { mutableStateOf(false) }
    val hasPlayedSoundThisSessionState = remember { mutableStateOf(false) }
    val captureStepsState = remember { mutableStateOf(false) }
    val isGeneratingMazeState = remember { mutableStateOf(false) }
    val isAnimatingGenerationState = remember { mutableStateOf(false) }
    val generationStepsState = remember { mutableStateOf<List<List<MazeCell>>>(emptyList()) }
    val isLoadingState = remember { mutableStateOf(false) }
    val optionalColorState = remember { mutableStateOf<Color?>(null) }
    val showHelpState = remember { mutableStateOf(false) }

    // Load saved preferences
    LaunchedEffect(Unit) {
        val sharedPrefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        selectedSizeState.value = CellSize.values().find { it.value == sharedPrefs.getInt("lastSize", CellSize.MEDIUM.value) } ?: CellSize.MEDIUM
        selectedMazeTypeState.value = MazeType.values().find { it.ffiName == sharedPrefs.getString("lastMazeType", MazeType.ORTHOGONAL.ffiName) } ?: MazeType.ORTHOGONAL
        selectedAlgorithmState.value = MazeAlgorithm.values().find { it.name == sharedPrefs.getString("lastAlgorithm", MazeAlgorithm.RECURSIVE_BACKTRACKER.name) } ?: MazeAlgorithm.RECURSIVE_BACKTRACKER
        showHeatMapState.value = sharedPrefs.getBoolean("showHeatMap", false)

        sharedPrefs.edit().apply {
            putInt("lastSize", selectedSizeState.value.value)
            putString("lastMazeType", selectedMazeTypeState.value.ffiName)
            putString("lastAlgorithm", selectedAlgorithmState.value.name)
            putBoolean("showHeatMap", showHeatMapState.value)
            apply()
        }

        ffiIntegrationTestResultState.value = MazerNative.mazerFfiIntegrationTest()
        println("mazer_ffi_integration_test returned: ${ffiIntegrationTestResultState.value}")
        if (ffiIntegrationTestResultState.value == 42) {
            println("FFI integration test passed ✅")
        } else {
            println("FFI integration test failed ❌")
        }
    }

    // Save preferences on change
    LaunchedEffect(selectedSizeState.value) {
        context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
            .edit()
            .putInt("lastSize", selectedSizeState.value.value)
            .apply()
    }
    LaunchedEffect(selectedMazeTypeState.value) {
        context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("lastMazeType", selectedMazeTypeState.value.ffiName)
            .apply()
    }
    LaunchedEffect(selectedAlgorithmState.value) {
        context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("lastAlgorithm", selectedAlgorithmState.value.name)
            .apply()
    }
    LaunchedEffect(showHeatMapState.value) {
        context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("showHeatMap", showHeatMapState.value)
            .apply()
    }

    fun randomPaletteExcluding(current: HeatMapPalette, allPalettes: List<HeatMapPalette>): HeatMapPalette {
        val availablePalettes = allPalettes.filter { it != current }
        return availablePalettes.randomOrNull() ?: current
    }

    fun randomDefaultExcluding(current: Color, all: List<Color>): Color {
        val others = all.filter { it != current }
        return others.randomOrNull() ?: current
    }

    fun cleanupMazeData() {
        currentGridState.value?.let { gridPtr ->
            MazerNative.destroyMaze(gridPtr)
            currentGridState.value = null
        }
        mazeCellsState.value = emptyList()
        generationStepsState.value = emptyList()
        mazeGeneratedState.value = false
        isAnimatingGenerationState.value = false
    }

    fun submitMazeRequest() {
        coroutineScope.launch {
            println("FFI integration test result: ${MazerNative.mazerFfiIntegrationTest()}")
            isLoadingState.value = true
            withContext(Dispatchers.IO) {
                currentGridState.value?.let { gridPtr ->
                    MazerNative.destroyMaze(gridPtr)
                    currentGridState.value = null
                }

                val (squareCellSize, octagonCellSize) = computeCellSizes(selectedMazeTypeState.value, selectedSizeState.value, context)
                val metrics = context.resources.displayMetrics
                val density = metrics.density
                val screenH = metrics.heightPixels / density
                val screenW = metrics.widthPixels / density
                val isSmallDevice = screenH <= 667

                val perSidePad: Float = when (selectedMazeTypeState.value) {
                    MazeType.ORTHOGONAL -> 20f
                    MazeType.UPSILON -> 12f
                    MazeType.DELTA -> 20f
                    MazeType.RHOMBIC -> 20f
                    MazeType.SIGMA -> if (Build.VERSION.SDK_INT <= 34) {
                        if (isSmallDevice) 30f else 60f // Reduced padding for Android 14 to add rows
                    } else {
                        if (isSmallDevice) 50f else 100f
                    }
                }
                val totalVerticalPadding = perSidePad * 2
                val controlArea = if (Build.VERSION.SDK_INT <= 34) 96f else 80f // Add gesture padding for Android 14
                val availableH = screenH - controlArea - totalVerticalPadding
                val drawableH = availableH

                val cellSize = if (selectedMazeTypeState.value == MazeType.UPSILON) octagonCellSize else squareCellSize
                val spacing = if (selectedMazeTypeState.value == MazeType.UPSILON) (sqrt(2f) * 0.5f) * octagonCellSize else cellSize
                val rowHeight = if (selectedMazeTypeState.value == MazeType.UPSILON) octagonCellSize * (sqrt(2f) * 0.5f) else cellSize

                val maxHeightRows = maxOf(1, floor(availableH / rowHeight).toInt())
                val maxWidth = maxOf(1, floor(screenW / spacing).toInt())

                var finalWidth: Int
                var finalHeight: Int

                if (selectedMazeTypeState.value == MazeType.RHOMBIC) {
                    val diag = cellSize * sqrt(2f)
                    finalWidth = maxOf(1, floor(2.0 * screenW / diag - 1.0).toInt())
                    finalHeight = maxOf(1, floor(2.0 * drawableH / diag - 1.0).toInt())
                } else if (selectedMazeTypeState.value == MazeType.DELTA) {
                    finalWidth = maxOf(1, floor(2.0 * screenW / cellSize - 1.0).toInt())
                    finalHeight = maxOf(1, floor(2.0 * drawableH / (cellSize * sqrt(3f)) - 0.0001).toInt()) // Minor epsilon to avoid floating-point overflow
                } else {
                    finalWidth = if (selectedMazeTypeState.value == MazeType.SIGMA) maxWidth / 3 else maxWidth
                    finalHeight = if (selectedMazeTypeState.value == MazeType.SIGMA) maxHeightRows / 3 else maxHeightRows
                }

                if (captureStepsState.value && (finalWidth > 100 || finalHeight > 100)) {
                    withContext(Dispatchers.Main) {
                        errorMessageState.value = "Show Maze Generation is only available for mazes with width and height ≤ 100."
                        isLoadingState.value = false
                    }
                    return@withContext
                }

                val mazeRequest = MazeRequest(
                    mazeType = selectedMazeTypeState.value.ffiName,
                    width = finalWidth,
                    height = finalHeight,
                    algorithm = selectedAlgorithmState.value.ffiName,
                    captureSteps = captureStepsState.value
                )

                val jsonString = Json.encodeToString(mazeRequest)
                println("Valid JSON: $jsonString")

                val gridPtr = MazerNative.generateMaze(jsonString)
                if (gridPtr == 0L) {
                    println("Failed to generate maze")
                    withContext(Dispatchers.Main) {
                        errorMessageState.value = "Failed to generate maze."
                        isLoadingState.value = false
                    }
                    return@withContext
                }

                currentGridState.value = gridPtr

                val cellsPtr = MazerNative.getCells(gridPtr)
                if (cellsPtr == null) {
                    println("Error occurred getting cells from grid pointer!")
                    withContext(Dispatchers.Main) {
                        errorMessageState.value = "Failed to retrieve cells."
                        isLoadingState.value = false
                    }
                    return@withContext
                }

                println("mapping cells to FFI cells...")
                val cells = cellsPtr.map { ffiCell ->
                    MazeCell(
                        x = ffiCell.x.toInt(),
                        y = ffiCell.y.toInt(),
                        mazeType = ffiCell.maze_type ?: "",
                        linked = ffiCell.linked?.toList() ?: emptyList(),
                        distance = ffiCell.distance,
                        isStart = ffiCell.is_start,
                        isGoal = ffiCell.is_goal,
                        isActive = ffiCell.is_active,
                        isVisited = ffiCell.is_visited,
                        hasBeenVisited = ffiCell.has_been_visited,
                        onSolutionPath = ffiCell.on_solution_path,
                        orientation = ffiCell.orientation ?: "",
                        isSquare = ffiCell.is_square
                    )
                }

                val cellCount = cells.count()
                println("Cells count: $cellCount")

                var steps: List<List<MazeCell>> = emptyList()
                if (captureStepsState.value) {
                    val stepsCount = MazerNative.getGenerationStepsCount(gridPtr)
                    steps = (0 until stepsCount).mapNotNull { stepIndex ->
                        val stepCellsPtr = MazerNative.getGenerationStepCells(gridPtr, stepIndex)
                        if (stepCellsPtr == null) {
                            withContext(Dispatchers.Main) {
                                errorMessageState.value = "Failed to retrieve generation step cells."
                                isLoadingState.value = false
                            }
                            null
                        } else {
                            val stepCells = stepCellsPtr.map { ffiCell ->
                                MazeCell(
                                    x = ffiCell.x.toInt(),
                                    y = ffiCell.y.toInt(),
                                    mazeType = ffiCell.maze_type ?: "",
                                    linked = ffiCell.linked?.toList() ?: emptyList(),
                                    distance = ffiCell.distance,
                                    isStart = ffiCell.is_start,
                                    isGoal = ffiCell.is_goal,
                                    isActive = ffiCell.is_active,
                                    isVisited = ffiCell.is_visited,
                                    hasBeenVisited = ffiCell.has_been_visited,
                                    onSolutionPath = ffiCell.on_solution_path,
                                    orientation = ffiCell.orientation ?: "",
                                    isSquare = ffiCell.is_square
                                )
                            }
                            stepCells
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    mazeCellsState.value = cells
                    mazeTypeState.value = MazeType.fromFFIName(cells.firstOrNull()?.mazeType) ?: MazeType.ORTHOGONAL
                    if (captureStepsState.value) {
                        generationStepsState.value = steps
                        isAnimatingGenerationState.value = true
                    } else {
                        mazeGeneratedState.value = true
                    }
                    optionalColorState.value = if ((0..1).random() == 1) {
                        listOf(Color.Magenta, Color.Gray, Color.Yellow, Color.Blue, Color(0xFFA200FF), Color(0xFFFF9500)).randomOrNull()
                    } else null
                    isLoadingState.value = false
                    errorMessageState.value = null
                    selectedPaletteState.value = randomPaletteExcluding(selectedPaletteState.value, allPalettes)
                    defaultBackgroundColorState.value = randomDefaultExcluding(defaultBackgroundColorState.value, CellColors.defaultBackgroundColors)
                }
            }
        }
    }

//    fun submitMazeRequest() {
//        coroutineScope.launch {
//            println("FFI integration test result: ${MazerNative.mazerFfiIntegrationTest()}")
//            isLoadingState.value = true
//            withContext(Dispatchers.IO) {
//                currentGridState.value?.let { gridPtr ->
//                    MazerNative.destroyMaze(gridPtr)
//                    currentGridState.value = null
//                }
//
//                val (squareCellSize, octagonCellSize) = computeCellSizes(selectedMazeTypeState.value, selectedSizeState.value, context)
//                val metrics = context.resources.displayMetrics
//                val screenH = metrics.heightPixels / metrics.density
//                val screenW = metrics.widthPixels / metrics.density
//                val isSmallDevice = screenH <= 667
//
//                val perSidePad: Float = when (selectedMazeTypeState.value) { MazeType.ORTHOGONAL -> 20f
//                    MazeType.UPSILON -> 12f
//                    MazeType.DELTA -> 20f
//                    MazeType.RHOMBIC -> 20f
//                    MazeType.SIGMA -> if (isSmallDevice) 50f else 100f
//                }
//                val totalVerticalPadding = perSidePad * 2
//                val controlArea = 80f
//                val availableH = screenH - controlArea - totalVerticalPadding
//                val drawableH = availableH
//
//                val cellSize = if (selectedMazeTypeState.value == MazeType.UPSILON) octagonCellSize else squareCellSize
//                val spacing = if (selectedMazeTypeState.value == MazeType.UPSILON) (sqrt(2f) * 0.5f) * octagonCellSize else cellSize
//                val rowHeight = if (selectedMazeTypeState.value == MazeType.UPSILON) octagonCellSize * (sqrt(2f) * 0.5f) else cellSize
//
//                val maxHeightRows = maxOf(1, floor(availableH / rowHeight).toInt())
//                val maxWidth = maxOf(1, floor(screenW / spacing).toInt())
//
//                var finalWidth: Int
//                var finalHeight: Int
//
////                if (selectedMazeTypeState.value == MazeType.RHOMBIC) {
////                    val s = squareCellSize
////                    val diag = s * sqrt(2f)
////                    val pitch = diag * 0.5f
////                    finalWidth = maxOf(1, floor(screenW / diag).toInt())
////                    finalHeight = maxOf(1, floor(drawableH / pitch).toInt())
////                } else if (selectedMazeTypeState.value == MazeType.DELTA) {
////                    finalWidth = maxOf(1, floor(screenW / (cellSize * 0.75f)).toInt())
////                    finalHeight = maxOf(1, floor(drawableH / (cellSize * sqrt(3f) / 2f)).toInt())
//                if (selectedMazeTypeState.value == MazeType.RHOMBIC) {
//                    val diag = cellSize * sqrt(2f)
//                    finalWidth = maxOf(1, floor(2.0 * screenW / diag - 1.0).toInt())
//                    finalHeight = maxOf(1, floor(2.0 * drawableH / diag - 1.0).toInt())
//                } else if (selectedMazeTypeState.value == MazeType.DELTA) {
////                    finalWidth = maxOf(1, floor(screenW / (cellSize * 0.75f)).toInt())
////                    finalHeight = maxOf(1, floor(drawableH / (cellSize * sqrt(3f) / 2f)).toInt())
//                    finalWidth = maxOf(1, floor(2.0 * screenW / cellSize - 1.0).toInt())
//                    finalHeight = maxOf(1, floor(2.0 * drawableH / (cellSize * sqrt(3f)) - 0.0001).toInt()) // Minor epsilon to avoid floating-point overflow
//                } else {
//                    finalWidth = if (selectedMazeTypeState.value == MazeType.SIGMA) maxWidth / 3 else maxWidth
//                    finalHeight = if (selectedMazeTypeState.value == MazeType.SIGMA) maxHeightRows / 3 else maxHeightRows
//                }
//
//                if (captureStepsState.value && (finalWidth > 100 || finalHeight > 100)) {
//                    withContext(Dispatchers.Main) {
//                        errorMessageState.value = "Show Maze Generation is only available for mazes with width and height ≤ 100."
//                        isLoadingState.value = false
//                    }
//                    return@withContext
//                }
//
//                val mazeRequest = MazeRequest(
//                    mazeType = selectedMazeTypeState.value.ffiName,
//                    width = finalWidth,
//                    height = finalHeight,
//                    algorithm = selectedAlgorithmState.value.ffiName,
//                    captureSteps = captureStepsState.value
//                )
//
//                val jsonString = Json.encodeToString(mazeRequest)
//                println("Valid JSON: $jsonString")
//
//                val gridPtr = MazerNative.generateMaze(jsonString)
//                if (gridPtr == 0L) {
//                    println("Failed to generate maze")
//                    withContext(Dispatchers.Main) {
//                        errorMessageState.value = "Failed to generate maze."
//                        isLoadingState.value = false
//                    }
//                    return@withContext
//                }
//
//                currentGridState.value = gridPtr
//
//                val cellsPtr = MazerNative.getCells(gridPtr)
//                if (cellsPtr == null) {
//                    println("Error occurred getting cells from grid pointer!")
//                    withContext(Dispatchers.Main) {
//                        errorMessageState.value = "Failed to retrieve cells."
//                        isLoadingState.value = false
//                    }
//                    return@withContext
//                }
//
//                println("mapping cells to FFI cells...")
//                val cells = cellsPtr.map { ffiCell ->
//                    MazeCell(
//                        x = ffiCell.x.toInt(),
//                        y = ffiCell.y.toInt(),
//                        mazeType = ffiCell.maze_type ?: "",
//                        linked = ffiCell.linked?.toList() ?: emptyList(),
//                        distance = ffiCell.distance,
//                        isStart = ffiCell.is_start,
//                        isGoal = ffiCell.is_goal,
//                        isActive = ffiCell.is_active,
//                        isVisited = ffiCell.is_visited,
//                        hasBeenVisited = ffiCell.has_been_visited,
//                        onSolutionPath = ffiCell.on_solution_path,
//                        orientation = ffiCell.orientation ?: "",
//                        isSquare = ffiCell.is_square
//                    )
//                }
//
//                val cellCount = cells.count()
//                println("Cells count: $cellCount")
//
//                var steps: List<List<MazeCell>> = emptyList()
//                if (captureStepsState.value) {
//                    val stepsCount = MazerNative.getGenerationStepsCount(gridPtr)
//                    steps = (0 until stepsCount).mapNotNull { stepIndex ->
//                        val stepCellsPtr = MazerNative.getGenerationStepCells(gridPtr, stepIndex)
//                        if (stepCellsPtr == null) {
//                            withContext(Dispatchers.Main) {
//                                errorMessageState.value = "Failed to retrieve generation step cells."
//                                isLoadingState.value = false
//                            }
//                            null
//                        } else {
//                            val stepCells = stepCellsPtr.map { ffiCell ->
//                                MazeCell(
//                                    x = ffiCell.x.toInt(),
//                                    y = ffiCell.y.toInt(),
//                                    mazeType = ffiCell.maze_type ?: "",
//                                    linked = ffiCell.linked?.toList() ?: emptyList(),
//                                    distance = ffiCell.distance,
//                                    isStart = ffiCell.is_start,
//                                    isGoal = ffiCell.is_goal,
//                                    isActive = ffiCell.is_active,
//                                    isVisited = ffiCell.is_visited,
//                                    hasBeenVisited = ffiCell.has_been_visited,
//                                    onSolutionPath = ffiCell.on_solution_path,
//                                    orientation = ffiCell.orientation ?: "",
//                                    isSquare = ffiCell.is_square
//                                )
//                            }
//                            stepCells
//                        }
//                    }
//                }
//
//                withContext(Dispatchers.Main) {
//                    mazeCellsState.value = cells
//                    mazeTypeState.value = MazeType.fromFFIName(cells.firstOrNull()?.mazeType) ?: MazeType.ORTHOGONAL
//                    if (captureStepsState.value) {
//                        generationStepsState.value = steps
//                        isAnimatingGenerationState.value = true
//                    } else {
//                        mazeGeneratedState.value = true
//                    }
//                    optionalColorState.value = if ((0..1).random() == 1) {
//                        listOf(Color.Magenta, Color.Gray, Color.Yellow, Color.Blue, Color(0xFFA200FF), Color(0xFFFF9500)).randomOrNull()
//                    } else null
//                    isLoadingState.value = false
//                    errorMessageState.value = null
//                    selectedPaletteState.value = randomPaletteExcluding(selectedPaletteState.value, allPalettes)
//                    defaultBackgroundColorState.value = randomDefaultExcluding(defaultBackgroundColorState.value, CellColors.defaultBackgroundColors)
//                }
//            }
//        }
//    }

    fun celebrateVictory() {
        showCelebrationState.value = true
        captureStepsState.value = false

//        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        if (vibrator.hasAmplitudeControl()) {
            vibrator.vibrate(VibrationEffect.createOneShot(100, 120))
        } else {
            // Fallback for devices without amplitude control
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        }

        toneGenerator.startTone(ToneGenerator.TONE_CDMA_CONFIRM, 200)
        coroutineScope.launch {
            kotlinx.coroutines.delay(2000)
            withContext(Dispatchers.Main) {
                showCelebrationState.value = false
                if (mazeGeneratedState.value) {
                    showSolutionState.value = false
                    mazeIDState.value = UUID.randomUUID().toString()
                    submitMazeRequest()
                }
            }
        }
    }

    fun performMove(direction: String): Boolean {
        if (showCelebrationState.value) return false
        val gridPtr = currentGridState.value ?: return false
        val tryDirections = when (mazeTypeState.value) {
            MazeType.ORTHOGONAL -> listOf(direction)
            MazeType.DELTA -> when (direction) {
                "UpperRight" -> listOf("UpperRight", "Right")
                "LowerRight" -> listOf("LowerRight", "Right")
                "UpperLeft" -> listOf("UpperLeft", "Left")
                "LowerLeft" -> listOf("LowerLeft", "Left")
                else -> listOf(direction)
            }
            MazeType.SIGMA -> when (direction) {
                "UpperRight" -> listOf("UpperRight", "LowerRight")
                "LowerRight" -> listOf("LowerRight", "UpperRight")
                "UpperLeft" -> listOf("UpperLeft", "LowerLeft")
                "LowerLeft" -> listOf("LowerLeft", "UpperLeft")
                else -> listOf(direction)
            }
            MazeType.UPSILON -> listOf(direction)
            MazeType.RHOMBIC -> listOf(direction)
        }

        var newGridPtr: Long? = null
        for (dir in tryDirections) {
            newGridPtr = MazerNative.makeMove(gridPtr, dir)
            if (newGridPtr != 0L) break
        }

        if (newGridPtr == null || newGridPtr == 0L) return false

        if (vibrator.hasAmplitudeControl()) {
            vibrator.vibrate(VibrationEffect.createOneShot(20, 132))
        } else {
            // Fallback for devices without amplitude control
            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
        }

        currentGridState.value = newGridPtr
        val cellsPtr = MazerNative.getCells(newGridPtr)
        if (cellsPtr == null) {
            errorMessageState.value = "Failed to retrieve updated maze."
            return false
        }

        val cells = cellsPtr.map { ffiCell ->
            MazeCell(
                x = ffiCell.x.toInt(),
                y = ffiCell.y.toInt(),
                mazeType = ffiCell.maze_type ?: "",
                linked = ffiCell.linked?.toList() ?: emptyList(),
                distance = ffiCell.distance,
                isStart = ffiCell.is_start,
                isGoal = ffiCell.is_goal,
                isActive = ffiCell.is_active,
                isVisited = ffiCell.is_visited,
                hasBeenVisited = ffiCell.has_been_visited,
                onSolutionPath = ffiCell.on_solution_path,
                orientation = ffiCell.orientation ?: "",
                isSquare = ffiCell.is_square
            )
        }

        mazeCellsState.value = cells
        val isGoalReached = cells.any { it.isGoal && it.isActive }
        if (isGoalReached && !showCelebrationState.value) {
            celebrateVictory()
        }
        return isGoalReached
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isGeneratingMazeState.value -> {
                CircularProgressIndicator(modifier = Modifier.fillMaxSize())
            }
            isAnimatingGenerationState.value -> {
                MazeGenerationAnimationScreen(
                    generationSteps = generationStepsState.value,
                    mazeType = mazeTypeState.value,
                    cellSize = selectedSizeState.value,
                    isAnimatingGeneration = isAnimatingGenerationState,
                    mazeGenerated = mazeGeneratedState,
                    showSolution = showSolutionState,
                    showHeatMap = showHeatMapState,
                    showControls = showControlsState,
                    selectedPalette = selectedPaletteState,
                    defaultBackground = defaultBackgroundColorState,
                    showHelp = showHelpState,
                    mazeID = mazeIDState.value,
                    currentGrid = currentGridState.value,
                    regenerateMaze = { submitMazeRequest() },
                    cleanupMazeData = { cleanupMazeData() },
                    cellSizes = computeCellSizes(selectedMazeTypeState.value, selectedSizeState.value, LocalContext.current),
                    optionalColor = optionalColorState.value
                )
            }
            mazeGeneratedState.value -> {
                MazeRenderScreen(
                    mazeGenerated = mazeGeneratedState,
                    showSolution = showSolutionState,
                    showHeatMap = showHeatMapState,
                    showControls = showControlsState,
                    padOffset = padOffsetState,
                    selectedPalette = selectedPaletteState,
                    mazeID = mazeIDState.value,
                    defaultBackground = defaultBackgroundColorState,
                    showHelp = showHelpState,
                    mazeCells = mazeCellsState.value,
                    mazeType = mazeTypeState.value,
                    cellSize = selectedSizeState.value,
                    optionalColor = optionalColorState.value,
                    regenerateMaze = { submitMazeRequest() },
                    moveAction = { direction -> performMove(direction) },
                    cellSizes = computeCellSizes(selectedMazeTypeState.value, selectedSizeState.value, LocalContext.current),
                    toggleHeatMap = {
                        showHeatMapState.value = !showHeatMapState.value
                        if (showHeatMapState.value) {
                            selectedPaletteState.value = randomPaletteExcluding(selectedPaletteState.value, allPalettes)
                            defaultBackgroundColorState.value = randomDefaultExcluding(defaultBackgroundColorState.value, CellColors.defaultBackgroundColors)
                        }
                    },
                    cleanupMazeData = { cleanupMazeData() },
                    showCelebration = showCelebrationState
                )
            }
            else -> {
                MazeRequestScreen(
                    mazeCells = mazeCellsState,
                    mazeGenerated = mazeGeneratedState,
                    mazeType = mazeTypeState,
                    selectedSize = selectedSizeState,
                    selectedMazeType = selectedMazeTypeState,
                    selectedAlgorithm = selectedAlgorithmState,
                    captureSteps = captureStepsState,
                    submitMazeRequest = { submitMazeRequest() }
                )
            }
        }

        if (isLoadingState.value) {
            LoadingOverlay(algorithm = selectedAlgorithmState.value)
        }
        if (showHelpState.value) {
            HelpModal(onDismiss = { showHelpState.value = false })
        }
        if (showCelebrationState.value) {
            SparkleScreen(count = 25, totalDuration = 3f)
        }
    }
}
