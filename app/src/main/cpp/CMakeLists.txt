cmake_minimum_required(VERSION 3.22.1)
project("MazerAndroid")

# Import the prebuilt `mazer` Rust shared library (libmazer.so) from jmiLibs
add_library(mazer SHARED IMPORTED)
set_target_properties(mazer PROPERTIES IMPORTED_LOCATION
        "${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libmazer.so")

## This prevents the build-time absolute path from being embedded in the
## DT_NEEDED entry of libmazer_jni.so, ensuring the runtime loader looks
## for libmazer.so in the app's extracted lib directory
## (e.g., /data/app/.../lib/arm64-v8a/) without trying the invalid host path.
set_target_properties(mazer PROPERTIES IMPORTED_NO_SONAME TRUE)

# Build the JNI wrapper library
add_library(mazer_jni SHARED mazer_jni.c)

# Link the JNI wrapper to the prebuilt mazer lib
target_link_libraries(mazer_jni mazer)