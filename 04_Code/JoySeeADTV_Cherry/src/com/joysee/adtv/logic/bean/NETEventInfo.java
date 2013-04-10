package com.joysee.adtv.logic.bean;

public class NETEventInfo {
	private int logicNumer;// <逻辑频道号
	private int serviceId;// <业务 ID
	private int ChannelId; // <频道 ID 或业务 ID(service id):逻辑上的与 DVB-C'SID 存在映射关系
	private int typeId;// <节目所属类型 ID(按内容性质分)
	private int programId; // /<节目 ID(不是唯一的)
	private int eventId;// /<DVB-c event_id
	private long begintime;// <节目开始时间
	private long duration;// <播放时长
	private int nibble1_id; // /<一级分类(按内容)
	private int nibble2_id; // /<二级分类(按内容)
	private String nibble1;// <一级分类(e.g. '电视剧')
	private String nibble2;// <二级分类(e.g. '电视剧\动作')
	private String ename;// <节目名称(e.g. '潜伏-1')
	private String channelName;// <频道名称(e.g. BTV-1)
	private String typeName;// <类型名称(e.g. '央视及教育')
	private String imgPath;// <图片地址(URI/URL)
	private String description;// <节目介绍
	
	private int progress;//进度
	
	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public int getLogicNumer() {
		return logicNumer;
	}

	public void setLogicNumer(int logicNumer) {
		this.logicNumer = logicNumer;
	}

	public int getServiceId() {
		return serviceId;
	}

	public void setServiceId(int serviceId) {
		this.serviceId = serviceId;
	}

	public int getChannelId() {
		return ChannelId;
	}

	public void setChannelId(int channelId) {
		ChannelId = channelId;
	}

	public int getTypeId() {
		return typeId;
	}

	public void setTypeId(int typeId) {
		this.typeId = typeId;
	}

	public int getProgramId() {
		return programId;
	}

	public void setProgramId(int programId) {
		this.programId = programId;
	}

	public int getEventId() {
		return eventId;
	}

	public void setEventId(int eventId) {
		this.eventId = eventId;
	}

	public long getBegintime() {
		return begintime;
	}

	public void setBegintime(long begintime) {
		this.begintime = begintime;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public int getNibble1_id() {
		return nibble1_id;
	}

	public void setNibble1_id(int nibble1_id) {
		this.nibble1_id = nibble1_id;
	}

	public int getNibble2_id() {
		return nibble2_id;
	}

	public void setNibble2_id(int nibble2_id) {
		this.nibble2_id = nibble2_id;
	}

	public String getNibble1() {
		return nibble1;
	}

	public void setNibble1(String nibble1) {
		this.nibble1 = nibble1;
	}

	public String getNibble2() {
		return nibble2;
	}

	public void setNibble2(String nibble2) {
		this.nibble2 = nibble2;
	}

	public String getEname() {
		return ename;
	}

	public void setEname(String ename) {
		this.ename = ename;
	}

	public String getChannelName() {
		return channelName;
	}

	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public String getImgPath() {
		return imgPath;
	}

	public void setImgPath(String imgPath) {
		this.imgPath = imgPath;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "NETEventInfo [logicNumer=" + logicNumer + ", serviceId="
				+ serviceId + ", ChannelId=" + ChannelId + ", typeId=" + typeId
				+ ", programId=" + programId + ", eventId=" + eventId
				+ ", begintime=" + begintime + ", duration=" + duration
				+ ", nibble1_id=" + nibble1_id + ", nibble2_id=" + nibble2_id
				+ ", nibble1=" + nibble1 + ", nibble2=" + nibble2 + ", ename="
				+ ename + ", channelName=" + channelName + ", typeName="
				+ typeName + ", imgPath=" + imgPath + ", description="
				+ description + "]";
	}

}
