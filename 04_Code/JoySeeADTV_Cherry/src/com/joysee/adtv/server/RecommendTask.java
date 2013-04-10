package com.joysee.adtv.server;

import android.util.Log;


abstract public class RecommendTask extends ADTVJsonTask{
    
    private static final String TAG="FetchTimeTask";

    public String result;
    
    public RecommendTask() {
        super("getRecommendList", ADTVTask.PRIO_DATA, 1);
        addPostData("pageSize=10");
        addPostData("pageNo=1");
        start();
    }

    @Override
    boolean onGotResponse(String str) {
        Log.d(TAG, "--------str="+str);
        result=str;
        return true;
    }

}
