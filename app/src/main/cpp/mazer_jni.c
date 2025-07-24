#include <jni.h>
#include "mazer.h"

JNIEXPORT jint JNICALL
Java_com_jmisabella_mazerandroid_MazerNative_mazerFfiIntegrationTest(JNIEnv *env, jobject obj) {
    return mazer_ffi_integration_test();
}