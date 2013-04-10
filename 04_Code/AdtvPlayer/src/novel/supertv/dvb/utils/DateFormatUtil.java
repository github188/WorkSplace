package novel.supertv.dvb.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateFormatUtil {
    
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
        Date date = new Date();
        date.setTime(time);
        SimpleDateFormat  format = new SimpleDateFormat("HH:mm");
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
}
