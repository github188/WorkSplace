
package com.joysee.adtv.service;

import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.joysee.adtv.aidl.ISearchService;
import com.joysee.adtv.aidl.ISearchService.Stub;
import com.joysee.adtv.aidl.OnSearchEndListener;
import com.joysee.adtv.aidl.OnSearchNewTransponderListener;
import com.joysee.adtv.aidl.OnSearchProgressChangeListener;
import com.joysee.adtv.aidl.OnSearchReceivedNewChannelsListener;
import com.joysee.adtv.aidl.OnSearchTunerSignalStateListener;
import com.joysee.adtv.common.DvbLog;
import com.joysee.adtv.logic.ChannelManager;
import com.joysee.adtv.logic.DVBPlayManager;
import com.joysee.adtv.logic.DVBPlayManager.OnMonitorListener;
import com.joysee.adtv.logic.bean.DvbService;
import com.joysee.adtv.logic.bean.ServiceType;
import com.joysee.adtv.logic.bean.Transponder;
import com.joysee.adtv.logic.bean.TunerSignal;

/**
 * 搜索服务类
 * @author songwenxuan
 */
public class SearchService extends Service implements OnMonitorListener{

    private static final DvbLog log = new DvbLog(
            "com.joysee.adtv.service.SearchService", DvbLog.DebugType.D);
    private ChannelManager mChannelManager = ChannelManager.getInstance();
    private DVBPlayManager mDvbPlayManager;
    private static final int RECEIVED_NEW_CHANNELS = 400;
    private static final int RECEIVED_PROGRESS_CHANGED = 401;
    private static final int RECEIVED_NEW_TRANSPONDER = 402;
    private static final int RECEIVED_SIGNAL_STATE = 403;
    private static final int RECEIVED_SEARCH_END = 404;
    
    @Override
    public IBinder onBind(Intent intent) {
        if(intent.getAction() != null && "com.joysee.adtv.aidl.search".equals(intent.getAction())){
            mDvbPlayManager = DVBPlayManager.getInstance(getApplicationContext());
            mDvbPlayManager.init();
            mDvbPlayManager.setOnMonitorListener(this);
            return mBinder;
        }
        log.E("action not martch ! check please !");
        return null;
    }
    @Override
    public boolean onUnbind(Intent intent) {
    	mDvbPlayManager.uninit();
    	return super.onUnbind(intent);
    }

    /**
     * 开始搜索，供UI层直接调用
     * 
     * @param searchType 搜索类型
     * @param tp 频点数据，包括频率、调制、符号率
     */
    public void startSearch(int searchType, Transponder tp) {
        log.D("startSearch()");
        log.D("startSearch() tparam = " + tp.toString());
        mChannelManager.nativeStartSearchTV(searchType, tp);
        mDvbPlayManager.reInitChannels();
        log.D("startSearch() StartSearchTV from JNI OK !!!");
    }

    public void stopSearch(boolean isSave) {
        log.D("stopSearch()");
        mChannelManager.nativeCancelSearchTV(isSave);
        log.D("CancelSearchTV() CancelSearchTV from JNI OK !!!");
    }

    // 搜索结束监听
    private OnSearchEndListener mOnSearchEndListener;

    // 搜到新的频道列表监听
    private OnSearchReceivedNewChannelsListener mOnSearchReceivedNewChannelsListener;

    // 搜索到新的频点监听
    private OnSearchNewTransponderListener mOnSearchNewTransponderListener;

    // 搜索进度监听
    private OnSearchProgressChangeListener mOnSearchProgressChangeListener;
    
    // 搜索信号状态监听
    private OnSearchTunerSignalStateListener mOnSearchTunerSignalStateListener;
    
