# Define the compiler and flags
CC := gcc
CXX := g++
CFLAGS := -W -Wall -fPIC -DPIC -DDARWIN_NO_CARBON -DFT2_BUILD_LIBRARY -O0
INCLUDES := -I./code -I./code/misc -I./code/objects -I./freetype/inc

# Define the source files
SRC_FILES :=\
    code/GameLogic.cpp \
    code/misc/Hitbox.cpp \
    code/objects/Block.cpp \
    code/objects/Bomb.cpp \
    code/objects/Crate.cpp \
    code/objects/Explosion.cpp \
    code/objects/Player.cpp \
    code/misc/Level.cpp\
    main.cpp

# Define the output library
OUTPUT_LIB := ../../build/main.exe

# Define the build rules
all: $(OUTPUT_LIB)

$(OUTPUT_LIB): $(SRC_FILES)
# $(info "$(CXX) $(CFLAGS) $(INCLUDES) -shared -o $@ $^")
	$(CXX) $(CFLAGS) $(INCLUDES) -o $@ $^

clean:
	rm -f $(OUTPUT_LIB) *.o

.PHONY: all clean
