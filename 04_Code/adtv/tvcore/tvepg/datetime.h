/** 
 * \file datetime.h
 * \brief ������ʱ��ӿڶ���.
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

/// ������ʱ�����Ͷ���
struct StbTime
{
    /**
     * ���캯��.
     *
     * \param[in] t ʱ��ֵ����λ:��
     */
    StbTime( U32 t );

    /**
     * ���캯��.
     *
     * \param[in] uct UTCʱ��.
     */
    StbTime( U8 const* uct ); 

    /// ���캯��.
    StbTime();

    /**
     * ��ȡʱ���е������Ϣ��years since 1900.
     *
     * \return �����Ϣ.
     */
    int year() { return year_ + 1900; }

    /**
     * ����ʱ���е��·���Ϣ��months since January - [0,11].
     *
     * \return �·���Ϣ.
     */
    int month(){ return month_ + 1; }
    
    /**
     * ʱ���е�����Ϣ��ȡֵ��Χ[1,31].
     *
     * \return ����Ϣ.
     */    
    int day() { return day_; }
    
    /**
     * ʱ���е�Сʱ��Ϣ��ȡֵ��Χ[0,23]
     *
     * \return Сʱ��Ϣ.
     */    
    int hour(){ return hour_; }

    /**
     * ��ȡʱ���еķ�����Ϣ��ȡֵ��Χ[0,59].
     *
     * \return ������Ϣ.
     */    
    int minute() { return minute_; }

    /**
     * ��ȡʱ���е�����Ϣ��ȡֵ��Χ[0,59].
     *
     * \return ����Ϣ.
     */    
    int second() { return second_; }

    /**
     * ��ȡһ���еĵڼ��죬days since Sunday - [0,6].
     *
     * \return һ���еĵڼ���.
     */    
    int day_of_week() { return dayofweek_; }

    /**
     * ��ȡһ���еĵڼ��죬days since January 1 - [0,365].
     *
     * \return һ���еĵڼ���.
     */    
    int day_of_year() { return dayofyear_ + 1; }

    /**
     * �ж��Ƿ�������ʱ
     *
     * \return �Ƿ�������ʱ.
     */    
    bool is_dst() { return isdst_!=0; } //hjj-c

    /**
     * ��StbTimeת��Ϊ����������
     *
     * \return ����������
     */ 
    U32 make_time();
    
    /**
     * ��StbTimeʱ��תΪ��׼��ʽ���ַ�����
     *
     * \return ��׼��ʽ���ַ���.
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
 * �ж��Ƿ����յ�����ʱ�䣬ֻ���յ�����ʱ�䣬��ģ��Ĺ��ܲ�������.
 *
 * \return �Ƿ����յ�����ʱ��.
 * -true  ���յ�����ʱ��.
 * -false δ�յ�����ʱ��.
 */
bool IsGotNetworkTime();

/** 
 * ����GMTʱ��
 *
 * \param[in] time GMTʱ��.
 */
void CurrentGMTTime( U32 time );

/** 
 * ����GMTʱ�䣬��λ:��
 *
 * \return GMTʱ��.
 */
U32 CurrentGMTTime();

/**
 * ��ȡ����ʱ�䣬��λ:��
 *
 * \return ����ʱ��.
 */
U32 CurrentLocalTime();

/** 
 * GMTʱ�䵽����ʱ��ת��
 *
 * \param[in] time GMTʱ��.
 *
 * \return ����ʱ��. 
 */
U32 GMTToLocal( U32 time );

/**
 * ����ʱ�䵽GMTʱ��ת��
 *
 * \param[in] time ����ʱ��.
 *
 * \return GMTʱ��.
 */
U32 LocalToGMT( U32 time );

/// ʱ�����Ͷ���.
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
 * ��ȡ��ǰʱ����
 *
 * \return ��ǰʱ��.
 */
Zone  TimeZone();

/**
 * ����ʱ��.
 *
 * \param[in] zone ʱ��.
 */
void TimeZone( Zone zone );

/**
 * ��ʽ��ʱ����ʾ.
 *
 * \param[in] time   ʱ��ֵ.
 * \param[in] format ��ʽ���ַ���.
 * <pre>
 * ��ʽ���壺
 * d    ��ʾday����ǰ�����(1-31)
 * dd   ��ʾday��ǰ�����(01-31)
 * ddd  ��ʾday��Sun-Sat
 * dddd ��ʾday��Sunday-Saturday
 * m    ��ʾmonth����ǰ�����(1-12)
 * mm   ��ʾmonth��ǰ�����(01-12)
 * mmm  ��ʾmonth��Jan-Dec
 * mmmm ��ʾmonth��January-December
 * yy   ��ʾyear(00-99)
 * yyyy ��ʾyear(0000-9999)
 * h    ��ʾhour����ǰ�����(0-23)
 * hh   ��ʾhour��ǰ�����(00-23)
 * n    ��ʾminute����ǰ�����(0-59)
 * nn   ��ʾminute��ǰ�����(00-59)
 * s    ��ʾsecond����ǰ�����(0-59)
 * ss   ��ʾsecond��ǰ�����(00-59)
 * w    ��ʾday������-����
 * ww   ��ʾday��������-������
 * www  ��ʾday�������-�����
 * </pre>
 * 
 * \return ��ʽ�����ָ��ʱ���ַ���.
 *
 * \note ����: Format( 0, "yyyy/mm/dd");����1900/01/01.
 */
std::string FormatTime( U32 time, std::string const& format );

/** 
 * ��ʽ������ʱ��.
 *
 * \param[in] format ��ʽ���ַ���.
 * 
 * \return ��ʽ����ĵ�ǰʱ���ַ���.
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

