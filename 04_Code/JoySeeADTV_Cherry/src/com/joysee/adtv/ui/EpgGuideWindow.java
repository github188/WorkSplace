
package com.joysee.adtv.ui;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.joysee.adtv.R;
import com.joysee.adtv.common.DateFormatUtil;
import com.joysee.adtv.common.DvbLog;
import com.joysee.adtv.common.DvbMessage;
import com.joysee.adtv.common.DefaultParameter.ViewMessage;
import com.joysee.adtv.controller.ViewController;
import com.joysee.adtv.db.Channel;
import com.joysee.adtv.doc.ADTVEpgDoc;
import com.joysee.adtv.doc.ADTVResource;
import com.joysee.adtv.logic.SettingManager;
import com.joysee.adtv.logic.bean.NETEventInfo;
import com.joysee.adtv.logic.bean.Program;
import com.joysee.adtv.server.ADTVService;

import java.util.Date;
import java.util.TimeZone;

public class EpgGuideWindow extends BasicWindow implements IDvbBaseView {

    private static final DvbLog log = new DvbLog(
            "com.joysee.adtv.ui.EPGGuideWindow", DvbLog.DebugType.D);

    public static final int RES_Bitmap=1;
    public static final int RES_Actor=2;//主演或播出
    public static final int RES_Type=3;//类型或主持人
    public static final int RES_About=4;//简介
    public static final int RES_Nibble=5;//
    public static final int RES_ProgramList=6;//
    
    public static final int Status_End=1;
    public static final int Status_Ing=2;
    public static final int Status_Future=3;
    public static final int Status_Error=4;
    
    public static final int Type_Move=1;
    public static final int Type_Other=2;
    
    protected boolean isShow;
    private Activity mActivity;
    private LayoutInflater mInflater;
    private EpgWeekView epgWeek = null;
    private EpgChannelFrame epgChannel = null;
    private LinearLayout epgGuideLayout=null;
    private PopupWindow epgGuideWindow;
    private PopupWindow mAlertPopupWindow;
    private ViewController mViewController;
    private SettingManager mSettingManager;
    private Dialog mAlertDialog;
    public static double mTimeZone;
    public static final int MSG_Close_Window = 1;
    public static final int MSG_Get_Detail = 2;
    public static final int MSG_Get_ProgramList = 3;
    public static long TimeOffset;
    
    private TextView actor_con,type_con,about_con,actor,type;
    private ImageView poster;
    private int programId=0;
    ADTVEpgDoc doc;
    
    
    private static final int RESERVE_STATUS_OFF = 0;
    private static final int RESERVE_STATUS_ON = 1;
    
    public EpgGuideWindow(Activity activity){
        mActivity=activity;
        mInflater=mActivity.getLayoutInflater();
        mSettingManager = SettingManager.getSettingManager();
        mTimeZone = (double)(TimeZone.getDefault().getRawOffset())/1000/3600;
        Log.d("songwenxuan","time zone = " + mTimeZone);
        TimeOffset=(long)((8-mTimeZone)*3600*1000);
        doc=new ADTVEpgDoc();
        setDoc(doc);
    }
    
