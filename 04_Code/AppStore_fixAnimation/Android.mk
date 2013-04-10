LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := user

#LOCAL_SRC_FILES := \
#    $(call all-java-files-under, src)\
    
LOCAL_SRC_FILES := $(call all-subdir-java-files)\
	src/com/bestv/ott/appstore/aidl/IAppStoreSearch.aidl \
	src/com/bestv/ott/appstore/aidl/IDownloadService.aidl \


#LOCAL_SRC_FILES := $(call all-subdir-java-files)\
#	src/android/content/pm/IPackageInstallObserver.aidl \

LOCAL_MODULE_TAGS := optional

LOCAL_STATIC_JAVA_LIBRARIES := vlctechlib

LOCAL_DEX_PREOPT := false

LOCAL_PACKAGE_NAME := AppStore

LOCAL_JAVA_LIBRARIES :=

#LOCAL_PROGUARD_ENABLED := full  
  
#LOCAL_PROGUARD_FLAGS := -include $(LOCAL_PATH)/proguard.cfg 

LOCAL_CERTIFICATE := platform

include $(BUILD_PACKAGE)

##################################################

include $(CLEAR_VARS)


LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := vlctechlib:libs/BesTVOttFramework.jar
include $(BUILD_MULTI_PREBUILT)
