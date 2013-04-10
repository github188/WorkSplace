package com.bestv.ott.appstore.pay;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.util.Log;
 
import com.bestv.ott.appstore.common.Constants;
import com.bestv.ott.appstore.utils.AppLog;
import com.bestv.ott.appstore.utils.OrdersCaCheManager;
import com.bestv.ott.epay.client.IBesTVWalletServiceCallBackListener;
import com.bestv.ott.framework.BesTVServicesMgr;
import com.bestv.ott.framework.IBesTVServicesConnListener;
import com.bestv.ott.framework.services.ApkInfoService;
import com.bestv.ott.framework.services.OrderService;
import com.bestv.ott.framework.services.UserService;
import com.bestv.ott.util.bean.OrderParam;
import com.bestv.ott.util.bean.ResultParam;
import com.bestv.ott.util.bean.SubsParam;
import com.bestv.ott.util.bean.UserOrder;
import com.bestv.ott.util.bean.UserProfile; 

/**
 * 存储和管理用户订购和支付业务<br>
 * 1,应用在启动的时候会自动从中间件获取其订单信息，并保存在内存中<br>
 * 2,能灵活增减应用中的订单交易记录,避免重复调用中间件(异步)<br>
 * 3,提供应用鉴权，订购，充值等功能<br>
 * 4,关闭时候，需注销相关资源
 * @author penghui
 *
 */
public class PayMng {  
	/**
	 * -1:网络异常	
	 * 0：鉴权失败/没有订购
	 * 1：鉴权成功
	 * 2：订购成功
	 * 3：余额不足
	 * >=4：后台系统异常
	 */
	public static final int REQ_NETWORK_ERROR = -1 ;
	public static final int REQ_AUTH_FAILED = 0 ;
	public static final int REQ_AUTH_SUCCESS = 1 ;
	public static final int REQ_ORDER_SUCCESS = 2 ;
	public static final int REQ_MONEY_NOT_ENOUGH = 3 ;
	public static final int REQ_AUTH_MSG_EXCEPTION = 4 ;
	public static final int REQ_ORDER_CHARGE_ERROR = 5 ;
	
	public static final int USERSERVICE = 6;
	public static final int ORDERSERVICE = 7;
	public static final int APKINFOSERVICE = 8;
	
	/**
	 * 鉴权类型，0：视频，1:产品
	 */
	public static final int AUTHTYPE_VIDEO = 0 ; 
	public static final int AUTHTYPE_PRODUCT = 1 ;
	
	public static final int SERVICETYPE_ONLINE = 0 ; 
	public static final int SERVICETYPE_DOWN = 1 ;
	public static final int SERVICETYPE_PLAY = 2 ; 
	
	public static final int BIZTYPE_ALL = 0 ; 
	public static final int BIZTYPE_VOD = 1 ; 
	public static final int BIZTYPE_APP = 2 ;
	
	public static final int ACTION_AUTH = 0 ;
	public static final int ACTION_ORDER = 1 ;
	public static final int ACTION_CHARGE = 2 ;
	public static final int ACTION_ORDER_CONFIRM = 3 ;
	public static final int ACTION_CHARGE_ = 2 ;
	protected static final String TAG = "com.bestv.ott.appstore.pay.PayMng";
	
	
	private Handler mHandler ;
	
	private List<UserOrder> mUserOrders ;
	
	private BesTVServicesMgr mBesTVServicesMgr ;  
	private UserService mUserService=null;
	private OrderService mOrderService=null;
	private ApkInfoService mApkInfoService = null ;
	
	
	private Context mContext ;
	private boolean isConnected = false ;
	private boolean done = false ;
	
	public boolean isDone() {
		return done;
	}

	private Object  mResult;
	
	public Object getResult() {
		return mResult;
	}

	public PayMng(Context context){
		this.mContext = context ;
		if(mBesTVServicesMgr==null){
            AppLog.d(TAG, "-------mBesTVServicesMgr = BesTVServicesMgr.getInstance(mContext);--------");
            mBesTVServicesMgr = BesTVServicesMgr.getInstance(mContext);
        }
		mUserOrders = new ArrayList<UserOrder>();
	} 
	
