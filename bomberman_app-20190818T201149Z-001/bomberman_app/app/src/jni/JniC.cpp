//
// Created by Unixt on 7/19/2023.
//
#include <jni.h>
#include "JniC.h"
#include "ft2build.h"
#include "freetype/freetype.h"
#include FT_FREETYPE_H"freetype/freetype.h"

#include "code/GameLogic.h"
extern "C"
{
FT_Library library;


JNIEXPORT jintArray JNICALL
Java_main_nativeclasses_GameManager_getObjects(JNIEnv * env,
                                               jclass type) {


    jint *pjiObjects = NULL;
    int16_t i16TotalCount = jiGameGetObjects(&pjiObjects);

    /* Copy to external array */
    jintArray jArray = env -> NewIntArray(i16TotalCount);
    env->SetIntArrayRegion(jArray, 0, i16TotalCount, pjiObjects);
    return jArray;
}

JNIEXPORT jintArray JNICALL
Java_main_nativeclasses_GameManager_getRemovedObjects(JNIEnv * env,
                                                      jclass type) {

    jint *pjiObjects = NULL;
    int16_t i16TotalCount = jiGameGetRemovedObjects(&pjiObjects);

    /* Copy to external array */
    jintArray jArray = env -> NewIntArray(i16TotalCount);
    env->SetIntArrayRegion(jArray, 0, i16TotalCount, pjiObjects);
    return jArray;
}

JNIEXPORT jint JNICALL
Java_main_nativeclasses_GameManager_createGame(JNIEnv * env,
                                               jclass type)
{
    return jiGameCreate();
}

JNIEXPORT jint JNICALL
Java_main_nativeclasses_GameManager_updateGame(JNIEnv * env,
                                               jclass type,
                                               jint ji32Dt)
{

    /* Update game objects */
    uint32_t ui32UpdatedPlayers = 0;
    return jiUpdateObjects(ji32Dt, &ui32UpdatedPlayers);
}


JNIEXPORT jint JNICALL
Java_main_nativeclasses_GameManager_getZ(   JNIEnv * env,
                                            jclass type,
                                            jint i32ObjType,
                                            jint i32ObjectStateOffset)
{
    return i16GameGetObjectZ(i32ObjType, i32ObjectStateOffset);
}




JNIEXPORT void JNICALL
Java_main_nativeclasses_GameManager_setInput(   JNIEnv * env,
                                                jclass type,
                                                jint i32ObjType,
                                                jint i32ObjectStateOffset,
                                                jbyte ui8Input)
{
    vGameSetInput(i32ObjectStateOffset, ui8Input);
}



JNIEXPORT jlongArray JNICALL
        Java_main_nativeclasses_GameManager_getPosition(JNIEnv * env,
                                                        jclass type,
jint i32ObjType,
        jint i32ObjectStateOffset)
{
    jlong *pjlPositions = NULL;
    jiGameGetPositions(&pjlPositions, i32ObjType, i32ObjectStateOffset);

    /* Copy to external array */
    jlongArray jArray = env -> NewLongArray(2);
    env->SetLongArrayRegion(jArray, 0, 2, pjlPositions);
    return jArray;
}

JNIEXPORT jint JNICALL Java_main_nativeclasses_GameManager_getState(JNIEnv * env,
                                                     jclass type,
                                                        jint i32ObjType,
                                                                jint i32ObjectStateOffset)
{
    return ui32GameGetState(i32ObjType, i32ObjectStateOffset);
}

JNIEXPORT jint JNICALL Java_main_nativeclasses_GameManager_updateGameTicker(JNIEnv *env,
                                                             jclass type)
{
    return jiGameUpdateTicker();
}

JNIEXPORT jobjectArray  JNICALL Java_main_nativeclasses_GameManager_getHitboxes(JNIEnv *env,
                                                        jclass type,
                                                        jint i32ObjType,
                                                                jint i32ObjectStateOffset)
{

    jobjectArray array2D = env->NewObjectArray(
            1, env->FindClass("[I"), NULL);
    jintArray array1D = env->NewIntArray(4);
    env->SetObjectArrayElement(array2D, 0, array1D);

    jint *pi32Hitbox =  NULL;
    jiGameGetHitbox(&pi32Hitbox, i32ObjType, i32ObjectStateOffset);

    env->SetIntArrayRegion(array1D, 0, 4, pi32Hitbox);
    return array2D;
}

JNIEXPORT jlong JNICALL
        Java_main_nativeclasses_GameManager_initFreeType(JNIEnv *env,
                                                         jclass type)
{

    uint32_t error = 0;//FT_Init_FreeType(&library);
    if (error) {
        return error;
    } else {
        return 0xBEEFl;
    }
}

}


