package com.ismartv.client;

import java.util.HashSet;
import java.util.ArrayList;
import android.os.Message;
import android.os.Looper;
import android.os.Handler;
import android.util.Log;

import com.ismartv.doc.ISTVDoc;
import com.ismartv.service.ISTVError;
import com.ismartv.service.ISTVEvent;
import com.ismartv.service.ISTVRequest;
import com.ismartv.service.ISTVBitmapCache;
import com.ismartv.service.ISTVEpg;
import com.ismartv.service.ISTVBitmapCache;
import com.ismartv.service.ISTVService;
import java.util.Iterator;
import java.util.Collections;
import java.util.Set;
import java.util.Vector;

abstract public class ISTVClient{
	private static final String TAG="ISTVClient";
	private Vector<ISTVSignal> signalSet = new Vector<ISTVSignal>();
	private ArrayList<ISTVSignal> signalAddList = new ArrayList<ISTVSignal>();
	private ArrayList<ISTVSignal> signalRemoveList = new ArrayList<ISTVSignal>();
	private boolean inLoop=false;
	private ISTVService service;
	private boolean hasError=false;
	private boolean regFlag=false;
	private ISTVDoc doc;
	
	private Integer statusLock = new Integer(0);

	private Handler handler = new Handler(Looper.getMainLooper()){
		public void handleMessage(Message msg) {
			if(msg.what==1978){
				ISTVEvent evt = (ISTVEvent)msg.obj;
				solveEvent(evt);
			}
		}
	};

	public ISTVClient(){
		service = ISTVService.getService();
	}
	
	public void connect(){
		service.registerClient(this);
		onConnected();
	}

	public void disconnect(){
		service.unregisterClient(this);
	}
	
	public void setDoc(ISTVDoc d){
		this.doc=d;
	}
	
	public ISTVDoc getDoc(){
		return doc;
	}

	public ISTVEpg getEpg(){
		return service.getEpg();
	}

	public ISTVBitmapCache getBitmapCache(){
		return service.getBitmapCache();
	}

	abstract public void onConnected();

	void addSignal(ISTVSignal s){
		if(inLoop){
			signalAddList.add(s);
		}else{
		synchronized(signalSet) {
			signalSet.add(s);
		}
		}
	}

	void removeSignal(ISTVSignal s){
		if(inLoop){
			signalRemoveList.add(s);
		}else{
		synchronized(signalSet) {	
			signalSet.remove(s);
		}
		}
	}

	void sendRequest(ISTVRequest req){
		service.sendRequest(req);
	}

	public void onEvent(ISTVEvent evt){
		Message msg = handler.obtainMessage(1978, evt);

		handler.sendMessage(msg);
	}

	public void reload(){	

		for(ISTVSignal s : signalSet){
			if((s.status==ISTVSignal.STATUS_END) || (s.status==ISTVSignal.STATUS_ERROR)){
				s.refresh();
			}
		}	
	
	}

	public void reloadError(){
		if(!hasError||inLoop)
			return;

		for(ISTVSignal s : signalSet){
			if(s.status==ISTVSignal.STATUS_ERROR){
				synchronized(statusLock){
					s.refresh();
				}
			}
		}

		hasError = true;
	}

	void solveEvent(ISTVEvent evt){
		inLoop=true;
		boolean error=false;
		ISTVEvent errEvent=null;
		boolean match=false;
		
		if(evt.type!=ISTVEvent.Type.ERROR){
		
			for(ISTVSignal s : signalSet){
				if(s.status==ISTVSignal.STATUS_RUNNING){
					if(s.match(evt)){
						if(evt.error!=0){
							if(!s.checkError(evt)){
								Log.d(TAG, "event "+evt.type+" error "+evt.error);
								synchronized(statusLock){
									s.setStatus(ISTVSignal.STATUS_ERROR);
								}
								//下面三行为评分,收藏单独处理,如果请求被服务器拒绝,仍要向上返回,让用户知道评分成功与否,chenggang
								if(evt.type==ISTVEvent.Type.RATE||evt.type==ISTVEvent.Type.ADD_BOOKMARK||evt.type==ISTVEvent.Type.REMOVE_BOOKMARK){
									if(s.isEnd()){
										synchronized(statusLock){
										    s.setStatus(ISTVSignal.STATUS_END);
										}
									}
									s.onSignal();
								}else if(evt.type==ISTVEvent.Type.BOOKMARK_LIST){//这个一直取不到，单独处理，不然页面会弹出提示窗口
									hasError=true;
								}else{
									error=true;
								}
							}
						}else{
							s.copyData(evt);
							if(s.isEnd()){
								synchronized(statusLock){
								    s.setStatus(ISTVSignal.STATUS_END);
								}
							}
							s.onSignal();
						}
						match=true;
					}
				}
			}
		
			if(error){
				errEvent = new ISTVEvent(ISTVEvent.Type.ERROR);
				errEvent.error = evt.error;
			}
		}else{
			error = true;
			errEvent = evt;
		}		
		if(error){
		
			for(ISTVSignal s : signalSet){
				if(s.status==ISTVSignal.STATUS_RUNNING){					
					Log.d(TAG, "match type=" + errEvent.type + ",error" + errEvent.error);
					if(s.match(errEvent)){				
						
						s.copyData(errEvent);
						s.onSignal();
						match=true;
					}
				}
			}
		
			hasError=true;
		}
		synchronized(signalSet) {
		for(ISTVSignal s : signalAddList){
			signalSet.add(s);
		}

		for(ISTVSignal s : signalRemoveList){
			signalSet.remove(s);
		}
		}
		signalAddList.clear();
		signalRemoveList.clear();

		inLoop=false;
	}

	public void checkEndSignals(){
		regFlag = true;
		
		for(ISTVSignal s : signalSet){
			if(s.status==ISTVSignal.STATUS_CHECK_END){
				if(s.isEnd()){
					s.setStatus(ISTVSignal.STATUS_END);
				}
				s.onSignal();
			}
		}
		
	}

	boolean registered(){
		return regFlag;
	}
}