	private synchronized boolean createConnection(int key , final CallbackActionListener callback){
		isConnected = false ;
		done = false ;
		
		/* check mUserOrder Service */
		if(key == USERSERVICE){
		    AppLog.d(TAG, "----USERSERVICE-----");
		    if(mUserService!=null){
		        AppLog.d(TAG, "-----mUserService!=null-----setCallBack-----");
		        setCallBack(callback);
		    }else{
		        return connectMgr(callback);
		    }
		}
		/* check mOrderService */
		else if(key == ORDERSERVICE){
		    AppLog.d(TAG, "----ORDERSERVICE-----");
		    if(mOrderService!=null){
		        AppLog.d(TAG, "-----mOrderService!=null-----setCallBack-----");
		        setCallBack(callback);
		    }else{
		        boolean ret = connectMgr(callback);
		        if(!ret){
		            try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
		            ret = connectMgr(callback);
		        }else{
		            return ret;
		        }
		    }
		}
		
		/* check apkInfoService */
		else if(key == APKINFOSERVICE){
		    AppLog.d(TAG, "----APKINFOSERVICE-----");
		    if(mApkInfoService!=null){
		        AppLog.d(TAG, "-----mApkInfoService!=null-----setCallBack-----");
		        setCallBack(callback);
		    }else{
		        return connectMgr(callback);
		    }
		}
		
		return false;
	}

    private synchronized void setCallBack(final CallbackActionListener callback) {
        if(callback!=null){
            callback.execute();
            done = true ;
        }
    }

	/* 只有服务没有时才连接 */
    private boolean connectMgr(final CallbackActionListener callback) {
        if(mBesTVServicesMgr==null){
            AppLog.d(TAG, "-------mBesTVServicesMgr = BesTVServicesMgr.getInstance(mContext);--------");
            mBesTVServicesMgr = BesTVServicesMgr.getInstance(mContext);
        }
        boolean b =  mBesTVServicesMgr.connect(new IBesTVServicesConnListener(){
			public void onBesTVServicesConnected(BesTVServicesMgr client){
			    AppLog.d(TAG, "-------  BesTVServicesMgr  ---->  onBesTVServicesConnected--------");
				new Thread(new Runnable() {
					public void run() {
						isConnected = true ;
						Log.d(TAG,"createConnection start .$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ isConnected:" + isConnected );
						mUserService =   mBesTVServicesMgr.getUserService();
						mOrderService =  mBesTVServicesMgr.getOrderService();
						mApkInfoService = mBesTVServicesMgr.getApkService();
						AppLog.d(TAG, "-------  BesTVServicesMgr  ---->  setCallBack(callback)--------");
						setCallBack(callback);
					}
				}).start(); 
			}
		});
		Log.d(TAG," createConnection >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" +  b  );
		return b ;
    }
	

	public BesTVServicesMgr getBesTVServicesMgr() {
		return mBesTVServicesMgr;
	}

	public UserService getmUserService() {
		return mUserService;
	}

	public OrderService getmOrderService() {
		return mOrderService;
	}

	public ApkInfoService getApkInfoService() {
	    return mApkInfoService;
	}

	/**
	 * 仅且仅在系统启动时候载入用户所有业务订单记录
	 * @param besTVServicesMgr
	 */
	public synchronized void loadAllUserOrders(final CallbackActionListener callback, Context context){
		mContext = context ; 
		
		this.createConnection(USERSERVICE,new CallbackActionListener() {
			@Override
			public void execute() { 
				OrderParam op = new OrderParam();
				op.setOrderType(0);
				op.setBizType(BIZTYPE_APP);
				op.setIncludeExpireOrder(0);//不包含失败订购记录
				mUserOrders = mOrderService.getOrdersByParam(op); 
				if(callback != null){
					callback.execute();
				}
			}
		}); 
	}
	
	/**
	 * 获取所有用户的业务订单信息
	 * @param besTVServicesMgr
	 * @return
	 */
	public synchronized List<UserOrder> getUserOrders(){
		if(mUserOrders == null){
			mUserOrders =  new ArrayList<UserOrder>();
		}
		return mUserOrders ;
	}
	
	/**
	 * 业务鉴权接口
	 * @param productCode
	 */
	public synchronized boolean authOrder(final CallbackActionListener callback, final SubsParam subsParam){  
		Log.d(TAG,"PayMng auth .$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$auth $ 0000" +";code="+subsParam.getProductCode()+";serverCode="+subsParam.getServiceCodes() );
		mResult = null ;
		return createConnection(ORDERSERVICE,new CallbackActionListener() {
			@Override
			public void execute() { 
				_doAuth(subsParam); 
				if(callback != null ){
					callback.execute();
				}
			}
		});  
	} 	 
	
