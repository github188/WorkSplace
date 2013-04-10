package com.joysee.adtv.common;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.joysee.adtv.R;

/**
 * 弹出toast提示工具类
 * @author songwenxuan
 */
public class ToastUtil {
/**
     * 用于在屏幕中央弹出toast提示
     * @param context
     * @param sum
     */
    public static void showToast(Context context,int sum){
        LayoutInflater inflater = LayoutInflater.from(context);
        View tview = inflater.inflate(R.layout.notify_dialog_layout, null);
        TextView text = (TextView) tview.findViewById(R.id.notify_dialog_tv);
        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(tview);
        text.setText(sum);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
    
    /**
     * 用于在屏幕中央弹出toast提示
     * @param context
     * @param str
     */
    public static void showToast(Context context,String str){
        LayoutInflater inflater = LayoutInflater.from(context);
        View tview = inflater.inflate(R.layout.notify_dialog_layout, null);
        TextView text = (TextView) tview.findViewById(R.id.notify_dialog_tv);
        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(tview);
        text.setText(str);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    /**
     * 用于生成一个PopupWindow 供弹出提示用
     * @param context
     * @param str
     * @return
     */
    public static PopupWindow showPopToast(Context context,String str){
        LayoutInflater inflater = LayoutInflater.from(context);
        View tview = inflater.inflate(R.layout.notify_dialog_layout, null);
        TextView text = (TextView) tview.findViewById(R.id.notify_dialog_tv);
        text.setText(str);
        PopupWindow pop = new PopupWindow(tview);
        pop.setWidth(716);
        pop.setHeight(173);
        pop.setFocusable(false);
        return pop;
    }

}
