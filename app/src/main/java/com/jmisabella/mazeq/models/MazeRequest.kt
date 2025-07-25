package com.jmisabella.mazeq.models

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Serializable

@Serializable
data class MazeRequest(
    val mazeType: MazeType,
    val width: Int,
    val height: Int,
    val algorithm: MazeAlgorithm,
    @EncodeDefault val captureSteps: Boolean = false
)