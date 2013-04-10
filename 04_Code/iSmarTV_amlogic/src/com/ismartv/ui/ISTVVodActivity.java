package com.ismartv.ui;

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import android.widget.*;
import android.view.*;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.graphics.PixelFormat;
import android.graphics.Canvas;
import android.view.animation.AnimationUtils;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.AdapterView.OnItemClickListener;
import android.text.InputType;
import java.util.Timer;
import java.util.TimerTask;
import android.os.Message;
import android.os.Looper;
import android.os.Handler;
import android.provider.Settings;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.animation.Animation;
import android.widget.ArrayAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;

import com.ismartv.doc.ISTVResource;
import com.ismartv.doc.ISTVDoc;
import com.ismartv.client.ISTVClient;
import com.ismartv.service.ISTVError;
import com.ismartv.ui.ISTVVodItemDetail.VodReceiver;
import com.ismartv.util.Constants;
import com.ismartv.util.StringUtils;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import com.lenovo.tvcustom.lenovo.LenovoService;
import com.lenovo.leos.push.PsAuthenServiceL;

public class ISTVVodActivity extends Activity implements ISTVDoc.Callback ,  OnAudioFocusChangeListener{
	private static final String TAG="ISTVVodActivity";

	public static final int RES_STR_ACTIVE_ERROR=9996;
	public static final int RES_STR_LOGIN_ERROR=9997;
	public static final int RES_BOOL_LOGIN=9998;
	public static final int RES_STR_ERROR=9999;

	public static final int DIALOG_OK_CANCEL=0;
	public static final int DIALOG_RETRY_CANCEL=1;
	public static final int DIALOG_IKNOW=2;
	public static final int DIALOG_NET_BROKEN=3;
	public static final int DIALOG_CANNOT_GET_DATA=4;
	public static final int DIALOG_LOGIN=5;
	public static final int DIALOG_ITEM_CLICK_NET_BROKEN=6;
	public static final int DIALOG_ACTIVE=7;

	public View rootView=null;
	private ISTVVodMenu menu=null;
	private ArrayList<VodTimer> rmTimerList=null;
	private String error;
	private ISTVDoc doc=null;
	private VodTimer loadErrTimer=null;
	private VodTimer checkNetTimer=null;
	private VodTimer volPanelTimer=null;
	private VodTimer errorCheckTimer=null;
	private boolean errorCheck=false;
	private int volPanelCounter=0;
	private boolean netConnected=true;
	public boolean isActive=false;
	private BaseReceiver baseReceiver;
	public boolean isPlayer=false;
	
	public static HashMap<String,Drawable> mapBG=new HashMap<String,Drawable>();

	private void stopErrorCheckTimer(){
		if(errorCheckTimer!=null){
			errorCheckTimer.remove();
			errorCheckTimer=null;
		}
	}

	private void restartErrorCheck(){
		errorCheck = false;
		stopErrorCheckTimer();
		errorCheckTimer = addVodTimer(9996, 30);
	}

