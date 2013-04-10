package novel.supertv.dvb;

import novel.supertv.dvb.jni.struct.tagDVBService;
import novel.supertv.dvb.utils.DvbLog;
import android.app.Application;
import android.content.Context;

/**
 * 这里存放应用程序内的全局变量
 * 比如，当前播放的频道编号等
 * @author dr
 *
 */
public class DvbApplication extends Application {

    private static final DvbLog log = new DvbLog(
            "novel.supertv.dvb.DvbApplication",DvbLog.DebugType.D);

    private tagDVBService mCurrentChannelTag;

    public tagDVBService getmCurrentChannelTag() {
        log.D("getmCurrentChannelTag()");
        return mCurrentChannelTag;
    }

    public void setmCurrentChannelTag(tagDVBService mCurrentChannelTag) {
        log.D("setmCurrentChannelTag mCurrentChannelTag = "+mCurrentChannelTag);
        this.mCurrentChannelTag = mCurrentChannelTag;
    }
}
