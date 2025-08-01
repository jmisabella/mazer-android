package com.jmisabella.mazer.layout

import com.jmisabella.mazer.models.MazeCell
import com.jmisabella.mazer.models.MazeType
import com.jmisabella.mazer.models.CellSize
import android.content.Context
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt


fun computeCellSize(mazeCells: List<MazeCell>, mazeType: MazeType, cellSize: CellSize, context: Context): Float {
    val cols = (mazeCells.maxOfOrNull { it.x } ?: 0) + 1
    val displayMetrics = context.resources.displayMetrics
    val density = displayMetrics.density
    val screenWidthDp = displayMetrics.widthPixels.toFloat() / density
    val screenHeightDp = displayMetrics.heightPixels.toFloat() / density
    return when (mazeType) {
        MazeType.ORTHOGONAL -> screenWidthDp / cols.toFloat()
        MazeType.DELTA -> computeDeltaCellSize(cellSize, cols, screenWidthDp, screenHeightDp)
        MazeType.SIGMA -> {
            val units = 1.5f * (cols - 1).toFloat() + 1f
            screenWidthDp / units
        }
        else -> screenWidthDp / cols.toFloat()
    }
}

fun adjustedCellSize(mazeType: MazeType, cellSize: CellSize, context: Context): Float {
    val displayMetrics = context.resources.displayMetrics
    val density = displayMetrics.density
    val screenWidthDp = displayMetrics.widthPixels.toFloat() / density
    val screenHeightDp = displayMetrics.heightPixels.toFloat() / density
    val screenSize = screenWidthDp to screenHeightDp
    val adjustment: Float = when (mazeType) {
        MazeType.DELTA -> when (cellSize) {
            CellSize.TINY -> 1.07f
            CellSize.SMALL -> 1.36f
            CellSize.MEDIUM -> 1.47f
            CellSize.LARGE -> 1.6f
        }
        MazeType.ORTHOGONAL -> when (cellSize) {
            CellSize.TINY -> 1.2f
            CellSize.SMALL -> 1.3f
            CellSize.MEDIUM -> 1.65f
            CellSize.LARGE -> 1.8f
        }
        MazeType.SIGMA -> when (cellSize) {
            CellSize.TINY -> 0.5f
            CellSize.SMALL -> 0.65f
            CellSize.MEDIUM -> 0.75f
            CellSize.LARGE -> 0.8f
        }
        MazeType.UPSILON -> when (cellSize) {
            CellSize.TINY -> 2.35f
            CellSize.SMALL -> 2.5f
            CellSize.MEDIUM -> 2.85f
            CellSize.LARGE -> 3.3f
        }
        MazeType.RHOMBIC -> when (cellSize) {
            CellSize.TINY -> 0.97f
            CellSize.SMALL -> 1.07f
            CellSize.MEDIUM -> 1.2f
            CellSize.LARGE -> if (screenSize.first == 440f && screenSize.second == 956f ||
                screenSize.first == 414f && screenSize.second == 896f) 1.4f else 1.5f
        }
    }
    val rawSize = cellSize.value.toFloat() // Assuming CellSize has a 'value' property like rawValue in Swift enum
    return adjustment * rawSize
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
    screenWidthDp: Float,
    screenHeightDp: Float
): Float {
    // Define a list of tuples for padding map
    val paddingMap: List<Triple<Pair<Float, Float>, CellSize, Float>> = listOf(
        // iPhone SE 2nd gen, SE 3rd gen
        Triple(Pair(375f, 667f), CellSize.TINY, 46f),
        Triple(Pair(375f, 667f), CellSize.SMALL, 46f),
        Triple(Pair(375f, 667f), CellSize.MEDIUM, 46f),
        Triple(Pair(375f, 667f), CellSize.LARGE, 46f),
        // iPhone Xs, 11 Pro, 12 mini, 13 mini
        Triple(Pair(375f, 812f), CellSize.TINY, 42f),
        Triple(Pair(375f, 812f), CellSize.SMALL, 40f),
        Triple(Pair(375f, 812f), CellSize.MEDIUM, 36f),
        Triple(Pair(375f, 812f), CellSize.LARGE, 36f),
        // iPhone 12, 12 Pro, 13, 13 Pro, 14, 16e
        Triple(Pair(390f, 844f), CellSize.TINY, 42.5f),
        Triple(Pair(390f, 844f), CellSize.SMALL, 44.5f),
        Triple(Pair(390f, 844f), CellSize.MEDIUM, 40.7f),
        Triple(Pair(390f, 844f), CellSize.LARGE, 40.7f),
        // iPhone 14 Pro, 15, 15 Pro
        Triple(Pair(393f, 852f), CellSize.TINY, 43f),
        Triple(Pair(393f, 852f), CellSize.SMALL, 43f),
        Triple(Pair(393f, 852f), CellSize.MEDIUM, 43f),
        Triple(Pair(393f, 852f), CellSize.LARGE, 43f),
        // iPhone 16 Pro
        Triple(Pair(402f, 874f), CellSize.TINY, 45.5f),
        Triple(Pair(402f, 874f), CellSize.SMALL, 47.5f),
        Triple(Pair(402f, 874f), CellSize.MEDIUM, 45f),
        Triple(Pair(402f, 874f), CellSize.LARGE, 48f),
        // iPhone Xr, Xs Max, 11, 11 Pro Max
        Triple(Pair(414f, 896f), CellSize.TINY, 50f),
        Triple(Pair(414f, 896f), CellSize.SMALL, 48f),
        Triple(Pair(414f, 896f), CellSize.MEDIUM, 48.5f),
        Triple(Pair(414f, 896f), CellSize.LARGE, 45f),
        // iPhone 12 Pro Max, 13 Pro Max
        Triple(Pair(428f, 926f), CellSize.TINY, 51f),
        Triple(Pair(428f, 926f), CellSize.SMALL, 51f),
        Triple(Pair(428f, 926f), CellSize.MEDIUM, 51f),
        Triple(Pair(428f, 926f), CellSize.LARGE, 51f),
        // iPhone 14 Pro Max, 15 Pro Max, 15 Plus, 16 Plus
        Triple(Pair(430f, 932f), CellSize.TINY, 54f),
        Triple(Pair(430f, 932f), CellSize.SMALL, 52f),
        Triple(Pair(430f, 932f), CellSize.MEDIUM, 54f),
        Triple(Pair(430f, 932f), CellSize.LARGE, 52f),
        // iPhone 16 Pro Max
        Triple(Pair(440f, 956f), CellSize.TINY, 59f),
        Triple(Pair(440f, 956f), CellSize.SMALL, 59f),
        Triple(Pair(440f, 956f), CellSize.MEDIUM, 53f),
        Triple(Pair(440f, 956f), CellSize.LARGE, 57f),
    )

    // Filter by cellSize
    val filteredMap = paddingMap.filter { it.second == cellSize }
    var closestPadding = 0f
    var minDistance = Float.MAX_VALUE

    for (entry in filteredMap) {
        val distance = abs(screenWidthDp - entry.first.first) + abs(screenHeightDp - entry.first.second)
        if (distance < minDistance) {
            minDistance = distance
            closestPadding = entry.third
        }
    }

    // Determine padding
    val padding: Float = if (minDistance < 50f) {
        closestPadding
    } else {
        screenWidthDp * 0.1f
    }

    // Clamp padding
    val minPadding = 20f
    val maxPadding = screenWidthDp * 0.15f
    val clampedPadding = max(minPadding, min(padding, maxPadding))

    // Calculate available width and return cell size
    val available = screenWidthDp - clampedPadding * 2
    return available * 2 / (columns.toFloat() + 1f)
}