	private Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			if(msg.what==1982){
				for(VodTimer t : timers){
					t.now--;
					if(t.now==0){
						boolean restart=false;

						if(t==loadErrTimer){
							if(doc!=null){
//								doc.reloadError();失败不再重新加载
							}
							restart=true;
						}else if(t==checkNetTimer){
							if(doc!=null){
								try{
									boolean conn = isNetConnected();
									Log.d(TAG, "net="+conn+" ;netConnected="+netConnected+" "+popupDlg+" "+isActive);
									if(conn != netConnected){
										if(conn && popupDlg!=null){
											popupDlg.dismiss();
											doc.reloadError();
										}
										popupDlgBlockMask = 0;
										netConnected = conn;

										if(conn){
											onNetConnected();
											if(popupDlgType==DIALOG_NET_BROKEN && popupDlg!=null){
											    popupDlg.dismiss();
											}
										}else{
											onNetDisconnected();
											if(isActive){
												showPopupDialog(DIALOG_NET_BROKEN, getResources().getString(R.string.vod_net_broken_error));
											}
										}
									}
								}catch(Exception e){
								}
							}
							restart=true;
						}else if(t==volPanelTimer){
							volPanelCounter--;
							if(volPanelCounter<=0){
								hideVolumePanel();
							}
							restart=true;
						}else if(t==errorCheckTimer){
							errorCheck = true;
							stopErrorCheckTimer();
						}else{
							restart = onVodTimer(t);
						}

						if(restart){
							t.now = t.sec;
						}else{
							if(rmTimerList==null){
								rmTimerList = new ArrayList<VodTimer>();
							}
							rmTimerList.add(t);
						}
					}
				}

				if(rmTimerList!=null){
					for(VodTimer t : rmTimerList){
						timers.remove(t);
					}
				}
			}else if(msg.what==RES_STR_ACTIVE_ERROR){
			    Log.d(TAG, "------RES_STR_ACTIVE_ERROR----msg.arg1="+msg.arg1);
				if(msg.arg1==0){
					showPopupDialog(DIALOG_ACTIVE, getResources().getString(R.string.vod_get_data_error));
				}else if(msg.arg1==1){
					showPopupDialog(DIALOG_ACTIVE, getResources().getString(R.string.vod_get_data_error));
				}
			}
		}
	};

	private Timer timer=null;
	
	public void onGotResource(ISTVResource res){
		switch(res.getType()){
			case RES_BOOL_LOGIN:
				//hideBuffer();
				if(popupDlg == null) {
					break;
				}
				ImageView image = (ImageView)popupDlg.findViewById(R.id.BufferImage);
				if(image!=null){
					image.setVisibility(View.GONE);
				}
				TextView text = (TextView)popupDlg.findViewById(R.id.text);				
				Message message;
				if(res.getBoolean()){
					Log.d(TAG, "login success");		
					if(text!=null){
						text.setText(R.string.vod_login_success);
					}
					message = loginHandler.obtainMessage(1984);
					loginHandler.sendMessageDelayed(message, 2000);	
				}else{
					Log.d(TAG, "login failed");
					if(text!=null){
						text.setText(R.string.vod_login_failed);
					}
					message = loginHandler.obtainMessage(1985);
					loginHandler.sendMessageDelayed(message, 2000);	
				}				
				//showBuffer();
				
				break;
			case RES_STR_LOGIN_ERROR:
				String login_error = res.getString();				
				if(login_error == null){
				    hideBufferLogin();
					break;
				}
				Log.d(TAG, "RES_STR_LOGIN_ERROR="+login_error);
				if(login_error.equals("USS-0100")){
					nameerror.setText(R.string.account_name_format_error);
				}else if(login_error.equals("USS-0101")){
					nameerror.setText(R.string.account_password_error);
				}else if(login_error.equals("USS-0103")){
					nameerror.setText(R.string.vod_login_usererror);
				}else if(login_error.equals("USS-0105")){
					nameerror.setText(R.string.account_name_not_active);
				}else if(login_error.equals(("USS-0111"))){
					nameerror.setText(R.string.account_name_disable);
				}else if(login_error.equals("USS-0151")){
					nameerror.setText(R.string.account_name_locked);
				}else if(login_error.equals("USS-0-1")){
					nameerror.setText(R.string.account_connect_error);
				}else if(login_error.equals("USS-0403")){
					nameerror.setText(R.string.account_proxy_authorization);
				}else if(login_error.equals("USS-0407")){
					nameerror.setText(R.string.account_proxy_refused);
				}				
				break;
			case RES_STR_ERROR:
				int type = res.getID();
				String msg = res.getString();

				showPopupDialog(type, msg);
				break;
			case RES_STR_ACTIVE_ERROR:
				Log.d(TAG, "-------RES_STR_ACTIVE_ERROR------");
				if(isNetConnected()){
					int ty=res.getID();
					Message mes=handler.obtainMessage(RES_STR_ACTIVE_ERROR);
					mes.arg1=ty;
					handler.sendMessageDelayed(mes, 1000);
				}
				break;
		}
//		Log.d(TAG, "onGotResource :" + res.getType());
		onDocGotResource(res);
	}

	public void onUpdate(){
		onDocUpdate();
	}

	protected void onDocGotResource(ISTVResource res){
	}

	protected void onDocUpdate(){
	}

	class VodTimer{
		int sec;
		int now;
		int id;

		public VodTimer(int id, int sec){
			this.id  = id;
			this.sec = sec;
			this.now = sec;

			if(timer==null){
				timer = new Timer();
				timer.schedule(new TimerTask() {
				public void run() {
					Message msg = handler.obtainMessage(1982);
					handler.sendMessage(msg);
					}
				}, 1*1000, 1*1000);
			}

			timers.add(this);
		}

		public int getID(){
			return id;
		}

		public void remove(){
			if(rmTimerList==null){
				rmTimerList = new ArrayList<VodTimer>();
			}
			rmTimerList.add(this);
		}
	};

	private HashSet<VodTimer> timers = new HashSet<VodTimer>();

	private boolean winOK=false;
	private void createWindow(){
		View win;

		if(winOK)
			return;

		ViewGroup root = (ViewGroup)findViewById(Window.ID_ANDROID_CONTENT);
		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		win = inflater.inflate(R.layout.menu, null);
		ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		win.setLayoutParams(lp);
		root.addView(win);

		winOK=true;
	}

	public void setDoc(ISTVDoc doc){
		this.doc = doc;
		doc.registerCallback(this);
		doc.registerActivity(this);

		if(loadErrTimer==null){
			loadErrTimer = addVodTimer(9999, 5);
		}
		if(checkNetTimer==null){
			checkNetTimer = addVodTimer(10000, 10);
		}

		restartErrorCheck();
	}

	public boolean isNetConnected(){
		boolean ret=false;

		try{
			ConnectivityManager cManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE); 
			NetworkInfo info = cManager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
			if (info != null && (info.getState()==NetworkInfo.State.CONNECTED)){
				ret = true;
			}
			if(!ret){
				info = cManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
				if (info != null && (info.getState()==NetworkInfo.State.CONNECTED)){
					ret = true;
				}
			}
		}catch(Exception e){
		}

		return ret;
	}

	private static Dialog popupDlg=null;
	private int popupDlgType=0;
	private static int popupDlgBlockMask=0;
	
	public void closeDialog(int type){
	    if(type==popupDlgType && popupDlg !=null){
	        popupDlg.dismiss();
	    }
	}

	public void showPopupDialog(int type, String msg){
		Log.d(TAG,">>>>>>>>> showPopupDialog popupDlg:" + popupDlg +"/msg:" + msg + "/type:" + type );
		
		if(popupDlg!=null)
			return;
		
		Log.d(TAG,">>>>>>>>>> showPopupDialog type:" + type +"/popupDlgBlockMask:" + popupDlgBlockMask );
		if(type==DIALOG_ACTIVE){
			popupDlgBlockMask=0;
		}
		Log.d(TAG, "1<<type="+(1<<type)+";popupDlgBlockMask="+popupDlgBlockMask+";(1<<type)&popupDlgBlockMask="+((1<<type)&popupDlgBlockMask));
		if(((1<<type)&popupDlgBlockMask)!=0){
			return;
		}

		Log.d(TAG,">>>>>>>>>showPopupDialog errorCheck:" + errorCheck +"/isActive:" + isActive +";!errorCheck || !isActive="+(!errorCheck || !isActive)+";type="+type);
		
//		if((type==DIALOG_CANNOT_GET_DATA) && (!errorCheck || !isActive))
//			return;
		if(!isActive){
		    return;
		}

		Log.d(TAG, "create popup dialog");
		
		popupDlgType=type;

		View view;
		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		if((type==DIALOG_IKNOW) || (type==DIALOG_NET_BROKEN)||(type==DIALOG_ITEM_CLICK_NET_BROKEN)||type==DIALOG_ACTIVE){
			view = inflater.inflate(R.layout.popup_1btn, null);
		}else{
			view = inflater.inflate(R.layout.popup_2btn, null);
		}

		if(popupDlg==null)
		    popupDlg = new Dialog(this, R.style.PopupDialog);
		
		//popupDlg.getWindow().setLayout(1920, 1080);
		popupDlg.addContentView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

		TextView tv = (TextView)view.findViewById(R.id.PopupText);
		tv.setText(msg);

		Button btn1=null, btn2=null;
		switch(type){
			case DIALOG_NET_BROKEN:
//				popupDlgBlockMask |= (1<<DIALOG_NET_BROKEN);
//				popupDlgBlockMask |= (1<<DIALOG_CANNOT_GET_DATA);
			case DIALOG_ITEM_CLICK_NET_BROKEN:	
			case DIALOG_IKNOW:
			case DIALOG_ACTIVE:
				btn1 = (Button)view.findViewById(R.id.LongButton);
				btn1.setText(R.string.vod_i_know);
				
				break;
			case DIALOG_OK_CANCEL:
				btn1 = (Button)view.findViewById(R.id.LeftButton);
				btn1.setText(R.string.vod_ok);
				btn2 = (Button)view.findViewById(R.id.RightButton);
				btn2.setText(R.string.vod_cancel);
				break;
			case DIALOG_CANNOT_GET_DATA:
//				popupDlgBlockMask |= (1<<DIALOG_CANNOT_GET_DATA);
			case DIALOG_RETRY_CANCEL:
				btn1 = (Button)view.findViewById(R.id.LeftButton);
				btn1.setText(R.string.vod_retry);
				btn2 = (Button)view.findViewById(R.id.RightButton);
				btn2.setText(R.string.vod_cancel);
				break;
			default:
				break;
		}

		if(btn1!=null){
			btn1.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
				    Log.d(TAG, "-------------------onclick-------------");
					onPopupDialogClicked(0);
					if(!isPlayer)
					    hideBuffer();

					if(popupDlgType==DIALOG_CANNOT_GET_DATA){
						if(doc!=null){
							doc.reloadError();
						}
						restartErrorCheck();
					}

					if(popupDlg!=null){
						popupDlg.dismiss();
					}
				};
			});
		}

		if(btn2!=null){
			btn2.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					onPopupDialogClicked(1);
					if(!isPlayer)
					    hideBuffer();

					if(popupDlg!=null){
						popupDlg.dismiss();
					}
				};
			});
		}

		popupDlg.setOnDismissListener(new DialogInterface.OnDismissListener(){
			public void onDismiss(DialogInterface dialog){
				popupDlg = null;
			}
		});
