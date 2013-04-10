LOCAL_PATH:= $(call my-dir)
################################################################################
# hello.so
include $(CLEAR_VARS)
base := $(LOCAL_PATH)

LOCAL_CFLAGS := -DJS_USE_SHUMACA_SEARCH -fexceptions

ifdef JS_USE_SHUMA_CALIB_NORMAL
LOCAL_CFLAGS += -DJS_USE_SHUMA_CALIB_NORMAL
endif

#ifdef JS_USE_SHUMA_CALIB_YONGAN
#LOCAL_CFLAGS += -DJS_USE_SHUMA_CALIB_YONGAN
#endif
#
#ifdef JS_USE_SHUMA_CALIB_NINGDE
#LOCAL_CFLAGS += -DJS_USE_SHUMA_CALIB_NINGDE
#endif

ifdef JS_USE_SHUMA_CALIB_TEST
LOCAL_CFLAGS += -DJS_USE_SHUMA_CALIB_TEST
endif
ifdef JS_USE_SHUMA_CALIB_HUANGGANG
LOCAL_CFLAGS += -DJS_USE_SHUMA_CALIB_HUANGGANG
endif
ifdef JS_USE_SHUMA_CALIB_NANPING
LOCAL_CFLAGS += -DJS_USE_SHUMA_CALIB_NANPING
endif

LOCAL_C_INCLUDES := \
	$(base) \
    $(base)/../include \
    $(base)/../../include \
	$(base)/../../stbruntime \
	frameworks/base/include/utils

include external/stlport/libstlport.mk
LOCAL_SRC_FILES := \
	dvt_interface.cpp \
	dvtstb_casinf.cpp \
	dvtcas_utility.cpp
			
LOCAL_SHARED_LIBRARIES := libc libcutils libdl libstlport
### LOCAL_STATIC_LIBRARIES := 121205_V5230_Amlogic_8726_sano_shuma


LOCAL_PRELINK_MODULE := false
LOCAL_MODULE:= libcalib_shuma
LOCAL_MODULE_TAGS := optional

### include $(BUILD_SHARED_LIBRARY)
include $(BUILD_STATIC_LIBRARY)
################################################################################
# hello.exe
# 当采用数码相关的库时
ifdef JS_USE_SHUMA_CALIB_NORMAL

include $(CLEAR_VARS)
base := $(LOCAL_PATH)
# LOCAL_SRC_FILES := \
# 	dvt_stbca_t2.cpp \
# 	./../../include/utils/DataObject.cpp

LOCAL_SRC_FILES := \
 	dvt_stbca_t.cpp \
 	./../../include/utils/DataObject.cpp


LOCAL_CFLAGS := -D_GNU_SOURCE -UNDEBUG -DGOOGLE_PROTOBUF_NO_RTTI -DRIL_SHLIB -DJS_USE_SHUMACA_SEARCH -fexceptions
LOCAL_SHARED_LIBRARIES := libz libcutils libutils libdl libstlport libc libstdc++ 

#LOCAL_STATIC_LIBRARIES := libtvstbca libstbruntime libtvdevice libtsdemux libcalib_shuma 121205_V5230_Amlogic_8726_sano_shuma libcalib_shuma libgnustl_static
LOCAL_STATIC_LIBRARIES := libtvstbca libstbruntime libtvdevice libtsdemux libcalib_shuma 121212_V5230_AML8726_shuma_huanggang libcalib_shuma libgnustl_static

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
LOCAL_MODULE:=dvt_stbca_t
LOCAL_MODULE_TAGS := optional
include $(BUILD_EXECUTABLE)

endif	

