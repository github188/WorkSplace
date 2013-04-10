package com.ismartv.ui.widget;

import java.net.URL;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ismartv.doc.ISTVVodItemListDoc;
import com.ismartv.ui.ISTVVodConstant;
import com.ismartv.ui.ISTVVodItemDetail;
import com.ismartv.ui.ISTVVodPlayer;
import com.ismartv.ui.R;

public class MovieListView extends LinearLayout {

    protected static final String TAG = "MovieListView";

    private Context mContext = null;
    private ISTVVodItemListDoc doc = null;
    private Activity mActivity = null;
    private SectionListView mSecList = null;

    private int startIdx = 0;
    private int focusRow;
    private int focusViewId = 0;
    private long mlast_keydown_time = 0;
    private boolean isTitleFinished = false;

    public int getFocusViewId() {
        return focusViewId;
    }

    public int getStartRow() {
        if (focusViewId < 4)
            return focusRow;
        else if (focusViewId > 7)
            return focusRow - 2;
        else
            return focusRow - 1;
    }

    public int getFocusRow() {
        return focusRow;
    }

    public int getTotalRowCount() {
        return doc.getTotalRowCount();
    }

    public MovieListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public void setDoc(ISTVVodItemListDoc doc) {
        this.doc = doc;
    }

    public void setActivity(Activity activity) {
        this.mActivity = activity;
    }

    public void setSectionList(SectionListView secList) {
        this.mSecList = secList;
    }

    public void initItemList(int rowCount) {
        LayoutInflater inflater = (LayoutInflater) mContext
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        for (int rowId = 0; rowId < rowCount; rowId++) {
            View view = createItemRowView(inflater, rowId);
            this.addView(view);
            addRowViewListener(rowId);
        }

    }

    private View createItemRowView(LayoutInflater inflater, int rowId) {
        View view = inflater.inflate(R.layout.movie_list_item, null);

        for (int col = 0; col < ISTVVodConstant.ITEM_PER_ROW; col++) {
            MovieItemView childView = null;
            if (col == 0)
                childView = (MovieItemView) view.findViewById(R.id.movie_view1);
            else if (col == 1)
                childView = (MovieItemView) view.findViewById(R.id.movie_view2);
            else if (col == 2)
                childView = (MovieItemView) view.findViewById(R.id.movie_view3);
            else if (col == 3)
                childView = (MovieItemView) view.findViewById(R.id.movie_view4);

            childView.setId(rowId * ISTVVodConstant.ITEM_PER_ROW + col);
            childView.setFocusable(true);
            childView.setFocusableInTouchMode(true);
            childView.setLoading(true);

        }

        return view;
    }

