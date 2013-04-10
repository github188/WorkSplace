package novel.supertv.dvb.jni.struct;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Tuner的状态
 *
 */
public final class stTunerState implements Parcelable{
    public int  LockState = 0;
    public int  Frequency = -1;
    public int  SymbolRate = -1;
    public int  Modulation = -1;
    public int  SignalStrength = 0;     /* 信号强度 */
    public int  SignalQuality = 0;      /* 信号质量 */
    public int  BitErrorRate = 0;       /* 误码率  */
    
    public stTunerState(){
    }
    
    public stTunerState(Parcel in) {
        LockState = in.readInt();
        Frequency = in.readInt();
        SymbolRate = in.readInt();
        Modulation = in.readInt();
        SignalStrength = in.readInt();
        SignalQuality = in.readInt();
        BitErrorRate = in.readInt();
    }
    
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(LockState);
        dest.writeInt(Frequency);
        dest.writeInt(Modulation);
        dest.writeInt(SymbolRate);
        dest.writeInt(SignalStrength);
        dest.writeInt(SignalQuality);
        dest.writeInt(BitErrorRate);
    }
    
    public static final Parcelable.Creator<stTunerState> CREATOR
        = new Parcelable.Creator<stTunerState>() {
        public stTunerState createFromParcel(Parcel in) {
            return new stTunerState(in);
        }
        
        public stTunerState[] newArray(int size) {
            return new stTunerState[size];
        }
    };
    
    @Override
    public String toString() {
        return String.format("[LockState=%d, Frequency=%d, Modulation=%d, SymbolRate=%d, " + 
                "SignalStrength=%d, SignalQuality=%d, BitErrorRate=%d", 
                LockState, Frequency, Modulation, SymbolRate, SignalStrength, SignalQuality, BitErrorRate);
    }

    public int getSignalStrength() {
        return SignalStrength;
    }

    public void setSignalStrength(int signalStrength) {
        SignalStrength = signalStrength;
    }

    public int getSignalQuality() {
        return SignalQuality;
    }

    public void setSignalQuality(int signalQuality) {
        SignalQuality = signalQuality;
    }

    public int getBitErrorRate() {
        return BitErrorRate;
    }

    public void setBitErrorRate(int bitErrorRate) {
        BitErrorRate = bitErrorRate;
    }
    
    

}
