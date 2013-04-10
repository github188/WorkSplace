package novel.supertv.dvb.jni.struct;

import android.os.Parcel;
import android.os.Parcelable;

public class tagDVBTS implements Parcelable{

    public int ts_id;

    public int net_id;

    public TuningParam tuning_param;

    public tagDVBTS(){
        
    }

    public tagDVBTS(int tid,int nid,TuningParam tp){
        this.ts_id = tid;
        this.net_id = nid;
        this.tuning_param = tp;
    }

    public tagDVBTS(Parcel in){
        this.ts_id = in.readInt();
        this.net_id = in.readInt();
        this.tuning_param = (TuningParam)in.readParcelable(TuningParam.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.net_id);
        dest.writeInt(this.ts_id);
        dest.writeParcelable(this.tuning_param, 0);
    }

    public static final Parcelable.Creator<tagDVBTS> CREATOR
        = new Parcelable.Creator<tagDVBTS>() {
        public tagDVBTS createFromParcel(Parcel in) {
            return new tagDVBTS(in);
        }
        
        public tagDVBTS[] newArray(int size) {
            return new tagDVBTS[size];
        }
        };

    public int getTs_id() {
        return ts_id;
    }

    public void setTs_id(int ts_id) {
        this.ts_id = ts_id;
    }

    public int getNet_id() {
        return net_id;
    }

    public void setNet_id(int net_id) {
        this.net_id = net_id;
    }

    public TuningParam getTuning_param() {
        return tuning_param;
    }

    public void setTuning_param(TuningParam tuning_param) {
        this.tuning_param = tuning_param;
    }

}
