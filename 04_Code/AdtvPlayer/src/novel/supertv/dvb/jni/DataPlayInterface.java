package novel.supertv.dvb.jni;

import novel.supertv.dvb.jni.struct.Transponder;
import novel.supertv.dvb.jni.struct.stChannel;
import novel.supertv.dvb.jni.struct.stTunerState;
import novel.supertv.dvb.utils.DvbLog;
import android.graphics.Rect;


public final class DataPlayInterface {
    
    private static final DvbLog log = new DvbLog(
            "novel.supertv.dvb.jni.DataPlayInterface",DvbLog.DebugType.D);
    
    public static final class ProgramPlay_Type{
        public static final int DM_DP_Programplay_Live = 0;
        public static final int DM_DP_Programplay_PIP = 1;
        public static final int DM_DP_Programplay_Unknown = 2;
    }

    public static final class ProgramPlay_CallBackType{
        public static final int ProgramPlay_CallBackType_TunerLocked = 0;
        public static final int ProgramPlay_CallBackType_LockFailed = 1;
        public static final int ProgramPlay_CallBackType_TunerSingal = 2;
        public static final int ProgramPlay_CallBackType_VideoLost = 3;
        public static final int ProgramPlay_CallBackType_VideoResume = 4;
        public static final int ProgramPlay_CallBackType_PlaySuccess = 5;
        public static final int ProgramPlay_CallBackType_PlayFailed = 6;
        public static final int ProgramPlay_CallBackType_Stoped = 7;
    }
    
    public static final class DataPlay_ServiceType{
        public static final int DataPlay_ServiceType_Reserved = 0;
        public static final int DataPlay_ServiceType_TV = 1;        
        public static final int DataPlay_ServiceType_Radio = 2;
        public static final int DataPlay_ServiceType_TeleText = 3;
        public static final int DataPlay_ServiceType_RefNvod = 4;
        public static final int DataPlay_ServiceType_TimeShiftNvod = 5;
        public static final int DataPlay_ServiceType_Mosaic = 6;
    }
    
    public static final class Monitor_CallBackType{
        public static final int Monitor_CallBackType_Nit_Loader = 0;
        public static final int Monitor_CallBackType_Nit_Search = 1;
        public static final int Monitor_CallBackType_Pmt = 2;
        public static final int Monitor_CallBackType_Sdt = 3;
        public static final int Monitor_CallBackType_Pat = 4;
    }
    
    
    public int InitPlay(){
        return 0;
    }
    public int UnInitPlay(){
        return 0;
    }
    
    public int PlayOpen(int nProgramType,int eChannelType){
        return 0;
    }
    public int PlayClose(){
        return 0; 
    }
    
    public int PlayService(Transponder tp, stChannel channel){
        return 0;
    }
    public int StopService(){
        return 0;
    }
    
    public int SetWindowSize(Rect rect){
        return 0;
    }
    
    public int SetWindowId(int nWindowId){
        return 0;
    }
    public int GetWindowId(){
        return 0;
    }
    
    public int SetServiceAudio(int nAudioPid, int uAudioType){
        return 0;
    }
    public int SetAudioChannel(int nChannelType){
        return 0;
    }
    
    public stTunerState GetTunerState(){
        return null;
    }
    
    public int SetNITVersion(int nNITVersion){
        return 0;
    }
    
    public void SetServiceType(int nType){ServiceType = nType;};
    
    @SuppressWarnings("unused")
    private int PlayHandle;
    
    private int ServiceType;
    
//    private ChannelPlay callback;
//    
//    public void setCallback(ChannelPlay obj){
//        callback = obj;
//    }
//    
//    public int CallBackFunc(int nType, Object obj)
//    {
//        if(callback != null)
//            callback.CallBackFunc(this, ServiceType, nType, obj);
//        return 0;
//    }
//    
//    public int MonitorCallBackFunc(int nType, Object obj,Object objloader)
//    {
//        log.LOGI("Type:" + nType);
//        if(callback != null)
//            callback.MonitorCallBackFunc(this, ServiceType, nType, obj ,objloader);
//        return 0;
//    }
    
//    static 
//    {
//        System.loadLibrary("ngbdvb");
//    }
}