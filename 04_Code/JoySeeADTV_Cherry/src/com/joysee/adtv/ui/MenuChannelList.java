package com.joysee.adtv.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.joysee.adtv.R;
import com.joysee.adtv.common.DefaultParameter.ServiceType;
import com.joysee.adtv.common.DvbLog;
import com.joysee.adtv.common.ToastUtil;
import com.joysee.adtv.controller.ViewController;
import com.joysee.adtv.ui.Menu.InterceptKeyListener;
import com.joysee.adtv.ui.Menu.MenuListener;
import com.joysee.adtv.ui.adapter.MenuExpandableAdapter;

public class MenuChannelList extends LinearLayout implements MenuListener{
	DvbLog log = new DvbLog("MenuChannelList",DvbLog.DebugType.D);
	
	private ExpandableListView mChannelListView;
	private MenuExpandableAdapter mMenuExpandableAdapter;
	private View mLastFocusView;
	private int mLastIndex = -1;

	private int mPosition;
	private int mMaxPosition = 5;

	private ArrayList<Integer> mTvChannelList = new ArrayList<Integer>();
	private ArrayList<Integer> mBcChannelList = new ArrayList<Integer>();
	private List<ArrayList<Integer>> mChild = new ArrayList<ArrayList<Integer>>();; // 子列表
	private int mLastFavorite = -2;

	private boolean mKeyRepeat = false;
	private int mKeyRepeatInterval = 100;
	private long mLastKeyTime;
	private boolean mExpanded;

	private ViewController mController;
	private Context mContext;
	private Handler mHandler = new Handler();
	private int mFirstSelection;
	
	
	private int mLastExpandGroup;
	private ImageView mExpandIcon;
	
	private boolean mFromCLKey;
	
	public MenuChannelList(Context context) {
		super(context);
	}

