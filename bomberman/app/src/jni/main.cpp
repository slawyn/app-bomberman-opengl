#define _HAS_STD_BYTE 0
#define NOMINMAX
#include <Windows.h>

using namespace std;
#include <iostream>
#include "GameLogic.h"

int32_t ri32FieldSizes[2u];
bool bGameExecuting = true;

DWORD WINAPI thread1(LPVOID pm)
{   (void)pm;
    //check the getchar value
    int a = getchar();
    while (a != '0'){
        a = getchar();
    }
    bGameExecuting = false;
    return 0;
}

int32_t STEP = 20;
uint8_t ui8Input = 0u;


int main(int argc, char** argv){
    (void)argc;
    (void)argv;

    HANDLE handle = CreateThread(NULL, 0, thread1, NULL, 0, NULL);
    (void) handle;

    vGameGetFieldSizes(ri32FieldSizes);
    std::cout<<"x:"<<ri32FieldSizes[0]<<" y:"<<ri32FieldSizes[1]<<std::endl;
    jiGameCreate();
    uint32_t ui32UpdatedPlayers = 0;

    while(bGameExecuting){
        int32_t i32ObjectStateOffset = 0;
        vGameSetInput(i32ObjectStateOffset, ui8Input);


        jiUpdateObjects(STEP, &ui32UpdatedPlayers);

        int32_t *pjiObjects = NULL;
        int16_t i16TotalCount = jiGameGetRemovedObjects(&pjiObjects);
        (void) i16TotalCount;


        // ui32GameGetState(i32ObjType, i32ObjectStateOffset)
        // jiGameGetPositions(float ** ppfPositions, int32_t i32ObjType, int32_t i32ObjectStateOffset)
        // jiGameGetHitbox(int32_t **ppi32Hitbox, int32_t i32ObjType, int32_t i32ObjectStateOffset)
        // jiGameGetObjects(int32_t ** ppui32Objects);
        // i16GameGetObjectZ(int32_t i32ObjType, int32_t i32ObjectStateOffset);
        // jiGameUpdateTicker(void)
    }
    
    return 0;
}