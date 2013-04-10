#pragma once
#include "searchDef.h"
#include <all.h> /* parse TS */

using namespace std;

// TDT  table_id == 0x70*/
// TOT  table_id == 0x73*/
// RST  table_id == 0x71*/
// NIT  table_id ==  0x40 -0x41  
// CAT  table_id ==  0x01
// PMT  table_id ==  0x02
// BAT  table_id ==  0x4a
// SDT  table_id ==  0x42 - 0x46

#define INVALID_EVENT_SERVICEID (0xffff)
#define INVALID_EVENT_ID        (0xffff)

struct ConstraintFactorT{
    public:
        ConstraintFactorT():iBeginSectionNumber(-1),
        iBeginLastSectionNumber(-1),iBeginServiceId(INVALID_EVENT_SERVICEID){}

        void Reset(){
            iBeginSectionNumber = -1 ;
            iBeginLastSectionNumber = -1;
            iBeginServiceId = INVALID_EVENT_SERVICEID;
        }

        S16 iBeginSectionNumber;
        S16 iBeginLastSectionNumber;
        U16 iBeginServiceId;
};
typedef ::std::map<U16,U16> PSIConstraintT;

struct NitServDescT{
    U8  service_type;
    U16 service_id;
};
typedef std::vector<NitServDescT> NitServiceListT;

struct NitInfoT{
    NitInfoT(){
        iTsid = iOnid = 0;
        iFreq = iSymb = iQam = 0;
        network_name = "";
        sServList.clear();
    }
    void clear(){
        iTsid = iOnid = 0;
        iFreq = iSymb = iQam = 0;
        network_name.clear();
        sServList.clear();
    }
    U16             iTsid;
    U16             iOnid;        // original network id
    U32             iFreq;
    U32             iSymb;
    U32             iQam;
    std::string     network_name;
    NitServiceListT sServList;		// 一个集合
};
typedef std::vector<NitInfoT> NitInfoListT;

typedef struct SdtInfoT{
    SdtInfoT():service_type(0),sid(0),ts_id(0),net_id(0),ca_mode(0),name(""),provider_name(""){}
    U8              service_type;
    U16             sid;
    U16             ts_id;
    U16             net_id;
    U8              ca_mode; //0: 不加密, 1:加密. 青岛版本(私有sdt)加上了此字节
    std::string     name;
    std::string     provider_name;
}SdtInfoT;
typedef map<U16,SdtInfoT> mapSdtInfoT;

typedef struct PmtInfoT
{
    U8                audio_index;
    U16               pcr_pid;
    DVBStream         video_stream;
    DVBStream         audio_stream[AUDIOSTREAM_MAXCOUNT];
}PmtInfoT;
typedef map<U16,PmtInfoT> mapPmtInfoT;


class ParseBase
{
    public:
        ParseBase(void);
        virtual ~ParseBase(void);
		////////////////////////////////////////	
		// set 部分
		////////////////////////////////////////	
        virtual bool InitTVSearch(int searchMode);			// 搜索时，调用一次
        void SetTuningParam(TuningParam tparam);	
        void SetCatSdtTableState(bool v){dvbstatus_.cat_ok=v;dvbstatus_.sdta_ok=v;} // 由search core 设置参数，控制search 流程
        void SetAreaInfo(const AreaInfo areainfo);
        
		////////////////////////////////////////	
		// get 部分
		////////////////////////////////////////	
        int GetVersion(IN const STVMode iMode,OUT BYTE* pNitVersion, OUT BYTE *pBatVersion);
        Table_Status GetCatSdtTableState(){
            if(dvbstatus_.cat_ok && dvbstatus_.sdta_ok) return Cat_Sdt_Table_ok;
            if(dvbstatus_.cat_ok ) return Cat_Table_ok;
            if(dvbstatus_.sdta_ok) return Sdt_Table_ok;
            return Cat_Sdt_Table_Not_ok;
        }
        bool CatSdtMissing(){return true;}        // 央视内网需要设为true, 目前我默认设置，允许Cat丢失

        int GetTableFilters(OUT vector<SECFilter>& tFilters,BuildStep bdStep);
        int GetTuningParamformNit(OUT vector<TuningParam>& TuningParamList);
        int GetDVBExtServTypeTable(OUT vector<ServiceTypeTableItem>& table);
		
		
		////////////////////////////////////////	
		// 分析部分
		////////////////////////////////////////	
        ePutDataRT AnalyseSectionData(U16 pid, U8 const *pData,U32 iDataLen);

