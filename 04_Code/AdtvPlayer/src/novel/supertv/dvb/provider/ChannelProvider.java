package novel.supertv.dvb.provider;

import java.util.ArrayList;

import novel.supertv.dvb.utils.DvbLog;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class ChannelProvider extends ContentProvider {

    private static final DvbLog log = new DvbLog(
            "novel.supertv.dvb.provider.ChannelProvider",DvbLog.DebugType.D);

    private DvbDatabaseHelper mOpenHelper;

    public Context context;

    private static final int VIEW_CHANNELS = 1;
    private static final int TABLE_CHANNELS = 2;
    private static final int TABLE_TRANSPONDERS = 3;
    private static final int TABLE_STLNBS = 4;
    private static final int TABLE_RESERVES = 5;

    private static final UriMatcher uriMatcher = new UriMatcher(
            UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(Channel.AUTHORITY, DvbDataContent.VIEW_CHANNELS,
                VIEW_CHANNELS);
        uriMatcher.addURI(Channel.AUTHORITY, DvbDataContent.TABLE_CHANNELS,
                TABLE_CHANNELS);
        uriMatcher.addURI(Channel.AUTHORITY, DvbDataContent.TABLE_TRANSPONDERS,
                TABLE_TRANSPONDERS);
        uriMatcher.addURI(Channel.AUTHORITY, DvbDataContent.TABLE_STLNBS,
                TABLE_STLNBS);
        uriMatcher.addURI(Channel.AUTHORITY, DvbDataContent.TABLE_RESERVES,
                TABLE_RESERVES);
    }

    @Override
    public boolean onCreate() {
        log.D("onCreate()");
        mOpenHelper = new DvbDatabaseHelper(getContext(), "dvb");
        context = this.getContext();
        return true;
    }

    @Override
    public ContentProviderResult[] applyBatch(
            ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();// 开始事务
        try {
            ContentProviderResult[] results = super.applyBatch(operations);
            db.setTransactionSuccessful();// 设置事务标记为successful
            return results;
        } finally {
            db.endTransaction();// 结束事务
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        log.D("delete " + uri);
        int match = uriMatcher.match(uri);
        log.D("match rst " + match);
        int result = -1;
        String tableName = "";
        switch (match) {
        case TABLE_CHANNELS:
        	tableName = DvbDataContent.TABLE_CHANNELS;
            break;
        case TABLE_TRANSPONDERS:
        	tableName = DvbDataContent.TABLE_TRANSPONDERS;
            break;
        case TABLE_STLNBS:
        	tableName = DvbDataContent.TABLE_STLNBS;
            break;
        case TABLE_RESERVES:
            tableName = DvbDataContent.TABLE_RESERVES;
            break;
        default:
        	throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        result =  mOpenHelper.getWritableDatabase().delete(tableName, 
        		selection, selectionArgs);
        return result;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        log.D("insert " + uri);
        int match = uriMatcher.match(uri);
        log.D("match rst " + match);
        long result = -1;
        String tableName = "";
        Uri u = null;
        switch (match) {
        case TABLE_CHANNELS:
        	tableName = DvbDataContent.TABLE_CHANNELS;
        	if(tableName.equals("") == false){
                result = mOpenHelper.getWritableDatabase().insert( tableName, null, values);
            }
            u = Uri.withAppendedPath(uri, "" + result);
            break;
        case TABLE_TRANSPONDERS:
        	tableName = DvbDataContent.TABLE_TRANSPONDERS;
        	if(tableName.equals("") == false){
                result = mOpenHelper.getWritableDatabase().insert( tableName, null, values);
            }
            u = Uri.withAppendedPath(uri, "" + result);
            break;
        case TABLE_STLNBS:
        	tableName = DvbDataContent.TABLE_STLNBS;
        	tableName = DvbDataContent.TABLE_TRANSPONDERS;
            if(tableName.equals("") == false){
                result = mOpenHelper.getWritableDatabase().insert( tableName, null, values);
            }
            u = Uri.withAppendedPath(uri, "" + result);
            break;
        case TABLE_RESERVES:
            tableName = DvbDataContent.TABLE_RESERVES;
            if(tableName.equals("") == false){
                result = mOpenHelper.getWritableDatabase().insert( tableName, null, values);
            }
            u = Uri.withAppendedPath(uri, "" + result);
            break;
        default:
        	throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        
        return u;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        log.D("query " + uri);
        int match = uriMatcher.match(uri);
        log.D("match rst " + match);
        Cursor cursor = null;
        SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();
        switch (match) {
        case VIEW_CHANNELS:
            qBuilder.setTables(DvbDataContent.VIEW_CHANNELS);
            break;
        case TABLE_CHANNELS:
            qBuilder.setTables(DvbDataContent.TABLE_CHANNELS);
            break;
        case TABLE_TRANSPONDERS:
            qBuilder.setTables(DvbDataContent.TABLE_TRANSPONDERS);
            break;
        case TABLE_STLNBS:
            qBuilder.setTables(DvbDataContent.TABLE_STLNBS);
            break;
        case TABLE_RESERVES:
            qBuilder.setTables(DvbDataContent.TABLE_RESERVES);
            break;
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        cursor = qBuilder.query(mOpenHelper.getReadableDatabase(),
                projection, selection, selectionArgs, null, null,
                sortOrder, null);

        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        log.D("update " + uri);
        int match = uriMatcher.match(uri);
        log.D("match rst " + match);
        
        int result = 0;
        String tableName = "";
        switch (match) {
        case TABLE_CHANNELS:
            tableName = DvbDataContent.TABLE_CHANNELS;
            break;
        case TABLE_TRANSPONDERS:
            tableName = DvbDataContent.TABLE_TRANSPONDERS;
            break;
        case TABLE_STLNBS:
            tableName = DvbDataContent.TABLE_STLNBS;
            break;
        case TABLE_RESERVES:
            tableName = DvbDataContent.TABLE_RESERVES;
            break;
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        
        result = mOpenHelper.getWritableDatabase().update(tableName, values, selection, selectionArgs);
        
        return result;
    }
    
    /** 删除预约消息 */
    protected void removeReserveProgramFromAlarm(int reserveId) {
        
        log.D("removeReserveProgramFromAlarm(), enter! reserveId=" + reserveId);
        Intent intent = new Intent("program alarm");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                reserveId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
    
    /** 添加预约信息 */
    protected void addReServeProgramToAlam(int reserveId, long startTime){
        
        log.D("addReServeProgramToAlam(), enter! reserveId=" + reserveId + 
                ", startTime=" + startTime);
        Intent intent = new Intent("program alarm");
//        intent.putExtra("reserveid", reserveId);
//        intent.putExtra("reserveType", 1);//类型什么用？
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                reserveId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, startTime, pendingIntent);
    }

}
