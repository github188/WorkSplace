package novel.supertv.dvb.utils;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * 数字键换台的工具类
 * @author dr
 *
 */
public class ChannelNumberUtil {

    /**
     * 频道的位数，目前默认是3位
     */
    private int positionCount = 100;// maybe change

    /**
     * current channel number
     */
    private int userInputChannelNumber = 0;

    /**
     * 到时执行跳转频道
     */
    private static final int ONKEY_SWITCH_CHANNEL_TIMEOUT = 1;

    /**
     * when user press 0~9 key ,then change channel
     * @param num
     */
    public int numKeyDown(int num){
        if(userInputChannelNumber / positionCount > 0){
            return userInputChannelNumber;
        }
        
        myHandler.removeMessages(ONKEY_SWITCH_CHANNEL_TIMEOUT);
        myHandler.sendEmptyMessageDelayed(ONKEY_SWITCH_CHANNEL_TIMEOUT,3000);
        
        userInputChannelNumber = userInputChannelNumber * 10 + num;
        
        return userInputChannelNumber;
    }

    private OnSwitchChannelListener mOnSwitchChannelListener;

    /**
     * 设置数字键输入的频道号到时跳转
     * @param l
     */
    public void setOnSwitchChannelListener(OnSwitchChannelListener l){
        this.mOnSwitchChannelListener = l;
    }

    /**
     * 执行跳转频道的操作
     */
    public interface OnSwitchChannelListener{
        
        void OnSwitchChannel(int channelNum);
        
    }

    /**
     * 清空频道号
     */
    public void clear(){
        userInputChannelNumber = 0;
    }

    /**
     * 取消执行频道跳转
     */
    public void removeMessages()
    {
    	myHandler.removeMessages(ONKEY_SWITCH_CHANNEL_TIMEOUT);
    }

    public void setChannelNumber(int channelNum){
        userInputChannelNumber = channelNum;
    }

    public int getChannelNumber(){
        return userInputChannelNumber;
    }

    public Handler myHandler = new Handler(Looper.getMainLooper()){
        
        @Override
        public void handleMessage(Message msg) {
            
            switch(msg.what){
            case ONKEY_SWITCH_CHANNEL_TIMEOUT:
                
                if(mOnSwitchChannelListener != null){
                    mOnSwitchChannelListener.OnSwitchChannel(getChannelNumber());
                }
                
                break;
            }
            
            super.handleMessage(msg);
        }
        
    };

    /**
     *  设置频道的位数
     * @param positionCount 如果是3位则设置100
     */
    public void setPositionCount(int positionCount) {
        this.positionCount = positionCount;
    }

    /**
     * 立即切换频道
     */
    public void switchChannelNow(){
        
        myHandler.removeMessages(ONKEY_SWITCH_CHANNEL_TIMEOUT);
        myHandler.sendEmptyMessage(ONKEY_SWITCH_CHANNEL_TIMEOUT);
    }
}
