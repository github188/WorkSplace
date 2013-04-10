
package com.joysee.adtv.logic.bean;


/**
 * 节目指南中节目数据封装类
 * @author songwenxuan
 *
 */
public class EpgEvent{
    public int id;
    public int serviceId;/* 服务id，每个频道有唯一的serviceId */
    public String name;  /* 节目名称 */
    public int channelNumber;
    public long start_time; /* 节目开始时间 */
    public long end_time;   /* 节目结束时间 */
    public String description; /* 节目描述 */

    public EpgEvent() {}
    
    /**
     * @param id          事件id.
     * @param programName 节目名.
     * @param startTime   开始时间.
     * @param endTime     结束时间.
     */
    public EpgEvent(int id, String programName, long startTime, long endTime) {
        this.id = id;
        this.name = programName;
        this.start_time = startTime;
        this.end_time = endTime;
    }

    public int getChannelNumber() {
		return channelNumber;
	}

	public void setChannelNumber(int channelNumber) {
		this.channelNumber = channelNumber;
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

    public String getProgramName() {
        return name;
    }

    public void setProgramName(String programName) {
        this.name = programName;
    }

    public long getStartTime() {
        return start_time;
    }

    public void setStartTime(long startTime) {
        this.start_time = startTime;
    }

    public long getEndTime() {
        return end_time;
    }

    public void setEndTime(long endTime) {
        this.end_time = endTime;
    }

    public String getProgramDescription() {
        return description;
    }

    public void setProgramDescription(String programDescription) {
        this.description = programDescription;
    }

}
