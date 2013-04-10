package novel.supertv.dvb.jni.struct;

/**
 * ?
 *
 */
public class stPidStream {
    public int      StreamPid;      /* stream pid，无效值设置0x1FFF */
    public int      EcmPid;         /* stream ecm pid，没有则置无效值0x1FFF */
    public int      PesType;        /* stream pes type, descripte in pmt */
    public String      StreamDesc;     /* stream description, define & used by user */
    
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
    public String getStreamDesc() {
        return StreamDesc;
    }
    public void setStreamDesc(String streamDesc) {
        StreamDesc = streamDesc;
    }
    
    @Override
    public String toString() {
        return String.format("[StreamPid=%d, EcmPid=%d, PesType=%d, StreamDesc=%d]", 
                StreamPid, EcmPid, PesType, StreamDesc);
    }
}