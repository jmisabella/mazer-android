package com.jmisabella.mazer.screens.mazecomponents

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import com.jmisabella.mazer.models.MazeCell
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

@Composable
fun UpsilonCell(
    cell: MazeCell,
    gridCellSize: Float, // this is in dp
    squareSize: Float, // this is also dp
    isSquare: Boolean,
    fillColor: Color,
    optionalColor: Color?,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    // convert dp→px once:
    val gridPx = with(density) { gridCellSize.dp.toPx() }
    val squarePx = with(density) { squareSize.dp.toPx() }
    val overlapPx = 1f // 1px bleed so your shapes butt up
    val strokePx = gridPx / 12f // exactly as you had, but now in px

    Canvas(modifier = modifier.size(gridCellSize.dp)) {
        // 1) draw the background (either square or octagon)
        if (isSquare) {
            val inset = (gridPx - squarePx) / 2f
            drawRect(
                color = fillColor,
                topLeft = Offset(inset - overlapPx, inset - overlapPx),
                size = Size(squarePx + overlapPx * 2, squarePx + overlapPx * 2)
            )
        } else {
            val path = octagonPath(
                rect = Rect(0f, 0f, gridPx, gridPx),
                overlap = overlapPx
            )
            drawPath(path, fillColor, style = Fill)
        }
        // 2) draw the walls
        // here you’ll need to recalc your “points” in px as well:
        val pts = if (isSquare) {
            // 4 corners of your square, in px
            val i = (gridPx - squarePx) / 2f
            listOf(
                Offset(i, i),
                Offset(i + squarePx, i),
                Offset(i + squarePx, i + squarePx),
                Offset(i, i + squarePx)
            )
        } else {
            // your 8 octagon vertices, using gridPx and k, in px
            val r = gridPx / 2f
            val cx = r; val cy = r
            val k = 2 * r / (2 + sqrt(2f))
            listOf(
                Offset(cx - r + k, cy - r),
                Offset(cx + r - k, cy - r),
                Offset(cx + r, cy - r + k),
                Offset(cx + r, cy + r - k),
                Offset(cx + r - k, cy + r),
                Offset(cx - r + k, cy + r),
                Offset(cx - r, cy + r - k),
                Offset(cx - r, cy - r + k)
            )
        }
        val directions = if (isSquare) mapOf(
            "Up" to (0 to 1),
            "Right" to (1 to 2),
            "Down" to (2 to 3),
            "Left" to (3 to 0)
        ) else mapOf(
            "Up" to (0 to 1),
            "UpperRight" to (1 to 2),
            "Right" to (2 to 3),
            "LowerRight" to (3 to 4),
            "Down" to (4 to 5),
            "LowerLeft" to (5 to 6),
            "Left" to (6 to 7),
            "UpperLeft" to (7 to 0)
        )
        for ((dir, pair) in directions) {
            if (!cell.linked.contains(dir)) {
                val (a, b) = pair
                drawLine(
                    color = Color.Black,
                    start = pts[a],
                    end = pts[b],
                    strokeWidth = strokePx,
                    cap = StrokeCap.Butt
                )
            }
        }
    }
}

