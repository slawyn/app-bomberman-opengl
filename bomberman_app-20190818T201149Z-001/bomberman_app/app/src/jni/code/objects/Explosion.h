//
// Created by Unixt on 6/22/2023.
//

#ifndef EXPLOSION_H
#define EXPLOSION_H
#include <jni.h>
#ifdef __cplusplus
extern "C" {
#endif
typedef struct
{
    int16_t ui16Id;
    int16_t i16PosX;
    int16_t i16PosY;
    uint8_t ui8State;
} Explosion_t;
#ifdef __cplusplus
}
#endif
#endif //BOMBERMAN_APP_EXPLOSION_H
