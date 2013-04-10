package novel.supertv.appmng;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class Utils {
	
	public static void showTipToast(int gravity,Context con,String text){
		LayoutInflater inflater = LayoutInflater.from(con);   
		View view=inflater.inflate(R.layout.toast_layout, null);
		TextView textv=(TextView)view.findViewById(R.id.text_view);
		textv.setText(text);
		Toast toast = new Toast(con);
		toast.setGravity(gravity,0,0);
		toast.setView(view);
		toast.show();
	}
	
	//把系统已安装应用来源
	public static int filterApp(ApplicationInfo info) {
		// 代表的是系统的应用,但是被用户升级了 用户应用 
		if ((info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
			return Constants.APP_SOURCE_INNER_UPDATE;
		// 代表的用户的应用 
		} else if ((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
			return Constants.APP_SOURCE_STORE;
		}
		return Constants.APP_SOURCE_INNER;
	}
	
	public static byte[] Bitmap2Bytes(Bitmap bm) {
		if(bm==null)
			return null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}
	
	public static  Bitmap drawableToBitmap2(Drawable drawable){  
        int width = drawable.getIntrinsicWidth();  
        int height = drawable.getIntrinsicHeight();  
        Bitmap bitmap = Bitmap.createBitmap(width, height,  
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888  
                        : Bitmap.Config.RGB_565);  
        Canvas canvas = new Canvas(bitmap);  
        drawable.setBounds(0,0,width,height);  
        drawable.draw(canvas);  
        return bitmap;  
    }
	
	/**
     * Drawableת转成Bitmap 
    */
    public static Bitmap drawableToBitmap(Drawable drawable) {  
        if(null==drawable){
            return null;
        }
        BitmapDrawable bd = (BitmapDrawable) drawable;
        return bd.getBitmap();
     }
    
    public static byte[] DrawableToBytes(Drawable drawable){
    	return Bitmap2Bytes(drawableToBitmap(drawable));
    }

    /**
     * 将Bitmap转成图片保存
     * @param bmp
     * @param filePath
     */
    public static void saveBitmap(final Bitmap bmp,final String filePath,String format){
        //AppLog.log_D(TAG,"----saveBitmap----byte="+bmp.getByteCount());
        new Thread(new Runnable() {
            public void run() {
                File file = new File(filePath);
                try {
                    FileOutputStream out = new FileOutputStream(file);
                    if (bmp.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                        out.flush();
                        out.close();
                        Log.d("jjjj","----- -----------end Bitmap");
                    }
                } catch (FileNotFoundException e) {
                    System.out.println("FileNotFoundException");
                    e.printStackTrace();
                } catch (IOException e) {
                    System.out.println("FileNotFoundException");
                    e.printStackTrace();
                }
            }
        }).start();
    }
    
}
