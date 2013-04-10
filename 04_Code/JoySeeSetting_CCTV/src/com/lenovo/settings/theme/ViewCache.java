package com.lenovo.settings.theme;

import com.lenovo.settings.R;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ViewCache {
	private View baseView;
	private TextView textView;
	private ImageView textViewMsg;
	private ImageView imageView;

	public ViewCache(View baseView) {
		this.baseView = baseView;
	}

	public TextView getTextView() {
		if (textView == null) {
			textView = (TextView) baseView.findViewById(R.id.theme_title);
		}
		return textView;
	}
	
	public ImageView getTextViewMsg() {
		if (textViewMsg == null) {
			textViewMsg = (ImageView) baseView.findViewById(R.id.theme_msg);
		}
		return textViewMsg;
	}

	public ImageView getImageView() {
		if (imageView == null) {
			imageView = (ImageView) baseView.findViewById(R.id.theme_imag);
		}
		return imageView;
	}
}
