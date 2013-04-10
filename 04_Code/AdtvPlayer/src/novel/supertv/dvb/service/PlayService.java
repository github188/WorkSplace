package novel.supertv.dvb.service;

import novel.supertv.dvb.aidl.IPlayService;
import novel.supertv.dvb.jni.JniChannelPlay;
import novel.supertv.dvb.jni.JniSetting;
import novel.supertv.dvb.jni.struct.tagDVBService;
import novel.supertv.dvb.provider.Channel;
import novel.supertv.dvb.utils.ChannelTypeNumUtil;
import novel.supertv.dvb.utils.DefaultParameter;
import novel.supertv.dvb.utils.DvbLog;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.os.RemoteException;

public class PlayService extends Service {

    private static final DvbLog log = new DvbLog(
            "novel.supertv.dvb.service.PlayService",DvbLog.DebugType.D);

    private static final String ACTION_PLAY = "novel.supertv.dvb.service.PlayService";

    private JniChannelPlay mJniPlay;
    private JniSetting mJniSetting;

    @Override
    public IBinder onBind(Intent intent) {
        log.D("onBind() intent.action = "+intent.getAction());
        if(intent.getAction() != null && ACTION_PLAY.equals(intent.getAction())){
            return mBinder;
        }
        log.E("action not martch ! check please !");
        return null;
    }

    private final IPlayService.Stub mBinder = new IPlayService.Stub(){

        @Override
        public void init() throws RemoteException {
            log.D("init()");
            jniPlayInit();
        }

        @Override
        public void uninit() throws RemoteException {
            log.D("uninit()");
            jniPlayUninit();
        }

        @Override
        public void play() throws RemoteException {
            log.D("play()");
            jniPlayStart();
        }

        @Override
        public void stop() throws RemoteException {
            log.D("stop()");
            jniPlayStop();
        }

        @Override
        public void next() throws RemoteException {
            
        }

        @Override
        public void previous() throws RemoteException {
            
        }

        @Override
        public void setWinSize(int x, int y, int width, int height)
                throws RemoteException {
            log.D("setWinSize x = "+x+", y = "+y+", width = "+width +", height = "+height);
            setWindowSize(x,y,width,height);
        }
        
    };

    /**
     * JNI播放初始化
     */
    private void jniPlayInit(){
        log.D("jniPlayInit()");
        
        mJniPlay = new JniChannelPlay();
        log.D("play.getUriString() = "+mJniPlay.getUriString());
        log.D("mJniPlay.init(); start ********************************************");
        mJniPlay.init();
        log.D("mJniPlay.init(); end   ********************************************");
        
        mJniSetting = new JniSetting();
    }

