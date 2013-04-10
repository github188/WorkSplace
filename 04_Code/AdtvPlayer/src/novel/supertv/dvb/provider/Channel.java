package novel.supertv.dvb.provider;

import android.net.Uri;

public class Channel {

    public static final String AUTHORITY = "novel.supertv.dvb.provider.ChannelProvider";

    public static final class TablesName{
        public static final String TABLE_CHANNELS = "Data_Channels";
        public static final String TABLE_TRANSPONDERS = "Data_Transponders";
        public static final String TABLE_STLNBS = "Data_stLNBs";
        public static final String VIEW_CHANNELS = "View_Channels";
        public static final String TABLE_RESERVES = "Data_Reserves";
    }

    public static final class URI {
        public static String BASE_URI = "content://" + AUTHORITY +"/";
        
        public static final Uri VIEW_CHANNELS = Uri.parse(BASE_URI+ TablesName.VIEW_CHANNELS);
        public static final Uri TABLE_CHANNELS = Uri.parse(BASE_URI+ TablesName.TABLE_CHANNELS);
        public static final Uri TABLE_TRANSPONDERS = Uri.parse(BASE_URI+ TablesName.TABLE_TRANSPONDERS);
        public static final Uri TABLE_STLNBS = Uri.parse(BASE_URI+ TablesName.TABLE_STLNBS);
        public static final Uri TABLE_RESERVES = Uri.parse(BASE_URI + TablesName.TABLE_RESERVES);
    }

    public class ViewChannels implements TableChannelsColumns, 
                            TableTranspondersColumns, TablestLNBsColumns{
    }

    public interface TableChannelsColumns {
        public static final String ID = "_id";
        public static final String TRANSPONDER_ID = "Transponder_id";
        public static final String BOUQUET_ID = "Bouquet_id";
        public static final String LOGICCHNUMBER = "LogicChNumber";
        public static final String SERVICENAME = "ServiceName";
        public static final String SERVICETYPE = "ServiceType";
        public static final String SERVICEORGTYPE = "ServiceOrgType";
        public static final String PROVIDERNAME = "ProviderName";
        public static final String PCRPID = "PcrPid";
        public static final String EMMPID = "EmmPid";
        public static final String PMTPID = "PmtPid";
        public static final String PMTVERSION = "PmtVersion";
        public static final String VOLBALANCE = "VolBalance";
        public static final String VOLCOMPENSATION = "VolCompensation";
        public static final String FAVORITE = "Favorite";
        public static final String LOCK = "Lock";
        public static final String VOLUME = "Volume";
        public static final String SERVICEID = "ServiceId";
        public static final String TSID = "TsId";
        public static final String ORGNETID = "OrgNetId";
        public static final String AUDIOINDEX = "AudioIndex";//当前伴音
        public static final String VIDEOSTREAMPID = "VideoStreamPid";
        public static final String VIDEOECMPID = "VideoEcmPid";
        public static final String VIDEOPESTYPE = "VideoPesType";

        public static final int SERVICEDATAAUDIOSTREAMCOLUMNSIZE = 4;
        public static final String AUDIOSTREAMPID = "AudioStreamPid";
        public static final String AUDIOECMPID = "AudioEcmPid";
        public static final String AUDIOPESTYPE = "AudioPesType";
        public static final String AUDIOLANGCODE = "AudioLangCode";
        public static final String AUDIOSTREAMSIZE = "AudioStreamSize";

        public static final int SERVICEDATATELETEXTSTREAMCOLUMNSIZE = 4;
        public static final String TELETEXTSTREAMPID = "TeletextStreamPid";
        public static final String TELETEXTECMPID = "TeletextEcmPid";
        public static final String TELETEXTPESTYPE = "TeletextPesType";
        public static final String TELETEXTSTREAMDESC = "TeletextStreamDesc";
        public static final String TELETEXTSTREAMSIZE = "TeletextStreamSize";

        public static final int SERVICEDATASUBTITLESTREAMCOLUMNSIZE = 4;
        public static final String SUBTITLESTREAMPID = "SubtitleStreamPid";
        public static final String SUBTITLEECMPID = "SubtitleEcmPid";
        public static final String SUBTITLEPESTYPE = "SubtitlePesType";
        public static final String SUBTITLESTREAMDESC = "SubtitleStreamDesc";
        public static final String SUBTITLESTREAMSIZE = "SubtitleStreamSize";

        public static final int SERVICEDATACASYSTEMIDCOLUMNSIZE = 4;
        public static final String CASYSTEMID = "CaSystemId";
        public static final String CASYSTEMIDSIZE = "CaSystemIdSize";

        public static final int SERVICEDATACAECMPIDCOLUMNSIZE = 4;
        public static final String CAECMPID = "CaEcmPid";
        public static final String CAECMPIDSIZE = "CaEcmPidSize";
    }
    
    public interface TableTranspondersColumns {
        public static final String ID = "_id";
        public static final String STLNBS_ID = "stLNBs_id";
        public static final String FREQUENCY = "Frequency";
        public static final String MODULATION = "Modulation";
        public static final String SYMBOLRATE = "SymbolRate";
        public static final String NPATVERSION = "nPATVersion";
        public static final String NSDTVERSION = "nSDTVersion";
        public static final String NCATVERSION = "nCATVersion";
    }
    
    public interface TablestLNBsColumns{
        public static final String ID = "_id";
        public static final String BLNBTYPE = "bLnbType";
        public static final String DWALNBFREQUENCY0 = "dwaLnbFrequency0";
        public static final String DWALNBFREQUENCY1 = "dwaLnbFrequency1";
        public static final String BTONE22KHZ = "bTone22KHz";
        public static final String BSW12V = "bSw12v";
        public static final String BDISEQCTYPE = "bDiseqcType";
        public static final String BDISEQCSW = "bDiseqcSw";
        public static final String BLNBVOLTAGE = "bLnbVoltage";
        public static final String BPOSITIONER = "bPositioner";
    }
    
    public interface TableReservesColumns{
        public static final String ID = "_id";
        public static final String PROGRAMNAME = "programName";
        public static final String STARTTIME = "startTime";
        public static final String ENDTIME = "endTime"; 
        public static final String SERVICEID = "serviceId";
        public static final String CHANNELNAME = "channelName";
    }
}
