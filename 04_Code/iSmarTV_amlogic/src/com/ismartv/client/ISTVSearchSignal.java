package com.ismartv.client;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Arrays;
import com.ismartv.service.ISTVSection;
import com.ismartv.service.ISTVEvent;
import com.ismartv.service.ISTVItem;
import com.ismartv.service.ISTVRequest;
import android.util.Log;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;

abstract public class ISTVSearchSignal extends ISTVSignal{
	private static final String TAG = "ISTVSearchSignal";
	private String word;
	private String contentModel;
	private int start;
	private int count;
	private ISTVItem items[];
	private int totalCount;
	private String mContentModel;

	public ISTVSearchSignal(ISTVClient c, String model, String s, int start, int count){
		super(c);
		Log.d(TAG, "ISTVSearchSignal");
		setSearchWord(model, s, start, count);		
	}

	public Collection<ISTVItem> getItemList(){
		ArrayList<ISTVItem> retItems=null;
		int i = start;

		if(items!=null){
			retItems = new ArrayList<ISTVItem>();			
			for(ISTVItem item : items){
				retItems.add(item);
			}
		}

		return retItems;
	}

	public void setSearchWord(String model, String s, int start, int count){
		if(contentModel==null || !model.equals(contentModel) || (word==null) || !word.equals(s)){
			this.contentModel = model;
			this.word = s;
			this.start = 0;
			this.count = 0;
			items = null;
		}

		setRange(start, count);
	}

	public void setRange(int startPos, int itemCount){
		Log.d(TAG, "setRange");
		if((this.start==startPos) && (this.count==itemCount))
			return;

		if(itemCount>0){
			items = new ISTVItem[itemCount];
			Arrays.fill(items, null);
		}else{
			items = null;
		}

		this.start = startPos;
		this.count = itemCount;		
		refresh();
	}

	boolean match(ISTVEvent evt){
		Log.d(TAG, "match contentModel="+evt.contentModel+"/"+ contentModel +", error="+evt.error);
		if(evt.type==ISTVEvent.Type.SEARCH){						
			if((evt.contentModel==contentModel) && (evt.word==word)){				
				Log.d(TAG, "match contentModel@@@="+evt.contentModel+"/"+ contentModel +", error="+evt.error);
				if(items==null)
					return false;				
				int first = evt.page*ISTVSection.countPerPage;
				int last  = first+ISTVSection.countPerPage;

				if((last<=start) || (first>=(start+count)))
					return false;
				Log.d(TAG, "match contentModel="+evt.contentModel+"/"+ contentModel +", error="+evt.error);
				return true;
			}
		}

		return false;
	}

	boolean copyData(ISTVEvent evt){
		Log.d(TAG, "copyData=" + evt.error);
		if(items==null)
			return false;		
		//int id = evt.page*ISTVSection.countPerPage;
		int id = 0;
		for(ISTVItem item : evt.items){
		/*	
			if(id>=start){
				if(id>=(start+count))
					break;

				items[id-start] = item;
			}
			id++;
		*/
			if(id < items.length) {
				items[id] = item;
				id++;	
				continue;
			}
			break;
		}		
		totalCount = evt.count;
		mContentModel = evt.contentModel;
		return true;
	}

	boolean sendRequest(){
		int ps = start/ISTVSection.countPerPage;
		int pe = (start+count-1)/ISTVSection.countPerPage;
		Log.d(TAG, "sendRequest ps= " + ps +",pe="+pe);
		while(ps<=pe){
			ISTVRequest req = new ISTVRequest(ISTVRequest.Type.SEARCH);
			try {
			
				String keyWord = URLEncoder.encode(word,"UTF-8");		
				word = keyWord;
				String model = URLEncoder.encode(contentModel,"UTF-8");		
			} catch(UnsupportedEncodingException e) {
			}
			req.contentModel = contentModel;
			req.word = word;
			req.page = ps;
			Log.d(TAG, "sendRequest = " + ps);
			client.sendRequest(req);

			ps++;
		}

		return true;
	}
	
	public int getTotalCount() {
		return totalCount;
	}
	
	public String getContentModel() {
		return mContentModel;
	}
}

