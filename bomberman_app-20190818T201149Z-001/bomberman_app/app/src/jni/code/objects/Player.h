//
// Created by Unixt on 6/22/2023.
//

#ifndef PLAYER_H
#define PLAYER_H
#include <jni.h>
#include "Hitbox.h"
#include "Bomb.h"

#ifdef __cplusplus
extern "C" {
#endif
typedef struct
{
    uint16_t ui16Id;
    int16_t i16PosX;
    int16_t i16PosY;
    int16_t i16PreviousX;
    int16_t i16PreviousY;
    uint16_t ui16BaseSpeed;
    uint16_t ui16BombTotalCount;
    uint16_t ui16BombAvailableCount;
    uint16_t ui16BombCountdown;
    uint16_t ui16ExplosionStrength;
    uint16_t ui16Input;
    uint8_t ui8State;
} Player_t;

void vPlayerGetCollisionPoints(Player_t* pxPlayer, int16_t x[4], int16_t y[4]);
int16_t i16PlayerUpdateState(Player_t *pxPlayer, Bomb_t *pxBomb, int32_t dt);
int16_t i16PlayerInit(Player_t * pxPlayer, int16_t i16PositionX, int16_t i16PositionY);
void vPlayerGetHitboxValues(Player_t * pxPlayer, jint *jiHitboxes);
jint jiCorrectPlayerPosition(Player_t *pxPlayer, Hitbox_t * pxHitbox);
#ifdef __cplusplus
}
#endif
#endif //BOMBERMAN_APP_PLAYER_H