package novel.supertv.dvb.jni.struct;

public class stChannel implements Comparable<stChannel>{
    public int key;
    public int TransponderId;
    public stServiceIdent   ServiceIdent;       /* 节目标识号 */
    public String           ServiceName;        /* 节目名称 */
    public byte             ServiceType;        /* 节目类型，SDT表的service描述符中定义的节目类型码，可能根据要求在搜索时进行了类型变换。无效值为0xFF */
    public byte             ServiceOrgType;     /* 原始节目类型，SDT表的service描述符中定义的原始节目类型码。无效值为0xFF */
    public String           ProviderName;       /* 内容供应商名称 */
    
    
    private int             Frequency;          /* 频率 */
    private int             SymbolRate;         /* 符号率 */
    private int             Modulation;         /* 调制 */
    
    

    public stVideoTrack     VideoTrack;         /* 视频流情况。其中的EcmPid字段没有使用 */
    public stAudioTrack[]   AudioTrack = new stAudioTrack[3];       /* 音频流情况。其中的EcmPid字段没有使用 */
    public int             PcrPid;             /* pcr pid */
    
    public stPidStream[]    Teletext = new stPidStream[3];          /* 图文流描述 */
    public stPidStream[]    Subtitle = new stPidStream[0];;         /* 字幕流描述 */
    
    public char[]           CaSystemId = new char[0];           /* CA系统号，用于同密 */
    public char[]           CaEcmPid = new char[0];             /* CA系统对应的ECMpid，用于同密 */
    
    public int             LogicChNumber;      /* 逻辑频道号。无效值为0xFFFF */
    public stServiceIdent[] NvodTimeshiftServices = new stServiceIdent[0];  /* 如果是参考业务，表示其对于的时移业务集合 */
    public int             PmtPid;             /* 该节目的pmt pid。无效值为0x1FFF */
    public int              PmtVersion;         /* 该节目的pmt表版本号。无效值为0xFFFFFFFF */
    public byte             VolBalance;         /* 声道模式。无效值为0xFF */
    public byte             VolCompensation;    /* 音量补偿。无效值为0xFFFFFFFF */
    public int favorite;
    public int lock;
    public int Volume;
    private int playEnable;
    
    public int EmmPid;
    
    public int compareTo(stChannel another) {
        return ServiceIdent.compareTo(another.ServiceIdent);
    }
    
    @Override
    public String toString() {
        return "[ServiceIdent=" + ServiceIdent + ", ServiceName=" + ServiceName 
                + ", ServiceType=" + ServiceType + "]";
    }
    
    @Override
    public boolean equals(Object obj) {
        return (obj != null) && ((obj instanceof stChannel) == true)
                && ServiceIdent.equals(((stChannel)obj).ServiceIdent);
    }
    
    @Override
    public int hashCode() {
        final int prime = 37;
        int result = 17;
        result = prime * result + ServiceIdent.hashCode();
        return result;
    }
    
    public void copyMonitorPMT(stChannel info){
        this.key = info.key;
        this.TransponderId = info.TransponderId;
        this.favorite = info.favorite;
        this.lock = info.lock;
        this.Volume = info.Volume;
        this.playEnable = info.playEnable;
    }
    
    public int getFrequency() {
        return Frequency;
    }

    public void setFrequency(int frequency) {
        Frequency = frequency;
    }

    public int getSymbolRate() {
        return SymbolRate;
    }

    public void setSymbolRate(int symbolRate) {
        SymbolRate = symbolRate;
    }

    public int getModulation() {
        return Modulation;
    }

    public void setModulation(int modulation) {
        Modulation = modulation;
    }
    
    /**
     * chao
     *保存新频道中的必要数据 
     * @param searchResult
     */
    public void saveFromSearchResultTo(stChannel searchResult){
        this.favorite = searchResult.favorite;
        this.lock = searchResult.lock;
    }

