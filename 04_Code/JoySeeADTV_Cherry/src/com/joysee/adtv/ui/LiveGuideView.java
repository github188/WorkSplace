
package com.joysee.adtv.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Interpolator;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.TextView;

import com.joysee.adtv.R;
import com.joysee.adtv.common.DefaultParameter.ServiceType;
import com.joysee.adtv.common.DvbLog;
import com.joysee.adtv.controller.ViewController;
import com.joysee.adtv.logic.SettingManager;
import com.joysee.adtv.logic.bean.NETEventInfo;
import com.joysee.adtv.logic.bean.ProgramType;


import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;

public class LiveGuideView extends RelativeLayout implements View.OnFocusChangeListener{
    private static final DvbLog log = new DvbLog(
            "LiveGuideView", DvbLog.DebugType.D);
    private ViewPager mViewPager;
    private ArrayList<View> mPageViews;
    private View mParentView;
    TopMenuView mTopMenuView;
    private int mPageID = 0;
    private int mPageSize = 1;
    private int mProgramSize;
    private String TAG = "LiveGuideView";
    private int upKeyCount = 0;
    private int onePageSize = 8;
    private boolean canDispatchKey = true;
    private boolean canPageLeft = false;
    private boolean canPageRight = false;
    private LiveGuideWindow mGuideWindow;
    private final int mKeyRepeatInterval = 150;
    private long mLastKeyDownTime = -1;
    private boolean canKeyRight = true;
    private int mMenuItemID = -1;
    private int mLastItem = -1;
    private boolean canPressKey = true;
    private TextView pageNo;
    /**
     * 初始化菜单和第一个菜单下的内容
     */
    public static final int MSG_GET_HEADER = 1;
    /**
     * 更新选中菜单下的内容
     */
    public static final int MSG_REFESH_DATA = 2;
    /**
     * 获取缩略图
     */
    public static final int MSG_GET_BITMAP = 3;
    /**
     * 获取缩略图后通知UI更新
     */
    public static final int MSG_NOTIFYUI_UPDATE = 4;
    /**
     * 通知GridView更新UI
     */
    public static final int MSG_NOTIFY_GRIDVIEW = 5;
    public static final int MSG_SET_SELECTEDTEXT_COLOR = 6;
    /**
     * 通过它调用DvbController来调用JNI接口
     */
    private ViewController mViewController;
    /**
     * 一级菜单List
     */
    private ArrayList<ProgramType> mHeaderTitleList;
    /**
     * 各菜单对应的节目详情
     */
    private ArrayList<NETEventInfo> mProgramInfoList;
    
    /**
     * 选中的二级分类列
     */
    int whichRow = 0;
    /**
     * 二级分类总条目
     */
    int listSize ;
    private Context mContext;
    /**
     * 初始化时选择的一级分类
     */
    private int mWhichItem = 0;
    private int mDuration = 500;//设置viewPager移动速度

