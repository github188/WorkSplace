package com.joysee.launcher.common;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.joysee.launcher.activity.R;
import com.joysee.launcher.utils.LauncherLog;

public class CoverflowAdapter extends BaseAdapter {
	
	private static final String TAG = "com.joysee.launcher.common.CoverflowAdapter";
	
	private Context mContext;
	
	private ArrayList<Long> ids = new ArrayList<Long>();
	private ArrayList<Bitmap> icons = new ArrayList<Bitmap>();
	private ArrayList<Bitmap> iconFocus = new ArrayList<Bitmap>();
	private ArrayList<String> appName = new ArrayList<String>();
	private ArrayList<Intent> intents = new ArrayList<Intent>();
	
	private HashMap<Long, FolderInfo> mFolders;
	
	public CoverflowAdapter(Context context) {
		super();
		mContext = context;
	}

	@Override
	public int getCount() {
		return Integer.MAX_VALUE;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LauncherLog.log_D(TAG, "----View getView   position = ---" + position);
		if(position >= mFolders.size()){
			position = position % mFolders.size();
		}else if(position < 0){
			position = mFolders.size() - (Math.abs(position) % mFolders.size());
		}
		LauncherLog.log_D(TAG, "----View getView   position = ---" + position);
		CoverflowItem item = (CoverflowItem) LayoutInflater.from(mContext).inflate(R.layout.coverflow_item, null);
		ImageView image = item.getImage();
		BitmapDrawable bg = new BitmapDrawable(icons.get(position));
		image.setBackgroundDrawable(bg);
		item.getItemTitle().setText(appName.get(position));
		item.appIntent = intents.get(position);
		
		item.setTag(ids.get(position));
		item.setImageRes(icons.get(position));
		item.setSelectImageRes(iconFocus.get(position));
		
		
		return item;
	}
	
	public Context getmContext() {
		return mContext;
	}

	public void setmContext(Context mContext) {
		this.mContext = mContext;
	}

	public ArrayList<Bitmap> getIcons() {
		return icons;
	}

	public void setIcons(ArrayList<Bitmap> icons) {
		this.icons = icons;
	}

	public ArrayList<Bitmap> getIconFocus() {
		return iconFocus;
	}

	public void setIconFocus(ArrayList<Bitmap> iconFocus) {
		this.iconFocus = iconFocus;
	}

	public ArrayList<String> getAppName() {
		return appName;
	}

	public void setAppName(ArrayList<String> appName) {
		this.appName = appName;
	}

	public HashMap<Long, FolderInfo> getFolders() {
		return mFolders;
	}

	public void setFolders(HashMap<Long, FolderInfo> mFolders) {
		this.mFolders = mFolders;
		ids.clear();
		icons.clear();
		iconFocus.clear();
		appName.clear();
		intents.clear();
		for(FolderInfo info : mFolders.values()){
			ids.add(info.getId());
			icons.add(info.getIcon());
			iconFocus.add(info.getIconFocus());
			appName.add(info.getTitle().toString());
			intents.add(info.getIntent());
			LauncherLog.log_D(TAG, "setFolders   " + info.getId());
			LauncherLog.log_D(TAG, "setFolders   " + info.getTitle().toString());
		}
	}
}
