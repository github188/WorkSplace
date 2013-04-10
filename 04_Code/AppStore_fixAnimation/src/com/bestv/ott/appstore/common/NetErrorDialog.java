package com.bestv.ott.appstore.common;

	
import com.bestv.ott.appstore.R;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.TextView;



public class NetErrorDialog extends Dialog {
	private Context context = null;
	private static NetErrorDialog netErrorDialog = null;
	
	public NetErrorDialog(Context context){
		super(context);
		this.context = context;
	}
	
	public NetErrorDialog(Context context, int theme) {
        super(context, theme);
    }
	
	public static NetErrorDialog createDialog(Context context){
		netErrorDialog = new NetErrorDialog(context,R.style.CustomProgressDialog);
		netErrorDialog.setContentView(R.layout.net_error_dialog_layout);
		netErrorDialog.getWindow().getAttributes().gravity = Gravity.CENTER;
		
		return netErrorDialog;
	}
 
    public void onWindowFocusChanged(boolean hasFocus){
    	
    	if (netErrorDialog == null){
    		return;
    	}
    	
//        ImageView imageView = (ImageView) netErrorDialog.findViewById(R.id.loadingImageView);
//        AnimationDrawable animationDrawable = (AnimationDrawable) imageView.getBackground();
//        animationDrawable.start();
    }
 
    public NetErrorDialog setTitile(String strTitle){
    	return netErrorDialog;
    }
    
    public NetErrorDialog setMessage(String strMessage){
    	TextView tvMsg = (TextView)netErrorDialog.findViewById(R.id.id_tv_error);
    	if (tvMsg != null){
    		tvMsg.setText(strMessage);
    	}
    	return netErrorDialog;
    }
}