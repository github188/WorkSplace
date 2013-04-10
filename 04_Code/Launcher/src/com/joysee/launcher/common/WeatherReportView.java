package com.joysee.launcher.common;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Calendar;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.joysee.launcher.activity.R;
import com.joysee.launcher.utils.LauncherLog;
import com.joysee.launcher.utils.XmlHandler;

public class WeatherReportView extends FrameLayout {

	private static final String TAG = "com.joysee.launcher.common.WeatherReportView";

	private View mWeatherPic;
	private TextView mWeatherStatus;
	private TextView mWeatherTemperature;
	private static final int WEATHER_SUC = 1;
	private static final int WEATHER_FAIL = 2;
	
	private static final String TEMPERATURE_MARK = "℃";
	private static final String TEMPERATURE_GAP = "~";
	
	
	private static final int STATUS_FINE = 1;
	private static final int STATUS_YIN  = 2;
	private static final int STATUS_CLOUDY = 3;
	private static final int STATUS_SLEET = 4;
	private static final int STATUS_SNOW_FLURRIES = 5;
	private static final int STATUS_LIGHT_RAIN = 6;
	private static final int STATUS_LIGHT_SNOW = 7;
	private static final int STATUS_FREEZING_RAIN = 8;
	private static final int STATUS_SHOWERS = 9;
	private static final int STATUS_THE_FOG = 10;
	private static final int STATUS_SNOW = 11;
	private static final int STATUS_BLIZZARD = 12;
	private static final int STATUS_ZHONGXUE = 13;
	private static final int STATUS_LITTLE_TO_ZHONGXUE = 14;
	private static final int STATUS_COSTARRING = 15;
	private static final int STATUS_THE_HEAVY_RAIN = 16;
	private static final int STATUS_THUNDER_SHOWER = 17;
	private static final int STATUS_FLOATING_DUST = 18;
	private static final int STATUS_SMALL_TO_COSTARRING = 19;
	private static final int STATUS_DUST = 20;
	private static final int STATUS_TO_SNOW_IN = 21;
	private static final int STATUS_MAJOR_TO_BLIZZARD = 22;
	private static final int STATUS_TO_HEAVY_RAIN_IN = 23;
	private static final int STATUS_HEAVY_RAIN_AND = 29;
	private static final int STATUS_BIG_TO_THE_STORM = 30;
	private static final int STATUS_SANDSTORM = 31;
	private static final int STATUS_TORRENTIAL_RAIN = 32;
	
	private static String WEATHER_FAIL_HINT;
	
