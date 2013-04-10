package com.ismartv.service;

import java.util.HashMap;
import java.io.Serializable;

public class ISTVContentModel implements Serializable{
	public class Attribute implements Serializable{
		public String name;		
		public HashMap<String, String> title=new HashMap<String, String>();
	}

	public String    name;
	public String    personAttr;
	public HashMap<String, String> title= new HashMap<String, String>();
	public HashMap<String, Attribute> attrs=new HashMap<String, Attribute>();

	public Attribute addAttr(String name){
		Attribute attr = new Attribute();
		attr.name = name;

		attrs.put(name, attr);

		return attr;
	}

	public String getTitle(String lang){
		return title.get(lang);
	}

	public String getAttrTitle(String lang, String name){
		Attribute attr = attrs.get(name);

		if(attr==null)
			return null;

		String title = attr.title.get(name);

		return title;
	}
}

