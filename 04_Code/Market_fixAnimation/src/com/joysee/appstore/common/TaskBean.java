package com.joysee.appstore.common;

import java.util.List;
import android.os.Parcel;
import android.os.Parcelable;

public class TaskBean implements Parcelable{
	
	private int id;
	private String downloadDir;
	private String finalFileName;
	private String tmpFileName;
	private int sumSize;
	private int downloadSize;
	private String appName;
	private String url;
	private int status;
	private String iconUrl;
	private String pkgName; //this item not from database
	private String version;//this item not from database
	private byte[] icon;
	private String serAppID ;
    private String typeID;
    private String typeName;
	private List<ThreadBean> threads;
	
	public TaskBean(){
		
	}
	
	@SuppressWarnings("unchecked")
	public TaskBean(Parcel in){
		this.setId(in.readInt());
		this.setDownloadDir(in.readString());
		this.setFinalFileName(in.readString());
		this.setTmpFileName(in.readString());
		this.setSumSize(in.readInt());
		this.setDownloadSize(in.readInt());
		this.setAppName(in.readString());
		this.setUrl(in.readString());
		this.setStatus(in.readInt());
		this.setIconUrl(in.readString());
		this.setPkgName(in.readString());
		this.setVersion(in.readString());
		 int len = in.readInt();
	        if (len == -1) {
	        	icon = null;
	        } else {
	        	icon = new byte[len];
	            in.readByteArray(icon);
	        }
	    this.setSerAppID(in.readString());
	    this.setTypeID(in.readString());
	    this.setTypeName(in.readString());
	    this.setThreads(in.readArrayList(ThreadBean.class.getClassLoader()));
		//mSearchParams = in.readParcelable(SearchParams.class.getClassLoader());
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getDownloadDir() {
		return downloadDir;
	}
	public void setDownloadDir(String downloadDir) {
		this.downloadDir = downloadDir;
	}
	
	public String getFinalFileName() {
		return finalFileName;
	}
	public void setFinalFileName(String finalFileName) {
		this.finalFileName = finalFileName;
	}
	public String getTmpFileName() {
		return tmpFileName;
	}
	public void setTmpFileName(String tmpFileName) {
		this.tmpFileName = tmpFileName;
	}
	public int getSumSize() {
		return sumSize;
	}
	public void setSumSize(int sumSize) {
		this.sumSize = sumSize;
	}
	
	public int getDownloadSize() {
		return downloadSize;
	}
	public void setDownloadSize(int downloadSize) {
		this.downloadSize = downloadSize;
	}
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getIconUrl() {
		return iconUrl;
	}
	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}
	public String getPkgName() {
		return pkgName;
	}
	public void setPkgName(String pkgName) {
		this.pkgName = pkgName;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public byte[] getIcon() {
		return icon;
	}
	public void setIcon(byte[] icon) {
		this.icon = icon;
	}
	
	public List<ThreadBean> getThreads() {
		return threads;
	}
	public void setThreads(List<ThreadBean> threads) {
		this.threads = threads;
	}
	public String getSerAppID() {
		return serAppID;
	}
	public void setSerAppID(String serAppID) {
		this.serAppID = serAppID;
	}
	public String getTypeID() {
		return typeID;
	}
	public void setTypeID(String typeID) {
		this.typeID = typeID;
	}
	public String getTypeName() {
		return typeName;
	}
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}
	@Override
	public boolean equals(Object o) {
		if(o instanceof TaskBean){
			if(this.getId()==((TaskBean)o).getId()){
				return true;
			}else{
				return false;
			}
		}else{
			return false;
		}
		
	}
	@Override
	public int describeContents() {
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
			dest.writeInt(id);
		 	dest.writeString(downloadDir);
	        dest.writeString(finalFileName);
	        dest.writeString(tmpFileName);
	        dest.writeInt(sumSize);
	        dest.writeInt(downloadSize);
	        dest.writeString(appName);
	        dest.writeString(url);
	        dest.writeInt(status);
	        dest.writeString(iconUrl);
	        dest.writeString(pkgName);
	        dest.writeString(version);
	        if (icon == null) {
	            dest.writeInt(-1);
	        } else {
	            dest.writeInt(icon.length);
	            dest.writeByteArray(icon);
	        }
	        dest.writeString(serAppID);
	        dest.writeString(typeID);
	        dest.writeString(typeName);
	        dest.writeList(threads);
	}
	
	 public static final Parcelable.Creator<TaskBean> CREATOR
     = new Parcelable.Creator<TaskBean>() {
         public TaskBean createFromParcel(Parcel in) {
             return new TaskBean(in);
         }

         public TaskBean[] newArray(int size) {
             return new TaskBean[size];
         }
     };
	
	
	
}
