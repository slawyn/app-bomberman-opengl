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

#define PLAYER_BASE_SPEED (4)
#define PLAYER_BOMB_STARTING_AMOUNT (10)
#define PLAYER_BOMB_EXPLOSION_STRENGTH (1)
#define BOMB_TIMER (3000)
#define EXPLOSION_TIME (2000)

#define GAME_TICKER_START (0)


/* Field size used for positional calculations*/
#define FIELD_SIZE_X (2000.0f)
#define FIELD_SIZE_Y (2000.0f)


#define NUMBER_OF_X_CELLS (11u)
#define NUMBER_OF_Y_CELLS (11u)

// CELLSIZEX = 140    // CELLSIZEY = 94
#define CELLSIZE_X (FIELD_SIZE_X/ NUMBER_OF_X_CELLS)
#define CELLSIZE_Y (FIELD_SIZE_Y / NUMBER_OF_Y_CELLS)
#define CELL_RATIO (CELLSIZE_Y / CELLSIZE_X)

#define ID(off, type) (off | type)

#endif // BOMBERMAN_APP_CONFIG_H
