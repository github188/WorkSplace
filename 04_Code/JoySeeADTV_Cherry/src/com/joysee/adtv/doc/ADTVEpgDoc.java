package com.joysee.adtv.doc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import android.util.Log;

import com.joysee.adtv.common.DateFormatUtil;
import com.joysee.adtv.logic.EPGManager;
import com.joysee.adtv.logic.bean.NETEventInfo;
import com.joysee.adtv.server.ADTVBitmapTask;
import com.joysee.adtv.server.ADTVError;
import com.joysee.adtv.server.ADTVProgramListTask;
import com.joysee.adtv.server.ADTVTask;
import com.joysee.adtv.server.ProgramDetailTask;
import com.joysee.adtv.ui.EpgGuideWindow;
import com.joysee.adtv.ui.EpgProgramFrame;

public class ADTVEpgDoc extends ADTVDoc{
    
    private static final String TAG="ADTVEpgDoc";
    EPGManager mEPGManager=null;
    
    public ADTVEpgDoc(){
        super();
        onCreate();
    }
    
    public void onCreate() {
        mEPGManager = EPGManager.getInstance();
    }
    
    public void getProgramDetail(int programId){
        ProgramDetailTask detail=new ProgramDetailTask(programId){
            public void onSingal() {
                Log.d(TAG, "------onSingal--time="+result+";error="+error);
                if(error==ADTVError.NO_ERROR && info!=null){
                    Log.d(TAG, "----type="+info.getNibble1());
                    if(info.getNibble1()!=null&&(info.getNibble1().equals("电影")||info.getNibble1().equals("电视剧"))){
                        onGotResource(new ADTVResource(EpgGuideWindow.RES_Nibble, programId, EpgGuideWindow.Type_Move)); 
                        String nibble=info.getNibble1()+" | "+info.getNibble2();
                        onGotResource(new ADTVResource(EpgGuideWindow.RES_Type, programId, nibble)); 
                    }else{
                        onGotResource(new ADTVResource(EpgGuideWindow.RES_Nibble, programId, EpgGuideWindow.Type_Other)); 
                        if(info.getDirectors()!=null&&info.getDirectors().size()>0){
                            List<String> directors=info.getDirectors();
                            String directorStr="";
                            for(int i=0;i<directors.size();i++){
                                if(i==directors.size()-1)
                                    directorStr+=directors.get(i)+"  ";
                                else 
                                    directorStr+=directors.get(i)+" | ";
                            }
                            onGotResource(new ADTVResource(EpgGuideWindow.RES_Type, programId, directorStr)); 
                        }
                    }
                    if(info.getActors()!=null&&info.getActors().size()>0){
                        
                        List<String> actors=info.getActors();
                        String actorStr="";
                        for(int i=0;i<actors.size();i++){
                            if(i==actors.size()-1)
                                actorStr+=actors.get(i)+"  ";
                            else 
                                actorStr+=actors.get(i)+" | ";
                        }
                        Log.d(TAG, "--------actorStr="+actorStr);
                        onGotResource(new ADTVResource(EpgGuideWindow.RES_Actor, programId, actorStr)); 
                    }
                    if(info.getDesc()!=null){
                        onGotResource(new ADTVResource(EpgGuideWindow.RES_About, programId, info.getDesc())); 
                    }
                    if(info.getImagepath()!=null){
                        getPoster(programId,info.getImagepath());
                    }
                }else{
                    Log.d(TAG, "--------info  is null");
                }
            }
        };
    }
    
    public void getPoster(int programId,String url){
        ADTVBitmapTask bitmatTask=new ADTVBitmapTask(programId,url){
            public void onSingal() {
                Log.d(TAG, "------onSingal-bitmap="+getBitmap()+";error="+error);
                onGotResource(new ADTVResource(EpgGuideWindow.RES_Bitmap, programId, getBitmap()));   
            }
        };
    }
    
    public void getProgramidList(int serviceId,long begin,long end){
//        Log.d(TAG, "--------------------getProgramidList------serviceId="+serviceId+";begin="+begin+";end="+end);
//        Log.d(TAG, "------------------begin="+DateFormatUtil.getStringFromMillis(begin)+";end="+DateFormatUtil.getStringFromMillis(end));
        ADTVProgramListTask programList=new ADTVProgramListTask(serviceId,begin,end){
            public void onSingal() {
                if(getStatus()==Status_Cancel){
                    Log.d(TAG, "----------is cancel------------");
                    return;
                }
                Log.d(TAG, "------onSingal--list.size="+pidList.size());
                HashMap<Integer,NETEventInfo> epglInfoList=new LinkedHashMap<Integer,NETEventInfo>();
                if(pidList!=null&&pidList.size()>0){
                    for(int i=0;i<pidList.size();i++){
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if(getStatus()==Status_Cancel){
                            Log.d(TAG, "----------is cancel------------");
                            return;
                        }
                        int eventid=pidList.get(i);
                        NETEventInfo info=getService().getEpg().getNETEventInfo(eventid);
                        if(info!=null){
                            epglInfoList.put(eventid, info);
//                            Log.d(TAG, "---------have--------NETEventInfo-------programid="+programid);
                        }else{
                            NETEventInfo eventInfo=new NETEventInfo();
//                            Log.d(TAG, "-------------------get programInfo---------begin--");
                            mEPGManager.nativeGetProgramInfo(eventid, eventInfo);
//                            Log.d(TAG, "-------------------get programInfo---------end--");
                            eventInfo.setBegintime(eventInfo.getBegintime()*1000+EpgGuideWindow.TimeOffset);
                            eventInfo.setDuration(eventInfo.getDuration()*1000);
                            epglInfoList.put(eventid, eventInfo);
                            getService().getEpg().addNETEventInfo(eventid, eventInfo);
                        }
                    }
                }
                onGotResource(new ADTVResource(EpgGuideWindow.RES_ProgramList, serviceId, pidList,epglInfoList)); 
            }
        };
    }
    
    public void cancelProgramListTask(int serviceId,long begin,long end){
        Log.d(TAG, "---------cancelProgramListTask-----------serviceId="+serviceId);
        String key=String.valueOf(serviceId)+String.valueOf(begin)+String.valueOf(end);
        ADTVProgramListTask task=ADTVProgramListTask.taskMap.get(key);
        if(task!=null){
            Log.d(TAG, "---------cancelProgramListTask-------have----");
            task.setStatus(ADTVTask.Status_Cancel);
            getService().getTaskManager().removeTask(task);
        }
    }

}
