package com.ismartv.service;

import java.net.URL;
import android.util.Log;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.Collection;

import com.ismartv.ui.ISTVVodApplication;
import com.ismartv.ui.ISTVVodHome;
import com.ismartv.ui.R;

public class ISTVContentModelTask extends ISTVJsonTask{
	private static final String TAG="ISTVContentModelTask";
	private static ISTVContentModelTask task;

	private static synchronized boolean setRunningTask(ISTVContentModelTask t){
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

	ISTVContentModelTask(ISTVTaskManager man){
		super(man, "/static/meta/content_model.json", PRIO_ACCESS);

		if(getEpg().hasGotContentModel()){
			return;
		}

		if(setRunningTask(this)){
			start();
		}
	}

	void onCancel(){
		setRunningTask(null);
	}

	boolean onGotJson(JSONObject obj) {
		JSONArray larray = obj.names();
		int lcnt = larray.length();
		Log.d(TAG, "onGotJson,obj="+obj.toString());
		try{
		    for(int l=0; l<lcnt; l++){
		        String lang = larray.getString(l);
		        JSONArray cm = obj.getJSONArray(lang);
		        int ccnt = cm.length();
		        
		        for(int c=0; c<ccnt; c++){
		            JSONObject mod = cm.getJSONObject(c);
		            String name  = mod.getString("content_model");
		            String title = mod.getString("title");
		            JSONObject attrs = mod.getJSONObject("attributes");
		            String person_attr = mod.getString("person_attributes");
		            JSONArray names = attrs.names();
		            int ncnt = names.length();
		            for(int n=0; n<ncnt; n++){
		                String aname = names.getString(n);
		                String aval  = attrs.getString(aname);
		                getEpg().addContentModel(lang, name, title, aname, aval, person_attr);
		                //Log.d(TAG, lang+" "+name+" "+title+" "+aname+" "+aval);
		            }
		            getEpg().addContentModel(lang, name, title, "area", ISTVVodApplication.getContext().getResources().getString(R.string.area), person_attr);
		            getEpg().addContentModel(lang, name, title, "genre", ISTVVodApplication.getContext().getResources().getString(R.string.genre), person_attr);
		        }
		    }
		}catch(Exception e){
		    e.printStackTrace();
		    Log.d(TAG, "---------onGotJson---e="+e);
		    return false;
		}
		return true;
	}

	void onGetDataError(){
		setError(ISTVError.CANNOT_GET_CM);
	}
}
