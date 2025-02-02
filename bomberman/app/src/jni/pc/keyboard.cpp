#include <Windows.h>
#include "keyboard.h"

static void vDummyHandler(void)
{
}

static pFunction_t rCallbacks[KeyboardKeys_quan] =
{
   &vDummyHandler,
   &vDummyHandler,
   &vDummyHandler,
   &vDummyHandler,
   &vDummyHandler,
   &vDummyHandler,
   &vDummyHandler,
   &vDummyHandler
};

static LRESULT CALLBACK lrKeyboardEvent(int nCode, WPARAM wParam, LPARAM lParam)
{
   if (nCode == HC_ACTION)
   {
      KBDLLHOOKSTRUCT *pKeyboard = (KBDLLHOOKSTRUCT *)lParam;
      switch (wParam)
      {
      case WM_KEYDOWN:
      case WM_SYSKEYDOWN:
         switch (pKeyboard->vkCode)
         {
         case VK_UP:
            rCallbacks[KeyboardKeysLeftPressed]();
            break;

         case VK_DOWN:
            rCallbacks[KeyboardKeys_down_pressed]();
            break;

         case VK_LEFT:
            rCallbacks[KeyboardKeysLeftPressed]();
            break;

         case VK_RIGHT:
            rCallbacks[KeyboardKeys_right_pressed]();
            break;
         }
         break;

      case WM_KEYUP:
      case WM_SYSKEYUP:
         switch (pKeyboard->vkCode)
         {
         case VK_UP:
            rCallbacks[KeyboardKeys_left_unpressed]();
            break;

         case VK_DOWN:
            rCallbacks[KeyboardKeys_down_unpressed]();
            break;

         case VK_LEFT:
            rCallbacks[KeyboardKeys_left_unpressed]();
            break;

         case VK_RIGHT:
            rCallbacks[KeyboardKeys_right_unpressed]();
            break;
         }
         break;
      }
   }
   return(CallNextHookEx(NULL, nCode, wParam, lParam));
}

void vMessageLoop()
{
   MSG message;
   while (GetMessage(&message, NULL, 0, 0))
   {
      TranslateMessage(&message);
      DispatchMessage(&message);
   }
}

DWORD WINAPI dwHookThread(LPVOID lpParm)
{
   HINSTANCE hInstance = GetModuleHandle(NULL);
   if (!hInstance)
   {
      hInstance = LoadLibrary((LPCSTR)lpParm);
   }
   if (!hInstance)
   {
      return(1);
   }

   HHOOK hKeyboardHook = SetWindowsHookEx(WH_KEYBOARD_LL, (HOOKPROC)lrKeyboardEvent, hInstance, NULL);
   vMessageLoop();
   UnhookWindowsHookEx(hKeyboardHook);
   return(0);
}

int Keyboard_init(KeyboardConfig_t *config)
{
   if (NULL != config && (config->ui8Count <= KeyboardKeys_quan) && (NULL != config->prKeyConfigs))
   {
      uint8_t i = config->ui8Count;
      while (i > 0u)
      {  
         const KeyboardKeysConfig_t * pxConfig =  &config->prKeyConfigs[--i];
         if((NULL !=pxConfig) && (pxConfig->eKey < KeyboardKeys_quan) && (NULL !=pxConfig->pFunction)){
            rCallbacks[pxConfig->eKey] = pxConfig->pFunction;
         }
      }

      HANDLE handle = CreateThread(NULL, 0, dwHookThread, NULL, 0, NULL);
      (void)handle;
   }

   return(0u);
}