    /**
     * 缩略图
     */
    private ImageView [][] mPreviewIconArray;
    /**
     * 播放进度
     */
    private float [][] mPercentArray;
    /**
     * 节目名称
     */
    private String [][] mProgramNameArray;
    /**
     * 频道名称
     */
    private String [][] mChannelNameArray;
    /**
     * 缩略图地址
     */
    private String [][] mUrlArray;
    /**
     * 逻辑频道号
     */
    private int [][] mLogicNumberArray;
    /**
     * 是否即将开始标志 0为否 1为即将开始
     */
    private byte [][] mBeginingFlagArray;
    private long mCurrentTime;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_GET_HEADER:
                    if (getContentData(0)) {
                        initPagerView(mPageSize);
                    }
                    mTopMenuView.refreshSubView("" + (mPageID + 1) + "/" + mPageSize);
                    pageNo.setText("" + (mPageID + 1) + "/" + mPageSize);
                    mMenuItemID = 0;
                    canPressKey = false;
                    break;
                case MSG_REFESH_DATA:
                    if (mMenuItemID == msg.arg1) {
                        break;
                    }
                    mMenuItemID = msg.arg1;
                    reset();
                    if (getContentData(msg.arg1)) {
                        initPagerView(mPageSize);
                    }
                    mTopMenuView.refreshSubView("" + (mPageID + 1) + "/" + mPageSize);
                    pageNo.setText("" + (mPageID + 1) + "/" + mPageSize);
                    break;
                case MSG_GET_BITMAP:
                    mGuideWindow.getBitmap(mPreviewIconArray, mUrlArray);
                    break;
                case MSG_NOTIFYUI_UPDATE:
                    if (mPageViews != null && msg.arg1 < mPageViews.size()) {
                        ((GridViewAdapter) ((GridView) mPageViews.get(msg.arg1).findViewById(
                                R.id.grid_view))
                                .getAdapter()).notifyDataSetChanged();
                    }
                    break;
                case MSG_NOTIFY_GRIDVIEW:
                    GridView pageview = (GridView) msg.obj;
                    pageview.setFocusable(true);
                    pageview.requestFocus();
                    int count = pageview.getChildCount();
                    switch (msg.arg1) {
                        case 0://向左翻页
                            switch (upKeyCount) {
                                case 1:
                                    pageview.setSelection(3);
                                    pageview.requestFocus(3);
                                    break;
                                case 2:
                                    pageview.setSelection(7);
                                    pageview.requestFocus(7);
                                    break;
                            }
                            break;
                        case 1://向右翻页
                            if (count <= 4) {
                                upKeyCount = 1;
                            }
                            switch (upKeyCount) {
                                case 1:
                                    pageview.setSelection(0);
                                    pageview.requestFocus(0);
                                    break;
                                case 2:
                                    pageview.setSelection(4);
                                    pageview.requestFocus(4);
                                    break;
                            }
                            break;
                    }
                    if(currentSelectView!=null){
                        ((TextView) currentSelectView.findViewById(R.id.cell_view_program_tv)).setTextColor(getResources().getColor(R.color.white_txt));
                    }
                    currentSelectView=pageview.getSelectedView();
                    ((TextView) currentSelectView.findViewById(R.id.cell_view_program_tv)).setTextColor(getResources().getColor(R.color.green_txt));
                    int selected = pageview.getSelectedItemPosition();
                    log.D("2222--------------------- GridView selected = " +
                            selected + " count = " + count + " upKeyCount = " + upKeyCount);
                    mViewPager.setCurrentItem(mPageID);
                    break;
                case MSG_SET_SELECTEDTEXT_COLOR:
                    GridView pageview0 = (GridView) msg.obj;
                    currentSelectView=pageview0.getSelectedView();
                    ((TextView) pageview0.getSelectedView().findViewById(R.id.cell_view_program_tv))
                            .setTextColor(getResources().getColor(R.color.green_txt));
                    break;
            }
            super.handleMessage(msg);
        }
    };
    private String mTitleStr [];
    public LiveGuideView(Context context) {
        super(context);
        mContext = context;
    }
    
    public LiveGuideView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }
    
    public LiveGuideView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
    }
    private boolean pageFocus;
    public void initView() {
        mParentView = inflate(mContext, R.layout.liveguide_layout, this);
        // 初始化顶部菜单
        mTopMenuView = (TopMenuView) mParentView.findViewById(R.id.topmenu_view);
        Log.d(TAG, " initView mHeaderTitleList.size() = " + mHeaderTitleList.size());
        if (mHeaderTitleList != null && mHeaderTitleList.size() > 0) {
            mTitleStr = new String[mHeaderTitleList.size()];
            for (int i = 0; i < mTitleStr.length; i++) {
                mTitleStr[i] = mHeaderTitleList.get(i).getName();
            }
        }
        mTopMenuView.init(mWhichItem, this, mTitleStr);
        mTopMenuView.setHandler(mHandler);
        mTopMenuView.setOnFocusChangeListener(this);
        mViewPager = (ViewPager) mParentView.findViewById(R.id.programguide_videpager);
        pageNo = (TextView)mParentView.findViewById(R.id.page_no);
        mPageViews = new ArrayList<View>();
        mViewPager.setAdapter(new ViewPagerAdapter());
        mViewPager.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Log.d(TAG, "------------------------******************----------------mViewPager hasFocus="+hasFocus);
                if(hasFocus){
                    pageFocus=true;
                }else{
                    pageFocus=false;
                }
            }
        });
