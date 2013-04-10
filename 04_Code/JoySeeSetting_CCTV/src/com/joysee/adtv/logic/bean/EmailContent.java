package com.joysee.adtv.logic.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 2012-11-02
 *封装邮件内容
 *@author wuhao 
 */
public class EmailContent implements Parcelable {
    /**
     * 邮件内容
     */
    private String mEmailContent;
    /**
     * 预留
     */
    private int mReserved;

    public EmailContent() {
    }

    public EmailContent(Parcel in) {
        this.mEmailContent = in.readString();
        this.mReserved = in.readInt();
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // TODO Auto-generated method stub
        dest.writeString(this.mEmailContent);
        dest.writeInt(this.mReserved);
    }

    public static final Parcelable.Creator<EmailContent> CREATOR = new Parcelable.Creator<EmailContent>() {

        public EmailContent createFromParcel(Parcel in) {
            return new EmailContent(in);
        }

        public EmailContent[] newArray(int size) {
            return new EmailContent[size];
        }
    };
    
    public static Parcelable.Creator<EmailContent> getCreator() {
        return CREATOR;
    }

    public String getEmailContent() {
        return mEmailContent+"";
    }

    public void setEmailContent(String mEmailContent) {
        this.mEmailContent = mEmailContent;
    }

    public int getReserved() {
        return mReserved;
    }

    public void setReserved(int mReserved) {
        this.mReserved = mReserved;
    }

	@Override
	public String toString() {
		return "EmailContent [mEmailContent=" + mEmailContent + ", mReserved="
				+ mReserved + "]";
	}
}
