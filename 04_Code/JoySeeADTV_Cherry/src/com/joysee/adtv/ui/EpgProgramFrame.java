package com.joysee.adtv.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.joysee.adtv.R;
import com.joysee.adtv.common.DateFormatUtil;
import com.joysee.adtv.common.DvbLog;
import com.joysee.adtv.controller.ViewController;
import com.joysee.adtv.logic.bean.NETEventInfo;
import com.joysee.adtv.server.ADTVService;

public class EpgProgramFrame extends FrameLayout{

    private static final DvbLog log = new DvbLog(
            "com.joysee.adtv.ui.EpgProgramFrame", DvbLog.DebugType.D);
    
    private Context mContext;
    private EpgChannelFrame channelFrame;
    private int transTime=150;
    private int mChanItemHeight=(int)getResources().getDimension(R.dimen.epg_program_item_height);
    private ArrayList<View> epgViewItems=new ArrayList<View>();
    private HashMap<Integer,NETEventInfo> epglInfoMap=new LinkedHashMap<Integer,NETEventInfo>();
    public static int count=8;
    private EpgProgramLinear epgListView;
    public int mCurChaId;
    public int mPosition = 0;
    public static long currentTime;
    public static boolean longkey,fastkey,leftRight=false;
    public static long preKeyTime;
    public int serviceId;
    public String channelName;
    private ViewController mViewController;
    public ArrayList<Integer> pidList=new ArrayList<Integer>();
    private int left_sub=(int)getResources().getDimension(R.dimen.epg_move_left_sub);
    private int top_sub=(int)getResources().getDimension(R.dimen.epg_move_top_sub);
    private int left_small=(int)getResources().getDimension(R.dimen.epg_move_left_small);
    private int left_big=(int)getResources().getDimension(R.dimen.epg_move_left_big);
    private int top_small=(int)getResources().getDimension(R.dimen.epg_move_top_small);;
    private int top_big=(int)getResources().getDimension(R.dimen.epg_move_top_big);
    private View focus;
    private boolean isFocus=false;
    public boolean loadOver=false;
    
    public static final int MSG_LOADOVER=1;
    
    public EpgProgramFrame(Context context) {
        super(context);
    }
    