    /**
     * 获取标准频道的实例,备用
     * @return stChannel的实例
     */
    public stChannel getStChannel(){
        stChannel channel = new stChannel();
        channel.setServiceIdent(this.getServiceIdent());
//        channel.setCaCharge(this.getCaSystemId().length > 0 ? 1 : 0);
        channel.setLogicChNumber(this.getLogicChNumber());
        channel.setFavorite(this.getFavorite());
        channel.setLock(this.getLock());
        channel.setServiceName(this.getServiceName());
        
        return channel;
    }

    /**
     * 保存从搜索结果中得到的数据
     * @param searchResult
     */
    public void saveFromSearchResult(stChannel searchResult){
        
        this.ServiceIdent = searchResult.ServiceIdent;
        // 除了ServiceId以外都保存???
        this.ServiceName = searchResult.ServiceName;
        
        this.ServiceType = searchResult.ServiceType;
        this.ServiceOrgType = searchResult.ServiceOrgType;
        
        this.ProviderName = searchResult.ProviderName;
        this.VideoTrack = searchResult.VideoTrack;
        this.AudioTrack = searchResult.AudioTrack;
        this.PcrPid = searchResult.PcrPid;
        this.Teletext = searchResult.Teletext;
        
        this.Subtitle = searchResult.Subtitle;
        this.CaSystemId = searchResult.CaSystemId;
        
        this.CaEcmPid = searchResult.CaEcmPid;
        this.LogicChNumber = searchResult.LogicChNumber;
        this.NvodTimeshiftServices = searchResult.NvodTimeshiftServices;
        this.PmtPid = searchResult.PmtPid;
        this.PmtVersion = searchResult.PmtVersion;
        this.VolBalance = searchResult.VolBalance;
        this.VolCompensation = searchResult.VolCompensation;
        
        // 这样肯定不行，但是别忘了这个
        this.favorite = searchResult.favorite;
        this.lock = searchResult.lock;
        this.Volume = searchResult.Volume;
        this.playEnable = searchResult.playEnable;
        
        this.EmmPid = searchResult.EmmPid;
        
    }
    public void copy(stChannel info){
        this.key = info.key;
        this.TransponderId = info.TransponderId;
        this.ServiceIdent = info.ServiceIdent;
        // 除了ServiceId以外都保存???
        this.ServiceName = info.ServiceName;
        
        this.ServiceType = info.ServiceType;
        this.ServiceOrgType = info.ServiceOrgType;
        
        this.ProviderName = info.ProviderName;
        this.VideoTrack = info.VideoTrack;
        this.AudioTrack = info.AudioTrack;
        this.PcrPid = info.PcrPid;
        this.Teletext = info.Teletext;
        
        this.Subtitle = info.Subtitle;
        this.CaSystemId = info.CaSystemId;
        
        this.CaEcmPid = info.CaEcmPid;
        this.LogicChNumber = info.LogicChNumber;
        this.NvodTimeshiftServices = info.NvodTimeshiftServices;
        this.PmtPid = info.PmtPid;
        this.PmtVersion = info.PmtVersion;
        this.VolBalance = info.VolBalance;
        this.VolCompensation = info.VolCompensation;
        
        // 这样肯定不行，但是别忘了这个
        this.favorite = info.favorite;
        this.lock = info.lock;
        this.Volume = info.Volume;
        this.playEnable = info.playEnable;
        
        this.EmmPid = info.EmmPid;
    }
    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public int getFavorite() {
        return favorite;
    }
    public void setFavorite(int favorite) {
        this.favorite = favorite;
    }
    public int getLock() {
        return lock;
    }
    public void setLock(int lock) {
        this.lock = lock;
    }
    
