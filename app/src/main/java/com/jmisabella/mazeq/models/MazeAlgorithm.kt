package com.jmisabella.mazeq.models

import kotlinx.serialization.Serializable

@Serializable
enum class MazeAlgorithm {
    ALDOUS_BRODER,
    BINARY_TREE,
    ELLERS,
    GROWING_TREE_NEWEST,
    GROWING_TREE_RANDOM,
    HUNT_AND_KILL,
    KRUSKALS,
    PRIMS,
    RECURSIVE_BACKTRACKER,
    RECURSIVE_DIVISION,
    REVERSE_DELETE,
    SIDEWINDER,
    WILSONS;

    val description: String
        get() = when (this) {
            // ... (copy descriptions from Swift; truncated for brevity)
            ALDOUS_BRODER -> "This algorithm performs a random walk over the grid..."
            // Add the rest
        }

    val displayName: String
        get() = when (this) {
            ALDOUS_BRODER -> "Aldous Broder"
            // ... (add the rest from Swift)
        }

    companion object {
        fun availableAlgorithms(for mazeType: MazeType): List<MazeAlgorithm> = when (mazeType) {
            MazeType.ORTHOGONAL -> enumValues<MazeAlgorithm>().toList()
            MazeType.RHOMBIC -> enumValues<MazeAlgorithm>().filterNot {
                listOf(BINARY_TREE, SIDEWINDER, ELLERS, GROWING_TREE_NEWEST, GROWING_TREE_RANDOM, HUNT_AND_KILL).contains(it)
            }
            else -> enumValues<MazeAlgorithm>().filterNot {
                listOf(BINARY_TREE, SIDEWINDER, ELLERS, RECURSIVE_DIVISION).contains(it)
            }
        }
    }
}