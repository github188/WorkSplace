package novel.supertv.dvb.jni;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import novel.supertv.dvb.jni.struct.TuningParam;
import novel.supertv.dvb.jni.struct.stAudioTrack;
import novel.supertv.dvb.jni.struct.stChannel;
import novel.supertv.dvb.jni.struct.stPidStream;
import novel.supertv.dvb.jni.struct.stServiceIdent;
import novel.supertv.dvb.jni.struct.stVideoTrack;
import novel.supertv.dvb.jni.struct.tagDVBService;
import novel.supertv.dvb.jni.struct.tagTunerSignal;
import novel.supertv.dvb.utils.DvbLog;

public class JniChannelSearch {

    private static final DvbLog log = new DvbLog(
            "novel.supertv.dvb.jni.JniChannelSearch",DvbLog.DebugType.D);
    
    static 
    {
        log.D("************************load adtv start************");
        System.loadLibrary("adtv");
        log.D("************************load adtv end  ************");
    }

    /**
     * 开始搜索
     * @param iMode
     * @param tuningParam
     * @param pNotify
     * @return
     */
    public native int StartSearchTV(int iMode,TuningParam tuningParam,ISearchTVNotify pNotify);

    /**
     * 停止搜索，人为调用的
     * @return
     */
    public native int CancelSearchTV();

    /**
     * 测试
     * @return
     */
    public native String getNameString();

    private static ISearchTVNotify notify;

    public void setNotify(ISearchTVNotify notify){
        this.notify = notify;
    }
    
    //Vector<tagDVBService> services
    public static void OnSTVComplete(Vector<tagDVBService> services){
        log.D("************************OnSTVComplete()************");
        log.D(services.size()+"         chehongliang");
        List<stChannel> channels = new ArrayList<stChannel>();
        if(services != null && services.size() > 0){
          
          for (int i = 0; i < services.size(); i++) {
              
              stChannel channel = new stChannel();
              channel.setServiceName(services.get(i).getName());
              //channel.setLogicChNumber((Char)(services.get(i).getChannel_number()));
              
              channels.add(channel);
              // 音频结构
              stVideoTrack stVt = new stVideoTrack();
              stVt.setEcmPid(services.get(i).getVideo_ecm_pid());
              stVt.setStreamPid(services.get(i).getVideo_stream_pid());
              stVt.setPesType(services.get(i).getVideo_stream_type());
              channel.setVideoTrack(stVt);
              
           // 视频结构
              stAudioTrack [] stAt = new stAudioTrack[3];
              
              stAt[0] = new stAudioTrack();
              stAt[0].setEcmPid(services.get(i).getAudio_ecm_pid());
              stAt[0].setPesType(services.get(i).getAudio_stream_type());
              stAt[0].setStreamPid(services.get(i).getAudio_stream_pid());
              stAt[1] = new stAudioTrack();
              stAt[1].setEcmPid(services.get(i).getAudio_ecm_pid1());
              stAt[1].setPesType(services.get(i).getAudio_stream_type1());
              stAt[1].setStreamPid(services.get(i).getAudio_stream_pid1());
              stAt[2] = new stAudioTrack();
              stAt[2].setEcmPid(services.get(i).getAudio_ecm_pid2());
              stAt[2].setPesType(services.get(i).getAudio_stream_type2());
              stAt[2].setStreamPid(services.get(i).getAudio_stream_pid2());
              
              channel.setAudioTrack(stAt);
              
              stPidStream[] stps = new stPidStream[3];
              stps[0] = new stPidStream();
              stps[0].setStreamDesc(services.get(i).getAudio_stream_name());
              stps[1] = new stPidStream();
              stps[1].setStreamDesc(services.get(i).getAudio_stream_name1());
              stps[2] = new stPidStream();
              stps[2].setStreamDesc(services.get(i).getAudio_stream_name2());
              
              channel.setTeletext(stps);
              
              channel.setLogicChNumber(services.get(i).getChannel_number());
              channel.setPcrPid(services.get(i).getPcr_pid());
              channel.setEmmPid(services.get(i).getEmm_pid());
              channel.setPmtPid(services.get(i).getPmt_id());
              
              stServiceIdent sident = new stServiceIdent();
              sident.setOrgNetId(services.get(i).getNet_id());
              sident.setServiceId(services.get(i).getSid());
              sident.setTsId(services.get(i).getTs_id());
              channel.setServiceIdent(sident);
              
              channel.setServiceName(services.get(i).getName());
              channel.setServiceType((byte)services.get(i).getService_type());
              
              channel.setTransponderId(services.get(i).getTs_id());
              
              channel.setFrequency(services.get(i).getFrequency());
              channel.setSymbolRate(services.get(i).getSymbolRate());
              channel.setModulation(services.get(i).getModulation());
          }
          
        }
        if(notify != null){
            notify.OnSTVComplete(channels);
        }
    }

