# libtvdevice.
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

ifdef JS_UNION_SHUMA_CALIB
	LOCAL_CFLAGS :=-DJS_UNION_SHUMA_CALIB
endif

ifdef JS_SHUMA_CALIB_1102
	LOCAL_CFLAGS    += -DJS_SHUMA_CALIB_1102
endif

ifdef JS_SHUMA_CALIB_1103
	LOCAL_CFLAGS    += -DJS_SHUMA_CALIB_1103
endif

ifdef JS_UNION_GEHUA_CALIB
	LOCAL_CFLAGS := -DJS_UNION_GEHUA_CALIB
endif

LOCAL_SRC_FILES := \
	utcas_interface.c\
	utcas_priv.c\
	utcasa_dummy.c \
	plugin_ca/pluginca.c\
	plugin_ca/shuma/shuma.c\
	plugin_ca/gehua/gh.c\

LOCAL_PRELINK_MODULE := false
LOCAL_MODULE:= libcalib_union
LOCAL_MODULE_TAGS := optional

include $(BUILD_STATIC_LIBRARY)
