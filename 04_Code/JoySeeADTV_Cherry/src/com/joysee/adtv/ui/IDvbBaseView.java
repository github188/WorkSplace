package com.joysee.adtv.ui;

import com.joysee.adtv.common.DvbMessage;

public interface IDvbBaseView {
    /**
     * 根据msg类型执行不同的操作
     * @param msg
     */
    void processMessage(Object sender,DvbMessage msg);
    
}
