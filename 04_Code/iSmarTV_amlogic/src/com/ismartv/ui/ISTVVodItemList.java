package com.ismartv.ui;

import java.net.URL;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.widget.ScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ismartv.doc.ISTVResource;
import com.ismartv.doc.ISTVVodItemListDoc;
import com.ismartv.ui.listen.OnScrollColChangedListerner;
import com.ismartv.ui.widget.ItemScrollView;
import com.ismartv.ui.widget.MovieItemView;
import com.ismartv.ui.widget.MovieListView;
import com.ismartv.ui.widget.SectionListView;

public class ISTVVodItemList extends ISTVVodActivity{

    private static final String TAG = "ISTVVodItemList";
    
    public static final int RES_INT_SECTION_COUNT = 0;
    public static final int RES_STR_SECTION_SLUG = 1;
    public static final int RES_STR_SECTION_TITLE = 2;
    public static final int RES_INT_SECTION_ITEMCOUNT = 3;
    public static final int RES_INT_SECTION_START = 4;
    public static final int RES_STR_ITEM_THUMB_URL = 5;
    public static final int RES_STR_ITEM_TITLE = 6;
    public static final int RES_INT_ITEM_PK = 7;
    public static final int RES_INT_TOTAL_ROW_COUNT = 8;
    public static final int RES_BMP_ITEM_THUMB = 9;
    public static final int RES_BOOL_IS_LOADING = 10;
    public static final int RES_INT_INSDATA = 11;
    public static final int RES_STR_SEC_TITLE = 12;
  	public static final int RES_BOOL_IS_COMPLEX = 13;
    public static final int RES_BOOL_IS_ITEM_TITLE_FINISHED = 14;
	
    
    private ISTVVodItemListDoc doc = null;
    
    private SectionListView mSecListView = null;
    private MovieListView mItemListView = null;
    
    private RelativeLayout bufferLayout = null;
    private AnimationDrawable bufferAnim = null;
    private boolean bufferShow=false;   
    
    private boolean fromLaucher = true;

    public void onCreate(Bundle savedInstanceState){
        Log.d(TAG, "ItemList create");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.vod_itemlist);
        rootView=this.findViewById(R.id.rootView);

        Bundle bundle = getIntent().getExtras();
        
        String channelId, channelName;
        if(bundle==null){
            channelId = "movie";
            channelName = "电影";
        }else{
            channelId = bundle.getString("chan_id");
            channelName = bundle.getString("chan_name");
            String from = bundle.getString("from");
            if("home".equals(from))
                fromLaucher = false;
        }
        Log.d(TAG, "Channel id="+channelId+", name="+channelName+", from="+fromLaucher);

        doc = new ISTVVodItemListDoc(channelId);
        setDoc(doc);
        
        mSecListView = (SectionListView) this.findViewById(R.id.seclist);    
        mItemListView = (MovieListView) this.findViewById(R.id.itemListView);
        
        mSecListView.setItemView(mItemListView);
        ScrollView secScrollView = (ScrollView)this.findViewById(R.id.secScrollView);
        secScrollView.setScrollbarFadingEnabled(false);
        mSecListView.setScrollView(secScrollView);
        
        ImageView prev = (ImageView)this.findViewById(R.id.secPrev);
        ImageView next = (ImageView)this.findViewById(R.id.secNext);
        mSecListView.setDirectionView(prev, next);
        
        mItemListView.setDoc(doc);
        mItemListView.setActivity(this);
        mItemListView.setSectionList(mSecListView);
        
        
        TextView chanTitle = (TextView) this.findViewById(R.id.chan_title);
        chanTitle.setText(channelName);
        
