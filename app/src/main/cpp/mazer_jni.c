#include <jni.h>
#include "mazer.h"
#include <stdlib.h>  // For malloc if needed, but mostly use JNI alloc

// Helper to create Java FFICell object from C struct
jobject createJavaFFICell(JNIEnv *env, const FFICell *cCell) {
    jclass ffiCellClass = (*env)->FindClass(env, "com/jmisabella/mazeq/FFICell");
    if (ffiCellClass == NULL) return NULL;

    jmethodID constructor = (*env)->GetMethodID(env, ffiCellClass, "<init>",
                                                "(JJLjava/lang/String;[Ljava/lang/String;IZZZZZLjava/lang/String;Z)V");
    if (constructor == NULL) return NULL;

    // Convert linked array to Java String array
    jobjectArray linkedArray = (*env)->NewObjectArray(env, cCell->linked_len, (*env)->FindClass(env, "java/lang/String"), NULL);
    for (size_t i = 0; i < cCell->linked_len; i++) {
        jstring linkedStr = (*env)->NewStringUTF(env, cCell->linked[i]);
        (*env)->SetObjectArrayElement(env, linkedArray, i, linkedStr);
        (*env)->DeleteLocalRef(env, linkedStr);
    }

    jstring mazeTypeStr = (*env)->NewStringUTF(env, cCell->maze_type);
    jstring orientationStr = (*env)->NewStringUTF(env, cCell->orientation);

    jobject javaCell = (*env)->NewObject(env, ffiCellClass, constructor,
                                         (jlong)cCell->x, (jlong)cCell->y,
                                         mazeTypeStr, linkedArray,
                                         (jint)cCell->distance,
                                         (jboolean)cCell->is_start,
                                         (jboolean)cCell->is_goal,
                                         (jboolean)cCell->is_active,
                                         (jboolean)cCell->is_visited,
                                         (jboolean)cCell->has_been_visited,
                                         (jboolean)cCell->on_solution_path,
                                         orientationStr,
                                         (jboolean)cCell->is_square);

    (*env)->DeleteLocalRef(env, mazeTypeStr);
    (*env)->DeleteLocalRef(env, orientationStr);
    (*env)->DeleteLocalRef(env, linkedArray);

    return javaCell;
}

JNIEXPORT jint JNICALL
Java_com_jmisabella_mazeq_MazerNative_mazerFfiIntegrationTest(JNIEnv *env, jclass cls) {
    return mazer_ffi_integration_test();
}

// Generate maze
JNIEXPORT jlong JNICALL
Java_com_jmisabella_mazeq_MazerNative_generateMaze(JNIEnv *env, jclass cls, jstring requestJson) {
    const char *json = (*env)->GetStringUTFChars(env, requestJson, NULL);
    Grid *grid = mazer_generate_maze(json);
    (*env)->ReleaseStringUTFChars(env, requestJson, json);
    return (jlong)grid;
}

// Destroy maze
JNIEXPORT void JNICALL
Java_com_jmisabella_mazeq_MazerNative_destroyMaze(JNIEnv *env, jclass cls, jlong gridPtr) {
    mazer_destroy((Grid *)gridPtr);
}

// Get cells
JNIEXPORT jobjectArray JNICALL
Java_com_jmisabella_mazeq_MazerNative_getCells(JNIEnv *env, jclass cls, jlong gridPtr) {
    size_t length = 0;
    FFICell *cCells = mazer_get_cells((Grid *)gridPtr, &length);
    if (cCells == NULL) return NULL;

    jclass ffiCellClass = (*env)->FindClass(env, "com/jmisabella/mazeq/FFICell");
    jobjectArray javaCells = (*env)->NewObjectArray(env, length, ffiCellClass, NULL);

    for (size_t i = 0; i < length; i++) {
        jobject javaCell = createJavaFFICell(env, &cCells[i]);
        (*env)->SetObjectArrayElement(env, javaCells, i, javaCell);
        (*env)->DeleteLocalRef(env, javaCell);
    }

    mazer_free_cells(cCells, length);  // Free immediately after copying
    return javaCells;
}

// Get generation steps count
JNIEXPORT jlong JNICALL
Java_com_jmisabella_mazeq_MazerNative_getGenerationStepsCount(JNIEnv *env, jclass cls, jlong gridPtr) {
    return (jlong)mazer_get_generation_steps_count((Grid *)gridPtr);
}

// Get generation step cells
JNIEXPORT jobjectArray JNICALL
Java_com_jmisabella_mazeq_MazerNative_getGenerationStepCells(JNIEnv *env, jclass cls, jlong gridPtr, jlong stepIndex) {
    size_t length = 0;
    FFICell *cCells = mazer_get_generation_step_cells((Grid *)gridPtr, (size_t)stepIndex, &length);
    if (cCells == NULL) return NULL;

    jclass ffiCellClass = (*env)->FindClass(env, "com/jmisabella/mazeq/FFICell");
    jobjectArray javaCells = (*env)->NewObjectArray(env, length, ffiCellClass, NULL);

    for (size_t i = 0; i < length; i++) {
        jobject javaCell = createJavaFFICell(env, &cCells[i]);
        (*env)->SetObjectArrayElement(env, javaCells, i, javaCell);
        (*env)->DeleteLocalRef(env, javaCell);
    }

    mazer_free_cells(cCells, length);  // Free immediately after copying
    return javaCells;
}

// Make move
JNIEXPORT jlong JNICALL
Java_com_jmisabella_mazeq_MazerNative_makeMove(JNIEnv *env, jclass cls, jlong gridPtr, jstring direction) {
    const char *dir = (*env)->GetStringUTFChars(env, direction, NULL);
    void *updatedGrid = mazer_make_move((void *)gridPtr, dir);
    (*env)->ReleaseStringUTFChars(env, direction, dir);
    return (jlong)updatedGrid;
}