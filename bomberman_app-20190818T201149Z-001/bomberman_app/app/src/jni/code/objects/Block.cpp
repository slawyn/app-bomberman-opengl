//
// Created by Unixt on 6/22/2023.
//

#include "../misc/States.h"
#include "../misc/Inputs.h"
#include "../misc/Hitbox.h"
#include "Config.h"
#include "Block.h"

// if object_block is 150 x 150
#define BLOCK_BOX_WIDTH                                         CELLSIZE_X/2
#define BLOCK_BOX_HEIGHT                                        CELLSIZE_Y/2
#define BLOCK_BOX_OFFSET_X                                      0
#define BLOCK_BOX_OFFSET_Y                                      0
extern "C"
{
    static Hitbox_t xHitbox = {.i16OffsetX = (uint16_t) BLOCK_BOX_OFFSET_X,
            .i16OffsetY = (uint16_t) BLOCK_BOX_OFFSET_Y,
            .i16HalfSizeX = (uint16_t) BLOCK_BOX_WIDTH,
            .i16HalfSizeY = (uint16_t) BLOCK_BOX_HEIGHT,
            .i16Top =0,
            .i16Left = 0,
            .i16Bottom = 0,
            .i16Right = 0,
            .pxHitboxNext = 0};


    Hitbox_t * pxBlockGetHitbox(Block_t* pxBlock)
    {
        vHitboxUpdateEdges(&xHitbox, pxBlock->i16PosX, pxBlock->i16PosY);
        return &xHitbox;
    }

    void vBlockGetHitboxValues(Block_t* pxBlock, jint *jiHitboxes) {
        vHitboxUpdateEdges(&xHitbox, pxBlock->i16PosX, pxBlock->i16PosY);
        jiHitboxes[0] = xHitbox.i16OffsetX;
        jiHitboxes[1] = xHitbox.i16OffsetY;
        jiHitboxes[2] = xHitbox.i16HalfSizeX;
        jiHitboxes[3] = xHitbox.i16HalfSizeY;
        jiHitboxes[4] = xHitbox.i16Left;
        jiHitboxes[5] = xHitbox.i16Bottom;
        jiHitboxes[6] = xHitbox.i16Right;
        jiHitboxes[7] = xHitbox.i16Top;
    }

    int16_t i16BlockUpdateState(Block_t *pxBlock, int32_t dt) {
        vHitboxUpdateEdges(&xHitbox, pxBlock->i16PosX, pxBlock->i16PosY);
        return 1;
    }

    int16_t i16BlockInit(Block_t *pxBlock, int16_t i16PositionX, int16_t i16PositionY) {
        pxBlock->ui8State = STATE_ALIVE;
        pxBlock->i16PosX = i16PositionX;
        pxBlock->i16PosY = i16PositionY;
        return 1;
    }
}