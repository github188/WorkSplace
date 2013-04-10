#include "datetime.h"

#include <xprocess.h>
#include <time.h>
#include <stbruntime.h>

#pragma warning(disable:4996)

const time_t BASE_YEAR        = 1900; // base time subtract one is the logic
									  // basetime
const time_t BASE_MONTH       = 0;    // so 01-01-1900 - 1 = 00-00-1900
const time_t BASE_DAY         = 0;
const time_t BASE_HOUR        = 0;
const time_t BASE_MINUTE      = 0;
const time_t BASE_SECOND      = 0;
const time_t SECOND_PER_HOUR  = 3600;
const time_t SECOND_PER_DAY   = 24 * SECOND_PER_HOUR;
const time_t SECOND_PER_YEAR  = 365 * SECOND_PER_DAY;
const time_t SECOND_FOUR_YEAR = ( 365 * 4 + 1 ) * SECOND_PER_DAY;
const time_t BASE_DOW         = 4;    // 01-01-1970 was a Thursday
const time_t MAX_INT          = 0x7FFFFFFF;

int lpdays[] = { -1, 30, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334, 365 };
int days[] = { -1, 30, 58, 89, 119, 150, 180, 211, 242, 272, 303, 333, 364 };

MutexT time_mutex;
U32 start_systime = 0;
U32 cur_time = 0;
U32 start_time = 0;
Zone time_zone = Zone_EAST8; // China U32 Zone

const time_t  GM_BASE_YEAR = 70;
const time_t  GM_MAX_YEAR  = 138;
const time_t GM_LEAP_YEAR_ADJUST =  17; // Leap years 1900 - 1970

struct tm tb = { 0 }; 

int gm_days[] = { -1, 30, 58, 89, 119, 150, 180, 211, 242, 272, 303, 333, 364 };

//判断是否为闰年
inline bool is_leap_year( int year )
{
    return year % 400 == 0 || ( year % 4 == 0 && year % 100 != 0 );
}

//把BCD码转化为整数
inline U8 bcd2d( U8 bcd )
{
    return ( ( bcd >> 4 ) & 0x0F ) * 10 + ( bcd & 0x0F );
}

inline bool check_year( time_t year )
{
    return ( ( year < GM_BASE_YEAR - 1 ) || ( year > GM_MAX_YEAR + 1 ) );
}

inline bool check_add( time_t dest, time_t src1, time_t src2 )
{
    return ( ( ( src1 >= 0L ) && ( src2 >= 0L ) && ( dest < 0L ) ) 
        || ( ( src1 < 0L ) && ( src2 < 0L ) && ( dest >= 0L ) ) );
}

inline bool check_mul( time_t dest, time_t src1, time_t src2 )
{
    return ( src1 ? ( dest / src1 != src2 ) : false );
}

struct tm*  gm_time( const time_t *timp )
{
    time_t caltim = *timp;  // calendar time to convert
    int islpyr = 0;         // is-current-year-a-leap-year flag
    int tmptim;
    int *mdays;             // pointer to days or lpdays

    struct tm *ptb = &tb;

    if( caltim < 0 )
        return 0;
            
    /*
     * Determine years since 1970. First, identify the four-year interval
     * since this makes handling leap-years easy (note that 2000 IS a
     * leap year and 2100 is out-of-range).
     */
    tmptim = (int)( caltim / SECOND_FOUR_YEAR );
    caltim -= ( ( time_t ) tmptim * SECOND_FOUR_YEAR );

    // Determine which year of the interval
    tmptim = ( tmptim * 4 ) + 70; // 1970, 1974, 1978,...,etc.

    if( caltim >= SECOND_PER_YEAR )
    {
        tmptim++;                 // 1971, 1975, 1979,...,etc.
        caltim -= SECOND_PER_YEAR;

        if( caltim >= SECOND_PER_YEAR ) 
        {
            tmptim++;                   // 1972, 1976, 1980,...,etc.
            caltim -= SECOND_PER_YEAR;

            /*
             * Note, it takes 366 days-worth of seconds to get past a leap
             * year.
             */
            if( caltim >= (SECOND_PER_YEAR + SECOND_PER_DAY) ) 
            {
                tmptim++;           // 1973, 1977, 1981,...,etc.
                caltim -= ( SECOND_PER_YEAR + SECOND_PER_DAY );
            }
            else 
            {
                // In a leap year after all, set the flag.
                islpyr++;
            }
        }
    }

