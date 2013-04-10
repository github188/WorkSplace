package com.joysee.adtv.logic.bean;
/**
 *封装预约节目数据的类 
 **/
public class Program {
    private String name;
    private long startTime;
    private long endTime;
    private int serviceId;
    private String channelName;
    private int id;
    private int channelNumber;
    private int programId;
    private int status;
    
    public int getChannelNumber() {
		return channelNumber;
	}
	public void setChannelNumber(int channelNumber) {
		this.channelNumber = channelNumber;
	}
	public long getEndTime() {
        return endTime;
    }
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
    public int getServiceId() {
        return serviceId;
    }
    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public long getStartTime() {
        return startTime;
    }
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
    public String getChannelName() {
        return channelName;
    }
    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }
    
    public int getProgramId() {
        return programId;
    }
    public void setProgramId(int programId) {
        this.programId = programId;
    }
    
    public int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }
    public Program(){}
    public Program(long startTime,String name,String channelName){}
    
    public String toString(){
        return "[id="+id+";serviceId="+serviceId+";programId="+programId+";name="+name+";channelName="+channelName+";" +
        		"channelNumber="+channelNumber+";startTime="+startTime+";endTime="+endTime+"]";
    }
}