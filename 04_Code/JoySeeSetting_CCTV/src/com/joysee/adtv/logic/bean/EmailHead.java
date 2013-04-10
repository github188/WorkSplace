package com.joysee.adtv.logic.bean;

import android.os.Parcel;
import android.os.Parcelable;
/**
 * 2012-11-02
 * 邮件头信息，包括邮件ID、已读标志、发送时间、邮件等级、邮件标题。
 * @author wuhao 
 */
public class EmailHead implements Parcelable {
    /**
     * 邮件ID
     */
    private int mEmailID;
    /**
     * 标志是否是新邮件
     */
    private boolean mNewEmail;
    /**
     * 邮件发送时间
     */
    private int mEmailSendTime;
    /**
     * 邮件级别
     * 0x0 普通等级
     * 0x1 重要等级
     */
    private int mEmailLevel;
    /**
     * 邮件标题
     */
    private String mEmailTitle;

    public EmailHead() {

    }

    public EmailHead(Parcel in) {
        this.mEmailID = in.readInt();
        this.mNewEmail = in.readInt() == 0;
        this.mEmailSendTime = in.readInt();
        this.mEmailLevel = in.readInt();
        this.mEmailTitle = in.readString();
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // TODO Auto-generated method stub
        dest.writeInt(this.mEmailID);
        dest.writeInt(this.mNewEmail ? 0 : 1);
        dest.writeInt(this.mEmailSendTime);
        dest.writeInt(this.mEmailLevel);
        dest.writeString(this.mEmailTitle);
    }

    public static final Parcelable.Creator<EmailHead> CREATOR = new Parcelable.Creator<EmailHead>() {

        public EmailHead createFromParcel(Parcel in) {
            return new EmailHead(in);
        }

        public EmailHead[] newArray(int size) {
            return new EmailHead[size];
        }
    };

    public int getEmailID() {
        return mEmailID;
    }

    public void setEmailID(int mEmailID) {
        this.mEmailID = mEmailID;
    }


    public boolean isNewEmail() {
        return mNewEmail;
    }

    public void setNewEmail(boolean mNewEmail) {
        this.mNewEmail = mNewEmail;
    }

    public long getEmailSendTime() {
        //amlogic 是 0时区,需要加上八个小时.
		return mEmailSendTime * 1000 + 8 * 3600 * 1000;
    }

    public void setEmailSendTime(int mEmailSendTime) {
        this.mEmailSendTime = mEmailSendTime;
    }

    public int getEmailLevel() {
        return mEmailLevel;
    }

    public void setEmailLevel(int mEmailLevel) {
        this.mEmailLevel = mEmailLevel;
    }

    public String getEmailTitle() {
        return mEmailTitle+"";
    }

    public void setEmailTitle(String mEmailTitle) {
        this.mEmailTitle = mEmailTitle;
    }

	@Override
	public String toString() {
		return "EmailHead [mEmailID=" + mEmailID + ", mNewEmail=" + mNewEmail
				+ ", mEmailSendTime=" + mEmailSendTime + ", mEmailLevel="
				+ mEmailLevel + ", mEmailTitle=" + mEmailTitle + "]";
	}
}
