package com.joysee.adtv.aidl;
import com.joysee.adtv.logic.bean.TunerSignal;
interface OnSearchTunerSignalStateListener {
    void onSearchTunerSignalState(in TunerSignal tunerSignal);
}