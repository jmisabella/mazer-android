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

// Helper function for octagon path (translated from OctagonShape)
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
//import androidx.compose.foundation.layout.size
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
//import com.jmisabella.mazer.models.MazeCell
//import kotlin.math.min
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
//    // convert dp→px once:
//    val gridPx = with(density) { gridCellSize.dp.toPx() }
//    val squarePx = with(density) { squareSize.dp.toPx() }
//    val overlapPx = 1f // 1px bleed so your shapes butt up
//    val strokePx = gridPx / 12f // exactly as you had, but now in px
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
//        // here you’ll need to recalc your “points” in px as well:
//        val pts = if (isSquare) {
//            // 4 corners of your square, in px
//            val i = (gridPx - squarePx) / 2f
//            listOf(
//                Offset(i, i),
//                Offset(i + squarePx, i),
//                Offset(i + squarePx, i + squarePx),
//                Offset(i, i + squarePx)
//            )
//        } else {
//            // your 8 octagon vertices, using gridPx and k, in px
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
//                val (a, b) = pair
//                drawLine(
//                    color = Color.Black,
//                    start = pts[a],
//                    end = pts[b],
//                    strokeWidth = strokePx,
//                    cap = StrokeCap.Butt
//                )
//            }
//        }
//    }
//}
//
//// Helper function for octagon path (translated from OctagonShape)
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
////import androidx.compose.foundation.background
////import androidx.compose.foundation.layout.*
////import androidx.compose.runtime.*
////import androidx.compose.ui.Modifier
////import androidx.compose.ui.geometry.Offset
////import androidx.compose.ui.geometry.Rect
////import androidx.compose.ui.graphics.Color
////import androidx.compose.ui.graphics.Path
////import androidx.compose.ui.graphics.StrokeCap
////import androidx.compose.ui.graphics.drawscope.DrawStyle
////import androidx.compose.ui.graphics.drawscope.Fill
////import androidx.compose.ui.graphics.drawscope.Stroke
////import androidx.compose.ui.platform.LocalDensity
////import androidx.compose.ui.unit.dp
////import com.jmisabella.mazer.models.*
////import kotlin.math.*
////
////@Composable
////fun UpsilonCell(
////    cell: MazeCell,
////    gridCellSize: Float,
////    squareSize: Float,
////    isSquare: Boolean,
////    fillColor: Color,
////    optionalColor: Color?,
////    modifier: Modifier = Modifier
////) {
////    val density = LocalDensity.current.density
////    val overlap = 1f / density
////    val strokeWidth = gridCellSize / 12f // Approximate stroke width
////
////    Box(modifier = modifier) {
////        if (isSquare) {
////            val adjustedSize = squareSize + 2 * overlap
////            val adjustedOffset = (gridCellSize - adjustedSize) / 2f
////            Box(
////                modifier = Modifier
////                    .offset(x = adjustedOffset.dp, y = adjustedOffset.dp)
////                    .size(adjustedSize.dp)
////                    .background(fillColor)
////            )
////        } else {
////            Canvas(modifier = Modifier.fillMaxSize()) {
////                val path = octagonPath(Rect(0f, 0f, size.width, size.height), overlap)
////                drawPath(
////                    path = path,
////                    color = fillColor,
////                    style = Fill
////                )
////            }
////        }
////
////        Canvas(modifier = Modifier.fillMaxSize()) {
////            if (isSquare) {
////                val offset = (gridCellSize - squareSize) / 2f
////                val points = listOf(
////                    Offset(offset, offset),
////                    Offset(offset + squareSize, offset),
////                    Offset(offset + squareSize, offset + squareSize),
////                    Offset(offset, offset + squareSize)
////                )
////                val directions = mapOf(
////                    "Up" to (0 to 1),
////                    "Right" to (1 to 2),
////                    "Down" to (2 to 3),
////                    "Left" to (3 to 0)
////                )
////                for ((dir, pair) in directions) {
////                    if (!cell.linked.contains(dir)) {
////                        val (startIdx, endIdx) = pair
////                        drawLine(
////                            color = Color.Black,
////                            start = points[startIdx],
////                            end = points[endIdx],
////                            strokeWidth = strokeWidth,
////                            cap = StrokeCap.Butt
////                        )
////                    }
////                }
////            } else {
////                val s = gridCellSize
////                val cx = s / 2f
////                val cy = s / 2f
////                val r = s / 2f
////                val k = (2f * r) / (2f + sqrt(2f))
////                val points = listOf(
////                    Offset(cx - r + k, cy - r),
////                    Offset(cx + r - k, cy - r),
////                    Offset(cx + r, cy - r + k),
////                    Offset(cx + r, cy + r - k),
////                    Offset(cx + r - k, cy + r),
////                    Offset(cx - r + k, cy + r),
////                    Offset(cx - r, cy + r - k),
////                    Offset(cx - r, cy - r + k)
////                )
////                val directions = mapOf(
////                    "Up" to (0 to 1),
////                    "UpperRight" to (1 to 2),
////                    "Right" to (2 to 3),
////                    "LowerRight" to (3 to 4),
////                    "Down" to (4 to 5),
////                    "LowerLeft" to (5 to 6),
////                    "Left" to (6 to 7),
////                    "UpperLeft" to (7 to 0)
////                )
////                for ((dir, pair) in directions) {
////                    if (!cell.linked.contains(dir)) {
////                        val (startIdx, endIdx) = pair
////                        drawLine(
////                            color = Color.Black,
////                            start = points[startIdx],
////                            end = points[endIdx],
////                            strokeWidth = strokeWidth,
////                            cap = StrokeCap.Butt
////                        )
////                    }
////                }
////            }
////        }
////    }
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
