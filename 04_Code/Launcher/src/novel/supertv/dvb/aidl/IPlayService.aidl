package novel.supertv.dvb.aidl;

//import novel.supertv.dvb.jni.struct.tagDVBService;

interface IPlayService{

    // 初始化播放资源，在应用开始的时候必须调用，也是最先调用的接口。
    void init();

    // 释放播放资源,在应用退出的时候必须调用。
    void uninit();

    // 开始播放：
    void play();

    // 停止播放，在停止播放的时候调用
    void stop();

    // 播放下一个频道，在开始播放之后才能调用。
    void next();

    // 播放上一个频道，在开始播放之后才能调用。
    void previous();

    // 设置播放窗口大小
    void setWinSize(in int x,in int y,in int width,in int height);
}
