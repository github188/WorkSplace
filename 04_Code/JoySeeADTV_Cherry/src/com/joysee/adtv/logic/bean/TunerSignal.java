package com.joysee.adtv.logic.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 信号状态封装类
 */
public class TunerSignal implements Parcelable{

    /**
     * 信号强度
     */
    private int level; //
    /**
     * 信号质量或者信燥比  它与信号强度决定信号能不能使用（播放或者搜台）
     */
    private int cn;
    
    /**
     * 误码率
     */
    private int errRate;
    
    public TunerSignal(){
    }

    public TunerSignal(int le,int cn,int err) {
        this.level = le;
        this.cn = cn;
        this.errRate = err;
    }

    public TunerSignal(Parcel in) {
        this.level = in.readInt();
        this.cn = in.readInt();
        this.errRate = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.level);
        dest.writeInt(this.cn);
        dest.writeInt(this.errRate);
    }

    public static final Parcelable.Creator<TunerSignal> CREATOR
        = new Parcelable.Creator<TunerSignal>() {
            public TunerSignal createFromParcel(Parcel in) {
                return new TunerSignal(in);
            }
            
            public TunerSignal[] newArray(int size) {
                return new TunerSignal[size];
            }
    };

    @Override
    public String toString() {
        return String.format("[" + 
                "SignalStrength=%d, SignalQuality=%d, BitErrorRate=%d", 
                level, cn, errRate);
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getCN() {
        return cn;
    }

    public void setCN(int cn) {
        this.cn = cn;
    }

    public int getErrRate() {
        return errRate;
    }

    public void setErrRate(int errRate) {
        this.errRate = errRate;
    }
    public void readFromParcel(Parcel in) {
        this.level = in.readInt();
        this.cn = in.readInt();
        this.errRate = in.readInt();
    }
}
