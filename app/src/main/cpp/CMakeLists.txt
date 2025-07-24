cmake_minimum_required(VERSION 3.22.1)
project("MazerAndroid")

# Import the prebuilt `mazer` Rust shared library (libmazer.so) from jmiLibs
add_library(mazer SHARED IMPORTED)
set_target_properties(mazer PROPERTIES IMPORTED_LOCATION
        "${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/libmazer.so")

# Build the JNI wrapper library
add_library(mazer_jni SHARED mazer_jni.c)

# Link the JNI wrapper to the prebuilt mazer lib
target_link_libraries(mazer_jni mazer)