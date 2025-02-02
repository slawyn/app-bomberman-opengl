@echo off
setlocal enabledelayedexpansion

:: Paths
set PROJECT_DIR=%~dp0..
set BUILD_DIR=!PROJECT_DIR!\app\.externalNativeBuild\ndk-build\
set JNI_DIR=!PROJECT_DIR!\app\src\jni
set GRADLE_BAT=C:\Users\Unixt\.gradle\wrapper\dists\gradle-7.4-bin\c0gwcg53nkjbqw7r0h0umtfvt\gradle-7.4\bin\gradle.bat
set MAKE_EXE=C:\msys64\mingw64\bin\mingw32-make.exe
set NDK_BUILD=C:\Users\Unixt\AppData\Local\Android\Sdk\ndk-bundle\ndk-build.cmd

:: Resolve paths
for %%I in ("!MAKE_EXE!") do set MAKE_EXE=%%~fI
for %%I in ("!NDK_BUILD!") do set NDK_BUILD=%%~fI
for %%I in ("!BUILD_DIR!") do set BUILD_DIR=%%~fI
for %%I in ("%GRADLE_BAT%") do set GRADLE_BAT=%%~fI
for %%I in ("!PROJECT_DIR!") do set PROJECT_DIR=%%~fI
for %%I in ("!JNI_DIR!") do set JNI_DIR=%%~fI

:: Get the input from the command line argument
set target=%~1
set clean=%~2

:: Check if the input is valid
if "!target!"=="sdk" (
    call :sdk
) else if "!target!"=="ndk" (
    call :ndk
) else if "!target!"=="windows" (
    call :windows
) else if "!target!"=="all" (
    call :sdk
    call :ndk
    call :windows
) else (
    echo Invalid input. Please provide a valid argument: sdk, ndk, windows, or all.
)

:: jump table
goto :end
:sdk
    echo Running SDK build...
    pushd "!PROJECT_DIR!"
    "!GRADLE_BAT!" :app:assembleDebug
    popd
    exit /b !ERRORLEVEL!

:ndk
    echo Running NDK build...
    pushd "!PROJECT_DIR!\app"
    "!NDK_BUILD!" %clean% NDK_LIBS_OUT=!BUILD_DIR! NDK_OUT=!BUILD_DIR! NDK_PROJECT_PATH=./src NDK_DEBUG=1
    popd
    exit /b !ERRORLEVEL!

:windows
    echo Running Windows build...
    pushd "!JNI_DIR!"
    "!MAKE_EXE!" %clean% -f Windows.make 
    popd
    exit /b !ERRORLEVEL!

:end
endlocal