package com.jmisabella.mazer // Replace with your actual package

object MazerNative {
    init {
        System.loadLibrary("mazer_jni")  // Loads the JNI wrapper lib, which depends on libmazer
    }

    external fun mazerFfiIntegrationTest(): Int

    external fun generateMaze(requestJson: String): Long  // Returns Grid* as long (pointer)

    external fun destroyMaze(gridPtr: Long): Unit

    external fun getCells(gridPtr: Long): Array<FFICell>?

    external fun getGenerationStepsCount(gridPtr: Long): Long

    external fun getGenerationStepCells(gridPtr: Long, stepIndex: Long): Array<FFICell>?

    external fun makeMove(gridPtr: Long, direction: String): Long  // Returns updated Grid* as long
}
