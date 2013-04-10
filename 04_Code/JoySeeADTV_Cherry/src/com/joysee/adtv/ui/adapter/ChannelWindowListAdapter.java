package com.joysee.adtv.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.joysee.adtv.R;
import com.joysee.adtv.controller.ViewController;
import com.joysee.adtv.logic.bean.DvbService;

public class ChannelWindowListAdapter extends BaseAdapter {

	private LayoutInflater mLayoutInflater;
	private ViewController mViewController;
	
	private int mMaxPosition;
	private int mPosition;
	private boolean mFlag;
	
	public ChannelWindowListAdapter(Context context, ViewController viewController) {
		this.mViewController = viewController;
		mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return mViewController.getTotalChannelSize();
	}

	@Override
	public Object getItem(int position) {
		return mViewController.getChannelByListIndex(position);
	}

	@Override
	public long getItemId(int position) {
		return -1;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ChannelListViewHolder viewHolder = null;
		if (convertView == null) {
			convertView = mLayoutInflater.inflate(R.layout.dvb_channelwindow_listitem, null);
			viewHolder = new ChannelListViewHolder();
			viewHolder.mChannelFavourite = (ImageView) convertView
					.findViewById(R.id.dvb_channelwindow_listitem_cfavourite);
			viewHolder.mChannelNum = (TextView) convertView.findViewById(R.id.dvb_channelwindow_listitem_cnum);
			viewHolder.mChannelName = (TextView) convertView.findViewById(R.id.dvb_channelwindow_listitem_cname);
			convertView.setTag(viewHolder);
			
		} else {
			viewHolder = (ChannelListViewHolder) convertView.getTag();
		}
		
		if(!mFlag){
			mPosition++;
			if(position==0){
				mMaxPosition = mPosition-2;
			}else if(position == mMaxPosition){
				mFlag = true;
				mMaxPosition = -1;
			}
		}else{
			System.out.println(" getView getChannelByIndex "+position);
			DvbService item = mViewController.getChannelByListIndex(position);
			viewHolder.mChannelFavourite.setVisibility(item.getFavorite() == 0 ? View.INVISIBLE : View.VISIBLE);
			viewHolder.mChannelNum.setText(String.valueOf(item.getLogicChNumber()));
			viewHolder.mChannelName.setText(item.getChannelName());
		}
		return convertView;
		
	}
	
	class ChannelListViewHolder {
		ImageView mChannelFavourite;
		TextView mChannelNum;
		TextView mChannelName;
	}

}
