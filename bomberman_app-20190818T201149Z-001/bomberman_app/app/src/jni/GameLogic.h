#ifndef GAME_STATE_BUFFER_H
#define GAME_STATE_BUFFER_H
#include <jni.h>
#include <jni.h>
#include <jni.h>

extern "C" {



#define     OBJ_BOMB  0x1000
#define     OBJ_EXPLN  0x2000
#define     OBJ_CRATE  0x3000
#define     OBJ_BLOCK  0x4000
#define     OBJ_ITEM  0x5000
#define     OBJ_PLAYR  0x6000



#define OBJ_MASK                    (0xF000)
JNIEXPORT jlong JNICALL Java_main_nativeclasses_GameLogic_initFreeType(JNIEnv *env, jclass type);
JNIEXPORT jint JNICALL Java_main_nativeclasses_GameLogic_updateGameTicker(JNIEnv * env, jclass type);
JNIEXPORT jint JNICALL Java_main_nativeclasses_GameLogic_getState(JNIEnv * env, jclass type, jint i32ObjType, jint i32ObjectStateOffset);

JNIEXPORT void JNICALL
Java_main_nativeclasses_GameLogic_init(JNIEnv * env,
                                             jclass type,
                                             jint i32ObjType,
                                             jint i32ObjectStateOffset,
                                             jint i32PositionX,
                                             jint i32PositionY);


JNIEXPORT void JNICALL
Java_main_nativeclasses_GameLogic_setInput(JNIEnv * env,
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
        Java_main_nativeclasses_GameLogic_getZ(JNIEnv * env,jclass type,jint i32ObjType,jint i32ObjectStateOffset);

}

#endif
