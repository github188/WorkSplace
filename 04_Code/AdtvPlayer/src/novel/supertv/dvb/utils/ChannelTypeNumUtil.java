package novel.supertv.dvb.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * 用于保存频道类型和频道号
 * 按类型分别保存频道号，避免冲突混淆
 *
 */
public class ChannelTypeNumUtil {

    private static final DvbLog log = new DvbLog("novel.supertv.dvb.utils.ChannelTypeUtil",
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
        SharedPreferences share = context.getSharedPreferences(PREFERENCE_NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = share.edit();
        
        editor.putInt(DefaultParameter.ChannelTypeKey.KEY_CURRENT_TYPE, channelType);
        
        switch(channelType){
        case DefaultParameter.ServiceType.digital_television_service:
            
            editor.putInt(DefaultParameter.ChannelTypeKey.KEY_DIGITAL_TELEVISION_SERVICE, channelNum);
            
            break;
        case DefaultParameter.ServiceType.digital_radio_sound_service:
            
            editor.putInt(DefaultParameter.ChannelTypeKey.KEY_DIGITAL_RADIO_SOUND_SERVICE, channelNum);
            
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
        SharedPreferences share = context.getSharedPreferences(PREFERENCE_NAME, Activity.MODE_PRIVATE);
        int channelNum = 0;
        
        switch(channelType){
        case DefaultParameter.ServiceType.digital_television_service:
            
            channelNum = share.getInt(DefaultParameter.ChannelTypeKey.KEY_DIGITAL_TELEVISION_SERVICE, 0);
            
            break;
        case DefaultParameter.ServiceType.digital_radio_sound_service:
            
            channelNum = share.getInt(DefaultParameter.ChannelTypeKey.KEY_DIGITAL_RADIO_SOUND_SERVICE, 0);
            
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
        SharedPreferences share = context.getSharedPreferences(PREFERENCE_NAME, Activity.MODE_PRIVATE);
        int channelType = share.getInt(DefaultParameter.ChannelTypeKey.KEY_CURRENT_TYPE, 
                DefaultParameter.ServiceType.digital_television_service);
        log.D("get channel from xml current channelType = "+channelType);
        
        return channelType;
    }

}
