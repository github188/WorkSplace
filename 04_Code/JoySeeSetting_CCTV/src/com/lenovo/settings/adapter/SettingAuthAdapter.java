package com.lenovo.settings.adapter;

import java.util.List;
import java.util.Map;

import com.lenovo.settings.R;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

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
            convertView = mLayoutInflater.inflate(R.layout.config_authorise_message_item, null);
        }
//        if(position%2==0){
//            convertView.setBackgroundColor(Color.GRAY);
//        }else{
//            convertView.setBackgroundColor(Color.LTGRAY);
//        }
        mTextNumber = (TextView) convertView.findViewById(R.id.config_auth_number);
        mTextEnd = (TextView) convertView.findViewById(R.id.config_auth_end);
        mTextRecord = (TextView) convertView.findViewById(R.id.config_auth_record);
        
        mTextNumber.setText(mList.get(position).get("number"));
        mTextEnd.setText(mList.get(position).get("time"));
        mTextRecord.setText(mList.get(position).get("record"));
        return convertView;
    }

}
