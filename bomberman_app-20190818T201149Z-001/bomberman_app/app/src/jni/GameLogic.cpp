//
// Created by slaw on 5/10/2020.
//


#include <stdio.h>      // C Standard IO Header
#include "ft2build.h"
#include "freetype/freetype.h"
#include FT_FREETYPE_H

#include "GameLogic.h"
#include "code/misc/Hitbox.h"
#include "code/objects/Player.h"
#include "code/objects/Bomb.h"
#include "code/objects/Block.h"
#include "code/objects/Crate.h"
#include "code/objects/Explosion.h"
#include "code/misc/Level.h"
#include "Config.h"
#include "States.h"


extern "C"
{
#define GET_CURRENT_OBJECT_STATES(x)\
    Bomb_t* pxBomb = &pxBombs[x];\
    Player_t* pxPlayer = &pxPlayers[x];\
    Block_t* pxBlock = &pxBlocks[x];\
    Crate_t* pxCrate = &pxCrates[x];

FT_Library library;
/* Constants ------------------------------------               ---------- */

static Player_t rxPlayers[TOTAL_STATE_COUNT][PLAYER_COUNT_MAX]      = {0};
static Bomb_t rxBombs[TOTAL_STATE_COUNT][BOMB_COUNT_MAX]            = {0};
static Crate_t rxCrates[TOTAL_STATE_COUNT][CRATE_COUNT_MAX]         = {0};
static Block_t rxBlocks[TOTAL_STATE_COUNT][BLOCK_COUNT_MAX]         = {0};
static Explosion_t rxExplosion[TOTAL_STATE_COUNT][BOMB_COUNT_MAX]   = {0};
static uint32_t ui32GameTicker = GAME_TICKER_START;

/* Set pointers to Objects */
Player_t* pxPlayers = rxPlayers[GAME_TICKER_START];
Bomb_t* pxBombs = rxBombs[GAME_TICKER_START];
Crate_t* pxCrates = rxCrates[GAME_TICKER_START];
Block_t* pxBlocks = rxBlocks[GAME_TICKER_START];
Explosion_t* pxExplosion = rxExplosion[GAME_TICKER_START];



uint16_t ui16FieldMap[NUMBER_OF_X_CELLS][NUMBER_OF_Y_CELLS] = {0};
int16_t STARTING_CELL_POSITIONS[2][2]  = {{0, 0}, {10, 0}};


int16_t i16InitObject(int32_t i32ObjectStateOffset,
                int32_t i32ObjType,
                int32_t i32PositionX,
                int32_t i32PositionY)
{
    GET_CURRENT_OBJECT_STATES(i32ObjectStateOffset)
    switch(i32ObjType)
    {
        case OBJ_PLAYR:
            pxPlayer->ui16Id = ID(i32ObjectStateOffset, OBJ_PLAYR);
            i16PlayerInit(pxPlayer, i32PositionX, i32PositionY);
            break;
        case OBJ_BLOCK:
            pxBlock->ui16Id = ID(i32ObjectStateOffset, OBJ_BLOCK);
            i16BlockInit(pxBlock, i32PositionX, i32PositionY);
            break;
        case OBJ_CRATE:
            pxCrate->ui16Id = ID(i32ObjectStateOffset, OBJ_CRATE);
            i16CrateInit(pxCrate, i32PositionX, i32PositionY);
            break;
        case OBJ_BOMB:
            pxBomb->ui16Id = ID(i32ObjectStateOffset, OBJ_BOMB);
            i16BombInit(pxBomb, i32PositionX, i32PositionY);
            break;
        case OBJ_EXPLN:
            //createBoundingBoxes(2);
            /*

            mEpicenterX = cellx;
            mEpicenterY = celly;
            timer = EXPLOSION_TIME;
            owner = owner_;
            mStrength = strength_;
            mLeft = radius[0];
            mRight = radius[1];
            mUp = radius[2];
            mDown = radius[3];
            addBoundingBox(0,(-mLeft * CELLSIZE_X),  0,((mRight + mLeft + 1) * CELLSIZE_X), (CELLSIZE_Y));
            addBoundingBox(1, (0), (-mUp * CELLSIZE_Y), (CELLSIZE_X), ((mUp + mDown + 1) * CELLSIZE_Y));
             */
            break;
    }
    return 0;
}



int16_t jiInitMap(int16_t ri16Levels[NUMBER_OF_Y_CELLS][NUMBER_OF_X_CELLS])
{


    int32_t i = 0;
    int16_t i16BlockCount = 0;
    int16_t i16CrateCount = 0;
    while( i < NUMBER_OF_Y_CELLS)
    {

        for(int32_t j = 0; j < NUMBER_OF_X_CELLS; j++)
        {
            int16_t ri16Positions[2];
            i16LevelGetPositionFromCellXY(ri16Positions, j, i);
            if(ri16Levels[i][j] == BK)
            {
                i16InitObject(i16BlockCount, OBJ_BLOCK, ri16Positions[0], ri16Positions[1]);
                ++i16BlockCount;
            }
            else if(ri16Levels[i][j] == CR)
            {
                i16InitObject(i16CrateCount, OBJ_CRATE, ri16Positions[0], ri16Positions[1]);
                ++i16CrateCount;
            }
            else
            {
                // Do nothing
            }
        }

        i++;
    }
    return 0;
}



/* Update according to priority which is set by the type: mPlayers > Bombs > Explosions> Crates > Blocks > Items */
// Get Updated Objects
jint jiUpdateObjects(jint ji32Dt, uint32_t *ui32PlayerUpdates)
{
    /* Get all sets */
    GET_CURRENT_OBJECT_STATES(0)

    /* Update blocks */
    uint32_t ui32Idx = 0;
    jint jiHitbox [8] = {0};
    while(pxBlock->ui16Id)
    {
        i16BlockUpdateState(pxBlock,ji32Dt);

        /* Place in the map */
        vBlockGetHitboxValues(pxBlock, jiHitbox);
        int16_t x = (jint)(((jiHitbox[4] + jiHitbox[2] / 2) - FIELD_X1) / CELLSIZE_X);
        int16_t y = (jint)(((jiHitbox[7] + jiHitbox[3]/ 2) - FIELD_Y1) / CELLSIZE_Y);

        ui16FieldMap[x][y] = pxBlock->ui16Id;

        /* Go to the next one*/
        pxBlock = &(pxBlocks[++ui32Idx]);
    }
    ui32Idx = 0;
    while(pxCrate->ui16Id)
    {
        i16CrateUpdateState(pxCrate,ji32Dt);

        /* Place in the map */
        vCrateGetHitboxValues(pxCrate, jiHitbox);
        int16_t x = (jint)(((jiHitbox[4] + jiHitbox[2] / 2) - FIELD_X1) / CELLSIZE_X);
        int16_t y = (jint)(((jiHitbox[7] + jiHitbox[3]/ 2) - FIELD_Y1) / CELLSIZE_Y);

        ui16FieldMap[x][y] = pxCrate->ui16Id;

        /* Go to the next one*/
        pxCrate = &(pxCrates[++ui32Idx]);
    }

    /* Update players */
    ui32Idx = 0;
    int16_t x[4];
    int16_t y[4];


    while(pxPlayer->ui16Id)
    {
#define DEBUG_FIRST_BOMB            (0)
        Bomb_t xBomb={0};
        if(i16PlayerUpdateState(pxPlayer, &xBomb, ji32Dt))
        {
            *ui32PlayerUpdates = (*ui32PlayerUpdates |(1<<ui32Idx));
            if(xBomb.ui16IdOwner)
            {
                vBombGetHitboxValues(&xBomb, jiHitbox);
                int16_t x = (jint)(((jiHitbox[4] + jiHitbox[2] / 2) - FIELD_X1) / CELLSIZE_X);
                int16_t y = (jint)(((jiHitbox[7] + jiHitbox[3]/ 2) - FIELD_Y1) / CELLSIZE_Y);
                if(ui16FieldMap[x][y] == O_)
                {
                    i16InitObject(DEBUG_FIRST_BOMB, OBJ_BOMB, xBomb.i16PosX,xBomb.i16PosY);
                    ui16FieldMap[x][y] = (&pxBombs[DEBUG_FIRST_BOMB])->ui16Id;
                }
            }
        }

        /* Check collision
         * 0: Most right
         * 1: Most left
         * 2: Most bottom+
         * 3: Most top
         * */
        vPlayerGetCollisionPoints(pxPlayer, x,y);

        volatile uint8_t ui8IdxCell = 0;
        while(ui8IdxCell<4)
        {
            /* Collision with an object */
            uint16_t ui16IdCollide = ui16FieldMap[x[ui8IdxCell]][y[ui8IdxCell]];

            Hitbox_t* pxHitbox = NULL;
            switch(ui16IdCollide&OBJ_MASK)
            {
                case OBJ_CRATE:
                    pxHitbox = pxCrateGetHitbox(&pxCrates[ui16IdCollide&(~OBJ_MASK)]);
                    jiCorrectPlayerPosition(pxPlayer,pxHitbox);
                    break;
                case OBJ_BLOCK:
                    // ** Correct position of the player: TODO correct it to the brim
                    pxHitbox = pxBlockGetHitbox(&pxBlocks[ui16IdCollide&(~OBJ_MASK)]);
                    jiCorrectPlayerPosition(pxPlayer,pxHitbox);
                    break;
                default:
                    break;
            }
            ++ui8IdxCell;
        }


        pxPlayer = &(pxPlayers[++ui32Idx]);
    }

    ui32Idx = 0;
    while(pxBomb->ui16Id)
    {
        i16BombUpdateState(pxBomb, ji32Dt);

        /* Place in the map */
        vBombGetHitboxValues(pxBomb, jiHitbox);
        int16_t x = (jint)(((jiHitbox[4] + jiHitbox[2] / 2) - FIELD_X1) / CELLSIZE_X);
        int16_t y = (jint)(((jiHitbox[7] + jiHitbox[3]/ 2) - FIELD_Y1) / CELLSIZE_Y);
        ui16FieldMap[x][y] = pxBomb->ui16Id;

        pxBomb = &(pxBombs[++ui32Idx]);
    }

    return 0;
}


JNIEXPORT jintArray JNICALL
Java_main_nativeclasses_GameLogic_getObjects(JNIEnv * env,
                                            jclass type) {
    int16_t i16TotalCount = 0;
    int16_t i16LocalCount = 0;

    jint ui32Objects[PLAYER_COUNT_MAX+BLOCK_COUNT_MAX+CRATE_COUNT_MAX] = {0};
    while(i16LocalCount<PLAYER_COUNT_MAX)
    {
        if(pxPlayers[i16LocalCount].ui16Id)
        {
            ui32Objects[i16TotalCount++] = pxPlayers[i16LocalCount].ui16Id;
        }

        ++i16LocalCount;
    }

    i16LocalCount = 0;
    while(i16LocalCount<BLOCK_COUNT_MAX)
    {
        if(pxBlocks[i16LocalCount].ui16Id)
        {
            ui32Objects[i16TotalCount++] = pxBlocks[i16LocalCount].ui16Id;
        }
        ++i16LocalCount;
    }

    i16LocalCount = 0;
    while(i16LocalCount<CRATE_COUNT_MAX)
    {
        if(pxCrates[i16LocalCount].ui16Id)
        {
            ui32Objects[i16TotalCount++] = pxCrates[i16LocalCount].ui16Id;
        }
        ++i16LocalCount;
    }

    i16LocalCount = 0;
    while(i16LocalCount<BOMB_COUNT_MAX)
    {
        if(pxBombs[i16LocalCount].ui16Id)
        {
            ui32Objects[i16TotalCount++] = pxBombs[i16LocalCount].ui16Id;
        }
        ++i16LocalCount;
    }

    jintArray jArray = env -> NewIntArray(i16TotalCount);
    env->SetIntArrayRegion(jArray, 0, i16TotalCount, ui32Objects);
    return jArray;
}

JNIEXPORT jintArray JNICALL
Java_main_nativeclasses_GameLogic_getRemovedObjects(JNIEnv * env,
                                             jclass type) {
    jint ui32Objects[PLAYER_COUNT_MAX+BLOCK_COUNT_MAX+CRATE_COUNT_MAX] = {0};
    int16_t i16LocalCount = 0;
    int16_t i16TotalCount = 0;
    while(i16LocalCount<BOMB_COUNT_MAX)
    {
        if(pxBombs[i16LocalCount].ui8State == STATE_REMOVE)
        {
            ui32Objects[i16TotalCount++] = pxBombs[i16LocalCount].ui16Id;
            pxBombs[i16LocalCount].ui16Id = 0;
            pxBombs[i16LocalCount].ui8State = 0;
        }
        ++i16LocalCount;
    }

    jintArray jArray = env -> NewIntArray(i16TotalCount);
    env->SetIntArrayRegion(jArray, 0, i16TotalCount, ui32Objects);
    return jArray;
}

JNIEXPORT jint JNICALL
Java_main_nativeclasses_GameLogic_createGame(JNIEnv * env,
                                             jclass type) {

#define DEBUG_CONSTANT_LEVEL            (0)
#define DEBUG_CONSTANT_PLAYER_POS       (0)
    jiInitMap(LEVELS[DEBUG_CONSTANT_LEVEL]);

    int16_t ri16Positions[2];
    i16LevelGetPositionFromCellXY(ri16Positions, STARTING_CELL_POSITIONS[0][1],
                                  STARTING_CELL_POSITIONS[0][0]);
    i16InitObject(DEBUG_CONSTANT_PLAYER_POS, OBJ_PLAYR, ri16Positions[0], ri16Positions[1]);

    return 0;
}

JNIEXPORT jint JNICALL
Java_main_nativeclasses_GameLogic_updateGame(JNIEnv * env,
                                       jclass type,
                                        jint ji32Dt)
{

    /* Update game objects */
    uint32_t ui32UpdatedPlayers = 0;
    jiUpdateObjects(ji32Dt, &ui32UpdatedPlayers);

    return 0;
}


JNIEXPORT jint JNICALL
Java_main_nativeclasses_GameLogic_getZ(JNIEnv * env,
                     jclass type,
                     jint i32ObjType,
                     jint i32ObjectStateOffset)
    {
        GET_CURRENT_OBJECT_STATES(i32ObjectStateOffset)
        jint ar[] = {0,0,0,0, 0,0,0,0};
        switch(i32ObjType) {
            case OBJ_PLAYR:
                vPlayerGetHitboxValues(pxPlayer, ar);
                break;
            case OBJ_BLOCK:
                vBlockGetHitboxValues(pxBlock, ar);
                break;
            case OBJ_CRATE:
                vCrateGetHitboxValues(pxCrate, ar);
                break;
            case OBJ_BOMB:
                vBombGetHitboxValues(pxBomb, ar);
                break;
            case OBJ_EXPLN:
                break;
        }

        int16_t top = ar[7];
        return top;
    }




JNIEXPORT void JNICALL
Java_main_nativeclasses_GameLogic_setInput(JNIEnv * env,
                         jclass type,
                         jint i32ObjType,
                         jint i32ObjectStateOffset,
                         jbyte ui8Input)
    {
        Player_t* pxPlayer = &pxPlayers[i32ObjectStateOffset];
        pxPlayer->ui16Input = ui8Input;
    }


jint jiCheckCollisions(uint32_t ui32Players)
{

        // Check 8 nearest cells against collision
        int16_t cellpos[] = {0, 0};
        int16_t cellposx = cellpos[0];
        int16_t cellposy = cellpos[1];
        for(int b = cellposx - 1; b <= (cellposx + 1); b++)
        {
            /* horizontally inside the map */
            if(b >= 0 && b < NUMBER_OF_X_CELLS)
            {
                for(int c = cellposy - 1; c <= (cellposy + 1); c++)
                {
                    /* Vertically inside the map */
                    if(c >= 0 && c < NUMBER_OF_Y_CELLS)
                    {
                        int id = ui16FieldMap[b][c];
                        if(id != 0)
                        {
                            //GameElement go = mGameObjects.get(id);
                            //if(playr.collisionCheck(go))
                            {
                                //return go;
                            }
                        }
                    }
                }
            }
        }
    }

    JNIEXPORT jlongArray JNICALL
        Java_main_nativeclasses_GameLogic_getPosition(JNIEnv * env,
                                                         jclass type,
                                                         jint i32ObjType,
                                                         jint i32ObjectStateOffset)
    {

        GET_CURRENT_OBJECT_STATES(i32ObjectStateOffset)

        jlong ui32Positions[2] = {0,0};
        switch(i32ObjType) {
            case OBJ_PLAYR:
                ui32Positions[0] = pxPlayer->i16PosX;
                ui32Positions[1] = pxPlayer->i16PosY;
                break;
            case OBJ_BLOCK:
                ui32Positions[0] = pxBlock->i16PosX;
                ui32Positions[1] = pxBlock->i16PosY;
                break;
            case OBJ_CRATE:
                ui32Positions[0] = pxCrate->i16PosX;
                ui32Positions[1] = pxCrate->i16PosY;
                break;
            case OBJ_BOMB:
                ui32Positions[0] = pxBomb->i16PosX;
                ui32Positions[1] = pxBomb->i16PosY;
                break;
            case OBJ_EXPLN:
                break;
        }

        jlongArray jArray = env -> NewLongArray(2);
        env->SetLongArrayRegion(jArray, 0, 2, ui32Positions);
        return jArray;
    }

    JNIEXPORT jint JNICALL
    Java_main_nativeclasses_GameLogic_getState(JNIEnv * env,
                                                     jclass type,
                                                     jint i32ObjType,
                                                     jint i32ObjectStateOffset)
    {
        GET_CURRENT_OBJECT_STATES(i32ObjectStateOffset)

        uint32_t ui32State = 0;
        switch(i32ObjType) {
            case OBJ_PLAYR:
                ui32State = pxPlayer->ui8State;
                break;
            case OBJ_BLOCK:
                ui32State = pxBlock->ui8State;
                break;
            case OBJ_CRATE:
                ui32State = pxCrate->ui8State;
                break;
            case OBJ_BOMB:
                ui32State = pxBomb->ui8State;
                break;
            case OBJ_EXPLN:
                break;
        }

        return ui32State;
    }

    JNIEXPORT jint JNICALL
    Java_main_nativeclasses_GameLogic_updateGameTicker(JNIEnv *env,
                                     jclass type)
    {
        uint32_t ui32PreviousTick = ui32GameTicker;
        ui32GameTicker = (ui32GameTicker + 1) % TOTAL_STATE_COUNT;

        /* Copy */
        memcpy(rxPlayers[ui32GameTicker], rxPlayers[ui32PreviousTick],
               sizeof(Player_t) * PLAYER_COUNT_MAX);
        memcpy(rxBombs[ui32GameTicker], rxBombs[ui32PreviousTick], sizeof(Bomb_t) * BOMB_COUNT_MAX);
        memcpy(rxCrates[ui32GameTicker], rxCrates[ui32PreviousTick],
               sizeof(Crate_t) * CRATE_COUNT_MAX);
        memcpy(rxBlocks[ui32GameTicker], rxBlocks[ui32PreviousTick],
               sizeof(Block_t) * BLOCK_COUNT_MAX);
        memcpy(rxExplosion[ui32GameTicker], rxExplosion[ui32PreviousTick],
               sizeof(Explosion_t) * BOMB_COUNT_MAX);

        pxPlayers = rxPlayers[ui32GameTicker];
        pxBombs = rxBombs[ui32GameTicker];
        pxCrates = rxCrates[ui32GameTicker];
        pxBlocks = rxBlocks[ui32GameTicker];
        pxExplosion = rxExplosion[ui32GameTicker];

        /* Reset position */
        memset(ui16FieldMap, 0, sizeof(ui16FieldMap[0][0])*(NUMBER_OF_X_CELLS)*(NUMBER_OF_Y_CELLS));
        return 0;
    }

JNIEXPORT jobjectArray  JNICALL
Java_main_nativeclasses_GameLogic_getHitboxes(JNIEnv *env,
                                                            jclass type,
                                                            jint i32ObjType,
                                                            jint i32ObjectStateOffset)
    {

        jobjectArray array2D = env->NewObjectArray(
                1, env->FindClass("[I"), NULL);
        jintArray array1D = env->NewIntArray(4);
        env->SetObjectArrayElement(array2D, 0, array1D);

        GET_CURRENT_OBJECT_STATES(i32ObjectStateOffset)
        jint i32Hitbox[] =  {0,0,0,0, 0,0,0,0};
        switch(i32ObjType) {
            case OBJ_PLAYR:
                vPlayerGetHitboxValues(pxPlayer, i32Hitbox);
                break;
            case OBJ_BLOCK:
                vBlockGetHitboxValues(pxBlock, i32Hitbox);
                break;
            case OBJ_CRATE:
                vCrateGetHitboxValues(pxCrate, i32Hitbox);
                break;
            case OBJ_BOMB:
                vBombGetHitboxValues(pxBomb, i32Hitbox);
                break;
            case OBJ_EXPLN:
                break;
        }
        env->SetIntArrayRegion(array1D, 0, 4, i32Hitbox);
        return array2D;
    }

    JNIEXPORT jlong JNICALL
    Java_main_nativeclasses_GameLogic_initFreeType(JNIEnv *env,
                                 jclass type)
    {

        uint32_t error = 0;//FT_Init_FreeType(&library);
        if (error) {
            return error;
        } else {
            return 0xBEEFl;
        }
    }

}

