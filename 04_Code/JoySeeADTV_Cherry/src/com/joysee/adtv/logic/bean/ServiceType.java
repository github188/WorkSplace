
package com.joysee.adtv.logic.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 频道类型封装类
 * @author songwenxuan
 *
 */
public class ServiceType implements Parcelable {
    private int typeID;
    private String typeName;

    public ServiceType() {}

    public int getTypeID() {
        return typeID;
    }

    public void setTypeID(int typeID) {
        this.typeID = typeID;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.typeID);
        dest.writeString(this.typeName);
    }

    public ServiceType(Parcel in) {
        this.typeID = in.readInt();
        this.typeName = in.readString();
    }

    public static final Parcelable.Creator<ServiceType> CREATOR = 
            new Parcelable.Creator<ServiceType>() {
        public ServiceType createFromParcel(Parcel in) {
            return new ServiceType(in);
        }

        public ServiceType[] newArray(int size) {
            return new ServiceType[size];
        }
    };
}
