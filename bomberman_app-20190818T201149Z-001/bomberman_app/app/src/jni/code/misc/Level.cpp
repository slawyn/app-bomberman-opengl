//
// Created by Unixt on 7/3/2023.
//
#include "Level.h"
#include "Config.h"
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
    int16_t i16CellPosX = i16X/NUMBER_OF_X_CELLS;
    int16_t i16CellPosY = i16Y/NUMBER_OF_Y_CELLS;
}

#ifdef __cplusplus
}
#endif

