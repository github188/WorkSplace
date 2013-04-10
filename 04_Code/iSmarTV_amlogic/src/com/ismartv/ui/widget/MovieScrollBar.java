package com.ismartv.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class MovieScrollBar extends LinearLayout {

	public MovieScrollBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public MovieScrollBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public MovieScrollBar(Context context) {
		super(context);
	}
	
	private ImageView bg = null;
	private ImageView button = null;
}
