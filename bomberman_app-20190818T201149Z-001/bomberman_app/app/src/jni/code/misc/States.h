//
// Created by Unixt on 6/22/2023.
//

#ifndef STATES_H
#define STATES_H
#include <jni.h>
#ifdef __cplusplus
extern "C" {
#endif
enum
{
    STATE_ALIVE = 0,
    STATE_MOVEDOWN,
    STATE_MOVEUP,
    STATE_MOVELEFT,
    STATE_MOVERIGHT,
    STATE_DEAD,
    STATE_REMOVE,
    STATE_EXPLODED
};
#ifdef __cplusplus
}
#endif
#endif //BOMBERMAN_APP_STATES_H