//        mViewPager.getAdapter().notifyDataSetChanged();
        mViewPager.setOnPageChangeListener(new ViewPagerChangeListener());
        try {
            Field mScroller;
            mScroller = (Field) ViewPager.class.getDeclaredField("mScroller");
            mScroller.setAccessible(true);
            ViewPagerScroller scroller = new ViewPagerScroller(mViewPager.getContext(),
                    new Interpolator() {
                        @Override
                        public float getInterpolation(float input) {
                            // TODO Auto-generated method stub
                            float result = (float) (Math.sqrt(2 * input - input * input));
                            return result;
                        }
                    });//
            mScroller.set(mViewPager, scroller);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mHandler.sendEmptyMessageDelayed(MSG_GET_HEADER, 800);
        mTopMenuView.refreshSubView("" + (mPageID + 1) + "/" + mPageSize);
        pageNo.setText("" + (mPageID + 1) + "/" + mPageSize);
        showErrorInfo(getResources().getString(R.string.programguide_loading));
    }
    /**
     * 初始化函数 在setViewController()之后调用
     * @param itemID 初始化item ID
     * @param window LiveGuideWindow
     * @param HeaderTitleList 一级菜单List
     */
    public void init(int itemID, LiveGuideWindow window, ArrayList<ProgramType> HeaderTitleList){
        mGuideWindow = window;
        mWhichItem = itemID;
//        mHeaderTitleList = new ArrayList<ProgramType>();
//        mContentList = new ArrayList<ProgramType>();
        mProgramInfoList = new ArrayList<NETEventInfo>();
        mHeaderTitleList = HeaderTitleList;
        initView();
    }
    
    private View currentSelectView;
    
    /**
     * 初始化PagerView
     */
    public void initPagerView(int length) {
        long begintime = System.currentTimeMillis();
        log.D("------------- initPagerView pager count = " + length );
//        mTopMenuView.refreshSubView("" + (mPageID + 1) + "/" + mPageSize);
        mPageViews.clear();
        mViewPager.removeAllViews();
        if (length <= 0) {
            length = 1;
        }
        View[] viewArray = new View[length];
        for (int i = 0; i < viewArray.length; i++) {
            viewArray[i] = inflate(mContext, R.layout.pagerview_item, null);
            GridView gridView = (GridView) viewArray[i].findViewById(R.id.grid_view);
            mPageViews.add(viewArray[i]);
            GridViewAdapter adapter = new GridViewAdapter(mContext);
            gridView.setAdapter(adapter);
            gridView.setOnFocusChangeListener(this);
            gridView.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
                    Log.d(TAG, "-----onKey----- canDispatchKey = " +
                            canDispatchKey + " canKeyRight = " + canKeyRight + " upKeyCount = "
                            + upKeyCount + " mLastItem = " + mLastItem);
                    //当在滑动过程中不能响应按键
                    if (!canDispatchKey) {
                        return true;
                    }
                    //当item是最后一个item时不让焦点移动到上面菜单
                    if (!canKeyRight || mLastItem == 1 || (mLastItem == 5 && upKeyCount == 2)) {
                        if (arg2.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
                            return true;
                        }
                    }
                    return false;
                }
            });
            gridView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    if (arg2 == 0 || arg2 == 4) {
                        canPageLeft = true;
                    } else if (arg2 == 3 || arg2 == 7) {
                        canPageRight = true;
                    } else {
                        canPageLeft = false;
                        canPageRight = false;
                    }
                    if (mLastItem == arg2 + 1) {
                        canKeyRight = false;
                    } else {
                        canKeyRight = true;
                    }
                    if(currentSelectView!=null){
                        ((TextView) currentSelectView.findViewById(R.id.cell_view_program_tv)).setTextColor(getResources().getColor(R.color.white_txt));
                    }
                    currentSelectView=arg1;
                    ((TextView) currentSelectView.findViewById(R.id.cell_view_program_tv)).setTextColor(getResources().getColor(R.color.green_txt));
                    Log.d(TAG, " ---------------- onItemSelected arg2 = " + arg2
                            + " canPageLeft = " + canPageLeft + " canPageRight = " + canPageRight
                            + " mLastItem = " + mLastItem);
                }
                
                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                    // TODO Auto-generated method stub
                     Log.d(TAG, " -----   onNothingSelected  " );
                }
            });
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    // TODO Auto-generated method stub
                    Log.d(TAG, " ---------------- onItemClick arg2 = " + arg2 + " mPageID = "
                            + mPageID + " number = " + mLogicNumberArray[mPageID][arg2]);
                    mViewController.switchChannelFromNum(ServiceType.TV,mLogicNumberArray[mPageID][arg2]);
                    quitView();
                }
            });
        }
        mViewPager.setCurrentItem(mPageID);
        mViewPager.getAdapter().notifyDataSetChanged();
        dismissErrorDialog();
        resetSelected();
        log.D(" initPagerView  end use time = " + (System.currentTimeMillis() - begintime));
    }
    /**
     * 左翻页
     */
    public void pageLeft() {
        Log.d(TAG, "-----pageLeft----- canPageLeft = " + canPageLeft);
        if (canPageLeft ) {
            mPageID--;
            if (mPageID < 0) {
                mPageID = 0;
                return;
            }
            GridView pageview = (GridView) mPageViews.get(mPageID).findViewById(R.id.grid_view);
            int selected = pageview.getSelectedItemPosition();
            int count = pageview.getChildCount();
            log.D(" 0000--------------------- GridView selected = " +
                    selected + " count = " + count + "  upKeyCount = " + upKeyCount);
            ((GridViewAdapter)
                    pageview.getAdapter()).notifyDataSetChanged();
            Message msg = new Message();
            msg.what = MSG_NOTIFY_GRIDVIEW;
            msg.arg1 = 0;
            msg.obj = pageview;
            mHandler.sendMessage(msg);
            canPageLeft = false;
            selected = pageview.getSelectedItemPosition();
            log.D("1111--------------------- GridView selected = " +
                    selected);
        }
    }
    /**
     * 右翻页
     */
    public void pageRight() {
        Log.d(TAG, "-----pageRight-----canPageRight = " + canPageRight);
        if (canPageRight ) {
            mPageID++;
            if (mPageID > (mPageViews.size() - 1)) {
                mPageID = mPageViews.size() - 1;
                return;
            }
            GridView pageview = (GridView) mPageViews.get(mPageID).findViewById(R.id.grid_view);
            int selected = pageview.getSelectedItemPosition();
            int count = pageview.getChildCount();
            log.D(" 0000--------------------- GridView selected = " +
                    selected + " count = " + count + "  upKeyCount = " + upKeyCount);
            ((GridViewAdapter)
                    pageview.getAdapter()).notifyDataSetChanged();
            Message msg = new Message();
            msg.what = MSG_NOTIFY_GRIDVIEW;
            msg.arg1 = 1;
            msg.obj = pageview;
            mHandler.sendMessage(msg);
            canPageRight = false;
            selected = pageview.getSelectedItemPosition();
            log.D("1111--------------------- GridView selected = " +
                    selected);
        }
    }
    /**
     * 获取一级分类下的内容
     * @param TitleID
     */
    public boolean getContentData(int TitleID) {
        log.D("----------- getContentData TitleID = " + TitleID + " mHeaderTitleList.size() = "
                + mHeaderTitleList.size());
        if (TitleID < mHeaderTitleList.size()) {
            mCurrentTime = LiveGuideWindow.getUtcTime();
            mProgramInfoList.clear();
            long begintime = System.currentTimeMillis();
            mProgramInfoList = mViewController.getProgramList(
                    mHeaderTitleList.get(TitleID)
                            .getId(), mCurrentTime, 0);
            log.D(" getProgramList use time is " + (System.currentTimeMillis() - begintime));
            mProgramSize = mProgramInfoList.size();
            if (mProgramSize % onePageSize == 0) {
                mPageSize = mProgramSize / onePageSize;
            } else {
                mPageSize = mProgramSize / onePageSize + 1;
            }                    
            log.D(" getContentData mProgramInfoList.size() = " + mProgramInfoList.size()
                    + " mPageCount = "
                    + mPageSize);
            if (mPageSize <= 0) {
                mPageSize = 1;
            }
            if (mProgramInfoList.size() <= 0) {
                // TODO :显示提示信息
                if (mPageViews != null && mViewPager != null) {
                    mPageViews.clear();
                    mViewPager.removeAllViews();
                    mViewPager.getAdapter().notifyDataSetChanged();
                    mViewPager.setCurrentItem(mPageID);
                }
                showErrorInfo(getResources().getString(R.string.programguide_nochannel));
                return false;
            }
            mPreviewIconArray = new ImageView[mPageSize][onePageSize];
            mPercentArray = new float[mPageSize][onePageSize];
            mProgramNameArray = new String[mPageSize][onePageSize];
            mChannelNameArray = new String[mPageSize][onePageSize];
            mUrlArray = new String[mPageSize][onePageSize];
            mBeginingFlagArray = new byte[mPageSize][onePageSize];
            mLogicNumberArray = new int[mPageSize][onePageSize];
            for (int i = 0; i < mPageSize; i++) {
                for (int j = 0; j < onePageSize; j++) {
//                    mProgramNameArray[i][j] = new StringBuffer();
//                    mChannelNameArray[i][j] = new StringBuffer();
//                    mUrlArray[i][j] = new StringBuffer();
                    mPreviewIconArray[i][j] = new ImageView(mContext);
                    mPreviewIconArray[i][j].setImageDrawable(getResources().getDrawable(
                            R.drawable.preview_default));
                }
            }
            long BeginTime ,currentTime;
            float duration;
            int row = 0, line = 0;
            for (int i = 0; i < mProgramInfoList.size(); i++) {
                row = i / onePageSize;
                line = i % onePageSize;
//                log.D(" getContentData i = " + i + " row = " + row + " line = " + line + " "
//                        + mProgramInfoList.get(i).toString());
                try {
//                    mUrlArray[row][line].append(mProgramInfoList.get(i)
//                            .getImgPath());
                    mUrlArray[row][line] = mProgramInfoList.get(i)
                            .getImgPath();
                    BeginTime = mProgramInfoList.get(i).getBegintime();
                    duration = mProgramInfoList.get(i).getDuration();
                    currentTime = mCurrentTime/1000;
                    if (BeginTime >= currentTime) {
                        mBeginingFlagArray[row][line] = 1;
                    } else {
                        if (BeginTime <= 0) {
                            mPercentArray[row][line] = 0;
                        } else {
                            mPercentArray[row][line] = (float)(currentTime - BeginTime) / duration;
                        }
                    }
//                    log.D("-------------------currentTime = "
//                            + currentTime + "----- mPercentArray = " + mPercentArray[row][line]);
//                    mProgramNameArray[row][line].append(mProgramInfoList
//                            .get(i)
//                            .getEname());
//                    mChannelNameArray[row][line].append(mProgramInfoList
//                            .get(i)
//                            .getChannelName());
                    mProgramNameArray[row][line] = mProgramInfoList
                            .get(i)
                            .getEname();
                    mChannelNameArray[row][line] = mProgramInfoList
                            .get(i)
                            .getChannelName();
                    mLogicNumberArray[row][line] = mProgramInfoList.get(i).getLogicNumer();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            log.D(" getProgramList use time is " + (System.currentTimeMillis() - begintime));
            mHandler.sendEmptyMessageDelayed(MSG_GET_BITMAP, 1000);
            dismissErrorDialog();
        } else {
            log.E(" getContentData " + TitleID + " mHeaderTitleList.size() = "
                    + mHeaderTitleList.size());
            dismissErrorDialog();
            showErrorInfo(getResources().getString(R.string.programguide_nochannel));
            return false;
        }
        return true;
    }
    /**
     * 重置数据
     */
    public void reset() {
        mPageSize = 1;
        mPageID = 0;
        mLastItem = -1;
        upKeyCount = 0;
        dismissErrorDialog();
    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (canPressKey) {
            return true;
        }
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            final long currenKeyDownTime = SystemClock.uptimeMillis();
            final long interval = currenKeyDownTime - mLastKeyDownTime;
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    if (interval < mKeyRepeatInterval) {
                        return true;
                    }
                    mLastKeyDownTime = currenKeyDownTime;
                    upKeyCount++;
                    if (upKeyCount > 2) {
                        upKeyCount = 2;
                    }
                    break;
                case KeyEvent.KEYCODE_DPAD_UP:
                    if (interval < mKeyRepeatInterval) {
                        return true;
                    }
                    mLastKeyDownTime = currenKeyDownTime;
                    if (upKeyCount == 0) {
                        return true;
                    }
                    upKeyCount--;
                    if (upKeyCount < 0) {
                        upKeyCount = 0;
                    }
                    if (upKeyCount == 0) {
                        if(currentSelectView!=null){
                            ((TextView) currentSelectView.findViewById(R.id.cell_view_program_tv)).setTextColor(getResources().getColor(R.color.white_txt));
                        }
                        mTopMenuView.setCanFocus(true);
                        mTopMenuView.setFocusView();
                        canPageLeft = false;
                        canPageRight = false;
                        return true;
                    }
                    break;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    if (interval < mKeyRepeatInterval) {
                        return true;
                    }
                    mLastKeyDownTime = currenKeyDownTime;
                    pageLeft();
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    if (interval < mKeyRepeatInterval) {
                        return true;
                    }
                    mLastKeyDownTime = currenKeyDownTime;
                    pageRight();
                    break;
                // case KeyEvent.KEYCODE_HOME:
                // case KeyEvent.KEYCODE_ESCAPE:
                case KeyEvent.KEYCODE_BACK:
                    quitView();
                    break;
            }
        }else if(event.getAction()==KeyEvent.ACTION_UP){
            if(event.getKeyCode()==KeyEvent.KEYCODE_MENU){
                quitView();
                mViewController.showMainMenu();
            }
        }
        return super.dispatchKeyEvent(event);
    }
    
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        switch(v.getId()){
            case R.id.grid_view:
//                Log.d(TAG,
//                        " onFocusChange GridView " + " mEpgWeekView isFocus = "
//                                + mTopMenuView.isFocus() + " canDispatchKey = " + canDispatchKey);
                break;
            case R.id.topmenu_view:
//                Log.d(TAG, " onFocusChange mEpgWeekView " + hasFocus);
                break;
                
        }
    }
    
    public void resetSelected() {
        Log.d(TAG, "---resetSelected---- mPageViews = " + mPageViews);
        if (mPageViews == null||mPageViews.size()<=0) {
            return;
        }
        GridView pageview = (GridView) mPageViews.get(mPageID).findViewById(R.id.grid_view);
        pageview.setSelection(0);
        pageview.setFocusable(true);
        pageview.requestFocus(0);
        currentSelectView=pageview.getSelectedView();
        if(currentSelectView!=null){
            ((TextView) currentSelectView.findViewById(R.id.cell_view_program_tv)).setTextColor(getResources().getColor(R.color.green_txt));
        }else{
            Message msg = new Message();
            msg.obj = pageview;
            msg.what = MSG_SET_SELECTEDTEXT_COLOR;
            mHandler.sendMessageDelayed(msg, 200);
        }
//        if (pageview.getSelectedView() != null) {
//            TextView textview = ((TextView) pageview.getSelectedView().findViewById(
//                    R.id.cell_view_program_tv));
//            if (textview != null) {
//                textview.setTextColor(getResources().getColor(R.color.green_txt));
//            }
//        } else {
//            Message msg = new Message();
//            msg.obj = pageview;
//            msg.what = MSG_SET_SELECTEDTEXT_COLOR;
//            mHandler.sendMessageDelayed(msg, 200);
//        }
        upKeyCount = 1;
        canPageLeft = true;
        whichRow = (mPageID * onePageSize) / 2;
    }
    private void quitView(){
        mGuideWindow.dismiss();
        dismissErrorDialog();
    }
    /**
     * 获取二级菜单
     * @param programType
     */
    public void getPlayingOrBeginningList(ArrayList<ProgramType> programType){
        
    }
    public class GridViewAdapter extends BaseAdapter {
        public GridViewAdapter(Context c) {
            mContext = c;
        }
        
        public int getCount() {
            int count = onePageSize;
            if (mPageID + 1 == mPageSize) {
                count = mProgramSize % 8;
                if (count == 0) {
                    count = onePageSize;
                }
                mLastItem = count;
            }else{
                mLastItem = -1;
            }
//            log.D("------- getCount ------------- mPageID = " + mPageID + " count = " + count
//                    + " mLastItem = " + mLastItem);
            return count;
        }
        
        public Object getItem(int position) {
            return position;
        }
        
        public long getItemId(int position) {
            return position;
        }
        
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHodler viewHodler = null;
            if (convertView == null) {
                convertView = inflate(mContext, R.layout.cell_view, null);
                viewHodler = new ViewHodler();
                viewHodler.previewImage = (ImageView) convertView
                        .findViewById(R.id.cell_view_preview_iv);
                viewHodler.progressImage = (ImageView) convertView
                        .findViewById(R.id.cell_view_progress_iv);
                viewHodler.beginIconImage = (ImageView) convertView
                        .findViewById(R.id.cell_view_begin_icon);
                viewHodler.programName = (TextView) convertView
                        .findViewById(R.id.cell_view_program_tv);
                viewHodler.channelName = (TextView) convertView
                        .findViewById(R.id.cell_view_channel_tv);
                convertView.setTag(viewHodler);
            } else {
                viewHodler = (ViewHodler) convertView.getTag();
            }
//            log.D(" getView mPageID = " + mPageID + " position = " + position + " programName = "
//                    + mProgramNameArray[mPageID][position] + " channelName = "
//                    + mChannelNameArray[mPageID][position]);
            if (mPreviewIconArray != null && mPreviewIconArray[mPageID][position] != null) {
                Bitmap bitMap = ((BitmapDrawable) mPreviewIconArray[mPageID][position]
                        .getDrawable()).getBitmap();
                if (bitMap != null) {
                    viewHodler.previewImage
                            .setImageBitmap(bitMap);
                } else {
                    viewHodler.previewImage.setImageDrawable(getResources().getDrawable(
                            R.drawable.guide_poster));
                }
            } else {
                viewHodler.previewImage.setImageDrawable(getResources().getDrawable(
                        R.drawable.guide_poster));
            }
            if (mBeginingFlagArray != null && mBeginingFlagArray[mPageID][position] == 1) {
                viewHodler.beginIconImage.setVisibility(View.VISIBLE);
            }else{
                viewHodler.beginIconImage.setVisibility(View.GONE);
            }
            LayoutParams para;
            para = (LayoutParams) viewHodler.progressImage.getLayoutParams();
            para.width = (int) (390 * mPercentArray[mPageID][position]);
            viewHodler.progressImage.setLayoutParams(para);
            viewHodler.programName.setText(""+mProgramNameArray[mPageID][position]);
            viewHodler.channelName.setText(""+mChannelNameArray[mPageID][position]);
            return convertView;
        }
    }
    
    class ViewHodler {
        ImageView previewImage;
        ImageView progressImage;
        ImageView beginIconImage;
        TextView programName;
        TextView channelName;
    }


    class ViewPagerAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return mPageViews.size();
        }
        
        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }
        @Override
        public int getItemPosition(Object object) {
//            return super.getItemPosition(object);
            return POSITION_NONE;
        }
        
        @Override
        public void destroyItem(View arg0, int arg1, Object arg2) {
            if (arg1 < mPageViews.size()) {
                ((ViewPager) arg0).removeView(mPageViews.get(arg1));
            }
        }
        
        @Override
        public Object instantiateItem(View arg0, int arg1) {
            ((ViewPager) arg0).addView(mPageViews.get(arg1));
            return mPageViews.get(arg1);
        }
        
        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {
        }
        
        @Override
        public Parcelable saveState() {
            return null;
        }
        
        @Override
        public void startUpdate(View arg0) {
        }
        
        @Override
        public void finishUpdate(View arg0) {
        }
    }
    
    /**
     * 指引页面更改事件监听器
     * @author wuhao
     */
    class ViewPagerChangeListener implements OnPageChangeListener {
        @Override
        /**
         * 开始前调用一次 结束时调用一次,最先或者最后调用
         * arg0 2表示开始滑动 0表示滑动完毕
         */ 
        public void onPageScrollStateChanged(int arg0) {
            log.D(" onPageScrollStateChanged arg0 = " + arg0);
            if (arg0 == 0) {
                GridView pageview = (GridView) mPageViews.get(mPageID).findViewById(
                        R.id.grid_view);
                int selected = pageview.getSelectedItemPosition();
                if (selected == 0 || selected == 4) {
                    canPageLeft = true;
                }
                if (selected == 3 || selected == 7) {
                    canPageRight = true;
                }
                if (selected == 0 || selected == 3) {
                    upKeyCount = 1;
                }
                if (selected == 4 || selected == 7) {
                    upKeyCount = 2;
                }
                canDispatchKey = true;
                Log.d(TAG, " onPageScrollStateChanged selected = " + selected
                        + " canPageLeft = "
                        + canPageLeft + " canPageRight = " + canPageRight + " upKeyCount = " + upKeyCount);
            } else {
                canDispatchKey = false;
            }
        }
        
        @Override
        /**
         * 滑动过程中调用
         * arg0 滑动前page和滑动后page
         * arg1 滑动时间
         * arg2 滑动距离
         */
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }
        
        @Override
        /**
         * 开始前调用一次,晚于onPageScrollStateChanged调用
         * arg0 滑动后选中的page
         */
        public void onPageSelected(int arg0) {
            Log.d(TAG, " ---- onPageSelected arg0 = " + arg0);
            mPageID = arg0;
            if (mTopMenuView != null) {
                mTopMenuView.refreshSubView("" + (mPageID+1) + "/" + mPageSize);
                pageNo.setText("" + (mPageID+1) + "/" + mPageSize);
            }
        }
    }
    /**
     * 重载滑动器
     * @author wuhao
     */
    public class ViewPagerScroller extends Scroller {
        public ViewPagerScroller(Context context) {
            super(context);
        }
        public void setFixedDuration(int i) {
            // TODO Auto-generated method stub
            mDuration = i;
        }
        public ViewPagerScroller(Context context, Interpolator interpolator) {
            super(context, interpolator);
        }
        public ViewPagerScroller(Context context, Interpolator interpolator, boolean flywheel) {
            super(context, interpolator);
        }
        @Override
        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
            // Ignore received duration, use fixed one instead
            super.startScroll(startX, startY, dx, dy, mDuration);
        }
        @Override
        public void startScroll(int startX, int startY, int dx, int dy) {
            // Ignore received duration, use fixed one instead
            super.startScroll(startX, startY, dx, dy, mDuration);
        }
    }
    
    public void setViewController(ViewController mViewController) {
        this.mViewController = mViewController;
    }
    
    public void notifyDataChange(int page) {
        if (page == mPageID) {
            log.D(" notifyDataChange page = " + page + " mPageID = " + mPageID);
            Message msg = new Message();
            msg.what = MSG_NOTIFYUI_UPDATE;
            msg.arg1 = page;
            mHandler.sendMessage(msg);
        }
    }
    
    private Dialog mErrorDialog;
    private View mErrorView;
    private TextView mErrorText;
    
    private void showErrorInfo(String errorString) {
        log.D(" begin showErrorInfo " + errorString);
        if (mErrorView == null || mErrorText == null) {
            mErrorView = ((Activity) mContext).getLayoutInflater().inflate(
                    R.layout.dvb_ca_notify_layout, null);
            mErrorText = (TextView) mErrorView.findViewById(R.id.notify_text);
        }
        mErrorText.setText(errorString);
        final int windowHeight = (int) mContext.getResources().getDimension(
                R.dimen.alert_dialog_no_button_height);
        final int windowWidth = (int) mContext.getResources().getDimension(
                R.dimen.alert_dialog_no_button_width);
        if (mErrorDialog == null) {
            mErrorDialog = new Dialog(mContext, R.style.dvbErrorDialogTheme);
        }
        mErrorDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        mErrorDialog.setContentView(mErrorView,
                new LayoutParams(windowWidth, windowHeight));
        mErrorDialog.show();
        log.D(" end showErrorInfo " + errorString);
    }
    
    private void dismissErrorDialog() {
        log.D(" ---------dismissErrorDialog------- ");
        if (mErrorDialog != null && mErrorDialog.isShowing()) {
            mErrorDialog.dismiss();
        }
    }
}
