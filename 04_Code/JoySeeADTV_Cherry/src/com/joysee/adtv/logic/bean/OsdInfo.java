package com.joysee.adtv.logic.bean;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.joysee.adtv.common.DefaultParameter;
import com.joysee.adtv.common.DvbLog;

/**
 * 封装OSD信息结构
 * @author wuhao
 */
public class OsdInfo {
    private static final DvbLog log = new DvbLog(
            " com.joysee.adtv.jni.struct.tagOsdInfo", DvbLog.DebugType.D);
    /**
     * OSD显示位置和显示状态
     * 高16位：显示位置 Top Bottom
     * 底16位：显示状态 0：隐藏 1：显示
     */
    private int osdPosAndState;
    /**
     * OSD的显示内容
     */
    private String osdMsg;

    public int getOsdInfo() {
        return osdPosAndState;
    }

    public void setOsdInfo(int osdInfo) {
        this.osdPosAndState = osdInfo;
    }

    public String getOsdMsg() {
        return osdMsg;
    }

    public void setOsdMsg(String osdMsg) {
        this.osdMsg = osdMsg;
    }

    /**
     * 获得OSD的显示位置和显示方式（半/满屏） 
     * 0x01 OSD风格：显示在屏幕上方 
     * 0x02 OSD风格：显示在屏幕下方 
     * 0x03 OSD风格：整屏显示 
     * 0x04 OSD风格：半屏显示
     * @return
     */
    public int getShowPosition() {
        int temp = osdPosAndState;
        log.D("-------- getShowPosition  osdPosAndState = " + osdPosAndState);
        return temp >> 16;
    }
    /**
     * 获得OSD的显示状态
     * @return int 0：隐藏 1：显示
     */
    public int getShowOrHide() {
        int temp = osdPosAndState;
        log.D("-------- getShowOrHide  osdPosAndState = " + osdPosAndState);
        return temp & 0x0000ffff;
    }
    /**
     * 保存OSD的状态到SharePreferences
     * @param context
     * @param state 
     * @param msg 
     */
    public void saveOsdStateToSharePre(Context context, int state, String msg,
            int position) {
        SharedPreferences OsdPreferences = context.getSharedPreferences(
                DefaultParameter.PREFERENCE_NAME, Activity.MODE_PRIVATE);
        Editor edit = OsdPreferences.edit();
        edit.putInt(DefaultParameter.TpKey.KEY_OSD_STATE, state);
        if (null != msg) {
            edit.putString(DefaultParameter.TpKey.KEY_OSD_MSG, msg);
        }
        edit.putInt(DefaultParameter.TpKey.KEY_OSD_POSITION, position);
        edit.commit();
    }
}
