//
// Created by Unixt on 6/22/2023.
//

#ifndef BOMB_H
#define BOMB_H
#include <jni.h>
#ifdef __cplusplus
extern "C" {
#endif
typedef struct
{
    int16_t ui16Id;
    int16_t i16PosX;
    int16_t i16PosY;
    uint16_t ui16BombCountdown;
    uint16_t ui16ExplosionStrength;
    uint16_t ui16Clip;
    uint16_t ui16IdOwner;
    uint8_t ui8State;
} Bomb_t;

int16_t i16BombUpdateState(Bomb_t *pxBomb, int32_t dt);
int16_t i16BombInit(Bomb_t * pxBomb,  int16_t i16PositionX, int16_t i16PositionY);
void vBombGetHitboxValues(Bomb_t* pxBomb, jint* jiHitboxes);
#ifdef __cplusplus
}
#endif
#endif //BOMBERMAN_APP_BOMB_H
