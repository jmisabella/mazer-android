package com.jmisabella.mazer.models

sealed class MazeRequestError : Exception() {
    abstract val errorDescription: String?
}

object INVALID_MAZE_TYPE : MazeRequestError() {
    override val errorDescription: String? = "The selected maze type is invalid."
}

object INVALID_DIMENSIONS : MazeRequestError() {
    override val errorDescription: String? = "The provided maze dimensions are invalid."
}

object START_AND_GOAL_COORDINATES_SAME : MazeRequestError() {
    override val errorDescription: String? = "Start and goal coordinates cannot be the same."
}

object INVALID_COORDINATES : MazeRequestError() {
    override val errorDescription: String? = "One or more coordinates are out of bounds."
}

object INVALID_ALGORITHM : MazeRequestError() {
    override val errorDescription: String? = "The selected algorithm is not valid for this maze type."
}

object INVALID_DIMENSIONS_FOR_CAPTURE_STEPS : MazeRequestError() {
    override val errorDescription: String? = "Capture steps is only available for mazes with width and height ≤ 100."
}

object INVALID_MAZE_REQUEST_JSON : MazeRequestError() {
    override val errorDescription: String? = "The maze request JSON is malformed."
}

