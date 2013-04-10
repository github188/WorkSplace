package com.joysee.adtv.aidl;
import com.joysee.adtv.logic.bean.Transponder;
import com.joysee.adtv.logic.bean.TunerSignal;
interface OnSearchNewTransponderListener {
    void onSearchNewTransponder(in int frequency);
}