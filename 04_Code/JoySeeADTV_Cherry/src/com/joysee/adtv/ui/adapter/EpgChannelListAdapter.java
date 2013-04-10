
package com.joysee.adtv.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.joysee.adtv.R;
import com.joysee.adtv.controller.ViewController;
import com.joysee.adtv.logic.bean.DvbService;

public class EpgChannelListAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private ViewController mViewController;
    public EpgChannelListAdapter(LayoutInflater inflater,ViewController viewController) {
        this.inflater = inflater;
        this.mViewController = viewController;
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
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.epg_channel_list_item, null);
        convertView.setTag(position);
        
        TextView numTextView = (TextView) convertView.findViewById(R.id.channel_list_num);
        TextView nameTextView = (TextView) convertView.findViewById(R.id.channel_list_name);
        
        DvbService service = mViewController.getChannelByListIndex(position);
        int channelNum = service.getLogicChNumber();
        
        numTextView.setText(""+channelNum);
        String channelName = service.getChannelName();
        nameTextView.setText(channelName);
        
        return convertView;
    }

}
