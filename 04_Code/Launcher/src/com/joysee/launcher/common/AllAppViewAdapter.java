package com.joysee.launcher.common;

import java.util.ArrayList;

import com.joysee.launcher.activity.Launcher;
import com.joysee.launcher.activity.R;
import com.joysee.launcher.utils.FastBitmapDrawable;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;

public class AllAppViewAdapter extends BaseAdapter {
	
	private Context mContext;
	private ArrayList<ApplicationInfo> mApps;

	public AllAppViewAdapter(Context context,ArrayList<ApplicationInfo> apps){
		this.mContext = context;
		this.mApps = apps;
	}
	
	@Override
	public int getCount() {
		return mApps.size();
	}

	@Override
	public Object getItem(int position) {
		return mApps.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ApplicationItem item = (ApplicationItem) LayoutInflater.from(mContext).inflate(R.layout.application_item, null);
		
		item.mIcon.setBackgroundDrawable(new FastBitmapDrawable(mApps.get(position).getIcon()));
		item.mTitle.setText(mApps.get(position).getTitle());
		item.setTag(mApps.get(position).getIntent());
		
		
		return item;
	}

}
