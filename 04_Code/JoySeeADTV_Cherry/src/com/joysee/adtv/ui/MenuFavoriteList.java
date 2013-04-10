package com.joysee.adtv.ui;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.joysee.adtv.R;
import com.joysee.adtv.common.DefaultParameter.FavoriteFlag;
import com.joysee.adtv.common.DvbKeyEvent;
import com.joysee.adtv.common.DvbLog;
import com.joysee.adtv.common.DvbLog.DebugType;
import com.joysee.adtv.controller.ViewController;
import com.joysee.adtv.logic.bean.DvbService;
import com.joysee.adtv.ui.Menu.InterceptKeyListener;
import com.joysee.adtv.ui.Menu.MenuListener;
import com.joysee.adtv.ui.adapter.FChannelWindowListAdapter;

public class MenuFavoriteList extends FrameLayout implements MenuListener {

	private ListView mFavListView;
	private TextView mAddTextView;
	private ImageView mFocusView;
	private InterceptKeyListener mInterceptKeyListener;
	private FChannelWindowListAdapter mFChannelPopupMenuListAdapter;
	private DvbLog log = new DvbLog("MenuFavoriteList", DebugType.D);
	private ViewController mController;
	private Context mContext;
	private HashMap<Integer, DvbService> mDelFavMap;
	private ImageView mAddTextIcon;
	private int favo;
	private static final int UNFAVORITE = 0;
	private static final int FAVORITE = 1;
	private final int OFFSET = (int) getResources().getDimension(R.dimen.menu_margin_top);
	private static final int MAX_FAV_COUNT = 5;
	private boolean isListCantSelected = true;
	private boolean isFirstIn = true;
	private View mLastFocusView;
	/**
	 * mFavListView的第一个item，用来解决从按钮向下时不走onItemSelected方法问题。
	 */
	private View mFirstFocusView;

	public MenuFavoriteList(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.mContext = context;
	}

	public MenuFavoriteList(Context context, AttributeSet attrs) {
		this(context, attrs,0);
	}

	public MenuFavoriteList(Context context) {
		this(context,null);
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mFavListView = (ListView) findViewById(R.id.favorites_list);
		mAddTextView = (TextView) findViewById(R.id.menu_fav_add_textview);
		mFocusView = (ImageView) findViewById(R.id.ivFocus);
		mAddTextIcon = (ImageView) findViewById(R.id.menu_fav_add_text_icon);
		mAddTextView.setOnFocusChangeListener(onFocusChangeListener);
		mDelFavMap = new HashMap<Integer, DvbService>();
		mFavListView.setFocusable(false);
	}
	
	public void fillData(Activity activity,ViewController controller){
		mController = controller;
		reset();
		setListener();
		log.D("showFavoritesWindow cur.getCount() = " + mController.getFavouriteIndex().size());
		mFChannelPopupMenuListAdapter = new FChannelWindowListAdapter(activity, mController);
		mFavListView.setAdapter(mFChannelPopupMenuListAdapter);
		favo = FavoriteFlag.FAVORITE_NO;
		DvbService curChannels = mController.getCurrentChannel();
		if (curChannels != null) {
			mAddTextView.setEnabled(true);
			favo = curChannels.getFavorite();
		} else {
			mAddTextView.setEnabled(false);
		}
		log.D("showFavoritesWindow mfavoBtn favo = " + (favo == 1 ? true : false));
		mAddTextView.setText(favo == FavoriteFlag.FAVORITE_YES ? R.string.dvb_delete_favorite
				: R.string.dvb_set_favorite);
		mAddTextIcon.setImageResource(favo == FavoriteFlag.FAVORITE_YES ? R.drawable.menu_fav_cancel_focus
				: R.drawable.menu_fav_add_focus);
		mAddTextView.requestFocus();
	}

