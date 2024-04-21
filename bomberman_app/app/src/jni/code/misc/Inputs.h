//
// Created by Unixt on 6/22/2023.
//

#ifndef INPUTS_H
#define INPUTS_H
#include <jni.h>
#ifdef __cplusplus
extern "C" {
#endif
enum
{
    INPUT_NONE = 0x00,
    INPUT_MOVE_RIGHT = 0x01,
    INPUT_MOVE_LEFT = 0x02,
    INPUT_MOVE_UP = 0x03,
    INPUT_MOVE_DOWN = 0x04,
    INPUT_PLACE_BOMB = 0x10,
    INPUT_LOWER_NIBBLE = 0x0F,
    INPUT_HIGHER_NIBBLE = 0xF0
};
#ifdef __cplusplus
}
#endif
#endif //BOMBERMAN_APP_INPUTS_H
