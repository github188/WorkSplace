package novel.supertv.dvb.jni.struct;

/**
 * 音频流参数
 *
 */
public class stAudioTrack {
    public int      StreamPid;      /* audio pid, descripte in pmt */
    public int      EcmPid;         /* audio ecm pid，没有则置无效值0x1FFF */
    public int      PesType;        /* audio stream type, descripte in pmt */
    public int      LangCode;       /* audio stream language code(ISO639-2) */
    public int      channelType;    /* audio mode */
    
    public int getStreamPid() {
        return StreamPid;
    }
    public void setStreamPid(int streamPid) {
        StreamPid = streamPid;
    }
    public int getEcmPid() {
        return EcmPid;
    }
    public void setEcmPid(int ecmPid) {
        EcmPid = ecmPid;
    }
    public int getPesType() {
        return PesType;
    }
    public void setPesType(int pesType) {
        PesType = pesType;
    }
    public int getLangCode() {
        return LangCode;
    }
    public void setLangCode(int langCode) {
        LangCode = langCode;
    }
    public int getchannelType() {
        return channelType;
    }
    public void setchannelType(int channeltype) {
        channelType = channeltype;
    }
    
    @Override
    public String toString() {
        return String.format("[StreamPid=%d, EcmPid=%d, PesType=%d, LangCode=0x%X, channelType=%d]", 
                StreamPid, EcmPid, PesType, LangCode, channelType);
    }
}
