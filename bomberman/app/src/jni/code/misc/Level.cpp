//
// Created by Unixt on 7/3/2023.
//
#include "Level.h"
#include "Config.h"
#include "Bomb.h"
#include "GameLogic.h"
#include <string.h>

#ifdef __cplusplus
extern "C" {
#endif

    static uint16_t ui16FieldMap[NUMBER_OF_X_CELLS][NUMBER_OF_Y_CELLS] = {0};
    int16_t LEVELS[][NUMBER_OF_X_CELLS][NUMBER_OF_Y_CELLS] =
        {
                {
                        {O_, O_, O_, O_, O_, O_, O_, O_, O_, O_, O_},
                        {O_, O_, O_, O_, O_, O_, O_, O_, O_, O_, O_},
                        {O_, O_, O_, O_, O_, O_, O_, BK, O_, O_, O_},
                        {O_, O_, O_, O_, O_, O_, O_, O_, O_, O_, O_},
                        {O_, O_, O_, O_, O_, CR, O_, O_, O_, O_, O_},
                        {O_, O_, O_, O_, O_, O_, O_, O_, O_, O_, O_},
                        {O_, O_, O_, O_, O_, O_, O_, O_, O_, O_, O_},
                        {O_, O_, O_, O_, O_, O_, O_, O_, O_, O_, O_},
                        {O_, O_, O_, O_, O_, O_, O_, O_, O_, O_, O_},
                        {O_, O_, O_, O_, O_, O_, O_, O_, O_, O_, O_},
                        {O_, O_, O_, O_, O_, O_, O_, O_, O_, O_, O_},
                },
                {
                        {O_, O_, O_, O_, O_, O_, O_, O_, O_, O_, O_},
                        {O_, BK, O_, BK, O_, O_, O_, BK, O_, BK, O_},
                        {O_, O_, O_, O_, O_, O_, O_, O_, O_, O_, O_},
                        {O_, BK, O_, BK, O_, CR, O_, BK, CR, BK, CR},
                        {O_, O_, O_, O_, CR, O_, CR, CR, O_, O_, O_},
                        {O_, O_, O_, O_, O_, CR, O_, O_, CR, O_, O_},
                        {O_, O_, O_, O_, O_, O_, O_, O_, O_, O_, O_},
                        {O_, BK, O_, BK, O_, O_, O_, BK, O_, BK, O_},
                        {O_, O_, O_, O_, O_, O_, O_, O_, O_, O_, O_},
                        {O_, BK, O_, BK, O_, O_, O_, BK, O_, BK, O_},
                        {O_, O_, O_, O_, O_, O_, O_, O_, O_, O_, O_},
                },
                {
                        {O_, O_, O_, O_, O_, O_, O_, O_, O_, O_, O_},
                        {O_, BK, O_, BK, O_, BK, O_, BK, O_, BK, O_},
                        {O_, O_, O_, CR, O_, O_, O_, O_, O_, O_, O_},
                        {O_, BK, O_, O_, O_, CR, O_, O_, CR, BK, CR},
                        {O_, O_, CR, O_, CR, O_, CR, CR, CR, O_, O_},
                        {O_, O_, O_, O_, O_, CR, O_, O_, CR, O_, O_},
                        {O_, O_, CR, O_, CR, O_, O_, CR, O_, O_, O_},
                        {O_, BK, O_, CR, O_, O_, O_, O_, O_, BK, O_},
                        {O_, O_, O_, O_, O_, CR, O_, CR, O_, O_, O_},
                        {O_, BK, O_, BK, O_, BK, O_, BK, O_, BK, O_},
                        {O_, O_, O_, O_, O_, O_, O_, O_, O_, O_, O_},
                }
        };


void vLevelMemorySetCell(int16_t x, int16_t y, uint16_t value)
{
    ui16FieldMap[x][y] = value;
} 
uint16_t vLevelMemoryGetCell(int16_t x, int16_t y)
{
    return ui16FieldMap[x][y];
} 
void vLevelMemoryReset(void)
{
    memset(ui16FieldMap, 0, sizeof(ui16FieldMap[0][0]) * (NUMBER_OF_X_CELLS) * (NUMBER_OF_Y_CELLS));
}

int16_t i16LevelGetPositionFromCellXY(int16_t ri16Positions[2], int16_t i16X, int16_t i16Y)
{
    ri16Positions[0] = ((CELLSIZE_X * i16X) + CELLSIZE_X/2);
    ri16Positions[1] = ((CELLSIZE_Y * i16Y) + CELLSIZE_Y/2);
    return 0;
}

int16_t i16LevelGetCenteredPositionXY(int16_t ri16Positions[2], int16_t i16X, int16_t i16Y)
{
    ri16Positions[0] = (i16X)/CELLSIZE_X;
    ri16Positions[1] = (i16Y)/CELLSIZE_Y;
    return 0;
}


int16_t i16ExpandExplosion(Bomb_t * pxBomb, int16_t *rri16Fieldmap[])
{
    int16_t cellsCovered[] = {0, 0, 0, 0};
    uint8_t expansionUnderway = 0;


    int16_t cellposxy[2];

    i16LevelGetCenteredPositionXY(cellposxy, pxBomb->object.i16PosX, pxBomb->object.i16PosY);
    rri16Fieldmap[cellposxy[0]][cellposxy[1]] = (pxBomb->object.ui16Id);

    for(int16_t direction = 0; direction < 4; direction++) {

        for (int radius = 1; radius <= pxBomb->ui16ExplosionStrength && (((expansionUnderway&(1u<<direction)) == 0u)); radius++) {
            int posx = 0;
            int posy = 0;

            switch (direction) {
                case 0: // mLeft
                    posx = cellposxy[0] - radius;
                    posy = cellposxy[1];
                    break;
                case 1: // mRight
                    posx = cellposxy[0] + radius;
                    posy = cellposxy[1];
                    break;
                case 2: // mUp
                    posx = cellposxy[0];
                    posy = cellposxy[1] - radius;
                    break;
                case 3: // mDown
                    posx = cellposxy[0];
                    posy = cellposxy[1] + radius;
                    break;
            }
            
            // Continue only if the exposion didn't hit a wall
            if (posx < 0 || posx == NUMBER_OF_X_CELLS || posy < 0 || posy == NUMBER_OF_Y_CELLS) {
                expansionUnderway|= (1<<direction);
            }
            else
            {
                uint16_t ui16Id = rri16Fieldmap[posx][posy];
                if (ui16Id &OBJ_MASK) {
                    switch (ui16Id &OBJ_MASK) {
                        case OBJ_BOMB:
                            /*  TODO trigger bomb here */
                            expansionUnderway|= (1<<direction);
                            break;
                        case OBJ_CRATE:
                            /*  TODO destroy crate here */
                            expansionUnderway|= (1<<direction);
                            cellsCovered[direction]++;
                            break;
                        case OBJ_BLOCK:
                        case OBJ_EXPLN:
                            expansionUnderway|= (1<<direction);
                            break;
                    }
                } else {
                    rri16Fieldmap[posx][posy] = (pxBomb->object.ui16Id);
                    cellsCovered[direction]++;
                }
            }
        }
    }

    return 0;
}


#ifdef __cplusplus
}
#endif