	private Drawable mBackground;
	private Context mContext;

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what){
				case WEATHER_SUC:
					if(mBackground == null)
						mBackground = mWeatherPic.getBackground();
					Weather weather = (Weather) msg.obj;
					mWeatherStatus.setText(weather.getDesc());
					StringBuilder sb = new StringBuilder();
					sb.append(weather.getLowTemp());
					sb.append(TEMPERATURE_MARK);
					sb.append(TEMPERATURE_GAP);
					sb.append(weather.getHighTemp());
					sb.append(TEMPERATURE_MARK);
					mWeatherTemperature.setText(sb.toString());
					
					String symbolStr = weather.getSymbol();
					int symbol = Integer.parseInt(symbolStr);
					
					switch (symbol) {
						case STATUS_YIN://2
							mBackground.setLevel(1);//yin
							break;
						case STATUS_CLOUDY:
							mBackground.setLevel(2);//cloudy
							break;
						case STATUS_SLEET:
						case STATUS_LIGHT_SNOW:
						case STATUS_ZHONGXUE:
						case STATUS_LITTLE_TO_ZHONGXUE:
							mBackground.setLevel(3);//light_snow
							break;
						case STATUS_LIGHT_RAIN:
						case STATUS_COSTARRING:
						case STATUS_SMALL_TO_COSTARRING:
							mBackground.setLevel(4);//light_rain
							break;
						case STATUS_FREEZING_RAIN:
						case STATUS_SHOWERS:
						case STATUS_THE_HEAVY_RAIN:
						case STATUS_TO_HEAVY_RAIN_IN:
						case STATUS_HEAVY_RAIN_AND:
						case STATUS_BIG_TO_THE_STORM:
						case STATUS_TORRENTIAL_RAIN:
							mBackground.setLevel(5);//freezing_rain
							break;
						case STATUS_SNOW:
						case STATUS_SNOW_FLURRIES:
						case STATUS_BLIZZARD:
						case STATUS_MAJOR_TO_BLIZZARD:
						case STATUS_TO_SNOW_IN:
							mBackground.setLevel(6);//freezing_snow
							break;
						case STATUS_THE_FOG:
							mBackground.setLevel(7);//fog
							break;
						case STATUS_SANDSTORM:
						case STATUS_FLOATING_DUST:
						case STATUS_DUST:
							mBackground.setLevel(8);//sandstorm
							break;
						case STATUS_FINE:
							mBackground.setLevel(9);//fine
							break;
						case STATUS_THUNDER_SHOWER:
							mBackground.setLevel(10);//thunder_shower
						default:
							mBackground.setLevel(0);//fail
							break;
					}
					
					break;
				case WEATHER_FAIL:
					mWeatherStatus.setText(WEATHER_FAIL_HINT);
					mWeatherTemperature.setText("");
					break;
			}
		};
	};
	
	

	class LoadWeatherThread extends Thread {
		public void run() {

			try {
				SAXParserFactory spf = SAXParserFactory.newInstance();
				SAXParser sp = spf.newSAXParser();
				XMLReader reader = sp.getXMLReader();
				XmlHandler handler = new XmlHandler();
				reader.setContentHandler(handler);
				URL url = new URL("http://m.sohu.com/weather/cms/" + getCurrentDay() + "/110000.xml");
				LauncherLog.log_D(TAG, "http://m.sohu.com/weather/cms/" + getCurrentDay() + "/110000.xml");
				InputStream is = url.openStream();
				InputStreamReader isr = new InputStreamReader(is, "GBK");

				InputSource source = new InputSource(isr);
				reader.parse(source);
				Weather weather = handler.getCurrentWeather();
				LauncherLog.log_D(TAG, "weather.getCity        " + weather.getCity());
				LauncherLog.log_D(TAG, "weather.getDesc        " + weather.getDesc());
				LauncherLog.log_D(TAG, "weather.getLowTemp     " + weather.getLowTemp());
				LauncherLog.log_D(TAG, "weather.getHighTemp    " + weather.getHighTemp());
				LauncherLog.log_D(TAG, "weather.getSymbol    " + weather.getSymbol());
				String symbol = weather.getSymbol();
				symbol = symbol.substring(0, 2);
				LauncherLog.log_D(TAG, "symbol    " + symbol);
				weather.setSymbol(symbol);
				Message msg = new Message();
				msg.obj = weather;
				msg.what = WEATHER_SUC;
				mHandler.sendMessage(msg);

			} catch (Exception e) {
				e.printStackTrace();
				Message msg = new Message();
				msg.what = WEATHER_FAIL;
				mHandler.sendMessage(msg);
				
			}

		}
	}

	public String getCurrentDay() {
		Calendar c = Calendar.getInstance();

		StringBuilder b = new StringBuilder();
		b.append(c.get(Calendar.YEAR));
		b.append(getMonthString(c.get(Calendar.MONTH) + 1));
		b.append(getDayOfMonthString(c.get(Calendar.DAY_OF_MONTH)));

		LauncherLog.log_D(TAG, "WeatherReport   getCurrentDay is " + b.toString());

		return b.toString();
	}

	private String getMonthString(int month) {
		if (month < 1 || month > 12)
			return null;
		if (month >= 1 && month < 10) {
			return new String("0" + month);
		} else {
			return String.valueOf(month);
		}
	}

	private String getDayOfMonthString(int day) {
		if (day < 1 || day > 31)
			return null;
		if (day >= 1 && day < 10) {
			return new String("0" + day);
		} else {
			return String.valueOf(day);
		}
	}

	public WeatherReportView(Context context) {
		super(context);
	}

	public WeatherReportView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public WeatherReportView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
	}
 
	@Override
	protected void onFinishInflate() {
		LauncherLog.log_D(TAG, "WeatherReportView : onFinishInflate() begin");
		super.onFinishInflate();

		mWeatherPic = (View) findViewById(R.id.weather_report_pic);
		mWeatherStatus = (TextView) findViewById(R.id.weather_report_status);
		mWeatherTemperature = (TextView) findViewById(R.id.weather_report_temperature);
		WEATHER_FAIL_HINT = getResources().getString(R.string.launcher_weather_fail_hint);
		new LoadWeatherThread().start();
	}

}
