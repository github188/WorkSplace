package com.ismartv.client;

import java.util.Collection;
import java.util.Arrays;
import java.util.ArrayList;

import android.util.Log;

import com.ismartv.service.ISTVEvent;
import com.ismartv.service.ISTVSection;
import com.ismartv.service.ISTVItem;
import com.ismartv.service.ISTVRequest;

abstract public class ISTVItemListSignal extends ISTVSignal{
	private String section;
	private int start;
	private int count;
	private ISTVItem items[];

	public ISTVItemListSignal(ISTVClient c, String secID, int startPos, int itemCount){
		super(c);
		setRange(secID, startPos, itemCount);
	}

	public ISTVItemListSignal(ISTVClient c, int id, String secID, int startPos, int itemCount){
		super(c, id);
		setRange(secID, startPos, itemCount);
	}


	public Collection<ISTVItem> getItemList(){
		ArrayList<ISTVItem> retItems = null;

		if(items!=null){
			retItems = new ArrayList<ISTVItem>();
			for(ISTVItem item : items){
				retItems.add(item);
			}
		}

		return retItems;
	}

	public void setRange(int startPos, int itemCount){
		setRange(section, startPos, itemCount);
	}

	public void setRange(String secID, int startPos, int itemCount){
		if(section!=null && (section.equals(secID)) && (start==startPos) && (count==itemCount))
			return;

		section = secID;
		start   = startPos;
		count   = itemCount;

		if(count>0){
			items = new ISTVItem[count];
			Arrays.fill(items, null);
		}else{
			items = null;
		}

		refresh();
	}

	boolean isEnd(){
		if(items==null)
			return false;

		for(int i=0; i<items.length; i++){
			if(items[i]==null)
				return false;
		}

		return true;
	}

	boolean match(ISTVEvent evt){
		if(evt.type==ISTVEvent.Type.ITEM_LIST){
			if(section.equals(evt.secID)){
				if(items==null)
					return false;

				int first = evt.page*ISTVSection.countPerPage;
				int last  = first+ISTVSection.countPerPage;

				if((last<=start) || (first>=(start+count)))
					return false;

				return true;
			}
		}

		return false;
	}

	boolean copyData(ISTVEvent evt){
		if(items==null)
			return false;

		int id = evt.page*ISTVSection.countPerPage;

		for(ISTVItem item : evt.items){
			if(id>=start){
				if(id>=(start+count))
					break;

				items[id-start] = item;
			}
			id++;
		}

		return true;
	}
	
	public String getSection(){
	    return section;
	}

	boolean sendRequest(){
		int ps = start/ISTVSection.countPerPage;
		int pe = (start+count-1)/ISTVSection.countPerPage;

		while(ps<=pe){
			ISTVRequest req = new ISTVRequest(ISTVRequest.Type.ITEM_LIST);

			req.secID = section;
			req.page  = ps;

			client.sendRequest(req);

			ps++;
			
			Log.d("ItemListSignal", "send item list request: sec = "+section+", page = "+ps);
			try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
		}

		return true;
	}
}

