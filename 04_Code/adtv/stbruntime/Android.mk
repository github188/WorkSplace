# libstbruntime
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_CFLAGS    := -Wall -D_REENTRANT -DANDROID_DEBUG -fexceptions -fvisibility=hidden -DHAVE_PTHREADS
LOCAL_SRC_FILES := \
	stbRuntime.cpp \
	linuxInterface.cpp \
			
#LOCAL_SHARED_LIBRARIES := \
#    libz libcutils libutils libdl libstlport libc libstdc++
LOCAL_SHARED_LIBRARIES := libcutils libdl libstlport
    
LOCAL_STATIC_LIBRARIES := libgnustl_static
 
LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog
LOCAL_CFLAGS := -D_GNU_SOURCE -UNDEBUG -DGOOGLE_PROTOBUF_NO_RTTI -DRIL_SHLIB

base := $(LOCAL_PATH)
LOCAL_C_INCLUDES := \
	$(base) \
	$(base)/../include \
     frameworks/base/include/utils

include external/stlport/libstlport.mk

LOCAL_PRELINK_MODULE := false
LOCAL_MODULE:= libstbruntime
LOCAL_MODULE_TAGS := optional

include $(BUILD_STATIC_LIBRARY)
#include $(BUILD_SHARED_LIBRARY)
