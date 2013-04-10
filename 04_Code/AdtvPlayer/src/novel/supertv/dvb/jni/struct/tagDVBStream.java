package novel.supertv.dvb.jni.struct;

import android.os.Parcel;
import android.os.Parcelable;

public class tagDVBStream implements Parcelable{

    public int stream_type;

    public int stream_pid;

    public int ecm_pid;

    // 最大64byte
    public String name;

    public tagDVBStream(){
        
    }

    public tagDVBStream(int type,int spid,int epid,String name){
        this.stream_type = type;
        this.stream_pid = spid;
        this.ecm_pid = epid;
        this.name = name;
    }

    public tagDVBStream(Parcel in){
        this.stream_type = in.readInt();
        this.stream_pid = in.readInt();
        this.ecm_pid = in.readInt();
        this.name = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.stream_type);
        dest.writeInt(this.stream_pid);
        dest.writeInt(this.ecm_pid);
        dest.writeString(this.name);
    }

    public static final Parcelable.Creator<tagDVBStream> CREATOR
        = new Parcelable.Creator<tagDVBStream>() {
        public tagDVBStream createFromParcel(Parcel in) {
            return new tagDVBStream(in);
        }
        
        public tagDVBStream[] newArray(int size) {
            return new tagDVBStream[size];
        }
        };

    public int getStream_type() {
        return stream_type;
    }

    public void setStream_type(int stream_type) {
        this.stream_type = stream_type;
    }

    public int getStream_pid() {
        return stream_pid;
    }

    public void setStream_pid(int stream_pid) {
        this.stream_pid = stream_pid;
    }

    public int getEcm_pid() {
        return ecm_pid;
    }

    public void setEcm_pid(int ecm_pid) {
        this.ecm_pid = ecm_pid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
