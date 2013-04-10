package com.joysee.launcher.common;

import com.joysee.launcher.activity.R;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class ApplicationItem extends FrameLayout {
	
	public ImageView mIcon;
	public TextView mTitle;

	public ApplicationItem(Context context) {
		this(context,null);
	}

	public ApplicationItem(Context context, AttributeSet attrs) {
		this(context, attrs,0);
	}

	public ApplicationItem(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		
		mIcon = (ImageView) findViewById(R.id.application_item_pic);
		mTitle = (TextView) findViewById(R.id.application_item_title);
	}

}
