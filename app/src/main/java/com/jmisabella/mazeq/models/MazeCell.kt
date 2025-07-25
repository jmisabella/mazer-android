package com.jmisabella.mazeq.models

data class MazeCell(
    val x: Int,
    val y: Int,
    val mazeType: String,
    val linked: List<String>,
    val distance: Int,
    val isStart: Boolean,
    val isGoal: Boolean,
    val isActive: Boolean,
    val isVisited: Boolean,
    val hasBeenVisited: Boolean,
    val onSolutionPath: Boolean,
    val orientation: String,
    val isSquare: Boolean
)