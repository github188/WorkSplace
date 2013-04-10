package com.ismartv.ui;

import com.ismartv.doc.ISTVResource;
import com.ismartv.doc.ISTVVodBookmarkDoc;
import com.ismartv.doc.ISTVVodHistoryDoc;
import com.ismartv.ui.widget.MovieItemView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.widget.RelativeLayout;

public class ISTVVodHistory extends ISTVVodActivity {
    
    private static final String TAG = "ISTVVodHistory";
    
    private ISTVVodHistoryDoc doc = null;    
 
    public static final int RES_INT_ITEMCOUNT = 0;
    public static final int RES_BMP_ITEM_POSTER = 1;
    public static final int RES_STR_ITEM_TITLE = 2;
    public static final int RES_INT_ITEM_PK = 3;
    public static final int RES_STR_ITEM_MODEL = 4;
    public static final int RES_INT_SECTION_COUNT = 5;
    public static final int RES_STR_SECTION_TITLE = 6;
    public static final int RES_INT_SECTION_ITEMCOUNT = 7;
    public static final int RES_INT_SECTION_START = 8;
    public static final int RES_INT_PK = 9;
       
    private String mSecTitle[];
    private Integer mSecItemCount[];
    private Integer mSecStart[];
    private int mSecCount;
    private int mSelSecId;
    private int mFocusedRow = 0;
    
    private LinearLayout mSecListLayout = null;
    private LinearLayout mItemListLayout = null;
    private ScrollView mItemScrollView = null;

	private RelativeLayout bufferLayout;
	private AnimationDrawable bufferAnim;
	
	private View focus_item_view;
	
	private int history_count = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "History create");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		initView(-1);
        doc = new ISTVVodHistoryDoc();
        setDoc(doc);   
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
                if(mSecCount>0){
                    mSecTitle = new String[mSecCount];
                    mSecItemCount = new Integer[mSecCount];  
                    mSecStart = new Integer[mSecCount];  
                }
                break;
            case RES_STR_SECTION_TITLE:
                Log.d("RES_STR_SECTION_TITLE","title="+res.getString());
                mSecTitle[res.getID()] = res.getString();
                updateSectionTitle(res.getID(),res.getString());
                break;
            case RES_INT_SECTION_ITEMCOUNT:
                Log.d("RES_INT_SECTION_ITEMCOUNT","count="+res.getInt());
                mSecItemCount[res.getID()] = res.getInt();
                break;
            case RES_INT_SECTION_START:   
                Log.d("RES_INT_SECTION_START","start="+res.getInt());
                mSecStart[res.getID()] = res.getInt();
                if(res.getID()==mSecCount-1){
                    showSectionTag();
                    hideInvalidItem(); 
                    setSectionViewListener();
                    setItemViewListener(doc.getHistoryRowCount());
                }
                       
                break;
            case RES_INT_ITEM_PK:
                Log.d("RES_STR_ITEM_PK","item pk ="+res.getInt() +",id="+res.getID());
                setItemPk(res.getID(),res.getInt());
                break;
            case RES_INT_PK:
                Log.d("RES_STR_PK","pk ="+res.getInt() +",id="+res.getID());
                setPk(res.getID(),res.getInt());
                break;
            case RES_STR_ITEM_TITLE:
                Log.d("RES_STR_ITEM_TITLE","item:"+res.getID()+", title ="+res.getString());
                updateItemTitle(res.getID(), res.getString());
                break;
            case RES_BMP_ITEM_POSTER:
                Bitmap bmp = res.getBitmap();