	private void setListener() {
		mFavListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final DvbService service = (DvbService) parent.getAdapter().getItem(position);
				ImageView imageView = (ImageView) view.findViewById(R.id.dvb_channelwindow_listitem_cfavourite);
				int channelNum = mController.getChannelNum();
				int tag = (Integer)imageView.getTag();
				if(tag == UNFAVORITE){
					imageView.setBackgroundDrawable(null);
					imageView.setImageDrawable(getResources().getDrawable(R.drawable.menu_fav_icon));
					imageView.setTag(FAVORITE);
					mDelFavMap.remove(service.getServiceId());
					if(channelNum == service.getLogicChNumber()){
						mAddTextView.setText(R.string.dvb_delete_favorite);
						mAddTextIcon.setImageResource(R.drawable.menu_fav_cancel_unfocus);
					}
					alert(mContext,getResources().getString(R.string.menu_fav_add_success));
				}else{
					imageView.setBackgroundDrawable(null);
					imageView.setImageDrawable(getResources().getDrawable(R.drawable.menu_fav_icon_canceled));
					imageView.setTag(UNFAVORITE);
					mDelFavMap.put(service.getServiceId(), service);
					if(channelNum == service.getLogicChNumber()){
						mAddTextView.setText(R.string.dvb_set_favorite);
						mAddTextIcon.setImageResource(R.drawable.menu_fav_add_unfocus);
					}
					alert(mContext,getResources().getString(R.string.menu_fav_remove_success));
				}
			}
		});
		
		mFavListView.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				if(position == 0)
					mFirstFocusView = view;
				if(position == 0 && isListCantSelected)
					return;
				if(!isFirstIn){
					if(mLastFocusView!= null){
						setFocusTextColor(false, mLastFocusView);
					}
					setFocusTextColor(true, view);
				}
				int [] location = new int [2];
				view.getLocationInWindow(location);
				MarginLayoutParams params = (MarginLayoutParams) mFocusView
						.getLayoutParams();
				params.topMargin = location[1] - OFFSET;
				Log.d("songwenxuan","onItemSelected(),params.topMargin = " + params.topMargin);
				mFocusView.setVisibility(View.VISIBLE);
				mFocusView.setLayoutParams(params);
