//
// Created by Unixt on 7/19/2023.
//

#ifndef BOMBERMAN_APP_JNIC_H
#define BOMBERMAN_APP_JNIC_H
#include <jni.h>

extern "C"
{
JNIEXPORT jintArray JNICALL Java_main_nativeclasses_GameManager_getFieldSizes(JNIEnv *env, jclass type);
JNIEXPORT jintArray JNICALL Java_main_nativeclasses_GameManager_getObjects(JNIEnv *env, jclass type);
JNIEXPORT jintArray JNICALL Java_main_nativeclasses_GameManager_getRemovedObjects(JNIEnv *env, jclass type);
JNIEXPORT jint JNICALL Java_main_nativeclasses_GameManager_createGame(JNIEnv *env, jclass type);
JNIEXPORT jint JNICALL Java_main_nativeclasses_GameManager_updateGame(JNIEnv *env, jclass type, jint ji32Dt);
JNIEXPORT jint JNICALL Java_main_nativeclasses_GameManager_getZ(JNIEnv *env, jclass type, jint i32ObjType, jint i32ObjectStateOffset);
JNIEXPORT void JNICALL Java_main_nativeclasses_GameManager_setInput(JNIEnv *env, jclass type, jint i32ObjType, jint i32ObjectStateOffset, jbyte ui8Input);
JNIEXPORT jfloatArray JNICALL Java_main_nativeclasses_GameManager_getPosition(JNIEnv *env, jclass type, jint i32ObjType, jint i32ObjectStateOffset);
JNIEXPORT jint JNICALL Java_main_nativeclasses_GameManager_getState(JNIEnv *env, jclass type, jint i32ObjType, jint i32ObjectStateOffset);
JNIEXPORT jint JNICALL Java_main_nativeclasses_GameManager_updateGameTicker(JNIEnv *env, jclass type);
JNIEXPORT jobjectArray JNICALL Java_main_nativeclasses_GameManager_getHitboxes(JNIEnv *env, jclass type, jint i32ObjType, jint i32ObjectStateOffset);
JNIEXPORT jlong JNICALL Java_main_nativeclasses_GameManager_initFreeType(JNIEnv *env, jclass type);
}
#endif // BOMBERMAN_APP_JNIC_H
