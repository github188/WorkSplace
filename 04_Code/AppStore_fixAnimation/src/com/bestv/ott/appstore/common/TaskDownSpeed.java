package com.bestv.ott.appstore.common;

import com.bestv.ott.appstore.utils.AppLog;

public class TaskDownSpeed {
	
	private String TAG="com.bestv.ott.appstore.common.TaskDownSpeed";
	
	private int taskID;
	private int downSize;
	private int sumSize;
	private long speed;
	private long time;
	
	public int getTaskID() {
		return taskID;
	}
	public void setTaskID(int taskID) {
		this.taskID = taskID;
	}
	public int getDownSize() {
		return downSize;
	}
	public void setDownSize(int downSize) {
		this.downSize = downSize;
		time=System.currentTimeMillis();
	}
	
	public void setOffSet(int offset){
		this.downSize=this.downSize+offset;
//		AppLog.log_D(TAG, "--------**********************-----------time="+time+";setTime="+setTime+";offset="+offset);
//		if((setTime-time)<=0){
//			return;
//		}
//		speed=(offset/1024)*1000/(setTime-time);
//		time=setTime;
	}
	
	public int getSumSize() {
		return sumSize;
	}
	public void setSumSize(int sumSize) {
		this.sumSize = sumSize;
	}
	public long getSpeed() {
		long setTime=System.currentTimeMillis();
		speed=(downSize/1024)*1000/(setTime-time);
		time=setTime;
		downSize=0;
		return speed;
	}
	public void setSpeed(int speed) {
		this.speed = speed;
	}

	
}
