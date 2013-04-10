#ifndef SECTIONFILTER_H_
#define SECTIONFILTER_H_

#include <vector>
//#include "tvcomm.h"
#include "tsdemux_def.h"

struct SectionFilterItem
{
	bool enable;
	utSectionFilter	filter;
};

struct SectionFilter
{
	SectionFilter();

	~SectionFilter();

	void process_section(long iTuner,utPid pid,utByte *SecData,long SecDataSize);
	bool matchFilter(utByte *filter,utByte *mask,utByte *SecData,long SecDataLen);

	// bDataType = false(Section),true(ts packet)
	void process(long iTuner,utPid pid,utByte *pData,long lLength,bool bDataType = false);
	void checkTimeOut();
	
private:
	::std::vector<SectionFilterItem> filters_;
	utFilterDataCallback handle_;
	utFilterDestoryNotify destoryCallback_;
	utContext *context_;
	unsigned long beginTick_;
    bool  valid_;
	bool  bTSOrSection;
	friend struct TsCatcher;
};

#endif //defined(SECTIONFILTER_H_)