/*		
		popupDlg.setCancelable(true);
		popupDlg.setOnCancelListener( new DialogInterface.OnCancelListener(){
			@Override
                public void onCancel(DialogInterface Dialog ) {
                	                	                	
                	ISTVVodActivity.this.finish();	
                	
                	}
		});*/
		popupDlg.show();
	}

	public boolean isNeedLogin(){
		boolean ret=false;
		int lenovo_logout = 1;
		int lenovo_login = 2;

		/*lenovo*/
		int lenovo_login_status = PsAuthenServiceL.getStatus(ISTVVodActivity.this);
		Log.d(TAG, "lenovo_login_status " + lenovo_login_status);
		
		if(lenovo_login_status == lenovo_logout){
			ret = true;

			Log.d(TAG, "need login lenovo");
		}else if(lenovo_login_status == lenovo_login){
			if(!(doc.needlogin())){
				Log.d(TAG, "not need login");
			}else{
				String name_ret = PsAuthenServiceL.getUserName(ISTVVodActivity.this);

				doc.login(name_ret, null);

				Log.d(TAG, "need login vod and vod name:" + name_ret);
			}
		}

		return ret;
	}

	static EditText edit_name = null, edit_password = null;
	static String name = null, password = null;
	static TextView nameerror;
	
    private RelativeLayout bufferLayout = null;
    private AnimationDrawable bufferAnim = null;
	
	public void showPopupLoginDialog(int type){
		
		Log.d(TAG, "$$$$$$$$$$$ show popup dialog "+type);
		if(popupDlg!=null)
			return;

		/*if(((1<<type)&popupDlgBlockMask)!=0){
			return;
		}

		if((type==DIALOG_CANNOT_GET_DATA) && !errorCheck)
			return;*/

		Log.d(TAG, "create popup dialog");
		popupDlgType=type;

		View view;
		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		if(type==DIALOG_LOGIN){
			view = inflater.inflate(R.layout.popup_login, null);
		}else{
			return;
		}

		bufferLayout = (RelativeLayout)view.findViewById(R.id.BufferLayout);
		bufferAnim = (AnimationDrawable)((ImageView)view.findViewById(R.id.BufferImage)).getBackground();
		hideBufferLogin();
		
		popupDlg = new Dialog(this, R.style.PopupDialog);
		popupDlg.addContentView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

		nameerror = (TextView)view.findViewById(R.id.PopupLoginTextUserNameError);
		nameerror.setText("");
		
		Button  btn1=null, btn2=null;
		TextView btn_findpassword = null;
		final TextView btn_findpassword_down = (TextView)view.findViewById(R.id.PopupLoginTextPasswordError);
		switch(type){
			case DIALOG_LOGIN:
				
				btn_findpassword = (TextView)view.findViewById(R.id.PopupLoginBtnPasswordError);
				btn_findpassword.setClickable(true);
				btn_findpassword.setFocusable(true);
				btn1 = (Button)view.findViewById(R.id.LeftButton);
				btn2 = (Button)view.findViewById(R.id.RightButton);

				edit_name = (EditText)view.findViewById(R.id.PopupLoginEditUserName);
				edit_password = (EditText)view.findViewById(R.id.PopupLoginEditUserpassword);		

				edit_password.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);

				break;
			default:
				break;
		}

		if(btn_findpassword!=null){
			btn_findpassword.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					String settingPwd = getPassword();
					Log.d(TAG, "-------------------settingPws="+settingPwd);
					if(settingPwd==null||settingPwd.equals("")){
						Intent it = new Intent();
						it.setAction(Intent.ACTION_VIEW);
						Bundle mBundle = new Bundle();   
						mBundle.putInt("curChoice", 9);
						mBundle.putString("subChoice", "forgetPassword");
						it.putExtras(mBundle);
						it.putExtra("from", "vod");
						it.setClassName("com.lenovo.settings", "com.lenovo.settings.LenovoSettingsActivity");
						startActivity(it);

						if(popupDlg!=null){
							popupDlg.dismiss();
						}
					}else{
						showSettingPasswordDialog(1);
					}
				};
			});
			btn_findpassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
				public void onFocusChange(View v, boolean hasFocus) {
					Log.d(TAG, "onFocusChange hasFocus="+hasFocus);
					if(hasFocus == true) {
						btn_findpassword_down.setVisibility(View.VISIBLE);
					} else {
						btn_findpassword_down.setVisibility(View.INVISIBLE);
					}					
				}
			});
		}

		if(btn1!=null){
			btn1.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					onPopupDialogClicked(0);

					name = edit_name.getText().toString();
					password = edit_password.getText().toString();
					
					if(StringUtils.isBlank(name)){ 
						//TODO show tip dialog
						onGotResource(new ISTVResource(ISTVVodActivity.RES_STR_LOGIN_ERROR, 0, "USS-0100"));
						edit_name.requestFocus();
						return ;
					}
					
					Log.d(TAG, " name " + name + " password " + password);	

					doc.login(name, password);
					ImageView image = (ImageView)popupDlg.findViewById(R.id.BufferImage);
					image.setVisibility(View.VISIBLE);
					TextView text = (TextView)popupDlg.findViewById(R.id.text);
					text.setText(R.string.vod_Logining);					
					showBufferLogin();
				};
			});
		}

		if(btn2!=null){
			btn2.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					onPopupDialogClicked(1);
					String settingPwd = getPassword();
					Log.d(TAG, "-------------------settingPws="+settingPwd);
					if(settingPwd==null||settingPwd.equals("")){
						Intent it = new Intent();
						it.setAction(Intent.ACTION_VIEW);
						Bundle mBundle = new Bundle();   
						mBundle.putInt("curChoice", 9);
						mBundle.putString("subChoice", "register");
						it.putExtras(mBundle);
						it.putExtra("from", "vod");
						it.setClassName("com.lenovo.settings", "com.lenovo.settings.LenovoSettingsActivity");
						startActivity(it);

						if(popupDlg!=null){
							popupDlg.dismiss();
						}
					}else{
						showSettingPasswordDialog(2);
					}
				};
			});
		}

		popupDlg.setOnDismissListener(new DialogInterface.OnDismissListener(){
			public void onDismiss(DialogInterface dialog){
				popupDlg = null;
			}
		});
		
		popupDlg.setCancelable(true);
		popupDlg.setOnCancelListener( new DialogInterface.OnCancelListener(){
			@Override
                public void onCancel(DialogInterface Dialog ) {
				Log.d(TAG, "popupDlgType============="+popupDlgType);
					if(popupDlgType==DIALOG_LOGIN||popupDlgType==DIALOG_ITEM_CLICK_NET_BROKEN){
						onBackPopupDlg();
					}
                }
		});
		popupDlg.show();
	}
	
	Dialog settingDialog;
	/**
	 * 1为点找回密码，2为点注册
	 * @param type
	 */
	public void showSettingPasswordDialog(final int type){
		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.setting_password, null);
		settingDialog = new Dialog(this, R.style.PopupDialog);
		settingDialog.addContentView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		final EditText text=(EditText)view.findViewById(R.id.settingPasswordEdit);
		final TextView error=(TextView)view.findViewById(R.id.settingPasswordError);
		Button btn1,btn2;
		btn1 = (Button)view.findViewById(R.id.LeftButton);
		btn2 = (Button)view.findViewById(R.id.RightButton);
		btn1.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				String password=text.getText().toString();
				if(password==null||password.equals("")){
					text.requestFocus();
					return;
				}
				String settingPwd = getPassword();
				Log.d(TAG, "-------------------settingPws="+settingPwd+";password="+password);
				if(settingPwd.equals(password)){
					if(type==1){
						Intent it = new Intent();
						it.setAction(Intent.ACTION_VIEW);
						Bundle mBundle = new Bundle();   
						mBundle.putInt("curChoice", 9);
						mBundle.putString("subChoice", "forgetPassword");
						it.putExtras(mBundle);
						it.putExtra("from", "vod");
						it.setClassName("com.lenovo.settings", "com.lenovo.settings.LenovoSettingsActivity");
						startActivity(it);
					}else if(type==2){
						Intent it = new Intent();
						it.setAction(Intent.ACTION_VIEW);
						Bundle mBundle = new Bundle();   
						mBundle.putInt("curChoice", 9);
						mBundle.putString("subChoice", "register");
						it.putExtras(mBundle);
						it.putExtra("from", "vod");
						it.setClassName("com.lenovo.settings", "com.lenovo.settings.LenovoSettingsActivity");
						startActivity(it);
					}
					if(popupDlg!=null){
                        popupDlg.dismiss();
                    }
					settingDialog.dismiss();
				}else{
					error.setVisibility(View.VISIBLE);
					text.requestFocus();
				}
			}
		});
		btn2.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				settingDialog.dismiss();
			}
		});
		settingDialog.setOnDismissListener(new DialogInterface.OnDismissListener(){
			public void onDismiss(DialogInterface dialog){
			    settingDialog = null;
			}
		});
		
		settingDialog.setCancelable(true);
		settingDialog.show();
	}
	
