
package novel.supertv.dvb.jni;

import java.util.Map;

import novel.supertv.dvb.jni.struct.TuningParam;
import novel.supertv.dvb.jni.struct.tagEpgEvent;
import novel.supertv.dvb.jni.struct.tagProgramEpg;

public class JniEpgSearch {
    /** EIT事件类型 */
    public static final int EIT_EVENT_UN = -1; // 未定义
    public static final int EIT_EVENT_PF = 0;  // 事件表:(当前/其它流)P/F当前/后续事件
    public static final int EIT_EVENT_AS = 1;  // 时间表当前流
    public static final int EIT_EVENT_OS = 2;  // 时间表其它流
    public static final int EIT_EVENT_SH = 3;  // 时间表
    public static final int EIT_EVENT_ALL = 4; // 全部表

    public static OnEpgListener listener;

    public interface OnEpgListener {
        void onEpgDataReceived(int notifyCode);
    }

    /*public void setOnEpgListener(OnEpgListener listener) {
        this.listener = listener;
    }*/

    public native boolean startEpgSearch(TuningParam tuning, int type);

    public native boolean cancleEpgSearch();

    public native Map<Integer, tagProgramEpg> getEpgData();

    public native tagEpgEvent[] getEpgDataBySID(int sid);

    public native tagEpgEvent[] getEpgDataByDuration(int sid, long startTime, long endTime);

    public static void onEpgSearchComplete(int notifyCode) {
        if(listener!=null){
            listener.onEpgDataReceived(notifyCode);
        }
    }
    public native String getUTCTime();
}
