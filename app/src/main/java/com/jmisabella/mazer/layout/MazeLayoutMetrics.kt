package com.jmisabella.mazer.layout

import com.jmisabella.mazer.models.MazeCell
import com.jmisabella.mazer.models.MazeType
import com.jmisabella.mazer.models.CellSize
import android.content.Context
import kotlin.math.min
import kotlin.math.sqrt

fun adjustedCellSize(mazeType: MazeType, cellSize: CellSize, context: Context): Float {
    val displayMetrics = context.resources.displayMetrics
    val density = displayMetrics.density
    val screenWidthDp = displayMetrics.widthPixels.toFloat() / density
    val screenHeightDp = displayMetrics.heightPixels.toFloat() / density
    val screenSize = screenWidthDp to screenHeightDp
    val adjustment: Float = when (mazeType) {
        MazeType.DELTA -> when (cellSize) {
            CellSize.TINY -> 1.6f
            CellSize.SMALL -> 1.85f
            CellSize.MEDIUM -> 2.2f
            CellSize.LARGE -> 2.5f
        }
        MazeType.ORTHOGONAL -> when (cellSize) {
            CellSize.TINY -> 1.2f
            CellSize.SMALL -> 1.3f
            CellSize.MEDIUM -> 1.65f
            CellSize.LARGE -> 1.8f
        }
        MazeType.SIGMA -> when (cellSize) {
            CellSize.TINY -> 0.63f
            CellSize.SMALL -> 0.73f
            CellSize.MEDIUM -> 0.85f
            CellSize.LARGE -> 1.1f
        }
        MazeType.UPSILON -> when (cellSize) {
            CellSize.TINY -> 2.35f
            CellSize.SMALL -> 2.5f
            CellSize.MEDIUM -> 2.85f
            CellSize.LARGE -> 3.3f
        }
        MazeType.RHOMBIC -> when (cellSize) {
            CellSize.TINY -> 1.45f
            CellSize.SMALL -> 1.65f
            CellSize.MEDIUM -> 2.0f
            CellSize.LARGE -> if (screenSize.first == 440f && screenSize.second == 956f ||
                screenSize.first == 414f && screenSize.second == 896f) 2.4f else 2.5f
        }
    }
    val rawSize = cellSize.value.toFloat()
    return adjustment * rawSize
}

fun computeCellSize(mazeCells: List<MazeCell>, mazeType: MazeType, cellSize: CellSize, context: Context): Float {
    val cols = (mazeCells.maxOfOrNull { it.x } ?: 0) + 1
    val rows = (mazeCells.maxOfOrNull { it.y } ?: 0) + 1
    val displayMetrics = context.resources.displayMetrics
    val density = displayMetrics.density
    val screenWidthDp = displayMetrics.widthPixels.toFloat() / density
    val screenHeightDp = displayMetrics.heightPixels.toFloat() / density

    val statusResourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
    val statusBarHeightPx = if (statusResourceId > 0) context.resources.getDimensionPixelSize(statusResourceId) else 0
    val statusBarHeightDp = statusBarHeightPx.toFloat() / density

    // Dynamically check navigation mode
    val navResourceId = context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
    var navBarHeightPx = if (navResourceId > 0) context.resources.getDimensionPixelSize(navResourceId) else 0
    var navBarHeightDp = navBarHeightPx.toFloat() / density

    // Check if gesture navigation is enabled (mode 2)
    val configNavId = context.resources.getIdentifier("config_navBarInteractionMode", "integer", "android")
    val navMode = if (configNavId > 0) context.resources.getInteger(configNavId) else 0
    if (navMode == 2) { // Gesture mode: no need to subtract nav bar height
        navBarHeightDp = 0f
    }

    val menuVerticalAdj = navigationMenuVerticalAdjustment(mazeType, cellSize, context)
    val estimatedRowHeight = 48f // Approximate height of the navigation menu row
    val estimatedBottomPadding = 0f // Reduced to rely on WindowInsets
    val overhead = statusBarHeightDp + menuVerticalAdj + estimatedRowHeight + navBarHeightDp + estimatedBottomPadding
    val availableHeightDp = screenHeightDp - overhead

    return when (mazeType) {
        MazeType.ORTHOGONAL -> {
            val cellFromHeight = availableHeightDp / rows.toFloat()
            val cellFromWidth = screenWidthDp / cols.toFloat()
            min(cellFromWidth, cellFromHeight)
        }
        MazeType.DELTA -> {
            val cellFromWidth = screenWidthDp * 2f / (cols + 1f)
            val heightFactor = rows * sqrt(3f) / 2f
            val cellFromHeight = availableHeightDp / heightFactor
            min(cellFromWidth, cellFromHeight)
        }
        MazeType.SIGMA -> {
            val units = 1.5f * cols.toFloat() + 0.5f
            val cellFromWidth = screenWidthDp / units
            val heightFactor = rows.toFloat() + 0.5f
            val cellFromHeight = availableHeightDp / (sqrt(3f) * heightFactor)
            min(cellFromWidth, cellFromHeight)
        }
        MazeType.UPSILON -> {
            val cellFromWidth = screenWidthDp / cols.toFloat()
            val cellFromHeight = availableHeightDp / rows.toFloat()
            min(cellFromWidth, cellFromHeight)
        }
        MazeType.RHOMBIC -> {
            val sqrt2 = sqrt(2f)
            val cellFromWidth = screenWidthDp / (cols * sqrt2 / 2 + sqrt2)
            val cellFromHeight = availableHeightDp / (rows * sqrt2 / 2 + sqrt2)
            min(cellFromWidth, cellFromHeight)
        }
    }
}

