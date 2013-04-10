package com.ismartv.ui;

import java.sql.Wrapper;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ismartv.doc.ISTVResource;
import com.ismartv.doc.ISTVVodHistoryDoc;
import com.ismartv.doc.ISTVVodRelateDoc;
import com.ismartv.ui.ISTVVodHome.MouseClick;
import com.ismartv.ui.widget.MovieItemView;
import com.ismartv.ui.widget.MovieScrollView;

public class ISTVVodRelateList extends ISTVVodActivity  {

    private static final String TAG = "ISTVVodRelateList";
    
    public static final int ITEM_PER_COL = 3;
    public static final int COL_PER_PAGE = 4;
    public static final int ITEM_PER_PAGE = 12;
    
    public static final int RES_INT_SECTION_COUNT = 0;
    public static final int RES_STR_SECTION_SLUG = 1;
    public static final int RES_STR_SECTION_TITLE = 2;
    public static final int RES_BMP_ITEM_THUMB = 3;
    public static final int RES_STR_ITEM_TITLE = 4;
    public static final int RES_INT_ITEM_PK = 5;
    public static final int RES_INT_ITEM_COUNT = 6;
    
    
    private int mSecCount;
    private int mSelSecId = -1;;
    
    private ISTVVodRelateDoc doc = null;
    
    private LinearLayout mSecListLayout = null;
    private LinearLayout mItemListLayout = null;
    private RelativeLayout bufferLayout = null;
    private AnimationDrawable bufferAnim = null;
    private boolean bufferShow=false;
 //   private MovieScrollView mItemScrollView = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Relate List create");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
  
        
        setContentView(R.layout.vod_relate);
        rootView=this.findViewById(R.id.rootView);
        
        Bundle bundle = getIntent().getExtras();
        if(bundle==null){
            Log.e(TAG, "No pk set!!!");
            return;
        }
        int pk = bundle.getInt("itemPK");
        Log.d(TAG, "pkItem="+pk);

        doc = new ISTVVodRelateDoc(pk);
        setDoc(doc); 
        
