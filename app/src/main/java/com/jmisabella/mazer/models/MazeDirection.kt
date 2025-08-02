package com.jmisabella.mazer.models

enum class MazeDirection {
    UP, DOWN, LEFT, RIGHT,
    UPPER_LEFT, UPPER_RIGHT, LOWER_LEFT, LOWER_RIGHT;

    val systemImage: String
        get() = when (this) {
            UP -> "arrow.up"
            DOWN -> "arrow.down"
            LEFT -> "arrow.left"
            RIGHT -> "arrow.right"
            UPPER_LEFT -> "arrow.up.left"
            UPPER_RIGHT -> "arrow.up.right"
            LOWER_LEFT -> "arrow.down.left"
            LOWER_RIGHT -> "arrow.down.right"
        }
}