package com.jmisabella.mazer.models

import kotlinx.serialization.Serializable

@Serializable
enum class MazeType {
    DELTA,
    ORTHOGONAL,
    RHOMBIC,
    SIGMA,
    UPSILON;

    val description: String
        get() = when (this) {
            DELTA -> "Triangular cells (normal and inverted) creating jagged, complex paths."
            ORTHOGONAL -> "Orthogonal mazes carve a classic square-grid layout with straight paths and right-angle turns."
            RHOMBIC -> "Diamond cells forming a grid with slanted paths."
            SIGMA -> "Hexagonal cells forming a web of interconnected paths, promoting more intuitive navigation."
            UPSILON -> "Alternating octagon and square cells add variety to pathfinding."
        }

    val ffiName: String
        get() = when (this) {
            DELTA -> "Delta"
            ORTHOGONAL -> "Orthogonal"
            RHOMBIC -> "Rhombic"
            SIGMA -> "Sigma"
            UPSILON -> "Upsilon"
        }

    val displayName: String
        get() = when (this) {
            DELTA -> "Delta"
            ORTHOGONAL -> "Ortho"
            RHOMBIC -> "Rhombic"
            SIGMA -> "Sigma"
            UPSILON -> "Upsilon"
        }

    companion object {
        fun fromFFIName(name: String?): MazeType? = when (name) {
            "Delta" -> DELTA
            "Orthogonal" -> ORTHOGONAL
            "Sigma" -> SIGMA
            "Upsilon" -> UPSILON
            "Rhombic" -> RHOMBIC
            else -> null
        }

        fun availableMazeTypes(isSmallScreen: Boolean): List<MazeType> =
            if (isSmallScreen) enumValues<MazeType>().filter { it != RHOMBIC } else enumValues<MazeType>().toList()
    }
}