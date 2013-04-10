package novel.supertv.dvb.jni;

import novel.supertv.dvb.jni.struct.tagDVBService;
import novel.supertv.dvb.jni.struct.tagMiniEPGNotify;
import novel.supertv.dvb.utils.DvbLog;
import android.content.Context;
import android.content.IntentSender.SendIntentException;


public final class JniChannelPlay {

    private static final DvbLog log = new DvbLog(
            "novel.supertv.dvb.jni.JniChannelPlay",DvbLog.DebugType.D);

    // 测试函数，测试JNI返回字符串是否正常
    public native String getUriString();

    /**
     * 打开demux
     * @return 1 成功 0 失败
     */
    public native int open();

    /**
     * 关闭demux
     * @return 1 成功 0 失败
     */
    public native int close();

    /**
     * Tuner的数量
     * @return 个数
     */
    public native int getTunerCount();

    /**
     * 锁频 是否成功
     * @param iTuner Tuner的编号
     * @param frequency   频率
     * @param qam         调制方式
     * @param symbol rate 符号率
     * @return true 成功 false 失败
     */
    public native boolean tune(int iTuner,int frequency,int qam,int symb);

    /**
     *  查询Tuner是否锁频的状态
     * @param iTuner 编号
     * @return true 锁频了 false 未锁
     */
    public native boolean getLocked(int iTuner);

    /**
     * 增加ts的过滤条件
     * @param pid
     * @return
     */
    public int addFilter(int pid){
        
        return addTsFilter("novel.player",pid);
        
    }
    public native int addTsFilter(String name,int pid);

    /**
     * 删除ts的过滤条件
     * @param pid
     * @return
     */
    public int delFilter(int pid){
        
        return delTsFilter("novel.player",pid);
        
    }
    public native int delTsFilter(String name,int pid);

    /**
     * 删除全部ts的过滤条件
     * @return 1 成功 0 失败
     */
    public native int delAllTsFilter();

    public int addSectionFilter(int pid,int tid,int timeout){
        
        return addSectionFilter("novel.player", pid, tid, timeout);
        
    }
    public native int addSectionFilter(String name,int pid,int tid,int timeout);

    /**
     * 删除Sectin过滤条件
     * @param pid
     * @param tid
     * @return
     */
    public int delSectionFilter(int pid,int tid){
        
        return delSectionFilter("novel.player", pid, tid);
        
    }
    public native int delSectionFilter(String name,int pid,int tid);

    /**
     * 删除全部Section的过滤条件
     * @return 1 成功 0 失败
     */
    public native int delSectionAllFilter(String name);

    /**
     * ts数据上行的开关
     * @param enablets
     * @return true 开启 false 关闭
     */
    public native int enableTS(boolean enablets);

    /**
     * 设置回调
     * @param name
     * @return
     */
    public native int setClientCallBack(String name);

    public native int start(int vidiopid,int vfat,int audiopid,int afat);

    static 
    {
        log.D("************************load adtv start************");
        System.loadLibrary("adtv");
        log.D("************************load adtv end  ************");
    }

    public native int init();

    public native int uninit();

    public native int play();

    public native int stop();

    public native int setService(tagDVBService service);


    public native int delAllTVNotify();

    
    //TVCORE_API void tvcore_addTVNotify(TVNOTIFY callback);
    //TVCORE_API void tvcore_delTVNotify(TVNOTIFY callback);
    //TVCORE_API void tvcore_delAllTVNotify();

    protected static  OnPfListener onPfListener;

    public interface OnPfListener{
        
        void showPf(tagMiniEPGNotify pf);
        
    }

    public void setOnPfListener(OnPfListener pfListener){
        
        this.onPfListener = pfListener;
    }

    // JNI 回调的函数 miniEPG
    public static void onPfCallBack(tagMiniEPGNotify notify){
        log.D("******************onPfCallBack()************"+notify.getCurrentEventName());
        log.D("******************onPfCallBack()************"+notify.getNextEventName());
        if(onPfListener != null){
            onPfListener.showPf(notify);
        }
        
        JniChannelPlay.notify = notify;
    }

    public static tagMiniEPGNotify notify;

    public tagMiniEPGNotify getPf(){
        log.D("getPf()");
        return notify;
    }

    // JNI 回调的函数 各种监控
    public static void onMonitorCallBack(int monitorType,Object message){
        log.D("**************onMonitorCallBack()***************monitorType"+monitorType);
        if(onMonitorListener != null){
            onMonitorListener.onMonitor(monitorType, message);
        }
    }

    protected static  OnMonitorListener onMonitorListener;

    public interface OnMonitorListener{
        
        void onMonitor(int monitorType,Object message);
        
    }

    public void setOnMonitorListener(OnMonitorListener onMonitorListener){
        this.onMonitorListener = onMonitorListener;
    }
}