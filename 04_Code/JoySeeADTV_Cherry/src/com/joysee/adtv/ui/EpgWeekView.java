package com.joysee.adtv.ui;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.joysee.adtv.R;
import com.joysee.adtv.common.DateFormatUtil;
import com.joysee.adtv.common.DvbLog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class EpgWeekView extends RelativeLayout implements View.OnClickListener,View.OnFocusChangeListener{
    
    private static final DvbLog log = new DvbLog(
            "com.joysee.adtv.ui.EpgWeekView", DvbLog.DebugType.D);
    
    private Context mContext = null;
    private EpgChannelFrame channelView=null;
    private RelativeLayout weeks[],current,today;
    private TextView mTime;
    private View focusView=null; 
    private static final int UPDATE_TIME=1;
    public static long time;
    private EpgGuideWindow epgGuideWindow;
    Calendar selectCalendar=Calendar.getInstance();
    public static long todaytime,beginTime,endTime;
    public static long oneTayTime=86399000;//24*60*60*1000-1000
    private int txtSize_focus=(int)getResources().getDimension(R.dimen.epg_data_txtSize_focus);
    private int txtSize_normal=(int)getResources().getDimension(R.dimen.epg_data_txtSize_normal);
    public static boolean isToday=true;

    public EpgWeekView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage (Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATE_TIME:
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm"); 
                    EpgGuideWindow.refreshUtcTime=true;
                    Date currentDate = new Date(epgGuideWindow.getUtcTime()+EpgGuideWindow.TimeOffset);
                    mTime.setText(sdf.format(currentDate));
                    break;
                
                default:
                    break;
            }
        }
    };
    
    public void init(){
        weeks=new RelativeLayout[7];
        weeks[0]=(RelativeLayout)findViewById(R.id.sun);
        weeks[0].setTag(7);
        weeks[1]=(RelativeLayout)findViewById(R.id.mon);
        weeks[1].setTag(1);
        weeks[2]=(RelativeLayout)findViewById(R.id.tue);
        weeks[2].setTag(2);
        weeks[3]=(RelativeLayout)findViewById(R.id.wen);
        weeks[3].setTag(3);
        weeks[4]=(RelativeLayout)findViewById(R.id.thu);
        weeks[4].setTag(4);
        weeks[5]=(RelativeLayout)findViewById(R.id.fri);
        weeks[5].setTag(5);
        weeks[6]=(RelativeLayout)findViewById(R.id.sat);
        weeks[6].setTag(6);
        for(int i=0;i<7;i++){
            weeks[i].setOnFocusChangeListener(this);
            weeks[i].setOnClickListener(this);
        }
        mTime=(TextView)findViewById(R.id.time);
        
        Calendar calendar = Calendar.getInstance();
        EpgGuideWindow.refreshUtcTime=true;
        calendar.setTimeInMillis(epgGuideWindow.getUtcTime());
//        calendar.setTime(new Date());
        selectCalendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        todaytime=selectCalendar.getTimeInMillis()-EpgGuideWindow.TimeOffset;
        beginTime=todaytime;
        endTime=beginTime+oneTayTime;
        log.D("--------beginTime="+beginTime+";----"+DateFormatUtil.getDateFromMillis(beginTime));
        log.D("--------"+selectCalendar.getTimeInMillis()+";"+DateFormatUtil.getDateFromMillis(selectCalendar.getTime())+";endTime="+DateFormatUtil.getStringFromMillis(endTime));
//        long todayTim=selectCalendar.getTimeInMillis();
        int dayIndex = calendar.get(Calendar.DAY_OF_WEEK);
        if (dayIndex>=1&&dayIndex<=7) {
            current=weeks[dayIndex-1];
            current.setBackgroundResource(R.drawable.epg_menu_bg);
            current.findViewById(R.id.week_big).setVisibility(View.VISIBLE);
            current.findViewById(R.id.week_date).setVisibility(View.VISIBLE);
            current.findViewById(R.id.week).setVisibility(View.GONE);
            today=weeks[dayIndex-1];
        }
        SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日"); 
        Date currentDate = new Date(epgGuideWindow.getUtcTime()+EpgGuideWindow.TimeOffset);
        List<Date> days = DateFormatUtil.dateToWeek(currentDate);
        System.out.println("今天的日期: " + sdf.format(currentDate));
        String today=sdf.format(currentDate);
        for(int i=0;i<days.size();i++){
            if(i==days.size()-1){
                if(sdf.format(days.get(i)).equals(today)){
                    ((TextView)weeks[0].findViewById(R.id.week_big)).setText("今天");
                    ((TextView)weeks[0].findViewById(R.id.week_date)).setText(sdf.format(days.get(i)));
                }else{
                    ((TextView)weeks[0].findViewById(R.id.week_date)).setText(sdf.format(days.get(i)));
                }
            }else{
                if(sdf.format(days.get(i)).equals(today)){
                    ((TextView)weeks[i+1].findViewById(R.id.week_big)).setText("今天");
                    ((TextView)weeks[i+1].findViewById(R.id.week_date)).setText(sdf.format(days.get(i)));
                }else{
                    ((TextView)weeks[i+1].findViewById(R.id.week_date)).setText(sdf.format(days.get(i)));
                }
            }
        }
        for (Date date : days) {
            System.out.println(sdf.format(date));
        }
        log.D("--------------current="+(dayIndex-1));
        new TimeThread().start();
    }
    
    public void setChannelView(EpgChannelFrame all){
        channelView=all;
    }
    
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        log.D("-------------keyCode="+event.getKeyCode()+";action="+event.getAction());
        if(event.getAction()==KeyEvent.ACTION_DOWN){
            switch(event.getKeyCode()){
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    channelView.onfocusView();
                    return true;
                case KeyEvent.KEYCODE_DPAD_UP:
                    return true;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    long tim=System.currentTimeMillis();
                    if((tim-time)<300){
                        return true;
                    }
                    time=tim;
                    if(focusView!=null&&focusView.getId()==R.id.mon){
                        weeks[0].requestFocus();
                        return true;
                    }
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    long tim2=System.currentTimeMillis();
                    if((tim2-time)<300){
                        return true;
                    }
                    time=tim2;
                    if(focusView!=null&&focusView.getId()==R.id.sun){
                        weeks[1].requestFocus();
                        return true;
                    }
                    break;
                case KeyEvent.KEYCODE_HOME:
                case KeyEvent.KEYCODE_ESCAPE:
                case KeyEvent.KEYCODE_BACK:
                    epgGuideWindow.dismiss();
                    return true;
            	}
            }else if(event.getKeyCode() ==  KeyEvent.KEYCODE_MENU && event.getAction() == KeyEvent.ACTION_UP){
            	 epgGuideWindow.showMenu();
                 return true;
            }
            
           
        return super.dispatchKeyEvent(event);
    }
    
    public void setFocusView(){
        current.requestFocus();
    }
    
    public class TimeThread extends Thread {
        @Override
        public void run () {
            do {
                try {
                    Message msg = new Message();
                    msg.what = UPDATE_TIME;
                    mHandler.sendMessage(msg);
                    Thread.sleep(60000);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while(true);
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.mon:
            case R.id.tue:
            case R.id.wen:
            case R.id.thu:
            case R.id.fri:
            case R.id.sat:
            case R.id.sun:
                log.D("------onclick  v.getTag="+v.getTag().toString()+";current.getTag="+current.getTag());
                if(v.getTag().toString().equals(current.getTag().toString())){
                    return;
                }
                int space=(Integer)v.getTag()-(Integer)today.getTag();
                if(space==0){
                    isToday=true;
                }else{
                    isToday=false;
                }
                log.D("--------------space="+space);
                selectCalendar.setTimeInMillis(todaytime);
                selectCalendar.add(Calendar.DAY_OF_YEAR, space);
                beginTime=selectCalendar.getTimeInMillis();
                endTime=beginTime+oneTayTime;
                log.D("--------"+selectCalendar.getTimeInMillis()+";"+DateFormatUtil.getDateFromMillis(selectCalendar.getTime())+";endtime="+DateFormatUtil.getStringFromMillis(endTime));
                current.setBackgroundDrawable(null);
                current.findViewById(R.id.week_big).setVisibility(View.GONE);
                current.findViewById(R.id.week_date).setVisibility(View.GONE);
                current.findViewById(R.id.week).setVisibility(View.VISIBLE);
                ((TextView)current.findViewById(R.id.week)).setTextSize(txtSize_normal);
                ((TextView)current.findViewById(R.id.week)).setTextColor(getResources().getColor(R.color.grey_txt));
                current=(RelativeLayout)v;
                v.setBackgroundResource(R.drawable.epg_menu_select_bg);
                current.findViewById(R.id.week_big).setVisibility(View.VISIBLE);
                current.findViewById(R.id.week_date).setVisibility(View.VISIBLE);
                ((TextView)current.findViewById(R.id.week_big)).setTextColor(getResources().getColor(R.color.white_txt));
                ((TextView)current.findViewById(R.id.week_date)).setTextColor(getResources().getColor(R.color.white_txt));
                current.findViewById(R.id.week).setVisibility(View.GONE);
                channelView.refreshData();
                
                break;
        }
    }
    
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        switch(v.getId()){
            case R.id.mon:
            case R.id.tue:
            case R.id.wen:
            case R.id.thu:
            case R.id.fri:
            case R.id.sat:
            case R.id.sun:
                if(hasFocus){
                    focusView=v;
                    if(v==current){
                        v.setBackgroundResource(R.drawable.epg_menu_select_bg);
                        ((TextView)current.findViewById(R.id.week_big)).setTextColor(getResources().getColor(R.color.white_txt));
                        ((TextView)current.findViewById(R.id.week_date)).setTextColor(getResources().getColor(R.color.white_txt));
                    }else{
                        ((TextView)v.findViewById(R.id.week)).setTextSize(txtSize_focus);
                        ((TextView)v.findViewById(R.id.week)).setTextColor(getResources().getColor(R.color.green_txt));
                    }
                }else{
                    if(v==current){
                        v.setBackgroundResource(R.drawable.epg_menu_bg);
                        ((TextView)current.findViewById(R.id.week_big)).setTextColor(getResources().getColor(R.color.green_txt));
                        ((TextView)current.findViewById(R.id.week_date)).setTextColor(getResources().getColor(R.color.green_txt));
                    }else{
                        ((TextView)v.findViewById(R.id.week)).setTextSize(txtSize_normal);
                        ((TextView)v.findViewById(R.id.week)).setTextColor(getResources().getColor(R.color.grey_txt));
                    }
                }
                break;
        }
    }
    
    public void setGuideWindow(EpgGuideWindow win){
        this.epgGuideWindow=win;
    }

}
