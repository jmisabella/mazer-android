package com.jmisabella.mazer.models

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Serializable

@Serializable
data class MazeRequest(
    val maze_type: MazeType,
    val width: Int,
    val height: Int,
    val algorithm: MazeAlgorithm,
    @kotlinx.serialization.ExperimentalSerializationApi @EncodeDefault val capture_steps: Boolean = false
)