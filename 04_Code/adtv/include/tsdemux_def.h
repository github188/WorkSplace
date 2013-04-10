#ifndef JOYSEE_TSDEMUX_DEF_H_
#define JOYSEE_TSDEMUX_DEF_H_

#include <vector>
#define UT_INFINITE	 (0xffffffff)
#define TD_TSDATSZIE (188*496*2)
#define TD_TSPACKET	 (496*2)
#define NO_TABLE_ID	 (0xff)

typedef unsigned short utPid;
typedef unsigned char utTid;
typedef unsigned char utByte;
typedef void * utContext;
typedef void * utHandle;

typedef void (*utFilterDataCallback)(utHandle hFilter,long iTuner,utPid pid,utTid tid,utByte *data,long datasize,utContext context);
typedef void (*utFilterDestoryNotify)(utHandle hFilter,utContext context);

typedef struct tagTsDataFilter
{
	long iTuner;
	long pid;
} utTsDataFilter;
typedef std::vector<utTsDataFilter> TSDataFilterListT;

typedef struct tagSectionFilter
{
	long iTuner;
	unsigned short pid;
	unsigned char tid; // 0xff - filterData and filterMask is valid
	unsigned char filterData[8];
	unsigned char filterMask[8];
	unsigned long timeout;
} utSectionFilter;
typedef std::vector<utSectionFilter> SectionFilterListT;

#endif //defined(JOYSEE_TSDEMUX_DEF_H_)
