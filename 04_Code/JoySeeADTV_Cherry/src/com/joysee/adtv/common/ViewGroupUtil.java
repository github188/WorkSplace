package com.joysee.adtv.common;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
/**
 * 管理播放界面的各种视图相互之间的显示和隐藏关系
 * @author dr
 *
 */
public class ViewGroupUtil {
	
    public static final int KEY_VOLUMEBAR = 1;
    public static final int KEY_MUTE = 2;
    public static final int KEY_MENU = 4;
    public static final int KEY_CHNUMBER = 8;
    public static final int KEY_CHINFO = 16;
    public static final int KEY_SUBMENU = 32;
    public static final int KEY_VOLUME_AUDIOINDEX_NOTIFY = 64;
    
    public static final int SHOW_TIME_VOLUMEBAR = 3000;
    public static final int SHOW_TIME_MUTE = -1;
    public static final int SHOW_TIME_MENU = 10000;
    public static final int SHOW_TIME_CHNUMBER = 5000;
    public static final int SHOW_TIME_CHINFO = 5000;
    public static final int SHOW_VOLUME_AUDIOINDEX_NOTIFY = 5000;
    public static final int SHOW_TIME_SUBMENU = 0;
    
    public static final int FLAG_NONE = -1;
    public static final int FLAG_MAINMENU = 0;
    public static final int FLAG_CHANNELMENU = 1;
    public static final int FLAG_FCHANNELMENU = 2;
    public static final int FLAG_VOLUMESETTING = 3;

    private int mShowingViews = 0;

    private class ViewInfo{ 
        public int viewKey;
        public Object view;
        public List<Integer> showWith ;
        public int hideTime;
    }
    
    public interface OnItemNeedShowListener{
        public void onItemNeedShow(int itemId, Object viewItem, int flag);
    }
    private OnItemNeedShowListener itemNeedShowListener;

    /**
     * 设置View需要显示时的监听
     * @param listener
     */
    public void setOnItemNeedShowListener(OnItemNeedShowListener listener){
        itemNeedShowListener = listener;
    }

    public interface OnItemNeedHideListener{
        public void onItemNeedHide(int itemId, Object viewItem, int flag);
    }

    private OnItemNeedHideListener itemNeedHideListener;

    /**
     * 设置View需要隐藏时的监听
     * @param listener
     */
    public void setOnItemNeedHideListener(OnItemNeedHideListener listener){
        itemNeedHideListener = listener;
    }

    private Map<Integer ,ViewInfo> views = new TreeMap<Integer ,ViewInfo>(); 

    private static final int HIDE_VIEW = 0x56;

    /**
     * 添加View，这些View按照下列规则加入后就可以使用这个工具来控制
     * @param key View在Activity中的编号
     * @param item View对象的引用
     * @param hideTime 可持续显示的时间 单位：毫秒
     * @param showWith 可以和其他的View一起显示，这些View要放入这个List中
     */
    public void addItem(int key, Object item, int hideTime, List<Integer> showWith){
        ViewInfo info = new ViewInfo();
        info.viewKey = key;
        info.view = item;
        info.hideTime = hideTime;
        if(showWith != null)
            info.showWith = new ArrayList<Integer>(showWith);
        views.put(key, info);
    }
    
    public void showView(int key){
    	showView(key, FLAG_NONE);
    }

    /**
     * 显示指定的View
     * @param key 在Activity中给View编辑的ID
     */
    public void showView(int key, int flag){
        if(views.containsKey(key)){
            ViewInfo viewInfo = views.get(key);
            
            myHandler.removeMessages(HIDE_VIEW,viewInfo);
			if (viewInfo.hideTime >= 0) {
				Message msg = new Message();
				msg.what = HIDE_VIEW;
				msg.obj = viewInfo;
				msg.arg1 = viewInfo.viewKey;
				msg.arg2 = flag;
				myHandler.sendMessageDelayed(msg, viewInfo.hideTime);
			}
            
            Iterator<ViewInfo> iterator = views.values().iterator();
            
            while(iterator.hasNext())
            {
                ViewInfo temp = iterator.next();
				if (temp == viewInfo) {
					if (itemNeedShowListener != null) {
						mShowingViews = mShowingViews | key;
						itemNeedShowListener.onItemNeedShow(temp.viewKey, temp.view, flag);
					}
				}
                else{
					if (viewInfo.showWith != null) {
						if (viewInfo.showWith.contains(temp.viewKey)) {
							continue;
						}
						if (itemNeedHideListener != null) {
							mShowingViews = mShowingViews & ~temp.viewKey;
							itemNeedHideListener.onItemNeedHide(temp.viewKey, temp.view, -1);
						}
					}
                        
                }
            }
        }
    }
    
	public void hideView(int key, int flag) {
		if (views.containsKey(key)) {
			ViewInfo viewInfo = views.get(key);
			if (itemNeedHideListener != null)
				mShowingViews = mShowingViews & ~key;
				itemNeedHideListener.onItemNeedHide(key, viewInfo.view, flag);
		}
	}

    /**
     * 执行隐藏视图的功能
     */
    private void itemTimeout(){
        if(itemNeedHideListener == null)
            return;
        Iterator<ViewInfo> iterator = views.values().iterator();
        
        while(iterator.hasNext())
        {
            ViewInfo temp = iterator.next();
            
            if(temp.hideTime >= 0){
            	mShowingViews = mShowingViews & ~temp.viewKey;
                itemNeedHideListener.onItemNeedHide(temp.viewKey, temp.view,-1);
            }
        }
    }

    /**
     * 主线程的Handler，用于定时隐藏视图
     */
    public Handler myHandler = new Handler(Looper.getMainLooper()){
        
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
            case HIDE_VIEW:
                hideView(msg.arg1, msg.arg2);
                break;
	        }
            
            super.handleMessage(msg);
        }
        
    };

    public void delayHideView(int key, int flag){
    	if (views.containsKey(key)) {
    		ViewInfo obj = views.get(key);
    		delayHideView(key,flag,obj.hideTime);
    	}
    }
    
    public void delayHideView(int key, int flag,int delayTime){
    	if (views.containsKey(key)) {
    		ViewInfo obj = views.get(key);
	        myHandler.removeMessages(HIDE_VIEW,obj);
	        Message msg = new Message();
	        msg.what = HIDE_VIEW;
	        msg.obj = obj;
	        msg.arg1 = key;
	        msg.arg2 = flag;
	        myHandler.sendMessageDelayed(msg, delayTime);
    	}
    }

    public void hideAllViews(){
    	itemTimeout();
    }
}
