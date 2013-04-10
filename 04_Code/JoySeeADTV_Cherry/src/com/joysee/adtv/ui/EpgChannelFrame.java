package com.joysee.adtv.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.joysee.adtv.R;
import com.joysee.adtv.common.DvbLog;
import com.joysee.adtv.controller.ViewController;
import com.joysee.adtv.doc.ADTVEpgDoc;
import com.joysee.adtv.logic.bean.DvbService;
import com.joysee.adtv.logic.bean.NETEventInfo;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

public class EpgChannelFrame extends FrameLayout{
    
    private static final DvbLog log = new DvbLog(
            "com.joysee.adtv.ui.EpgChannelFrame", DvbLog.DebugType.D);
    
    private Context mContext;
    public EpgWeekView epgWeek;
    public ArrayList<EpgProgramFrame> frameList=new ArrayList<EpgProgramFrame>();
    private ArrayList<View> chanItems=new ArrayList<View>();
//    private List<String> channelList = new ArrayList<String>();
    private EpgProgramFrame currentEpgList;
    private EpgChannelLinear epgAllView;
    private int count=5;
    private int mChanItemWidth=(int)getResources().getDimension(R.dimen.epg_channel_item_width);
    private ViewController mViewController;
    public EpgGuideWindow epgGuideWindow;  
    private int mTotalSize=0;
    private int transTime=500;
    public ImageView flowImg;
    public long lastTime;
    private static final int Space=250;
//    private static final String iconPath="/system/bin/102/";
    private Object lock=new Object();
    public ADTVEpgDoc doc;
    public static int onFocusServiceId;
    public static View onFocusView;
    
    public EpgChannelFrame(Context context) {
        super(context);
    }
    
    public EpgChannelFrame(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public EpgChannelFrame(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
    }
    
    public void init(){
        epgAllView=(EpgChannelLinear)findViewById(R.id.allEpg);
        flowImg=(ImageView)findViewById(R.id.flow_img);
        mTotalSize=mViewController.getTotalChannelSize();  
        int currentPosition=mViewController.getCurrentPosition1();
        log.D("-------------mTotalSize="+mTotalSize+";currentPosition="+currentPosition);
        EpgGuideWindow.refreshUtcTime=true;
        for(int i=0;i<count;i++){
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.epg_channel_item, null);
            TextView channel=(TextView)view.findViewById(R.id.channel_name);
            TextView num=(TextView)view.findViewById(R.id.channel_num);
            ImageView icon=(ImageView)view.findViewById(R.id.channel_icon);
            DvbService item=null;
            int position;
            position=currentPosition+i-1;
            log.D("-------------1111position="+position);
            if(position<0){
                position=mTotalSize+position;
            }else if(position>mTotalSize-1){
                position=position-(mTotalSize-1)-1;
                log.D("------------2222-position="+position);
            }
            log.D("-------------3333position="+position);
            item = mViewController.getChannelByListIndex(position);
            log.D("-----------i="+i+";position="+position+";name="+item.getChannelName()+";item.getServiceId()="+item.getServiceId());
            num.setText(""+item.getLogicChNumber());
            channel.setText(item.getChannelName());
            String path=mViewController.getTVIcons(item.getServiceId());
            log.D("---------path="+path);
            Bitmap bitmap=BitmapFactory.decodeFile(path);
            icon.setImageBitmap(bitmap);
            view.setTag(position);
            EpgProgramFrame epgframe=(EpgProgramFrame)view.findViewById(R.id.epgframe);
            epgframe.setChannelFrame(this);
            epgframe.setTag(position);
            epgframe.serviceId=item.getServiceId();
            epgframe.channelName=item.getChannelName();
            epgframe.setId(item.getServiceId());
            epgframe.setCurChaId(position);
            epgframe.setViewController(mViewController);
            epgframe.init();
            view.setAlpha((float)0.5);
            frameList.add(epgframe);
            chanItems.add(view);
            epgAllView.addView(view);
//            doc.getProgramidList(item.getServiceId(), EpgWeekView.beginTime, EpgWeekView.endTime);
            Message mes=epgGuideWindow.handler.obtainMessage(EpgGuideWindow.MSG_Get_ProgramList);
            mes.arg1=item.getServiceId();
            epgGuideWindow.handler.sendMessageDelayed(mes, 1000);
        }
        log.D("----------------init------over-------");
    }
    
