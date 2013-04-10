package com.joysee.adtv.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.joysee.adtv.R;

public class EpgProgramLinear extends LinearLayout{

    public boolean mAdding;
    private int mChanItemHeight=(int)getResources().getDimension(R.dimen.epg_program_item_height);
    
    public EpgProgramLinear(Context context) {
        super(context);
    }
    
    public EpgProgramLinear(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EpgProgramLinear(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    @Override
    protected void dispatchDraw(Canvas canvas) {
        if(mAdding){
            canvas.translate(0, -mChanItemHeight);
        }
        super.dispatchDraw(canvas);
    }
    
    public void setAdding(boolean adding){
        mAdding = adding;
    }
    
}
