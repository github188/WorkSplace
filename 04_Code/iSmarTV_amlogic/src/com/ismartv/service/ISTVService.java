package com.ismartv.service;

import java.net.URL;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;
import android.graphics.Bitmap;
import android.os.Message;
import android.os.Looper;
import android.os.Handler;
import android.util.Log;
import com.ismartv.client.ISTVClient;

public class ISTVService{
	private static String TAG="ISTVService";
	private static ISTVService service;
	private ISTVEpg         epg;
	private ISTVBitmapCache cache;
	private ISTVTaskManager taskMan;
	private ServiceThread   thread;
	private ISTVLoginTask   login;
	private HashSet<ISTVClient> clients=new HashSet<ISTVClient>();

	private class ServiceThread extends Thread{
		private Handler handler = null;

		public void run(){
			Looper.prepare();

			handler = new Handler(){
				public void handleMessage(Message msg){
					if(msg.what==1949){
						ISTVRequest req = (ISTVRequest)msg.obj;
						solveRequest(req);
					}
				}
			};

			Looper.loop();
		}

		public void sendRequest(ISTVRequest req){
			if((handler==null) || (handler.getLooper()==null))
				return;
			Message msg = handler.obtainMessage(1949, req);
			handler.sendMessage(msg);
		}

		public boolean ready(){
			if((handler==null) || (handler.getLooper()==null))
				return false;
			return true;
		}

		public void exit(){
			if (handler != null && handler.getLooper() != null) {
				handler.getLooper().quit();
			}
		}
	}

	public static synchronized ISTVService getService(){
		if(service==null){
			Log.d(TAG, "create service");
			service = new ISTVService();
		}
		return service;
	}
	
	public synchronized void setNullService(int ty){
		Log.d(TAG, "*********************setNullService");
		for(ISTVClient c : clients){
			if(c!=null)
			    c.getDoc().activeFail(ty);
		}
		if(epg!=null)
			epg     = null;
		if(taskMan!=null)
			taskMan = null;
		if(cache!=null)
			cache   = null;
		if(thread!=null&&thread.isAlive()){
			thread.exit();
		}
		service=null;
	}

	private ISTVService(){
		epg     = new ISTVEpg();
		taskMan = new ISTVTaskManager(this);
		cache   = new ISTVBitmapCache();
		thread = new ServiceThread();
		thread.start();

		while(!thread.ready()){
			try{
				Thread.sleep(10);
			}catch(Exception e){
			}
		}

		new ISTVAccreditTask(taskMan);
		new ISTVActiveTask(taskMan);
		new ISTVContentModelTask(taskMan);
	}

	public ISTVTaskManager getTaskManager(){
		return taskMan;
	}

	public synchronized void registerClient(ISTVClient c){
		Log.d(TAG, "----------------registerClient");
		clients.add(c);
	}

	public synchronized void unregisterClient(ISTVClient c){
		clients.remove(c);
	}

	synchronized void sendEvent(ISTVEvent evt){
		for(ISTVClient c : clients){
			c.onEvent(evt);
		}
	}
	
	synchronized void sendError(int err, String info){
		ISTVEvent evt = new ISTVEvent(ISTVEvent.Type.ERROR);
		evt.error = err;
		evt.errInfo = info;

		Log.d(TAG, "send error event");

		sendEvent(evt);
	}

