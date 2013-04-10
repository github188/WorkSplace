package com.joysee.adtv.common;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolManager {
	
	private static ExecutorService mExecutorService;

	public static ExecutorService getExecutor() {
		if (mExecutorService == null) {
			mExecutorService = Executors.newFixedThreadPool(2);
		}
		return mExecutorService;
	}
}
