package com.ismartv.doc;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ismartv.client.ISTVBitmapSignal;
import com.ismartv.client.ISTVItemBitmapSignal;
import com.ismartv.client.ISTVItemListSignal;
import com.ismartv.client.ISTVSectionListSignal;
import com.ismartv.service.ISTVItem;
import com.ismartv.service.ISTVSection;
import com.ismartv.ui.ISTVVodConstant;
import com.ismartv.ui.ISTVVodItemList;

import android.util.Log;

public class ISTVVodItemListDoc extends ISTVDoc {

    private static final String TAG = "ISTVVodItemListDoc";
    
    private String channelId = null;
    
    private ISTVSectionListSignal sectionList = null;
    private List<ISTVSection> sections = null;    
    
    private List<ISTVItem> dataList = null;
    private Map<Integer, String> secTags = null;
    private int startSecId=-1, startPage=-1, endSecId=-1, endPage=-1;
    
    private int mSecCount; 
    private int[] mSecStartRow;
    private int[] mSecEndRow;
    private int mTotalCount,mTotalRowCount; 
    
    private ISTVItemBitmapSignal[] signals;
    
    private boolean isLoadingData = false;
    private int loadingIdx = 0;
    
    public ISTVVodItemListDoc(String channel)
    {
        super();
        channelId = channel;
        Log.d(TAG,"on create: chann="+channel);       
        onCreate();
    }
    
    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        Log.d(TAG,"ISTVVodItemListDoc onCreate!!!!!");
        super.onCreate();

