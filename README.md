# MazeR for Android

A fully offline, free Android maze app powered by the [MazeR](https://github.com/jmisabella/mazer) Rust library. Generate, explore, and solve mazes across **13 algorithms** and **5 grid types** — with heat-map hints, solution reveal, and step-by-step generation animation.

---

## Features

### Maze Generation & Solving
- **13 maze algorithms** — from classic Recursive Backtracker to graph-based Kruskal's and Prim's
- **5 grid types** — square, triangular, hexagonal, diamond, and octagon grids
- **Interactive solving** — navigate from start to goal using directional controls with haptic and audio feedback
- **Fully offline** — all maze generation and solving runs locally via Rust FFI; no network required

### Visualization
- **Heat-map hints** — 20 color palettes with 10-shade gradients show distance from start, helping you gauge how close you are to the goal
- **Solution reveal** — toggle the optimal path overlay at any time
- **Generation animation** — watch the maze carve itself step-by-step to understand how each algorithm works
- **Celebration effect** — sparkle particles, vibration, and a confirmation tone when you reach the goal

### Customization
- **3 cell sizes** — Small, Medium, Large
- **Dark and Light themes**
- **Preference persistence** — your last selected algorithm, grid type, size, and heat-map setting are remembered between sessions

---

## Maze Algorithms

| Algorithm | Description |
|---|---|
| **Aldous-Broder** | Random walk producing unbiased, uniformly random mazes |
| **Binary Tree** | Fast generation with a north/east directional bias |
| **Eller's** | Row-by-row generation, memory-efficient for very large mazes |
| **Growing Tree (Newest)** | Active cell list with newest selection; behaves like Recursive Backtracker |
| **Growing Tree (Random)** | Active cell list with random selection for more varied layouts |
| **Hunt and Kill** | Random walks combined with systematic scanning |
| **Kruskal's** | Minimum spanning tree approach producing uniform structures |
| **Prim's** | Weighted cell growth for moderate-length passages |
| **Recursive Backtracker** | Depth-first search creating long, twisting corridors |
| **Recursive Division** | Top-down subdivision from an open grid; fewer dead ends |
| **Reverse Delete** | Loop-erased generation starting from an open grid |
| **Sidewinder** | Row-wise generation with horizontal bias and random vertical links |
| **Wilson's** | Loop-erased random walks producing uniformly random spanning trees |

> Not all algorithms are available for every grid type. For example, Binary Tree and Sidewinder are only available for orthogonal mazes.

---

## Grid Types

| Grid | Cells | Description |
|---|---|---|
| **Orthogonal** | Squares | Classic grid with straight paths and right-angle turns |
| **Delta** | Triangles | Alternating normal and inverted triangles for jagged, complex paths |
| **Sigma** | Hexagons | Six-directional navigation in a honeycomb pattern |
| **Rhombic** | Diamonds | Slanted, diamond-shaped cells forming diagonal paths |
| **Upsilon** | Octagons + Squares | Alternating shapes for diverse passage widths |

---

## Architecture

```
Rust (MazeR library)
  |
  | FFI (C ABI)
  v
JNI Wrapper (C)          -- app/src/main/cpp/
  |
  v
Kotlin Native Bindings   -- MazerNative.kt
  |
  v
Jetpack Compose UI        -- Screens, Canvas renderers, controls
```

- **UI** — Jetpack Compose with Material 3
- **Rendering** — Custom `Canvas` composables for each grid topology with geometry-specific drawing
- **State** — Compose `mutableStateOf` with coroutine-based background generation on `Dispatchers.IO`
- **Native** — Prebuilt Rust `libmazer.so` linked via CMake; JNI wrapper marshals data between Kotlin and C
- **Min SDK** — 24 &nbsp;|&nbsp; **Target SDK** — 36

---

## Setup Instructions

1. **Download and Prepare the `mazer` Rust Library for Android Development**
    1. Run `setup.sh` from the root of `mazer-android/` with either `DEVELOP` or `RELEASE` as an argument:
       ```sh
       ./setup.sh DEVELOP  # For Android emulator (x86_64-linux-android, aarch64-linux-android)
       ./setup.sh RELEASE  # For real Android devices (armeabi-v7a, arm64-v8a)
       ```
    2. After a successful setup, you should see the compiled shared libraries in:
       - For **DEVELOP** (Emulator):
         ```
         mazer/target/x86_64-linux-android/release/libmazer.so
         mazer/target/aarch64-linux-android/release/libmazer.so
         ```
       - For **RELEASE** (Device):
         ```
         mazer/target/armeabi-v7a/release/libmazer.so
         mazer/target/arm64-v8a/release/libmazer.so
         ```
       These are copied to `app/src/main/jniLibs/{ABI}/libmazer.so`.

2. **Create a New Android Studio Project**
    - If you haven't already, create a new Android project for a phone app in the root `mazer-android/` directory (see [AndroidProjectSetupGuide.md](#AndroidProjectSetupGuide.md) for details).

3. **Add `libmazer.so` to the Android Project**
    1. In Android Studio, open the project navigator and locate the `app` module.
    2. Verify that `app/src/main/jniLibs/` contains the `.so` files for each architecture (e.g., `arm64-v8a/libmazer.so`).
    3. Open `app/build.gradle.kts` and ensure the NDK configuration is set:
       ```kotlin
       android {
           ...
           defaultConfig {
               ...
               ndk {
		   abiFilters += listOf("x86_64", "arm64-v8a")  // For DEVELOP/emulator; use listOf("armeabi-v7a", "arm64-v8a") for RELEASE
               }
           }
           externalNativeBuild {
               cmake {
                   path = file("src/main/cpp/CMakeLists.txt")
               }
           }
       }
       ```
    4. Sync the project (click **Sync Project with Gradle Files**—the elephant icon in the toolbar).
4. **Set Up JNI Integration**
    1. In Android Studio, create a Kotlin file (e.g., `MazerNative.kt`) in `app/src/main/kotlin/com/yourname/mazerandroid/` to define native methods:
       ```kotlin
       package com.yourname.mazerandroid

       object MazerNative {
           init {
               System.loadLibrary("mazer_jni")  // Loads the JNI wrapper, which links to libmazer
           }

           external fun mazerFfiIntegrationTest(): Int
       }
       ```
    2. Copy `mazer.h` from the project root to `app/src/main/cpp/mazer.h` for reference when writing JNI bindings.
    3. Create a JNI wrapper in `app/src/main/cpp/mazer_jni.c`:
       ```c
       #include <jni.h>
       #include "mazer.h"

       JNIEXPORT jint JNICALL
       Java_com_yourname_mazerandroid_MazerNative_mazerFfiIntegrationTest(JNIEnv *env, jobject obj) {
           return mazer_ffi_integration_test();
       }
       ```
    4. Update `app/src/main/cpp/CMakeLists.txt` to build the JNI wrapper:
       ```cmake
       cmake_minimum_required(VERSION 3.22.1)
       project("MazerAndroid")

       add_library(mazer SHARED IMPORTED)
       set_target_properties(mazer PROPERTIES IMPORTED_LOCATION
           "${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libmazer.so")
       add_library(mazer_jni SHARED mazer_jni.c)
       target_link_libraries(mazer_jni mazer)
       ```

5. **Verify FFI Connection**
    1. In `MainActivity.kt`, call the native function to test integration:
       ```kotlin
       import android.os.Bundle
       import androidx.activity.ComponentActivity
       import androidx.activity.compose.setContent
       import androidx.compose.foundation.layout.fillMaxSize
       import androidx.compose.material3.Text
       import androidx.compose.runtime.mutableStateOf
       import androidx.compose.runtime.remember
       import androidx.compose.ui.Modifier
       import com.yourname.mazerandroid.MazerNative

       class MainActivity : ComponentActivity() {
           override fun onCreate(savedInstanceState: Bundle?) {
               super.onCreate(savedInstanceState)
               setContent {
                   val testResult = remember { mutableStateOf(0) }
                   testResult.value = MazerNative.mazerFfiIntegrationTest()
                   Text(
                       text = "FFI Test Result: ${testResult.value}",
                       modifier = Modifier.fillMaxSize()
                   )
               }
           }
       }
       ```
    2. Run the app on an emulator or device.
    3. Verify the UI displays "FFI Test Result: 42" and check Logcat for any related output (e.g., if the Rust function logs "FFI integration test passed ✅").
       ```
       FFI integration test passed ✅
       ```

---

## License

This project uses the [MazeR](https://github.com/jmisabella/mazer) Rust library for maze generation and solving.