//	private static final String DEFAULT_PASSWORD = "123456";
    static final String AUTHORITY = "com.joysee.launcher.settings";
    static final String TABLE_APPMENU = "appmenu";
    static final String PARAMETER_NOTIFY = "notify";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_APPMENU + "?" + PARAMETER_NOTIFY + "=true");
	
	private String getPassword() {
        Cursor cursor = this.getContentResolver().query(CONTENT_URI,new String[]{"launchpwd"},"itemType=? and appName=?",new String[]{"1","系统设置"}, null);
        if(cursor != null && cursor.moveToFirst()){
            Log.d(TAG, "---------cursor.getString(0)="+cursor.getString(0));
            return cursor.getString(0);
        }else{
            Log.d(TAG, "-----------------cursor is null ");
            return null;
        }
    }

	
	public void onBackPopupDlg(){
		Log.d(TAG, "++++++++++++++++onBackPopupDlg");
	}

	public void cancelPopupDialog(){
		if(popupDlg!=null){
			popupDlg.dismiss();
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		IntentFilter filter=new IntentFilter();
        filter.addAction(Constants.INTENT_HOME_KEY);
        baseReceiver=new BaseReceiver();
        this.registerReceiver(baseReceiver, filter);
	}
	
	@Override
	protected void onDestroy() {
		if(baseReceiver!=null){
			this.unregisterReceiver(baseReceiver);
		}
		super.onDestroy();
	}

	public void onPopupDialogClicked(int which){
	}
	
	public void showBuffer(){}
    public void hideBuffer(){
        Log.d(TAG, "-------------------hideBuffer-------------");
    }
	
/*
	@Override
	public boolean onCreateOptionsMenu(Menu m) {

		boolean ret;

		m.add("menu");
		ret = super.onCreateOptionsMenu(m);

		if(ret && menu==null){
			createWindow();
			menu = new ISTVVodMenu(this);
			ret = onCreateVodMenu(menu);
		}

		return ret;		
	}
*/
	@Override
	public boolean onPrepareOptionsMenu (Menu m) {
		boolean ret;
		if(menu!=null && menu.isVisible())
			return false;
		m.add("menu");
		ret = super.onPrepareOptionsMenu(m);

		if(ret){
			winOK = false;
			createWindow();
			menu = new ISTVVodMenu(this);
			ret = onCreateVodMenu(menu);
		}

		return ret;	
	}
	
	@Override
	public boolean onMenuOpened(int featureId, Menu m) {		
		if(onVodMenuOpened(menu)){
			menu.show();
		}
		return false;
	}

	public VodTimer addVodTimer(int id, int sec){
		VodTimer timer = new VodTimer(id, sec);

		return timer;
	}

	public boolean onVodTimer(VodTimer timer){
		return false;
	}

	public boolean onCreateVodMenu(ISTVVodMenu menu){
		return true;
	}

	public boolean onVodMenuOpened(ISTVVodMenu menu){
		return true;
	}

	public boolean onVodMenuClicked(ISTVVodMenu menu, int id){
		return true;
	}

	public boolean onVodMenuClosed(ISTVVodMenu menu){
		return true;
	}

	public boolean isVodMenuVisible(){
		if(menu==null)
			return false;
		return menu.isVisible();
	}

	public void onNetConnected(){
	}

	public void onNetDisconnected(){
	}

	private Dialog popuprateDlg=null;

	public void showPopupRateDialog(double ratingaverage){
		if(popuprateDlg!=null)
			return;

		Log.d(TAG, "create popup rate dialog");

		View view;
		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		view = inflater.inflate(R.layout.popup_rate, null);

		popuprateDlg = new Dialog(this, R.style.PopupDialog);
		popuprateDlg.addContentView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

		popuprateDlg.getWindow().setLayout(1920, 1080);

		TextView tv = (TextView)view.findViewById(R.id.PopupRateText);
		tv.setText(R.string.vod_rate);

		final RatingBar rate = (RatingBar)view.findViewById(R.id.PopupRateBar);
		float tmp_ratingaverage = (float)ratingaverage;
		rate.setRating(tmp_ratingaverage);
		
		if(rate!=null){
			rate.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					float rate_value = rate.getRating();
					
					onPopupRateDialogClicked(rate_value);

					if(popuprateDlg!=null){
						popuprateDlg.dismiss();
					}
				};
			});
		}

		popuprateDlg.setOnDismissListener(new DialogInterface.OnDismissListener(){
			public void onDismiss(DialogInterface dialog){
				popuprateDlg = null;
				boolean conn = isNetConnected();
				if(!conn){
					showPopupDialog(DIALOG_NET_BROKEN, getResources().getString(R.string.vod_net_broken_error));
				}
			}
		});
		popuprateDlg.show();
	}

	public void cancelPopupRateDialog(){
		if(popuprateDlg!=null){
			popuprateDlg.dismiss();
		}
	}

	public void onPopupRateDialogClicked(float rate_value){
	}
	
	//private int storedVol=0;
	private View volPanel=null;
	private boolean volPanelShow=false;
	private ProgressBar volBar;
	private TextView volText;
	private View muteImg;
	private void hideVolumePanel(){
		if(!volPanelShow)
			return;

		if(volPanelTimer!=null){
			volPanelTimer.remove();
			volPanelTimer=null;
		}

		volPanel.setVisibility(View.INVISIBLE);
		volPanelShow=false;
	}
	private void showVolumePanel(){
		if(volPanel==null){
			ViewGroup root = (ViewGroup)findViewById(Window.ID_ANDROID_CONTENT);
			LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			volPanel = inflater.inflate(R.layout.volume, null);
			ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
			volPanel.setLayoutParams(lp);
			root.addView(volPanel);

			volBar = (ProgressBar)volPanel.findViewById(R.id.VolumeProgBar);
			volText = (TextView)volPanel.findViewById(R.id.VolumeText);
			muteImg = (View)volPanel.findViewById(R.id.MuteImgRelativeLayout);

		}else{
			ViewGroup root = (ViewGroup)findViewById(Window.ID_ANDROID_CONTENT);
			root.bringChildToFront(volPanel);
		}

		if(!volPanelShow){
			volPanel.setVisibility(View.VISIBLE);
			volPanelShow=true;
		}

		AudioManager audio=(AudioManager) getSystemService(Context.AUDIO_SERVICE);
		int max = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		
		int vol = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
		boolean mute = audio.isStreamMute(AudioManager.STREAM_MUSIC);

		if(mute){
		//	vol = storedVol;
			muteImg.setBackgroundResource(R.drawable.vod_volume_mute);
		}else{
			//vol = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
			muteImg.setBackgroundResource(R.drawable.vod_volume_unmute);
		}

		if(volBar!=null){
			volBar.setMax(max);
			volBar.setProgress(vol);
		}

		if(volText!=null){
			String txt = ""+(vol);
			volText.setText(txt);
		}

		volPanelCounter = 5;
		
		if(volPanelTimer==null){
			volPanelTimer = addVodTimer(9998, 1);
		}
	}
	
	private boolean volKeyDown=false;
	public boolean onKeyDown(int keyCode, KeyEvent event){
		switch(keyCode){
/*		
			case KeyEvent.KEYCODE_VOLUME_UP:
			case KeyEvent.KEYCODE_VOLUME_DOWN:
			case KeyEvent.KEYCODE_VOLUME_MUTE:
				Log.d(TAG, "volume key "+keyCode+" down");

				AudioManager audio=(AudioManager) getSystemService(Context.AUDIO_SERVICE);
				int state=0, flags=0;
				boolean show=false;
				boolean mute = audio.isStreamMute(AudioManager.STREAM_MUSIC);

				switch(keyCode){
					case KeyEvent.KEYCODE_VOLUME_UP:
						state = AudioManager.ADJUST_RAISE;
						flags = AudioManager.FLAG_PLAY_SOUND;
						if(mute){
							//storedVol++;
							//int max = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
							//if(storedVol>max)
							//	storedVol=max;
						}
						show = true;
						break;
					case KeyEvent.KEYCODE_VOLUME_DOWN:
						state = AudioManager.ADJUST_LOWER;
						flags = AudioManager.FLAG_PLAY_SOUND;
						if(mute){
							//storedVol--;
							//if(storedVol<0)
							//	storedVol = 0;
						}
						show = true;
						break;
					case KeyEvent.KEYCODE_VOLUME_MUTE:
						if(!volKeyDown){
							state = AudioManager.ADJUST_SAME;
							mute = !mute;
							if(mute){
								//storedVol = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
							}
							audio.setStreamMute(AudioManager.STREAM_MUSIC, mute);
							if(!mute){
								flags = AudioManager.FLAG_PLAY_SOUND;
							}
							show = true;
						}
						break;
				}

				if(flags!=0){
					
   					boolean bmute = audio.isStreamMute(AudioManager.STREAM_MUSIC);
			
					if( bmute == false  ){
					   	int vol = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
			
							if( state == AudioManager.ADJUST_LOWER ){
								vol -- ;
								audio.setStreamVolume( AudioManager.STREAM_MUSIC, vol, flags);								
							}
							else if( state == AudioManager.ADJUST_RAISE ){
								vol ++ ;
								audio.setStreamVolume( AudioManager.STREAM_MUSIC, vol, flags);
							}
				  }
				}

				if(show){
					showVolumePanel();
				}

				volKeyDown=true;
				return true;
*/				
			case KeyEvent.KEYCODE_MENU: 
				return false; 
		}

		return super.onKeyDown(keyCode, event);
	}
	
	public boolean onKeyUp(int keyCode, KeyEvent event){
		switch(keyCode){
			case KeyEvent.KEYCODE_VOLUME_UP:
			case KeyEvent.KEYCODE_VOLUME_DOWN:
			case KeyEvent.KEYCODE_VOLUME_MUTE:
				volKeyDown=false;
				return true;
			case KeyEvent.KEYCODE_SEARCH:
			case KeyEvent.KEYCODE_Q:
			
				Bundle bundle = new Bundle();
				bundle.putString("layoutInstruction", "vod");
				startSearch(null, false, bundle, true);
				return true;				
		}

		return super.onKeyUp(keyCode, event);
	}

	@Override
	protected void onPause(){
		isActive=false;
		super.onPause();
		requestAudioFocus(false);
	}

	@Override
	protected void onResume(){
		isActive=true;
		super.onResume();
		requestAudioFocus(true);
		if(rootView!=null){
			Drawable bg = getThemePaper();
	        if (bg != null) {
	        	rootView.setBackgroundDrawable(bg);
	        } else {
	            Log.d(TAG, "getThemePaper fail");
	        }
		}
	}
	
	/**
     * 取setting背景图，用于设置成这个activity的背景
     * @return
     */
    public Drawable getThemePaper(){
        String url = Settings.System.getString(this.getContentResolver(), "settings.theme.url");
        if(mapBG.containsKey(url)){
        	return mapBG.get(url);
        }
        if(url!=null && url.length()>0){
                Bitmap bitmap = BitmapFactory.decodeFile(url);
                Drawable drawable = new BitmapDrawable(bitmap);
                mapBG.clear();
                mapBG.put(url, drawable);
                return drawable;
        }
        return null;
    }

	private void showBufferLogin(){        
		if(bufferLayout == null)
			return;
        if(bufferLayout.getVisibility() == View.GONE){
            bufferLayout.setVisibility(View.VISIBLE);
            bufferAnim.start();
        }
    }

    private void hideBufferLogin(){        
		if(bufferLayout == null)
			return;
        if(bufferLayout.getVisibility() == View.VISIBLE){
            bufferLayout.setVisibility(View.GONE);
            bufferAnim.stop();
        }
    }
	private Handler	loginHandler = new Handler(){
			public void handleMessage(Message msg) {
				if(msg.what==1984){
				    hideBufferLogin();
					if(popupDlg!=null){
						popupDlg.dismiss();
					}										
				} else if(msg.what==1985) {
				    hideBufferLogin();
				}
			}
		};	
		
		private AudioManager audioManager;
		private boolean isHasAudioFocus = false;
		public void requestAudioFocus(boolean request){ 
	        if (audioManager == null)
	            return;
	        if (request && !isHasAudioFocus) {
	            int result = audioManager.requestAudioFocus(this,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN_TRANSIENT); 
	            isHasAudioFocus = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED ? true : false;
	        } else if (!request && isHasAudioFocus) {
	            audioManager.abandonAudioFocus(this); 
	            isHasAudioFocus = false;
	        }
		}

		@Override
		public void onAudioFocusChange(int focusChange) {
			// TODO Auto-generated method stub
			
		}
		
		private boolean isFinish = false;
		public class BaseReceiver extends BroadcastReceiver {
			@Override
			public void onReceive(Context context, Intent intent) {
				Log.d(TAG, "----------------onreceiver-------intent="+intent.getAction());
				if(intent.getAction().equals(Constants.INTENT_HOME_KEY) && !isFinish){
					isFinish = true;
					onHomeReceive();
				}
			}
		}
		
		public void onHomeReceive(){
//			finish();
		}
}

