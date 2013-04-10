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
import com.ismartv.ui.ISTVVodItemEpisode;

public class ISTVVodItemEpisodeDoc extends ISTVDoc{
	private static final String TAG = "ISTVVodItemEpisodeDoc";
	
	private int itemPK;
	private ISTVItemDetailSignal itemdetail;
	private String lang;
	private ISTVItemBitmapSignal posterurl;
	private String itemattrs="";
	private int valuecount;

	public ISTVVodItemEpisodeDoc(int pk){
		super();
		itemPK = pk;
		onCreate();
	}
	
	public void onCreate(){
		super.onCreate();

		itemdetail = new ISTVItemDetailSignal(getClient(), itemPK){
			public void onSignal(){
				int offset;
				
				ISTVItem item = getItem();
				if(item==null)
					return;

				/*get and init lang*/
				lang = "zh_CN";

				/*if(item.posterURL!=null)*/{
					posterurl = new ISTVItemBitmapSignal(getClient(), item, ISTVItemBitmapSignal.POSTER){
						public void onSignal(){
							Bitmap bmp = getBitmap();

							onGotResource(new ISTVResource(ISTVVodItemEpisode.RES_BMP_ITEM_POSTERURL, 0, bmp));

							onUpdate();
						}
					};
				}				

				onGotResource(new ISTVResource(ISTVVodItemEpisode.RES_STR_ITEM_TITLE, 0, item.title));

				onGotResource(new ISTVResource(ISTVVodItemEpisode.RES_DOUBLE_ITEM_RATINGAVERAGE, 0, item.ratingAverage));
				
				/*if(item.attrs.airDateAttr!=null){
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
							itemattrs = itemattrs + str_tmp;
						}
					}
					itemattrs = itemattrs + "\n";
				}*/
				
				/*if(item.attrs.areaAttr != null) {
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
				}*/
				
				/*if(item.attrs.genreAttr != null) {
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
				}*/
				if(item.attrs.attrs!=null){
					for(int i=item.attrs.attrs.length-1; i>=0; i--){
						ISTVItem.Attribute attr=item.attrs.attrs[i];
						if(attr!=null){
							String str_tmp = attr.attr.title.get(lang);
						    Log.d(TAG,"i="+i+";str_tmp="+str_tmp);
							if(str_tmp != null)
							{
								itemattrs = itemattrs + str_tmp;
								itemattrs = itemattrs + " :    ";
							}

							if(attr.values!=null){
								valuecount = 0;
								for(ISTVItem.Value value : attr.values){
									if(valuecount != 0){
										itemattrs = itemattrs + " ,  ";
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
				
				onGotResource(new ISTVResource(ISTVVodItemEpisode.RES_STR_ITEM_ATTRS, 0, itemattrs));

				onGotResource(new ISTVResource(ISTVVodItemEpisode.RES_INT_EPISODE_COUNT, 0, item.episode));

				if(item.subItems!=null){
					onGotResource(new ISTVResource(ISTVVodItemEpisode.RES_INT_EPISODE_REALCOUNT, 0, item.subItems.length));
/*
					boolean looked=false;
					for(int i=item.subItems.length-1; i>=0; i--){
						ISTVItem subItem = getClient().getEpg().getItem(item.subItems[i]);
						if(subItem!=null){
							if(looked){
								subItem.offset = -1;
							}else if(subItem.offset!=0){
								looked = true;
							}
						}
					}
*/
					for(int i=0; i<item.subItems.length; i++){
						onGotResource(new ISTVResource(ISTVVodItemEpisode.RES_INT_EPISODE_SUBITEM_PK, i, item.subItems[i]));

						if(item.subItems[i]!=-1){
							ISTVItem subItem;
							
							subItem = getClient().getEpg().getItem(item.subItems[i]);
							onGotResource(new ISTVResource(ISTVVodItemEpisode.RES_INT_EPISODE_SUBITEM_TITLE, i, subItem.title));

							if(subItem.clipLength!=null){
								int length = Integer.parseInt(subItem.clipLength);
								onGotResource(new ISTVResource(ISTVVodItemEpisode.RES_INT_EPISODE_CLIP_LENGTH, i, length));
							}

							offset = client.getEpg().getOffset(itemPK, item.subItems[i]);
							Log.d(TAG, "item "+itemPK+" subItem "+item.subItems[i]+" offset "+offset);
							onGotResource(new ISTVResource(ISTVVodItemEpisode.RES_INT_EPISODE_OFFSET_LENGTH, i, offset));
						}
					}
				}
				
				onUpdate();
			}
		};
		

	}
}

