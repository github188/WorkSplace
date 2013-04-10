package com.lenovo.settings;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
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

public class InputSettings extends Fragment {
	protected static final String TAG = "InputSettings";
	private View mView;
	private InputMethodManager mImm;
	List<InputMethodInfo> inputMethodInfos;
	private ArrayList<String> allItems = new ArrayList<String>();
	private int mIndex;
	private TextView mSpinner;
	private Button mInputSetting;
	private ListView mListView;
	private RelativeLayout mListLayout;
	private ArrayAdapter<String> mAdapter;
	protected boolean mListEnd = false;
	protected boolean mEnableList = false;
//	private DropdownMenu mDropMenu;
	private ArrayList<String> mInputArrays;
	private int mIndexInput;
	private int mDropMenuSize = 2;
	private ListViewAdapter mListViewAdapter;
	String[] mArray;
	int mArrayMsg;
	int mImage;
	private static final int SET_DEFAULT_SUCC = 1;
	private static final int SET_DEFAULT_FAIL = 2;
	

	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
	

			switch(msg.what){
			case SET_DEFAULT_SUCC:
				mArrayMsg = R.string.default_input;
				mImage = R.drawable.sel_icon;
				mInputArrays = getMethodList();
				mIndexInput = getDefaultMethod();
				mListViewAdapter.notifyDataSetChanged();
				break;
//			case DropdownMenu.DROPDOWN_MENU_ITEM_CHICK:
//				if(msg.arg1 == DropdownMenu.DROPDOWN_MENU_BUTTON_CAN_EDIT){
//					EditText edit = (EditText) msg.obj;
//				}else if(msg.arg1 == DropdownMenu.DROPDOWN_MENU_BUTTON_NOT_EDIT){
//					TextView text = (TextView) msg.obj;
//					int position = msg.arg2;
//					if(text == mSpinner){
//						  if(mIndexInput != position){
//							  mIndexInput = position;
//							  setDefaultMethod(mIndexInput);
//						  }
//					}else{
//						break;
//					}
//				}
//				break;
//			case DropdownMenu.DROPDOWN_MENU_GET_ADAPTER:
//				if(msg.arg1 == DropdownMenu.DROPDOWN_MENU_BUTTON_CAN_EDIT){
//					EditText edit = (EditText) msg.obj;
//				}else if(msg.arg1 == DropdownMenu.DROPDOWN_MENU_BUTTON_NOT_EDIT){
//					TextView text = (TextView) msg.obj;
//					if(text == mSpinner){
//						mDropMenu.setListViewPosition(0, mDropMenuSize);
//						mDropMenu.setListViewAdapter(mInputArrays, mIndexInput);
//					}else{
//						break;
//					}
//					mDropMenu.showDropdownListEnable(true);
//				}
//				break;
			}
		}
		
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		mArrayMsg = R.string.default_input;
		mImage = R.drawable.sel_icon;

//		mSpinner = (TextView) mView.findViewById(R.id.spn_input_setting);
		mImm = (InputMethodManager) getActivity().getSystemService(
				Context.INPUT_METHOD_SERVICE);
		inputMethodInfos = mImm.getInputMethodList();
//		mDropdownList = (ListView) mView.findViewById(R.id.dropdownlist);
//		mListLayout = (RelativeLayout) mView.findViewById(R.id.listLayout);
//
//
//		mDropMenu = new DropdownMenu(getActivity(),mView,mHandler );
		mInputArrays = getMethodList();
		mIndexInput = getDefaultMethod();
		
