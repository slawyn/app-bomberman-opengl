//
// Created by Unixt on 7/19/2023.
//

#ifndef BOMBERMAN_APP_JNIC_H
#define BOMBERMAN_APP_JNIC_H
#include <jni.h>

extern "C"
{
    JNIEXPORT jlong JNICALL Java_main_nativeclasses_GameManager_initFreeType(JNIEnv *env, jclass type);
    JNIEXPORT jint JNICALL Java_main_nativeclasses_GameManager_updateGameTicker(JNIEnv * env, jclass type);
    JNIEXPORT jint JNICALL Java_main_nativeclasses_GameManager_getState(JNIEnv * env, jclass type, jint i32ObjType, jint i32ObjectStateOffset);

    JNIEXPORT void JNICALL
    Java_main_nativeclasses_GameLogic_init(JNIEnv * env,
    jclass type,
            jint i32ObjType,
    jint i32ObjectStateOffset,
            jint i32PositionX,
    jint i32PositionY);


    JNIEXPORT void JNICALL
    Java_main_nativeclasses_GameManager_setInput(JNIEnv * env,
    jclass type,
            jint i32ObjType,
    jint i32ObjectStateOffset,
            jbyte ui8Input);
    JNIEXPORT jint JNICALL
            Java_main_nativeclasses_GameLogic_updateState(JNIEnv *env,
                                                          jclass type,
    jint i32ObjType,
            jint i32ObjectStateOffset,
    jint i32Dt);

    JNIEXPORT void JNICALL
    Java_main_nativeclasses_GameLogic_updateBoundingBoxes(JNIEnv *env,
    jclass type,
            jint i32ObjType,
    jint i32ObjectStateOffset);
    JNIEXPORT jint JNICALL
            Java_main_nativeclasses_GameManager_getZ(JNIEnv * env, jclass type, jint i32ObjType, jint i32ObjectStateOffset);

}
#endif //BOMBERMAN_APP_JNIC_H
