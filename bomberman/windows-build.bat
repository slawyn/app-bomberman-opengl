@echo off

set MAKE=C:\msys64\mingw64\bin\mingw32-make.exe
set NDK_BUILD=C:\Users\Unixt\AppData\Local\Android\Sdk\ndk-bundle\ndk-build.cmd
set BUILD_DIR=.externalNativeBuild/ndk-build/

:: clean project
set CLEAN=%~1

pushd app\src\jni
%MAKE% -f Windows.mak
popd