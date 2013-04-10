package novel.supertv.dvb.jni.struct;

public enum STVMode {

    STVMODE_MANUAL,     ///<手动搜索模式
    STVMODE_FULL,       ///<全频搜索模式
    STVMODE_NIT ,       ///<NIT搜索模式(NIT+PAT+PMT+SDT+CAT)
    STVMODE_NIT_S,      ///<NIT搜索模式简化版(NIT+SDT)
    STVMODE_MONITOR_PMT,///<手动方式(按SID取VPID+APID)并监视NIT表的版本
    STVMODE_NULL
    
}
