
package com.joysee.adtv.ui.adapter;

import java.util.ArrayList;
import java.util.TimeZone;

import com.joysee.adtv.R;
import com.joysee.adtv.common.DateFormatUtil;
import com.joysee.adtv.db.Channel;
import com.joysee.adtv.logic.SettingManager;
import com.joysee.adtv.logic.bean.EpgEvent;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class EpgProgramListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<EpgEvent> epgEvents;
    private LayoutInflater inflater;
    private int serviceId;
    private int timeZone;
    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public void setEpgEvents(ArrayList<EpgEvent> epgEvents) {
        this.epgEvents = epgEvents;
    }

    public EpgProgramListAdapter(Context context, LayoutInflater inflater) {
        timeZone = TimeZone.getDefault().getRawOffset()/3600/1000;
        this.context = context;
        this.inflater = inflater;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setInflater(LayoutInflater inflater) {
        this.inflater = inflater;
    }

    public EpgProgramListAdapter(LayoutInflater inflater) {
        this.inflater = inflater;
    }

    @Override
    public int getCount() {
        return epgEvents.size();
    }

    @Override
    public Object getItem(int position) {
        return epgEvents.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        
        convertView = inflater.inflate(R.layout.epg_program_list_item, null);
        TextView programStartTime = (TextView) convertView.findViewById(R.id.program_list_time);
        TextView programName = (TextView) convertView.findViewById(R.id.program_list_name);
        ImageView programClockImage = (ImageView) convertView.findViewById(R.id.program_list_reserve_tag);
        programClockImage.setTag(position);
        Cursor query = context.getContentResolver().query(Channel.URI.TABLE_RESERVES,null,
                Channel.TableReservesColumns.STARTTIME+"=? and "+Channel.TableReservesColumns.PROGRAMNAME+"=? and "+Channel.TableReservesColumns.SERVICEID+"=? ",
                new String[]{""+(epgEvents.get(position).getStartTime()),""+epgEvents.get(position).getProgramName(),""+serviceId}, null);
        if(query.getCount() > 0 && getUtcTime() < epgEvents.get(position).getStartTime()){
            programClockImage.setVisibility(View.VISIBLE);
        }
        programName.setText(epgEvents.get(position).getProgramName());
        programStartTime.setText(DateFormatUtil.getTimeFromLong(epgEvents.get(position).getStartTime()*1000+(8-timeZone)*3600*1000)+ "-"
                + DateFormatUtil.getTimeFromLong(epgEvents.get(position)
                        .getEndTime()*1000 + (8-timeZone)*3600*1000));
        return convertView;
    }
    private long getUtcTime() {
        SettingManager mSettingManager = SettingManager.getSettingManager();
        String utcTimeStr = mSettingManager.nativeGetTimeFromTs();
        String[] utcTime = utcTimeStr.split(":");
        long currentTimeSec = Long.valueOf(utcTime[0]);
        return currentTimeSec;
    }
}
