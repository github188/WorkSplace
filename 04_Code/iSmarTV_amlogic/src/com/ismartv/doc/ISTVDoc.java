package com.ismartv.doc;

import java.util.Collection;

import com.ismartv.client.ISTVClient;
import com.ismartv.client.ISTVErrorSignal;
import com.ismartv.client.ISTVLoginSignal;
import com.ismartv.service.ISTVChannel;
import com.ismartv.service.ISTVError;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;

import com.ismartv.ui.ISTVVodActivity;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;

import com.ismartv.ui.R;
import android.util.Log;
import android.content.Context;
import android.database.Cursor;

public class ISTVDoc{
	private static final String TAG="ISTVDoc";

	private ISTVClient client;
	private Callback cb;
	private ISTVVodActivity activity;
	protected ISTVErrorSignal errorSignal;
	protected ISTVLoginSignal loginSignal;

	public interface Callback{
		public void onGotResource(ISTVResource res);
		public void onUpdate();
	}

	public ISTVDoc(){
		client = new ISTVClient(){
			public void onConnected(){
			}
		};
		client.connect();
		client.setDoc(this);
	}
	
	public void activeFail(int ty){
		Log.d(TAG, "--------------activeFail");
		onGotResource(new ISTVResource(ISTVVodActivity.RES_STR_ACTIVE_ERROR, ty, ""));
	}

	public boolean isNetConnected(){
		boolean ret=false;

		try{
			if(activity!=null){
				ConnectivityManager cManager = (ConnectivityManager)activity.getSystemService(Context.CONNECTIVITY_SERVICE); 
				NetworkInfo info = cManager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
				if (info != null && (info.getState()==NetworkInfo.State.CONNECTED)){
					ret = true;
				}
				if(!ret){
					info = cManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
					if (info != null && (info.getState()==NetworkInfo.State.CONNECTED)){
						ret = true;
					}
				}
			}
		}catch(Exception e){
		}

		return ret;
	}

	public void onCreate(){
		errorSignal = new ISTVErrorSignal(getClient()){
			public void onSignal(){
				String msg="";
				int type=0;

				/*if(!isNetConnected()){
					msg = activity.getResources().getString(R.string.vod_net_broken_error);
					type = ISTVVodActivity.DIALOG_NET_BROKEN;
				}else*/{
					msg = activity.getResources().getString(R.string.vod_get_data_error);
					type = ISTVVodActivity.DIALOG_CANNOT_GET_DATA;
				}

				onGotResource(new ISTVResource(ISTVVodActivity.RES_STR_ERROR, type, msg));
			}
		};
	}

	public boolean needlogin(){
		return client.getEpg().needLogin();
	}

	public void login(String user, String passwd){
		if(loginSignal==null){
			loginSignal = new ISTVLoginSignal(getClient()){
				public void onSignal(){
					onGotResource(new ISTVResource(ISTVVodActivity.RES_BOOL_LOGIN, 0, isOK()));
					onGotResource(new ISTVResource(ISTVVodActivity.RES_STR_LOGIN_ERROR, 0, getErrorInfo()));
				}
			};
		}

		if(activity!=null){
			loginSignal.login(activity, user, passwd);
		}
	}

	public void registerCallback(Callback cb){
		this.cb = cb;
	}

	public void registerActivity(ISTVVodActivity act){
		this.activity = act;
		client.checkEndSignals();
	}
	
	static final String AUTHORITY = "com.joysee.launcher.settings";
	static final String TABLE_APPMENU = "appmenu";
	static final String PARAMETER_NOTIFY = "notify";
	public static final Uri CONTENT_URI = Uri.parse("content://" +AUTHORITY + "/" + TABLE_APPMENU + "?" + PARAMETER_NOTIFY + "=true");
	private ContentResolver cr;
	
	public void updateLuanchMenu(Collection<ISTVChannel> channels){
        Log.d(TAG, "11111111111updateLuanchMenu");
        cr = activity.getContentResolver();
        String oldIDs[]=new String[5];
        String oldNames[]=new String[5];
        int oldIds[] = new int[5];

        int num=0;
        int parentId=-1;
        Cursor cursor = cr.query(CONTENT_URI, new String[]{"_id"}, "appName=?", new String[]{"在线视频"}, null);
        if (cursor != null) {
            if (cursor.moveToNext()) {
                parentId = cursor.getInt(0);
                if (parentId <= 0)
                    return;
                Cursor subMenu = cr.query(CONTENT_URI, new String[] { "_id","appName","intent" }, "parentItem=?",
                        new String[] { "" + parentId }, "itemOrder");
                if (subMenu != null) {
                    boolean temp=true;
                    while (subMenu.moveToNext()&&num<5) {
                        if(temp){
                            temp=false;
                            continue;
                        }
                        oldIds[num] = subMenu.getInt(0);
                        oldNames[num]=subMenu.getString(1);
                        Intent intent=null;
                        try{
                            intent = Intent.parseUri(subMenu.getString(2), 0);
                        }catch(java.net.URISyntaxException e){
                        }
                        String tempOld=intent.getStringExtra("chan_id");
                        oldIDs[num]=tempOld;
                        num++;
                    }
                }
            }
        }
        num=0;
        for(ISTVChannel chan : channels){
            Log.d(TAG, "chan.channel="+chan.channel+";chan.name="+chan.name+";old.channel="+oldIDs[num]+";old.name="+oldNames[num]);
            if(chan.channel!=null&&chan.name!=null){
                if(!chan.channel.equals(oldIDs[num])||!chan.name.equals(oldNames[num])){
                    ContentValues value = new ContentValues();
                    value.put("appName", chan.name);
                    final Intent intent = gotoMovieList(chan.channel,chan.name);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    value.put("intent", intent.toURI());
                    String where =" parentItem=" + parentId + " and _id=" + oldIds[num];
                    Log.d(TAG, "----update----appname="+oldNames[num]+"; chan.name="+chan.name);
                    try{
                        cr.update(CONTENT_URI, value, where, null);                        
                    }catch(Exception e){
                        Log.d(TAG, "----------update error CONTENT_URI="+CONTENT_URI);
                    }
                }
            }
            num++;
            if(num>4)
                return;
        }

    }
	
	 public static Intent gotoMovieList(String channelID, String channelName) {
         Bundle bundle = new Bundle();
         bundle.putString("chan_id", channelID);
         if (channelName != null)
                 bundle.putString("chan_name", channelName);

         Intent intent = new Intent();
         if ("$histories".equals(channelID))
                 intent.setClassName("com.ismartv.ui", "com.ismartv.ui.ISTVVodHistory");
         else if ("$bookmarks".equals(channelID))
                 intent.setClassName("com.ismartv.ui", "com.ismartv.ui.ISTVVodBookmark");
         else
                 intent.setClassName("com.ismartv.ui", "com.ismartv.ui.ISTVVodItemList");
         intent.putExtras(bundle);

         return intent;
 }

	public void reload(){
		client.reload();
	}

	public void reloadError(){
		client.reloadError();
	}

	final protected void onGotResource(ISTVResource res){
		if(cb!=null)
			cb.onGotResource(res);
	}

	final protected void onUpdate(){
		if(cb!=null)
			cb.onUpdate();
	}

	ISTVClient getClient(){
		return client;
	}

	public void dispose(){
		client.disconnect();
	}
}