    private ISearchService.Stub mBinder = new Stub() {
        
        @Override
        public void stopSearch(boolean isSave) throws RemoteException {
            SearchService.this.stopSearch(isSave);
        }
        
        @Override
        public void startSearch(int type, Transponder tp) throws RemoteException {
            SearchService.this.startSearch(type, tp);
        }
        
        @Override
        public void setOnSearchProgressChange(
                OnSearchProgressChangeListener onSearchProgressChangeListener) throws RemoteException {
            SearchService.this.mOnSearchProgressChangeListener = onSearchProgressChangeListener;
            if(onSearchProgressChangeListener!=null){
            	log.D("set onSearchProgressChangeListener sucess!");
            }else{
            	log.D("set onSearchProgressChangeListener failed!");
            }
        }
        
        @Override
        public void setOnSearchNewTransponder(
                OnSearchNewTransponderListener onSearchNewTransponderListener) throws RemoteException {
            SearchService.this.mOnSearchNewTransponderListener = onSearchNewTransponderListener;
            if(onSearchNewTransponderListener!=null){
                log.D("set onSearchProgressChangeListener sucess!");
            }else{
            	log.D("set onSearchProgressChangeListener failed!");
            }
        }
        
        @Override
        public void setOnSearchEndListener(OnSearchEndListener onSearchEndListener)
                throws RemoteException {
            SearchService.this.mOnSearchEndListener = onSearchEndListener;
            if(onSearchEndListener!=null){
            	log.D("set onSearchEndListener sucess!");
            }else{
            	log.D("set onSearchEndListener failed!");
            }
        }
        
        @Override
        public void setOnSearchReceivedNewChannelsListener(
        		OnSearchReceivedNewChannelsListener onSearchReceivedNewChannelsListener) throws RemoteException {
            SearchService.this.mOnSearchReceivedNewChannelsListener = onSearchReceivedNewChannelsListener;
            if(onSearchReceivedNewChannelsListener!=null){
            	log.D("set onSearchFindNewChannelListener sucess!");
            }else{
            	log.D("set onSearchFindNewChannelListener failed!");
            }
        }

        @Override
        public void saveChannels(List<DvbService> channelsList, List<ServiceType> serviceTypes)
                throws RemoteException {
            
        }

        @Override
        public void deleteChannels() throws RemoteException {
            
        }

        @Override
        public void saveOldChannels(List<DvbService> channelsList) throws RemoteException {
            
        }

		@Override
		public void setOnSearchTunerSignalStateListener(
				OnSearchTunerSignalStateListener onSearchTunerSignalStateListener)
				throws RemoteException {
			SearchService.this.mOnSearchTunerSignalStateListener = onSearchTunerSignalStateListener;
		}
    };


	@SuppressWarnings("unchecked")
	@Override
    public void onMonitor(int monitorType, Object message) {
    	try {
	    	switch (monitorType) {
			case RECEIVED_NEW_CHANNELS:
				ArrayList<DvbService> services = (ArrayList<DvbService>)message;
				if(services != null && services.size()>0){
					mOnSearchNewTransponderListener.onSearchNewTransponder(services.get(0).getFrequency());
					mOnSearchReceivedNewChannelsListener.onSearchReceivedNewChannelsListener(services);
				}
				break;
			case RECEIVED_NEW_TRANSPONDER:
				Transponder transponder = (Transponder)message;
				if(transponder != null){
					Log.d("songwenxuan","frequency = " + transponder.getFrequency());
					mOnSearchNewTransponderListener.onSearchNewTransponder(transponder.getFrequency());
				}
				break;
			case RECEIVED_SIGNAL_STATE:
				Log.d("songwenxuan","RECEIVED_SIGNAL_STATE,"+ (TunerSignal)message);
				mOnSearchTunerSignalStateListener.onSearchTunerSignalState((TunerSignal)message);
				break;
			case RECEIVED_PROGRESS_CHANGED:
				Log.d("songwenxuan","RECEIVED_PROGRESS_CHANGED");
				mOnSearchProgressChangeListener.onSearchProgressChanged((Integer)message);
				break;
			case RECEIVED_SEARCH_END:
				Log.d("songwenxuan","search end~~");
				mOnSearchEndListener.onSearchEnd((ArrayList<DvbService>)message);
				mDvbPlayManager.reInitChannels();
				break;
			default:
				break;
			}
    	} catch (RemoteException e) {
    		e.printStackTrace();
    	}
    }
	
	
}
