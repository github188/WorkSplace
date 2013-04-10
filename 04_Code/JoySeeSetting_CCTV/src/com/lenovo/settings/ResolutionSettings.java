package com.lenovo.settings;


import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class ResolutionSettings extends Fragment {
	public static final String TAG = "ResolutionSettings";
	private View mView;
	private ListView mListView;
	private ListViewAdapter mListViewAdapter;
	private static final String STR_OUTPUT_VAR = "ubootenv.var.outputmode";
	private int[] mResolutionImage = {R.drawable.ic_launcher,R.drawable.ic_launcher,R.drawable.ic_launcher};
	private String[] mResolutionArrays;
	private String[] mResolutionArraysMsg;
	private int mIndexEntry;
	private int sel_index;
	private String valOutputmode;
	private final static long set_delay = 15*1000;
	private Handler mProgressHandler;
	private Dialog OutPutSetConfirmDiag=null;
	private LayoutInflater mInflater;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mInflater = inflater;
		mResolutionArrays = getResources().getStringArray(R.array.outputmode_entries);
		mResolutionArraysMsg = getResources().getStringArray(R.array.outputmode_entries_msg);
		
		mView = inflater.inflate(R.layout.resolution_settings, container, false);
		mListView = (ListView)mView.findViewById(R.id.resolution_list);
		
		valOutputmode = SystemProperties.get(STR_OUTPUT_VAR);
		mIndexEntry = findIndexOfEntry(valOutputmode, mResolutionArrays);
		
		mListViewAdapter = new ListViewAdapter(getActivity(), mResolutionArrays, mResolutionArraysMsg);
		mListView.setAdapter(mListViewAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position,
					long arg3) {
				sel_index = position;
				if (mIndexEntry != position) {
					showDispmodeSetMsg();
				}		
			}
		});
		
		
			
		mListView.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View arg0, int keyCode, KeyEvent arg2) {
				if((keyCode == KeyEvent.KEYCODE_DPAD_LEFT) || (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)){ 
						return true;
				}
				return false;
			}
		});
		LenovoSettingsActivity.setTitleFocus(false);
		mListView.requestFocus();
		mListView.setSelection(mIndexEntry);
		return mView;
	}
	
	public class ListViewAdapter extends BaseAdapter {
	   	 
    	//private Context mContext;
		public String[] mArray;
		public String[] mArrayMsg;
		//public int mImage;
		private LayoutInflater mLayoutInflater;
		private int selectedPosition = -1;  
		
		public void setSelectedPosition(int position) {   
			selectedPosition = position;   
		}

		public ListViewAdapter(Context context,String[] array,String[] arrayMsg) { 
			Log.d(TAG,"array size = "+array.length);
    		//mContext = context; 
            mArray = array; 
           // mImage = image;
            mArrayMsg = arrayMsg;
            mLayoutInflater = LayoutInflater.from(context); 
        } 

		@Override
        public int getCount() {
            return 1;
            // return mArray.length;
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
		public View getView(int position, View convertView, ViewGroup parent) {
	
			ViewHolder holder = null;
			if (convertView == null) { 
				holder = new ViewHolder();
				convertView = mLayoutInflater.inflate(R.layout.resolution_list, null); 
				holder.textView = (TextView) convertView.findViewById(R.id.resolutionArray);
				holder.imageView = (ImageView) convertView.findViewById(R.id.resolutionImg);
				holder.textViewMsg = (TextView) convertView.findViewById(R.id.resolutionArrayMsg);
				holder.layout = (LinearLayout) convertView.findViewById(R.id.resolutionLayout);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			holder.textView.setText(mArray[position]);
			holder.textViewMsg.setText(mArrayMsg[position]);
//			holder.textViewMsg.setAlpha(0.3f);
			if(position == mIndexEntry){
				holder.imageView.setImageResource(R.drawable.sel_icon);
				holder.imageView.setVisibility(View.VISIBLE);
			}else{
				holder.imageView.setVisibility(View.INVISIBLE);
				//holder.imageView.setImageResource(R.drawable.ic_launcher_settings);
			}
			
//			if(selectedPosition == position){
//				if(convertView.hasFocus()){
//					holder.textView.setBackgroundResource(R.drawable.tab_none); 					
//				}else{
//					holder.textView.setBackgroundResource(R.drawable.tab_selected);
//				}
//			}else{
//				holder.textView.setBackgroundResource(R.drawable.tab_none); 
//			}
			return convertView; 
		}

		public class ViewHolder { 
		    public TextView textView;
		    public TextView textViewMsg;
		    public ImageView imageView;
		    public LinearLayout layout;
		} 
    }
	
    private class SetconfirmHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            // super.handleMessage(msg);
            // Log.e(TAG,"----------------------timeout");
            // setResult(Activity.RESULT_CANCELED,null);
            // OutPutSetConfirmDiag.dismiss();
            switch (msg.what) {
            case 0:
                setResult(Activity.RESULT_OK, null);
                break;
            default:
                break;
            }
        }
    }
	public void setResult(int resultCode, Object object) {
        if (resultCode == Activity.RESULT_OK && getActivity() != null) {
            String[] values = getResources().getStringArray(
                    R.array.outputmode_entries);
            mIndexEntry = sel_index;
            String tv_outputmode = values[sel_index];
            Log.d(TAG, "-------------setResult tv_outputmode = " + tv_outputmode);
            SystemProperties.set(STR_OUTPUT_VAR, tv_outputmode);
//            SystemProperties.set("ctl.start", "display_reset");
            String ret = SystemProperties.get("init.svc.display_reset", "");
            String mode = SystemProperties.get(STR_OUTPUT_VAR, "");
//            try {
//                Thread.currentThread().sleep(2000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            mListViewAdapter.notifyDataSetChanged();
            Log.i(TAG, "-------mode = " + mode + ", ret = " + ret);
        } else if (resultCode == Activity.RESULT_CANCELED) {
			//Resolution.setText(mEntryValues[mIndexEntry]);
		}
		
	}
	
	private void showDispmodeSetMsg(){
		Button btnConfirm,btnCancel;
		TextView msg;
		mProgressHandler = new SetconfirmHandler();		  
//		mProgressHandler.sendEmptyMessageDelayed(0, set_delay);
		OutPutSetConfirmDiag = new Dialog(getActivity(),R.style.DialogStyle);
		int width = (int) getResources().getDimension(R.dimen.ca_worktime_pwd_dialog_width);
        int height = (int) getResources().getDimension(R.dimen.ca_worktime_pwd_dialog_height);
        View view  = mInflater.inflate(R.layout.resolution_dialog, null);
		OutPutSetConfirmDiag.setContentView(view,new LayoutParams(width, height));
		msg = (TextView) OutPutSetConfirmDiag.findViewById(R.id.textview_dialog);
		//msg.setSingleLine(false);
		msg.setText(R.string.dlg_OutPutSetting_info);
		btnConfirm = (Button) OutPutSetConfirmDiag.findViewById(R.id.btn_dlg_confirm);
		btnConfirm.setText(R.string.dlg_confirm);
		btnConfirm.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
		
				mProgressHandler.removeMessages(0);
//				setResult(Activity.RESULT_OK,null);
				Message msg = new Message();
				msg.what = 0;
				mProgressHandler.sendMessageDelayed(msg, 2000);
				OutPutSetConfirmDiag.dismiss();
			}
       	
		});
       
		btnCancel = (Button) OutPutSetConfirmDiag.findViewById(R.id.btn_dlg_cancel);
		btnCancel.setText(R.string.dlg_cancel);
		btnCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
		
				Log.e(TAG,"----------------------no"+mIndexEntry);
				mProgressHandler.removeMessages(0);
				//DisableFreeScaleJni(old_mode);
				OutPutSetConfirmDiag.dismiss();
