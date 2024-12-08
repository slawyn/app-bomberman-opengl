#ifndef GAME_STATE_BUFFER_H
#define GAME_STATE_BUFFER_H
#include <stdint.h>

extern "C" {


#define     OBJ_BOMB  0x1000
#define     OBJ_EXPLN  0x2000
#define     OBJ_CRATE  0x3000
#define     OBJ_BLOCK  0x4000
#define     OBJ_ITEM  0x5000
#define     OBJ_PLAYR  0x6000


#define OBJ_MASK                    (0xF000)
void vGameGetFieldSizes(int32_t ri32FieldSizes[2]);
int32_t jiUpdateObjects(int32_t ji32Dt, uint32_t *ui32PlayerUpdates);
int32_t  jiGameGetObjects(int32_t ** ppui32Objects);
int32_t  jiGameGetRemovedObjects(int32_t ** ppui32Objects);
int32_t jiGameCreate();
int16_t i16GameGetObjectZ(int32_t i32ObjType, int32_t i32ObjectStateOffset);
int32_t jiGameUpdateTicker();
uint32_t ui32GameGetState(int32_t i32ObjType, int32_t i32ObjectStateOffset);
int32_t jiGameGetPositions(float ** ppfPositions, int32_t i32ObjType, int32_t i32ObjectStateOffset);
void vGameSetInput(int32_t i32ObjectStateOffset, uint8_t ui8Input);
int32_t jiGameGetHitbox(int32_t **ppi32Hitbox, int32_t i32ObjType, int32_t i32ObjectStateOffset);
}
#endif
