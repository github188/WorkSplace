package com.ismartv.client;

import android.util.Log;
import com.ismartv.service.ISTVEvent;
import com.ismartv.service.ISTVRequest;

abstract public class ISTVAddHistorySignal extends ISTVSignal{
	private static final String TAG="ISTVAddHistorySignal";
	private int itemPK;
	private int subItemPK;
	private int offset;

	public ISTVAddHistorySignal(ISTVClient c, int pk, int sub){
		super(c);
		itemPK = pk;
		subItemPK = sub;
	}

	public void setOffset(int pk, int sub, int off){
		itemPK = pk;
		subItemPK = sub;
		offset = off;

		if(client.getEpg().addHistory(itemPK, subItemPK, offset)){
//			refresh();//播放记录不上传服务器
			client.getEpg().storeHistory();
		}
	}

	public void setOffset(int off){
		if(off!=offset){
			offset = off;

			if(client.getEpg().addHistory(itemPK, subItemPK, offset)){
				refresh();
				client.getEpg().storeHistory();
			}
		}
	}

	boolean match(ISTVEvent evt){
		if(evt.type==ISTVEvent.Type.ADD_HISTORY){
			if(evt.itemPK==itemPK && evt.subItemPK==subItemPK){
				return true;
			}
		}

		return false;
	}

	boolean copyData(ISTVEvent evt){
		return true;
	}

	boolean sendRequest(){
		ISTVRequest req = new ISTVRequest(ISTVRequest.Type.ADD_HISTORY);

		req.itemPK = itemPK;
		req.subItemPK = subItemPK;
		req.offset = offset;
			
		client.sendRequest(req);
		return false;
	}
}

