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
//            CellSize.TINY -> 1.6f
            CellSize.SMALL -> 1.85f
            CellSize.MEDIUM -> 2.2f
            CellSize.LARGE -> 2.5f
        }
        MazeType.ORTHOGONAL -> when (cellSize) {
//            CellSize.TINY -> 1.2f
            CellSize.SMALL -> 1.3f
            CellSize.MEDIUM -> 1.65f
            CellSize.LARGE -> 1.8f
        }
        MazeType.SIGMA -> when (cellSize) {
//            CellSize.TINY -> 0.63f
//            CellSize.SMALL -> 0.73f
//            CellSize.MEDIUM -> 0.85f
//            CellSize.LARGE -> 1.1f
//            CellSize.TINY -> 0.73f
            CellSize.SMALL -> 0.8f
            CellSize.MEDIUM -> 0.85f
            CellSize.LARGE -> 0.9f
        }
        MazeType.UPSILON -> when (cellSize) {
//            CellSize.TINY -> 2.35f
            CellSize.SMALL -> 2.5f
            CellSize.MEDIUM -> 2.85f
            CellSize.LARGE -> 3.3f
        }
        MazeType.RHOMBIC -> when (cellSize) {
//            CellSize.TINY -> 1.45f
            CellSize.SMALL -> 1.65f
            CellSize.MEDIUM -> 2.0f
            CellSize.LARGE -> if (screenSize.first == 440f && screenSize.second == 956f ||
                screenSize.first == 414f && screenSize.second == 896f) 2.4f else 2.5f
        }
    }
    val rawSize = cellSize.value.toFloat()
    return adjustment * rawSize
}

fun computeCellSize(mazeCells: List<MazeCell>, mazeType: MazeType, cellSize: CellSize, context: Context, availableHeightDp: Float? = null): Float {
    val cols = (mazeCells.maxOfOrNull { it.x } ?: 0) + 1
    val rows = (mazeCells.maxOfOrNull { it.y } ?: 0) + 1
    val displayMetrics = context.resources.displayMetrics
    val density = displayMetrics.density
    val screenWidthDp = displayMetrics.widthPixels.toFloat() / density
    val screenHeightDp = displayMetrics.heightPixels.toFloat() / density

    val statusResourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
    val statusBarHeightPx = if (statusResourceId > 0) context.resources.getDimensionPixelSize(statusResourceId) else 0
    val statusBarHeightDp = statusBarHeightPx.toFloat() / density

    val navResourceId = context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
    var navBarHeightPx = if (navResourceId > 0) context.resources.getDimensionPixelSize(navResourceId) else 0
    var navBarHeightDp = navBarHeightPx.toFloat() / density

    val configNavId = context.resources.getIdentifier("config_navBarInteractionMode", "integer", "android")
    val navMode = if (configNavId > 0) context.resources.getInteger(configNavId) else 0
    if (navMode == 2) {
        navBarHeightDp = 0f
    }

    val menuVerticalAdj = navigationMenuVerticalAdjustment(mazeType, cellSize, context)
    val estimatedRowHeight = 48f  // Existing estimate for navigation menu row
    val estimatedBottomPadding = 20f  // NEW: Account for explicit bottom padding in Compose layout

    val overhead = statusBarHeightDp + menuVerticalAdj + estimatedRowHeight + navBarHeightDp + estimatedBottomPadding
    val effectiveAvailableHeightDp = availableHeightDp ?: (screenHeightDp - overhead)

    return when (mazeType) {
//        MazeType.ORTHOGONAL -> {
//            val cellFromHeight = (effectiveAvailableHeightDp - 8f) / rows.toFloat()  // NEW: Subtract approx border height (strokeWidth ~4dp top/bottom)
//            val cellFromWidth = screenWidthDp / cols.toFloat()
//            min(cellFromWidth, cellFromHeight)
//        }
        MazeType.ORTHOGONAL -> {
            val cellFromHeight = (effectiveAvailableHeightDp - 8f) / rows.toFloat()  // NEW: Subtract approx border height (strokeWidth ~4dp top/bottom)
            val cellFromWidth = screenWidthDp / cols.toFloat()
//            min(min(cellFromWidth, cellFromHeight), adjustedCellSize(mazeType, cellSize, context))
            min(min(cellFromWidth, cellFromHeight), adjustedCellSize(mazeType, cellSize, context))
        }
        MazeType.DELTA -> {
            val cellFromWidth = screenWidthDp * 2f / (cols + 1f)
            val cellFromHeight = effectiveAvailableHeightDp * 2f / sqrt(3f) / rows.toFloat()
            min(cellFromWidth, cellFromHeight)
        }
        MazeType.SIGMA -> {
            val cellFromWidth = screenWidthDp / (1.5f * cols + 0.5f)
            val cellFromHeight = effectiveAvailableHeightDp / (sqrt(3f) * (rows.toFloat() + 0.5f))
            min(cellFromWidth, cellFromHeight)
        }
        MazeType.UPSILON -> {
            val cellFromWidth = screenWidthDp / cols.toFloat()
            val cellFromHeight = effectiveAvailableHeightDp / rows.toFloat()
            min(cellFromWidth, cellFromHeight)
        }
        MazeType.RHOMBIC -> {
            val halfDiag = 1f / sqrt(2f)
            val cellFromWidth = screenWidthDp / (halfDiag * cols.toFloat() + sqrt(2f))
            val cellFromHeight = effectiveAvailableHeightDp / (halfDiag * rows.toFloat() + sqrt(2f))
            min(cellFromWidth, cellFromHeight)
        }
    }
}

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
//        CellSize.TINY -> 0.35f
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
//        CellSize.TINY -> 0.8f
        CellSize.SMALL -> 0.9f
        CellSize.MEDIUM -> 1.0f
        CellSize.LARGE -> 1.1f
    }

    // Specific adjustments for RHOMBIC maze type
    val paddingMap: List<Quadruple<Pair<Float, Float>, MazeType, CellSize, Float>> = listOf(
//        Quadruple(Pair(375f, 812f), MazeType.RHOMBIC, CellSize.TINY, 8f),
//        Quadruple(Pair(390f, 844f), MazeType.RHOMBIC, CellSize.TINY, 6f),
//        Quadruple(Pair(393f, 852f), MazeType.RHOMBIC, CellSize.TINY, 5f),
//        Quadruple(Pair(402f, 874f), MazeType.RHOMBIC, CellSize.TINY, 4f),
//        Quadruple(Pair(414f, 896f), MazeType.RHOMBIC, CellSize.TINY, 2f),
//        Quadruple(Pair(428f, 926f), MazeType.RHOMBIC, CellSize.TINY, 0f),
//        Quadruple(Pair(430f, 932f), MazeType.RHOMBIC, CellSize.TINY, -2f),
//        Quadruple(Pair(440f, 956f), MazeType.RHOMBIC, CellSize.TINY, -4f),
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

