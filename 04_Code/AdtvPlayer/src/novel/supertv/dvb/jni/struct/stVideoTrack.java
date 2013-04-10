package novel.supertv.dvb.jni.struct;

/**
 * 视频流参数
 *
 */
public class stVideoTrack {
    public int      StreamPid;  /* video pid, descripte in pmt */
    public int      EcmPid = 0x1FFF;        /* video ecm pid，没有则置无效值0x1FFF */
    public int      PesType;    /* video stream type, descripte in pmt */

    public stVideoTrack(){
        
    }

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
    
    @Override
    public String toString() {
        return String.format("[StreamPid=%d, EcmPid=%d, PesType=%d]", StreamPid, EcmPid, PesType);
    }
}
