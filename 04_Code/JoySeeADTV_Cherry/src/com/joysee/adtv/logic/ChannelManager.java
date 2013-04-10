
package com.joysee.adtv.logic;

import java.util.ArrayList;

import com.joysee.adtv.common.DvbLog;
import com.joysee.adtv.logic.bean.DvbService;
import com.joysee.adtv.logic.bean.Transponder;

/**
 * 频道搜索和频道管理Manager
 * @author songwenxuan
 *
 */
public class ChannelManager {
    
    private ChannelManager(){}
    private static ChannelManager channelManager;
    public static ChannelManager getInstance(){
        if(channelManager == null){
            channelManager = new ChannelManager();
        }
        return channelManager;
    }

    private static final DvbLog log = new DvbLog(
            "com.joysee.adtv.logic.ChannelManager", DvbLog.DebugType.D);


    /**
     * @param searchMode 搜索模式：手动 快速 全频
     * @param tuningParam 频点信息：频率 符号率 调制
     * @param pNotify 回调接口
     */
    public native int nativeStartSearchTV(int searchMode, Transponder transponder);

    /**
     * 停止搜索，人为调用的
     */
    public native int nativeCancelSearchTV(boolean isSave);

    /**
     * 按业务分类获取节目索引
     * 
     * @param index
     * @param filter
     * @return
     */
    public native int nativeGetNextDVBService(int index, int filter);
    
    /**
     * 按业务分类获取节目索引
     * 
     * @param index
     * @param filter
     * @return
     */
    public native int nativeGetLastDVBService(int index, int filter);

    /**
     * 按索引获取节目信息
     * 
     * @param index 
     * @param service 参数作为返回值
     */
    public native int nativeGetServiceByIndex(int channelIndex, DvbService service);

    /**
     * 获取当前频道信息
     * 
     * @param service 参数作为返回值
     */
    public native int nativeGetCurrentService(DvbService service);

    /**
     * 按频道号获取频道信息
     * 
     * @param channelNumber
     * @param service
     * @return 底层角标
     */
    public native int nativeGetService(int channelNumber, DvbService service,int type);

    /**
     * 获取频道表数量
     */
    public native int nativeGetServiceCount();

    /**
     * 删除当前频道表
     */
    public native int nativeDelAllService();

    /**
     * @param serviceList 参数作为返回值
     */
    public native int nativeGetAllService(ArrayList<DvbService> serviceList);
}