	private void _doAuth( SubsParam subsParam){
	    AppLog.d(TAG, "=============_doAuth = "+subsParam.getBizType());
		mResult = mOrderService.auth(subsParam) ;
		ResultParam mResultParam = (ResultParam)mResult;
		Log.d(TAG,mResultParam.getReturnCode() + ">>>>>>" + mResultParam.getReturnDec() + ">>\r\n>>" + mResultParam.getOrderProduct());
		
		if(mResultParam.getReturnCode() == PayMng.REQ_AUTH_FAILED){
			Intent intent = new Intent(Constants.INTENT_ACTION_ORDER_AUTH_FAILED);
			intent.putExtra("RETURN_CODE", mResultParam.getReturnCode());
			intent.putExtra("RETURN_DESC", mResultParam.getReturnDec()); 
			
			mContext.sendBroadcast(intent);
		}else if(mResultParam.getReturnCode() == PayMng.REQ_AUTH_FAILED){
			Intent intent = new Intent(Constants.INTENT_ACTION_ORDER_AUTH_OK);
			intent.putExtra("RETURN_CODE", mResultParam.getReturnCode());
			intent.putExtra("RETURN_DESC", mResultParam.getReturnDec()); 
			
			mContext.sendBroadcast(intent);
		}
		 
	}
	
	/* orderService */
	public synchronized boolean order(final CallbackActionListener callback , final SubsParam subsParam){  
		Log.d(TAG,"start auth ...........................") ;
		mResult = null ;
		return this.createConnection(ORDERSERVICE,new CallbackActionListener() {
			@Override
			public void execute() {
				Log.d(TAG,"start auth ...........................000000000000000 mOrderService:"+ mOrderService) ;
				mResult  = mOrderService.order(subsParam);
				ResultParam mResultParam = (ResultParam)mResult ;
				Log.d(TAG,"start auth ...........................11111111111111 mResult:" + mResultParam.getReturnCode()) ;
				if(mResultParam.getReturnCode()==REQ_ORDER_SUCCESS){
					OrderParam op = new OrderParam();
					op.setOrderType(0);
					op.setBizType(BIZTYPE_APP);
					op.setIncludeExpireOrder(0);//不包含失败订购记录
					mUserOrders = mOrderService.getOrdersByParam(op);
					OrdersCaCheManager.getInstance().addOrders(mUserOrders);
					AppLog.d(TAG, "----------OrdersCaCheManager.getInstance().addOrders(mUserOrders);----------");
				}
				if(callback != null ){
					callback.execute();
				}
			}
		});   
	}
	
	/* 获取apk服务 */
	public synchronized void apk(final CallbackActionListener callback){
		mResult = null ;
		this.createConnection(APKINFOSERVICE,new CallbackActionListener() {
			@Override
			public void execute() {
				Log.d(TAG,"apk exec >>>>>>>>>>>>>>>>>>>>>>");
				mResult = true ;
				if(callback != null ){
					callback.execute();
				}
				Log.d(TAG,"apk run over >>>>>>>>>>>>>>>>>>>>>>" + mResult );
			}
		}); 
	}
	
	/* 获取用户信息 */
	public synchronized void authUser(final CallbackActionListener callback){
	    AppLog.d(TAG, "==========get authUser form BaseActivity=========");
	    this.createConnection(USERSERVICE, new CallbackActionListener() {
            public void execute() {
                AppLog.d(TAG, "==========get authUser go to execute() =========");
                try {   
                    SharedPreferences sp = mContext.getSharedPreferences("user_information", Context.MODE_PRIVATE);
                    if (mUserService!=null) {
                        String groupId = mUserService.getUserGroup2();
                        String userId = mUserService.getUserID();
                        AppLog.d(TAG, "---------------------------------------------------------------------------");
                        AppLog.d(TAG, "=======mUserService.getUserGroup2() = "+groupId);
                        Editor editor = sp.edit();
                        
                        if(userId!=null){
                            editor.putString("userID",userId).commit();
                        }
                        if(groupId!=null){
                            editor.putString("groupID",  groupId).commit();
                        }
                    }else{
                        AppLog.d(TAG, "==========mUserService == null =========");
                    }
                    mResult = true ;
                    if(callback != null ){
                        callback.execute();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
	}
	
	public synchronized void authUserID(final CallbackActionListener callback){
	    
	}
	
	/**
	 * 充值
	 * @param packageName 应用包名
	 * @param onCallBackListener 充值完成侦听器
	 * @param orderAmt 金额，单位为分
	 * @param context 上下文
	 * @return
	 */
	public boolean rechargePay(String packageName, IBesTVWalletServiceCallBackListener onCallBackListener, String orderAmt, Context context){
		return new com.bestv.ott.epay.pay.BestvWalletPay().rechargePay(packageName, onCallBackListener, orderAmt, context);
	}

	/**
	 * 释放资源，安全+mem
	 * //TODO when
	 */
	public void destroy(){
		if (null != mBesTVServicesMgr){
		    mBesTVServicesMgr.disconnect();
		}
		mUserService = null;
        mOrderService = null;
        mApkInfoService = null;
        mBesTVServicesMgr = null;
		mUserOrders.clear();
	}
	
	public static interface CallbackActionListener {
		public void execute();
	}
}


