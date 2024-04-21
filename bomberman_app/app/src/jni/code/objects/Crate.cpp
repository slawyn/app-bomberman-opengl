//
// Created by Unixt on 6/22/2023.
//

#include "Crate.h"
#include "../misc/States.h"
#include "../misc/Inputs.h"
#include "../misc/Hitbox.h"
#include "Config.h"

#define CRATE_BOX_WIDTH CELLSIZE_X / 2
#define CRATE_BOX_HEIGHT CELLSIZE_Y / 2
#define CRATE_BOX_OFFSET_X 0
#define CRATE_BOX_OFFSET_Y 0

extern "C"
{
    static Hitbox_t xHitbox = {
        .i16HalfSizeX = (uint16_t)CRATE_BOX_WIDTH,
        .i16HalfSizeY = (uint16_t)CRATE_BOX_HEIGHT,
        .i16Top = 0,
        .i16Left = 0,
        .i16Bottom = 0,
        .i16Right = 0,
        .pxHitboxNext = 0};

    Hitbox_t *pxCrateGetHitbox(Crate_t *pxCrate)
    {
        vHitboxUpdateEdges(&xHitbox, pxCrate->object.i16PosX, pxCrate->object.i16PosY);
        return &xHitbox;
    }

    void vCrateGetHitboxValues(Crate_t *pxCrate, jint *jiHitboxes)
    {
        vHitboxUpdateEdges(&xHitbox, pxCrate->object.i16PosX, pxCrate->object.i16PosY);
        jiHitboxes[0] = 0;
        jiHitboxes[1] = 0;
        jiHitboxes[2] = xHitbox.i16HalfSizeX;
        jiHitboxes[3] = xHitbox.i16HalfSizeY;
        jiHitboxes[4] = xHitbox.i16Left;
        jiHitboxes[5] = xHitbox.i16Bottom;
        jiHitboxes[6] = xHitbox.i16Right;
        jiHitboxes[7] = xHitbox.i16Top;
    }
    bool bCrateNeedsToBeRemoved(Crate_t *pxCrate)
    {
        return (pxCrate->object.ui8State == STATE_REMOVE);
    }

    int16_t i16CrateUpdateState(Crate_t *pxCrate, int32_t dt)
    {
        vHitboxUpdateEdges(&xHitbox, pxCrate->object.i16PosX, pxCrate->object.i16PosY);
        switch (pxCrate->object.ui8State)
        {
        case STATE_DEAD:
            pxCrate->object.ui8State = STATE_REMOVE;
            break;
        }
        return 1;
    }

    int16_t i16CrateInit(Crate_t *pxCrate, int16_t i16PositionX, int16_t i16PositionY)
    {
        pxCrate->object.ui8State = STATE_ALIVE;
        pxCrate->object.i16PosX = i16PositionX;
        pxCrate->object.i16PosY = i16PositionY;
        return 1;
    }
}