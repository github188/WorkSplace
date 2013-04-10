LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
base := $(LOCAL_PATH)

LOCAL_CFLAGS    := -Wall -Wno-unused-parameter -D_REENTRANT -DANDROID_DEBUG -fexceptions

LOCAL_C_INCLUDES := $(base)/../include \
					$(base)/../include/utils \
	                $(base)/include

LOCAL_SRC_FILES := tvcore.cpp 

ifdef JS_USE_SHUMA_CALIB_NORMAL
LOCAL_SRC_FILES += tvcore_ca.cpp \
				./../include/utils/DataObject.cpp
endif

#LOCAL_C_INCLUDES := $(base)/../include \
#	                $(base)/include
#
#LOCAL_SRC_FILES := tvcore.cpp tvcore_ca.cpp

#LOCAL_SHARED_LIBRARIES := \
#    libz libcutils libutils libdl libstlport libc libstdc++ libtvstbca libtvplayer libstbruntime libtvsearchcore
LOCAL_SHARED_LIBRARIES := libz libcutils libutils libdl libstlport libc libstdc++ libtvsearchlocal libamplayer 
LOCAL_STATIC_LIBRARIES := libtvstbca  libtvplayer libtvepg libtvadapter libtvadapter libtvsearchcore libtvdevice libstbruntime libtsdemux libgnustl_static

ifdef JS_USE_SHUMA_CALIB_HUANGGANG
LOCAL_STATIC_LIBRARIES += libcalib_shuma 
LOCAL_STATIC_LIBRARIES += libcalib_shuma 121212_V5230_AML8726_shuma_huanggang libcalib_shuma
endif

ifdef JS_USE_SHUMA_CALIB_YONGAN
LOCAL_STATIC_LIBRARIES += libcalib_shuma 
LOCAL_STATIC_LIBRARIES += libcalib_shuma 121218_V5230_AML8726_shuma_YongAn libcalib_shuma
endif

ifdef JS_USE_SHUMA_CALIB_NANPING
LOCAL_STATIC_LIBRARIES += libcalib_shuma 
LOCAL_STATIC_LIBRARIES += libcalib_shuma 121218_V5230_AML8726_shuma_nanping libcalib_shuma
endif

ifdef JS_USE_SHUMA_CALIB_NINGDE
LOCAL_STATIC_LIBRARIES += libcalib_shuma 
LOCAL_STATIC_LIBRARIES += libcalib_shuma 121224_V5230_AML8726_shuma_ningde libcalib_shuma
endif


ifdef JS_USE_SHUMA_CALIB_LONGYAN
LOCAL_STATIC_LIBRARIES += libcalib_shuma 
LOCAL_STATIC_LIBRARIES += libcalib_shuma 121224_V5230_AML8726_shuma_longyan libcalib_shuma
endif

ifdef JS_USE_SHUMA_CALIB_PUTIAN
LOCAL_STATIC_LIBRARIES += libcalib_shuma 
LOCAL_STATIC_LIBRARIES += libcalib_shuma 121224_V5230_AML8726_shuma_putian libcalib_shuma
endif

ifdef JS_USE_SHUMA_CALIB_TEST
LOCAL_STATIC_LIBRARIES += libcalib_shuma 
LOCAL_STATIC_LIBRARIES += 121205_V5230_Amlogic_8726_sano_shuma libcalib_shuma
endif

ifdef JS_USE_UNION_CALIB
LOCAL_STATIC_LIBRARIES += libcalib_union
endif

ifdef JS_USE_NOVEL_CALIB
LOCAL_STATIC_LIBRARIES += Y1120-jiashi-RT10UP-safechip-20121102
endif

LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog

include external/stlport/libstlport.mk

LOCAL_PRELINK_MODULE := false
LOCAL_MODULE:= libtvcore
LOCAL_MODULE_TAGS := optional

include $(BUILD_SHARED_LIBRARY)
include $(call all-makefiles-under,$(LOCAL_PATH))

