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
    NitServiceListT sServList;		// һ������
};
typedef std::vector<NitInfoT> NitInfoListT;

typedef struct SdtInfoT{
    SdtInfoT():service_type(0),sid(0),ts_id(0),net_id(0),ca_mode(0),name(""),provider_name(""){}
    U8              service_type;
    U16             sid;
    U16             ts_id;
    U16             net_id;
    U8              ca_mode; //0: ������, 1:����. �ൺ�汾(˽��sdt)�����˴��ֽ�
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
		// set ����
		////////////////////////////////////////	
        virtual bool InitTVSearch(int searchMode);			// ����ʱ������һ��
        void SetTuningParam(TuningParam tparam);	
        void SetCatSdtTableState(bool v){dvbstatus_.cat_ok=v;dvbstatus_.sdta_ok=v;} // ��search core ���ò���������search ����
        void SetAreaInfo(const AreaInfo areainfo);
        
		////////////////////////////////////////	
		// get ����
		////////////////////////////////////////	
        int GetVersion(IN const STVMode iMode,OUT BYTE* pNitVersion, OUT BYTE *pBatVersion);
        Table_Status GetCatSdtTableState(){
            if(dvbstatus_.cat_ok && dvbstatus_.sdta_ok) return Cat_Sdt_Table_ok;
            if(dvbstatus_.cat_ok ) return Cat_Table_ok;
            if(dvbstatus_.sdta_ok) return Sdt_Table_ok;
            return Cat_Sdt_Table_Not_ok;
        }
        bool CatSdtMissing(){return true;}        // ����������Ҫ��Ϊtrue, Ŀǰ��Ĭ�����ã�����Cat��ʧ

        int GetTableFilters(OUT vector<SECFilter>& tFilters,BuildStep bdStep);
        int GetTuningParamformNit(OUT vector<TuningParam>& TuningParamList);
        int GetDVBExtServTypeTable(OUT vector<ServiceTypeTableItem>& table);
		
		
		////////////////////////////////////////	
		// ��������
		////////////////////////////////////////	
        ePutDataRT AnalyseSectionData(U16 pid, U8 const *pData,U32 iDataLen);

		////////////////////////////////////////	
		// �麯������		
		////////////////////////////////////////	
        virtual int GetDVBServices(OUT vector<DVBService>& services)=0;
        virtual int GetDVBALLService(OUT vector<DVBService>& services); // ������ʵ�֣��������л�������
        virtual bool IsGoodBat(U8 const *pData,U32 iDataLen)=0;				// ����Ϊ˽�к���
        virtual bool IsGoodSdtA(U8 const *pData,U32 iDataLen);
		// ����/��ȡͨ��ģʽ
		virtual int SetParameter(int key, const void* request,int reqLength){
			UNUSED_PARAM(key),UNUSED_PARAM(request),UNUSED_PARAM(reqLength);
			return -1;
		}
		virtual int GetParameter(int key, void* reply,  int* replyLength){
			UNUSED_PARAM(key),UNUSED_PARAM(reply),UNUSED_PARAM(replyLength);
			return -1;
		}

    protected:

        void OneFreqSeachInit();					// ��Ƶ������ʱ����ʼ��������ֵ
        U32  bcd2d( U32 bcd );
        U8   U8ToQam( U8 m );

        bool ChkPmtTable();
        bool ChkServiceOK();						// �ж���Ƶ������Ƿ����(RT_SRVICE_OK)
        bool IsExistPmtPidInPat(U16 pmtPid);		//����pat map �����Ƿ���ڸ�PmtPid
        bool IsExistProgNumberInPAT(const U32 iSID);	// ����pat map �����Ƿ���ڸý�Ŀ��(service ID)

        bool IsGoodPat(U8 const *pData,U32 iDataLen);
        bool IsGoodPmt(U8 const *pData,U32 iDataLen);
        bool IsGoodCat(U8 const *pData,U32 iDataLen);
        bool IsGoodNitA(U8 const *pData,U32 iDataLen);
        bool IsGoodNitO(U8 const *pData,U32 iDataLen){UNUSED_PARAM(pData);UNUSED_PARAM(iDataLen);return true;}
        bool IsGoodSdtO(U8 const *pData,U32 iDataLen);
/*
        bool IsNewPatGroup(U16 pid , U8 const *pData,U32 iLen); // �ú���δ��
        bool FindFreqInfoFromNit(U16 iSID,TuningParam& sTuning); // �ú���δ��
        U32  GetPATCRC(U8 const *pData,U32 iLen);	//�ú���δ��
*/		
		
		////////////////////////////////////////	
		// ����ֵ
		////////////////////////////////////////	
        AreaInfo             areaparam_;				// Ŀǰ��û����
        U16                  m_iSystemID;               // for search
        U8                   m_iNitVersion;             ///< NIT�汾      
        U8                   m_iBatVersion;             ///< Bat �汾      
 //       U32                  m_iLastPATCRC;             ///< ��һ��PAT ���CRC      
        STVMode              mode_;                     ///< ��ǰ����ģʽ      
        bool                 m_bUpdateFilter;           ///< ��pat,cat ������ɺ���Ҫ���¹�����Ϊpmt, �Ѹ��±�־
        bool                 m_bNitToFull;              ///< ��nit ��������ɣ���Ҫ�л����ѵ���ȫƵ�����������л���־

        DVBParam             dvbstatus_;                 ///< ��ǰƵ�����״̬
        
        mapPatParamT         patparam_;                 ///< PAT��������(��Ŀ�б�)  
        CatParam             catparam_;                 ///< CAT��������      
        vector<BatParam>     vecBatParam_;                 ///< BAT��������  
        mapSdtInfoT          sdta_info_;                ///< SDTa��������
        mapPmtInfoT          pmt_info_;                 ///< ����PMT��������
        NitInfoListT         nit_info_;                 ///< NIT��������
        vector<TuningParam>  tuningparam_;              ///< NIT�������(Ƶ���б�)

        mapDVBServiceT       dvbOneFreqService_;        ///< ��Ƶ���Ŀ��Ϣ(chanel_number ---service)
        vector<DVBService>   dvballservice_;            ///< ȫƵ���Ŀ��Ϣ

        PSIConstraintT       m_patTableSectionNumber;   ///< У��PAT��������
        PSIConstraintT       m_sSdtATable;              ///< У��SDT��������(��ǰ��)
        PSIConstraintT       m_sNitATable;              ///< У��NIT��������
        PSIConstraintT       m_sCatTable;               ///< У��cat��������
        ConstraintFactorT    m_sSdtOTable;              ///< У��SDT��������(������)
        PSIConstraintT       m_sBatTable;               ///< У��bat��������

		////////////////////////////////////////	
		// ��Ա��̬����
		////////////////////////////////////////	
        static    bool DVBServiceSortByChNo(const DVBService& one,const DVBService& two);
        static    bool DVBServiceSortByServceID(const DVBService& one,const DVBService& two);

};
