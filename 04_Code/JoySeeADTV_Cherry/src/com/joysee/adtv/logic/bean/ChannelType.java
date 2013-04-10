package com.joysee.adtv.logic.bean;

import java.util.ArrayList;
/**
 * 存储不同类型频道数据
 */
public class ChannelType {

	public int typeId;
	public String typeName;
	public ArrayList<DvbService> channels = new ArrayList<DvbService>();

	public ChannelType(int typeId, String typeName) {
		this.typeId = typeId;
		this.typeName = typeName;
	}

	public void addChannel(DvbService service) {
		if (service != null && !channels.contains(service)) {
			channels.add(service);
		}
	}

	public void removeChannel(DvbService service) {
		if (service != null && channels.contains(service)) {
			channels.remove(service);
		}
	}
}
