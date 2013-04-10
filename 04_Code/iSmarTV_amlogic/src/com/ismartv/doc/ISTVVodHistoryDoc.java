package com.ismartv.doc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.ismartv.client.ISTVBitmapSignal;
import com.ismartv.client.ISTVClearHistorySignal;
import com.ismartv.client.ISTVClearOneHistorySignal;
import com.ismartv.client.ISTVItemBitmapSignal;
import com.ismartv.client.ISTVClient;
import com.ismartv.client.ISTVHistoryListSignal;
import com.ismartv.client.ISTVItemListSignal;
import com.ismartv.client.ISTVSignal;
import com.ismartv.client.ISTVVodHomeListSignal;
import com.ismartv.service.ISTVContentModel;
import com.ismartv.service.ISTVItem;
import com.ismartv.ui.ISTVVodConstant;
import com.ismartv.ui.ISTVVodHistory;

import android.graphics.Bitmap;
import android.util.Log;

public class ISTVVodHistoryDoc extends ISTVDoc {

    private static final String TAG = "ISTVVodHistoryDoc";   
   
    private int historyCount = 0;
    private int historyRowCount = 0;
    private int todayCount=0, yesterdayCount=0, earlyCount=0;
    private int todayStart=0, yesterdayStart=0, earlyStart=0;
    
    private ArrayList<ISTVItem> sortItems = null;
    
    private List<ISTVSignal> pool = new ArrayList<ISTVSignal>();
    ISTVHistoryListSignal historyList;
    
    private Date today, yesterday;

    public ISTVVodHistoryDoc() {
        // TODO Auto-generated constructor stub
        super();
        onCreate();
    }
    
    public void onCreate() {
        // TODO Auto-generated method stub
        Log.d(TAG,"ISTVVodHistoryDoc onCreate!!!!!");
        super.onCreate();
    }
    
    public void resetInt(){
    	historyCount = 0;
        historyRowCount = 0;
        todayCount=0;yesterdayCount=0;earlyCount=0;
        todayStart=0;yesterdayStart=0;earlyStart=0;
    }
    
    public void removeHistory(int pk){
    	historyList.removeHistory(pk);
    	getClient().getEpg().storeHistory();
    }
    
    public void fetchHistoryData() {
         historyList = new ISTVHistoryListSignal(getClient()){          
            @Override
            public void onSignal() {
                // TODO Auto-generated method stub
            	resetInt();
                Collection<ISTVItem> items = getHistoryList();
                historyCount = Math.min(ISTVVodConstant.MAX_HISTORY_ITEM, items.size());
                for(ISTVItem item:items){
                    Log.d(TAG, "----------onSignal--itemPK="+item.itemPK+";pk="+item.pk);
                }
                Log.d(TAG, "size = "+historyCount);
                if(historyCount!=0){  
                    if(sortItems == null){
                    	sortItems = new ArrayList<ISTVItem>();
                    }
                    else{
                    	sortItems.clear();
                    }
                    fetchHistorySections(items);
                }
                
                onGotResource(new ISTVResource(ISTVVodHistory.RES_INT_ITEMCOUNT, 0, historyCount));
                dispose();
            }            
        };
    }
    
    public void getRecommentItem() {
        ISTVVodHomeListSignal homeVod = new ISTVVodHomeListSignal(getClient()){        
            @Override
            public void onSignal() {
                // TODO Auto-generated method stub
                Collection<ISTVItem> recommends = getItemList();
                int count = Math.min(ISTVVodConstant.RECOMMENT_ITEM_COUNT, recommends.size());
                
                int idx = 0;
                for(ISTVItem item: recommends){
                    if(idx>=count)
                        break;
                    
                    onGotResource(new ISTVResource(ISTVVodHistory.RES_STR_ITEM_TITLE, idx, item.title));
                    onGotResource(new ISTVResource(ISTVVodHistory.RES_INT_ITEM_PK, idx, item.pk));
                    
                    if(item!=null){
                    	ISTVItemBitmapSignal itemBmp = new ISTVItemBitmapSignal(getClient(), idx, item){
                            public void onSignal(){
                                Bitmap bmp = getBitmap();

                                onGotResource(new ISTVResource(ISTVVodHistory.RES_BMP_ITEM_POSTER, getID(), bmp));
                                dispose();
                            }
                        };
                    }
                    idx++;
                }
                dispose();
                
            }            
        };
    }
    
