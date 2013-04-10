package novel.supertv.appmng;

import java.util.ArrayList;
import java.util.List;

import novel.supertv.appmng.db.DBUtils;

import android.app.Activity; 
import android.app.ProgressDialog;
import android.content.Context; 
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo; 
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;  
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView; 
import android.widget.ImageView; 
import android.widget.RelativeLayout;
import android.widget.TextView; 
import android.widget.AdapterView.OnItemClickListener;

public class ShowAppActivity extends Activity implements Runnable,
		OnItemClickListener {

	/**
	 * 检索应用
	 */
	private static final int SEARCH_APP = 0;
	private static final int DELETE_APP = 1;
	
	private static final String TAG = "novel.supertv.db.ShowAppActivity";
	/**
	 * 应用包名
	 */
	private static final String PACKAGE_NAME = "novel.supertv.appmng";
	/**
	 * 显示应用网格
	 */
	private GridView mGridView;  
	/**
	 * 需要显示的应用集合
	 */
	private List<PackageInfo> userPackageInfos; 
	DBUtils mDBUtisl=null;

	private ProgressDialog mProgressDialog; 

	private Handler mHandler = new Handler() { 
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			switch(msg.what){
			case SEARCH_APP: 
				mGridView.setAdapter(new GridViewAdapter(ShowAppActivity.this,userPackageInfos));
//				mProgressDialog.dismiss();
				setProgressBarIndeterminateVisibility(false);
				break;  
			} 
		} 
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "-------------------onCreate----------");
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.main4tv);
		mDBUtisl=new DBUtils(this);
		setProgressBarIndeterminateVisibility(true);
		mGridView = (GridView) this.findViewById(R.id.gv_apps);
		mGridView.setOnItemClickListener(this); 
		
//		mProgressDialog = ProgressDialog.show(this, getString(R.string.app_load_title), getString(R.string.app_loading), true,false);
		new Thread(this).start();

	}
	/**
     * 取setting背景图，用于设置成这个activity的背景
     * @return
     */
    public Drawable getThemePaper(){
        String url = Settings.System.getString(this.getContentResolver(), "settings.theme.url");
        if(url!=null && url.length()>0){
                Bitmap bitmap = BitmapFactory.decodeFile(url);
                Drawable drawable = new BitmapDrawable(bitmap);
                return drawable;
        }
        return null;
    }

	class GridViewAdapter extends BaseAdapter { 
		LayoutInflater inflater;  
		List<PackageInfo> pkInfos; 
		public GridViewAdapter(Context context, List<PackageInfo> packageInfos) {
			inflater = LayoutInflater.from(context);
			this.pkInfos = packageInfos;
		}
		@Override
		public int getCount() {
			return pkInfos.size();
		}
		@Override
		public Object getItem(int arg0) {
			return pkInfos.get(arg0);
		}
		@Override
		public long getItemId(int position) {
			return position;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = inflater.inflate(R.layout.gv_item, null);
			TextView tv = (TextView) view.findViewById(R.id.gv_item_appname);
			ImageView iv = (ImageView) view.findViewById(R.id.gv_item_icon);
			tv.setText(pkInfos.get(position).applicationInfo.loadLabel(getPackageManager()));
			Drawable da = pkInfos.get(position).applicationInfo.loadIcon(getPackageManager());
			StringBuffer sb = new StringBuffer();
			sb.append("/data/data/novel.supertv.appmng/");
			sb.append(""+position);
			Utils.saveBitmap(Utils.drawableToBitmap(da),sb.toString() ,null);
			iv.setImageDrawable(da);
			return view;
		} 
	} 
	
	

	@Override
	public void run() {
		userPackageInfos = new ArrayList<PackageInfo>();
		List<PackageInfo> packageInfos = getPackageManager().getInstalledPackages(0);
		for (int i = 0; i < packageInfos.size(); i++) { 
			PackageInfo temp = packageInfos.get(i);
			if(PACKAGE_NAME.equals(temp.packageName))
				continue ;
			if(Utils.filterApp(temp.applicationInfo)!=Constants.APP_SOURCE_INNER){
				if(getPackageManager().getLaunchIntentForPackage(temp.packageName)!=null)
				    userPackageInfos.add(temp);
			}else if(Constants.SHOWSYSTEMAPP.contains(temp.packageName)){
				if(getPackageManager().getLaunchIntentForPackage(temp.packageName)!=null)
				    userPackageInfos.add(temp);
			}
		}
		//去除三奥相关的apk
		List<PackageInfo> sanaoList = new ArrayList<PackageInfo>();
		for (PackageInfo appInfo : userPackageInfos) {
            if (appInfo.packageName.trim().startsWith("com.sanao")) {
                sanaoList.add(appInfo);
            }
        }
		userPackageInfos.removeAll(sanaoList);
		
		mHandler.sendEmptyMessage(SEARCH_APP);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		PackageInfo tempPkInfo = userPackageInfos.get(position);
		if(tempPkInfo==null){
			Utils.showTipToast(Gravity.CENTER,ShowAppActivity.this, ShowAppActivity.this.getString(R.string.start_error));
			return;
		}
		String packageName = tempPkInfo.packageName;
		Log.d(TAG, "----------:"+packageName);
		Intent intent = null;
		intent = getPackageManager().getLaunchIntentForPackage(packageName);
		if(intent!=null){
			startActivity(intent); 
			mDBUtisl.addAppCountsByPkgName(packageName);
		}else{
			Utils.showTipToast(Gravity.CENTER,ShowAppActivity.this, ShowAppActivity.this.getString(R.string.start_error));
		}
	}
	
//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		if(keyCode==KeyEvent.KEYCODE_DPAD_UP){
//			mDBUtisl.getCountByName();
//		}
//		return super.onKeyDown(keyCode, event);
//	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d(TAG,"################ onActivityResult >> " + requestCode  );  
	}

	@Override
    protected void onResume() {
        Drawable bg = getThemePaper();
        if (bg != null) {
            RelativeLayout rootView = (RelativeLayout) (this
                    .findViewById(R.id.appmanager_main_rl));
            rootView.setBackgroundDrawable(bg);
        } else {
            Log.d(TAG, " getThemePaper Error");
        }
        super.onResume();
    }
 

	@Override
	protected void onDestroy() {
		userPackageInfos.clear();
		super.onDestroy(); 
	}
	
}