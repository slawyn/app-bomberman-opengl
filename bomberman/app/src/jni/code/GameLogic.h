#ifndef GAME_STATE_BUFFER_H
#define GAME_STATE_BUFFER_H
#include <jni.h>


extern "C" {


#define     OBJ_BOMB  0x1000
#define     OBJ_EXPLN  0x2000
#define     OBJ_CRATE  0x3000
#define     OBJ_BLOCK  0x4000
#define     OBJ_ITEM  0x5000
#define     OBJ_PLAYR  0x6000


#define OBJ_MASK                    (0xF000)
void vGameGetFieldSizes(jint rjiFieldSizes[2]);
jint jiUpdateObjects(jint ji32Dt, uint32_t *ui32PlayerUpdates);
jint  jiGameGetObjects(jint ** ppui32Objects);
jint  jiGameGetRemovedObjects(jint ** ppui32Objects);
jint jiGameCreate();
int16_t i16GameGetObjectZ(int32_t i32ObjType, int32_t i32ObjectStateOffset);
jint jiGameUpdateTicker();
uint32_t ui32GameGetState(int32_t i32ObjType, int32_t i32ObjectStateOffset);
jint jiGameGetPositions(jfloat ** ppfPositions, int32_t i32ObjType, int32_t i32ObjectStateOffset);
void vGameSetInput(int32_t i32ObjectStateOffset, uint8_t ui8Input);
jint jiGameGetHitbox(jint **ppi32Hitbox, int32_t i32ObjType, int32_t i32ObjectStateOffset);
}
#endif
