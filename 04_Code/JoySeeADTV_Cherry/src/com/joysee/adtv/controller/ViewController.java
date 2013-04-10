package com.joysee.adtv.controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Intent;

import com.joysee.adtv.logic.bean.DvbService;
import com.joysee.adtv.logic.bean.EpgEvent;
import com.joysee.adtv.logic.bean.MiniEpgNotify;
import com.joysee.adtv.logic.bean.NETEventInfo;
import com.joysee.adtv.logic.bean.ProgramType;
import com.joysee.adtv.logic.bean.Transponder;


public class ViewController {
	private DvbController mDvbController;
	public ViewController(DvbController dvbController){
		this.mDvbController = dvbController;
	}
	
	/**
	 * 发送指定键值的模拟按键
	 * @param key 	指定的键值
	 */
	public void doInjectKeyEvent(final int key) {
		mDvbController.doInjectKeyEvent(key);
	}
	
	/**
	 * 改变声量的接口
	 * 
	 * @param value
	 */
	public void changeVolume(int value) {
		mDvbController.changeVolume(value);
	}
	
	/**
	 * 判断是否在播放电视
	 * @return
	 */
	public boolean isTVService(){
		return mDvbController.isTVService();
	}
	
	/**
	 * 获取显示模式
	 * @return
	 */
	public int getDisplayMode(){
		return mDvbController.getDisplayMode();
	}
	
	/**
	 * 设置显示模式
	 * @param mode
	 */
	public void setDisplayMode(int mode) {
		mDvbController.setDisplayMode(mode);
	}
	
	/**
	 * 获取声道
	 * @return
	 */
	public int getSoundTrack(){
		return mDvbController.getSoundTrack();
	}
	
	/**
	 * 获取当前节目的伴音数量
	 * @return
	 */
	public int getCurrentAudioIndexSum(){
		return mDvbController.getCurrentAudioIndexSum();
	}
	
	/**
	 * 设置伴音
	 * @param position
	 */
	public void setAudioIndex(int position) {
		mDvbController.setAudioIndex(position);
	}
	
	/**
	 * 获取频道号
	 * @return
	 */
	public int getChannelNum(){
		return mDvbController.getChannelNum();
	}
	
	/**
	 * 获取当前伴音
	 * @return
	 */
	public int getAudioIndex() {
		return mDvbController.getAudioIndex();
	}
	
	/**
	 * 切换播放模式，电视或广播。
	 */
	public void switchPlayMode() {
		mDvbController.switchPlayMode();
	}
	
	/**
	 * 当前是否播放的是广播
	 * @return
	 */
	public boolean isBCService(){
		return mDvbController.isBCService();
	}
	
	/**
	 * 获取当前播放的角标
	 * @return
	 */
	public int getCurrentIndexInChannelWindow(){
		return mDvbController.getCurrentIndexInChannelWindow();
	}
	
	/**
	 * 当设置喜爱频道完成时，则调用这个方法，刷新频道信息条。
	 */
	public void refreshChannelInfo() {
		mDvbController.refreshChannelInfo();
	}
	
    /**
     * 设置声道
     * @param position
     */
    public void setSoundTrack(int position){
    	mDvbController.setSoundTrack(position);
	}
    
    /**
     * 获取当前播放的节目
     * @return DvbService
     */
    public DvbService getCurrentChannel(){
		return mDvbController.getCurrentChannel();
	}
    
    /**
	 * 加入喜爱频道
	 * @param isAdd true添加 false删除
	 */
	public void setChannelFavorite(boolean isAdd){
		mDvbController.setChannelFavorite(isAdd);
	}
	
	/**
	 * 用户通过按遥控器或键盘的数字键调用到切台时调用的方法
	 * 
	 * @param channelNum
	 *            频道号
	 */
	public void switchChannelFromNum(int channelNum) {
		mDvbController.switchChannelFromNum(channelNum);
	}
	/**
	 * 频道列表切台时使用
	 * 
	 * @param index	底层角标
	 *        
	 */
	public void switchChannelFromIndex(int serviceType,int index) {
		mDvbController.switchChannelFromIndex(serviceType,index);
	}
	
	/** 判断是否有频道 */
	public boolean isChannelEnable() {
		return mDvbController.isChannelEnable();
	}
	
	/**
	 * 获取当前频道数量 主菜单使用
	 */
	public int getTotalChannelSize(){
		return mDvbController.getTotalChannelSize();
	}
	/**
	 * 通过List角标获取Service
	 */
	public DvbService getChannelByListIndex(int index){
		return mDvbController.getChannelByListIndex(index);
	}
	
	/**
	 * 通过本地角标获取Service
	 */
	public DvbService getChannelByNativeIndex(int index){
		return mDvbController.getChannelByNativeIndex(index);
	}
	
	/**
	 * 获取所有喜爱频道的底层角标
	 * @return
	 */
	public ArrayList<Integer> getFavouriteIndex() {
		return mDvbController.getFavouriteIndex();
	}
	
