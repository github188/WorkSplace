package com.ismartv.ui.widget;

import com.ismartv.ui.ISTVVodConstant;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;

public class ItemScrollView extends ScrollView {

	private static final String TAG = "ItemScrollView";
	
	private MovieListView mDataView = null;

	public ItemScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public ItemScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	public ItemScrollView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public void setItemListView(MovieListView itemView){
		mDataView = itemView;
	}

	@Override
	protected int computeVerticalScrollOffset() {
		// TODO Auto-generated method stub
		int offset = mDataView.getStartRow()*ISTVVodConstant.MOVIE_ITEM_HEIGHT;
		Log.d(TAG, "computeVerticalScrollOffset="+mDataView.getStartRow());
		return offset;
	}

	@Override
	protected int computeVerticalScrollRange() {
		// TODO Auto-generated method stub
		int range = mDataView.getTotalRowCount()*ISTVVodConstant.MOVIE_ITEM_HEIGHT;;
		Log.d(TAG, "computeVerticalScrollRange="+mDataView.getTotalRowCount());
		return range;
	}

	@Override
	protected boolean awakenScrollBars(int startDelay, boolean invalidate) {
		// TODO Auto-generated method stub
		boolean ret = super.awakenScrollBars(startDelay, invalidate);
		Log.d(TAG, "awakenScrollBar: ret="+ret);
		return ret;
	}

	@Override
	protected void initializeScrollbars(TypedArray a) {
		// TODO Auto-generated method stub
		super.initializeScrollbars(a);
		Log.d(TAG, "initializeScrollbars");
	}
	
		
}
