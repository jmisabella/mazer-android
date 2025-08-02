package com.jmisabella.mazer

data class FFICell(
    val x: Long,
    val y: Long,
    val maze_type: String?,
    val linked: Array<String>?,
    val distance: Int,
    val is_start: Boolean,
    val is_goal: Boolean,
    val is_active: Boolean,
    val is_visited: Boolean,
    val has_been_visited: Boolean,
    val on_solution_path: Boolean,
    val orientation: String?,
    val is_square: Boolean
)