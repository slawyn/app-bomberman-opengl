#include <Windows.h>
#include <iostream>

#include "GameLogic.h"
#include "Inputs.h"
#include "keyboard.h"

using namespace std;

int32_t STEP = 20;
int32_t ri32FieldSizes[2u];
uint8_t ui8Input       = INPUT_NONE;
bool    bGameExecuting = true;

static void vKeyPressedLeft(void)
{
   ui8Input |= (INPUT_MOVE_LEFT);
}

static void vKeyUnpressedLeft(void)
{
   ui8Input &= (~INPUT_MOVE_LEFT);
}

static void vKeyPressedRight(void)
{
   ui8Input |= (INPUT_MOVE_RIGHT);
}

static void vKeyUnpressedRight(void)
{
   ui8Input &= (~INPUT_MOVE_RIGHT);
}

static void vKeyPressedUp(void)
{
   ui8Input |= (INPUT_MOVE_UP);
}

static void vKeyUnpressedUp(void)
{
   ui8Input &= (~INPUT_MOVE_UP);
}

static void vKeyPressedDown(void)
{
   ui8Input |= (INPUT_MOVE_DOWN);
}

static void vKeyUnpressedDown(void)
{
   ui8Input &= (~INPUT_MOVE_DOWN);
}

int main(int argc, char **argv)
{
   (void)argc;
   (void)argv;

   KeyboardKeysConfig_t xKeysConfig[] =
   {
      { .eKey = KeyboardKeysLeftPressed,    .pFunction = &vKeyPressedLeft    },
      { .eKey = KeyboardKeys_left_unpressed,  .pFunction = &vKeyUnpressedLeft  },
      { .eKey = KeyboardKeys_right_pressed,   .pFunction = &vKeyPressedRight   },
      { .eKey = KeyboardKeys_right_unpressed, .pFunction = &vKeyUnpressedRight },
      { .eKey = KeyboardKeys_up_pressed,      .pFunction = &vKeyPressedUp      },
      { .eKey = KeyboardKeys_up_unpressed,    .pFunction = &vKeyUnpressedUp    },
      { .eKey = KeyboardKeys_down_pressed,    .pFunction = &vKeyPressedDown    },
      { .eKey = KeyboardKeys_down_unpressed,  .pFunction = &vKeyUnpressedDown  }
   };

   KeyboardConfig_t xConfig = {
      .prKeyConfigs = xKeysConfig,
      .ui8Count     = sizeof(xKeysConfig)/sizeof(KeyboardKeysConfig_t)
   };

   Keyboard_init(&xConfig);


   jiGameCreate();
   vGameGetFieldSizes(ri32FieldSizes);
   std::cout << "Field x:" << ri32FieldSizes[0u] << " y:" << ri32FieldSizes[1u] << std::endl;

   uint32_t ui32UpdatedPlayers = 0u;
   float *pfPositions;
   while (bGameExecuting)
   {
      jiGameGetPositions(&pfPositions, OBJ_PLAYR, 0u);
      std::cout << "Input:" << std::uppercase << std::hex << static_cast <int>(ui8Input) << " X:" << pfPositions[0] << " Y:" << pfPositions[1] << std::endl;


      int32_t i32ObjectStateOffset = 0;
      vGameSetInput(i32ObjectStateOffset, ui8Input);
      jiUpdateObjects(STEP, &ui32UpdatedPlayers);

      int32_t *pjiObjects    = NULL;
      int16_t  i16TotalCount = jiGameGetRemovedObjects(&pjiObjects);
      (void)i16TotalCount;

      Sleep(15u);


      // ui32GameGetState(i32ObjType, i32ObjectStateOffset)
      // jiGameGetHitbox(int32_t **ppi32Hitbox, int32_t i32ObjType, int32_t i32ObjectStateOffset)
      // jiGameGetObjects(int32_t ** ppui32Objects);
      // i16GameGetObjectZ(int32_t i32ObjType, int32_t i32ObjectStateOffset);
      // jiGameUpdateTicker(void)
   }

   return(0);
}