    public static void OnProgress(int progress){
        log.D("************************OnProgress()************"+progress);
        
        if(notify != null){
            notify.OnProgress(progress);
        }
        
    }

    public static void OnTunerInfo(TuningParam transponder, tagTunerSignal signal){
        log.D("************************OnTunerInfo()************");
        log.D("TuningParam = "+transponder.toString());
        log.D("tagTunerSignal = "+signal.toString());
        if(notify != null){
            notify.OnTunerInfo(transponder, signal);
        }
        
    }

    public static void OnDVBService(tagDVBService services){
        log.D("************************OnDVBService()************");
        log.D("OnDVBService = "+services);
        
        
        stChannel channel = new stChannel();
        
        channel.setServiceName(services.getName());
        
        // 音频结构
        stVideoTrack stVt = new stVideoTrack();
        stVt.setEcmPid(services.getVideo_ecm_pid());
        stVt.setStreamPid(services.getVideo_stream_pid());
        stVt.setPesType(services.getVideo_stream_type());
        channel.setVideoTrack(stVt);
        
        // 视频结构
        
        stAudioTrack [] stAt = new stAudioTrack[3];
////        stAt.setchannelType(channeltype)
        // 没东西，空指针
        stAt[0] = new stAudioTrack();
        stAt[0].setEcmPid(services.getAudio_ecm_pid());
////        stAt.setLangCode(langCode)
        stAt[0].setPesType(services.getAudio_stream_type());
        stAt[0].setStreamPid(services.getAudio_stream_pid());
        channel.setAudioTrack(stAt);
        
        channel.setLogicChNumber(services.getChannel_number());
        channel.setPcrPid(services.getPcr_pid());
        channel.setEmmPid(services.getEmm_pid());
        channel.setPmtPid(services.getPmt_id());
        
        // tsid...
//        stServiceIdent sident = new stServiceIdent();
//        sident.setOrgNetId(services.getTs().getNet_id());
//        sident.setServiceId(services.getSid());
//        sident.setTsId(services.getTs().getTs_id());
//        channel.setServiceIdent(sident);
        stServiceIdent sident = new stServiceIdent();
        sident.setOrgNetId(services.getNet_id());
        sident.setServiceId(services.getSid());
        sident.setTsId(services.getTs_id());
        channel.setServiceIdent(sident);
        
        channel.setServiceName(services.getName());
//        channel.setServiceOrgType(services.get)
        channel.setServiceType((byte)services.getService_type());
//        channel.setSubtitle(subtitle)
//        channel.setTeletext(teletext)
        
        // 待调试
        channel.setTransponderId(services.getTs_id());
        
        
        
//        channel.setVideoTrack(videoTrack)
//        channel.setVolBalance(volBalance)
//        channel.setVolCompensation(volCompensation)
//        channel.setVolume(volume)
        
        
        
        if(notify != null){
            notify.OnDVBService(channel);
        }
        
    }

    // 备用
//    public static void OnDVBService(List<tagDVBService> services){
//        log.D("************************OnDVBService()************");
//        log.D("channels count = "+services.size());
//        if(services != null && services.size() > 0){
//            List<stChannel> channels = new ArrayList<stChannel>();
//            
//            for (int i = 0; i < services.size(); i++) {
//                
//                stChannel channel = new stChannel();
//                channel.setServiceName(services.get(i).getName());
//                //channel.setLogicChNumber((Char)(services.get(i).getChannel_number()));
//                
//                channels.add(channel);
//            }
//            
//            if(notify != null){
//                notify.OnDVBService(channels);
//            }
//        }
//    }
}
