package com.jmisabella.mazer.screens.mazecomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import com.jmisabella.mazer.layout.cellBackgroundColor
import com.jmisabella.mazer.models.Coordinates
import com.jmisabella.mazer.models.HeatMapPalette
import com.jmisabella.mazer.models.MazeCell
import kotlinx.coroutines.delay

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
    val rows = (cells.maxOfOrNull { it.y } ?: 0) + 1
    val columns = (cells.maxOfOrNull { it.x } ?: 0) + 1
    val overlapDp = (octagonSize - squareSize) / 2f
    val negativeSpacing = -overlapDp

    var revealedSolutionPath by remember { mutableStateOf(setOf<Coordinates>()) }

    LaunchedEffect(showSolution) {
        if (showSolution) {
            revealedSolutionPath = emptySet<Coordinates>()
            val pathCells = cells.filter { it.onSolutionPath && !it.isVisited }.sortedBy { it.distance }
            pathCells.forEachIndexed { index, cell ->
                delay(15L * index.toLong())
                revealedSolutionPath = revealedSolutionPath + Coordinates(cell.x, cell.y)
            }
        } else {
            revealedSolutionPath = emptySet<Coordinates>()
        }
    }

    val sortedCells = remember(cells) { cells.sortedBy { it.y * columns + it.x } }

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        horizontalArrangement = Arrangement.spacedBy(negativeSpacing.dp),
        verticalArrangement = Arrangement.spacedBy(negativeSpacing.dp),
        modifier = Modifier.padding(top = 10.dp),
        contentPadding = PaddingValues(top = 10.dp)
    ) {
        items(sortedCells) { cell ->
            val coord = Coordinates(cell.x, cell.y)
            val fillColor = cellBackgroundColor(
                cell = cell,
                showSolution = showSolution,
                showHeatMap = showHeatMap,
                maxDistance = maxDistance,
                selectedPalette = selectedPalette,
                isRevealedSolution = revealedSolutionPath.contains(coord),
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
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.unit.dp
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
//            revealedSolutionPath = emptySet()
//            val pathCells = cells.filter { it.onSolutionPath && !it.isVisited }.sortedBy { it.distance }
//            pathCells.forEachIndexed { index, cell ->
//                delay(15L * index.toLong())
//                revealedSolutionPath = revealedSolutionPath + Coordinates(cell.x, cell.y)
//            }
//        } else {
//            revealedSolutionPath = emptySet()
//        }
//    }
//
//    val sortedCells = remember(cells) { cells.sortedBy { it.y * columns + it.x } }
//
//    LazyVerticalGrid(
//        columns = GridCells.Fixed(columns),
//        horizontalArrangement = Arrangement.spacedBy(negativeSpacing.dp),
//        verticalArrangement = Arrangement.spacedBy(negativeSpacing.dp),
//        modifier = Modifier.padding(top = 10.dp),
//        contentPadding = PaddingValues(top = 10.dp)
//    ) {
//        items(sortedCells.size) { index ->
//            val cell = sortedCells[index]
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
////package com.jmisabella.mazer.screens.mazecomponents
////
////import androidx.compose.foundation.layout.Arrangement
////import androidx.compose.foundation.layout.PaddingValues
////import androidx.compose.foundation.lazy.grid.GridCells
////import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
////import androidx.compose.foundation.layout.padding
////import androidx.compose.foundation.lazy.grid.items
////import androidx.compose.runtime.getValue
////import androidx.compose.runtime.setValue
////import androidx.compose.runtime.Composable
////import androidx.compose.runtime.LaunchedEffect
////import androidx.compose.runtime.mutableStateOf
////import androidx.compose.runtime.remember
////import androidx.compose.ui.Modifier
////import androidx.compose.ui.graphics.Color
////import androidx.compose.ui.unit.dp
////import com.jmisabella.mazer.layout.cellBackgroundColor
////import com.jmisabella.mazer.models.Coordinates
////import com.jmisabella.mazer.models.HeatMapPalette
////import com.jmisabella.mazer.models.MazeCell
////import kotlinx.coroutines.delay
////
////@Composable
////fun UpsilonMazeScreen(
////    cells: List<MazeCell>,
////    squareSize: Float,
////    octagonSize: Float,
////    showSolution: Boolean,
////    showHeatMap: Boolean,
////    selectedPalette: HeatMapPalette,
////    maxDistance: Int,
////    defaultBackgroundColor: Color,
////    optionalColor: Color?
////) {
////    val rows = (cells.maxOfOrNull { it.y } ?: 0) + 1
////    val columns = (cells.maxOfOrNull { it.x } ?: 0) + 1
////    val overlapDp = (octagonSize - squareSize) / 2f
////    val negativeSpacing = -overlapDp
////
////    var revealedSolutionPath by remember { mutableStateOf(setOf<Coordinates>()) }
////
////    LaunchedEffect(showSolution) {
////        if (showSolution) {
////            revealedSolutionPath = emptySet()
////            val pathCells = cells.filter { it.onSolutionPath && !it.isVisited }.sortedBy { it.distance }
////            pathCells.forEachIndexed { index, cell ->
////                delay(15L * index.toLong())
////                revealedSolutionPath = revealedSolutionPath + Coordinates(cell.x, cell.y)
////            }
////        } else {
////            revealedSolutionPath = emptySet()
////        }
////    }
////
////    val sortedCells = remember(cells) { cells.sortedBy { it.y * columns + it.x } }
////
////    LazyVerticalGrid(
////        columns = GridCells.Fixed(columns),
////        horizontalArrangement = Arrangement.spacedBy(negativeSpacing.dp),
////        verticalArrangement = Arrangement.spacedBy(negativeSpacing.dp),
////        modifier = Modifier.padding(top = 10.dp),
////        contentPadding = PaddingValues(top = 10.dp)
////    ) {
////        items(sortedCells.size) { index ->
////            val cell = sortedCells[index]
////            val coord = Coordinates(cell.x, cell.y)
////            val fillColor = cellBackgroundColor(
////                cell = cell,
////                showSolution = showSolution,
////                showHeatMap = showHeatMap,
////                maxDistance = maxDistance,
////                selectedPalette = selectedPalette,
////                isRevealedSolution = revealedSolutionPath.contains(coord),
////                defaultBackground = defaultBackgroundColor,
////                totalRows = rows,
////                optionalColor = optionalColor
////            )
////            UpsilonCell(
////                cell = cell,
////                gridCellSize = octagonSize,
////                squareSize = squareSize,
////                isSquare = cell.isSquare,
////                fillColor = fillColor,
////                optionalColor = optionalColor,
////                modifier = Modifier.size(octagonSize.dp)
////            )
////        }
////    }
////}
////
//////package com.jmisabella.mazer.screens.mazecomponents
//////
//////import androidx.compose.foundation.layout.*
//////import androidx.compose.foundation.lazy.grid.GridCells
//////import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
//////import androidx.compose.foundation.lazy.grid.items
//////import androidx.compose.runtime.*
//////import androidx.compose.ui.Modifier
//////import androidx.compose.ui.graphics.Color
//////import androidx.compose.ui.unit.dp
//////import com.jmisabella.mazer.layout.cellBackgroundColor
//////import com.jmisabella.mazer.models.*
//////import kotlinx.coroutines.delay
//////import kotlin.math.*
//////
//////@Composable
//////fun UpsilonMazeScreen(
//////    cells: List<MazeCell>,
//////    squareSize: Float,
//////    octagonSize: Float,
//////    showSolution: Boolean,
//////    showHeatMap: Boolean,
//////    selectedPalette: HeatMapPalette,
//////    maxDistance: Int,
//////    defaultBackgroundColor: Color,
//////    optionalColor: Color?
//////) {
//////    val rows = (cells.maxOfOrNull { it.y } ?: 0) + 1
//////    val columns = (cells.maxOfOrNull { it.x } ?: 0) + 1
//////    val horizontalSpacing = -(octagonSize - squareSize) / 2f
//////    val verticalSpacing = -1f
//////
//////    var revealedSolutionPath by remember { mutableStateOf(setOf<Coordinates>()) }
//////
//////    LaunchedEffect(showSolution) {
//////        if (showSolution) {
//////            revealedSolutionPath = emptySet()
//////            val pathCells = cells.filter { it.onSolutionPath && !it.isVisited }.sortedBy { it.distance }
//////            pathCells.forEachIndexed { index, cell ->
//////                delay(15L * index.toLong())
//////                revealedSolutionPath = revealedSolutionPath + Coordinates(cell.x, cell.y)
//////            }
//////        } else {
//////            revealedSolutionPath = emptySet()
//////        }
//////    }
//////    val spacing = -(octagonSize - squareSize) // No / 2f
////////    val sortedCells = remember(cells) { cells.sortedBy { it.y * columns + it.x } }
//////    val sortedCells = remember(cells) { cells.sortedBy { it.y * columns + it.x } }
//////
//////    LazyVerticalGrid(
//////        columns = GridCells.Fixed(columns),
////////        horizontalArrangement = Arrangement.spacedBy(horizontalSpacing.dp),
////////        verticalArrangement = Arrangement.spacedBy(verticalSpacing.dp),
//////        horizontalArrangement = Arrangement.spacedBy(spacing.dp),
//////        verticalArrangement = Arrangement.spacedBy(spacing.dp),
//////        modifier = Modifier.padding(top = 10.dp)
//////    ) {
//////        items(sortedCells.size) { index ->
//////            val cell = sortedCells[index]
//////            val coord = Coordinates(cell.x, cell.y)
//////            val fillColor = cellBackgroundColor(
//////                cell = cell,
//////                showSolution = showSolution,
//////                showHeatMap = showHeatMap,
//////                maxDistance = maxDistance,
//////                selectedPalette = selectedPalette,
//////                isRevealedSolution = revealedSolutionPath.contains(coord),
//////                defaultBackground = defaultBackgroundColor,
//////                totalRows = rows,
//////                optionalColor = optionalColor
//////            )
//////            UpsilonCell(
//////                cell = cell,
//////                gridCellSize = octagonSize,
//////                squareSize = squareSize,
//////                isSquare = cell.isSquare,
//////                fillColor = fillColor,
//////                optionalColor = optionalColor,
////////                modifier = Modifier.size(width = octagonSize.dp, height = (octagonSize * sqrt(2f) / 2f).dp)
//////                modifier = Modifier.size(octagonSize.dp)
//////            )
//////        }
//////    }
//////}
