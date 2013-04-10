package com.joysee.appstore.common;

import android.os.Parcel;
import android.os.Parcelable;

import com.joysee.appstore.utils.FileUtils;

/**
 * 从服务器获取的数据保存到此对象中
 * @author Administrator
 *
 */
public class AppsBean implements Parcelable{

    /*目前有些字段是没有用到*/
    private int ID;//服务器端对应的ID
    private String appName;
    private String pkgName;
    private int typeID;
    private String version;
    private String remark;
    private String natImageUrl;  //本地图片路径
    private String serImageUrl; //远程图片路径
    private String apkUrl;     //远程apk路径
    private String createTime;
    private int size;
    private String typeName;
    private double price;
    private String score; 
    private String downloadDir ;
    private int voteNum;//
    private String versionName;
    
    public AppsBean(){
    	
    }
    public AppsBean(Parcel in){
    	setID(in.readInt());
    	setAppName(in.readString());
    	setPkgName(in.readString());
    	setTypeID(in.readInt());
    	setVersion(in.readString());
    	setRemark(in.readString());
    	setNatImageUrl(in.readString());
    	setSerImageUrl(in.readString());
    	setApkUrl(in.readString());
    	setCreateTime(in.readString());
    	setSize(in.readInt());
    	setTypeName(in.readString());
    	setPrice(in.readDouble());
    	setScore(in.readString());
    	setDownloadDir(in.readString());
    	setVoteNum(in.readInt());
    	setVersionName(in.readString());
    }
    
    public String getAppName() {
        return appName;
    }
    public void setAppName(String appName) {
        this.appName = appName;
    }
    public String getPkgName() {
        return pkgName;
    }
    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public String getRemark() {
        return remark;
    }
    public void setRemark(String remark) {
        this.remark = remark;
    }
    public int getID() {
        return ID;
    }
    public void setID(int iD) {
        ID = iD;
    }
    public String getNatImageUrl() {
        return natImageUrl;
    }
    public void setNatImageUrl(String natImageUrl) {
        this.natImageUrl = natImageUrl;
    }
    public String getSerImageUrl() {
        return serImageUrl;
    }
    public void setSerImageUrl(String serImageUrl) {
        this.serImageUrl = serImageUrl;
    }
    public String getApkUrl() {
        return apkUrl;
    }
    public void setApkUrl(String apkUrl) {
        this.apkUrl = apkUrl;
    }
    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }
    public int getTypeID() {
        return typeID;
    }
    public void setTypeID(int typeID) {
        this.typeID = typeID;
    }
    
    public void deleteImage(){
        FileUtils.delete(natImageUrl);
    }
    public String getCreateTime() {
        return createTime;
    }
    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
    public int getSize() {
        return size;
    }
    public void setSize(int size) {
        this.size = size;
    }
    public String getTypeName() {
        return typeName;
    }
    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	public String getScore() {
		return score;
	}
	public void setScore(String score) {
		this.score = score;
	}
	public String getDownloadDir() {
		return downloadDir;
	}
	public void setDownloadDir(String downloadDir) {
		this.downloadDir = downloadDir;
	}
	
	
	public int getVoteNum() {
		return voteNum;
	}
	public void setVoteNum(int voteNum) {
		this.voteNum = voteNum;
	}
	
	public String getVersionName() {
		return versionName;
	}
	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}
	@Override
	public int describeContents() {
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(ID);
		dest.writeString(appName);
		dest.writeString(pkgName);
		dest.writeInt(typeID);
		dest.writeString(version);
		dest.writeString(remark);
		dest.writeString(natImageUrl);
		dest.writeString(serImageUrl);
		dest.writeString(apkUrl);
		dest.writeString(createTime);
		dest.writeInt(size);
		dest.writeString(typeName);
		dest.writeDouble(price);
		dest.writeString(score);
		dest.writeString(downloadDir);
		dest.writeInt(voteNum);
		dest.writeString(versionName);
	}
    
	public static final Parcelable.Creator<AppsBean> CREATOR
    = new Parcelable.Creator<AppsBean>() {
        public AppsBean createFromParcel(Parcel in) {
            return new AppsBean(in);
        }

        public AppsBean[] newArray(int size) {
            return new AppsBean[size];
        }
    };
}
