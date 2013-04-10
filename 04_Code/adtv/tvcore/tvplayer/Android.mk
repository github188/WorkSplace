LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_CFLAGS    := -Wall -Wno-unused-parameter -D_REENTRANT -DANDROID_DEBUG -fexceptions -DTS_COUNTER_PRINT
ifdef JS_USE_GEHUACA_SEARCH
LOCAL_CFLAGS += -DJS_USE_GEHUACA_SEARCH
endif
LOCAL_SRC_FILES := tvplayer.cpp \
	               tvcontroller.cpp \
				   tvtask.cpp \
				   epgsearcher.cpp \
				   datetime.cpp \
				   ucsconvert.cpp \
				   tvtimemonitor.cpp \
				   servicemon.cpp


#LOCAL_SHARED_LIBRARIES := \
#    libutils libstlport libstdc++ libtvstbca libtsdemux libtvdevice libtvadapter libstbruntime libtvepg

LOCAL_SHARED_LIBRARIES := libutils libdl libstlport libamplayer
LOCAL_STATIC_LIBRARIES := libgnustl_static \
	libtvstbca libtsdemux libtvdevice libtvadapter libstbruntime libtvepg

### ifdef JS_USE_SHUMA_CALIB_TEST
### LOCAL_STATIC_LIBRARIES += libcalib_shuma
### endif
### 
### ifdef JS_USE_UNION_CALIB
### LOCAL_STATIC_LIBRARIES += libcalib_union
### endif
### 
### ifdef JS_USE_NOVEL_CALIB
### LOCAL_STATIC_LIBRARIES += Y1120-jiashi-RT10UP-safechip-20121102
### endif


LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog

base := $(LOCAL_PATH)

LOCAL_C_INCLUDES := $(base) \
					$(base)/../tvepg \
	                $(base)/../include \
					$(base)/../include/psisi_parse \
					$(base)/../../include \
					$(base)/../../stbruntime

include external/stlport/libstlport.mk

LOCAL_PRELINK_MODULE := false
LOCAL_MODULE:= libtvplayer
LOCAL_MODULE_TAGS := optional

include $(BUILD_STATIC_LIBRARY)
#include $(BUILD_SHARED_LIBRARY)
########################################
ifdef JS_USE_NOVEL_SEARCH
include $(CLEAR_VARS)

LOCAL_C_INCLUDES := $(base) \
	$(base)/../include \
	$(base)/../../include \
	$(base)/../../stbruntime

LOCAL_SRC_FILES  := tvplayer_t.cpp tvservice.cpp

LOCAL_SHARED_LIBRARIES := libutils libstlport libamplayer
LOCAL_STATIC_LIBRARIES := libtvplayer libtvadapter libtvepg libtsdemux  libstbruntime libtvdevice libtvstbca  libgnustl_static Y1120-jiashi-RT10UP-safechip-20121102

include external/stlport/libstlport.mk

LOCAL_PRELINK_MODULE := false
LOCAL_MODULE := tvplayer_t
LOCAL_MODULE_TAGS:= optional

include $(BUILD_EXECUTABLE)
endif
