package com.ismartv.service;

import java.net.URL;
import java.util.List;
import java.util.Arrays;
import java.util.Collection;
import org.json.JSONObject;

public class ISTVSection{
	public static int countPerPage=100;

	public String slug;
	public String title;
	public int    templ;
	public URL    url;
	
	public int    count;
	int    pageCount;
	int    pageMask[];
	int    items[];
	
	boolean gotPageCount=false;

	void addPage(int c, int p, Collection<ISTVItem> il){
		if(c<0 || p<0)
			return;

		if(!gotPageCount){
			pageCount = (c+countPerPage-1)/countPerPage;
			count = c;

			int size = (pageCount+7)/8;
			pageMask  = new int[size];

			items = new int[count];
			Arrays.fill(items, -1);

			gotPageCount = true;
		}

		int off = p * countPerPage;

		for(ISTVItem i : il){
			if(off>=count)
				break;
			items[off++] = i.pk;
		}

		if((pageMask!=null) && (p<pageCount))
			pageMask[p>>3] = 1<<(p&7);
	}

	boolean gotPage(int p){
		if((pageMask==null) || (p<0) || (p>=pageCount))
			return false;

		return ((pageMask[p>>3] & (1<<(p&7)))==0)?false:true;
	}

	ISTVSection(){
	}

	ISTVSection(JSONObject obj) throws Exception {
		String str;

		slug  = obj.getString("slug");
		title = obj.getString("title");
		templ = obj.optInt("template");

		str = obj.getString("url");
		if(str!=null && !str.equals("") && !str.equals("null")){
			try{
				url = new URL(str);
			}catch(Exception e){
			}
		}

		count = obj.optInt("count");
	}
}

