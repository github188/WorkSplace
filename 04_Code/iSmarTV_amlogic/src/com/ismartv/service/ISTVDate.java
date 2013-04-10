package com.ismartv.service;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ISTVDate{
	private static SimpleDateFormat sdfFull = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");

	static Date parse(String str) throws Exception{
		return sdfFull.parse(str);
	}

	static Date parseDate(String str) throws Exception{
		return sdfDate.parse(str);
	}

}
