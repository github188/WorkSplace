package com.ismartv.doc;

import java.util.Collection;
import java.net.URL;
import java.lang.Integer;
import android.graphics.Bitmap;
import com.ismartv.client.*;
import com.ismartv.service.ISTVChannel;
import com.ismartv.service.ISTVItem;
import com.ismartv.service.ISTVClip;
import com.ismartv.ui.ISTVVodPlayer;
import android.util.Log;

public class ISTVVodPlayerDoc extends ISTVDoc{
	private static final String TAG="ISTVVodPlayerDoc";
	private int itemPK;
	private int subItemPK;
	private int subItemID=-1;
	private ISTVClipSignal clipSignal;
	private ISTVItemDetailSignal itemDetailSignal;
	private ISTVAddHistorySignal addHistorySignal;
	private ISTVBookmarkSignal bookmarkSignal;
	private boolean getDetail=false;
	
	public ISTVVodPlayerDoc(int itemPK, int subItemPK){
		super();
		this.itemPK    = itemPK;
		this.subItemPK = subItemPK;
		onCreate();
	}

	public void setItemPK(int pk, int sub){
		this.itemPK = pk;
		this.subItemPK = sub;

		if(itemDetailSignal!=null){
			itemDetailSignal.refresh();
		}
		//注掉，当点剧集时，不能让它为0，不然不会断点续播
		/*if(getDetail && addHistorySignal!=null){
			addHistorySignal.setOffset(pk, sub, 0);
		}*/
	}

	public int getNextSubItemPK(){
		ISTVItem item;

		if(itemPK==-1 || subItemPK==-1 || subItemID==-1)
			return -1;

		item = getClient().getEpg().getItem(itemPK);
		if(item==null || item.subItems==null)
			return -1;

		if(subItemID>=(item.subItems.length - 1))
			return -1;

		return item.subItems[++subItemID];
	}

	public int getPrevSubItemPK(){
		ISTVItem item;

		if(itemPK==-1 || subItemPK==-1 || subItemID==-1)
			return -1;

		item = getClient().getEpg().getItem(itemPK);
		if(item==null || item.subItems==null)
			return -1;

		if(subItemID<=0)
			return -1;

		return item.subItems[--subItemID];
	}

	public int getSubItemPK(int index){
		ISTVItem item;

		if(itemPK==-1 || subItemPK==-1 || subItemID==-1)
			return -1;

		item = getClient().getEpg().getItem(itemPK);
		if(item==null || item.subItems==null)
			return -1;

		subItemID = index;

		return item.subItems[index];
	}

	public int getEpisode(){
		return subItemID;
	}

	public void setOffset(int offset){
		if(getDetail && addHistorySignal!=null){
			Log.d(TAG, "set offset item "+itemPK+" subItem "+subItemPK+" offset "+offset);
			addHistorySignal.setOffset(itemPK, subItemPK, offset);
		}
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

	public void onCreate(){
		super.onCreate();

		addHistorySignal = new ISTVAddHistorySignal(getClient(), itemPK, subItemPK){
			public void onSignal(){
			}
		};

//		bookmarkSignal = new ISTVBookmarkSignal(getClient()){
//			public void onSignal(){
//			}
//		};

		itemDetailSignal = new ISTVItemDetailSignal(getClient(), itemPK){
			public void onSignal(){
				ISTVItem item = getItem();
				ISTVItem subItem;
				String title;
				String clipLength;
				int clipPK;
				int offset;

				if(item.subItems!=null){
					if((subItemPK==-1) && item.subItems!=null){
						int i;

						for(i=item.subItems.length-1; i>0; i--){
							int pk = item.subItems[i];

							if(pk!=-1){
								ISTVItem eitem = getClient().getEpg().getItem(pk);
								if((i==item.subItems.length-1) && (eitem.offset==-1)){
									i = 0;
									break;
								}
								if(eitem!=null && eitem.offset!=0)
									break;
							}
						}

						subItemPK = item.subItems[i];
					}
				}
				
				if(subItemPK!=-1){
					subItem = getClient().getEpg().getItem(subItemPK);
					title = subItem.title;
					clipLength = subItem.clipLength;
					clipPK = subItem.clipPK;

					if(item.subItems!=null){
						for(int i=0; i<item.subItems.length; i++){
							if(subItemPK==item.subItems[i]){
								subItemID=i;
								break;
							}
						}
					}
				}else{
					title = item.title;
					clipLength = item.clipLength;
					clipPK = item.clipPK;
				}

				if(client.getEpg().hasGotBookmarkList()){
					boolean bookmarked;

					bookmarked = client.getEpg().getBookmarkFlag(itemPK);
					onGotResource(new ISTVResource(ISTVVodPlayer.RES_BOOL_BOOKMARKED, 0, bookmarked));					
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
							onGotResource(new ISTVResource(ISTVVodPlayer.RES_BOOL_BOOKMARKED, 0, bookmarked));

							dispose();
						}            
					};
				}
				
				offset = client.getEpg().getOffset(itemPK, subItemPK);
				Log.d(TAG, "item "+itemPK+" subItem "+subItemPK+" offset "+offset);
				onGotResource(new ISTVResource(ISTVVodPlayer.RES_INT_OFFSET, 0, offset));

				if(title!=null){
					onGotResource(new ISTVResource(ISTVVodPlayer.RES_STR_ITEM_TITLE, 0, title));
				}

				if(clipLength!=null){
					try{
						int length = Integer.parseInt(clipLength);
						onGotResource(new ISTVResource(ISTVVodPlayer.RES_INT_CLIP_LENGTH, 0, length));
					}catch(Exception e){
					}
				}

				clipSignal = new ISTVClipSignal(getClient(), clipPK){
					public void onSignal(){
						ISTVClip clip = getClip();

						for(int i=0; i<6; i++){
							//URL url = clip.urls[i];
							String url = clip.urls[i];
							Log.d(TAG, "ISTVClipSignal url="+url);
							if(url!=null){
								onGotResource(new ISTVResource(i, 0, url));
							}
						}

						onUpdate();
					}
				};

				if(item.subItems!=null){
					onGotResource(new ISTVResource(ISTVVodPlayer.RES_INT_EPISODE_REALCOUNT, 0, item.subItems.length));
				}
				
				getDetail = true;

				onUpdate();
			}
		};
	}
}

