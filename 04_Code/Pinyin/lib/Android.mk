LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES := \
         $(call all-subdir-java-files) \
         com/joysee/pinyin/IPinyinDecoderService.aidl

LOCAL_MODULE := com.joysee.pinyin.lib

include $(BUILD_STATIC_JAVA_LIBRARY)
