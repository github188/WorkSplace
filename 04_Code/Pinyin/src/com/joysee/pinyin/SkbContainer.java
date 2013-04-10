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

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.ViewFlipper;

/**
 * The top container to host soft keyboard view(s).
 */
public class SkbContainer extends RelativeLayout {

    private InputModeSwitcher mInputModeSwitcher;
    private Environment mEnvironment;
    private ViewFlipper mSkbFlipper;
    /**
     * The current soft keyboard layout.
     */
    private int mSkbLayout = 0;
    /** The major sub soft keyboard. */
    private SoftKeyboardView mMajorView;
    
    /**
     * The last parameter when function {@link #toggleCandidateMode(boolean)}
     * was called.
     */
    private boolean mLastCandidatesShowing;

    public SkbContainer(Context context) {
        super(context);
    }

    public SkbContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SkbContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        mEnvironment = Environment.getInstance();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Environment env = Environment.getInstance();
        int measuredWidth = (int) (env.getScreenWidth()*0.9);
        // measuredWidth = 600;
        int measuredHeight = getPaddingTop();
        measuredHeight += env.getSkbHeight();
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(measuredWidth,
                MeasureSpec.EXACTLY);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(measuredHeight,
                MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void updateInputMode() {
        int skbLayout = mInputModeSwitcher.getSkbLayout();// R.xml.id
//                                                          // 可以提供一个设置mSkb
        
        if (mSkbLayout != skbLayout) {
            mSkbLayout = skbLayout;
            updateSkbLayout();
        }
        
        mLastCandidatesShowing = false;
        if (null == mMajorView)
            return;

        SoftKeyboard skb = mMajorView.getSoftKeyboard();
        if (null == skb)
            return;
        skb.enableToggleStates(mInputModeSwitcher.getToggleStates());
        invalidate();
        return;
    }

    private void updateSkbLayout() {
        int screenWidth = (int) (mEnvironment.getScreenWidth()*0.88);
        int keyHeight = mEnvironment.getKeyHeight();
        int skbHeight = (int)(mEnvironment.getSkbHeight()*0.9);

        Resources r = mContext.getResources();
        if (null == mSkbFlipper) {
            mSkbFlipper = (ViewFlipper) findViewById(R.id.alpha_floatable);
        }
        mMajorView = (SoftKeyboardView) mSkbFlipper.getChildAt(0);
        SoftKeyboard majorSkb = null;
        SkbPool skbPool = SkbPool.getInstance();

        Log.d("songwenxuan","mSkbLayout="+mSkbLayout);//2130968579
        switch (mSkbLayout) {
            case R.xml.skb_qwerty:
                majorSkb = skbPool.getSoftKeyboard(R.xml.skb_qwerty,
                        R.xml.skb_qwerty, screenWidth, skbHeight, mContext);
                break;

            case R.xml.skb_sym1:
                majorSkb = skbPool.getSoftKeyboard(R.xml.skb_sym1, R.xml.skb_sym1,
                        screenWidth, skbHeight, mContext);
                break;

            case R.xml.skb_sym2:
                majorSkb = skbPool.getSoftKeyboard(R.xml.skb_sym2, R.xml.skb_sym2,
                        screenWidth, skbHeight, mContext);
                break;

            case R.xml.skb_smiley:
                majorSkb = skbPool.getSoftKeyboard(R.xml.skb_smiley,
                        R.xml.skb_smiley, screenWidth, skbHeight, mContext);
                break;

            case R.xml.skb_phone:
                majorSkb = skbPool.getSoftKeyboard(R.xml.skb_phone,
                        R.xml.skb_phone, screenWidth, skbHeight, mContext);
                break;
            default:
        }
        if (null == majorSkb || !mMajorView.setSoftKeyboard(majorSkb)) {
            return;
        }
        // mMajorView.setBalloonHint(mBalloonOnKey, mBalloonPopup, false);
        mMajorView.invalidate();
    }

    public void setInputModeSwitcher(InputModeSwitcher inputModeSwitcher) {
        mInputModeSwitcher = inputModeSwitcher;
    }
    public SoftKeyboard getSoftKeyboard() {
        return mMajorView.getSoftKeyboard();
    }
    
    public void setFocus(int row, int key) {
        mMajorView.mFocusRowNum = row;
        mMajorView.mFocusKeyNum = key;
        Log.d("songwenxuan", "row = " + row + " key = " + key);
        mMajorView.invalidate();
    }
    
    public void resetFocus(){
        mMajorView.mFocusRowNum = 1;
        mMajorView.mFocusKeyNum = 5;
    }

    public int getFocusRowNum() {
        return mMajorView.mFocusRowNum;
    }

    public int getFocusKeyNum() {
        return mMajorView.mFocusKeyNum;
    }
    
    public void toggleCandidateMode(boolean candidatesShowing) {
        if (null == mMajorView || !mInputModeSwitcher.isChineseText()
                || mLastCandidatesShowing == candidatesShowing)
            return;
        mLastCandidatesShowing = candidatesShowing;

        SoftKeyboard skb = mMajorView.getSoftKeyboard();
        if (null == skb)
            return;

        int state = mInputModeSwitcher.getTooggleStateForCnCand();
        if (!candidatesShowing) {
            skb.disableToggleState(state, false);
            skb.enableToggleStates(mInputModeSwitcher.getToggleStates());
        } else {
            skb.enableToggleState(state, false);
        }

        mMajorView.invalidate();
    }
}
