package com.lenovo.settings.Util;

import android.graphics.Bitmap;

public class ThemeImage {
	Bitmap bitmap;
	String title;
	int    ischeck;
	
	public ThemeImage() {
	}
	
	public ThemeImage(Bitmap bitmap,String title,int ischeck) {
		this.bitmap = bitmap;
		this.title = title;
		this.ischeck = ischeck;
	}
	
	public Bitmap getBitmap() {
		return bitmap;
	}
	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public int getIscheck() {
		return ischeck;
	}
	public void setIscheck(int ischeck) {
		this.ischeck = ischeck;
	}
	
	
}