    public Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_Close_Window:
                    if(mAlertPopupWindow != null && mAlertPopupWindow.isShowing()){
                        mAlertPopupWindow.dismiss();
                    }
                    break;
                case MSG_Get_Detail:
                    getDetail(msg.arg1);
                    break;
                case MSG_Get_ProgramList:
                    doc.getProgramidList(msg.arg1, EpgWeekView.beginTime, EpgWeekView.endTime);
                    break;
            }
        }
    };
    
    public void getDetail(int programId){
        log.D("**************getDetail*********   programId="+programId);
        clearProgramInfo(programId);
        if(programId>0)
            doc.getProgramDetail(programId);
    }

    @Override
    public void processMessage(Object sender, DvbMessage msg) {
        switch (msg.what) {
            case ViewMessage.SHOW_PROGRAM_GUIDE:
                mViewController = (ViewController) sender;
                checkOrderList();
                showProgramGuide();
                break;
            case ViewMessage.SHOW_PROGRAM_RESERVE_ALERT:
                dismiss();
                break;
        }
    }

    public void showProgramGuide() {
//        if(epgGuideWindow!=null&&epgGuideWindow.isShowing()){
//            epgGuideWindow.dismiss();
//            return;
//        }
        if(epgGuideLayout!=null){
            epgChannel.removeAllViews();
            epgWeek.removeAllViews();
            epgGuideLayout=null;
        }
        epgGuideLayout=(LinearLayout) mInflater.inflate(R.layout.epg_guide, null);
        epgWeek=(EpgWeekView)epgGuideLayout.findViewById(R.id.week_day);
        epgChannel=(EpgChannelFrame)epgGuideLayout.findViewById(R.id.channel);
        actor_con=(TextView)epgGuideLayout.findViewById(R.id.actor_con);
        actor=(TextView)epgGuideLayout.findViewById(R.id.actor);
        type_con=(TextView)epgGuideLayout.findViewById(R.id.type_con);
        type=(TextView)epgGuideLayout.findViewById(R.id.type);
        about_con=(TextView)epgGuideLayout.findViewById(R.id.about_con);
        poster=(ImageView)epgGuideLayout.findViewById(R.id.poster);
        epgWeek.setChannelView(epgChannel);
        epgChannel.doc=doc;
        epgChannel.setEpgWeekView(epgWeek);
        epgChannel.setViewController(mViewController);
        epgChannel.setGuideWindow(this);
        epgWeek.setGuideWindow(this);
        epgWeek.init();
        epgChannel.init();
        epgChannel.onfocusView();
        if(epgGuideWindow == null)
            epgGuideWindow = new PopupWindow();
        epgGuideWindow.setContentView(epgGuideLayout);
        epgGuideWindow.setWidth((int)mActivity.getResources().getDimension(R.dimen.program_guide_popupwindow_width));
        epgGuideWindow.setHeight((int)mActivity.getResources().getDimension(R.dimen.program_guide_popupwindow_height));
        epgGuideWindow.setFocusable(true);
        epgGuideWindow.showAtLocation(mActivity.getWindow().getDecorView(), Gravity.CENTER, 0, 0);
        setIsShow(true);
    }
    
    /**
     * 检查缓存中预约是否过期
     */
    public void checkOrderList(){
        refreshUtcTime=true;
        long utc=getUtcTime()/1000;
        ADTVService.getService().getEpg().checkReservesList((int)utc);
    }
    
    protected void dismiss() {
        if(epgGuideWindow!=null && epgGuideWindow.isShowing()){
            setIsShow(false);
            clearDialog();
            epgGuideWindow.dismiss();
            mViewController.exitEpgGuide();
        }
    }
    
    public void setIsShow(boolean flag) {
        this.isShow = flag;
    }
    
    public void clearProgramInfo(int programId){
        this.programId=programId;
        actor_con.setText("");
        type_con.setText("");
        about_con.setText("");
        poster.setImageResource(R.drawable.program_poster);
    }
    
    public void clearDialog(){
        if(mAlertPopupWindow != null && mAlertPopupWindow.isShowing()){
            mAlertPopupWindow.dismiss();
        }
    }
    
    public void showMenu(){
        dismiss();
        if(mViewController!=null)
            mViewController.showMainMenu();
    }
    
    public int programStatus(NETEventInfo info){
        if(null==info){
            log.D("----------------------------error---info is null");
            return Status_Error;
        }
        long startTime=info.getBegintime();
        long endTime=info.getBegintime()+info.getDuration();
        long tsTime=getUtcTime();
        long utcTime =tsTime + TimeOffset;
        if(endTime <= utcTime){//过期节目
            return Status_End;
        }else if(startTime < utcTime && endTime >= utcTime ){//正在播放
            return Status_Ing;
        }else{//可以预约
            return Status_Future;
        }
    }
    
    public void showAlertDialog(final NETEventInfo info,final View program){
        refreshUtcTime=true;
        if(info==null||info.getProgramId()<=0||info.getEname()==null||info.getEname().trim().equals("")){
            showAlertPop(R.string.program_reserve_no_program_text,false);
            return;
        }
        Cursor tProgramReserveCursor = mActivity.getContentResolver().query(
                Channel.URI.TABLE_RESERVES,null, null, null, null);
        int tProgramReserveCount = tProgramReserveCursor.getCount();
        if(tProgramReserveCount >= 40){
            showAlertPop(R.string.program_reserve_max_text,false);
            mAlertDialog.dismiss();
            tProgramReserveCursor.close();
            return;
        }
        long startTime=info.getBegintime();
        long endTime=info.getBegintime()+info.getDuration();
        long tsTime=getUtcTime();
        long utcTime =tsTime + TimeOffset;
        log.D("-------startTime="+startTime+";endTime="+endTime+";utcTime="+utcTime);
        log.D("-------startTime="+DateFormatUtil.getStringFromMillis(startTime)+";endTime="+DateFormatUtil.getStringFromMillis(endTime)+";utcTime="+DateFormatUtil.getStringFromMillis(utcTime));
        int status=programStatus(info);
        log.D("----------------status="+status);
        if(status==Status_End){//过期节目
            showAlertPop(R.string.program_reserve_time_out,false);
        }else if(status==Status_Ing){//正在播放
            dismiss();
            mViewController.switchChannelFromNum(info.getLogicNumer());
        }else{//可以预约
            Cursor query = mActivity.getContentResolver().query(Channel.URI.TABLE_RESERVES, null, "startTime=? and serviceId=?",new String[]{""+((startTime-TimeOffset)/1000),""+info.getServiceId()}, null);
            View epgNotifyView=null;
            epgNotifyView = mActivity.getLayoutInflater().inflate(R.layout.order_dialog_layout, null);
            Button confirmBtn = (Button) epgNotifyView.findViewById(R.id.epg_alert_confirm_btn);
            Button cancleBtn = (Button) epgNotifyView.findViewById(R.id.epg_alert_cancle_btn);
            log.D("---------query.getCount="+query.getCount());
            if(query.getCount() == 0){//没有预约
                confirmBtn.setText(mActivity.getResources().getString(R.string.program_reverse_alert));
                confirmBtn.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        log.D("----------serviceId="+info.getServiceId()+";channelName="+info.getChannelName()+";programName="+info.getEname());
                        
                        long startTime=info.getBegintime();
                        long endTime=info.getBegintime()+info.getDuration();
                        String programName = info.getEname();
                        ContentValues values = new ContentValues();
                        values.put(Channel.TableReservesColumns.PROGRAMNAME, programName);
                        values.put(Channel.TableReservesColumns.SERVICEID, info.getServiceId());
                        values.put(Channel.TableReservesColumns.CHANNELNUMBER, info.getLogicNumer());
                        values.put(Channel.TableReservesColumns.CHANNELNAME, info.getChannelName());
                        values.put(Channel.TableReservesColumns.ENDTIME, endTime);
                        values.put(Channel.TableReservesColumns.STARTTIME, (startTime-TimeOffset)/1000);
                        values.put(Channel.TableReservesColumns.PROGRAMID, info.getProgramId());
                        values.put(Channel.TableReservesColumns.RESERVESTATUS, RESERVE_STATUS_ON);

                        long utcTime = getUtcTime() + TimeOffset;
                        long timeCompensate = System.currentTimeMillis() - utcTime;
                        log.D("current time=" + System.currentTimeMillis());
                        log.D("utctime=" + utcTime);
                        log.D("timecompensate=" + timeCompensate);
                        Uri uri = mActivity.getContentResolver().insert(Channel.URI.TABLE_RESERVES, values);
                        View programReserveTagView = program.findViewById(R.id.order_icon);
                        programReserveTagView.setVisibility(View.VISIBLE);
                        
                        Program pro=new Program();
                        pro.setId(Integer.valueOf(uri.getLastPathSegment()));
                        pro.setChannelName(info.getChannelName());
                        pro.setChannelNumber(info.getLogicNumer());
                        pro.setEndTime(endTime);
                        pro.setName(programName);
                        pro.setProgramId(info.getProgramId());
                        pro.setStartTime(startTime);
                        pro.setServiceId(info.getServiceId());
                        pro.setStatus(RESERVE_STATUS_ON);
                        ADTVService.getService().getEpg().addProgram(String.valueOf(info.getServiceId())+String.valueOf((startTime-TimeOffset)/1000), pro);
                        
                        addReServeProgramToAlam(Integer.valueOf(uri.getLastPathSegment()),startTime + timeCompensate- 60 * 1000);
                        log.D("reserve success! reserve date="+new Date(startTime + timeCompensate - 60 * 1000).toLocaleString());
                        //
                        log.D("current timee=" + new Date().toLocaleString());
                        mAlertDialog.dismiss();
                        showAlertPop(R.string.program_reserve_success,true);
                    }
                });
            }else{//已预约
                query.moveToFirst();
                final int tProgramReserveId = query.getInt(query.getColumnIndex(Channel.TableReservesColumns.ID));
                log.D("----------have order tProgramReserveId="+tProgramReserveId);
                confirmBtn.setText(mActivity.getResources().getString(R.string.program_reverse_alert_cancel));
                confirmBtn.setOnClickListener(new OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        mActivity.getContentResolver().delete(Channel.URI.TABLE_RESERVES, Channel.TableReservesColumns.ID+"=?", new String[]{tProgramReserveId+""});
                        removeReserveProgramFromAlarm(tProgramReserveId);
                        ADTVService.getService().getEpg().removeProgram(String.valueOf(info.getServiceId())+String.valueOf((info.getBegintime()-TimeOffset)/1000));
                        mAlertDialog.dismiss();
                        View programReserveTagView = program.findViewById(R.id.order_icon);
                        programReserveTagView.setVisibility(View.INVISIBLE);
                        showAlertPop(R.string.program_reverse_alert_cancel_success,true);
                    }
                });
            }
            cancleBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAlertDialog.dismiss();
                }
            });
            if(mAlertDialog == null){
                mAlertDialog = new Dialog(mActivity,R.style.epgActivityTheme);
            }