//				setResult(Activity.RESULT_CANCELED,null);
			}
		});
		OutPutSetConfirmDiag.setOnKeyListener(new DialogInterface.OnKeyListener() {
	       	@Override
	       	public boolean onKey (DialogInterface dialog, int keyCode, KeyEvent event) {
	       		if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP && !event.isCanceled()) {
	       			dialog.cancel(); 
	       			Log.e(TAG,"----------------------back "+mIndexEntry);
	       			mProgressHandler.removeMessages(0);
	       			//DisableFreeScaleJni(old_mode);
//	       			setResult(Activity.RESULT_CANCELED,null);
	       			return true;
	       		}	
	       		return false;
       		}
		});        
		OutPutSetConfirmDiag.show();
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
	@Override
	public void onResume() {
	    
	    Log.i(TAG, "-------onResume   ");
	    super.onResume();
	}
	@Override
	public void onStop() {
	    
	    Log.i(TAG, "-------onStop   ");
        if (OutPutSetConfirmDiag != null && OutPutSetConfirmDiag.isShowing()) {
            OutPutSetConfirmDiag.dismiss();
        }
	    super.onStop();
	}
	@Override
	public void onDestroy() {
	    
	    Log.i(TAG, "-------onDestroy   ");
	    super.onDestroy();
	}
	@Override
	public void onDetach() {
	    
	    Log.i(TAG, "-------onDetach   ");
	    super.onDetach();
	}
	@Override
	public void onAttach(Activity activity) {
	    
	    Log.i(TAG, "-------onAttach   ");
	    super.onAttach(activity);
	}
	@Override
	public void onStart() {
	    
	    Log.i(TAG, "-------onStart   ");
	    super.onStart();
	}
	@Override
	public void onPause() {
	    
	    Log.i(TAG, "-------onStart   ");
	    super.onPause();
	}
}
