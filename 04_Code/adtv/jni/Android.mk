LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_CFLAGS    := -DANDROID_DEBUG -fexceptions


LOCAL_SRC_FILES :=\
			novel_supertv_dvb_jni_JniChannelPlay.cpp\
			novel_supertv_dvb_jni_JniChannelSearch.cpp \
			novel_supertv_dvb_jni_JniSetting.cpp	\
			novel_supertv_dvb_jni_JniEpgSearch.cpp \
			JDVBService.cpp 



ifdef JS_USE_SHUMA_CALIB_NORMAL
LOCAL_CFLAGS += -DJS_USE_SHUMA_CALIB_NORMAL
LOCAL_SRC_FILES +=	JniCaInterface.cpp \
			$(base)/../include/utils/DataObject.cpp 
endif



LOCAL_SHARED_LIBRARIES := \
	libutils \
	libstlport \
	libtvcore \

LOCAL_STATIC_LIBRARIES := libgnustl_static


base := $(LOCAL_PATH)

LOCAL_C_INCLUDES := \
	$(base)/../include \
	$(base)/../include/utils \
	$(base)/../stbruntime \

include  $(base)/../../stlport/libstlport.mk


LOCAL_LDLIBS :=  -L$(SYSROOT)/usr/lib -llog

LOCAL_PRELINK_MODULE := false

LOCAL_MODULE:= libadtv

LOCAL_MODULE_TAGS := optional
include $(BUILD_SHARED_LIBRARY)
#include $(BUILD_EXECUTABLE)
