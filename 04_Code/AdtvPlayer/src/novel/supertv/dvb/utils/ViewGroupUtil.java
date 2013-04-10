package novel.supertv.dvb.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import novel.supertv.dvb.activity.PlayActivity;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
/**
 * 管理播放界面的各种视图相互之间的显示和隐藏关系
 * @author dr
 *
 */
public class ViewGroupUtil {

    private class ViewInfo{ 
        public int viewKey;
        public Object view;
        public List<Integer> showWith ;
        public int hideTime;
    }
    
    public interface OnItemNeedShowListener{
        public void onItemNeedShow(int itemId, Object viewItem);
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
        public void onItemNeedHide(int itemId, Object viewItem);
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

    /**
     * 显示指定的View
     * @param key 在Activity中给View编辑的ID
     */
    public void showView(int key){
        if(views.containsKey(key)){
            ViewInfo viewInfo = views.get(key);
            
            myHandler.removeMessages(HIDE_VIEW);
            if(viewInfo.hideTime >= 0)
                myHandler.sendEmptyMessageDelayed(HIDE_VIEW, viewInfo.hideTime);
            
            Iterator<ViewInfo> iterator = views.values().iterator();
            
            while(iterator.hasNext())
            {
                ViewInfo temp = iterator.next();
                if(temp == viewInfo){
                    if(itemNeedShowListener != null)
                        itemNeedShowListener.onItemNeedShow(
                                temp.viewKey, temp.view);
                }
                else{
                    if(viewInfo.showWith != null){
                        if(viewInfo.showWith.contains(temp.viewKey))
                            continue;
                    }
                        
                    if(itemNeedHideListener != null)
                        itemNeedHideListener.onItemNeedHide(
                                temp.viewKey, temp.view);
                }
            }
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
                itemNeedHideListener.onItemNeedHide(temp.viewKey, temp.view);
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
                itemTimeout();
                break;
            }
            
            super.handleMessage(msg);
        }
        
    };

    public void delayHideView(){
        
        myHandler.removeMessages(HIDE_VIEW);
        myHandler.sendEmptyMessageDelayed(HIDE_VIEW, PlayActivity.ViewShowTime.VIEW_HIDE_TIME_KEY_MENU);
    }

    public void hideAllViews(){
        
        myHandler.removeMessages(HIDE_VIEW);
        myHandler.sendEmptyMessage(HIDE_VIEW);
    }
}
