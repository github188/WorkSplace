package com.joysee.adtv.logic.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.lenovo.settings.Util.DefaultParameter;
/**
 * 2012-11-02
 * 封装电视信息供JNI调用
 * @author wuhao
 */
public class DvbService implements Parcelable{
    /** 节目名称 */
    private String channelName;
    /**节目类型*/
    private int serviceType;
    /** video ecm pid，没有则置无效值0x1FFF */
    private int videoEcmPid;
    /** video pid, descripte in pmt */
    private int videoPid;
    /** video stream type, descripte in pmt */
    private int videoType;
    /** 声道 */
    private int audioChannel;
    private int audioFormat;
    private int audioIndex;

    /**
     * 多半音
     */
    private int audioEcmPid0;
    private int audioEcmPid1;
    private int audioEcmPid2;
    
    private int audioPid0;
    private int audioPid1;
    private int audioPid2;
    
    private int audioType0;
    private int audioType1;
    private int audioType2;
    
    private String audioDescribe0;
    private String audioDescribe1;
    private String audioDescribe2;

    /** 频道号 */
    private int logicChNumber;
    /** 频道类型 */
    private int channelType;

    private int pcrPid;
    private int emmPid;
    /** 该节目的pmt pid。无效值为0x1FFF */
    private int pmtId;
    /** 该节目的pmt表版本号。无效值为0xFFFFFFFF */
    private int pmtVersion; 

    private int volumeComp;
    /** nit版本 */
    private int nitVersion;
    /** bat版本 */
    private int batVersion;
    private int favorite;

    private int channelVol;
    
//    private int channelVolAdd;
    /** 频率 */
    private int frequency;
    /** 符号率 */
    private int symbolRate;
    /** 调制 */
    private int modulation;
    /*
     * 节目标示号。
     * 任何一个节目，均是由三个ID结合起来作为一个节目的唯一标志。
     * 这三个ID分别是service id、transponder id、original network id
     */
    /** service id */
    private int serviceId;
    /** original network id */
    private int orgNetId;
    /** transponder id */
    private int tsId;

    public DvbService() {
    }

    public DvbService(Parcel in){
        this.audioFormat = in.readInt();
        this.audioIndex = in.readInt();
        this.logicChNumber = in.readInt();

        this.pcrPid = in.readInt();
        this.emmPid = in.readInt();
        this.pmtId = in.readInt();
        this.pmtVersion = in.readInt();
        
        this.channelName = in.readString();
        this.serviceType = in.readInt();
        this.serviceId = in.readInt();
        
        this.volumeComp = in.readInt();

        this.videoType = in.readInt();
        this.videoPid = in.readInt();
        this.videoEcmPid = in.readInt();

        this.tsId = in.readInt();
        this.orgNetId = in.readInt();

        this.audioType0 = in.readInt();
        this.audioPid0 = in.readInt();
        this.audioEcmPid0 = in.readInt();
        this.audioType1 = in.readInt();
        this.audioPid1 = in.readInt();
        this.audioEcmPid1 = in.readInt();
        this.audioType2 = in.readInt();
        this.audioPid2 = in.readInt();
        this.audioEcmPid2 = in.readInt();

        this.audioDescribe0 = in.readString();
        this.audioDescribe1 = in.readString();
        this.audioDescribe2 = in.readString();

        this.frequency = in.readInt();
        this.symbolRate = in.readInt();
        this.modulation = in.readInt();

        this.nitVersion = in.readInt();
        this.batVersion = in.readInt();
        this.channelVol = in.readInt();
//        this.channelVolAdd = in.readInt();
        this.audioChannel = in.readInt();
        this.favorite = in.readInt();
    }

