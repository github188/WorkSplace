package com.joysee.adtv.logic.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * CA卡观看时间数据封装类。
 * @author wuhao
 */
public class WatchTime implements Parcelable {
    public int startHour;
    public int endHour;
    public int startMin;
    public int endMin;
    public int startSec;
    public int endSec;

    public WatchTime() {}
    
    public WatchTime(int startHour, int startMin, int startSec, int endHour, int endMin, int endSec) {
        this.startHour = startHour;
        this.startMin = startMin;
        this.startSec = startSec;
        this.endHour = endHour;
        this.endMin = endMin;
        this.endSec = endSec;
    }
    public WatchTime(Parcel in){
        this.startHour = in.readInt();
        this.endHour = in.readInt();
        this.startMin = in.readInt();
        this.endMin = in.readInt();
        this.startSec = in.readInt();
        this.endSec = in.readInt();
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.startHour);
        dest.writeInt(this.endHour);
        dest.writeInt(this.startMin);
        dest.writeInt(this.endMin);
        dest.writeInt(this.startSec);
        dest.writeInt(this.endSec);
    }
    
    public static final Parcelable.Creator<WatchTime> CREATOR 
        = new Parcelable.Creator<WatchTime>() {

        public WatchTime createFromParcel(Parcel in) {
            return new WatchTime(in);
        }

        public WatchTime[] newArray(int size) {
            return new WatchTime[size];
        }
    };
    
    public int getStartHour() {
        return startHour;
    }

    public void setStartHour(int startHour) {
        this.startHour = startHour;
    }

    public int getEndHour() {
        return endHour;
    }

    public void setEndHour(int endHour) {
        this.endHour = endHour;
    }

    public int getStartMin() {
        return startMin;
    }

    public void setStartMin(int startMin) {
        this.startMin = startMin;
    }

    public int getEndMin() {
        return endMin;
    }

    public void setEndMin(int endMin) {
        this.endMin = endMin;
    }

    public int getStartSec() {
        return startSec;
    }

    public void setStartSec(int startSec) {
        this.startSec = startSec;
    }

    public int getEndSec() {
        return endSec;
    }

    public void setEndSec(int endSec) {
        this.endSec = endSec;
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
