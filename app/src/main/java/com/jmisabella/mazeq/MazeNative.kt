package com.jmisabella.mazeq // Replace with your actual package

object MazerNative {
    init {
        System.loadLibrary("mazer_jni")  // Loads the JNI wrapper lib, which depends on libmazer
    }

    external fun mazerFfiIntegrationTest(): Int
}