    private void addRowViewListener(int rowId) {
        for (int col = 0; col < ISTVVodConstant.ITEM_PER_ROW; col++) {
            View view = this.findViewById(rowId * ISTVVodConstant.ITEM_PER_ROW
                + col);
            if (view == null)
                continue;

            view.setOnFocusChangeListener(new OnFocusChangeListener() {

                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    TextView text = (TextView) v.findViewById(R.id.movie_title);
                    View bg = v.findViewById(R.id.movie_bg);
                    Log.d(TAG, "--------------onFocusChange----hasFocus="+hasFocus+";v.getid="+v.getId());
                    if (hasFocus) {
                        focusViewId = v.getId();
                        text.setTextColor(getResources().getColor(
                            R.color.vod_movieitem_sel));
                        text.setEllipsize(TruncateAt.MARQUEE);
                        bg.setBackgroundResource(R.drawable.voditem_focus);
                        if (v.getId() % ISTVVodConstant.ITEM_PER_ROW == 0) {
                            if (mSecList.getCurSecId() != -1) {
                                mSecList.syncMoveListView();
                            }
                        }
                    } else {
                        text.setTextColor(getResources().getColor(
                            R.color.vod_movieitem_nosel));
                        text.setEllipsize(TruncateAt.END);
                        bg.setBackgroundDrawable(null);
                    }
                }
            });

            view.setOnKeyListener(new OnKeyListener() {

                boolean needLoadNewData = false;

                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER
                        || keyCode == KeyEvent.KEYCODE_ENTER) {
                        Log
                            .d(TAG, "onKey isTitleFinished = "
                                + isTitleFinished);
                        if (isTitleFinished == false)
                            return true;

                        if (event.getAction() == KeyEvent.ACTION_DOWN) {
                            if (v instanceof MovieItemView) {
                                MovieItemView itemView = (MovieItemView) v;
                                Log
                                    .d(TAG, "select movie = "
                                        + itemView.getPk());
                                if (itemView.getComplex()) {
                                    gotoItemDetail(itemView.getPk());
                                } else {
                                    gotoPlayer(itemView.getPk(), -1);
                                }
                            }
                        }
                        return true;
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        if (event.getAction() == KeyEvent.ACTION_DOWN) {
                            if (v.getId() % ISTVVodConstant.ITEM_PER_ROW == 0) {
                                if (mSecList.getCurSecId() != -1) {
                                    View selSec = mSecList.getChildAt(mSecList
                                        .getCurSecId());
                                    if (selSec != null) {
                                        focusViewId = v.getId();
                                        selSec.requestFocus();
                                        return true;
                                    }
                                }
                            }
                        }

                    }/*
                      * else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                      * if(event.getAction()==KeyEvent.ACTION_DOWN){ break; } }
                      */else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        if (event.getAction() == KeyEvent.ACTION_DOWN) {
                            // ###########Add by dgg 2012/03/29
                            Date date = new Date();
                            long current_time = date.getTime();
                            if ((current_time - mlast_keydown_time) < 250) {
                                return true;
                            }
                            if (isTitleFinished == false)
                                return true;

                            mlast_keydown_time = current_time;
                            
                            MovieItemView view = (MovieItemView) MovieListView.this
                            .findViewById(v.getId()
                                - ISTVVodConstant.ITEM_PER_ROW);

                            if ((view != null && view.getPk()!=null && view.getPk() < 0)) {
                                int nextId = v.getId()
                                    - ISTVVodConstant.ITEM_PER_ROW 
                                    - v.getId() % ISTVVodConstant.ITEM_PER_ROW;
                                    Log.d(TAG, "Id nextId = " + nextId);
                                if (nextId >= 0) {                                   
                                    mSecList.updateSectionPos(--focusRow);
                                    MovieItemView nextview = (MovieItemView) MovieListView.this
                                        .findViewById(nextId);
                                    nextview.requestFocus();
                                    return true;
                                } else {
                                    MovieItemView view0 = (MovieItemView) MovieListView.this
                                        .findViewById(0);
                                    view0.setFocusable(true);
                                    view0.requestFocus();
                                }
                            }
                            // ########End

                            boolean focusChanged = false;
                            if (focusRow > 0) {
                                mSecList.updateSectionPos(--focusRow);
                                focusChanged = true;
                            }
                            if (v.getId() < 4) {
                                if (focusChanged) {
                                    startIdx -= 4;
                                    Log.d(TAG, "focusRow=" + focusRow
                                        + ", startIdx = " + startIdx);
                                    needLoadNewData = true;
                                    setAllItemLoading();

                                    if (needLoadNewData) {
                                        refreshData();
                                        needLoadNewData = false;
                                        return true;
                                    }
                                }
                                return true;
                            }
                            needLoadNewData = false;
                        } else if (event.getAction() == KeyEvent.ACTION_UP) {
                            if (needLoadNewData) {
                                // refreshData();
                                // needLoadNewData = false;
                                return true;
                            }                           
                        }
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        if (event.getAction() == KeyEvent.ACTION_DOWN) {
                            Date date = new Date();
                            long current_time = date.getTime();
                            if ((current_time - mlast_keydown_time) < 250) {
                                return true;
                            }
                            if (isTitleFinished == false)
                                return true;

                            mlast_keydown_time = current_time;

                            MovieItemView view = (MovieItemView) MovieListView.this
                                .findViewById(v.getId()
                                    + ISTVVodConstant.ITEM_PER_ROW);

                            if ((view != null && view.getPk()!=null && view.getPk() < 0)) {
                                int nextId = ISTVVodConstant.ITEM_PER_ROW
                                    - (v.getId() % ISTVVodConstant.ITEM_PER_ROW)
                                    + v.getId();
                                if (nextId < ISTVVodConstant.ITEM_DISPLAY) {
                                    Log.d(TAG, "Id nextId = " + nextId);
                                    mSecList.updateSectionPos(++focusRow);
                                    MovieItemView nextview = (MovieItemView) MovieListView.this
                                        .findViewById(nextId);
                                    nextview.requestFocus();
                                    return true;
                                } else {
                                    MovieItemView view8 = (MovieItemView) MovieListView.this
                                        .findViewById(ISTVVodConstant.ITEM_DISPLAY
                                            - ISTVVodConstant.ITEM_PER_ROW);
                                    view8.setFocusable(true);
                                    view8.requestFocus();
                                }
                            }

                            // #########End

                            boolean focusChanged = false;
                            if (focusRow < doc.getTotalRowCount() - 1) {
                                mSecList.updateSectionPos(++focusRow);
                                focusChanged = true;
                            }
                            if (v.getId() > 7) {
                                if (focusChanged) {
                                    startIdx += 4;
                                    Log.d(TAG, "focusRow=" + focusRow
                                        + ", startIdx = " + startIdx);
                                    needLoadNewData = true;
                                    setAllItemLoading();
                                    if (needLoadNewData) {
                                        refreshData();
                                        needLoadNewData = false;
                                        return true;
                                    }
                                }
                                return true;
                            }
                            needLoadNewData = false;
                        } else if (event.getAction() == KeyEvent.ACTION_UP) {
                            if (needLoadNewData) {
                                // refreshData();
                                // needLoadNewData = false;
                                return true;
                            }
                        }
                    }
                    return false;
                }

            });
        }
    }

    public void refreshData() {
        if (doc == null)
            return;
        isTitleFinished = false;
        doc.getItemData(startIdx, ISTVVodConstant.ITEM_DISPLAY);
    }

    public void onInsertData(int count) {
        startIdx += count;
        refreshData();
    }

    public void onSectionSelected(int secId, int focusRow) {
        startIdx = 0;
        focusViewId = 0;
        this.focusRow = focusRow;
        setAllItemLoading();
        doc.fetchPageBySec(secId);
    }

    public void updateItemTitle(int id, String title) {
        if(id>7){
            View view = findViewById(0);
            TextView text = (TextView) view.findViewById(R.id.movie_title);
            if(text.getText().toString().equals(mContext.getString(R.string.vod_loading))){
                return;
            }
        }
        View view = findViewById(id);
        TextView text = (TextView) view.findViewById(R.id.movie_title);
        text.setText(title);
        text.setSelected(true);
        Log.d(TAG,">>>>>>>>>>>  updateItemTitle:" + title );
    }

    public void updateItemPosterUrl(int id, URL url) {
        // doc.getPoster(id, url);
    }

    public void updateItemPoster(int id, Bitmap bmp) {
        MovieItemView view = (MovieItemView) findViewById(id);
        ImageView img = (ImageView) view.findViewById(R.id.movie_poster);
        img.setImageBitmap(bmp);
        view.setLoading(false);
        bmp = null;
    }

    public void setItemPk(int id, int pk) {
        MovieItemView view = (MovieItemView) findViewById(id);
        view.setPk(pk);
        if(pk < 0 && view.hasFocus()){
            int nextId = view.getId() - view.getId() % ISTVVodConstant.ITEM_PER_ROW;
            MovieItemView nextview = (MovieItemView) MovieListView.this
            .findViewById(nextId);
            if(nextview!=null)
                nextview.requestFocus();
        }
        view.setVisibility(pk < 0 ? View.INVISIBLE : View.VISIBLE);        
    }

    public void setItemComplex(int id, Boolean isComplex) {
        MovieItemView view = (MovieItemView) findViewById(id);
        view.setComplex(isComplex);
    }

    private void setItemLoading(int id) {
        MovieItemView view = (MovieItemView) findViewById(id);
        if (view != null ) {
            TextView text = (TextView) view.findViewById(R.id.movie_title);
            text.setText(getResources().getString(R.string.vod_loading));

            ImageView img = (ImageView) view.findViewById(R.id.movie_poster);
            img.setImageResource(R.drawable.default_poster);

            view.setLoading(true);
        }
    }

    private void setAllItemLoading() {
        for (int i = 0; i < ISTVVodConstant.ITEM_DISPLAY; i++)
            setItemLoading(i);
    }

    public void showSectionTag(int colId, String secTitle) {
        View view = this.getChildAt(colId);

        View secStart = view.findViewById(R.id.sec_start);

        TextView secTag = (TextView) view.findViewById(R.id.sec_tag);
        secTag.setText(secTitle);

        secStart.setVisibility(secTitle.isEmpty() ? View.INVISIBLE
            : View.VISIBLE);
    }

    private void gotoItemDetail(Integer pk) {
        if (pk == null || pk < 0)
            return;

        Bundle bundle = new Bundle();
        bundle.putInt("itemPK", pk);

        Intent intent = new Intent();
        intent.setClass(mContext, ISTVVodItemDetail.class);
        intent.putExtras(bundle);

        mActivity.startActivityForResult(intent, 1);
    }

    private void gotoPlayer(int pk, int sub_pk) {
        if (pk == -1)
            return;

        Bundle bundle = new Bundle();
        bundle.putInt("itemPK", pk);
        bundle.putInt("subItemPK", sub_pk);

        Intent intent = new Intent();
        intent.setClass(mContext, ISTVVodPlayer.class);
        intent.putExtras(bundle);

        mActivity.startActivityForResult(intent, 1);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Log.d(TAG, "dispatch:" + event.getKeyCode() + ", action:"
            + event.getAction());
        return super.dispatchKeyEvent(event);
    }

    public void setTitileFinished(boolean isFinished) {
        isTitleFinished = isFinished;
        Log.d(TAG, "setTitileFinished isTitleFinished = " + isTitleFinished);
    }
}