    /**
     * 开始播放
     * 之前要初始化过 init
     */
    private void jniPlayStart(){
        log.D("jniPlayStart()");
        
        log.D("mJniPlay.play() ******************************************** start");
        mJniPlay.play();
        log.D("mJniPlay.play() ******************************************** end");
        
//        tagDVBService service;
        
//        DvbApplication app = (DvbApplication)getApplication();
//        if(app != null){
//            service = app.getmCurrentChannelTag();
//            if(service != null){
//                mJniPlay.setService(service);
//            }else{
//                log.D("first come in has no service in application");
//                  service = new tagDVBService();
//                  service.setFrequency(666000);
//                  service.setSymbolRate(6875);
//                  service.setModulation(2);
//                  service.setPmt_id(100);
//                  service.setVideo_stream_pid(102);
//                  service.setVideo_stream_type(2);
//                  service.setAudio_stream_pid(103);
//                  service.setAudio_stream_type(4);
//                  service.setVideo_ecm_pid(6011);
//                  service.setAudio_ecm_pid(6011);
//                  service.setSid(104);
//                //              service.setEmm_pid(emmPid);
//                  
//                  mJniPlay.setService(service);
//            }
//            
//        }
        
        // for test:
        
//        service = new tagDVBService();
//        service.setFrequency(666000);
//        service.setSymbolRate(6875);
//        service.setModulation(2);
//        service.setPmt_id(100);
//        service.setVideo_stream_pid(102);
//        service.setVideo_stream_type(2);
//        service.setAudio_stream_pid(103);
//        service.setAudio_stream_type(4);
//        service.setVideo_ecm_pid(6011);
//        service.setAudio_ecm_pid(6011);
//        service.setSid(104);
////        service.setEmm_pid(emmPid);
//        
//        mJniPlay.setService(service);
        
        String[] selectionArgs = new String[1];
        selectionArgs[0] = ""+DefaultParameter.ServiceType.digital_television_service;
        Cursor cur = getContentResolver().query(Channel.URI.TABLE_CHANNELS,
                null, Channel.TableChannelsColumns.SERVICETYPE+" = ? ", 
                selectionArgs, Channel.TableChannelsColumns.LOGICCHNUMBER);
        
        int channelNum = ChannelTypeNumUtil.getPlayChannelNum(getApplicationContext(),
                DefaultParameter.ServiceType.digital_television_service);
        
        if(channelNum >= 0 && channelNum < cur.getCount()){
            cur.moveToPosition(channelNum);
        }else{
            cur.moveToFirst();
        }
        
     // 从cursor中取出数据,主要是pmtid,videoPid,audioPid
        int pmtid = 0;
        int pmtid_index = cur.getColumnIndex(Channel.TableChannelsColumns.PMTPID);
        if(pmtid_index >= 0){
            pmtid = cur.getInt(pmtid_index);
            log.D("playCurrentCursor pmtid ="+pmtid);
        }
        
        int videoPid = 0;
        int videoPid_index = cur.getColumnIndex(Channel.TableChannelsColumns.VIDEOSTREAMPID);
        if(videoPid_index >= 0){
            videoPid = cur.getInt(videoPid_index);
            log.D("playCurrentCursor videoPid ="+videoPid);
        }
        
        int videoTyp = 0;
        int videoTyp_index = cur.getColumnIndex(Channel.TableChannelsColumns.VIDEOPESTYPE);
        if(videoTyp_index >= 0){
            videoTyp = cur.getInt(videoTyp_index);
            log.D("playCurrentCursor videoTyp ="+videoTyp);
        }
        
        int videoEcmPid = 0;
        int videoEcmPid_index = cur.getColumnIndex(Channel.TableChannelsColumns.VIDEOECMPID);
        if(videoEcmPid_index >= 0){
            videoEcmPid = cur.getInt(videoEcmPid_index);
            log.D("playCurrentCursor videoEcmPid ="+videoEcmPid);
        }
        
        int audioPid = 0;
        int audioPid_index = cur.getColumnIndex(Channel.TableChannelsColumns.AUDIOSTREAMPID+"0");
        if(audioPid_index >= 0){
            audioPid = cur.getInt(audioPid_index);
            log.D("playCurrentCursor audioPid ="+audioPid);
        }
        
        int audioTyp = 0;
        int audioTyp_index = cur.getColumnIndex(Channel.TableChannelsColumns.AUDIOPESTYPE+"0");
        if(audioTyp_index >= 0){
            audioTyp = cur.getInt(audioTyp_index);
            log.D("playCurrentCursor audioTyp ="+audioTyp);
        }
        
        int audioEcmPid = 0;
        int audioEcmPid_index = cur.getColumnIndex(Channel.TableChannelsColumns.AUDIOECMPID+"0");
        if(audioEcmPid_index >= 0){
            audioEcmPid = cur.getInt(audioEcmPid_index);
            log.D("playCurrentCursor audioEcmPid ="+audioEcmPid);
        }
        
        int serviceId = 0;
        int serviceId_index = cur.getColumnIndex(Channel.TableChannelsColumns.SERVICEID);
        if(serviceId_index >= 0){
            serviceId = cur.getInt(serviceId_index);
            log.D("playCurrentCursor serviceId ="+serviceId);
        }
        
        int emmPid = 0;
        int emmPid_index = cur.getColumnIndex(Channel.TableChannelsColumns.EMMPID);
        if(emmPid_index >= 0){
            emmPid = cur.getInt(emmPid_index);
            log.D("playCurrentCursor emmPid ="+emmPid);
        }
        
        int transId = 0;
        int transId_index = cur.getColumnIndex(Channel.TableChannelsColumns.TRANSPONDER_ID);
        if(transId_index >= 0){
            transId = cur.getInt(transId_index);
            log.D("playCurrentCursor transId ="+transId);
        }
        
        int frequency = 0;
        int modulation = 0;
        int symbol = 0;
        String[] selectionArgs2 = new String[1];
        selectionArgs2[0] = ""+transId;
        Cursor tsCursor = getContentResolver().query(Channel.URI.TABLE_TRANSPONDERS,
                null, Channel.TableTranspondersColumns.ID+" = ? ", selectionArgs2, null);
        log.D("playCurrentCursor tsCursor ="+tsCursor);
        if(tsCursor != null){
            log.D("playCurrentCursor tsCursor getCount ="+tsCursor.getCount());
            if(tsCursor.getCount() > 0){
                
                tsCursor.moveToFirst();
                
                int frequency_index = tsCursor.getColumnIndex(Channel.TableTranspondersColumns.FREQUENCY);
                if(frequency_index >= 0){
                    frequency = tsCursor.getInt(frequency_index);
                    log.D("playCurrentCursor frequency ="+frequency);
                }
                
                int modulation_index = tsCursor.getColumnIndex(Channel.TableTranspondersColumns.MODULATION);
                if(modulation_index >= 0){
                    modulation = tsCursor.getInt(modulation_index);
                    log.D("playCurrentCursor modulation ="+modulation);
                }
                
                int symbol_index = tsCursor.getColumnIndex(Channel.TableTranspondersColumns.SYMBOLRATE);
                if(symbol_index >= 0){
                    symbol = tsCursor.getInt(symbol_index);
                    log.D("playCurrentCursor symbol ="+symbol);
                }
                
            }
            
            tsCursor.close();
        }else{
            log.E("tsCursor == null ! error !");
        }
        
        log.D("mJniPlay.setService()  ********************************************");
        
        tagDVBService service = new tagDVBService();
        service.setFrequency(frequency);
        service.setSymbolRate(symbol);
        service.setModulation(modulation);
        service.setPmt_id(pmtid);
        service.setVideo_stream_pid(videoPid);
        service.setVideo_stream_type(videoTyp);
        service.setAudio_stream_pid(audioPid);
        service.setAudio_stream_type(audioTyp);
        service.setVideo_ecm_pid(videoEcmPid);
        service.setAudio_ecm_pid(audioEcmPid);
        service.setSid(serviceId);
        service.setEmm_pid(emmPid);
        
        mJniPlay.setService(service);
    }

    /**
     * 停止播放，在换台之前调用
     */
    private void jniPlayStop(){
        log.D("jniPlayStop()");
        
        log.D("mJniPlay.stop() ******************************************** start");
        mJniPlay.stop();
        log.D("mJniPlay.stop() ******************************************** end");
    }

    /**
     * 释放播放资源
     */
    private void jniPlayUninit(){
        log.D("jniPlayUninit()");
        
        log.D("mJniPlay.uninit() ******************************************** start");
        mJniPlay.uninit();
        log.D("mJniPlay.uninit() ******************************************** end");
        
    }

    /**
     * 设置窗口大小
     * @param x
     * @param y
     * @param width
     * @param height
     */
    private void setWindowSize(int x,int y,int width,int height){
        
        mJniSetting.setDisplayRect(x, y, width, height);
    }
}
