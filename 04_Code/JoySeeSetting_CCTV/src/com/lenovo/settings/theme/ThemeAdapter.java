package com.lenovo.settings.theme;

import java.util.List;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.lenovo.settings.R;
import com.lenovo.settings.theme.AsyncImageLoader.ImageCallback;


public class ThemeAdapter extends ArrayAdapter<ThemeBitmap>{

	private GridView gridView;
    private AsyncImageLoader asyncImageLoader;
    private String TAG = "ThemeAdapter";
    Handler mHandler;
    public ThemeAdapter(Activity activity, List<ThemeBitmap> imageAndTexts, GridView gridView1,Handler mHandler) {
        super(activity, 0, imageAndTexts);
        this.gridView = gridView1;
        this.mHandler = mHandler;
        asyncImageLoader = new AsyncImageLoader();
//        mHandler.sendEmptyMessage(ThemeSetting.THEME_LOADING);
        Log.d(TAG, "----------ThemeAdapter------size = " + imageAndTexts.size());
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        Activity activity = (Activity) getContext();
        // Inflate the views from XML
        View rowView = convertView;
        ViewCache viewCache;
        if (rowView == null) {
            LayoutInflater inflater = activity.getLayoutInflater();
            rowView = inflater.inflate(R.layout.theme_list, null);
            viewCache = new ViewCache(rowView);
            rowView.setTag(viewCache);
        } else {
            viewCache = (ViewCache) rowView.getTag();
        }
        ThemeBitmap imageAndText = getItem(position);

        // Load the image and set it on the ImageView
        String imageUrl = imageAndText.getBitmapurl();
        ImageView imageView = viewCache.getImageView();
        imageView.setTag(imageUrl);
        Drawable cachedImage = asyncImageLoader.loadDrawable(imageUrl, new ImageCallback() {
            public void imageLoaded(Drawable imageDrawable, String imageUrl) {
                ImageView imageViewByTag = (ImageView) gridView.findViewWithTag(imageUrl);
                if (imageViewByTag != null) {
                    imageViewByTag.setImageDrawable(imageDrawable);
                    mHandler.sendEmptyMessage(ThemeSetting.THEME_LOAD_OVER);
                }
            }
        });
        if (cachedImage == null) {
            imageView.setImageResource(R.drawable.bg_app);
        }else{
            imageView.setImageDrawable(cachedImage);
        }
        // Set the text on the TextView
        TextView textView = viewCache.getTextView();
        Log.d(TAG, " imageAndText.getTitle() = " + imageAndText.getTitle());
        textView.setText(imageAndText.getTitle());
        
        ImageView textViewMsg = viewCache.getTextViewMsg();
        if(position == ThemeSetting.mIndexTheme){
        	textViewMsg.setVisibility(View.VISIBLE);
        }else{
        	textViewMsg.setVisibility(View.INVISIBLE);
        }
        /*if(imageAndText.getIscheck().equals("0"))
        	textViewMsg.setVisibility(View.VISIBLE);
        else
        	textViewMsg.setVisibility(View.INVISIBLE);*/
        if (position == getCount()) {
            Log.d(" imageLoaded ", "-----------------position = " + position);
            mHandler.sendEmptyMessage(ThemeSetting.THEME_LOAD_OVER);
        }
        return rowView;
    }

}
