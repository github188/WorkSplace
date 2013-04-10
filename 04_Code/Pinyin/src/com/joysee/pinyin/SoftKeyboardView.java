/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.joysee.pinyin;


import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.joysee.pinyin.SoftKeyboard.KeyRow;

/**
 * Class used to show a soft keyboard.
 * 
 * A soft keyboard view should not handle touch event itself, because we do bias
 * correction, need a global strategy to map an event into a proper view to
 * achieve better user experience.
 */
public class SoftKeyboardView extends View {
    
    
    /**
     * The definition of the soft keyboard for the current this soft keyboard
     * view.
     */
    private SoftKeyboard mSoftKeyboard;

    /** The last key pressed. */
    private SoftKey mSoftKeyDown;

    /** Used to indicate whether the user is holding on a key. */
    private boolean mKeyPressed = false;

    /**
     * The location offset of the view to the keyboard container.
     */
    private int mOffsetToSkbContainer[] = new int[2];

    /**
     * The location of the desired hint view to the keyboard container.
     */
    private int mHintLocationToSkbContainer[] = new int[2];

    /**
     * Text size for normal key.
     */
    private int mNormalKeyTextSize;

    /**
     * Text size for function key.
     */
    private int mFunctionKeyTextSize;

    /**
     * Long press timer used to response long-press.
     */
//    private SkbContainer.LongPressTimer mLongPressTimer;

    /**
     * Repeated events for long press
     */
    private boolean mRepeatForLongPress = false;

    /**
     * If this parameter is true, the balloon will never be dismissed even if
     * user moves a lot from the pressed point.
     */
    private boolean mMovingNeverHidePopupBalloon = false;

    /** Vibration for key press. */
    private Vibrator mVibrator;

    /** Vibration pattern for key press. */
    protected long[] mVibratePattern = new long[] {1, 20};

    /**
     * The dirty rectangle used to mark the area to re-draw during key press and
     * release. Currently, whenever we can invalidate(Rect), view will call
     * onDraw() and we MUST draw the whole view. This dirty information is for
     * future use.
     */
    private Rect mDirtyRect = new Rect();

    private Paint mPaint;
    private FontMetricsInt mFmi;
    private boolean mDimSkb;

    private Drawable mActiveCellDrawable;

