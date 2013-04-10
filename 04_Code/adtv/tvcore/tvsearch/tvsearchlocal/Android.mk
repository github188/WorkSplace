LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
base := $(LOCAL_PATH)

LOCAL_CFLAGS    := -Wall -D_REENTRANT -DANDROID_DEBUG -fexceptions -fvisibility=hidden

ifdef JS_USE_NOVELCA_SEARCH
	LOCAL_CFLAGS    += -DJS_USE_NOVELCA_SEARCH
endif

ifdef JS_USE_SHUMACA_SEARCH
	LOCAL_CFLAGS    += -DJS_USE_SHUMACA_SEARCH
endif

ifdef JS_USE_GEHUACA_SEARCH
	LOCAL_CFLAGS += -DJS_USE_GEHUACA_SEARCH
endif

LOCAL_C_INCLUDES := \
	$(base) \
	$(base)/../tvsearchinclude \
	$(base)/../../include/ \
	$(base)/../../include/psisi_parse \
	$(base)/../../include/psisi_parse/imp \
	$(base)/../../../stbruntime \
	$(base)/../../../include

include external/stlport/libstlport.mk

LOCAL_SRC_FILES := \
	parsebase.cpp \
	tvsearchlocal.cpp \
	ucsconvert.cpp \

ifdef JS_BEJING_NIT_SEARCH
	LOCAL_SRC_FILES+=beijing/parse_beijing.cpp
	LOCAL_CFLAGS += -DJS_BEJING_NIT_SEARCH
endif

ifdef JS_FUZHOU_NIT_SEARCH	
	LOCAL_SRC_FILES+=fuzhou/parse_fuzhou.cpp
	LOCAL_CFLAGS += -DJS_FUZHOU_NIT_SEARCH
endif


ifdef JS_QINGDAO_NIT_SEARCH
	LOCAL_CFLAGS += -DJS_QINGDAO_NIT_SEARCH
	LOCAL_SRC_FILES+=qingdao/parse_qingdao.cpp
endif

LOCAL_SHARED_LIBRARIES := \
	libz libcutils libutils libdl libstlport libc libstdc++
LOCAL_STATIC_LIBRARIES := libstbruntime libgnustl_static


LOCAL_PRELINK_MODULE := false
LOCAL_MODULE:= libtvsearchlocal
LOCAL_MODULE_TAGS := optional

include $(BUILD_SHARED_LIBRARY)
#include $(BUILD_EXECUTABLE)