//                final int windowHeight = (int) mActivity.getResources().getDimension(R.dimen.dvb_notify_window_height);
//                final int windowWidth = (int) mActivity.getResources().getDimension(R.dimen.dvb_notify_window_width);
//                mAlertDialog.setContentView(epgNotifyView,new LayoutParams(windowWidth, windowHeight));
            mAlertDialog.setContentView(epgNotifyView);
            mAlertDialog.show();
        }
        
    }
    
    public static long UTCTime;
    public static boolean refreshUtcTime=true;
    
    public long getUtcTime() {
        if(refreshUtcTime){
            refreshUtcTime=false;
            String utcTimeStr = mSettingManager.nativeGetTimeFromTs();
            String[] utcTime = utcTimeStr.split(":");
            long currentTimeMillis = Long.valueOf(utcTime[0])*1000;
            UTCTime=currentTimeMillis;
            return UTCTime;            
        }else{
            return UTCTime;
        }
//        log.D("---getUtcTime--currentTimeMillis="+currentTimeMillis);
//        return currentTimeMillis;
    }
    
    /** 添加预约闹钟 */
    private void addReServeProgramToAlam(int id, long startTime){
        log.D("addReServeProgramToAlam() -- reserveId=" + id + ", startTime=" + startTime);
        Intent intent = new Intent("program alarm");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mActivity,id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) mActivity.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, startTime, pendingIntent);
    }
    /** 删除预约闹钟 */
    private void removeReserveProgramFromAlarm(int id) {
        Log.d("songwenxuan","remove alarm id = "+id);
        Intent intent = new Intent("program alarm");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                mActivity,id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) mActivity.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
    /** 提示 */
    private void showAlertPop(int id,boolean showIcon){ 
        if(mAlertPopupWindow != null && mAlertPopupWindow.isShowing()){
            mAlertPopupWindow.dismiss();
        }
        LayoutInflater inflater = LayoutInflater.from(mActivity);
        View tView = inflater.inflate(R.layout.order_result_dialog_layout, null);
        ImageView img=(ImageView)tView.findViewById(R.id.result_img);
        if(!showIcon){
            img.setVisibility(View.GONE);
        }
        TextView text = (TextView) tView.findViewById(R.id.result_txt);
        text.setText(mActivity.getResources().getString(id));
        if(mAlertPopupWindow == null){
            mAlertPopupWindow = new PopupWindow(tView);
        }
        mAlertPopupWindow.setContentView(tView);
        mAlertPopupWindow.setWidth((int)mActivity.getResources().getDimension(R.dimen.dvb_notify_window_width));
        mAlertPopupWindow.setHeight((int)mActivity.getResources().getDimension(R.dimen.dvb_notify_window_height));
        mAlertPopupWindow.setFocusable(false);
        log.D("show alert toast");
        mAlertPopupWindow.showAtLocation(mActivity.getWindow().getDecorView(), Gravity.CENTER_HORIZONTAL, 0, 0);
        handler.removeMessages(MSG_Close_Window);
        handler.sendEmptyMessageDelayed(MSG_Close_Window, 3000);
    }
    
    protected void onDocGotResource(ADTVResource res){
        log.D("--------res.getID()="+res.getID()+";programId="+programId);
        if (isShow) {
            if(res.getType()==RES_ProgramList){
                epgChannel.refreshProgramFrame(res.getID(),res.getArrayList(),res.getHashMap());
                return;
            }
            if(res.getID()!=programId)
                return;
            switch (res.getType()) {
                case RES_Bitmap:
                    poster.setImageBitmap(res.getBitmap());
                    break;
                case RES_Actor:
                    actor_con.setText(res.getString());
                    break;
                case RES_Type:
                    type_con.setText(res.getString());
                    break;
                case RES_About:
                    about_con.setText(res.getString());
                    break;
                case RES_Nibble:
                    if(res.getInt()==Type_Move){
//                        actor.setText(mActivity.getResources().getString(R.string.actor));
                        type.setText(mActivity.getResources().getString(R.string.type));
                    }else if(res.getInt()==Type_Other){
//                        actor.setText(mActivity.getResources().getString(R.string.playout));
                        type.setText(mActivity.getResources().getString(R.string.presenter));
                    }
                    break;
            }
        }
    }

}
