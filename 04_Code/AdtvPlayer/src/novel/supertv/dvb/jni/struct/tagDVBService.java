package novel.supertv.dvb.jni.struct;

import android.os.Parcel;
import android.os.Parcelable;

public class tagDVBService implements Parcelable{

    public int sid;

    public int channel_number;

    public String name;

    public int service_type;

    public int reserved1;

    public int category;

    public int reserved2;

    public int pcr_pid;

    public int reserved3;

    // PMT PID
    public int pmt_id;
    
    public int emm_pid;

    public int volume_ratio;

    public int reserved4;

    public int volume_reserve;
    
    public int audio_channel_set;

    public int audio_format;

    public int audio_index;

    public tagDVBStream video_stream;

    public tagDVBTS ts;

    public int video_stream_type;

    public int video_stream_pid;

    public int video_ecm_pid;

    public int ts_id;

    public int net_id;

    public int audio_stream_type;

    public int audio_stream_pid;

    public int audio_ecm_pid;

    public int audio_stream_type1;

    public int audio_stream_pid1;

    public int audio_ecm_pid1;
    
    public int audio_stream_type2;

    public int audio_stream_pid2;

    public int audio_ecm_pid2;
    
    public String audio_stream_name;
    public String audio_stream_name1;
    public String audio_stream_name2;

    private int Frequency;

    private int SymbolRate;

    private int Modulation;

    /*
     * 多语言，最多支持3种
     */
    public tagDVBStream[] audio_stream = new tagDVBStream[3];

    public tagDVBService(){
        
    }

    public tagDVBService(int sid,int chNumber,String name){
        this.sid = sid;
        this.channel_number = chNumber;
        this.name = name;
    }

    public tagDVBService(Parcel in){
        
        this.audio_channel_set = in.readInt();
        this.audio_format = in.readInt();
        this.audio_index = in.readInt();
        this.category = in.readInt();
        this.channel_number = in.readInt();
//        this.name = in.readCharArray(val);
        this.pcr_pid = in.readInt();
        this.emm_pid = in.readInt();
        this.pmt_id = in.readInt();
        this.emm_pid = in.readInt();
        this.reserved1 = in.readInt();
        this.reserved2 = in.readInt();
        this.reserved3 = in.readInt();
        this.reserved4 = in.readInt();
        
        this.service_type = in.readInt();
        this.sid = in.readInt();
        this.volume_ratio = in.readInt();
        this.volume_reserve = in.readInt();
        
        this.video_stream_type = in.readInt();

        this.video_stream_pid = in.readInt();

        this.video_ecm_pid = in.readInt();

        this.ts_id = in.readInt();

        this.net_id = in.readInt();

        this.audio_stream_type = in.readInt();

        this.audio_stream_pid = in.readInt();

        this.audio_ecm_pid = in.readInt();
        
        this.audio_stream_type1 = in.readInt();

        this.audio_stream_pid1 = in.readInt();

        this.audio_ecm_pid1 = in.readInt();
        
        this.audio_stream_type2 = in.readInt();

        this.audio_stream_pid2 = in.readInt();

        this.audio_ecm_pid2 = in.readInt();
        
        this.audio_stream_name = in.readString();
        this.audio_stream_name1 = in.readString();
        this.audio_stream_name2 = in.readString();
        
        this.Frequency = in.readInt();
        this.Modulation = in.readInt();
        this.SymbolRate = in.readInt();
        
        this.video_stream = (tagDVBStream)in.readParcelable(tagDVBStream.class.getClassLoader());
        this.ts = (tagDVBTS)in.readParcelable(tagDVBTS.class.getClassLoader());
        this.audio_stream = (tagDVBStream[]) in.readParcelableArray(tagDVBStream.class.getClassLoader());
    }