	/**
	 * 通过底层角标获取Service，喜爱频道使用
	 * @param channelIndex
	 * @return
	 */
	public DvbService getChannelByChannelIndex(int channelIndex) {
		return mDvbController.getChannelByChannelIndex(channelIndex);
	}
	
	public MiniEpgNotify getPfFromEPG(DvbService service){
		return mDvbController.getPfByEpg(service);
	}
	
	public int getPf(int serviceId ,MiniEpgNotify pf){
		return mDvbController.getPf(serviceId,pf);
	}

	public void showProgramGuide() {
		mDvbController.showProgramGuide();
	}

	public void getEpgDataByDuration(ArrayList<EpgEvent> mEpgEvents, long startTime,long endTime) {
		mDvbController.getEpgDataByDuration(mEpgEvents,startTime,endTime);
	}

	public void showProgramReserve() {
		mDvbController.showProgramReserve();
	}

	public int getLastTunerParam() {
		return mDvbController.getLastTunerParam();
	}

	public int getLastCAParam() {
		return mDvbController.getLastCAParam();
	}
	/**
	 * 获取所有频道的层维护角标
	 * @param mTvChannelList
	 * @param mBcChannelList
	 */
	public void getAllChannelIndex(ArrayList<Integer> mTvChannelList,
			ArrayList<Integer> mBcChannelList) {
		mDvbController.getAllChannelIndex(mTvChannelList,mBcChannelList);
	}
	
	public void removeChannelFavorite(int channelNumber,int type){
		mDvbController.removeChannelFavorite(channelNumber, type);
	}
	
	public void addChannelFavorite(int channelNumber,int type){
		mDvbController.addChannelFavorite(channelNumber, type);
	}
	
	public int getFavoriteCount(){
		return mDvbController.getFavoriteCount();
	}
	public int getCurrentPosition(){
	    return mDvbController.getCurrentPosition();
	}
	public int getCurrentPlayType(){
		return mDvbController.getmCurrentPlayType();
	}
	public int getCurrentPosition1(){
        return mDvbController.getCurrentPosition1();
    }
	/**
	 * 获取当前节目信息 频道列表使用
	 * @param serviceId
	 * @return
	 */
	public NETEventInfo getCurrentProgramInfo(int serviceId) {
		return mDvbController.getCurrentProgramInfo(serviceId);
	}
	
	public DvbController getDvbController(){
		return mDvbController;
	}
	
	public int getAllChannelCount(){
		return mDvbController.getAllChannelCount();
	}
	public ArrayList<Integer> getProgramIdListBySid(int serviceId, long startTime, long endTime){
	    return mDvbController.getProgramIdListBySid(serviceId, startTime, endTime);
	}
	
	public NETEventInfo getProgramInfo(int programId){
	    return mDvbController.getProgramInfo(programId);
	}
	
	public String getTVIcons(int serviceId){
        return mDvbController.getTVIcons(serviceId);
    }

	public void showLiveGuide() {
		mDvbController.showLiveGuide();
	}
    /**
     * 获取直播节目指南一级分类id和名称
     * @param id 0xff 返回一级分类 ID 和分类名称;否则返回二级分类 ID 和分类名称
     * @return
     */
    public ArrayList<ProgramType> getProgramTypes(int id ) {
        ArrayList<ProgramType> programTypeList = new ArrayList<ProgramType>();
        int back = mDvbController.getProgramTypes(id, programTypeList);
        System.out.println(" getProgramTypes back = " + back);
        return programTypeList;
    }
    /**
     * 获取正在播放或者即将播放的节目详情 List
     * @param programId
     * @param startTime
     * @param endTime
     * @return
     */
    public ArrayList<NETEventInfo> getProgramList(int programId, long startTime, long endTime) {
        ArrayList<Integer> programIdList = new ArrayList<Integer>();
        ArrayList<NETEventInfo> programList = new ArrayList<NETEventInfo>();
        int back = mDvbController.getProgramIdListByType(programId, startTime, endTime,
                programIdList);
        System.out.println(" getProgramList back = " + back + " programIdList.size() = "
                + programIdList.size());
        if (programIdList.size() > 0) {
            for (Integer id : programIdList) {
                programList.add(mDvbController.getProgramInfo_LiveGuide(id.intValue()));
            }
        }
        return programList;
    }
	
	public long getUtcTime(){
		return mDvbController.getUtcTime();
	}

	public String getChannelIconPath(int serviceId) {
		return mDvbController.getChannelIconPath(serviceId);
	}
	
	public void showMainMenu(){
	    mDvbController.showMainMenu();
	}

	public void switchChannelFromNum(int type, int num) {
		mDvbController.switchChannelFromNum(type,num);
	}
	
	public void play(Intent intent) {
		mDvbController.play(intent);
	}
	
	public int startEPGSearch(Transponder param , int type){
		return mDvbController.startEPGSearch(param, type);
	}
	
	public int cancelEPGSearch(){
		return mDvbController.cancelEPGSearch();
	}

	public void exitLiveGuide() {
		 mDvbController.exitLiveGuide();
	}

	public void exitEpgGuide() {
		mDvbController.exitEpgGuide();
	}
}
