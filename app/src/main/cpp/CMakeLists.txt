cmake_minimum_required(VERSION 3.22.1)
project("MazerAndroid")
add_library(mazer SHARED IMPORTED)
set_target_properties(mazer PROPERTIES IMPORTED_LOCATION
        "${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/libmazer.so")