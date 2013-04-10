package com.joysee.adtv.aidl;
import com.joysee.adtv.logic.bean.DvbService;
interface OnSearchReceivedNewChannelsListener {
    boolean onSearchReceivedNewChannelsListener(in List<DvbService> services);
}