        bufferLayout = (RelativeLayout)findViewById(R.id.BufferLayout);
        bufferAnim = (AnimationDrawable)((ImageView)findViewById(R.id.BufferImage)).getBackground();
        showBuffer();
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        doc.dispose();
        super.onDestroy();
    }
    
    
    protected void onDocGotResource(ISTVResource res){
        switch(res.getType()){
            case RES_INT_SECTION_COUNT:
                mSecCount = res.getInt();
                Log.d("RES_INT_SECTION_COUNT","size="+mSecCount);               
                initSectionList();
                doc.getSections();
                initItemList();
                View view = mSecListLayout.findViewById(0);             
                view.setSelected(true);
                view.requestFocus();
                onSectionSelected(0);
            break;
        case RES_STR_SECTION_TITLE:
    //        mSecTitle[res.getID()] = res.getString();
            updateSectionTitle(res.getID(), res.getString());
            break;
        case RES_INT_ITEM_COUNT:
 //           mSecItemCount[res.getID()] = res.getInt();
            Log.d(TAG, "count = "+res.getInt());
            hideBuffer();
            if(res.getInt()==0)
                showNoItemTip();
            else
                hideNoItemTip();
            break;
      case RES_STR_SECTION_SLUG:
        //    mSecSlug[res.getID()] = res.getString();
            Log.d(TAG, "id = "+res.getID()+", slug = "+res.getString());
            break;
        case RES_INT_ITEM_PK:
            Log.d("RES_STR_ITEM_PK","id = "+res.getID()+", item pk ="+res.getInt());
            setItemPk(res.getID(),res.getInt());
            break;
        case RES_STR_ITEM_TITLE:
            Log.d("RES_STR_ITEM_TITLE","id = "+res.getID()+", item title ="+res.getString());
            updateItemTitle(res.getID(), res.getString());
            break;
        case RES_BMP_ITEM_THUMB:
            Bitmap bmp = res.getBitmap();
            Log.d("RES_BMP_ITEM_THUMB","id = "+res.getID()+", item thumb size ="+bmp.getWidth()+"*"+bmp.getHeight());
            updateItemPoster(res.getID(),bmp);
            break;
    /*    case RES_INT_TOTAL_COL_COUNT:
            int colCount = doc.getTotalColCount();
            Log.d(TAG, "###############ColCount = "+colCount);
      //      doc.getItemByStartCol(0,colCount);
            initItemList(colCount); 
            doc.getItemByStartCol(0,CACHE_ITEM_COUNT/ITEM_PER_COL);*/
        default:
            break;
    }
}

    protected void onDocUpdate(){
        /*Redraw the activity*/
    } 

    private void initSectionList() {
        mSecListLayout = (LinearLayout) this.findViewById(R.id.seclist);
        
        if(mSecCount >8){
        	View down_view = (View) findViewById(R.id.seclist_down_arrow);
        	down_view.setVisibility(View.VISIBLE);
        }
        
        
        for (int i = 0; i < mSecCount; i++) {
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.relate_sec_item, null);
    
            view.setFocusable(true);
            view.setFocusableInTouchMode(true);

			view.setOnClickListener(new SelClick());
            view.setLayoutParams(new LinearLayout.LayoutParams(406, 100));
			TextView text = (TextView) view.findViewById(R.id.sec_text);
			text.setAlpha(0.5f);
			view.setId(i);
			
            mSecListLayout.addView(view);
            
            view.setOnFocusChangeListener(new OnFocusChangeListener() {
				public void onFocusChange(View v, boolean isFocused) {
					if (isFocused == true) {
						v.setBackgroundResource(R.drawable.vodhome_channellist_focus);
						TextView text0 = (TextView) v
								.findViewById(R.id.sec_text);
						text0.setAlpha(1);
					} else {
						v.setBackgroundDrawable(null);
						TextView text0 = (TextView) v
								.findViewById(R.id.sec_text);
						text0.setAlpha(0.5f);
					}
				}
			});
            if (i == 0) {
				view.setOnKeyListener(new OnKeyListener() {
					public boolean onKey(View v, int keyCode, KeyEvent event) {
						switch (keyCode) {
						case KeyEvent.KEYCODE_DPAD_UP:
							View up_view = (View) findViewById(R.id.seclist_up_arrow);
							up_view.setVisibility(View.INVISIBLE);
						}
						return false;
					}
				});
			} 
            
            if (i == (mSecCount-1)) {
				view.setOnKeyListener(new OnKeyListener() {
					public boolean onKey(View v, int keyCode, KeyEvent event) {
						switch (keyCode) {
						case KeyEvent.KEYCODE_DPAD_DOWN:
							View down_view = (View) findViewById(R.id.seclist_down_arrow);
							down_view.setVisibility(View.INVISIBLE);
							return true;
						}
						
						return false;
					}
				});
			}
            
            if(mSecCount > 8){
				if( i == 8 ) {
					view.setOnKeyListener(new OnKeyListener() {
						public boolean onKey(View v, int keyCode, KeyEvent event) {
							switch (keyCode) {
							case KeyEvent.KEYCODE_DPAD_DOWN:
								View up_view = (View) findViewById(R.id.seclist_up_arrow);
								up_view.setVisibility(View.VISIBLE);
							}
							return false;
						}
					});
					
					if(i==(mSecCount-1)){
						view.setOnKeyListener(new OnKeyListener() {
							public boolean onKey(View v, int keyCode, KeyEvent event) {
								switch (keyCode) {
								case KeyEvent.KEYCODE_DPAD_DOWN:
									View up_view = (View) findViewById(R.id.seclist_up_arrow);
									up_view.setVisibility(View.VISIBLE);
									View down_view = (View) findViewById(R.id.seclist_down_arrow);
									down_view.setVisibility(View.INVISIBLE);
								}
								return false;
							}
						});
					}
					
				}
				if(i == mSecCount-9) {
					view.setOnKeyListener(new OnKeyListener() {
						public boolean onKey(View v, int keyCode, KeyEvent event) {
							switch (keyCode) {
							case KeyEvent.KEYCODE_DPAD_UP:
								View down_view = (View) findViewById(R.id.seclist_down_arrow);
								down_view.setVisibility(View.VISIBLE);
							}
							return false;
						}
					});
					
					if(i == 0){
						view.setOnKeyListener(new OnKeyListener() {
							public boolean onKey(View v, int keyCode, KeyEvent event) {
								switch (keyCode) {
								case KeyEvent.KEYCODE_DPAD_UP:
									View down_view = (View) findViewById(R.id.seclist_down_arrow);
									down_view.setVisibility(View.VISIBLE);
									View up_view = (View) findViewById(R.id.seclist_up_arrow);
									up_view.setVisibility(View.INVISIBLE);
								}
								return false;
							}
						});
					}
					
				} 
				
			}
        }      
    }
    
    class SelClick implements android.view.View.OnClickListener {
		public void onClick(View v) {
			v.setSelected(true);
            onSectionSelected(v.getId());
		}
    }
    private void initItemList() {
    	
       
        LayoutInflater inflater= (LayoutInflater) this
        .getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
  
        for(int row=0; row<ITEM_PER_COL; row++) {   
            View view = inflater.inflate(R.layout.relate_item, null);             
            mItemListLayout = (LinearLayout) this.findViewById(R.id.itemlist);
            LinearLayout.LayoutParams para = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    		para.setMargins(0, 0, 0, 24);
    		view.setLayoutParams(para);
    		
            mItemListLayout.addView(view); 
            
            for(int col=0;col<COL_PER_PAGE;col++){
                MovieItemView childView = null;
                if(col==0)
                    childView = (MovieItemView)view.findViewById(R.id.movie_view1);
                else if(col==1)
                    childView = (MovieItemView)view.findViewById(R.id.movie_view2);
                else if(col==2)
                    childView = (MovieItemView)view.findViewById(R.id.movie_view3);
                else if(col==3)
                    childView = (MovieItemView)view.findViewById(R.id.movie_view4);
                childView.setId(row*COL_PER_PAGE+col);
                childView.setFocusable(true);
                childView.setFocusableInTouchMode(true);                                 
                
                childView.setOnFocusChangeListener(new OnFocusChangeListener(){
    
                    public void onFocusChange(View v, boolean hasFocus) {
                        // TODO Auto-generated method stub        
                        Log.d(TAG, "OnFocus: v = "+ v + ",hasFocus = "+hasFocus);     
                        TextView text = (TextView)v.findViewById(R.id.movie_title);
                        View bg = v.findViewById(R.id.movie_bg);
                        if(hasFocus){                                                                              
                        	v.setBackgroundResource(R.drawable.voditem_focus);
                        	text.setSelected(true);
                        	text.setAlpha(1);
                        }
                        else {
                        	text.setAlpha(0.75f);
    						v.setBackgroundDrawable(null);
							text.setSelected(false);
                        }
                    }                  
                });
                
                childView.setOnKeyListener(new OnKeyListener(){
    
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        // TODO Auto-generated method stub
                        Log.d(TAG, "OnKey: id = " + v.getId() 
                            + ",keyCode = " + keyCode);
                        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
                            if (event.getAction() == KeyEvent.ACTION_DOWN) {  
                                if( v instanceof MovieItemView){
                                    MovieItemView itemView = (MovieItemView)v;
                                    Log.d(TAG, "select movie = "+itemView.getPk());
                                    gotoItemDetail(itemView.getPk());
                                }
                            }
                            return true;
                        } 
                        return false;
                    }
                    
                });
            }         
        }
    }
    
    protected void onSectionSelected(int secId){
    	
    	LinearLayout ll = (LinearLayout)this.findViewById(R.id.seclist);
        View view_sel = ll.getChildAt(secId);
        if(view_sel!=null) {
        	TextView text = (TextView) view_sel.findViewById(R.id.sec_text);          
            TextView txtView = (TextView) this.findViewById(R.id.relate_player_name);
            if(text!=null)
            	txtView.setText(text.getText());
        }
    	
        if(mSelSecId != secId){
            mSelSecId = secId;
            
            for(int id = 0; id< ITEM_PER_PAGE; id++){
                View view = mItemListLayout.findViewById(id);                        
                if(view!=null && view.getVisibility()==View.VISIBLE){
                    ImageView img = (ImageView) view.findViewById(R.id.movie_poster);            
                    img.setImageResource(R.drawable.default_poster);
                    view.setVisibility(View.INVISIBLE);
                }
            }
            showBuffer();
            doc.getItemBySection(secId); 
        }
    }
    
    private void gotoItemDetail(int pk){
        if(pk==-1){
        	return;
        }
        Bundle bundle = new Bundle();
        bundle.putInt("itemPK", pk);
        
        Intent intent = new Intent();
        intent.setClass(this, ISTVVodItemDetail.class);
        intent.putExtras(bundle);
        startActivity(intent);
//        finish();
    }
    
    private void updateSectionTitle(int id, String title){
    	Log.d("updateSectionTitle","id = " + id);
        View view = mSecListLayout.findViewById(id);
        if(view!=null) {
            TextView txtView = (TextView) view.findViewById(R.id.sec_text);
            txtView.setText(title);
        }
    }
    private void updateItemTitle(int id, String title) {        
        View view = mItemListLayout.findViewById(id);             
        
        if(view!=null){
            TextView text = (TextView) view.findViewById(R.id.movie_title);          
            text.setText(title);
            if(view.getVisibility() == View.INVISIBLE)
                view.setVisibility(View.VISIBLE);
        }
    }
    
    private void updateItemPoster(int id, Bitmap bmp) {        
        View view = mItemListLayout.findViewById(id);      
        
        if(view!=null){
            ImageView img = (ImageView) view.findViewById(R.id.movie_poster);            
            img.setImageBitmap(bmp);
            if(view.getVisibility() == View.INVISIBLE)
                view.setVisibility(View.VISIBLE);
        }
    }
    
    public void setItemPk(int id, int pk){        
        MovieItemView view = (MovieItemView)mItemListLayout.findViewById(id);    
        if(view!=null){
            view.setPk(pk);     
            if(view.getVisibility() == View.INVISIBLE)
                view.setVisibility(View.VISIBLE);
        }
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
    
    private void showNoItemTip(){
    }
    
    private void hideNoItemTip(){
    }
}
