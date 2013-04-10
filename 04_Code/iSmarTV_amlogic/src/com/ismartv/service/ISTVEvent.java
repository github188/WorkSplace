package com.ismartv.service;

import java.net.URL;
import java.util.Collection;
import android.graphics.Bitmap;

public class ISTVEvent{
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
		IMPORT_HISTORY,
		CLEAR_HISTORY,
		REMOVE_HISTORY,
		BOOKMARK_LIST,
		RELATE_LIST,
		SEARCH,
		HOT_WORDS,
		SUGGEST_WORDS,
		RATE,
		ADD_HISTORY,
		FILT_RATE,
		ADD_BOOKMARK,
		REMOVE_BOOKMARK,
		CLEAR_BOOKMARK,
		LOGIN_FAILED,
		LOGIN_SUCCESS,
		ERROR;
	};

	public Type   type;
	public int    error;
	public String errInfo;
	public String chanID;
	public String secID;
	public int    itemPK;
	public int    subItemPK;
	public int    clipPK;
	public String contentModel;
	public String attribute;
	public int    attrID;
	public String word;
	public URL    url;
	public int    count;
	public int    pageCount;
	public int    countPerPage;
	public int    page;
	public Collection<String> words;
	public Collection<ISTVChannel> channels;
	public Collection<ISTVSection> sections;
	public Collection<ISTVItem> items;
	public ISTVItem item;
	public ISTVClip clip;
	public ISTVTopVideo topVideo;

	public ISTVEvent(Type t){
		type = t;
	}
}
