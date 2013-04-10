package com.joysee.adtv.ui.adapter;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.joysee.adtv.R;
/**
 * CA 授权信息Adapter
 * @author wuhao
 */
public class SettingAuthAdapter extends BaseAdapter {

    private List<Map<String,String>> mList;
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private TextView  mTextNumber;
    private TextView mTextEnd;
    private TextView mTextRecord;

    public SettingAuthAdapter(Context context,List<Map<String,String>> list) {
        this.mContext = context;
        this.mList = list;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            mLayoutInflater = LayoutInflater.from(mContext);
            convertView = mLayoutInflater.inflate(R.layout.ca_authoriseinfo_item, null);
        }
        mTextNumber = (TextView) convertView.findViewById(R.id.config_auth_number);
        mTextEnd = (TextView) convertView.findViewById(R.id.config_auth_end);
        mTextRecord = (TextView) convertView.findViewById(R.id.config_auth_record);
        
        mTextNumber.setText(mList.get(position).get("number"));
        mTextEnd.setText(mList.get(position).get("time"));
        mTextRecord.setText(mList.get(position).get("record"));
        
        if(position%2 == 0)
        	convertView.setBackgroundDrawable(null);
        else
        	convertView.setBackgroundResource(R.color.search_channel_list_color);
        
        return convertView;
    }

}
