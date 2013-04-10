package com.ismartv.service;

import java.net.URL;
import android.util.Log;

abstract public class ISTVTask{
	private static final String TAG="ISTVTask";
	private ISTVTaskManager manager;


	static final int PRIO_ACCREDIT=0;
	static final int PRIO_ACTIVE=1;
	static final int PRIO_ACCESS=2;
	static final int PRIO_DATA=3;
	static final int PRIO_BITMAP=4;
	static final int PRIO_COUNT=5;

	URL url;
	int prio;
	int tryTimes;
	boolean running=true;
	int error=ISTVError.NO_ERROR;
	String errInfo;

	ISTVTask(ISTVTaskManager man, URL u, int p, int times){
		manager = man;
		url  = u;
		prio = p;
		tryTimes = times;
	}

	ISTVTask(ISTVTaskManager man, URL u, int p){
		Log.d(TAG, "==========ISTVTask.init()=========man"+man);
		manager = man;
		url  = u;
		prio = p;
		tryTimes = 1;
	}

	ISTVTaskManager getManager(){
		return manager;
	}

	ISTVService getService(){
		return manager.getService();
	}

	ISTVEpg getEpg(){
		return getService().getEpg();
	}

	ISTVBitmapCache getBitmapCache(){
		return getService().getBitmapCache();
	}

	void start(){
		Log.d(TAG,">>>>>>>>>>>> ISTVTask.start()");
		manager.addTask(this);
	}

	URL getURL(){
		return url;
	}

	void cancel(){
		running = false;
		manager.removeTask(this);
		onCancel();
	}

	boolean isRunning(){
		return running;
	}

	void setError(int err){
		error = err;
	}

	void setError(int err, String info){
		error = err;
		errInfo = info;
	}

	void doJob(){
		if(tryTimes>0){
			while(tryTimes>0){
				error = 0;
				if(process())
					break;
				tryTimes--;
				try{
					Thread.sleep(10);
				}catch(Exception e){
				}
				if(tryTimes<=0){
					Log.d(TAG, ">>>>>>>>>>>>>>>>>>>  ----prio="+prio);
					if(prio==PRIO_ACTIVE){
						manager.removeTask();
						if(error==ISTVError.CANNOT_CONNECT_TO_SERVER){
							manager.setNullService(0);
						}else{
							manager.setNullService(1);
						}
						running = false;
						onCancel();
						return;
					}else if(prio==PRIO_ACCESS){
					    manager.removeTask();
                        manager.setNullService(1);
                        running = false;
                        onCancel();
                        return;
					}else if(prio==PRIO_ACCREDIT){
					    onCancel();
					    return;
					}
				}
			}
		}else{
			while(true){
				error = 0;
				if(process())
					break;
				try{
					Thread.sleep(10);
				}catch(Exception e){
				}
			}
		}

		if(error!=ISTVError.NO_ERROR){
			ISTVEvent evt = createEvent();

			if(evt==null){
				Log.d(TAG, "system task error");
				getService().sendError(error, errInfo);
			}else{
			    Log.d(TAG, "------------------*********----type="+evt.type+";error="+error);
			    if(evt.type!=ISTVEvent.Type.BITMAP){
			        Log.d(TAG, "normal task error");
	                evt.error = error;
	                getService().sendEvent(evt);
			    }else{
			        Log.d(TAG, "--------------------------))))))))))))))))---get Bitmap error---");
			    }
			}
		}
		cancel();
	}

	abstract boolean process();
	
	ISTVEvent createEvent(){
		return null;
	}

	void onCancel(){
	}
}
