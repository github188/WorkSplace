package com.bestv.ott.appstore.common;

import android.graphics.Bitmap;

public class ApplicationBean {
    public int  id;
    public String downDir;
    public String fileName;
    public String appName;
    public String url;
    public int status;
    public String pkgName;
    public byte[] icon;
    public String version;
    public String iconUrl;
    private String serAppID ;
    private String typeID;
    private String typeName;
    private int appSource;
    
    
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getDownDir() {
		return downDir;
	}
	public void setDownDir(String downDir) {
		this.downDir = downDir;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
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
	public String getPkgName() {
		return pkgName;
	}
	public void setPkgName(String pkgName) {
		this.pkgName = pkgName;
	}
	public byte[] getIcon() {
		return icon;
	}
	public void setIcon(byte[] icon) {
		this.icon = icon;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
    public String getIconUrl() {
        return iconUrl;
    }
    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
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
	public int getAppSource() {
		return appSource;
	}
	public void setAppSource(int appSource) {
		this.appSource = appSource;
	}
    
    
    
}
