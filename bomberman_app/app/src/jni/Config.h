//
// Created by Unixt on 6/22/2023.
//

#ifndef BOMBERMAN_APP_CONFIG_H
#define BOMBERMAN_APP_CONFIG_H

/* Macros ----------------------------------------------- */
#define PLAYER_COUNT_MAX (4)
#define BOMB_COUNT_MAX (4)
#define CRATE_COUNT_MAX (30)
#define BLOCK_COUNT_MAX (30)
#define TOTAL_STATE_COUNT (20)

#define PLAYER_BASE_SPEED (3)
#define PLAYER_BOMB_STARTING_AMOUNT (10)
#define PLAYER_BOMB_EXPLOSION_STRENGTH (1)
#define BOMB_TIMER (3000)
#define EXPLOSION_TIME (2000)

#define GAME_TICKER_START (0)

#define SCALE_FACTOR (1.0f)
#define GAME_WIDTH (1920 * SCALE_FACTOR)
#define GAME_HEIGHT (1080 * SCALE_FACTOR)

/* for 1920 x 1080 , field is 1540 x 1034*/
#define FIELD_X1 ((190 * SCALE_FACTOR))
#define FIELD_X2 ((GAME_WIDTH - FIELD_X1))
#define FIELD_Y1 ((36 * SCALE_FACTOR))
#define FIELD_Y2 ((GAME_HEIGHT - 10))

#define NUMBER_OF_X_CELLS (11u)
#define NUMBER_OF_Y_CELLS (11u)

// CELLSIZEX = 140    // CELLSIZEY = 94
#define CELLSIZE_X ((FIELD_X2 - FIELD_X1) / NUMBER_OF_X_CELLS)
#define CELLSIZE_Y ((FIELD_Y2 - FIELD_Y1) / NUMBER_OF_Y_CELLS)
#define CELL_RATIO (CELLSIZE_Y / CELLSIZE_X)

#define ID(off, type) (off | type)

#endif // BOMBERMAN_APP_CONFIG_H
