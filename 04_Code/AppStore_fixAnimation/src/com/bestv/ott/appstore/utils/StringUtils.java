package com.bestv.ott.appstore.utils;

import java.text.DecimalFormat;

public class StringUtils {
	private static final String TAG = "StringUtils";

	public static boolean isBlank(Object obj){
		return obj == null || "".equals(obj) ;
	} 

	public static String formatMoney(double money) {
//		DecimalFormat format = new DecimalFormat("#.00 ");
		DecimalFormat format = new DecimalFormat("0.00");
		return format.format(money);
	}

	public static String formatMoney(String money) {
		if(isNumeric(money)){
			return formatMoney(money);
		}
		
		return "0.00" ;
	}

	public static boolean isNumeric(String str) {
		for (int i = str.length(); --i >= 0;) {
			if (!Character.isDigit(str.charAt(i))) {
				return false;
			}
		}
		return true;
	}
}
