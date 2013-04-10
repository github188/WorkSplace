#ifndef DEMUXFILTER_INF_H_
#define DEMUXFILTER_INF_H_

#ifdef __cplusplus
extern "C"
{
#endif

struct IDemuxFilter{
	virtual void OnTSData(long iTuner,long iPid,unsigned char *pData,long iLength)=0;
	virtual void OnSectionData(long iTuner,long iPid,long tid,unsigned char *pData,long iLength)=0;
	virtual ~IDemuxFilter(){};
};
#ifdef __cplusplus
}
#endif

#endif //defined(DEMUXFILTER_INF_H_)