package com.bestv.ott.appstore.parser;

import org.json.JSONException;

import com.bestv.ott.appstore.common.PageBean;

/**
 * 
 * @author benz
 * 
 * @param <T>
 */
public abstract class BaseParser<T> {
	public static final String TAG_ID = "id" ;
	public static final String TAG_SEPRATE = "/"; 
	
	public abstract T parseJSON(String paramString,PageBean pageBean) throws JSONException;

	public abstract String checkResponse(String paramString) throws JSONException ;

}
