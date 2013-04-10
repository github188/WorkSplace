/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.ismartv.ISTVStatusBar;

import android.app.Service;
import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.IBinder;
import android.os.Message;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManagerImpl;
import android.view.Display;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.Long;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.widget.AbsoluteLayout;
import android.view.Window;
import android.view.LayoutInflater;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.view.ViewGroup;
import android.content.res.AssetManager;
import java.io.IOException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import android.graphics.Paint.FontMetrics;
import java.lang.String;
import android.os.SystemProperties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NameList;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.util.Log;

import com.ismartv.ISTVStatusBar.R;
import android.view.KeyEvent;
import android.database.Cursor;
import android.content.ContentValues;
import android.database.ContentObserver;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.net.Uri;
import android.provider.Settings;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.content.res.Configuration;
import java.util.Locale;
import android.app.backup.BackupManager;
import android.os.RemoteException;
import android.content.ComponentName;

import android.widget.TextView;
import java.util.Calendar;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import android.text.format.DateFormat;
import android.net.ethernet.EthernetManager;
import android.net.ethernet.EthernetStateTracker;

import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import com.android.internal.util.AsyncChannel;

import com.ismartv.ISTVStatusBar.WifiIcons;
import java.util.Timer;
import java.util.TimerTask;
import android.os.Handler;
import java.lang.Runnable;
import android.os.StrictMode;
import org.json.JSONObject;
import java.net.URL;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.NetworkInterface;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;

import android.widget.Toast;

public class ISTVStatusBarService extends Service {

	private View mView;
	private View mView_message_prompts;
	private Canvas myCanvas;
	private Canvas myCanvas01;
	private int box_y = 0;
	private Bitmap focus = null;
	private Bitmap tail = null;
	private int progress = 50;
	private Strings strings;
	private InputResImage inputResImage;
	private static SharedPreferences mLast = null;
	public HttpUtil city_weather_request;
	public static String global_city_id = null;
	private static final String TAG = "ISTVStatusBarService";

	public static final String PROVIDER_NAME = "com.ismartv.ISTVStatusBar.ISTVSysNotifyProvider";
	public static final Uri CONTENT_URI = Uri.parse("content://"
			+ PROVIDER_NAME + "/sysnotify");
	public static final String TITLE = "title";
	public static final String SUMMARY = "summary";
	public static final String UPDATED = "updated";

	public static final String BROADCAST_NOTIFY_ENABLE_ACTION = "com.lenovo.NOTIFY";

	public boolean sysnotify_switch_flag = true;
	public boolean xml_json_thread_run_flag = true;
	public boolean network_json_thread_run_flag = true;
	public LunarCalendar mLunarCalendar;

	private Animation showAnimation;
	private Animation hideAnimation;

	public void setXmlJsonThreadFlag(boolean value) {
		this.xml_json_thread_run_flag = value;
	}

	public boolean getXmlJsonThreadFlag() {
		return this.xml_json_thread_run_flag;
	}

	public void setNetworkJsonThreadFlag(boolean value) {
		this.network_json_thread_run_flag = value;
	}

	public boolean getNetworkJsonThreadFlag() {
		return this.network_json_thread_run_flag;
	}

	private void ViewShowEnable(int flag) {
		if (flag == 1) {

			mView.setVisibility(View.VISIBLE);
		} else {
			mView.setVisibility(View.INVISIBLE);
			mView.setVisibility(View.GONE);
			mView_message_prompts.setVisibility(View.INVISIBLE);
		}
	}

	private void ResetView() {
		ViewShowEnable(0);
	}

	public class Strings {
		String date = new String("2012/1/12");
		String week = new String("FRI");
		String place = new String("BeiJing");
		String temperature = new String("13C");
		String time = new String("PM 6:30");
	}

	public class InputResImage {

		InputStream sderror_icon = getResources().openRawResource(
				R.drawable.sderror_icon);
		Bitmap mBitmap_sderror_icon = BitmapFactory.decodeStream(sderror_icon);

	}

	private class TestView extends View {
		TestView(Context c) {
			super(c);
		}

		public void DrawMainMenu() {

			int icon_x = 1350;
			int text_x = 100;
			int icon_width = 50;
			int text_y = -200;
			int icon_y = -230;
			Paint mPaint = new Paint();
			mPaint.setAntiAlias(true);

			// myCanvas.drawBitmap(inputResImage.mBitmap_wifi_icon,icon_x,icon_y,
			// mPaint);
			// myCanvas.drawBitmap(inputResImage.mBitmap_download_icon,icon_x+icon_width,
			// icon_y, mPaint);
			myCanvas.drawBitmap(inputResImage.mBitmap_sderror_icon, icon_x
					+ icon_width * 2, icon_y, mPaint);
			// myCanvas.drawBitmap(inputResImage.mBitmap_eth_connect_icon,
			// icon_x+icon_width*3,icon_y, mPaint);

			Paint mPaintbox = new Paint();
			mPaintbox.setAntiAlias(true);
			mPaintbox.setColor(Color.WHITE);
			mPaintbox.setTextSize(35);

			myCanvas.drawText(strings.date, text_x, text_y, mPaintbox);
			myCanvas.drawText(strings.week, text_x + 200, text_y, mPaintbox);
			myCanvas.drawText(strings.place, text_x + 200 + 100, text_y,
					mPaintbox);
			myCanvas.drawText(strings.temperature, text_x + 200 + 100 + 200,
					text_y, mPaintbox);

			myCanvas.drawText(strings.time, icon_x + icon_width * 4, text_y,
					mPaintbox);

		}

		@Override
		public void onDraw(Canvas canvas) {
			// super.onDraw(canvas);
			Log.d("********************************onDraw", "onDraw");

			// canvas.scale(0.65f, 0.65f);
			canvas.translate(0, 250);
			myCanvas = canvas;

			canvas.drawColor(Color.TRANSPARENT);// transparent

			Paint mPaint = new Paint();
			mPaint.setAntiAlias(true);
			mPaint.setColor(Color.WHITE);
			mPaint.setTextSize(40);
			mPaint.setTextAlign(Paint.Align.CENTER);

			DrawMainMenu();

			return;

		}
	}

