package com.ismartv.doc;

import java.net.URL;
import android.graphics.Bitmap;
import java.util.HashMap;

public class ISTVResource{
	int    type;
	int    id;
	int    intValue;
	float  floatValue;
	double  doubleValue;
	boolean booleanValue;
	String strValue;
	Bitmap bitmap;
	URL    url;
	HashMap<String, Object> searchMap;

	public ISTVResource(int t, int i, boolean v){
		type = t;
		id   = i;
		booleanValue = v;
	}

	public ISTVResource(int t, int i, int v){
		type = t;
		id   = i;
		intValue = v;
	}

	public ISTVResource(int t, int i, float v){
		type = t;
		id   = i;
		floatValue = v;
	}

	public ISTVResource(int t, int i, double v){
		type = t;
		id   = i;
		doubleValue = v;
	}	

	public ISTVResource(int t, int i, String v){
		type = t;
		id   = i;
		strValue = v;
	}

	public ISTVResource(int t, int i, Bitmap bmp){
		type = t;
		id   = i;
		bitmap = bmp;
	}

	public ISTVResource(int t, int i, URL u){
		type = t;
		id   = i;
		url  = u;
	}

	public ISTVResource(int t, int i, HashMap<String, Object> map){
		type = t;
		id   = i;
		searchMap = map;
	}	

	public int getType(){
		return type;
	}

	public int getID(){
		return id;
	}

	public boolean getBoolean(){
		return booleanValue;
	}

	public int getInt(){
		return intValue;
	}

	public float getFloat(){
		return floatValue;
	}

	public double getDouble(){
		return doubleValue;
	}

	public String getString(){
		return strValue;
	}

	public Bitmap getBitmap(){
		return bitmap;
	}

	public URL getURL(){
		return url;
	}
	
	public HashMap<String, Object> getSearchMap() {
		return searchMap;
	}
}
