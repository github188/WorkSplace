
package com.lenovo.settings;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

public class PowerOffDialog {
    
    protected static final String TAG = "PowerOffDialog";
    
    public static void showPowerOffDialog(Context context) {
        Log.d(TAG, "-----showPowerOffDialog--------");
        Dialog dialog = new Dialog(context, R.style.DialogStyle);
        dialog.setContentView(R.layout.power_off_dialog);
        ImageView imageView = (ImageView) dialog.findViewById(R.id.animation_view);
        // TextView title = (TextView) dialog.findViewById(R.id.textTitle);
        TextView msg = (TextView) dialog.findViewById(R.id.textMsg);
        AnimationDrawable animation = (AnimationDrawable) imageView.getBackground();
        // title.setText(context.getText(R.string.power_off));
        msg.setText(context.getText(R.string.shutdown_progress));
        animation.start();
        dialog.show();
    }
}
