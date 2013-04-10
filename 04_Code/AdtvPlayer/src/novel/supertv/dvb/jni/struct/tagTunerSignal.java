package novel.supertv.dvb.jni.struct;

import android.os.Parcel;
import android.os.Parcelable;

public class tagTunerSignal implements Parcelable{

    private int Level;
    private int CN;
    private int ErrRate;

    public tagTunerSignal(){
    }

    public tagTunerSignal(int le,int cn,int err) {
        Level = le;
        CN = cn;
        ErrRate = err;
    }

    public tagTunerSignal(Parcel in) {
        Level = in.readInt();
        CN = in.readInt();
        ErrRate = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(Level);
        dest.writeInt(CN);
        dest.writeInt(ErrRate);
    }

    public static final Parcelable.Creator<tagTunerSignal> CREATOR
        = new Parcelable.Creator<tagTunerSignal>() {
            public tagTunerSignal createFromParcel(Parcel in) {
                return new tagTunerSignal(in);
            }
            
            public tagTunerSignal[] newArray(int size) {
                return new tagTunerSignal[size];
            }
    };

    @Override
    public String toString() {
        return String.format("[" + 
                "SignalStrength=%d, SignalQuality=%d, BitErrorRate=%d", 
                Level, CN, ErrRate);
    }

    public int getLevel() {
        return Level;
    }

    public void setLevel(int level) {
        Level = level;
    }

    public int getCN() {
        return CN;
    }

    public void setCN(int cN) {
        CN = cN;
    }

    public int getErrRate() {
        return ErrRate;
    }

    public void setErrRate(int errRate) {
        ErrRate = errRate;
    }
}