    /*
     * tmptim now holds the value for tm_year. caltim now holds the
     * number of elapsed seconds since the beginning of that year.
     */
    ptb->tm_year = tmptim;

    /*
     * Determine days since January 1 (0 - 365). This is the tm_yday value.
     * Leave caltim with number of elapsed seconds in that day.
     */
    ptb->tm_yday = (int)( caltim / SECOND_PER_DAY );
    caltim -= (time_t)( ptb->tm_yday ) * SECOND_PER_DAY;

    /*
     * Determine months since January (0 - 11) and day of month (1 - 31)
     */
    if( islpyr )
        mdays = lpdays;
    else
        mdays = days;


    for( tmptim = 1 ; mdays[tmptim] < ptb->tm_yday ; tmptim++ ) ;

    ptb->tm_mon = --tmptim;

    ptb->tm_mday = ptb->tm_yday - mdays[tmptim];

    /*
     * Determine days since Sunday (0 - 6)
     */
    ptb->tm_wday = ( ( int )( *timp / SECOND_PER_DAY) + BASE_DOW ) % 7;

    /*
     *  Determine hours since midnight (0 - 23), minutes after the hour
     *  (0 - 59), and seconds after the minute (0 - 59).
     */
    ptb->tm_hour = ( int )( caltim / 3600 );
    caltim -= (time_t)ptb->tm_hour * 3600L;

    ptb->tm_min = ( int )( caltim / 60 );
    ptb->tm_sec = ( int )( caltim - ( ptb->tm_min ) * 60 );

    ptb->tm_isdst = 0;
    return( (struct tm *)ptb );

}

time_t  mk_gm_time ( struct tm *tb )
{
    time_t tmptm1, tmptm2, tmptm3;
    struct tm *tbtemp;

    tmptm1 = tb->tm_year;

     // First, make sure tm_year is reasonably close to being in range.
    if( check_year( tmptm1 ) )
        return (time_t)(-1);

    /*
     * Adjust month value so it is in the range 0 - 11.  This is because
     * we don't know how many days are in months 12, 13, 14, etc.
     */
    if( ( tb->tm_mon < 0 ) || ( tb->tm_mon > 11 ) ) 
    {
        // no danger of overflow because the range check above.
        tmptm1 += ( tb->tm_mon / 12 );

        if ( ( tb->tm_mon %= 12 ) < 0 ) 
        {
            tb->tm_mon += 12;
            tmptm1--;
        }

        // Make sure year count is still in range.
        if ( check_year( tmptm1 ) )
            return (time_t)(-1);
    }

    /***** HERE: tmptm1 holds number of elapsed years *****/

    /*
     * Calculate days elapsed minus one, in the given year, to the given
     * month. Check for leap year and adjust if necessary.
     */
    tmptm2 = gm_days[tb->tm_mon];
    if ( !( tmptm1 & 3 ) && ( tb->tm_mon > 1 ) )
            tmptm2++;

    /*
     * Calculate elapsed days since base date (midnight, 1/1/70, UTC)
     *
     *
     * 365 days for each elapsed year since 1970, plus one more day for
     * each elapsed leap year. no danger of overflow because of the range
     * check (above) on tmptm1.
     */
    tmptm3 = ( tmptm1 - GM_BASE_YEAR ) * 365L + ( ( tmptm1 - 1L ) >> 2 )
      - GM_LEAP_YEAR_ADJUST;

    // elapsed days to current month (still no possible overflow)
    tmptm3 += tmptm2;

     //elapsed days to current date. overflow is now possible.
    tmptm2 = (time_t)( tb->tm_mday );
    tmptm1 = tmptm3 + tmptm2;
    if ( check_add( tmptm1, tmptm3, tmptm2 ) )
        return (time_t)(-1);

    /***** HERE: tmptm1 holds number of elapsed days *****/

    // Calculate elapsed hours since base date
    tmptm2 = tmptm1 * 24L;
    if ( check_mul( tmptm2, tmptm1, 24L ) )
        return (time_t)(-1);

    tmptm1 = tmptm2 + ( tmptm3 = (time_t)tb->tm_hour );
    if ( check_add( tmptm1, tmptm2, tmptm3 ) )
        return (time_t)(-1);

    /***** HERE: tmptm1 holds number of elapsed hours *****/

    /*
     * Calculate elapsed minutes since base date
     */

    tmptm2 = tmptm1 * 60L;
    if ( check_mul( tmptm2, tmptm1, 60L ) )
        return (time_t)(-1);

    tmptm1 = tmptm2 + ( tmptm3 = (time_t)tb->tm_min );
    if ( check_add( tmptm1, tmptm2, tmptm3 ) )
        return (time_t)(-1);

    //***** HERE: tmptm1 holds number of elapsed minutes *****

    // * Calculate elapsed seconds since base date
    tmptm2 = tmptm1 * 60L;
    if ( check_mul( tmptm2, tmptm1, 60L ) )
        return (time_t)(-1);

    tmptm1 = tmptm2 + ( tmptm3 = (time_t)tb->tm_sec );
    if ( check_add( tmptm1, tmptm2, tmptm3 ) )
        return (time_t)(-1);

    //***** HERE: tmptm1 holds number of elapsed seconds *****

    if ( ( tbtemp = gm_time( &tmptm1 ) ) == 0 )
        return (time_t)(-1);

    //***** HERE: tmptm1 holds number of elapsed seconds, adjusted *****
    //*****       for local time if requested                      *****

    *tb = *tbtemp;
    return (time_t)tmptm1;
}

