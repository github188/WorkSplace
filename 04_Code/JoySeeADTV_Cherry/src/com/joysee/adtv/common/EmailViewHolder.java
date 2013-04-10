package com.joysee.adtv.common;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;
/**
 * CA 邮件每一个Item的抽象类
 */
public class EmailViewHolder {
    private ImageView mIcon;
    private TextView mTitle;
    private TextView mTime;
    private TextView mType;

    public EmailViewHolder(Context context) {
        // TODO Auto-generated constructor stub
        mIcon = new ImageView(context);
        mTitle = new TextView(context);
        mTime = new TextView(context);
        mType = new TextView(context);
    }

    public ImageView getmIcon() {
        return mIcon;
    }

    public void setmIcon(ImageView mIcon) {
        this.mIcon = mIcon;
    }

    public TextView getmTitle() {
        return mTitle;
    }

    public void setmTitle(TextView mTitle) {
        this.mTitle = mTitle;
    }

    public TextView getmTime() {
        return mTime;
    }

    public void setmTime(TextView mTime) {
        this.mTime = mTime;
    }

    public TextView getmType() {
        return mType;
    }

    public void setmType(TextView mType) {
        this.mType = mType;
    }
}
