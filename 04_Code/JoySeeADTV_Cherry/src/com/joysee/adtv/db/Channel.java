
package com.joysee.adtv.db;

import android.net.Uri;

public class Channel {

    public static final String AUTHORITY = "com.joysee.adtv.db.ChannelProvider";

    public static final class TablesName {
        public static final String TABLE_RESERVES = "Data_Reserves";
        public static final String TABLE_SERVICE_TYPE = "Data_Service_Type";
    }

    public static final class URI {
        public static String BASE_URI = "content://" + AUTHORITY + "/";
        public static final Uri TABLE_RESERVES = Uri.parse(BASE_URI + TablesName.TABLE_RESERVES);
        public static final Uri TABLE_SERVICE_TYPE = Uri.parse(BASE_URI
                + TablesName.TABLE_SERVICE_TYPE);
    }

    public interface TableReservesColumns {
        public static final String ID = "_id";
        public static final String PROGRAMNAME = "programName";
        public static final String STARTTIME = "startTime";
        public static final String ENDTIME = "endTime";
        public static final String SERVICEID = "serviceId";
        public static final String CHANNELNUMBER = "channelNumber";
        public static final String CHANNELNAME = "channelName";
        public static final String RESERVESTATUS = "reserveStatus";
        public static final String PROGRAMID = "programId";
    }

    public interface TableChannelTypeColumns {
        public static final String ID = "_id";
        public static final String TYPEID = "typeId";
        public static final String TYPENAME = "typeName";
    }
}
