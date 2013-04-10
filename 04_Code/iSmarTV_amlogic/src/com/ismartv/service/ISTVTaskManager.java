package com.ismartv.service;

import java.util.ArrayList;
import java.util.HashSet;
import android.util.Log;

public class ISTVTaskManager{
	private static final String TAG="ISTVTaskManager";
	private static int THREAD_COUNT=10;

	private ISTVService service;
	private ArrayList<ISTVTask> taskLists[];
	private MyThread threads[];
	private boolean prio0Running=false;
	private boolean prio1Running=false;
	private boolean prio2Running=false;

	ISTVTaskManager(ISTVService s){
		service = s;
		taskLists = new ArrayList[ISTVTask.PRIO_COUNT];

		for(int i=0; i<ISTVTask.PRIO_COUNT; i++){
			taskLists[i] = new ArrayList<ISTVTask>();
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
				ISTVTask task = getReadyTask();
				if(task!=null){
					task.doJob();
                     try{
                            Thread.sleep(10);
                        }catch(Exception e){
                        }
				}else{
					try{
						Thread.sleep(10);
					}catch(Exception e){
					}
				}
			}
		}
	}

	ISTVService getService(){
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

	synchronized void addTask(ISTVTask task){
		int pos;

		if(task.prio<0 || task.prio>=ISTVTask.PRIO_COUNT)
			return;

		pos = taskLists[task.prio].size();
		taskLists[task.prio].add(pos, task);
		
		Log.d(TAG," >>>>>>> ISTVTaskManager , addTask:" + task ) ;
	}
	
	synchronized void removeTask(){
		Log.d(TAG, ">>>>>>>>>>> removeTask ");
		for(int i=0;i<ISTVTask.PRIO_COUNT;i++){
			ArrayList<ISTVTask> list1=taskLists[i];
			Log.d(TAG, "prio="+i+">>>>>>>>>>> removeTask clear list1.size="+list1.size());
			list1.clear();
		}
	}

	synchronized void removeTask(ISTVTask task){
		if(task.prio<0 || task.prio>=ISTVTask.PRIO_COUNT)
			return;

		taskLists[task.prio].remove(task);
		
		if(task.prio==0){
		    prio0Running = false;
		}

		if(task.prio==1){
			prio1Running = false;
		}

		if(task.prio==2){
			prio2Running = false;
		}
	}

	synchronized ISTVTask getReadyTask(){
		for(int i=0; i<ISTVTask.PRIO_COUNT; i++){
		    if(prio0Running && i>0){
		        return null;
		    }
			if(prio1Running && i>1)
				return null;
			if(prio2Running && i>2)
				return null;

			if(taskLists[i].size()>0){
				ISTVTask task = taskLists[i].get(0);

				if(i==0){
                    prio0Running = true;
                }
				if(i==1){
					prio1Running = true;
				}
				if(i==2){
					prio2Running = true;
				}

				taskLists[i].remove(task);

				return task;
			}
		}

		return null;
	}
}
