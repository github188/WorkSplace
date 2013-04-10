package novel.supertv.dvb.jni;

import java.util.List;

import novel.supertv.dvb.jni.struct.TuningParam;
import novel.supertv.dvb.jni.struct.stChannel;
import novel.supertv.dvb.jni.struct.tagTunerSignal;

public interface ISearchTVNotify {

    // 搜索到一组频道
    // 为什么不是一个一个的给呢？也许这样好做，但是不知道需求怎样
    public void OnDVBService(stChannel services);
    // 搜索的进度
    public void OnProgress(int progress);
   
    // 正在搜索的频点的tuner状态信息
    // TunerSignal应包括信号强度，载噪比，误码率
    public void OnTunerInfo(TuningParam tuningParam,tagTunerSignal signal);
 
    // 搜索完毕
    public void OnSTVComplete(List<stChannel> channels);

    // CA
//    public void OnCAMessage(int type,CAMessageT pMSG);

    //需要增加的函数

    // 搜索的频段
//    public void OnSearchNewTransponder();
}