//fun computeCellSize(mazeCells: List<MazeCell>, mazeType: MazeType, cellSize: CellSize, context: Context): Float {
////    val cols = (mazeCells.maxOfOrNull { it.x } ?: 0) + 1
////    val rows = (mazeCells.maxOfOrNull { it.y } ?: 0) + 1
////    val displayMetrics = context.resources.displayMetrics
////    val density = displayMetrics.density
////    val screenWidthDp = displayMetrics.widthPixels.toFloat() / density
////    val screenHeightDp = displayMetrics.heightPixels.toFloat() / density
////
////    val statusResourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
////    val statusBarHeightPx = if (statusResourceId > 0) context.resources.getDimensionPixelSize(statusResourceId) else 0
////    val statusBarHeightDp = statusBarHeightPx.toFloat() / density
////
////    val navResourceId = context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
////    val navBarHeightPx = if (navResourceId > 0) context.resources.getDimensionPixelSize(navResourceId) else 0
////    val navBarHeightDp = navBarHeightPx.toFloat() / density
////
////    val menuVerticalAdj = navigationMenuVerticalAdjustment(mazeType, cellSize, context)
////    val estimatedRowHeight = 48f // Approximate height of the navigation menu row
////    val estimatedBottomPadding = 20f // As defined in MazeRenderScreen.kt
////    val overhead = statusBarHeightDp + menuVerticalAdj + estimatedRowHeight + navBarHeightDp + estimatedBottomPadding
////    val availableHeightDp = screenHeightDp - overhead
//
//    val cols = (mazeCells.maxOfOrNull { it.x } ?: 0) + 1
//    val rows = (mazeCells.maxOfOrNull { it.y } ?: 0) + 1
//    val displayMetrics = context.resources.displayMetrics
//    val density = displayMetrics.density
//    val screenWidthDp = displayMetrics.widthPixels.toFloat() / density
//    val screenHeightDp = displayMetrics.heightPixels.toFloat() / density
//
//    val statusResourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
//    val statusBarHeightPx = if (statusResourceId > 0) context.resources.getDimensionPixelSize(statusResourceId) else 0
//    val statusBarHeightDp = statusBarHeightPx.toFloat() / density
//
//    // Dynamically check navigation mode
//    val navResourceId = context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
//    var navBarHeightPx = if (navResourceId > 0) context.resources.getDimensionPixelSize(navResourceId) else 0
//    var navBarHeightDp = navBarHeightPx.toFloat() / density
//
//    // Check if gesture navigation is enabled (mode 2)
//    val configNavId = context.resources.getIdentifier("config_navBarInteractionMode", "integer", "android")
//    val navMode = if (configNavId > 0) context.resources.getInteger(configNavId) else 0
//    if (navMode == 2) {  // Gesture mode: no need to subtract nav bar height
//        navBarHeightDp = 0f
//    }
//
//    val menuVerticalAdj = navigationMenuVerticalAdjustment(mazeType, cellSize, context)
//    val estimatedRowHeight = 48f // Approximate height of the navigation menu row
//    val estimatedBottomPadding = 20f // As defined in MazeRenderScreen.kt
//    val overhead = statusBarHeightDp + menuVerticalAdj + estimatedRowHeight + navBarHeightDp + estimatedBottomPadding
//    val availableHeightDp = screenHeightDp - overhead
//
//    return when (mazeType) {
//        MazeType.ORTHOGONAL -> {
//            val cellFromHeight = availableHeightDp / rows.toFloat()
//            val cellFromWidth = screenWidthDp / cols.toFloat()
//            min(cellFromWidth, cellFromHeight)
//        }
//        MazeType.DELTA -> {
//            val cellFromWidth = screenWidthDp * 2f / (cols + 1f)
//            val heightFactor = rows * sqrt(3f) / 2f
//            val cellFromHeight = availableHeightDp / heightFactor
//            // Prioritize fitting width exactly (as in previous behavior) but cap at height to prevent overflow
//            min(cellFromWidth, cellFromHeight)
//        }
//        MazeType.SIGMA -> {
//            val units = 1.5f * cols.toFloat() + 0.5f
//            val cellFromWidth = screenWidthDp / units
//
//            val hexH = sqrt(3f)
//            val cellFromHeight = availableHeightDp / (hexH * (rows.toFloat() + 0.5f))
//
//            min(cellFromWidth, cellFromHeight)
//        }
//        MazeType.RHOMBIC -> {
//            val factorW = (cols.toFloat() + 1f) * sqrt(2f) / 2f
//            val factorH = (rows.toFloat() + 1f) * sqrt(2f) / 2f
//            val cellFromWidth = screenWidthDp / factorW
//            val cellFromHeight = availableHeightDp / factorH
//            min(cellFromWidth, cellFromHeight)
//        }
//        MazeType.UPSILON -> {
//            val spacingFactor = (2f - sqrt(2f)) / 2f // Corrected to ≈0.2929
//            val effectiveCols = cols.toFloat() - spacingFactor * (cols - 1).toFloat()
//            val cellFromWidth = screenWidthDp / effectiveCols
//            val effectiveRows = rows.toFloat() - spacingFactor * (rows - 1).toFloat()
//            val cellFromHeight = availableHeightDp / effectiveRows
//            min(cellFromWidth, cellFromHeight)
//        }
//        else -> screenWidthDp / cols.toFloat()
//    }
//}

