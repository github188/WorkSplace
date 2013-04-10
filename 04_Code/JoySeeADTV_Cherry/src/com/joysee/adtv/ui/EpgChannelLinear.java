package com.joysee.adtv.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.joysee.adtv.R;

public class EpgChannelLinear extends LinearLayout{

    public boolean mAdding;
    public int mChanItemWidth=(int)getResources().getDimension(R.dimen.epg_channel_item_width);
    
    public EpgChannelLinear(Context context) {
        super(context);
    }
    
    public EpgChannelLinear(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EpgChannelLinear(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    @Override
    protected void dispatchDraw(Canvas canvas) {
        if(mAdding){
            canvas.translate(-mChanItemWidth, 0);
        }
        super.dispatchDraw(canvas);
    }
    
    public void setAdding(boolean adding){
        mAdding = adding;
    }
    
}