        bufferLayout = (RelativeLayout)findViewById(R.id.BufferLayout);
        bufferAnim = (AnimationDrawable)((ImageView)findViewById(R.id.BufferImage)).getBackground();
        showBuffer();
    }
    
    public void onDestroy(){
        doc.dispose();
        super.onDestroy();
    }
    
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if(keyCode == KeyEvent.KEYCODE_BACK){
            if(fromLaucher){
                Log.d(TAG, "Kill thread!Quit!!!!");
//                android.os.Process.killProcess(android.os.Process.myPid());
//                return true;
            }           
        }
        return super.onKeyDown(keyCode, event);
    }

    protected void onDocGotResource(ISTVResource res){
        switch(res.getType()){
            case RES_INT_SECTION_COUNT:
                Log.d("RES_INT_SECTION_COUNT","size="+res.getInt());
                if(res.getInt()>0){                    
                    mSecListView.initSectionList(res.getInt());                    
                }               
                break;
            case RES_STR_SECTION_TITLE:
                mSecListView.updateSectionTitle(res.getID(), res.getString());                    
                break;
            case RES_INT_SECTION_ITEMCOUNT:
                mSecListView.setSecItemCount(res.getID(), res.getInt());
                break;
            case RES_INT_SECTION_START:    
                mSecListView.setSecStart(res.getID(), res.getInt());
                if(res.getID()==mSecListView.getSecCount()-1){
                    mSecListView.initFocusSection(0);
                    mItemListView.onSectionSelected(0, 0);
                    //hideBuffer();
                }
                break;
            case RES_STR_SECTION_SLUG:
                break;
            case RES_INT_ITEM_PK:
                Log.d("RES_STR_ITEM_PK","item id ="+res.getID()+", pk ="+res.getInt());
                mItemListView.setItemPk(res.getID(),res.getInt());
                break;
            case RES_STR_ITEM_TITLE:
                Log.d("RES_STR_ITEM_TITLE","################# item id ="+res.getID()+", title ="+res.getString());
                mItemListView.updateItemTitle(res.getID(), res.getString());
				hideBuffer();
                break;
			case RES_BOOL_IS_COMPLEX:
				mItemListView.setItemComplex(res.getID(), res.getBoolean());
				break;
            case RES_STR_ITEM_THUMB_URL:
                Log.d("RES_BMP_ITEM_THUMB_URL","item id ="+res.getID()+", thumb url="+res.getURL());
                mItemListView.updateItemPosterUrl(res.getID(),res.getURL());
                break;
            case RES_BMP_ITEM_THUMB:
                Log.d("RES_BMP_ITEM_THUMB","item id ="+res.getID()+", thumb="+res.getBitmap());
                mItemListView.updateItemPoster(res.getID(),res.getBitmap());
                break;
            case RES_INT_TOTAL_ROW_COUNT:
                mSecListView.setTotalRowCount(res.getInt());
                mItemListView.initItemList(ISTVVodConstant.ROW_PER_PAGE);
                doc.getSections();
                break;
            case RES_BOOL_IS_LOADING:
                Log.d("RES_BOOL_IS_LOADING","id ="+res.getID()+", loading="+res.getBoolean());
                if(!res.getBoolean()){
                    mItemListView.refreshData();
                }
                break;
            case RES_INT_INSDATA:
                Log.d("RES_INT_INSDATA","id ="+res.getID()+", insert="+res.getInt());
                mItemListView.onInsertData(res.getInt());
                break;
            case RES_STR_SEC_TITLE:
                Log.d("RES_STR_SEC_TITLE","col ="+res.getID()+", title="+res.getString());
                mItemListView.showSectionTag(res.getID(), res.getString());
				break;
			case RES_BOOL_IS_ITEM_TITLE_FINISHED:
				mItemListView.setTitileFinished(true);
				break;
            default:
                break;
        }
    }

    protected void onDocUpdate(){
        /*Redraw the activity*/
    } 
    
    public void showBuffer(){
        Log.d(TAG,"############SHOW Buffer: bufferShow="+bufferShow);
        if(!bufferShow){
            bufferLayout.setVisibility(View.VISIBLE);
            bufferAnim.start();
            bufferShow=true;
        }
    }

    public void hideBuffer(){
        Log.d(TAG,"############HIDE Buffer: bufferShow="+bufferShow);
        if(bufferShow){
            bufferLayout.setVisibility(View.INVISIBLE);
            bufferAnim.stop();
            bufferShow=false;
        }
    }     
}