//@Composable
//fun UpsilonCell(
//    cell: MazeCell,
//    gridCellSize: Float, // this is in dp
//    squareSize: Float, // this is also dp
//    isSquare: Boolean,
//    fillColor: Color,
//    optionalColor: Color?,
//    modifier: Modifier = Modifier
//) {
//    val density = LocalDensity.current
//    val gridPx = with(density) { gridCellSize.dp.toPx() }
//    val squarePx = with(density) { squareSize.dp.toPx() }
//    val overlapPx = gridPx / 20f
//    val strokePx = gridPx / 16f
//
//    Canvas(modifier = modifier.size(gridCellSize.dp)) {
//        // 1) draw the background (either square or octagon)
//        if (isSquare) {
//            val inset = (gridPx - squarePx) / 2f
//            drawRect(
//                color = fillColor,
//                topLeft = Offset(inset - overlapPx, inset - overlapPx),
//                size = Size(squarePx + overlapPx * 2, squarePx + overlapPx * 2)
//            )
//        } else {
//            val path = octagonPath(
//                rect = Rect(0f, 0f, gridPx, gridPx),
//                overlap = overlapPx
//            )
//            drawPath(path, fillColor, style = Fill)
//        }
//
//        // 2) draw the walls
//        val pts = if (isSquare) {
//            val i = (gridPx - squarePx) / 2f
//            listOf(
//                Offset(i, i),
//                Offset(i + squarePx, i),
//                Offset(i + squarePx, i + squarePx),
//                Offset(i, i + squarePx)
//            )
//        } else {
//            val r = gridPx / 2f
//            val cx = r; val cy = r
//            val k = 2 * r / (2 + sqrt(2f))
//            listOf(
//                Offset(cx - r + k, cy - r),
//                Offset(cx + r - k, cy - r),
//                Offset(cx + r, cy - r + k),
//                Offset(cx + r, cy + r - k),
//                Offset(cx + r - k, cy + r),
//                Offset(cx - r + k, cy + r),
//                Offset(cx - r, cy + r - k),
//                Offset(cx - r, cy - r + k)
//            )
//        }
//
//        // Round points to nearest pixel
//        val roundedPts = pts.map {
//            Offset(
//                it.x.roundToInt().toFloat(),
//                it.y.roundToInt().toFloat()
//            )
//        }
//
//        val directions = if (isSquare) mapOf(
//            "Up" to (0 to 1),
//            "Right" to (1 to 2),
//            "Down" to (2 to 3),
//            "Left" to (3 to 0)
//        ) else mapOf(
//            "Up" to (0 to 1),
//            "UpperRight" to (1 to 2),
//            "Right" to (2 to 3),
//            "LowerRight" to (3 to 4),
//            "Down" to (4 to 5),
//            "LowerLeft" to (5 to 6),
//            "Left" to (6 to 7),
//            "UpperLeft" to (7 to 0)
//        )
//
//        val passes = if (gridPx > 100f) 2 else 1  // Multiple passes for larger cells to fill any tiny antialiasing gaps
//
//        for ((dir, pair) in directions) {
//            if (!cell.linked.contains(dir)) {
//                val (startIdx, endIdx) = pair
//                val start = roundedPts[startIdx]
//                val end = roundedPts[endIdx]
//                repeat(passes) {
//                    drawLine(
//                        color = Color.Black,
//                        start = start,
//                        end = end,
//                        strokeWidth = strokePx,
//                        cap = StrokeCap.Butt  // Butt works better with rounding for gap handling
//                    )
//                }
//            }
//        }
//    }
//}

private fun octagonPath(rect: Rect, overlap: Float): Path {
    val cx = rect.center.x
    val cy = rect.center.y
    val r = min(rect.width, rect.height) / 2f + overlap
    val k = (2f * r) / (2f + sqrt(2f))
    val points = listOf(
        Offset(cx - r + k, cy - r),
        Offset(cx + r - k, cy - r),
        Offset(cx + r, cy - r + k),
        Offset(cx + r, cy + r - k),
        Offset(cx + r - k, cy + r),
        Offset(cx - r + k, cy + r),
        Offset(cx - r, cy + r - k),
        Offset(cx - r, cy - r + k)
    )
    return Path().apply {
        moveTo(points[0].x, points[0].y)
        points.drop(1).forEach { lineTo(it.x, it.y) }
        close()
    }
}

