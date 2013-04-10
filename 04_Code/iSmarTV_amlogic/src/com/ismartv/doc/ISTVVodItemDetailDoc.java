package com.ismartv.doc;

import java.util.Collection;
import java.lang.System;
import java.util.Date;
import java.text.SimpleDateFormat;
import android.graphics.Bitmap;
import android.util.Log;
import com.ismartv.client.*;
import com.ismartv.service.ISTVItem;
import com.ismartv.ui.ISTVVodItemDetail;


public class ISTVVodItemDetailDoc extends ISTVDoc{
	private static final String TAG = "ISTVVodItemDetailDoc";
	private static final int maxrelateitem = 4;
	
	private int itemPK;
	private ISTVItemDetailSignal itemdetail;
	private ISTVBookmarkSignal bookmarkSignal;
	private ISTVRateSignal ratesignal;
	private ISTVRelateListSignal itemrelate;
	private String lang;
	private String itemattrs="";
	private int valuecount;
	private ISTVItemBitmapSignal posterurl;
	private ISTVItemBitmapSignal relateBmps[];

	public ISTVVodItemDetailDoc(int pk){
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
//				itemdetail.refresh();由于服务器有缓存,即时刷新也没有意思
			}
			
		}
	}	
	
	public void onCreate(){
		super.onCreate();

		itemdetail = new ISTVItemDetailSignal(getClient(), itemPK){
			public void onSignal(){
				
				ISTVItem item = getItem();
				if(item==null)
					return;

				/*get and init lang*/
				lang = "zh_CN";

				onGotResource(new ISTVResource(ISTVVodItemDetail.RES_STR_ITEM_TITLE, 0, item.title));

				onGotResource(new ISTVResource(ISTVVodItemDetail.RES_DOUBLE_ITEM_RATINGAVERAGE, 0, item.ratingAverage));

				
				if(item.attrs.airDateAttr!=null){
					String str_tmp = item.attrs.airDateAttr.title.get(lang); 
					if(str_tmp != null)
					{
						itemattrs = str_tmp;
						itemattrs = itemattrs + ":";				
					} 
					if(item.attrs.airDate!=null){						
						//SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); 
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy"); 
						str_tmp = sdf.format(item.attrs.airDate);  
						if(str_tmp != null)
						{
//							if(itemattrs == null){
//								itemattrs = str_tmp ;
//							}else{
//								itemattrs = itemattrs + str_tmp;
//							}
							itemattrs = (itemattrs==null?"":itemattrs) + str_tmp;
						}
					}
					itemattrs = itemattrs + "\n";
				} 
				if(item.attrs.areaAttr != null) {
					String str_tmp = item.attrs.areaAttr.title.get(lang);
					if(str_tmp != null)
					{
						itemattrs = itemattrs + str_tmp;
						itemattrs = itemattrs + ":";				
					}					
					
					if(item.attrs.area!=null){
						str_tmp = item.attrs.area;
						if(str_tmp != null){
							itemattrs = itemattrs + str_tmp;
						}
					}
					itemattrs = itemattrs + "\n";
				} 
				if(item.attrs.genreAttr != null) {
					String str_tmp = item.attrs.genreAttr.title.get(lang);
					if(str_tmp != null)
					{
						itemattrs = itemattrs + str_tmp;
						itemattrs = itemattrs + ":";				
					}					

					if(item.attrs.genres!=null){
						valuecount = 0;
						for(String genre : item.attrs.genres){
							if(valuecount != 0){
								itemattrs = itemattrs + ",";
							}					
							valuecount++;
							
							str_tmp = genre;
							if(str_tmp != null){
								itemattrs = itemattrs + str_tmp;
							}
							
						}						
					}
					itemattrs = itemattrs + "\n";					
				} 
				
				if(item.attrs.attrs!=null){
					for(ISTVItem.Attribute attr : item.attrs.attrs){

						if(attr!=null){
							String str_tmp = attr.attr.title.get(lang);
						
							if(str_tmp != null)
							{
								itemattrs = itemattrs + str_tmp;
								itemattrs = itemattrs + ":";
							}

							if(attr.values!=null){
								valuecount = 0;
								for(ISTVItem.Value value : attr.values){
									if(valuecount != 0){
										itemattrs = itemattrs + ",";
									}
								
									valuecount++;

									str_tmp = value.name;
								
									if(str_tmp != null)
									{
										itemattrs = itemattrs + str_tmp;
									}
								}
							}
							itemattrs = itemattrs + "\n";							
						}
						
					}
				}				 
				
				onGotResource(new ISTVResource(ISTVVodItemDetail.RES_STR_ITEM_ATTRS, 0, itemattrs));

				onGotResource(new ISTVResource(ISTVVodItemDetail.RES_STR_ITEM_DESCRIPTION, 0, (item.description!=null)?item.description:null));

				/*if(item.posterURL!=null)*/{
					posterurl = new ISTVItemBitmapSignal(getClient(), item, ISTVItemBitmapSignal.POSTER){
						public void onSignal(){
							Bitmap bmp = getBitmap();

							onGotResource(new ISTVResource(ISTVVodItemDetail.RES_BMP_ITEM_POSTERURL, 0, bmp));

							onUpdate();
						}
					};
				}

				if(item.url!=null){
					onGotResource(new ISTVResource(ISTVVodItemDetail.RES_BMP_ITEM_PLAYURL, 0, item.url));
				}

				onGotResource(new ISTVResource(ISTVVodItemDetail.RES_INT_EPISODE_COUNT, 0, item.episode));

				if(item.subItems!=null){
					onGotResource(new ISTVResource(ISTVVodItemDetail.RES_INT_EPISODE_REALCOUNT, 0, item.subItems.length));
					
					Date updateDate = new Date();
					int pk = -1;
					for(int i=0; i<item.subItems.length; i++){
						onGotResource(new ISTVResource(ISTVVodItemDetail.RES_INT_EPISODE_SUBITEM_PK, i, item.subItems[i]));						
						
						if(item.subItems[i]!=-1){
							ISTVItem subItem;
							subItem = getClient().getEpg().getItem(item.subItems[i]);
							if((subItem.updateDate != null) && updateDate.before(subItem.updateDate)){
								updateDate = subItem.updateDate;
								pk = subItem.pk;
							}
						}
					}
					onGotResource(new ISTVResource(ISTVVodItemDetail.RES_INT_FINALEPISODE_SUBITEM_PK, 0, pk));
				}

				if(client.getEpg().hasGotBookmarkList()){
					boolean bookmarked;	
					bookmarked = client.getEpg().getBookmarkFlag(itemPK);
					onGotResource(new ISTVResource(ISTVVodItemDetail.RES_BOOL_BOOKMARKED, 0, bookmarked));						
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
							onGotResource(new ISTVResource(ISTVVodItemDetail.RES_BOOL_BOOKMARKED, 0, bookmarked));		

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
//					onGotResource(new ISTVResource(ISTVVodItemDetail.RES_BOOL_BOOKMARK_ADD_ISSUCCESS, 0, getSuccess()));
//				} else {
//					onGotResource(new ISTVResource(ISTVVodItemDetail.RES_BOOL_BOOKMARK_REMOVED_ISSUCCESS, 0, getSuccess()));
//				}
//			}
//		};

		ratesignal = new ISTVRateSignal(getClient(), itemPK, -1){
			public void onSignal(){				
				onGotResource(new ISTVResource(ISTVVodItemDetail.RES_BOOL_RATINGAVERAGE_ISSUCCESS, 0, getSuccess()));
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

				onGotResource(new ISTVResource(ISTVVodItemDetail.RES_INT_RELATEITEML_COUNT, 0, size));

				int id=0;
				for(ISTVItem item : items){
					onGotResource(new ISTVResource(ISTVVodItemDetail.RES_STR_RELATEITEML_TITLE, id, (item.title!=null)?item.title:null));
					onGotResource(new ISTVResource(ISTVVodItemDetail.RES_STR_RELATEITEML_DESCRIPTION, id, (item.focus!=null)?item.focus:null));
					onGotResource(new ISTVResource(ISTVVodItemDetail.RES_INT_RELATEITEML_ITEMPK, id, item.pk));
					onGotResource(new ISTVResource(ISTVVodItemDetail.RES_BOOL_RELATEITEML_ISCOMPLEX, id, item.isComplex));

					/*if(item.adletURL!=null)*/{
						relateBmps[id] = new ISTVItemBitmapSignal(getClient(), id, item, ISTVItemBitmapSignal.ADLET){
							public void onSignal(){
								Bitmap bmp = getBitmap();

								onGotResource(new ISTVResource(ISTVVodItemDetail.RES_BMP_RELATEITEML, getID(), bmp));

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

