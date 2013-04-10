package com.joysee.launcher.common;

import android.content.Context;
import android.database.Cursor;
import android.widget.SimpleCursorAdapter;

public class MenuAdapter extends SimpleCursorAdapter {

	public MenuAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
		super(context, layout, c, from, to);
	}

	public MenuAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
	}

}
