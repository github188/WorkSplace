package com.joysee.appstore.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.SimpleAdapter.ViewBinder;

import com.joysee.appstore.R;
import com.joysee.appstore.common.ApplicationBean;
import com.joysee.appstore.common.Constants;
import com.joysee.appstore.common.DataOperate;
import com.joysee.appstore.common.PageBean;
import com.joysee.appstore.db.DBUtils;
import com.joysee.appstore.service.DownloadService;
import com.joysee.appstore.service.DownloadService.ServiceBinder;
import com.joysee.appstore.utils.AppLog;
import com.joysee.appstore.utils.FileUtils;

/**
 * 本地应用
 * @author Administrator
 *
 */
public class NativeAppActivity extends Activity{
    
    public static final String TAG = "com.joysee.appstore.NativeAppActivity";
    
    public static final int MSG_SHOW_APP=0;//显示应用数据
    public static final int MSG_GET_ALL_INSTALL=1;//获取所有应用数据
    public static final int MSG_GET_ALL_UPDATE=2;//获取所有可升级应用
    public static final int MSG_GET_APP=3;//获取一页应用数据
    public static final int MSG_GET_UPDATE=4;//获取一页可升级应用

    
    private ImageButton leftButton;
    private ImageButton rightButton;
    private Button installBut;
    private Button updateBut;
    private GridView gridView;
    private TextView pageView;
    private TextView downRecord;
    private List<ApplicationBean> allInstall;
    private List<ApplicationBean> allUpdate;
    private SimpleAdapter appsSimleAdapter;
    private List <Map<String, Object>> adpaterList=new ArrayList<Map<String, Object>>();
    private static List<ApplicationBean> mList=null;//当前显示的应用
    private DBUtils tDBUtils;
    private int type=0;//0为已安装，1为可升级
    private ProgressDialog progress;
    private PageBean pageBean=new PageBean();
    private DataOperate dataOperate;
    private long firstTime;
    private long nextTime;
    public boolean right=false;//gridview是否到了最右边
    public boolean left=false;//girdview是否到了最左边
    
    private AlertDialog listDialog;//弹出窗口
    private ApplicationBean selectApp;
    private AppReceived appReceived;
    
