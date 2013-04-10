package com.bestv.ott.appstore.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.bestv.ott.util.bean.UserOrder;

import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

/**
 * 订单缓存管理
 */
public class OrdersCaCheManager {

	private static final String TAG = "OrdersCaCheManager";
	private long ALLOW_CACHE_SIZE = 50; //allow total cache = 50MB 
	private static OrdersCaCheManager manager;
	public static List<UserOrder> mUserOrders ;
	
	private OrdersCaCheManager(){
		mUserOrders = new ArrayList<UserOrder>();
	}
	
	public static OrdersCaCheManager getInstance() {
		if(manager==null){
			AppLog.e(TAG, "---new OrdersCaCheManagerr()---");
			manager = new OrdersCaCheManager(); 
		}
		return manager; 
	}
	public synchronized void addOrders(List<UserOrder> userOrders){  
		mUserOrders.clear();
		for(UserOrder u : userOrders){
			addOrder(u);
		}
	}
	
	/* clearThread */
	private class clearThread extends Thread {
		public void run() {
			super.run();
//			int size = mUserOrders.size();
			mUserOrders.clear();
		}
	}
 
	public int getSize(){
		return mUserOrders.size();
	}

	/**
	 * 新增订单 
	 */
	public synchronized void addOrder(UserOrder u){   
		UserOrder uo = new UserOrder(); 
		
		uo.setExpireTime(u.getExpireTime() ); //clone
		uo.setProductCode(u.getProductCode());
		uo.setPrice(u.getPrice());
		uo.setProductName(u.getProductName());
		uo.setValidTime(u.getValidTime());
		mUserOrders.add(uo);
	}
	
	/**
	 * 查找订单
	 * @param order
	 */
	public UserOrder findOrder(String productCode){
		if(productCode==null){
			return null ;
		}
		for(UserOrder order:mUserOrders){
			AppLog.d(TAG,"~~~~~~~~~~` find userorder :" + order.getProductCode()   );
			if(productCode.equals(order.getProductCode())){
				return order ;
			}
		}
		return null ;
		
	}
	
	/* 有一种情况，假如一个礼包内应用全被单个购买完，此大礼包鉴权时就会默认为已购买，而不会去支付，所以要手动增加一个大礼包的购买记录 */
	public void addGiftCache(String productCode){
	    UserOrder gift = new UserOrder();
	    if(productCode==null){
	        return;
	    }
	    gift.setProductCode(productCode);
	    if(mUserOrders!=null){
	        mUserOrders.add(gift);
	    }
	}
	
}
