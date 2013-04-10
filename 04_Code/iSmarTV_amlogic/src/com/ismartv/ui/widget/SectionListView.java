package com.ismartv.ui.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ismartv.ui.ISTVVodConstant;
import com.ismartv.ui.R;

public class SectionListView extends LinearLayout{

    private static final String TAG = "SectionListView";    

    private Context mContext = null;
    
    private Integer mSecItemCount[];
    private Integer mSecStart[];
    private int mSecCount;
    private int mTotalRowCount;
    private int mFocuseId;
    
    private ScrollView secScrollView = null;

    public void setScrollView(ScrollView secScrollView) {
        this.secScrollView = secScrollView;
    }

    public void setTotalRowCount(int totalRowCount) {
        this.mTotalRowCount = totalRowCount;
    }

    public int getSecCount() {
        return mSecCount;
    }

    private int mCurSecId;
    
    public int getCurSecId() {
        return mCurSecId;
    }

    private MovieListView itemView = null;
    private ImageView prevImg = null;
    private ImageView nextImg = null;
    
    public void setSecItemCount(int id, int count) {
        if(mSecItemCount!=null && id>=0 && id<mSecItemCount.length)
            mSecItemCount[id] = count;
    }

    public void setSecStart(int id, int start) {
        if(mSecStart!=null && id>=0 && id<mSecStart.length)
            mSecStart[id] = start;
    }

    public void setItemView(MovieListView itemView) {
        this.itemView = itemView;
    }
    
    public void setDirectionView(ImageView prevImg, ImageView nextImg) {
        this.prevImg = prevImg;
        this.nextImg = nextImg;
    }  