////////////////////////////////////////////////////////////////////////////////
// begin of StbTime class
////////////////////////////////////////////////////////////////////////////////
StbTime::StbTime( U32 t ) 
:	second_( 0 ), 
	minute_( 0 ), 
	hour_( 0 ), 
	day_( 1 ), 
	month_( 0 ), 
	year_( 70 ), 
	dayofweek_( 4 ), 
	dayofyear_( 0 ), 
	isdst_( 0 )
{ 
    time_to_stbtime( t ); 
}

StbTime::StbTime(  U8 const* utc ) 
: second_( 0 ), minute_( 0 ), hour_( 0 )
, day_( 1 ), month_( 0 ), year_( 70)
, dayofweek_( 4 ), dayofyear_( 0 ), isdst_( 0 )
{ 
    mjd_to_stbtime( utc ); 
}
    
StbTime::StbTime()
: second_( 0 ), minute_( 0 ), hour_( 0 )
, day_( 1 ), month_( 0 ), year_( 70 )
, dayofweek_( 4 ), dayofyear_( 0 ), isdst_( 0 )
{} 

U32 StbTime::make_time()
{
    struct tm ctm;

    ctm.tm_sec = second_;
    ctm.tm_min = minute_;
    ctm.tm_hour = hour_;
    ctm.tm_mday = day_;
    ctm.tm_mon = month_;
    ctm.tm_year = year_;
    ctm.tm_isdst = isdst_;
    ctm.tm_wday = dayofweek_;
    ctm.tm_yday = dayofyear_;

    time_t timep = mk_gm_time( &ctm );

    if( timep == (time_t)(-1) )
        return (U32)(-1);
/*	这和上面不是重复吗，有必要再赋值吗?		
    second_ = ctm.tm_sec;
    minute_ = ctm.tm_min;
    hour_ = ctm.tm_hour;
    day_ = ctm.tm_mday;
    month_ = ctm.tm_mon;
    year_ = ctm.tm_year;
    dayofweek_ = ctm.tm_wday;
    dayofyear_ = ctm.tm_yday;
*/    
    isdst_ = ctm.tm_isdst;

    return (U32) timep; //hjj-c
}

std::string StbTime::time_string()
{
    struct tm ctm;

    ctm.tm_sec = second_;
    ctm.tm_min = minute_;
    ctm.tm_hour = hour_;
    ctm.tm_mday = day_;
    ctm.tm_mon = month_;
    ctm.tm_year = year_;
    ctm.tm_wday = dayofweek_;
    ctm.tm_yday = dayofyear_;
    ctm.tm_isdst = isdst_;
    
    return std::string( asctime( &ctm ) );
}

void StbTime::mjd_to_stbtime( U8 const* mjd )
{
    if( !mjd )
        return;

    mjd_to_date( mjd );

    //取得UTC值，24位，mjd的后3字节
    U8 hour   = bcd2d( mjd[2] ) + BASE_HOUR;
    U8 minute = bcd2d( mjd[3] ) + BASE_MINUTE;
    U8 second = bcd2d( mjd[4] ) + BASE_SECOND;

    //调整时间
    if( second > 60 )
    {
        second -= 60;
        minute++;
    }
    if( minute > 60 )
    {
        minute -= 60;
        hour++;
    }
    if( hour > 24 )
        hour -= 24;

    hour_ = hour;
    minute_ = minute;
    second_ = second;
}