	public void getContentFromXml(String fileName) {

		DocumentBuilderFactory factory = null;
		DocumentBuilder builder = null;
		Document document = null;
		InputStream inputStream = null;

		mLast = PreferenceManager.getDefaultSharedPreferences(this);

		factory = DocumentBuilderFactory.newInstance();
		try {
			builder = factory.newDocumentBuilder();
			inputStream = this.getResources().getAssets().open(fileName);
			document = builder.parse(inputStream);
			Element root = document.getDocumentElement();

			NodeList nodes = root.getElementsByTagName("weather");

			for (int i = 0; i < nodes.getLength(); i++) {
				Element contentElement = (Element) (((NodeList) nodes).item(i));
				Log.d("xxxxx", contentElement.getAttribute("name"));
				Log.d("xxxxx", contentElement.getAttribute("icon"));
				mLast.edit()
						.putString(contentElement.getAttribute("name"),
								contentElement.getAttribute("icon")).commit();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void deal_phenomenon_string(String phenomenon) {
		String Temp = new String();
		String Temp1 = new String();
		String Temp2 = new String();

		Temp = phenomenon;
		String mast = this.getResources().getString(
				R.string.phenomenon_sting_mask);

		int pos = phenomenon.indexOf(mast);
		if (pos != -1) {
			Temp1 = phenomenon.substring(0, pos);
			int h = mast.length();
			Log.d(TAG, "@@@" + pos + Temp1 + h);
			Temp2 = phenomenon.substring(pos + mast.length(),
					phenomenon.length());

			Log.d(TAG, "@@@" + pos + Temp1 + h + Temp2);
		} else {
			Temp1 = phenomenon;
		}
		Log.d(TAG, "####" + pos + Temp1 + "    " + Temp2);

		try {
			String icon = mLast.getString(Temp1, null);
			String icon1 = mLast.getString(Temp2, null);
			Log.d(TAG, "####" + icon + "**********" + icon1);

			if (icon != null) {
				Field field = R.drawable.class.getField(icon);
				int i = field.getInt(new R.drawable());
				ImageView image = (ImageView) mView
						.findViewById(R.id.phenomenon_icon);
				image.setBackgroundResource(i);
				image.setVisibility(View.VISIBLE);
				Log.d("icon", i + "");
			} else {
				ImageView image = (ImageView) mView
						.findViewById(R.id.phenomenon_icon);
				image.setVisibility(View.INVISIBLE);
			}
			if (icon1 != null) {
				Field field = R.drawable.class.getField(icon1);
				int i = field.getInt(new R.drawable());
				ImageView image = (ImageView) mView
						.findViewById(R.id.phenomenon_icon_next);
				image.setBackgroundResource(i);
				image.setVisibility(View.VISIBLE);
				Log.d("icon", i + "");
			} else {
				ImageView image = (ImageView) mView
						.findViewById(R.id.phenomenon_icon_next);
				image.setVisibility(View.INVISIBLE);
			}

		} catch (Exception e) {
			Log.e("icon", e.toString());
		}

	}

	private void update_geo_weather(Intent intent) {
		Bundle bundle = intent.getExtras();
		if (bundle != null) {
			String name_en = bundle.getString("name_en");
			String name = bundle.getString("name");
			String geoid = bundle.getString("geoid");
			String date = bundle.getString("date");

			String phenomenon = bundle.getString("phenomenon");
			/*
			 * String wind_direction = bundle.getString("wind_direction");
			 * String wind_power = bundle.getString("wind_power");
			 */
			String temperature = bundle.getString("temperature");
			String temperature_cels = this.getResources().getString(
					R.string.temperature_cels);
			Log.d(TAG, "!!!!" + name_en + date);
			TextView text_where = (TextView) mView.findViewById(R.id.where);
			text_where.setText(name);
			/*
			 * TextView text_date = (TextView) mView.findViewById(R.id.date);
			 * text_date.setText(date);
			 */
			TextView text_phenomenon = (TextView) mView
					.findViewById(R.id.phenomenon_text);
			text_phenomenon.setText(phenomenon);
			/*
			 * TextView text_wind_direction = (TextView)
			 * mView.findViewById(R.id.wind_direction);
			 * text_wind_direction.setText(wind_direction); TextView
			 * text_wind_power = (TextView) mView.findViewById(R.id.wind_power);
			 * text_wind_power.setText(wind_power);
			 */
			TextView text_temperature = (TextView) mView
					.findViewById(R.id.temperature);
			text_temperature.setText(temperature + temperature_cels);
			deal_phenomenon_string(phenomenon);
		}
	}

	private BroadcastReceiver mMediaStatusReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
				String path = intent.getData().toString()
						.substring("file://".length());
				Log.d(TAG, "-------------------ACTION_MEDIA_MOUNTED" + path);
				if (path.equals("/mnt/sdcard")) {
					ShowMessagesPrompts(context.getResources().getString(
							R.string.sdcard_dev_mounted));
					ImageView image_sderror_icon = (ImageView) mView
							.findViewById(R.id.sderror_icon);
					image_sderror_icon.setVisibility(View.INVISIBLE);
					sdcard_icon_display_flag = false;
				} else {

					ShowMessagesPrompts(context.getResources().getString(
							R.string.usb_dev_mounted));
				}
			} else if (action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
				Log.d(TAG, "-------------------ACTION_MEDIA_UNMOUNTED");
				ImageView image_sderror_icon = (ImageView) mView
						.findViewById(R.id.sderror_icon);
				if (hasStorage() != true) {

					image_sderror_icon
							.setImageResource(R.drawable.sderror_icon);
					image_sderror_icon.setVisibility(View.VISIBLE);
					sdcard_icon_display_flag = true;

				}

			} else if (action.equals(Intent.ACTION_MEDIA_EJECT)) {
				ShowMessagesPrompts(context.getResources().getString(
						R.string.usb_dev_eject));
				String path = intent.getData().toString()
						.substring("file://".length());
				Log.d(TAG, "-------------------ACTION_MEDIA_EJECT" + path);

			} else if (action.equals(Intent.ACTION_MEDIA_REMOVED)) {
				Log.d(TAG, "-------------------ACTION_MEDIA_REMOVED");
			}
		}
	};

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (action.equals("com.amlogic.weather.TopStatus.onResume")) {
				Log.d(TAG, "-------------------onResume");

				setXmlJsonThreadFlag(true);

				try {
					Thread.sleep(1500);
				} catch (Exception e) {
					e.printStackTrace();
				}

				ViewShowEnable(1);

				setTime();
			} else if (action.equals("com.amlogic.weather.TopStatus.onPause")) {
				Log.d(TAG, "-------------------onPause");
				ViewShowEnable(0);
			} else if (action.equals("com.amlogic.weather.TopStatus.onUpdate")) {
				Log.d(TAG, "weather onUpdate!");
				update_geo_weather(intent);
			} else if (action.equals("com.lenovo.CITYID")) {
				Bundle bundle = intent.getExtras();
				if (bundle != null) {
					String geoid = bundle.getString("city_id");
					global_city_id = geoid;
					setXmlJsonThreadFlag(true);
					Log.d(TAG, "#######" + geoid);
				}
			} else if (action.equals(BROADCAST_NOTIFY_ENABLE_ACTION)) {
				Bundle bundle = intent.getExtras();
				if (bundle != null) {
					String status = bundle.getString("status");
					if (status.equals("enable")) {
						sysnotify_switch_flag = true;
					} else {
						sysnotify_switch_flag = false;
					}

				}
			} else if (action.equals("com.ismartv.ISTVStatusBar.sysnotify")) {
				Bundle bundle = intent.getExtras();
				if (bundle != null) {
					String notify_content = bundle.getString("notify_content");
					ShowMessagesPrompts(notify_content);
				}
			} else if (action.equals(EthernetManager.ETH_STATE_CHANGED_ACTION)) {
				Log.d(TAG, "-------------------ETH_STATE_CHANGED_ACTION");

				boolean bEthernetConnected = mEthernetConnected;

				updateEth(intent);

				Log.d(TAG, "-------------------EthernetConnected: "
						+ bEthernetConnected + ";" + mEthernetConnected);

				if (bEthernetConnected != mEthernetConnected) {
					updateStatusbarView();
				}

			}

