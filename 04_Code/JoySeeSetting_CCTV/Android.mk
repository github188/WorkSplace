LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_JAVA_LIBRARIES := bouncycastle
LOCAL_STATIC_JAVA_LIBRARIES := guava
LOCAL_STATIC_JAVA_LIBRARIES += leos_service nebula_service

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_SRC_FILES += \
		src/com/joysee/adtv/aidl/OnSearchEndListener.aidl \
        src/com/joysee/adtv/aidl/ISearchService.aidl \
        src/com/joysee/adtv/aidl/OnSearchNewTransponderListener.aidl \
        src/com/joysee/adtv/aidl/OnSearchProgressChangeListener.aidl \
        src/com/joysee/adtv/aidl/OnSearchReceivedNewChannelsListener.aidl \
        src/com/joysee/adtv/aidl/OnSearchTunerSignalStateListener.aidl \
        src/com/joysee/adtv/aidl/ca/ICaSettingService.aidl \

LOCAL_PACKAGE_NAME := LenovoSettings
LOCAL_CERTIFICATE := platform

LOCAL_PROGUARD_ENABLED := full
LOCAL_PROGUARD_FLAG_FILES := proguard.flags
# LOCAL_REQUIRED_MODULES := liboutputswitchjni
include $(BUILD_PACKAGE)

###############################
include $(CLEAR_VARS) 
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := leos_service:libs/leos.service.jar nebula_service:libs/nebula.service.jar
include $(BUILD_MULTI_PREBUILT)
############################### 

# Use the folloing include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))
