package com.jmisabella.mazeq

data class FFICell(
    val x: Long,
    val y: Long,
    val mazeType: String?,
    val linked: Array<String>?,
    val distance: Int,
    val isStart: Boolean,
    val isGoal: Boolean,
    val isActive: Boolean,
    val isVisited: Boolean,
    val hasBeenVisited: Boolean,
    val onSolutionPath: Boolean,
    val orientation: String?,
    val isSquare: Boolean
)