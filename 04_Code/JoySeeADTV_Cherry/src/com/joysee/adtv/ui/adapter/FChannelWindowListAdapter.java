package com.joysee.adtv.ui.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.joysee.adtv.R;
import com.joysee.adtv.common.DefaultParameter.FavoriteFlag;
import com.joysee.adtv.controller.ViewController;
import com.joysee.adtv.logic.bean.DvbService;

public class FChannelWindowListAdapter extends BaseAdapter {

	private LayoutInflater mLayoutInflater;
//	private int mCurrentChNum = -1;
	
	private ViewController mViewController;
	private ArrayList<Integer> mFIndexs;

	public FChannelWindowListAdapter(Context context, ViewController controller) {
		this.mViewController = controller;
		mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		mCurrentChNum = mViewController.getChannelNum();
		mFIndexs = mViewController.getFavouriteIndex();
	}
	
	public void setData(ArrayList<Integer> indexs){
		if (indexs != null)
			this.mFIndexs = indexs;
//		mCurrentChNum = mViewController.getChannelNum();
	}

	@Override
	public int getCount() {
		return mFIndexs.size();
	}

	@Override
	public Object getItem(int position) {
		return mViewController.getChannelByChannelIndex(mFIndexs.get(mFIndexs.size()-1-position));
	}

	@Override
	public long getItemId(int position) {
		return -1;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		Log.d("songwenxuan","get view position = " + position);

		ChannelListViewHolder viewHolder = null;
		if (convertView == null) {
			convertView = mLayoutInflater.inflate(R.layout.menu_favorite_listitem, null);
			viewHolder = new ChannelListViewHolder();
			viewHolder.mChannelFavourite = (ImageView) convertView
					.findViewById(R.id.dvb_channelwindow_listitem_cfavourite);
			viewHolder.mChannelNum = (TextView) convertView.findViewById(R.id.dvb_channelwindow_listitem_cnum);
			viewHolder.mChannelName = (TextView) convertView.findViewById(R.id.dvb_channelwindow_listitem_cname);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ChannelListViewHolder) convertView.getTag();
		}
		DvbService item = mViewController.getChannelByChannelIndex(mFIndexs.get(mFIndexs.size()-1-position));
		if (item.getLogicChNumber() > 0) {
//			if(item.getLogicChNumber() == mCurrentChNum)
//				viewHolder.mChannelFavourite.setImageResource(R.drawable.icon_faovrite_play_now);
//			else 
			viewHolder.mChannelFavourite.setImageResource(R.drawable.menu_fav_icon);
			viewHolder.mChannelFavourite.setTag(1);
			viewHolder.mChannelFavourite.setVisibility(item.getFavorite() == FavoriteFlag.FAVORITE_YES? View.VISIBLE : View.INVISIBLE);
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
