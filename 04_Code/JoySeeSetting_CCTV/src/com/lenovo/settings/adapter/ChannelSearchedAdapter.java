
package com.lenovo.settings.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lenovo.settings.R;
import com.lenovo.settings.Util.DefaultParameter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ChannelSearchedAdapter extends BaseAdapter {

    // private static final DvbLog log = new
    // DvbLog("cn.vc.dvb.app.adapter.ChannelSearchedAdapter",DvbLog.DebugType.D);

    private List<String> channelList;
    private String mFrequency;
    /**
     * 用于存放频道类型的list
     */
    private List<Integer> typeList;

    /**
     * 用来映射频道类型和图片资源的对应关系
     */
    private Map<Integer, Integer> srcMap = new HashMap<Integer, Integer>();

    private LayoutInflater mInflater;

    public ChannelSearchedAdapter(Context context, LayoutInflater inflater) {

        channelList = new ArrayList<String>();
        typeList = new ArrayList<Integer>();
        mInflater = inflater;

        initSourceMap();
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
        convertView = mInflater.inflate(R.layout.search_auto_list_item, null);
        TextView number = (TextView) convertView
                .findViewById(R.id.search_program_num);
        TextView name = (TextView) convertView
                .findViewById(R.id.search_program_name);
        TextView type = (TextView) convertView
                .findViewById(R.id.search_program_type);
        TextView frequency = (TextView) convertView
                .findViewById(R.id.search_program_fre);
        // ImageView imageView = (ImageView)
        // convertView.findViewById(R.id.search_image);

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
        if(typeList.get(position)==1){
            type.setText(R.string.search_channel_list_tv);
        }else {
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

    /**
     * 把资源使用Map管理,这里面的对应关系是固定的，可扩充的。
     */
    private void initSourceMap() {


    }
}
