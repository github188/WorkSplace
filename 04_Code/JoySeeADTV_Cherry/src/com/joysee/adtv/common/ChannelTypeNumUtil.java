package com.joysee.adtv.common;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * 用于保存频道类型和频道号
 * 按类型分别保存频道号，避免冲突混淆
 *
 */
public class ChannelTypeNumUtil {

    private static final DvbLog log = new DvbLog("com.joysee.adtv.common.ChannelTypeNumUtil",
            DvbLog.DebugType.D);

    // Suppress default constructor for noninstantiability
    private ChannelTypeNumUtil(){
        throw new AssertionError("Suppress default constructor for noninstantiability");
    }

    /**
     * xml的名称
     */
    public final static String PREFERENCE_NAME = "channel_type_num";

    /**
     * 用于记录播放的频道
     * @param context  上下文
     * @param channel  频道号
     * @param channelType 频道类型
     */
    public static void savePlayChannel(Context context,int channelType,int channelNum){
    	log.D("savePlayChannel enter ");
        SharedPreferences share = context.getSharedPreferences(PREFERENCE_NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = share.edit();
        
        editor.putInt(DefaultParameter.ChannelTypeKey.KEY_CURRENT_TYPE, channelType);
        
        switch(channelType){
        case DefaultParameter.ServiceType.TV:
            
            editor.putInt(DefaultParameter.ChannelTypeKey.KEY_TV, channelNum);
            
            break;
        case DefaultParameter.ServiceType.BC:
            
            editor.putInt(DefaultParameter.ChannelTypeKey.KEY_BC, channelNum);
            
            break;
        }
        
        editor.commit();
        
        log.D("save channel to xml channelType = "+channelType+" and channelNum = "+channelNum);
    }

    /**
     * 用于获取上次播放的频道号
     * @param context
     * @return int 获取 上次播放的频道号，默认为0 为1频道
     */
    public static int getPlayChannelNum(Context context,int channelType){
    	new File("/data/data/com.joysee.adtv/shared_prefs/"+PREFERENCE_NAME+".xml.bak").delete();
        SharedPreferences share = context.getSharedPreferences(PREFERENCE_NAME, Activity.MODE_PRIVATE);
        int channelNum = 0;
        
        switch(channelType){
        case DefaultParameter.ServiceType.TV:
            channelNum = share.getInt(DefaultParameter.ChannelTypeKey.KEY_TV, 0);
            
            break;
        case DefaultParameter.ServiceType.BC:
            
            channelNum = share.getInt(DefaultParameter.ChannelTypeKey.KEY_BC, 0);
            
            break;
        }
        
        log.D("get channel from xml channelNum = "+channelNum);
        
        return channelNum;
    }

    /**
     * 用于获取上次播放的频道类型
     * @param context
     * @return 获取 上次播放的频道类型 默认0 为电视
     */
    public static int getPlayChannelType(Context context){
    	new File("/data/data/com.joysee.adtv/shared_prefs/"+PREFERENCE_NAME+".xml.bak").delete();
        SharedPreferences share = context.getSharedPreferences(PREFERENCE_NAME, Activity.MODE_PRIVATE);
        int channelType = share.getInt(DefaultParameter.ChannelTypeKey.KEY_CURRENT_TYPE, 
                DefaultParameter.ServiceType.TV);
        log.D("get channel from xml current channelType = "+channelType);
        
        return channelType;
    }

}
