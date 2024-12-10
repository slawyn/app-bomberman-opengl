#ifndef KEYBOARD_H
#define KEYBOARD_H

#include <stdint.h>

typedef void (*pFunction_t)(void);

typedef enum
{
   KeyboardKeysLeftPressed,
   KeyboardKeys_left_unpressed,
   KeyboardKeys_right_pressed,
   KeyboardKeys_right_unpressed,
   KeyboardKeys_up_pressed,
   KeyboardKeys_up_unpressed,
   KeyboardKeys_down_pressed,
   KeyboardKeys_down_unpressed,
   KeyboardKeys_quan
} KeyboardKeys_e;

typedef struct
{
   KeyboardKeys_e eKey;
   pFunction_t    pFunction;
} KeyboardKeysConfig_t;

typedef struct
{
   KeyboardKeysConfig_t *prKeyConfigs;
   uint8_t               ui8Count;
} KeyboardConfig_t;


int Keyboard_init(KeyboardConfig_t *config);

#endif