    @Override
	public String toString() {
		return "DvbService [channelName=" + channelName + ", serviceType="
				+ serviceType + ", videoEcmPid=" + videoEcmPid + ", videoPid="
				+ videoPid + ", videoType=" + videoType + ", audioChannel="
				+ audioChannel + ", audioFormat=" + audioFormat
				+ ", audioIndex=" + audioIndex + ", audioEcmPid0="
				+ audioEcmPid0 + ", audioEcmPid1=" + audioEcmPid1
				+ ", audioEcmPid2=" + audioEcmPid2 + ", audioPid0=" + audioPid0
				+ ", audioPid1=" + audioPid1 + ", audioPid2=" + audioPid2
				+ ", audioType0=" + audioType0 + ", audioType1=" + audioType1
				+ ", audioType2=" + audioType2 + ", audioDescribe0="
				+ audioDescribe0 + ", audioDescribe1=" + audioDescribe1
				+ ", audioDescribe2=" + audioDescribe2 + ", logicChNumber="
				+ logicChNumber + ", channelType=" + channelType + ", pcrPid="
				+ pcrPid + ", emmPid=" + emmPid + ", pmtId=" + pmtId
				+ ", pmtVersion=" + pmtVersion + ", volumeComp=" + volumeComp
				+ ", nitVersion=" + nitVersion + ", batVersion=" + batVersion
				+ ", favorite=" + favorite + ", channelVol=" + channelVol
				+ ", frequency=" + frequency + ", symbolRate=" + symbolRate
				+ ", modulation=" + modulation + ", serviceId=" + serviceId
				+ ", orgNetId=" + orgNetId + ", tsId=" + tsId + "]";
	}

