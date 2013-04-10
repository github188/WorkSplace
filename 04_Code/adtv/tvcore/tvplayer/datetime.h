/** 
 * \file datetime.h
 * \brief 机顶盒时间接口定义.
 *
 * Copyright(c) 2007-2009 Novel-SuperTV, All rights reserved.
 *
 * Date        Author           Modification\n
 * ----------------------------------------------------------------\n
 * 2007-10-11  Fuwenchao        Created\n
 */
#ifndef NOVELSUPERTV_STBRT_DATETIME_H
#define NOVELSUPERTV_STBRT_DATETIME_H

#include <typ.h>
#include <string>

/// 机顶盒时间类型定义
struct StbTime
{
    /**
     * 构造函数.
     *
     * \param[in] t 时间值，单位:秒
     */
    StbTime( U32 t );

    /**
     * 构造函数.
     *
     * \param[in] uct UTC时间.
     */
    StbTime( U8 const* uct ); 

    /// 构造函数.
    StbTime();

    /**
     * 获取时间中的年份信息，years since 1900.
     *
     * \return 年份信息.
     */
    int year() { return year_ + 1900; }

    /**
     * 返回时间中的月份信息，months since January - [0,11].
     *
     * \return 月份信息.
     */
    int month(){ return month_ + 1; }
    
    /**
     * 时间中的日信息，取值范围[1,31].
     *
     * \return 日信息.
     */    
    int day() { return day_; }
    
    /**
     * 时间中的小时信息，取值范围[0,23]
     *
     * \return 小时信息.
     */    
    int hour(){ return hour_; }

    /**
     * 获取时间中的分钟信息，取值范围[0,59].
     *
     * \return 分钟信息.
     */    
    int minute() { return minute_; }

    /**
     * 获取时间中的秒信息，取值范围[0,59].
     *
     * \return 秒信息.
     */    
    int second() { return second_; }

    /**
     * 获取一周中的第几天，days since Sunday - [0,6].
     *
     * \return 一周中的第几天.
     */    
    int day_of_week() { return dayofweek_; }

    /**
     * 获取一年中的第几天，days since January 1 - [0,365].
     *
     * \return 一年中的第几天.
     */    
    int day_of_year() { return dayofyear_ + 1; }

    /**
     * 判断是否是夏令时
     *
     * \return 是否是夏令时.
     */    
    bool is_dst() { return isdst_!=0; } //hjj-c

    /**
     * 将StbTime转换为经过的秒数
     *
     * \return 经过的秒数
     */ 
    U32 make_time();
    
    /**
     * 将StbTime时间转为标准格式的字符串。
     *
     * \return 标准格式的字符串.
     */ 
    std::string time_string();
    
private:
    int second_;     // seconds after the minute - [0,59]
    int minute_;     // minutes after the hour - [0,59]
    int hour_;       // hours since midnight - [0,23]
    int day_;        // day of the month - [1,31]
    int month_;      // months since January - [0,11]
    int year_;       // years since 1900
    int dayofweek_;  // days since Sunday - [0,6]
    int dayofyear_;  // days since January 1 - [0,365]
    int isdst_;      // daylight savings time flag
private:
    void mjd_to_stbtime( U8 const* mjd );
    void mjd_to_date( U8 const* mjd );
    void time_to_stbtime( U32 time );
};

/**
 * 判断是否已收到网络时间，只有收到网络时间，本模块的功能才有意义.
 *
 * \return 是否已收到网络时间.
 * -true  已收到网络时间.
 * -false 未收到网络时间.
 */
bool IsGotNetworkTime();

/** 
 * 设置GMT时间
 *
 * \param[in] time GMT时间.
 */
void CurrentGMTTime( U32 time );

/** 
 * 返回GMT时间，单位:秒
 *
 * \return GMT时间.
 */
U32 CurrentGMTTime();

/**
 * 获取本地时间，单位:秒
 *
 * \return 本地时间.
 */
U32 CurrentLocalTime();

/** 
 * GMT时间到本地时间转换
 *
 * \param[in] time GMT时间.
 *
 * \return 本地时间. 
 */
U32 GMTToLocal( U32 time );

