package com.jmisabella.mazeq.utility

import com.jmisabella.mazeq.models.MazeAlgorithm
import com.jmisabella.mazeq.models.MazeRequest
import com.jmisabella.mazeq.models.MazeType
import com.jmisabella.mazeq.models.MazeRequestError
import com.jmisabella.mazeq.models.INVALID_DIMENSIONS
import com.jmisabella.mazeq.models.INVALID_DIMENSIONS_FOR_CAPTURE_STEPS
import com.jmisabella.mazeq.models.INVALID_MAZE_REQUEST_JSON
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

sealed class Result<out T, out E>
data class Success<T>(val value: T) : Result<T, Nothing>()
data class Failure<E>(val error: E) : Result<Nothing, E>()

object MazeRequestValidator {
    fun validate(
        mazeType: MazeType,
        width: Int,
        height: Int,
        algorithm: MazeAlgorithm,
        captureSteps: Boolean
    ): Result<String, MazeRequestError> {
        if (width <= 0 || height <= 0) {
            return Failure(INVALID_DIMENSIONS)
        }
        if (captureSteps && (width > 100 || height > 100)) {
            return Failure(INVALID_DIMENSIONS_FOR_CAPTURE_STEPS)
        }

        val mazeRequest = MazeRequest(
            mazeType = mazeType,
            width = width,
            height = height,
            algorithm = algorithm,
            captureSteps = captureSteps
        )

        return try {
            val jsonString = Json { prettyPrint = true }.encodeToString(mazeRequest)
            Success(jsonString)
        } catch (e: Exception) {
            Failure(INVALID_MAZE_REQUEST_JSON)
        }
    }
}