package com.joysee.adtv.controller;

import android.content.Context;
import android.content.Intent;

import com.joysee.adtv.logic.bean.Transponder;
import com.joysee.adtv.ui.IDvbBaseView;
import com.joysee.adtv.ui.Menu;

public class ActivityController {
	private DvbController mDvbController;

	public void registerView(IDvbBaseView view) {
		mDvbController.registerView(view);
	}

	public ActivityController(Context context) {
		mDvbController = new DvbController(context);
	}

	public void init() {
		mDvbController.init();
	}

//	public void onStart() {
//		mDvbController.onStart();
//	}

//	public void onPause() {
//		mDvbController.onPause();
//	}

	public void play(Intent intent) {
		mDvbController.play(intent);
	}

	public void stop() {
		mDvbController.stop();
	}

	public void uninit() {
		mDvbController.uninit();
	}

	/**
	 * 用户通过按遥控器或键盘的数字键调用到切台时调用的方法
	 * @param channelNum 频道号
	 * @param now true 立刻切台
	 */
	public void switchChannelFromNum(int channelNum,boolean now) {
		mDvbController.switchChannelFromNum(channelNum,now);
	}
	
	/**
     * 用户通过遥控器或键盘输入数字，但还没有按确定切台，或者没到3秒自动切台时，需要显示频道号时调用。
     * @param number
     */
    public void inputNumber(int number){
//    	mDvbController.inputNumber(number);
    }
    
	/**
	 * 回看键按下的时候调用。
	 */
	public void backSee() {
		mDvbController.backSee();
	}
	
	/**
	 * 显示频道信息条，当信息键按下时调用。
	 */
	public void showChannelInfo() {
		mDvbController.showChannelInfo();
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
	 * 切换播放模式，电视或广播。
	 */
	public void switchPlayMode() {
		mDvbController.switchPlayMode();
	}
	
	/** 判断是否有频道 */
	public boolean isChannelEnable() {
		return mDvbController.isChannelEnable();
	}

	public void showSoundTrackSetting() {
		mDvbController.showSoundTrackSetting();
	}

	public void showMainMenu() {
		mDvbController.showMainMenu();
	}

	public void showFavoriteChannel() {
		mDvbController.showFavoriteChannel();
	}

	public void showChannelList() {
		mDvbController.showChannelList();
	}

	public void showProgramGuide() {
		mDvbController.showProgramGuide();
	}
	
//	public void init(){
//		mDvbController.init();
//	}
//	
//	public void uninit(){
//		mDvbController.uninit();
//	}
//	
//	public void playLast(int lastChannelNum){
//		mDvbController.playlast(lastChannelNum);
//	}
//	
//	public void stop(){
//		mDvbController.stop();
//	}
	//TODO:待确认增加回调接口供Launcher使用
	public void setOnMonitorCallBack(){
	}
	
	public void showMenu(Menu menu){
		mDvbController.showMenu(menu);
	}

	public void nextChannel(){
		mDvbController.nextChannel();
	}
	public void previousChannel(){
		mDvbController.previousChannel();
	}
	public void executeNumKey(int keyNum , boolean now){
		mDvbController.executeNumKey(keyNum, now);
	}
	public void actionUp(){
		mDvbController.acionUp();
	}
	public int getChannelNum(){
//		return mDvbController.getCurrentChannel().getLogicChNumber();
		return mDvbController.getChannelNum();
	}
	
	public void downF12() {
		mDvbController.downF12();
	}

	public boolean isBCPlaying() {
		return mDvbController.isBCPlaying();
	}
	
	public int startEPGSearch(Transponder param , int type){
		return mDvbController.startEPGSearch(param, type);
	}
	
	public int cancelEPGSearch(){
		return mDvbController.cancelEPGSearch();
	}
}
