package com.ismartv.ui.widget;

import com.ismartv.ui.ISTVVodItemDetail;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;

public class MovieItemView extends LinearLayout {

    private static final String TAG = "MovieItemView";
    
    private Integer pk = null;
    private Boolean isLoading = true;
	private Boolean isComplex = true;
    private int colId = 0;

    public Boolean getLoading() {
        return isLoading;
    }

	public void setComplex(Boolean isComplex) {
		this.isComplex = isComplex;
	}
	
	public Boolean getComplex() {
		return isComplex;
	}
	
    public void setLoading(Boolean isLoading) {
        this.isLoading = isLoading;
    }

    public MovieItemView(Context context) {
        super(context);
    }

    public MovieItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MovieItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /* (non-Javadoc)
     * @see android.widget.LinearLayout#onDraw(android.graphics.Canvas)
     */
    @Override
    protected void onDraw(Canvas canvas) {
        Log.d(TAG, "onDraw: id="+getId());
        super.onDraw(canvas);
    }

    /* (non-Javadoc)
     * @see android.view.View#invalidate()
     */
    @Override
    public void invalidate() {
        // TODO Auto-generated method stub
        Log.d(TAG, "invalidate: id="+getId());
        super.invalidate();
    }

    public void setPk(Integer pk) {
        this.pk = pk;
    }

    public Integer getPk() {
        return pk;
    }
    
    // add by dgg 2012/03/26
    public void setColId( int colId){
    	
    	this.colId = colId;
    }
    
    public int getColId(){
    	
    	return colId;
    }
}
