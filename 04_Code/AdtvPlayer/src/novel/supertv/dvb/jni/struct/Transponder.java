package novel.supertv.dvb.jni.struct;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 频点信息
 * 附加的后四个参数暂时不用
 *
 */
public class Transponder implements Parcelable{
    
    private int TransponderId;
    public int Frequency;
    private int Modulation;
    private int SymbolRate;
    private int nPATVersion;
    private int nSDTVersion;
    private int nCATVersion;
    private int nNITVersion;
    
    public Transponder(){

    }
    
    public Transponder(Parcel in) {
        TransponderId = in.readInt();
        Frequency = in.readInt();
        Modulation = in.readInt();
        SymbolRate = in.readInt();
    }
    
    public Transponder(int Frequency, int SymbolRate, int Modulation){
        this.Frequency = Frequency;
        this.Modulation = Modulation;
        this.SymbolRate = SymbolRate;
    }
    
    public Transponder(int TransponderId, int Frequency, int SymbolRate, int Modulation){
        this.TransponderId = TransponderId;
        this.Frequency = Frequency;
        this.Modulation = Modulation;
        this.SymbolRate = SymbolRate;
    }
    
    public int getTransponderId() {
        return TransponderId;
    }

    public void setTransponderId(int transponderId) {
        TransponderId = transponderId;
    }

    public void setFrequency(int frequency) {
        Frequency = frequency;
    }
    public int getFrequency() {
        return Frequency;
    }
    public void setModulation(int modulation) {
        Modulation = modulation;
    }
    public int getModulation() {
        return Modulation;
    }
    
    public void setSymbolRate(int symbolRate) {
        SymbolRate = symbolRate;
    }
    public int getSymbolRate() {
        return SymbolRate;
    }
    
    public void setPATVersion(int PATVersion) {
        nPATVersion = PATVersion;
    }
    public int getPATVersion() {
        return nPATVersion;
    }
    
    public void setSDTVersion(int SDTVersion) {
        nSDTVersion = SDTVersion;
    }
    public int getSDTVersion() {
        return nSDTVersion;
    }
    
    public void setCATVersion(int CATVersion) {
        nCATVersion = CATVersion;
    }
    public int getCATVersion() {
        return nCATVersion;
    }
    
    public void setNITVersion(int NITVersion) {
        nNITVersion = NITVersion;
    }
    public int getNITVersion() {
        return nNITVersion;
    }
    
    public int describeContents() {
        return 0;
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
    
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(TransponderId);
        dest.writeInt(Frequency);
        dest.writeInt(Modulation);
        dest.writeInt(SymbolRate);
    }
    
    @Override
    public boolean equals(Object o) {
           if (!(o instanceof Transponder))
               return false;
           Transponder tp = (Transponder) o;
           return tp.Frequency == Frequency
                  && tp.Modulation == Modulation
                  && tp.SymbolRate == SymbolRate;
    } 
    
    @Override
    public int hashCode() {
           int result = 17;
           result = 37 * result + Frequency;
           result = 37 * result + Modulation;
           result = 37 * result + SymbolRate;
           return result;
    } 
    
    @Override
    public String toString() {
        return "[freq=" + Frequency + ", mod=" + Modulation + ", symb=" + SymbolRate 
                + ", PATver=" + nPATVersion + ", SDTver=" + nSDTVersion + ", CATver=" + nCATVersion 
                + ", NITver=" + nNITVersion + "]";
    }
}
