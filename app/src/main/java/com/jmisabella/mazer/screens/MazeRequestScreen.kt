package com.jmisabella.mazer.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jmisabella.mazer.R
import com.jmisabella.mazer.layout.CellColors
import com.jmisabella.mazer.layout.interpolateColor
import com.jmisabella.mazer.models.CellSize
import com.jmisabella.mazer.models.MazeAlgorithm
import com.jmisabella.mazer.models.MazeCell
import com.jmisabella.mazer.models.MazeType
import com.jmisabella.mazer.utility.toColor
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MazeRequestScreen(
    mazeCells: MutableState<List<MazeCell>>,
    mazeGenerated: MutableState<Boolean>,
    mazeType: MutableState<MazeType>,
    selectedSize: MutableState<CellSize>,
    selectedMazeType: MutableState<MazeType>,
    selectedAlgorithm: MutableState<MazeAlgorithm>,
    captureSteps: MutableState<Boolean>,
    submitMazeRequest: () -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current.density
    val displayMetrics = context.resources.displayMetrics
    val screenWidth = (displayMetrics.widthPixels / density)
    val screenHeight = (displayMetrics.heightPixels / density)
    val isDark = isSystemInDarkTheme()
    val fontScale = if (screenWidth > 700) 1.3f else 1.0f
    val isTablet = (configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE

    val availableMazeTypes = remember { MazeType.availableMazeTypes(isSmallScreen = screenHeight <= 667f) }
    var availableAlgorithms by remember(selectedMazeType.value) { mutableStateOf(MazeAlgorithm.availableAlgorithms(selectedMazeType.value)) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val horizontalMargin = 10f
    val verticalMargin = 280f

    val availableWidth = screenWidth - horizontalMargin
    val availableHeight = screenHeight - verticalMargin

    val mazeWidth = max(1, (availableWidth / selectedSize.value.value).toInt())
    val mazeHeight = max(1, (availableHeight / selectedSize.value.value).toInt())

    // onChange of selectedMazeType
    LaunchedEffect(selectedMazeType.value) {
        availableAlgorithms = MazeAlgorithm.availableAlgorithms(selectedMazeType.value)
        if (!availableAlgorithms.contains(selectedAlgorithm.value)) {
            availableAlgorithms.randomOrNull()?.let { selectedAlgorithm.value = it }
        }
    }

    // onChange of selectedSize
    LaunchedEffect(selectedSize.value) {
        if (selectedSize.value != CellSize.LARGE) {
            captureSteps.value = false
        }
    }

    // onAppear equivalent
    LaunchedEffect(Unit) {
        if (isTablet) {
            selectedSize.value = CellSize.LARGE
            captureSteps.value = false
        }
        if (!availableMazeTypes.contains(selectedMazeType.value)) {
            selectedMazeType.value = MazeType.ORTHOGONAL
        }
    }

    val fontColor = if (isDark) Color.White else CellColors.lightModeSecondary
    val selectedBg = if (isDark) Color(0xFF4A4A4A) else Color.White
    val unselectedBg = if (isDark) Color(0xFF2A2A2A) else interpolateColor(CellColors.offWhite, Color.LightGray, 0.3)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDark) Color.Black else CellColors.offWhite)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(20.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val logoSize = (if (isDark) 60f else 120f) * fontScale
                val logoRes = if (isDark) R.drawable.logo_gradient else R.drawable.logo_cream
                Image(
                    painter = painterResource(logoRes),
                    contentDescription = "Logo",
                    modifier = Modifier.size(logoSize.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Omni Mazes & Solutions",
                    fontSize = (14 * fontScale).sp,
                    color = if (isDark) "B3B3B3".toColor() else CellColors.lightModeSecondary,
                    fontStyle = FontStyle.Italic
                )
            }
            Spacer(Modifier.height(8.dp))

            if (!isTablet) {
                // Cell Size Picker (Segmented)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CellSize.values().forEach { size ->
                        val isSelected = selectedSize.value == size
                        Button(
                            onClick = { selectedSize.value = size },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) selectedBg else unselectedBg,
                                contentColor = fontColor
                            ),
                            shape = MaterialTheme.shapes.small,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(size.label)
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            // Maze Type Picker (Segmented)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                availableMazeTypes.forEach { type ->
                    val isSelected = selectedMazeType.value == type
                    Button(
                        onClick = { selectedMazeType.value = type },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) selectedBg else unselectedBg,
                            contentColor = fontColor
                        ),
                        shape = MaterialTheme.shapes.small,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(type.displayName)
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = selectedMazeType.value.description,
                fontSize = (12 * fontScale).sp,
                color = if (isDark) CellColors.grayerSky else CellColors.lightModeSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(20.dp))

            // Algorithm Picker (Dropdown)
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                TextField(
                    readOnly = true,
                    value = selectedAlgorithm.value.displayName,
                    onValueChange = { },
                    label = null,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(
                        focusedLabelColor = if (isDark) CellColors.lightSkyBlue else CellColors.orangeRed,
                        unfocusedLabelColor = if (isDark) CellColors.lightSkyBlue else CellColors.orangeRed,
                        focusedIndicatorColor = if (isDark) CellColors.lightSkyBlue else CellColors.orangeRed,
                        unfocusedIndicatorColor = if (isDark) CellColors.lightSkyBlue else CellColors.orangeRed,
                        focusedTrailingIconColor = if (isDark) CellColors.lightSkyBlue else CellColors.orangeRed,
                        unfocusedTrailingIconColor = if (isDark) CellColors.lightSkyBlue else CellColors.orangeRed,
                        focusedContainerColor = if (isDark) Color.Black else CellColors.offWhite,
                        unfocusedContainerColor = if (isDark) Color.Black else CellColors.offWhite,
                        disabledContainerColor = if (isDark) Color.Black else CellColors.offWhite,
                        focusedTextColor = if (isDark) CellColors.lightSkyBlue else CellColors.orangeRed,
                        unfocusedTextColor = if (isDark) CellColors.lightSkyBlue else CellColors.orangeRed
                    ),
                    textStyle = LocalTextStyle.current.copy(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .background(if (isDark) Color.DarkGray else CellColors.barelyLavenderMostlyWhite)
                ) {
                    val scrollState = rememberScrollState()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 300.dp)
                                .verticalScroll(scrollState)
                        ) {
                            availableAlgorithms.forEach { algo ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            algo.displayName,
                                            fontSize = (16 * fontScale).sp,
                                            color = if (isDark) Color.White else CellColors.lightModeSecondary
                                        )
                                    },
                                    onClick = {
                                        selectedAlgorithm.value = algo
                                        expanded = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                )
                            }
                        }
                        Column(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .fillMaxWidth()
                        ) {
                            if (scrollState.value > 0) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_arrow_up),
                                    contentDescription = "Scroll up",
                                    tint = if (isDark) CellColors.lightSkyBlue else CellColors.orangeRed,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .align(Alignment.CenterHorizontally)
                                        .background(if (isDark) Color.DarkGray else CellColors.barelyLavenderMostlyWhite)
                                )
                            }
                        }
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                        ) {
                            if (scrollState.value < scrollState.maxValue) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_arrow_down),
                                    contentDescription = "Scroll down",
                                    tint = if (isDark) CellColors.lightSkyBlue else CellColors.orangeRed,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .align(Alignment.CenterHorizontally)
                                        .background(if (isDark) Color.DarkGray else CellColors.barelyLavenderMostlyWhite)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(4.dp))
            Text(
                text = selectedAlgorithm.value.description,
                fontSize = (12 * fontScale).sp,
                color = if (isDark) CellColors.grayerSky else CellColors.lightModeSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(20.dp))

            if (!isTablet) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Show Maze Generation",
                        fontSize = (16 * fontScale).sp,
                        color = if (isDark) CellColors.lightSkyBlue else CellColors.orangeRed,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(10.dp))
                    Switch(
                        checked = captureSteps.value,
                        onCheckedChange = { captureSteps.value = it },
                        enabled = selectedSize.value == CellSize.LARGE,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = if (isDark) CellColors.lightSkyBlue else CellColors.orangeRed,
                            checkedTrackColor = if (isDark) CellColors.lightSkyBlue.copy(alpha = 0.5f) else CellColors.orangeRed.copy(alpha = 0.5f),
                            uncheckedThumbColor = if (isDark) Color.Gray else CellColors.offWhite,
                            uncheckedTrackColor = if (isDark) Color.LightGray else CellColors.orangeRed.copy(alpha = 0.2f)
                        )
                    )
                }
                if (selectedSize.value != CellSize.LARGE) {
                    Text(
                        text = "Show Maze Generation is only available for large cell sizes.",
                        fontSize = (12 * fontScale).sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(Modifier.height(20.dp))
            }

            val buttonTint = if (isDark) CellColors.lighterSky else CellColors.orangeRed
            Button(
                onClick = submitMazeRequest,
                colors = ButtonDefaults.buttonColors(containerColor = buttonTint),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "Generate",
                    color = if (isDark) Color.Black else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            errorMessage?.let { err ->
                Text(
                    text = err,
                    color = Color.Red,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(top = 16.dp))
        }
    }
}
