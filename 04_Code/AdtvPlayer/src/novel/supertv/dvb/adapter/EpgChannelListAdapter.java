
package novel.supertv.dvb.adapter;

import novel.supertv.dvb.R;
import novel.supertv.dvb.provider.Channel;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class EpgChannelListAdapter extends BaseAdapter {
    private Cursor cursor;
    private LayoutInflater inflater;

    public EpgChannelListAdapter(Cursor cursor, LayoutInflater inflater) {
        this.cursor = cursor;
        this.inflater = inflater;
    }

    public void setCursor(Cursor cursor) {
        this.cursor = cursor;
    }

    @Override
    public int getCount() {
        return cursor.getCount();
    }

    @Override
    public Object getItem(int position) {
        cursor.moveToPosition(position);
        return cursor;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = inflater.inflate(R.layout.epg_channel_list_item, null);
        view.setTag(position);
        TextView numTextView = (TextView) view.findViewById(R.id.channel_list_num);
        TextView nameTextView = (TextView) view.findViewById(R.id.channel_list_name);
        cursor.moveToPosition(position);
        int num = cursor.getInt(cursor.getColumnIndex(Channel.TableChannelsColumns.LOGICCHNUMBER));
        numTextView.setText("" + num);
        String name = cursor.getString(cursor.getColumnIndex(Channel.TableChannelsColumns.SERVICENAME));
        nameTextView.setText(name);
        return view;
    }

}
