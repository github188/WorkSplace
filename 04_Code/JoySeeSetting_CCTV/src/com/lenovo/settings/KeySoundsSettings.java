package com.lenovo.settings;

import android.app.Fragment;
import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class KeySoundsSettings extends Fragment{
		
	private View mView;
	private TextView soundSpinner;
	


//    public static final Uri CONTENT_URI_SYS = CityProvider.Citys.CONTENT_URI;


    private String[] mSoundArrays ;
	private RelativeLayout mListLayout;
	private DropdownMenu mDropMenu;
	private int mIndexSound;
	
	ContentResolver resolver;

	Handler mHandler  = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case DropdownMenu.DROPDOWN_MENU_GET_ADAPTER:
				if(msg.arg1 == DropdownMenu.DROPDOWN_MENU_BUTTON_NOT_EDIT){
					TextView text = (TextView) msg.obj;
					if(text == soundSpinner){
						mDropMenu.setListViewPosition(0, 2);
						mDropMenu.setListViewAdapter(mSoundArrays, mIndexSound);
					}else{
						break;
					}
					mDropMenu.showDropdownListEnable(true);
				}
				break;
				
			case DropdownMenu.DROPDOWN_MENU_ITEM_CHICK:
				if(msg.arg1 == DropdownMenu.DROPDOWN_MENU_BUTTON_NOT_EDIT){
					TextView text = (TextView) msg.obj;
					int position = msg.arg2;
					System.out.println("position and text:"+position+"---"+getKeySoundVal()+"--"+text);
					System.out.println("ext == soundSpinner--->"+(text == soundSpinner));	
					if(text == soundSpinner){
						if (getKeySoundVal() != position){
							setKeySoundVal(position);
							System.out.println("getKeySoundVal()-->"+getKeySoundVal());
						}
					}else{
						break;
					}
				}
				break;
			}
		}
		
	};
	
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		mSoundArrays = getResources().getStringArray(R.array.frequency_array);
		resolver = getActivity().getContentResolver(); 
		
		mView = inflater.inflate(R.layout.keysounds_settings, container, false);
		soundSpinner = (TextView) mView.findViewById(R.id.soundsSpinner);

		mIndexSound = getKeySoundVal();
		mDropMenu = new DropdownMenu(getActivity(),mView,mHandler);
		mDropMenu.setListViewListener();
		mDropMenu.setButtonListener(soundSpinner,mSoundArrays[mIndexSound]);
		
		LenovoSettingsActivity.setTitleFocus(false);
		soundSpinner.requestFocus();
		
		return mView;
	} 

	private int getKeySoundVal(){
		return Settings.System.getInt(resolver,Settings.System.LOCKSCREEN_SOUNDS_ENABLED, 1);
	}
	
	public void setKeySoundVal(int pos){
		if(pos >=0)
		Settings.System.putInt(resolver, Settings.System.LOCKSCREEN_SOUNDS_ENABLED,pos);
	}

}