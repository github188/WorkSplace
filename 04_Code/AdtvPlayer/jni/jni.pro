HEADERS += \
    Common.h \
    ConditionAccess.h \
    novel_supertv_dvb_jni_JniChannelPlay.h \
SOURCES += \
    JNILoader.cpp \
    Common.cpp \
    novel_supertv_dvb_jni_JniChannelPlay.cpp \

INCLUDEPATH +=  /home/zhyh/AndroidNDK/build/platforms/android-8/arch-arm/usr/include \
                ../../../../external/dvb/src/include \
                ../../../../external/dvb/src/include/dm \
                ../../../../external/dvb/src/dm/TableSystem \
                ../../../../external/dvb/src/CA/include/cas/ \
                ../../../../external/dvb/src/include/dm/pvr/include \
                ../../../../external/dvb/src/include/dm/epg/include \
                ../../../../external/dvb/src/dm/programPlay/include \
                ../../../../external/dvb/src/dm/programSearch/include \
                ../../../../external/dvb/src/dm/pvr/include \
                ../../../../external/dvb/src/dm/serviceFactory/include \
                ../../../../external/dvb/src/include/dm/bouquet/include

OTHER_FILES += \
    Android.mk






