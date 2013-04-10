package novel.supertv.dvb.jni.struct;

import android.os.Parcel;
import android.os.Parcelable;

public class tagProgramEpg implements Parcelable {
    public int serviceId;
    public int presentVersion;
    public int followingVersion;
    public int eventVersion;
    public tagEpgEvent presentEpgEvent;
    public tagEpgEvent followingEpgEvent;
    public tagEpgEvent[] events;
    
    public tagEpgEvent[] getEvents() {
        return events;
    }

    public void setEvents(tagEpgEvent[] events) {
        this.events = events;
    }

    public tagProgramEpg(){
        
    }
    
    public tagProgramEpg(Parcel in){
        this.serviceId = in.readInt();
        this.presentVersion = in.readInt();
        this.followingVersion = in.readInt();
        this.eventVersion = in.readInt();
        this.presentEpgEvent = in.readParcelable(tagProgramEpg.class.getClassLoader());
        this.followingEpgEvent = in.readParcelable(tagProgramEpg.class.getClassLoader());
    }
    
    public static final Parcelable.Creator<tagProgramEpg> CREATOR = new Creator<tagProgramEpg>() {
        
        @Override
        public tagProgramEpg[] newArray(int size) {
            return new tagProgramEpg[size];
        }
        
        @Override
        public tagProgramEpg createFromParcel(Parcel in) {
            return new tagProgramEpg(in);
        }
    };
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.serviceId);
        dest.writeInt(presentVersion);
        dest.writeInt(followingVersion);
        dest.writeInt(eventVersion);
        dest.writeParcelable(this.presentEpgEvent, 0);
        dest.writeParcelable(this.followingEpgEvent, 0);
    }
    
    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public int getPresentVersion() {
        return presentVersion;
    }

    public void setPresentVersion(int presentVersion) {
        this.presentVersion = presentVersion;
    }

    public int getFollowingVersion() {
        return followingVersion;
    }

    public void setFollowingVersion(int followingVersion) {
        this.followingVersion = followingVersion;
    }

    public int getEventVersion() {
        return eventVersion;
    }

    public void setEventVersion(int eventVersion) {
        this.eventVersion = eventVersion;
    }

    public tagEpgEvent getPresentEpgEvent() {
        return presentEpgEvent;
    }

    public void setPresentEpgEvent(tagEpgEvent presentEpgEvent) {
        this.presentEpgEvent = presentEpgEvent;
    }

    public tagEpgEvent getFollowingEpgEvent() {
        return followingEpgEvent;
    }

    public void setFollowingEpgEvent(tagEpgEvent followingEpgEvent) {
        this.followingEpgEvent = followingEpgEvent;
    }

}