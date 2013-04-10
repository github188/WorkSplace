LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

#LOCAL_MODULE_TAGS := 

# Only compile source java files in this apk.
LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_STATIC_JAVA_LIBRARIES := nebula.service leos.service leos.parse leos.parsesub

LOCAL_PACKAGE_NAME := iSmarTV

#LOCAL_SDK_VERSION := current

LOCAL_CERTIFICATE := platform


LOCAL_PROGUARD_ENABLED := disabled

include $(BUILD_PACKAGE)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := nebula.service:lenovo_account/nebula.service.jar leos.service:lenovo_account/leos.service.jar leos.parse:lenovo_account/accessProxy.jar leos.parsesub:lenovo_account/PlayCodeUtil.jar
include $(BUILD_MULTI_PREBUILT)

# Use the following include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))
