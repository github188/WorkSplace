package com.joysee.adtv.server;

import com.joysee.adtv.logic.bean.NETDetailEventInfo;
import com.joysee.adtv.logic.bean.NETEventInfo;
import com.joysee.adtv.logic.bean.Program;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;


public class ADTVEpg{
    
    private HashMap<Integer,NETDetailEventInfo> programDetailList=new HashMap<Integer,NETDetailEventInfo>();//节目详情
    private HashMap<Integer,NETEventInfo> programList=new HashMap<Integer,NETEventInfo>();//节目信息
    private HashMap<String,Program> reservesList=new HashMap<String,Program>();//预约列表
    private HashMap<String,ArrayList<Integer>> programIdList=new HashMap<String,ArrayList<Integer>>();//频道节目id list
    
    public ADTVEpg(){
        
    }
    
    public void addProgramIdList(String key,ArrayList<Integer> list){
        programIdList.put(key,list);
    }
    
    /**
     * 根据传来的时间检查缓存中是否有过期的
     * @param time
     */
    public void checkReservesList(int time){
        Set<String> keys=reservesList.keySet();
        ArrayList<String> removeList=new ArrayList<String>();
        for(String key:keys){
            Program program=reservesList.get(key);
            if(program!=null){
                if(program.getStartTime()<time){
                    removeList.add(key);
                }
            }
        }
        for(String key:removeList){
            reservesList.remove(key);
        }
    }
    
    public ArrayList<Integer> getProgramIdList(String key){
        return programIdList.get(key);
    }
    
    public void addNETEventInfo(int programid,NETEventInfo info){
        programList.put(programid,info);
    }
    
    public NETEventInfo getNETEventInfo(int programid){
        return programList.get(programid);
    }
    
    public void addNETDetailEventInfo(int programid,NETDetailEventInfo info){
        programDetailList.put(programid,info);
    }
    
    public NETDetailEventInfo getNETDetailEventInfo(int programid){
        return programDetailList.get(programid);
    }
    
    public void addProgram(String key,Program pro){
        reservesList.put(key, pro);
    }
    
    public Program getProgram(String key){
        return reservesList.get(key);
    }
    
    public void removeProgram(String key){
        reservesList.remove(key);
    }

}

