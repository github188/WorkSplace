
package novel.supertv.dvb.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import novel.supertv.dvb.activity.SearchActivity;
import novel.supertv.dvb.activity.SearchMainActivity;
import novel.supertv.dvb.jni.ISearchTVNotify;
import novel.supertv.dvb.jni.JniChannelSearch;
import novel.supertv.dvb.jni.struct.TuningParam;
import novel.supertv.dvb.jni.struct.stChannel;
import novel.supertv.dvb.jni.struct.tagTunerSignal;
import novel.supertv.dvb.provider.Channel;
import novel.supertv.dvb.utils.DvbLog;
import android.app.Service;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;

public class SearchService extends Service {

    private static final DvbLog log = new DvbLog(
            "novel.supertv.dvb.service.SearchService", DvbLog.DebugType.D);
    private List<Integer> tpTemp=new ArrayList<Integer>();
    /**
     * 当前使用的搜索类型,默认全频搜索
     */
    private int mCurrentSearchType = SearchActivity.SEARCH_TYPE_FULL;
    private Map<String, Integer> map = new HashMap<String, Integer>();
    /**
     * 当前搜索到的频率的ID
     */
    private int mTransponderId = 0;
    
    private int tvCount = 0;
    
    private int bcCount = 0;

    private JniChannelSearch search = new JniChannelSearch();

    private ISearchTVNotify callback = new ISearchTVNotify() {

        @Override
        public void OnTunerInfo(TuningParam transponder, tagTunerSignal signal) {

            if (onSearchNewTransponderListener != null) {

                onSearchNewTransponderListener.onSearchNewTransponder(transponder, signal);
              //测试集中存储频道，先注掉 by SongWenxuan
              saveTp(transponder);
            }

        }

        @Override
        public void OnSTVComplete(List<stChannel> channels) {

            if (onSearchEndListener != null) {
                onSearchEndListener.onSearchEnd(channels);
                log.D("total channel size："+channels.size());
            }

        }

        @Override
        public void OnProgress(int progress) {

            if (onSearchProgressChangeListener != null) {
                onSearchProgressChangeListener.onSearchProgressChanged(progress);
            }

        }

        @Override
        public void OnDVBService(stChannel services) {

            if (onSearchFindNewChannelListener != null) {
                onSearchFindNewChannelListener
                        .onFindNewChannel(
                                services.getServiceName(),
                                services.ServiceType
                        );
                // 存储数据库中
                //saveChannel(services);

            }

        }
    };

    /**
     * 开始搜索，供UI层直接调用
     * 
     * @param searchType 搜索类型，参见{@link SearchActivity} 中定义的3种类型
     * @param tp Transponder 类似于 TuningParam，待更换
     */
    public void startSearch(int searchType, TuningParam tp) {
        log.D("startSearch()");

        mCurrentSearchType = searchType;
        Cursor tvCur = getContentResolver().query(Channel.URI.TABLE_CHANNELS,new String[]{"LogicChNumber"}, "ServiceType=?", new String[]{"1"}, "LogicChNumber desc");
        if(tvCur.moveToFirst()){
            tvCount = tvCur.getInt(tvCur.getColumnIndex("LogicChNumber"));
        }
        Cursor bcCur = getContentResolver().query(Channel.URI.TABLE_CHANNELS,new String[]{"LogicChNumber"}, "ServiceType=?", new String[]{"2"}, "LogicChNumber desc");
        if(bcCur.moveToFirst()){
            bcCount = bcCur.getInt(bcCur.getColumnIndex("LogicChNumber"));
        }
        log.D("startSearch() tparam = " + tp.toString());

        // 测试

        String uri = search.getNameString();
        log.D("search.getUriString() = " + uri);

        // test
        search.setNotify(callback);
        // 调用JNI方法成功，先注释，使用假的方法测试
        search.StartSearchTV(searchType, tp, callback);

        log.D("startSearch() StartSearchTV from JNI OK !!!");

        // 开始测试代码:
        // mainHandler.sendEmptyMessage(SEARCH_TEST);
    }

