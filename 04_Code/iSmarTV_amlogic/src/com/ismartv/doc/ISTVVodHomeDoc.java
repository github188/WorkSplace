package com.ismartv.doc;

import java.util.Collection;
import android.graphics.Bitmap;
import android.util.Log;
import com.ismartv.client.*;
import com.ismartv.service.ISTVChannel;
import com.ismartv.service.ISTVItem;
import com.ismartv.service.ISTVTopVideo;
import com.ismartv.ui.ISTVVodHome;

public class ISTVVodHomeDoc extends ISTVDoc{
	private static final String TAG="ISTVVodHomeDoc";
	private ISTVChannelListSignal chanList;
	private ISTVVodHomeListSignal vodHomeList;
	private ISTVTopVideoSignal vodTopVideo;

	private ISTVBitmapSignal chanNormalBmps[];
	private ISTVBitmapSignal chanFocusedBmps[];
	private ISTVBitmapSignal homeListBmps[]; 
	private ISTVBitmapSignal topVideoideoideoBmps;
	//private long geoId;

	public ISTVVodHomeDoc()
	{
		super();
		//geoId=geoid;
		onCreate();
	}
	
	public void onCreate(){

		Log.d(TAG,"ISTVVodHomeDoc onCreate!!!!!");

		vodHomeList = new ISTVVodHomeListSignal(getClient()){
			public void onSignal(){
				Collection<ISTVItem> items = getItemList();
				if(items==null)
					return;

				int size = items.size();

				homeListBmps = new ISTVBitmapSignal[size];
				
				onGotResource(new ISTVResource(ISTVVodHome.RES_INT_RECOMMEND_COUNT, 0, size));

				int id=0;
				for(ISTVItem item : items){

					Log.d(TAG,"id"+id+"channel name:"+item.title);

					onGotResource(new ISTVResource(ISTVVodHome.RES_INT_RECOMMEND_PK, id, item.pk));
					onGotResource(new ISTVResource(ISTVVodHome.RES_STR_RECOMMEND_TITLE, id, item.title));
					onGotResource(new ISTVResource(ISTVVodHome.RES_BOOL_RECOMMEND_ISCOMPLEX, id, item.isComplex));

					if(item.posterURL!=null)
						homeListBmps[id] = new ISTVBitmapSignal(getClient(), id, item.posterURL){
							public void onSignal(){
								Bitmap bmp = getBitmap();

								onGotResource(new ISTVResource(ISTVVodHome.RES_BMP_RECOMMEND, getID(), bmp));
								onUpdate();
							}
						};
					
					id++;
				}

				onUpdate();
			}
		};

		chanList = new ISTVChannelListSignal(getClient()){
			public void onSignal(){
				Collection<ISTVChannel> channels = getChannelList();
				if(channels==null)
					return;

				int size = channels.size() - 1;//不要收藏

				onGotResource(new ISTVResource(ISTVVodHome.RES_INT_CHANNEL_COUNT, 0, size));

				chanNormalBmps = new ISTVBitmapSignal[size];
				chanFocusedBmps = new ISTVBitmapSignal[size];

				int id=0;
				for(ISTVChannel chan : channels){
					Log.d("ISTVChannelListSignal","id"+id+"channel name:"+chan.name +" channel " +chan.channel);
					if("收藏".equals(chan.name)){
						Log.d("ISTVChannelListSignal", "    收藏    return");
					}else{
						onGotResource(new ISTVResource(ISTVVodHome.RES_STR_CHANNEL_ID, id, chan.channel));
						onGotResource(new ISTVResource(ISTVVodHome.RES_STR_CHANNEL_NAME, id, chan.name));
						chanNormalBmps[id] = new ISTVBitmapSignal(getClient(), id, chan.bmpNormalURL){
							public void onSignal(){
								Bitmap bmp = getBitmap();	
								Log.d(TAG,">>>>>ISTVBitmapSignal:" + getURL() );
								onGotResource(new ISTVResource(ISTVVodHome.RES_BMP_CHANNEL_NORMAL, getID(), bmp));
								onUpdate();
							}
						};
						chanFocusedBmps[id] = new ISTVBitmapSignal(getClient(), id,chan.bmpFocusedURL){
							public void onSignal(){
								Bitmap bmp = getBitmap();
								Log.d(TAG,">>>>>ISTVBitmapSignal:" + getURL() );
								onGotResource(new ISTVResource(ISTVVodHome.RES_BMP_CHANNEL_FOCUSED, getID(), bmp));
								onUpdate();
							}
						};
						id++;
					}
				}
				updateLuanchMenu(channels);

				onUpdate();
			}
		};


		vodTopVideo = new ISTVTopVideoSignal(getClient()){
			public void onSignal(){
				ISTVTopVideo tv = getTopVideo();
				
				if(tv==null)
					return;
				
				if(tv.videoTitle!=null){
					onGotResource(new ISTVResource(ISTVVodHome.RES_STR_TOP_VIDEO_TITLE, 0, tv.videoTitle));
					Log.d(TAG,"video title:"+tv.videoTitle);
				}
				

				if(tv.videoChanID!=null&&tv.videoSecID!=null){
					onGotResource(new ISTVResource(ISTVVodHome.RES_STR_TOP_VIDEO_CHAN, 0, tv.videoChanID));
					Log.d(TAG,"video channel:"+tv.videoChanID);

					onGotResource(new ISTVResource(ISTVVodHome.RES_STR_TOP_VIDEO_SEC, 0, tv.videoSecID));
					Log.d(TAG,"video channel:"+tv.videoChanID);
					
				}
				else
				{
					onGotResource(new ISTVResource(ISTVVodHome.RES_STR_TOP_VIDEO_PK, 0, tv.videoItemPK));
				}

				if(tv.videoPosterURL!=null)
					topVideoideoideoBmps = new ISTVBitmapSignal(getClient(), tv.videoPosterURL){
						public void onSignal(){
							Bitmap bmp = getBitmap();

							onGotResource(new ISTVResource(ISTVVodHome.RES_BMP_TOP_VIDEO_POSTER, getID(), bmp));
							Log.d(TAG, "top video poster");
							onUpdate();
						}
					};

				if(tv.videoURL!=null){
					onGotResource(new ISTVResource(ISTVVodHome.RES_URL_TOP_VIDEO, 0, tv.videoURL));
					Log.d(TAG,"video url:"+tv.videoURL);
				}

				if(tv.imageURL!=null)
					topVideoideoideoBmps = new ISTVBitmapSignal(getClient(), tv.imageURL){
						public void onSignal(){
							Bitmap bmp = getBitmap();

							onGotResource(new ISTVResource(ISTVVodHome.RES_BMP_TOP_IMAGE, getID(), bmp));
							Log.d(TAG, "top video image poster");
							onUpdate();
						}
					};

				if(tv.imageChanID!=null&&tv.imageSecID!=null)
				{
					onGotResource(new ISTVResource(ISTVVodHome.RES_STR_TOP_IMAGE_CHAN, 0, tv.imageChanID));
					Log.d(TAG,"image imageChanID:"+tv.imageChanID);

					onGotResource(new ISTVResource(ISTVVodHome.RES_STR_TOP_IMAGE_SEC, 0, tv.imageSecID));
					Log.d(TAG,"image imageSecID:"+tv.imageSecID);

				}
				else
				{
					//if(tv.imageItemPK)
					onGotResource(new ISTVResource(ISTVVodHome.RES_STR_TOP_IMAGE_PK, 0, tv.imageItemPK));
				}

				if(tv.imageTitle!=null)
				{
					onGotResource(new ISTVResource(ISTVVodHome.RES_STR_TOP_IMAGE_TITLE, 0, tv.imageTitle));
					Log.d(TAG,"image imageTitle:"+tv.imageTitle);
				}

				onUpdate();
			}
		};
		
	}
}

