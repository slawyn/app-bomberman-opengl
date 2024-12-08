//
// Created by Unixt on 6/22/2023.
//

#include "States.h"
#include "Inputs.h"
#include "Config.h"
#include "Bomb.h"
#include "Hitbox.h"

#define BOMB_BOX_WIDTH                                           (CELLSIZE_X / 4.0f)
#define BOMB_BOX_HEIGHT                                          (CELL_RATIO * BOMB_BOX_WIDTH)

extern "C"
{

    static Hitbox_t xHitboxExplosionX = {
            .i16HalfSizeX = (uint16_t)0,
            .i16HalfSizeY = (uint16_t)0,
            .i16Top =0,
            .i16Left = 0,
            .i16Bottom = 0,
            .i16Right = 0,
            .pxHitboxNext = 0

    };

    static Hitbox_t xHitboxExplosionY = {
            .i16HalfSizeX = (uint16_t)0,
            .i16HalfSizeY = (uint16_t)0,
            .i16Top =0,
            .i16Left = 0,
            .i16Bottom = 0,
            .i16Right = 0,
            .pxHitboxNext = 0

    };


    static Hitbox_t xHitbox = {
            .i16HalfSizeX = (uint16_t)BOMB_BOX_WIDTH,
            .i16HalfSizeY = (uint16_t)BOMB_BOX_HEIGHT,
            .i16Top =0,
            .i16Left = 0,
            .i16Bottom = 0,
            .i16Right = 0,
            .pxHitboxNext = 0};

    void vBombGetHitboxValues(Bomb_t* pxBomb, int32_t *jiHitboxes)
    {
        vHitboxUpdateEdges(&xHitbox, pxBomb->object.i16PosX, pxBomb->object.i16PosY);
        jiHitboxes[0] = 0;
        jiHitboxes[1] = 0;
        jiHitboxes[2] = xHitbox.i16HalfSizeX;
        jiHitboxes[3] = xHitbox.i16HalfSizeY;
        jiHitboxes[4] = xHitbox.i16Left;
        jiHitboxes[5] = xHitbox.i16Bottom;
        jiHitboxes[6] = xHitbox.i16Right;
        jiHitboxes[7] = xHitbox.i16Top;
    }

    bool bBombHasExploded(Bomb_t* pxBomb)
    {
        return (pxBomb->object.ui8State == STATE_EXPLODED);
    }

    bool bBombNeedsToBeRemoved(Bomb_t* pxBomb)
    {
        return (pxBomb->object.ui8State == STATE_REMOVE);
    }

    void vExplosionGetHitboxValues(Bomb_t* pxBomb, int32_t *jiHitboxes)
    {
        jiHitboxes[0] = 0;
        jiHitboxes[1] = 0;
        jiHitboxes[2] = xHitbox.i16HalfSizeX;
        jiHitboxes[3] = xHitbox.i16HalfSizeY;
        jiHitboxes[4] = xHitbox.i16Left;
        jiHitboxes[5] = xHitbox.i16Bottom;
        jiHitboxes[6] = xHitbox.i16Right;
        jiHitboxes[7] = xHitbox.i16Top;


        xHitboxExplosionX.i16HalfSizeX = pxBomb->ui16ExplosionStrength*(CELLSIZE_X/2);
        xHitboxExplosionX.i16HalfSizeY = pxBomb->ui16ExplosionStrength*(CELLSIZE_Y/2);
        vHitboxUpdateEdges(&xHitboxExplosionX, pxBomb->object.i16PosX, pxBomb->object.i16PosY);


        xHitboxExplosionY.i16HalfSizeX = pxBomb->ui16ExplosionStrength*(CELLSIZE_X/2);
        xHitboxExplosionY.i16HalfSizeY = pxBomb->ui16ExplosionStrength*(CELLSIZE_Y/2);
        vHitboxUpdateEdges(&xHitboxExplosionY, pxBomb->object.i16PosX, pxBomb->object.i16PosY);

    }

    int32_t i32BombUpdateState(Bomb_t *pxBomb, int32_t dt)
{
        vHitboxUpdateEdges(&xHitbox, pxBomb->object.i16PosX, pxBomb->object.i16PosY);
        switch (pxBomb->object.ui8State) {
            case STATE_ALIVE:
                if ((pxBomb->ui16BombCountdown -= dt) <= 0) {
                    pxBomb->ui16BombCountdown = EXPLOSION_TIME;
                    pxBomb->object.ui8State = STATE_EXPLODED;
                }
                break;
            case STATE_EXPLODED:
                if ((pxBomb->ui16BombCountdown -= dt) <= 0) {
                    pxBomb->object.ui8State = STATE_DEAD;
                }
                break;
            case STATE_DEAD:
                pxBomb->object.ui8State = STATE_REMOVE;
                break;
        }
        return 1;
    }
int32_t i32BombInit(Bomb_t *pxBomb,  int16_t i16PositionX, int16_t i16PositionY)
{
    pxBomb->object.ui8State = STATE_ALIVE;
    pxBomb->ui16BombCountdown = BOMB_TIMER;
    pxBomb->ui16ExplosionStrength = PLAYER_BOMB_EXPLOSION_STRENGTH;
    pxBomb->ui16Clip = 1;
    pxBomb->object.i16PosX = i16PositionX;
    pxBomb->object.i16PosY = i16PositionY;
    return 1;
}


}
