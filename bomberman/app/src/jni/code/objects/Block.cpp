//
// Created by Unixt on 6/22/2023.
//

#include "States.h"
#include "Inputs.h"
#include "Hitbox.h"
#include "Config.h"
#include "Block.h"

// if object_block is 150 x 150
#define BLOCK_BOX_WIDTH                                         (CELLSIZE_X/2)
#define BLOCK_BOX_HEIGHT                                        (CELL_RATIO * BLOCK_BOX_WIDTH)
extern "C"
{
    static Hitbox_t xHitbox = {
            .i16HalfSizeX = (uint16_t) BLOCK_BOX_WIDTH,
            .i16HalfSizeY = (uint16_t) BLOCK_BOX_HEIGHT,
            .i16Top =0,
            .i16Left = 0,
            .i16Bottom = 0,
            .i16Right = 0,
            .pxHitboxNext = 0};


    Hitbox_t * pxBlockGetHitbox(Block_t* pxBlock)
    {
        vHitboxUpdateEdges(&xHitbox, pxBlock->object.i16PosX, pxBlock->object.i16PosY);
        return &xHitbox;
    }

    void vBlockGetHitboxValues(Block_t* pxBlock, int32_t *jiHitboxes) {
        vHitboxUpdateEdges(&xHitbox, pxBlock->object.i16PosX, pxBlock->object.i16PosY);
        jiHitboxes[0] = 0;
        jiHitboxes[1] = 0;
        jiHitboxes[2] = xHitbox.i16HalfSizeX;
        jiHitboxes[3] = xHitbox.i16HalfSizeY;
        jiHitboxes[4] = xHitbox.i16Left;
        jiHitboxes[5] = xHitbox.i16Bottom;
        jiHitboxes[6] = xHitbox.i16Right;
        jiHitboxes[7] = xHitbox.i16Top;
    }

    int32_t i32BlockUpdateState(Block_t *pxBlock, int32_t dt) {
        (void)dt;
        vHitboxUpdateEdges(&xHitbox, pxBlock->object.i16PosX, pxBlock->object.i16PosY);
        switch(pxBlock->object.ui8State)
        {
            case STATE_DEAD:
                pxBlock->object.ui8State = STATE_REMOVE;
                break;
        }
        return 1;
    }

    int32_t i32BlockInit(Block_t *pxBlock, int16_t i16PositionX, int16_t i16PositionY) {
        pxBlock->object.ui8State = STATE_ALIVE;
        pxBlock->object.i16PosX = i16PositionX;
        pxBlock->object.i16PosY = i16PositionY;
        return 1;
    }
}