/*****************************************************************************
MJD计算日期公式:
       Y' = int [ (MJD - 15 078,2) / 365,25 ]
       M' = int { [ MJD - 14 956,1 - int (Y' * 365,25) ] / 30,6001 }
       If M' = 14 or M' = 15, then K = 1; else K = 0
       Y = Y' + K
       M = M' - 1 - K * 12
       D = MJD - 14 956 - int (Y' * 365,25) - int (M' ′ 30,6001 )
*****************************************************************************/
void StbTime::mjd_to_date( U8 const* mjd )
{
    U32 date = ( mjd[0] << 8 ) + mjd[1]; //取得MJD值
    U32 year = ( date * 100 - 1507820 ) / 36525;
    U8 month = (U8) (( date * 10000 - 149561000 - ( year * 3652500 ) ) / 306001); //hjj-c
    int k = 0;
    if( month == 14 || month == 15 )
        k = 1;
    U16 newyear = (U16)(year + k);	//hjj-c
    U8 newmonth = month - 1 - k * 12;
    U8 newday   = (U8)(date - 14956 - year * 36525 / 100 - month * 306001 / 10000); //hjj-c


    //加上基准日期，计算标准日期
    newyear  += BASE_YEAR;
    newmonth += BASE_MONTH;
    if( newmonth > 12 )
    {
        newmonth -= 12;
        newyear++;
    }
    newday += BASE_DAY;//日期最大为31+31=62天

    if( newmonth == 2 )
    {//当为2月时，调整如下
        if( is_leap_year( newyear ) && newday > 29 )
        {
            newday -= 29;
            newmonth++;
        }
        else if( newday > 28 )
        {
            newday -= 28;
            newmonth++;
        }
        if( newday > 31 )
        {
            newday -= 31;
            newmonth++;
        }
    }
    else if( newmonth == 1 || newmonth == 3 
        || newmonth == 5 || newmonth == 7 
        || newmonth == 8 || newmonth == 10 
        || newmonth == 12 )
    {//当前为大月份，调整如下
        if( newday > 31 )
        {
            newday -= 31;
            newmonth++;

            //当是1月加1为2月时，继续调整日期
            if( newmonth == 2 )
            {
               if( is_leap_year( newyear ) && newday > 29 )
               {
                   newday -= 29;
                   newmonth++;
               }
               else if( newday > 28 )
               {
                   newday -= 28;
                   newmonth++;
               }
            }

            //当不是7月或12月加1为8月或13月（这两月为31天）时，继续调整日期
            if( ( newmonth != 8 ) && ( newmonth != 13 ) && ( newday > 30 ) )
            {
               newday -= 30;
               newmonth++;
            }

            if( newmonth > 12 )
            {
                newmonth -= 12;
                newyear++;
            }
        }
    }
    else if( newmonth == 4 || newmonth == 6 
        || newmonth == 9 || newmonth == 11 )
    {//当前为小月份，调整如下
        if( newday > 30 )
        {
            newday -= 30;
            newmonth++;

            if( newday > 31 )
            {
                newday -= 31;
                newmonth++;
            }

            if( newmonth > 12 )
            {
                newmonth -= 12;
                newyear++;
            }
        }
    }

    //判断公式的有效性（1900-3-1至2100-2-28）
    if( ( newyear < BASE_YEAR ) || ( newyear > 2100 ) 
        || ( ( newyear == BASE_YEAR ) && ( newmonth < 3 ) ) 
        || ( ( newyear == 2100 ) && ( newmonth > 2 ) ) )
        return;

    year_ = newyear - BASE_YEAR;
    month_ = newmonth - 1;
    day_ = newday;
}

void StbTime::time_to_stbtime( U32 time )
{
    time_t tmptm = static_cast<time_t>( time );
    struct tm *tb = gm_time( &tmptm );
    
    if( !tb )
    {
        return;
    }
    
    mk_gm_time( tb );

    second_    = tb->tm_sec;
    minute_    = tb->tm_min;
    hour_      = tb->tm_hour;
    day_       = tb->tm_mday;
    month_     = tb->tm_mon;
    year_      = tb->tm_year;
    dayofweek_ = tb->tm_wday;
    dayofyear_ = tb->tm_yday;
    isdst_     = tb->tm_isdst;
}