fun computeVerticalPadding(mazeType: MazeType, cellSize: CellSize, context: Context): Float {
    val displayMetrics = context.resources.displayMetrics
    val density = displayMetrics.density
    val screenHeightDp = displayMetrics.heightPixels.toFloat() / density
    val basePadding: Float = when (mazeType) {
        MazeType.DELTA -> 230f
        MazeType.ORTHOGONAL -> 140f
        MazeType.SIGMA -> 280f
        MazeType.UPSILON -> 0f
        MazeType.RHOMBIC -> 0f
    }
    val sizeRatio: Float = when (cellSize) {
        CellSize.TINY -> 0.35f
        CellSize.SMALL -> 0.30f
        CellSize.MEDIUM -> 0.25f
        CellSize.LARGE -> 0.20f
    }
    return min(basePadding, screenHeightDp * sizeRatio)
}


data class CellSizes(val square: Float, val octagon: Float)

fun computeCellSizes(mazeType: MazeType, cellSize: CellSize, context: Context): CellSizes {
    val baseCellSize = adjustedCellSize(mazeType, cellSize, context)
    return if (mazeType == MazeType.UPSILON) {
        val octagonCellSize = baseCellSize
        val squareCellSize = octagonCellSize * (sqrt(2f) - 1f)
        CellSizes(squareCellSize, octagonCellSize)
    } else {
        CellSizes(baseCellSize, baseCellSize)
    }
}

fun computeDeltaCellSize(
    cellSize: CellSize,
    columns: Int,
    rows: Int,
    screenWidthDp: Float,
    screenHeightDp: Float,
    context: Context
): Float {
    val displayMetrics = context.resources.displayMetrics
    val density = displayMetrics.density

    val statusResourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
    val statusBarHeightPx = if (statusResourceId > 0) context.resources.getDimensionPixelSize(statusResourceId) else 0
    val statusBarHeightDp = statusBarHeightPx.toFloat() / density

    val navResourceId = context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
    val navBarHeightPx = if (navResourceId > 0) context.resources.getDimensionPixelSize(navResourceId) else 0
    val navBarHeightDp = navBarHeightPx.toFloat() / density

    val menuVerticalAdj = navigationMenuVerticalAdjustment(MazeType.DELTA, cellSize, context)
    val estimatedRowHeight = 48f // Approximate height of the navigation menu row
    val estimatedBottomPadding = 20f // As defined in MazeRenderScreen.kt
    val overhead = statusBarHeightDp + menuVerticalAdj + estimatedRowHeight + navBarHeightDp + estimatedBottomPadding
    val availableHeightDp = screenHeightDp - overhead

    val widthBased = screenWidthDp * 2f / (columns.toFloat() + 1f)
    val heightBased = availableHeightDp * 2f / (rows.toFloat() * sqrt(3f))

    return min(widthBased, heightBased)
}

fun navigationMenuVerticalAdjustment(mazeType: MazeType, cellSize: CellSize, context: Context): Float {
    val displayMetrics = context.resources.displayMetrics
    val density = displayMetrics.density
    val screenWidthDp = displayMetrics.widthPixels.toFloat() / density
    val screenHeightDp = displayMetrics.heightPixels.toFloat() / density
    val screenSize = Pair(screenWidthDp, screenHeightDp)

    // Base vertical offset to position menu closer to maze grid
    val baseOffset = when (mazeType) {
        MazeType.ORTHOGONAL -> 10f // Reduced from 20f to move the menu higher up
        MazeType.DELTA -> 0f
        MazeType.SIGMA -> 0f
        MazeType.UPSILON -> 0f
        MazeType.RHOMBIC -> 10f
    }

    // Adjust offset based on cell size
    val sizeAdjustment = when (cellSize) {
        CellSize.TINY -> 0.8f
        CellSize.SMALL -> 0.9f
        CellSize.MEDIUM -> 1.0f
        CellSize.LARGE -> 1.1f
    }

    // Specific adjustments for RHOMBIC maze type
    val paddingMap: List<Quadruple<Pair<Float, Float>, MazeType, CellSize, Float>> = listOf(
        Quadruple(Pair(375f, 812f), MazeType.RHOMBIC, CellSize.TINY, 8f),
        Quadruple(Pair(390f, 844f), MazeType.RHOMBIC, CellSize.TINY, 6f),
        Quadruple(Pair(393f, 852f), MazeType.RHOMBIC, CellSize.TINY, 5f),
        Quadruple(Pair(402f, 874f), MazeType.RHOMBIC, CellSize.TINY, 4f),
        Quadruple(Pair(414f, 896f), MazeType.RHOMBIC, CellSize.TINY, 2f),
        Quadruple(Pair(428f, 926f), MazeType.RHOMBIC, CellSize.TINY, 0f),
        Quadruple(Pair(430f, 932f), MazeType.RHOMBIC, CellSize.TINY, -2f),
        Quadruple(Pair(440f, 956f), MazeType.RHOMBIC, CellSize.TINY, -4f),
        Quadruple(Pair(375f, 812f), MazeType.RHOMBIC, CellSize.SMALL, 8f),
        Quadruple(Pair(390f, 844f), MazeType.RHOMBIC, CellSize.SMALL, 6f),
        Quadruple(Pair(393f, 852f), MazeType.RHOMBIC, CellSize.SMALL, 5f),
        Quadruple(Pair(402f, 874f), MazeType.RHOMBIC, CellSize.SMALL, 4f),
        Quadruple(Pair(414f, 896f), MazeType.RHOMBIC, CellSize.SMALL, 2f),
        Quadruple(Pair(428f, 926f), MazeType.RHOMBIC, CellSize.SMALL, 0f),
        Quadruple(Pair(430f, 932f), MazeType.RHOMBIC, CellSize.SMALL, -2f),
        Quadruple(Pair(440f, 956f), MazeType.RHOMBIC, CellSize.SMALL, -4f),
        Quadruple(Pair(375f, 812f), MazeType.RHOMBIC, CellSize.MEDIUM, 6f),
        Quadruple(Pair(390f, 844f), MazeType.RHOMBIC, CellSize.MEDIUM, 4f),
        Quadruple(Pair(393f, 852f), MazeType.RHOMBIC, CellSize.MEDIUM, 3f),
        Quadruple(Pair(402f, 874f), MazeType.RHOMBIC, CellSize.MEDIUM, 2f),
        Quadruple(Pair(414f, 896f), MazeType.RHOMBIC, CellSize.MEDIUM, 0f),
        Quadruple(Pair(428f, 926f), MazeType.RHOMBIC, CellSize.MEDIUM, -2f),
        Quadruple(Pair(430f, 932f), MazeType.RHOMBIC, CellSize.MEDIUM, -3f),
        Quadruple(Pair(440f, 956f), MazeType.RHOMBIC, CellSize.MEDIUM, -5f),
        Quadruple(Pair(375f, 812f), MazeType.RHOMBIC, CellSize.LARGE, 6f),
        Quadruple(Pair(390f, 844f), MazeType.RHOMBIC, CellSize.LARGE, 4f),
        Quadruple(Pair(393f, 852f), MazeType.RHOMBIC, CellSize.LARGE, 3f),
        Quadruple(Pair(402f, 874f), MazeType.RHOMBIC, CellSize.LARGE, 2f),
        Quadruple(Pair(414f, 896f), MazeType.RHOMBIC, CellSize.LARGE, 0f),
        Quadruple(Pair(428f, 926f), MazeType.RHOMBIC, CellSize.LARGE, -2f),
        Quadruple(Pair(430f, 932f), MazeType.RHOMBIC, CellSize.LARGE, -3f),
        Quadruple(Pair(440f, 956f), MazeType.RHOMBIC, CellSize.LARGE, -5f),
    )

    if (mazeType == MazeType.RHOMBIC) {
        for (entry in paddingMap) {
            if (entry.second == mazeType &&
                entry.third == cellSize &&
                entry.first.first == screenSize.first &&
                entry.first.second == screenSize.second) {
                return baseOffset * sizeAdjustment + entry.fourth
            }
        }
    }
    return baseOffset * sizeAdjustment
}