    public HandlerThread workThread;
    public static Handler workHandler;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppLog.d(TAG," onCreate ");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.navite_app_layout);
        init();
        bindService(new Intent("com.joysee.appstore.service.DownloadService"),mServiceConnection, Context.BIND_AUTO_CREATE);
        dataOperate=new DataOperate(NativeAppActivity.this,mainHandler);
        dataOperate.setPageBean(pageBean);
        workThread=new HandlerThread("handler_thread");
        workThread.start();
        workHandler=new Handler(workThread.getLooper()){
            public void handleMessage(Message msg) {
                switch(msg.what){
                case MSG_GET_ALL_INSTALL:
                    allInstall=tDBUtils.queryApplicationByStatus(Constants.APP_STATUS_INSTALLED);
                    AppLog.d(TAG, "-------MSG_GET_ALL_INSTALL------allinstall.size()="+allInstall.size());
                    pageBean.setPageNo(1);
                    pageBean.calculatePageTotal(allInstall.size());
                    workHandler.sendEmptyMessageDelayed(MSG_GET_APP, 500);
                    AppLog.d(TAG, "MSG_GET_ALL_INSTALL  mList.size="+allInstall.size());
                    break;
                case MSG_GET_ALL_UPDATE:
                    if(null!=allInstall&&allInstall.size()>0){
                        allUpdate=dataOperate.getNativeUpdateAppsList(allInstall);
                        pageBean.setPageNo(1);
                        pageBean.calculatePageTotal(allUpdate.size());
                        workHandler.sendEmptyMessage(MSG_GET_APP);
                        AppLog.d(TAG, "MSG_GET_ALL_UPDATE  mList.size="+allUpdate.size());
                    }else{
                        AppLog.d(TAG, "MSG_GET_ALL_UPDATE  mList.size=0");
                    }
                    break;
                case MSG_GET_APP:
                    if(type==0){
                        mList=dataOperate.getOnePageAppsList(allInstall);
                    }else if(type==1){
                        mList=dataOperate.getOnePageAppsList(allUpdate);
                    }
                    mainHandler.sendEmptyMessage(MSG_SHOW_APP);
                    AppLog.d(TAG, "MSG_GET_APP mList.size="+mList.size());
                    break;
                }
                super.handleMessage(msg);
            }
        };
    }
    
    
    public void init(){
        gridView=(GridView)findViewById(R.id.app_grid);
        leftButton=(ImageButton)findViewById(R.id.left_image);
        installBut=(Button)findViewById(R.id.install);
        updateBut=(Button)findViewById(R.id.update);
        rightButton=(ImageButton)findViewById(R.id.right_imgae);
        pageView=(TextView)findViewById(R.id.page);
        downRecord=(TextView)findViewById(R.id.top_down_record);
        updateBut.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
                type=1;
                workHandler.sendEmptyMessage(MSG_GET_ALL_UPDATE);
                updateBut.setBackgroundResource(R.drawable.button_selected);
                installBut.setBackgroundResource(R.drawable.background01);
            }
        });
        installBut.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
                type=0;
                workHandler.sendEmptyMessage(MSG_GET_ALL_INSTALL);
                installBut.setBackgroundResource(R.drawable.button_selected);
                updateBut.setBackgroundResource(R.drawable.background01);
            } 
        });
        downRecord.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                downloadMgr();
            }
        });
        
        LayoutAnimationController controller = new LayoutAnimationController(AnimationUtils.loadAnimation(this,R.anim.list_animation), 1);
        gridView.setLayoutAnimation(controller);
        tDBUtils = new DBUtils(NativeAppActivity.this);
        initAppGridView();
        appReceived=new AppReceived();
        IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_REMOVED);
        filter.addDataScheme("package");
        this.registerReceiver(appReceived, filter);
    }
    
    public void showProgressDialog(){
        progress=new ProgressDialog(NativeAppActivity.this);
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setIndeterminate(false);
        progress.setMax(100);  
        progress.setProgress(0);
        progress.setCancelable(true);
        progress.show();
    }
    
    public Handler mainHandler=new Handler(){
        public void handleMessage(Message msg) {
            switch(msg.what){
            case MSG_SHOW_APP:
                AppLog.d(TAG, "MSG_SHOW_APP");
                nextTime=System.currentTimeMillis();
                AppLog.d(TAG, "get "+mList.size()+" apps from service take time="+(nextTime-firstTime));
                getAppsData(mList);
                appsSimleAdapter.notifyDataSetChanged();
                if(mList.size()>0){
                    gridView.requestFocus();
                    gridView.setSelection(0);
                }
                pageView.setText(NativeAppActivity.this.getString(R.string.page,mList.size()>0?pageBean.getPageNo():0,pageBean.getPageTotal()));
                if(pageBean.getPageNo()==1){
                    leftButton.setVisibility(View.INVISIBLE);
                }else{
                    leftButton.setVisibility(View.VISIBLE);
                }
                if(pageBean.getPageNo()==pageBean.getPageTotal()){
                    rightButton.setVisibility(View.INVISIBLE);
                }else{
                    rightButton.setVisibility(View.VISIBLE);
                }
                if(pageBean.getPageTotal()<2){
                    leftButton.setVisibility(View.INVISIBLE);
                    rightButton.setVisibility(View.INVISIBLE);
                }
                break;
            }
            super.handleMessage(msg);
        }
    };
    
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        AppLog.d(TAG, "---------keyCode---------"+keyCode);
        switch(keyCode){
        case KeyEvent.KEYCODE_DPAD_CENTER:
        case KeyEvent.KEYCODE_ENTER:
            break;
        case KeyEvent.KEYCODE_DPAD_DOWN:
            break;
        case KeyEvent.KEYCODE_DPAD_LEFT:
            if(left){
                previousPage();
            }
            break;
        case KeyEvent.KEYCODE_DPAD_RIGHT:
            if(right){
                nextPage();
            }
            break;
        case KeyEvent.KEYCODE_DPAD_UP:
            break;
        }
        return super.onKeyDown(keyCode, event);
    }
    
    /**
     * 下一页
     */
    public void nextPage(){
        if(!pageBean.nextPage()){//表示没有下一页
            return;
        }
        workHandler.removeMessages(MSG_GET_APP);
        workHandler.sendEmptyMessageDelayed(MSG_GET_APP,500);
    }
    
    /**
     * 上一页
     */
    public void previousPage(){
        if(!pageBean.previousPage()){//表示没有上一页
            return;
        }
        workHandler.removeMessages(MSG_GET_APP);
        workHandler.sendEmptyMessageDelayed(MSG_GET_APP,500);
    }
    
    /**
     * 进入下载管理 
     */
    public void downloadMgr() {
        Intent intent = new Intent();
        intent.setClassName(NativeAppActivity.this, "com.joysee.appstore.activity.DownloadMgrActivity");
        startActivity(intent);
    }
    
    public void initAppGridView(){
        appsSimleAdapter=new SimpleAdapter(NativeAppActivity.this,adpaterList,R.layout.app_store_item_layout
                ,new String[]{"name","image"},new int[]{R.id.item_name,R.id.item_top});
        gridView.setAdapter(appsSimleAdapter);
        appsSimleAdapter.setViewBinder(new ViewBinder(){
            @Override
            public boolean setViewValue(View view, Object data,
                    String textRepresentation) {
                if(view instanceof ImageView){
                    Bitmap bitmap=FileUtils.byteToBitmap((byte[])data);
                    ((ImageView) view).setImageBitmap(bitmap);
                }else if(view instanceof TextView){
                    ((TextView)view).setText(""+data.toString());
                }
                return true;
            }
        });
        gridView.setOnItemSelectedListener(new OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                    int arg2, long arg3) {
                AppLog.d(TAG, "item arg2="+arg2);
                if(arg2==(appsSimleAdapter.getCount()/2-1)||arg2==appsSimleAdapter.getCount()-1){
                    right=true;
                }else if(arg2==0||arg2==Constants.MAX_LENGTH/2){
                    left=true;
                }else{
                    right=false;
                    left=false;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        gridView.setOnItemClickListener(new OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3) {
                selectApp=mList.get(gridView.getSelectedItemPosition());
                AppLog.d(TAG, "--gridView.getSelectedItemPosition()="+arg2);
                appSelectDialog();
            }
        });
    }
    /**
     * 组装adpater数据
     * @param listApp
     */
    public void getAppsData(List<ApplicationBean> listApp){
        Map<String, Object> map;
        adpaterList.clear();
        for(int i=0;i<listApp.size();i++){
            map= new HashMap<String, Object>();
            map.put("name",listApp.get(i).getAppName());
            map.put("image",listApp.get(i).getIcon());
            adpaterList.add(map);
        }
    }
    
    /**
     * 功能选择弹出窗口
     * @param pkgName
     */
    public void appSelectDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View viewLayout = LayoutInflater.from(NativeAppActivity.this).inflate(R.layout.app_layout, null);
        ListView listView = (ListView) viewLayout.findViewById(R.id.appslist);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("img", R.drawable.down_record_icon);
        map.put("title", getString(R.string.update_ven));
        list.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.down_record_icon);
        map.put("title", getString(R.string.del_icon));
        list.add(map);
        map = new HashMap<String, Object>();
        map.put("img", R.drawable.down_record_icon);
        map.put("title", getString(R.string.del_app));
        list.add(map);
        SimpleAdapter adapter = new SimpleAdapter(NativeAppActivity.this,
                list, R.layout.app_item_layout,
                new String[] { "img", "title" }, new int[] { R.id.img,
                        R.id.title });
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                listDialog.dismiss();
                switch(position){
                case 0:// update
                    updateApp();
                    break;
                case 1:// delete icon
                    deleteShortCut();
                    break;
                case 2:// delete app
                    deleteApp();
                    break;
                case 3:// sort
                    sortApp();
                    break;
                }
            }
        });
        builder.setView(viewLayout);
        listDialog = builder.create();
        ImageButton close = (ImageButton) viewLayout.findViewById(R.id.close);
        close.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                listDialog.dismiss();
            }
        });
        listDialog.getWindow().setLayout(350, 400);
        listDialog.show();
    }
    
    private ServiceBinder downloadService;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceDisconnected(ComponentName name) {
            downloadService = null;
        }
        public void onServiceConnected(ComponentName name, IBinder service) {
        	AppLog.d(TAG, "================enter onServiceConnected====");
        	downloadService = ((DownloadService.ServiceBinder)service).getService();
        }
    };

    /**
     * download application method
     * 
     * @param path
     */
    public void download(final String url,String appName,String iconUrl) {
        // 先简单测试
        File baseDir = Environment.getExternalStorageDirectory();
        File downDir = new File(baseDir, "joysee");
        if(downloadService!=null){
            downloadService.startDownload(url, downDir.getAbsolutePath(),appName,iconUrl);
        }
    }
    
    private void updateApp(){
        download(selectApp.getUrl(),selectApp.appName, selectApp.getIconUrl());
    }
    
    /**
     * 删除APK
     */
    private void deleteApp(){
        AppLog.d(TAG, "deleteApp");
        String pkgName=selectApp.getPkgName();
        Uri uri = Uri.parse("package:"+pkgName);
        AppLog.d(TAG, "the app is deleted appUri="+uri.toString()+";appName="+selectApp.appName);
        startActivity(new Intent(Intent.ACTION_DELETE, uri));
    }
    
    private void deleteShortCut(){
        AppLog.d(TAG, "deleteShortCut");
    }

    private void sortApp(){
        AppLog.d(TAG, "sortApp");
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        AppLog.d(TAG," onResume ");
        if(!workThread.isAlive()){
            workThread.start();
        }
        workHandler.sendEmptyMessage(MSG_GET_ALL_INSTALL);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.unbindService(mServiceConnection);
        this.unregisterReceiver(appReceived);
    }
    
    public static void refeshApp(){
        workHandler.sendEmptyMessage(MSG_GET_ALL_INSTALL);
    }
    /**
     * 安装卸载APP的监听
     * @author Administrator
     *
     */
    public class AppReceived extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            
            if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) { 
                String packageName = intent.getDataString();  
                int index = packageName.indexOf(":");
                if(index!=-1){
                    packageName=packageName.substring(index+1);
                }
                DBUtils tDBUtils = new DBUtils(context);
                ApplicationBean tBean = tDBUtils.queryApplicationByPkgName(packageName);
                if(null==tBean){
                    AppLog.d(TAG,"--------removed----------bean is null" );
                }
                if(null!=tBean){
                     int count = tDBUtils.deleteOneApplication(packageName);
                     if(count==-1){
                         Log.e(TAG, "delete Application status error");
                     }
                }
                workHandler.sendEmptyMessageDelayed(MSG_GET_ALL_INSTALL,500);
            }
            if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {  
                
            }
        }
    }



}