    public int getVolume() {
        return Volume;
    }
    public void setVolume(int volume) {
        Volume = volume;
    }
    public int getTransponderId() {
        return TransponderId;
    }
    public void setTransponderId(int transponderId) {
        TransponderId = transponderId;
    }
    public stServiceIdent getServiceIdent() {
        return ServiceIdent;
    }
    public void setServiceIdent(stServiceIdent serviceIdent) {
        ServiceIdent = serviceIdent;
    }
    public String getServiceName() {
        return ServiceName;
    }
    public void setServiceName(String serviceName) {
        ServiceName = serviceName;
    }
    public byte getServiceType() {
        return ServiceType;
    }
    public void setServiceType(byte serviceType) {
        ServiceType = serviceType;
    }
    public byte getServiceOrgType() {
        return ServiceOrgType;
    }
    public void setServiceOrgType(byte serviceOrgType) {
        ServiceOrgType = serviceOrgType;
    }
    public String getProviderName() {
        return ProviderName;
    }
    public void setProviderName(String providerName) {
        ProviderName = providerName;
    }
    public stVideoTrack getVideoTrack() {
        return VideoTrack;
    }
    public void setVideoTrack(stVideoTrack videoTrack) {
        VideoTrack = videoTrack;
    }
    public stAudioTrack[] getAudioTrack() {
        return AudioTrack;
    }
    public void setAudioTrack(stAudioTrack[] audioTrack) {
        AudioTrack = audioTrack;
    }
    public int getPcrPid() {
        return PcrPid;
    }
    public void setPcrPid(int pcrPid) {
        PcrPid = pcrPid;
    }
    public stPidStream[] getTeletext() {
        return Teletext;
    }
    public void setTeletext(stPidStream[] teletext) {
        Teletext = teletext;
    }
    public stPidStream[] getSubtitle() {
        return Subtitle;
    }
    public void setSubtitle(stPidStream[] subtitle) {
        Subtitle = subtitle;
    }
    public char[] getCaSystemId() {
        return CaSystemId;
    }
    public void setCaSystemId(char[] caSystemId) {
        CaSystemId = caSystemId;
    }
    public char[] getCaEcmPid() {
        return CaEcmPid;
    }
    public void setCaEcmPid(char[] caEcmPid) {
        CaEcmPid = caEcmPid;
    }
    public int getLogicChNumber() {
        return LogicChNumber;
    }
    public void setLogicChNumber(int logicChNumber) {
        LogicChNumber = logicChNumber;
    }
    public stServiceIdent[] getNvodTimeshiftServices() {
        return NvodTimeshiftServices;
    }
    public void setNvodTimeshiftServices(stServiceIdent[] nvodTimeshiftServices) {
        NvodTimeshiftServices = nvodTimeshiftServices;
    }
    public int getPmtPid() {
        return PmtPid;
    }
    public void setPmtPid(int pmtPid) {
        PmtPid = pmtPid;
    }
    public int getPmtVersion() {
        return PmtVersion;
    }
    public void setPmtVersion(int pmtVersion) {
        PmtVersion = pmtVersion;
    }
    public byte getVolBalance() {
        return VolBalance;
    }
    public void setVolBalance(byte volBalance) {
        VolBalance = volBalance;
    }
    public byte getVolCompensation() {
        return VolCompensation;
    }
    public void setVolCompensation(byte volCompensation) {
        VolCompensation = volCompensation;
    }

    public int getPlayEnable() {
        return playEnable;
    }

    public void setPlayEnable(int playEnable) {
        this.playEnable = playEnable;
    }

//    public Channel getChannel(){
//        Channel channel = new Channel();
//        channel.setServiceIdent(this.getServiceIdent());
//        channel.setCaCharge(this.getCaSystemId().length > 0 ? 1 : 0);
//        channel.setLogicalNumber(this.getLogicChNumber());
//        channel.setFavorite(this.getFavorite() == 1 ? true : false);
//        channel.setLocked(this.getLock() == 1 ? true : false);
//        channel.setName(this.getServiceName());
//        
//        return channel;
//    }

    public int getEmmPid() {
        return EmmPid;
    }

    public void setEmmPid(int emmPid) {
        EmmPid = emmPid;
    }

    public int describeContents() {
        return 0;
    }
}