			else if (action.equals(WifiManager.RSSI_CHANGED_ACTION)
					|| action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)
					|| action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {

				boolean bWifiConnected = mWifiConnected;

				updateWifiState(intent);

				if (bWifiConnected != mWifiConnected) {
					updateStatusbarView();
				}

				Log.d(TAG, "-------------------WifiConnected: "
						+ mWifiConnected + ";" + bWifiConnected);
			} else if (action
					.equalsIgnoreCase(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
				Log.d(TAG, "-------------------ACTION_DOWNLOAD_COMPLETE");
			}
		}
	};

	private int mInetCondition = 0;
	private static final int INET_CONDITION_THRESHOLD = 50;

	// wifi/////

	WifiManager mWifiManager;
	AsyncChannel mWifiChannel;
	boolean mWifiEnabled = false;
	boolean mWifiConnected = false;
	int mWifiRssi, mWifiLevel;
	String mWifiSsid;
	int mWifiIconId = 0;
	int mWifiActivityIconId = 0; // overlay arrows for wifi direction
	int mWifiActivity = WifiManager.DATA_ACTIVITY_NONE;
	boolean mDataAndWifiStacked = false;

	class WifiHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case AsyncChannel.CMD_CHANNEL_HALF_CONNECTED:
				if (msg.arg1 == AsyncChannel.STATUS_SUCCESSFUL) {
					mWifiChannel.sendMessage(Message.obtain(this,
							AsyncChannel.CMD_CHANNEL_FULL_CONNECTION));
				} else {
					Log.d(TAG, "Failed to connect to wifi");
				}
				break;
			case WifiManager.DATA_ACTIVITY_NOTIFICATION:
				if (msg.arg1 != mWifiActivity) {
					mWifiActivity = msg.arg1;
					// refreshViews();
				}
				break;
			default:
				// Ignore
				break;
			}
		}
	}

	private void updateWifiState(Intent intent) {
		final String action = intent.getAction();
		if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {

			mWifiEnabled = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
					WifiManager.WIFI_STATE_UNKNOWN) == WifiManager.WIFI_STATE_ENABLED;

		} else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
			final NetworkInfo networkInfo = (NetworkInfo) intent
					.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
			boolean wasConnected = mWifiConnected;

			if (networkInfo != null)
				mWifiConnected = networkInfo.isConnected();
			// If we just connected, grab the inintial signal strength and ssid
			if (mWifiConnected && !wasConnected) {
				// try getting it out of the intent first
				WifiInfo info = (WifiInfo) intent
						.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
				if (info == null) {
					info = mWifiManager.getConnectionInfo();
				}
				if (info != null) {
					mWifiSsid = huntForSsid(info);
				} else {
					mWifiSsid = null;
				}
			} else if (!mWifiConnected) {
				mWifiSsid = null;
			}
			// Apparently the wifi level is not stable at this point even if
			// we've just connected to
			// the network; we need to wait for an RSSI_CHANGED_ACTION for that.
			// So let's just set
			// it to 0 for now
			mWifiLevel = 0;
			mWifiRssi = -200;
		} else if (action.equals(WifiManager.RSSI_CHANGED_ACTION)) {
			if (mWifiConnected) {
				mWifiRssi = intent
						.getIntExtra(WifiManager.EXTRA_NEW_RSSI, -200);
				mWifiLevel = WifiManager.calculateSignalLevel(mWifiRssi,
						WifiIcons.WIFI_LEVEL_COUNT);
			}
		}

		updateWifiIcons();
	}

	private void updateWifiIcons() {
		if (mWifiConnected) {
			mWifiIconId = WifiIcons.WIFI_SIGNAL_STRENGTH[mInetCondition][mWifiLevel];
			// mContentDescriptionWifi = this.getString(
			// AccessibilityContentDescriptions.WIFI_CONNECTION_STRENGTH[mWifiLevel]);
		} else {
			if (mDataAndWifiStacked) {
				mWifiIconId = 0;
			} else {
				mWifiIconId = mWifiEnabled ? WifiIcons.WIFI_SIGNAL_STRENGTH[0][0]
						: 0;
			}
			// mContentDescriptionWifi =
			// this.getString(R.string.accessibility_no_wifi);
		}
	}

	private String huntForSsid(WifiInfo info) {
		String ssid = info.getSSID();
		if (ssid != null) {
			return ssid;
		}
		// OK, it's not in the connectionInfo; we have to go hunting for it
		List<WifiConfiguration> networks = mWifiManager.getConfiguredNetworks();
		for (WifiConfiguration net : networks) {
			if (net.networkId == info.getNetworkId()) {
				return net.SSID;
			}
		}
		return null;
	}

	// Ethernet
	boolean mEthernetConnected = false;
	boolean mEthernetWaitingDHCP = false;
	int mEthernetIconId = 0;
	private static final int[] sEthImages = { R.drawable.ethernet_connected,
			R.drawable.ethernet_disconnected, R.drawable.ethernet_connecting };

	// ===== Ethernet
	// ===================================================================
	private final void updateEth(Intent intent) {
		final int event = intent.getIntExtra(EthernetManager.EXTRA_ETH_STATE,
				EthernetStateTracker.EVENT_HW_DISCONNECTED);
		Log.d(TAG, "updateEth event=" + event);
		switch (event) {
		case EthernetStateTracker.EVENT_HW_CONNECTED:
			if (mEthernetWaitingDHCP)
				return;
			// else fallthrough
			return;
		case EthernetStateTracker.EVENT_INTERFACE_CONFIGURATION_SUCCEEDED: {
			if (event == EthernetStateTracker.EVENT_INTERFACE_CONFIGURATION_SUCCEEDED)
				mEthernetWaitingDHCP = false;
			EthernetManager ethManager = (EthernetManager) this
					.getSystemService(Context.ETH_SERVICE);
			if (ethManager.isEthDeviceAdded()) {
				mEthernetConnected = true;
				mEthernetIconId = sEthImages[0];
				// mContentDescriptionEthernet =
				// mContext.getString(R.string.accessibility_ethernet_connected);
			}
			return;
		}
		case EthernetStateTracker.EVENT_INTERFACE_CONFIGURATION_FAILED:
			mEthernetWaitingDHCP = false;
			mEthernetConnected = false;
			mEthernetIconId = sEthImages[1];
			// mContentDescriptionEthernet =
			// mContext.getString(R.string.accessibility_ethernet_disconnected);

			return;
		case EthernetStateTracker.EVENT_DHCP_START:
			// There will be a DISCONNECTED and PHYCONNECTED when doing a
			// DHCP request. Ignore them.
			mEthernetWaitingDHCP = true;
			// mEthernetConnected = true;
			mEthernetIconId = sEthImages[2];
			// mContentDescriptionEthernet =
			// mContext.getString(R.string.accessibility_ethernet_connecting);
			return;
		case EthernetStateTracker.EVENT_HW_CHANGED:
			return;
		case EthernetStateTracker.EVENT_HW_DISCONNECTED:

		default:
			if (mEthernetWaitingDHCP)
				return;
			mEthernetConnected = false;
			mEthernetIconId = -1;
			// mContentDescriptionEthernet = null;
			return;
		}

	}

	private void updateStatusbarView() {
		ImageView image_eth_connect_icon = (ImageView) mView
				.findViewById(R.id.eth_connect_icon);

		Log.d(TAG, "updateStatusbarView =" + mEthernetIconId + ";"
				+ mWifiIconId);

		if (mEthernetIconId != -1) {
			image_eth_connect_icon.setImageResource(mEthernetIconId);

			if (mEthernetIconId == R.drawable.ethernet_connected) {
				ShowMessagesPrompts(this.getResources().getString(
						R.string.network_connected));
				setXmlJsonThreadFlag(true);
				setNetworkJsonThreadFlag(true);
			}
		} else {
			image_eth_connect_icon
					.setImageResource(R.drawable.ethernet_disconnected);
			// if(mEthernetIconId==R.drawable.ethernet_disconnected)
			{
				ShowMessagesPrompts(this.getResources().getString(
						R.string.network_disconnected));
			}
		}
		// Modify by dgg 2012/03/20,
		// Fixed : When disconnect wlan and then link wifi OK, but still display
		// network disconnect
		if (mWifiIconId != -1 && mWifiIconId != 0 && mWifiConnected) {
			image_eth_connect_icon.setImageResource(mWifiIconId);

			ShowMessagesPrompts(this.getResources().getString(
					R.string.network_connected));
			setXmlJsonThreadFlag(true);
			setNetworkJsonThreadFlag(true);
		} else {

		}
	}

	private void weatherStatusViewEnable(int flag) {
		Intent intent = new Intent("com.amlogic.iSmart.TopStatus.ViewShow");
		if (flag == 1) {
			mView.setVisibility(View.VISIBLE);
			intent.putExtra("Status", flag);
		} else {
			mView.setVisibility(View.GONE);
			intent.putExtra("Status", flag);
		}
		sendBroadcast(intent);
	}

	private final static String mFormat = "EEEE";// h:mm:ss aa

	private void setDateTime() {

		final Calendar c = Calendar.getInstance();
		int mYear = c.get(Calendar.YEAR);
		int mMonth = c.get(Calendar.MONTH);
		int mDay = c.get(Calendar.DAY_OF_MONTH); 
		TextView text = (TextView) mView.findViewById(R.id.data_dis);
		
		/*
		 text.setText(new StringBuilder().append(mYear).append( (mMonth + 1) <
		 10 ? "/"+"0" + (mMonth + 1) : "/"+(mMonth + 1)).append( (mDay < 10) ?
		 "/"+"0" + mDay : "/"+mDay).append("  "+DateFormat.format(mFormat,
		 c)));
		*/
		 
		//text.setText(new StringBuilder().append(mYear).append( "/" + (mMonth + 1) ).append( "/"+mDay)
		//		.append("  "+DateFormat.format(mFormat,
		//		 c)));
		 
		
		text.setText(new StringBuilder().append(
				DateFormat.getDateFormat(this).format(c.getTime())
						.replace('-', '/')).append(
				"  " + DateFormat.format(mFormat, c)));
		
		//text.setText(new StringBuilder().append(
		//		DateFormat.getDateFormat(this).format(c.getTime())
		//				.replace('/', '-')).append(
		//		"  " + DateFormat.format(mFormat, c)));
		
	}

	private void setTime() {
		final String mFormat = "aa";// h:mm:ss aa
		final Calendar c = Calendar.getInstance();
		TextView text = (TextView) mView.findViewById(R.id.digital_clock);

		int mHours = 0;
		int mMinutes = c.get(Calendar.MINUTE);
		boolean value = DateFormat.is24HourFormat(this);

		if (value) {

			mHours = c.get(Calendar.HOUR_OF_DAY);
			// Log.d(TAG,"#########24hour###########"+mHours);
			text.setText(new StringBuilder()
			.append( mHours ).append(":")
					.append(mMinutes < 10 ? "0" + mMinutes : mMinutes));


		} else {
			mHours = c.get(Calendar.HOUR);
			// Log.d(TAG,"#########12hour###########"+mHours);
			if (mHours == 0)
				mHours = 12;
			text.setText(new StringBuilder()
					//.append(mHours < 10 ? "0" + mHours : mHours).append(":")
					.append( mHours ).append(":")
					.append(mMinutes < 10 ? "0" + mMinutes : mMinutes)
					.append("  " + DateFormat.format(mFormat, c)));
		}

	}

	private String getDateTime() {

		final Calendar c = Calendar.getInstance();
		int mYear = c.get(Calendar.YEAR);
		int mMonth = c.get(Calendar.MONTH);
		int mDay = c.get(Calendar.DAY_OF_MONTH);
		TextView text = (TextView) mView.findViewById(R.id.data_dis);
		// text.setText(DateFormat.format(mFormat, c));
		String date_time;

		// time display
		mLunarCalendar = new LunarCalendar();
		long test[] = mLunarCalendar.calElement(mYear, mMonth + 1, mDay);
		Log.d(TAG, "######" + test[0] + "/" + test[1] + "/" + test[2] + "/"
				+ test[3] + "/" + test[4] + "/" + test[5] + "/" + test[6]);

		date_time = getResources().getString(R.string.str_date_text)
				+ mLunarCalendar.getChinaMonth((int) test[1])
				+ mLunarCalendar.getChinaDate((int) test[2]) + "  "
				+ mLunarCalendar.cyclical((int) test[0])
				+ getResources().getString(R.string.str_year_text);

		return date_time;
	}

	private boolean sdcard_icon_display_flag = true;

	public boolean hasStorage() {
		String state = android.os.Environment.getExternalStorageState();
		if (android.os.Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}

	private Handler msgprompts_timer_handler = new Handler();
	private Runnable msgprompts_timer_runnable = new Runnable() {

		public void run() {
			HideMessagesPrompts();
			msgprompts_timer_handler.removeCallbacks(msgprompts_timer_runnable);
		}
	};

	private int sysnotify_get_time = 15 * 60 * 1000;
	private Handler sysnotify_timer_handler = new Handler();
	private Runnable sysnotify_timer_runnable = new Runnable() {

		public void run() {
			setXmlJsonThreadFlag(true);
			setNetworkJsonThreadFlag(true);
			sysnotify_timer_handler.postDelayed(sysnotify_timer_runnable,
					sysnotify_get_time);
		}
	};

	private boolean vodhome_date_display_flag = false;
	private int vodhome_date_display_time = 6 * 1000;
	private Handler vodhome_date_display_handler = new Handler();
	private Runnable vodhome_date_display_runnable = new Runnable() {

		public void run() {
			Log.d(TAG, "vodhome_date_display_runnable");
			String value = SystemProperties.get("persist.sys.statuabar");
			if (value.equals("true")) {
				ViewShowEnable(1);
				SystemProperties.set("persist.sys.statuabar", "false");
			}

			TextView text_date = (TextView) mView.findViewById(R.id.data_dis);
			text_date.startAnimation(hideAnimation);
			vodhome_date_display_handler.postDelayed(
					vodhome_date_display_runnable, vodhome_date_display_time);
		}
	};

	@Override
	public void onStart(Intent intent, int startId) {
		Log.d(TAG, "onStart");

		mgr = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
		// strings = new Strings();
		// inputResImage = new InputResImage();

		// receive broadcasts
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.amlogic.weather.TopStatus.onUpdate");
		filter.addAction("com.amlogic.weather.TopStatus.onResume");
		filter.addAction("com.amlogic.weather.TopStatus.onPause");

		filter.addAction("com.lenovo.CITYID");
		filter.addAction("com.ismartv.ISTVStatusBar.sysnotify");
		filter.addAction(BROADCAST_NOTIFY_ENABLE_ACTION);

		filter.addAction(EthernetManager.ETH_STATE_CHANGED_ACTION);
		filter.addAction(WifiManager.RSSI_CHANGED_ACTION);
		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		filter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);

		IntentFilter filter_media = new IntentFilter();
		filter_media.addAction(Intent.ACTION_MEDIA_MOUNTED);
		filter_media.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
		filter_media.addAction(Intent.ACTION_MEDIA_EJECT);
		filter_media.addAction(Intent.ACTION_MEDIA_REMOVED);
		filter_media.addDataScheme("file");

		registerReceiver(mMediaStatusReceiver, filter_media);
		registerReceiver(mBroadcastReceiver, filter);

		getContentFromXml("geo_weather_icon.xml");

		// mView=new TestView(this);
		LayoutInflater inflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mView = inflater.inflate(R.layout.main, null);
		mView_message_prompts = inflater
				.inflate(R.layout.message_prompts, null);
		// mView = View.inflate(Context, R.layout.main, null);
		ImageView imgView = (ImageView) mView
				.findViewById(R.id.phenomenon_icon);
		// imgView.setImageResource(R.drawable.movie_focus);
		TextView text = (TextView) mView.findViewById(R.id.date);
		setDateTime();
		setTime();

		WindowManager.LayoutParams params = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
				WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
				PixelFormat.TRANSLUCENT);
		// params.gravity = Gravity.RIGHT | Gravity.TOP;
		params.gravity = 0x30;// Gravity.TOP;
		WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Log.d(TAG,
				"Width=" + display.getWidth() + "Height=" + display.getHeight());
		ViewShowEnable(0);
		wm.addView(mView, params);
		wm.addView(mView_message_prompts, params);

		// wifi
		mWifiManager = (WifiManager) this
				.getSystemService(Context.WIFI_SERVICE);
		Handler handler = new WifiHandler();
		mWifiChannel = new AsyncChannel();
		Messenger wifiMessenger = mWifiManager.getMessenger();
		if (wifiMessenger != null) {
			mWifiChannel.connect(this, handler, wifiMessenger);
		}

		ImageView image_sderror_icon = (ImageView) mView
				.findViewById(R.id.sderror_icon);
		if (hasStorage()) {
			image_sderror_icon.setVisibility(View.INVISIBLE);
			image_sderror_icon.setVisibility(View.GONE);
			sdcard_icon_display_flag = false;
		} else {
			image_sderror_icon.setImageResource(R.drawable.sderror_icon);
			image_sderror_icon.setVisibility(View.VISIBLE);
			sdcard_icon_display_flag = true;
		}

		// showAnimation = AnimationUtils.loadAnimation(this,
		// R.anim.push_left_in);
		// hideAnimation = AnimationUtils.loadAnimation(this,
		// R.anim.push_left_out);
		showAnimation = AnimationUtils.loadAnimation(this, R.anim.fly_up);
		hideAnimation = AnimationUtils.loadAnimation(this, R.anim.fly_down);

		hideAnimation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				TextView text_date = (TextView) mView
						.findViewById(R.id.data_dis);
				vodhome_date_display_flag = !vodhome_date_display_flag;
				text_date.setVisibility(View.INVISIBLE);
				text_date.startAnimation(showAnimation);
				if (vodhome_date_display_flag)
					text_date.setText(getDateTime());
				else {
					setDateTime();
					setTime();
				}
				
				text_date.setVisibility(View.VISIBLE);
			}
		});

