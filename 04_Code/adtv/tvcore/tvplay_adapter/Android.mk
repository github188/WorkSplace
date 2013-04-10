#libtvadapter
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

base := $(LOCAL_PATH)

LOCAL_CFLAGS    := -Wall -D_REENTRANT -DANDROID_DEBUG 

LOCAL_C_INCLUDES := $(base)/../include \
		$(base)/../../include \
		$(base)/../include/amcodec \
		$(base)/../include/amadec \

LOCAL_SRC_FILES := TsPlayer.cpp \
			ringbuffer.cpp \
			tvplay_adapter.cpp \

#LOCAL_SHARED_LIBRARIES := \
#    libz libcutils libutils libstlport libc libstdc++  libamplayer
LOCAL_SHARED_LIBRARIES := libcutils libstlport libamplayer
	
LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog

ifndef NDK_ROOT
include external/stlport/libstlport.mk
endif

LOCAL_PRELINK_MODULE := false

LOCAL_MODULE:= libtvadapter

LOCAL_MODULE_TAGS := optional

include $(BUILD_STATIC_LIBRARY)
#include $(BUILD_SHARED_LIBRARY)


