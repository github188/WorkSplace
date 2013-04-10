package com.joysee.adtv.ui;

import com.joysee.adtv.common.DvbLog;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

/**
 * 自定义的EditText
 * 扩展的功能：
 * 1.检查输入的数字如果是0开头的就去掉，并且把光标放到后面
 * 2.检查输入的数字是否超出给定的范围，并返回相应的监听值用于UI提示
 * *3.可以支持循环输入，像安广风格的那种,待完善,暂时不用
 * @author songwenxuan
 */
public class SearchEditText extends EditText {

    private static final DvbLog log = new DvbLog(
            "com.joysee.adtv.ui.SearchEditText",DvbLog.DebugType.D);
    

    public SearchEditText(Context context) {
        super(context);
    }

    public SearchEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SearchEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction,
            Rect previouslyFocusedRect) {
        // 当焦点离开时检查并纠正数字格式
        if(!focused){
            checkNumFormat();
            checkInputData();
        }
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 检查文字编辑框的数字格式
     * 如：056要纠正为56,并且光标要停留在后面
     */
    private void checkNumFormat(){
        log.D("checkNumFormat()");
        
        String frequency = this.getText().toString();
        if(frequency != null && !frequency.equals("")){
            this.setText(""+Integer.parseInt(frequency));
        }
        
        if(this.getText().length() > 0){
            log.D("text.getText().length() ="+this.getText().length());
            this.setSelection(this.getText().length());
        }
        
    }

    /**
     * 检查输入的数据是否超出范围
     */
    public int checkInputData(){
        
        if(mOnInputDataErrorListener == null){
            log.D("not set OnInputDataErrorListener so return normal ...");
            return INPUT_DATA_ERROR_TYPE_NORMAL;
        }
        
        String text = getText().toString();
        // 默认是正常的输入值
        int checkValue = INPUT_DATA_ERROR_TYPE_NORMAL;
        
        if(text == null || text.equals("")){
            log.W( "input data null ...");
            
            checkValue = INPUT_DATA_ERROR_TYPE_NULL;
            
        }else{
            int value = Integer.parseInt(text);
            
            if (value < this.min || value > this.max) {
                log.W( "input data out of range ...");
                
                checkValue = INPUT_DATA_ERROR_TYPE_OUT;
                
            }else{
                // 正常情况
                checkValue = INPUT_DATA_ERROR_TYPE_NORMAL;
            }
        }
        
        mOnInputDataErrorListener.onInputDataError(checkValue);
        
        return checkValue;
        
    }

    private int min = 0;
    private int max = 0;

    /** 数据范围正常 */
    public static final int INPUT_DATA_ERROR_TYPE_NORMAL = 0;
    /** 数据为空 */
    public static final int INPUT_DATA_ERROR_TYPE_NULL = 1;
    /** 数据超出范围 */
    public static final int INPUT_DATA_ERROR_TYPE_OUT = 2;

    /**
     * 设置输入的数字范围限定,在初始化时应及时设置
     * @param min 最小值
     * @param max 最大值
     */
    public void setRange(int min,int max){
        
        this.min = min;
        this.max = max;
        
    }

    protected OnInputDataErrorListener mOnInputDataErrorListener;

    /**
     * 设置输入数据检查监听
     * @param l
     */
    public void setOnInputDataErrorListener(OnInputDataErrorListener l) {
        
        mOnInputDataErrorListener = l;
    }

    /**
     * 检测数据内容的监听
     *
     */
    public interface OnInputDataErrorListener{
        
        void onInputDataError(int errorType);
    }
}