		////////////////////////////////////////	
		// 虚函数部分		
		////////////////////////////////////////	
        virtual int GetDVBServices(OUT vector<DVBService>& services)=0;
        virtual int GetDVBALLService(OUT vector<DVBService>& services); // 由子类实现，但基类有基本代码
        virtual bool IsGoodBat(U8 const *pData,U32 iDataLen)=0;				// 可设为私有函数
        virtual bool IsGoodSdtA(U8 const *pData,U32 iDataLen);
		// 设置/获取通用模式
		virtual int SetParameter(int key, const void* request,int reqLength){
			UNUSED_PARAM(key),UNUSED_PARAM(request),UNUSED_PARAM(reqLength);
			return -1;
		}
		virtual int GetParameter(int key, void* reply,  int* replyLength){
			UNUSED_PARAM(key),UNUSED_PARAM(reply),UNUSED_PARAM(replyLength);
			return -1;
		}

    protected:

        void OneFreqSeachInit();					// 新频点搜索时，初始化各变量值
        U32  bcd2d( U32 bcd );
        U8   U8ToQam( U8 m );

        bool ChkPmtTable();
        bool ChkServiceOK();						// 判定该频点服务是否完成(RT_SRVICE_OK)
        bool IsExistPmtPidInPat(U16 pmtPid);		//查找pat map 表中是否存在该PmtPid
        bool IsExistProgNumberInPAT(const U32 iSID);	// 查找pat map 表中是否存在该节目号(service ID)

        bool IsGoodPat(U8 const *pData,U32 iDataLen);
        bool IsGoodPmt(U8 const *pData,U32 iDataLen);
        bool IsGoodCat(U8 const *pData,U32 iDataLen);
        bool IsGoodNitA(U8 const *pData,U32 iDataLen);
        bool IsGoodNitO(U8 const *pData,U32 iDataLen){UNUSED_PARAM(pData);UNUSED_PARAM(iDataLen);return true;}
        bool IsGoodSdtO(U8 const *pData,U32 iDataLen);
/*
        bool IsNewPatGroup(U16 pid , U8 const *pData,U32 iLen); // 该函数未用
        bool FindFreqInfoFromNit(U16 iSID,TuningParam& sTuning); // 该函数未用
        U32  GetPATCRC(U8 const *pData,U32 iLen);	//该函数未用
*/		
		
		////////////////////////////////////////	
		// 属性值
		////////////////////////////////////////	
        AreaInfo             areaparam_;				// 目前还没有用
        U16                  m_iSystemID;               // for search
        U8                   m_iNitVersion;             ///< NIT版本      
        U8                   m_iBatVersion;             ///< Bat 版本      
 //       U32                  m_iLastPATCRC;             ///< 上一个PAT 表的CRC      
        STVMode              mode_;                     ///< 当前搜索模式      
        bool                 m_bUpdateFilter;           ///< 当pat,cat 搜索完成后，需要更新过滤器为pmt, 已更新标志
        bool                 m_bNitToFull;              ///< 当nit 表搜索完成，需要切换到搜到的全频点搜索，已切换标志

        DVBParam             dvbstatus_;                 ///< 当前频点分析状态
        
        mapPatParamT         patparam_;                 ///< PAT表分析结果(节目列表)  
        CatParam             catparam_;                 ///< CAT表分析结果      
        vector<BatParam>     vecBatParam_;                 ///< BAT表分析结果  
        mapSdtInfoT          sdta_info_;                ///< SDTa表分析结果
        mapPmtInfoT          pmt_info_;                 ///< 所有PMT表分析结果
        NitInfoListT         nit_info_;                 ///< NIT表分析结果
        vector<TuningParam>  tuningparam_;              ///< NIT分析结果(频点列表)

        mapDVBServiceT       dvbOneFreqService_;        ///< 单频点节目信息(chanel_number ---service)
        vector<DVBService>   dvballservice_;            ///< 全频点节目信息

        PSIConstraintT       m_patTableSectionNumber;   ///< 校验PAT表完整性
        PSIConstraintT       m_sSdtATable;              ///< 校验SDT表完整性(当前表)
        PSIConstraintT       m_sNitATable;              ///< 校验NIT表完整性
        PSIConstraintT       m_sCatTable;               ///< 校验cat表完整性
        ConstraintFactorT    m_sSdtOTable;              ///< 校验SDT表完整性(其他表)
        PSIConstraintT       m_sBatTable;               ///< 校验bat表完整性

		////////////////////////////////////////	
		// 成员静态函数
		////////////////////////////////////////	
        static    bool DVBServiceSortByChNo(const DVBService& one,const DVBService& two);
        static    bool DVBServiceSortByServceID(const DVBService& one,const DVBService& two);

};
