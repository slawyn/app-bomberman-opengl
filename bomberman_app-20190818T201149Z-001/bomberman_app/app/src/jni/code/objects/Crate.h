//
// Created by Unixt on 6/22/2023.
//

#ifndef CRATE_H
#define CRATE_H
#include <jni.h>
#include "Hitbox.h"
#include "Object.h"
#ifdef __cplusplus
extern "C" {
#endif
typedef struct
{
    Object_t object;
} Crate_t;

int16_t i16CrateUpdateState(Crate_t *pxCrate, int32_t dt);
int16_t i16CrateInit(Crate_t * pxCrate, int16_t i16PositionX, int16_t i16PositionY);
void vCrateGetHitboxValues(Crate_t* pxCrate, jint *jiHitboxes);
Hitbox_t * pxCrateGetHitbox(Crate_t* pxCrate);
bool bCrateNeedsToBeRemoved(Crate_t* pxCrate);
#ifdef __cplusplus
}
#endif
#endif //BOMBERMAN_APP_CRATE_H
