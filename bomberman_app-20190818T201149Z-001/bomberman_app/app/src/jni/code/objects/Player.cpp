//
// Created by Unixt on 6/22/2023.
//
#include "Player.h"
#include "../misc/States.h"
#include "../misc/Inputs.h"
#include "../misc/Hitbox.h"
#include "../../Config.h"
#include "Bomb.h"



// if player sprite is 180 x 180
#define PLAYER_BOX_RATIO_X                                      2.34f
#define PLAYER_BOX_RATIO_Y                                      0.78571f
#define PLAYER_BOX_WIDTH                                        ((CELLSIZE_X / PLAYER_BOX_RATIO_X)/2.0f)
#define PLAYER_BOX_HEIGHT                                       ((CELL_RATIO * PLAYER_BOX_WIDTH))
#define PLAYER_BOX_OFFSET_X                                     ((CELLSIZE_X / PLAYER_BOX_RATIO_X))
#define PLAYER_BOX_OFFSET_Y                                     (((PLAYER_BOX_OFFSET_X + CELLSIZE_X * PLAYER_BOX_RATIO_Y) * CELL_RATIO))

extern "C"
{
static Hitbox_t xHitbox = {
        .i16HalfSizeX = (uint16_t)PLAYER_BOX_WIDTH,
        .i16HalfSizeY = (uint16_t)PLAYER_BOX_HEIGHT,
        .i16Top =0,
        .i16Left = 0,
        .i16Bottom = 0,
        .i16Right = 0,
        .pxHitboxNext = 0};

void vPlayerGetCollisionPoints(Player_t* pxPlayer, int16_t x[4], int16_t y[4])
{
    vHitboxUpdateEdges(&xHitbox, pxPlayer->i16PosX, pxPlayer->i16PosY);
    // top left
    x[0] = ((xHitbox.i16Left- FIELD_X1) / CELLSIZE_X);
    y[0] = ((xHitbox.i16Top - FIELD_Y1) / CELLSIZE_Y);

    // top right
    x[1] = ((xHitbox.i16Right - FIELD_X1) / CELLSIZE_X);
    y[1] = ((xHitbox.i16Top - FIELD_Y1) / CELLSIZE_Y);

    // bottom right
    x[2] = ((xHitbox.i16Right - FIELD_X1) / CELLSIZE_X);
    y[2] = ((xHitbox.i16Bottom - FIELD_Y1) / CELLSIZE_Y);

    // bottom left
    x[3] = ((xHitbox.i16Left- FIELD_X1) / CELLSIZE_X);
    y[3] = ((xHitbox.i16Bottom - FIELD_Y1) / CELLSIZE_Y);
}

void vPlayerGetHitboxValues(Player_t * pxPlayer, jint *jiHitboxes) {
    vHitboxUpdateEdges(&xHitbox, pxPlayer->i16PosX, pxPlayer->i16PosY);
    jiHitboxes[0] = 0;
    jiHitboxes[1] = 0;
    jiHitboxes[2] = xHitbox.i16HalfSizeX;
    jiHitboxes[3] = xHitbox.i16HalfSizeY;
    jiHitboxes[4] = xHitbox.i16Left;
    jiHitboxes[5] = xHitbox.i16Bottom;
    jiHitboxes[6] = xHitbox.i16Right;
    jiHitboxes[7] = xHitbox.i16Top;
}

int16_t i16PlayerUpdateState(Player_t *pxPlayer, Bomb_t *pxBomb, int32_t dt) {

    switch (pxPlayer->ui16Input & INPUT_LOWER_NIBBLE) {
        case INPUT_MOVE_DOWN:
            pxPlayer->ui8State = STATE_MOVEDOWN;
            break;
        case INPUT_MOVE_UP:
            pxPlayer->ui8State = STATE_MOVEUP;
            break;
        case INPUT_MOVE_LEFT:
            pxPlayer->ui8State = STATE_MOVELEFT;
            break;
        case INPUT_MOVE_RIGHT:
            pxPlayer->ui8State = STATE_MOVERIGHT;
            break;
        case INPUT_NONE:
            pxPlayer->ui8State = STATE_ALIVE;
            break;
        default:
            break;
    }

    if ((pxPlayer->ui16Input & INPUT_HIGHER_NIBBLE) == INPUT_PLACE_BOMB)
    {
        /* Place a bomb */
        pxBomb->ui16BombCountdown = pxPlayer->ui16BombCountdown;
        pxBomb->ui16IdOwner = pxPlayer->ui16Id;
        pxBomb->ui16ExplosionStrength = pxPlayer->ui16ExplosionStrength;
        pxBomb->i16PosX = pxPlayer->i16PosX;
        pxBomb->i16PosY = pxPlayer->i16PosY;
    }

    /* Stay inside the field
     * ::Limit movement to the field
     * */
    pxPlayer->i16PreviousX = pxPlayer->i16PosX;
    pxPlayer->i16PreviousY = pxPlayer->i16PosY;
    int16_t i16SpeedDelta = (dt * pxPlayer->ui16BaseSpeed / 10);
    vHitboxUpdateEdges(&xHitbox, pxPlayer->i16PosX, pxPlayer->i16PosY);
    switch (pxPlayer->ui8State) {
        case STATE_MOVEDOWN:
            if (FIELD_Y2 <= (i16SpeedDelta + xHitbox.i16Bottom)) {
                i16SpeedDelta = FIELD_Y2 - xHitbox.i16Bottom;
            }

            pxPlayer->i16PosY += (i16SpeedDelta);
            break;
        case STATE_MOVEUP:
            if (FIELD_Y1 + i16SpeedDelta >= (xHitbox.i16Top)) {
                i16SpeedDelta = xHitbox.i16Top - FIELD_Y1;
            }
            pxPlayer->i16PosY -= (i16SpeedDelta);
            break;
        case STATE_MOVELEFT:
            if (FIELD_X1 + i16SpeedDelta >= (xHitbox.i16Left)) {
                i16SpeedDelta = xHitbox.i16Left - FIELD_X1;
            }
            pxPlayer->i16PosX -= (i16SpeedDelta);
            break;
        case STATE_MOVERIGHT:
            if (FIELD_X2 <= (i16SpeedDelta + xHitbox.i16Right)) {
                i16SpeedDelta = FIELD_X2 - xHitbox.i16Right;
            }
            pxPlayer->i16PosX += (i16SpeedDelta);
            break;
        case STATE_ALIVE:
            break;
        case STATE_DEAD:
            break;
    }
    vHitboxUpdateEdges(&xHitbox, pxPlayer->i16PosX, pxPlayer->i16PosY);
    return 1;
}

int16_t i16PlayerInit(Player_t *pxPlayer, int16_t i16PositionX, int16_t i16PositionY) {
    pxPlayer->ui16BombCountdown = BOMB_TIMER;
    pxPlayer->ui16BombTotalCount = PLAYER_BOMB_STARTING_AMOUNT;
    pxPlayer->ui16BaseSpeed = PLAYER_BASE_SPEED;
    pxPlayer->ui16ExplosionStrength = PLAYER_BOMB_EXPLOSION_STRENGTH;
    pxPlayer->ui8State = STATE_ALIVE;
    pxPlayer->i16PosX = i16PositionX;
    pxPlayer->i16PosY = i16PositionY;
    return 1;
}

jint jiCorrectPlayerPosition(Player_t *pxPlayer, Hitbox_t * pxHitbox)
{

    // horizontal
    int16_t dx = pxPlayer->i16PosX - pxPlayer->i16PreviousX;
    int16_t dy = pxPlayer->i16PosY - pxPlayer->i16PreviousY;

    vHitboxUpdateEdges(&xHitbox, pxPlayer->i16PosX, pxPlayer->i16PosY);
    if(dx>1)
    {
        pxPlayer->i16PosX = pxPlayer->i16PosX - (xHitbox.i16Right-pxHitbox->i16Left) -1;
    }
    else if(dx<-1)
    {
        pxPlayer->i16PosX = pxPlayer->i16PosX + (pxHitbox->i16Right- xHitbox.i16Left) + 1;
    }
    else if(dy>1)
    {
        pxPlayer->i16PosY = pxPlayer->i16PosY - (xHitbox.i16Bottom-pxHitbox->i16Top) - 1;
    }
        // vertical
    else if(dy<-1)
    {
        pxPlayer->i16PosY = pxPlayer->i16PosY + (pxHitbox->i16Bottom- xHitbox.i16Top) + 1;
    }


    //        int[] state = mGameStateBuffer.getStateData(mObjectType);
//        Hitbox box1 = mBoxes[0];
//        Hitbox box2 = mCollidedWith;
//
//        /* Minkowski sum, simplified */
//        int w = ((box1.sizeX + box2.sizeX));
//        int h = ((box1.sizeY + box2.sizeY));
//        int dx = ((box2.mLeft + box2.mRight) - (box1.mLeft + box1.mRight));
//        int dy = ((box2.mBottom + box2.mTop) - (box1.mTop + box1.mBottom));
//
//        /* Collision! */
//        int wy = (w * dy);
//        int hx = (h * dx);

    /*
    if (wy > hx)
        if (wy > -hx)        // collision at the mUp
            state[mObjectStateOffset + IDX_POSY] = ( box2.mTop - (box1.sizeY + box1.offsetY));
        else                // on the mRight
            state[mObjectStateOffset + IDX_POSX] = ( box2.mRight - box1.offsetX);
    else if (wy > -hx)      // on the mLeft
        state[mObjectStateOffset + IDX_POSX] = (box2.mLeft - (box1.sizeX + box1.offsetX));
    else                    // at the mDown
        state[mObjectStateOffset + IDX_POSY] = (box2.mBottom - box1.offsetY);
    */
    return 0;
}

}