//		if(mInputArrays.size() > 4){
//			mDropMenuSize = 4;
//		}else{
//			mDropMenuSize = mInputArrays.size();
//		}
//		mDropMenu.setButtonListener(mSpinner,mInputArrays.get(mIndexInput));
//		mDropMenu.setListViewListener();
//		
//		mInputSetting = (Button) mView.findViewById(R.id.inputmethodSetting);
//		mInputSetting.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//		
//				startActivity(getInputMethodIntent(mIndex));
//			}
//		});
//		LenovoSettingsActivity.setTitleFocus(false);
//		mSpinner.requestFocus(); 
		
		mView = inflater.inflate(R.layout.input_settings, container, false);
		mListView = (ListView) mView.findViewById(R.id.input_list);
		mListViewAdapter = new ListViewAdapter(getActivity(), mImage, mInputArrays, mArrayMsg);
		mListView.setAdapter(mListViewAdapter);
		//mListView.getChildAt(mIndexInput).requestFocus();
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position,
					long arg3) {
				if(position!=mIndexInput){
					setDefaultMethod(position);
					Message msg = new Message();
					msg.what = SET_DEFAULT_SUCC;
					mHandler.sendMessage(msg);
					NetworkWireless.showToast(getActivity(), R.string.input_succ);
//					ToastView totast = new ToastView(getActivity(),getString(R.string.input_succ),2000,false);
//					totast.showTotast();
					//Toast.makeText(getActivity(), "输入法设置成功", Toast.LENGTH_SHORT).show();
				}
			}
		});
		LenovoSettingsActivity.setTitleFocus(false);
		mListView.requestFocus();
		mListView.setSelection(mIndexInput);
		return mView;
	}

	private ArrayList<String> getMethodList() {
		allItems.clear();
		for (InputMethodInfo imi : inputMethodInfos) {
			String label = (String) imi.loadLabel(getActivity()
					.getPackageManager());
			if (label != null)
				allItems.add(label);
		}

		return allItems;

	}

	private Intent getInputMethodIntent(int index) {
		Intent intent;
		String settingsActivity = inputMethodInfos.get(index)
				.getSettingsActivity();
		intent = new Intent(Intent.ACTION_MAIN);
		intent.setClassName(inputMethodInfos.get(index).getPackageName(),
				settingsActivity);
		return intent;

	}

	private int getDefaultMethod() {

		final String currentInputMethodId = Settings.Secure.getString(
				getActivity().getContentResolver(),
				Settings.Secure.DEFAULT_INPUT_METHOD);
		for (int i=0 ;i< inputMethodInfos.size();i++)
			if (inputMethodInfos.get(i).getId().equals(currentInputMethodId))
					return i;
		return 0;
	}

	private void setDefaultMethod(int pos) {

		String currentInputMethodId = inputMethodInfos.get(pos).getId();
		Settings.Secure.putString(getActivity().getContentResolver(),
				Settings.Secure.DEFAULT_INPUT_METHOD,
				currentInputMethodId != null ? currentInputMethodId : "");
	}
	
    /*private class SettingsObserver extends ContentObserver {
        public SettingsObserver(Handler handler, Context context) {
            super(handler);
            final ContentResolver cr = context.getContentResolver();
            cr.registerContentObserver(
                    Settings.Secure.getUriFor(Settings.Secure.DEFAULT_INPUT_METHOD), false, this);
            cr.registerContentObserver(Settings.Secure.getUriFor(
                    Settings.Secure.SELECTED_INPUT_METHOD_SUBTYPE), false, this);
        }

        @Override public void onChange(boolean selfChange) {
        }
    }*/
    
	public class ListViewAdapter extends BaseAdapter {
	   	 
    	//private Context mContext;
		public ArrayList<String>  mArray;
		public int mArrayMsg;
		public int mImage;
		private LayoutInflater mLayoutInflater;
		private int selectedPosition = -1;  
		
		public void setSelectedPosition(int position) {   
			selectedPosition = position;   
		}

		public ListViewAdapter(Context context, int image,ArrayList<String> array,int arrayMsg) { 
			Log.d(TAG,"array size = "+array.size());
			System.out.println("array size = "+array.size());
    		//mContext = context; 
            mArray = array; 
            mImage = image;
            mArrayMsg = arrayMsg;
            mLayoutInflater = LayoutInflater.from(context); 
        } 

		@Override
		public int getCount() {
	
			return mArray.size();
		}

		@Override
		public Object getItem(int position) {
	
			return mArray.get(position);
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
				convertView = mLayoutInflater.inflate(R.layout.input_list, null); 
				holder.textView = (TextView) convertView.findViewById(R.id.inputArray);
				holder.imageView = (ImageView) convertView.findViewById(R.id.inputImg);
				holder.textViewMsg = (TextView) convertView.findViewById(R.id.inputArrayMsg);
				holder.layout = (LinearLayout) convertView.findViewById(R.id.inputLayout);
//				holder.textViewMsg.setAlpha(0.3f);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			holder.textView.setText(mArray.get(position));
			setDefaultMsg(holder.textViewMsg,holder.imageView, position);
			
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
		    public TextView textViewMsg;
		    public ImageView imageView;
		    public LinearLayout layout;
		} 
    }
	
	private void setDefaultMsg(TextView tv,ImageView iv,int position){
		if(mIndexInput == position){
			tv.setText(mArrayMsg);
			iv.setImageResource(mImage);
			iv.setVisibility(View.VISIBLE);
		}else{
			tv.setText("");
			iv.setVisibility(View.INVISIBLE);
		}
	}

    @Override
    public void onDestroy() {
        
        Log.d(TAG, "---onDestroy---");
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        
        Log.d(TAG, "---onDestroyView---");
        super.onDestroyView();
	}
}