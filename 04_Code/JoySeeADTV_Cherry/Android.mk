LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
#LOCAL_JAVA_LIBRARIES := 
#LOCAL_STATIC_JAVA_LIBRARIES := 
LOCAL_STATIC_JAVA_LIBRARIES += android-support-v4
include $(BUILD_MULTI_PREBUILT)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_SRC_FILES += \
       src/com/joysee/adtv/aidl/ISearchService.aidl \
       src/com/joysee/adtv/aidl/OnSearchProgressChangeListener.aidl \
       src/com/joysee/adtv/aidl/OnSearchEndListener.aidl \
       src/com/joysee/adtv/aidl/OnSearchNewTransponderListener.aidl \
       src/com/joysee/adtv/aidl/OnSearchReceivedNewChannelsListener.aidl\
       src/com/joysee/adtv/aidl/OnSearchTunerSignalStateListener.aidl\
       src/com/joysee/adtv/aidl/ca/ICaSettingService.aidl \
       
LOCAL_PACKAGE_NAME := JoySeeTv
LOCAL_CERTIFICATE := platform
LOCAL_DEX_PREOPT := false 
#LOCAL_PROGUARD_ENABLED := full
#LOCAL_PROGUARD_FLAG_FILES := proguard.flags
###############################
include $(BUILD_PACKAGE)
# Use the folloing include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))

###############################
include $(CLEAR_VARS) 
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := android-support-v4:libs/android-support-v4.jar
include $(BUILD_MULTI_PREBUILT)

