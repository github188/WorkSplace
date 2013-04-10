package com.joysee.adtv.activity;

import android.app.Application;
import android.database.Cursor;

import com.joysee.adtv.common.DvbLog;
import com.joysee.adtv.db.Channel;
import com.joysee.adtv.logic.bean.Program;
import com.joysee.adtv.server.ADTVEpg;
import com.joysee.adtv.server.ADTVService;

public class TVApplication extends Application{
    
    private static final DvbLog log = new DvbLog("com.joysee.adtv.activity.TVApplication", DvbLog.DebugType.D);
    
    @Override
    public void onCreate() {
        super.onCreate();
        log.D("-----------onCreate-----begin----");
        ADTVEpg epg=ADTVService.getService().getEpg();
        //加载预约列表
        Cursor reserve = this.getContentResolver().query(Channel.URI.TABLE_RESERVES,null, null, null, null);
        if(reserve!=null&&reserve.getCount()>0){
            while(reserve.moveToNext()){
                Program pro=new Program();
                pro.setId(reserve.getInt(reserve.getColumnIndex(Channel.TableReservesColumns.ID)));
                pro.setServiceId(reserve.getInt(reserve.getColumnIndex(Channel.TableReservesColumns.SERVICEID)));
                pro.setName(reserve.getString(reserve.getColumnIndex(Channel.TableReservesColumns.PROGRAMNAME)));
                pro.setChannelName(reserve.getString(reserve.getColumnIndex(Channel.TableReservesColumns.CHANNELNAME)));
                pro.setChannelNumber(reserve.getInt(reserve.getColumnIndex(Channel.TableReservesColumns.CHANNELNUMBER)));
                pro.setEndTime(reserve.getLong(reserve.getColumnIndex(Channel.TableReservesColumns.ENDTIME)));
                pro.setStartTime(reserve.getInt(reserve.getColumnIndex(Channel.TableReservesColumns.STARTTIME)));
                pro.setProgramId(reserve.getInt(reserve.getColumnIndex(Channel.TableReservesColumns.PROGRAMID)));
                log.D("----------"+pro.toString());
                epg.addProgram(""+String.valueOf(pro.getServiceId())+String.valueOf(pro.getStartTime()),pro);
            }
        }
        log.D("-----------onCreate----end-----");
    }

}
