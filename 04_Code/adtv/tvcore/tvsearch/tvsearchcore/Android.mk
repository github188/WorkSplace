# libtvsearchLocal
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
LOCAL_CFLAGS    := -Wall -D_REENTRANT -DANDROID_DEBUG -fexceptions -fvisibility=hidden -DHAVE_PTHREADS
LOCAL_SRC_FILES := \
	itvsearchcore.cpp \
	tvsearchcore.cpp \
	basesearch.cpp \
	basesearchmgr.cpp \
	tvsearchmgr.cpp \
	tvnotify.cpp \
	xml.cpp \
	xmlparser.cpp \
			
#LOCAL_SHARED_LIBRARIES := \
#    libz libcutils libutils libdl libstlport libc libstdc++  libtsdemux libtvsearchlocal libstbruntime libtvdevice libtvstbca
LOCAL_SHARED_LIBRARIES :=  libz libcutils libutils libdl libstlport libc libstdc++  libtvsearchlocal 
LOCAL_STATIC_LIBRARIES := libtvstbca libtvdevice libtsdemux libstbruntime libgnustl_static
LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog
LOCAL_CFLAGS := -D_GNU_SOURCE -UNDEBUG -DGOOGLE_PROTOBUF_NO_RTTI -DRIL_SHLIB
base := $(LOCAL_PATH)
LOCAL_C_INCLUDES := \
	$(base) \
	$(base)/../tvsearchinclude \
	$(base)/../../../stbruntime \
	$(base)/../../../include \
	$(base)/../../include
ifndef NDK_ROOT
include external/stlport/libstlport.mk
endif
LOCAL_PRELINK_MODULE := false
LOCAL_MODULE:= libtvsearchcore
LOCAL_MODULE_TAGS := optional
#include $(BUILD_SHARED_LIBRARY)
include $(BUILD_STATIC_LIBRARY)

