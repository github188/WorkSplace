package com.ismartv.doc;

import java.util.Collection;
import java.lang.System;
import java.util.Date;
import java.text.SimpleDateFormat;
import android.graphics.Bitmap;
import android.util.Log;
import com.ismartv.client.*;
import com.ismartv.service.ISTVItem;
import com.ismartv.ui.ISTVVodPlayerFinish;

public class ISTVVodPlayerFinishDoc extends ISTVDoc{
	private static final String TAG = "ISTVVodPlayerFinishDoc";
	private static final int maxrelateitem = 9;
	
	private int itemPK;
	private ISTVItemDetailSignal itemdetail;
	private ISTVBookmarkSignal bookmarkSignal;
	private ISTVRateSignal ratesignal;
	private ISTVRelateListSignal itemrelate;
	private String lang;
	private ISTVItemBitmapSignal posterurl;
	private ISTVItemBitmapSignal relateBmps[];

	public ISTVVodPlayerFinishDoc(int pk){
		super();
		itemPK = pk;
		onCreate();
	}

	public void addBookmark(){
		if(bookmarkSignal!=null){
			bookmarkSignal.addBookmark(itemPK);
		}
	}

	public void removeBookmark(){
		if(bookmarkSignal!=null){
			bookmarkSignal.removeBookmark(itemPK);
		}
	}

	public void rate(int v){
		if(ratesignal!=null){
			ratesignal.rate(v);

			if(itemdetail!=null){
				itemdetail.refresh();
			}			
		}
	}	
	
	public void onCreate(){
		itemdetail = new ISTVItemDetailSignal(getClient(), itemPK){
			public void onSignal(){
				
				ISTVItem item = getItem();
				if(item==null)
					return;

				/*get and init lang*/
				lang = "zh_CN";

				onGotResource(new ISTVResource(ISTVVodPlayerFinish.RES_STR_ITEM_TITLE, 0, item.title));

				onGotResource(new ISTVResource(ISTVVodPlayerFinish.RES_DOUBLE_ITEM_RATINGAVERAGE, 0, item.ratingAverage));

				/*if(item.posterURL!=null)*/{
					posterurl = new ISTVItemBitmapSignal(getClient(), item, ISTVItemBitmapSignal.POSTER){
						public void onSignal(){
							Bitmap bmp = getBitmap();

							onGotResource(new ISTVResource(ISTVVodPlayerFinish.RES_BMP_ITEM_POSTERURL, 0, bmp));

							onUpdate();
						}
					};
				}

				onGotResource(new ISTVResource(ISTVVodPlayerFinish.RES_INT_EPISODE_COUNT, 0, item.episode));

				if(item.subItems!=null){
					onGotResource(new ISTVResource(ISTVVodPlayerFinish.RES_INT_EPISODE_REALCOUNT, 0, item.subItems.length));

					for(int i=0; i<item.subItems.length; i++){
						onGotResource(new ISTVResource(ISTVVodPlayerFinish.RES_INT_EPISODE_SUBITEM_PK, i, item.subItems[i]));
					}
				}


				if(client.getEpg().hasGotBookmarkList()){
					boolean bookmarked;
					
					bookmarked = client.getEpg().getBookmarkFlag(itemPK);
					onGotResource(new ISTVResource(ISTVVodPlayerFinish.RES_BOOL_BOOKMARKED, 0, bookmarked));						
				}
				else{
					ISTVBookmarkListSignal bookmarkList = new ISTVBookmarkListSignal(getClient()){          
						@Override
						public void onSignal() {
							boolean bookmarked;
						
							// TODO Auto-generated method stub
							Collection<ISTVItem> items = getBookmarkList();
							if(items == null){
								return;
							}

							bookmarked = client.getEpg().getBookmarkFlag(itemPK);
							onGotResource(new ISTVResource(ISTVVodPlayerFinish.RES_BOOL_BOOKMARKED, 0, bookmarked));		

							onUpdate();

							dispose();
						}            
					};
				}
				

				onUpdate();
			}
		};

//		bookmarkSignal = new ISTVBookmarkSignal(getClient()){
//			public void onSignal(){
//				if(isAdd()) {
//					onGotResource(new ISTVResource(ISTVVodPlayerFinish.RES_BOOL_BOOKMARK_ADD_ISSUCCESS, 0, getSuccess()));
//				} else {
//					onGotResource(new ISTVResource(ISTVVodPlayerFinish.RES_BOOL_BOOKMARK_REMOVED_ISSUCCESS, 0, getSuccess()));
//				}			
//			}
//		};		

		ratesignal = new ISTVRateSignal(getClient(), itemPK, -1){
			public void onSignal(){
				onGotResource(new ISTVResource(ISTVVodPlayerFinish.RES_BOOL_RATINGAVERAGE_ISSUCCESS, 0, getSuccess()));
			}
		};

		itemrelate = new ISTVRelateListSignal(getClient(), itemPK){
			public void onSignal(){
				Collection<ISTVItem> items = getItemList();
				if(items==null)
					return;

				int size = items.size();
				if(size > maxrelateitem){
					size = maxrelateitem;
				}

				relateBmps = new ISTVItemBitmapSignal[size];

				onGotResource(new ISTVResource(ISTVVodPlayerFinish.RES_INT_RELATEITEML_COUNT, 0, size));

				int id=0;
				for(ISTVItem item : items){
					onGotResource(new ISTVResource(ISTVVodPlayerFinish.RES_STR_RELATEITEML_TITLE, id, (item.title!=null)?item.title:null));
					onGotResource(new ISTVResource(ISTVVodPlayerFinish.RES_STR_RELATEITEML_DESCRIPTION, id, (item.focus!=null)?item.focus:null));
					onGotResource(new ISTVResource(ISTVVodPlayerFinish.RES_INT_RELATEITEML_ITEMPK, id, item.pk));
					onGotResource(new ISTVResource(ISTVVodPlayerFinish.RES_BOOL_RELATEITEML_ISCOMPLEX, id, item.isComplex));

					/*if(item.adletURL!=null)*/{
						relateBmps[id] = new ISTVItemBitmapSignal(getClient(), id, item, ISTVItemBitmapSignal.ADLET){
							public void onSignal(){
								Bitmap bmp = getBitmap();

								onGotResource(new ISTVResource(ISTVVodPlayerFinish.RES_BMP_RELATEITEML, getID(), bmp));

								onUpdate();
							}
						};
					}

					if(id == (maxrelateitem - 1))
					{
						break;
					}
					
					id++;
				}

				onUpdate();				
			}
		};
		

	}
}

