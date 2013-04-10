package com.lenovo.settings.theme;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Dialog;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lenovo.settings.LenovoSettingsActivity;
import com.lenovo.settings.R;
import com.lenovo.settings.Util.FileSorter;
import com.lenovo.settings.Util.MyComparator;

public class ThemeSetting extends Fragment implements OnItemClickListener{
	
	public 	final static int THEME_LOADING = 201;
	public 	final static int THEME_LOAD_OVER = THEME_LOADING+1;
	public 	final static int THEME_SETTING = THEME_LOADING+2;
	public 	final static int THEME_SET_OVER = THEME_LOADING+3;
	public 	final static int REFRESH_THEME = THEME_LOADING+5;
	public final static int THEME_LOAD_ERROR = THEME_LOADING+6;
	public 	final static String THEME_URL = "settings.theme.url";
	public 	final static String THEME_POSTION = "settings.theme.postion";
	public  final static String THEME_CHANGE_ACTION = "settings.theme.change";
    public final static String TAG = "com.lenovo.settings.theme.ThemeSetting";
	
	View mView;
	GridView mThemeGrid;
	List<ThemeBitmap> mList;
	//private String filepath = "/mnt/sdcard/image";
	private String filepath = "/system/theme";
	
	private RelativeLayout mLoadingLayout;
	private AnimationDrawable mAnimationLoading;
	