//                Log.d("RES_BMP_ITEM_POSTER","item thumb size ="+bmp.getWidth()+"*"+bmp.getHeight());
                updateItemPoster(res.getID(),bmp);
                break;                
            case RES_STR_ITEM_MODEL:
                updateItemComment(res.getID(), res.getString());
                break;
            case RES_INT_ITEMCOUNT:                                
                Log.d(TAG, "###############count = "+res.getInt());
				history_count = res.getInt();
                initView(res.getInt());
                break;
            default:
                break;
        }
    }
    
    protected void onDocUpdate(){
        /*Redraw the activity*/
    } 
    
    @Override
    protected void onResume() {
        Log.d(TAG,"########onResume");
        super.onResume();
        doc.fetchHistoryData();
    }
    
    @Override
    protected void onStart() {
        Log.d(TAG,"########onStart");
        super.onStart();
    }
    
   
    @Override
    public boolean onCreateVodMenu(ISTVVodMenu menu) {
        // TODO Auto-generated method stub
        Log.d(TAG, "vod_history_clear");
        menu.addItem(0, getResources().getString(R.string.vod_history_clear));      
        menu.addItem(1, getResources().getString(R.string.vod_history_clear_one));
        return true;
    }

	public boolean onVodMenuOpened(ISTVVodMenu menu) {
		ISTVVodMenuItem item;
		if(history_count == 0) {
			item = menu.findItem(0);
			item.disable();
			item = menu.findItem(1);
			item.disable();
		}else {
			item = menu.findItem(0);
			item.enable();
			if ((focus_item_view != null) && ( focus_item_view instanceof MovieItemView)){		
				item = menu.findItem(1);
				item.enable();		
			}else {
				item = menu.findItem(1);
				item.disable();
			}
		}				
		return true;
	}
    @Override
    public boolean onVodMenuClicked(ISTVVodMenu menu, int id) {  
        if(id==0){
            showPopupDialog(DIALOG_OK_CANCEL, getResources().getString(R.string.vod_history_clear_dialog));           
        }else if(id == 1){
			if ((focus_item_view != null) && ( focus_item_view instanceof MovieItemView)){
				MovieItemView itemView = (MovieItemView)focus_item_view;
				
//				Object obj=itemView.getTag();
//				Log.d(TAG, "------ clear a pk="+itemView.getPk()+";tag="+obj);
//				if(obj!=null&&Integer.class.isInstance(obj)){
//				    doc.removeHistory((Integer)obj);
//				}else{
//				    doc.removeHistory(itemView.getPk());
//				}
//
//				doc.dispose();
//				doc = new ISTVVodHistoryDoc();
//				setDoc(doc);  
//				doc.fetchHistoryData();
				if(itemView.getTag()!=null){
				    Integer pk=(Integer)itemView.getTag();
				    onPopupDialogClicked(id,pk);
				}else{
				    onPopupDialogClicked(id,itemView.getPk());
				}
				//load ui again 
				doc.dispose();
				doc = new ISTVVodHistoryDoc();
				setDoc(doc);  
				doc.fetchHistoryData();
			}	
		}
        return true;
    }
       
    @Override
    public void onPopupDialogClicked(int which) {
    	/* 清除全部 */
    	if(which==0){
            doc.clearAllHistory();
        }
    }
    private void onPopupDialogClicked(int which,int itemPk){
    	/* 清除一个 */
    	if(which==1){
        	doc.clearOneHistory(which,itemPk);
        }
    }
    
    
    public void initView(int count) {
		if(count == -1){
			setContentView(R.layout.vod_buffer);			
			bufferLayout = (RelativeLayout)findViewById(R.id.BufferLayout);
			bufferAnim = (AnimationDrawable)((ImageView)findViewById(R.id.BufferImage)).getBackground();
			showBuffer();				
        }else if(count==0){
            setContentView(R.layout.vod_history_empty);
            mItemListLayout = (LinearLayout) this.findViewById(R.id.itemlist);
            initRecommentList(ISTVVodConstant.RECOMMENT_ITEM_COUNT);
            doc.getRecommentItem();
			mItemListLayout.requestFocus();
			hideBuffer();			
        }
        else {
            setContentView(R.layout.vod_history);      
            
            mSecListLayout = (LinearLayout) this.findViewById(R.id.seclist);
            mItemListLayout = (LinearLayout) this.findViewById(R.id.itemlist);
            mItemScrollView = (ScrollView) this.findViewById(R.id.itemScrollView);
       //     mItemScrollView.setItemWidth(320);
            
            initSectionList(mSecCount);          
            int rowCount = doc.getHistoryRowCount();			
            initItemList(rowCount); 
            
            doc.getHistorySections();
            doc.getHistoryItem();
			mSecListLayout.requestFocus();    
			hideBuffer();
        }            
		rootView=this.findViewById(R.id.rootView);
		if(rootView!=null){
			Drawable bg = getThemePaper();
	        if (bg != null) {
	        	rootView.setBackgroundDrawable(bg);
	        } else {
	            Log.d(TAG, "getThemePaper fail");
	        }
		}
    }
    
    public void initSectionList(int size) {
        LayoutInflater inflater = (LayoutInflater) this
        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (int i = 0; i < size; i++) {
            View view = inflater.inflate(R.layout.movielist_sec_item, null);
            view.setFocusable(true);
            view.setFocusableInTouchMode(true);
            view.setId(i);
//            view.setOnClickListener(new View.OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					// TODO srollview scroll to 
//					
//				}
//			});
            mSecListLayout.addView(view);
            
        }      
    }
      
    public void initItemList(int rowCount) {
        LayoutInflater inflater= (LayoutInflater) this
        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);                            
        
        for(int row=0;row<rowCount;row++) {   
            View view = inflater.inflate(R.layout.history_item, null);             
            mItemListLayout.addView(view);  
            
            for(int col=0; col<ISTVVodConstant.ITEM_PER_ROW; col++){
                MovieItemView childView = null;
                if(col==0)
                    childView = (MovieItemView)view.findViewById(R.id.movie_view1);
                else if(col==1)
                    childView = (MovieItemView)view.findViewById(R.id.movie_view2);
                else if(col==2)
                    childView = (MovieItemView)view.findViewById(R.id.movie_view3);
                else if(col==3)
                    childView = (MovieItemView)view.findViewById(R.id.movie_view4);
                Log.d(TAG, "initItemList id="+(row*ISTVVodConstant.ITEM_PER_ROW+col));
                childView.setId(row*ISTVVodConstant.ITEM_PER_ROW+col);
                childView.setFocusable(true);
                childView.setFocusableInTouchMode(true);         
                childView.setOnFocusChangeListener(new OnFocusChangeListener(){
    				@Override
    				public void onFocusChange(View v, boolean hasFocus) {
    					TextView text = (TextView)v.findViewById(R.id.movie_title);
    					 if(hasFocus){
    						 text.setSelected(true);
    						 focus_item_view = v;
    					 }else{
    						 text.setSelected(false);
    					 }
    				}
                });
            }         
        }        
    }
    
    private void initRecommentList(int itemCount){
        LayoutInflater inflater= (LayoutInflater) this
        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);          
        
        for(int i=0;i<itemCount; i++){
            View view = inflater.inflate(R.layout.history_recomm_item, null);             
            mItemListLayout.addView(view);   
            
            view.setId(i);
            view.setFocusable(true);
            view.setFocusableInTouchMode(true);                                 
            
            view.setOnFocusChangeListener(new OnFocusChangeListener(){

                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    // TODO Auto-generated method stub        
                    Log.d(TAG, "OnFocus: v = "+ v + ",hasFocus = "+hasFocus);     
                    TextView text = (TextView)v.findViewById(R.id.movie_title);
                    View bg = v.findViewById(R.id.movie_bg);
                    if(hasFocus){                                                     
                        text.setTextColor(getResources().getColor(R.color.vod_movieitem_sel));
						text.setSelected(true);
                        bg.setBackgroundResource(R.drawable.voditem_focus);
                    }
                    else {
                        text.setTextColor(getResources().getColor(R.color.vod_movieitem_nosel));
						text.setSelected(false);
                        bg.setBackgroundDrawable(null);
                    }
                }                  
            });
            
            view.setOnKeyListener(new OnKeyListener(){

                @Override
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

    public void showSectionTag(){
        for(int i = 0;i < mSecCount; i++) {
            String secTitle = mSecTitle[i];
            int startRow = mSecStart[i];
            
            Log.d(TAG, "show tag: row="+startRow+", sec="+secTitle);
            View view = mItemListLayout.getChildAt(startRow);
            
            View secStart = view.findViewById(R.id.sec_start);
                    
            TextView secTag = (TextView) view.findViewById(R.id.sec_tag);
            secTag.setText(secTitle);
            
            secStart.setVisibility(View.VISIBLE);
        }       
    }
    
    public void hideInvalidItem() {
        for(int i = 0;i < mSecCount; i++) {
            int startRow = mSecStart[i];
            int itemCount = mSecItemCount[i];
            
            int itemCountInEndRow = itemCount % ISTVVodConstant.ITEM_PER_ROW;
            if(itemCountInEndRow>0){
                int endRow = mSecItemCount[i]/ISTVVodConstant.ITEM_PER_ROW +startRow;
                for(int col = ISTVVodConstant.ITEM_PER_ROW-1; col>=itemCountInEndRow; col--){
                    View view = mItemListLayout.findViewById(endRow*ISTVVodConstant.ITEM_PER_ROW+col);
                    view.setVisibility(View.INVISIBLE);
                }                    
            }
        }
    }
    
    private void setSectionViewListener(){
        for (int i = 0; i < mSecCount; i++) {
            View view = mSecListLayout.findViewById(i);
            view.setOnKeyListener(new OnKeyListener(){

                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    // TODO Auto-generated method stub
                    Log.d(TAG, "OnKey: v = " + v + "id = " + v.getId()
                        + ",keyCode = " + keyCode);
                    if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
                        if (event.getAction() == KeyEvent.ACTION_DOWN) {
                            if(v.getId()!=mSelSecId) {
                                View view = mSecListLayout.getChildAt(mSelSecId);
                                if (view != null) {
                                    view.setSelected(false);
                                    TextView txtView = (TextView) view
                                        .findViewById(R.id.sec_text);
                                    txtView.setBackgroundDrawable(null);
                                }
                                v.setSelected(true);
                                onSectionSelected(v.getId());
                            }
                        }
                        return true;
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        if (v.getId() == 0) {
                            return true;
                        }
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        if (v.getId() == mSecCount - 1)
                            return true;
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        return true;
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                        if (event.getAction() == KeyEvent.ACTION_DOWN) {
                            View focusView = mItemListLayout.findViewById(ISTVVodConstant.ITEM_PER_ROW*mFocusedRow);
                        	if(focusView!=null)                                
                                focusView.requestFocus();
                            else
                                mItemListLayout.requestFocus();
                        }
                        return true;
                    }
                    return false;
                }                
            });
            
            view.setOnFocusChangeListener(new OnFocusChangeListener(){

                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    // TODO Auto-generated method stub
                    Log.d(TAG, "OnFocus: v = "+ v + "id = " + v.getId() + ",hasFocus = "+hasFocus);
                    
                    if(hasFocus){
                    	for(int i=0; i<mSecCount;i++){
                    		View view = mSecListLayout.findViewById(i);
                    		TextView txt = (TextView) view.findViewById(R.id.sec_text);
                    		if(i==v.getId()){
		            			txt.setBackgroundResource(R.drawable.voditem_sec_focus); 
		            			txt.setTextColor(Color.rgb(240, 240, 240));
                    		}
                    		else {
                    			txt.setBackgroundDrawable(null); 
                    			txt.setTextColor(Color.argb(128, 240, 240, 240));
                    		}
                    	}
                    	focus_item_view = null;
            		}                   			
                }           
            });
            
            if (i == 0) {                
                view.setSelected(true);
                view.requestFocus();
                onSectionSelected(0);
            }
        }
    }
        
    private void setItemViewListener(int rowCount){
        for(int id=0;id<rowCount*ISTVVodConstant.ITEM_PER_ROW;id++) {   
            View view = mItemListLayout.findViewById(id) ;

            view.setOnFocusChangeListener(new OnFocusChangeListener(){
    
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    // TODO Auto-generated method stub        
                    Log.d(TAG, "OnFocus: v = "+ v.getId() + ",hasFocus = "+hasFocus);     
                    TextView text = (TextView)v.findViewById(R.id.movie_title);
                    View bg = v.findViewById(R.id.movie_bg);
                    if(hasFocus){
                    	Log.d(TAG, "---text="+text.getText().toString());
                        int row = v.getId()/ISTVVodConstant.ITEM_PER_ROW;                            
                        setSelectedSectionByFocusRow(row);                                                      
                        text.setTextColor(getResources().getColor(R.color.vod_movieitem_sel));
						text.setSelected(true);
                        bg.setBackgroundResource(R.drawable.voditem_focus);
                        focus_item_view = v;
                    }
                    else {
                        text.setTextColor(getResources().getColor(R.color.vod_movieitem_nosel));
						text.setSelected(false);
                        bg.setBackgroundDrawable(null);
                    }
                }                  
            });
            
            view.setOnKeyListener(new OnKeyListener(){
    
                @Override
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
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        Log.d(TAG, "getScrollX()="+mItemScrollView.getScrollX()+",Max="+mItemScrollView.getMaxScrollAmount());
                            return false;
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        Log.d(TAG, "getScrollX()="+mItemScrollView.getScrollX()+",Max="+mItemScrollView.getMaxScrollAmount());
    
                            return false;
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {  
                        if(event.getAction()==KeyEvent.ACTION_DOWN){
                            if(v.getId()%ISTVVodConstant.ITEM_PER_ROW==0){
                                int selSecId = getSelectedSectionIdByFocusRow(v.getId()/ISTVVodConstant.ITEM_PER_ROW);
                                Log.d(TAG, "Focis Section: "+selSecId);
                                if(selSecId!=-1){
                                    View selSec = mSecListLayout.getChildAt(selSecId);
                                    if(selSec !=null){
                                        selSec.requestFocus();
                                        return true;
                                    }
                                }                                    
                            }
                        }
                    }
                    /*else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        if (event.getAction() == KeyEvent.ACTION_DOWN) {
                            mItemListLayout.requestFocus();
                        }
                        return true;
                    }*/
                    return false;
                }
                
            });
        }
    }
    
    public void updateItemTitle(int id, String title) {        
        View view = mItemListLayout.findViewById(id);
        
        if(view!=null){
            TextView text = (TextView) view.findViewById(R.id.movie_title);          
            text.setText(title);
        }
    }
    
    public void updateItemComment(int id, String contentModel) {        
        View view = mItemListLayout.findViewById(id);
        
        if(view!=null){
            TextView text = (TextView) view.findViewById(R.id.movie_comment);
            text.setText(contentModel);
        }
    }
    
    public void updateItemPoster(int id, Bitmap bmp) {        
        View view = mItemListLayout.findViewById(id);
        
        if(view!=null){
            ImageView img = (ImageView) view.findViewById(R.id.movie_poster);            
            img.setImageBitmap(bmp);
        }
    }
    
    public void setItemPk(int id, int pk){        
        MovieItemView view = (MovieItemView)mItemListLayout.findViewById(id);
		if(view == null) {
			Log.d(TAG, "view is null");
			return;
		}
        view.setPk(pk);
    }
    
    /**
     * 主要是用于电视剧
     * @param id
     * @param pk
     */
    public void setPk(int id,int pk){
        MovieItemView view = (MovieItemView)mItemListLayout.findViewById(id);
        if(view == null) {
            Log.d(TAG, "view is null");
            return;
        }
        Log.d(TAG, "setTag pk="+pk);
        view.setTag(pk);
    }
    
    protected void onSectionSelected(int secId){
        mSelSecId = secId;
        Log.d(TAG,"############### onSectionSelected : " + secId ) ;
        String secTitle = mSecTitle[secId]; 
        int startRow = mSecStart[secId];   
        Log.d(TAG, "show tag: row="+startRow+", sec="+secTitle);
		@SuppressWarnings("deprecation")
		AbsoluteLayout layout = (AbsoluteLayout) mItemListLayout.getChildAt(startRow);
        View view = layout.getChildAt(0);
        if(view!=null){
        	mItemScrollView.scrollTo( 0, startRow*view.getHeight());
        	view.setSelected(true);
        	view.requestFocus();
        }
//        View selectView = mSecListLayout.findViewById(secId);
//        if(selectView!=null){
//        	selectView.setNextFocusRightId(1);
//        }
    }

    private void gotoItemDetail(int pk){
        if(pk==-1)
            return;

        Bundle bundle = new Bundle();
        bundle.putInt("itemPK", pk);
        
        Intent intent = new Intent();
        intent.setClass(this, ISTVVodItemDetail.class);
        intent.putExtras(bundle);
        
        startActivityForResult(intent, 1);
    }
    
    private int getSelectedSectionIdByFocusRow(int newRow){
        if(newRow>= doc.getHistoryRowCount() || newRow<0)
            return -1;

        mFocusedRow = newRow;
        
        int newSecId = 0;
        for(int i=0;i<mSecCount-1; i++){
            if(newRow >= mSecStart[i] && newRow<mSecStart[i+1]){
                newSecId = i;
                break;
            }         
        }
        if(newRow>=mSecStart[mSecCount-1])
            newSecId = mSecCount-1;
        
        return newSecId;
    }
    
    private void setSelectedSectionByFocusRow(int newRow){
        int newSecId = getSelectedSectionIdByFocusRow(newRow);
        if(newSecId == -1)
            return;
        
        Log.d("onScrollChanged", "newSecId="+newSecId);
        
        if(newSecId!=mSelSecId) {            
            View view = mSecListLayout.getChildAt(mSelSecId);
            if (view != null) {
                view.setSelected(false);
                TextView txtView = (TextView) view
                    .findViewById(R.id.sec_text);
                txtView.setBackgroundDrawable(null);
                txtView.setTextColor(Color.argb(128, 240, 240, 240));
            }
            
            View selView = mSecListLayout.getChildAt(newSecId);
            if (selView != null) {
                selView.setSelected(true);
                TextView txtView = (TextView) selView.findViewById(R.id.sec_text);
                txtView.setBackgroundResource(R.drawable.voditem_sec_focus); 
    			txtView.setTextColor(Color.rgb(240, 240, 240));
            }
            
            mSelSecId = newSecId;
        }  
    }
    
    private void updateSectionTitle(int id, String title){
        View view = mSecListLayout.findViewById(id);
        if(view!=null) {
            TextView txtView = (TextView) view.findViewById(R.id.sec_text);
            txtView.setText(title);
        }
    }


    public void showBuffer(){		
		bufferLayout.setVisibility(View.VISIBLE);
		bufferAnim.start();
	}

    public void hideBuffer(){
		bufferLayout.setVisibility(View.INVISIBLE);		
		bufferAnim.stop();
	}			
}
