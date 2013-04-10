package com.ismartv.service;

import java.net.URL;
import android.util.Log;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.ArrayList;

public class ISTVTopVideoTask extends ISTVJsonTask{
	private static final String TAG="ISTVTopVideoTask";
	private static ISTVTopVideoTask task;

	private static synchronized boolean setRunningTask(ISTVTopVideoTask t){
		if(t==null){
			task = null;
			return true;
		}else if(task==null){
			task = t;
			return true;
		}else if(task!=null){
			task=t;
			return true;
		}

		return false;
	}

	ISTVEvent createEvent(){
		ISTVEvent evt = new ISTVEvent(ISTVEvent.Type.TOP_VIDEO);
		return evt;
	}

	ISTVTopVideoTask(ISTVTaskManager man){
		super(man, "/api/tv/frontpage/", PRIO_DATA);

		Log.d(TAG, "send front page URL "+url.toString());
//不用缓存
//		ISTVTopVideo tv = getEpg().getTopVideo();
//		if(tv!=null){
//			ISTVEvent evt = createEvent();
//
//			evt.topVideo = tv;
//			getService().sendEvent(evt);
//			return;
//		}

//		if(setRunningTask(this)){
			Log.d(TAG, "request front page URL "+url.toString());
			start();
//		}
	}

	void onCancel(){
		setRunningTask(null);
	}

	private URL parseURL(JSONObject obj, String name){
		String str;
		URL u = null;

		str = obj.optString(name);
		if(str!=null && !str.equals("") && !str.equals("null")){
			try{
				u = new URL(str);
			}catch(Exception e){
			}
		}

		return u;
	}

	boolean onGotJson(JSONObject obj) throws Exception{
		ISTVEvent evt = createEvent();
		ISTVTopVideo tv = new ISTVTopVideo();

		Log.d(TAG, "get top video");
		evt.topVideo = tv;

		JSONArray array = obj.optJSONArray("images");
		if(array!=null){
			if(array.length()>0){
				JSONObject img = array.getJSONObject(0);

				tv.videoPosterURL = parseURL(img, "image_url");
				tv.videoTitle = img.optString("title");
				tv.videoIsComplex = img.optBoolean("is_complex");

				String mod = img.optString("model_name");
				URL u= parseURL(img, "url");

				if(u!=null){
					if(mod!=null && mod.equals("section")){
						String id = u.getPath().replaceAll("/api/tv/section/(\\w+)/", "$1");
						tv.videoSecID = id;
						tv.videoChanID = img.optString("channel");
					}else{
						String pk = u.getPath().replaceAll("/api/item/(\\d+)/", "$1");
						tv.videoItemPK = Integer.parseInt(pk);
					}
				}
			}
			if(array.length()>1){
				JSONObject img = array.getJSONObject(1);

				tv.imageURL = parseURL(img, "image_url");
				tv.imageTitle = img.optString("title");
				tv.imageIsComplex = img.optBoolean("is_complex");

				String mod = img.optString("model_name");
				URL u= parseURL(img, "url");

				if(u!=null){
					if(mod!=null && mod.equals("section")){
						String id = u.getPath().replaceAll("/api/tv/section/(\\w+)/", "$1");
						tv.imageSecID = id;
						tv.imageChanID = img.optString("channel");
					}else{
						String pk = u.getPath().replaceAll("/api/item/(\\d+)/", "$1");
						tv.imageItemPK = Integer.parseInt(pk);
					}
				}
			}
		}

		array = obj.optJSONArray("videos");
		if(array!=null){
			if(array.length()>0){
				JSONObject vid = array.getJSONObject(0);

				tv.videoURL = parseURL(vid, "video_url");
				tv.videoTitle = vid.optString("title");
				tv.videoIsComplex = vid.optBoolean("is_complex");

				String mod = vid.optString("model_name");
				URL u= parseURL(vid, "url");

				if(u!=null){
					if(mod!=null && mod.equals("section")){
						String id = u.getPath().replaceAll("/api/tv/section/(\\w+)/", "$1");
						tv.videoSecID = id;
						tv.videoChanID = vid.optString("channel");
					}else{
						String pk = u.getPath().replaceAll("/api/item/(\\d+)/", "$1");
						tv.videoItemPK = Integer.parseInt(pk);
					}
				}
			}
		}

		getEpg().setTopVideo(evt.topVideo);
		getService().sendEvent(evt);
		return true;
	}

	void onGetDataError(){
		setError(ISTVError.CANNOT_GET_TOP_VIDEO);
	}
}
