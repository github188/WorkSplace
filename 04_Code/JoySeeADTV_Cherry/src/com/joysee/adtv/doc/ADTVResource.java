package com.joysee.adtv.doc;

import java.net.URL;
import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.HashMap;

public class ADTVResource{
	int    type;
	int    id;
	int    intValue;
	float  floatValue;
	double  doubleValue;
	boolean booleanValue;
	String strValue;
	Bitmap bitmap;
	URL    url;
	HashMap<Object, Object> map;
	ArrayList<Object> list;

	public ADTVResource(int t, int i, boolean v){
		type = t;
		id   = i;
		booleanValue = v;
	}

	public ADTVResource(int t, int i, int v){
		type = t;
		id   = i;
		intValue = v;
	}

	public ADTVResource(int t, int i, float v){
		type = t;
		id   = i;
		floatValue = v;
	}

	public ADTVResource(int t, int i, double v){
		type = t;
		id   = i;
		doubleValue = v;
	}	

	public ADTVResource(int t, int i, String v){
		type = t;
		id   = i;
		strValue = v;
	}

	public ADTVResource(int t, int i, Bitmap bmp){
		type = t;
		id   = i;
		bitmap = bmp;
	}

	public ADTVResource(int t, int i, URL u){
		type = t;
		id   = i;
		url  = u;
	}

	public ADTVResource(int t, int i, HashMap<Object, Object> map){
		type = t;
		id   = i;
		this.map = map;
	}	
	
	public ADTVResource(int t, int i,ArrayList list, HashMap map){
        type = t;
        id   = i;
        this.list=list;
        this.map = map;
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
	
	public HashMap<Object, Object> getHashMap() {
		return map;
	}
	
	public ArrayList<Object> getArrayList(){
	    return list;
	}
}
