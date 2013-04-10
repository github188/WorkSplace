
package com.joysee.adtv.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.joysee.adtv.R;
import com.joysee.adtv.common.DefaultParameter;
import com.joysee.adtv.common.DefaultParameter.DvbIntent;

/**
 * 搜索菜单主界面
 * @author songwenxuan
 */
public class SearchMenuActivity extends Activity implements OnClickListener {
    
    private LinearLayout mSearchFastView;
    private LinearLayout mSearchAllView;
    private LinearLayout mSearchManualView;
	private ImageView mFocusView;
//    private LinearLayout mSearchRessetTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_menu_layout);
//        FrameLayout search_menu_bg = (FrameLayout) findViewById(R.id.search_menu_bg);
//        
//        if(getThemePaper() != null){
//            search_menu_bg.setBackgroundDrawable(getThemePaper());
//        }
        findViews();
        setListener();
    }

    private void setListener() {
        mSearchFastView.setOnClickListener(this);
        mSearchAllView.setOnClickListener(this);
        mSearchManualView.setOnClickListener(this);
        
        mSearchFastView.setOnFocusChangeListener(onFocusChangeListener);
        mSearchAllView.setOnFocusChangeListener(onFocusChangeListener);
        mSearchManualView.setOnFocusChangeListener(onFocusChangeListener);
//        mSearchRessetTextView.setOnClickListener(this);
    }

    private void findViews() {
        mSearchFastView = (LinearLayout) findViewById(R.id.search_fast_search_ll);
        mSearchAllView = (LinearLayout) findViewById(R.id.search_all_search_ll);
        mSearchManualView = (LinearLayout) findViewById(R.id.search_manual_search_ll);
        mFocusView = (ImageView) findViewById(R.id.ivFocus);
    }

    @Override
    public void onClick(View v) {
        final String broadcast = getIntent().getStringExtra(DvbIntent.INTENT_SHOW_MENU);
        switch (v.getId()) {
            case R.id.search_fast_search_ll:
                Intent autoSearchIntent = new Intent(this,
                        SearchMainActivity.class);
                if (broadcast != null && broadcast.length() > 0) {
                    autoSearchIntent.putExtra(DvbIntent.INTENT_SHOW_MENU, 
                            DefaultParameter.DvbIntent.INTENT_BROADCAST);
                }
                startActivity(autoSearchIntent);
                break;
            case R.id.search_all_search_ll:
                Intent fullSearchIntent = new Intent(this,
                        SearchAllMenuActivity.class);
                fullSearchIntent.putExtra(SearchMainActivity.SEARCH_TYPE,
                        SearchMainActivity.ALL_SEARCH);
                if (broadcast != null && broadcast.length() > 0) {
                    fullSearchIntent.putExtra(DvbIntent.INTENT_SHOW_MENU, 
                            DefaultParameter.DvbIntent.INTENT_BROADCAST);
                }
                startActivity(fullSearchIntent);
                break;
            case R.id.search_manual_search_ll:
                Intent manualSearchIntent = new Intent(this,
                        SearchManualActivity.class);
                if (broadcast != null && broadcast.length() > 0) {
                    manualSearchIntent.putExtra(DvbIntent.INTENT_SHOW_MENU, 
                            DefaultParameter.DvbIntent.INTENT_BROADCAST);
                }
                startActivity(manualSearchIntent);
                break;
//            case R.id.search_resset_textview://恢复默认值
//                TransponderUtil.saveDefaultTransponer(this);
//                break;
        }
    }

    /**
     * 取setting背景图，用于设置成这个activity的背景
     * @return
     */
    public Drawable getThemePaper(){
        String url = Settings.System.getString(this.getContentResolver(), "settings.theme.url");
        if(url!=null && url.length()>0){
                Bitmap bitmap = BitmapFactory.decodeFile(url);
                Drawable drawable = new BitmapDrawable(bitmap);
                return drawable;
        }
        return null;
    }
    
    private OnFocusChangeListener onFocusChangeListener = new OnFocusChangeListener() {
		
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if(hasFocus){
				int [] location = new int [2];
				v.getLocationInWindow(location);
				MarginLayoutParams params = (MarginLayoutParams) mFocusView
						.getLayoutParams();
				params.topMargin = location[1];
				mFocusView.setLayoutParams(params);
				Animation anim = new AlphaAnimation(0.0f, 1.0f);
				anim.setDuration(300);
				anim.setFillAfter(true);
				anim.setFillEnabled(true);
				mFocusView.startAnimation(anim);
			}
		}
	};
}
