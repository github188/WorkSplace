package com.joysee.appstore.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import com.joysee.appstore.common.Constants;
import com.joysee.appstore.common.TaskBean;
import com.joysee.appstore.common.TaskDownSpeed;
import com.joysee.appstore.common.ThreadBean;
import com.joysee.appstore.db.DBUtils;
import com.joysee.appstore.utils.AppLog;
import com.joysee.appstore.utils.AppStoreConfig;

public class DownloadThread extends Thread {

	private static final String TAG = "com.joysee.appstore.DownloadThread";
	private static final int SLEEP_TIME = 10;
	private Context mContext;
	private Handler mHandler;
	private DBUtils mDBUtils = null;
	private RandomAccessFile saveFile;
	private URL downUrl;
	private int block;
	private int startPos;
	private int downLength;
	private int mTaskId;
	private int mThreadId;
	private boolean finish = false;
	private boolean isStop = false;
	private static final long BIGAPP = 1024 * 1024l; // apk block >1M
	private static final int BIGBUFFER = 102400;
	private int sumOffSize = 0;
	private long startTime = 0;

	public DownloadThread(Context context, URL downUrl,
			RandomAccessFile saveFile, int block, int startPos, int taskId,
			int threadId) {
		this(context,null,downUrl,saveFile,block,startPos,taskId,threadId);
		
	}

	public DownloadThread(Context context,Handler serviceHandler, URL downUrl,
			RandomAccessFile saveFile, int block, int startPos, int taskId,
			int threadId) {
		Process.setThreadPriority(Process.THREAD_PRIORITY_LOWEST);
		mHandler = serviceHandler;
		mContext = context;
		mDBUtils = new DBUtils(context);
		mTaskId = taskId;
		mThreadId = threadId;
		this.downUrl = downUrl;
		this.saveFile = saveFile;
		this.block = block;
		this.startPos = startPos;
		this.downLength = startPos - block*(threadId - taskId * Constants.RATE);
		startTime = System.currentTimeMillis();
		if (AppStoreConfig.DOWNLOADDEBUG) {
		Log.d(TAG,"=======new download thread===TaskId=" + mTaskId
				+ "==============thread=" + mThreadId+"======startTime ="+new Date(startTime));
		}
		sumOffSize = 0;
		
	}
	public void threadPause() {
		isStop = true;
	}

	public void threadStop() {
		finish = true;
	}
	
