package novel.supertv.dvb.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DvbDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "novel.supertv.dvb.provider.DatabaseHelper";

    public final static byte[] _writeLock = new byte[0];

    /**
     * 数据库版本号，用于数据库升级
     */
    private static final int DB_VERSION = 17;

    /**
     * 构造，由Provider使用
     * @param context
     * @param name
     */
    public DvbDatabaseHelper(Context context, String name) {
        super(context, name, null, DB_VERSION);
        Log.d(TAG,"this is DvbDatabaseHelper " );
    }

    /**
     * 初始化数据,主要是拼接用于在onCreate()中执行的字段
     */
    public void initData() {

        // 拼接删除表的字段，用于onUpgrade中升级数据库使用
        DvbDataContent.DROP_DATA_TRANSPONDERS = "DROP TABLE IF EXISTS "
                + DvbDataContent.TABLE_TRANSPONDERS;

        DvbDataContent.DROP_DATA_TRIGGER_TP_DEL_CAL = "DROP TRIGGER IF EXISTS "
                + DvbDataContent.TRIGGER_TP_DEL_CAL;

        DvbDataContent.DROP_DATA_TRIGGER_CAL_ADD_TP = "DROP TRIGGER IF EXISTS "
                + DvbDataContent.TRIGGER_CAL_ADD_TP;

        DvbDataContent.DROP_DATA_TRIGGER_CAL_DEL_RES = "DROP TRIGGER IF EXISTS "
                + DvbDataContent.TRIGGER_CAL_DEL_RES;

        DvbDataContent.DROP_DATA_VIEW_CHANNELS = "DROP VIEW IF EXISTS  "
                + DvbDataContent.VIEW_CHANNELS;

        DvbDataContent.DROP_DATA_CHANNELS = "DROP TABLE IF EXISTS "
                + DvbDataContent.TABLE_CHANNELS;

        // 创建频道表
        
        DvbDataContent.DATA_CHANNELS = "create table "
                + DvbDataContent.TABLE_CHANNELS + "(" + Channel.TableChannelsColumns.ID
                + " integer PRIMARY KEY," + Channel.TableChannelsColumns.TRANSPONDER_ID
                + "  integer ," + Channel.TableChannelsColumns.BOUQUET_ID + " integer  ,"
                + Channel.TableChannelsColumns.LOGICCHNUMBER + " integer ,"
                + Channel.TableChannelsColumns.SERVICENAME + " varchar ,"
                + Channel.TableChannelsColumns.SERVICETYPE + "  integer , "
                + Channel.TableChannelsColumns.SERVICEORGTYPE + " integer , "
                + Channel.TableChannelsColumns.PROVIDERNAME + "  varchar , "
                + Channel.TableChannelsColumns.PCRPID + "  integer , "
                + Channel.TableChannelsColumns.EMMPID + "  integer , "
                + Channel.TableChannelsColumns.PMTPID + "  integer , "
                + Channel.TableChannelsColumns.PMTVERSION + " integer,"
                + Channel.TableChannelsColumns.VOLBALANCE + " integer,"
                + Channel.TableChannelsColumns.VOLCOMPENSATION + " integer,"
                + Channel.TableChannelsColumns.FAVORITE + "  integer ,"
                + Channel.TableChannelsColumns.LOCK + " integer," + Channel.TableChannelsColumns.VOLUME
                + "  integer," + Channel.TableChannelsColumns.SERVICEID + "  integer , "
                + Channel.TableChannelsColumns.TSID + " integer , "
                + Channel.TableChannelsColumns.ORGNETID + " integer , "
                + Channel.TableChannelsColumns.AUDIOINDEX + " integer , "
                + Channel.TableChannelsColumns.VIDEOSTREAMPID + "  varchar , "
                + Channel.TableChannelsColumns.VIDEOECMPID + " integer , "
                + Channel.TableChannelsColumns.VIDEOPESTYPE + " integer , ";

        for (int i = 0; i < Channel.TableChannelsColumns.SERVICEDATAAUDIOSTREAMCOLUMNSIZE; i++) {
            DvbDataContent.DATA_CHANNELS += (""
                    + Channel.TableChannelsColumns.AUDIOSTREAMPID + i + " integer,  "
                    + Channel.TableChannelsColumns.AUDIOECMPID + i + " integer , "
                    + Channel.TableChannelsColumns.AUDIOPESTYPE + i + " integer , "
                    + Channel.TableChannelsColumns.AUDIOLANGCODE + i + " integer , ");
        }

        DvbDataContent.DATA_CHANNELS += Channel.TableChannelsColumns.AUDIOSTREAMSIZE
                + "  integer , ";

        for (int i = 0; i < Channel.TableChannelsColumns.SERVICEDATATELETEXTSTREAMCOLUMNSIZE; i++) {
            DvbDataContent.DATA_CHANNELS += (""
                    + Channel.TableChannelsColumns.TELETEXTSTREAMPID + i + " integer, "
                    + Channel.TableChannelsColumns.TELETEXTECMPID + i + " integer, "
                    + Channel.TableChannelsColumns.TELETEXTPESTYPE + i + " integer ,"
                    + Channel.TableChannelsColumns.TELETEXTSTREAMDESC + i + " varchar , ");
        }

        DvbDataContent.DATA_CHANNELS += Channel.TableChannelsColumns.TELETEXTSTREAMSIZE
                + "  integer , ";

        for (int i = 0; i < Channel.TableChannelsColumns.SERVICEDATASUBTITLESTREAMCOLUMNSIZE; i++) {
            DvbDataContent.DATA_CHANNELS += (""
                    + Channel.TableChannelsColumns.SUBTITLESTREAMPID + i + " integer, "
                    + Channel.TableChannelsColumns.SUBTITLEECMPID + i + " integer, "
                    + Channel.TableChannelsColumns.SUBTITLEPESTYPE + i + " integer , "
                    + Channel.TableChannelsColumns.SUBTITLESTREAMDESC + i + " integer , ");

        }

        DvbDataContent.DATA_CHANNELS += Channel.TableChannelsColumns.SUBTITLESTREAMSIZE
                + "  integer , ";

        for (int i = 0; i < Channel.TableChannelsColumns.SERVICEDATACASYSTEMIDCOLUMNSIZE; i++) {
            DvbDataContent.DATA_CHANNELS += (Channel.TableChannelsColumns.CASYSTEMID + i + " integer, ");

        }

        DvbDataContent.DATA_CHANNELS += Channel.TableChannelsColumns.CASYSTEMIDSIZE
                + " integer , ";

        for (int i = 0; i < Channel.TableChannelsColumns.SERVICEDATACAECMPIDCOLUMNSIZE; i++) {
            DvbDataContent.DATA_CHANNELS += (Channel.TableChannelsColumns.CAECMPID + i + " integer, ");

        }
        DvbDataContent.DATA_CHANNELS += Channel.TableChannelsColumns.CAECMPIDSIZE
                + "  integer , ";

        DvbDataContent.DATA_CHANNELS += "constraint fk_TP_Cal  foreign key("
                + Channel.TableChannelsColumns.TRANSPONDER_ID + ") references "
                + DvbDataContent.TABLE_TRANSPONDERS + "(_id))";

        // 创建频点表
        
        DvbDataContent.DATA_TRANSPONDERS = "create table "
                + DvbDataContent.TABLE_TRANSPONDERS + "("
                + Channel.TableTranspondersColumns.ID + " integer PRIMARY KEY, "
                + Channel.TableTranspondersColumns.STLNBS_ID + " integer ,"
                + Channel.TableTranspondersColumns.FREQUENCY + " integer, "
                + Channel.TableTranspondersColumns.MODULATION + " integer, "
                + Channel.TableTranspondersColumns.SYMBOLRATE + " integer ,"
                + Channel.TableTranspondersColumns.NPATVERSION + "  integer , "
                + Channel.TableTranspondersColumns.NSDTVERSION + "  integer , "
                + Channel.TableTranspondersColumns.NCATVERSION + " integer , "
                + " constraint fk_tp_stlnb  foreign key("
                + Channel.TableTranspondersColumns.STLNBS_ID + ") references "
                + DvbDataContent.TABLE_STLNBS + "(_id) " + ")";

        DvbDataContent.DATA_TRIGGER_CAL_ADD_TP = "CREATE TRIGGER  "
                + DvbDataContent.TRIGGER_CAL_ADD_TP + " BEFORE INSERT "
                + " ON " + DvbDataContent.TABLE_CHANNELS
                + " FOR EACH ROW BEGIN" + " SELECT CASE WHEN ((SELECT _id "
                + " FROM " + DvbDataContent.TABLE_TRANSPONDERS + "  WHERE _id "
                + "=new." + "Transponder_id ) IS NULL)"
                + " THEN RAISE (ABORT,'Foreign Key Violation') END;" + "  END;";

        DvbDataContent.DATA_TRIGGER_TP_DEL_CAL = "CREATE TRIGGER  "
                + DvbDataContent.TRIGGER_TP_DEL_CAL + " BEFORE DELETE "
                + " ON " + DvbDataContent.TABLE_TRANSPONDERS
                + " FOR EACH ROW  " + "  BEGIN DELETE  FROM "
                + DvbDataContent.TABLE_CHANNELS + "  WHERE Transponder_id "
                + "=old." + "_id" + "; " + "  END;";

        // 创建视图
        DvbDataContent.DATA_VIEW_CHANNELS = "CREATE VIEW "
            + DvbDataContent.VIEW_CHANNELS + " AS SELECT "
            + DvbDataContent.TABLE_CHANNELS + ".*" + " ,"
            + DvbDataContent.TABLE_TRANSPONDERS + ".Frequency " + " ,"
            + DvbDataContent.TABLE_TRANSPONDERS + ".Modulation " + ", "
            + DvbDataContent.TABLE_TRANSPONDERS + ".SymbolRate " + " ,"
            + DvbDataContent.TABLE_TRANSPONDERS + ".nPATVersion " + ", "
            + DvbDataContent.TABLE_TRANSPONDERS + ".nSDTVersion " + " ,"
            + DvbDataContent.TABLE_TRANSPONDERS + ".nCATVersion " + " "
            + " FROM " + DvbDataContent.TABLE_CHANNELS + " , "
            + DvbDataContent.TABLE_TRANSPONDERS + "  "
            + " where "
            + DvbDataContent.TABLE_CHANNELS + ".Transponder_id" + " ="
            + DvbDataContent.TABLE_TRANSPONDERS + "._id  ";
        
        // 创建预约表
        DvbDataContent.DATA_RESERVE = "create table "
                + DvbDataContent.TABLE_RESERVES + "("
                + Channel.TableReservesColumns.ID + " integer PRIMARY KEY, "
                + Channel.TableReservesColumns.STARTTIME + " integer ,"
                + Channel.TableReservesColumns.ENDTIME + " integer, "
                + Channel.TableReservesColumns.PROGRAMNAME + " varchar, "
                + Channel.TableReservesColumns.SERVICEID + " integer, "
                + Channel.TableReservesColumns.CHANNELNAME + " integer"
                + ")";
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        
        initData();
        
        // 开始创建表
        try {
            Log.d(TAG,"this is DatabaseHelper = " + "DatabaseHelper");

            Log.d(TAG,"this is Data_Channels = " + DvbDataContent.DATA_CHANNELS);
            db.execSQL(DvbDataContent.DATA_CHANNELS);
            
            Log.d(TAG,"this is Data_Transponders = "
                    + DvbDataContent.DATA_TRANSPONDERS);
            db.execSQL(DvbDataContent.DATA_TRANSPONDERS);

            Log.d(TAG,"this is Data_VIEW_CHANNELS = "
                    + DvbDataContent.DATA_VIEW_CHANNELS);
            db.execSQL(DvbDataContent.DATA_VIEW_CHANNELS);

            Log.d(TAG,"this is Data_TRIGGER_TP_DEL_CAL = "
                    + DvbDataContent.DATA_TRIGGER_TP_DEL_CAL);
            db.execSQL(DvbDataContent.DATA_TRIGGER_TP_DEL_CAL);

            Log.d(TAG,"this is Data_TRIGGER_CAL_ADD_TP = "
                    + DvbDataContent.DATA_TRIGGER_CAL_ADD_TP);
            db.execSQL(DvbDataContent.DATA_TRIGGER_CAL_ADD_TP);
            
            Log.d(TAG, "this is Data_reserve = " + DvbDataContent.DATA_RESERVE);
            db.execSQL(DvbDataContent.DATA_RESERVE);
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // 初始化数据，假的
//        init_(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG,"Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");

        db.execSQL(DvbDataContent.DROP_DATA_CHANNELS);
        db.execSQL(DvbDataContent.DROP_DATA_TRANSPONDERS);
        db.execSQL(DvbDataContent.DROP_DATA_DEFAULT);
        db.execSQL(DvbDataContent.DROP_DATA_VIEW_CHANNELS);
        db.execSQL(DvbDataContent.DROP_DATA_TRIGGER_TP_DEL_CAL);
        db.execSQL(DvbDataContent.DROP_DATA_TRIGGER_CAL_ADD_TP);
        db.execSQL(DvbDataContent.DROP_DATA_TRIGGER_CAL_DEL_RES);

        onCreate(db);
    }

    /**
     * 用于测试，往数据库中增加内容的
     * 
     * 或者用于初始化假数据
     */
    public void init_(SQLiteDatabase db) {
        Log.d(TAG,"init_");
        
        // 从OnCreate中不能使用这个，会报错，如：
        // Caused by: java.lang.IllegalStateException: getWritableDatabase called recursively
//        SQLiteDatabase db = this.getWritableDatabase();

//        ContentValues values = new ContentValues();
//        for (int i = 0; i < 20; i++) {
//            values.put("Frequency", i * 10);
//            values.put("stLNBs_id", i + 1);
//            db.insert(DvbDataContent.TABLE_TRANSPONDERS, null, values);
//        }

        
        ContentValues value = new ContentValues();
        
        value.put(Channel.TableTranspondersColumns.ID, 0);
        
        value.put(Channel.TableTranspondersColumns.FREQUENCY, 333);
        value.put(Channel.TableTranspondersColumns.MODULATION, 64);
        value.put(Channel.TableTranspondersColumns.SYMBOLRATE, 6875);
        
        db.insert(DvbDataContent.TABLE_TRANSPONDERS,null, value);
        
        ContentValues values = new ContentValues();
        
        
        
        values.put(Channel.TableChannelsColumns.AUDIOECMPID+0, 1);
        values.put(Channel.TableChannelsColumns.AUDIOECMPID+1, 1);
        values.put(Channel.TableChannelsColumns.AUDIOECMPID+2, 1);
        values.put(Channel.TableChannelsColumns.AUDIOECMPID+3, 1);
        
        values.put(Channel.TableChannelsColumns.AUDIOLANGCODE+0, 1);
        values.put(Channel.TableChannelsColumns.AUDIOLANGCODE+1, 1);
        values.put(Channel.TableChannelsColumns.AUDIOLANGCODE+2, 1);
        values.put(Channel.TableChannelsColumns.AUDIOLANGCODE+3, 1);
        
        values.put(Channel.TableChannelsColumns.AUDIOPESTYPE+0, 1);
        values.put(Channel.TableChannelsColumns.AUDIOPESTYPE+1, 1);
        values.put(Channel.TableChannelsColumns.AUDIOPESTYPE+2, 1);
        values.put(Channel.TableChannelsColumns.AUDIOPESTYPE+3, 1);
        
        values.put(Channel.TableChannelsColumns.AUDIOSTREAMPID+0, 1);
        values.put(Channel.TableChannelsColumns.AUDIOSTREAMPID+1, 1);
        values.put(Channel.TableChannelsColumns.AUDIOSTREAMPID+2, 1);
        values.put(Channel.TableChannelsColumns.AUDIOSTREAMPID+3, 1);
        
        values.put(Channel.TableChannelsColumns.AUDIOSTREAMSIZE, 4);
        
        values.put(Channel.TableChannelsColumns.BOUQUET_ID, 1);
        
        values.put(Channel.TableChannelsColumns.CAECMPID+0, 1);
        values.put(Channel.TableChannelsColumns.CAECMPID+1, 1);
        values.put(Channel.TableChannelsColumns.CAECMPID+2, 1);
        values.put(Channel.TableChannelsColumns.CAECMPID+3, 1);
        
        values.put(Channel.TableChannelsColumns.CAECMPIDSIZE, 1);
        
        values.put(Channel.TableChannelsColumns.CASYSTEMID+0, 1);
        values.put(Channel.TableChannelsColumns.CASYSTEMID+1, 1);
        values.put(Channel.TableChannelsColumns.CASYSTEMID+2, 1);
        values.put(Channel.TableChannelsColumns.CASYSTEMID+3, 1);
        
        values.put(Channel.TableChannelsColumns.CASYSTEMIDSIZE, 1);
        
        values.put(Channel.TableChannelsColumns.FAVORITE, 1);
        values.put(Channel.TableChannelsColumns.AUDIOINDEX, 1);
//        values.put(Channel.TableChannelsColumns.ID, 1);
        values.put(Channel.TableChannelsColumns.LOCK, 1);
        values.put(Channel.TableChannelsColumns.LOGICCHNUMBER, 1);
        
        values.put(Channel.TableChannelsColumns.ORGNETID, 1);
        values.put(Channel.TableChannelsColumns.PCRPID, 1);
        values.put(Channel.TableChannelsColumns.PMTPID, 1);
        values.put(Channel.TableChannelsColumns.PMTVERSION, 1);
        values.put(Channel.TableChannelsColumns.PROVIDERNAME, 1);
        values.put(Channel.TableChannelsColumns.SERVICEID, 1);
        values.put(Channel.TableChannelsColumns.SERVICENAME, "江苏卫视");
        values.put(Channel.TableChannelsColumns.SERVICEORGTYPE, 1);
        values.put(Channel.TableChannelsColumns.SERVICETYPE, 1);
        
        values.put(Channel.TableChannelsColumns.SUBTITLEECMPID+0, 1);
        values.put(Channel.TableChannelsColumns.SUBTITLEECMPID+1, 1);
        values.put(Channel.TableChannelsColumns.SUBTITLEECMPID+2, 1);
        values.put(Channel.TableChannelsColumns.SUBTITLEECMPID+3, 1);
        
        values.put(Channel.TableChannelsColumns.SUBTITLEPESTYPE+0, 1);
        values.put(Channel.TableChannelsColumns.SUBTITLEPESTYPE+1, 1);
        values.put(Channel.TableChannelsColumns.SUBTITLEPESTYPE+2, 1);
        values.put(Channel.TableChannelsColumns.SUBTITLEPESTYPE+3, 1);
        
        values.put(Channel.TableChannelsColumns.SUBTITLESTREAMDESC+0, 1);
        values.put(Channel.TableChannelsColumns.SUBTITLESTREAMDESC+1, 1);
        values.put(Channel.TableChannelsColumns.SUBTITLESTREAMDESC+2, 1);
        values.put(Channel.TableChannelsColumns.SUBTITLESTREAMDESC+3, 1);
        
        values.put(Channel.TableChannelsColumns.SUBTITLESTREAMPID+0, 1);
        values.put(Channel.TableChannelsColumns.SUBTITLESTREAMPID+1, 1);
        values.put(Channel.TableChannelsColumns.SUBTITLESTREAMPID+2, 1);
        values.put(Channel.TableChannelsColumns.SUBTITLESTREAMPID+3, 1);
        
        values.put(Channel.TableChannelsColumns.SUBTITLESTREAMSIZE, 1);
        
        
        
        values.put(Channel.TableChannelsColumns.TELETEXTECMPID+0, 1);
        values.put(Channel.TableChannelsColumns.TELETEXTECMPID+1, 1);
        values.put(Channel.TableChannelsColumns.TELETEXTECMPID+2, 1);
        values.put(Channel.TableChannelsColumns.TELETEXTECMPID+3, 1);
        
        values.put(Channel.TableChannelsColumns.TELETEXTPESTYPE+0, 1);
        values.put(Channel.TableChannelsColumns.TELETEXTPESTYPE+1, 1);
        values.put(Channel.TableChannelsColumns.TELETEXTPESTYPE+2, 1);
        values.put(Channel.TableChannelsColumns.TELETEXTPESTYPE+3, 1);
        
        values.put(Channel.TableChannelsColumns.TELETEXTSTREAMDESC+0, 1);
        values.put(Channel.TableChannelsColumns.TELETEXTSTREAMDESC+1, 1);
        values.put(Channel.TableChannelsColumns.TELETEXTSTREAMDESC+2, 1);
        values.put(Channel.TableChannelsColumns.TELETEXTSTREAMDESC+3, 1);
        
        values.put(Channel.TableChannelsColumns.TELETEXTSTREAMPID+0, 1);
        values.put(Channel.TableChannelsColumns.TELETEXTSTREAMPID+1, 1);
        values.put(Channel.TableChannelsColumns.TELETEXTSTREAMPID+2, 1);
        values.put(Channel.TableChannelsColumns.TELETEXTSTREAMPID+3, 1);
        
        values.put(Channel.TableChannelsColumns.TELETEXTSTREAMSIZE, 1);
        
        values.put(Channel.TableChannelsColumns.TRANSPONDER_ID, 0);
        
        values.put(Channel.TableChannelsColumns.TSID, 1);
        values.put(Channel.TableChannelsColumns.VIDEOECMPID, 1);
        values.put(Channel.TableChannelsColumns.VIDEOPESTYPE, 1);
        values.put(Channel.TableChannelsColumns.VIDEOSTREAMPID, 1);
        values.put(Channel.TableChannelsColumns.VOLUME, 1);
        
        
        // 这些不是频道表的字段，只是标记长度信息的值
        
//        values.put(Channel.TableChannelsColumns.SERVICEDATAAUDIOSTREAMCOLUMNSIZE, 1);
//        values.put(Channel.TableChannelsColumns.SERVICEDATACAECMPIDCOLUMNSIZE, 1);
//        values.put(Channel.TableChannelsColumns.SERVICEDATASUBTITLESTREAMCOLUMNSIZE, 1);
//        values.put(Channel.TableChannelsColumns.SERVICEDATATELETEXTSTREAMCOLUMNSIZE, 1);
        
        
        db.insert(DvbDataContent.TABLE_CHANNELS,null, values);
        
//        values = new ContentValues();
//        for (int i = 0; i < 20; i++) {
//            values.put("Transponder_id", i + 1);
//            values.put("ServiceName", "C " + i);
//            db.insert(DvbDataContent.TABLE_CHANNELS, null, values);
//        }

        // 千万别在onCreate里面调用db.close()，否则得到的数据库无法open ！！！
//        db.close();
        
        
        ContentValues values2 = new ContentValues();
        
        
        
        values2.put(Channel.TableChannelsColumns.AUDIOECMPID+0, 1);
        values2.put(Channel.TableChannelsColumns.AUDIOECMPID+1, 1);
        values2.put(Channel.TableChannelsColumns.AUDIOECMPID+2, 1);
        values2.put(Channel.TableChannelsColumns.AUDIOECMPID+3, 1);
        
        values2.put(Channel.TableChannelsColumns.AUDIOLANGCODE+0, 1);
        values2.put(Channel.TableChannelsColumns.AUDIOLANGCODE+1, 1);
        values2.put(Channel.TableChannelsColumns.AUDIOLANGCODE+2, 1);
        values2.put(Channel.TableChannelsColumns.AUDIOLANGCODE+3, 1);
        
        values2.put(Channel.TableChannelsColumns.AUDIOPESTYPE+0, 1);
        values2.put(Channel.TableChannelsColumns.AUDIOPESTYPE+1, 1);
        values2.put(Channel.TableChannelsColumns.AUDIOPESTYPE+2, 1);
        values2.put(Channel.TableChannelsColumns.AUDIOPESTYPE+3, 1);
        
        values2.put(Channel.TableChannelsColumns.AUDIOSTREAMPID+0, 1);
        values2.put(Channel.TableChannelsColumns.AUDIOSTREAMPID+1, 1);
        values2.put(Channel.TableChannelsColumns.AUDIOSTREAMPID+2, 1);
        values2.put(Channel.TableChannelsColumns.AUDIOSTREAMPID+3, 1);
        
        values2.put(Channel.TableChannelsColumns.AUDIOSTREAMSIZE, 4);
        
        values2.put(Channel.TableChannelsColumns.BOUQUET_ID, 1);
        
        values2.put(Channel.TableChannelsColumns.CAECMPID+0, 1);
        values2.put(Channel.TableChannelsColumns.CAECMPID+1, 1);
        values2.put(Channel.TableChannelsColumns.CAECMPID+2, 1);
        values2.put(Channel.TableChannelsColumns.CAECMPID+3, 1);
        
        values2.put(Channel.TableChannelsColumns.CAECMPIDSIZE, 1);
        
        values2.put(Channel.TableChannelsColumns.CASYSTEMID+0, 1);
        values2.put(Channel.TableChannelsColumns.CASYSTEMID+1, 1);
        values2.put(Channel.TableChannelsColumns.CASYSTEMID+2, 1);
        values2.put(Channel.TableChannelsColumns.CASYSTEMID+3, 1);
        
        values2.put(Channel.TableChannelsColumns.CASYSTEMIDSIZE, 1);
        
        values2.put(Channel.TableChannelsColumns.FAVORITE, 1);
//        values.put(Channel.TableChannelsColumns.ID, 1);
        values2.put(Channel.TableChannelsColumns.LOCK, 1);
        values2.put(Channel.TableChannelsColumns.LOGICCHNUMBER, 2);
        
        values2.put(Channel.TableChannelsColumns.ORGNETID, 1);
        values2.put(Channel.TableChannelsColumns.PCRPID, 1);
        values2.put(Channel.TableChannelsColumns.PMTPID, 1);
        values2.put(Channel.TableChannelsColumns.PMTVERSION, 1);
        values2.put(Channel.TableChannelsColumns.PROVIDERNAME, 1);
        values2.put(Channel.TableChannelsColumns.SERVICEID, 1);
        values2.put(Channel.TableChannelsColumns.SERVICENAME, "CCTV1");
        values2.put(Channel.TableChannelsColumns.SERVICEORGTYPE, 1);
        values2.put(Channel.TableChannelsColumns.SERVICETYPE, 1);
        
        values2.put(Channel.TableChannelsColumns.SUBTITLEECMPID+0, 1);
        values2.put(Channel.TableChannelsColumns.SUBTITLEECMPID+1, 1);
        values2.put(Channel.TableChannelsColumns.SUBTITLEECMPID+2, 1);
        values2.put(Channel.TableChannelsColumns.SUBTITLEECMPID+3, 1);
        
        values2.put(Channel.TableChannelsColumns.SUBTITLEPESTYPE+0, 1);
        values2.put(Channel.TableChannelsColumns.SUBTITLEPESTYPE+1, 1);
        values2.put(Channel.TableChannelsColumns.SUBTITLEPESTYPE+2, 1);
        values2.put(Channel.TableChannelsColumns.SUBTITLEPESTYPE+3, 1);
        
        values2.put(Channel.TableChannelsColumns.SUBTITLESTREAMDESC+0, 1);
        values2.put(Channel.TableChannelsColumns.SUBTITLESTREAMDESC+1, 1);
        values2.put(Channel.TableChannelsColumns.SUBTITLESTREAMDESC+2, 1);
        values2.put(Channel.TableChannelsColumns.SUBTITLESTREAMDESC+3, 1);
        
        values2.put(Channel.TableChannelsColumns.SUBTITLESTREAMPID+0, 1);
        values2.put(Channel.TableChannelsColumns.SUBTITLESTREAMPID+1, 1);
        values2.put(Channel.TableChannelsColumns.SUBTITLESTREAMPID+2, 1);
        values2.put(Channel.TableChannelsColumns.SUBTITLESTREAMPID+3, 1);
        
        values2.put(Channel.TableChannelsColumns.SUBTITLESTREAMSIZE, 1);
        
        
        
        values2.put(Channel.TableChannelsColumns.TELETEXTECMPID+0, 1);
        values2.put(Channel.TableChannelsColumns.TELETEXTECMPID+1, 1);
        values2.put(Channel.TableChannelsColumns.TELETEXTECMPID+2, 1);
        values2.put(Channel.TableChannelsColumns.TELETEXTECMPID+3, 1);
        
        values2.put(Channel.TableChannelsColumns.TELETEXTPESTYPE+0, 1);
        values2.put(Channel.TableChannelsColumns.TELETEXTPESTYPE+1, 1);
        values2.put(Channel.TableChannelsColumns.TELETEXTPESTYPE+2, 1);
        values2.put(Channel.TableChannelsColumns.TELETEXTPESTYPE+3, 1);
        
        values2.put(Channel.TableChannelsColumns.TELETEXTSTREAMDESC+0, 1);
        values2.put(Channel.TableChannelsColumns.TELETEXTSTREAMDESC+1, 1);
        values2.put(Channel.TableChannelsColumns.TELETEXTSTREAMDESC+2, 1);
        values2.put(Channel.TableChannelsColumns.TELETEXTSTREAMDESC+3, 1);
        
        values2.put(Channel.TableChannelsColumns.TELETEXTSTREAMPID+0, 1);
        values2.put(Channel.TableChannelsColumns.TELETEXTSTREAMPID+1, 1);
        values2.put(Channel.TableChannelsColumns.TELETEXTSTREAMPID+2, 1);
        values2.put(Channel.TableChannelsColumns.TELETEXTSTREAMPID+3, 1);
        
        values2.put(Channel.TableChannelsColumns.TELETEXTSTREAMSIZE, 1);
        
        values2.put(Channel.TableChannelsColumns.TRANSPONDER_ID, 0);
        
        values2.put(Channel.TableChannelsColumns.TSID, 1);
        values2.put(Channel.TableChannelsColumns.VIDEOECMPID, 1);
        values2.put(Channel.TableChannelsColumns.VIDEOPESTYPE, 1);
        values2.put(Channel.TableChannelsColumns.VIDEOSTREAMPID, 1);
        values2.put(Channel.TableChannelsColumns.VOLUME, 1);
        
        db.insert(DvbDataContent.TABLE_CHANNELS,null, values2);
        
        
        ContentValues values3 = new ContentValues();
        
        
        
        values3.put(Channel.TableChannelsColumns.AUDIOECMPID+0, 1);
        values3.put(Channel.TableChannelsColumns.AUDIOECMPID+1, 1);
        values3.put(Channel.TableChannelsColumns.AUDIOECMPID+2, 1);
        values3.put(Channel.TableChannelsColumns.AUDIOECMPID+3, 1);
        
        values3.put(Channel.TableChannelsColumns.AUDIOLANGCODE+0, 1);
        values3.put(Channel.TableChannelsColumns.AUDIOLANGCODE+1, 1);
        values3.put(Channel.TableChannelsColumns.AUDIOLANGCODE+2, 1);
        values3.put(Channel.TableChannelsColumns.AUDIOLANGCODE+3, 1);
        
        values3.put(Channel.TableChannelsColumns.AUDIOPESTYPE+0, 1);
        values3.put(Channel.TableChannelsColumns.AUDIOPESTYPE+1, 1);
        values3.put(Channel.TableChannelsColumns.AUDIOPESTYPE+2, 1);
        values3.put(Channel.TableChannelsColumns.AUDIOPESTYPE+3, 1);
        
        values3.put(Channel.TableChannelsColumns.AUDIOSTREAMPID+0, 1);
        values3.put(Channel.TableChannelsColumns.AUDIOSTREAMPID+1, 1);
        values3.put(Channel.TableChannelsColumns.AUDIOSTREAMPID+2, 1);
        values3.put(Channel.TableChannelsColumns.AUDIOSTREAMPID+3, 1);
        
        values3.put(Channel.TableChannelsColumns.AUDIOSTREAMSIZE, 4);
        
        values3.put(Channel.TableChannelsColumns.BOUQUET_ID, 1);
        
        values3.put(Channel.TableChannelsColumns.CAECMPID+0, 1);
        values3.put(Channel.TableChannelsColumns.CAECMPID+1, 1);
        values3.put(Channel.TableChannelsColumns.CAECMPID+2, 1);
        values3.put(Channel.TableChannelsColumns.CAECMPID+3, 1);
        
        values3.put(Channel.TableChannelsColumns.CAECMPIDSIZE, 1);
        
        values3.put(Channel.TableChannelsColumns.CASYSTEMID+0, 1);
        values3.put(Channel.TableChannelsColumns.CASYSTEMID+1, 1);
        values3.put(Channel.TableChannelsColumns.CASYSTEMID+2, 1);
        values3.put(Channel.TableChannelsColumns.CASYSTEMID+3, 1);
        
        values3.put(Channel.TableChannelsColumns.CASYSTEMIDSIZE, 1);
        
        values3.put(Channel.TableChannelsColumns.FAVORITE, 1);
//        values.put(Channel.TableChannelsColumns.ID, 1);
        values3.put(Channel.TableChannelsColumns.LOCK, 1);
        values3.put(Channel.TableChannelsColumns.LOGICCHNUMBER, 3);
        
        values3.put(Channel.TableChannelsColumns.ORGNETID, 1);
        values3.put(Channel.TableChannelsColumns.PCRPID, 1);
        values3.put(Channel.TableChannelsColumns.PMTPID, 1);
        values3.put(Channel.TableChannelsColumns.PMTVERSION, 1);
        values3.put(Channel.TableChannelsColumns.PROVIDERNAME, 1);
        values3.put(Channel.TableChannelsColumns.SERVICEID, 1);
        values3.put(Channel.TableChannelsColumns.SERVICENAME, "湖南卫视");
        values3.put(Channel.TableChannelsColumns.SERVICEORGTYPE, 1);
        values3.put(Channel.TableChannelsColumns.SERVICETYPE, 1);
        
        values3.put(Channel.TableChannelsColumns.SUBTITLEECMPID+0, 1);
        values3.put(Channel.TableChannelsColumns.SUBTITLEECMPID+1, 1);
        values3.put(Channel.TableChannelsColumns.SUBTITLEECMPID+2, 1);
        values3.put(Channel.TableChannelsColumns.SUBTITLEECMPID+3, 1);
        
        values3.put(Channel.TableChannelsColumns.SUBTITLEPESTYPE+0, 1);
        values3.put(Channel.TableChannelsColumns.SUBTITLEPESTYPE+1, 1);
        values3.put(Channel.TableChannelsColumns.SUBTITLEPESTYPE+2, 1);
        values3.put(Channel.TableChannelsColumns.SUBTITLEPESTYPE+3, 1);
        
        values3.put(Channel.TableChannelsColumns.SUBTITLESTREAMDESC+0, 1);
        values3.put(Channel.TableChannelsColumns.SUBTITLESTREAMDESC+1, 1);
        values3.put(Channel.TableChannelsColumns.SUBTITLESTREAMDESC+2, 1);
        values3.put(Channel.TableChannelsColumns.SUBTITLESTREAMDESC+3, 1);
        
        values3.put(Channel.TableChannelsColumns.SUBTITLESTREAMPID+0, 1);
        values3.put(Channel.TableChannelsColumns.SUBTITLESTREAMPID+1, 1);
        values3.put(Channel.TableChannelsColumns.SUBTITLESTREAMPID+2, 1);
        values3.put(Channel.TableChannelsColumns.SUBTITLESTREAMPID+3, 1);
        
        values3.put(Channel.TableChannelsColumns.SUBTITLESTREAMSIZE, 1);
        
        
        
        values3.put(Channel.TableChannelsColumns.TELETEXTECMPID+0, 1);
        values3.put(Channel.TableChannelsColumns.TELETEXTECMPID+1, 1);
        values3.put(Channel.TableChannelsColumns.TELETEXTECMPID+2, 1);
        values3.put(Channel.TableChannelsColumns.TELETEXTECMPID+3, 1);
        
        values3.put(Channel.TableChannelsColumns.TELETEXTPESTYPE+0, 1);
        values3.put(Channel.TableChannelsColumns.TELETEXTPESTYPE+1, 1);
        values3.put(Channel.TableChannelsColumns.TELETEXTPESTYPE+2, 1);
        values3.put(Channel.TableChannelsColumns.TELETEXTPESTYPE+3, 1);
        
        values3.put(Channel.TableChannelsColumns.TELETEXTSTREAMDESC+0, 1);
        values3.put(Channel.TableChannelsColumns.TELETEXTSTREAMDESC+1, 1);
        values3.put(Channel.TableChannelsColumns.TELETEXTSTREAMDESC+2, 1);
        values3.put(Channel.TableChannelsColumns.TELETEXTSTREAMDESC+3, 1);
        
        values3.put(Channel.TableChannelsColumns.TELETEXTSTREAMPID+0, 1);
        values3.put(Channel.TableChannelsColumns.TELETEXTSTREAMPID+1, 1);
        values3.put(Channel.TableChannelsColumns.TELETEXTSTREAMPID+2, 1);
        values3.put(Channel.TableChannelsColumns.TELETEXTSTREAMPID+3, 1);
        
        values3.put(Channel.TableChannelsColumns.TELETEXTSTREAMSIZE, 1);
        
        values3.put(Channel.TableChannelsColumns.TRANSPONDER_ID, 0);
        
        values3.put(Channel.TableChannelsColumns.TSID, 1);
        values3.put(Channel.TableChannelsColumns.VIDEOECMPID, 1);
        values3.put(Channel.TableChannelsColumns.VIDEOPESTYPE, 1);
        values3.put(Channel.TableChannelsColumns.VIDEOSTREAMPID, 1);
        values3.put(Channel.TableChannelsColumns.VOLUME, 1);
        
        db.insert(DvbDataContent.TABLE_CHANNELS,null, values3);
    }

    public void DeleteTP(int id) {
        Log.d(TAG,"DeleteDept id = " + id);
        SQLiteDatabase db = this.getWritableDatabase();

        int back = db.delete(DvbDataContent.TABLE_TRANSPONDERS, "_id =?",
                new String[] { String.valueOf(id) });
        Log.d(TAG,"DeleteDept back = " + back);

        db.close();

    }

    public Cursor getAllCalData() {

        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = new String[] { "Transponder_id", "Frequency", "_id",
                "ServiceName" };

        Cursor c = db.query(DvbDataContent.VIEW_CHANNELS, columns, null, null,
                null, null, null);

        return c;
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);

        if (db.isReadOnly()) {
            Log.d(TAG,"onOpen(), SQLite is opened read-only!!! db=" + db);
        }
    }
}