    public EpgProgramFrame(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public EpgProgramFrame(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
    }
    
    Handler mhandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case MSG_LOADOVER:
                    mhandler.removeMessages(MSG_LOADOVER);
                    log.D("---------------MSG_LOADOVER-------------");
                    channelFrame.loadAllData();
                    break;
            }
            super.handleMessage(msg);
        }
    };
    
    public void init(){
        epgListView=(EpgProgramLinear)this.findViewById(R.id.linear2);
        epglInfoMap.clear();
        epgViewItems.clear();
        pidList.clear();
        for(int i=0;i<8;i++){
            pidList.add(0);
        }
        for(int i=0;i<count;i++){
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.epg_program_item, null);
            view.setTag(i);
            view.setId(0);
            view.setFocusable(true);
            view.setOnFocusChangeListener(new OnFocusChangeListener(){
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if(hasFocus){
                        focus=v;
                        isFocus=true;
                        changeFlowImg(v);
                        setOnFocus(v);
//                        int index=(Integer)v.getTag();
//                        NETEventInfo info=epglInfoMap.get(pidList.get(index));
//                        if(info!=null){
//                            long endTime=info.getBegintime()+info.getDuration();
//                            log.D("---onfocus---index="+index+";begin"+DateFormatUtil.getStringFromMillis(info.getBegintime())+";endTime="+DateFormatUtil.getStringFromMillis(endTime));
//                        }
                    }else{
                        ((TextView)v.findViewById(R.id.name)).setTextColor(getResources().getColor(R.color.white_txt));
                        isFocus=false;
                    }
                }
            });
            view.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v) {
                    log.D("-------------onClick----------v.getTag="+v.getTag().toString()+";mCurChaId="+mCurChaId);
                    epgOnClick(v);
                }
            });
            TextView name=(TextView)view.findViewById(R.id.name);
            TextView time=(TextView)view.findViewById(R.id.begin_time);
            ImageView icon=(ImageView)view.findViewById(R.id.order_icon);
            icon.setVisibility(View.INVISIBLE);
            name.setText("           ");
            time.setText("    ");   
            time.setTextColor(getResources().getColor(R.color.white_txt));
            epgListView.addView(view);
            epgViewItems.add(view);
        }
        log.D("init----------epgListView.size="+epgListView.getChildCount());
    }
    
    public void clearProgram(){
        for(int i=0;i<count;i++){
            View view=epgListView.getChildAt(i);
            view.setId(0);
            view.setTag(i);
            pidList.clear();
            epglInfoMap.clear();
            loadOver=false;
            TextView name=(TextView)view.findViewById(R.id.name);
            TextView time=(TextView)view.findViewById(R.id.begin_time);
            ImageView icon=(ImageView)view.findViewById(R.id.order_icon);
            icon.setVisibility(View.INVISIBLE);
            name.setText("             ");
            time.setText("     ");   
            time.setTextColor(getResources().getColor(R.color.white_txt));
        }
    }
    
    public void setProgramList(ArrayList<Integer> list,HashMap<Integer,NETEventInfo> map){
//        log.D("---------setProgramList   list.size="+list.size()+"; map.size="+map.size()+"---------serviceid="+serviceId);
//        long begin=System.currentTimeMillis();
        pidList.clear();
        pidList=list;
        epglInfoMap.clear();
        epglInfoMap=map;
        pidList.add(0, 0);//第一个为隐藏的view,如果epg不满6个，用0添加
        loadOver=true;
        int size=pidList.size();
        if(size<=7){
            for(int i=size;i<7;i++){
                pidList.add(0);
            }
        }
        pidList.add(pidList.size(),0);
        int dingWei=dingWei();
        for(int i=0;i<count;i++){
            View view=epgListView.getChildAt(i);
            view.setTag(dingWei+i);
            NETEventInfo info=getNETEventInfo(dingWei+i);
            TextView name=(TextView)view.findViewById(R.id.name);
            TextView time=(TextView)view.findViewById(R.id.begin_time);
            ImageView icon=(ImageView)view.findViewById(R.id.order_icon);
            icon.setVisibility(View.INVISIBLE);
            if(info!=null){
                view.setId(info.getProgramId());
                name.setText(info.getEname());
                int status=channelFrame.epgGuideWindow.programStatus(info);
                if(status==EpgGuideWindow.Status_End){
                    time.setTextColor(getResources().getColor(R.color.yellow_txt));
                }else{
                    time.setTextColor(getResources().getColor(R.color.green_txt));
                }
                time.setText(""+DateFormatUtil.getTimeFromLong(info.getBegintime()));
                if(ADTVService.getService().getEpg().getProgram(String.valueOf(info.getServiceId())+String.valueOf((info.getBegintime()-EpgGuideWindow.TimeOffset)/1000))!=null){
                    icon.setVisibility(View.VISIBLE);
                }
            }else{
                view.setId(0);
                name.setText("------------");
                time.setText("--:--");   
                time.setTextColor(getResources().getColor(R.color.white_txt));
            }
        }
        this.invalidate();
        if(serviceId==EpgChannelFrame.onFocusServiceId&&LeftRight){
            channelFrame.getDetail();
        }
//        long end=System.currentTimeMillis();
//        log.D("--------------------setProgramList take time "+(end-begin));
    }
    
    public int dingWei(){
//        long time1=System.currentTimeMillis();
        int playIndex=-1;
        int size=pidList.size();
        if(size<9||!EpgWeekView.isToday){
//            log.D("----------dingWei---------size<9"+0);
            return 0;
        }
        for(int i=0;i<size;i++){
            if(pidList.get(i)>0){
                NETEventInfo info=epglInfoMap.get(pidList.get(i));
                if(info==null)
                    break;
                int status=channelFrame.epgGuideWindow.programStatus(info);
                if(status==EpgGuideWindow.Status_Future){
                    NETEventInfo info2=epglInfoMap.get(pidList.get(i-1));
                    int status2=channelFrame.epgGuideWindow.programStatus(info2);
                    playIndex=i;
                    if(status2==EpgGuideWindow.Status_Ing){
                        playIndex=i-1;
                    }
                    break;
                }
            }
        }
        if(playIndex>0){
            int offset=size-playIndex;
//            log.D("--------dingWei--------size="+size+";playIndex="+playIndex+";offset="+offset);
            if(offset<7){
                playIndex=playIndex-(7-offset);
            }
            playIndex-=1;
            if(playIndex<0){
                playIndex=0;
            }
        }else{
            playIndex=0;
        }
//        long time2=System.currentTimeMillis();
        log.D("----------playIndex="+playIndex);
        return playIndex;
    }
    
    public NETEventInfo getNETEventInfo(int index){
        NETEventInfo info=null;
        if(index<pidList.size()&&index>0&&pidList.get(index)>0){
            info=epglInfoMap.get(pidList.get(index));
        }
        return info;
    }
    
    public void epgOnClick(View v){
        int index=(Integer)v.getTag();
        log.D("--------index="+index+";pidList.size()="+pidList.size());
        if(index>=0&&index<pidList.size()){
            NETEventInfo info=mViewController.getProgramInfo(pidList.get(index));
            log.D("-------info.name="+info.getEname());
            channelFrame.epgGuideWindow.showAlertDialog(info, v);
        }
    }
    
    public void changeFlowImg(View v){
        long time=System.currentTimeMillis();
//        int index=(Integer)v.getTag();
//        log.D("--------index="+index+";pidList.size()="+pidList.size());
        ((TextView)v.findViewById(R.id.name)).setTextColor(getResources().getColor(R.color.green_txt));
        int location[] = new int[2];
        v.getLocationInWindow(location);
        MarginLayoutParams params = (MarginLayoutParams)channelFrame.flowImg.getLayoutParams();
        Log.d("chenggang","--params.topMargin="+params.topMargin+";params.leftMargin="+params.leftMargin);
        Log.d("chenggang","--location[0]  left   ="+location[0]+"           ;location[1]  top ="+location[1]);
        location[0] = location[0]-left_sub; 
        location[1] = location[1]-top_sub;
        Log.d("chenggang","--location[0]  left   ="+location[0]+"           ;location[1]  top ="+location[1]);
        //
        if ((location[0] >= left_small&& (location[0]) <= left_big)&&(location[1] >= top_small && (location[1]) <= top_big)) {
            params.leftMargin = location[0];
            params.topMargin = location[1];
            channelFrame.flowImg.setLayoutParams(params);
            channelFrame.flowImg.setVisibility(View.VISIBLE);
        }
        long time2=System.currentTimeMillis();
        log.D("---------changeFlowImg---take time="+(time2-time));
    }
    
    public static long LongKey=300;
    public static long NormalKey=400;
    public static long spaceTime=NormalKey;
    public static long upTimeLast;
    public static boolean LeftRight=true;
    
    public void getDetail(){
        int index=(Integer)focus.getTag();
        NETEventInfo info=getNETEventInfo(index);
        channelFrame.epgGuideWindow.handler.removeMessages(EpgGuideWindow.MSG_Get_Detail);
        Message mes=channelFrame.epgGuideWindow.handler.obtainMessage(EpgGuideWindow.MSG_Get_Detail);
        if(info!=null){
            mes.arg1=info.getProgramId();
        }else{
            mes.arg1=0;                
        }
        channelFrame.epgGuideWindow.handler.sendMessageDelayed(mes, spaceTime);
    }
    
    public boolean keyDispatch(int keyCode){
        long time=SystemClock.uptimeMillis();
//        Log.d("chenggang","time="+time+";currentTime="+currentTime+";-----time-currentTime="+(time-currentTime)+"   ;time-preKeyTime="+(time-preKeyTime));
        if((time-preKeyTime)<400){
            longkey=true;
            spaceTime=LongKey;
        }else{
            longkey=false;
            spaceTime=NormalKey;
        }
//        if((time-preKeyTime)<600){
////            spaceTime=LongKey;
////            fastkey=true;
//        }else{
//            spaceTime=NormalKey;
//            fastkey=false;
//        }
        preKeyTime=time;
        if(!(keyCode==KeyEvent.KEYCODE_DPAD_LEFT||keyCode==KeyEvent.KEYCODE_DPAD_RIGHT)){
            spaceTime=NormalKey;
        }else{
            mhandler.removeMessages(MSG_LOADOVER);
        }
//        if(longkey){
//            channelFrame.cancleAllLoad();
//        }
        if((time-currentTime)<spaceTime){
//            Log.d("chenggang", "----lost---spaceTime="+spaceTime);
            return true;
        }
        currentTime=time;
        return false;
    }
    
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode=event.getKeyCode();
        if (event.getAction() == KeyEvent.ACTION_DOWN ) {
            
            Log.d("chenggang","----keyCode="+keyCode+";longkey="+longkey+";fastkey="+fastkey);
            if(keyCode==KeyEvent.KEYCODE_DPAD_DOWN){
                LeftRight=false;
//                log.D("-----focus.tag="+focus.getTag().toString()+"        ;pidList.size="+pidList.size());
                if(focus.getTag().toString().equals(""+(pidList.size()-2))){
                    return true;
                }
                if (focus.getTag().toString().equals(epgViewItems.get(count-2).getTag().toString())&&pidList.size()>8) {
                    if(keyDispatch(keyCode)){
                        return true;
                    }
                    if(pidList.size()>count){
                        int index=(Integer)epgViewItems.get(count-1).getTag();
                        NETEventInfo info=epglInfoMap.get(pidList.get(index));
                        channelFrame.moveDownTogeter(info);
                    }
                }
            }else if(keyCode==KeyEvent.KEYCODE_DPAD_UP){
                LeftRight=false;
//                log.D("-----focus.tag="+focus.getTag().toString()+"        ;pidList.size="+pidList.size());
                if(focus.getTag().toString().equals("1")){
                    channelFrame.onFocusUp();
                    return true;
                }
                if (focus.getTag().toString().equals(epgViewItems.get(1).getTag().toString())&&pidList.size()>8) {
                    if(keyDispatch(keyCode)){
                        return true;
                    }
                    if(pidList.size()>count){
                        int index=(Integer)epgViewItems.get(0).getTag();
                        NETEventInfo info=epglInfoMap.get(pidList.get(index));
                        long endtime=info.getBegintime()+info.getDuration();
                        log.D("-----moveUpTogeter  info   begintime=="+DateFormatUtil.getStringFromMillis(info.getBegintime())+";endtime="+DateFormatUtil.getStringFromMillis(endtime));
                        channelFrame.moveUpTogeter(info);
                    }
                }
            }else if(keyCode==KeyEvent.KEYCODE_DPAD_LEFT){
                LeftRight=true;
                if(getTagThis().equals(channelFrame.frameList.get(1).getTag().toString())){
                    if(keyDispatch(keyCode)){
                        return true;
                    }
                    channelFrame.addLeftView();
                }
            }else if(keyCode==KeyEvent.KEYCODE_DPAD_RIGHT){
                LeftRight=true;
                if(getTagThis().equals(channelFrame.frameList.get(3).getTag().toString())){
                    if(keyDispatch(keyCode)){
                        return true;
                    }
                    channelFrame.addRightView();          
                }
            }
//            else
        }else if (event.getAction() == KeyEvent.ACTION_UP ) {
            long time=System.currentTimeMillis();
            if((time-upTimeLast)<700){
                fastkey=true;
            }else{
                fastkey=false;
            }
            mhandler.removeMessages(MSG_LOADOVER);
            mhandler.sendEmptyMessageDelayed(MSG_LOADOVER, 600);
            longkey=false;
            fastkey=true;
            spaceTime=NormalKey;
            if(keyCode==KeyEvent.KEYCODE_MENU){
                channelFrame.epgGuideWindow.showMenu();
            }
        }
        return super.dispatchKeyEvent(event);
    };
    
    public void setOnFocus(View v){
        getDetail();
        channelFrame.setCurrentList(this);
        EpgChannelFrame.onFocusServiceId=serviceId;
        EpgChannelFrame.onFocusView=v;
    }
    
    public String getTagThis(){
        return this.getTag().toString();
    }
    
    public void focusView(int i){
        if(i==-1){
            epgViewItems.get(1).requestFocus();
        }
    }
    
    public void addTopView(){
//        log.D("-----------addTopView---channelName="+channelName);
        int index=(Integer)epgViewItems.get(0).getTag();
        int programid=pidList.get(index);
        if (programid<=0) {
            return;
        }
//        if(!EpgProgramFrame.longkey){
//            TranslateAnimation animation = null;
//            animation = new TranslateAnimation(0, 0,0, mChanItemHeight);
//            animation.setAnimationListener(topAnimLis);
//            animation.setDuration(transTime);
//            animation.setFillAfter(false);
//            animation.setInterpolator(new DecelerateInterpolator());
//            epgListView.startAnimation(animation);
//        }
        
        epgListView.removeViewAt(count-1);
//        log.D("---------chanItems.size="+epgViewItems.size()+";count="+count);
        View view = epgViewItems.remove(count-1);
        epgViewItems.add(0, view);
        
        View child = epgListView.getChildAt(0);
        int position = (Integer) child.getTag() - 1;
        if (position <0)
            position = pidList.size()-1;
        view.setTag(position);
        NETEventInfo info=getNETEventInfo(position);
        TextView name=(TextView)view.findViewById(R.id.name);
        TextView time=(TextView)view.findViewById(R.id.begin_time);
        ImageView icon=(ImageView)view.findViewById(R.id.order_icon);
        icon.setVisibility(View.INVISIBLE);
        if(info!=null){
            view.setId(info.getProgramId());
            name.setText(info.getEname());
            int status=channelFrame.epgGuideWindow.programStatus(info);
            if(status==EpgGuideWindow.Status_End){
                time.setTextColor(getResources().getColor(R.color.yellow_txt));
            }else{
                time.setTextColor(getResources().getColor(R.color.green_txt));
            }
            time.setText(""+DateFormatUtil.getTimeFromLong(info.getBegintime()));
            if(ADTVService.getService().getEpg().getProgram(String.valueOf(info.getServiceId())+String.valueOf((info.getBegintime()-EpgGuideWindow.TimeOffset)/1000))!=null){
                icon.setVisibility(View.VISIBLE);
            }
            epglInfoMap.put(info.getEventId(), info);
        }else{
            name.setText("------------");
            time.setText("--:--");         
            time.setTextColor(getResources().getColor(R.color.white_txt));
        }
        epgListView.addView(view, 0);
    }
    
    public boolean tempIninfoTop(NETEventInfo info,NETEventInfo temp){
        long tempEnd=temp.getBegintime()+temp.getDuration();
//        log.D("------------channelName="+channelName+"----temp.getBeginTime="+DateFormatUtil.getStringFromMillis(temp.getBegintime())+";temp.endtime="+DateFormatUtil.getStringFromMillis(tempEnd));
        if(tempEnd>info.getBegintime()){
            return true;
        }else{
            return false;
        }
    }
    
    public void timeAxisTop(NETEventInfo info){
//        log.D("----------- timeAxisTop----isFocus="+isFocus+";channeName="+channelName);
        if(info==null){
            return;
        }
        if(isFocus){
            addTopView();
        }else{
            int index=(Integer)epgViewItems.get(0).getTag();
            for(int i=index;i>0;i--){
                NETEventInfo temp=epglInfoMap.get(pidList.get(i));
                if(temp!=null){
                    if(tempIninfoTop(info,temp)){
                        addTopView();
                    }else{
                        return;
                    }
                }else{
                    return;
                }
            }
        }
    }
    
    public boolean tempIninfoDown(NETEventInfo info,NETEventInfo temp){
        long infoEnd=info.getBegintime()+info.getDuration();
//        log.D("------------channelName="+channelName+"----temp.getBeginTime="+DateFormatUtil.getStringFromMillis(temp.getBegintime()));
        if(temp.getBegintime()>=infoEnd){
            return true;
        }else{
            return false;
        }
    }
    
    public void timeAxisBottom(NETEventInfo info){
//        log.D("-----------timeAxisBottom----isFocus="+isFocus+";channeName="+channelName);
        if(info==null){
            return;
        }
        if(isFocus){
            addBottomView();
        }else{
            int index=(Integer)epgViewItems.get(count-1).getTag();
            for(int i=index;i<pidList.size();i++){
                NETEventInfo temp=epglInfoMap.get(pidList.get(i));
                if(temp!=null){
                    if(!tempIninfoDown(info,temp)){
                        addBottomView();
                    }else{
                        return;
                    }
                }else{
                    return;
                }
            }
        }
    }
    
    public void addBottomView(){
//        log.D("-----------addBottomView---channelName="+channelName);
        int index=(Integer)epgViewItems.get(count-1).getTag();
        int programid=pidList.get(index);
//        log.D("---programid="+programid+";index="+index+";pidList.size="+pidList.size());
        if (programid<=0) {
            return;
        }
//        if(!EpgProgramFrame.longkey){
//            TranslateAnimation animation = null;
//            animation = new TranslateAnimation(0, 0, mChanItemHeight, 0);
//            animation.setFillAfter(true);
//            animation.setFillEnabled(true);
//            animation.setInterpolator(new DecelerateInterpolator());
//            epgListView.startAnimation(animation);
//        }
        epgListView.removeViewAt(0);
        View view = epgViewItems.remove(0);
        epgViewItems.add(view);
        View child = epgListView.getChildAt(count-2);
        int position = (Integer) child.getTag() + 1;
        
        if (position == pidList.size())
            position = 0;
        view.setTag(position);
        NETEventInfo info=getNETEventInfo(position);
        TextView name=(TextView)view.findViewById(R.id.name);
        TextView time=(TextView)view.findViewById(R.id.begin_time);
        ImageView icon=(ImageView)view.findViewById(R.id.order_icon);
        icon.setVisibility(View.INVISIBLE);
        if(info!=null){
            view.setId(info.getProgramId());
            name.setText(info.getEname());
            int status=channelFrame.epgGuideWindow.programStatus(info);
            if(status==EpgGuideWindow.Status_End){
                time.setTextColor(getResources().getColor(R.color.yellow_txt));
            }else{
                time.setTextColor(getResources().getColor(R.color.green_txt));
            }
            time.setText(""+DateFormatUtil.getTimeFromLong(info.getBegintime()));
            if(ADTVService.getService().getEpg().getProgram(String.valueOf(info.getServiceId())+String.valueOf((info.getBegintime()-EpgGuideWindow.TimeOffset)/1000))!=null){
                icon.setVisibility(View.VISIBLE);
            }
            epglInfoMap.put(info.getEventId(), info);
        }else{
            name.setText("------------");
            time.setText("--:--");   
            time.setTextColor(getResources().getColor(R.color.white_txt));
        }
        epgListView.addView(view, count-1);
        
    }
    
    private AnimationListener topAnimLis = new AnimationListener() {
        public void onAnimationStart(Animation animation) {
            epgListView.setAdding(true);
        }
        public void onAnimationRepeat(Animation animation) {
        }
        public void onAnimationEnd(Animation animation) {
            epgListView.setAdding(false);
        }
    };
    
    public void setChannelFrame(EpgChannelFrame channel){
        this.channelFrame=channel;
    }
    
    public void setCurChaId(int channelId){
        this.mCurChaId=channelId;
    }
    
    public void setViewController(ViewController vc){
        this.mViewController=vc;
    }
}
