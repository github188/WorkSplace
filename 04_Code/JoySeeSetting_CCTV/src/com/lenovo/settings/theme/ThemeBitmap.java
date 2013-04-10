package com.lenovo.settings.theme;


public class ThemeBitmap {
	String bitmapurl;
	String title;
	String ischeck;
	
	public ThemeBitmap() {
	}
	
	public ThemeBitmap(String bitmapurl1,String title,String ischeck) {
		this.bitmapurl = bitmapurl1;
		this.title = title;
		this.ischeck = ischeck;
	}

	public String getBitmapurl() {
		return bitmapurl;
	}

	public void setBitmapurl(String bitmapurl) {
		this.bitmapurl = bitmapurl;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getIscheck() {
		return ischeck;
	}

	public void setIscheck(String ischeck) {
		this.ischeck = ischeck;
	}
	
	
	
}
