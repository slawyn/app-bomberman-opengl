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
	freetype/src/base/ftbbox.c \
	freetype/src/base/ftbitmap.c \
	freetype/src/base/ftglyph.c \
	freetype/src/base/ftstroke.c \
	freetype/src/base/ftxf86.c \
	freetype/src/base/ftbase.c \
	freetype/src/base/ftsystem.c \
	freetype/src/base/ftinit.c \
	freetype/src/base/ftgasp.c \
	freetype/src/raster/raster.c \
	freetype/src/sfnt/sfnt.c \
	freetype/src/smooth/smooth.c \
	freetype/src/autofit/autofit.c \
	freetype/src/truetype/truetype.c \
	freetype/src/cff/cff.c \
	freetype/src/psnames/psnames.c \
	freetype/src/pshinter/pshinter.c

LOCAL_C_INCLUDES += \
	$(LOCAL_PATH)/freetype/inc\
	$(LOCAL_PATH)

LOCAL_CFLAGS += -fPIC -DPIC
LOCAL_CFLAGS += "-DDARWIN_NO_CARBON"
LOCAL_CFLAGS += "-DFT2_BUILD_LIBRARY"
LOCAL_CFLAGS += -O0

include $(BUILD_STATIC_LIBRARY)
