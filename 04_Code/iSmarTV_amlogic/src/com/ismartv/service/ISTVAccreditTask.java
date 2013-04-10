package com.ismartv.service;

import android.util.Log;

import com.ismartv.util.StringUtils;

import org.json.JSONObject;

/**
 * 获取授权码
 * @author chenggang
 *
 */
public class ISTVAccreditTask extends ISTVJsonTask{

    private static final String TAG="ISTVAccreditTask";
//    private static String url="http://192.168.0.13:8080/oms/oms/syAuthorize!doGetauthorizeId.action";//公司服务器地址
    private static String url="http://shiyunvod.joyseetv.com:8078/oms/oms/syAuthorize!doGetauthorizeId.action";//公司服务器地址
    
    ISTVAccreditTask(ISTVTaskManager man) {
        super(man, url, PRIO_ACCREDIT, 1,null);
        addPostData("deviceId="+getMACAddress());
        start();
    }
    
    boolean onGotJson(JSONObject obj) throws Exception{
        String accredit = obj.getString("authorizeId");
        Log.d(TAG, "get Accredit: "+accredit);
        if(accredit==null||accredit.trim().equals("")||accredit.trim().equals("null")||accredit.trim().length()<12)
            return false;
        getEpg().setAccredit(accredit);
        return true;
    }
    
    @Override
    void onCancel() {
        Log.d(TAG, "onCancel");
        //先注掉，以后要打开
        if(getEpg().getAccredit()==null||getEpg().getAccredit().equals("")){//表示文件中没有，后台服务器也没有
            Log.d(TAG, "---------Accredit-is null");
//            getManager().setNullService(1);
//            return;
        }
        running = false;
        getManager().removeTask(this);
        super.onCancel();
    }

    void onGetDataError(){
        setError(ISTVError.CANNOT_ACTIVE);
    }

}