    public void stopSearch() {
        log.D("stopSearch()");
        // 调用JNI方法成功，先注释，使用假的方法测试
        log.D("CancelSearchTV() CancelSearchTV from JNI OK !!!");
        search.CancelSearchTV();

        // 停止测试的测试命令
        // mainHandler.removeMessages(SEARCH_TEST);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        if(tpTemp!=null){
            tpTemp.clear();
        }
        super.onDestroy();
    }

    /**
     * Class for clients to access. Because we know this service always runs in
     * the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public SearchService getService() {
            return SearchService.this;
        }
    }

    // This is the object that receives interactions from clients. See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();
    /*private int testProgress = 0;
    private static final int SEARCH_TEST = 0;
    private Handler mainHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SEARCH_TEST:
                    if (testProgress < 100) {
                        testProgress++;
                        if (callback != null) {
                            callback.OnProgress(testProgress);
                            if (testProgress == 3) {
                                log.E("testProgress == 3");
                                TuningParam tp = new TuningParam(642000, 6875, 3);
                                tagTunerSignal sg = new tagTunerSignal();
                                sg.setCN(100);
                                sg.setErrRate(30);
                                sg.setLevel(10);
                                callback.OnTunerInfo(tp, sg);
                            }
                            if (testProgress % 10 == 0) {
                                // List<stChannel> list = new
                                // ArrayList<stChannel>();
                                stChannel channel = new stChannel();
                                channel.setServiceName("CCTV" + testProgress);
                                channel.setServiceType((byte) 1);
                                // list.add(channel);
                                // callback.OnDVBService(list);
                                callback.OnDVBService(channel);
                            }
                            // if(testProgress%20 == 0){
                            //
                            // TuningParam tp = new TuningParam(642000, 6875,
                            // 3);
                            // tagTunerSignal ts = new tagTunerSignal();
                            // ts.setCN(250);
                            // ts.setErrRate(360);
                            // ts.setLevel(720);
                            //
                            // callback.OnTunerInfo(tp, ts);
                            //
                            // }
                        }
                        mainHandler.sendEmptyMessageDelayed(SEARCH_TEST, 200);
                    } else {
                        if (callback != null) {
                            testProgress = 0;
                            //callback.OnSTVComplete();
                        }
                    }
                    break;
            }
            super.handleMessage(msg);
        }

    };*/

    // 设置监听函数

    // 搜索结束监听
    private OnSearchEndListener onSearchEndListener;

    public void setOnSearchEndListener(OnSearchEndListener onSearchEndListener) {

        this.onSearchEndListener = onSearchEndListener;
    }

    public interface OnSearchEndListener {

        public void onSearchEnd(List<stChannel> channels);

    };

    // 搜到新的频道监听
    private OnSearchFindNewChannelListener onSearchFindNewChannelListener;

    public void setOnSearchFindNewChannelListener(
            OnSearchFindNewChannelListener onSearchFindNewChannelListener) {

        this.onSearchFindNewChannelListener = onSearchFindNewChannelListener;
    }

    public interface OnSearchFindNewChannelListener {

        public boolean onFindNewChannel(String name, int type);

    };

    // 搜索到新的频点监听
    private OnSearchNewTransponderListener onSearchNewTransponderListener;

    public void setOnSearchNewTransponderListener(
            OnSearchNewTransponderListener onSearchNewTransponderListener) {

        this.onSearchNewTransponderListener = onSearchNewTransponderListener;
    }

    public interface OnSearchNewTransponderListener {

        public void onSearchNewTransponder(TuningParam transponder, tagTunerSignal signal);

    };

    // 搜索进度监听
    private OnSearchProgressChangeListener onSearchProgressChangeListener;

    public void setOnSearchProgressChangeListener(
            OnSearchProgressChangeListener onSearchProgressChangeListener) {

        this.onSearchProgressChangeListener = onSearchProgressChangeListener;
    }

    public interface OnSearchProgressChangeListener {

        public void onSearchProgressChanged(int arg0);

    };

    /**
     * 保存TuningParam频点信息
     * 
     * @param transponder
     */
    private void saveTp(TuningParam transponder) {
        tpTemp.add(transponder.getFrequency());
        // 如果存在这个频点的话，先删除，再保存
        Cursor cur = getContentResolver().query(Channel.URI.TABLE_TRANSPONDERS,
                null,
                Channel.TableTranspondersColumns.FREQUENCY + "= " + transponder.getFrequency(),
                null, null);
        log.D("@dingran@ cur == " + cur);
        if (cur != null) {
            log.D("@dingran@ cur count== " + cur.getCount());
            if (cur.getCount() > 0) {
                cur.moveToFirst();
                if(mCurrentSearchType == SearchMainActivity.SEARCH_TYPE_MANUAL){
                    log.D("@SongWenxuan  search manual save LogicChNumber");
                    int id = cur.getInt(cur.getColumnIndex("_id"));
                    Cursor query = getContentResolver().query(Channel.URI.TABLE_CHANNELS, new String[]{"ServiceName","LogicChNumber"}, "Transponder_id=?", new String [] {""+id}, null);
                    while(query.moveToNext()){
                        map.put(query.getString(query.getColumnIndex("ServiceName")), query.getInt(query.getColumnIndex("LogicChNumber")));
                    }
                }
                getContentResolver().delete(
                        Channel.URI.TABLE_TRANSPONDERS,
                        Channel.TableTranspondersColumns.FREQUENCY + "= "
                                + transponder.getFrequency(),
                        null);
            }
            cur.close();
        }
        ContentValues values = new ContentValues();
        values.put(Channel.TableTranspondersColumns.FREQUENCY, transponder.getFrequency());
        values.put(Channel.TableTranspondersColumns.MODULATION, transponder.getModulation());
        values.put(Channel.TableTranspondersColumns.SYMBOLRATE, transponder.getSymbolRate());
        getContentResolver().insert(Channel.URI.TABLE_TRANSPONDERS, values);
        // 查出上面插入的的transponder的ID
        Cursor cursor = getContentResolver().query(Channel.URI.TABLE_TRANSPONDERS,
                null,
                Channel.TableTranspondersColumns.FREQUENCY + "= " + transponder.getFrequency(),
                null, null);
        log.D("@dingran@ cursor == " + cursor);
        if (cursor != null) {
            log.D("@dingran@ cursor count== " + cursor.getCount());
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                mTransponderId = cursor.getInt(cursor
                        .getColumnIndex(Channel.TableTranspondersColumns.ID));
                log.D("queryed transponderId = " + mTransponderId);
            }
            cursor.close();
        }
    }

    /**
     * 保存频道到数据库，事务存储
     * 
     * @param channelsList
     */
    public void saveChannels(List<stChannel> channelsList) {
        log.D("saveChannels channelsList size = " + channelsList.size());
        
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        
        for (int i = 0; i < channelsList.size(); i++) {
            ops.add(ContentProviderOperation.newInsert(Channel.URI.TABLE_CHANNELS)
                    .withValues(changeToConentValues(channelsList.get(i))).build());
        }
        // 处理事务
        try {
            getContentResolver().applyBatch(Channel.AUTHORITY, ops);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将stChannel转换成对应的ContentValues
     * 
     * @param channel
     * @return
     */
    private ContentValues changeToConentValues(stChannel channel) {
        
        ContentValues values = new ContentValues();
        
        values.put(Channel.TableChannelsColumns.AUDIOECMPID + 0, channel.getAudioTrack()[0].getEcmPid());
        values.put(Channel.TableChannelsColumns.AUDIOECMPID + 1, channel.getAudioTrack()[1].getEcmPid());
        values.put(Channel.TableChannelsColumns.AUDIOECMPID + 2, channel.getAudioTrack()[2].getEcmPid());
        values.put(Channel.TableChannelsColumns.AUDIOECMPID + 3, 0);
        
        values.put(Channel.TableChannelsColumns.AUDIOLANGCODE + 0, 0);
        values.put(Channel.TableChannelsColumns.AUDIOLANGCODE + 1, 0);
        values.put(Channel.TableChannelsColumns.AUDIOLANGCODE + 2, 0);
        values.put(Channel.TableChannelsColumns.AUDIOLANGCODE + 3, 0);
        
        values.put(Channel.TableChannelsColumns.AUDIOPESTYPE + 0, channel.getAudioTrack()[0].getPesType());
        values.put(Channel.TableChannelsColumns.AUDIOPESTYPE + 1, channel.getAudioTrack()[1].getPesType());
        values.put(Channel.TableChannelsColumns.AUDIOPESTYPE + 2, channel.getAudioTrack()[2].getPesType());
        values.put(Channel.TableChannelsColumns.AUDIOPESTYPE + 3, 0);
        
        values.put(Channel.TableChannelsColumns.AUDIOSTREAMPID + 0, channel.getAudioTrack()[0].getStreamPid());
        values.put(Channel.TableChannelsColumns.AUDIOSTREAMPID + 1, channel.getAudioTrack()[1].getStreamPid());
        values.put(Channel.TableChannelsColumns.AUDIOSTREAMPID + 2, channel.getAudioTrack()[2].getStreamPid());
        values.put(Channel.TableChannelsColumns.AUDIOSTREAMPID + 3, 0);
        
        values.put(Channel.TableChannelsColumns.AUDIOSTREAMSIZE, 4);
        
        values.put(Channel.TableChannelsColumns.BOUQUET_ID, 0);
        
        values.put(Channel.TableChannelsColumns.CAECMPID + 0, 0);
        values.put(Channel.TableChannelsColumns.CAECMPID + 1, 0);
        values.put(Channel.TableChannelsColumns.CAECMPID + 2, 0);
        values.put(Channel.TableChannelsColumns.CAECMPID + 3, 0);
        
        values.put(Channel.TableChannelsColumns.CAECMPIDSIZE, 0);
        
        values.put(Channel.TableChannelsColumns.CASYSTEMID + 0, 0);
        values.put(Channel.TableChannelsColumns.CASYSTEMID + 1, 0);
        values.put(Channel.TableChannelsColumns.CASYSTEMID + 2, 0);
        values.put(Channel.TableChannelsColumns.CASYSTEMID + 3, 0);
        
        values.put(Channel.TableChannelsColumns.CASYSTEMIDSIZE, 0);
        
        values.put(Channel.TableChannelsColumns.FAVORITE, 0);
        values.put(Channel.TableChannelsColumns.AUDIOINDEX, 0);
        // values.put(Channel.TableChannelsColumns.ID, 1);
        values.put(Channel.TableChannelsColumns.LOCK, 0);
        //手动搜索需要之前的频道号
        if(mCurrentSearchType == SearchMainActivity.SEARCH_TYPE_MANUAL){
            Integer logicChNumber=map.get(channel.getServiceName());
            //如果之前的频道里没有包含新搜处理的频道。
            if(logicChNumber == null){
                //如果是广播类型，则依然存储广播自己的频道号。
                if(channel.getServiceType()==2){
                    values.put(Channel.TableChannelsColumns.LOGICCHNUMBER, channel.getLogicChNumber()+bcCount);
                }else {
                    values.put(Channel.TableChannelsColumns.LOGICCHNUMBER,channel.getLogicChNumber()+tvCount);
                }
            }else{
                values.put(Channel.TableChannelsColumns.LOGICCHNUMBER, map.get(channel.getServiceName()));
            }
        }else {
            values.put(Channel.TableChannelsColumns.LOGICCHNUMBER, channel.getLogicChNumber());
        }
        values.put(Channel.TableChannelsColumns.ORGNETID, channel.getServiceIdent().getOrgNetId());
        values.put(Channel.TableChannelsColumns.PCRPID, String.valueOf(channel.getPcrPid()));
        values.put(Channel.TableChannelsColumns.EMMPID, String.valueOf(channel.getEmmPid()));
        values.put(Channel.TableChannelsColumns.PMTPID, String.valueOf(channel.getPmtPid()));
        values.put(Channel.TableChannelsColumns.PMTVERSION, channel.getPmtVersion());
        values.put(Channel.TableChannelsColumns.PROVIDERNAME, 0);
        values.put(Channel.TableChannelsColumns.SERVICEID, channel.getServiceIdent().getServiceId());
        values.put(Channel.TableChannelsColumns.SERVICENAME, channel.getServiceName());
        values.put(Channel.TableChannelsColumns.SERVICEORGTYPE, 0);
        values.put(Channel.TableChannelsColumns.SERVICETYPE, channel.getServiceType());
        
        values.put(Channel.TableChannelsColumns.SUBTITLEECMPID + 0, 0);
        values.put(Channel.TableChannelsColumns.SUBTITLEECMPID + 1, 0);
        values.put(Channel.TableChannelsColumns.SUBTITLEECMPID + 2, 0);
        values.put(Channel.TableChannelsColumns.SUBTITLEECMPID + 3, 0);
        
        values.put(Channel.TableChannelsColumns.SUBTITLEPESTYPE + 0, 0);
        values.put(Channel.TableChannelsColumns.SUBTITLEPESTYPE + 1, 0);
        values.put(Channel.TableChannelsColumns.SUBTITLEPESTYPE + 2, 0);
        values.put(Channel.TableChannelsColumns.SUBTITLEPESTYPE + 3, 0);
        
        values.put(Channel.TableChannelsColumns.SUBTITLESTREAMDESC + 0, 0);
        values.put(Channel.TableChannelsColumns.SUBTITLESTREAMDESC + 1, 0);
        values.put(Channel.TableChannelsColumns.SUBTITLESTREAMDESC + 2, 0);
        values.put(Channel.TableChannelsColumns.SUBTITLESTREAMDESC + 3, 0);
        
        values.put(Channel.TableChannelsColumns.SUBTITLESTREAMPID + 0, 0);
        values.put(Channel.TableChannelsColumns.SUBTITLESTREAMPID + 1, 0);
        values.put(Channel.TableChannelsColumns.SUBTITLESTREAMPID + 2, 0);
        values.put(Channel.TableChannelsColumns.SUBTITLESTREAMPID + 3, 0);
        
        values.put(Channel.TableChannelsColumns.SUBTITLESTREAMSIZE, 0);
        
        values.put(Channel.TableChannelsColumns.TELETEXTECMPID + 0, 0);
        values.put(Channel.TableChannelsColumns.TELETEXTECMPID + 1, 0);
        values.put(Channel.TableChannelsColumns.TELETEXTECMPID + 2, 0);
        values.put(Channel.TableChannelsColumns.TELETEXTECMPID + 3, 0);
        
        values.put(Channel.TableChannelsColumns.TELETEXTPESTYPE + 0, 0);
        values.put(Channel.TableChannelsColumns.TELETEXTPESTYPE + 1, 0);
        values.put(Channel.TableChannelsColumns.TELETEXTPESTYPE + 2, 0);
        values.put(Channel.TableChannelsColumns.TELETEXTPESTYPE + 3, 0);
        
        values.put(Channel.TableChannelsColumns.TELETEXTSTREAMDESC + 0, channel.getTeletext()[0].getStreamDesc());
        values.put(Channel.TableChannelsColumns.TELETEXTSTREAMDESC + 1, channel.getTeletext()[1].getStreamDesc());
        values.put(Channel.TableChannelsColumns.TELETEXTSTREAMDESC + 2, channel.getTeletext()[2].getStreamDesc());
        values.put(Channel.TableChannelsColumns.TELETEXTSTREAMDESC + 3, 0);
        
        values.put(Channel.TableChannelsColumns.TELETEXTSTREAMPID + 0, 0);
        values.put(Channel.TableChannelsColumns.TELETEXTSTREAMPID + 1, 0);
        values.put(Channel.TableChannelsColumns.TELETEXTSTREAMPID + 2, 0);
        values.put(Channel.TableChannelsColumns.TELETEXTSTREAMPID + 3, 0);
        
        values.put(Channel.TableChannelsColumns.TELETEXTSTREAMSIZE, 1);
        
        values.put(Channel.TableChannelsColumns.TRANSPONDER_ID, getTransponderId(channel.getFrequency()));
        //values.put(Channel.TableChannelsColumns.TSID, 1);
        values.put(Channel.TableChannelsColumns.VIDEOECMPID, channel.getVideoTrack().getEcmPid());
        values.put(Channel.TableChannelsColumns.VIDEOPESTYPE, channel.getVideoTrack().getPesType());
        values.put(Channel.TableChannelsColumns.VIDEOSTREAMPID, channel.getVideoTrack().getStreamPid());
        values.put(Channel.TableChannelsColumns.VOLUME, 20);
        
        return values;
    }

    private int getTransponderId(int frequency){
        int transponderId = 0;
        Cursor cursor = getContentResolver().query(Channel.URI.TABLE_TRANSPONDERS,
                null,
                Channel.TableTranspondersColumns.FREQUENCY + "= " + frequency,
                null, null);
        log.D("@dingran@ cursor == " + cursor);
        if (cursor != null) {
            log.D("@dingran@ cursor count== " + cursor.getCount());
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                transponderId = cursor.getInt(cursor.getColumnIndex(Channel.TableTranspondersColumns.ID));
                log.D("queryed transponderId = " + mTransponderId);
            }
            cursor.close();
        }
        return transponderId;
    }

    public void deleteChannels() {
        getContentResolver().delete(Channel.URI.TABLE_TRANSPONDERS, null, null);
    }
}
