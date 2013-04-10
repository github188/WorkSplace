package com.lenovo.settings.Util;

/**
 * 用于规定各种默认参数的数值
 * 相当于参数配置文件
 * @author dr
 *
 */
public class DefaultParameter {

    public static final String CHANNEL_NUMBER_FORMAT = "%1$03d";
    public static final String START_SEARCH = "start search";
    public static final String FINISH_SEARCH = "finish seearch";

    public static final class ServiceType{
    	public static final int TV = 0x01;//数字电视业务
        public static final int BC = 0x02;//数字音频广播业务
        public static final int FAVORITE = 0x80;//喜爱频道
    }

    /**
     * SharedPreferences中存储的频道类型KEY
     */
    public static final class ChannelTypeKey{
        /** 当前播放的类型 */
        public final static String KEY_CURRENT_TYPE = "current_type";
        /** 数字电视业务 */
        public final static String KEY_DIGITAL_TELEVISION_SERVICE = "digital_television_service";
        /** 数字音频广播业务 */
        public final static String KEY_DIGITAL_RADIO_SOUND_SERVICE = "digital_radio_sound_service";
    }

    public class NotificationAction{
        /** PF 搜索完成通知 */
        public static final int NOTIFICATION_TVNOTIFY_MINEPG = 100;
        /** EPG 节目指南完成通知 */
        public static final int NOTIFICATION_TVNOTIFY_EPGCOMPLETE = 101;
        /** TUNER 实时信号状态通知 */
        public static final int NOTIFICATION_TVNOTIFY_TUNER_SIGNAL = 102;
        /** PAT/PMT/SDT信息改变 */
        public static final int NOTIFICATION_TVNOTIFY_UPDATE_SERVICE =103;
        /** NIT/BAT 信息改变 */
        public static final int NOTIFICATION_TVNOTIFY_UPDATE_PROGRAM = 104;
        
        /** tuner 状态 */
        public class TunerStatus{
            /** tuner 有信号*/
            public static final int ACTION_TUNER_LOCKED = 0;
            /** tuner 无信号*/
            public static final int ACTION_TUNER_UNLOCKED = 1;
        }
        
        /** 不能正常收看节目的提示 */
        public static final int NOTIFICATION_TVNOTIFY_BUYMSG = 200;
        /** 显示/隐藏 OSD 信息 */
        public static final int NOTIFICATION_TVNOTIFY_OSD = 201;
        /** 指纹显示 */
        public static final int NOTIFICATION_TVNOTIFY_SHOW_FINGERPRINT = 202;
        /** 进度显示 */
        public static final int NOTIFICATION_TVNOTIFY_SHOW_PROGRESSSTRIP = 203;
        /** 新邮件通知消息 */
        public static final int NOTIFICATION_TVNOTIFY_MAIL_NOTIFY = 204;
        
        public class CA{
            
            /*---------- CAS 提示信息---------*/
            /** 取消当前的显示*/
            public static final int NOTIFICATION_ACTION_CA_MESSAGE_CANCEL_TYPE = 0x00;
            /** 无法识别卡*/
            public static final int NOTIFICATION_ACTION_CA_MESSAGE_BADCARD_TYPE = 0x01;
            /** 智能卡过期,请更换新卡*/
            public static final int NOTIFICATION_ACTION_CA_MESSAGE_EXPICARD_TYPE = 0x02;
            /** 加扰节目,请插入智能卡*/
            public static final int NOTIFICATION_ACTION_CA_MESSAGE_INSERTCARD_TYPE = 0x03;
            /** 卡中不存在节目运营商*/
            public static final int NOTIFICATION_ACTION_CA_MESSAGE_NOOPER_TYPE = 0x04;
            /** 条件禁播*/
            public static final int NOTIFICATION_ACTION_CA_MESSAGE_BLACKOUT_TYPE = 0x05;
            /** 当前时段被设定为不能观看*/
            public static final int NOTIFICATION_ACTION_CA_MESSAGE_OUTWORKTIME_TYPE = 0x06;
            /** 节目级别高于设定的观看级别*/
            public static final int NOTIFICATION_ACTION_CA_MESSAGE_WATCHLEVEL_TYPE = 0x07;
            /** 智能卡与本机顶盒不对应*/
            public static final int NOTIFICATION_ACTION_CA_MESSAGE_PAIRING_TYPE = 0x08;
            /** 没有授权*/
            public static final int NOTIFICATION_ACTION_CA_MESSAGE_NOENTITLE_TYPE = 0x09;
            /** 节目解密失败*/
            public static final int NOTIFICATION_ACTION_CA_MESSAGE_DECRYPTFAIL_TYPE = 0x0A;
            /** 卡内金额不足*/
            public static final int NOTIFICATION_ACTION_CA_MESSAGE_NOMONEY_TYPE = 0x0B;
            /** 区域不正确*/
            public static final int NOTIFICATION_ACTION_CA_MESSAGE_ERRREGION_TYPE = 0x0C;
            /** 子卡需要和母卡对应,请插入母卡*/
            public static final int NOTIFICATION_ACTION_CA_MESSAGE_NEEDFEED_TYPE = 0x0D;
            /** 智能卡校验失败,请联系运营商*/
            public static final int NOTIFICATION_ACTION_CA_MESSAGE_ERRCARD_TYPE = 0x0E;
            /** 智能卡升级中,请不要拔卡或者关机*/
            public static final int NOTIFICATION_ACTION_CA_MESSAGE_UPDATE_TYPE = 0x0F;
            /** 请升级智能卡*/
            public static final int NOTIFICATION_ACTION_CA_MESSAGE_LOWCARDVER_TYPE = 0x10;
            /** 请勿频繁切换频道*/
            public static final int NOTIFICATION_ACTION_CA_MESSAGE_VIEWLOCK_TYPE = 0x11;
            /** 智能卡暂时休眠请分钟后重新开机*/
            public static final int NOTIFICATION_ACTION_CA_MESSAGE_MAXRESTART_TYPE = 0x12;
            /** 智能卡已冻结,请联系运营商*/
            public static final int NOTIFICATION_ACTION_CA_MESSAGE_FREEZE_TYPE = 0x13;
            /** 智能卡已暂停请回传收视记录给运营商*/
            public static final int NOTIFICATION_ACTION_CA_MESSAGE_CALLBACK_TYPE = 0x14;
            /** 请重启机顶盒*/
            public static final int NOTIFICATION_ACTION_CA_MESSAGE_STBLOCKED_TYPE = 0x20;
            /** 机顶盒被冻结*/
            public static final int NOTIFICATION_ACTION_CA_MESSAGE_STBFREEZE_TYPE = 0x21;
        }
    }

