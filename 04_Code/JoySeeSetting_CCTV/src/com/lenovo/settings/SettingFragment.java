package com.lenovo.settings;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.util.Log;

public class SettingFragment{

	private static final String TAG = "SettingFragment";
	private Activity mActivity;

	public SettingFragment(Activity activity){
		mActivity = activity;
	}
	
	public void setFragment(Fragment fragment){   
		FragmentManager fragmentManager=mActivity.getFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        //ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
    	fragmentManager.popBackStack();
        ft.replace(R.id.setting_content_fragment, fragment);
        //ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);  
        ft.commit();
	}
	
    public void setFragment(Fragment fragment, boolean add_back_stack) {
        FragmentManager fragmentManager = mActivity.getFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        // ft.setCustomAnimations(android.R.animator.fade_in,
        // android.R.animator.fade_out);
        if (add_back_stack) {
            ft.addToBackStack(null);
        } else {
            if (fragmentManager.getBackStackEntryCount() > 0) {
                // fragmentManager.popBackStack(R.id.setting_content_fragment,
                // FragmentManager.POP_BACK_STACK_INCLUSIVE);
                Log.e(TAG,
                        "getBackStackEntryCount = "
                                + fragmentManager.getBackStackEntryCount());
                for (int i = 0; i < fragmentManager.getBackStackEntryCount(); i++) {
                    Log.e(TAG, "getBackStackEntryCount = " + i);
                    fragmentManager.popBackStack();
                }
                // make sure that all the list is clean
                ConfigFocus.Items_t.clear();
                ConfigFocus.Master.clear();
            }
        }
        ft.replace(R.id.setting_content_fragment, fragment);
//         ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
    }

}
