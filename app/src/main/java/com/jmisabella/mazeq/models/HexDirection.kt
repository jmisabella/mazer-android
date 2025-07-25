package com.jmisabella.mazeq.models

enum class HexDirection {
    UP, UPPER_RIGHT, LOWER_RIGHT, DOWN, LOWER_LEFT, UPPER_LEFT;

    val systemImage: String
        get() = when (this) {
            UP -> "arrow.up"
            DOWN -> "arrow.down"
            UPPER_LEFT -> "arrow.up.left"
            UPPER_RIGHT -> "arrow.up.right"
            LOWER_LEFT -> "arrow.down.left"
            LOWER_RIGHT -> "arrow.down.right"
        }

    val vertexIndices: Pair<Int, Int>
        get() = when (this) {
            UP -> 0 to 1
            UPPER_RIGHT -> 1 to 2
            LOWER_RIGHT -> 2 to 3
            DOWN -> 3 to 4
            LOWER_LEFT -> 4 to 5
            UPPER_LEFT -> 5 to 0
        }

    val opposite: HexDirection
        get() = when (this) {
            UP -> DOWN
            UPPER_RIGHT -> LOWER_LEFT
            LOWER_RIGHT -> UPPER_LEFT
            DOWN -> UP
            LOWER_LEFT -> UPPER_RIGHT
            UPPER_LEFT -> LOWER_RIGHT
        }

    fun delta(): Pair<Int, Int> = when (this) {
        UP -> 0 to -1
        UPPER_RIGHT -> 1 to -1
        LOWER_RIGHT -> 1 to 0
        DOWN -> 0 to 1
        LOWER_LEFT -> -1 to 1
        UPPER_LEFT -> -1 to 0
    }

    fun offsetDelta(isOddColumn: Boolean): Pair<Int, Int> = when (this) {
        UP -> 0 to -1
        UPPER_RIGHT -> if (!isOddColumn) 1 to -1 else 1 to 0
        LOWER_RIGHT -> if (!isOddColumn) 1 to 0 else 1 to 1
        DOWN -> 0 to 1
        LOWER_LEFT -> if (!isOddColumn) -1 to 0 else -1 to 1
        UPPER_LEFT -> if (!isOddColumn) -1 to -1 else -1 to 0
    }
}