//fun navigationMenuVerticalAdjustment(mazeType: MazeType, cellSize: CellSize, context: Context): Float {
//    val displayMetrics = context.resources.displayMetrics
//    val density = displayMetrics.density
//    val screenWidthDp = displayMetrics.widthPixels.toFloat() / density
//    val screenHeightDp = displayMetrics.heightPixels.toFloat() / density
//    val screenSize = Pair(screenWidthDp, screenHeightDp)
//
//    // Base vertical offset to position menu closer to maze grid
//    val baseOffset = when (mazeType) {
//        MazeType.ORTHOGONAL -> 10f // Reduced from 20f to move the menu higher up
//        MazeType.DELTA -> 30f
//        MazeType.SIGMA -> 30f
//        MazeType.UPSILON -> 10f
//        MazeType.RHOMBIC -> 10f
//    }
//
//    // Adjust offset based on cell size
//    val sizeAdjustment = when (cellSize) {
//        CellSize.TINY -> 0.8f
//        CellSize.SMALL -> 0.9f
//        CellSize.MEDIUM -> 1.0f
//        CellSize.LARGE -> 1.1f
//    }
//
//    // Specific adjustments for RHOMBIC maze type
//    val paddingMap: List<Quadruple<Pair<Float, Float>, MazeType, CellSize, Float>> = listOf(
//        Quadruple(Pair(375f, 812f), MazeType.RHOMBIC, CellSize.TINY, 8f),
//        Quadruple(Pair(390f, 844f), MazeType.RHOMBIC, CellSize.TINY, 6f),
//        Quadruple(Pair(393f, 852f), MazeType.RHOMBIC, CellSize.TINY, 5f),
//        Quadruple(Pair(402f, 874f), MazeType.RHOMBIC, CellSize.TINY, 4f),
//        Quadruple(Pair(414f, 896f), MazeType.RHOMBIC, CellSize.TINY, 2f),
//        Quadruple(Pair(428f, 926f), MazeType.RHOMBIC, CellSize.TINY, 0f),
//        Quadruple(Pair(430f, 932f), MazeType.RHOMBIC, CellSize.TINY, -2f),
//        Quadruple(Pair(440f, 956f), MazeType.RHOMBIC, CellSize.TINY, -4f),
//        Quadruple(Pair(375f, 812f), MazeType.RHOMBIC, CellSize.SMALL, 8f),
//        Quadruple(Pair(390f, 844f), MazeType.RHOMBIC, CellSize.SMALL, 6f),
//        Quadruple(Pair(393f, 852f), MazeType.RHOMBIC, CellSize.SMALL, 5f),
//        Quadruple(Pair(402f, 874f), MazeType.RHOMBIC, CellSize.SMALL, 4f),
//        Quadruple(Pair(414f, 896f), MazeType.RHOMBIC, CellSize.SMALL, 2f),
//        Quadruple(Pair(428f, 926f), MazeType.RHOMBIC, CellSize.SMALL, 0f),
//        Quadruple(Pair(430f, 932f), MazeType.RHOMBIC, CellSize.SMALL, -2f),
//        Quadruple(Pair(440f, 956f), MazeType.RHOMBIC, CellSize.SMALL, -4f),
//        Quadruple(Pair(375f, 812f), MazeType.RHOMBIC, CellSize.MEDIUM, 6f),
//        Quadruple(Pair(390f, 844f), MazeType.RHOMBIC, CellSize.MEDIUM, 4f),
//        Quadruple(Pair(393f, 852f), MazeType.RHOMBIC, CellSize.MEDIUM, 3f),
//        Quadruple(Pair(402f, 874f), MazeType.RHOMBIC, CellSize.MEDIUM, 2f),
//        Quadruple(Pair(414f, 896f), MazeType.RHOMBIC, CellSize.MEDIUM, 0f),
//        Quadruple(Pair(428f, 926f), MazeType.RHOMBIC, CellSize.MEDIUM, -2f),
//        Quadruple(Pair(430f, 932f), MazeType.RHOMBIC, CellSize.MEDIUM, -3f),
//        Quadruple(Pair(440f, 956f), MazeType.RHOMBIC, CellSize.MEDIUM, -5f),
//        Quadruple(Pair(375f, 812f), MazeType.RHOMBIC, CellSize.LARGE, 6f),
//        Quadruple(Pair(390f, 844f), MazeType.RHOMBIC, CellSize.LARGE, 4f),
//        Quadruple(Pair(393f, 852f), MazeType.RHOMBIC, CellSize.LARGE, 3f),
//        Quadruple(Pair(402f, 874f), MazeType.RHOMBIC, CellSize.LARGE, 2f),
//        Quadruple(Pair(414f, 896f), MazeType.RHOMBIC, CellSize.LARGE, 0f),
//        Quadruple(Pair(428f, 926f), MazeType.RHOMBIC, CellSize.LARGE, -2f),
//        Quadruple(Pair(430f, 932f), MazeType.RHOMBIC, CellSize.LARGE, -3f),
//        Quadruple(Pair(440f, 956f), MazeType.RHOMBIC, CellSize.LARGE, -5f),
//    )
//
//    if (mazeType == MazeType.RHOMBIC) {
//        for (entry in paddingMap) {
//            if (entry.second == mazeType &&
//                entry.third == cellSize &&
//                entry.first.first == screenSize.first &&
//                entry.first.second == screenSize.second) {
//                return baseOffset * sizeAdjustment + entry.fourth
//            }
//        }
//    }
//    return baseOffset * sizeAdjustment
//}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