    public void refreshData(){
        if(frameList!=null&&frameList.size()>0){
            for(EpgProgramFrame view:frameList){
                if(view!=null){
                    view.clearProgram();
                    doc.getProgramidList(view.serviceId, EpgWeekView.beginTime, EpgWeekView.endTime);
                }
            }
        }
    }
    
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(event.getAction()==KeyEvent.ACTION_DOWN){
            switch(event.getKeyCode()){
                case KeyEvent.KEYCODE_HOME:
                case KeyEvent.KEYCODE_ESCAPE:
                case KeyEvent.KEYCODE_BACK:
                    epgGuideWindow.dismiss();
                    return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }
    
    public void refreshProgramFrame(int serviceId,ArrayList list,HashMap map){
//        log.D("----------refreshProgramFrame-----");
        long time=System.currentTimeMillis();
        EpgProgramFrame epgframe=(EpgProgramFrame)epgAllView.findViewById(serviceId);
        if(epgframe!=null){
            epgframe.setProgramList(list,map);
        }
        long time2=System.currentTimeMillis();
//        log.D("----------------refreshProgramFrame  task time="+(time2-time));
    }
    
    public void getDetail(){
        epgGuideWindow.handler.removeMessages(EpgGuideWindow.MSG_Get_Detail);
        Message mes=epgGuideWindow.handler.obtainMessage(EpgGuideWindow.MSG_Get_Detail);
        int programId=onFocusView.getId();
        log.D("-----getDetail------programId="+programId);
        mes.arg1=programId;
        epgGuideWindow.handler.sendMessageDelayed(mes, 200);
    }
    
    /**
     * 当长按停下时，加载所有epg
     */
    public void loadAllData(){
        for(int i=0;i<frameList.size();i++){
            EpgProgramFrame epgFrame=frameList.get(i);
            if(!epgFrame.loadOver)
                doc.getProgramidList(epgFrame.serviceId, EpgWeekView.beginTime, EpgWeekView.endTime);
        }
    }
    
    /**
     * 当长按开始时，取消所有的加载
     */
    public void cancleAllLoad(){
        for(int i=0;i<frameList.size();i++){
            EpgProgramFrame epgFrame=frameList.get(i);
            if(epgFrame.serviceId>0)
                doc.cancelProgramListTask(epgFrame.serviceId, EpgWeekView.beginTime, EpgWeekView.endTime);
        }
    }
    
    public void addLeftView(){
        log.D("-----------addLeftView---");
//        if (!EpgProgramFrame.longkey) {
//            TranslateAnimation animation = null;
//            animation = new TranslateAnimation(0, mChanItemWidth, 0, 0);
//            animation.setAnimationListener(leftAnimLis);
//            animation.setDuration(transTime);
//            animation.setFillAfter(false);
//            epgAllView.startAnimation(animation);
//        }
        EpgGuideWindow.refreshUtcTime=true;
        epgAllView.removeViewAt(count-1);
        log.D("---------chanItems.size="+chanItems.size()+";count="+count);
        View view = chanItems.remove(count-1);
        chanItems.add(0, view);
        EpgProgramFrame epgfra=null;
        synchronized (lock) {
            epgfra=frameList.remove(count-1);
            frameList.add(0, epgfra);            
        }
        int serid=epgfra.serviceId;
//        doc.cancelProgramListTask(serid, EpgWeekView.beginTime, EpgWeekView.endTime);
        
        View child = epgAllView.getChildAt(0);
        int position = (Integer) child.getTag() - 1;
        if (position <0)
            position = mTotalSize-1;
        view.setTag(position);
        TextView text = (TextView) view.findViewById(R.id.channel_name);
        TextView num = (TextView) view.findViewById(R.id.channel_num);
        ImageView icon=(ImageView)view.findViewById(R.id.channel_icon);
        DvbService item = mViewController.getChannelByListIndex(position);
        log.D("-----------addRightView------position="+position+";name="+item.getChannelName()+";serverid="+item.getServiceId());
        text.setText(item.getChannelName());
        String path=mViewController.getTVIcons(item.getServiceId());
        Bitmap bitmap=BitmapFactory.decodeFile(path);
        icon.setImageBitmap(bitmap);
        num.setText(""+item.getLogicChNumber());
        epgfra.setTag(position);
        epgfra.setId(item.getServiceId());
        epgfra.serviceId=item.getServiceId();
        epgfra.channelName=item.getChannelName();
        epgfra.setCurChaId(position);
//        epgfra.init();
//        epgfra.refresh();
        epgfra.clearProgram();
        log.D("---------------longkey="+EpgProgramFrame.longkey+";fastkey="+EpgProgramFrame.fastkey);
//        if(EpgProgramFrame.longkey||EpgProgramFrame.fastkey){
//        }else{
//            doc.getProgramidList(item.getServiceId(), EpgWeekView.beginTime, EpgWeekView.endTime);            
//        }
//        Message mes=epgGuideWindow.handler.obtainMessage(EpgGuideWindow.MSG_Get_ProgramList);
//        mes.arg1=item.getServiceId();
//        epgGuideWindow.handler.sendMessage(mes);
        epgAllView.addView(view, 0);
    }
    
    
    boolean isAnim=false;
    public int rightDwon=0;
    
    public void addRightView(){
//        long begin=System.currentTimeMillis();
        log.D("-----------addRightView---");
//        if(!EpgProgramFrame.longkey&&!EpgProgramFrame.fastkey){
//            TranslateAnimation animation = null;
//            animation = new TranslateAnimation(mChanItemWidth, 0, 0, 0);
//            animation.setDuration(transTime);
//            animation.setAnimationListener(rightAnimLis);
//            animation.setFillAfter(true);
//            animation.setFillEnabled(true);
//            epgAllView.startAnimation(animation);
//        }
        EpgGuideWindow.refreshUtcTime=true;
        epgAllView.removeViewAt(0);
        
        View view = chanItems.remove(0);
        chanItems.add(view);
        EpgProgramFrame epgfra=null;
        synchronized (lock) {
            epgfra=frameList.remove(0);
            frameList.add(epgfra);            
        }
        int serid=epgfra.serviceId;
//        doc.cancelProgramListTask(serid, EpgWeekView.beginTime, EpgWeekView.endTime);
        
        View child = epgAllView.getChildAt(count-2);
        int position = (Integer) child.getTag() + 1;
        
        if (position >= mTotalSize)
            position = 0;
        view.setTag(position);
        TextView text = (TextView) view.findViewById(R.id.channel_name);
        TextView num = (TextView) view.findViewById(R.id.channel_num);
        ImageView icon=(ImageView)view.findViewById(R.id.channel_icon);
//        long time1=System.currentTimeMillis();
        DvbService item = mViewController.getChannelByListIndex(position);
//        long time2=System.currentTimeMillis();
//        log.D("---------getchannellist task time="+(time2-time1));
//        log.D("-----------addRightView------position="+position+";name="+item.getChannelName()+";serverid="+item.getServiceId());
        text.setText(item.getChannelName());
        num.setText(""+item.getLogicChNumber());
        String path=mViewController.getTVIcons(item.getServiceId());
        Bitmap bitmap=BitmapFactory.decodeFile(path);
        icon.setImageBitmap(bitmap);
        epgfra.setTag(position);
        epgfra.setCurChaId(position);
        epgfra.setId(item.getServiceId());
        epgfra.serviceId=item.getServiceId();
        epgfra.channelName=item.getChannelName();
        epgfra.clearProgram();
//        long time3=System.currentTimeMillis();
        log.D("---------------longkey="+EpgProgramFrame.longkey+";fastkey="+EpgProgramFrame.fastkey);
//        if(EpgProgramFrame.longkey||EpgProgramFrame.fastkey){
//        }else{
//            doc.getProgramidList(item.getServiceId(), EpgWeekView.beginTime, EpgWeekView.endTime);                        
//        }
//        long time4=System.currentTimeMillis();
//        log.D("-------------getProgramidList take time="+(time4-time3));
        epgAllView.addView(view, count-1);
//        long end=System.currentTimeMillis();
//        log.D("-------------addRight take time="+(end-begin));
    }
    
    public void setCurrentList(EpgProgramFrame epgList){
        if(currentEpgList!=null){
            View vi=(View)currentEpgList.getParent();
            vi.setAlpha((float)0.5);            
        }
        currentEpgList=epgList;
        View vie=(View)currentEpgList.getParent();
        vie.setAlpha((float)1);
    }
    
    public EpgProgramFrame getCurrentList(){
        return currentEpgList;
    }
    
    public void moveDownTogeter(NETEventInfo info){
        for(int i=1;i<count-1;i++){
            if(frameList.get(i).pidList.size()>EpgProgramFrame.count){
                frameList.get(i).timeAxisBottom(info);      
//                frameList.get(i).addBottomView();
            }
        }
    }
    
    public void moveUpTogeter(NETEventInfo info){
        for(int i=1;i<count-1;i++){
            if(frameList.get(i).pidList.size()>EpgProgramFrame.count){
                frameList.get(i).timeAxisTop(info);    
//                frameList.get(i).addTopView();
            }
        }
    }
    
    public void onfocusView(){
        log.D("---onfocusView--------------");
        if(currentEpgList==null){
            frameList.get(1).focusView(-1);
        }else{
            currentEpgList.focusView(-1);
        }
    }
    
    public void onFocusUp(){
        log.D("---------onFocus-----------");
        flowImg.setVisibility(View.INVISIBLE);
        if(currentEpgList!=null){
            View vi=(View)currentEpgList.getParent();
            vi.setAlpha((float)0.5);            
        }
        epgWeek.setFocusView();
    }

    private AnimationListener leftAnimLis = new AnimationListener() {
        public void onAnimationStart(Animation animation) {
            epgAllView.setAdding(true);
        }
        public void onAnimationRepeat(Animation animation) {
        }
        public void onAnimationEnd(Animation animation) {
            epgAllView.setAdding(false);
        }
    };
    
    private AnimationListener rightAnimLis = new AnimationListener() {
        public void onAnimationStart(Animation animation) {
        }
        public void onAnimationRepeat(Animation animation) {
        }
        public void onAnimationEnd(Animation animation) {
            isAnim=false;
        }
    };
    
    public void setFocusView(){
        
    }
    
    public void setEpgWeekView(EpgWeekView epgWeek){
        this.epgWeek=epgWeek;
    }
    
    public void setViewController(ViewController vc){
        this.mViewController=vc;
    }
    
    public void setGuideWindow(EpgGuideWindow win){
        this.epgGuideWindow=win;
    }
}
