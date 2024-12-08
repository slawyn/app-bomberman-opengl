//
// Created by Unixt on 7/3/2023.
//

#ifndef OBJECT_H
#define OBJECT_H
#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif

    typedef struct
    {
        int16_t ui16Id;
        int16_t i16PosX;
        int16_t i16PosY;
        uint8_t ui8State;
    } Object_t;

#ifdef __cplusplus
}
#endif
#endif //OBJECT_H
