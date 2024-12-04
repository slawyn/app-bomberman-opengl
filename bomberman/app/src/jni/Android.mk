LOCAL_PATH:= $(call my-dir)
include $(LOCAL_PATH)/Freetype.mk

##########################################
include $(CLEAR_VARS)


LOCAL_MODULE := libJniC

# compile in ARM mode, since the glyph loader/renderer is a hotspot
# when loading complex pages in the browser
#
LOCAL_ARM_MODE:= arm
LOCAL_SRC_FILES:= \
    JniC.cpp\
	code/GameLogic.cpp\
    code/misc/Hitbox.cpp\
    code/objects/Block.cpp\
    code/objects/Bomb.cpp\
    code/objects/Crate.cpp\
    code/objects/Explosion.cpp\
    code/objects/Player.cpp\
    code/misc/Level.cpp

LOCAL_C_INCLUDES += \
	$(LOCAL_PATH)/code\
    $(LOCAL_PATH)/code/misc\
    $(LOCAL_PATH)/code/objects\
	$(LOCAL_PATH)

# dependencies 
LOCAL_C_INCLUDES += $(LOCAL_PATH)/freetype/inc
LOCAL_STATIC_LIBRARIES:=libfreetype


# lags
LOCAL_CFLAGS += -W -Wall
LOCAL_CFLAGS += -fPIC -DPIC
LOCAL_CFLAGS += "-DDARWIN_NO_CARBON"
LOCAL_CFLAGS += "-DFT2_BUILD_LIBRARY"
LOCAL_CFLAGS += -O0
LOCAL_CFLAGS += 

include $(BUILD_SHARED_LIBRARY)
