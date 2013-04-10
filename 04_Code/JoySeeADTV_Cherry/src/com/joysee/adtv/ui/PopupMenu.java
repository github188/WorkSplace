package com.joysee.adtv.ui;

import java.util.Stack;

import com.joysee.adtv.common.DvbLog;

import android.content.Context;
import android.graphics.Color;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;


public class PopupMenu extends PopupWindow {
	
	private static final String TAG = "com.joysee.adtv.ui.PopupMenu";
	private static final DvbLog log = new DvbLog(TAG, DvbLog.DebugType.D);
	private OnBackKeyPressedListener mBackPressedLis;
	private OnInterceptKeyEvent mInterceptKeyListener;
	public class PopupMenuItem {
	    /** 频道列表 */
	    public static final int CHANNEL_LIST = 0;
	    /** 节目指南 */
	    public static final int PROGRAM_GRUIDE = 1;
	    /** 预约列表 */
	    public static final int ORDER_LIST = 2;
	    /** 喜爱列表 */
	    public static final int FAVORITE_CHANNEL = 3;
	    /** 画面设置 */
	    public static final int SCREEN_SETTING = 4;
	    /** 声道设置 */
	    public static final int VOLUME_TRANSFER = 5;
	    /** 多语音切换 伴音*/
	    public static final int AUDIOINDEX_TRANSFER = 6;
	    /** 智能卡设置 */
	    public static final int CARD_MANAGER = 7;
	    /** 频道搜索 */
	    public static final int CHANNEL_SEARCH = 8;
	    /** 广播节目 */
	    public static final int BROADCASE_PROGRAM = 9;
	    
	    //对广播菜单列表
	    /** 广播列表 */
	    public static final int BROADCAST_CHANNEL_LIST =0;
	    /** 广播画面设置 */
	    public static final int BROADCAST_SCREEN_SETTING = Integer.MAX_VALUE;
	    /** 喜爱列表 */
	    public static final int BROADCAST_FAVORITE_CHANNEL = 1;
	    /** 广播声道设置 */
	    public static final int BROADCAST_VOLUME_TRANSFER = 2;
	    /** 广播多语音切换 */
	    public static final int BROADCAST_AUDIOINDEX_TRANSFER = 3;
	    /** 直播电视 */
	    public static final int PLAY_TV_PROGRAM = 4;
	}

	
	private Context mContext;
	private View mCurrentView = null;
	private Stack<View> mViewStack = new Stack<View>();
	private Stack<View> mFViewStack = new Stack<View>();
	private Stack<Integer> mFViewSelectionStack = new Stack<Integer>();
	private PopupMenuContainer mMenuView = null;
	
	public  PopupMenu(Context context) {
		this(context,new View(context));
	}

	public PopupMenu(Context context, View view) {
		super(context);
		this.mContext = context;
		setContentView(view);
	}
	
	private void clearStack(){
		mViewStack.clear();
		mFViewStack.clear();
		mFViewSelectionStack.clear();
	}
	
	@Override
	public void setContentView(View view) {
		if (mMenuView == null)
			mMenuView = new PopupMenuContainer(mContext);
		mMenuView.removeAllViews();
		mMenuView.addView(view);
		mCurrentView = view;
		clearStack();
		super.setContentView(mMenuView);
		setBackgroundDrawable(null);
	}
	
	@Override
	public void dismiss() {
		clearStack();
		mMenuView.removeAllViews();
		super.dismiss();
	}

	public boolean backToParentMenu() {
		if (mViewStack.size() <= 0)
			return false;
		View parent = mViewStack.pop();
		View fView = mFViewStack.pop();
		int selection = mFViewSelectionStack.pop();
		mCurrentView = parent;
		mMenuView.removeAllViews();
		mMenuView.addView(parent);
		
		if(fView != null){
			fView.requestFocus();
			if(fView instanceof ListView && selection != -1){
				((ListView)fView).setSelection(selection);
			}
		}
		return true;
	}

	public void displaySubMenu(View item, View focus, int selection) {
		mViewStack.push(mCurrentView);
		mFViewStack.push(focus);
		mFViewSelectionStack.push(selection);
		mCurrentView = (View) item;
		mMenuView.removeAllViews();
		mMenuView.addView(item);
	}
	
	public void setOnBackPressedLis(OnBackKeyPressedListener lis){
		if (lis != null && lis != mBackPressedLis)
			this.mBackPressedLis = lis;
	}

	public class PopupMenuContainer extends LinearLayout {
		
		private long mLastKeyDownTime = -1;
		private boolean isKeyRepeating = false;
		private long mKeyRepeatInterval = 120L;
		private int mLastDownKeyCode = -1;
		
		public PopupMenuContainer(Context context) {
			super(context);
			this.setClickable(true);
			this.setBackgroundColor(Color.TRANSPARENT);
		}

		@Override
		public boolean dispatchKeyEvent(KeyEvent event) {
			final int keyCode = event.getKeyCode();
			final int action = event.getAction();
			log.D("keyCode = " + keyCode);
			log.D( "action = " + action);
            //TODO :解决mmm编译问题,但是以后是否在frameworks添加KEYCODE_DVB键?---by wuhao 2012-11-05
            // if (keyCode == KeyEvent.KEYCODE_DVB) {
            // return true;
            // }
			if (action == KeyEvent.ACTION_DOWN) {
				if (mLastDownKeyCode == keyCode) {
					final long currenKeyDownTime = SystemClock.uptimeMillis();
					final long interval = currenKeyDownTime - mLastKeyDownTime;
					if (isKeyRepeating && interval < mKeyRepeatInterval) {
						log.D( "PopupMenuContainer dispatchKeyEvent drop KeyEvent");
						return true;
					}
					mLastKeyDownTime = currenKeyDownTime;
					isKeyRepeating = true;
				}
				mLastDownKeyCode = keyCode;
			}else{
				isKeyRepeating = false;
			}
			
			if (mInterceptKeyListener != null){
				boolean ret = mInterceptKeyListener.onIntercept(this, keyCode, event);
				if (ret)
					return true;
			}
			if ((keyCode == KeyEvent.KEYCODE_ESCAPE || keyCode == KeyEvent.KEYCODE_BACK) && action == KeyEvent.ACTION_UP) {
				log.D( "MenuView onKeyDown back is press");
				boolean ret = backToParentMenu();
				if (!ret)
					dismiss();
				
				if (mBackPressedLis != null)
					return mBackPressedLis.onBackPress();
				return true;
			}

			return super.dispatchKeyEvent(event);
		}

		@Override
		public boolean onKeyDown(int keyCode, KeyEvent event) {
			if (keyCode == KeyEvent.KEYCODE_ESCAPE || keyCode == KeyEvent.KEYCODE_BACK) {
				log.D( "MenuView onKeyDown back is press");
				return true;
			}
			return super.onKeyDown(keyCode, event);
		}
	}
	
	public interface OnBackKeyPressedListener{
		boolean onBackPress();
	}
	
	public interface OnInterceptKeyEvent {
		boolean onIntercept(View v, int keyCode, KeyEvent event);
	}
	
	public void setInterceptKeyListener(OnInterceptKeyEvent lis){
		this.mInterceptKeyListener = lis;
	}
	
	public void setOnTouchListener (OnTouchListener lis){
		this.mMenuView.setOnTouchListener(lis);
	}
}
