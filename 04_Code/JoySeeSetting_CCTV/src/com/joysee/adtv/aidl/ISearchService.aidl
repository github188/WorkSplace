package com.joysee.adtv.aidl;
import com.joysee.adtv.aidl.OnSearchEndListener;
import com.joysee.adtv.aidl.OnSearchReceivedNewChannelsListener;
import com.joysee.adtv.aidl.OnSearchNewTransponderListener;
import com.joysee.adtv.aidl.OnSearchProgressChangeListener;
import com.joysee.adtv.aidl.OnSearchTunerSignalStateListener;
import com.joysee.adtv.logic.bean.Transponder;
import com.joysee.adtv.logic.bean.DvbService;
import com.joysee.adtv.logic.bean.ServiceType;

interface ISearchService {
    void startSearch(in int searchType,in Transponder tp);
    void stopSearch(in boolean isSave);
    
    void setOnSearchEndListener(in OnSearchEndListener onSearchEndListener);
    void setOnSearchReceivedNewChannelsListener(in OnSearchReceivedNewChannelsListener onSearchReceivedNewChannelsListener);
    void setOnSearchNewTransponder(in OnSearchNewTransponderListener onSearchNewTransponderListener);
    void setOnSearchProgressChange(in OnSearchProgressChangeListener onSearchProgressChangeListener);
    void setOnSearchTunerSignalStateListener(in OnSearchTunerSignalStateListener onSearchTunerSignalStateListener);
    
    void saveChannels(in List<DvbService> channelsList,in List<ServiceType> serviceTypes);
    void deleteChannels();
    void saveOldChannels(in List<DvbService> channelsList);
}
