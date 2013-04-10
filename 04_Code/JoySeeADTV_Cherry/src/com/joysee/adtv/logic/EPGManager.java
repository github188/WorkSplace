package com.joysee.adtv.logic;

import java.util.ArrayList;

import android.util.Log;

import com.joysee.adtv.logic.bean.NETDetailEventInfo;
import com.joysee.adtv.logic.bean.NETEventInfo;
import com.joysee.adtv.logic.bean.ProgramType;
import com.joysee.adtv.logic.bean.Transponder;


public class EPGManager {
	
	private EPGManager(){}
    private static EPGManager epgManater;
    public static EPGManager getInstance(){
        if(epgManater == null){
        	epgManater = new EPGManager();
        }
        return epgManater;
    }

	
	/**
	 * 通过id获取分类列表
	 * @param id 初始传0xFF
	 * @param programTypeList 分类信息list，参数作为返回值
	 * @return
	 */
	public native int nativeGetProgramTypes(int id, ArrayList<ProgramType> programTypeList);

	/**
	 * 通过节目类型，开始、结束时间获取节目id列表
	 * @param programType 节目类型
	 * @param startTime 开始时间
	 * @param endTime 结束时间
	 * @param programIdList 节目id列表，参数作为返回值
	 * @return
	 */
	public native int nativeGetProgramIdListByType(int programType, long startTime, long endTime, ArrayList<Integer> programIdList);

	/**
	 * 通过serviceId，开始、结束时间获取节目id列表
	 * @param serviceId 
	 * @param startTime 开始时间
	 * @param endTime 结束时间
	 * @param programIdList 节目id列表，参数作为返回值
	 * @return
	 */
	public native int nativeGetProgramIdListBySid(int serviceId, long startTime, long endTime, ArrayList<Integer> programIdList);

	/**
	 * 通过serviceId及开始时间获取pf
	 * @param serviceId 
	 * @param startTime 开始时间
	 * @param programIdList 节目id列表，参数作为返回值
	 * @return
	 */
	public native int nativeGetPFEvent(int serviceId, long startTime, ArrayList<Integer> programIdList);//TODO

	/**
	 * 通过节目id获取节目信息
	 * @param programId 节目id
	 * @param eventInfo 节目信息bean类
	 * @return
	 */
	public native int nativeGetProgramInfo(int programId, NETEventInfo eventInfo);//TODO

	/**
	 * 通过节目id获取节目详情
	 * @param programId 节目id
	 * @param detailEventInfo 节目详情bean类
	 * @return
	 */
	public native int nativeGetProgramDetail(int programId,NETDetailEventInfo detailEventInfo);

	/**
	 * 通过serviceId获取频道图标地址
	 * @param serviceId
	 * @param url 本地地址 
	 * @return
	 */
	public native String nativeGetTVIcons(int serviceId);

	/**
	 * 设置是否开启网络获取epg
	 * @param isTSMode
	 * @return true  tsMode
	 * 		   false netMode
	 */
	public native int nativeSetEPGSourceMode(boolean isTSMode);
	
	/**
	 * 开始搜索epg
	 * @param param
	 * @param type
	 * @return 0: 开始下载,等待完成通知
	 *		  -1: 主频点参数错误
     *        -2: 主频点锁频失败
     *        -3: 今天已经下载过
	 */
	public native int nativeStartEPGSearch(Transponder param , int type);
	
	/**
	 * 取消搜索epg
	 * @return
	 */
	public native int nativeCancelEPGSearch();
	
}
