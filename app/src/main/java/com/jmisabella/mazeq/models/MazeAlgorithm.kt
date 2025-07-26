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
            ALDOUS_BRODER -> "This algorithm performs a random walk over the grid, carving a passage whenever it encounters an unvisited cell. It produces an unbiased maze, though it can be inefficient because it may visit cells many times."
            BINARY_TREE -> "This method iterates through each cell in a grid, carving passages either north or east (or in another fixed pair of directions). The result is a maze with a predictable bias and long, straight corridors."
            ELLERS -> "Eller’s algorithm builds the maze row by row, randomly joining cells within each row and ensuring connectivity to the next row. It produces mazes with a row-wise structure and is memory-efficient for infinite mazes."
            GROWING_TREE_NEWEST -> "This algorithm maintains a list of active cells, always choosing the newest one to carve a passage to an unvisited neighbor. It can mimic other algorithms like Recursive Backtracker."
            GROWING_TREE_RANDOM -> "This algorithm maintains a list of active cells, choosing one randomly to carve a passage to an unvisited neighbor. It can mimic other algorithms like Recursive Backtracker."
            HUNT_AND_KILL -> "Combining random walks with systematic scanning, this method randomly carves a passage until it reaches a dead end, then 'hunts' for an unvisited cell adjacent to the currently carved maze. This process creates mazes with long corridors and noticeable dead ends, balancing randomness with structure."
            KRUSKALS -> "Kruskal’s algorithm treats the grid as a graph, randomly merging cells by removing walls to form a minimum spanning tree. It creates mazes with a uniform, tree-like structure and no bias in direction."
            PRIMS -> "Prim’s algorithm starts with a random cell and grows the maze by adding passages to unvisited neighbors with the lowest random weights. It produces mazes with a uniform structure and moderate-length passages."
            RECURSIVE_BACKTRACKER -> "Essentially a depth-first search, this algorithm recursively explores neighbors and backtracks upon reaching dead ends. It’s fast and generates mazes with long, twisting passages and fewer short loops."
            RECURSIVE_DIVISION -> "This method starts with an open grid and recursively divides it into chambers by adding walls with random passages. It creates mazes with a hierarchical layout, featuring long walls and fewer dead ends."
            REVERSE_DELETE -> "Beginning with a fully open grid, this algorithm randomly adds walls between adjacent cells, but only if the addition doesn’t isolate any part of the maze. This creates a perfect maze with balanced passages and no directional bias."
            SIDEWINDER -> "Processed row-by-row, this algorithm carves eastward passages with occasional upward connections. It creates mazes with a strong horizontal bias and randomly placed vertical links."
            WILSONS -> "Wilson’s algorithm uses loop-erased random walks, starting from a random cell and extending a path until it connects with the growing maze. It produces uniformly random mazes and avoids the inefficiencies of Aldous-Broder."
        }

    val displayName: String
        get() = when (this) {
            ALDOUS_BRODER -> "Aldous Broder"
            BINARY_TREE -> "Binary Tree"
            ELLERS -> "Eller’s"
            GROWING_TREE_NEWEST -> "Growing Tree (Newest Selection)"
            GROWING_TREE_RANDOM -> "Growing Tree (Random Selection)"
            HUNT_AND_KILL -> "Hunt and Kill"
            KRUSKALS -> "Kruskal’s"
            PRIMS -> "Prim's"
            RECURSIVE_BACKTRACKER -> "Recursive Backtracker"
            RECURSIVE_DIVISION -> "Recursive Division"
            REVERSE_DELETE -> "Reverse Delete"
            SIDEWINDER -> "Sidewinder"
            WILSONS -> "Wilson's"
        }

    companion object {
        fun availableAlgorithms(mazeType: MazeType): List<MazeAlgorithm> = when (mazeType) {
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