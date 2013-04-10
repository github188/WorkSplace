
package novel.supertv.dvb.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import novel.supertv.dvb.R;
import novel.supertv.dvb.utils.DefaultParameter;
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

        // if (convertView == null) {
        // convertView = mInflater.inflate(R.layout.search_auto_list_item,
        // null);
        // }

        convertView = mInflater.inflate(R.layout.search_auto_list_item, null);
//        if (position % 2 == 0) {
//            convertView.setBackgroundResource(R.color.aaaa);
//        }
//        else {
//            convertView.setBackgroundResource(R.color.bbbb);
//        }

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

        // type.setText("" + typeList.get(position));
        if(typeList.get(position)==1){
            type.setText(R.string.search_channel_list_tv);
        }else {
            type.setText(R.string.search_channel_list_bc);
        }

        // 显示频道所在频率
        frequency.setText(mFrequency);

        // frequency.setText("642000Hz");

        // 取得对应于频道类型的资源设置给图片视图
        // if(srcMap.containsKey(typeList.get(position))){
        // imageView.setBackgroundResource(srcMap.get(typeList.get(position)));
        // }

        // if(position%2 == 1){
        // convertView.setBackgroundColor(android.R.color.holo_green_light);
        // }else{
        // convertView.setBackgroundColor(android.R.color.holo_red_light);
        // }

        return convertView;
    }

    public void clear() {
        // log.D("clear()");
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

        // srcMap.put(DefaultParameter.ServiceType.digital_radio_sound_service,
        // R.drawable.search_result_broadcast);
        // srcMap.put(DefaultParameter.ServiceType.NVOD_reference_service,
        // R.drawable.search_result_nvod);

    }
}
