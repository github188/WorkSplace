LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

#LOCAL_CFLAGS    := -DANDROID_DEBUG 
LOCAL_SRC_FILES :=\
                  novel_supertv_dvb_jni_JniChannelPlay.cpp\
                  novel_supertv_dvb_jni_JniChannelSearch.cpp
			


LOCAL_SHARED_LIBRARIES := \
  libutils \
  libcutils \
  libui \
   libcutils \
   libstlport \
   libbinder \
   libtvsearchLocal \
   libnovelplayer \
   libdemuxservice \
   libtvsearchCore
   

base := $(LOCAL_PATH)

LOCAL_C_INCLUDES := \
	$(base) \
	$(base)/../demux/libdemuxservice \
	$(base)/../demux/libdemuxservice/libdemux \
	$(base)/../usbtv \
	$(base)/../include \
	$(base)/../demux/libdemuxservice/demux_callback \
	$(base)/../player/libplayerservice/libplayer \
	$(base)/../player/libplayerservice/loop_buffer\
	$(base)/../player/libplayerservice\
        ~/android4.0.3/frameworks/base/include/utils

include  ~/android4.0.3/external/stlport/libstlport.mk


LOCAL_LDLIBS :=  -L$(SYSROOT)/usr/lib -llog

LOCAL_PRELINK_MODULE := false

LOCAL_MODULE:= libadtv

LOCAL_MODULE_TAGS := optional
include $(BUILD_SHARED_LIBRARY)
#include $(BUILD_EXECUTABLE)
