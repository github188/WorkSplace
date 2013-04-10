package com.joysee.appstore.common;

import com.joysee.appstore.utils.AppLog;

public class TaskDownSpeed {
	
	private String TAG="com.joysee.appstore.common.TaskDownSpeed";
	
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
	}
	
	public int getSumSize() {
		return sumSize;
	}
	public void setSumSize(int sumSize) {
		this.sumSize = sumSize;
	}
	public long getSpeed() {
		long setTime=System.currentTimeMillis();
		speed=((long)downSize/1024)*1000/(setTime-time);
		time=setTime;
		downSize=0;
		return speed;
	}
	public void setSpeed(int speed) {
		this.speed = speed;
	}

	
}
