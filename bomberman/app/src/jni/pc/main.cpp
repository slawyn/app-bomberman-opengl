#include <Windows.h>
#include <iostream>
#include <chrono>
#include <iomanip>
#include <time.h>
#include <thread>

#include "jni.h"
#include "JniC.h"
#include "GameLogic.h"
#include "Inputs.h"
#include "keyboard.h"

using namespace std;

#define MILLISECONDS_STEP    (20u)
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
      { .eKey = KeyboardKeysLeftPressed,      .pFunction = &vKeyPressedLeft    },
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
      .ui8Count     = sizeof(xKeysConfig) / sizeof(KeyboardKeysConfig_t)
   };

   Keyboard_init(&xConfig);

   /* Initialize Game */
   JNIEnv env = JNIEnv();
   (void)Java_main_nativeclasses_GameManager_createGame(&env, 0);
   jintArray rjiFieldSizes = Java_main_nativeclasses_GameManager_getFieldSizes(&env, 0);


   std::cout << "Field x:" << rjiFieldSizes[0u] << " y:" << rjiFieldSizes[1u] << std::endl;
   uint32_t ui32UpdatedPlayers = 0u;
   float *  pfPositions;



   auto xTimestampStart = std::chrono::high_resolution_clock::now();
   std::chrono::duration <double> xTimeAccumulator;
   uint32_t ui32GameCycles = 0u;
   while (bGameExecuting)
   {
      auto xTimeNow = std::chrono::high_resolution_clock::now();
      xTimeAccumulator += xTimeNow - xTimestampStart;
      xTimestampStart   = xTimeNow;


      const std::chrono::duration <double> xStep = std::chrono::milliseconds(MILLISECONDS_STEP);
      if (xTimeAccumulator >= xStep)
      {
         xTimeAccumulator -= xStep;
         jiGameGetPositions(&pfPositions, OBJ_PLAYR, 0u);

         auto    in_time_t = std::chrono::system_clock::to_time_t(xTimeNow);
         auto    ms        = std::chrono::duration_cast <std::chrono::milliseconds>(xTimeNow.time_since_epoch()) % 1000;
         std::tm buf; localtime_s(&buf, &in_time_t);

         std::cout << std::dec;
         std::cout << std::put_time(&buf, "%H:%M:%S") << '.' << std::setfill('0') << std::setw(3) << ms.count();
         std::cout << "(" << xTimeAccumulator.count() << ")";
         std::cout << "[" << ++ui32GameCycles << "] "
                   << "Input: " << std::uppercase << std::hex << static_cast <int>(ui8Input) << " "
                   << "X: " << pfPositions[0] << " "
                   << "Y: " << pfPositions[1] << std:: endl;

         int32_t i32ObjectStateOffset = 0;
         vGameSetInput(i32ObjectStateOffset, ui8Input);
         jiUpdateObjects(MILLISECONDS_STEP, &ui32UpdatedPlayers);

         int32_t *pjiObjects    = NULL;
         int16_t  i16TotalCount = jiGameGetRemovedObjects(&pjiObjects);
         (void)i16TotalCount;
      }

      std::this_thread::sleep_for(std::chrono::milliseconds(MILLISECONDS_STEP >> 1u));

      // ui32GameGetState(i32ObjType, i32ObjectStateOffset)
      // jiGameGetHitbox(int32_t **ppi32Hitbox, int32_t i32ObjType, int32_t i32ObjectStateOffset)
      // jiGameGetObjects(int32_t ** ppui32Objects);
      // i16GameGetObjectZ(int32_t i32ObjType, int32_t i32ObjectStateOffset);
      // jiGameUpdateTicker(void)
   }

   return(0);
}
