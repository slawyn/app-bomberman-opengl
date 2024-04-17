//
// Created by slaw on 5/10/2020.
//

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
#include <stdio.h>
#include <string.h>

extern "C"
{

    typedef struct
    {
        int16_t ui16Id;
        int16_t i16PosX;
        int16_t i16PosY;
        uint8_t ui8State; 
    } Object_t;

    typedef void (*xUpdaterFunction)(Object_t *pxObject, int32_t dt);
    typedef void (*xHitboxFunction)(Object_t * pxObject, jint *jiHitboxes);

    static void vUpdateSimpleObjects(jint ji32Dt, Object_t *pxObject, xUpdaterFunction pUpFunction, xHitboxFunction pHbFunction);
   
#define GET_CURRENT_OBJECT_STATES(x)    \
    Bomb_t *pxBomb = &pxBombs[x];       \
    Player_t *pxPlayer = &pxPlayers[x]; \
    Block_t *pxBlock = &pxBlocks[x];    \
    Crate_t *pxCrate = &pxCrates[x];
    
    /* Constants ------------------------------------               ---------- */

    static Player_t rxPlayers[TOTAL_STATE_COUNT][PLAYER_COUNT_MAX] = {0};
    static Bomb_t rxBombs[TOTAL_STATE_COUNT][BOMB_COUNT_MAX] = {0};
    static Crate_t rxCrates[TOTAL_STATE_COUNT][CRATE_COUNT_MAX] = {0};
    static Block_t rxBlocks[TOTAL_STATE_COUNT][BLOCK_COUNT_MAX] = {0};
    static Explosion_t rxExplosion[TOTAL_STATE_COUNT][BOMB_COUNT_MAX] = {0};
    static uint32_t ui32GameTicker = GAME_TICKER_START;

    /* Set pointers to Objects */
    Player_t *pxPlayers = rxPlayers[GAME_TICKER_START];
    Bomb_t *pxBombs = rxBombs[GAME_TICKER_START];
    Crate_t *pxCrates = rxCrates[GAME_TICKER_START];
    Block_t *pxBlocks = rxBlocks[GAME_TICKER_START];
    Explosion_t *pxExplosion = rxExplosion[GAME_TICKER_START];

    uint16_t ui16FieldMap[NUMBER_OF_X_CELLS][NUMBER_OF_Y_CELLS] = {0};
    int16_t STARTING_CELL_POSITIONS[2][2] = {{0, 0},
                                             {10, 0}};

    jint pi32Hitbox[8] = {10, 10, 10, 10, 0, 0, 0, 0};
    jlong rjiPosition[2] = {0, 0};

    int16_t i16InitObject(int32_t i32ObjectStateOffset,
                          int32_t i32ObjType,
                          int32_t i32PositionX,
                          int32_t i32PositionY)
    {
        GET_CURRENT_OBJECT_STATES(i32ObjectStateOffset)
        switch (i32ObjType)
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
        }
        return 0;
    }



    static void vUpdateSimpleObjects(jint ji32Dt, Object_t *pxObject, xUpdaterFunction pUpFunction, xHitboxFunction pHbFunction)
    {
        /* Update bombs */
        jint jiHitbox[8] = {0};
        if (pxObject->ui16Id)
        {
            pUpFunction(pxObject, ji32Dt);

            /* Place in the map */
            pHbFunction(pxObject, jiHitbox);
            int16_t x = (jint)(((jiHitbox[4] + jiHitbox[2] / 2) - FIELD_X1) / CELLSIZE_X);
            int16_t y = (jint)(((jiHitbox[7] + jiHitbox[3] / 2) - FIELD_Y1) / CELLSIZE_Y);
            ui16FieldMap[x][y] = pxObject->ui16Id;
        }
    }

    /**
     * @brief Updates all the players
     * @param ji32Dt time delta
     * @param pxPlayer Pointer to first player
     */
    void vUpdateAllPlayers(jint ji32Dt, Player_t *pxPlayer)
    {
        uint32_t ui32Idx = 0;
        jint jiHitbox[8] = {0};
        int16_t x[4];
        int16_t y[4];
        uint32_t ui32PlayerUpdates = 0;
        while (ui32Idx < PLAYER_COUNT_MAX)
        {
            if (pxPlayer->ui16Id)
            {
#define DEBUG_FIRST_BOMB (0)
                Bomb_t xBomb = {0};
                if (i16PlayerUpdateState(pxPlayer, &xBomb, ji32Dt))
                {
                    ui32PlayerUpdates = (ui32PlayerUpdates | (1 << ui32Idx));
                    if (xBomb.ui16IdOwner)
                    {
                        vBombGetHitboxValues(&xBomb, jiHitbox);
                        int16_t x = (jint)(((jiHitbox[4] + jiHitbox[2] / 2) - FIELD_X1) / CELLSIZE_X);
                        int16_t y = (jint)(((jiHitbox[7] + jiHitbox[3] / 2) - FIELD_Y1) / CELLSIZE_Y);
                        if (ui16FieldMap[x][y] == O_)
                        {
                            i16InitObject(DEBUG_FIRST_BOMB, OBJ_BOMB, xBomb.i16PosX, xBomb.i16PosY);
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
                vPlayerGetCollisionPoints(pxPlayer, x, y);

                volatile uint8_t ui8IdxCell = 0;
                while (ui8IdxCell < 4)
                {
                    /* Collision with an object */
                    uint16_t ui16IdCollide = ui16FieldMap[x[ui8IdxCell]][y[ui8IdxCell]];

                    Hitbox_t *pxHitbox = NULL;
                    switch (ui16IdCollide & OBJ_MASK)
                    {
                    case OBJ_CRATE:
                        pxHitbox = pxCrateGetHitbox(&pxCrates[ui16IdCollide & (~OBJ_MASK)]);
                        jiCorrectPlayerPosition(pxPlayer, pxHitbox);
                        break;
                    case OBJ_BLOCK:
                        // ** Correct position of the player: TODO correct it to the brim
                        pxHitbox = pxBlockGetHitbox(&pxBlocks[ui16IdCollide & (~OBJ_MASK)]);
                        jiCorrectPlayerPosition(pxPlayer, pxHitbox);
                        break;
                    case OBJ_BOMB:
                        break;
                    default:
                        break;
                    }
                    ++ui8IdxCell;
                }
            }

            pxPlayer = &(pxPlayers[++ui32Idx]);
        }
    }

    void vUpdateExplosions(Bomb_t *pxBomb)
    {
        /* All positions have been set in the field
           Run explosions */
        uint32_t ui32Idx = 0;
        jint jiHitbox[8] = {0};
        pxBomb = &pxBombs[0];
        while (pxBomb->ui16Id)
        {
            if (bBombHasExploded(pxBomb))
            {
                int16_t cellsCovered[] = {0, 0, 0, 0};
                bool expansionUnderway[] = {true, true, true, true};
                int16_t strength = pxBomb->ui16ExplosionStrength;

                int16_t i16Pos[2];
                i16LevelGetCenteredPositionXY(i16Pos, pxBomb->i16PosX, pxBomb->i16PosY);

                int cellposx = i16Pos[0];
                int cellposy = i16Pos[1];
                for (int16_t direction = 0;
                     direction < 4 && expansionUnderway[direction]; direction++)
                {

                    // Continue only if the exposion didn't hit a wall
                    for (int radius = 1; radius <= strength; radius++)
                    {
                        int posx = 0;
                        int posy = 0;

                        switch (direction)
                        {
                        case 0: // mLeft
                            posx = cellposx - radius;
                            posy = cellposy;
                            break;
                        case 1: // mRight
                            posx = cellposx + radius;
                            posy = cellposy;
                            break;
                        case 2: // mUp
                            posx = cellposx;
                            posy = cellposy - radius;
                            break;
                        case 3: // mDown
                            posx = cellposx;
                            posy = cellposy + radius;
                            break;
                        }

                        if ((posx >= 0 && posx < NUMBER_OF_X_CELLS) &&
                            (posy >= 0 && posy < NUMBER_OF_Y_CELLS))
                        {

                            int id_obj = ui16FieldMap[posx][posy];
                            int id_type = (id_obj & OBJ_MASK);
                            int idx = (id_obj & ~OBJ_MASK);

                            int explosionid = 0;
                            switch (id_type)
                            {
                            case OBJ_BOMB:
                                /* TODO add next explosion */
                                if (!bBombHasExploded(&pxBomb[idx]))
                                {
                                    pxBombs[idx].ui8State = STATE_DEAD;
                                }

                                expansionUnderway[direction] = false;
                                break;
                            case OBJ_CRATE:
                                if (pxCrates[idx].ui8State == STATE_ALIVE)
                                {
                                    pxCrates[idx].ui8State = STATE_DEAD;
                                }
                                expansionUnderway[direction] = false;
                                cellsCovered[direction]++;
                                break;
                            case OBJ_BLOCK:
                                expansionUnderway[direction] = false;
                                break;
                            default:
                                /* TODO Set correct explosion id */
                                ui16FieldMap[posx][posy] = (explosionid);
                                cellsCovered[direction]++;
                                break;
                            }
                        }
                        else
                        {
                            expansionUnderway[direction] = false;
                        }
                    }
                }
            }
            pxBomb = &(pxBombs[++ui32Idx]);
        }
    }

    /**
     * @brief Initialize maps
     * @param ri16Levels levels matrix
     * @return
     */
    int16_t jiInitMap(int16_t ri16Levels[NUMBER_OF_Y_CELLS][NUMBER_OF_X_CELLS])
    {
        int32_t i = 0;
        int16_t i16BlockCount = 0;
        int16_t i16CrateCount = 0;
        while (i < NUMBER_OF_Y_CELLS)
        {
            for (int32_t j = 0; j < NUMBER_OF_X_CELLS; j++)
            {
                int16_t ri16Positions[2];
                i16LevelGetPositionFromCellXY(ri16Positions, j, i);
                if (ri16Levels[i][j] == BK)
                {
                    i16InitObject(i16BlockCount, OBJ_BLOCK, ri16Positions[0], ri16Positions[1]);
                    ++i16BlockCount;
                }
                else if (ri16Levels[i][j] == CR)
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

    /**
     * @brief Update according to priority which is set by the type: mPlayers > Bombs > Explosions> Crates > Blocks > Items
     * @param ji32Dt
     * @param ui32PlayerUpdates
     * @return
     */
    jint jiUpdateObjects(jint ji32Dt, uint32_t *ui32PlayerUpdates)
    {
        /* Get all sets */
        
        GET_CURRENT_OBJECT_STATES(0)

        for (uint32_t ui32Idx = 0; ui32Idx < BLOCK_COUNT_MAX; ++ui32Idx)
        {
            vUpdateSimpleObjects(ji32Dt, (Object_t*)&(pxBlocks[ui32Idx]), (xUpdaterFunction)i16BlockUpdateState, (xHitboxFunction)vBlockGetHitboxValues);
        }

        for (uint32_t ui32Idx = 0; ui32Idx < CRATE_COUNT_MAX; ++ui32Idx)
        {
            vUpdateSimpleObjects(ji32Dt, (Object_t*)&(pxCrates[ui32Idx]), (xUpdaterFunction)i16CrateUpdateState, (xHitboxFunction)vCrateGetHitboxValues);
        }

        for (uint32_t ui32Idx = 0; ui32Idx < BOMB_COUNT_MAX; ++ui32Idx)
        {
            vUpdateSimpleObjects(ji32Dt, (Object_t*)&(pxBombs[ui32Idx]), (xUpdaterFunction)i16BombUpdateState, (xHitboxFunction)vBombGetHitboxValues);
        }

        vUpdateAllPlayers(ji32Dt, pxPlayer);
        vUpdateExplosions(pxBomb);

        return 0;
    }

    jint rjiObjects[PLAYER_COUNT_MAX + BLOCK_COUNT_MAX + CRATE_COUNT_MAX] = {0};
    jint jiGameGetRemovedObjects(jint **ppui32Objects)
    {

        int16_t i16LocalCount = 0;
        int16_t i16TotalCount = 0;
        while (i16LocalCount < BOMB_COUNT_MAX)
        {
            if (bBombNeedsToBeRemoved(&pxBombs[i16LocalCount]))
            {
                rjiObjects[i16TotalCount++] = pxBombs[i16LocalCount].ui16Id;
                pxBombs[i16LocalCount].ui16Id = 0;
                pxBombs[i16LocalCount].ui8State = 0;
            }
            ++i16LocalCount;
        }

        i16LocalCount = 0;
        while (i16LocalCount < CRATE_COUNT_MAX)
        {
            if (bCrateNeedsToBeRemoved(&pxCrates[i16LocalCount]))
            {
                rjiObjects[i16TotalCount++] = pxCrates[i16LocalCount].ui16Id;
                pxCrates[i16LocalCount].ui16Id = 0;
                pxCrates[i16LocalCount].ui8State = 0;
            }
            ++i16LocalCount;
        }

        *ppui32Objects = rjiObjects;
        return i16TotalCount;
    }

    jint rjiTempObjects[PLAYER_COUNT_MAX + BLOCK_COUNT_MAX + CRATE_COUNT_MAX] = {0};
    jint jiGameGetObjects(jint **ppui32Objects)
    {
        int16_t i16TotalCount = 0;
        int16_t i16LocalCount = 0;

        while (i16LocalCount < PLAYER_COUNT_MAX)
        {
            if (pxPlayers[i16LocalCount].ui16Id)
            {
                rjiTempObjects[i16TotalCount++] = pxPlayers[i16LocalCount].ui16Id;
            }

            ++i16LocalCount;
        }

        i16LocalCount = 0;
        while (i16LocalCount < BLOCK_COUNT_MAX)
        {
            if (pxBlocks[i16LocalCount].ui16Id)
            {
                rjiTempObjects[i16TotalCount++] = pxBlocks[i16LocalCount].ui16Id;
            }
            ++i16LocalCount;
        }

        i16LocalCount = 0;
        while (i16LocalCount < CRATE_COUNT_MAX)
        {
            if (pxCrates[i16LocalCount].ui16Id)
            {
                rjiTempObjects[i16TotalCount++] = pxCrates[i16LocalCount].ui16Id;
            }
            ++i16LocalCount;
        }

        i16LocalCount = 0;
        while (i16LocalCount < BOMB_COUNT_MAX)
        {
            if (pxBombs[i16LocalCount].ui16Id)
            {
                rjiTempObjects[i16TotalCount++] = pxBombs[i16LocalCount].ui16Id;
            }
            ++i16LocalCount;
        }

        *ppui32Objects = rjiTempObjects;
        return i16TotalCount;
    }

    jint jiGameCreate()
    {

#define DEBUG_CONSTANT_LEVEL (0)
#define DEBUG_CONSTANT_PLAYER_POS (0)
        jiInitMap(LEVELS[DEBUG_CONSTANT_LEVEL]);

        int16_t ri16Positions[2];
        i16LevelGetPositionFromCellXY(ri16Positions, STARTING_CELL_POSITIONS[0][1],
                                      STARTING_CELL_POSITIONS[0][0]);
        i16InitObject(DEBUG_CONSTANT_PLAYER_POS, OBJ_PLAYR, ri16Positions[0], ri16Positions[1]);

        return 0;
    }

    int16_t i16GameGetObjectZ(int32_t i32ObjType, int32_t i32ObjectStateOffset)
    {

        GET_CURRENT_OBJECT_STATES(i32ObjectStateOffset)
        jint ar[] = {0, 0, 0, 0, 0, 0, 0, 0};
        switch (i32ObjType)
        {
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

    jint jiCheckCollisions(uint32_t ui32Players)
    {

        // Check 8 nearest cells against collision
        int16_t cellpos[] = {0, 0};
        int16_t cellposx = cellpos[0];
        int16_t cellposy = cellpos[1];
        for (int b = cellposx - 1; b <= (cellposx + 1); b++)
        {
            /* horizontally inside the map */
            if (b >= 0 && b < NUMBER_OF_X_CELLS)
            {
                for (int c = cellposy - 1; c <= (cellposy + 1); c++)
                {
                    /* Vertically inside the map */
                    if (c >= 0 && c < NUMBER_OF_Y_CELLS)
                    {
                        int id = ui16FieldMap[b][c];
                        if (id != 0)
                        {
                            // GameElement go = mGameObjects.get(id);
                            // if(playr.collisionCheck(go))
                            {
                                // return go;
                            }
                        }
                    }
                }
            }
        }
        return 0;
    }

    jint jiGameUpdateTicker()
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
        memset(ui16FieldMap, 0, sizeof(ui16FieldMap[0][0]) * (NUMBER_OF_X_CELLS) * (NUMBER_OF_Y_CELLS));

        return ui32GameTicker;
    }

    uint32_t ui32GameGetState(int32_t i32ObjType, int32_t i32ObjectStateOffset)
    {
        GET_CURRENT_OBJECT_STATES(i32ObjectStateOffset)

        uint32_t ui32State = 0;
        switch (i32ObjType)
        {
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

    /**
     * @brief
     * @param ppi32Positions
     * @param i32ObjType
     * @param i32ObjectStateOffset
     * @return
     */
    jint jiGameGetPositions(jlong **ppi32Positions, int32_t i32ObjType, int32_t i32ObjectStateOffset)
    {
        GET_CURRENT_OBJECT_STATES(i32ObjectStateOffset)

        switch (i32ObjType)
        {
        case OBJ_PLAYR:
            rjiPosition[0] = pxPlayer->i16PosX;
            rjiPosition[1] = pxPlayer->i16PosY;
            break;
        case OBJ_BLOCK:
            rjiPosition[0] = pxBlock->i16PosX;
            rjiPosition[1] = pxBlock->i16PosY;
            break;
        case OBJ_CRATE:
            rjiPosition[0] = pxCrate->i16PosX;
            rjiPosition[1] = pxCrate->i16PosY;
            break;
        case OBJ_BOMB:
            rjiPosition[0] = pxBomb->i16PosX;
            rjiPosition[1] = pxBomb->i16PosY;
            break;
        case OBJ_EXPLN:
            break;
        }
        *ppi32Positions = rjiPosition;

        return 0;
    }

    /******************************************
     * @brief
     *
     * @param ppi32Hitbox
     * @param i32ObjType
     * @param i32ObjectStateOffset
     * @return jint
     ******************************************/
    jint jiGameGetHitbox(jint **ppi32Hitbox, int32_t i32ObjType, int32_t i32ObjectStateOffset)
    {
        GET_CURRENT_OBJECT_STATES(i32ObjectStateOffset)

        switch (i32ObjType)
        {
        case OBJ_PLAYR:
            vPlayerGetHitboxValues(pxPlayer, pi32Hitbox);
            break;
        case OBJ_BLOCK:
            vBlockGetHitboxValues(pxBlock, pi32Hitbox);
            break;
        case OBJ_CRATE:
            vCrateGetHitboxValues(pxCrate, pi32Hitbox);
            break;
        case OBJ_BOMB:
            vBombGetHitboxValues(pxBomb, pi32Hitbox);
            break;
        case OBJ_EXPLN:
            break;
        }
        *ppi32Hitbox = pi32Hitbox;
        return 0;
    }

    /******************************************
     * @brief
     *
     * @param i32ObjectStateOffset
     * @param ui8Input
     ******************************************/
    void vGameSetInput(int32_t i32ObjectStateOffset, uint8_t ui8Input)
    {
        Player_t *pxPlayer = &pxPlayers[i32ObjectStateOffset];
        pxPlayer->ui16Input = ui8Input;
    }
}