        sectionList = new ISTVSectionListSignal(getClient(),channelId){
            
            public void onSignal(){

                if(getSectionList()==null)
                    return;
                sections = new ArrayList<ISTVSection>();
                sections.addAll(getSectionList());

                mSecCount = sections.size();
                mSecStartRow = new int[mSecCount];
                mSecEndRow = new int[mSecCount];

                onGotResource(new ISTVResource(ISTVVodItemList.RES_INT_SECTION_COUNT, 0, mSecCount));

                int id=0;
                mTotalCount = 0;
                mTotalRowCount = 0;
                for(ISTVSection sec : sections){                    
                    mSecStartRow[id] = mTotalRowCount;
                    int itemCount = sec.count;
                    mTotalCount += itemCount;
                    if(itemCount%ISTVVodConstant.ITEM_PER_ROW == 0)
                        mTotalRowCount += itemCount / ISTVVodConstant.ITEM_PER_ROW;
                    else
                        mTotalRowCount += itemCount / ISTVVodConstant.ITEM_PER_ROW + 1;
                    mSecEndRow[id] = mTotalRowCount-1;  
                    Log.d(TAG, "sec: id="+id+", title="+sec.title+", count="+itemCount+", start="+mSecStartRow[id]+", end="+mSecEndRow[id]);
                    id++;
                }
                onGotResource(new ISTVResource(ISTVVodItemList.RES_INT_TOTAL_ROW_COUNT, -1, mTotalRowCount));
                Log.d(TAG,"total row count ="+mTotalRowCount);              
                dispose();
            }
        };        
    }
     
    public int getTotalRowCount() {
        return mTotalRowCount;
    }

    @Override
    public void dispose() {
        if(sections!=null)
            sections.clear();
        if(dataList!=null)
            dataList.clear();
        if(secTags!=null)
            secTags.clear();
        super.dispose();        
    }

    public void getSections(){
        int id=0;
        for(ISTVSection sec : sections){
            onGotResource(new ISTVResource(ISTVVodItemList.RES_STR_SECTION_SLUG, id, sec.slug));
            onGotResource(new ISTVResource(ISTVVodItemList.RES_STR_SECTION_TITLE, id, sec.title));
            onGotResource(new ISTVResource(ISTVVodItemList.RES_INT_SECTION_ITEMCOUNT, id, sec.count));
            onGotResource(new ISTVResource(ISTVVodItemList.RES_INT_SECTION_START, id, mSecStartRow[id]));
            id++;
        }
    }
    
    public String currentSlug="";
    
    private void getItemBySec(int secId, int start, int count, boolean isInsert,boolean isMenu){
        isLoadingData = true;
        final boolean bIns = isInsert;
        final boolean isM=isMenu;
        final ISTVSection section = sections.get(secId);
        final int startIdx = start;
        final int secIdTemp=secId;
        Log.d(TAG, "request "+section.slug+" from "+start);
        currentSlug=section.slug;
        ISTVItemListSignal listSignal = new ISTVItemListSignal(getClient(), loadingIdx, section.slug, start, count){ 
            public void onSignal() {
                // TODO Auto-generated method stub
                //Log.d("#############OnSignal", "");
                Log.d(TAG, "-----------onSignal-----------section="+getSection()+";currentSlug="+currentSlug);
                if(!currentSlug.equals(getSection())){
                    Log.d(TAG, "-----------onSignal------return-----");
                    return;
                }
                Collection<ISTVItem> items = getItemList();
                if(items == null)
                    return;    
                if(dataList==null)
                    dataList = new ArrayList<ISTVItem>();
                List<ISTVItem> tempList = new ArrayList<ISTVItem>();
                for(ISTVItem item:items){
                    if(item == null)
                        break;
                    tempList.add(item);
                }
                if(tempList.size()%ISTVVodConstant.ITEM_PER_ROW!=0){
                    int nullItem = ISTVVodConstant.ITEM_PER_ROW-tempList.size()%ISTVVodConstant.ITEM_PER_ROW;
                    for(int i=0; i<nullItem; i++)
                        tempList.add(null);
                }                
                
                if(secTags==null)
                    secTags = new HashMap<Integer, String>();
                if(bIns){
                    Set<Integer> keySet = new HashSet<Integer>(secTags.keySet());
                    for(Integer key: keySet){
                        String title = secTags.get(key);
                        if(title!=null){
                            Integer newKey = key + tempList.size();
                            secTags.remove(key);
                            secTags.put(newKey, title);
                        }
                    }
                    keySet.clear();
                    keySet = null;
                    
                    if(startIdx == 0)
                        secTags.put(0, section.title);
                    dataList.addAll(0, tempList);
                    onGotResource(new ISTVResource(ISTVVodItemList.RES_INT_INSDATA, getID(), tempList.size()));
                }
                else{
                	if(isM){
                		dataList.clear();
                	}
                    if(startIdx == 0)
                    	secTags.put(dataList.size(), section.title);
                    dataList.addAll(tempList);
                    if(endSecId==secIdTemp){
                    	onGotResource(new ISTVResource(ISTVVodItemList.RES_BOOL_IS_LOADING, getID(), false));
                    }
                }
                
                tempList.clear();
                tempList = null;
                    
                Log.d(TAG, "new data size: "+dataList.size());                
                isLoadingData = false;
                dispose();
            }                   
        }; 
        onGotResource(new ISTVResource(ISTVVodItemList.RES_BOOL_IS_LOADING, loadingIdx++, true));
        clearSignal();
    }
    
    public boolean fetchNextPageItems(){
        Log.d(TAG, "fetchNextPageItems begin: sec="+ endSecId +", page="+endPage);
        if(endSecId==-1){
            endSecId = 0;
            endPage = 0;
        }
        else {
            ISTVSection curSec = sections.get(endSecId);
            int pageCount = curSec.count/ISTVVodConstant.ITEM_PER_PAGE;
            if(curSec.count%ISTVVodConstant.ITEM_PER_PAGE != 0)
                pageCount++;
            
            if(endPage+1 >= pageCount){
                if(endSecId+1>=sections.size() || endSecId+1<0)
                    return false;
                endSecId++;
                endPage = 0;
            }
            else
                endPage ++;
        }       
            
        getItemBySec(endSecId, ISTVVodConstant.ITEM_PER_PAGE*endPage, ISTVVodConstant.ITEM_PER_PAGE, false,false);
        Log.d(TAG, "fetchNextPageItems end: sec="+ endSecId +", page="+endPage);
        return true;
    }
    
    public boolean fetchPrevPageItems(){
        Log.d(TAG, "fetchPrevPageItems begin: sec="+ startSecId +", page="+startPage);
        if(startSecId==-1){
            startSecId = 0;
            startPage = 0;
            return false;
        }
        else {
            if(startPage == 0){               
                if(startSecId-1>=sections.size() || startSecId-1<0)
                    return false;
                
                startSecId --;
                ISTVSection curSec = sections.get(startSecId);
                int pageCount = curSec.count/ISTVVodConstant.ITEM_PER_PAGE;
                if(curSec.count%ISTVVodConstant.ITEM_PER_PAGE != 0)
                    pageCount++;
                
                startPage = pageCount-1;
            }
            else
                startPage --;   
        }
                    
        getItemBySec(startSecId, ISTVVodConstant.ITEM_PER_PAGE*startPage, ISTVVodConstant.ITEM_PER_PAGE, true,false);
        Log.d(TAG, "fetchPrevPageItems end: sec="+ startSecId +", page="+startPage);
        return true;
    }
    
    public void fetchPageBySec(int secId){
        if(secId>=sections.size() || secId<0)
            return;
        
        if(dataList!=null)
            dataList.clear();
        if(secTags!=null)
            secTags.clear();
   
        startSecId = secId;
        startPage = 0;
        endSecId = secId;
        endPage = 0;

        getItemBySec(endSecId, ISTVVodConstant.ITEM_PER_PAGE*endPage, ISTVVodConstant.ITEM_PER_PAGE, false,true);        
    }
    
    public void getItemData(int start, int count){      
        Log.d(TAG, "getItemData: start="+ start +", count="+count);
        if(dataList==null)
            return;
            
        if(start<0){
            if(fetchPrevPageItems())
                return;           
        }
        else if(start + count -1>= dataList.size()){
            if(fetchNextPageItems())
                return;    
        }
            
        for(int pos=0; pos<count; pos++){
            int idx = pos + start;
      
            if(pos%ISTVVodConstant.ITEM_PER_ROW==0){
                int row = pos/ISTVVodConstant.ITEM_PER_ROW;
                if(secTags!=null){
                    if(secTags.containsKey(idx)){                       
                        onGotResource(new ISTVResource(ISTVVodItemList.RES_STR_SEC_TITLE, row, secTags.get(idx)));
                    }
                    else
                        onGotResource(new ISTVResource(ISTVVodItemList.RES_STR_SEC_TITLE, row, ""));
                }
                else
                    onGotResource(new ISTVResource(ISTVVodItemList.RES_STR_SEC_TITLE, row, ""));
            }
            
            if(idx < 0){
                onGotResource(new ISTVResource(ISTVVodItemList.RES_STR_ITEM_TITLE, pos, ""));
                onGotResource(new ISTVResource(ISTVVodItemList.RES_INT_ITEM_PK, pos, -2));
				onGotResource(new ISTVResource(ISTVVodItemList.RES_BOOL_IS_COMPLEX, pos, true));
     //           onGotResource(new ISTVResource(ISTVVodItemList.RES_STR_ITEM_THUMB_URL, pos, ""));
                getPoster(pos,null);
                continue;
            }
            
            if(idx >= dataList.size()){
                onGotResource(new ISTVResource(ISTVVodItemList.RES_STR_ITEM_TITLE, pos, ""));
                onGotResource(new ISTVResource(ISTVVodItemList.RES_INT_ITEM_PK, pos, -2));
				onGotResource(new ISTVResource(ISTVVodItemList.RES_BOOL_IS_COMPLEX, pos, true));
      //          onGotResource(new ISTVResource(ISTVVodItemList.RES_STR_ITEM_THUMB_URL, pos, ""));
                getPoster(pos,null);
                continue;
            }
            
            ISTVItem item = dataList.get(idx);
            if(item==null){
                onGotResource(new ISTVResource(ISTVVodItemList.RES_STR_ITEM_TITLE, pos, ""));
                onGotResource(new ISTVResource(ISTVVodItemList.RES_INT_ITEM_PK, pos, -1));
				onGotResource(new ISTVResource(ISTVVodItemList.RES_BOOL_IS_COMPLEX, pos, true));
      //          onGotResource(new ISTVResource(ISTVVodItemList.RES_STR_ITEM_THUMB_URL, pos, ""));
                getPoster(pos,null);
            } else {
                onGotResource(new ISTVResource(ISTVVodItemList.RES_STR_ITEM_TITLE, pos, item.title));
                onGotResource(new ISTVResource(ISTVVodItemList.RES_INT_ITEM_PK, pos, item.pk));
				onGotResource(new ISTVResource(ISTVVodItemList.RES_BOOL_IS_COMPLEX, pos, item.isComplex));
       //         onGotResource(new ISTVResource(ISTVVodItemList.RES_STR_ITEM_THUMB_URL, pos, item.thumbURL));
                getPoster(pos, item);
            }            
        }
		onGotResource(new ISTVResource(ISTVVodItemList.RES_BOOL_IS_ITEM_TITLE_FINISHED, 0, -1));
    }
    
  /*  public void getPoster(int id, URL url){
        Log.d(TAG, "request poster: id="+id+",url="+url);
        if(url!=null){mm
            if(sigPool==null)
                sigPool = new HashMap<URL, ISTVBitmapSignal>();            
          
            if(sigPool.get(url)!=null)
                sigPool.get(url).setID(id);
            else{
                final URL urlKey = url;
                ISTVBitmapSignal bmpSignal = new ISTVBitmapSignal(getClient(), id, url){
                    public void onSignal(){
                        Log.d(TAG, "got poster: id="+getID()+",url="+getURL());
                        onGotResource(new ISTVResource(ISTVVodItemList.RES_BMP_ITEM_THUMB, getID(), getBitmap()));
                        onUpdate();
                        dispose();
                        sigPool.remove(urlKey);
                    }
                };
                sigPool.put(url, bmpSignal);
            }
        }
    }*/
    
    public void clearSignal(){
        if(signals!=null){
            int size=signals.length;
            for(int i=0;i<size;i++){
                if(signals.length>i&&signals[i] != null){
                    signals[i].dispose();
                    signals[i] = null;
                }
            }
        }
    }
    
    public void getPoster(int id, ISTVItem item){
        Log.d(TAG, "request poster: id=" + id);

        if (signals == null) {
            signals = new ISTVItemBitmapSignal[ISTVVodConstant.ITEM_DISPLAY];
            Arrays.fill(signals, null);
        }

        if (signals[id] != null) {
            signals[id].dispose();
            signals[id] = null;
        }
        
        if (item != null) {
            signals[id] = new ISTVItemBitmapSignal(getClient(), id, item) {
                public void onSignal() {
//                    Log.d(TAG, "got poster: id=" + getID());
                    onGotResource(new ISTVResource(
                        ISTVVodItemList.RES_BMP_ITEM_THUMB, getID(),
                        getBitmap()));
                    onUpdate();
                    dispose();
                }
            };
        }
    }
}
