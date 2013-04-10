package com.ismartv.doc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.ismartv.client.ISTVBitmapSignal;
import com.ismartv.client.ISTVItemBitmapSignal;
import com.ismartv.client.ISTVClient;
import com.ismartv.client.ISTVFiltRateSignal;
import com.ismartv.client.ISTVItemDetailSignal;
import com.ismartv.client.ISTVItemListSignal;
import com.ismartv.client.ISTVRelateListSignal;
import com.ismartv.client.ISTVSignal;
import com.ismartv.service.ISTVContentModel;
import com.ismartv.service.ISTVItem;
import com.ismartv.ui.ISTVVodRelateList;

import android.graphics.Bitmap;
import android.util.Log;

public class ISTVVodRelateDoc extends ISTVDoc {
    
    private static final String TAG = "ISTVVodRelateDoc";
    private int mPk;
    private String mContentModel;
    
    private List<String> names = new ArrayList<String>();
    private List<String> queries = new ArrayList<String>();
    
    private List<ISTVSignal> pool;
    
    public ISTVVodRelateDoc(int pk)
    {
        super();
        mPk = pk;
        pool = new ArrayList<ISTVSignal>();
        Log.d(TAG,"on create: pk="+pk);       
        onCreate();
    }
    
    public void onCreate() {
        // TODO Auto-generated method stub
        Log.d(TAG,"ISTVVodRelateDoc onCreate!!!!!");
        super.onCreate();      
        
        ISTVItemDetailSignal itemSignal = new ISTVItemDetailSignal(getClient(), mPk){

            @Override
            public void onSignal() {

                ISTVItem item = getItem();
                mContentModel = item.contentModel;
                ISTVContentModel cm = getClient().getEpg().getContentModel(item.contentModel);
				if(cm == null) {
					dispose();
					return;
				}				
                Log.d(TAG,"person_attributes:"+cm.personAttr);				
                
                queries.add("relate");
                names.add("同类型影片");
                
                if(cm.personAttr != null){              
                    String[] person_attrs = cm.personAttr.split(",");
                    for(String attr_name: person_attrs){
                        if(item.attrs.attrs!=null){
                            for(ISTVItem.Attribute attr : item.attrs.attrs){                        
                                if(attr!=null && attr_name.equals(attr.attr.name)){
                                    if(attr.values!=null){
                                        for(ISTVItem.Value value : attr.values){                                       
                                            names.add(value.name);
                                            String str = attr_name+","+value.id;
                                            queries.add(str);
                                        }
                                    }
                                }
                            }
                        }
                    }                    
                }   
                
                onGotResource(new ISTVResource(ISTVVodRelateList.RES_INT_SECTION_COUNT, 0, names.size()));
                dispose();
            }
        };  
    }
    
    public void getSections(){
        for(int i=0;i<names.size();i++) {
            onGotResource(new ISTVResource(ISTVVodRelateList.RES_STR_SECTION_SLUG, i, queries.get(i)));
            onGotResource(new ISTVResource(ISTVVodRelateList.RES_STR_SECTION_TITLE, i, names.get(i)));
        }
    }
    
    public void getItemBySection(int secId) {
        if(secId < 0 || secId >= queries.size())
            return;
        
        if(!pool.isEmpty()){
            for(ISTVSignal signal:pool)
                signal.dispose();
            pool.clear();
        }
          
        
        String query = queries.get(secId);
        Log.d(TAG,"query="+query);
        if("relate".equals(query)){
            ISTVRelateListSignal signal = new ISTVRelateListSignal(getClient(), mPk){

                @Override
                public void onSignal() {
                    Collection<ISTVItem> items = getItemList();
                    int count = Math.min(items.size(),ISTVVodRelateList.ITEM_PER_PAGE);
                    onGotResource(new ISTVResource(ISTVVodRelateList.RES_INT_ITEM_COUNT, 0, count));
                    
                    int i = 0;
                    for(ISTVItem item:items){
                        if(i >= count)
                            break;
                        
                        onGotResource(new ISTVResource(ISTVVodRelateList.RES_STR_ITEM_TITLE, i, item.title));
                        onGotResource(new ISTVResource(ISTVVodRelateList.RES_INT_ITEM_PK, i, item.pk));
                        
                        /*if(item.posterURL!=null)*/{
                            ISTVItemBitmapSignal itemBmp = new ISTVItemBitmapSignal(getClient(), i, item, ISTVItemBitmapSignal.THUMB){
                                public void onSignal(){
                                    Bitmap bmp = getBitmap();

                                    onGotResource(new ISTVResource(ISTVVodRelateList.RES_BMP_ITEM_THUMB, getID(), bmp));
                                    pool.remove(this);
                                    dispose();
                                }
                            };
                            pool.add(itemBmp);
                        }
                        
                        i++;
                    } 
                    
                    pool.remove(this);
                }                
            };
            pool.add(signal);
        }
        else {
            String str[] = query.split(",");
            ISTVFiltRateSignal signal = new ISTVFiltRateSignal(getClient(), mContentModel, str[0], Integer.parseInt(str[1])){

                @Override
                public void onSignal() {
                    Collection<ISTVItem> items = getItemList();
                    int count = Math.min(items.size(),ISTVVodRelateList.ITEM_PER_PAGE);
                    onGotResource(new ISTVResource(ISTVVodRelateList.RES_INT_ITEM_COUNT, 0, count));
                    
                    int i = 0;
                    for(ISTVItem item:items){
                        if(i >= count)
                            break;
                        
                        onGotResource(new ISTVResource(ISTVVodRelateList.RES_STR_ITEM_TITLE, i, item.title));
                        onGotResource(new ISTVResource(ISTVVodRelateList.RES_INT_ITEM_PK, i, item.pk));
                        
                        /*if(item.posterURL!=null)*/{
                            ISTVItemBitmapSignal itemBmp = new ISTVItemBitmapSignal(getClient(), i, item, ISTVItemBitmapSignal.THUMB){
                                public void onSignal(){
                                    Bitmap bmp = getBitmap();

                                    onGotResource(new ISTVResource(ISTVVodRelateList.RES_BMP_ITEM_THUMB, getID(), bmp));
                                    pool.remove(this);
                                    dispose();
                                   
                                }
                            };
                            pool.add(itemBmp);
                        }
                        
                        i++;
                    } 
                    pool.remove(this);
                }
                
            };
            pool.add(signal);
        }
        
    }

}
