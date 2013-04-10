package novel.supertv.dvb.jni.struct;

import android.os.Parcel;
import android.os.Parcelable;

public class TuningParam implements Parcelable{

    private int Frequency;

    private int SymbolRate;

    private int Modulation;

    public TuningParam(){
        
    }

    public TuningParam(Parcel in){
        Frequency = in.readInt();
        Modulation = in.readInt();
        SymbolRate = in.readInt();
    }

    public TuningParam(int Frequency, int SymbolRate, int Modulation){
        this.Frequency = Frequency;
        this.Modulation = Modulation;
        this.SymbolRate = SymbolRate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(Frequency);
        dest.writeInt(Modulation);
        dest.writeInt(SymbolRate);
        
    }

    public static final Parcelable.Creator<TuningParam> CREATOR
        = new Parcelable.Creator<TuningParam>() {
        public TuningParam createFromParcel(Parcel in) {
            return new TuningParam(in);
        }
        
        public TuningParam[] newArray(int size) {
            return new TuningParam[size];
        }
    };

    @Override
    public String toString() {
        return "[freq=" + Frequency + ", mod=" + Modulation + ", symb=" + SymbolRate +"]";
    }

    public int getFrequency() {
        return Frequency;
    }

    public void setFrequency(int frequency) {
        Frequency = frequency;
    }

    public int getSymbolRate() {
        return SymbolRate;
    }

    public void setSymbolRate(int symbolRate) {
        SymbolRate = symbolRate;
    }

    public int getModulation() {
        return Modulation;
    }

    public void setModulation(int modulation) {
        Modulation = modulation;
    }
}
