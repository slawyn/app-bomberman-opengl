//
// Created by slaw on 5/10/2020.
//
#include <jni.h>        // JNI header provided by JDK
#include <stdio.h>      // C Standard IO Header
#include "main_nativeclasses_NativeInterface.h"   // Generated
#include "ft2build.h"
#include "freetype/freetype.h"
#include FT_FREETYPE_H


FT_Library library;

JNIEXPORT jlong JNICALL Java_main_nativeclasses_NativeInterface_initFreeType(JNIEnv *env, jclass type) {

    uint32_t error = FT_Init_FreeType( &library );
    if(error){

        return error;
    }
    else{
        return 0xBEEFl;
    }

}

