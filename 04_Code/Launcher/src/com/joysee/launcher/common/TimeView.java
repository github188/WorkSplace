package com.joysee.launcher.common;

import java.util.Calendar;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.joysee.launcher.activity.R;
import com.joysee.launcher.utils.LauncherLog;

public class TimeView extends FrameLayout {

	private static final String TAG = "com.joysee.launcher.common.TimeView";

	private String mDayOfWeek1;
	private String mDayOfWeek2;
	private String mDayOfWeek3;
	private String mDayOfWeek4;
	private String mDayOfWeek5;
	private String mDayOfWeek6;
	private String mDayOfWeek7;

	private TextView mDay;
	private TextView mTime;
	private boolean mStop;
	private Context mContext;
	private Resources mRes;
	private Time mCurrentTime;
	private boolean mIs24Format = true;
	private LooperThread mClockThread;
	
	private String am;
	private String pm;

	private Handler mHandler = new Handler() {

		public void handleMessage(Message message) {
			LauncherLog.log_D(TAG, "handleMessage");
			if (message.obj instanceof Time) {
				Time time = (Time) message.obj;
				if (time.getYear() != mCurrentTime.getYear() || time.getMonth() != mCurrentTime.getMonth()
						|| time.getDayOfMonth() != mCurrentTime.getDayOfMonth()
						|| time.getHour() != mCurrentTime.getHour() || time.getMinute() != mCurrentTime.getMinute()
						|| time.getDayOfWeek() != mCurrentTime.getDayOfWeek()
						|| time.getSecond() != mCurrentTime.getSecond()) {
					mCurrentTime.setTime(time.getYear(), time.getMonth(), time.getDayOfMonth(), time.getHour(),
							time.getMinute(), time.getSecond(), time.getDayOfWeek());
					if (!mStop) {
						final int year = mCurrentTime.getYear();
						final int month = mCurrentTime.getMonth();
						final int dayOfMonth = mCurrentTime.getDayOfMonth();
						final int hour = mCurrentTime.getHour();
						final int minute = mCurrentTime.getMinute();
						final int dayOfWeek = mCurrentTime.getDayOfWeek();
						LauncherLog.log_D(TAG, "update time");
						StringBuilder sday = new StringBuilder();

						sday.append(year);
						sday.append(".");
						sday.append(getMonthString(month));
						sday.append(".");
						sday.append(getDayOfMonthString(dayOfMonth)).append(" ");
						sday.append(getDayOfWeekString(dayOfWeek));

						StringBuilder stime = new StringBuilder();
						stime.append(getFormatedHour(hour));
						stime.append(":");
						stime.append(getFormatedMinute(minute));
						stime.append(":");
						stime.append(getFormatedSecond(mCurrentTime.getSecond()));

						mDay.setText(sday);
						mTime.setText(stime);
					}
				}
			}
		}
	};
	
	private String getFormatedSecond(int second){
		if(second < 10)
			return String.valueOf("0" + second);
		else
			return String.valueOf(second);
	}
	
	private String getFormatedMinute(int minute){
		if(minute < 10)
			return String.valueOf("0" + minute);
		else
			return String.valueOf(minute);
	}

	private String getFormatedHour(int hour) {
		
		if (mIs24Format) {
			if(hour < 10)
				return String.valueOf("0" + hour);
			return String.valueOf(hour);
		} else {
			if (hour <= 12) {
				StringBuilder sb = new StringBuilder();
				sb.append(am);
				sb.append("   ");
				if(hour < 10)
					sb.append("0");
				sb.append(hour);
				return sb.toString();
			} else {
				hour = hour - 12;
				StringBuilder sb = new StringBuilder();
				sb.append(pm);
				sb.append("   ");
				if(hour < 10)
					sb.append("0");
				sb.append(hour);
				return sb.toString(); 
			}
		}
	}

	private String getMonthString(int month) {
		if (month < 1 || month > 12)
			return null;
		if (month >= 1 && month < 10) {
			return new String("0" + month);
		} else {
			return String.valueOf(month);
		}
	}

	private String getDayOfMonthString(int day) {
		if (day < 1 || day > 31)
			return null;
		if (day >= 1 && day < 10) {
			return new String("0" + day);
		} else {
			return String.valueOf(day);
		}
	}

	private String getDayOfWeekString(int day) {
		switch (day) {
		case 1:
			return mDayOfWeek1;
		case 2:
			return mDayOfWeek2;
		case 3:
			return mDayOfWeek3;
		case 4:
			return mDayOfWeek4;
		case 5:
			return mDayOfWeek5;
		case 6:
			return mDayOfWeek6;
		case 7:
			return mDayOfWeek7;
		}
		return null;
	}

	protected void onDetachedFromWindow() {
		stopPreview();
	};
	
	public TimeView(Context context) {
		this(context, null);
	}

