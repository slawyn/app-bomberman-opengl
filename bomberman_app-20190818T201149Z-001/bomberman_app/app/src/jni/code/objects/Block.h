//
// Created by Unixt on 6/22/2023.
//

#ifndef BLOCK_H
#define BLOCK_H
#include <jni.h>
#include "Hitbox.h"
#ifdef __cplusplus
extern "C" {
#endif
typedef struct
{
    int16_t ui16Id;
    int16_t i16PosX;
    int16_t i16PosY;
    uint8_t ui8State;
} Block_t;
int16_t i16BlockUpdateState(Block_t *pxBlock, int32_t dt);
int16_t i16BlockInit(Block_t * pxBlock, int16_t i16PositionX, int16_t i16PositionY);
void vBlockGetHitboxValues(Block_t* pxBlock, jint* jiHitboxes);
Hitbox_t * pxBlockGetHitbox(Block_t* pxBlock);
#ifdef __cplusplus
}
#endif
#endif //BOMBERMAN_APP_BLOCK_H
