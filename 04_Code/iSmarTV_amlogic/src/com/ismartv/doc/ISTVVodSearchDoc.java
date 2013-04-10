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
import com.ismartv.ui.ISTVVodHistory;
import com.ismartv.ui.ISTVVodItemList;
import com.ismartv.client.ISTVSearchSignal;
import com.ismartv.client.ISTVHotWordsSignal;
import com.ismartv.client.ISTVSuggestWordsSignal;

import android.graphics.Bitmap;
import android.util.Log;

public class ISTVVodSearchDoc extends ISTVDoc {
    private static final String TAG = "ISTVVodSearchDoc";
	
	public static final int RES_INT_SEARCH_RESULT = 0;
	public static final int RES_INT_SEARCH_END = 1;
	public static final int RES_STR_HOTWORDS_RESULT = 2;
	public static final int RES_INT_HOTWORDS_END = 3;
	public static final int RES_STR_SUGGESTS_RESULT = 4;
	public static final int RES_INT_SUGGESTS_END = 5;	
	public static final int RES_BMP_ITEM_POSTER = 6;
	private int row = 0;
	
    public ISTVVodSearchDoc()
    {
        super();
    }
	
	public void getSearchResult(String model, String keyWord, int start, int count) {
		Log.d(TAG, "model=" + model + ",keyWord=" + keyWord + ",start=" + start + ",count=" + count);
		ISTVSearchSignal searchSignal = new ISTVSearchSignal(getClient(), model, keyWord, start, count) {
			public void onSignal() {				
				Collection<ISTVItem> items = getItemList();
				if(items == null){
				    return;                
				}
				row = items.size();
				if(row<=0){
				    return;
				}
				int pos = 0;
                for(ISTVItem item:items) {
                    if(item == null || pos>=row){
                        break;                    
                    }
                    HashMap<String, Object> hashMap = new HashMap<String, Object>();
                    hashMap.put("pk", "" + item.pk);
					hashMap.put("content_model", item.contentModel);
					hashMap.put("title", item.title);
					hashMap.put("focus", item.focus);
					hashMap.put("url", item.url.toString());
					hashMap.put("adlet_url", item.adletURL.toString());
					hashMap.put("position", "" + pos);
					if(item.isComplex) {
						hashMap.put("is_complex", "true");
					} else {
						hashMap.put("is_complex", "false");
					}
					if(item.quality < 4) {
						hashMap.put("is_hd", "false");						
					} else {
						hashMap.put("is_hd", "true");
					}
					hashMap.put("price", "" + item.price);
					hashMap.put("count", "" + getTotalCount());
					onGotResource(new ISTVResource(RES_INT_SEARCH_RESULT, pos, hashMap));
					
					//增加搜索结果图片回调
					ISTVItemBitmapSignal itemBmp = new ISTVItemBitmapSignal(getClient(), pos, item){
                        public void onSignal() {
                            Bitmap bmp = getBitmap();
                            onGotResource(new ISTVResource(ISTVVodSearchDoc.RES_BMP_ITEM_POSTER, getID(), bmp));
                        }
					    
					};
					pos++;
                }
				HashMap<String, Object> hashMap1 = new HashMap<String, Object>();
				hashMap1.put("content_model", getContentModel());
				hashMap1.put("count", getTotalCount());				
				onGotResource(new ISTVResource(RES_INT_SEARCH_END, -1, hashMap1));
				dispose();
			}
		};
	}
	
	public void getHotwordsResult() {
		Log.d(TAG, "getHotwordsResult");
		ISTVHotWordsSignal hotwordsSignal = new ISTVHotWordsSignal(getClient()) {
			public void onSignal() {
				Collection<String> hotwords = getHotWords();
				int pos = 0;
				for(String hotword : hotwords) {
					
					onGotResource(new ISTVResource(RES_STR_HOTWORDS_RESULT, pos, hotword));
					pos++;
				}
				onGotResource(new ISTVResource(RES_INT_HOTWORDS_END, -1, 1));
			}
		};
	}
	
	public void getSuggestResult(String word) {
		Log.d(TAG, "getSuggestResult");
		ISTVSuggestWordsSignal suggestSignal = new ISTVSuggestWordsSignal(getClient(), word) {
			public void onSignal() {
				Collection<String> suggests = getSuggestWords();
				int pos = 0;
				for(String suggest : suggests) {
					
					onGotResource(new ISTVResource(RES_STR_SUGGESTS_RESULT, pos, suggest));
					pos++;
				}
				onGotResource(new ISTVResource(RES_INT_SUGGESTS_END, -1, 1));
			}
		};
	}
	
	public int getSearchRowCount(){
	    double total = (double)row/4;
        long integerRow=Math.round(total);
        if(integerRow<total){
            return (int) (integerRow+1);
        }else{
            return (int) integerRow;
        }
	}
}