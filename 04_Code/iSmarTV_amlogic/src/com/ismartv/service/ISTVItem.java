package com.ismartv.service;

import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.text.SimpleDateFormat;
import org.json.JSONObject;
import org.json.JSONArray;
import android.util.Log;
import java.lang.StringBuilder;
import java.io.Serializable;
import java.io.ObjectOutputStream;

public class ISTVItem implements Serializable{
	private static final String TAG="ISTVItem";

	public class Value implements Serializable{
		public String          name;
		public int             id;
	};
	public class Attribute implements Serializable{
		public ISTVContentModel.Attribute attr;
		public Value                  values[];
	};
	public class Attributes implements Serializable{
		public ISTVContentModel.Attribute airDateAttr;
		public Date            airDate;
		public ISTVContentModel.Attribute areaAttr;
		public String		   area;
		public ISTVContentModel.Attribute genreAttr;
		public String		   genres[];
		public Attribute       attrs[];
	};
	public URL                     adletURL;
	public String                  contentModel;
	public Attributes              attrs;
	public int                     episode=1;
	public String                  focus;
	public boolean                 isComplex;
	public int                     itemPK;
	public URL                     itemURL;
	public boolean                 isSubItem;
	public int                     subID=0;
	public int                     pk;
	public URL                     posterURL;
	public Date                    publishDate;
	public Date                    updateDate;
	public int                     subItems[];

	public static final int QUALITY_ADAPTIVE=0;
	public static final int QUALITY_LOW=1;
	public static final int QUALITY_MEDIUM=2;
	public static final int QUALITY_NORMAL=3;
	public static final int QUALITY_HIGH=4;
	public static final int QUALITY_ULTRA=5;
	public static final int QUALITY_COUNT=6;

	public boolean                 bookmarked=false;
	public int                     quality;
	public String                  tags[];
	public URL                     thumbURL;
	public String                  title;
	public URL                     url;
	public ISTVExpense             expense;
	public double                  ratingAverage=0.;
	public int                     ratingCount=0;
	public int                     countingCount=0;
	public String                  clipLength;
	public int                     clipPK=-1;
	public URL                     clipURL;
	public int                     spinoffPK;
	public String                  description;
	public int					   price = -1;
	public int                     offset=0;
	boolean                        gotDetail=false;

	synchronized void setGotDetail(){
		gotDetail=true;
	}

	public synchronized boolean hasGotDetail(){
		return gotDetail;
	}

	public ISTVItem(){
	}

	public ISTVItem clone(){
		ISTVItem item = new ISTVItem();

		item.adletURL = this.adletURL;
		item.contentModel = this.contentModel;
		item.attrs    = this.attrs;
		item.episode  = this.episode;
		item.focus    = this.focus;
		item.isComplex = this.isComplex;
		item.itemPK   = this.itemPK;
		item.itemURL  = this.itemURL;
		item.isSubItem = this.isSubItem;
		item.pk       = this.pk;
		item.posterURL = this.posterURL;
		item.publishDate = this.publishDate;
		item.updateDate  = this.updateDate;
		item.subItems    = this.subItems;
		item.bookmarked  = this.bookmarked;
		item.quality  = this.quality;
		item.tags     = this.tags;
		item.thumbURL = this.thumbURL;
		item.title    = this.title;
		item.url      = this.url;
		item.expense  = this.expense;
		item.ratingAverage = this.ratingAverage;
		item.ratingCount   = this.ratingCount;
		item.countingCount = this.countingCount;
		item.clipLength    = this.clipLength;
		item.clipPK   = this.clipPK;
		item.clipURL  = this.clipURL;
		item.spinoffPK = this.spinoffPK;
		item.description = this.description;
		item.offset   = this.offset;
		item.gotDetail= this.gotDetail;
		item.price = this.price;
		return item;
	}

	public void mergeInfo(ISTVItem old){
		if(this.updateDate==null){
			this.updateDate = old.updateDate;
		}else if(old.updateDate!=null){
			if(this.updateDate.before(old.updateDate)){
				this.updateDate = old.updateDate;
			}
		}

		if(this.offset>=0){
			if(this.offset<old.offset || old.offset==-1){
				this.offset = old.offset;
			}
		}

		if(this.attrs==null && old.attrs!=null){
			this.contentModel = old.contentModel;
			this.attrs = old.attrs;
		}
		
		if(this.subItems==null && old.subItems!=null){
			this.subItems = old.subItems;
		}
		
		if(this.adletURL==null && old.adletURL!=null){
			this.adletURL = old.adletURL;
		}
		
		if(this.thumbURL==null && old.thumbURL!=null){
			this.thumbURL = old.thumbURL;
		}


		if(this.description==null && old.description!=null){
			this.description=old.description;
		}

		if(this.focus==null && old.focus!=null){
			this.focus=old.focus;
		}

		if(this.bookmarked || old.bookmarked){
			this.bookmarked = true;
		}

		if(this.expense==null && old.expense!=null){
			this.expense = old.expense;
		}

		if(this.clipPK==-1 && old.clipPK!=-1){
			this.clipPK = old.clipPK;
			this.clipLength = old.clipLength;
			this.clipURL = old.clipURL;
		}
		
		if(this.ratingCount < old.ratingCount){
			this.ratingCount = old.ratingCount;
			this.ratingAverage = old.ratingAverage;
		}

		if(this.countingCount < old.countingCount){
			this.countingCount = old.countingCount;
		}
		
		if(this.price == -1 && old.price != -1) {
			this.price = old.price;
		}		
	}

	private URL parseURL(JSONObject obj, String name){
		String str = obj.optString(name);
		URL u = null;

		if(str!=null && !str.equals("") && !str.equals("null")){
			try{
				u = new URL(str);
			}catch(Exception e){
			}
		}

		return u;
	}

