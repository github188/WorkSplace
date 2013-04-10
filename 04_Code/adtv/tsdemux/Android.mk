LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

base := $(LOCAL_PATH)

LOCAL_CFLAGS    := -Wall -Wno-unused-parameter -D_REENTRANT -DANDROID_DEBUG 

LOCAL_C_INCLUDES := $(base)/../include \
	                $(base)/../stbruntime \
                    $(base)    
					
LOCAL_SRC_FILES := tsdemux.cpp \
				   TsCatcher.cpp \
				   NtfTS2Section.cpp \
				   sectionfilter.cpp 

LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog

#LOCAL_SHARED_LIBRARIES := \
#    libz libcutils libutils libstlport libc libstdc++ libstbruntime libtvdevice
LOCAL_SHARED_LIBRARIES := libcutils libstlport libdl
LOCAL_STATIC_LIBRARIES := libstbruntime libtvdevice

include external/stlport/libstlport.mk

LOCAL_PRELINK_MODULE := false
LOCAL_MODULE:= libtsdemux
LOCAL_MODULE_TAGS := optional

include $(BUILD_STATIC_LIBRARY)
#include $(BUILD_SHARED_LIBRARY)

######################################################################
include $(CLEAR_VARS)

LOCAL_C_INCLUDES := $(base)/../include \
	                $(base)/../stbruntime
					
LOCAL_SRC_FILES  := tsdemux_t.cpp adDemux.cpp

#LOCAL_SHARED_LIBRARIES  := libz libcutils libutils libstlport libc libstdc++ libstbruntime libtvdevice libtsdemux
LOCAL_SHARED_LIBRARIES  := libcutils libstlport
LOCAL_STATIC_LIBRARIES  := libstbruntime libtvdevice libtsdemux

include external/stlport/libstlport.mk

LOCAL_PRELINK_MODULE := false
LOCAL_MODULE := tsdemux_t
LOCAL_MODULE_TAGS:= optional

include $(BUILD_EXECUTABLE)

