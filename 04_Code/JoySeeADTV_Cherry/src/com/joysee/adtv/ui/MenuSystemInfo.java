package com.joysee.adtv.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import org.apache.http.conn.util.InetAddressUtils;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.joysee.adtv.R;
import com.joysee.adtv.ui.Menu.InterceptKeyListener;
import com.joysee.adtv.ui.Menu.MenuListener;

public class MenuSystemInfo extends LinearLayout implements MenuListener {

	private TextView mCpuTextView;
	private TextView mMemTextView;
	private TextView mWifiTextView;
	private TextView mInnerTextView;
	private TextView mSdcardTextView;
	private TextView mAndroidTextView;
	private TextView mkernelTextView;
	private TextView mHardWareTitleTextView;
	private Context mContext;
	private InterceptKeyListener mInterceptKeyListener;

	public MenuSystemInfo(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.mContext = context;
	}

	public MenuSystemInfo(Context context, AttributeSet attrs) {
		this(context, attrs, -1);
	}

	public MenuSystemInfo(Context context) {
		this(context, null);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mCpuTextView = (TextView) findViewById(R.id.menu_sys_cpu);
		mMemTextView = (TextView) findViewById(R.id.menu_sys_mem);
		mWifiTextView = (TextView) findViewById(R.id.menu_sys_wifi);
		mInnerTextView = (TextView) findViewById(R.id.menu_sys_inner);
		mSdcardTextView = (TextView) findViewById(R.id.menu_sys_sdcard);
		mAndroidTextView = (TextView) findViewById(R.id.menu_sys_android);
		mkernelTextView = (TextView) findViewById(R.id.menu_sys_kernel);
		mHardWareTitleTextView = (TextView) findViewById(R.id.hard_ware_title);
	}

	public void fillData() {
		mCpuTextView.setText(Build.CPU_ABI);
		mMemTextView.setText(getTotalMemory());
		mWifiTextView.setText(getWifiAddr());
		mWifiTextView.setText(getLocalIpAddress());
		// mWifiTextView.setText(getHostIp());
		Log.d("songwenxuan", "getHostIP = " + getLocalIpAddress());
		mInnerTextView.setText(getInnerStorageTotal() + "G");

		mSdcardTextView.setText(getSdcardSpace());
		mAndroidTextView.setText(Build.VERSION.RELEASE);
		mkernelTextView.setText(getVersion());
	}

	private String getSdcardSpace() {
		DecimalFormat df = new DecimalFormat("##0.0");
		String str = df.format(getSDCardMemory()[0] / 1024 / 1024 / 1024)
				+ "G （" + getResources().getString(R.string.menu_sys_remain)
				+ df.format(getSDCardMemory()[1] / 1024 / 1024 / 1024) + "G"
				+ getResources().getString(R.string.menu_sys_available) + "）";
		return str;
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		int action = event.getAction();
		int keyCode = event.getKeyCode();
//		if ((keyCode == KeyEvent.KEYCODE_ESCAPE || keyCode == KeyEvent.KEYCODE_BACK)
//				&& action == KeyEvent.ACTION_DOWN) {
//			mInterceptKeyListener.backSettingParent();
//			return true;
//		}
		if(mInterceptKeyListener.onKeyEvent(keyCode, action)){
			return true;
		}
		return super.dispatchKeyEvent(event);
	}

	public String getVersion() {
		String[] version = { "null", "null", "null", "null", "null", "null" };
		String str1 = "/proc/version";
		String str2;
		String[] arrayOfString;
		try {
			FileReader localFileReader = new FileReader(str1);
			BufferedReader localBufferedReader = new BufferedReader(
					localFileReader, 8192);
			str2 = localBufferedReader.readLine();
			arrayOfString = str2.split("\\s+");
			version[0] = arrayOfString[2];// KernelVersion
			localBufferedReader.close();
		} catch (IOException e) {
		}
		return version[0];
	}

