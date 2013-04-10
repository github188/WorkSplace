package com.joysee.adtv.server;

import com.joysee.adtv.doc.ADTVResource;

public interface ADTVCallback {

    public void onGotResource(ADTVResource res);
    public void onUpdate();
    
}
