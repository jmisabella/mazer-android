package com.jmisabella.mazer.models

import kotlinx.serialization.Serializable

@Serializable
data class Coordinates(
    val x: Int,
    val y: Int
)