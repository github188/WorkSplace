package com.joysee.adtv.common;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DateFormatUtil {
    
    public static final String Format_1="HH:mm";
    public static final String Format_2="HH:mm:ss";
    
    public static String getWeekFromInt(int dayOfWeek){
        String week = "";
        switch (dayOfWeek) {
            case 1:
                week = "日";
                break;
            case 2:
                week = "一";
                break;
            case 3:
                week = "二";
                break;
            case 4:
                week = "三";
                break;
            case 5:
                week = "四";
                break;
            case 6:
                week = "五";
                break;
            case 7:
                week = "六";
                break;
        }
        return week;
    }
    
    public static String getDate(Date date){
        SimpleDateFormat  format = new SimpleDateFormat("yyyy年MM月dd日");
        return format.format(date);
    }
    
    public static String getDateFromMillis(long time){
        Date date = new Date();
        date.setTime(time);
        SimpleDateFormat  format = new SimpleDateFormat("MM月dd日");
        return format.format(date);
    }
    
    public static String getTimeFromMillis(long time){
    	if(time == 0){
    		return null;
    	}
        Date date = new Date();
        date.setTime(time);
        SimpleDateFormat  format = new SimpleDateFormat(Format_1);
        return format.format(date);
    }
    
    public static String getShowTimeFromMillis(long time){
    	 Date date = new Date();
         date.setTime(time);
         SimpleDateFormat  format = new SimpleDateFormat(Format_2);
         return format.format(date);
    }
    
    public static String getTimeFromLong(long time){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        return ((calendar.get(Calendar.HOUR_OF_DAY)<10 ? "0"+calendar.get(Calendar.HOUR_OF_DAY):calendar.get(Calendar.HOUR_OF_DAY)))+":"+((calendar.get(Calendar.MINUTE)<10 ? "0"+calendar.get(Calendar.MINUTE):calendar.get(Calendar.MINUTE)));
    }
    
    public static long getNextDate(Calendar now) {

        Calendar nextDay = Calendar.getInstance();
        nextDay.set(now.get(Calendar.YEAR),now.get(Calendar.MONTH) , now.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        nextDay.add(Calendar.DAY_OF_YEAR, 1);
        return nextDay.getTimeInMillis();

    }
    /**
     * @param date
     * @return yyyy-MM-dd HH:mm 
     */
    public static String getDateFromMillis(Date date){
        SimpleDateFormat  format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return format.format(date);
    }
    
    /**
     * @param date
     * @return yyyy-MM-dd HH:mm 
     */
    public static String getStringFromMillis(long date){
        SimpleDateFormat  format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return format.format(date);
    }
    
    /**
     * 根据日期获得所在周的日期 
     * @param mdate
     * @return
     */
    public static List<Date> dateToWeek(Date mdate) {
        int b = mdate.getDay();
        Date fdate;
        List<Date> list = new ArrayList<Date>();
        Long fTime = mdate.getTime() - b * 24 * 3600000;
        for (int a = 1; a <= 7; a++) {
            fdate = new Date();
            fdate.setTime(fTime + (a * 24 * 3600000));
            list.add(a-1, fdate);
        }
        return list;
    }
    
}
