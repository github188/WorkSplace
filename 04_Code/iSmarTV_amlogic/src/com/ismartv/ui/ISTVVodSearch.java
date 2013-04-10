
package com.ismartv.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ismartv.doc.ISTVResource;
import com.ismartv.doc.ISTVVodSearchDoc;
import com.ismartv.ui.widget.MovieItemView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ISTVVodSearch extends ISTVVodActivity implements OnClickListener {

    private static final String TAG = "ISTVVodSearch";
    private static final int VOD_GET_SEARCH_RESULT = 0;
    private static final int VOD_REFLSE_SUGGEST_LIST = 1;
    private LinearLayout mMenuList, mSearchList;
    private ListView suggestListView;
    private AbsoluteLayout resultLayout;
    private EditText mSearchText;
    private Button mSearch;
    private ISTVVodSearchDoc doc;
    private View focus_item_view;
    
    public  Dialog dialog;
    private AnimationDrawable bufferAnim = null;
    
    private ArrayList<String> mHotWord;
    private ArrayList<Object> mResult;
    private ArrayList<String> mSuggest;
    private Map<String, ArrayList> mSuggestMap;
    private boolean isHasSoftKey = false; //是否有软键盘

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.vod_search);
        initView();
        initLoading();
        doc = new ISTVVodSearchDoc();
        setDoc(doc);
        doc.getHotwordsResult();
        showLoading();
    }

    private void initView() {
        mMenuList = (LinearLayout) this.findViewById(R.id.seclist);
        mSearchList = (LinearLayout) this.findViewById(R.id.search_result);
        mSearchText = (EditText) this.findViewById(R.id.search_et);
        suggestListView = (ListView)this.findViewById(R.id.listview);
        resultLayout = (AbsoluteLayout)this.findViewById(R.id.result_layout);
        mSearch = (Button) this.findViewById(R.id.search);
        mSearch.setOnClickListener(this);
        
        mHotWord = new ArrayList<String>();
        mResult = new ArrayList<Object>();
        mSuggest = new ArrayList<String>();
        mSuggestMap = new HashMap<String, ArrayList>();
        mSearchText.setNextFocusLeftId(mMenuList.getId());
        mMenuList.setNextFocusRightId(mSearchText.getId());
        
        initEditListener();
    }
    

    private void initHotWordMenu(final int count){
        if(count==0){
            return;
        }
        /* menu for hotWord */
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        for(int i=0;i<count;i++){
            View view = inflater.inflate(R.layout.movielist_sec_item, null);
            view.setFocusable(true);
            view.setFocusableInTouchMode(true);
            view.setId(i);
            TextView txt = (TextView) view.findViewById(R.id.sec_text);
            txt.setText(mHotWord.get(i));
            txt.setBackgroundDrawable(null); 
            txt.setTextColor(Color.argb(128, 240, 240, 240));
            mMenuList.addView(view);
        }
        for(int i=0;i<count;i++){
            final View view = mMenuList.getChildAt(i);
            
            view.setOnKeyListener(new View.OnKeyListener() {
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if(keyCode == KeyEvent.KEYCODE_DPAD_UP && event.getAction() == KeyEvent.ACTION_DOWN) {
                        Log.d(TAG, "----up----   view.id = "+v.getId());
                        if (v.getId() == 0) {
                            return true;
                        }
                    }else if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN && event.getAction() == KeyEvent.ACTION_DOWN){
                        Log.d(TAG, "----down----   view.id = "+v.getId());
                        if (v.getId() == mHotWord.size()-1) {
                            return true;
                        }
                    }else if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && event.getAction() == KeyEvent.ACTION_DOWN){
                        Log.d(TAG, "----right----   mSearchList.count = "+mSearchList.getChildCount());
                        if (mSearchList.getChildCount()<=0){
                            mSearchText.requestFocus();
                        }else{
                            mSearchList.getChildAt(0).requestFocus();
                        }
                        return true;
                    }else if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT && event.getAction() == KeyEvent.ACTION_DOWN){
                        Log.d(TAG, "----left----");
                        return true;
                    }
                    return false;
                }
            });
            
            view.setOnFocusChangeListener(new OnFocusChangeListener() {
                public void onFocusChange(View v, boolean hasFocus) {
                    if(hasFocus){
                        for(int i=0; i<count;i++){
                            TextView txt = (TextView) mMenuList.findViewById(i).findViewById(R.id.sec_text);
                            if(i==view.getId()){
                                txt.setBackgroundResource(R.drawable.voditem_sec_focus); 
                                txt.setTextColor(Color.rgb(240, 240, 240));
                            }
                            else{
                                txt.setBackgroundDrawable(null); 
                                txt.setTextColor(Color.argb(128, 240, 240, 240));
                            }
                        }
                    }else{
                        view.setSelected(false);
                        view.findViewById(R.id.sec_text).setBackgroundDrawable(null);
                        ((TextView) view.findViewById(R.id.sec_text)).setTextColor(Color.argb(128, 240, 240, 240));
                    }
                }
            });
            
            view.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Log.d(TAG, ">>>>>> search hotWord : "+mHotWord.get(v.getId()));
                    allSearch(mHotWord.get(v.getId()));
                }
            }); 
            
            if(mMenuList.getChildAt(0)!=null){
                if (i == 0) {                
                    mMenuList.getChildAt(0).setSelected(true);
                    mMenuList.getChildAt(0).requestFocus();
                }
            }
        }
    }
    
    /* 输入框事件监听 */
    private void initEditListener(){
        mSearchText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    mSearchText.setBackgroundResource(R.drawable.vod_popuplogin_editbgfocus);
                }else{
                    mSearchText.setBackgroundResource(R.drawable.vod_popuplogin_editbg);
                }
            }
        });
        mSearchText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                
                if(keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER){
                    InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    im.showSoftInput(mSearchText, 0);
                    isHasSoftKey = true;
                    return true;
                }else if((keyCode==KeyEvent.KEYCODE_ESCAPE||keyCode==KeyEvent.KEYCODE_BACK) //
                        &&event.getAction() == KeyEvent.ACTION_DOWN){
                    Log.d(TAG, "-----OnKeyListener------");
                    if(isHasSoftKey){
                        isHasSoftKey = false;
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
                        imm.hideSoftInputFromWindow(mSearchText.getWindowToken(),0);
                        return true;
                    }else{
                        finish();
                        return true;
                    }
                }else if(event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_LEFT){
                    if(mMenuList!=null && mMenuList.getChildAt(0)!=null){
                        mMenuList.getChildAt(0).requestFocus();
                        return true;
                    }
                }else if(event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
                    if(mSearchList!=null && mSearchList.getChildAt(0)!=null){
                        mSearchList.getChildAt(0).requestFocus();
                        return true;
                    }
                }else if(event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
                    if(mSearchText.getSelectionEnd()==mSearchText.getText().toString().length()-1){
                        mSearch.requestFocus();
                        return true;
                    }
                }
                return false;
            }
        });
        /* 输入信息监听 */
        mSearchText.addTextChangedListener(new TextWatcher() {
            
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String searchKey = mSearchText.getText().toString();
                if(searchKey!=null && !"".equals(searchKey.trim())){
                    Log.d(TAG, "   >>>>> search key : "+searchKey);
//                    suggestSearch(searchKey);
                }
            }
            
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            public void afterTextChanged(Editable s) {
            }
        });
        
    }
    
    
    private void initLoading() {
        dialog = new Dialog(this, R.style.FullHeightDialog);
        dialog.setContentView(R.layout.dialog);
        bufferAnim = (AnimationDrawable)((ImageView)dialog.findViewById(R.id.loading_iv)).getBackground();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            public void onShow(DialogInterface log) {
                if (bufferAnim != null) {
                    bufferAnim.start();
                }
            }
        });
    }
    private void showLoading(){
        if(dialog!=null){
            dialog.show();
        }
        if(bufferAnim!=null){
            bufferAnim.start();
        }
    }
    private void hideLoading(){
        if(bufferAnim!=null){
            bufferAnim.stop();
        }
        if(dialog!=null){
            dialog.hide();
        }
    }
    
    
    private void initSearchResult(int count){
        if(count==0){
           return; 
        }
        mSearchList.removeAllViews();
        LayoutInflater inflater= (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        int row = doc.getSearchRowCount();
        Log.d(TAG, "------>row = "+row);
        for(int i=0;i<row ;i++){
            View view = inflater.inflate(R.layout.search_item, null);
            mSearchList.addView(view);
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
                int position = i*4 + col;
                Log.d(TAG, "itemList id="+position);
                childView.setId(position);
                childView.setFocusable(true);
                childView.setFocusableInTouchMode(true);
                if(position<mResult.size()){
                    HashMap<String, Object> childMap = (HashMap<String, Object>) mResult.get(i*4 + col);
                    TextView title = (TextView) childView.findViewById(R.id.movie_title);
                    TextView model = (TextView) childView.findViewById(R.id.movie_comment);
                    title.setText((String)childMap.get("title"));
                    model.setText((String)childMap.get("content_model"));
                    setItemSectionViewListener(childView,position,childMap.get("pk"));
                }else{
                    Log.d(TAG, "position > mResult.size()");
                    childView.setVisibility(View.GONE);
                }
            } 
        }
    }
    
    
    private void setItemSectionViewListener(View view , int position,final Object pk){
        view.setOnFocusChangeListener(new View.OnFocusChangeListener(){
            public void onFocusChange(View v, boolean hasFocus) {
                Log.d(TAG, "OnFocus: v = "+ v + ",hasFocus = "+hasFocus);
                TextView text = (TextView)v.findViewById(R.id.movie_title);
                View bg = v.findViewById(R.id.movie_bg);
                if(hasFocus){
                    text.setTextColor(getResources().getColor(R.color.vod_movieitem_sel));
                    text.setSelected(true);
                    bg.setBackgroundResource(R.drawable.voditem_focus);
                    focus_item_view = v;
                }else{
                    text.setSelected(false);
                    text.setTextColor(getResources().getColor(R.color.vod_movieitem_nosel));
                    bg.setBackgroundDrawable(null);
                }
            }
        });
        view.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                
                if ((keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) //
                        && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if( v instanceof MovieItemView){
                        goToDetail(pk);
                    }
                    return true;
                }
                return false;
            }
        });
        
    }
    

    private void updateItemPoster(int id, Bitmap bmp){
        View view = mSearchList.findViewById(id);
        if(view!=null){
            ImageView img = (ImageView) view.findViewById(R.id.movie_poster);            
            img.setImageBitmap(bmp);
        }
    }
    
    
    /* 搜索关键字补全 */
    @SuppressWarnings("unused")
    private void suggestSearch(String keyWord){
        mResult.clear();
        doc.getSuggestResult(keyWord);
    }
    /* 指定类型搜索 */
    @SuppressWarnings("unused")
    private void modelSearch(String model,String keyWord){
        mResult.clear();
    }
    /* 在所有片源中搜索 */
    private void allSearch(String keyWord){
        mResult.clear();
        showLoading();
        Log.d(TAG, "---->> keyWord = "+keyWord);
        //-->   /api/{tv|mobile}/search/${content_model}/{query}/{page}/
        doc.getSearchResult(null, keyWord, 1, 12);
    }
    
    
    protected void onDocGotResource(ISTVResource res) {
        super.onDocGotResource(res);

        Log.d(TAG, "--------onDocGotResource---->> " + res.getType());
//        Log.d(TAG, " String =>>> "+res.getString());
//        Log.d(TAG, " URL    =>>> "+res.getURL());
//        if(res.getSearchMap()!=null){
//            Log.d(TAG, " Map.size=>>> "+res.getSearchMap().size());
//        }
//        Log.d(TAG, " Int =>>> "+res.getInt());
//        Log.d(TAG, " ID =>>> "+res.getID());
//        Log.d(TAG, " Float =>>> "+res.getFloat());
//        Log.d(TAG, " Double =>>> "+res.getDouble());

        
        
        
        switch (res.getType()) {
            case ISTVVodSearchDoc.RES_INT_SEARCH_RESULT:
                mResult.add(res.getSearchMap());
                break;
            case ISTVVodSearchDoc.RES_INT_SEARCH_END:
                hideLoading();
                initSearchResult(mResult.size());
                break;
            case ISTVVodSearchDoc.RES_STR_HOTWORDS_RESULT:
                if(!"".equals(res.getString())){
                    mHotWord.add(res.getString());
                }else{
                    Log.d(TAG, "-------has no hotWord--------");
                    hideLoading();
                }
                break;
            case ISTVVodSearchDoc.RES_INT_HOTWORDS_END:
                hideLoading();
                initHotWordMenu(mHotWord.size());
                break;
            case ISTVVodSearchDoc.RES_STR_SUGGESTS_RESULT:
                if(!"".equals(res.getString())){
                    mSuggest.add(res.getString());
                }
                break;
            case ISTVVodSearchDoc.RES_INT_SUGGESTS_END:
                mSuggestMap.put("", mSuggest);
                mSuggest.clear();
                break;
            case ISTVVodSearchDoc.RES_BMP_ITEM_POSTER:
                Bitmap bmp = res.getBitmap();
                updateItemPoster(res.getID(),bmp);
                break;
            default:
                break;
        }
    }

    
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case VOD_GET_SEARCH_RESULT:
                    handler.removeMessages(VOD_GET_SEARCH_RESULT);
                    String keyWord = mSearchText.getText().toString();
                    if("".equals(keyWord.trim())){
                        Toast.makeText(getApplicationContext(), //
                                ISTVVodSearch.this.getString(R.string.null_word), Toast.LENGTH_SHORT).show();
                        break;
                    }
                    showLoading();
                    allSearch(keyWord);
                    break;
                    
                case VOD_REFLSE_SUGGEST_LIST:
                    ArrayList a = (ArrayList) msg.obj;
                    break;
                default:
                    break;
            }
        }

    };

    
    private void goToDetail(Object strPK){
        int pk = Integer.parseInt((String)strPK);
        if(pk==-1){
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putInt("itemPK", pk);
        
        Intent intent = new Intent();
        intent.setClass(this, ISTVVodItemDetail.class);
        intent.putExtras(bundle);
        startActivityForResult(intent, 1);
    }
    
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search:
                resultLayout.setVisibility(View.VISIBLE);
                handler.sendEmptyMessage(VOD_GET_SEARCH_RESULT);
                break;
            default:
                break;
        }
    }
}
