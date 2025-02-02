# Define the compiler and flags
CC := gcc
CXX := g++
CFLAGS := -W -Wall -fPIC -DPIC -DDARWIN_NO_CARBON -DFT2_BUILD_LIBRARY -O0 -ggdb -Og
INCLUDES := -I./code -I./code/misc -I./code/objects -I./freetype/inc -I./pc -I./
TOOL_MKDIR := mkdir

# Define the source files
SRC_FILES :=\
    JniC.cpp\
    code/GameLogic.cpp \
    code/misc/Hitbox.cpp \
    code/objects/Block.cpp \
    code/objects/Bomb.cpp \
    code/objects/Crate.cpp \
    code/objects/Explosion.cpp \
    code/objects/Player.cpp \
    code/misc/Level.cpp \
    pc/keyboard.cpp \
    pc/jni.cpp\
    pc/main.cpp\

# Define the build directory
BUILD_DIR := $(abspath ../../build/windows)

# Define the object files
OBJ_FILES := $(patsubst %.cpp,${BUILD_DIR}/%.o,$(SRC_FILES))

# Define the output executable
OUTPUT_EXE := ${BUILD_DIR}/main.exe

# Define the build rules
all: $(OUTPUT_EXE)

$(OUTPUT_EXE): $(OBJ_FILES)
	$(CXX) $(CFLAGS) $(INCLUDES) -o $@ $^ -Wl,--pdb=${BUILD_DIR}/main.pdb

${BUILD_DIR}/%.o: %.cpp
	@if not exist $(subst /,\,$(dir $@)) $(TOOL_MKDIR) $(subst /,\,$(dir $@))
	$(CXX) $(CFLAGS) $(INCLUDES) -c -o $@ $<

clean:
	rm -f $(OUTPUT_EXE) $(OBJ_FILES) ${BUILD_DIR}

.PHONY: all clean
