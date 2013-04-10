package novel.supertv.dvb.jni;

import java.util.Vector;

import novel.supertv.dvb.jni.struct.tagEntitle;
import novel.supertv.dvb.jni.struct.tagWatchTime;

public class JniSetting {

    /**
     * 测试函数
     * @return
     */
    public native String getUriString();
    
    public native int caInit();
    public native int caUnInit();
    
    
    //获取观看等级
    public native int getWatchLevel();
    //设置观看等级
    public native int setWatchLevel(String pin, int level);

    /**
     * 设置工作时段 小时
     * @param start
     * @param end
     * @return 成功失败
     */
    public native int setWatchTime0(int start,int end);

    /**
     * 设置工作时段 小时 需密码时
     * @param pwd
     * @param start
     * @param end
     * @return
     */
    public native int setWatchTime1(String pwd,int start,int end);

    /**
     * 设置工作时段 时分秒 需密码
     * @param pwd
     * @param iStarthour
     * @param iStartMin
     * @param iStartSec
     * @param iEndHour
     * @param iEndMin
     * @param iEndSec
     * @return
     */
    public native int setWatchTime2(String pwd, int iStarthour,int  iStartMin,int  iStartSec,
            int iEndHour,int iEndMin,int iEndSec);
    
    /**
     * 获取工作时段
     * @param iStarthour
     * @param iEndhour
     * @return
     */
    public native int getWatchTime(tagWatchTime tag);

    /**
     * 修改密码
     * @param oldPwd
     * @param newPwd
     * @return
     */
    public native int changePincode(String oldPwd,String newPwd);


    
    //获取卡序列号，成功返回序号字符串，失败返回空字符串
    public native String getCardSN();
    
    //获取授权信息列表
    //参数为运营商ID
    public native int getAuthorization(int operID,Vector<tagEntitle> vec);
    //获取运营商ID
    public native int getOperatorID(Vector<Integer> vector);

    
    ///////////////////////////////////////////////////////////////

    /**
     * 声道设置
     * @param volume
     * @return
     */
    public native int setVolume(int volume);
    //获取声量
    public native int getVolume();
    
    //设置声道
    //成功返回0
    public native int setAudioChannel(int channel);
    //获取声道信息,返回值非负返回声道信息
    public native int getAudioChannel();

    /**
     * 画面设置
     * @param screen
     * @return
     */
    public native int setDisplayMode(int mode);
    //设置视频播放位置，大小
    public native int setDisplayRect(int x, int y, int width, int height);
    
    public native int cleanVideoFrame(boolean clean);
    

    /**
     * 设置多语言且画
     * @param language
     * @return
     */
    public native int setAudioLanguage(int language);

    /**
     * 获取伴音个数
     * @return
     */
    public native int getAudioLanguage();
    
}