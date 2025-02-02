@echo off 

:: Start logcat
adb -s %~1 logcat -v color -T 1