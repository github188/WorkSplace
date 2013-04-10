package com.ismartv.service;

import java.net.URL;
import java.util.Collection;
import org.json.JSONObject;
import org.json.JSONArray;

public class ISTVChannel{
	public String  channel;
	public String  name;
	public int     templ;
	public URL     url;
	public URL     bmpNormalURL;
	public URL     bmpFocusedURL;
	boolean gotSectionList=false;
	String  sections[];

	void setSectionList(Collection<ISTVSection> sl){
		sections = new String[sl.size()];
		int i = 0;

		for(ISTVSection s : sl){
			sections[i++] = s.slug;
		}

		gotSectionList = true;
	}

	public ISTVChannel(){
	}

	private URL parseURL(JSONObject obj, String name){
		String str = obj.optString(name);
		URL u=null;

		if(str!=null && !str.equals("") && !str.equals("null")){
			try{
				u = new URL(str);
			}catch(Exception e){
			}
		}

		return u;
	}

	public ISTVChannel(JSONObject obj) throws Exception{
		name    = obj.optString("name");
		url     = parseURL(obj, "url");
		bmpNormalURL  = parseURL(obj, "icon_url");
		bmpFocusedURL = parseURL(obj, "icon_focus_url");
		channel = obj.getString("channel");
	}
}

