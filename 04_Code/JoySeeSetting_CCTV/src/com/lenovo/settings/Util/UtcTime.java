package com.lenovo.settings.Util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import android.util.Log;


public class UtcTime {

	public static final String TAG = "UtcTime";
	public static final String UTC_TIME_PATTEN = "yyyy-MM-dd'T'HH:mm:ssZ";
	public static final String LOCAL_TIME_PATTEN = "yyyy-MM-dd HH:mm";
	public static final String DATE_PATTEN = "yyyy-MM-dd";

	public static int Compare_Date(String date1, String date2, String patten) {
		SimpleDateFormat df = new SimpleDateFormat(patten);
		try {
			Date dt1 = df.parse(date1);
			Date dt2 = df.parse(date2);
			if (dt1.getTime() > dt2.getTime()) {
				return -1;
			} else if (dt1.getTime() < dt2.getTime()) {
				return 1;
			} else {
				return 0;
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return 0;
	}
	
	public static String Utc2Local(String utcTime, String utcTimePatten,
			   String localTimePatten) {
		  SimpleDateFormat utcFormater = new SimpleDateFormat(utcTimePatten);
		  utcFormater.setTimeZone(TimeZone.getTimeZone("UTC"));
		  Date gpsUTCDate = null;
		  try {
		   gpsUTCDate = utcFormater.parse(utcTime);
		  } catch (ParseException e) {
		   e.printStackTrace();
		  }
		  SimpleDateFormat localFormater = new SimpleDateFormat(localTimePatten);
		  localFormater.setTimeZone(TimeZone.getDefault());
		  String localTime = localFormater.format(gpsUTCDate.getTime());
		  return localTime;
	}
	
	public static Boolean isDiaplayDate(String date_str, String date_patten){
		SimpleDateFormat formater = new SimpleDateFormat(date_patten);
		Date d=new Date();
		try {
			Date date = formater.parse(date_str);
			int ret = Compare_Date(formater.format(date),
								formater.format(new Date(d.getTime() - 2 * 24 * 60 * 60 * 1000)),
								date_patten);
			if(ret == 1){
				return false;
			}else{
				return true;
			}
		} catch (ParseException e) {
			
			e.printStackTrace();
		}
		
		return false;
	}
}
