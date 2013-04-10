package com.ismartv.service;

import java.net.URL;
import android.content.Context;

public class ISTVRequest{
	public enum Type{
		BITMAP,
		VOD_HOME_LIST,
		TOP_VIDEO,
		CHANNEL_LIST,
		SECTION_LIST,
		ITEM_LIST,
		ITEM_DETAIL,
		CLIP,
		HISTORY_LIST,
		BOOKMARK_LIST,
		RELATE_LIST,
		SEARCH,
		HOT_WORDS,
		SUGGEST_WORDS,
		ADD_HISTORY,
		IMPORT_HISTORY,
		CLEAR_HISTORY,
		REMOVE_HISTORY,
		ADD_BOOKMARK,
		CLEAR_BOOKMARK,
		REMOVE_BOOKMARK,
		RATE,
		FILT_RATE,
		STORE_DATA,
		LOGIN;
	};
	public Type    type;
	public URL     url;
	public String  chanID;
	public String  secID;
	public String  word;
	public String  contentModel;
	public String  attribute;
	public int     attrID;
	public int     itemPK;
	public int     subItemPK;
	public int     clipPK;
	public int     page;
	public int     value;
	public int     offset;
	public long    geoid;
	public String  user;
	public String  passwd;
	public Context context;

	public ISTVRequest(Type t){
		type = t;
	}
}

