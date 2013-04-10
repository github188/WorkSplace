LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_CFLAGS    := -Wall -Wno-unused-parameter -D_REENTRANT -DANDROID_DEBUG 

LOCAL_SRC_FILES := tvdevice.cpp \
	               tunerdevice.cpp

#LOCAL_SHARED_LIBRARIES := \
#    libz libcutils libutils libstlport libc libstdc++
LOCAL_SHARED_LIBRARIES := libcutils libstlport

LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog

base := $(LOCAL_PATH)

LOCAL_C_INCLUDES := $(base)/../include \
					$(base)/../driver \
	                $(base)/../stbruntime

include external/stlport/libstlport.mk

LOCAL_PRELINK_MODULE := false

LOCAL_MODULE:= libtvdevice

LOCAL_MODULE_TAGS := optional

include $(BUILD_STATIC_LIBRARY)
