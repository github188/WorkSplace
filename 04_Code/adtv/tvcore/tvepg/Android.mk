#libtvepg
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

base := $(LOCAL_PATH)

LOCAL_CFLAGS    := -Wall -D_REENTRANT -DANDROID_DEBUG -fexceptions
#LOCAL_CFLAGS	+= -DFUZHOU

LOCAL_C_INCLUDES := $(base)/../include \
					$(base)/../include/psisi_parse \
					$(base)/../../include \
					$(base)/../../stbruntime
					
LOCAL_SRC_FILES := tvepg.cpp datetime.cpp ucsconvert.cpp

#LOCAL_SHARED_LIBRARIES := \
#    libc libutils libstlport libstdc++ libstbruntime 

LOCAL_SHARED_LIBRARIES := libutils libdl libstlport
LOCAL_STATIC_LIBRARIES := libgnustl_static libstbruntime 
	 
LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog

include external/stlport/libstlport.mk

LOCAL_PRELINK_MODULE := false
LOCAL_MODULE:= libtvepg
LOCAL_MODULE_TAGS := optional

include $(BUILD_STATIC_LIBRARY)
#include $(BUILD_SHARED_LIBRARY)








