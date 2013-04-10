package com.lenovo.settings;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AudioOuputSettings extends Fragment {
	protected static final String TAG = "AudioOuputSettings";
	private View mView;
	private InputMethodManager mImm;
	List<InputMethodInfo> inputMethodInfos;
	private ArrayList<String> allItems = new ArrayList<String>();
	private TextView mSpinner;
	private Button mInputSetting;
	private ListView mListView;
	private RelativeLayout mListLayout;
	private ArrayAdapter<String> mAdapter;
	protected boolean mListEnd = false;
	protected boolean mEnableList = false;
	private String[] audio_name;
	private String[] audio_array;
	private int mIndexAudio = 0;
	private int mDropMenuSize = 2;
	private ListViewAdapter mListViewAdapter;
	String[] mArray;
	private static final int SET_AUDIO_SUCC = 1;
	private static final int SET_DEFAULT_FAIL = 2;
	

	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
	

			switch(msg.what){
			case SET_AUDIO_SUCC:
				int position = msg.arg2;
				mIndexAudio = position;
				audio_array = getResources().getStringArray(R.array.audio_array);
				audio_name = getResources().getStringArray(R.array.audio_name);
				mListViewAdapter.notifyDataSetChanged();
				break;
			}
		}
		
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		
		audio_array = getResources().getStringArray(R.array.audio_array);
		audio_name = getResources().getStringArray(R.array.audio_name);
		
		mIndexAudio = getOuputMode();
		
		mView = inflater.inflate(R.layout.audio_output_settings, container, false);
		mListView = (ListView) mView.findViewById(R.id.output_list);
		mListViewAdapter = new ListViewAdapter(getActivity(), audio_name);
		mListView.setAdapter(mListViewAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position,
					long arg3) {
                if (position != mIndexAudio) {
                    mIndexAudio = position;
                    setAudioOutputMode(position);
                    Message msg = new Message();
                    msg.what = SET_AUDIO_SUCC;
                    msg.arg2 = position;
                    mHandler.sendMessage(msg);
                    NetworkWireless.showToast(getActivity(), R.string.output_succ);
                    // ToastView totast = new
                    // ToastView(getActivity(),getString(R.string.output_succ),2000,false);
//                    totast.showTotast();
                }
			}
		});
		LenovoSettingsActivity.setTitleFocus(false);
		mListView.requestFocus();
		mListView.setSelection(mIndexAudio);
		return mView;
	}

	public class ListViewAdapter extends BaseAdapter {
	   	 
		public String[]  mArray;
		private LayoutInflater mLayoutInflater;
		private int selectedPosition = -1;  
		
		public void setSelectedPosition(int position) {   
			selectedPosition = position;   
		}

		public ListViewAdapter(Context context,String[] array) { 
            mArray = array; 
            mLayoutInflater = LayoutInflater.from(context); 
        } 

		@Override
		public int getCount() {
	
			return mArray.length;
		}

		@Override
		public Object getItem(int position) {
	
			return mArray.length;
		}

		@Override
		public long getItemId(int position) {
	
			return position;
		}
 
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
	
			ViewHolder holder = null;
			if (convertView == null) { 
				holder = new ViewHolder();
				convertView = mLayoutInflater.inflate(R.layout.audio_output_list, null); 
				holder.textView = (TextView) convertView.findViewById(R.id.outputArray);
				holder.imageView = (ImageView) convertView.findViewById(R.id.outputImg);
				holder.layout = (LinearLayout) convertView.findViewById(R.id.outputLayout);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			holder.textView.setText(mArray[position]);
			
			if(mIndexAudio == position){
				holder.imageView.setImageResource(R.drawable.sel_icon);
				holder.imageView.setVisibility(View.VISIBLE);
			}else{
				holder.imageView.setVisibility(View.INVISIBLE);
				//holder.imageView.setImageResource(R.drawable.ic_launcher_settings);
			}
			
//			if(selectedPosition == position){
//				if(convertView.hasFocus()){
//					holder.layout.setBackgroundResource(R.drawable.tab_none); 					
//				}else{
//					holder.layout.setBackgroundResource(R.drawable.tab_selected);
//				}
//			}else{
//				holder.layout.setBackgroundResource(R.drawable.tab_none); 
//			}
			return convertView; 
		}

		public class ViewHolder { 
		    public TextView textView;
		    public ImageView imageView;
		    public LinearLayout layout;
		} 
    }
	
	public void setAudioOutputMode(int value){
        String val = (value == 0) ? "1":"0";
        try {
            File f = new File("/sys/class/audiodsp/digital_raw");
            FileWriter fw = new FileWriter(f);
            BufferedWriter buf = new BufferedWriter(fw);
            buf.write(val);
            buf.close();
	    } catch (IOException e){
	            e.printStackTrace();
	    }
        Settings.System.putInt(getActivity().getContentResolver(), "curAudio", value);
    }
	
    public int getOuputMode() {
        int curadio = 0;
        try {
            curadio = Settings.System.getInt(getActivity().getContentResolver(), "curAudio");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return curadio;
    }
}