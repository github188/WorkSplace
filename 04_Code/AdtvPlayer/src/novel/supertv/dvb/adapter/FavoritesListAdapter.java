package novel.supertv.dvb.adapter;

import novel.supertv.dvb.utils.DvbLog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class FavoritesListAdapter extends BaseAdapter {

    private static final DvbLog log = new DvbLog(
            "novel.supertv.dvb.adapter.FavoritesListAdapter",DvbLog.DebugType.D);

    private Context mContext;

    public FavoritesListAdapter(Context context) {
        
        this.mContext = context;
        
    }

    public void setDate(){
        
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        return null;
    }

}