bool IsGotNetworkTime()
{
    return start_time != 0;
}

void CurrentGMTTime( U32 time )
{
	AutoLockT lock(time_mutex);
    
    start_time = time;
    start_systime = NS_GetTickCount();
    cur_time = start_time;
}

U32 CurrentGMTTime()
{
    AutoLockT lock(time_mutex);
    // need to check
    cur_time = start_time + ( NS_GetTickCount()-start_systime ) / 1000;
    return cur_time;
}

U32 CurrentLocalTime()
{
    return GMTToLocal( CurrentGMTTime() );
}

U32 GMTToLocal( U32 time )
{
    int ltime = time + time_zone * SECOND_PER_HOUR;

    if ( ( time > 3 * SECOND_PER_DAY ) 
        && ( time < MAX_INT - 3 * SECOND_PER_DAY ) )
    {
        /* The date does not fall within the first three, or last
         * three, representable days of the Epoch. Therefore, there
         * is no possibility of overflowing or underflowing the
         * time_t representation as we compensate for timezone and
         * Daylight Savings U32.  
         */
        StbTime stb_time( ltime );
        return stb_time.make_time();
    }

    StbTime stb_time( time );

    /* The date falls with the first three, or last three days
     * of the Epoch. It is possible the time_t representation
     * would overflow or underflow while compensating for
     * timezone and Daylight Savings U32. Therefore, make the
     * timezone and Daylight Savings U32 adjustments directly
     * in the tm structure. The beginning of the Epoch is
     * 00:00:00, 01-01-70 (UTC) and the last representable second
     * in the Epoch is 03:14:07, 01-19-2038 (UTC). This will be
     * used in the calculations below.
     *
     * First, adjust for the timezone.  
     */
    int second = ltime % 60;
    if( second < 0 )
    {
        second += 60;
        ltime -= 60;
    }

    ltime = stb_time.minute() + ltime / 60;
    int minute = ltime % 60;
    if( minute < 0 )
    {
        minute += 60;
        ltime -= 60;
    }

    ltime = stb_time.hour() + ltime / 60;
    int hour = ltime % 24 ;
    if( hour < 0 )
    {
        hour += 24;
        ltime -= 24;
    }

    ltime /= 24;

    int day = stb_time.day();
    int month = stb_time.month() - 1;
    int day_of_week = stb_time.day_of_week();
    int day_of_year = stb_time.day_of_year() - 1;
    
    if( ltime > 0 )
    {
        /* There is no possibility of overflowing the tm_mday
         * and tm_yday fields since the date can be no later
         * than January 19. 
         */
        day_of_week = ( day_of_week + ltime ) % 7;
        day += ltime;
        day_of_year += ltime;
    }
    else if( ltime < 0 )
    {
        /* It is possible to underflow the tm_mday and tm_yday
         * fields. If this happens, then adjusted date must
         * lie in December 1969.  */
        day_of_week = ( day_of_week + 7 + ltime ) % 7;
        if( ( day += ltime ) <= 0 )
        {
            day += 31;
            day_of_year = 364;
            month = 11;
            day_of_year--;
        }
        else
            day_of_year += ltime;
    }
    struct tm ctm;

    ctm.tm_sec = second;
    ctm.tm_min = minute;
    ctm.tm_hour = hour;
    ctm.tm_mday = day;
    ctm.tm_mon = month;
    ctm.tm_yday = day_of_year;
    ctm.tm_wday = day_of_week;
    ctm.tm_year = stb_time.year() - 1900;
    ctm.tm_isdst = stb_time.is_dst();

    time_t timep = mk_gm_time( &ctm );

    if( timep == (time_t)(-1) )
        return (U32)(-1);
    return (U32) timep; //hjj-c
}

U32 LocalToGMT( U32 time )
{
    return time - time_zone * SECOND_PER_HOUR;
}

Zone  TimeZone()
{
    return time_zone;
}

void TimeZone( Zone z )
{
    time_zone = z;
}

