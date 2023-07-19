//
// Created by Unixt on 7/3/2023.
//
#include "Level.h"
#include "Config.h"
#include "Bomb.h"
#include "code/GameLogic.h"

#ifdef __cplusplus
extern "C" {
#endif

    int16_t LEVELS[][NUMBER_OF_X_CELLS][NUMBER_OF_Y_CELLS] =
        {
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



int16_t i16LevelGetPositionFromCellXY(int16_t ri16Positions[2], int16_t i16X, int16_t i16Y)
{
    ri16Positions[0] = (FIELD_X1 + (CELLSIZE_X * i16X) + CELLSIZE_X/2);
    ri16Positions[1] = (FIELD_Y1 + (CELLSIZE_Y * i16Y) + CELLSIZE_Y/2);
    return 0;
}

int16_t i16LevelGetCenteredPositionXY(int16_t ri16Positions[2], int16_t i16X, int16_t i16Y)
{
    ri16Positions[0] = (i16X-FIELD_X1)/CELLSIZE_X;
    ri16Positions[1] = (i16Y-FIELD_Y1)/CELLSIZE_Y;
    return 0;
}


int16_t i16ExpandExplosion(Bomb_t * pxBomb, int16_t *rri16Fieldmap[])
{
    int16_t cellsCovered[] = {0, 0, 0, 0};
    uint8_t expansionUnderway = 0;


    int16_t cellposxy[2];

    i16LevelGetCenteredPositionXY(cellposxy, pxBomb->i16PosX, pxBomb->i16PosY);
    rri16Fieldmap[cellposxy[0]][cellposxy[1]] = (pxBomb->ui16Id);

    for(int16_t direction = 0; direction < 4; direction++) {

        for (int radius = 1; radius <= pxBomb->ui16ExplosionStrength && ((expansionUnderway&(1<<direction) == 0)); radius++) {
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
                    rri16Fieldmap[posx][posy] = (pxBomb->ui16Id);
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