    public static final class ModulationType{
//        public static final int MODULATION_UNDEFINE = 0x00;
        public static final int MODULATION_16QAM = 0x00;
        public static final int MODULATION_32QAM = 0x01;
        public static final int MODULATION_64QAM = 0x02;
        public static final int MODULATION_128QAM = 0x03;
        public static final int MODULATION_256QAM = 0x04;
        public static final int MODULATION_RESERVED = 0x06;
    }

    /**
     * SharedPreferences的名称
     */
    public final static String PREFERENCE_NAME = "dvbset";

    /**
     * 默认频点类型，手动和全频
     * @author dr
     *
     */
    public static final class DefaultTransponderType{
        /** 全频 */
    	public static final int DEFAULT_TRANSPONDER_TYPE_ALL = 1;
        /** 手动 */
        public static final int DEFAULT_TRANSPONDER_TYPE_MANUAL = 0;
        /** 自动 */
        public static final int DEFAULT_TRANSPONDER_TYPE_AUTO = 2;
    }

    /**
     * SharedPreferences中存储的KEY
     */
    public static final class TpKey{
        /** 全频 */
        public final static String KEY_FREQUENCY_MAINTP = "frequencymaintp";
        public final static String KEY_SYMBOL_RATE_MAINTP = "symbolratemaintp";
        public final static String KEY_MODULATION_MAINTP = "modulationmaintp";
        /** 手动 */
        public final static String KEY_FREQUENCY_MANUAL = "frequencymanual";
        public final static String KEY_SYMBOL_RATE_MANUAL = "symbolratemanual";
        public final static String KEY_MODULATION_MANUAL = "modulationmanual";
        public final static String KEY_NITVersionL = "nitversion";
        /** 自动 */
        public final static String KEY_FREQUENCY_AUTO = "frequencyauto";
        public final static String KEY_SYMBOL_RATE_AUTO = "symbolrateauto";
        public final static String KEY_MODULATION_AUTO = "modulationauto";
    }

    /**
     * 手动和主频搜索 默认值
     */
    public static final class DefaultTpValue{
        public static final int FREQUENCY = 642000;
        public static final int SYMBOL_RATE = 6875;
        public static final int MODULATION = 2;
    }

    /**
     * 安卓状态栏的通知ID，用于标记不同的通知
     * @author dr
     *
     */
    public static final class NotificationId{
        
        /** 正在进行频道搜索 */
        public static final int CHANNEL_SEARCH_SEARCHING = 20111212;
        
        
    }

    /**
     * 搜索用的频率和符号率的范围参数
     *
     */
    public static final class SearchParameterRange{
        
        public static final int FREQUENCY_MIN = 47;
        
        public static final int FREQUENCY_MAX = 862;
        
        public static final int SYMBOLRATE_MIN = 1500;
        
        public static final int SYMBOLRATE_MAX = 7200;
    }

    /**
     * 喜爱频道的标记
     *
     */
    public static final class FavoriteFlag{
        
        public static final int FAVORITE_NO = 0;
        
        public static final int FAVORITE_IS = 1;
    }

    /**
     * 供外部调用dvb时进入的Intent
     * 主要包括：
     * 频道列表，喜爱频道，节目指南，预约列表，广播
     */
    public static final class DvbIntent{
        
        public final static String KEY = "com.joysee.key";
        /** 频道列表 */
        public final static String INTENT_CHANNEL_LIST = "com.joysee.intent.channel.list";
        /** 喜爱频道 */
        public final static String INTENT_FAVORITE_LIST = "com.joysee.intent.favorite.list";
        /** 节目指南 */
        public final static String INTENT_PROGRAM_GUIDE = "com.joysee.intent.program.guide";
        /** 预约列表 */
        public final static String INTENT_RESERVE_LIST = "com.joysee.intent.reserve.list";
        /** 广播 */
        public final static String INTENT_BROADCAST = "com.joysee.intent.broadcast";
        
        
        
        /** 从Launcher进入播放，此时不需要重新setService */
        public final static String KEY_PLAY = "com.joysee.intent.play";
        public final static String KEY_TUNERSTATUS = "tuner.status";
        public final static String KEY_CASTATUS = "ca.status";
        public final static String KEY_UPDATE_PROGRAM = "update.program";
    }
}