//package com.jmisabella.mazer.screens.mazecomponents
//
//import androidx.compose.foundation.Canvas
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.geometry.Rect
//import androidx.compose.ui.geometry.Size
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.Path
//import androidx.compose.ui.graphics.StrokeCap
//import androidx.compose.ui.graphics.drawscope.Fill
//import androidx.compose.ui.platform.LocalDensity
//import androidx.compose.ui.unit.dp
//import androidx.compose.foundation.layout.size
//import com.jmisabella.mazer.models.MazeCell
//import kotlin.math.min
//import kotlin.math.roundToInt
//import kotlin.math.sqrt
//
//@Composable
//fun UpsilonCell(
//    cell: MazeCell,
//    gridCellSize: Float, // this is in dp
//    squareSize: Float, // this is also dp
//    isSquare: Boolean,
//    fillColor: Color,
//    optionalColor: Color?,
//    modifier: Modifier = Modifier
//) {
//    val density = LocalDensity.current
//    val gridPx = with(density) { gridCellSize.dp.toPx() }
//    val squarePx = with(density) { squareSize.dp.toPx() }
//    val overlapPx = gridPx / 24f  // Increased for more bleed on large cells to close hair-thin gaps
//    val strokePx = gridPx / 16f
//
//    Canvas(modifier = modifier.size(gridCellSize.dp)) {
//        // 1) draw the background (either square or octagon)
//        if (isSquare) {
//            val inset = (gridPx - squarePx) / 2f
//            drawRect(
//                color = fillColor,
//                topLeft = Offset(inset - overlapPx, inset - overlapPx),
//                size = Size(squarePx + overlapPx * 2, squarePx + overlapPx * 2)
//            )
//        } else {
//            val path = octagonPath(
//                rect = Rect(0f, 0f, gridPx, gridPx),
//                overlap = overlapPx
//            )
//            drawPath(path, fillColor, style = Fill)
//        }
//
//        // 2) draw the walls
//        val pts = if (isSquare) {
//            val i = (gridPx - squarePx) / 2f
//            listOf(
//                Offset(i, i),
//                Offset(i + squarePx, i),
//                Offset(i + squarePx, i + squarePx),
//                Offset(i, i + squarePx)
//            )
//        } else {
//            val r = gridPx / 2f
//            val cx = r; val cy = r
//            val k = 2 * r / (2 + sqrt(2f))
//            listOf(
//                Offset(cx - r + k, cy - r),
//                Offset(cx + r - k, cy - r),
//                Offset(cx + r, cy - r + k),
//                Offset(cx + r, cy + r - k),
//                Offset(cx + r - k, cy + r),
//                Offset(cx - r + k, cy + r),
//                Offset(cx - r, cy + r - k),
//                Offset(cx - r, cy - r + k)
//            )
//        }
//
//        // Round points to nearest pixel
//        val roundedPts = pts.map {
//            Offset(
//                it.x.roundToInt().toFloat(),
//                it.y.roundToInt().toFloat()
//            )
//        }
//
//        val directions = if (isSquare) mapOf(
//            "Up" to (0 to 1),
//            "Right" to (1 to 2),
//            "Down" to (2 to 3),
//            "Left" to (3 to 0)
//        ) else mapOf(
//            "Up" to (0 to 1),
//            "UpperRight" to (1 to 2),
//            "Right" to (2 to 3),
//            "LowerRight" to (3 to 4),
//            "Down" to (4 to 5),
//            "LowerLeft" to (5 to 6),
//            "Left" to (6 to 7),
//            "UpperLeft" to (7 to 0)
//        )
//
//        for ((dir, pair) in directions) {
//            if (!cell.linked.contains(dir)) {
//                val (startIdx, endIdx) = pair
//                drawLine(
//                    color = Color.Black,
//                    start = roundedPts[startIdx],
//                    end = roundedPts[endIdx],
//                    strokeWidth = strokePx,
//                    cap = StrokeCap.Round  // Round ends to fill corner/edge gaps
//                )
//            }
//        }
//    }
//}
//
//private fun octagonPath(rect: Rect, overlap: Float): Path {
//    val cx = rect.center.x
//    val cy = rect.center.y
//    val r = min(rect.width, rect.height) / 2f + overlap
//    val k = (2f * r) / (2f + sqrt(2f))
//    val points = listOf(
//        Offset(cx - r + k, cy - r),
//        Offset(cx + r - k, cy - r),
//        Offset(cx + r, cy - r + k),
//        Offset(cx + r, cy + r - k),
//        Offset(cx + r - k, cy + r),
//        Offset(cx - r + k, cy + r),
//        Offset(cx - r, cy + r - k),
//        Offset(cx - r, cy - r + k)
//    )
//    return Path().apply {
//        moveTo(points[0].x, points[0].y)
//        points.drop(1).forEach { lineTo(it.x, it.y) }
//        close()
//    }
//}
//
////package com.jmisabella.mazer.screens.mazecomponents
////
////import androidx.compose.foundation.Canvas
////import androidx.compose.runtime.Composable
////import androidx.compose.ui.Modifier
////import androidx.compose.ui.geometry.Offset
////import androidx.compose.ui.geometry.Rect
////import androidx.compose.ui.geometry.Size
////import androidx.compose.ui.graphics.Color
////import androidx.compose.ui.graphics.Path
////import androidx.compose.ui.graphics.StrokeCap
////import androidx.compose.ui.graphics.drawscope.Fill
////import androidx.compose.ui.platform.LocalDensity
////import androidx.compose.ui.unit.dp
////import androidx.compose.foundation.layout.size
////import com.jmisabella.mazer.models.MazeCell
////import kotlin.math.min
////import kotlin.math.roundToInt
////import kotlin.math.sqrt
////
////@Composable
////fun UpsilonCell(
////    cell: MazeCell,
////    gridCellSize: Float, // this is in dp
////    squareSize: Float, // this is also dp
////    isSquare: Boolean,
////    fillColor: Color,
////    optionalColor: Color?,
////    modifier: Modifier = Modifier
////) {
////    val density = LocalDensity.current
////    val gridPx = with(density) { gridCellSize.dp.toPx() }
////    val squarePx = with(density) { squareSize.dp.toPx() }
////    val overlapPx = gridPx / 28f  // Keep as-is; tunes ~5px for LARGE, ~2px for SMALL
////    val strokePx = gridPx / 16f  // Increased divisor for thinner strokes on large cells
////
////    Canvas(modifier = modifier.size(gridCellSize.dp)) {
////        // 1) draw the background (either square or octagon) - unchanged
////        if (isSquare) {
////            val inset = (gridPx - squarePx) / 2f
////            drawRect(
////                color = fillColor,
////                topLeft = Offset(inset - overlapPx, inset - overlapPx),
////                size = Size(squarePx + overlapPx * 2, squarePx + overlapPx * 2)
////            )
////        } else {
////            val path = octagonPath(
////                rect = Rect(0f, 0f, gridPx, gridPx),
////                overlap = overlapPx
////            )
////            drawPath(path, fillColor, style = Fill)
////        }
////
////        // 2) draw the walls
////        val pts = if (isSquare) {
////            val i = (gridPx - squarePx) / 2f
////            listOf(
////                Offset(i, i),
////                Offset(i + squarePx, i),
////                Offset(i + squarePx, i + squarePx),
////                Offset(i, i + squarePx)
////            )
////        } else {
////            val r = gridPx / 2f
////            val cx = r; val cy = r
////            val k = 2 * r / (2 + sqrt(2f))
////            listOf(
////                Offset(cx - r + k, cy - r),
////                Offset(cx + r - k, cy - r),
////                Offset(cx + r, cy - r + k),
////                Offset(cx + r, cy + r - k),
////                Offset(cx + r - k, cy + r),
////                Offset(cx - r + k, cy + r),
////                Offset(cx - r, cy + r - k),
////                Offset(cx - r, cy - r + k)
////            )
////        }
////
////        // Round points to nearest pixel to fix antialiasing gaps at line ends/corners
////        val roundedPts = pts.map {
////            Offset(
////                it.x.roundToInt().toFloat(),
////                it.y.roundToInt().toFloat()
////            )
////        }
////
////        val directions = if (isSquare) mapOf(
////            "Up" to (0 to 1),
////            "Right" to (1 to 2),
////            "Down" to (2 to 3),
////            "Left" to (3 to 0)
////        ) else mapOf(
////            "Up" to (0 to 1),
////            "UpperRight" to (1 to 2),
////            "Right" to (2 to 3),
////            "LowerRight" to (3 to 4),
////            "Down" to (4 to 5),
////            "LowerLeft" to (5 to 6),
////            "Left" to (6 to 7),
////            "UpperLeft" to (7 to 0)
////        )
////
////        for ((dir, pair) in directions) {
////            if (!cell.linked.contains(dir)) {
////                val (startIdx, endIdx) = pair
////                drawLine(
////                    color = Color.Black,
////                    start = roundedPts[startIdx],
////                    end = roundedPts[endIdx],
////                    strokeWidth = strokePx,
////                    cap = StrokeCap.Butt  // Keep Butt; rounding handles gaps better than Square here
////                )
////            }
////        }
////    }
////
//////    val density = LocalDensity.current
//////    // convert dp→px once:
//////    val gridPx = with(density) { gridCellSize.dp.toPx() }
//////    val squarePx = with(density) { squareSize.dp.toPx() }
////////    val overlapPx = 1f // 1px bleed so your shapes butt up
//////    val overlapPx = gridPx / 28f  // Tuned multiplier: ~5 for large (~140px grid), ~3 for medium (~84px), ~2 for small (~56px)
//////    val strokePx = gridPx / 12f
//////
//////    Canvas(modifier = modifier.size(gridCellSize.dp)) {
//////        // 1) draw the background (either square or octagon)
//////        if (isSquare) {
//////            val inset = (gridPx - squarePx) / 2f
//////            drawRect(
//////                color = fillColor,
//////                topLeft = Offset(inset - overlapPx, inset - overlapPx),
//////                size = Size(squarePx + overlapPx * 2, squarePx + overlapPx * 2)
//////            )
//////        } else {
//////            val path = octagonPath(
//////                rect = Rect(0f, 0f, gridPx, gridPx),
//////                overlap = overlapPx
//////            )
//////            drawPath(path, fillColor, style = Fill)
//////        }
//////
//////        // 2) draw the walls
//////        // here you’ll need to recalc your “points” in px as well:
//////        val pts = if (isSquare) {
//////            // 4 corners of your square, in px
//////            val i = (gridPx - squarePx) / 2f
//////            listOf(
//////                Offset(i, i),
//////                Offset(i + squarePx, i),
//////                Offset(i + squarePx, i + squarePx),
//////                Offset(i, i + squarePx)
//////            )
//////        } else {
//////            // your 8 octagon vertices, using gridPx and k, in px
//////            val r = gridPx / 2f
//////            val cx = r; val cy = r
//////            val k = 2 * r / (2 + sqrt(2f))
//////            listOf(
//////                Offset(cx - r + k, cy - r),
//////                Offset(cx + r - k, cy - r),
//////                Offset(cx + r, cy - r + k),
//////                Offset(cx + r, cy + r - k),
//////                Offset(cx + r - k, cy + r),
//////                Offset(cx - r + k, cy + r),
//////                Offset(cx - r, cy + r - k),
//////                Offset(cx - r, cy - r + k)
//////            )
//////        }
//////
//////        val directions = if (isSquare) mapOf(
//////            "Up" to (0 to 1),
//////            "Right" to (1 to 2),
//////            "Down" to (2 to 3),
//////            "Left" to (3 to 0)
//////        ) else mapOf(
//////            "Up" to (0 to 1),
//////            "UpperRight" to (1 to 2),
//////            "Right" to (2 to 3),
//////            "LowerRight" to (3 to 4),
//////            "Down" to (4 to 5),
//////            "LowerLeft" to (5 to 6),
//////            "Left" to (6 to 7),
//////            "UpperLeft" to (7 to 0)
//////        )
//////
//////        for ((dir, pair) in directions) {
//////            if (!cell.linked.contains(dir)) {
//////                val (a, b) = pair
//////                drawLine(
//////                    color = Color.Black,
//////                    start = pts[a],
//////                    end = pts[b],
//////                    strokeWidth = strokePx,
////////                    cap = StrokeCap.Butt
//////                    cap = StrokeCap.Square
//////                )
//////            }
//////        }
//////    }
////}
////
////// Helper function for octagon path (translated from OctagonShape)
////private fun octagonPath(rect: Rect, overlap: Float): Path {
////    val cx = rect.center.x
////    val cy = rect.center.y
////    val r = min(rect.width, rect.height) / 2f + overlap
////    val k = (2f * r) / (2f + sqrt(2f))
////    val points = listOf(
////        Offset(cx - r + k, cy - r),
////        Offset(cx + r - k, cy - r),
////        Offset(cx + r, cy - r + k),
////        Offset(cx + r, cy + r - k),
////        Offset(cx + r - k, cy + r),
////        Offset(cx - r + k, cy + r),
////        Offset(cx - r, cy + r - k),
////        Offset(cx - r, cy - r + k)
////    )
////    return Path().apply {
////        moveTo(points[0].x, points[0].y)
////        points.drop(1).forEach { lineTo(it.x, it.y) }
////        close()
////    }
////}
////