    public SoftKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mFmi = mPaint.getFontMetricsInt();
        
        
        Resources r = context.getResources();
        mActiveCellDrawable = r.getDrawable(R.drawable.normal_key_hl_bg);
    }

    public boolean setSoftKeyboard(SoftKeyboard softSkb) {
        if (null == softSkb) {
            return false;
        }
        mSoftKeyboard = softSkb;
        Drawable bg = softSkb.getSkbBackground();
        if (null != bg) setBackgroundDrawable(bg);
        return true;
    }

    public SoftKeyboard getSoftKeyboard() {
        return mSoftKeyboard;
    }

    public void resizeKeyboard(int skbWidth, int skbHeight) {
        mSoftKeyboard.setSkbCoreSize(skbWidth, skbHeight);
    }

    public void setOffsetToSkbContainer(int offsetToSkbContainer[]) {
        mOffsetToSkbContainer[0] = offsetToSkbContainer[0];
        mOffsetToSkbContainer[1] = offsetToSkbContainer[1];
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth = 0;
        int measuredHeight = 0;
        if (null != mSoftKeyboard) {
            measuredWidth = mSoftKeyboard.getSkbCoreWidth();    
            measuredHeight = mSoftKeyboard.getSkbCoreHeight();
            measuredWidth += mPaddingLeft + mPaddingRight;
            measuredHeight += mPaddingTop + mPaddingBottom;
            Log.d("songwenxuan","measuredWidth="+measuredWidth+" measuredHeight="+measuredHeight);
        }
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    public void dimSoftKeyboard(boolean dimSkb) {
//        mDimSkb = dimSkb;
        mDimSkb = true;
        invalidate();
    }
    
    
    public int mFocusRowNum = 1;
    public int mFocusKeyNum = 5;

    @Override
    protected void onDraw(Canvas canvas) {
    	
        if (null == mSoftKeyboard) return;

        canvas.translate(mPaddingLeft, mPaddingTop);

        Environment env = Environment.getInstance();
        mNormalKeyTextSize = env.getKeyTextSize(false);
        mFunctionKeyTextSize = env.getKeyTextSize(true);
        // Draw the last soft keyboard
        int rowNum = mSoftKeyboard.getRowNum();
        int keyXMargin = mSoftKeyboard.getKeyXMargin();
        int keyYMargin = mSoftKeyboard.getKeyYMargin();
        Log.d("songwenxuan","row number = " + rowNum);
        for (int row = 0; row < rowNum; row++) {
            KeyRow keyRow = mSoftKeyboard.getKeyRowForDisplay(row);
            
            if (null == keyRow){
                Log.d("songwenxuan","key row " + row + " == null");
                continue;
            }
            List<SoftKey> softKeys = keyRow.mSoftKeys;
            
            int keyNum = softKeys.size();
            for (int i = 0; i < keyNum; i++) {
                boolean isFocusKey = false;
                
                SoftKey softKey = softKeys.get(i);
                
                if (SoftKeyType.KEYTYPE_ID_NORMAL_KEY == softKey.mKeyType.mKeyTypeId) {
                    mPaint.setTextSize(mNormalKeyTextSize);
                } else {
                    mPaint.setTextSize(mFunctionKeyTextSize);
                }
                if(row == mFocusRowNum && i == mFocusKeyNum){
                    isFocusKey = true;
                    Log.d("songwenxuan","focus key num = " + row + " " + i);
                }
                drawSoftKey(canvas, softKey, keyXMargin, keyYMargin,isFocusKey);//画按键
            }
        }

        if (mDimSkb) {
            mPaint.setColor(0xa0000000);
            canvas.drawRect(0, 0, getWidth(), getHeight(), mPaint);
        }
        
        Log.d("songwenxuan","getWidth = " + getWidth());
        Log.d("songwenxuan","getHeight = " + getHeight());

        mDirtyRect.setEmpty();
    }

    private void drawSoftKey(Canvas canvas, SoftKey softKey, int keyXMargin,
            int keyYMargin , boolean isFocusKey) {
        Drawable bg;
        int textColor;
        if (mKeyPressed && softKey == mSoftKeyDown) {
            bg = softKey.getKeyHlBg();
            textColor = softKey.getColorHl();
        } else {
            bg = softKey.getKeyBg();
            textColor = softKey.getColor();
        }

        if (null != bg) {//每个按键的背景。//可以在这搞焦点。在收到按键的时候判断哪个键应该获取焦点，则重绘把背景画成焦点样式。
            bg.setBounds(softKey.mLeft + keyXMargin, softKey.mTop + keyYMargin,
                    softKey.mRight - keyXMargin, softKey.mBottom - keyYMargin);
            bg.draw(canvas);
            if(isFocusKey){//focus键背景
                mActiveCellDrawable.setBounds(softKey.mLeft + keyXMargin, softKey.mTop + keyYMargin,
                        softKey.mRight - keyXMargin, softKey.mBottom - keyYMargin);
                mActiveCellDrawable.draw(canvas);
            }
        }
        String keyLabel = softKey.getKeyLabel();
        Drawable keyIcon = softKey.getKeyIcon();
        if (null != keyIcon) {//画特殊的按键符号。
            Drawable icon = keyIcon;
            int marginLeft = (softKey.width() - icon.getIntrinsicWidth()) / 2;
            int marginRight = softKey.width() - icon.getIntrinsicWidth()
                    - marginLeft;
            int marginTop = (softKey.height() - icon.getIntrinsicHeight()) / 2;
            int marginBottom = softKey.height() - icon.getIntrinsicHeight()
                    - marginTop;
            icon.setBounds(softKey.mLeft + marginLeft,
                    softKey.mTop + marginTop, softKey.mRight - marginRight,
                    softKey.mBottom - marginBottom);
            icon.draw(canvas);
        } else if (null != keyLabel) {//画按键。
            mPaint.setColor(textColor);
            float x = softKey.mLeft
                    + (softKey.width() - mPaint.measureText(keyLabel)) / 2.0f;
            int fontHeight = mFmi.bottom - mFmi.top;
            float marginY = (softKey.height() - fontHeight) / 2.0f;
            float y = softKey.mTop + marginY - mFmi.top + mFmi.bottom / 1.5f;
            canvas.drawText(keyLabel, x, y + 1, mPaint);
        }
    }
}
