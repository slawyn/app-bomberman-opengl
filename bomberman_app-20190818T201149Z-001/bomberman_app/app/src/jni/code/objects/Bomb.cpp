//
// Created by Unixt on 6/22/2023.
//

#include "../misc/States.h"
#include "../misc/Inputs.h"
#include "../../Config.h"
#include "Bomb.h"
#include "../misc/Hitbox.h"

#define BOMB_BOX_WIDTH                                           ((CELLSIZE_X / 2.34f)/2.0f)
#define BOMB_BOX_HEIGHT                                          (CELL_RATIO * BOMB_BOX_WIDTH)
#define BOMB_BOX_OFFSET_X                                        ((CELLSIZE_X - BOMB_BOX_WIDTH) / 2)
#define BOMB_BOX_OFFSET_Y                                        (BOMB_BOX_OFFSET_X * CELL_RATIO)

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

    void vBombGetHitboxValues(Bomb_t* pxBomb, jint *jiHitboxes)
    {
        vHitboxUpdateEdges(&xHitbox, pxBomb->i16PosX, pxBomb->i16PosY);
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
        return (pxBomb->ui8State == STATE_EXPLODED);
    }

    bool bBombNeedsToBeRemoved(Bomb_t* pxBomb)
    {
        return (pxBomb->ui8State == STATE_REMOVE);
    }

    void vExplosionGetHitboxValues(Bomb_t* pxBomb, jint *jiHitboxes)
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
        vHitboxUpdateEdges(&xHitboxExplosionX, pxBomb->i16PosX, pxBomb->i16PosY);


        xHitboxExplosionY.i16HalfSizeX = pxBomb->ui16ExplosionStrength*(CELLSIZE_X/2);
        xHitboxExplosionY.i16HalfSizeY = pxBomb->ui16ExplosionStrength*(CELLSIZE_Y/2);
        vHitboxUpdateEdges(&xHitboxExplosionY, pxBomb->i16PosX, pxBomb->i16PosY);

    }

    int16_t i16BombUpdateState(Bomb_t *pxBomb, int32_t dt)
    {
        vHitboxUpdateEdges(&xHitbox, pxBomb->i16PosX, pxBomb->i16PosY);
        switch (pxBomb->ui8State) {
            case STATE_ALIVE:
                if ((pxBomb->ui16BombCountdown -= dt) <= 0) {
                    pxBomb->ui16BombCountdown = EXPLOSION_TIME;
                    pxBomb->ui8State = STATE_EXPLODED;
                }
                break;
            case STATE_EXPLODED:
                if ((pxBomb->ui16BombCountdown -= dt) <= 0) {
                    pxBomb->ui8State = STATE_DEAD;
                }
                break;
            case STATE_DEAD:
                pxBomb->ui8State = STATE_REMOVE;
                break;
        }
        return 1;
    }
int16_t i16BombInit(Bomb_t *pxBomb,  int16_t i16PositionX, int16_t i16PositionY)
{
    pxBomb->ui8State = STATE_ALIVE;
    pxBomb->ui16BombCountdown = BOMB_TIMER;
    pxBomb->ui16ExplosionStrength = PLAYER_BOMB_EXPLOSION_STRENGTH;
    pxBomb->ui16Clip = 1;
    pxBomb->i16PosX = i16PositionX;
    pxBomb->i16PosY = i16PositionY;
    return 1;
}


}
