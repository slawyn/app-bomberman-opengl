set NDK_BUILD=C:\Users\Unixt\AppData\Local\Android\Sdk\ndk-bundle\ndk-build.cmd
set BUILD_DIR=.externalNativeBuild/ndk-build/

:: clean project
set CLEAN=%~1

pushd app
%NDK_BUILD% %CLEAN% NDK_LIBS_OUT=%BUILD_DIR% NDK_OUT=%BUILD_DIR% NDK_PROJECT_PATH=./src NDK_DEBUG=1
popd