	public void solveRequest(ISTVRequest req){
		Log.d(TAG, "@@@@@@@@@@@@@@@@@@@@solveRequest=" + req.type);
		switch(req.type){
			case LOGIN:
				Log.d(TAG, "login "+req.user+" "+req.passwd);
				login = new ISTVLoginTask(taskMan, req.context, req.user, req.passwd);
				break;
			case STORE_DATA:
				Log.d(TAG, "request store data");
				getEpg().storeHistory();
				break;
			case BITMAP:
				Log.d(TAG, "request bitmap "+req.url.toString());
				new ISTVBitmapTask(taskMan, req.url);
				break;
			case VOD_HOME_LIST:
				Log.d(TAG, "request home list");
				new ISTVVodHomeListTask(taskMan);
				break;
			case TOP_VIDEO:
				Log.d(TAG, "request top video");
				new ISTVTopVideoTask(taskMan);
				break;
			case CHANNEL_LIST:
				Log.d(TAG, "request channel list");
				new ISTVChannelListTask(taskMan);
				break;
			case SECTION_LIST:
				Log.d(TAG, "request section list "+req.chanID);
				new ISTVSectionListTask(taskMan, req.chanID);
				break;
			case ITEM_LIST:
				Log.d(TAG, "request item list "+req.secID+" page "+req.page);
				new ISTVItemListTask(taskMan, req.secID, req.page);
				break;
			case ITEM_DETAIL:
				Log.d(TAG, "request item detail "+req.itemPK);
				new ISTVItemDetailTask(taskMan, req.itemPK);
				break;
			case CLIP:
				Log.d(TAG, "request clip "+req.clipPK);
				new ISTVClipTask(taskMan, req.clipPK);
				break;
			case HISTORY_LIST:
				Log.d(TAG, "history list");
				new ISTVHistoryListTask(taskMan);
				break;
			case BOOKMARK_LIST:
				Log.d(TAG, "request bookmark list");
				new ISTVBookmarkListTask(taskMan);
				break;
			case RELATE_LIST:
				Log.d(TAG, "request relate "+req.itemPK);
				new ISTVRelateListTask(taskMan, req.itemPK);
				break;
			case SEARCH:
				Log.d(TAG, "request search=" + req.contentModel+ "/" + req.word + "/" + req.page);
				new ISTVSearchTask(taskMan, req.contentModel, req.word, req.page);
				break;
			case HOT_WORDS:
				Log.d(TAG, "request hotwords");
				new ISTVHotWordsTask(taskMan);
				break;
			case SUGGEST_WORDS:
				Log.d(TAG, "request suggest words="+ req.word);
				new ISTVSuggestWordsTask(taskMan, req.word);
				break;
			case ADD_HISTORY:
				Log.d(TAG, "add history "+req.itemPK+" "+req.subItemPK+" "+req.offset);
				new ISTVAddHistoryTask(taskMan, req.itemPK, req.subItemPK, req.offset);
				break;
			case CLEAR_HISTORY:
				Log.d(TAG, "clear history");
				new ISTVClearHistoryTask(taskMan);
				break;
			case REMOVE_HISTORY:
				Log.d(TAG,"remove one history | pk = " + req.itemPK);
				new ISTVClearOneHistoryTask(taskMan, req.itemPK);
				break;
			case ADD_BOOKMARK:
				Log.d(TAG, "add bookmark "+req.itemPK);
				new ISTVAddBookmarkTask(taskMan, req.itemPK);
				break;
			case REMOVE_BOOKMARK:
				Log.d(TAG, "remove bookmark "+req.itemPK);
				new ISTVRemoveBookmarkTask(taskMan, req.itemPK);
				break;
			case CLEAR_BOOKMARK:
				Log.d(TAG, "clear bookmark");
				new ISTVClearBookmarkTask(taskMan);
				break;
			case RATE:
				Log.d(TAG, "rate item "+req.itemPK+" value "+req.value);
				new ISTVRateTask(taskMan, req.itemPK, req.value);
				break;
			case FILT_RATE:
				Log.d(TAG, "filt rate "+req.contentModel+" "+req.attribute+" "+req.attrID);
				new ISTVFiltRateTask(taskMan, req.contentModel, req.attribute, req.attrID);
				break;
		}
	}

	public void sendRequest(ISTVRequest req){
		thread.sendRequest(req);
	}

	public ISTVEpg getEpg(){
		return epg;
	}

	public ISTVBitmapCache getBitmapCache(){
		return cache;
	}
}