//				Animation anim = new AlphaAnimation(0.0f, 1.0f);
//				anim.setDuration(300);
//				anim.setFillAfter(true);
//				anim.setFillEnabled(true);
//				mFocusView.startAnimation(anim);
				mLastFocusView = view;
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {}
		});

		mFavListView.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				log.D("on key ....");
				final int action = event.getAction();
				switch (keyCode) {
				case KeyEvent.KEYCODE_VOLUME_DOWN:
				case KeyEvent.KEYCODE_VOLUME_UP:
					if (action == KeyEvent.ACTION_DOWN) {
						if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
							mController.changeVolume(-1);
						else
							mController.changeVolume(1);
						return true;
					}
					break;
				}
				return false;
			}
		});

		mAddTextView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				removeDeletedFav();
				isListCantSelected = true;
				log.D("mFChannelPopupMenuButton onClick ...");
				if (mAddTextView.getText().equals(getResources().getString(R.string.dvb_delete_favorite))) {
					log.D("delete from favorites list ..."); // 删除当前频道从喜爱列表中
					mController.setChannelFavorite(false);
					mAddTextView.setText(R.string.dvb_set_favorite);
					mAddTextIcon.setImageResource(R.drawable.menu_fav_add_focus);
					alert(mContext,getResources().getString(R.string.menu_fav_remove_success));
				} else {
					if(mController.getFavoriteCount() == MAX_FAV_COUNT){
						alert(mContext,getResources().getString(R.string.menu_fav_over_max_count));
						return;
					}
					log.D("add to favorites list ..."); // 添加当前频道到喜爱列表中
					mController.setChannelFavorite(true);
					mAddTextView.setText(R.string.dvb_delete_favorite);
					mAddTextIcon.setImageResource(R.drawable.menu_fav_cancel_focus);
					alert(mContext, getResources().getString(R.string.menu_fav_add_success));
				}
				final ArrayList<Integer> fIndexs = mController.getFavouriteIndex();
				mFChannelPopupMenuListAdapter.setData(fIndexs);
				mFChannelPopupMenuListAdapter.notifyDataSetChanged();
				mController.refreshChannelInfo();
			}
		});

		mAddTextView.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				switch (keyCode) {
				case KeyEvent.KEYCODE_HOME:
					if (event.getAction() == KeyEvent.ACTION_DOWN) {
						log.D("mFChannelPopupMenuButton onKeyListener HOME is press");
					}
					break;
				case KeyEvent.KEYCODE_DPAD_DOWN:
					if (event.getAction() == KeyEvent.ACTION_DOWN) {
						if(mFavListView.getCount() ==0)
							return true;
						isFirstIn = false;
						mFavListView.setFocusable(true);
						mFavListView.requestFocus();
						isListCantSelected = false;
						MarginLayoutParams params = (MarginLayoutParams) mFocusView
								.getLayoutParams();
						params.topMargin = (int) getResources().getDimension(R.dimen.menu_favorite_list_first_item_margin);
						mFocusView.setLayoutParams(params);
						setFocusTextColor(true, mFirstFocusView);
						mLastFocusView = mFirstFocusView;
					}
					break;
				}
				return false;
			}
		});
	}

	private void reset() {
		isListCantSelected = true;
		isFirstIn = true;
		mDelFavMap.clear();
	}

	public void setInterceptKeyListener(InterceptKeyListener interceptKeyListener) {
		this.mInterceptKeyListener = interceptKeyListener;
	}
	
	@Override
	public void getFocus() {
		int [] location = new int [2];
		mAddTextView.getLocationInWindow(location);
		MarginLayoutParams params = (MarginLayoutParams) mFocusView
				.getLayoutParams();
		params.topMargin = location[1] - OFFSET;
		mFocusView.setLayoutParams(params);
		mAddTextView.requestFocus();
	}

	@Override
	public void loseFocus() {
		
	}
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if(event.getKeyCode() == KeyEvent.KEYCODE_ESCAPE || event.getKeyCode() == KeyEvent.KEYCODE_BACK){
			log.D("event.getKeyCode() == KeyEvent.KEYCODE_ESCAPE" );
			removeDeletedFav();
		}else if(event.getKeyCode() == DvbKeyEvent.KEYCODE_FAVORITE && event.getAction() == KeyEvent.ACTION_UP){
			mInterceptKeyListener.exitMenu();
			return true;
		}
		
		if(mInterceptKeyListener.onKeyEvent(event.getKeyCode(), event.getAction())){
			return true;
		}
		return super.dispatchKeyEvent(event);
	}
	
	private OnFocusChangeListener onFocusChangeListener = new OnFocusChangeListener() {
		
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if(hasFocus){
				if(mLastFocusView!= null){
					setFocusTextColor(false,mLastFocusView);
				}
				isListCantSelected = true;
				mFavListView.setFocusable(false);
				int [] location = new int [2];
				v.getLocationInWindow(location);
				MarginLayoutParams params = (MarginLayoutParams) mFocusView
						.getLayoutParams();
				params.topMargin = location[1] - OFFSET;
				Log.d("songwenxuan","params.topMargin = " + params.topMargin);
				if(isListCantSelected)
					params.topMargin = (int) getResources().getDimension(R.dimen.menu_favorite_list_title_margin);
				mFocusView.setLayoutParams(params);
				mFocusView.setVisibility(View.VISIBLE);
//				Animation anim = new AlphaAnimation(0.0f, 1.0f);
//				anim.setDuration(300);
//				anim.setFillAfter(true);
//				anim.setFillEnabled(true);
//				mFocusView.startAnimation(anim);
				
				mAddTextView.setTextColor(getResources().getColor(R.color.menu_list_focus));
				if(favo == FavoriteFlag.FAVORITE_NO)
					mAddTextIcon.setImageResource(R.drawable.menu_fav_add_focus);
				else
					mAddTextIcon.setImageResource(R.drawable.menu_fav_cancel_focus);
				
			}else{
				mAddTextView.setTextColor(getResources().getColor(R.color.menu_text_unfocus));
				if(favo == FavoriteFlag.FAVORITE_NO)
					mAddTextIcon.setImageResource(R.drawable.menu_fav_add_unfocus);
				else
					mAddTextIcon.setImageResource(R.drawable.menu_fav_cancel_unfocus);
			}
		}
	};
	
	private void removeDeletedFav(){
		for (Integer key : mDelFavMap.keySet()) {
			mController.removeChannelFavorite(mDelFavMap.get(key).getLogicChNumber(), mDelFavMap.get(key).getServiceType()&0x0F);
		}
		mDelFavMap.clear();
	}
	
	private void setFocusTextColor(boolean isFocus , View view){
		TextView channelNameTextView =  (TextView) view.findViewById(R.id.dvb_channelwindow_listitem_cname);
		TextView channelNumTextView =  (TextView) view.findViewById(R.id.dvb_channelwindow_listitem_cnum);
		if(isFocus){
			channelNameTextView.setTextColor(getResources().getColor(R.color.menu_list_focus));
			channelNumTextView.setTextColor(getResources().getColor(R.color.menu_list_focus));
		}else{
			channelNameTextView.setTextColor(getResources().getColor(R.color.menu_text_unfocus));
			channelNumTextView.setTextColor(getResources().getColor(R.color.menu_text_unfocus));
			
		}
	}
	
	private  Dialog mAlertDialog;
	private static final int DISMISS = 1000;
	private  Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case DISMISS:
				mAlertDialog.dismiss();
				break;

			default:
				break;
			}
			super.handleMessage(msg);
		}
	};

	public void alert(Context context, String text) {
		View view = LayoutInflater.from(context).inflate(
				R.layout.alert_dialog_no_button_layout, null);
		TextView textView = (TextView) view.findViewById(R.id.alert_text);
		textView.setText(text);
		if (mAlertDialog == null) {
			mAlertDialog = new Dialog(context, R.style.alertDialogTheme);
		}
		mAlertDialog.setContentView(
				view,
				new LayoutParams(
						(int) context.getResources().getDimension(R.dimen.alert_dialog_no_button_width), 
						(int) context.getResources().getDimension(R.dimen.alert_dialog_no_button_height)
						)
				);
		mAlertDialog.show();
		mAlertDialog.setOnKeyListener(new Dialog.OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				mHandler.removeMessages(DISMISS);
				mAlertDialog.dismiss();
				return false;
			}
		});
		mHandler.sendEmptyMessageDelayed(DISMISS, 3000);
	}

}
