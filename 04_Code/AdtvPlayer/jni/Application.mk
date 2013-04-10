########################################################
#    system --> 系统默认的最小支持的C++运行时库
#    stlport_static --> 以静态链接的方式使用stlport版本的STL
#    stlport_shared --> 以动态链接的方式使用stlport版本的STL
#    gnustl_static  --> 以静态链接的方式使用gnu版本的STL
#########################################################

APP_STL := stlport_static

#APP_CFLAGS += -fexceptions
LOCAL_SHARED_LIBRARIES +=  libutils libcutils libui libbinder libdemux libnovelplayer libdemuxservice 

#LOCAL_LDLIBS := -L(/usr/lib)  -llog \
#    ndk/sources/cxx-stl/stlport/libs/armeabi/libstlport_static.a
