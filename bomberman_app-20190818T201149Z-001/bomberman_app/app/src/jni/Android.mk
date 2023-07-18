# this is now the default FreeType build for Android
#
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := libfreetype

# compile in ARM mode, since the glyph loader/renderer is a hotspot
# when loading complex pages in the browser
#
LOCAL_ARM_MODE := arm

LOCAL_SRC_FILES:= \
	src/base/ftbbox.c \
	src/base/ftbitmap.c \
	src/base/ftglyph.c \
	src/base/ftstroke.c \
	src/base/ftxf86.c \
	src/base/ftbase.c \
	src/base/ftsystem.c \
	src/base/ftinit.c \
	src/base/ftgasp.c \
	src/raster/raster.c \
	src/sfnt/sfnt.c \
	src/smooth/smooth.c \
	src/autofit/autofit.c \
	src/truetype/truetype.c \
	src/cff/cff.c \
	src/psnames/psnames.c \
	src/pshinter/pshinter.c

LOCAL_C_INCLUDES += \
    $(LOCAL_PATH)/code/misc\
    $(LOCAL_PATH)/code/objects\
	$(LOCAL_PATH)/builds \
	$(LOCAL_PATH)/include\
	$(LOCAL_PATH)

LOCAL_CFLAGS += -W -Wall
LOCAL_CFLAGS += -fPIC -DPIC
LOCAL_CFLAGS += "-DDARWIN_NO_CARBON"
LOCAL_CFLAGS += "-DFT2_BUILD_LIBRARY"
LOCAL_CFLAGS += -O0

include $(BUILD_STATIC_LIBRARY)


##########################################
include $(CLEAR_VARS)

LOCAL_MODULE := libGameLogic

# compile in ARM mode, since the glyph loader/renderer is a hotspot
# when loading complex pages in the browser
#
LOCAL_ARM_MODE:= arm
LOCAL_SRC_FILES:= \
	GameLogic.cpp\
    code/misc/Hitbox.cpp\
    code/objects/Block.cpp\
    code/objects/Bomb.cpp\
    code/objects/Crate.cpp\
    code/objects/Explosion.cpp\
    code/objects/Player.cpp\
    code/misc/Level.cpp

LOCAL_C_INCLUDES += \
	$(LOCAL_PATH)/builds \
	$(LOCAL_PATH)/include\
    $(LOCAL_PATH)/code/misc\
    $(LOCAL_PATH)/code/objects\
	$(LOCAL_PATH)

LOCAL_STATIC_LIBRARIES:=libfreetype
LOCAL_CFLAGS += -W -Wall
LOCAL_CFLAGS += -fPIC -DPIC
LOCAL_CFLAGS += "-DDARWIN_NO_CARBON"
LOCAL_CFLAGS += "-DFT2_BUILD_LIBRARY"
LOCAL_CFLAGS += -O0

include $(BUILD_SHARED_LIBRARY)
