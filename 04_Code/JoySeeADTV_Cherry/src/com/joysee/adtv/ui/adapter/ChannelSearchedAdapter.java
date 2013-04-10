package com.joysee.adtv.ui.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.joysee.adtv.R;
import com.joysee.adtv.common.DefaultParameter;
import com.joysee.adtv.common.DefaultParameter.ServiceType;

public class ChannelSearchedAdapter extends BaseAdapter {

	private List<String> channelList;
	private String mFrequency;
	private List<Integer> typeList;
	private LayoutInflater mInflater;

	public ChannelSearchedAdapter(Context context, LayoutInflater inflater) {
		channelList = new ArrayList<String>();
		typeList = new ArrayList<Integer>();
		mInflater = inflater;
	}

	public int getCount() {
		return channelList.size();
	}

	public Object getItem(int position) {

		if (channelList != null && channelList.size() > 0) {
			return channelList.get(position);
		}

		return null;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		convertView = mInflater.inflate(R.layout.search_fast_search_list_item,
				null);
		if (position % 2 == 0) {
			convertView.setBackgroundResource(R.color.search_channel_list_transparent);
		}else{
			convertView.setBackgroundResource(R.color.search_channel_list_color);
		}
		TextView number = (TextView) convertView
				.findViewById(R.id.search_program_num);
		TextView name = (TextView) convertView
				.findViewById(R.id.search_program_name);
		TextView type = (TextView) convertView
				.findViewById(R.id.search_program_type);
		TextView frequency = (TextView) convertView
				.findViewById(R.id.search_program_fre);
		// 显示频道号
		String s = String.format(DefaultParameter.CHANNEL_NUMBER_FORMAT,
				position + 1);
		number.setText("" + s);
		// 显示频道名称
		String channelName = (String) getItem(position);
		if (channelName != null) {
			name.setText(channelName);
		}
		// 显示频道类型
		if (typeList.get(position) == ServiceType.TV) {
			type.setText(R.string.search_channel_list_tv);
		} else if (typeList.get(position) == ServiceType.BC) {
			type.setText(R.string.search_channel_list_bc);
		}
		// 显示频道所在频率
		frequency.setText(mFrequency);
		return convertView;
	}

	public void clear() {
		channelList.clear();
		typeList.clear();
	}

	public void add(String channelName, int serviceType, String frequency) {
		channelList.add(channelName);
		typeList.add(serviceType);
		this.mFrequency = frequency;
	}
}
