package com.joysee.adtv.logic.bean;


/**
 * 节目指南的节目数据封装类
 */
public class ProgramEpg{
    public int serviceId;
    public int presentVersion;
    public int followingVersion;
    public int eventVersion;
    public EpgEvent presentEpgEvent;
    public EpgEvent followingEpgEvent;
    public EpgEvent[] events;
    
    public EpgEvent[] getEvents() {
        return events;
    }

    public void setEvents(EpgEvent[] events) {
        this.events = events;
    }

    public ProgramEpg(){
        
    }
    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public int getPresentVersion() {
        return presentVersion;
    }

    public void setPresentVersion(int presentVersion) {
        this.presentVersion = presentVersion;
    }

    public int getFollowingVersion() {
        return followingVersion;
    }

    public void setFollowingVersion(int followingVersion) {
        this.followingVersion = followingVersion;
    }

    public int getEventVersion() {
        return eventVersion;
    }

    public void setEventVersion(int eventVersion) {
        this.eventVersion = eventVersion;
    }

    public EpgEvent getPresentEpgEvent() {
        return presentEpgEvent;
    }

    public void setPresentEpgEvent(EpgEvent presentEpgEvent) {
        this.presentEpgEvent = presentEpgEvent;
    }

    public EpgEvent getFollowingEpgEvent() {
        return followingEpgEvent;
    }

    public void setFollowingEpgEvent(EpgEvent followingEpgEvent) {
        this.followingEpgEvent = followingEpgEvent;
    }

}