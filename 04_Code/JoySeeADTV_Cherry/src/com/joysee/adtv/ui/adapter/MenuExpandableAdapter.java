package com.joysee.adtv.ui.adapter;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.joysee.adtv.R;
import com.joysee.adtv.common.DefaultParameter.FavoriteFlag;
import com.joysee.adtv.common.DvbLog;
import com.joysee.adtv.common.ThreadPoolManager;
import com.joysee.adtv.controller.ViewController;
import com.joysee.adtv.logic.bean.DvbService;
import com.joysee.adtv.logic.bean.NETEventInfo;

public class MenuExpandableAdapter extends BaseExpandableListAdapter{
	DvbLog log = new DvbLog("MenuExpandableAdapter", DvbLog.DebugType.D);
    private LayoutInflater mInflater;
    private ArrayList<Integer> mParentList;
    private List<ArrayList<Integer>> mChildList;
    private Context mContext;
    private ViewController mViewController;
    private int mLastFavorite;
    private int mProgramNameLength;
    
    @SuppressWarnings("rawtypes")
	private SparseArray<AsyncTask> mTasks = new SparseArray<AsyncTask>();
    
    
    public MenuExpandableAdapter(Context context,ArrayList<Integer> dadList, List<ArrayList<Integer>> child,ViewController viewController) {
        mInflater = LayoutInflater.from(context);
        mParentList = dadList;
        mChildList=child;
        mContext = context;
        mViewController = viewController;
        mProgramNameLength = (int)mContext.getResources().getDimension(R.dimen.menu_clitem_program_info);
        log.D(" mLastFavorite "+mLastFavorite);
    }
    