    public SectionListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }
    
    public void initSectionList(int size) {
        if(size >0){
            mSecCount = size;
            mSecItemCount = new Integer[mSecCount];  
            mSecStart = new Integer[mSecCount];  
        }
        for (int i = 0; i < size; i++) {            
            LayoutInflater inflater = (LayoutInflater) mContext
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.movielist_sec_item, null);
        
            view.setFocusable(true);
            view.setFocusableInTouchMode(true);
            view.setId(i);
            
            addView(view);
            
            view.setOnKeyListener(new OnKeyListener(){
                
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
                        if (event.getAction() == KeyEvent.ACTION_DOWN) {
                            if(v.getId()!=mCurSecId) {                            
                                View view = getChildAt(mCurSecId);
                                if (view != null) {
                                    view.setSelected(false);
                                }
                                
                                  //Add it by dgg 2012/04/06
                                if( itemView != null ){
                                			
                                			for ( int n = 0 ; n < itemView.getChildCount(); n ++ ){                                				
                                				View mview = itemView.getChildAt(n);                                				                                				
                                				View sectmp = (View)mview.findViewById(R.id.sec_start);	
                                				if( sectmp != null )
                                					sectmp.setVisibility(View.INVISIBLE);    
                                			}
                                			
                                    	View iview1= itemView.getChildAt(0);
                                    	if( iview1 != null ){
                                 
                                    		TextView secTitle = (TextView)iview1.findViewById(R.id.sec_tag);	
                                    		
                                    		if( secTitle != null ){                                    			
                                    			
                                    			Log.d( TAG, "####################### secTitle.getText:" + secTitle.getText().toString());
                                    			
                                    			TextView vtxt= (TextView) v.findViewById(R.id.sec_text);
        																	secTitle.setText( vtxt.getText().toString());	        																	
        																	Log.d( TAG, "####################### secTitle.setText:" + vtxt.getText().toString());
                                    		}
                                    		
                                    		View secStart = (View)iview1.findViewById(R.id.sec_start);	
                                    		if( secStart != null )
                                    			secStart.setVisibility(View.VISIBLE);                                        		
                                    	}                                    	                        															
                                }
 																//                                            
                                v.setSelected(true);
                                mCurSecId = v.getId();
                                itemView.onSectionSelected(v.getId(), mSecStart[v.getId()]);
                            }
                        }
                        return true;
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        if (v.getId() == 0) {
                            return true;
                        }
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        if (v.getId() == mSecCount - 1){
                            return true;
                        }
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        return true;
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                        if (event.getAction() == KeyEvent.ACTION_DOWN) {
                            int focusId = itemView.getFocusViewId();
                            if(focusId<0 || focusId>= 12)
                                focusId = 0;
                            View focusView = itemView.findViewById(focusId);
                            if(focusView!=null)                                
                                focusView.requestFocus();
                            else
                                itemView.requestFocus();
                        }
                        return true;
                    }
                    return false;
                }                
            });
            
            view.setOnFocusChangeListener(new OnFocusChangeListener(){
        
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                	Log.d(TAG, "---------onFocusChange--id="+v.getId()+", hasFocus="+hasFocus);  
                	
                    if(hasFocus){
                        mFocuseId=v.getId();
                    	for(int i=0; i<SectionListView.this.mSecCount;i++){
                    		View view = SectionListView.this.findViewById(i);
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
            			
                        int scrollY = secScrollView.getScrollY();
                        Log.d("TEST", "scrollY="+scrollY);
                        if(prevImg!=null)
                        	prevImg.setVisibility(scrollY>0? View.VISIBLE:View.INVISIBLE);
                        if(nextImg!=null)
                        	nextImg.setVisibility(scrollY<(mSecCount-ISTVVodConstant.SEC_ITEM_COUNT)*ISTVVodConstant.SEC_ITEM_HEIGHT? View.VISIBLE:View.INVISIBLE);  
            		}                   			                       			                                        
                }           
            });
        }      
    }   

    public void updateSectionTitle(int id, String title){
        View view = findViewById(id);        
        TextView txtView = (TextView) view.findViewById(R.id.sec_text);
        txtView.setText(title);
    }
    
    public void initFocusSection(int secId){       
        View view = findViewById(secId);
        if(view!=null){
            view.setSelected(true);
            mCurSecId = secId;
            view.requestFocus();               
        }
    }
    
    public void syncMoveListView(){
        if(mFocuseId==mCurSecId){
            return;
        }
        mFocuseId=mCurSecId;
        Log.d(TAG, "---syncMoveListView-----------------------");
        for(int i=0; i<SectionListView.this.mSecCount;i++){
            View view = SectionListView.this.findViewById(i);
            TextView txt = (TextView) view.findViewById(R.id.sec_text);
            if(mCurSecId==view.getId()){
                txt.setBackgroundResource(R.drawable.voditem_sec_focus); 
                txt.setTextColor(Color.rgb(240, 240, 240));
            }
            else {
                txt.setBackgroundDrawable(null); 
                txt.setTextColor(Color.argb(128, 240, 240, 240));
            }
        }
        
        int scrollY = secScrollView.getScrollY();
        Log.d("TEST", "scrollY="+scrollY);
        if(prevImg!=null)
            prevImg.setVisibility(scrollY>0? View.VISIBLE:View.INVISIBLE);
        if(nextImg!=null)
            nextImg.setVisibility(scrollY<(mSecCount-ISTVVodConstant.SEC_ITEM_COUNT)*ISTVVodConstant.SEC_ITEM_HEIGHT? View.VISIBLE:View.INVISIBLE);  
    
    }
    
    public void updateSectionPos(int row){       
        int newSecId =  getSectionIdByRow(row);
        if(newSecId == -1)
            return;        
        
        View selView = null;
        if(newSecId!=mCurSecId) {            
            /*View view = getChildAt(mCurSecId);
            if (view != null) {
                view.setSelected(false);
                TextView txt = (TextView) view.findViewById(R.id.sec_text);
                txt.setBackgroundDrawable(null); 
    			txt.setTextColor(Color.argb(128, 240, 240, 240));
            }*/
            for(int i=0; i<SectionListView.this.mSecCount;i++){
            	View view = SectionListView.this.findViewById(i);
            	if(view!=null){
            		view.setSelected(false);
            		TextView txt = (TextView) view.findViewById(R.id.sec_text);
            		txt.setBackgroundDrawable(null); 
        			txt.setTextColor(Color.argb(128, 240, 240, 240));
            	}
            }
            
            selView = getChildAt(newSecId);
            if (selView != null) {
                selView.setSelected(true);
                TextView selTxt = (TextView) selView.findViewById(R.id.sec_text);
                selTxt.setBackgroundResource(R.drawable.voditem_sec_focus); 
                selTxt.setTextColor(Color.rgb(240, 240, 240));
            }
            
            mCurSecId = newSecId;
        }
        
        if(selView!=null &&secScrollView!=null){
            int scrollY = secScrollView.getScrollY();
            int selViewY = mCurSecId * ISTVVodConstant.SEC_ITEM_HEIGHT;
            if(selViewY >=ISTVVodConstant.SEC_ITEM_HEIGHT*ISTVVodConstant.SEC_ITEM_COUNT+scrollY || selViewY<scrollY)
                secScrollView.scrollTo(0, selViewY);
        }
    }
    
    private int getSectionIdByRow(int row){
        if(row>= mTotalRowCount || row<0)
            return -1;
        
        int secId = 0;
        for(int i=0;i<mSecCount-1; i++){
            if(row >= mSecStart[i] && row<mSecStart[i+1]){
                secId = i;
                break;
            }         
        }
        if(row>=mSecStart[mSecCount-1])
            secId = mSecCount-1;
        
        return secId;
    }

}
