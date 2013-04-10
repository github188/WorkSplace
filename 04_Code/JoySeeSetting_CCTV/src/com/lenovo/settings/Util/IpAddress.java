package com.lenovo.settings.Util;

import com.lenovo.settings.R;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.*;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class IpAddress extends LinearLayout implements OnFocusChangeListener,
	OnClickListener, OnKeyListener ,TextWatcher{

	private static final String TAG = "IpAddress";
	private EditText mEdit1, mEdit2, mEdit3, mEdit4;
	private TextView mText1, mText2, mText3;
	protected boolean mLayoutEnable = false;
	private Context mContext; 
	private boolean mHasEnable = false;
	private Activity mActivity;
	private int max = 255;
	private String tpString ;
	

	public IpAddress(Context context) {
		super(context);
		
		mContext = context;
	}
	
	public void setActivity(Activity activity){
		mActivity = activity;
	}
	
	public IpAddress(Context context, AttributeSet attrs) {
        super(context, attrs);
		mContext = context;
    }
	
	void setTextColor(int color){
		mEdit1.setTextColor(color);
		mEdit2.setTextColor(color);
		mEdit3.setTextColor(color);
		mEdit4.setTextColor(color);
		mText1.setTextColor(color);
		mText2.setTextColor(color);
		mText3.setTextColor(color);
	}
	
	void setEditEnable(boolean enable){
		mEdit1.setEnabled(enable);
		mEdit2.setEnabled(enable);
		mEdit3.setEnabled(enable);
		mEdit4.setEnabled(enable);
		mEdit1.setFocusable(enable);
		mEdit2.setFocusable(enable);
		mEdit3.setFocusable(enable);
		mEdit4.setFocusable(enable);
	}   
	
	public void initIpAddress(){        
        LayoutInflater inflater = (LayoutInflater) mContext
        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.ipaddress, null);
        this.setBackgroundResource(R.drawable.setting_time_nofocus);
        mEdit1 = (EditText) view.findViewById(R.id.editText1);
        mEdit2 = (EditText) view.findViewById(R.id.editText2);
        mEdit3 = (EditText) view.findViewById(R.id.editText3);
        mEdit4 = (EditText) view.findViewById(R.id.editText4);
        mText1 = (TextView) view.findViewById(R.id.textView1);
        mText2 = (TextView) view.findViewById(R.id.textView2); 
        mText3 = (TextView) view.findViewById(R.id.textView3);
        mHasEnable = false;
        setTextColor(getResources().getColor(R.color.grey));
//        setEditEnable(false);
        this.setOnFocusChangeListener(this);
        mEdit1.setOnFocusChangeListener(this);
        mEdit2.setOnFocusChangeListener(this);
        mEdit3.setOnFocusChangeListener(this);
        mEdit4.setOnFocusChangeListener(this);
        //this.setOnClickListener(this); 
        this.setOnKeyListener(this);
        mEdit1.setOnKeyListener(this);
        mEdit2.setOnKeyListener(this);
        mEdit3.setOnKeyListener(this);
        mEdit4.setOnKeyListener(this);
        mEdit1.addTextChangedListener(this);
        mEdit2.addTextChangedListener(this);
        mEdit3.addTextChangedListener(this);
        mEdit4.addTextChangedListener(this);
        addView(view);
	}
	
	public void setIpAddress(String ipAddress){
		String strs[] = ipAddress.split("\\.");
		if(strs.length != 4){
			return;
		}
		mEdit1.setText(strs[0]);
		mEdit2.setText(strs[1]);
		mEdit3.setText(strs[2]);
		mEdit4.setText(strs[3]);
	}
	
    public String getIpAddress() {
        String ip = "";
        try {
            ip = Integer.parseInt(mEdit1.getText().toString().trim()) + "."
                    + Integer.parseInt(mEdit2.getText().toString().trim()) + "."
                    + Integer.parseInt(mEdit3.getText().toString().trim()) + "."
                    + Integer.parseInt(mEdit4.getText().toString().trim());
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return ip;
    }

	private int flag = 0;
	@Override
	public void onFocusChange(View v, boolean hasFocus) {

		Log.d(TAG," focus id = "+v.getId()+" this id = "+this.getId());
		
		if(flag == 0){
			mEdit1.requestFocus();
			flag = 1;
		}
		
		if(hasFocus){
//			if(v.getId() == this.getId()){
//				mHasEnable = false;
//			}else{
//				mHasEnable = true;
//				//Selection.setSelection((Spannable) v, 3);
//			}
			setSeletion();
			setTextColor(getResources().getColor(R.color.yellow));
			Log.d(TAG,"hase focuse");
			this.setBackgroundResource(R.drawable.setting_time_focus);
		}else{
			mHasEnable = false;
			setTextColor(getResources().getColor(R.color.grey));
			this.setBackgroundResource(R.drawable.setting_time_nofocus);
			Log.d(TAG,"lost focuse");
		}
			
	}

	@Override
	public void onClick(View v) {


		if(v.getId() == this.getId()){
			if(!mHasEnable){
				Log.d(TAG,"hase enable!");
				mHasEnable = true;
				setEditEnable(mHasEnable);	
				IpAddress.this.clearFocus();
				mEdit1.requestFocus();
			}
		}else{
			if(mHasEnable){
				Log.d(TAG,"lost enable!");
				mHasEnable = false;
				setEditEnable(mHasEnable);
				IpAddress.this.requestFocus();					
			}
		}
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {

		Log.d(TAG,"keyCode = "+keyCode+" id = "+v.getId()+" this id = "+this.getId());
		
		if((keyCode == KeyEvent.KEYCODE_DPAD_CENTER) || (keyCode == KeyEvent.KEYCODE_ENTER)){
//			if(event.getAction() != KeyEvent.ACTION_DOWN){
//				return true;
//			}
			if(v.getId() == this.getId()){
				if(!mHasEnable){
					Log.d(TAG,"hase enable!");
					mHasEnable = true;
					setEditEnable(mHasEnable);	
					IpAddress.this.clearFocus();
					mEdit1.requestFocus();
				}
			}else{
				if(mHasEnable){
					Log.d(TAG,"lost enable!");
					mHasEnable = false;
//					setEditEnable(mHasEnable);
					IpAddress.this.requestFocus();					
				}
			}
			return false;
			
		}else if((keyCode == KeyEvent.KEYCODE_DPAD_DOWN) || (keyCode == KeyEvent.KEYCODE_DPAD_UP)){
			//#########ipaddress
			setEditEnable(true);	
			IpAddress.this.clearFocus();
			mEdit1.requestFocus();
			
//			if(mHasEnable){
//				return true;
//			}
		}else if(keyCode == KeyEvent.KEYCODE_TAB){
			if(v.getId() == R.id.editText4){
				return true;
			}
		}else if(keyCode == KeyEvent.KEYCODE_BACK){
			if(mHasEnable){
				if(event.getAction() != KeyEvent.ACTION_DOWN){
					mHasEnable = false;
					return true;
				}
				//mHasEnable = false;
				setEditEnable(mHasEnable);
				IpAddress.this.requestFocus();
				return true;
			}
		}
		return false;
	}
	

	//############################setselection
    private void setSeletion() {
        if (mEdit1.isFocused()) {
            int mStrLength = mEdit1.getText().toString().length();
            mEdit1.setSelection(mStrLength);
        } else if (mEdit2.isFocused()) {
            int mStrLength = mEdit2.getText().toString().length();
            mEdit2.setSelection(mStrLength);
        } else if (mEdit3.isFocused()) {
            int mStrLength = mEdit3.getText().toString().length();
            mEdit3.setSelection(mStrLength);
        } else if (mEdit4.isFocused()) {
            int mStrLength = mEdit4.getText().toString().length();
            mEdit4.setSelection(mStrLength);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
            int after) {
        
        
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        
    }

    @Override
    public void afterTextChanged(Editable s) {
        
        tpString = s.toString();
        Log.d(TAG, "------------------ afterTextChanged   string = " + tpString);
        try {
            if (Integer.parseInt("0" + tpString) > max) {
                setMaxText();
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }
    /**
     * 限制最大输入值为255
     */
    public void setMaxText() {
        if (mEdit1.isFocused()) {
            mEdit1.setText("" + max);
        } else if (mEdit2.isFocused()) {
            mEdit2.setText("" + max);
        } else if (mEdit3.isFocused()) {
            mEdit3.setText("" + max);
        } else if (mEdit4.isFocused()) {
            mEdit4.setText("" + max);
        }
        setSeletion();
    }
}