/**
 * 本地时间到GMT时间转换
 *
 * \param[in] time 本地时间.
 *
 * \return GMT时间.
 */
U32 LocalToGMT( U32 time );

/// 时区类型定义.
enum Zone
{
    Zone_WEST12 = -12,
    Zone_WEST11,
    Zone_WEST10,
    Zone_WEST9,
    Zone_WEST8,
    Zone_WEST7,
    Zone_WEST6,
    Zone_WEST5,
    Zone_WEST4,
    Zone_WEST3,
    Zone_WEST2,
    Zone_WEST1,
    Zone_GMT,
    Zone_EAST1,
    Zone_EAST2,
    Zone_EAST3,
    Zone_EAST4,
    Zone_EAST5,
    Zone_EAST6,
    Zone_EAST7,
    Zone_EAST8,
    Zone_EAST9,
    Zone_EAST0,
    Zone_EAST11,
    Zone_EAST12,
    Zone_EAST13
};

/** 
 * 获取当前时区。
 *
 * \return 当前时区.
 */
Zone  TimeZone();

/**
 * 设置时区.
 *
 * \param[in] zone 时区.
 */
void TimeZone( Zone zone );

/**
 * 格式化时间显示.
 *
 * \param[in] time   时间值.
 * \param[in] format 格式化字符串.
 * <pre>
 * 格式定义：
 * d    显示day不带前面的零(1-31)
 * dd   显示day带前面的零(01-31)
 * ddd  显示day按Sun-Sat
 * dddd 显示day按Sunday-Saturday
 * m    显示month不带前面的零(1-12)
 * mm   显示month带前面的零(01-12)
 * mmm  显示month按Jan-Dec
 * mmmm 显示month按January-December
 * yy   显示year(00-99)
 * yyyy 显示year(0000-9999)
 * h    显示hour不带前面的零(0-23)
 * hh   显示hour带前面的零(00-23)
 * n    显示minute不带前面的零(0-59)
 * nn   显示minute带前面的零(00-59)
 * s    显示second不带前面的零(0-59)
 * ss   显示second带前面的零(00-59)
 * w    显示day按周日-周六
 * ww   显示day按星期日-星期六
 * www  显示day按礼拜日-礼拜六
 * </pre>
 * 
 * \return 格式化后的指定时间字符串.
 *
 * \note 例如: Format( 0, "yyyy/mm/dd");返回1900/01/01.
 */
std::string FormatTime( U32 time, std::string const& format );

/** 
 * 格式化本地时间.
 *
 * \param[in] format 格式化字符串.
 * 
 * \return 格式化后的当前时间字符串.
 */
inline std::string FormatTime( std::string const& format )
{
    return FormatTime( CurrentLocalTime(), format );
}


struct StbTimex
{
	StbTimex() { t_=0; }
	StbTimex(U32 t) { t_=t; }
	StbTimex(StbTimex const &t) { t_=t.t_; }
	StbTimex(int year,int month,int day,int hour,int minute,int second);

	void SetDate(int year,int month,int day);
	void SetTime(int hour,int minute,int second);
	void SetDateTime(int year,int month,int day,int hour,int minute,int second);

	void operator=(StbTimex const &t) { t_=t.t_; }
	void operator=(U32 t) { t_ = t; }
	void operator=(StbTime &t) { t_=t.make_time(); }

	U32 time() const { return t_; }

	bool operator==(StbTimex const &t) const { return t_==t.t_; }
	bool operator!=(StbTimex const &t) const { return t_!=t.t_; }
	bool operator>(StbTimex const &t) const { return t_>t.t_; }
	bool operator>=(StbTimex const &t) const { return t_>=t.t_; }
	bool operator<(StbTimex const &t) const { return t_<t.t_; }
	bool operator<=(StbTimex const &t) const { return t_<=t.t_; }

	StbTimex& operator+=(U32 t) { t_+=t; return *this; }
	StbTimex& operator-=(U32 t) { t_-=t; return *this; }

	static StbTimex GetCurrTime();
	static void SetCurrTime(U32 t);

private:

	U32 t_;
};

int GetMonthDay(int year,int month);

#endif

