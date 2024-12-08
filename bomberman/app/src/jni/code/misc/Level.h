//
// Created by Unixt on 7/3/2023.
//

#ifndef BOMBERMAN_APP_LEVEL_H
#define BOMBERMAN_APP_LEVEL_H
#include <stdint.h>
#include "Config.h"

#ifdef __cplusplus
extern "C" {
#endif

#define O_          (0)
#define BK          (1)
#define CR          (2)
#define CENTERX(x) (x*CELLSIZE_X + CELLSIZE_X/2)
#define CENTERY(y) (y*CELLSIZE_Y + CELLSIZE_Y/2)

extern int16_t LEVELS[][NUMBER_OF_X_CELLS][NUMBER_OF_Y_CELLS];

int16_t i16LevelGetPositionFromCellXY(int16_t ri16Positions[2], int16_t i16X, int16_t i16Y);
int16_t i16LevelGetCenteredPositionXY(int16_t ri16Positions[2], int16_t i16X, int16_t i16Y);
uint16_t vLevelMemoryGetCell(int16_t x, int16_t y);
void vLevelMemorySetCell(int16_t x, int16_t y, uint16_t value);
void vLevelMemoryReset(void);
#ifdef __cplusplus
}
#endif
#endif //BOMBERMAN_APP_LEVEL_H