    public int getHistoryCount(){
        return historyCount;
    }
    
    public int getHistoryRowCount(){
        return historyRowCount;
    }
    
    
    public void clearAllHistory(){
		if(sortItems == null)
			return;
        sortItems.clear();
        historyCount = 0;
        getClient().getEpg().clearHistory();//清内存
        getClient().getEpg().storeHistory();//刷新本地
        onGotResource(new ISTVResource(ISTVVodHistory.RES_INT_ITEMCOUNT, 0, historyCount));
        //清网络
        ISTVClearHistorySignal clearSignal = new ISTVClearHistorySignal(getClient()) {
			public void onSignal() {
				Log.d(TAG, "---clearSignal---");
			}
		};
    }
    public void clearOneHistory(int one,int pk){
    	if(sortItems == null && pk == -1){
    		return;
    	}
    	int index = -1;
    	for(int i=0;i<sortItems.size();i++){
    		if(pk == sortItems.get(i).pk){
    			index = i;
    			break;
    		}
    	}
    	if(index<0){
    	    Log.d(TAG, "---clearSignal---index="+index+";sortItems.length="+sortItems.size());
    	    return;
    	}
    	sortItems.remove(index);
    	historyCount= historyCount - 1;
    	getClient().getEpg().removeHistoryPK(pk);//清内存
    	getClient().getEpg().storeHistory();//刷新本地
    	onGotResource(new ISTVResource(ISTVVodHistory.RES_INT_ITEMCOUNT, 0, historyCount));
    	//清网络
        ISTVClearOneHistorySignal clearSignal = new ISTVClearOneHistorySignal(getClient(),pk) {
			public void onSignal() {
				
			}
		};
    }
    
    public void getHistorySections(){
        int id = 0;
        if(todayCount>0){
            onGotResource(new ISTVResource(ISTVVodHistory.RES_STR_SECTION_TITLE, id, "今天"));
            onGotResource(new ISTVResource(ISTVVodHistory.RES_INT_SECTION_ITEMCOUNT, id, todayCount));
            onGotResource(new ISTVResource(ISTVVodHistory.RES_INT_SECTION_START, id, todayStart));
            id++;
        }
        if(yesterdayCount>0){
            onGotResource(new ISTVResource(ISTVVodHistory.RES_STR_SECTION_TITLE, id, "昨天"));
            onGotResource(new ISTVResource(ISTVVodHistory.RES_INT_SECTION_ITEMCOUNT, id, yesterdayCount));
            onGotResource(new ISTVResource(ISTVVodHistory.RES_INT_SECTION_START, id, yesterdayStart));
            id++;
        }
        if(earlyCount>0){
            onGotResource(new ISTVResource(ISTVVodHistory.RES_STR_SECTION_TITLE, id, "更早"));
            onGotResource(new ISTVResource(ISTVVodHistory.RES_INT_SECTION_ITEMCOUNT, id, earlyCount));
            onGotResource(new ISTVResource(ISTVVodHistory.RES_INT_SECTION_START, id, earlyStart));
            id++;
        }
    }
    
