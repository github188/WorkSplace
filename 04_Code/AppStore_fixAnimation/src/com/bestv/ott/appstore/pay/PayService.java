package com.bestv.ott.appstore.pay;
  

import java.util.List;

import com.bestv.ott.appstore.utils.AppLog;
import com.bestv.ott.appstore.utils.OrdersCaCheManager;
import com.bestv.ott.util.bean.UserOrder;

import android.app.Service;
import android.content.Intent; 
import android.os.IBinder;
import android.util.Log; 

/**
 * 支付管理后台服务<br>
 * 在系统启动或者应用第一次运行的时候启动，获取用户应用信息<br>
 * TODO 数据需要保存在本地数据库(暂未实现)
 * @author penghui
 *
 */
public class PayService extends Service{  
	private static final String TAG = "PayService" ; 
	
	private PayMng mPayMng;
	@Override  
    public void onCreate(){  
        super.onCreate();   
        Log.d(TAG ," -----> pid:"+String.valueOf(android.os.Process.myPid()));
		Log.d(TAG ," -----> tid:"+String.valueOf(android.os.Process.myTid()));
		
		mPayMng = new PayMng(this);
		mPayMng.loadAllUserOrders(new PayMng.CallbackActionListener() {
			@Override
			public void execute() { 
				_store();
			}
		},this); 
    }  

	protected void _store() { 
		Log.d(TAG,"store >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" + mPayMng.getUserOrders() + "/" + (mPayMng.getUserOrders().size()));
		if(mPayMng.getUserOrders().size()>0){
		    List a = mPayMng.getUserOrders();
		    for(int i=0;i<a.size();i++){
		        UserOrder b = (UserOrder) a.get(i);
		        AppLog.d(TAG, ">>>>>>>>>>>>>>>history>>>>>>> ProductCode = "+b.getProductCode());
		        AppLog.d(TAG, ">>>>>>>>>>>>>>>history>>>>>>> OrderTime = "+ b.getOrderTime());
		    }
			OrdersCaCheManager.getInstance().addOrders(mPayMng.getUserOrders());
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onDestroy() {
		if(mPayMng != null){
			mPayMng.destroy();
		}
		super.onDestroy();
	}  
    
}