fun navigationMenuVerticalAdjustment(mazeType: MazeType, cellSize: CellSize, context: Context): Float {
    val displayMetrics = context.resources.displayMetrics
    val density = displayMetrics.density
    val screenWidthDp = displayMetrics.widthPixels.toFloat() / density
    val screenHeightDp = displayMetrics.heightPixels.toFloat() / density
    val screenSize = Pair(screenWidthDp, screenHeightDp)

    val paddingMap: List<Quadruple<Pair<Float, Float>, MazeType, CellSize, Float>> = listOf(
        // Note: Commented out SE entries as per Swift code
        Quadruple(Pair(375f, 812f), MazeType.RHOMBIC, CellSize.TINY, -3f),
        Quadruple(Pair(390f, 844f), MazeType.RHOMBIC, CellSize.TINY, -9f),
        Quadruple(Pair(393f, 852f), MazeType.RHOMBIC, CellSize.TINY, -10f),
        Quadruple(Pair(402f, 874f), MazeType.RHOMBIC, CellSize.TINY, -11f),
        Quadruple(Pair(414f, 896f), MazeType.RHOMBIC, CellSize.TINY, -14f),
        Quadruple(Pair(428f, 926f), MazeType.RHOMBIC, CellSize.TINY, -16f),
        Quadruple(Pair(430f, 932f), MazeType.RHOMBIC, CellSize.TINY, -20f),
        Quadruple(Pair(440f, 956f), MazeType.RHOMBIC, CellSize.TINY, -24f),
        //
        Quadruple(Pair(375f, 812f), MazeType.RHOMBIC, CellSize.SMALL, -3f),
        Quadruple(Pair(390f, 844f), MazeType.RHOMBIC, CellSize.SMALL, -9f),
        Quadruple(Pair(393f, 852f), MazeType.RHOMBIC, CellSize.SMALL, -10f),
        Quadruple(Pair(402f, 874f), MazeType.RHOMBIC, CellSize.SMALL, -11f),
        Quadruple(Pair(414f, 896f), MazeType.RHOMBIC, CellSize.SMALL, -14f),
        Quadruple(Pair(428f, 926f), MazeType.RHOMBIC, CellSize.SMALL, -16f),
        Quadruple(Pair(430f, 932f), MazeType.RHOMBIC, CellSize.SMALL, -17f),
        Quadruple(Pair(440f, 956f), MazeType.RHOMBIC, CellSize.SMALL, -24f),
        //
        Quadruple(Pair(375f, 812f), MazeType.RHOMBIC, CellSize.MEDIUM, -14f),
        Quadruple(Pair(390f, 844f), MazeType.RHOMBIC, CellSize.MEDIUM, -12f),
        Quadruple(Pair(393f, 852f), MazeType.RHOMBIC, CellSize.MEDIUM, -14f),
        Quadruple(Pair(402f, 874f), MazeType.RHOMBIC, CellSize.MEDIUM, -11f),
        Quadruple(Pair(414f, 896f), MazeType.RHOMBIC, CellSize.MEDIUM, -24f),
        Quadruple(Pair(428f, 926f), MazeType.RHOMBIC, CellSize.MEDIUM, -22f),
        Quadruple(Pair(430f, 932f), MazeType.RHOMBIC, CellSize.MEDIUM, -21f),
        Quadruple(Pair(440f, 956f), MazeType.RHOMBIC, CellSize.MEDIUM, -29f),
        //
        Quadruple(Pair(375f, 812f), MazeType.RHOMBIC, CellSize.LARGE, -14f),
        Quadruple(Pair(390f, 844f), MazeType.RHOMBIC, CellSize.LARGE, -12f),
        Quadruple(Pair(393f, 852f), MazeType.RHOMBIC, CellSize.LARGE, -14f),
        Quadruple(Pair(402f, 874f), MazeType.RHOMBIC, CellSize.LARGE, -16f),
        Quadruple(Pair(414f, 896f), MazeType.RHOMBIC, CellSize.LARGE, -26f),
        Quadruple(Pair(428f, 926f), MazeType.RHOMBIC, CellSize.LARGE, -26f),
        Quadruple(Pair(430f, 932f), MazeType.RHOMBIC, CellSize.LARGE, -24f),
        Quadruple(Pair(440f, 956f), MazeType.RHOMBIC, CellSize.LARGE, -30f),
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

// Helper data class for quadruple (since Kotlin stdlib has up to Triple)
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