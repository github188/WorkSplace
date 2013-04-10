package com.lenovo.settings;

import java.util.Locale;

import android.app.ActivityManagerNative;
import android.app.Fragment;
import android.app.IActivityManager;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class LanguageSettings extends Fragment {
	protected static final String TAG = "LanguageSettings";
	private View mView;
	private String[] mLanStr,mLanValues;
	private ListView mListView;
	protected boolean mListEnd = false;
	protected boolean mEnableList = false;
	private static int mIndexLang;
	private ListViewAdapter mListViewAdapter;
	private static final int SET_DEFAULT_LANGUAGE_SUCC = 1;
	private static final int SET_DEFAULT_LANGUAGE_FAIL = 2;

    private boolean DEBUG = true;

    public void Log(String str) {
        if (DEBUG) {
            Log.d(TAG, "---------" +str);
        }
    }
	
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
	

			switch(msg.what){
			case SET_DEFAULT_LANGUAGE_SUCC:
				mIndexLang = msg.arg2;
				setLocal(mIndexLang);
				mListViewAdapter.notifyDataSetChanged();
				Settings.System.putInt(getActivity().getContentResolver(), "curChoice",1);
				break;
			}
		}
		
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		
		Settings.System.putInt(getActivity().getContentResolver(), "curChoice",0);
		
		mLanStr = getResources().getStringArray(R.array.lanuage_array);
		mLanValues = getResources().getStringArray(R.array.lanuage_code);
		String code = Locale.getDefault().getLanguage()+"_"+Locale.getDefault().getCountry();
		mIndexLang = findIndexOfEntry(code, mLanValues);
		Log(" language and Country = " + code + " mIndexLang = " + mIndexLang);
		
		mView = inflater.inflate(R.layout.language_settings, container, false);
		mListView = (ListView) mView.findViewById(R.id.laguage_list);
		mListViewAdapter = new ListViewAdapter(getActivity(), mLanStr);
		mListView.setAdapter(mListViewAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position,
					long arg3) {
				if(position!=mIndexLang){
					Message msg = new Message();
					msg.what = SET_DEFAULT_LANGUAGE_SUCC;
					msg.arg2 = position;
					mHandler.sendMessage(msg);
					ToastView totast = new ToastView(getActivity(),getString(R.string.language_succ),2000,false);
					totast.showTotast();
				}
			}
		});
		
		LenovoSettingsActivity.setTitleFocus(false);
		mListView.requestFocus();
		mListView.setSelection(mIndexLang);
		mListView.setSelected(false);
		return mView;
	}

	
	@Override
	public void onStop() {

		super.onStop();
	}


	public class ListViewAdapter extends BaseAdapter {
	   	 
    	//private Context mContext;
		public String[]  mArray;
		private LayoutInflater mLayoutInflater;
		private int selectedPosition = -1;  
		
		public void setSelectedPosition(int position) {   
			selectedPosition = position;   
		}

		public ListViewAdapter(Context context,String[] array) { 
			Log.d(TAG,"array size = "+array.length);
            mArray = array; 
            mLayoutInflater = LayoutInflater.from(context); 
        } 

		@Override
		public int getCount() {
	
			return mArray.length;
		}

		@Override
		public Object getItem(int position) {
	
			return mArray[position];
		}

		@Override
		public long getItemId(int position) {
	
			return position;
		}
 
		@Override
		public boolean isEnabled(int position) {
			if(position == 0)
				return true;
			else
				return false;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
	
			ViewHolder holder = null;
			if (convertView == null) { 
				holder = new ViewHolder();
				convertView = mLayoutInflater.inflate(R.layout.language_list, null); 
				holder.textView = (TextView) convertView.findViewById(R.id.languageArray);
				holder.imageView = (ImageView) convertView.findViewById(R.id.languageImg);
				holder.layout = (LinearLayout) convertView.findViewById(R.id.languageLayout);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			holder.textView.setText(mArray[position]);
			if(mIndexLang == position){
				holder.textView.setAlpha(1.0f);
				holder.imageView.setVisibility(View.VISIBLE);
				holder.imageView.setImageResource(R.drawable.sel_icon);
			}else{
				holder.textView.setAlpha(0.3f);
				holder.imageView.setVisibility(View.INVISIBLE);
			}
			
			return convertView; 
		}

		public class ViewHolder { 
		    public TextView textView;
		    public ImageView imageView;
		    public LinearLayout layout;
		} 
    }
	
	private int findIndexOfEntry(String value, String[] entry) {
		if (value != null && entry != null) {
			for (int i = entry.length - 1; i >= 0; i--) {
				if (entry[i].equals(value)) {
					return i;
				}
			}
		}
		return 0;
	}
	
	public static void updateLocale(Locale locale) {
		try {
			IActivityManager am = ActivityManagerNative.getDefault();
			Configuration config = am.getConfiguration();
			config.locale = locale;
			config.userSetLocale = true;
			am.updateConfiguration(config);
			BackupManager.dataChanged("com.android.providers.settings");
		} catch (RemoteException e) {
			// Intentionally left blank
		}
	}
	private void setLocal(int position){
		String s = mLanValues[position];
		Log.i("clei", "position = " + position + "s= " + s);
		Locale local = new Locale(s.substring(0, 2), s.substring(3, 5));
		updateLocale(local);
	}
	
	
}