@echo off 
set APP=app\build\outputs\apk\debug\app-debug.apk

:: Install
adb -s emulator-5554 install %APP%

:: Start App
adb shell am start -n "com.bomber.app/main.Connector" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER

:: Start logcat
adb logcat -v color -T 1