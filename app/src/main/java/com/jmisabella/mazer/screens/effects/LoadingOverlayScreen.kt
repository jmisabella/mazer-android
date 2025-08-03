package com.jmisabella.mazer.screens.effects

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.jmisabella.mazer.R
import com.jmisabella.mazer.layout.CellColors
import com.jmisabella.mazer.models.MazeAlgorithm

@Composable
fun LoadingOverlay(algorithm: MazeAlgorithm) {
    val isDark = isSystemInDarkTheme()
    val fontScale = if (LocalConfiguration.current.screenWidthDp > 700) 1.3f else 1f

    val darkModeGIFs = listOf(R.drawable.loading_blue, R.drawable.loading_purple)
    val lightModeGIFs = listOf(R.drawable.loading_orange, R.drawable.loading_red_orange)
    val gifRes = if (isDark) darkModeGIFs.random() else lightModeGIFs.random()

    val foregroundColor = if (isDark) Color.White else Color.Black
    val backgroundColor = if (isDark) Color.Black else CellColors.offWhite

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val margin = 48.dp // Slight margin on each side
    val maxContentWidth = (screenWidth - (margin * 2)).coerceIn(300.dp, 360.dp) // Ensure min 300, max 360, with margins

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .background(backgroundColor, shape = RoundedCornerShape(12.dp))
                .padding(vertical = 32.dp, horizontal = 32.dp)
                .widthIn(min = 300.dp, max = maxContentWidth)
                .heightIn(min = 300.dp)
                .padding(horizontal = 16.dp), // Additional inner padding for text readability
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            AndroidView(
                factory = { context ->
                    ImageView(context).apply {
                        Glide.with(context)
                            .asGif()
                            .load(gifRes)
                            .into(this)
                    }
                },
                modifier = Modifier
                    .size(120.dp)
                    .offset(y = 25.dp)
            )

            Text(
                text = "Generating Maze",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = MaterialTheme.typography.titleLarge.fontSize * fontScale,
                    color = foregroundColor
                )
            )

            Text(
                text = algorithm.displayName,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = MaterialTheme.typography.headlineSmall.fontSize * fontScale,
                    color = foregroundColor
                )
            )

            Text(
                text = algorithm.description,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize * fontScale,
                    color = foregroundColor
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

//@Composable
//fun LoadingOverlay(algorithm: MazeAlgorithm) {
//    val isDark = isSystemInDarkTheme()
//    val fontScale = if (LocalConfiguration.current.screenWidthDp > 700) 1.3f else 1f
//
//    val darkModeGIFs = listOf(R.drawable.loading_blue, R.drawable.loading_purple)
//    val lightModeGIFs = listOf(R.drawable.loading_orange, R.drawable.loading_red_orange)
//    val gifRes = if (isDark) darkModeGIFs.random() else lightModeGIFs.random()
//
//    val foregroundColor = if (isDark) Color.White else Color.Black
//    val backgroundColor = if (isDark) Color.Black else CellColors.offWhite
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color.Gray.copy(alpha = 0.5f))
//    ) {
//        Column(
//            modifier = Modifier
//                .align(Alignment.Center)
//                .background(backgroundColor, shape = RoundedCornerShape(12.dp))
//                .padding(vertical = 32.dp, horizontal = 32.dp)
//                .widthIn(min = 300.dp, max = 360.dp)
//                .heightIn(min = 300.dp)
//                .padding(horizontal = 16.dp), // Additional inner padding for text readability
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.spacedBy(24.dp)
//        ) {
//            AndroidView(
//                factory = { context ->
//                    ImageView(context).apply {
//                        Glide.with(context)
//                            .asGif()
//                            .load(gifRes)
//                            .into(this)
//                    }
//                },
//                modifier = Modifier
//                    .size(120.dp)
//                    .offset(y = 25.dp)
//            )
//
//            Text(
//                text = "Generating Maze",
//                style = MaterialTheme.typography.headlineSmall.copy(
//                    fontSize = MaterialTheme.typography.headlineSmall.fontSize * fontScale,
//                    color = foregroundColor
//                )
//            )
//
//            Text(
//                text = algorithm.displayName,
//                style = MaterialTheme.typography.titleMedium.copy(
//                    fontSize = MaterialTheme.typography.titleMedium.fontSize * fontScale,
//                    color = foregroundColor
//                )
//            )
//
//            Text(
//                text = algorithm.description,
//                style = MaterialTheme.typography.bodyLarge.copy(
//                    fontSize = MaterialTheme.typography.bodyLarge.fontSize * fontScale,
//                    color = foregroundColor
//                ),
//                textAlign = TextAlign.Center,
//                modifier = Modifier.fillMaxWidth()
//            )
//        }
//    }
//}