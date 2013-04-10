package com.bestv.ott.appstore.utils;

import java.util.HashMap;

import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import com.bestv.ott.appstore.activity.GiftsPackActivity;
import com.bestv.ott.appstore.common.AppsBean;
import com.bestv.ott.appstore.pay.PayMng;
import com.bestv.ott.appstore.pay.PayMng.CallbackActionListener;
import com.bestv.ott.util.bean.ResultParam;
import com.bestv.ott.util.bean.SubsParam;

public class AsyncAppCacheLoader {
    private static final String TAG = "AsyncAppCacheLoader";
    private static HashMap<Integer, String> appCache ;
    PayMng mPayMng;
    public AsyncAppCacheLoader(PayMng payMng) {
//        appCache = new HashMap<String, SoftReference<Drawable>>();
        appCache = new HashMap<Integer, String>();
        this.mPayMng=payMng;
    }
    
    public interface DateCallback {
        public void dateLoaded(int id, String appStatus);
    }
    
    public String loadDate(final int i , final AppsBean bean,final DateCallback dateCallback) {
        AppLog.d(TAG, "-----checkAppPayMng---- serviceCode="+bean.getServiceCode()  +"   |name"+bean.getAppName());
        AppLog.d(TAG, "-----checkAppPayMng---- AppProductCode="+bean.getAppProductCode());
        
        if (appCache.containsKey(bean.getID())) {
            AppLog.d(TAG, "------- appDataCache ----- isExist ------ name="+bean.getAppName());
            String status = appCache.get(bean.getID());
            return status;
        }
        final Handler handler = new Handler() {
            public void handleMessage(Message message) {
                dateCallback.dateLoaded(i, (String) message.obj);
            }
        };
        new Thread() {
            public void run() {
                SubsParam subsParam2 = new SubsParam();
                subsParam2.setServiceCodes(bean.getServiceCode());
                subsParam2.setProductCode(bean.getAppProductCode());
                subsParam2.setAuthType(PayMng.AUTHTYPE_PRODUCT);
                subsParam2.setBizType(PayMng.BIZTYPE_APP);
//                mPayMng.authOrder(new CallbackActionListener() {
//                    public void execute() {
//                        if(mPayMng.getResult()!=null){
//                            ResultParam mResult = (ResultParam)mPayMng.getResult();
//                            int retCode = mResult.getReturnCode();
//                            AppLog.d(TAG, "---- checkAppPayMng --> retCode---- "+retCode);
//                            if(retCode==PayMng.REQ_AUTH_SUCCESS){
//                                String status = loadStatusFromRetCode(true);
//                                appCache.put(bean.getID(), status);
//                                Message message = handler.obtainMessage(0, status);
//                                handler.sendMessage(message);
//                            }else{
//                                String status = loadStatusFromRetCode(false);
//                                appCache.put(bean.getID(), status);
//                                Message message = handler.obtainMessage(0, status);
//                                handler.sendMessage(message);
//                            }
//                        }
//                    }
//                }, subsParam2);
            }
        }.start();
        return null;
    }
    
    private String loadStatusFromRetCode(boolean tag) {
        if(tag){
            return "已订购";
        }else{
            return "未订购";
        }
    }
    
    public void clearCache(){
        appCache.clear();
    }
}
