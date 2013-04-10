package com.joysee.adtv.doc;

import android.util.Log;

import com.joysee.adtv.server.ADTVBitmapTask;
import com.joysee.adtv.ui.EpgGuideWindow;
import com.joysee.adtv.ui.LiveGuideWindow;

public class ADTVProgramGuideDoc extends ADTVDoc {
    
    private static final String TAG = "ADTVProgramGuideDoc";
    
    public ADTVProgramGuideDoc() {
        super();
        onCreate();
    }
    
    public void onCreate() {
    }
    
    public void getPoster(int programId, String url) {
        ADTVBitmapTask adtvBitmapTask = new ADTVBitmapTask(programId, url) {
            public void onSingal() {
                Log.d(TAG, "------onSingal getBitmap = " + getBitmap() + " error = " + error);
                onGotResource(new ADTVResource(LiveGuideWindow.RES_BITMEP, programId, getBitmap()));
            }
        };
    }
}
