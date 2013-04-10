package com.bestv.ott.appstore.activity;

import java.util.HashMap;
import java.util.Map;

import android.app.ActivityGroup;
import android.app.LocalActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import com.bestv.ott.appstore.R;
import com.bestv.ott.appstore.utils.AppLog;
import com.bestv.ott.appstore.utils.Utils;

/**
 * File：VlcTech UriActivity Description:资源跳转Uri处理类
 */

@SuppressWarnings("deprecation")
public class UriActivity extends ActivityGroup {
    private final static String TAG="com.joysee.appstore.activity.UriActivity"; 
    private LocalActivityManager localActivityManager;
    LinearLayout container;
    private String param = null;
    private String activityId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	Log.d(TAG,"-----------enter UriActivity oncreate()----");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.uriactivity);
        localActivityManager = getLocalActivityManager();
        container = (LinearLayout) findViewById(R.id.container);
        processExtraData();
        startGroupActivity(getClassNameByGroupId()); 
    }

    @Override
	protected void onNewIntent(Intent intent) {
    	AppLog.d(TAG, "---------onNewIntent()---------intent"+intent.getIntExtra("app_id",-1));
		setIntent(intent);
		processExtraData();
	}
    
    private void processExtraData(){
    	// 获取调用设置的参数和activityId
        String paramStr=getIntent().getStringExtra("param");
        Log.d(TAG, "----------paramStr="+paramStr);
        String[] paramArr = null;
        if(paramStr!=null){
        	 paramArr=paramStr.split("\\|");
             Log.d(TAG, "----------paramArr.length="+paramArr.length);
             if(paramArr.length>0&&paramArr.length<=1){
            	 activityId = "0" ;
             }
             if(paramArr.length>1){
            	 Log.d(TAG,"---enter paramArr.length>1--------paramArr[0]="+paramArr[0]+";paramArr[1]="+paramArr[1]);
            	 if(Utils.isNULL(paramArr[0])||Utils.isNULL(paramArr[1])){
            		 activityId = "0";
            	 }else {
            		 activityId = paramArr[0];
                  	 param=paramArr[1]; 
            	 }
             }
             Log.d(TAG,"--------UriActivity oncreate()---param="+param+";activityId="+activityId);
        }else{
        	Log.d(TAG, "----------paramStr=null----------");
        	activityId = "0";
        	param = null;
        }
        if(activityId.equals("0")){
        	startActivity(new Intent(UriActivity.this,MainActivity.class));
        	this.finish();
        }else if(activityId.equals("1")){
        	Intent intent = new Intent();
        	intent.setClass(UriActivity.this, ClassActivity.class);
        	intent.putExtra("type_id", Integer.valueOf(param));
        	startActivity(intent);
        	this.finish();
        }else if(activityId.equals("2")){
        	Intent intent = new Intent();
        	intent.setClass(UriActivity.this, DetailedActivity.class);
        	intent.putExtra("app_id", Integer.valueOf(param));
        	startActivity(intent);
        	this.finish();
        }
        startGroupActivity(getClassNameByGroupId());
    }
    /**
     * 页面跳转方法
     */
    public void startGroupActivity(Class<?> targetClass) {
        // 移除所有View
    	Log.d(TAG,"-----------enter UriActivity startGroupActivity()----param=<"+param+">");
        container.removeAllViews();
        Intent intent = new Intent();
        intent.setClass(UriActivity.this, targetClass);
        if(null==param||"null".trim().equals(param.trim())||"".equals(param.trim())){
        	activityId="0";
        }else{
        	if (activityId.equals("1")) {
        		Log.d(TAG, "*********activityId=1**********typeid="+param);
                intent.putExtra("type_id", Integer.valueOf(param));
            } else if (activityId.equals("2")) { 
            	Log.d(TAG, "*********activityId=2**********app_id="+param);
                intent.putExtra("app_id", Integer.valueOf(param));
            }
        }
        Log.d(TAG, "-----------------activityId="+activityId+"**********typeid="+param);
        container.addView(localActivityManager.startActivity(activityId, intent).getDecorView());
        Log.d(TAG,"-----------exit UriActivity startGroupActivity()----");
    }

    Class<?> getClassNameByGroupId() {
    	Log.d(TAG, "-----------------enter getClassNameByGroupId()---");
        return (Class<?>) NameValuePair().get(this.activityId);
    }

    Map<String, Object> NameValuePair() {
    	Log.d(TAG, "-----------------enter NameValuePair()---");
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("0", MainActivity.class);
        map.put("1", ClassActivity.class);
        map.put("2", DetailedActivity.class);
        Log.d(TAG, "-----------------exit NameValuePair()---");
        return map;
    }

}