    @Override
    public void run() {
    	Process.setThreadPriority(Process.THREAD_PRIORITY_LOWEST);
//    	Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
		if (AppStoreConfig.DOWNLOADDEBUG) {
		Log.d(TAG, "====download start===========TaskId=" + mTaskId
				+ "==============thread=" + mThreadId
				+ "-downing======:" + downLength + "=========block:"
				+ block + "======finish=" + finish
				+ "****this thread priority = " + this.getPriority());
		}
		/* do not finish download */
        if (block>downLength) {
        	InputStream inStream=null;
            try {
                HttpURLConnection http = (HttpURLConnection)downUrl.openConnection();
                http.setRequestMethod("GET");
                TaskBean taskB=mDBUtils.queryTaskById(mTaskId);
                if(taskB==null||taskB.getPkgName()==null){
                    Log.d(TAG, "-------------------error------------------");
                    isStop=true;
                    return ;
                }
                if(taskB.getPkgName().equals(mContext.getPackageName())){
                	http.setReadTimeout(60*1000);
                }else{
                	http.setReadTimeout(Constants.Delayed.MIN_10);
                }
                http.setRequestProperty("Accept", "image/gif, image/jpeg, image/pjpeg, "
                        + "image/pjpeg, application/x-shockwave-flash, "
                        + "application/xaml+xml, application/vnd.ms-xpsdocument, "
                        + "application/x-ms-xbap, application/x-ms-application, "
                        + "application/vnd.ms-excel, application/vnd.ms-powerpoint, "
                        + "application/msword, */*");

//                http.setRequestProperty("Accept-Language", "zh-CN");
                http.setRequestProperty("Referer", downUrl.toString());
                http.setRequestProperty("Charset", "UTF-8");
                http.setRequestProperty("Range", "bytes=" + this.startPos + "-");
                http.setRequestProperty("User-Agent",
                        "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; "
                                + "Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; "
                                + ".NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; "
                                + ".NET CLR 3.5.30729)");

                http.setRequestProperty("Connection", "Keep-Alive");
                inStream = http.getInputStream();
                int	max = (block-downLength)>=BIGAPP?BIGBUFFER:((block-downLength) > 1024 ? 1024 : (((block-downLength)>100?100:10)));
                byte[] buffer = new byte[max];
                int offset = 0;
                
                AppLog.d(TAG, "start-while");
                
                ThreadBean  tThreadBean = new ThreadBean();
                tThreadBean.setTaskId(mTaskId);
                tThreadBean.setThreadId(mThreadId);
                startTime = System.currentTimeMillis();
                while (downLength < block && (offset = inStream.read(buffer, 0, max)) != -1&&!finish) {
                	if (AppStoreConfig.DOWNLOADDEBUG) {
						Log.d(TAG, "=====TaskId=" + mTaskId + "===thread="
								+ Thread.currentThread().getId() + "===block="
								+ block + "-===downing=" + downLength
								+ "===offset=" + offset + "======finish="
								+ finish + "======isStop=" + isStop);
					}
                	if(isStop){
                		break;
                	}
                	int status=mDBUtils.queryTaskStatusById(mTaskId);
                	if(status!=Constants.DOWNLOAD_STATUS_EXECUTE){
                		isStop=true;
                		return;
                	}
//                	if(AppStoreConfig.DEBUG){
//                        Log.d(TAG, "=========================thread" + Thread.currentThread().getId() + "-downing:"
//                                + downLength);
//                        Log.d(TAG, "offset:" + offset);
//                    }
                    Thread.sleep(SLEEP_TIME);
                    saveFile.write(buffer, 0, offset);
                    downLength += offset;
                    sumOffSize +=offset;
                    TaskDownSpeed tds=DownloadService.speedMap.get(mTaskId);
                    if(tds!=null){
                    	tds.setOffSet(offset);
                        DownloadService.speedMap.put(mTaskId, tds);
                    }
                    startPos = block * (mThreadId-mTaskId*Constants.RATE)+downLength;
//                    long now = System.currentTimeMillis();
//                    if(now-startTime>Contants.MAX_PROGRESS_TIME||offset>=(block-downLength)){
                    if(sumOffSize>=(block-1)/10||sumOffSize>=(block-downLength)){
                    	tThreadBean.setPosition(startPos);
                        tThreadBean.setDownLength(downLength);
                        if (AppStoreConfig.DOWNLOADDEBUG) {
    						Log.d(TAG, "=====downloading before updateProgress()========TaskId=" + mTaskId + "===thread="
								+ Thread.currentThread().getId()+"====sumOffSize="+sumOffSize);
                        }
                    	mDBUtils.updateProgress(tThreadBean,sumOffSize);
                    	sumOffSize = 0;
                    }
                    int spare = block - downLength;
                    if (spare < max)
                        max = (int)spare;
                    
                }
                saveFile.close();
                inStream.close();
                if(isStop){
                	return;
                }
                if(downLength>=block-1||offset==-1){
                	this.finish = true;
                }else{
                	if (AppStoreConfig.DOWNLOADDEBUG) {
    					Log.d(TAG, "====download error===========TaskId="
    							+ mTaskId + "==============thread="
    							+ mThreadId
    							+ "-downing======:" + downLength
    							+ "================offset:" + offset
    							+ "======finish=" + finish);
    				}
                	mDBUtils.updateTaskStatus(mTaskId, Constants.DOWNLOAD_STATUS_ERROR);
                	Message msg = mHandler.obtainMessage(DownloadService.WORKMSG_DOWNLOAD_ERROR);
                	msg.arg1 = mTaskId;
                	msg.arg2 = mThreadId ;
                	mHandler.sendMessage(msg);
                	sendErrorBroadCast(mTaskId);
                }
                
                if (AppStoreConfig.DOWNLOADDEBUG) {
					Log.d(TAG, "====download finished===========TaskId="
							+ mTaskId + "==============thread="
							+ mThreadId
							+ "-downing======:" + downLength
							+ "================offset:" + offset
							+ "======finish=" + finish+"=======endTime="+new Date(System.currentTimeMillis()));
				}
            } catch (InterruptedException e) {
            		e.printStackTrace();
                    Log.d(TAG, e.toString());
                    if (AppStoreConfig.DOWNLOADDEBUG) {
    					Log.d(TAG, "====download error====InterruptedException=======TaskId="
    							+ mTaskId + "==============thread="
    							+ mThreadId
    							+ "-downing======:" + downLength
    							+ "======finish=" + finish);
    				}
                    if(isStop){
                    	return;
                    }
                    mDBUtils.updateTaskStatus(mTaskId, Constants.DOWNLOAD_STATUS_ERROR);
                    Message msg = mHandler.obtainMessage(DownloadService.WORKMSG_DOWNLOAD_ERROR);
                	msg.arg1 = mTaskId;
                	msg.arg2 = mThreadId ;
                	mHandler.sendMessage(msg);
                    sendErrorBroadCast(mTaskId);
                    finish=true;
//                    if(downLength<block-1){
//                    	mDBUtils.updateTaskStatus(mTaskId, Contants.DOWNLOAD_STATUS_ERROR);
//                        Message msg = mHandler.obtainMessage(DownloadService.WORKMSG_DOWNLOAD_ERROR);
//                    	msg.arg1 = mTaskId;
//                    	msg.arg2 = mThreadId ;
//                    	mHandler.sendMessage(msg);
//                    	sendErrorBroadCast(mTaskId);
//                    }
//                    return ;
			}catch (IOException e){
				e.printStackTrace();
                Log.d(TAG, e.toString());
                if (AppStoreConfig.DOWNLOADDEBUG) {
					Log.d(TAG, "====download error====IOException=======TaskId="
							+ mTaskId + "==============thread="
							+ mThreadId
							+ "-downing======:" + downLength
							+ "======finish=" + finish);
				}
                if(isStop){
                	return;
                }
                mDBUtils.updateTaskStatus(mTaskId, Constants.DOWNLOAD_STATUS_ERROR);
                Message msg = mHandler.obtainMessage(DownloadService.WORKMSG_DOWNLOAD_ERROR);
            	msg.arg1 = mTaskId;
            	msg.arg2 = mThreadId ;
            	mHandler.sendMessage(msg);
                sendErrorBroadCast(mTaskId);
                finish=true;
//                if(downLength<block-1){
//                	mDBUtils.updateTaskStatus(mTaskId, Contants.DOWNLOAD_STATUS_ERROR);
//                    Message msg = mHandler.obtainMessage(DownloadService.WORKMSG_DOWNLOAD_ERROR);
//                	msg.arg1 = mTaskId;
//                	msg.arg2 = mThreadId ;
//                	mHandler.sendMessage(msg);
//                	sendErrorBroadCast(mTaskId);
//                }
//                return ;
			}catch(Exception e){
				e.printStackTrace();
                Log.d(TAG, e.toString());
                if (AppStoreConfig.DOWNLOADDEBUG) {
					Log.d(TAG, "====download error====Exception=======TaskId="
							+ mTaskId + "==============thread="
							+ mThreadId
							+ "-downing======:" + downLength
							+ "======finish=" + finish);
                }
                if(isStop){
                	return;
                }
                mDBUtils.updateTaskStatus(mTaskId, Constants.DOWNLOAD_STATUS_ERROR);
                Message msg = mHandler.obtainMessage(DownloadService.WORKMSG_DOWNLOAD_ERROR);
            	msg.arg1 = mTaskId;
            	msg.arg2 = mThreadId ;
            	mHandler.sendMessage(msg);
                sendErrorBroadCast(mTaskId);
                finish=true;
			}finally{
				try {
				    DownloadService.multiThreadMap.remove(mTaskId);
					saveFile.close();
					if(inStream!=null){
						inStream.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
        }
    }
	
    
    private void sendErrorBroadCast(int taskId){
    	AppLog.d(TAG, "-------------sendErrorBroadCast----------"+Constants.INTENT_DOWNLOAD_ERROR);
    	Intent intent = new Intent(Constants.INTENT_DOWNLOAD_ERROR);
    	TaskBean taskBean=mDBUtils.queryTaskById(taskId);
    	if(taskBean.getPkgName().equals(mContext.getPackageName())){
    		Log.d(TAG, "---------------update market error ,delete taskBean------------------");
    		if(!mDBUtils.needStartService("com.joysee.appstore.service.DownloadService")){
    			Intent intentSer = new Intent(mContext,DownloadService.class);
    			mContext.startService(intentSer);
    		}
    	}
    	intent.putExtra("taskid", taskId);
    	intent.putExtra("app_id", taskBean.getSerAppID());
    	intent.putExtra("app_name", taskBean.getAppName());
    	intent.putExtra("pkg_name", taskBean.getPkgName());
    	mContext.sendBroadcast(intent);
    	AppLog.d(TAG, "-------------sendErrorBroadCast----------");
    }

//	@Override
//	public void run() {
//		// Process.setThreadPriority(Process.THREAD_PRIORITY_LOWEST);
//		if (AppStoreConfig.DOWNLOADDEBUG) {
//			Log.d(TAG, "====download start===========TaskId=" + mTaskId
//					+ "==============thread=" + mThreadId
//					+ "-downing======:" + downLength + "=========block:"
//					+ block + "======finish=" + finish
//					+ "****this thread priority = " + this.getPriority());
//		}
//		if ((block - downLength) > 0) {// do not finish download
//			HttpURLConnection http = null;
//			try {
//				http = (HttpURLConnection) downUrl.openConnection();
//			} catch (IOException e) {
//				e.printStackTrace();
//				Log.e(TAG, "=====openConnection catch ioexception"+e.toString());
//			}
//			if (null != http) {
//				try {
//					http.setRequestMethod("GET");
//				} catch (ProtocolException e) {
//					e.printStackTrace();
//					Log.e(TAG, "=====setRequestMethod catch ProtocolException"+e.toString());
//				}
//				http.setRequestProperty(
//						"Accept",
//						"image/gif, image/jpeg, image/pjpeg, "
//								+ "image/pjpeg, application/x-shockwave-flash, "
//								+ "application/xaml+xml, application/vnd.ms-xpsdocument, "
//								+ "application/x-ms-xbap, application/x-ms-application, "
//								+ "application/vnd.ms-excel, application/vnd.ms-powerpoint, "
//								+ "application/msword, */*");
//
//				http.setRequestProperty("Accept-Language", "zh-CN");
//				http.setRequestProperty("Referer", downUrl.toString());
//				http.setRequestProperty("Charset", "UTF-8");
//				if (AppStoreConfig.DOWNLOADDEBUG) {
//					Log.e(TAG, "============set Range before========================");
//				}
//				http.setRequestProperty("Range", "bytes=" + this.startPos +"-");
////				if(Utils.isBreakpointCon(downUrl)){
////					http.setRequestProperty("Range", "bytes=" + this.startPos +"-");
////				}
//				http.setRequestProperty(
//						"User-Agent",
//						"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; "
//								+ "Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; "
//								+ ".NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; "
//								+ ".NET CLR 3.5.30729)");
//
//				http.setRequestProperty("Connection", "Keep-Alive");
//				InputStream inStream = null;
////				BufferedReader bufferReader =null;
//				try {
//					inStream = http.getInputStream();
////					bufferReader = new BufferedReader(new InputStreamReader(inStream));
//				} catch (IOException e) {
//					e.printStackTrace();
//					Log.e(TAG, "=====getInputStream catch IOException"+e.toString());
//				}
//				int max = (block - downLength) >= BIGAPP ? BIGBUFFER
//						: (mLeftLength > 1024 ? 1024
//								: ((mLeftLength > 100 ? 100 : 10)));
//				byte[] buffer = new byte[max];
////				char[] buffer = new char[max];
//				int offset = -1;
//				ThreadBean tThreadBean = new ThreadBean();
//				tThreadBean.setTaskId(mTaskId);
//				tThreadBean.setThreadId(mThreadId);
//				while ((block - downLength) > 1 && !finish) {
//					if (AppStoreConfig.DOWNLOADDEBUG) {
//						Log.d(TAG, "=====TaskId=" + mTaskId + "===thread="
//								+ Thread.currentThread().getId() + "===block="
//								+ block + "-===downing=" + downLength
//								+ "===offset=" + offset + "======finish="
//								+ finish + "======isStop=" + isStop);
//					}
//					if (isStop) {
//						break;
//					}
//					try {
//						offset = inStream.read(buffer, 0, max);
////						offset = bufferReader.read(buffer, 0, max);
//					} catch (IOException e) {
//						e.printStackTrace();
//						Log.e(TAG, "=====inStream.read(buffer, 0, max) catch IOException"+e.toString());
//					}
//
//					if (null != inStream && offset != -1) {
//						try {
//							Thread.sleep(SLEEP_TIME);
//						} catch (InterruptedException e) {
//							e.printStackTrace();
//							Log.e(TAG, "=====Thread.sleep(SLEEP_TIME) catch InterruptedException"+e.toString());
//						}
//						try {
//							saveFile.write(buffer, 0, offset);
//						} catch (IOException e) {
//							e.printStackTrace();
//							Log.e(TAG, "=====saveFile.write(buffer, 0, offset) catch IOException"+e.toString());
//						}
//						downLength += offset;
//						startPos = block
//								* (mThreadId - mTaskId * Contants.RATE)
//								+ downLength;
//						tThreadBean.setPosition(startPos);
//						tThreadBean.setDownLength(downLength);
//						mDBUtils.updateProgress(tThreadBean, offset);
//						int spare = block - downLength;
//						if (spare < max){
//							max = (int) spare;
//						}
//					}
////					else if((block - downLength) > 1){
////						try {
////							http = (HttpURLConnection) downUrl.openConnection();
////						} catch (IOException e) {
////							e.printStackTrace();
////							Log.e(TAG, "=====openConnection catch ioexception"+e.toString());
////						}
////						if (null != http) {
////							try {
////								http.setRequestMethod("GET");
////							} catch (ProtocolException e) {
////								e.printStackTrace();
////								Log.e(TAG, "=====setRequestMethod catch ProtocolException"+e.toString());
////							}
////							http.setRequestProperty(
////									"Accept",
////									"image/gif, image/jpeg, image/pjpeg, "
////											+ "image/pjpeg, application/x-shockwave-flash, "
////											+ "application/xaml+xml, application/vnd.ms-xpsdocument, "
////											+ "application/x-ms-xbap, application/x-ms-application, "
////											+ "application/vnd.ms-excel, application/vnd.ms-powerpoint, "
////											+ "application/msword, */*");
////
////							http.setRequestProperty("Accept-Language", "zh-CN");
////							http.setRequestProperty("Referer", downUrl.toString());
////							http.setRequestProperty("Charset", "UTF-8");
////							if(Utils.isBreakpointCon(downUrl)){
////								http.setRequestProperty("Range", "bytes=" + this.startPos +"-");
////							}
//////							http.setRequestProperty("Range", "bytes=" + this.startPos +"-");
////							http.setRequestProperty(
////									"User-Agent",
////									"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; "
////											+ "Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; "
////											+ ".NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; "
////											+ ".NET CLR 3.5.30729)");
////
////							http.setRequestProperty("Connection", "Keep-Alive");
////							try {
////								inStream = http.getInputStream();
////							} catch (IOException e) {
////								e.printStackTrace();
////								Log.e(TAG, "=====getInputStream catch IOException"+e.toString());
////							}
////					}
////						int spare = block - downLength;
////						if (spare < max){
////							max = (int) spare;
////						}
////						continue;
////				}
//				}
//				try {
//					saveFile.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//					Log.e(TAG, "=====saveFile.close() catch IOException"+e.toString());
//				}
//				try {
//					inStream.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//					Log.e(TAG, "=====inStream.close() catch IOException"+e.toString());
//				}
//				this.finish = true;
//				if (AppStoreConfig.DOWNLOADDEBUG) {
//					Log.d(TAG, "====download finished===========TaskId="
//							+ mTaskId + "==============thread="
//							+ mThreadId
//							+ "-downing======:" + downLength
//							+ "================offset:" + offset
//							+ "======finish=" + finish);
//				}
//			}
//		}
//	}

	public boolean isFinish() {
		return finish;

	}

	public long getDownLength() {
		return downLength;
	}
}
