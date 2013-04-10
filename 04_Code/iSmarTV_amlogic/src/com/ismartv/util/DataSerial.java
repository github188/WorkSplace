package com.ismartv.util;

import java.io.Serializable;

/**
 * 主要用于保存一些全局常量
 * @author chenggang
 *
 */
public class DataSerial implements Serializable{
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public String accredit;//验证码
    
    public DataSerial(String acc){
        this.accredit=acc;
    }

}
