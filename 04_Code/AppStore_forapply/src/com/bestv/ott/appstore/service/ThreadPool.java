package com.bestv.ott.appstore.service;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class ThreadPool {
	
	private static final String TAG = "com.joysee.appstore.ThreadPool";
	
	 private static final int CORE_POOL_SIZE = 5;
	 private static final int MAX_POOL_SIZE = 5;
	 private static final int KEEP_ALIVE_TIME = 2; // 10 seconds
	
	 private final ThreadPoolExecutor mExecutor;
	 
//	 private ExecutorService mExecutor;
	 
	 public Executor getExecutor() {
		return mExecutor;
	}

	private static final ThreadPool instance = new ThreadPool();
	 
	 private ThreadPool() {
        mExecutor = new ThreadPoolExecutor(
                CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME,
                TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(),
                new PriorityThreadFactory("thread-pool",
                android.os.Process.THREAD_PRIORITY_LOWEST));
//		 mExecutor = Executors.newFixedThreadPool(10, new PriorityThreadFactory("thread-pool",
//                android.os.Process.THREAD_PRIORITY_LOWEST));
    }
	 
	 public static ThreadPool getInstance(){
		 return instance;
	 }
	
	public void submit(Runnable worker){
//		if (AppStoreConfig.DOWNLOADDEBUG) {
//			Log.d(TAG, "======before====mExecutor.getQueue().size()="+mExecutor.getQueue().size());
//			Log.d(TAG, "=====before=====executor.size()="+mExecutor.getPoolSize());
//		}
		mExecutor.execute(worker);
//		if(AppStoreConfig.DOWNLOADDEBUG){
//			Log.d(TAG, "======after====mExecutor.getQueue().size()="+mExecutor.getQueue().size());
//			Log.d(TAG, "=====after=====executor.size()="+mExecutor.getPoolSize());
//		}
	}

}
