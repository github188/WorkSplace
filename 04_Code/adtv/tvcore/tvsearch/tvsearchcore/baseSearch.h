#ifndef __NOVELSUPERTV_SEARCH_H__
#define __NOVELSUPERTV_SEARCH_H__

#include "basesearchmgr.h"
#include "tsdemux.h"

struct BaseSearcher
{
	BaseSearcher(utSectionFilter const &filter,BaseSearchMgr *pSearchMgr);

	virtual ~BaseSearcher()
	{
		DelSectionFilter();
	}

	U16 GetPID(){
		return utSecfilter_.pid;
	}

	U8 GetTableId(){
		return utSecfilter_.tid;
	}
	void SetCanDelete(bool data)
	{
		m_bCanDelete = data;
	}
	bool GetCanDelete(){return m_bCanDelete;}
protected:
	static void DemuxCallBack( utHandle hFilter,long iTuner,utPid pid,utTid tid,utByte *data,long datasize,utContext context);
	static void DelSectionFilterCallBack(utHandle hFilter,utContext context);
	virtual bool SetSection( U8 const* buffer, U32 size );
private:
	
	utSectionFilter	utSecfilter_;
	HANDLE			hFilter_;
	BaseSearchMgr	*m_pBaseSearchMgr;
	bool			m_bCanDelete;
	
	void AddSectionFilter();
	void DelSectionFilter();
	
};

#endif // defined(__NOVELSUPERTV_SEARCH_H__)
