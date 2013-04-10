package com.lenovo.settings;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ToastView {
	private Context mContext;
	private String msg;
	private boolean isLoading;
	private int duration;
	public  ToastView(Context mContext1,String msg1,int duration1,boolean isLoading1) {
		this.mContext = mContext1;
		this.msg = msg1;
		this.isLoading = isLoading1;
		this.duration = duration1;
	}
	
	public void showTotast() {
		LayoutInflater inflater = LayoutInflater.from(mContext);
		View view = inflater.inflate(R.layout.toast_info, null);
		TextView textView = (TextView)view.findViewById(R.id.toast_text);
		ImageView img = (ImageView)view.findViewById(R.id.toastImage);
		textView.setText(msg);
		if(isLoading){
			
		}else{
			img.setVisibility(View.GONE);
		}
		Toast toast = new Toast(mContext);
		toast.setView(view);
		toast.setDuration(duration);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();	
		
	}
	
}
