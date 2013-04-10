package com.joysee.adtv.controller;

import java.util.ArrayList;

import com.joysee.adtv.common.DvbMessage;
import com.joysee.adtv.ui.IDvbBaseView;

public abstract class BaseController{
	 ArrayList<IDvbBaseView> mViews= new ArrayList<IDvbBaseView>();
    public void registerView(IDvbBaseView view){
    	if(!mViews. contains(view)){
            mViews.add(view);
        }
    }

    public void unRegisterView(IDvbBaseView view) {
    	 if(mViews != null&&mViews.contains(view)){
             mViews.remove(view);
         }
    }
    protected abstract void dispatchMessage(Object sender,DvbMessage msg);
}
