package com.joysee.adtv.server;

import android.util.Log;

abstract public class ADTVTask{
	private static final String TAG="ADTVTask";
	private ADTVTaskManager manager;


	static final int PRIO_ACCREDIT=0;
	static final int PRIO_ACCESS=1;
	static final int PRIO_JNI=2;
	static final int PRIO_DATA=3;
	static final int PRIO_BITMAP=4;
	static final int PRIO_COUNT=5;
	
	public static final int Status_Cancel=10;

	String url;
	int prio;
	int tryTimes;
	boolean running=true;
	public int error=ADTVError.NO_ERROR;
	String errInfo;
	int status=0;

	ADTVTask(String u, int p, int times){
	    manager=ADTVService.getService().getTaskManager();
		url  = u;
		prio = p;
		tryTimes = times;
	}

	ADTVTask(String u, int p){
		manager=ADTVService.getService().getTaskManager();
		url  = u;
		prio = p;
		tryTimes = 1;
	}
	
//	ADTVTask(ADTVTaskManager man, String u, int p){
//        Log.d(TAG, "==========ISTVTask.init()=========man"+man);
//        manager = man;
//        try {
//            url  = new URL(u);
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        }
//        prio = p;
//        tryTimes = 1;
//    }

	ADTVTaskManager getManager(){
		return manager;
	}

	ADTVService getService(){
		return manager.getService();
	}

	ADTVEpg getEpg(){
		return getService().getEpg();
	}

	ADTVBitmapCache getBitmapCache(){
		return getService().getBitmapCache();
	}

	void start(){
		Log.d(TAG,">>>>>>>>>>>> ISTVTask.start()");
		manager.addTask(this);
	}

	String getURL(){
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
					if(prio==PRIO_ACCREDIT){
						manager.removeTask();
						if(error==ADTVError.CANNOT_CONNECT_TO_SERVER){
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

		onSingal();
		cancel();
	}

	abstract boolean process();
	
	abstract public void onSingal();

	void onCancel(){
	}
	
	public void setStatus(int Status){
	    status=Status;
	}
	
	public int getStatus(){
	    return status;
	}
}
