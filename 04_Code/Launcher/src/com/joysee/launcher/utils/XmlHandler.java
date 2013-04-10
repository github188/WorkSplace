package com.joysee.launcher.utils;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.joysee.launcher.common.Weather;

public class XmlHandler extends DefaultHandler {
	
	private static final String TAG = "com.joysee.launcher.utils.XmlHandler";
	
	private boolean inForcast;
	private Weather currentWeather;

	private static final int CITY_TAG = 1;
	private static final int DESC_TAG = 2;
	private static final int LOW_TAG = 3;
	private static final int HIGH_TAG = 4;
	private static final int NAME_TAG = 5;
	private static final int WEATHER_TAG = 6;
	private static final int TEMPERATURE_TAG = 7;
	private static final int SYMBOL_TAG = 8;
	private static int CURRENTTAG = -1;
	private static int SUBTAG = -1;

	public XmlHandler() {
		inForcast = false;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		String tagName = localName.length() != 0 ? localName : qName;
		tagName = tagName.toLowerCase();
		if (tagName.equals("weatherforecast")) {
			inForcast = true;
			currentWeather = new Weather();
		}
		if (inForcast) {
			if (tagName.equals("city")) {
				CURRENTTAG = CITY_TAG;
			} else if (tagName.equals("weather")) {
				CURRENTTAG = WEATHER_TAG;
			} else if (tagName.equals("temperature")) {
				CURRENTTAG = TEMPERATURE_TAG;
			} else if (tagName.equals("name")) {
				SUBTAG = NAME_TAG;
			} else if (tagName.equals("desc")) {
				SUBTAG = DESC_TAG;
			} else if (tagName.equals("low")) {
				SUBTAG = LOW_TAG;
			} else if (tagName.equals("high")) {
				SUBTAG = HIGH_TAG;
			} else if (tagName.equals("symbol")) {
				SUBTAG = SYMBOL_TAG;
			}

		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		String tagName = localName.length() != 0 ? localName : qName;
		tagName = tagName.toLowerCase();

		if (tagName.equals("city")) {
			CURRENTTAG = -1;
		} else if (tagName.equals("weather")) {
			CURRENTTAG = -1;
		} else if (tagName.equals("temperature")) {
			CURRENTTAG = -1;
		} else if (tagName.equals("name")) {
			SUBTAG = -1;
		} else if (tagName.equals("desc")) {
			SUBTAG = -1;
		} else if (tagName.equals("low")) {
			SUBTAG = -1;
		} else if (tagName.equals("high")) {
			SUBTAG = -1;
		} else if (tagName.equals("symbol")) {
			SUBTAG = -1;
		}

		if (tagName.equals("weatherforecast")) {
			inForcast = false;
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		super.characters(ch, start, length);
		switch (SUBTAG) {
		case NAME_TAG:
			if (CURRENTTAG != CITY_TAG)
				return;
			LauncherLog.log_D(TAG, "NAME_TAG"  + new String(ch,start,length));
			currentWeather.setCity(new String(ch,start,length));
			break;
		case DESC_TAG:
			if (CURRENTTAG != WEATHER_TAG)
				return;
			LauncherLog.log_D(TAG, "DESC_TAG" + new String(ch,start,length));
			currentWeather.setDesc(new String(ch,start,length));
			break;
		case LOW_TAG:
			if (CURRENTTAG != TEMPERATURE_TAG)
				return;
			LauncherLog.log_D(TAG, "LOW_TAG" + new String(ch,start,length));
			currentWeather.setLowTemp(new String(ch,start,length));
			break;
		case HIGH_TAG:
			if (CURRENTTAG != TEMPERATURE_TAG)
				return;
			LauncherLog.log_D(TAG, "HIGH_TAG" + new String(ch,start,length));
			currentWeather.setHighTemp(new String(ch,start,length));
			break;
		case SYMBOL_TAG:
			if (CURRENTTAG != WEATHER_TAG)
				return;
			LauncherLog.log_D(TAG, "SYMBOL_TAG" + new String(ch,start,length));
			currentWeather.setSymbol(new String(ch,start,length));
			break;
		}
		
	}

	public Weather getCurrentWeather() {
		return currentWeather;
	}

}