    public static final Parcelable.Creator<tagDVBService> CREATOR
        = new Parcelable.Creator<tagDVBService>() {
        public tagDVBService createFromParcel(Parcel in) {
            return new tagDVBService(in);
        }
        
        public tagDVBService[] newArray(int size) {
            return new tagDVBService[size];
        }
        };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.audio_channel_set);
        dest.writeInt(this.audio_format);
        dest.writeInt(this.audio_index);
        dest.writeInt(this.category);
        dest.writeInt(this.channel_number);
        dest.writeInt(this.pcr_pid);
        dest.writeInt(this.emm_pid);
        dest.writeInt(this.pmt_id);
        dest.writeInt(this.emm_pid);
        dest.writeInt(this.reserved1);
        dest.writeInt(this.reserved2);
        dest.writeInt(this.reserved3);
        dest.writeInt(this.reserved4);
        dest.writeInt(this.service_type);
        dest.writeInt(this.sid);
        dest.writeInt(this.volume_ratio);
        dest.writeInt(this.volume_reserve);
        dest.writeString(this.name);
        
        dest.writeInt(this.video_stream_type);
        dest.writeInt(this.video_stream_pid);
        dest.writeInt(this.video_ecm_pid);
        dest.writeInt(this.ts_id);
        dest.writeInt(this.net_id);
        dest.writeInt(this.audio_stream_type);
        dest.writeInt(this.audio_stream_pid);
        dest.writeInt(this.audio_ecm_pid);
        dest.writeInt(this.audio_stream_type1);
        dest.writeInt(this.audio_stream_pid1);
        dest.writeInt(this.audio_ecm_pid1);
        dest.writeInt(this.audio_stream_type2);
        dest.writeInt(this.audio_stream_pid2);
        dest.writeInt(this.audio_ecm_pid2);
        
        dest.writeString(this.audio_stream_name);
        dest.writeString(this.audio_stream_name1);
        dest.writeString(this.audio_stream_name2);
        
        dest.writeInt(this.Frequency);
        dest.writeInt(this.SymbolRate);
        dest.writeInt(this.Modulation);
        
        dest.writeParcelable(this.video_stream, 0);
        dest.writeParcelable(this.ts, 0);
        dest.writeParcelableArray(this.audio_stream, 0);
        
        dest.writeInt(this.Frequency);
        dest.writeInt(this.Modulation);
        dest.writeInt(this.SymbolRate);
    }

    public int getSid() {
        return sid;
    }

    public void setSid(int sid) {
        this.sid = sid;
    }

    public int getChannel_number() {
        return channel_number;
    }

    public void setChannel_number(int channel_number) {
        this.channel_number = channel_number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getService_type() {
        return service_type;
    }

    public void setService_type(int service_type) {
        this.service_type = service_type;
    }

    public int getReserved1() {
        return reserved1;
    }

    public void setReserved1(int reserved1) {
        this.reserved1 = reserved1;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public int getReserved2() {
        return reserved2;
    }

    public void setReserved2(int reserved2) {
        this.reserved2 = reserved2;
    }

    public int getPcr_pid() {
        return pcr_pid;
    }

    public void setPcr_pid(int pcr_pid) {
        this.pcr_pid = pcr_pid;
    }

    public int getReserved3() {
        return reserved3;
    }

    public void setReserved3(int reserved3) {
        this.reserved3 = reserved3;
    }

    public int getEmm_pid() {
        return emm_pid;
    }

    public void setEmm_pid(int emm_pid) {
        this.emm_pid = emm_pid;
    }

    public int getPmt_id() {
        return pmt_id;
    }

    public void setPmt_id(int pmt_id) {
        this.pmt_id = pmt_id;
    }

    public int getVolume_ratio() {
        return volume_ratio;
    }

    public void setVolume_ratio(int volume_ratio) {
        this.volume_ratio = volume_ratio;
    }

    public int getReserved4() {
        return reserved4;
    }

    public void setReserved4(int reserved4) {
        this.reserved4 = reserved4;
    }

    public int getVolume_reserve() {
        return volume_reserve;
    }

    public void setVolume_reserve(int volume_reserve) {
        this.volume_reserve = volume_reserve;
    }

    public int getAudio_channel_set() {
        return audio_channel_set;
    }

    public void setAudio_channel_set(int audio_channel_set) {
        this.audio_channel_set = audio_channel_set;
    }

    public int getAudio_format() {
        return audio_format;
    }

    public void setAudio_format(int audio_format) {
        this.audio_format = audio_format;
    }

    public int getAudio_index() {
        return audio_index;
    }

    public void setAudio_index(int audio_index) {
        this.audio_index = audio_index;
    }

    public tagDVBStream getVideo_stream() {
        return video_stream;
    }

    public void setVideo_stream(tagDVBStream video_stream) {
        this.video_stream = video_stream;
    }

    public tagDVBTS getTs() {
        return ts;
    }

    public void setTs(tagDVBTS ts) {
        this.ts = ts;
    }

    public tagDVBStream[] getAudio_stream() {
        return audio_stream;
    }

    public void setAudio_stream(tagDVBStream[] audio_stream) {
        this.audio_stream = audio_stream;
    }

    public int getVideo_stream_type() {
        return video_stream_type;
    }

    public void setVideo_stream_type(int video_stream_type) {
        this.video_stream_type = video_stream_type;
    }

    public int getVideo_stream_pid() {
        return video_stream_pid;
    }

    public void setVideo_stream_pid(int video_stream_pid) {
        this.video_stream_pid = video_stream_pid;
    }

    public int getVideo_ecm_pid() {
        return video_ecm_pid;
    }

    public void setVideo_ecm_pid(int video_ecm_pid) {
        this.video_ecm_pid = video_ecm_pid;
    }

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

    public int getAudio_stream_type() {
        return audio_stream_type;
    }

    public void setAudio_stream_type(int audio_stream_type) {
        this.audio_stream_type = audio_stream_type;
    }

    public int getAudio_stream_pid() {
        return audio_stream_pid;
    }

    public void setAudio_stream_pid(int audio_stream_pid) {
        this.audio_stream_pid = audio_stream_pid;
    }

    public int getAudio_ecm_pid() {
        return audio_ecm_pid;
    }

    public void setAudio_ecm_pid(int audio_ecm_pid) {
        this.audio_ecm_pid = audio_ecm_pid;
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
    
    public int getAudio_stream_type1() {
        return audio_stream_type1;
    }

    public void setAudio_stream_type1(int audio_stream_type1) {
        this.audio_stream_type1 = audio_stream_type1;
    }

    public int getAudio_stream_pid1() {
        return audio_stream_pid1;
    }

    public void setAudio_stream_pid1(int audio_stream_pid1) {
        this.audio_stream_pid1 = audio_stream_pid1;
    }

    public int getAudio_ecm_pid1() {
        return audio_ecm_pid1;
    }

    public void setAudio_ecm_pid1(int audio_ecm_pid1) {
        this.audio_ecm_pid1 = audio_ecm_pid1;
    }

    public int getAudio_stream_type2() {
        return audio_stream_type2;
    }

    public void setAudio_stream_type2(int audio_stream_type2) {
        this.audio_stream_type2 = audio_stream_type2;
    }

    public int getAudio_stream_pid2() {
        return audio_stream_pid2;
    }

    public void setAudio_stream_pid2(int audio_stream_pid2) {
        this.audio_stream_pid2 = audio_stream_pid2;
    }

    public int getAudio_ecm_pid2() {
        return audio_ecm_pid2;
    }

    public void setAudio_ecm_pid2(int audio_ecm_pid2) {
        this.audio_ecm_pid2 = audio_ecm_pid2;
    }

    public String getAudio_stream_name() {
        return audio_stream_name;
    }

    public void setAudio_stream_name(String audio_stream_name) {
        this.audio_stream_name = audio_stream_name;
    }

    public String getAudio_stream_name1() {
        return audio_stream_name1;
    }

    public void setAudio_stream_name1(String audio_stream_name1) {
        this.audio_stream_name1 = audio_stream_name1;
    }

    public String getAudio_stream_name2() {
        return audio_stream_name2;
    }

    public void setAudio_stream_name2(String audio_stream_name2) {
        this.audio_stream_name2 = audio_stream_name2;
    }
}