//格式化时间显示
//格式定义：
// d    显示day不带前面的零(1-31)
// dd   显示day带前面的零(01-31)
// ddd  显示day按Sun-Sat
// dddd 显示day按Sunday-Saturday
// m    显示month不带前面的零(1-12)
// mm   显示month带前面的零(01-12)
// mmm  显示month按Jan-Dec
// mmmm 显示month按January-December
// yy   显示year(00-99)
// yyyy 显示year(0000-9999)
// h    显示hour不带前面的零(0-23)
// hh   显示hour带前面的零(00-23)
// n    显示minute不带前面的零(0-59)
// nn   显示minute带前面的零(00-59)
// s    显示second不带前面的零(0-59)
// ss   显示second带前面的零(00-59)
// w    显示day按周日-周六
// ww   显示day按星期日-星期六
// www  显示day按礼拜日-礼拜六
//例如: Format( 0, "yyyy/mm/dd");返回1900/01/01

static bool valid( char ch )
{
    if( ch == 'd' || ch == 'm' || ch == 'y' || ch == 'h'
        || ch == 'n' || ch == 's' || ch == 'w' )
        return true;
    return false;
}

namespace
{
    int const SIZE_BUF_MULTIPLE = 7;
    int const TEMP_BUF_SIZE = 100;
}

std::string FormatTime( U32 time, std::string const& format )
{
    StbTime stb_time( time );

    size_t size = format.size();
    if( !size )
        return std::string();

    char* time_str = new char[size * SIZE_BUF_MULTIPLE];
    int pos = 0;
    for( size_t i = 0; i < size; i++ )
    {
        char temp[TEMP_BUF_SIZE];
        if( !valid( format[i] ) )
        {
            time_str[pos++] = format[i];
            continue;
        }
        int k = 0;
        temp[k] = format[i];
        for( ; i < size && k < TEMP_BUF_SIZE - 2; i++ )
        {
            if( temp[k] == format[i + 1] )
                temp[++k] = format[i + 1];
            else
                break;
        }
        temp[k + 1] = 0;
        k = static_cast<int>(strlen( temp ));
        if( k == 1 )
        {
            if( temp[0] == 'd' )
                sprintf( temp, "%d", stb_time.day() );
            else if( temp[0] == 'm' )
                sprintf( temp, "%d", stb_time.month() );
            else if( temp[0] == 'h' )
                sprintf( temp, "%d", stb_time.hour() );
            else if( temp[0] == 'n' )
                sprintf( temp, "%d", stb_time.minute() );
            else if( temp[0] == 's' )
                sprintf( temp, "%d", stb_time.second() );
            else if( temp[0] == 'w' )
            {
                const char *weeks[] =
                {
                    "日", "一", "二", "三", "四", "五", "六"
                };
                sprintf( temp, "周%s", weeks[stb_time.day_of_week()] );
            }
            strncpy( time_str + pos, temp, strlen( temp ) );
            pos += static_cast<int>(strlen( temp ));
        }
        else if( k == 2 )
        {
            if( temp[0] == 'd' )
                sprintf( temp, "%02d", stb_time.day() );
            else if( temp[0] == 'm' )
                sprintf( temp, "%02d", stb_time.month() );
            else if( temp[0] == 'y' )
            {
                char str[10];
                sprintf( str, "%d", stb_time.year() );
                temp[0] = str[2];
                temp[1] = str[3];
                temp[2] = 0;
            }
            else if( temp[0] == 'h' )
                sprintf( temp, "%02d", stb_time.hour() );
            else if( temp[0] == 'n' )
                sprintf( temp, "%02d", stb_time.minute() );
            else if( temp[0] == 's' )
                sprintf( temp, "%02d", stb_time.second() );
            else if( temp[0] == 'w' )
            {
                const char *weeks[] =
                {
                    "日", "一", "二", "三", "四",
                    "五", "六"
                };
                sprintf( temp, "星期%s", weeks[stb_time.day_of_week()] );
            }
            strncpy( time_str + pos, temp, strlen( temp ) );
            pos += static_cast<int>(strlen( temp ));
        }
        else if( k == 3 )
        {
            if( temp[0] == 'd' )
            {
                const char *weeks[] =
                {
                    "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"
                };
                sprintf( temp, "%s", weeks[stb_time.day_of_week()]);
            }
            else if( temp[0] == 'm' )
            {
                const char *months[] =
                {
                    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
                };
                sprintf( temp, "%s", months[stb_time.month()] );
            }
            else if( temp[0] == 'w' )
            {
                const char *weeks[] =
                {
                    "日 ", "一 ", "二 ", "三 ", "四 ",
                    "五 ", "六 "
                };
                sprintf( temp, "礼拜%s", weeks[stb_time.day_of_week()] );
            }
            strncpy( time_str + pos, temp, strlen( temp ) );
            pos += static_cast<int>(strlen( temp ));
        }
        else if( k >= 4 )
        {
            char str[50];
            if( temp[0] == 'd' )
            {
                const char *weeks[] =
                {
                    "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday",
                    "Friday", "Saturday"
                };
                sprintf( str, "%s", weeks[stb_time.day_of_week()]);
            }
            else if( temp[0] == 'm' )
            {
                const char *months[] =
                {
                    "January", "February", "March", "April", "May", "June",
                    "July", "August", "September", "October", "November",
                    "December"
                };
                sprintf( str, "%s", months[stb_time.month()] );
            }
            else if( temp[0] == 'y' )
                sprintf( str, "%d", stb_time.year() );

            strncpy( time_str + pos, str, strlen( str ) );
            pos += static_cast<int>(strlen( str ));
            int i;
            for( i = 0; i < k - 4; i++ )
                str[i] = temp[k - 4 + i];
            str[i] = 0;
            strncpy( time_str + pos, temp, strlen( str ) );
            pos += static_cast<int>(strlen( str ));
        }
    }
    time_str[pos] = 0;
    std::string str( time_str );
    delete []time_str;
    return str;
}


