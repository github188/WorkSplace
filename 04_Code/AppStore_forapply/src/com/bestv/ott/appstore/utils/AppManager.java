package com.bestv.ott.appstore.utils;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import com.bestv.ott.appstore.common.ApplicationBean;
import com.bestv.ott.appstore.common.Constants;

/**
 * 获取机顶盒内置所有应用 / 第三方应用 / AppStore内置应用
 * 
 * @author benz
 * 
 */
public class AppManager {

	private static final String TAG = "com.bestv.ott.appstore.utils.AppManager";
	private Context context;

	public AppManager(Context context) {
		this.context = context;
	}

	// 系统中应用列表,用于加载，升级时
	public List<ApplicationBean> getAllInstallApp() {

		List<ApplicationBean> applists = new ArrayList<ApplicationBean>();

		PackageManager pManager = context.getPackageManager();
		List<PackageInfo> packages = pManager.getInstalledPackages(0);
		for(PackageInfo pakg:packages){
			ApplicationBean appBean = new ApplicationBean();
			appBean.setAppName(pakg.applicationInfo.loadLabel(pManager).toString());
			appBean.setPkgName(pakg.packageName);
			Drawable drawable = pakg.applicationInfo.loadIcon(pManager);
			Bitmap b = drawableToBitmap(drawable);
			byte[] icon = Bitmap2Bytes(b);
			appBean.setIcon(icon);
			appBean.setVersion(""+pakg.versionCode);
			appBean.setAppSource(filterApp(pakg.applicationInfo));
//			AppLog.d(TAG, "getAllInstallApp-  ------------------appName="+appBean.getAppName()+";pkgName="+appBean.getPkgName());
			applists.add(appBean);
		}
		
		return applists;
	}
	
	public List<ApplicationBean> getSystemApp(){
		List<ApplicationBean> systemApp = new ArrayList<ApplicationBean>();
		PackageManager pManager = context.getPackageManager();
		List<PackageInfo> packages = pManager.getInstalledPackages(0);
		for(PackageInfo pakg:packages){
			if(filterApp(pakg.applicationInfo)!=Constants.APP_SOURCE_STORE){
				ApplicationBean appBean = new ApplicationBean();
				appBean.setAppName(pakg.applicationInfo.loadLabel(pManager).toString());
				appBean.setPkgName(pakg.packageName);
				Drawable drawable = pakg.applicationInfo.loadIcon(pManager);
				Bitmap b = drawableToBitmap(drawable);
				byte[] icon = Bitmap2Bytes(b);
				appBean.setIcon(icon);
				appBean.setVersion(""+pakg.versionCode);
				systemApp.add(appBean);
//				AppLog.d(TAG, "getSystemApp--ApplicationInfo-------appName=" + appBean.getAppName()+ ";pkgName="+pakg.packageName+";versionCode="+pakg.versionCode);
			}
		}
		AppLog.e(TAG, "---------getSystemApp--application--count="+systemApp.size());
		return systemApp;
	}
	
	//把系统已安装应用来源
	private int filterApp(ApplicationInfo info) {
		// 代表的是系统的应用,但是被用户升级了 用户应用 
		if ((info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
			return Constants.APP_SOURCE_INNER_UPDATE;
		// 代表的用户的应用 
		} else if ((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
			return Constants.APP_SOURCE_STORE;
		}
		return Constants.APP_SOURCE_INNER;
	}

	//drawable-->bitmap
	public static Bitmap drawableToBitmap(Drawable drawable) {

		Bitmap bitmap = Bitmap
				.createBitmap(
						drawable.getIntrinsicWidth(),
						drawable.getIntrinsicHeight(),
						drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
								: Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		// canvas.setBitmap(bitmap);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight());
		drawable.draw(canvas);
		return bitmap;
	}

	//bitmap-->byte[]
	public static byte[] Bitmap2Bytes(Bitmap bm) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}
}