	private Date parseDate(JSONObject obj, String name, boolean isFull){
		String str = obj.optString(name);		
		Date d = null;

		if(str!=null && !str.equals("") && !str.equals("null")){
			try{
				if(isFull) {
					d = ISTVDate.parse(str);
				}
				else {	
					d = ISTVDate.parseDate(str);
				}
			}catch(Exception e){
			}
		}

		return d;
	}

	private String parseStr(JSONObject obj, String name){
		String str = obj.optString(name);

		if(str==null || str.equals("") || str.equals("null")){
		    Log.d(TAG, " pares failed ----------name="+name);
		    return null;
		}

		return str;
	}

	public ISTVItem(ISTVEpg epg, JSONObject obj) throws Exception{
		String str;

		publishDate = parseDate(obj, "publish_date", true);
		posterURL   = parseURL(obj, "poster_url");
		itemURL     = parseURL(obj, "item_url");
		episode     = obj.optInt("episode", 1);
		tags        = null;
		url         = parseURL(obj, "url");
		quality     = obj.optInt("quality");
		title       = parseStr(obj, "title");
		focus       = parseStr(obj, "focus");
		contentModel = parseStr(obj, "content_model");
		adletURL    = parseURL(obj, "adlet_url");
		isComplex   = obj.optBoolean("is_complex", true);
		pk          = obj.getInt("pk");
		thumbURL    = parseURL(obj, "thumb_url");
		itemPK      = obj.optInt("item_pk");
		price		= obj.optInt("price");
		updateDate  = parseDate(obj, "update_date", true);		
		if(thumbURL==null){
			thumbURL = (posterURL==null)?adletURL:posterURL;
		}else if(posterURL==null){
			posterURL = (thumbURL==null)?adletURL:thumbURL;
		}else if(adletURL==null){
			adletURL = (posterURL==null)?posterURL:thumbURL;
		}

		str = parseStr(obj, "model_name");
		if(str!=null){
			isSubItem = str.equals("subitem");
		}

		JSONArray subArray = obj.optJSONArray("subitems");
		if(subArray!=null){
			int cnt = subArray.length();
			if(cnt>0){
				subItems = new int[cnt];

				for(int i=0; i<cnt; i++){
					JSONObject sub = subArray.getJSONObject(i);
					subItems[i] = sub.getInt("pk");

					ISTVItem si = new ISTVItem(epg, sub);
					si.itemPK=this.itemPK;
					Log.d(TAG, "-----new ISTVItem() itempk="+this.itemPK);
					si.contentModel=this.contentModel;
					epg.addItem(si);
				}
			}
		}

		description = parseStr(obj, "description");

		JSONObject clipObj = obj.optJSONObject("clip");
		if(clipObj!=null){
			clipURL    = parseURL(clipObj, "url");
			clipLength = parseStr(clipObj, "length");
			clipPK     = clipObj.getInt("pk");
		}

		ratingAverage = obj.optDouble("rating_average", 0.);
		ratingCount = obj.optInt("rating_count");
		countingCount = obj.optInt("counting_count");

		try{
			JSONObject attrsObj;			
			ISTVContentModel cm = epg.getContentModel(contentModel);
			attrsObj = obj.optJSONObject("attributes");
			
			if(attrsObj!=null){
				attrs = new Attributes();
				int acnt = cm.attrs.size()-1;
				int id=0;
				
				attrs.attrs = new Attribute[acnt];				
				for(ISTVContentModel.Attribute ca : cm.attrs.values()){					
					if(ca.name.equals("air_date")){						
						attrs.airDateAttr = ca;
						attrs.airDate = parseDate(attrsObj, ca.name, false);
					} else if(ca.name.equals("area")){
						attrs.areaAttr = ca;
						JSONArray areaArray = attrsObj.optJSONArray(ca.name);
						attrs.area = areaArray.optString(1);						
					} else if(ca.name.equals("genre")){						
						attrs.genreAttr = ca;
						JSONArray genreArray = attrsObj.optJSONArray(ca.name);
						if(genreArray != null) {
							attrs.genres = new String[genreArray.length()];
							for(int i = 0; i < genreArray.length(); i++) {							
								attrs.genres[i] = genreArray.optJSONArray(i).optString(1);
							}
						} else {
							attrs.genres = new String[1];
							attrs.genres[0] = "";
						}						
					}else{
						attrs.attrs[id] = new Attribute();
						attrs.attrs[id].attr = ca;						
						JSONArray attrArray = attrsObj.optJSONArray(ca.name);			
						if(attrArray!=null){
							int vcnt = attrArray.length();
							attrs.attrs[id].values = new Value[vcnt];
							for(int v=0; v<vcnt; v++){
								Value val = new Value();
								attrs.attrs[id].values[v] = val;

								JSONArray varray = attrArray.getJSONArray(v);
								val.id = varray.getInt(0);
								val.name = varray.optString(1);								
							}
						}
						id++;						
					}
				}
			}
		}catch(Exception e){
			Log.d(TAG, "parse item failed! e.getMessage="+e.getMessage()+";e="+e);
		}
	}

	public void dump(){
		Log.d(TAG, "pk:"+pk);
		Log.d(TAG, "title:"+title);
		if(publishDate!=null)
			Log.d(TAG, "publish_date: "+publishDate.toString());
		if(posterURL!=null)
			Log.d(TAG, "poster_url: "+posterURL.toString());
		if(itemURL!=null)
			Log.d(TAG, "item_url: "+itemURL.toString());
		if(description!=null)
			Log.d(TAG, "description: "+description);
		Log.d(TAG, "clip: "+clipPK);
		if(clipLength!=null)
			Log.d(TAG, "length: "+clipLength);
	}
}