fun navigationMenuHorizontalAdjustment(mazeType: MazeType, cellSize: CellSize, context: Context): Float {
    val displayMetrics = context.resources.displayMetrics
    val density = displayMetrics.density
    val screenWidthDp = displayMetrics.widthPixels.toFloat() / density
    val screenHeightDp = displayMetrics.heightPixels.toFloat() / density
    val screenSize = Pair(screenWidthDp, screenHeightDp)

    val paddingMap: List<Quadruple<Pair<Float, Float>, MazeType, CellSize, Float>> = listOf(
        Quadruple(Pair(440f, 956f), MazeType.RHOMBIC, CellSize.LARGE, 6f),
    )

    if (mazeType == MazeType.RHOMBIC) {
        for (entry in paddingMap) {
            if (entry.second == mazeType &&
                entry.third == cellSize &&
                entry.first.first == screenSize.first &&
                entry.first.second == screenSize.second) {
                return entry.fourth
            }
        }
    }
    return 0f
}




//package com.jmisabella.mazer.layout
//
//import com.jmisabella.mazer.models.MazeCell
//import com.jmisabella.mazer.models.MazeType
//import com.jmisabella.mazer.models.CellSize
//import android.content.Context
//import kotlin.math.abs
//import kotlin.math.max
//import kotlin.math.min
//import kotlin.math.sqrt
//
//
//fun adjustedCellSize(mazeType: MazeType, cellSize: CellSize, context: Context): Float {
//    val displayMetrics = context.resources.displayMetrics
//    val density = displayMetrics.density
//    val screenWidthDp = displayMetrics.widthPixels.toFloat() / density
//    val screenHeightDp = displayMetrics.heightPixels.toFloat() / density
//    val screenSize = screenWidthDp to screenHeightDp
//    val adjustment: Float = when (mazeType) {
//        MazeType.DELTA -> when (cellSize) {
//            CellSize.TINY -> 1.6f
//            CellSize.SMALL -> 1.85f
//            CellSize.MEDIUM -> 2.2f
//            CellSize.LARGE -> 2.5f
//        }
//        MazeType.ORTHOGONAL -> when (cellSize) {
//            CellSize.TINY -> 1.2f
//            CellSize.SMALL -> 1.3f
//            CellSize.MEDIUM -> 1.65f
//            CellSize.LARGE -> 1.8f
//        }
//        MazeType.SIGMA -> when (cellSize) {
//            CellSize.TINY -> 0.63f
//            CellSize.SMALL -> 0.73f
//            CellSize.MEDIUM -> 0.85f
//            CellSize.LARGE -> 1.1f
//        }
//        MazeType.UPSILON -> when (cellSize) {
//            CellSize.TINY -> 2.35f
//            CellSize.SMALL -> 2.5f
//            CellSize.MEDIUM -> 2.85f
//            CellSize.LARGE -> 3.3f
//        }
//        MazeType.RHOMBIC -> when (cellSize) {
//            CellSize.TINY -> 1.45f
//            CellSize.SMALL -> 1.65f
//            CellSize.MEDIUM -> 2.0f
//            CellSize.LARGE -> if (screenSize.first == 440f && screenSize.second == 956f ||
//                screenSize.first == 414f && screenSize.second == 896f) 2.4f else 2.5f
//        }
//    }
//    val rawSize = cellSize.value.toFloat()
//    return adjustment * rawSize
//}
//
//fun computeCellSize(mazeCells: List<MazeCell>, mazeType: MazeType, cellSize: CellSize, context: Context): Float {
//    val cols = (mazeCells.maxOfOrNull { it.x } ?: 0) + 1
//    val rows = (mazeCells.maxOfOrNull { it.y } ?: 0) + 1
//    val displayMetrics = context.resources.displayMetrics
//    val density = displayMetrics.density
//    val screenWidthDp = displayMetrics.widthPixels.toFloat() / density
//    val screenHeightDp = displayMetrics.heightPixels.toFloat() / density
//
//    val statusResourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
//    val statusBarHeightPx = if (statusResourceId > 0) context.resources.getDimensionPixelSize(statusResourceId) else 0
//    val statusBarHeightDp = statusBarHeightPx.toFloat() / density
//
//    val navResourceId = context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
//    val navBarHeightPx = if (navResourceId > 0) context.resources.getDimensionPixelSize(navResourceId) else 0
//    val navBarHeightDp = navBarHeightPx.toFloat() / density
//
//    val menuVerticalAdj = navigationMenuVerticalAdjustment(mazeType, cellSize, context)
//    val estimatedRowHeight = 48f // Approximate height of the navigation menu row
//    val estimatedBottomPadding = 20f // As defined in MazeRenderScreen.kt
//    val overhead = statusBarHeightDp + menuVerticalAdj + estimatedRowHeight + navBarHeightDp + estimatedBottomPadding
//    val availableHeightDp = screenHeightDp - overhead
//
//    return when (mazeType) {
//        MazeType.ORTHOGONAL -> {
//            val cellFromHeight = availableHeightDp / rows.toFloat()
//            val cellFromWidth = screenWidthDp / cols.toFloat()
//            min(cellFromWidth, cellFromHeight)
//        }
////        MazeType.DELTA -> computeDeltaCellSize(cellSize, cols, screenWidthDp, screenHeightDp)
//        MazeType.DELTA -> computeDeltaCellSize(cellSize, cols, rows, screenWidthDp, screenHeightDp, context)
//        MazeType.SIGMA -> {
//            val units = 1.5f * (cols - 1).toFloat() + 1f
//            val cellFromWidth = screenWidthDp / units
//
//            val hexH = sqrt(3f)
//            val cellFromHeight = availableHeightDp / (hexH * (rows.toFloat() + 0.5f))
//
//            min(cellFromWidth, cellFromHeight)
//        }
//        MazeType.RHOMBIC -> {
//            val factorW = (cols.toFloat() + 1f) * sqrt(2f) / 2f
//            val factorH = (rows.toFloat() + 1f) * sqrt(2f) / 2f
//            val cellFromWidth = screenWidthDp / factorW
//            val cellFromHeight = availableHeightDp / factorH
//            min(cellFromWidth, cellFromHeight)
//        }
//        MazeType.UPSILON -> {
//            val spacingFactor = 2f - sqrt(2f) // ≈0.5858
//            val effectiveCols = cols.toFloat() - spacingFactor * (cols - 1).toFloat()
//            val cellFromWidth = screenWidthDp / effectiveCols
//            val effectiveRows = rows.toFloat() - spacingFactor * (rows - 1).toFloat()
//            val cellFromHeight = availableHeightDp / effectiveRows
//            min(cellFromWidth, cellFromHeight)
//        }
//        else -> screenWidthDp / cols.toFloat()
//    }
//}
//
//fun computeVerticalPadding(mazeType: MazeType, cellSize: CellSize, context: Context): Float {
//    val displayMetrics = context.resources.displayMetrics
//    val density = displayMetrics.density
//    val screenHeightDp = displayMetrics.heightPixels.toFloat() / density
//    val basePadding: Float = when (mazeType) {
//        MazeType.DELTA -> 230f
//        MazeType.ORTHOGONAL -> 140f
//        MazeType.SIGMA -> 280f
//        MazeType.UPSILON -> 0f
//        MazeType.RHOMBIC -> 0f
//    }
//    val sizeRatio: Float = when (cellSize) {
//        CellSize.TINY -> 0.35f
//        CellSize.SMALL -> 0.30f
//        CellSize.MEDIUM -> 0.25f
//        CellSize.LARGE -> 0.20f
//    }
//    return min(basePadding, screenHeightDp * sizeRatio)
//}
//
//
//data class CellSizes(val square: Float, val octagon: Float)
//
//fun computeCellSizes(mazeType: MazeType, cellSize: CellSize, context: Context): CellSizes {
//    val baseCellSize = adjustedCellSize(mazeType, cellSize, context)
//    return if (mazeType == MazeType.UPSILON) {
//        val octagonCellSize = baseCellSize
//        val squareCellSize = octagonCellSize * (sqrt(2f) - 1f)
//        CellSizes(squareCellSize, octagonCellSize)
//    } else {
//        CellSizes(baseCellSize, baseCellSize)
//    }
//}
//
//fun computeDeltaCellSize(
//    cellSize: CellSize,
//    columns: Int,
//    rows: Int,
//    screenWidthDp: Float,
//    screenHeightDp: Float,
//    context: Context
//): Float {
//    val displayMetrics = context.resources.displayMetrics
//    val density = displayMetrics.density
//
//    val statusResourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
//    val statusBarHeightPx = if (statusResourceId > 0) context.resources.getDimensionPixelSize(statusResourceId) else 0
//    val statusBarHeightDp = statusBarHeightPx.toFloat() / density
//
//    val navResourceId = context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
//    val navBarHeightPx = if (navResourceId > 0) context.resources.getDimensionPixelSize(navResourceId) else 0
//    val navBarHeightDp = navBarHeightPx.toFloat() / density
//
//    val menuVerticalAdj = navigationMenuVerticalAdjustment(MazeType.DELTA, cellSize, context)
//    val estimatedRowHeight = 48f // Approximate height of the navigation menu row
//    val estimatedBottomPadding = 20f // As defined in MazeRenderScreen.kt
//    val overhead = statusBarHeightDp + menuVerticalAdj + estimatedRowHeight + navBarHeightDp + estimatedBottomPadding
//    val availableHeightDp = screenHeightDp - overhead
//
//    val widthBased = screenWidthDp * 2f / (columns.toFloat() + 1f)
//    val heightBased = availableHeightDp * 2f / (rows.toFloat() * sqrt(3f))
//
//    return min(widthBased, heightBased)
//}
//
////fun computeDeltaCellSize(
////    cellSize: CellSize,
////    columns: Int,
////    screenWidthDp: Float,
////    screenHeightDp: Float
////): Float {
////    val paddingMap: List<Triple<Pair<Float, Float>, CellSize, Float>> = listOf(
////        Triple(Pair(375f, 667f), CellSize.TINY, 46f),
////        Triple(Pair(375f, 667f), CellSize.SMALL, 46f),
////        Triple(Pair(375f, 667f), CellSize.MEDIUM, 46f),
////        Triple(Pair(375f, 667f), CellSize.LARGE, 46f),
////        Triple(Pair(375f, 812f), CellSize.TINY, 42f),
////        Triple(Pair(375f, 812f), CellSize.SMALL, 40f),
////        Triple(Pair(375f, 812f), CellSize.MEDIUM, 36f),
////        Triple(Pair(375f, 812f), CellSize.LARGE, 36f),
////        Triple(Pair(390f, 844f), CellSize.TINY, 42.5f),
////        Triple(Pair(390f, 844f), CellSize.SMALL, 44.5f),
////        Triple(Pair(390f, 844f), CellSize.MEDIUM, 40.7f),
////        Triple(Pair(390f, 844f), CellSize.LARGE, 40.7f),
////        Triple(Pair(393f, 852f), CellSize.TINY, 43f),
////        Triple(Pair(393f, 852f), CellSize.SMALL, 43f),
////        Triple(Pair(393f, 852f), CellSize.MEDIUM, 43f),
////        Triple(Pair(393f, 852f), CellSize.LARGE, 43f),
////        Triple(Pair(402f, 874f), CellSize.TINY, 45.5f),
////        Triple(Pair(402f, 874f), CellSize.SMALL, 47.5f),
////        Triple(Pair(402f, 874f), CellSize.MEDIUM, 45f),
////        Triple(Pair(402f, 874f), CellSize.LARGE, 48f),
////        Triple(Pair(414f, 896f), CellSize.TINY, 50f),
////        Triple(Pair(414f, 896f), CellSize.SMALL, 48f),
////        Triple(Pair(414f, 896f), CellSize.MEDIUM, 48.5f),
////        Triple(Pair(414f, 896f), CellSize.LARGE, 45f),
////        Triple(Pair(428f, 926f), CellSize.TINY, 51f),
////        Triple(Pair(428f, 926f), CellSize.SMALL, 51f),
////        Triple(Pair(428f, 926f), CellSize.MEDIUM, 51f),
////        Triple(Pair(428f, 926f), CellSize.LARGE, 51f),
////        Triple(Pair(430f, 932f), CellSize.TINY, 54f),
////        Triple(Pair(430f, 932f), CellSize.SMALL, 52f),
////        Triple(Pair(430f, 932f), CellSize.MEDIUM, 54f),
////        Triple(Pair(430f, 932f), CellSize.LARGE, 52f),
////        Triple(Pair(440f, 956f), CellSize.TINY, 59f),
////        Triple(Pair(440f, 956f), CellSize.SMALL, 59f),
////        Triple(Pair(440f, 956f), CellSize.MEDIUM, 53f),
////        Triple(Pair(440f, 956f), CellSize.LARGE, 57f),
////    )
////
////    var closestPadding = 0f
////    var minDistance = Float.MAX_VALUE
////
////    for (entry in paddingMap.filter { it.second == cellSize }) {
////        val distance = abs(screenWidthDp - entry.first.first) + abs(screenHeightDp - entry.first.second)
////        if (distance < minDistance) {
////            minDistance = distance
////            closestPadding = entry.third
////        }
////    }
////
////    val padding: Float = if (minDistance < 50f) {
////        closestPadding
////    } else {
////        screenWidthDp * 0.1f
////    }
////
////    val minPadding = 20f
////    val maxPadding = screenWidthDp * 0.15f
////    val clampedPadding = max(minPadding, min(padding, maxPadding))
////
////    val available = screenWidthDp - clampedPadding * 2
////    return available * 2 / (columns.toFloat() + 1f)
////}
//
//fun navigationMenuVerticalAdjustment(mazeType: MazeType, cellSize: CellSize, context: Context): Float {
//    val displayMetrics = context.resources.displayMetrics
//    val density = displayMetrics.density
//    val screenWidthDp = displayMetrics.widthPixels.toFloat() / density
//    val screenHeightDp = displayMetrics.heightPixels.toFloat() / density
//    val screenSize = Pair(screenWidthDp, screenHeightDp)
//
//    // Base vertical offset to position menu closer to maze grid
//    val baseOffset = when (mazeType) {
//        MazeType.ORTHOGONAL -> 10f // Reduced from 20f to move the menu higher up
//        MazeType.DELTA -> 30f
//        MazeType.SIGMA -> 30f
//        MazeType.UPSILON -> 10f
//        MazeType.RHOMBIC -> 10f
//    }
//
//    // Adjust offset based on cell size
//    val sizeAdjustment = when (cellSize) {
//        CellSize.TINY -> 0.8f
//        CellSize.SMALL -> 0.9f
//        CellSize.MEDIUM -> 1.0f
//        CellSize.LARGE -> 1.1f
//    }
//
//    // Specific adjustments for RHOMBIC maze type
//    val paddingMap: List<Quadruple<Pair<Float, Float>, MazeType, CellSize, Float>> = listOf(
//        Quadruple(Pair(375f, 812f), MazeType.RHOMBIC, CellSize.TINY, 8f),
//        Quadruple(Pair(390f, 844f), MazeType.RHOMBIC, CellSize.TINY, 6f),
//        Quadruple(Pair(393f, 852f), MazeType.RHOMBIC, CellSize.TINY, 5f),
//        Quadruple(Pair(402f, 874f), MazeType.RHOMBIC, CellSize.TINY, 4f),
//        Quadruple(Pair(414f, 896f), MazeType.RHOMBIC, CellSize.TINY, 2f),
//        Quadruple(Pair(428f, 926f), MazeType.RHOMBIC, CellSize.TINY, 0f),
//        Quadruple(Pair(430f, 932f), MazeType.RHOMBIC, CellSize.TINY, -2f),
//        Quadruple(Pair(440f, 956f), MazeType.RHOMBIC, CellSize.TINY, -4f),
//        Quadruple(Pair(375f, 812f), MazeType.RHOMBIC, CellSize.SMALL, 8f),
//        Quadruple(Pair(390f, 844f), MazeType.RHOMBIC, CellSize.SMALL, 6f),
//        Quadruple(Pair(393f, 852f), MazeType.RHOMBIC, CellSize.SMALL, 5f),
//        Quadruple(Pair(402f, 874f), MazeType.RHOMBIC, CellSize.SMALL, 4f),
//        Quadruple(Pair(414f, 896f), MazeType.RHOMBIC, CellSize.SMALL, 2f),
//        Quadruple(Pair(428f, 926f), MazeType.RHOMBIC, CellSize.SMALL, 0f),
//        Quadruple(Pair(430f, 932f), MazeType.RHOMBIC, CellSize.SMALL, -2f),
//        Quadruple(Pair(440f, 956f), MazeType.RHOMBIC, CellSize.SMALL, -4f),
//        Quadruple(Pair(375f, 812f), MazeType.RHOMBIC, CellSize.MEDIUM, 6f),
//        Quadruple(Pair(390f, 844f), MazeType.RHOMBIC, CellSize.MEDIUM, 4f),
//        Quadruple(Pair(393f, 852f), MazeType.RHOMBIC, CellSize.MEDIUM, 3f),
//        Quadruple(Pair(402f, 874f), MazeType.RHOMBIC, CellSize.MEDIUM, 2f),
//        Quadruple(Pair(414f, 896f), MazeType.RHOMBIC, CellSize.MEDIUM, 0f),
//        Quadruple(Pair(428f, 926f), MazeType.RHOMBIC, CellSize.MEDIUM, -2f),
//        Quadruple(Pair(430f, 932f), MazeType.RHOMBIC, CellSize.MEDIUM, -3f),
//        Quadruple(Pair(440f, 956f), MazeType.RHOMBIC, CellSize.MEDIUM, -5f),
//        Quadruple(Pair(375f, 812f), MazeType.RHOMBIC, CellSize.LARGE, 6f),
//        Quadruple(Pair(390f, 844f), MazeType.RHOMBIC, CellSize.LARGE, 4f),
//        Quadruple(Pair(393f, 852f), MazeType.RHOMBIC, CellSize.LARGE, 3f),
//        Quadruple(Pair(402f, 874f), MazeType.RHOMBIC, CellSize.LARGE, 2f),
//        Quadruple(Pair(414f, 896f), MazeType.RHOMBIC, CellSize.LARGE, 0f),
//        Quadruple(Pair(428f, 926f), MazeType.RHOMBIC, CellSize.LARGE, -2f),
//        Quadruple(Pair(430f, 932f), MazeType.RHOMBIC, CellSize.LARGE, -3f),
//        Quadruple(Pair(440f, 956f), MazeType.RHOMBIC, CellSize.LARGE, -5f),
//    )
//
//    if (mazeType == MazeType.RHOMBIC) {
//        for (entry in paddingMap) {
//            if (entry.second == mazeType &&
//                entry.third == cellSize &&
//                entry.first.first == screenSize.first &&
//                entry.first.second == screenSize.second) {
//                return baseOffset * sizeAdjustment + entry.fourth
//            }
//        }
//    }
//    return baseOffset * sizeAdjustment
//}
//
//data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
//
//fun navigationMenuHorizontalAdjustment(mazeType: MazeType, cellSize: CellSize, context: Context): Float {
//    val displayMetrics = context.resources.displayMetrics
//    val density = displayMetrics.density
//    val screenWidthDp = displayMetrics.widthPixels.toFloat() / density
//    val screenHeightDp = displayMetrics.heightPixels.toFloat() / density
//    val screenSize = Pair(screenWidthDp, screenHeightDp)
//
//    val paddingMap: List<Quadruple<Pair<Float, Float>, MazeType, CellSize, Float>> = listOf(
//        Quadruple(Pair(440f, 956f), MazeType.RHOMBIC, CellSize.LARGE, 6f),
//    )
//
//    if (mazeType == MazeType.RHOMBIC) {
//        for (entry in paddingMap) {
//            if (entry.second == mazeType &&
//                entry.third == cellSize &&
//                entry.first.first == screenSize.first &&
//                entry.first.second == screenSize.second) {
//                return entry.fourth
//            }
//        }
//    }
//    return 0f
//}
//
