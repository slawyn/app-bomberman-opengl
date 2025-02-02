@echo off 
set APP=%~dp0..\app\build\outputs\apk\debug\app-debug.apk
set EMULATOR=emulator-5554
:: Install
adb -s %EMULATOR% install %APP%

:: Start App
adb -s %EMULATOR% shell am start -n "com.bomber.app/main.Connector" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER

:: Start logcat
adb -s %EMULATOR% logcat -v color -T 1