	public TimeView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public TimeView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		mRes = mContext.getResources();
	}

	protected void onFinishInflate() {
		LauncherLog.log_D(TAG, "onFinishInflate");
		super.onFinishInflate();

		mDay = (TextView) findViewById(R.id.timeview_day_text);
		mTime = (TextView) findViewById(R.id.timeview_time_text);
		am = mRes.getString(R.string.timeview_am);
		pm = mRes.getString(R.string.timeview_pm);
		init();
	}

	private void init() {
		mDayOfWeek1 = mRes.getString(R.string.timeview_dayofweek_1);
		mDayOfWeek2 = mRes.getString(R.string.timeview_dayofweek_2);
		mDayOfWeek3 = mRes.getString(R.string.timeview_dayofweek_3);
		mDayOfWeek4 = mRes.getString(R.string.timeview_dayofweek_4);
		mDayOfWeek5 = mRes.getString(R.string.timeview_dayofweek_5);
		mDayOfWeek6 = mRes.getString(R.string.timeview_dayofweek_6);
		mDayOfWeek7 = mRes.getString(R.string.timeview_dayofweek_7);

		if (mContext != null) {
			
			mIs24Format = android.text.format.DateFormat.is24HourFormat(getContext());
			
			LauncherLog.log_D(TAG, "strTimeFormat is 24hour    " + mIs24Format);
		}

		mCurrentTime = new Time();
		mClockThread = new LooperThread();
		mClockThread.start();
	}

	public void startPreview(){
		LauncherLog.log_D(TAG, "--------------startPreView()------------mStop---"  + mStop);
		if(mStop){
			init();
			mStop = false;
		}
	}
	
	public void stopPreview() {
		if (mClockThread != null)
			mClockThread.stopLooper();
		mStop = true;
	}

	private class LooperThread extends Thread {

		private boolean run;

		public void run() {
			LauncherLog.log_D(TAG, "run   begin ");
			try {
				boolean flag;
				do {
					Thread.sleep(1000L);

					LauncherLog.log_D(TAG, "while   begin ");
					long l = System.currentTimeMillis();
					Calendar calendar = Calendar.getInstance();
					calendar.setTimeInMillis(l);
					final int year = calendar.get(Calendar.YEAR);
					final int month = calendar.get(Calendar.MONTH) + 1;
					final int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
					final int hour = calendar.get(Calendar.HOUR_OF_DAY);
					final int minute = calendar.get(Calendar.MINUTE);
					final int second = calendar.get(Calendar.SECOND);
					final int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
					Message message = new Message();
					message.what = 264;
					message.obj = new Time(year, month, dayOfMonth, hour, minute, second, dayOfWeek);
					mHandler.sendMessage(message);
					if (interrupted())
						break;
					flag = run;
				} while (flag);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		public void stopLooper() {
			run = false;
		}

		private LooperThread() {
			super();
			run = true;
		}

	}
}

class Time {
	private int year;
	private int month;
	private int dayOfMonth;
	private int hour;
	private int minute;
	private int second;
	private int dayOfWeek;

	public Time() {
	}

	public Time(int year, int month, int dayOfMonth, int hour, int minute, int second, int dayOfWeek) {
		this.year = year;
		this.month = month;
		this.dayOfMonth = dayOfMonth;
		this.hour = hour;
		this.minute = minute;
		this.second = second;
		this.dayOfWeek = dayOfWeek;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		if (year >= 1970 && year <= 2038)
			this.year = year;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		if (month >= 1 && month <= 12)
			this.month = month;
	}

	public int getDayOfMonth() {
		return dayOfMonth;
	}

	public void setDayOfMonth(int day) {
		if (day >= 1 && day <= 31)
			this.dayOfMonth = day;
	}

	public int getHour() {
		return hour;
	}

	public void setHour(int hour) {
		if (hour >= 0 && hour < 24)
			this.hour = hour;
	}

	public int getMinute() {
		return minute;
	}

	public void setMinute(int minute) {
		if (minute >= 0 && minute < 60)
			this.minute = minute;
	}

	public int getSecond() {
		return second;
	}

	public void setSecond(int second) {
		if (second >= 0 && second < 60)
			this.second = second;
	}

	public int getDayOfWeek() {
		return dayOfWeek;
	}

	public void setDayOfWeek(int dayOfWeek) {
		if (dayOfWeek >= 1 && dayOfWeek <= 7)
			this.dayOfWeek = dayOfWeek;
	}

	public void setTime(int year, int month, int dayOfMonth, int hour, int minute, int second, int dayOfWeek) {
		this.year = year;
		this.month = month;
		this.dayOfMonth = dayOfMonth;
		this.hour = hour;
		this.minute = minute;
		this.second = second;
		this.dayOfWeek = dayOfWeek;
	}
}
