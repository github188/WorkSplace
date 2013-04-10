
package com.ismartv.doc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ismartv.client.*;
import com.ismartv.service.ISTVChannel;
import com.ismartv.service.ISTVContentModel;
import com.ismartv.service.ISTVItem;
import com.ismartv.ui.ISTVVodBookmark;
import com.ismartv.ui.ISTVVodConstant;
import com.ismartv.ui.ISTVVodHistory;

import android.graphics.Bitmap;
import android.util.Log;

public class ISTVVodBookmarkDoc extends ISTVDoc {

    private static final String TAG = "ISTVVodBookmarkDoc";   

//	private ISTVChannelListSignal chanList;
//	private Collection<ISTVChannel> channels;
    private List<String> cms;
	private int channels_bookmark_itemnum[];
	private int channels_bookmark_startrow[];
	private ISTVBookmarkSignal bookmarkSignal;
	
    
    private int bookmarkCount = 0;
    private int bookmarkRowCount = 0;
	private int bookmarkChannelCount = 0;

    private ArrayList<ISTVItem> sortItems = null;
    
    private List<ISTVSignal> pool = new ArrayList<ISTVSignal>();
    
    public ISTVVodBookmarkDoc() {
        // TODO Auto-generated constructor stub
        super();
        onCreate();
    }
    
    public void onCreate() {
        // TODO Auto-generated method stub
        Log.d(TAG,"ISTVVodBookmarkDoc onCreate!!!!!");
        super.onCreate();  
        
        cms = new ArrayList<String>();

	/*	chanList = new ISTVChannelListSignal(getClient()){
			public void onSignal(){
				channels = getChannelList();
				if(channels==null)
					return;

				channels_bookmark_itemnum = new int[channels.size()];
				channels_bookmark_startrow = new int[channels.size()];

				int i = 0;
				for(ISTVChannel chan : channels){
					Log.d("ISTVChannelListSignal","id"+i+"channel channel:"+chan.channel);
					
					channels_bookmark_itemnum[i] = 0;
					channels_bookmark_startrow[i] = 0;

					i++;
				}
			}
		};		*/

//		bookmarkSignal = new ISTVBookmarkSignal(getClient()){
//			public void onSignal(){
//			}
//		};			
    }
    
