// OrthogonalCellScreen.kt
package com.jmisabella.mazer.screens.mazecomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jmisabella.mazer.layout.cellBackgroundColor
import com.jmisabella.mazer.layout.wallStrokeWidth
import com.jmisabella.mazer.models.HeatMapPalette
import com.jmisabella.mazer.models.MazeCell
import com.jmisabella.mazer.models.MazeType

@Composable
fun OrthogonalCellScreen(
    cell: MazeCell,
    cellSize: Dp,
    showSolution: Boolean,
    showHeatMap: Boolean,
    selectedPalette: HeatMapPalette,
    maxDistance: Int,
    isRevealedSolution: Boolean,
    defaultBackgroundColor: Color,
    optionalColor: Color?,
    totalRows: Int
) {
    val density = LocalDensity.current.density
    val strokeWidth = wallStrokeWidth(MazeType.ORTHOGONAL, cellSize.value, density).dp

    Box(modifier = Modifier.size(cellSize)) {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(
                cellBackgroundColor(
                    cell,
                    showSolution,
                    showHeatMap,
                    maxDistance,
                    selectedPalette,
                    isRevealedSolution,
                    defaultBackgroundColor,
                    totalRows,
                    optionalColor
                )
            )
        )

        if (!cell.linked.contains("Left")) {
            Box(modifier = Modifier
                .offset(x = (-strokeWidth / 2))
                .width(strokeWidth)
                .height(cellSize)
                .background(Color.Black)
            )
        }
        if (!cell.linked.contains("Right")) {
            Box(modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = (strokeWidth / 2))
                .width(strokeWidth)
                .height(cellSize)
                .background(Color.Black)
            )
        }
        if (!cell.linked.contains("Up")) {
            Box(modifier = Modifier
                .offset(y = (-strokeWidth / 2))
                .height(strokeWidth)
                .width(cellSize)
                .background(Color.Black)
            )
        }
        if (!cell.linked.contains("Down")) {
            Box(modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (strokeWidth / 2))
                .height(strokeWidth)
                .width(cellSize)
                .background(Color.Black)
            )
        }
    }
}