
package com.joysee.adtv.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.TextView;

/**
 * 单行文本跑马灯控件
 */
public class AutoScrollTextView extends TextView implements OnClickListener {
//    private static final DvbLog log = new DvbLog(
//            "com.joysee.adtv.ui.AutoScrollTextView",DvbLog.DebugType.D);
    /**
     * 文本长度
     */
    private float mTextLength = 0f;
    /**
     * 显示窗口宽度
     */
    private float mWindowWidth = 0f;
    /**
     * 文字的横坐标
     */
    private float mText_X = 0f;
    /**
     * 文字的纵坐标
     */
    private float mText_Y = 0f;
    /**
     * 文字的开始坐标
     */
    private float mTextStart_X = 0.0f;
//    /**
//     * 文本滚动距离
//     */
//    private float mScrollLength = 0.0f;
    /**
     * 是否开始滚动
     */
    public boolean isStarting = false;
    /**
     * 文本画笔
     */
    private Paint mPaint = null;
    /**
     * 文本内容
     */
    private String mText;
    /**
     * 每次文本绘制位移
     */
    private int mStep = 3;

    public AutoScrollTextView(Context context) {
        super(context);
        initView();
    }

    public AutoScrollTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public AutoScrollTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    /**
     * 初始化控件
     */
    private void initView() {
        setOnClickListener(this);
    }

    /**
     * 文本初始化，每次更改文本内容或者文本效果等之后都需要重新初始化一下
     */
    public void init(WindowManager windowManager) {
        mPaint = getPaint();
        mText = getText().toString();
        mPaint.setColor(Color.WHITE);
        mPaint.setTextSize(35);
        mTextLength = mPaint.measureText(mText);
        mWindowWidth = getWidth();
        if (mWindowWidth == 0) {
            if (windowManager != null) {
                Display display = windowManager.getDefaultDisplay();
                mWindowWidth = display.getWidth();
            }
        }
        mTextStart_X = mText_X = mWindowWidth;
//        mScrollLength = mWindowWidth + mTextLength;
        mText_Y = getTextSize() + getPaddingTop();
//        log.D(" ---------- mTextStart_X = " + mTextStart_X + " mScrollLength = " 
//        + mScrollLength + " mWindowWidth = "
//                + mWindowWidth + " mText_Y = " + mText_Y);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);

        ss.step = mText_X;
        ss.isStarting = isStarting;

        return ss;

    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        mText_X = ss.step;
        isStarting = ss.isStarting;

    }

    public static class SavedState extends BaseSavedState {
        public boolean isStarting = false;
        public float step = 0.0f;

        SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeBooleanArray(new boolean[] {
                    isStarting
            });
            out.writeFloat(step);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }

            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }
        };

        private SavedState(Parcel in) {
            super(in);
            boolean[] b = null;
            in.readBooleanArray(b);
//            if (b != null && b.length > 0)
//                isStarting = b[0];
            step = in.readFloat();
        }
    }

    /**
     * 开始滚动
     */
    public void startScroll() {
        isStarting = true;
        invalidate();
    }

    /**
     * 停止滚动
     */
    public void stopScroll() {
        isStarting = false;
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawText(mText, mText_X, mText_Y, mPaint);
        if (!isStarting) {
            return;
        }
        mText_X -= mStep;
        if (mText_X < -mTextLength) {
            mText_X = mTextStart_X;
        }
        invalidate();
    }

    @Override
    public void onClick(View v) {
        // if (isStarting)
        // stopScroll();
        // else
        // startScroll();
    }
}
