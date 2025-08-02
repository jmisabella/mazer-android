package com.jmisabella.mazer

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import com.jmisabella.mazer.screens.MazeGenerationAnimationScreen
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

//@Serializable
//data class MazeRequest(
//    @SerialName("maze_type") val mazeType: String, // Use ffiName
//    @SerialName("width") val width: Int,
//    @SerialName("height") val height: Int,
//    @SerialName("algorithm") val algorithm: String, // Use ffiName
//    @SerialName("capture_steps") val captureSteps: Boolean
//)
//
//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent {
//            MazerTheme {
//                Surface(
//                    modifier = Modifier.fillMaxSize(),
//                    color = MaterialTheme.colorScheme.background
//                ) {
//                    ContentScreen()
//                }
//            }
//        }
//    }
//}

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
        enableEdgeToEdge() // Enable edge-to-edge display
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
    val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)

    // Explicit MutableState objects (instead of delegated vars)
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
    val currentGridState = remember { mutableStateOf<Long?>(null) } // Grid* as Long
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

        // Save initial preferences
        sharedPrefs.edit().apply {
            putInt("lastSize", selectedSizeState.value.value)
            putString("lastMazeType", selectedMazeTypeState.value.ffiName)
            putString("lastAlgorithm", selectedAlgorithmState.value.name)
            putBoolean("showHeatMap", showHeatMapState.value)
            apply()
        }

        // Run FFI integration test
        ffiIntegrationTestResultState.value = MazerNative.mazerFfiIntegrationTest()
        println("mazer_ffi_integration_test returned: ${ffiIntegrationTestResultState.value}")
        if (ffiIntegrationTestResultState.value == 42) {
            println("FFI integration test passed ✅")
        } else {
            println("FFI integration test failed ❌")
        }
    }

    // Save preferences on change (use .value)
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

    // Random palette selection excluding current
    fun randomPaletteExcluding(current: HeatMapPalette, allPalettes: List<HeatMapPalette>): HeatMapPalette {
        val availablePalettes = allPalettes.filter { it != current }
        return availablePalettes.randomOrNull() ?: current
    }

    // Random default background color excluding current
    fun randomDefaultExcluding(current: Color, all: List<Color>): Color {
        val others = all.filter { it != current }
        return others.randomOrNull() ?: current
    }

    // Cleanup maze data (use .value)
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

    // Submit maze request (use .value)
    fun submitMazeRequest() {
        coroutineScope.launch {

            println("FFI integration test result: " + MazerNative.mazerFfiIntegrationTest())

            isLoadingState.value = true
            withContext(Dispatchers.IO) {
                // Cleanup existing grid
                currentGridState.value?.let { gridPtr ->
                    MazerNative.destroyMaze(gridPtr)
                    currentGridState.value = null
                }

                val (squareCellSize, octagonCellSize) = computeCellSizes(selectedMazeTypeState.value, selectedSizeState.value, context)
                // Get screen dimensions
                val metrics = context.resources.displayMetrics
                val screenH = metrics.heightPixels / metrics.density
                val screenW = metrics.widthPixels / metrics.density
                val isSmallDevice = screenH <= 667

                val perSidePad: Float = when (selectedMazeTypeState.value) {
                    MazeType.ORTHOGONAL -> 20f
                    MazeType.UPSILON -> 12f
                    else -> if (isSmallDevice) 50f else 100f
                }

                val totalVerticalPadding = perSidePad * 2
                val controlArea = 80f
                val availableH = screenH - controlArea - totalVerticalPadding
                val drawableH = availableH // Simplified; adjust for insets if needed

                val cellSize = if (selectedMazeTypeState.value == MazeType.UPSILON) octagonCellSize else squareCellSize
                val spacing = if (selectedMazeTypeState.value == MazeType.UPSILON) (sqrt(2f) * 0.5f) * octagonCellSize else cellSize
                val rowHeight = if (selectedMazeTypeState.value == MazeType.UPSILON) octagonCellSize * (sqrt(2f) * 0.5f) else cellSize

                val maxHeightRows = maxOf(1, floor(availableH / rowHeight).toInt())
                val maxWidth = maxOf(1, floor(screenW / spacing).toInt())

                var finalWidth: Int
                var finalHeight: Int

                if (selectedMazeTypeState.value == MazeType.RHOMBIC) {
                    val s = squareCellSize
                    val diag = s * sqrt(2f)
                    val pitch = diag * 0.5f
                    finalWidth = maxOf(1, floor(screenW / diag).toInt())
                    finalHeight = maxOf(1, floor(drawableH / pitch).toInt())
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
                    mazeType = selectedMazeTypeState.value.ffiName, // Use ffiName
                    width = finalWidth,
                    height = finalHeight,
                    algorithm = selectedAlgorithmState.value.ffiName, // Use ffiName
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

    // Celebrate victory (use .value)
    fun celebrateVictory() {
        showCelebrationState.value = true
        captureStepsState.value = false

        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        toneGenerator.startTone(ToneGenerator.TONE_CDMA_CONFIRM, 200)

        coroutineScope.launch {
            kotlinx.coroutines.delay(3000)
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

    // Perform move (use .value)
    fun performMove(direction: String) {
        if (showCelebrationState.value) return

        val gridPtr = currentGridState.value ?: return

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

        if (newGridPtr == null || newGridPtr == 0L) return

        toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 200)
        vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))

        currentGridState.value = newGridPtr
        val cellsPtr = MazerNative.getCells(newGridPtr)
        if (cellsPtr == null) {
            errorMessageState.value = "Failed to retrieve updated maze."
            return
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

        if (!showCelebrationState.value && cells.any { it.isGoal && it.isActive }) {
            celebrateVictory()
        }
    }

    // Main content rendering (use .value and pass shared states directly)
    if (isGeneratingMazeState.value) {
        CircularProgressIndicator(modifier = Modifier.fillMaxSize())
    } else if (isAnimatingGenerationState.value) {
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
    } else if (mazeGeneratedState.value) {
        MazeRenderScreen(
            mazeGenerated = mazeGeneratedState,  // Pass shared state
            showSolution = showSolutionState,     // Pass shared state
            showHeatMap = showHeatMapState,       // Pass shared state
            showControls = showControlsState,     // Pass shared state
            padOffset = padOffsetState,           // Pass shared state
            selectedPalette = selectedPaletteState, // Pass shared state
            mazeID = mazeIDState.value,
            defaultBackground = defaultBackgroundColorState, // Pass shared state
            showHelp = showHelpState,             // Pass shared state
            mazeCells = mazeCellsState.value,
            mazeType = mazeTypeState.value,
            cellSize = selectedSizeState.value,
            optionalColor = optionalColorState.value,
            regenerateMaze = { submitMazeRequest() },
            moveAction = { direction -> performMove(direction) },
            cellSizes = computeCellSizes(selectedMazeTypeState.value, selectedSizeState.value, LocalContext.current),
            toggleHeatMap = {
                showHeatMapState.value = !showHeatMapState.value  // Update shared state
                if (showHeatMapState.value) {
                    selectedPaletteState.value = randomPaletteExcluding(selectedPaletteState.value, allPalettes)
                    defaultBackgroundColorState.value = randomDefaultExcluding(defaultBackgroundColorState.value, CellColors.defaultBackgroundColors)
                }
            },
            cleanupMazeData = { cleanupMazeData() }
        )
    } else {
        MazeRequestScreen(
            mazeCells = mazeCellsState,           // Pass shared state
            mazeGenerated = mazeGeneratedState,   // Pass shared state
            mazeType = mazeTypeState,             // Pass shared state
            selectedSize = selectedSizeState,     // Pass shared state
            selectedMazeType = selectedMazeTypeState, // Pass shared state
            selectedAlgorithm = selectedAlgorithmState, // Pass shared state
            captureSteps = captureStepsState,     // Pass shared state
            submitMazeRequest = { submitMazeRequest() }
        )
    }

    if (isLoadingState.value) {
        Text("Loading...")
    }
    if (showHelpState.value) {
        Text("Help Modal Placeholder") // TODO: Implement HelpModalScreen
    }
    if (showCelebrationState.value) {
        Text("Celebration Placeholder") // TODO: Implement SparkleScreen
    }
}

//@Composable
//fun ContentScreen() {
//    val coroutineScope = rememberCoroutineScope()
//    val context = LocalContext.current
//    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
//    val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
//
//    var ffiIntegrationTestResult by remember { mutableStateOf(0) }
//    var mazeCells by remember { mutableStateOf<List<MazeCell>>(emptyList()) }
//    var mazeType by remember { mutableStateOf(MazeType.ORTHOGONAL) }
//    var mazeGenerated by remember { mutableStateOf(false) }
//    var errorMessage by remember { mutableStateOf<String?>(null) }
//
//    var selectedSize by remember { mutableStateOf(CellSize.LARGE) }
//    var selectedMazeType by remember { mutableStateOf(MazeType.ORTHOGONAL) }
//    var selectedAlgorithm by remember { mutableStateOf(MazeAlgorithm.RECURSIVE_BACKTRACKER) }
//    var showSolution by remember { mutableStateOf(false) }
//    var showHeatMap by remember { mutableStateOf(false) }
//    var showControls by remember { mutableStateOf(false) }
//    var padOffset by remember { mutableStateOf(Offset.Zero) }
//    var showCelebration by remember { mutableStateOf(false) }
//    var selectedPalette by remember { mutableStateOf(allPalettes.randomOrNull() ?: turquoisePalette) }
//    var mazeID by remember { mutableStateOf(UUID.randomUUID().toString()) }
//    var currentGrid by remember { mutableStateOf<Long?>(null) } // Grid* as Long
//    var defaultBackgroundColor by remember { mutableStateOf<Color>(CellColors.defaultBackgroundColors.randomOrNull() ?: Color.White) }
//    var didInitialRandomization by remember { mutableStateOf(false) }
//    var hasPlayedSoundThisSession by remember { mutableStateOf(false) }
//    var captureSteps by remember { mutableStateOf(false) }
//    var isGeneratingMaze by remember { mutableStateOf(false) }
//    var isAnimatingGeneration by remember { mutableStateOf(false) }
//    var generationSteps by remember { mutableStateOf<List<List<MazeCell>>>(emptyList()) }
//    var isLoading by remember { mutableStateOf(false) }
//    var optionalColor by remember { mutableStateOf<Color?>(null) }
//    var showHelp by remember { mutableStateOf(false) }
//
//    // Load saved preferences
//    LaunchedEffect(Unit) {
//        val sharedPrefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
//        selectedSize = CellSize.values().find { it.value == sharedPrefs.getInt("lastSize", CellSize.MEDIUM.value) } ?: CellSize.MEDIUM
//        selectedMazeType = MazeType.values().find { it.ffiName == sharedPrefs.getString("lastMazeType", MazeType.ORTHOGONAL.ffiName) } ?: MazeType.ORTHOGONAL
//        selectedAlgorithm = MazeAlgorithm.values().find { it.name == sharedPrefs.getString("lastAlgorithm", MazeAlgorithm.RECURSIVE_BACKTRACKER.name) } ?: MazeAlgorithm.RECURSIVE_BACKTRACKER
//        showHeatMap = sharedPrefs.getBoolean("showHeatMap", false)
//
//        // Save initial preferences
//        sharedPrefs.edit().apply {
//            putInt("lastSize", selectedSize.value)
//            putString("lastMazeType", selectedMazeType.ffiName)
//            putString("lastAlgorithm", selectedAlgorithm.name)
//            putBoolean("showHeatMap", showHeatMap)
//            apply()
//        }
//
//        // Run FFI integration test
//        ffiIntegrationTestResult = MazerNative.mazerFfiIntegrationTest()
//        println("mazer_ffi_integration_test returned: $ffiIntegrationTestResult")
//        if (ffiIntegrationTestResult == 42) {
//            println("FFI integration test passed ✅")
//        } else {
//            println("FFI integration test failed ❌")
//        }
//    }
//
//    // Save preferences on change
//    LaunchedEffect(selectedSize) {
//        context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
//            .edit()
//            .putInt("lastSize", selectedSize.value)
//            .apply()
//    }
//    LaunchedEffect(selectedMazeType) {
//        context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
//            .edit()
//            .putString("lastMazeType", selectedMazeType.ffiName)
//            .apply()
//    }
//    LaunchedEffect(selectedAlgorithm) {
//        context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
//            .edit()
//            .putString("lastAlgorithm", selectedAlgorithm.name)
//            .apply()
//    }
//    LaunchedEffect(showHeatMap) {
//        context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
//            .edit()
//            .putBoolean("showHeatMap", showHeatMap)
//            .apply()
//    }
//
//    // Random palette selection excluding current
//    fun randomPaletteExcluding(current: HeatMapPalette, allPalettes: List<HeatMapPalette>): HeatMapPalette {
//        val availablePalettes = allPalettes.filter { it != current }
//        return availablePalettes.randomOrNull() ?: current
//    }
//
//    // Random default background color excluding current
//    fun randomDefaultExcluding(current: Color, all: List<Color>): Color {
//        val others = all.filter { it != current }
//        return others.randomOrNull() ?: current
//    }
//
//    // Cleanup maze data
//    fun cleanupMazeData() {
//        currentGrid?.let { gridPtr ->
//            MazerNative.destroyMaze(gridPtr)
//            currentGrid = null
//        }
//        mazeCells = emptyList()
//        generationSteps = emptyList()
//        mazeGenerated = false
//        isAnimatingGeneration = false
//    }
//
//    // Submit maze request
//    fun submitMazeRequest() {
//        coroutineScope.launch {
//
//            println("FFI integration test result: " + MazerNative.mazerFfiIntegrationTest())
//
//            isLoading = true
//            withContext(Dispatchers.IO) {
//                // Cleanup existing grid
//                currentGrid?.let { gridPtr ->
//                    MazerNative.destroyMaze(gridPtr)
//                    currentGrid = null
//                }
//
////                val (squareCellSize, octagonCellSize) = computeCellSizes(selectedMazeType, selectedSize)
//                val (squareCellSize, octagonCellSize) = computeCellSizes(selectedMazeType, selectedSize, context)
//                // Get screen dimensions
//                val metrics = context.resources.displayMetrics
//                val screenH = metrics.heightPixels / metrics.density
//                val screenW = metrics.widthPixels / metrics.density
//                val isSmallDevice = screenH <= 667
//
//                val perSidePad: Float = when (selectedMazeType) {
//                    MazeType.ORTHOGONAL -> 20f
//                    MazeType.UPSILON -> 12f
//                    else -> if (isSmallDevice) 50f else 100f
//                }
//
//                val totalVerticalPadding = perSidePad * 2
//                val controlArea = 80f
//                val availableH = screenH - controlArea - totalVerticalPadding
//                val drawableH = availableH // Simplified; adjust for insets if needed
//
//                val cellSize = if (selectedMazeType == MazeType.UPSILON) octagonCellSize else squareCellSize
//                val spacing = if (selectedMazeType == MazeType.UPSILON) (sqrt(2f) * 0.5f) * octagonCellSize else cellSize
//                val rowHeight = if (selectedMazeType == MazeType.UPSILON) octagonCellSize * (sqrt(2f) * 0.5f) else cellSize
//
//                val maxHeightRows = maxOf(1, floor(availableH / rowHeight).toInt())
//                val maxWidth = maxOf(1, floor(screenW / spacing).toInt())
//
//                var finalWidth: Int
//                var finalHeight: Int
//
//                if (selectedMazeType == MazeType.RHOMBIC) {
//                    val s = squareCellSize
//                    val diag = s * sqrt(2f)
//                    val pitch = diag * 0.5f
//                    finalWidth = maxOf(1, floor(screenW / diag).toInt())
//                    finalHeight = maxOf(1, floor(drawableH / pitch).toInt())
//                } else {
//                    finalWidth = if (selectedMazeType == MazeType.SIGMA) maxWidth / 3 else maxWidth
//                    finalHeight = if (selectedMazeType == MazeType.SIGMA) maxHeightRows / 3 else maxHeightRows
//                }
//
//                if (captureSteps && (finalWidth > 100 || finalHeight > 100)) {
//                    withContext(Dispatchers.Main) {
//                        errorMessage = "Show Maze Generation is only available for mazes with width and height ≤ 100."
//                        isLoading = false
//                    }
//                    return@withContext
//                }
//
//                val mazeRequest = MazeRequest(
//                    mazeType = selectedMazeType.ffiName, // Use ffiName
//                    width = finalWidth,
//                    height = finalHeight,
//                    algorithm = selectedAlgorithm.ffiName, // Use ffiName
//                    captureSteps = captureSteps
//                )
//
//                val jsonString = Json.encodeToString(mazeRequest)
//                println("Valid JSON: $jsonString")
//
//                val gridPtr = MazerNative.generateMaze(jsonString)
//                if (gridPtr == 0L) {
//                    println("Failed to generate maze")
//                    withContext(Dispatchers.Main) {
//                        errorMessage = "Failed to generate maze."
//                        isLoading = false
//                    }
//                    return@withContext
//                }
//
//                currentGrid = gridPtr
//
//                val cellsPtr = MazerNative.getCells(gridPtr)
//                if (cellsPtr == null) {
//                    println("Error occurred getting cells from grid pointer!")
//                    withContext(Dispatchers.Main) {
//                        errorMessage = "Failed to retrieve cells."
//                        isLoading = false
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
//                if (captureSteps) {
//                    val stepsCount = MazerNative.getGenerationStepsCount(gridPtr)
//                    steps = (0 until stepsCount).mapNotNull { stepIndex ->
//                        val stepCellsPtr = MazerNative.getGenerationStepCells(gridPtr, stepIndex)
//                        if (stepCellsPtr == null) {
//                            withContext(Dispatchers.Main) {
//                                errorMessage = "Failed to retrieve generation step cells."
//                                isLoading = false
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
//                    mazeCells = cells
//                    mazeType = MazeType.fromFFIName(cells.firstOrNull()?.mazeType) ?: MazeType.ORTHOGONAL
//                    if (captureSteps) {
//                        generationSteps = steps
//                        isAnimatingGeneration = true
//                    } else {
//                        mazeGenerated = true
//                    }
//                    optionalColor = if ((0..1).random() == 1) {
//                        listOf(Color.Magenta, Color.Gray, Color.Yellow, Color.Blue, Color(0xFFA200FF), Color(0xFFFF9500)).randomOrNull()
//                    } else null
//                    isLoading = false
//                    errorMessage = null
//                    selectedPalette = randomPaletteExcluding(selectedPalette, allPalettes)
//                    defaultBackgroundColor = randomDefaultExcluding(defaultBackgroundColor, CellColors.defaultBackgroundColors)
//                }
//            }
//        }
//    }
//
//    // Celebrate victory
//    fun celebrateVictory() {
//        showCelebration = true
//        captureSteps = false
//
//        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
//        toneGenerator.startTone(ToneGenerator.TONE_CDMA_CONFIRM, 200)
//
//        coroutineScope.launch {
//            kotlinx.coroutines.delay(3000)
//            withContext(Dispatchers.Main) {
//                showCelebration = false
//                if (mazeGenerated) {
//                    showSolution = false
//                    mazeID = UUID.randomUUID().toString()
//                    submitMazeRequest()
//                }
//            }
//        }
//    }
//
//    // Perform move
//    fun performMove(direction: String) {
//        if (showCelebration) return
//
//        val gridPtr = currentGrid ?: return
//
//        val tryDirections = when (mazeType) {
//            MazeType.ORTHOGONAL -> listOf(direction)
//            MazeType.DELTA -> when (direction) {
//                "UpperRight" -> listOf("UpperRight", "Right")
//                "LowerRight" -> listOf("LowerRight", "Right")
//                "UpperLeft" -> listOf("UpperLeft", "Left")
//                "LowerLeft" -> listOf("LowerLeft", "Left")
//                else -> listOf(direction)
//            }
//            MazeType.SIGMA -> when (direction) {
//                "UpperRight" -> listOf("UpperRight", "LowerRight")
//                "LowerRight" -> listOf("LowerRight", "UpperRight")
//                "UpperLeft" -> listOf("UpperLeft", "LowerLeft")
//                "LowerLeft" -> listOf("LowerLeft", "UpperLeft")
//                else -> listOf(direction)
//            }
//            MazeType.UPSILON -> listOf(direction)
//            MazeType.RHOMBIC -> listOf(direction)
//        }
//
//        var newGridPtr: Long? = null
//        for (dir in tryDirections) {
//            newGridPtr = MazerNative.makeMove(gridPtr, dir)
//            if (newGridPtr != 0L) break
//        }
//
//        if (newGridPtr == null || newGridPtr == 0L) return
//
//        toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 200)
//        vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
//
//        currentGrid = newGridPtr
//        val cellsPtr = MazerNative.getCells(newGridPtr)
//        if (cellsPtr == null) {
//            errorMessage = "Failed to retrieve updated maze."
//            return
//        }
//
//        val cells = cellsPtr.map { ffiCell ->
//            MazeCell(
//                x = ffiCell.x.toInt(),
//                y = ffiCell.y.toInt(),
//                mazeType = ffiCell.maze_type ?: "",
//                linked = ffiCell.linked?.toList() ?: emptyList(),
//                distance = ffiCell.distance,
//                isStart = ffiCell.is_start,
//                isGoal = ffiCell.is_goal,
//                isActive = ffiCell.is_active,
//                isVisited = ffiCell.is_visited,
//                hasBeenVisited = ffiCell.has_been_visited,
//                onSolutionPath = ffiCell.on_solution_path,
//                orientation = ffiCell.orientation ?: "",
//                isSquare = ffiCell.is_square
//            )
//        }
//
//        mazeCells = cells
//
//        if (!showCelebration && cells.any { it.isGoal && it.isActive }) {
//            celebrateVictory()
//        }
//    }
//
//    // Main content rendering
//    if (isGeneratingMaze) {
//        CircularProgressIndicator(modifier = Modifier.fillMaxSize())
//    } else if (isAnimatingGeneration) {
//        Text("Maze Generation Animation Placeholder") // TODO: Implement MazeGenerationAnimationScreen
//    } else if (mazeGenerated) {
//        MazeRenderScreen(
//            mazeGenerated = remember { mutableStateOf(mazeGenerated) },
//            showSolution = remember { mutableStateOf(showSolution) },
//            showHeatMap = remember { mutableStateOf(showHeatMap) },
//            showControls = remember { mutableStateOf(showControls) },
//            padOffset = remember { mutableStateOf(padOffset) },
//            selectedPalette = remember { mutableStateOf(selectedPalette) },
//            mazeID = mazeID,
//            defaultBackground = remember { mutableStateOf(defaultBackgroundColor) },
//            showHelp = remember { mutableStateOf(showHelp) },
//            mazeCells = mazeCells,
//            mazeType = mazeType,
//            cellSize = selectedSize,
//            optionalColor = optionalColor,
//            regenerateMaze = { submitMazeRequest() },
//            moveAction = { direction -> performMove(direction) },
//            cellSizes = computeCellSizes(selectedMazeType, selectedSize, LocalContext.current),
//            toggleHeatMap = {
//                showHeatMap = !showHeatMap
//                if (showHeatMap) {
//                    selectedPalette = randomPaletteExcluding(selectedPalette, allPalettes)
//                    defaultBackgroundColor = randomDefaultExcluding(defaultBackgroundColor, CellColors.defaultBackgroundColors)
//                }
//            },
//            cleanupMazeData = { cleanupMazeData() }
//        )
//    } else {
//        MazeRequestScreen(
//            mazeCells = remember { mutableStateOf(mazeCells) },
//            mazeGenerated = remember { mutableStateOf(mazeGenerated) },
//            mazeType = remember { mutableStateOf(mazeType) },
//            selectedSize = remember { mutableStateOf(selectedSize) },
//            selectedMazeType = remember { mutableStateOf(selectedMazeType) },
//            selectedAlgorithm = remember { mutableStateOf(selectedAlgorithm) },
//            captureSteps = remember { mutableStateOf(captureSteps) },
//            submitMazeRequest = { submitMazeRequest() }
//        )
//    }
//
//    if (isLoading) {
//        Text("Loading...")
//    }
//    if (showHelp) {
//        Text("Help Modal Placeholder") // TODO: Implement HelpModalScreen
//    }
//    if (showCelebration) {
//        Text("Celebration Placeholder") // TODO: Implement SparkleScreen
//    }
//}