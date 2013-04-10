
package com.joysee.adtv.ui;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.joysee.adtv.R;
import com.joysee.adtv.common.DvbLog;
import com.joysee.adtv.common.DvbMessage;
import com.joysee.adtv.common.DefaultParameter.ViewMessage;
import com.joysee.adtv.controller.ViewController;
import com.joysee.adtv.doc.ADTVProgramGuideDoc;
import com.joysee.adtv.doc.ADTVResource;
import com.joysee.adtv.logic.SettingManager;
import com.joysee.adtv.logic.bean.ProgramType;

import java.util.ArrayList;

public class LiveGuideWindow extends BasicWindow implements IDvbBaseView {

    private static final DvbLog log = new DvbLog(
            "LiveGuideWindow", DvbLog.DebugType.D);
    
    public static final int RES_BITMEP = 0;
    private Activity mActivity;
    private LiveGuideView liveGuideView = null;
    private PopupWindow epgGuideWindow;
    private ViewController mViewController;
    private ADTVProgramGuideDoc mDoc;
    private ImageView[][] mBitmapArray;
    /**
     * 一级菜单List
     */
    private ArrayList<ProgramType> mHeaderTitleList;
    /**
     * 从TS流获取当前时间值(毫秒)
     * @return
     */
    public static long getUtcTime() {
        String utcTimeStr = SettingManager.getSettingManager().nativeGetTimeFromTs();
        String[] utcTime = utcTimeStr.split(":");
        long currentTimeMillis = Long.valueOf(utcTime[0]) * 1000;
        log.D("---getUtcTime-- utcTimeStr = " + utcTimeStr + " currentTimeMillis = "
                + currentTimeMillis);
        return currentTimeMillis;
    }
    
    
    public LiveGuideWindow(Activity activity) {
        mActivity = activity;
        mDoc = new ADTVProgramGuideDoc();
        setDoc(mDoc);
    }

    @Override
    public void processMessage(Object sender, DvbMessage msg) {
        switch (msg.what) {
            case ViewMessage.SHOW_LIVE_GUIDE:
                long begintime = System.currentTimeMillis();
                mViewController = (ViewController) sender;
                liveGuideView = new LiveGuideView(mActivity);
                liveGuideView.setViewController(mViewController);
                showProgramGuide();
                mHeaderTitleList = new ArrayList<ProgramType>();
                mHeaderTitleList = mViewController.getProgramTypes(0xFF);
                log.D(" SHOW_LIVE_GUIDE = " + mHeaderTitleList.toString());
                liveGuideView.init(0, this, mHeaderTitleList);
                log.D(" processMessage use time = " + (System.currentTimeMillis() - begintime));
                break;
        }
    }

    public void showProgramGuide() {
        if (epgGuideWindow == null) {
            epgGuideWindow = new PopupWindow();
        }
        epgGuideWindow.setContentView(liveGuideView);
        epgGuideWindow.setWidth((int) mActivity.getResources().getDimension(
                R.dimen.program_guide_popupwindow_width));
        epgGuideWindow.setHeight((int) mActivity.getResources().getDimension(
                R.dimen.program_guide_popupwindow_height));
        epgGuideWindow.setFocusable(true);
        epgGuideWindow.showAtLocation(mActivity.getWindow().getDecorView(), Gravity.CENTER, 0, 0);
    }
    
    protected void dismiss() {
        if (epgGuideWindow != null && epgGuideWindow.isShowing()) {
            clearDialog();
            epgGuideWindow.dismiss();
            mViewController.exitLiveGuide();
        }
    }
    
    public void clearDialog() {
    }
    
    protected void onDocGotResource(ADTVResource res) {
        switch (res.getType()) {
            case RES_BITMEP:
                if (mBitmapArray != null) {
                    for (int i = 0; i < mBitmapArray.length; i++) {
                        for (int j = 0; j < mBitmapArray[i].length; j++) {
                            if (mBitmapArray[i][j].hashCode() == res.getID()) {
                                mBitmapArray[i][j].setImageBitmap(res.getBitmap());
                                if (liveGuideView != null) {
                                    liveGuideView.notifyDataChange(i);
                                }
                            }
                        }
                    }
                }
                break;
        }
    }

    public void getBitmap(ImageView[][] bitmapArray, String[][] url) {
        log.D(" getBitmap bitmapArray = " + bitmapArray);
        if (bitmapArray != null) {
            mBitmapArray = bitmapArray;
            for (int i = 0; i < bitmapArray.length; i++) {
                for (int j = 0; j < bitmapArray[i].length; j++) {
                    mDoc.getPoster(bitmapArray[i][j].hashCode(), url[i][j]);
                }
            }
        }
    }
}
