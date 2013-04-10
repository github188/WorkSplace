package com.joysee.adtv.server;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import android.util.Xml;

import com.joysee.adtv.logic.bean.NETDetailEventInfo;

/**
 * 获取epg详情
 * @author chenggang
 *
 */
abstract public class ProgramDetailTask extends ADTVJsonTask{
    
    private static final String TAG="FetchTimeTask";

    public String result;
    public int programId;
    public NETDetailEventInfo info;
    
    public ProgramDetailTask(int programId) {
        super("", ADTVTask.PRIO_DATA, 1);
        this.programId=programId;
        if(ADTVService.getService().getEpg().getNETDetailEventInfo(programId)!=null){
            info=ADTVService.getService().getEpg().getNETDetailEventInfo(programId);
            onSingal();
        }else{
            addPostData("commend=14");
            addPostData("param1="+programId);
            start();
        }
    }

    @Override
    boolean onGotResponse(String str) {
//        Log.d(TAG, "--------str="+str);
        try {
            info=parseXml(str);
            ADTVService.getService().getEpg().addNETDetailEventInfo(programId, info);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
    
    
    public NETDetailEventInfo parseXml(String body) throws Exception{
        NETDetailEventInfo info=null;
        List<String> directors=null;
        List<String> actors=null;
        InputStream inStream=new ByteArrayInputStream(body.getBytes("utf-8"));
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(inStream, "UTF-8");
        int eventType = parser.getEventType();//产生第一个事件
        while(eventType!=XmlPullParser.END_DOCUMENT){//只要不是文档结束事件
            switch (eventType) {
            case XmlPullParser.START_DOCUMENT:
                break;
            case XmlPullParser.START_TAG:
                String name = parser.getName();
                if("Program".equals(name)){
                    info=new NETDetailEventInfo();
                    info.setProgramName(parser.getAttributeValue(null, "name"));
                    info.setProgramId(Integer.valueOf(parser.getAttributeValue(null, "programid")));
                }
                if(info!=null){
                    if("Nibble1".equals(name)){
                        info.setNibble1_id(Integer.valueOf(parser.getAttributeValue(null, "nibble1id")));
                        info.setNibble1(parser.nextText());
                    }
                    if("Nibble2".equals(name)){
                        info.setNibble2_id(Integer.valueOf(parser.getAttributeValue(null, "nibble2id")));
                        info.setNibble2(parser.nextText());
                    }
                    if("OverView".equals(name)){
                        info.setDesc(parser.nextText());
                    }
                    if("MiniImage".equals(name)){
                        info.setImagepath(parser.nextText());
                    }
                    if("ActorList".equals(name)){
                        actors=new ArrayList<String>();
                    }
                    if(actors!=null){
                        if("Actor".equals(name)){
                            actors.add(parser.getAttributeValue(null, "actorname"));
                        }
                    }
                    if("DirectorList".equals(name)){
                        directors=new ArrayList<String>();
                    }
                    if(directors!=null){
                        if("Director".equals(name)){
                            directors.add(parser.getAttributeValue(null, "directorname"));
                        }
                    }
                    
                }
                break;
            case XmlPullParser.END_TAG:
                if("ActorList".equals(parser.getName())){
                    info.setActors(actors);
                }
                if("DirectorList".equals(parser.getName())){
                    info.setDirectors(directors);
                }
                break;
            }
            eventType = parser.next();
        }
        return info;
    }
}
