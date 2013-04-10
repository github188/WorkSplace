package com.joysee.adtv.common;

import com.joysee.adtv.R;
import com.joysee.adtv.logic.bean.Transponder;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * 用于读取和保存TuningParam的工具类
 * 使用shareprefence保存
 * @author songwenxuan
 */
public class TransponderUtil {

    private static final DvbLog log = new DvbLog("com.joysee.adtv.common.TransponderUtil",DvbLog.DebugType.D);

    // Suppress default constructor for noninstantiability
    private TransponderUtil(){
        throw new AssertionError("Suppress default constructor for noninstantiability");
    }

    /**
     * 读取shareprefence中保存的TuningParam
     * @param context
     * @param defaultTpType Transponder类型
     * @return
     */
    public static Transponder getTransponderFromXml(Context context, int defaultTpType){
        
        SharedPreferences mySharedPreferences = 
            context.getSharedPreferences(DefaultParameter.PREFERENCE_NAME, Activity.MODE_PRIVATE);
        
        Transponder defaultTp[] = new Transponder[3];
        
        // 全频 1
        defaultTp[DefaultParameter.DefaultTransponderType.DEFAULT_TRANSPONDER_TYPE_ALL] 
                  = new Transponder();
        
        defaultTp[DefaultParameter.DefaultTransponderType.DEFAULT_TRANSPONDER_TYPE_ALL]
                  .setFrequency(mySharedPreferences
                          .getInt(DefaultParameter.TpKey.KEY_FREQUENCY_MAINTP, 
                                  DefaultParameter.DefaultTpValue.FREQUENCY));
        
        defaultTp[DefaultParameter.DefaultTransponderType.DEFAULT_TRANSPONDER_TYPE_ALL]
                  .setModulation(mySharedPreferences
                          .getInt(DefaultParameter.TpKey.KEY_MODULATION_MAINTP, 
                                  DefaultParameter.DefaultTpValue.MODULATION));
        
        defaultTp[DefaultParameter.DefaultTransponderType.DEFAULT_TRANSPONDER_TYPE_ALL]
                  .setSymbolRate(mySharedPreferences
                          .getInt(DefaultParameter.TpKey.KEY_SYMBOL_RATE_MAINTP, 
                                  DefaultParameter.DefaultTpValue.SYMBOL_RATE));
        // 手动 0
        defaultTp[DefaultParameter.DefaultTransponderType.DEFAULT_TRANSPONDER_TYPE_MANUAL] 
                  = new Transponder();
        
        defaultTp[DefaultParameter.DefaultTransponderType.DEFAULT_TRANSPONDER_TYPE_MANUAL]
                  .setFrequency(mySharedPreferences
                          .getInt(DefaultParameter.TpKey.KEY_FREQUENCY_MANUAL, 
                                  DefaultParameter.DefaultTpValue.FREQUENCY));
        
        defaultTp[DefaultParameter.DefaultTransponderType.DEFAULT_TRANSPONDER_TYPE_MANUAL]
                  .setModulation(mySharedPreferences
                          .getInt(DefaultParameter.TpKey.KEY_MODULATION_MANUAL, 
                                  DefaultParameter.DefaultTpValue.MODULATION));
        
        defaultTp[DefaultParameter.DefaultTransponderType.DEFAULT_TRANSPONDER_TYPE_MANUAL]
                  .setSymbolRate(mySharedPreferences
                          .getInt(DefaultParameter.TpKey.KEY_SYMBOL_RATE_MANUAL, 
                                  DefaultParameter.DefaultTpValue.SYMBOL_RATE));
        // 自动 2
        defaultTp[DefaultParameter.DefaultTransponderType.DEFAULT_TRANSPONDER_TYPE_AUTO] 
                  = new Transponder();
        
        defaultTp[DefaultParameter.DefaultTransponderType.DEFAULT_TRANSPONDER_TYPE_AUTO]
                  .setFrequency(mySharedPreferences
                          .getInt(DefaultParameter.TpKey.KEY_FREQUENCY_AUTO, 
                                  DefaultParameter.DefaultTpValue.FREQUENCY));
        
        defaultTp[DefaultParameter.DefaultTransponderType.DEFAULT_TRANSPONDER_TYPE_AUTO]
                  .setModulation(mySharedPreferences
                          .getInt(DefaultParameter.TpKey.KEY_MODULATION_AUTO, 
                                  DefaultParameter.DefaultTpValue.MODULATION));
        
        defaultTp[DefaultParameter.DefaultTransponderType.DEFAULT_TRANSPONDER_TYPE_AUTO]
                  .setSymbolRate(mySharedPreferences
                          .getInt(DefaultParameter.TpKey.KEY_SYMBOL_RATE_AUTO, 
                                  DefaultParameter.DefaultTpValue.SYMBOL_RATE));
        
        log.D("get transponder from xml defaultTpType = "+defaultTpType+" and defaultp ="
                +defaultTp[defaultTpType].getFrequency()+"|"
                +defaultTp[defaultTpType].getModulation()+"|"+defaultTp[defaultTpType].getSymbolRate());
        return defaultTp[defaultTpType];
    }

