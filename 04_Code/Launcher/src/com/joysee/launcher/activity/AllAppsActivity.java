package com.joysee.launcher.activity;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;

public class AllAppsActivity extends Activity {

	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		ColorDrawable colorDrawable = new ColorDrawable(Color.argb(0, 0, 0, 0));
		getWindow().setBackgroundDrawable(colorDrawable);
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.allapps_layout);
		
		
		
	}
}
