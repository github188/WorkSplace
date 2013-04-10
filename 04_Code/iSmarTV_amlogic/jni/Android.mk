LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

base := $(LOCAL_PATH)
#LOCAL_C_INCLUDES := \
#	$(base)/../../../../external/adtv/include \
#	$(base)/../../../../external/adtv/stbruntime \

LOCAL_SHARED_LIBRARIES := libutils libcutils
#LOCAL_SHARED_LIBRARIES := libutils libcutils libtvdevice

LOCAL_MODULE := libjnisetting

#include  $(base)/../../../../external/stlport/libstlport.mk

LOCAL_SRC_FILES := com_ismartv_util_JniSetting.cpp

LOCAL_PRELINK_MODULE:=false

LOCAL_MODULE_TAGS := optional

LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog

include $(BUILD_SHARED_LIBRARY)