    public void fetchBookmarkData() {
        ISTVBookmarkListSignal bookmarkList = new ISTVBookmarkListSignal(getClient()){          
            @Override
            public void onSignal() {
                // TODO Auto-generated method stub
                Collection<ISTVItem> items = getBookmarkList();
				if(items == null){
					return;
				}
				
                bookmarkCount = Math.min(ISTVVodConstant.MAX_BOOKMARK_ITEM, items.size());
                Log.d(TAG, "size = "+bookmarkCount);
                
                if(bookmarkCount!=0){  
                    if(sortItems == null)
                        sortItems = new ArrayList<ISTVItem>();
                    else 
                        sortItems.clear();
                    fetchBookmarkSections(items);
                }
                
                onGotResource(new ISTVResource(ISTVVodBookmark.RES_INT_ITEMCOUNT, 0, bookmarkCount));
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
                    
                    onGotResource(new ISTVResource(ISTVVodBookmark.RES_STR_ITEM_TITLE, idx, item.title));
                    onGotResource(new ISTVResource(ISTVVodBookmark.RES_INT_ITEM_PK, idx, item.pk));
                    
                    /*if(item.posterURL!=null)*/{
                        ISTVItemBitmapSignal itemBmp = new ISTVItemBitmapSignal(getClient(), idx, item, ISTVItemBitmapSignal.POSTER){
                            public void onSignal(){
                                Bitmap bmp = getBitmap();

                                onGotResource(new ISTVResource(ISTVVodBookmark.RES_BMP_ITEM_POSTER, getID(), bmp));
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
    
    public int getBookmarkCount(){
        return bookmarkCount;
    }
    
    public int getBookmarkRowCount(){
        return bookmarkRowCount;
    }
    
    public void clearBookmark(){
		if(sortItems == null) {
			return;
		}
        sortItems.clear();
        bookmarkCount = 0;

		ISTVClearBookmarkSignal clearbookmark = new ISTVClearBookmarkSignal(getClient()){
			public void onSignal(){
			}
		};
		
		if(clearbookmark != null){
			clearbookmark.refresh();
		}
        //getClient().getEpg().clearBookmark();
        //getClient().getEpg().storeBookmark();
        onGotResource(new ISTVResource(ISTVVodBookmark.RES_INT_ITEMCOUNT, 0, bookmarkCount));
    }
    
    public void getBookmarkSections(){
		int i = 0;
		int id = 0;
		
		for(String cm : cms){
			if(channels_bookmark_itemnum[i] > 0){
			    ISTVContentModel contentModel = getClient().getEpg().getContentModel(cm);
	            if(contentModel!=null)
	                onGotResource(new ISTVResource(ISTVVodBookmark.RES_STR_SECTION_TITLE, id, contentModel.getTitle("zh_CN")));
				onGotResource(new ISTVResource(ISTVVodBookmark.RES_INT_SECTION_ITEMCOUNT, id, channels_bookmark_itemnum[i]));
				onGotResource(new ISTVResource(ISTVVodBookmark.RES_INT_SECTION_START, id, channels_bookmark_startrow[i]));	

				id++;
				
			}

			i++;
		}		
    }
    
    private void fetchBookmarkSections(Collection<ISTVItem> items){
			
		int idx = 0;

		for(ISTVItem item : items){
			if(idx >= bookmarkCount)
				break;
			Log.d(TAG, "----------item.contentModel="+item.contentModel);
			if(item.contentModel==null||item.contentModel.equals("")){
			    Log.d(TAG, "-------setGotBookmarkList(false)--------");
			    getClient().getEpg().setGotBookmarkList(false);
			}
			if(!cms.contains(item.contentModel)){
			    cms.add(item.contentModel);
			}
			
			idx++;
		}
		
        channels_bookmark_itemnum = new int[cms.size()];
        channels_bookmark_startrow = new int[cms.size()];

        int i = 0;
        for(String cm : cms){
            Log.d("getContentModels","id"+i+"cm:"+cm);
            
            channels_bookmark_itemnum[i] = 0;
            channels_bookmark_startrow[i] = 0;

            i++;
        }
		
        idx = 0;
        for (ISTVItem item : items) {
            if (idx >= bookmarkCount)
                break;

            i = cms.indexOf(item.contentModel);
            if(i>=0)
                channels_bookmark_itemnum[i]++;
            
            idx ++;
        }

		/*sort by channel list*/
		i = 0;
		for(String cm : cms){
			if(channels_bookmark_itemnum[i] > 0){
			    idx = 0;
				for(ISTVItem item : items){
					if(idx >= bookmarkCount)
						break;
					if(item.contentModel.equals(cm)){
						sortItems.add(item);
					}
					idx++;
				}
			}

			i++;
		}

		int start = 0;
		int id = 0;
		i = 0;
		for(String cm : cms){
			if(channels_bookmark_itemnum[i] > 0){
				int row = channels_bookmark_itemnum[i]/ISTVVodConstant.ITEM_PER_ROW;
				if(channels_bookmark_itemnum[i]%ISTVVodConstant.ITEM_PER_ROW!=0)
					row++;
				
				channels_bookmark_startrow[i] = start;
				
				start += row;
				
				id++;
			}

			i++;
		}	

		bookmarkRowCount = start;

		bookmarkChannelCount = id;
		
		onGotResource(new ISTVResource(ISTVVodBookmark.RES_INT_SECTION_COUNT, 0, id));
    }
    
    
    public void getBookmarkItem(){      
        int idx = 0;

        for(ISTVItem item : sortItems){
            if(idx >= bookmarkCount)
                break;
            
            int posId = caluPositionId(idx);
            
            onGotResource(new ISTVResource(ISTVVodBookmark.RES_STR_ITEM_TITLE, posId, item.title));
            onGotResource(new ISTVResource(ISTVVodBookmark.RES_INT_ITEM_PK, posId, item.pk));            
                       
            ISTVContentModel cm = getClient().getEpg().getContentModel(item.contentModel);
            if(cm!=null)
                onGotResource(new ISTVResource(ISTVVodBookmark.RES_STR_ITEM_MODEL, posId, cm.getTitle("zh_CN")));
            else
                Log.d(TAG, "cm is null!! model name:"+item.contentModel);
            
            /*if(item.posterURL!=null)*/{
                ISTVItemBitmapSignal itemBmp = new ISTVItemBitmapSignal(getClient(), posId, item, ISTVItemBitmapSignal.POSTER){
                    public void onSignal(){
                        Bitmap bmp = getBitmap();
						if(bmp != null){
							onGotResource(new ISTVResource(ISTVVodBookmark.RES_BMP_ITEM_POSTER, getID(), bmp));
						}
                        dispose();
                    }
                };
            }
            
            idx++;
        }
        onGotResource(new ISTVResource(ISTVVodBookmark.RES_BOOL_ITEM_UPDATED, -1, true));               
    }
    
    
    private int caluPositionId(int itemId){
		int i = 0;
		int j = 0;
		int itemnum_range0 = 0, itemnum_range1 = 0;
		int item_range = 0;

		int bookmark_valid_idx[] = new int[bookmarkChannelCount];
		
		for(String cm : cms){
			if(channels_bookmark_itemnum[i] > 0){
				bookmark_valid_idx[j] = i;
				j++;
			}

			i++;
		}		

		for(int k = 0; k <bookmarkChannelCount ; k++){

			itemnum_range1 += channels_bookmark_itemnum[bookmark_valid_idx[k]];
				
			item_range = channels_bookmark_startrow[bookmark_valid_idx[k]] * ISTVVodConstant.ITEM_PER_ROW;

			Log.d(TAG, "itemnum_range0="+itemnum_range0+",itemnum_range1="+itemnum_range1+",item_range="+item_range+",itemId="+itemId);
			if((k == 0) && (itemId < itemnum_range1)){
				return itemId;
			}else if((itemId >= itemnum_range0) && (itemId < itemnum_range1)){
				return itemId - itemnum_range0 + item_range;
			}else{
			}

			itemnum_range0 = itemnum_range1;
		}


		return itemId;
    }    
	
	public void removeBookmark(int itemPK){
		if(bookmarkSignal!=null){
			bookmarkSignal.removeBookmark(itemPK);
		}
	}	
}

