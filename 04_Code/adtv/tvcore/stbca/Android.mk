LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
base := $(LOCAL_PATH)

ifdef JS_USE_NOVELCA_SEARCH
LOCAL_CFLAGS := -DJS_USE_NOVELCA_SEARCH
endif

ifdef JS_USE_SHUMACA_SEARCH
LOCAL_CFLAGS := -DJS_USE_SHUMACA_SEARCH
endif

ifdef JS_USE_GEHUACA_SEARCH
LOCAL_CFLAGS := -DJS_USE_GEHUACA_SEARCH
endif

ifdef JS_USE_SHUMA_CALIB_NORMAL
LOCAL_CFLAGS += -DJS_USE_SHUMA_CALIB_NORMAL
endif

LOCAL_C_INCLUDES := \
	$(base) \
    $(base)/../include \
    $(base)/../../include \
	$(base)/../../stbruntime \
	frameworks/base/include/utils \
	$(base)/../stbca_shuma

include external/stlport/libstlport.mk
LOCAL_SRC_FILES := \
	capture.cpp \
	stbca.cpp \
	stbca_utility.cpp 


ifdef JS_USE_SHUMA_CALIB_NORMAL
	LOCAL_SRC_FILES +=capture_ex.cpp 
endif
			
LOCAL_SHARED_LIBRARIES := libcutils libdl libstlport
LOCAL_STATIC_LIBRARIES := libgnustl_static libstbruntime libtvdevice libtsdemux

### ifdef JS_USE_UNION_CALIB
### LOCAL_STATIC_LIBRARIES += libcalib_union
### else
### LOCAL_STATIC_LIBRARIES += Y1120-jiashi-RT10UP-safechip-20121102
### endif


LOCAL_PRELINK_MODULE := false
LOCAL_MODULE:= libtvstbca
LOCAL_MODULE_TAGS := optional

### include $(BUILD_SHARED_LIBRARY)
include $(BUILD_STATIC_LIBRARY)

################################################################################
###  hello.exe
ifdef  JS_USE_NOVELCA_SEARCH
base := $(LOCAL_PATH)
include $(CLEAR_VARS)
LOCAL_SRC_FILES := \
	stbca_t.cpp 
			
LOCAL_CFLAGS := -D_GNU_SOURCE -UNDEBUG -DGOOGLE_PROTOBUF_NO_RTTI -DRIL_SHLIB
LOCAL_SHARED_LIBRARIES := libz libcutils libutils libdl libstlport libc libstdc++ 
LOCAL_STATIC_LIBRARIES := libtvstbca libstbruntime libtvdevice libtsdemux 


LOCAL_STATIC_LIBRARIES += Y1120-jiashi-RT10UP-safechip-20121102

LOCAL_C_INCLUDES := \
	$(base) \
	$(base)/../tvsearchInclude \
        android4.0/frameworks/base/include \
    $(base)/../include \
	$(base)/../../include \
	$(base)/../../stbruntime

ifndef NDK_ROOT
include external/stlport/libstlport.mk
endif
LOCAL_PRELINK_MODULE := false
LOCAL_MODULE:=stbca_t
LOCAL_MODULE_TAGS := optional

include $(BUILD_EXECUTABLE)
endif
