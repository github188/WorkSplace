package com.ismartv.util;

public class StringUtils {
	public static boolean isBlank(String s){
		return s == null || s.trim().length() == 0 ;
	}
}
