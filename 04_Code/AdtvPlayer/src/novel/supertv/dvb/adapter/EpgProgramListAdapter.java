
package novel.supertv.dvb.adapter;

import novel.supertv.dvb.R;
import novel.supertv.dvb.jni.struct.tagEpgEvent;
import novel.supertv.dvb.provider.Channel;
import novel.supertv.dvb.utils.DateFormatUtil;
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
    private tagEpgEvent[] epgEvents;
    private LayoutInflater inflater;

    public void setEpgEvents(tagEpgEvent[] epgEvents) {
        this.epgEvents = epgEvents;
    }

    public EpgProgramListAdapter(Context context, LayoutInflater inflater) {
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
        return epgEvents.length;
    }

    @Override
    public Object getItem(int position) {
        return epgEvents[position];
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
                Channel.TableReservesColumns.STARTTIME+"=? and "+Channel.TableReservesColumns.PROGRAMNAME+"=?",
                new String[]{""+epgEvents[position].getStartTime(),""+epgEvents[position].getProgramName()}, null);
        if(query.getCount() > 0){
            programClockImage.setVisibility(View.VISIBLE);
        }
        programName.setText(epgEvents[position].getProgramName());
        programStartTime.setText(DateFormatUtil.getTimeFromLong(epgEvents[position].getStartTime()*1000));
        return convertView;
    }

}
