
package novel.supertv.dvb.adapter;

import novel.supertv.dvb.R;
import novel.supertv.dvb.provider.Channel;
import novel.supertv.dvb.utils.DateFormatUtil;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ProgramReservesListAdapter extends BaseAdapter {
    private Cursor cursor;
    private LayoutInflater inflater;
    
    public ProgramReservesListAdapter(Cursor cursor, LayoutInflater inflater) {
        this.cursor = cursor;
        this.inflater = inflater;
    }

    @Override
    public int getCount() {
        return cursor.getCount();
    }

    @Override
    public Object getItem(int position) {
        return cursor;
    }

    public Cursor getCursor() {
        return cursor;
    }

    public void setCursor(Cursor cursor) {
        this.cursor = cursor;
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
        
        convertView = inflater.inflate(R.layout.program_reserves_list_item, null);
        TextView dateTextView =(TextView) convertView.findViewById(R.id.program_reserves_date_textview);
        TextView timeTextView =(TextView) convertView.findViewById(R.id.program_reserves_time_textview);
        TextView programNameTextView = (TextView) convertView.findViewById(R.id.program_reserves_proram_name_textview);
        TextView channelNameTextView = (TextView) convertView.findViewById(R.id.program_reserves_channel_name_textview);
        ImageView reserveImage = (ImageView) convertView.findViewById(R.id.program_reserves_clock_image);
        reserveImage.setTag(position);
        
        cursor.moveToPosition(position);
        long startTime =(long)cursor.getInt(cursor.getColumnIndex(Channel.TableReservesColumns.STARTTIME));
        String programName = cursor.getString(cursor.getColumnIndex(Channel.TableReservesColumns.PROGRAMNAME));
        String channelName = cursor.getString(cursor.getColumnIndex(Channel.TableReservesColumns.CHANNELNAME));
        String date = DateFormatUtil.getDateFromMillis(startTime*1000);
        String time = DateFormatUtil.getTimeFromMillis(startTime*1000);
        
        dateTextView.setText(date);
        timeTextView.setText(time);
        programNameTextView.setText(programName);
        channelNameTextView.setText(channelName);
        return convertView;
    }
}
