#include <jni.h>
#include "mazer.h"

JNIEXPORT jint JNICALL
Java_com_jmisabella_mazeq_MazerNative_mazerFfiIntegrationTest(JNIEnv *env, jclass cls) {
    return mazer_ffi_integration_test();
}