
package novel.supertv.dvb.jni.struct;

import android.os.Parcel;
import android.os.Parcelable;

/** Epg事件信息结构体 */
public class tagEpgEvent implements Parcelable {
    public int id;
    public int serviceId;
    public String name;
    public long start_time;
    public long end_time;
    public String description;

    public tagEpgEvent() {

    }

    public tagEpgEvent(int id, String programName, long startTime, long endTime) {
        this.id = id;// 返上来的不是serviceId,是eid.
        this.name = programName;
        this.start_time = startTime;
        this.end_time = endTime;
    }

    public tagEpgEvent(Parcel in) {
        this.id = in.readInt();
        this.name = in.readString();
        this.start_time = in.readLong();
        this.end_time = in.readLong();
        this.description = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.name);
        dest.writeLong(this.start_time);
        dest.writeLong(this.end_time);
        dest.writeString(this.description);
    }

    public static final Parcelable.Creator<tagEpgEvent> CREATOR = new Parcelable.Creator<tagEpgEvent>() {

        public tagEpgEvent createFromParcel(Parcel in) {
            return new tagEpgEvent(in);
        }

        public tagEpgEvent[] newArray(int size) {
            return new tagEpgEvent[size];
        }
    };

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProgramName() {
        return name;
    }

    public void setProgramName(String programName) {
        this.name = programName;
    }

    public long getStartTime() {
        return start_time;
    }

    public void setStartTime(long startTime) {
        this.start_time = startTime;
    }

    public long getEndTime() {
        return end_time;
    }

    public void setEndTime(long endTime) {
        this.end_time = endTime;
    }

    public String getProgramDescription() {
        return description;
    }

    public void setProgramDescription(String programDescription) {
        this.description = programDescription;
    }

}
