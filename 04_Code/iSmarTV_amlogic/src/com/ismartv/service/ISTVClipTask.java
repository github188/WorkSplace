package com.ismartv.service;

import java.net.URL;
import java.util.HashMap;
import android.util.Log;

import com.ismartv.api.AccessProxy;
import com.ismartv.bean.ClipInfo;
import com.ismartv.ui.ISTVVodApplication;
import com.ismartv.ui.ISTVVodHome;

import org.json.JSONObject;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.Collection;

public class ISTVClipTask extends ISTVJsonTask{
	private static final String TAG="ISTVClipTask";
	private static HashMap<Integer,ISTVClipTask> taskMap=new HashMap<Integer,ISTVClipTask>();

	private int clipPK;

	private static synchronized boolean setRunningTask(int pk, ISTVClipTask t){
		if(t==null){
			taskMap.remove(pk);
			return true;
		}else{
			ISTVClipTask old = taskMap.get(pk);
			if(old==null){
				taskMap.put(pk, t);
				return true;
			}
		}

		return false;
	}

	ISTVEvent createEvent(){
		ISTVEvent evt = new ISTVEvent(ISTVEvent.Type.CLIP);
		evt.clipPK = clipPK;
		return evt;
	}

	ISTVClipTask(ISTVTaskManager man, int pk){
		super(man, "/api/clip/"+pk+"/"+getMACAddress()+"/", PRIO_DATA);

		this.clipPK  = pk;

		ISTVEvent evt = createEvent();
		evt.clip   = getEpg().getClip(clipPK);

		if(evt.clip!=null){
			getService().sendEvent(evt);
			return;
		}
		
		getUrl();

		/*if(setRunningTask(pk, this)){
			start();
		}*/
	}
	
	public void getUrl(){
        Log.d(TAG, "begin-------------AccessProxy------getMACAddress()="+getMACAddress()+";\n url="+getURL("/api/clip/"+clipPK+"/").toString());
        String token=ISTVService.getService().getEpg().getAccessToken();
        Log.d(TAG, "-------------AccessProxy------getAccessToken()="+token);
        if(token==null){
            Log.d(TAG, "-------------------token is null");
            token="";
        }
        String     urls[]=new String[ISTVItem.QUALITY_COUNT];
        AccessProxy.init("A11C", "1.0", getMACAddress());
        ClipInfo cinfo = AccessProxy.parse(getURL("/api/clip/"+clipPK+"/").toString(),token, ISTVVodApplication.getContext());
        if(cinfo!=null){
            urls[ISTVItem.QUALITY_ADAPTIVE] = cinfo.getAdaptive();
            urls[ISTVItem.QUALITY_LOW] = cinfo.getLow();
            urls[ISTVItem.QUALITY_MEDIUM] = cinfo.getMedium();
            urls[ISTVItem.QUALITY_NORMAL] = cinfo.getNormal();
            urls[ISTVItem.QUALITY_HIGH] = cinfo.getHigh();
            urls[ISTVItem.QUALITY_ULTRA] = cinfo.getUltra();
            for(String url : urls){
                Log.d(TAG, "-------------AccessProxy---url="+url);
            }
            ISTVEvent evt = createEvent();
            ISTVClip clip=new ISTVClip(clipPK,urls);
            evt.clip   = clip;
            getEpg().addClip(clip);
            getService().sendEvent(evt);
        }else{
            onGetDataError();
            ISTVEvent evt = createEvent();
            if(evt==null){
                Log.d(TAG, "system task error");
                getService().sendError(error, errInfo);
            }else{
                Log.d(TAG, "normal task error");
                evt.error = error;
                getService().sendEvent(evt);
            }
            Log.d(TAG, "--------------AccessProxy-------cinfo is null");
        }
        cancel();
        Log.d(TAG, "end--------------AccessProxy-------");
	}

	void onCancel(){
		setRunningTask(clipPK, null);
	}

	boolean onGotJson(JSONObject obj) throws Exception{
	    Log.d(TAG, ">>>>>>>>>JSON="+obj.toString());
		ISTVEvent evt = createEvent();
		ISTVClip clip = new ISTVClip(clipPK, obj);

		evt.clip   = clip;

		getEpg().addClip(clip);
		getService().sendEvent(evt);
		return true;
	}

	void onGetDataError(){
		setError(ISTVError.CANNOT_GET_CLIP);
	}
}
