package com.joysee.launcher.common;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.widget.TextView;

public class DropdownListItem extends TextView {

	
	public Intent mIntent;
	public boolean mGotoAllApp;
	public int mParentIndex;
	
	public DropdownListItem(Context context) {
		super(context);
	}

	public DropdownListItem(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DropdownListItem(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

}