    public void setLastFavorite(int lastFavorite){
    	mLastFavorite = lastFavorite;
    }
    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return (mChildList==null || mChildList.get(groupPosition%mParentList.size())==null)?null:mChildList.get(groupPosition%mParentList.size()).get(childPosition-1);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }
    
    @Override
    public View getChildView(int groupPosition, int childPosition,
            boolean isLastChild, View convertView, ViewGroup parent) {
    	
    	ViewHolder viewHolder;
        if (convertView == null|| convertView.getTag()==null) {
            viewHolder = new ViewHolder();
            convertView= mInflater.inflate(R.layout.menu_channel_list_item, null);
            viewHolder.channelView = convertView.findViewById(R.id.menu_clitem_channel_info);
            viewHolder.expandView = convertView.findViewById(R.id.menu_clitem_expand_info);
            viewHolder.expandDevide = (TextView) convertView.findViewById(R.id.menu_clitem_expand_devide);
            viewHolder.channelIcon = (ImageView) convertView.findViewById(R.id.menu_clitem_channel_icon);
            viewHolder.channelNum = (TextView) convertView.findViewById(R.id.menu_clitem_channel_num);
            viewHolder.channelName = (TextView) convertView.findViewById(R.id.menu_clitem_channel_name);
            viewHolder.programName = (TextView) convertView.findViewById(R.id.menu_clitem_program_name);
            viewHolder.programProgress = (TextView) convertView.findViewById(R.id.menu_clitem_program_progress);
            viewHolder.favoriteIcon = (ImageView) convertView.findViewById(R.id.menu_clitem_favorite_icon);
            viewHolder.expandIcon = (ImageView) convertView.findViewById(R.id.menu_clitem_expand_icon);
            viewHolder.expandText = (TextView) convertView.findViewById(R.id.menu_clitem_expand_text);
            convertView.setTag(viewHolder);
        }else {
            viewHolder=(ViewHolder)convertView.getTag();
        }
        
        if (mChildList!=null && mChildList.size()> 0 && mChildList.get(groupPosition%mParentList.size()) != null && mChildList.get(groupPosition%mParentList.size()).size() > childPosition) {
        	DvbService service = mViewController.getChannelByNativeIndex(mChildList.get(groupPosition%mParentList.size()).get(childPosition));
    		viewHolder.favoriteIcon.setVisibility(service.getFavorite() == FavoriteFlag.FAVORITE_YES?View.VISIBLE:View.INVISIBLE);	
    		viewHolder.channelNum.setText(String.valueOf(service.getLogicChNumber()));
    		viewHolder.channelName.setText(service.getChannelName());
			viewHolder.channelIcon.setImageResource(R.drawable.bc_icon);
			
    		if(isLastChild)
    			viewHolder.expandDevide.setVisibility(View.VISIBLE);
        }
        
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
    	log.D(" mParentList.size() "+mParentList.size());
    	log.D(" mChildList.size() "+mChildList.size());
        return (mChildList==null || mChildList.get(groupPosition%mParentList.size())==null)?0:(mChildList.get(groupPosition%mParentList.size())).size();
    }
    
    @Override
    public Object getGroup(int groupPosition) {
    	groupPosition = groupPosition % mParentList.size();
        return mParentList==null?null:mParentList.get(groupPosition);
    }
    
    @Override
    public int getGroupCount() {
    	return Integer.MAX_VALUE - 1000000;
    }
    
    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition%mParentList.size();
    }
    
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
            View convertView, ViewGroup parent) {
//    	log.D(" getGroupView position"+groupPosition +" index "+groupPosition%mParentList.size()+" mExpandPosition "+mExpandPosition);
    	long startTime = SystemClock.uptimeMillis();
        final ViewHolder viewHolder;
        if (convertView == null|| convertView.getTag()==null) {
            viewHolder = new ViewHolder();
            convertView= mInflater.inflate(R.layout.menu_channel_list_item, null);
            viewHolder.channelView = convertView.findViewById(R.id.menu_clitem_channel_info);
            viewHolder.expandView = convertView.findViewById(R.id.menu_clitem_expand_info);
            viewHolder.expandDevide = (TextView) convertView.findViewById(R.id.menu_clitem_expand_devide);
            viewHolder.channelNum = (TextView) convertView.findViewById(R.id.menu_clitem_channel_num);
            viewHolder.channelName = (TextView) convertView.findViewById(R.id.menu_clitem_channel_name);
            viewHolder.channelIcon = (ImageView) convertView.findViewById(R.id.menu_clitem_channel_icon);
            viewHolder.programName = (TextView) convertView.findViewById(R.id.menu_clitem_program_name);
            viewHolder.programProgress = (TextView) convertView.findViewById(R.id.menu_clitem_program_progress);
            viewHolder.favoriteIcon = (ImageView) convertView.findViewById(R.id.menu_clitem_favorite_icon);
            viewHolder.expandIcon = (ImageView) convertView.findViewById(R.id.menu_clitem_expand_icon);
            viewHolder.expandText = (TextView) convertView.findViewById(R.id.menu_clitem_expand_text);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
            if(viewHolder.tag !=0 ){
            	removeTask(viewHolder.tag);
            }
        }
        int index = groupPosition%mParentList.size();
    	if(index == mParentList.size()-1){
    		if(viewHolder.expandView.getVisibility() == View.GONE){
    			viewHolder.expandView.setVisibility(View.VISIBLE);
    			viewHolder.expandDevide.setVisibility(View.VISIBLE);
    		}
    		if(viewHolder.channelView.getVisibility() == View.VISIBLE){
    			viewHolder.channelView.setVisibility(View.GONE);
    		}
    		if(isExpanded){
    			viewHolder.expandIcon.setImageResource(R.drawable.menu_cltiem_shrink);
    			viewHolder.expandText.setText(R.string.close_bc_channal);
    		}else{
    			viewHolder.expandIcon.setImageResource(R.drawable.menu_clitem_expand);
    			viewHolder.expandText.setText(R.string.expand_bc_channel);
    		}
    	}else{
    		if(viewHolder.channelView.getVisibility() == View.GONE){
    			viewHolder.channelView.setVisibility(View.VISIBLE);
    		}
    		if(viewHolder.expandView.getVisibility() == View.VISIBLE){
    			viewHolder.expandView.setVisibility(View.GONE);
    			
    		}
    		if(index == mLastFavorite ){
    			viewHolder.expandDevide.setVisibility(View.VISIBLE);
    		}else{
    			viewHolder.expandDevide.setVisibility(View.INVISIBLE);
    		}
    		long start = SystemClock.uptimeMillis();
    		DvbService service = mViewController.getChannelByNativeIndex(mParentList.get(index));
    		int serviceId = service.getServiceId();
    		log.D("耗时 getView getChannelByNativeIndex  "+(SystemClock.uptimeMillis()-start));
    		viewHolder.favoriteIcon.setVisibility(service.getFavorite() == FavoriteFlag.FAVORITE_YES && index <= mLastFavorite?View.VISIBLE:View.INVISIBLE);	
    		viewHolder.channelNum.setText(String.valueOf(service.getLogicChNumber()));
    		viewHolder.channelName.setText(service.getChannelName());
    		
    		viewHolder.channelIcon.setImageDrawable(null);
    		viewHolder.programName.setText("");
			viewHolder.programProgress.setText("");
    		
			Bitmap bitmap = getBitmap(serviceId);
			if(bitmap != null)
				viewHolder.channelIcon.setImageBitmap(bitmap);
			else
				viewHolder.channelIcon.setImageResource(R.drawable.tv_icon);
			getNetEpgInfo(serviceId,viewHolder,index+1);
				
    	}
    	log.D(" getView 耗时  "+(SystemClock.uptimeMillis()-startTime));
    	log.D("");
        return convertView;
    }
    
    private final static SparseArray<SoftReference<Bitmap>> mIconCache = new SparseArray<SoftReference<Bitmap>>();
    
    private Bitmap getBitmap(int serviceId) {
    	long start = SystemClock.uptimeMillis();
    	SoftReference<Bitmap> value = mIconCache.get(serviceId);
    	Bitmap bitmap = null;
    	if(value != null)
    		bitmap = value.get();
    	if(bitmap != null){
    		log.D("-----------------------------   "+Thread.currentThread().getName()+"  "+serviceId);
			return bitmap;
    	}
		String iconPath = mViewController.getChannelIconPath(serviceId);
		log.D("耗时 native iconPath "+(SystemClock.uptimeMillis()-start) +" "+iconPath);
		if(iconPath != null){
			start = SystemClock.uptimeMillis();
			bitmap = BitmapFactory.decodeFile(iconPath);
			log.D("耗时 .png "+(SystemClock.uptimeMillis()-start) +" "+iconPath);
			if(bitmap != null){
				log.D("*******************************   "+Thread.currentThread().getName()+"  "+serviceId);
				mIconCache.put(serviceId, new SoftReference<Bitmap>(bitmap));
				return bitmap;
			}
		}
		return null;
	}
    private void removeTask(int tag){
    	log.D(" convertView !=  null mTasks.size() "+mTasks.size());
    	@SuppressWarnings("rawtypes")
		AsyncTask task = mTasks.get(tag);
    	if( task != null ){
    		Log.d("wgh", " remove "+tag);
        	task.cancel(true);
        	mTasks.remove(tag);
    	}
    }
    @SuppressWarnings("rawtypes")
	private void getNetEpgInfo(final int serviceId, final ViewHolder viewHolder,final int tag) {
    	viewHolder.tag = tag;
    	
    	AsyncTask task = new AsyncTask<Integer, Void, NETEventInfo>() {
			protected NETEventInfo doInBackground(Integer... params)  {
				long startTime = System.currentTimeMillis();
				log.D(" startTime ");
				NETEventInfo eventInfo = mViewController.getCurrentProgramInfo(serviceId);
				log.D("eventInfo 耗时"+(System.currentTimeMillis()-startTime)+"   "+Thread.currentThread().getName()+"  "+serviceId);
				return eventInfo;
			}

			@Override
			protected void onPostExecute(NETEventInfo eventInfo) {
				if(viewHolder.tag != tag)
					return;
				if(eventInfo !=null ){
					String eName = eventInfo.getEname();
					eventInfo.getBegintime();
					eventInfo.getDuration();
					if(eventInfo.getDuration() <= 0){
						viewHolder.programProgress.setText("");
					}else{
						int progress = (int) ((mViewController.getUtcTime()/1000 - eventInfo.getBegintime())*100/eventInfo.getDuration());
						if(progress >0 && progress<100){
							viewHolder.programProgress.setText("（"+progress+"%）");
						}else{
							viewHolder.programProgress.setText("");
						}
					}
					viewHolder.programName.setText(eName);
					if(viewHolder.programName.getText().length()>10){
						viewHolder.programName.setLayoutParams(new LayoutParams(mProgramNameLength, LayoutParams.WRAP_CONTENT));
					}else{
						viewHolder.programName.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
					}
					log.D(" ");
				}else{
					viewHolder.programName.setText(R.string.no_program_info);
				}
				viewHolder.tag = 0;
				mTasks.remove(tag);
				super.onPostExecute(eventInfo);
			}
			
		}.executeOnExecutor(ThreadPoolManager.getExecutor());
		
		Log.d("wgh", " put "+tag +"  serviceId " +serviceId);
		mTasks.put(tag, task);
	}
    
	@Override
    public boolean hasStableIds() {
        return false;
    }
    
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    } 
    
    private static class ViewHolder{
        ImageView channelIcon;
        TextView channelNum;
        TextView channelName;
        TextView programName;
        TextView programProgress;
        ImageView favoriteIcon;
        
        View channelView ;
		View expandView ;
		TextView expandDevide ;
		
		ImageView expandIcon ;
		TextView expandText ;
		int tag;
    }

	public void clearTaskMap() {
		Log.d("wgh"," clearTaskMap ");
		int size = mTasks.size();
		for(int i=0;i<size;i++){
			AsyncTask task = mTasks.valueAt(i);
			if(task !=null ){
				task.cancel(true);
			}
		}
		mTasks.clear();
	}
    
}