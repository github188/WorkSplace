package com.lenovo.settings.Util;

import java.lang.reflect.Method;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.lenovo.settings.R;

/**
 * 解决listview gridview 默认选中第一项时 焦点选中问题
 * @author yanhailong
 */
public class AdapterViewSelectionUtil {

    /**
     * 重设焦点 把相应视图焦点去掉 解决listview gridview 默认选中第一项时 焦点选中问题
     * @param v
     */
    public static void setFocus(View v){
        try {
            Method[] flds = null;
            if(ListView.class.isInstance(v)){//or v instanceof ListView
                @SuppressWarnings("unchecked")
                Class<ListView> c = (Class<ListView>) Class
                        .forName("android.widget.ListView");
                flds = c.getDeclaredMethods();
            }else if(GridView.class.isInstance(v)){
                @SuppressWarnings("unchecked")
                Class<GridView> c = (Class<GridView>) Class
                        .forName("android.widget.GridView");
                flds = c.getDeclaredMethods();
            }
            for (Method f : flds) {
                if ("setSelectionInt".equals(f.getName())) {
                    f.setAccessible(true);
                    f.invoke(v,
                            new Object[] { Integer.valueOf(-1) });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 用于在屏幕中央弹出toast提示
     * @param context
     * @param sum
     */
    public static void showToast(Context context,int sum){
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View tview = inflater.inflate(R.layout.config_text_toast, null);
        TextView text = (TextView) tview.findViewById(R.id.config_dialog_text);
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
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View tview = inflater.inflate(R.layout.config_text_toast, null);
        TextView text = (TextView) tview.findViewById(R.id.config_dialog_text);
        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(tview);
        text.setText(str);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

//    /**
//     * 用于生成一个pop 提示用
//     * @param context
//     * @param str
//     * @return
//     */
//    public static PopupWindow showPopToast(Context context,String str){
//        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
//        View tview = inflater.inflate(R.layout.config_text_toast, null);
//        TextView text = (TextView) tview.findViewById(R.id.config_dialog_text);
//        text.setText(str);
//        PopupWindow pop = new PopupWindow(tview);
//        pop.setWidth(716);
//        pop.setHeight(173);
//        pop.setFocusable(false);
//        return pop;
//    }

}