	String getWifiAddr() {
		WifiManager wifimanage = (WifiManager) mContext
				.getSystemService(Context.WIFI_SERVICE);// 获取WifiManager
		WifiInfo wifiinfo = wifimanage.getConnectionInfo();
		Log.d("songwenxuan",
				"wifiinfo.getIpAddress() = " + wifiinfo.getIpAddress());
		String ip = intToIp(wifiinfo.getIpAddress());
		return ip;
	}

	private String intToIp(int i) {
		return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF)
				+ "." + ((i >> 24) & 0xFF);
	}

	private String getTotalMemory() {
		DecimalFormat df = new DecimalFormat("##0.0");
		String str1 = "/proc/meminfo";// 系统内存信息文件
		String str2;
		String[] arrayOfString;
		double initial_memory = 0;
		try {

			FileReader localFileReader = new FileReader(str1);

			BufferedReader localBufferedReader = new BufferedReader(
					localFileReader, 8192);

			str2 = localBufferedReader.readLine();// 读取meminfo第一行，系统总内存大小
			arrayOfString = str2.split("\\s+");

			for (String num : arrayOfString) {

				Log.i(str2, num + "\t");

			}

			initial_memory = Integer.valueOf(arrayOfString[1]).intValue() * 1024;// 获得系统总内存，单位是KB，乘以1024转换为Byte

			localBufferedReader.close();
		} catch (IOException e) {
		}

		return df.format(initial_memory / 1024 / 1024 / 1024) + "G";
	}

	private String getInnerStorageTotal() {
		DecimalFormat df = new DecimalFormat("##0.0");
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		float blockSize = (float) stat.getBlockSize();
		float totalBlocks = (float) stat.getBlockCount();
		float totalMemory = totalBlocks * blockSize / 1024 / 1024 / 1024;
		String str = df.format(totalMemory);
		return str;
	}

	private double[] getSDCardMemory() {
		double[] sdCardInfo = new double[2];
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			File sdcardDir = Environment.getExternalStorageDirectory();
			StatFs sf = new StatFs(sdcardDir.getPath());
			long bSize = sf.getBlockSize();
			long bCount = sf.getBlockCount();
			long availBlocks = sf.getAvailableBlocks();

			sdCardInfo[0] = bSize * bCount;// 总大小
			sdCardInfo[1] = bSize * availBlocks;// 可用大小
		}
		return sdCardInfo;
	}

	public void setInterceptKeyListener(
			InterceptKeyListener interceptKeyListener) {
		mInterceptKeyListener = interceptKeyListener;
	}

	@Override
	public void getFocus() {
		mHardWareTitleTextView.requestFocus();
	}

	@Override
	public void loseFocus() {
	}

	// public String getLocalIpAddress() {
	// try {
	// for (Enumeration<NetworkInterface> en = NetworkInterface
	// .getNetworkInterfaces(); en.hasMoreElements();) {
	// NetworkInterface intf = en.nextElement();
	// for (Enumeration<InetAddress> enumIpAddr = intf
	// .getInetAddresses(); enumIpAddr.hasMoreElements();) {
	// InetAddress inetAddress = enumIpAddr.nextElement();
	// if (!inetAddress.isLoopbackAddress()) {
	// return inetAddress.getHostAddress().toString();
	// }
	// }
	// }
	// } catch (SocketException ex) {
	// Log.e("WifiPreference IpAddress", ex.toString());
	// }
	// return null;
	// }

	public String getLocalIpAddress() {
		try {
			String ipv4;

			ArrayList<NetworkInterface> mylist = Collections
					.list(NetworkInterface.getNetworkInterfaces());

			for (NetworkInterface ni : mylist) {

				ArrayList<InetAddress> ialist = Collections.list(ni
						.getInetAddresses());
				for (InetAddress address : ialist) {
					if (!address.isLoopbackAddress()
							&& InetAddressUtils.isIPv4Address(ipv4 = address
									.getHostAddress())) {
						return ipv4;
					}
				}

			}

		} catch (SocketException ex) {

		}
		return null;
	}
}
