
package com.joysee.adtv.ui.adapter;

import java.util.List;
import java.util.TimeZone;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.joysee.adtv.R;
import com.joysee.adtv.common.DateFormatUtil;
import com.joysee.adtv.logic.bean.Program;

public class ProgramReservesListAdapter extends BaseAdapter {
    private List<Program> mProgramList;
    private LayoutInflater inflater;
    private double timeZone;
    
    public ProgramReservesListAdapter(List<Program> programList, LayoutInflater inflater) {
        this.mProgramList = programList;
        this.inflater = inflater;
        timeZone = (double)(TimeZone.getDefault().getRawOffset())/3600/1000;
    }

    @Override
    public int getCount() {
        return mProgramList.size();
    }

    @Override
    public Object getItem(int position) {
        return mProgramList.get(position);
    }

    public List<Program> getmProgramList() {
        return mProgramList;
    }

    public void setmProgramList(List<Program> mProgramList) {
        this.mProgramList = mProgramList;
    }

    public LayoutInflater getInflater() {
        return inflater;
    }

    public void setInflater(LayoutInflater inflater) {
        this.inflater = inflater;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	
    	Log.d("songwenxuan", "getView()  position = " + position);
        
        convertView = inflater.inflate(R.layout.program_reserves_list_item, null);
        TextView dateTextView =(TextView) convertView.findViewById(R.id.program_reserves_date_textview);
        TextView timeTextView =(TextView) convertView.findViewById(R.id.program_reserves_time_textview);
        TextView programNameTextView = (TextView) convertView.findViewById(R.id.program_reserves_proram_name_textview);
        TextView channelNameTextView = (TextView) convertView.findViewById(R.id.program_reserves_channel_name_textview);
        ImageView reserveImage = (ImageView) convertView.findViewById(R.id.program_reserves_clock_image);
        reserveImage.setTag(position);
        
        Program tProgram = mProgramList.get(position);
        long startTime = tProgram.getStartTime();
        String programName = tProgram.getName();
        String channelName = tProgram.getChannelName();
        String date = DateFormatUtil.getDateFromMillis(startTime*1000 + (long)((8-timeZone)*3600*1000));
        String time = DateFormatUtil.getTimeFromMillis(startTime*1000 + (long)((8-timeZone)*3600*1000));
        
        Log.d("songwenxuan","startTime = "+time + "programName = "+programName +"channelName = "+channelName);
        dateTextView.setText(date);
        timeTextView.setText(time);
        programNameTextView.setText(programName);
        channelNameTextView.setText(channelName);
        return convertView;
    }
}
