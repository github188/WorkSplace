package com.ismartv.util; 

public class JniSetting { 
	static{
    	System.loadLibrary("jnisetting");//libjnisetting.so
    }
	
    private JniSetting(){
        
    }
    private static JniSetting instance = new JniSetting();
    
    public static JniSetting getInstance(){ 
        return instance;
    } 
    
    /**
     * 画面设置
     * @param screen
     * @return
     */
    public native int setDisplayMode(int mode);
    
    /**
     * 获取画面比例
     * @return
     */
    public native int getDisplayMode();
    
//    public native String getSTBID();
    
}
