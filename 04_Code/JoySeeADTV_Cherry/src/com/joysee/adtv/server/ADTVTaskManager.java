package com.joysee.adtv.server;

import java.util.ArrayList;

import android.util.Log;

public class ADTVTaskManager{
	private static final String TAG="ADTVTaskManager";
	private static int THREAD_COUNT=5;

	private ADTVService service;
	private ArrayList<ADTVTask> taskLists[];
	private MyThread threads[];
//	private boolean prio0Running=false;
//	private boolean prio1Running=false;

	ADTVTaskManager(ADTVService s){
		service = s;
		taskLists = new ArrayList[ADTVTask.PRIO_COUNT];

		for(int i=0; i<ADTVTask.PRIO_COUNT; i++){
			taskLists[i] = new ArrayList<ADTVTask>();
		}

		threads = new MyThread[THREAD_COUNT];
		for(int i=0; i<THREAD_COUNT; i++){
			threads[i] = new MyThread();
			threads[i].start();
		}
	}
	
	class MyThread extends Thread{
		boolean flag=true;
		public void run(){
			while(flag){ 
				ADTVTask task = getReadyTask();
				if(task!=null){
					task.doJob();
                     try{
                            Thread.sleep(100);
                        }catch(Exception e){
                        }
				}else{
					try{
						Thread.sleep(100);
					}catch(Exception e){
					}
				}
			}
		}
	}

	ADTVService getService(){
		return service;
	}
	
	void setNullService(int ty){
		Log.d(TAG, "setNullService");
		if(threads!=null){
			for(MyThread t:threads){
				t.flag=false;
			}
		}
		service.setNullService(ty);
	}

	synchronized void addTask(ADTVTask task){
		int pos;

		if(task.prio<0 || task.prio>=ADTVTask.PRIO_COUNT)
			return;

		pos = taskLists[task.prio].size();
		taskLists[task.prio].add(pos, task);
		
		Log.d(TAG," >>>>>>> ISTVTaskManager , addTask:" + task ) ;
	}
	
	synchronized void removeTask(){
		Log.d(TAG, ">>>>>>>>>>> removeTask ");
		for(int i=0;i<ADTVTask.PRIO_COUNT;i++){
			ArrayList<ADTVTask> list1=taskLists[i];
			Log.d(TAG, "prio="+i+">>>>>>>>>>> removeTask clear list1.size="+list1.size());
			list1.clear();
		}
	}

	public synchronized void removeTask(ADTVTask task){
		if(task.prio<0 || task.prio>=ADTVTask.PRIO_COUNT)
			return;

		taskLists[task.prio].remove(task);
		
//		if(task.prio==0){
//		    prio0Running = false;
//		}
//		if(task.prio==1){
//			prio1Running = false;
//		}
	}

	synchronized ADTVTask getReadyTask(){
		for(int i=0; i<ADTVTask.PRIO_COUNT; i++){
//		    if(prio0Running && i>0)
//		        return null;
//			if(prio1Running && i>1)
//				return null;

			if(taskLists[i].size()>0){
				ADTVTask task = taskLists[i].get(0);

//				if(i==0){
//                    prio0Running = true;
//                }
//				if(i==1){
//					prio1Running = true;
//				}

				taskLists[i].remove(task);

				return task;
			}
		}

		return null;
	}
}
