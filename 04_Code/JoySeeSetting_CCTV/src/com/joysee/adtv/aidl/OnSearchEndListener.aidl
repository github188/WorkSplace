package com.joysee.adtv.aidl;
import com.joysee.adtv.logic.bean.DvbService;
import com.joysee.adtv.logic.bean.ServiceType;
interface OnSearchEndListener {
    void onSearchEnd(in List<DvbService> services);
}