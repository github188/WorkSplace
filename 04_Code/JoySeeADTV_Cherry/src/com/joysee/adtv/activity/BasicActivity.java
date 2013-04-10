package com.joysee.adtv.activity;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;

import com.joysee.adtv.doc.ADTVDoc;
import com.joysee.adtv.doc.ADTVResource;
import com.joysee.adtv.server.ADTVCallback;

public class BasicActivity extends Activity implements ADTVCallback{
    
    private static final String TAG="BasicActivity";
    
    private static final int MSG_ONGOTRESOURCE=1;
    private static final int MSG_ONUPDATE=2;
    
    public ADTVDoc doc=null;
    
    public void setDoc(ADTVDoc doc){
        this.doc = doc;
        doc.registerCallback(this);
    }
    
    public Handler handler=new Handler(){
        public void handleMessage(android.os.Message msg) {
            switch(msg.what){
                case MSG_ONGOTRESOURCE:
                    onDocGotResource((ADTVResource)msg.obj);
                    break;
                case MSG_ONUPDATE:
                    onDocUpdate();
                    break;
            }
        };
    };

    public void onGotResource(ADTVResource res){
        Message mes=handler.obtainMessage(MSG_ONGOTRESOURCE, res);
        handler.sendMessage(mes);
    }

    public void onUpdate(){
        handler.sendEmptyMessage(MSG_ONUPDATE);
    }

    protected void onDocGotResource(ADTVResource res){
    }

    protected void onDocUpdate(){
    }
}
