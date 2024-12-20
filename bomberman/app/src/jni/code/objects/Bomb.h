//
// Created by Unixt on 6/22/2023.
//

#ifndef BOMB_H
#define BOMB_H
#include <stdint.h>
#include "Object.h"
#ifdef __cplusplus
extern "C" {
#endif
typedef struct
{
    Object_t object;
    uint16_t ui16BombCountdown;
    uint16_t ui16ExplosionStrength;
    uint16_t ui16Clip;
    uint16_t ui16IdOwner;
} Bomb_t;

int32_t i32BombUpdateState(Bomb_t *pxBomb, int32_t dt);
int32_t i32BombInit(Bomb_t * pxBomb,  int16_t i16PositionX, int16_t i16PositionY);
void vBombGetHitboxValues(Bomb_t* pxBomb, int32_t* jiHitboxes);
bool bBombNeedsToBeRemoved(Bomb_t *ptr);
bool bBombHasExploded(Bomb_t* pxBomb);
#ifdef __cplusplus
}
#endif
#endif //BOMBERMAN_APP_BOMB_H
