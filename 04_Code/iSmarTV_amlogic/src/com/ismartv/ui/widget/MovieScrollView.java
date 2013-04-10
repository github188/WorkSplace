package com.ismartv.ui.widget;

import com.ismartv.ui.listen.OnScrollColChangedListerner;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.FocusFinder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

public class MovieScrollView extends ScrollView {

    private static final String TAG = "MovieScrollView";
    
    private OnScrollColChangedListerner listener = null;
    
    private int mItemWidth;
    
    private int mScrollCol = 0;

    public MovieScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mItemWidth = 408;
    }
    
    public MovieScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mItemWidth = 408;
    }
    
    public MovieScrollView(Context context) {
        super(context);
        mItemWidth = 408;
    }    
    
    public void setItemWidth(int width){
        mItemWidth = width;
    }

    private final Rect mTempRect = new Rect();
    
    @Override
    public boolean arrowScroll(int direction) {
        View currentFocused = findFocus();
        if (currentFocused == this) currentFocused = null;

        View nextFocused = findNextFocus(this, currentFocused, direction);      
        //FocusFinder.getInstance().findNextFocus(this, currentFocused, direction);       

        final int maxJump = getMaxScrollAmount();
        
        Log.d(TAG, "nextFocused="+nextFocused+", maxJump="+maxJump);

        if (nextFocused != null && isWithinDeltaOfScreen(nextFocused, maxJump)) {
            nextFocused.getDrawingRect(mTempRect);
            offsetDescendantRectToMyCoords(nextFocused, mTempRect);
            
            int scrollDelta = computeScrollDeltaToGetChildRectOnScreen(mTempRect);
            doScrollX(scrollDelta);     
            Log.d(TAG, "scrollDelta="+scrollDelta);
            nextFocused.requestFocus(direction);
        } else {
            // no new focus
            int scrollDelta = maxJump;

            if (direction == View.FOCUS_LEFT && getScrollX() < scrollDelta) {
                scrollDelta = getScrollX();
            } else if (direction == View.FOCUS_RIGHT && getChildCount() > 0) {

                int daRight = getChildAt(0).getRight();

                int screenRight = getScrollX() + getWidth();

                if (daRight - screenRight < maxJump) {
                    scrollDelta = daRight - screenRight;
                }
            }
            if (scrollDelta == 0) {
                return false;
            }
            Log.d(TAG, "scrollDelta="+scrollDelta);
            doScrollX(direction == View.FOCUS_RIGHT ? scrollDelta : -scrollDelta);
        }

        if (currentFocused != null && currentFocused.isFocused()
                && isOffScreen(currentFocused)) {
            // previously focused item still has focus and is off screen, give
            // it up (take it back to ourselves)
            // (also, need to temporarily force FOCUS_BEFORE_DESCENDANTS so we are
            // sure to
            // get it)
            final int descendantFocusability = getDescendantFocusability();  // save
            setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
            requestFocus();
            setDescendantFocusability(descendantFocusability);  // restore
        }
        return true;
    }
    
    public void scrollToView(int col) {
        int curScrollX = this.getScrollX();
        int scrollDelta = mItemWidth * col;
        doScrollX(scrollDelta - curScrollX);
       
        View nextFocused = this.findViewById(col*3);
        if(nextFocused!=null)
            nextFocused.requestFocus();
    }
    
    protected int computeScrollDeltaToGetChildRectOnScreen(Rect rect) {
        if (getChildCount() == 0) return 0;

        int width = getWidth();
        int screenLeft = getScrollX();
        int screenRight = screenLeft + width;

        int fadingEdge = getHorizontalFadingEdgeLength();
        Log.d(TAG,"fadingEdge="+fadingEdge);

        // leave room for left fading edge as long as rect isn't at very left
        if (rect.left > 0) {
            screenLeft += fadingEdge+mItemWidth;
        }

        // leave room for right fading edge as long as rect isn't at very right
        if (rect.right < getChildAt(0).getWidth()) {
            screenRight -= fadingEdge+mItemWidth;
        }

        int scrollXDelta = 0;

        if (rect.right > screenRight && rect.left > screenLeft) {
            // need to move right to get it in view: move right just enough so
            // that the entire rectangle is in view (or at least the first
            // screen size chunk).

            if (rect.width() > width) {
                // just enough to get screen size chunk on
                scrollXDelta += (rect.left - screenLeft);
            } else {
                // get entire rect at right of screen
                scrollXDelta += (rect.right - screenRight);
            }

            // make sure we aren't scrolling beyond the end of our content
            int right = getChildAt(0).getRight();
            int distanceToRight = right - screenRight;
            scrollXDelta = Math.min(scrollXDelta, distanceToRight);

        } else if (rect.left < screenLeft && rect.right < screenRight) {
            // need to move right to get it in view: move right just enough so that
            // entire rectangle is in view (or at least the first screen
            // size chunk of it).

            if (rect.width() > width) {
                // screen size chunk
                scrollXDelta -= (screenRight - rect.right);
            } else {
                // entire rect at left
                scrollXDelta -= (screenLeft - rect.left);
            }

            // make sure we aren't scrolling any further than the left our content
            scrollXDelta = Math.max(scrollXDelta, -getScrollX());
        }
        return scrollXDelta;
    }
    
    private void doScrollX(int delta) {
        if (delta != 0) {
            if (isSmoothScrollingEnabled()) {
                smoothScrollBy(delta, 0);
            } else {
                scrollBy(delta, 0);
            }
            int oldScrollCol = mScrollCol;
            Log.d(TAG, "delta="+delta/408+", curScroll="+this.getScrollX()/408);
            mScrollCol += delta/mItemWidth;
     //       computeRequestRange();
            if(listener!=null)
                listener.onScrollColChanged(mScrollCol, oldScrollCol);
        }
    }
    
    private boolean isOffScreen(View descendant) {
        return !isWithinDeltaOfScreen(descendant, 0);
    }

    private boolean isWithinDeltaOfScreen(View descendant, int delta) {
        descendant.getDrawingRect(mTempRect);
        offsetDescendantRectToMyCoords(descendant, mTempRect);
        Log.d(TAG, "rect="+mTempRect);

        return (mTempRect.right + delta) >= getScrollX()
                && (mTempRect.left - delta) <= (getScrollX() + getWidth());
    }
    
    private View findNextFocus(ViewGroup root, View currentFocused, int direction){
        if(currentFocused == null)
            return null;        
              
        if(direction == View.FOCUS_LEFT) {
            View nextFocus = root.findViewById(currentFocused.getId()-3);
            if(nextFocus != null && nextFocus.getVisibility()==View.VISIBLE){
                return nextFocus;
            }
            else {
                return root.findViewById(currentFocused.getId()-6);
            }
        }
        else if (direction == View.FOCUS_RIGHT) {
            View nextFocus = root.findViewById(currentFocused.getId()+3);
            if(nextFocus != null && nextFocus.getVisibility()==View.VISIBLE){
                return nextFocus;
            }
            else {
                return root.findViewById(currentFocused.getId()+6);
            }
            
        }
        
        return null;
    }

    public void setOnScrollColChangedListerner(OnScrollColChangedListerner listener) {
        this.listener = listener;
    }  
}
