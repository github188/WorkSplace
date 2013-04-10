package novel.supertv.dvb.jni.struct;

import android.os.Parcel;
import android.os.Parcelable;

public class tagMiniEPGNotify implements Parcelable {

    String CurrentEventName;     // 当前节目名称
    long CurrentEventStartTime; // 当前节目开始时间
    long CurrentEventEndTime;   // 当前节目结束时间
    String NextEventName;        // 后继节目名称
    long NextEventStartTime;    // 后继节目开始时间
    long NextEventEndTime;      // 后继节目结束时间

    public tagMiniEPGNotify(){
        
    }

    public tagMiniEPGNotify(String CurrentEventName,long CurrentEventStartTime,long CurrentEventEndTime,
            String NextEventName,long NextEventStartTime,long NextEventEndTime){
        
        this.CurrentEventName = CurrentEventName;
        this.CurrentEventStartTime = CurrentEventStartTime;
        this.CurrentEventEndTime = CurrentEventEndTime;
        this.NextEventName = NextEventName;
        this.NextEventStartTime = NextEventStartTime;
        this.NextEventEndTime = NextEventEndTime;
        
    }

    public tagMiniEPGNotify(Parcel in){
        
        this.CurrentEventName      = in.readString();
        this.CurrentEventStartTime = in.readLong();
        this.CurrentEventEndTime   = in.readLong();
        this.NextEventName         = in.readString();
        this.NextEventStartTime    = in.readLong();
        this.NextEventEndTime      = in.readLong();
        
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.CurrentEventName);
        dest.writeLong(this.CurrentEventStartTime);
        dest.writeLong(this.CurrentEventEndTime);
        dest.writeString(this.NextEventName);
        dest.writeLong(this.NextEventStartTime);
        dest.writeLong(this.NextEventEndTime);
        
    }

    public static final Parcelable.Creator<tagMiniEPGNotify> CREATOR
        = new Parcelable.Creator<tagMiniEPGNotify>() {
        public tagMiniEPGNotify createFromParcel(Parcel in) {
            return new tagMiniEPGNotify(in);
        }
        
        public tagMiniEPGNotify[] newArray(int size) {
            return new tagMiniEPGNotify[size];
        }
    };

    @Override
    public String toString() {
        return "[" + "CurrentEventName = "+ CurrentEventName + "NextEventName = " +NextEventName +"]";
    }

    public String getCurrentEventName() {
        return CurrentEventName;
    }

    public void setCurrentEventName(String currentEventName) {
        CurrentEventName = currentEventName;
    }

    public long getCurrentEventStartTime() {
        return CurrentEventStartTime;
    }

    public void setCurrentEventStartTime(long currentEventStartTime) {
        CurrentEventStartTime = currentEventStartTime;
    }

    public long getCurrentEventEndTime() {
        return CurrentEventEndTime;
    }

    public void setCurrentEventEndTime(long currentEventEndTime) {
        CurrentEventEndTime = currentEventEndTime;
    }

    public String getNextEventName() {
        return NextEventName;
    }

    public void setNextEventName(String nextEventName) {
        NextEventName = nextEventName;
    }

    public long getNextEventStartTime() {
        return NextEventStartTime;
    }

    public void setNextEventStartTime(long nextEventStartTime) {
        NextEventStartTime = nextEventStartTime;
    }

    public long getNextEventEndTime() {
        return NextEventEndTime;
    }

    public void setNextEventEndTime(long nextEventEndTime) {
        NextEventEndTime = nextEventEndTime;
    }

}
