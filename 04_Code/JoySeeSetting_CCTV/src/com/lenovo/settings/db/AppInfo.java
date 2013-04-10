package com.lenovo.settings.db;

import android.net.Uri;

public class AppInfo {
    public static final String AUTHORITY = "com.lenovo.settings.db.ParentalControlProvider";
    public static final String TABLE_NAME = "app_lock";

    public static final class AppInfoColumns {
        public static String BASE_URI = "content://" + AUTHORITY + "/";
        public static final String ID = "_id";
        public static final String PACKAGE_NAME = "packagename";
        public static final String LOCK_FLAG = "lockflag";
        public static final String PASSWORD = "password";
        // CONTENT_URI跟数据库的表关联，最后根据CONTENT_URI来查询对应的表
        public static final Uri CONTENT_URI = Uri.parse(BASE_URI + TABLE_NAME);
    }
}
