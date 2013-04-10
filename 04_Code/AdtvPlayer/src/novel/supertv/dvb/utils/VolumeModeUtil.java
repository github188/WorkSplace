package novel.supertv.dvb.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * 用于获取声道类型 xml
 */
public class VolumeModeUtil {
    private static final DvbLog log = new DvbLog("novel.supertv.dvb.utils.ChannelTypeUtil",
            DvbLog.DebugType.D);

    // Suppress default constructor for noninstantiability
    private VolumeModeUtil(){
        throw new AssertionError("Suppress default constructor for noninstantiability");
    }

    /**
     * xml的名称
     */
    public final static String PREFERENCE_VOLUME_NAME = "volume_mode";
    
    /** 声道属性名 */
    public final static String VOLUME_MODE = "volume_mode";

    /**
     * 设置画面设置值
     * @param context
     * @param mode
     */
    public static void savePlayMode(Context context,int mode){
        SharedPreferences share = context.getSharedPreferences(PREFERENCE_VOLUME_NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = share.edit();
        
        editor.putInt(VOLUME_MODE, mode);
        log.D("savePlayMode="+mode);
        editor.commit();
        
    }

    /**
     * 获取画面设置值
     * @param context
     * @return
     */
    public static int getPlayMode(Context context){
        SharedPreferences share = context.getSharedPreferences(PREFERENCE_VOLUME_NAME, Activity.MODE_PRIVATE);
        int mode = share.getInt(VOLUME_MODE, 1);
        log.D("getPlayMode="+mode);
        return mode;//1为全屏 填充画面
    }
}