struct StbTimeMap
{
	int second_;     // seconds after the minute - [0,59]
	int minute_;     // minutes after the hour - [0,59]
	int hour_;       // hours since midnight - [0,23]
	int day_;        // day of the month - [1,31]
	int month_;      // months since January - [0,11]
	int year_;       // years since 1900
	int dayofweek_;  // days since Sunday - [0,6]
	int dayofyear_;  // days since January 1 - [0,365]
	int isdst_;      // daylight savings time flag
};

int GetMonthDay(int year,int month)   
{   
	if( month<1 || month>12 )
		return 0;

	int daymonth[12]={31,28,31,30,31,30,31,31,30,31,30,31};
	int rt = daymonth[month-1];   
	if( month==2 &&
		(((0==(year%4))&&(0!=(year%100)))||(0==(year%400))))
		rt++;   
	return rt;   
}

bool CheckDate(int year,int month,int day)
{
	if( year<1900 || year>2038 )
		return false;
	if( month<1 || month>12 )
		return false;
	if( day<=0 || day>GetMonthDay(year,month))
		return false;
	return true;
}

bool CheckTime(int hour,int minute,int second)
{
	return ( hour>=0 && hour<=23
		&& minute>=0 && minute<=59
		&& second>=0 && second<=59 );
}

StbTimex::StbTimex(int year,int month,int day,int hour,int minute,int second)
{
	SetDateTime(year,month,day,hour,minute,second);
}

void StbTimex::SetDate(int year,int month,int day)
{
	if(CheckDate(year,month,day))
	{
		StbTime rt(t_);
		StbTimeMap *p = reinterpret_cast<StbTimeMap *>(&rt);
		p->year_ = year-1900;
		p->month_ = month-1;
		p->day_ = day;
		t_ = rt.make_time();
	}
}

void StbTimex::SetTime(int hour,int minute,int second)
{
	if(	CheckTime(hour,minute,second) )
	{
		StbTime rt(t_);
		StbTimeMap *p = reinterpret_cast<StbTimeMap *>(&rt);
		p->hour_ = hour;
		p->minute_ = minute;
		p->second_ = second;
		t_ = rt.make_time();
	}
}

void StbTimex::SetDateTime(int year,int month,int day,int hour,int minute,int second)
{
	if( CheckDate(year,month,day) 
		&& CheckTime(hour,minute,second) )
	{
		StbTime rt(static_cast<U32>(0));
		StbTimeMap *p = reinterpret_cast<StbTimeMap *>(&rt);
		p->year_ = year-1900;
		p->month_ = month;
		p->day_ = day;
		p->hour_ = hour;
		p->minute_ = minute;
		p->second_ = second;
		t_ = rt.make_time();
	}
}

StbTimex StbTimex::GetCurrTime()
{
	//return StbTimex(NS_GetCurrTime());
	printf("no implement.\n");
	return StbTimex();
}

void StbTimex::SetCurrTime(U32 t)
{
	//NS_SeyCurrTime(t);
	printf("no implement.\n");
}