package novel.supertv.dvb.utils;

import novel.supertv.dvb.R;
import android.content.Context;

public class CaNotifyString {

    private Context mContext;

    public CaNotifyString(Context context){
        mContext = context;
    }

    public String getCaNotifyString(int notify){
        
        switch(notify){
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_BADCARD_TYPE:
            
            return mContext.getString(R.string.ca_message_badcard_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_BLACKOUT_TYPE:
            
            return mContext.getString(R.string.ca_message_blackout_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_CALLBACK_TYPE:
            
            return mContext.getString(R.string.ca_message_callback_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_CANCEL_TYPE:
            
            return mContext.getString(R.string.ca_message_cancel_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_DECRYPTFAIL_TYPE:
            
            return mContext.getString(R.string.ca_message_decryptfail_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_ERRCARD_TYPE:
            
            return mContext.getString(R.string.ca_message_errcard_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_ERRREGION_TYPE:
            
            return mContext.getString(R.string.ca_message_errregion_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_EXPICARD_TYPE:
            
            return mContext.getString(R.string.ca_message_expicard_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_FREEZE_TYPE:
            
            return mContext.getString(R.string.ca_message_freeze_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_INSERTCARD_TYPE:
            
            return mContext.getString(R.string.ca_message_insertcard_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_LOWCARDVER_TYPE:
            
            return mContext.getString(R.string.ca_message_lowcardver_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_MAXRESTART_TYPE:
            
            return mContext.getString(R.string.ca_message_maxrestart_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_NEEDFEED_TYPE:
            
            return mContext.getString(R.string.ca_message_needfeed_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_NOENTITLE_TYPE:
            
            return mContext.getString(R.string.ca_message_noentitle_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_NOMONEY_TYPE:
            
            return mContext.getString(R.string.ca_message_nomoney_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_NOOPER_TYPE:
            
            return mContext.getString(R.string.ca_message_nooper_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_OUTWORKTIME_TYPE:
            
            return mContext.getString(R.string.ca_message_outworktime_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_PAIRING_TYPE:
            
            return mContext.getString(R.string.ca_message_pairing_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_STBFREEZE_TYPE:
            
            return mContext.getString(R.string.ca_message_stbfreeze_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_STBLOCKED_TYPE:
            
            return mContext.getString(R.string.ca_message_stblocked_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_UPDATE_TYPE:
            
            return mContext.getString(R.string.ca_message_update_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_VIEWLOCK_TYPE:
            
            return mContext.getString(R.string.ca_message_viewlock_type);
        case DefaultParameter.NotificationAction.CA.NOTIFICATION_ACTION_CA_MESSAGE_WATCHLEVEL_TYPE:
            
            return mContext.getString(R.string.ca_message_watchlevel_type);
        }
        
        return null;
    }
}
