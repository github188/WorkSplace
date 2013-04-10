package com.joysee.adtv.server;

import com.joysee.adtv.logic.EPGManager;
import com.joysee.adtv.logic.bean.NETEventInfo;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 获取频道某一节目的内容
 * @author chenggang
 *
 */
abstract public class ADTVNETEventTask extends ADTVTask{
	private static final String TAG="ADTVProgramListTask";
	private static HashMap<String,ADTVNETEventTask> taskMap=new HashMap<String,ADTVNETEventTask>();
	
	public NETEventInfo eventInfo=new NETEventInfo();
	public int programId;
	EPGManager mEPGManager=null;

	public ADTVNETEventTask(int programId){
		super(null, ADTVTask.PRIO_JNI);
		mEPGManager = EPGManager.getInstance();
		this.programId=programId;
		
		NETEventInfo info=getEpg().getNETEventInfo(programId);
		if(info!=null){
		    eventInfo=info;
		    onSingal();
		    return;
		}
		
		start();
	}

	void onCancel(){
	}

	boolean process(){
		boolean ret = true;
        mEPGManager.nativeGetProgramInfo(programId, eventInfo);
        getEpg().addNETEventInfo(programId, eventInfo);
		return ret;
	}
}

