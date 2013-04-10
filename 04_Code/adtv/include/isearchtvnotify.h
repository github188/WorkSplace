// ���� for vim
#ifndef NOVELSUPERTV_ISEARCHTVNOTIFY_DEFINE_H
#define NOVELSUPERTV_ISEARCHTVNOTIFY_DEFINE_H

#include "tvcomm.h"
//#include "searchDef.h"

// ����Ƶ���ص��ӿ�
class ISearchTVNotify
{
  public:
	virtual ~ISearchTVNotify(){};
    // ��Ƶ���������
	virtual void OnDVBService(std::vector<DVBService> &services)=0;
	// ȫƵ����ʱ�Ľ����� 
    virtual void OnProgress(U32 iPercent)=0;
	// ȫƵ����ʱ��Ƶ����Ϣ(��ʼ������Ƶ�㼰�ź�ǿ�ȵ�)
	virtual void OnTunerInfo(const TuningParam& tuning,const TunerSignal& signal)=0;
	// Ƶ���������֪ͨ
	virtual void OnSearchTVComplete(std::vector<DVBService> &services, std::vector<ServiceTypeTableItem> &table)=0;
	// ��Ŀ�������֪ͨ
	virtual void OnSEPGComplete()=0;
	// ��Ƶ��NIT�汾����֪ͨ
	virtual void OnNitVersionChanged(U8 iVersion)=0;
};


#endif  // defined(NOVELSUPERTV_ISEARCHTVNOTIFY_DEFINE_H)