    private void fetchHistorySections(Collection<ISTVItem> items){
        getDateLimit();
        
        for(ISTVItem item : items){
            if(historyCount <= 0 || item.updateDate == null){
            	break;
            }
            sortItems.add(item);
            if(!item.updateDate.before(today)){
            	todayCount++;
            }
            else if(!item.updateDate.before(yesterday)){
            	yesterdayCount++;
            }
            else{
            	earlyCount++;
            }
        }
        
        Log.d(TAG, "todayCount = "+todayCount);
        Log.d(TAG, "yesterdayCount = "+yesterdayCount);
        Log.d(TAG, "earlyCount = "+earlyCount);
        
        int start = 0;
        int id = 0;
        if(todayCount>0){
            int col = todayCount/ISTVVodConstant.ITEM_PER_ROW;
            if(todayCount%ISTVVodConstant.ITEM_PER_ROW!=0){
            	col++;
            }
            todayStart=start;
            start += col;
            id++;
        }        
        if(yesterdayCount>0){
            int col = yesterdayCount/ISTVVodConstant.ITEM_PER_ROW;
            if(yesterdayCount%ISTVVodConstant.ITEM_PER_ROW!=0)
                col++;
            yesterdayStart=start;
            start += col;
            id++;
        }
        if(earlyCount>0){
            int col = earlyCount/ISTVVodConstant.ITEM_PER_ROW;
            if(earlyCount%ISTVVodConstant.ITEM_PER_ROW!=0)
                col++;
            earlyStart=start;
            start += col;
            id++;
        }
        historyRowCount = start;
        Log.d(TAG, "historyRowCount = "+historyRowCount);
        onGotResource(new ISTVResource(ISTVVodHistory.RES_INT_SECTION_COUNT, 0, id));
    }
    
    
    @SuppressWarnings("unchecked")
	public void getHistoryItem(){      
    	int itemID = 0;
        ItemComparator compare = new ItemComparator();
        Collections.sort(sortItems,compare);
        for(ISTVItem item : sortItems){
        	Log.d(TAG, "排序后 : "+item.title +"  |"+item.updateDate);
            if(itemID >= historyCount){
            	break;
            }
            int posId = caluPositionId(itemID);
            onGotResource(new ISTVResource(ISTVVodHistory.RES_STR_ITEM_TITLE, posId, item.title));
            onGotResource(new ISTVResource(ISTVVodHistory.RES_INT_ITEM_PK, posId, item.itemPK));  
            if(item.itemPK!=item.pk&&item.pk>0){
                onGotResource(new ISTVResource(ISTVVodHistory.RES_INT_PK, posId, item.pk)); 
            }
            Log.d(TAG, "itempk="+item.itemPK+";Pk="+item.pk);
                       
            ISTVContentModel cm = getClient().getEpg().getContentModel(item.contentModel);
            if(cm!=null)
                onGotResource(new ISTVResource(ISTVVodHistory.RES_STR_ITEM_MODEL, posId, cm.getTitle("zh_CN")));
            else
                Log.d(TAG, "cm is null!! model name:"+item.contentModel);
            
            if(item.posterURL!=null){
                ISTVItemBitmapSignal itemBmp = new ISTVItemBitmapSignal(getClient(), posId, item){
                    public void onSignal(){
                        Bitmap bmp = getBitmap();

                        onGotResource(new ISTVResource(ISTVVodHistory.RES_BMP_ITEM_POSTER, getID(), bmp));
                        dispose();
                    }
                };
            }
            
            itemID++;
       //     onGotResource(new ISTVResource(ISTVVodHistory.RES_BMP_ITEM_POSTER, idx, item.));
        }               
    }
    
    private void getDateLimit(){
        Date now = new Date();
        
        today = new Date(now.getTime());
        today.setHours(0);
        today.setMinutes(0);
        today.setSeconds(0);
        
        yesterday = new Date(now.getTime() - 24*3600*1000L);
        yesterday.setHours(0);
        yesterday.setMinutes(0);
        yesterday.setSeconds(0);
        
        Log.d(TAG, "today = "+today+", yesterday = "+yesterday);
    }
    
    private int caluPositionId(int itemId){
        if(itemId < todayCount){
            return itemId;
        }
        else if(itemId < todayCount+yesterdayCount){
            return itemId - todayCount + yesterdayStart*ISTVVodConstant.ITEM_PER_ROW;
        }
        else {
            return itemId - todayCount - yesterdayCount + earlyStart*ISTVVodConstant.ITEM_PER_ROW;
        }      
    }
    
    
    class ItemComparator implements Comparator {
        public int compare(Object arg0, Object arg1) {
            ISTVItem item0 = (ISTVItem) arg0;
            ISTVItem item1 = (ISTVItem) arg1;
            return item1.updateDate.compareTo(item0.updateDate);
        }
    }
        
    
    
}