//		vodhome_date_display_handler.postDelayed(vodhome_date_display_runnable,
//				vodhome_date_display_time);

		String sysnotify_status = SystemProperties.get("persist.sys.notify",
				"1");
		if (sysnotify_status.equals("0")) {
			sysnotify_switch_flag = false;
		} else
			sysnotify_switch_flag = true;

		city_weather_request = new HttpUtil();
		String city_id = SystemProperties.get("persist.sys.cityid");
		global_city_id = city_id;
		setXmlJsonThreadFlag(true);
		String city_id_test = SystemProperties.get("persist.sys.cityid.test");
		if (city_id_test.equals(city_id)) {
			global_city_id = "101010100";
			city_weather_request.setGeoID("101010100");
		} else {
			Log.d(TAG, "city_id" + city_id);
			city_weather_request.setGeoID(city_id);
		}

		sysnotify_timer_handler.postDelayed(sysnotify_timer_runnable,
				sysnotify_get_time);

		Thread threads = new Thread() {
			public void run() {
				while (true) {
					if (getXmlJsonThreadFlag()) {
						setXmlJsonThreadFlag(false);
						if (city_weather_request != null) {
							Log.d(TAG, "############"
									+ city_weather_request.cur_geoid);
							if ((global_city_id != city_weather_request.cur_geoid)
									|| (getNetworkJsonThreadFlag())) {
								setNetworkJsonThreadFlag(false);
								city_weather_request.setGeoID(global_city_id);
								city_weather_request.process();
								city_weather_request.process_notify();
							}
						}

					} else {
						try {
							Thread.sleep(1500);
						} catch (Exception e) {
						}
					}
				}
			}
		};
		threads.start();

		queryDownloadStatus();
	}

	public void ShowMessagesPrompts(String value) {

		/*
		 * LayoutInflater inflater = (LayoutInflater)
		 * this.getSystemService(Context.LAYOUT_INFLATER_SERVICE); View layout =
		 * inflater.inflate(R.layout.test,null);
		 * 
		 * TextView text = (TextView)
		 * layout.findViewById(R.id.message_prompts_text); text.setText(value);
		 * Toast toast = new Toast(this); toast.setGravity(Gravity.TOP, 1200,
		 * 80); toast.setDuration(Toast.LENGTH_LONG); toast.setView(layout);
		 * toast.show();
		 */
		//
		
		String s = "yyyy-MM-dd'T'HH:mm:ssZ";
		
		SimpleDateFormat sdf = new SimpleDateFormat(s, Locale.CHINA);
				
		ContentValues cv = new ContentValues();
		cv.put("id", 0x1001);
		cv.put("title", "Statu title");
		cv.put("summary", value );
		cv.put("updated", sdf.format( new Date()));
		
		getContentResolver().insert(ISTVStatusBarService.CONTENT_URI, cv);
		
		//
		
		if (sysnotify_switch_flag) {

			if (mView_message_prompts != null)
				mView_message_prompts.setVisibility(View.VISIBLE);

			TextView text = (TextView) mView_message_prompts
					.findViewById(R.id.message_prompts_text);
			text.setText(value);
			text.setVisibility(View.VISIBLE);

			LinearLayout layout = (LinearLayout) mView_message_prompts
					.findViewById(R.id.message_prompts);
			layout.setVisibility(View.VISIBLE);

			
			msgprompts_timer_handler.removeCallbacks(msgprompts_timer_runnable);
			msgprompts_timer_handler.postDelayed(msgprompts_timer_runnable,	6000);

		}
	}

	public void HideMessagesPrompts() {
		if (mView_message_prompts != null)
			mView_message_prompts.setVisibility(View.INVISIBLE);

		LinearLayout layout = (LinearLayout) mView_message_prompts
				.findViewById(R.id.message_prompts);
		layout.setVisibility(View.INVISIBLE);

		TextView text = (TextView) mView_message_prompts
				.findViewById(R.id.message_prompts_text);
		text.setVisibility(View.INVISIBLE);
		text.setText(null);
	}

	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			ImageView image_sderror_icon = (ImageView) mView
					.findViewById(R.id.sderror_icon);
			switch (msg.what) {
			case 1:
				if (sdcard_icon_display_flag == false) {
					image_sderror_icon.setImageResource(R.drawable.downloading);
					image_sderror_icon.setVisibility(View.VISIBLE);
				}
				break;
			case 2:
				if (sdcard_icon_display_flag == false)
					image_sderror_icon.setVisibility(View.INVISIBLE);
				break;
			default:
				break;
			}

		}
	};

	private DownloadManager mgr = null;

	public void queryDownloadStatus() {

		Runnable queryRunable = new Runnable() {
			long totalsize = 0;
			long dowsize = 0;
			boolean downok = false;
			Cursor c = null;

			public void run() {

				while (downok == false) {

					// c=mgr.query(new
					// DownloadManager.Query().setFilterById(lastDownload));
					c = mgr.query(new DownloadManager.Query()
							.setFilterByStatus(DownloadManager.STATUS_RUNNING));
					/*
					 * STATUS_FAILED STATUS_PAUSED STATUS_PENDING STATUS_RUNNING
					 * STATUS_SUCCESSFUL
					 */
					if (c == null) {

					} else {
						c.moveToFirst();
						int n = c.getCount();
						// Log.d(TAG,"STATUS_RUNNING count="+n);
						if ((n > 0) && (sdcard_icon_display_flag == false)) {
							Message msg = new Message();
							msg.what = 1;
							handler.sendMessage(msg);
						} else if (n == 0) {
							Message msg = new Message();
							msg.what = 2;
							handler.sendMessage(msg);
						}

					}

					try {
						Thread.sleep(1500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					c.close();
				}
			}// run
		};

		Thread background = new Thread(queryRunable);
		background.start();
	}

	@Override
	public void onCreate() {

		/*
		 * StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
		 * .detectDiskReads() .detectDiskWrites() .detectNetwork() // or
		 * .detectAll() for all detectable problems .penaltyLog() .build());
		 * 
		 * StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
		 * .detectLeakedSqlLiteObjects() .detectLeakedClosableObjects()
		 * .penaltyLog() .penaltyDeath() .build());
		 */
		super.onCreate();
		Log.d(TAG, "onCreate");
		setForeground(true);
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		unregisterReceiver(mBroadcastReceiver);
		unregisterReceiver(mMediaStatusReceiver);
		((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(mView);
		mView = null;
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private Handler mDisappearHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				ResetView();
				break;
			}
		}
	};

	private void KickDisappear() {
		int dly = SystemProperties.getInt("persist.tv.global_menu_timeout ",
				10000);
		mDisappearHandler.removeMessages(1);
		Message message = new Message();
		message.what = 1;
		mDisappearHandler.sendMessageDelayed(message, dly);
	}

	public class HttpUtil {
		String entry = "http://cord.tvxio.com";
		String server = entry;
		String devName = "A11C";
		String devVersion = "1.0";
		String mac = null;

		URL url;
		URL url_notify;
		String cur_geoid;

		private boolean isArray = false;
		String postData = null;

		public String name_en;
		public String name;
		public String geoid;
		public String date;
		public String wind_direction;
		public String phenomenon;
		public String wind_power;
		public String temperature;

		public HttpUtil() {
		}

		public void setGeoID(String geoid) {
			String addr;
			String addr_notify;
			URL u = null;
			URL u_notify = null;

			if (geoid == null || geoid == "") {
				addr = "http://lily.tvxio.com/media/101010100.json";
				addr_notify = "http://orchid.tvxio.com/messages/101010100/atom/";
			} else {
				addr = "http://lily.tvxio.com/media/" + geoid + ".json";
				addr_notify = "http://orchid.tvxio.com/messages/" + geoid
						+ "/atom/";
			}

			try {
				u = new URL(addr);
				u_notify = new URL(addr_notify);
			} catch (Exception e) {
				Log.d(TAG, "parse URL failed!");
			}
			this.url = u;
			this.url_notify = u_notify;
			this.cur_geoid = geoid;
		}

		String getDevVersion() {
			return devVersion;
		}

		synchronized String getMACAddress() {
			if (mac == null) {
				try {
					byte addr[];
					addr = NetworkInterface.getByName("eth0")
							.getHardwareAddress();
					mac = "";
					for (int i = 0; i < 6; i++) {
						mac += String.format("%02X", addr[i]);
					}
				} catch (Exception e) {
					mac = "00112233445566";
				}
			}
			return mac;
		}

		String getUserAgent() {
			return devName + "/" + devVersion + " " + getMACAddress();
		}

		protected void setPostData(String data) {
			postData = data;
		}

		protected void addPostData(String data) {
			if (postData == null) {
				postData = data;
			} else {
				postData += "&" + data;
			}
		}

		protected synchronized void setServer(String s) {
			server = "http://" + s;
		}

		boolean process() {
			HttpURLConnection conn = null;
			InputStream input = null;
			OutputStream output = null;
			boolean ret = false;

			try {
				Log.d(TAG, "url: " + url.toString());
				conn = (HttpURLConnection) this.url.openConnection();

				if (postData != null)
					conn.setDoOutput(true);
				conn.setDoInput(true);
				conn.setRequestProperty("User-Agent", getUserAgent());

				conn.connect();

				if (postData != null) {
					output = new BufferedOutputStream(conn.getOutputStream());
					output.write(postData.getBytes());
					output.flush();
				}

				if (conn.getResponseCode() < 300) {
					input = conn.getInputStream();
					InputStreamReader in = new InputStreamReader(input);
					StringBuilder sb = new StringBuilder();
					char buf[] = new char[4096];
					int cnt;
					String str;

					while ((cnt = in.read(buf, 0, buf.length)) != -1) {
						sb.append(buf, 0, cnt);
					}
					str = sb.toString();
					if (true) {
						Log.d(TAG, "JSON: " + str);

						JSONObject obj;
						obj = new JSONObject(str);

						if (dealJSONObject(obj))
							ret = true;
					}
				}

				ret = true;
			} catch (ConnectException e) {
				Log.d(TAG,
						"connect to download bitmap failed! " + e.getMessage());
			} catch (Exception e) {
				Log.d(TAG, "http request failed! " + e.getMessage());
			} finally {
				try {
					if (output != null)
						output.close();
					if (input != null)
						input.close();
					if (conn != null)
						conn.disconnect();
				} catch (Exception e) {
				}
			}

			return ret;
		}

		boolean process_notify() {
			HttpURLConnection conn = null;
			InputStream input = null;
			OutputStream output = null;
			boolean ret = false;

			try {
				Log.d(TAG, "url_notify: " + url_notify.toString());
				conn = (HttpURLConnection) this.url_notify.openConnection();

				if (postData != null)
					conn.setDoOutput(true);
				conn.setDoInput(true);
				conn.setRequestProperty("User-Agent", getUserAgent());

				conn.connect();

				if (postData != null) {
					output = new BufferedOutputStream(conn.getOutputStream());

					output.write(postData.getBytes());

					output.flush();
				}

				if (conn.getResponseCode() < 300) {
					input = conn.getInputStream();

					List<ISTVSysNotify> value = readXML(input);
					addSysnotifyToDB(value);
					ret = true;
				}

				ret = true;
			} catch (ConnectException e) {
				Log.d(TAG,
						"connect to download bitmap failed! " + e.getMessage());
			} catch (Exception e) {
				Log.d(TAG, "http request failed! " + e.getMessage());
			} finally {
				try {
					if (output != null)
						output.close();
					if (input != null)
						input.close();
					if (conn != null)
						conn.disconnect();
				} catch (Exception e) {
				}
			}

			return ret;
		}

		Bundle geo_weather_bundle;

		private boolean dealJSONObject(JSONObject obj) {
			ISTVVodHomeGeoWeather geoWeather = new ISTVVodHomeGeoWeather();
			Log.d(TAG, "get geoWeather");

			geoWeather.name_en = obj.optString("name_en");
			geoWeather.name = obj.optString("name");
			geoWeather.geoid = obj.optString("geoid");

			JSONObject today = obj.optJSONObject("today");

			// if(array!=null){
			// if(array.length()>0){
			// JSONObject tomorrow = array.getJSONObject(0);
			geoWeather.wind_direction = today.optString("wind_direction");
			geoWeather.date = today.optString("date");
			geoWeather.phenomenon = today.optString("phenomenon");
			geoWeather.wind_power = today.optString("wind_power");
			geoWeather.temperature = today.optString("temperature");
			// }
			// }

			geo_weather_bundle = new Bundle();
			geo_weather_bundle.putString("name_en", geoWeather.name_en);
			geo_weather_bundle.putString("name", geoWeather.name);
			geo_weather_bundle.putString("geoid", geoWeather.geoid);
			geo_weather_bundle.putString("date", geoWeather.date);
			geo_weather_bundle.putString("wind_direction",
					geoWeather.wind_direction);
			geo_weather_bundle.putString("phenomenon", geoWeather.phenomenon);
			geo_weather_bundle.putString("wind_power", geoWeather.wind_power);
			geo_weather_bundle.putString("temperature", geoWeather.temperature);

			Log.d(TAG, "###################" + geoWeather.name_en + "****"
					+ geoWeather.date + "****" + geoWeather.phenomenon + "****"
					+ geoWeather.temperature);

			Intent intent = new Intent("com.amlogic.weather.TopStatus.onUpdate");
			intent.putExtras(geo_weather_bundle);
			sendBroadcast(intent);
			return true;
		}

		public boolean querySysNotifyById(String id) {

			Cursor cursor = getContentResolver().query(
					ISTVStatusBarService.CONTENT_URI, null,
					"id=\"" + id + "\"", null, null);

			if (cursor == null)
				return false;
			int n = cursor.getCount();
			cursor.close();

			if (n > 0)

				return true;
			else
				return false;

			/*
			 * while(cursor.moveToNext()) { String title =
			 * cursor.getString(cursor.getColumnIndex(this.TITLE));
			 * Log.d(TAG,"title"+title); String summary =
			 * cursor.getString(cursor.getColumnIndex(this.SUMMARY));
			 * Log.d(TAG,"summary"+summary); String updated =
			 * cursor.getString(cursor.getColumnIndex(this.UPDATED));
			 * Log.d(TAG,"updated"+updated); } cursor.close();
			 */
		}

		public void insertSysNotify(String id, String title, String summary,
				String updated) {

			ContentValues cv = new ContentValues();
			cv.put("id", id);
			cv.put("title", title);
			cv.put("summary", summary);
			cv.put("updated", updated);
			getContentResolver().insert(ISTVStatusBarService.CONTENT_URI, cv);
		}

		public void addSysnotifyToDB(List<ISTVSysNotify> value) {

			Log.d(TAG, "addSysnotifyToDB===" + value.size());

			if (value.size() == 0)
				return;

			for (int i = 0; i < value.size(); i++) {
				String id = value.get(i).getId();
				Log.d(TAG, "ID===" + id);

				if (querySysNotifyById(id)) {
					Log.d(TAG, "continue");
					continue;
				} else {
					Log.d(TAG, "continue" + value.get(i).getId() + "***"
							+ value.get(i).getTitle() + "***"
							+ value.get(i).getSummary() + "***"
							+ value.get(i).getUpdated());
					insertSysNotify(value.get(i).getId(), value.get(i)
							.getTitle(), value.get(i).getSummary(), value
							.get(i).getUpdated());

					Bundle bundle = new Bundle();
					bundle.putString("notify_content", value.get(i).getTitle());

					Intent intent = new Intent(
							"com.ismartv.ISTVStatusBar.sysnotify");
					intent.putExtras(bundle);
					sendBroadcast(intent);

				}
			}

		}

		public List<ISTVSysNotify> readXML(InputStream inStream) {

			List<ISTVSysNotify> sysnotify = new ArrayList<ISTVSysNotify>();
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			try {
				DocumentBuilder builder = factory.newDocumentBuilder();
				Document dom = builder.parse(inStream);

				Element root = dom.getDocumentElement();

				NodeList items = root.getElementsByTagName("entry");
				Log.d(TAG, "entry" + items.getLength());
				for (int i = 0; i < items.getLength(); i++) {
					ISTVSysNotify sysnotify_node = new ISTVSysNotify();

					Element element_node = (Element) items.item(i);

					NodeList childsNodes = element_node.getChildNodes();
					Log.d(TAG,
							"childsNodes getLength==="
									+ childsNodes.getLength());
					for (int j = 0; j < childsNodes.getLength(); j++) {
						Node node = (Node) childsNodes.item(j);

						if (node.getNodeType() == Node.ELEMENT_NODE) {

							Element childNode = (Element) node;
							Log.d(TAG,
									"childNode.getNodeName()"
											+ childNode.getNodeName());

							if ("title".equals(childNode.getNodeName())) {
								Log.d(TAG, "titel value="
										+ childNode.getFirstChild()
												.getNodeValue());
								sysnotify_node.setTitle(childNode
										.getFirstChild().getNodeValue());
							} else if ("link".equals(childNode.getNodeName())) {
								Log.d(TAG,
										"link href"
												+ childNode
														.getAttribute("href"));
								Log.d(TAG,
										"link rel"
												+ childNode.getAttribute("rel"));
							} else if ("updated"
									.equals(childNode.getNodeName())) {
								Log.d(TAG, "updated="
										+ childNode.getFirstChild()
												.getNodeValue());
								sysnotify_node.setUpdated(childNode
										.getFirstChild().getNodeValue());
							} else if ("id".equals(childNode.getNodeName())) {
								Log.d(TAG, "id="
										+ childNode.getFirstChild()
												.getNodeValue());
								sysnotify_node.setId(childNode.getFirstChild()
										.getNodeValue());
							} else if ("summary"
									.equals(childNode.getNodeName())) {
								Log.d(TAG, "summary="
										+ childNode.getFirstChild()
												.getNodeValue());
								sysnotify_node.setSummary(childNode
										.getFirstChild().getNodeValue());
							} else if ("category".equals(childNode
									.getNodeName())) {
								Log.d(TAG,
										"category term"
												+ childNode
														.getAttribute("term"));
							} else if ("icon".equals(childNode.getNodeName())) {

							}
						}
					}

					sysnotify.add(sysnotify_node);
				}

				inStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

			return sysnotify;
		}
	}

}
