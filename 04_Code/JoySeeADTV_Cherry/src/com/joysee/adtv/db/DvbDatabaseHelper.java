
package com.joysee.adtv.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DvbDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "com.joysee.adtv.db.DvbDatabaseHelper";
    public final static byte[] _writeLock = new byte[0];
    /**
     * 数据库版本号，用于数据库升级
     */
    private static final int DB_VERSION = 20;
    /**
     * 构造，由Provider使用
     * 
     * @param context
     * @param name
     */
    private static DvbDatabaseHelper databaseHelper = null;
    public static DvbDatabaseHelper getInstance(Context context, String name) {
        if (databaseHelper == null) {
            databaseHelper = new DvbDatabaseHelper(context, name);
        }
        return databaseHelper;
    }
    private DvbDatabaseHelper(Context context, String name) {
        super(context, name, null, DB_VERSION);
        Log.d(TAG, "this is DvbDatabaseHelper ");
    }
    /**
     * 初始化数据,主要是拼接用于在onCreate()中执行的字段
     */
    public void initData() {

        // 创建预约表
        DvbDataContent.DATA_RESERVE = "create table "
                + DvbDataContent.TABLE_RESERVES + "("
                + Channel.TableReservesColumns.ID + " integer PRIMARY KEY, "
                + Channel.TableReservesColumns.PROGRAMID + " integer, "
                + Channel.TableReservesColumns.SERVICEID + " integer, "
                + Channel.TableReservesColumns.STARTTIME + " integer, "
                + Channel.TableReservesColumns.ENDTIME + " integer, "
                + Channel.TableReservesColumns.PROGRAMNAME + " varchar, "
                + Channel.TableReservesColumns.CHANNELNUMBER + " integer, "
                + Channel.TableReservesColumns.CHANNELNAME + " integer, "
                + Channel.TableReservesColumns.RESERVESTATUS + " integer"
                + ")";
        // 创建频道类型表
        DvbDataContent.DATA_SERVICE_TYPE = "create table "
                + DvbDataContent.TABLE_SERVICE_TYPE + "("
                + Channel.TableChannelTypeColumns.ID + " integer PRIMARY KEY, "
                + Channel.TableChannelTypeColumns.TYPEID + " integer, "
                + Channel.TableChannelTypeColumns.TYPENAME + " varchar"
                + ")";
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        initData();
        // 开始创建表
        try {
            Log.d(TAG, "this is Data_reserve = " + DvbDataContent.DATA_RESERVE);
            db.execSQL(DvbDataContent.DATA_RESERVE);
            Log.d(TAG, "this is Data_Servict_Type = " + DvbDataContent.DATA_SERVICE_TYPE);
            db.execSQL(DvbDataContent.DATA_SERVICE_TYPE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        // 拼接删除表的字段，用于onUpgrade中升级数据库使用
        DvbDataContent.DROP_DATA_RESERVE = "DROP TABLE IF EXISTS "
                + DvbDataContent.TABLE_RESERVES;
        DvbDataContent.DROP_DATA_SERVICE_TYPE = "DROP TABLE IF EXISTS "
                + DvbDataContent.TABLE_SERVICE_TYPE;
        if (oldVersion < 30) {
            db.execSQL(DvbDataContent.DROP_DATA_RESERVE);
            db.execSQL(DvbDataContent.DROP_DATA_SERVICE_TYPE);
            onCreate(db);
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (db.isReadOnly()) {
            Log.d(TAG, "onOpen(), SQLite is opened read-only!!! db=" + db);
        }
    }
}
