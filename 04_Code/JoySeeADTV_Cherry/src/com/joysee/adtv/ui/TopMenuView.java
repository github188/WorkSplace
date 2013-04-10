
package com.joysee.adtv.ui;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.joysee.adtv.R;

/**
 * 直播指南一级菜单
 * @author wuhao
 *
 */
public class TopMenuView extends RelativeLayout implements View.OnClickListener,
        View.OnFocusChangeListener {
    private int mSelectedID = 0 ,mFocusID = 0;
    private String TAG = "TopMenuView";
    private RelativeLayout mMenuItemView[], mSelectedItemView , mParentView;
    private int[] mItem_ID = {
            R.id.topmenu_item0, R.id.topmenu_item1, R.id.topmenu_item2, R.id.topmenu_item3,
            R.id.topmenu_item4, R.id.topmenu_item5
    };
    private String[] mMenuItemSubTitle = {
            "1/100", "1/120", "1/130", "1/140", "1/150", "1/160"
    };
    private String[] mMenuItemHeadTitle = {
            "影视", "高清", "体育", "综艺", "少儿", "综艺"
    };
    private int mTextSizeNormal = 36, mTextSizeFocus = 46;
    
    private boolean canFocus = true;
    
    private LiveGuideView guideView;
    
    private Handler mHandler;
    
    public boolean isCanFocus() {
        return canFocus;
    }

    public void setCanFocus(boolean canFocus) {
        this.canFocus = canFocus;
    }

    public TopMenuView(Context context) {
        super(context);
    }
    
    public TopMenuView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public TopMenuView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    /**
     * 初始化菜单中的View资源
     * @param ID 默认选中的第几个菜单,从0开始.
     */
    public void init(int ItemID ,LiveGuideView view ,String [] title) {
        guideView = view;
        if (title != null && title.length > 0) {
            Log.d(TAG, " init  title.length = " + title.length);
            mMenuItemHeadTitle = title;
        }
        mParentView = (RelativeLayout) this.findViewById(R.id.programguide_menu);
        mParentView.setOnFocusChangeListener(this);
        mMenuItemView = new RelativeLayout[mMenuItemHeadTitle.length];
        for (int i = 0; i < mMenuItemHeadTitle.length; i++) {
            mMenuItemView[i] = (RelativeLayout) findViewById(mItem_ID[i]);
            mMenuItemView[i].setOnFocusChangeListener(this);
            mMenuItemView[i].setOnClickListener(this);
            ((TextView) mMenuItemView[i].findViewById(R.id.topmenu_title_big))
                    .setText(mMenuItemHeadTitle[i]);
            ((TextView) mMenuItemView[i].findViewById(R.id.topmenu_title_normal))
                    .setText(mMenuItemHeadTitle[i]);
//            ((TextView) mMenuItemView[i].findViewById(R.id.topmenu_title_small))
//                    .setText(mMenuItemSubTitle[i]);
        }
        mSelectedID = ItemID;
        mFocusID = ItemID;
        mSelectedItemView = mMenuItemView[mSelectedID];
        setItemSelected(mSelectedItemView, true);
        mSelectedItemView.requestFocus();
    }
    
    public boolean dispatchKeyEvent(KeyEvent event) {
        Log.d(TAG,
                " dispatchKeyEvent mFocusID = " + mFocusID + " event.getAction() = "
                        + event.getAction());
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    guideView.resetSelected();
                    return false;
                case KeyEvent.KEYCODE_DPAD_UP:
                    return true;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    mFocusID--;
                    if (mFocusID < 0) {
                        mFocusID = 0;
                        return true;
                    }
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    mFocusID++;
                    if (mFocusID > mMenuItemHeadTitle.length - 1) {
                        mFocusID = mMenuItemHeadTitle.length - 1;
                        return true;
                    }
                    break;
            // case KeyEvent.KEYCODE_HOME:
            // case KeyEvent.KEYCODE_ESCAPE:
            // case KeyEvent.KEYCODE_BACK:
            }
        }
        return super.dispatchKeyEvent(event);
    }
    
    public void setFocusView() {
        clearFocusView();
        mSelectedItemView.requestFocus();
        canFocus = true;
    }
    
    public void clearFocusView() {
        for (int i = 0; i < mMenuItemView.length; i++) {
            if (mMenuItemView[i].isFocused()) {
                mMenuItemView[i].clearFocus();
            }
            findFocusID(mSelectedItemView);
        }
        Log.d(TAG, " clearFocusView mFocusID = " + mFocusID + " canFocus = " + canFocus);
    }
    
    public void findFocusID(View v) {
        for (int i = 0; i < mMenuItemView.length; i++) {
            if (v.equals(mMenuItemView[i])) {
                mFocusID = i;
            }
        }
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.topmenu_item0:
            case R.id.topmenu_item1:
            case R.id.topmenu_item2:
            case R.id.topmenu_item3:
            case R.id.topmenu_item4:
            case R.id.topmenu_item5:
                setItemSelected(v, true);
                break;
        }
    }
    
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        Log.d(TAG, " onFocusChange " + v.getId() + " hasFocus = " + hasFocus + " canFocus = " + canFocus);
        switch (v.getId()) {
            case R.id.topmenu_item0:
            case R.id.topmenu_item1:
            case R.id.topmenu_item2:
            case R.id.topmenu_item3:
            case R.id.topmenu_item4:
            case R.id.topmenu_item5:
                if (canFocus) {
                    setItemFocus(v, hasFocus);
                }
                break;
        }
    }
    
    public void setItemSelected(View v, boolean isSelected) {
        // 恢复上一个被选择的View背景和字体
        mSelectedItemView.setBackgroundDrawable(null);
        mSelectedItemView.findViewById(R.id.topmenu_title_big).setVisibility(View.GONE);
        mSelectedItemView.findViewById(R.id.topmenu_title_small).setVisibility(View.GONE);
        mSelectedItemView.findViewById(R.id.topmenu_title_normal).setVisibility(View.VISIBLE);
        ((TextView) mSelectedItemView.findViewById(R.id.topmenu_title_normal))
                .setTextSize(mTextSizeNormal);
        ((TextView) mSelectedItemView.findViewById(R.id.topmenu_title_normal))
                .setTextColor(getResources().getColor(
                        R.color.grey_txt));
        if (isSelected) {
            v.setBackgroundResource(R.drawable.epg_menu_select_bg);
        } else {
            v.setBackgroundResource(R.drawable.epg_menu_bg);
        }
        v.findViewById(R.id.topmenu_title_big).setVisibility(View.GONE);
        v.findViewById(R.id.topmenu_title_small).setVisibility(View.GONE);
        v.findViewById(R.id.topmenu_title_normal).setVisibility(View.GONE);
        v.findViewById(R.id.topmenu_title_big).setVisibility(View.VISIBLE);
        v.findViewById(R.id.topmenu_title_small).setVisibility(View.GONE);
        ((TextView) v.findViewById(R.id.topmenu_title_big))
                .setTextColor(getResources()
                        .getColor(R.color.white_txt));
        ((TextView) v.findViewById(R.id.topmenu_title_small))
                .setTextColor(getResources()
                        .getColor(R.color.white_txt));
        mSelectedItemView = (RelativeLayout) v;
        findFocusID(mSelectedItemView);
        Message msg = new Message();
        msg.what = LiveGuideView.MSG_REFESH_DATA;
        msg.arg1 = mFocusID;
        if (mHandler != null) {
            mHandler.sendMessage(msg);
        }
    }
    
    public void setItemFocus(View v, boolean hasFocus) {
        Log.d(TAG, " setItemFocus hasFocus = " + hasFocus);
        if (hasFocus) {
            if (v.equals(mSelectedItemView)) {
                v.setBackgroundResource(R.drawable.epg_menu_select_bg);
                ((TextView) v.findViewById(R.id.topmenu_title_big))
                        .setTextColor(getResources()
                                .getColor(R.color.white_txt));
                ((TextView) v.findViewById(R.id.topmenu_title_small))
                        .setTextColor(getResources()
                                .getColor(R.color.white_txt));
            } else {
                ((TextView) v.findViewById(R.id.topmenu_title_normal)).setTextSize(mTextSizeFocus);
                ((TextView) v.findViewById(R.id.topmenu_title_normal))
                        .setTextColor(getResources()
                                .getColor(R.color.green_txt));
            }
            findFocusID(v);
        } else {
            if (v.equals(mSelectedItemView)) {
                v.setBackgroundResource(R.drawable.epg_menu_bg);
                ((TextView) v.findViewById(R.id.topmenu_title_big))
                        .setTextColor(getResources()
                                .getColor(R.color.green_txt));
                ((TextView) v.findViewById(R.id.topmenu_title_small))
                        .setTextColor(getResources()
                                .getColor(R.color.green_txt));
            } else {
                ((TextView) v.findViewById(R.id.topmenu_title_normal)).setTextSize(mTextSizeNormal);
                ((TextView) v.findViewById(R.id.topmenu_title_normal))
                        .setTextColor(getResources()
                                .getColor(R.color.grey_txt));
            }
        }
    }
    
    public boolean isFocus() {
        for (int i = 0; i < mMenuItemView.length; i++) {
            if (mMenuItemView[i].isFocused()) {
                return true;
            }
        }
        return false;
    }
    
    public void refreshSubView(String subString) {
        ((TextView) mSelectedItemView.findViewById(R.id.topmenu_title_small))
                .setText(subString);
    }
    
    public void setHandler(Handler handler){
        this.mHandler = handler;
    }
}
