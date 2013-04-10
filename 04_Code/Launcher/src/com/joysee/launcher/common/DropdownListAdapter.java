package com.joysee.launcher.common;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.joysee.launcher.activity.R;
import com.joysee.launcher.utils.LauncherLog;

public class DropdownListAdapter extends BaseAdapter {
	
	private static final String TAG = "com.joysee.launcher.common.DropdownListAdapter";

	private Context mContext;
	private ArrayList<ShortcutInfo> mApps = new ArrayList<ShortcutInfo>();

	public DropdownListAdapter(Context context, ArrayList<ShortcutInfo> list) {
		this.mContext = context;
		this.mApps = list;
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
		
		DropdownListItem view = new DropdownListItem(mContext);
		view.setTextSize(40);
		view.setTextColor(mContext.getResources().getColor(R.color.launcher_coverflow_dropdown_listitem_textcolor));
		view.setGravity(Gravity.CENTER);
		
		
		ShortcutInfo app = mApps.get(position);
		view.mParentIndex = app.getParentIndex();
		
		view.mIntent = app.getIntent();
		view.setText(app.getTitle());
		view.setLayoutParams(new AbsListView.LayoutParams(319,96));
		
		return view;
	}

	public ArrayList<ShortcutInfo> getApps() {
		return mApps;
	}

	public void setApps(ArrayList<ShortcutInfo> apps) {
		this.mApps = apps;
	}
	
	

}
