package com.jmisabella.mazeq.models

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

//enum class MazeRequestError : Exception() {
//    INVALID_MAZE_TYPE,
//    INVALID_DIMENSIONS,
//    START_AND_GOAL_COORDINATES_SAME,
//    INVALID_COORDINATES,
//    INVALID_ALGORITHM,
//    INVALID_DIMENSIONS_FOR_CAPTURE_STEPS,
//    INVALID_MAZE_REQUEST_JSON;
//
//    val errorDescription: String?
//        get() = when (this) {
//            INVALID_MAZE_TYPE -> "The selected maze type is invalid."
//            INVALID_DIMENSIONS -> "The provided maze dimensions are invalid."
//            START_AND_GOAL_COORDINATES_SAME -> "Start and goal coordinates cannot be the same."
//            INVALID_COORDINATES -> "One or more coordinates are out of bounds."
//            INVALID_ALGORITHM -> "The selected algorithm is not valid for this maze type."
//            INVALID_DIMENSIONS_FOR_CAPTURE_STEPS -> "Capture steps is only available for mazes with width and height ≤ 100."
//            INVALID_MAZE_REQUEST_JSON -> "The maze request JSON is malformed."
//        }
//}