	ThemeAdapter mThemeAdapter;
	public static int mIndexTheme;
	private Dialog ThemeSetDialog;
	private boolean canPress = false;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.theme_settings, container, false);
        mThemeGrid = (GridView) mView.findViewById(R.id.gridViewTheme);
        mLoadingLayout = (RelativeLayout) mView.findViewById(R.id.toastLayout);
        File file = new File(filepath);
        mList = new ArrayList<ThemeBitmap>();
        mIndexTheme = getPostion();
        Log.d(TAG, " --- onCreat mIndexTheme = " + mIndexTheme);
        String[] filepaths = getAllFileUrl(filepath);
        if (filepaths != null) {
            File[] files = new File[filepaths.length];
            for (int i = 0; i < filepaths.length; i++) {
                files[i] = new File(filepaths[i]);
                Log.d(TAG, " before sort files[" + i + "].getName() = " + files[i].getName());
            }
            Arrays.sort(files, new FileSorter(FileSorter.TYPE_NAME));
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    Log.d(TAG, " after sort files[" + i + "].getName() = " + files[i].getName());
                    String name = files[i].getName().substring(0,
                            files[i].getName().length() - 4);
                    String path = filepath + "/" + files[i].getPath();
                    Log.d(TAG, " onCreateView name = " + name);
                    if (i == mIndexTheme)
                        mList.add(new ThemeBitmap(path, name, "0"));
                    else
                        mList.add(new ThemeBitmap(path, name, "1"));
                }
            }
            mThemeGrid.setOnItemClickListener(this);
            mThemeGrid.setOnKeyListener(new OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    
                    if(!canPress){
                        return true;
                    }
                    return false;
                }
            });
            mHandler.sendEmptyMessage(THEME_SETTING);
        }else{
            Log.e(TAG, " onCreateView Error filepaths = " + filepaths);
            mHandler.sendEmptyMessage(THEME_LOAD_ERROR);
        }
        return mView;
    }

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int postion, long arg3) {
		if(mIndexTheme!=postion){
//			mIndexTheme = postion;
			showThemeSetDialog(mList.get(postion).getBitmapurl(),postion);
		}
	}
	
	private String[] getAllFileUrl(String filepath){
		File targetFile = new File(filepath);
		String[] filepaths = null;
		if(targetFile.exists()){
			filepaths = targetFile.list();
		}else{
		    Log.e(TAG, " getAllFileUrl Error filepath = " + filepath);
		}
		return filepaths;
	}

	
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, " ------------------------ handleMessage msg what = " + msg.what);
            String str = "";
            switch (msg.what) {
            case THEME_LOADING:
                canPress = false;
                str = getActivity().getString(R.string.loading_theme);
                showThemeLoading(str, true);
                break;
            case THEME_LOAD_OVER:
                hideThemeLoading();
                LenovoSettingsActivity.setTitleFocus(false);
                mThemeGrid.setVisibility(View.VISIBLE);
                mThemeGrid.requestFocus();
                mThemeGrid.setSelection(mIndexTheme);
                //解决在显示时因为翻页造成的bug
                this.sendEmptyMessageDelayed(REFRESH_THEME, 500);
                break;
            case REFRESH_THEME:
                canPress = true;
                break;
            case THEME_SETTING:
                mThemeAdapter = new ThemeAdapter(getActivity(), mList,
                        mThemeGrid, mHandler);
                mThemeGrid.setAdapter(mThemeAdapter);
                mHandler.sendEmptyMessage(ThemeSetting.THEME_LOADING);
                canPress = false;
                break;
            case THEME_SET_OVER:
                mIndexTheme = getPostion();
                mThemeAdapter.notifyDataSetChanged();
                break;
            case THEME_LOAD_ERROR:
                str = getActivity().getString(R.string.load_theme_error);
                showThemeLoading(str, false);
                break;
            }
        }
    };
	
	void hideThemeLoading(){
		if(mAnimationLoading.isRunning()){
			mAnimationLoading.stop();
		}
		mLoadingLayout.setVisibility(View.INVISIBLE);
	}
	void showThemeLoading(String msg, boolean hasLoading){	
		TextView textView = (TextView)mLoadingLayout.findViewById(R.id.toast_text);
		ImageView img = (ImageView)mLoadingLayout.findViewById(R.id.toastImage);
		textView.setText(msg);
		if(hasLoading){
			img.setVisibility(View.VISIBLE);
			mAnimationLoading = (AnimationDrawable)img.getBackground();
			if(!mAnimationLoading.isRunning()){
				mAnimationLoading.start();
			}
		}else{
			img.setVisibility(View.INVISIBLE);
		}
		mLoadingLayout.setVisibility(View.VISIBLE);
	}
	
	private void showThemeSetDialog(final String bitmapUrl,final int position){
		TextView msg;
		final Button btnConfirm;
		Button btnCancel;
        if (ThemeSetDialog == null) {
            ThemeSetDialog = new Dialog(mView.getContext(), R.style.DialogStyle);
            ThemeSetDialog.setContentView(R.layout.resolution_dialog);
        }
		msg = (TextView) ThemeSetDialog.findViewById(R.id.textview_dialog);
        btnConfirm = (Button) ThemeSetDialog.findViewById(R.id.btn_dlg_confirm);
        btnCancel = (Button) ThemeSetDialog.findViewById(R.id.btn_dlg_cancel);
        msg.setText(R.string.set_theme_msg);
    	btnConfirm.setText(R.string.yes);
        btnCancel.setText(R.string.no);
        btnConfirm.requestFocus();
        btnConfirm.setOnClickListener(new OnClickListener() {

			@Override
            public void onClick(View v) {
                if (bitmapUrl != null && bitmapUrl.length() > 0) {
                    // setWallpaper(bitmapUrl);
                    boolean ok = Settings.System.putString(getActivity()
                            .getContentResolver(), THEME_URL, bitmapUrl);
                    Log.d(TAG, " putString " + bitmapUrl
                            + " in contentResolver back = " + ok);
                    Settings.System.putInt(getActivity().getContentResolver(),
                            THEME_POSTION, position);
                    ThemeSetDialog.dismiss();
                    mIndexTheme = position;
                    getActivity()
                            .sendBroadcast(new Intent(THEME_CHANGE_ACTION));
                    mHandler.sendEmptyMessage(THEME_SET_OVER);
                }
            }
        });
        btnCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
		
			    ThemeSetDialog.dismiss();
			}
        	
        });
        ThemeSetDialog.show();
	}
	
	private int getPostion(){
		int pos = 0;
		try {
			pos = Settings.System.getInt(getActivity().getContentResolver(), THEME_POSTION);
			Log.d(TAG, " ----------get Theme get select position  pos = " + pos);
		} catch (SettingNotFoundException e) {
            pos = 4;
			e.printStackTrace();
		}
//		String url = Settings.System.getString(getActivity().getContentResolver(), ThemeSetting.THEME_URL);
//		String[] filepaths = getAllFileUrl(filepath);
//		int i = 0;
//		while(filepaths[i].equals(url)){
//			i++;
//		}
		return pos;
	}
	
	@SuppressWarnings("rawtypes")
	public class ChineseCharComp implements Comparator {
		public int compare(Object o1, Object o2) {
			Collator myCollator = Collator
					.getInstance(java.util.Locale.ENGLISH);
			if (myCollator.compare(o1, o2) < 0)
				return -1;
			else if (myCollator.compare(o1, o2) > 0)
				return 1;
			else
				return 0;
		}
	}
	@Override
    public void onStop() {
        
        Log.i(TAG, "-------onStop   ");
        if(ThemeSetDialog!=null&&ThemeSetDialog.isShowing()){
            ThemeSetDialog.dismiss();
        }
        super.onStop();
    }
    @Override
    public void onDestroy() {
        
        Log.i(TAG, "-------onDestroy   ");
        super.onDestroy();
    }
    @Override
    public void onDetach() {
        
        Log.i(TAG, "-------onDetach   ");
        super.onDetach();
    }
}
