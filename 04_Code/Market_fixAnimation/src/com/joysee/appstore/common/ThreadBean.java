package com.joysee.appstore.common;

import android.os.Parcel;
import android.os.Parcelable;

public class ThreadBean implements Parcelable {
	
	private int id;
	
	private int taskId;
	
	private int threadId;
	
	private int position;
	
	private int downLength;
	
	public ThreadBean(){
		
	}
	
	public ThreadBean(Parcel in){
		setId(in.readInt());
		setTaskId(in.readInt());
		setThreadId(in.readInt());
		setPosition(in.readInt());
		setDownLength(in.readInt());
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getTaskId() {
		return taskId;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

	public int getThreadId() {
		return threadId;
	}

	public void setThreadId(int threadId) {
		this.threadId = threadId;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public int getDownLength() {
		return downLength;
	}

	public void setDownLength(int downLength) {
		this.downLength = downLength;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeInt(taskId);
		dest.writeInt(threadId);
		dest.writeInt(position);
		dest.writeInt(downLength);
	}
	
	 public static final Parcelable.Creator<ThreadBean> CREATOR
     = new Parcelable.Creator<ThreadBean>() {
         public ThreadBean createFromParcel(Parcel in) {
             return new ThreadBean(in);
         }

         public ThreadBean[] newArray(int size) {
             return new ThreadBean[size];
         }
     };

}