	public MenuChannelList(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	public MenuChannelList(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
	}

	@Override
	protected void onFinishInflate() {
		mChannelListView = (ExpandableListView) findViewById(R.id.menu_cl_list);
		mChannelListView.setOnGroupClickListener(new OnGroupClickListener() {
			public boolean onGroupClick(ExpandableListView parent, final View v,
					int groupPosition, long id) {
				int index = groupPosition % mTvChannelList.size();
				if( index != mTvChannelList.size()-1){
					Integer nativeChannelIndex = mTvChannelList.get(index);
					if(mBcChannelList.contains(nativeChannelIndex)){
						mController.switchChannelFromIndex(ServiceType.BC,nativeChannelIndex);
					}else{
						mController.switchChannelFromIndex(ServiceType.TV,nativeChannelIndex);
					}
					return true;
				}else{
					if(mChild.get(mTvChannelList.size()-1)!=null && mChild.get(mTvChannelList.size()-1).size() <= 0){
						ToastUtil.showToast(mContext, R.string.no_bc_channal);
						return true;
					}
					mExpanded = !mExpanded;
					if(mExpanded)
						mLastExpandGroup = groupPosition;//到其他设置列表时用于关闭广播频道
					log.D(" onGroupClick 展开关闭 "+mExpanded);
					mHandler.postDelayed(new Runnable() {
						@Override
						public void run() {
							mExpandIcon = (ImageView) v.findViewById(R.id.menu_clitem_expand_icon);
							if (!mExpanded) {
								mExpandIcon.setImageResource(R.drawable.menu_clitem_expand_focus);
							} else {
								mExpandIcon.setImageResource(R.drawable.menu_cltiem_shrink_focus);
							}
						}
					}, 100);
				}
				
				return false;
			}
		});
		mChannelListView.setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				if( groupPosition%mTvChannelList.size() == mTvChannelList.size()-1){
					mController.switchChannelFromIndex(ServiceType.BC,mBcChannelList.get(childPosition));
				}
				return false;
			}
		});

		mChannelListView.setOnItemSelectedListener(new OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> arg0, View view,
							int arg2, long arg3) {
						int location[] = new int[2];
						view.getLocationInWindow(location);
						log.D("mPosition  "+mPosition+" OnItemSelectedListener "+ location[1]);
						changeFocusView(view, arg2, true);
						if(mLastFocusView !=null)
							changeFocusView(mLastFocusView, mLastIndex, false);
						mLastFocusView = view;
						mLastIndex = arg2;
						
					}
					public void onNothingSelected(AdapterView<?> arg0) {
					}
				});
		mChannelListView.setOnGroupExpandListener(new OnGroupExpandListener() {
			public void onGroupExpand(final int groupPosition) {
				
				if(groupPosition % mTvChannelList.size() == mTvChannelList.size()-1){
					setDistanceFromTop(groupPosition);
					mPosition = 0;
				}
			}
		});
		super.onFinishInflate();
	}

	private void setDistanceFromTop(int groupPosition) {
		long pos = ExpandableListView.getPackedPositionForGroup(groupPosition);
		int flatpos = mChannelListView.getFlatListPosition(pos);
		mChannelListView.setSelectionFromTop(flatpos, 0);
	}

	public void fillData(int startIndex) {
		getNativeData();
		if (mMenuExpandableAdapter == null) {
			mMenuExpandableAdapter = new MenuExpandableAdapter(mContext,mTvChannelList, mChild, mController);
			mChannelListView.setAdapter(mMenuExpandableAdapter);
		}else{
			mMenuExpandableAdapter.setLastFavorite(mLastFavorite);
			mMenuExpandableAdapter.notifyDataSetChanged();
			log.D(" notifyDataSetChanged ");
		}
		mChannelListView.setSelection(mFirstSelection);
	}
	
	private void getNativeData(){
		mTvChannelList.clear();
		mBcChannelList.clear();
		mChild.clear();
		mLastFavorite  = mController.getFavouriteIndex().size()-1;
		mController.getAllChannelIndex(mTvChannelList, mBcChannelList);
		log.D("fillData mTvChannelList size= " + mTvChannelList.size()
				+ "  mBcChannelList size = " + mBcChannelList.size());
		if(mTvChannelList.size()<=0){
			return;
		}
		int mExpandPosition = mTvChannelList.size();
		mTvChannelList.add(mExpandPosition);

		mFirstSelection = Integer.MAX_VALUE / 2;
		for (int i = 0; i < mExpandPosition + 1; i++) {
			if (i == mExpandPosition)
				mChild.add(mExpandPosition, mBcChannelList);
			else
				mChild.add(null);
			if ((mFirstSelection + i) % mTvChannelList.size() == 0)
				mFirstSelection = mFirstSelection + i;
		}
		log.D(" mTvChannelList " + mTvChannelList.size() + " child " + mChild.size() );
	}

	public float getInterpolation(float input) {
		return (float) (Math.cos((input + 1) * Math.PI) / 2.0f) + 0.5f;
	}

	public boolean dispatchKeyEvent(KeyEvent event) {
		int keyCode = event.getKeyCode();
		int action = event.getAction();
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			final long currenKeyDownTime = SystemClock.uptimeMillis();
			final long interval = currenKeyDownTime - mLastKeyTime;
			if (mKeyRepeat && interval < mKeyRepeatInterval ){
				log.D(mKeyRepeat+" 抛弃");
				return true;
			}
			mKeyRepeat = true;
			mLastKeyTime = currenKeyDownTime;
			if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
				if(mLastFocusView!=null){
					changeFocusView(mLastFocusView, mLastIndex, false);
				}
				if(mPosition < mMaxPosition)
					mPosition++;
				if (mPosition == mMaxPosition ) {//长按时保持最后一个条目字体颜色为绿色
					int selectedItemPosition = mChannelListView.getSelectedItemPosition();
					int firstVisiblePosition = mChannelListView.getFirstVisiblePosition();
					View view = mChannelListView.getChildAt(selectedItemPosition-firstVisiblePosition+1);
					log.D("selectedPosition " + selectedItemPosition+" firstPosition"+firstVisiblePosition +" childAt "+(selectedItemPosition-firstVisiblePosition)+ " view "+view);
					if(view !=null ){
						changeFocusView(view, selectedItemPosition-firstVisiblePosition+1, true);
					}
					log.D(" down long action down");
				}
			}else if(keyCode == KeyEvent.KEYCODE_DPAD_UP){
				if(mLastFocusView!=null){
					changeFocusView(mLastFocusView, mLastIndex, false);
				}
				if(mPosition > 0)
					mPosition--;
				if (mPosition == 0 ) {//长按时保持第一个条目字体颜色为绿色
					int selectedItemPosition = mChannelListView.getSelectedItemPosition();
                	int firstVisiblePosition = mChannelListView.getFirstVisiblePosition();
                	View view = mChannelListView.getChildAt(selectedItemPosition-firstVisiblePosition-1);
                	if(view !=null ){
                		changeFocusView(view, selectedItemPosition-firstVisiblePosition-1, true);
                	}
				}
			}else if (keyCode == KeyEvent.KEYCODE_F9) {
				int count = mChannelListView.getChildCount();
				for(int i=0;i<count;i++){
					int[] location = new int[2];
					View v= mChannelListView.getChildAt(i);
					v.getLocationInWindow(location);
					log.D(" i "+i+" item top "+location[1]);
				}
			}
			if (mInterceptKeyListener != null && mInterceptKeyListener.onKeyEvent(keyCode, action)) {
				mKeyRepeat = false;
				return true;
			}
			return super.dispatchKeyEvent(event);
			
		}else if (event.getAction() == KeyEvent.ACTION_UP) {
			log.D("onKey up : " + mPosition);
			mKeyRepeat = false;	
		}
		if (mInterceptKeyListener != null && mInterceptKeyListener.onKeyEvent(keyCode, action)) {
			mKeyRepeat = false;
			return true;
		}

		return super.dispatchKeyEvent(event);
	}
	
	private InterceptKeyListener mInterceptKeyListener;

	public void setInterceptKeyListener(
			InterceptKeyListener interceptKeyListener) {
		mInterceptKeyListener = interceptKeyListener;
	}

	@Override
	public void getFocus(){
		fillData(0);
		mChannelListView.requestFocus();
		if(mFromCLKey){//直接按频道列表键时列表被挡住一块
			mChannelListView.setSelectionFromTop(mFirstSelection, 3);
			mFromCLKey = false;
		}else{
			mChannelListView.setSelection(mFirstSelection);
		}
		mExpanded = false;
	}
		
	@Override
	public void loseFocus() {
		mMenuExpandableAdapter.clearTaskMap();
		if(mExpanded){//关闭广播列表
			log.D("loseFocus collapseGroup "+mChannelListView.collapseGroup(mLastExpandGroup));
			mExpandIcon.setImageResource(R.drawable.menu_clitem_expand_focus);
		}
	}
	
	public void fillData(ViewController controller) {
		mController = controller;
		fillData(0);
	}
	
	public void changeFocusView(View view, int index, boolean isFocus) {
		TextView channelNumTv = (TextView) view.findViewById(R.id.menu_clitem_channel_num);
		TextView channelNameTv = (TextView) view.findViewById(R.id.menu_clitem_channel_name);
		TextView channelProgram = (TextView) view.findViewById(R.id.menu_clitem_program_name);
		TextView channelProgress = (TextView) view.findViewById(R.id.menu_clitem_program_progress);
		ImageView expandIcon = (ImageView) view.findViewById(R.id.menu_clitem_expand_icon);
		TextView expandText = (TextView) view.findViewById(R.id.menu_clitem_expand_text);
		if (isFocus) {
			channelNumTv.setTextColor(getResources().getColor(R.color.menu_list_focus));
			channelNameTv.setTextColor(getResources().getColor(R.color.menu_list_focus));
			channelProgram.setAlpha(1.0f);
			channelProgress.setAlpha(1.0f);
			index = index % mTvChannelList.size();
			if (index == mTvChannelList.size()-1) {
				log.D(" mExpandPosition has Focus");
				expandText.setTextColor(getResources().getColor(R.color.menu_list_focus));
				if (!mExpanded) {
					expandIcon.setImageResource(R.drawable.menu_clitem_expand_focus);
				} else {
					expandIcon.setImageResource(R.drawable.menu_cltiem_shrink_focus);
				}
			}
		} else {
			channelNumTv.setTextColor(getResources().getColor(R.color.menu_channel_info));
			channelNameTv.setTextColor(getResources().getColor(R.color.menu_channel_info));
			channelProgram.setAlpha(.4f);
			channelProgress.setAlpha(.4f);
			index = index % mTvChannelList.size();
			if (index == mTvChannelList.size()-1) {
				log.D(" mExpandPosition no Focus");
				expandText.setTextColor(getResources().getColor(R.color.menu_channel_info));
				if (!mExpanded) {
					expandIcon.setImageResource(R.drawable.menu_clitem_expand);
				} else {
					expandIcon.setImageResource(R.drawable.menu_cltiem_shrink);
				}
			}
		}
	}

	public void setFromChannelListKey() {
		mFromCLKey = true;
	}

}