    /**
     * 将Transponder存入shareprefence
     * @param context
     * @param defaultTpType Transponder类型
     * @param tp 将要保存的Transponder
     */
    public static void saveTransponerToXml(Context context, int defaultTpType, Transponder tp){
        
        if(tp == null){
            log.D("saveDefaultTransponer tp == null !");
            return;
        }
        SharedPreferences mySharedPreferences = 
            context.getSharedPreferences(DefaultParameter.PREFERENCE_NAME, Activity.MODE_PRIVATE);
        
        Transponder defaultTp[] = new Transponder[3];
        defaultTp[defaultTpType] = tp;
        
        if (defaultTpType == DefaultParameter.DefaultTransponderType.DEFAULT_TRANSPONDER_TYPE_ALL) {
            SharedPreferences.Editor editor = mySharedPreferences.edit();
            editor.putInt(DefaultParameter.TpKey.KEY_FREQUENCY_MAINTP,
                            defaultTp[DefaultParameter.DefaultTransponderType.DEFAULT_TRANSPONDER_TYPE_ALL]
                                    .getFrequency());
            editor.putInt(DefaultParameter.TpKey.KEY_SYMBOL_RATE_MAINTP,
                            defaultTp[DefaultParameter.DefaultTransponderType.DEFAULT_TRANSPONDER_TYPE_ALL]
                                    .getSymbolRate());
            editor.putInt(DefaultParameter.TpKey.KEY_MODULATION_MAINTP,
                            defaultTp[DefaultParameter.DefaultTransponderType.DEFAULT_TRANSPONDER_TYPE_ALL]
                                    .getModulation());
            editor.commit();
        } else if (defaultTpType == DefaultParameter.DefaultTransponderType.DEFAULT_TRANSPONDER_TYPE_MANUAL) {
            SharedPreferences.Editor editor = mySharedPreferences.edit();
            editor.putInt(DefaultParameter.TpKey.KEY_FREQUENCY_MANUAL,
                            defaultTp[DefaultParameter.DefaultTransponderType.DEFAULT_TRANSPONDER_TYPE_MANUAL]
                                    .getFrequency());
            editor.putInt(DefaultParameter.TpKey.KEY_SYMBOL_RATE_MANUAL,
                            defaultTp[DefaultParameter.DefaultTransponderType.DEFAULT_TRANSPONDER_TYPE_MANUAL]
                                    .getSymbolRate());
            editor.putInt(DefaultParameter.TpKey.KEY_MODULATION_MANUAL,
                            defaultTp[DefaultParameter.DefaultTransponderType.DEFAULT_TRANSPONDER_TYPE_MANUAL]
                                    .getModulation());
            editor.commit();
        } else if (defaultTpType == DefaultParameter.DefaultTransponderType.DEFAULT_TRANSPONDER_TYPE_AUTO) {
            SharedPreferences.Editor editor = mySharedPreferences.edit();
            editor.putInt(DefaultParameter.TpKey.KEY_FREQUENCY_AUTO,
                            defaultTp[DefaultParameter.DefaultTransponderType.DEFAULT_TRANSPONDER_TYPE_AUTO]
                                    .getFrequency());
            editor.putInt(DefaultParameter.TpKey.KEY_SYMBOL_RATE_AUTO,
                            defaultTp[DefaultParameter.DefaultTransponderType.DEFAULT_TRANSPONDER_TYPE_AUTO]
                                    .getSymbolRate());
            editor.putInt(DefaultParameter.TpKey.KEY_MODULATION_AUTO,
                            defaultTp[DefaultParameter.DefaultTransponderType.DEFAULT_TRANSPONDER_TYPE_AUTO]
                                    .getModulation());
            editor.commit();
        }
        log.D("save transponder to xml defaultTpType = "+defaultTpType+" and defaultp ="
                +defaultTp[defaultTpType].getFrequency()+"|"
                +defaultTp[defaultTpType].getModulation()+"|"+defaultTp[defaultTpType].getSymbolRate());
    }
    
    /**
     * 将Transponder存入shareprefence
     * 恢复默认值
     * @param context
     */
    public static void saveDefaultTransponer(Context context){
        
        SharedPreferences mySharedPreferences = 
            context.getSharedPreferences(DefaultParameter.PREFERENCE_NAME, Activity.MODE_PRIVATE);
        
        
            SharedPreferences.Editor editor = mySharedPreferences.edit();
            //全频
            editor.putInt(DefaultParameter.TpKey.KEY_FREQUENCY_MAINTP,
                    DefaultParameter.DefaultTpValue.FREQUENCY);
            editor.putInt(DefaultParameter.TpKey.KEY_SYMBOL_RATE_MAINTP,
                    DefaultParameter.DefaultTpValue.SYMBOL_RATE);
            editor.putInt(DefaultParameter.TpKey.KEY_MODULATION_MAINTP,
                    DefaultParameter.DefaultTpValue.MODULATION);
            //手动
            editor.putInt(DefaultParameter.TpKey.KEY_FREQUENCY_MANUAL,
                    DefaultParameter.DefaultTpValue.FREQUENCY);
            editor.putInt(DefaultParameter.TpKey.KEY_SYMBOL_RATE_MANUAL,
                    DefaultParameter.DefaultTpValue.SYMBOL_RATE);
            editor.putInt(DefaultParameter.TpKey.KEY_MODULATION_MANUAL,
                    DefaultParameter.DefaultTpValue.MODULATION);
            //自动
            editor.putInt(DefaultParameter.TpKey.KEY_FREQUENCY_AUTO,
                    DefaultParameter.DefaultTpValue.FREQUENCY);
            editor.putInt(DefaultParameter.TpKey.KEY_SYMBOL_RATE_AUTO,
                    DefaultParameter.DefaultTpValue.SYMBOL_RATE);
            editor.putInt(DefaultParameter.TpKey.KEY_MODULATION_AUTO,
                    DefaultParameter.DefaultTpValue.MODULATION);
            
            editor.commit();
            ToastUtil.showToast(context, R.string.search_resset_success);
            log.D("transponderutil savedefaulttransponer resset");
    }
}
