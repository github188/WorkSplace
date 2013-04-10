package com.joysee.adtv.logic.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 频点数据封装类
 */
public class Transponder implements Parcelable{

    private int frequency;//频率

    private int symbolRate;//符号率

    private int modulation;//调制方式

    public Transponder(){
        
    }

    public Transponder(Parcel in){
        this.frequency = in.readInt();
        this.modulation = in.readInt();
        this.symbolRate = in.readInt();
    }

    public Transponder(int Frequency, int SymbolRate, int Modulation){
        this.frequency = Frequency;
        this.symbolRate = SymbolRate;
        this.modulation = Modulation;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.frequency);
        dest.writeInt(this.modulation);
        dest.writeInt(this.symbolRate);
        
    }

    public static final Parcelable.Creator<Transponder> CREATOR
        = new Parcelable.Creator<Transponder>() {
        public Transponder createFromParcel(Parcel in) {
            return new Transponder(in);
        }
        
        public Transponder[] newArray(int size) {
            return new Transponder[size];
        }
    };

    @Override
    public String toString() {
        return "[freq=" + frequency + ", mod=" + modulation + ", symb=" + symbolRate +"]";
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public int getSymbolRate() {
        return symbolRate;
    }

    public void setSymbolRate(int symbolRate) {
        this.symbolRate = symbolRate;
    }

    public int getModulation() {
        return modulation;
    }

    public void setModulation(int modulation) {
        this.modulation = modulation;
    }

    public void readFromParcel(Parcel in) {
        this.frequency = in.readInt();
        this.modulation = in.readInt();
        this.symbolRate = in.readInt();
    }
}