	public static final Parcelable.Creator<DvbService> CREATOR = new Parcelable.Creator<DvbService>() {
        public DvbService createFromParcel(Parcel in) {
            return new DvbService(in);
        }
        
        public DvbService[] newArray(int size) {
            return new DvbService[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.audioFormat);
        dest.writeInt(this.audioIndex);
        dest.writeInt(this.logicChNumber);

        dest.writeInt(this.pcrPid);
        dest.writeInt(this.emmPid);
        dest.writeInt(this.pmtId);
        dest.writeInt(this.pmtVersion);
        
        dest.writeString(this.channelName);
        dest.writeInt(this.serviceType);
        dest.writeInt(this.serviceId);

        dest.writeInt(this.volumeComp);

        dest.writeInt(this.videoType);
        dest.writeInt(this.videoPid);
        dest.writeInt(this.videoEcmPid);

        dest.writeInt(this.tsId);
        dest.writeInt(this.orgNetId);

        dest.writeInt(this.audioType0);
        dest.writeInt(this.audioPid0);
        dest.writeInt(this.audioEcmPid0);
        dest.writeInt(this.audioType1);
        dest.writeInt(this.audioPid1);
        dest.writeInt(this.audioEcmPid1);
        dest.writeInt(this.audioType2);
        dest.writeInt(this.audioPid2);
        dest.writeInt(this.audioEcmPid2);

        dest.writeString(this.audioDescribe0);
        dest.writeString(this.audioDescribe1);
        dest.writeString(this.audioDescribe2);

        dest.writeInt(this.frequency);
        dest.writeInt(this.symbolRate);
        dest.writeInt(this.modulation);

        dest.writeInt(this.nitVersion);
        dest.writeInt(this.batVersion);
        dest.writeInt(this.channelVol);
//        dest.writeInt(this.channelVolAdd);
        dest.writeInt(this.audioChannel);
        dest.writeInt(this.favorite);
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String serviceName) {
        this.channelName = serviceName;
    }

    public int getServiceType() {
        return serviceType;
    }

    public void setServiceType(int serviceType) {
        this.serviceType = serviceType;
    }

    public int getVideoEcmPid() {
        return videoEcmPid;
    }

    public void setVideoEcmPid(int videoEcmPid) {
        this.videoEcmPid = videoEcmPid;
    }

    public int getVideoPid() {
        return videoPid;
    }

    public void setVideoPid(int videoPid) {
        this.videoPid = videoPid;
    }

    public int getVideoType() {
        return videoType;
    }

    public void setVideoType(int videoType) {
        this.videoType = videoType;
    }

    public int getSoundTrack() {
        return audioChannel;
    }

    public void setSoundTrack(int soundTrack) {
        this.audioChannel = soundTrack;
    }

    public int getAudioFormat() {
        return audioFormat;
    }

    public void setAudioFormat(int audioFormat) {
        this.audioFormat = audioFormat;
    }

    public int getAudioIndex() {
        return audioIndex;
    }

    public void setAudioIndex(int audioIndex) {
        this.audioIndex = audioIndex;
    }

    public int getAudioEcmPid0() {
        return audioEcmPid0;
    }

    public void setAudioEcmPid0(int audioEcmPid0) {
        this.audioEcmPid0 = audioEcmPid0;
    }

    public int getAudioEcmPid1() {
        return audioEcmPid1;
    }

    public void setAudioEcmPid1(int audioEcmPid1) {
        this.audioEcmPid1 = audioEcmPid1;
    }

    public int getAudioEcmPid2() {
        return audioEcmPid2;
    }

    public void setAudioEcmPid2(int audioEcmPid2) {
        this.audioEcmPid2 = audioEcmPid2;
    }

    public int getAudioPid0() {
        return audioPid0;
    }

    public void setAudioPid0(int audioPid0) {
        this.audioPid0 = audioPid0;
    }

    public int getAudioPid1() {
        return audioPid1;
    }

    public void setAudioPid1(int audioPid1) {
        this.audioPid1 = audioPid1;
    }

    public int getAudioPid2() {
        return audioPid2;
    }

    public void setAudioPid2(int audioPid2) {
        this.audioPid2 = audioPid2;
    }

    public int getAudioType0() {
        return audioType0;
    }

    public void setAudioType0(int audioType0) {
        this.audioType0 = audioType0;
    }

    public int getAudioType1() {
        return audioType1;
    }

    public void setAudioType1(int audioType1) {
        this.audioType1 = audioType1;
    }

    public int getAudioType2() {
        return audioType2;
    }

    public void setAudioType2(int audioType2) {
        this.audioType2 = audioType2;
    }

    public String getAudioDescribe0() {
        return audioDescribe0;
    }

    public void setAudioDescribe0(String audioDescribe0) {
        this.audioDescribe0 = audioDescribe0;
    }

    public String getAudioDescribe1() {
        return audioDescribe1;
    }

    public void setAudioDescribe1(String audioDescribe1) {
        this.audioDescribe1 = audioDescribe1;
    }

    public String getAudioDescribe2() {
        return audioDescribe2;
    }

    public void setAudioDescribe2(String audioDescribe2) {
        this.audioDescribe2 = audioDescribe2;
    }

    public int getLogicChNumber() {
        return logicChNumber;
    }

    public void setLogicChNumber(int logicChNumber) {
        this.logicChNumber = logicChNumber;
    }

    public int getChannelType() {
        return channelType;
    }

    public void setChannelType(int channelType) {
        this.channelType = channelType;
    }

    public int getPcrPid() {
        return pcrPid;
    }

    public void setPcrPid(int pcrPid) {
        this.pcrPid = pcrPid;
    }

    public int getEmmPid() {
        return emmPid;
    }

    public void setEmmPid(int emmPid) {
        this.emmPid = emmPid;
    }

    public int getPmtId() {
        return pmtId;
    }

    public void setPmtId(int pmtId) {
        this.pmtId = pmtId;
    }

    public int getPmtVersion() {
        return pmtVersion;
    }

    public void setPmtVersion(int pmtVersion) {
        this.pmtVersion = pmtVersion;
    }

    public int getVolumeComp() {
        return volumeComp;
    }

    public void setVolumeComp(int volumeComp) {
        this.volumeComp = volumeComp;
    }

    public int getNitVersion() {
        return nitVersion;
    }

    public void setNitVersion(int nitVersion) {
        this.nitVersion = nitVersion;
    }

    public int getBatVersion() {
        return batVersion;
    }

    public void setBatVersion(int batVersion) {
        this.batVersion = batVersion;
    }

    public int getFavorite() {
        return serviceType & DefaultParameter.ServiceType.FAVORITE;
    }

    public void setFavorite(int favorite) {
        this.favorite = favorite;
    }

    public int getChannelVol() {
        return channelVol;
    }

    public void setChannelVol(int channelVol) {
        this.channelVol = channelVol;
    }

//    public int getChannelVolAdd() {
//        return channelVolAdd;
//    }
//
//    public void setChannelVolAdd(int channelVolAdd) {
//        this.channelVolAdd = channelVolAdd;
//    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public int getSymbolRate() {
        return symbolRate;
    }

    public void setSymbolRate(int symbolRate) {
        this.symbolRate = symbolRate;
    }

    public int getModulation() {
        return modulation;
    }

    public void setModulation(int modulation) {
        this.modulation = modulation;
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public int getOrgNetId() {
        return orgNetId;
    }

    public void setOrgNetId(int orgNetId) {
        this.orgNetId = orgNetId;
    }

    public int getTsId() {
        return tsId;
    }

    public void setTsId(int tsId) {
        this.tsId = tsId;
    }

}
