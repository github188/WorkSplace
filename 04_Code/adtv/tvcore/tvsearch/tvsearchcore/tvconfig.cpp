#include "typ.h"
// #include <stdio.h>
#include "tvconfig.h"
//#include <io.h>
//#include <direct.h>
// #include <sys/stat.h>
// #include <sys/types.h>
// #include <tchar.h>
// #include <netepg_inf.h>
// #include <Windows.h>

// #pragma warning(disable : 4996)
using namespace std;

#undef _stat

/// 从Programs.xml读取节目信息列表
bool ReadDVBServiceFromXML(const char *strName,std::vector<DVBService> &services)
{
	bool bRet = false;
	FILE *file = NULL;

	if(strName  == NULL|| strName[0] == '\0')
		return bRet;

	file = fopen(strName,"r+b");
	if(NULL != file)
	{
		size_t length = GetFileLength(file);
		char *pXMLBuffer = new char[length];
		if(NULL != pXMLBuffer)
		{
			if(length == fread(pXMLBuffer,sizeof(char),length,file))
			{
				MyParser parser;
				ParseXMLData(pXMLBuffer,(int)length,parser);
				if( !parser.sucess())
				{
					goto FREE_XMLBUFFER ;
				}
				if(!parser.root_node() || !(parser.root_node())->first_child_node_ptr)
				{
					goto FREE_XMLBUFFER ;
				}

				XmlNode *pDVBServices = parser.root_node()->first_child_node_ptr;
				if(pDVBServices == NULL )
				{
					goto FREE_XMLBUFFER ;
				}

				XmlNodeList listService(pDVBServices->first_child_node_ptr);
				XmlNode *pService = listService.first();
				DVBService one_;
				while(NULL != pService)
				{
					memset(&one_,0,sizeof(DVBService));
					LoadService(pService,one_);
					services.push_back(one_);
					pService = listService.next();
				}	

			}

FREE_XMLBUFFER:
			delete[] pXMLBuffer;
			pXMLBuffer = NULL;
		}

		fclose(file);
		bRet = true;
	}

	return bRet;
}


bool ReadDVBServiceFromXML1(const char *strName,mapDVBServiceT& services)
{
	bool bRet = false;
	FILE *file = NULL;

	if(strName == NULL|| strName[0] == '\0')
		return bRet;

	file = fopen(strName,"r+b");
	if(NULL != file)
	{
		size_t length = GetFileLength(file);
		char *pXMLBuffer = new char[length];
		if(NULL != pXMLBuffer)
		{
			if(length == fread(pXMLBuffer,sizeof(char),length,file))
			{
				MyParser parser;
				ParseXMLData(pXMLBuffer,(int)length,parser);
				if( !parser.sucess())
				{
					goto FREE_XMLBUFFER;
				}
				if(!parser.root_node() || !(parser.root_node())->first_child_node_ptr)
				{
					goto FREE_XMLBUFFER;
				}

				XmlNode *pDVBServices = parser.root_node()->first_child_node_ptr;
				if(pDVBServices == NULL )
				{
					goto FREE_XMLBUFFER;
				}

				XmlNodeList listService(pDVBServices->first_child_node_ptr);
				XmlNode *pService = listService.first();
				DVBService one_;
				while(NULL != pService)
				{
					memset(&one_,0,sizeof(DVBService));
					LoadService(pService,one_);
					//services.push_back(one_);
					services[one_.channel_number] = one_;
					pService = listService.next();
				}	

			}

FREE_XMLBUFFER:
			delete[] pXMLBuffer;
			pXMLBuffer = NULL;
		}

		fclose(file);
		bRet = true;
	}

	return bRet;
}
/// 保存节目信息列表至Programs.xml文件 
bool WriteDVBServiceToXML(const char *strName,std::vector<DVBService> &services)
{
	bool bRet = false;
	FILE *file = NULL;

	if(services.empty() || (strName ==NULL ) || (strName[0] == '\0'))
		return bRet;

	file = fopen(strName,"w+b");
	if(NULL != file)
	{
		fprintf(file,"<?xml version=\"1.0\" encoding=\"gb2312\"?>\n");
		fprintf(file,"<STBUXML>\n");
		fprintf(file,"\t<DVBServices>\n");		

		for (int i = 0 ; i < (int)services.size() ; i++)
		{
			fprintf(file,"\t\t<Service name=\"%s\" serviceID=\"%d\" channel_number=\"%d\" service_type=\"%d\" category=\"%d\" pcr_pid=\"%d\" pmt_pid=\"%d\">\n",
				services[i].name,services[i].serviceID,services[i].channel_number,services[i].service_type,services[i].category,services[i].pcr_pid,services[i].pmt_id);

			fprintf(file,"\t\t\t<volume volume_ratio=\"%d\" volume_reserve=\"%d\" />\n",services[i].volume_ratio,services[i].volume_reserve);
			
			/// node:audio_streams
			fprintf(file,"\t\t\t<audio_streams audio_channel_set=\"%d\" audio_format=\"%d\" audio_index=\"%d\">\n",
				services[i].audio_channel_set,services[i].audio_format,services[i].audio_index);
			int iAudioStreams = sizeof(services[i].audio_stream)/sizeof(DVBStream);
			for(int j = 0 ; j < iAudioStreams ; j++)
			{
				fprintf(file,"\t\t\t\t<audio_stream stream_type=\"%d\" stream_pid=\"%d\" ecm_pid=\"%d\" name=\"%s\" />\n",
					services[i].audio_stream[j].stream_type,
					services[i].audio_stream[j].stream_pid,
					services[i].audio_stream[j].ecm_pid,
					services[i].audio_stream[j].name);
			}
			fprintf(file,"\t\t\t</audio_streams>\n");

			/// node:video_stream
			fprintf(file,"\t\t\t<video_stream stream_type=\"%d\" stream_pid=\"%d\" ecm_pid=\"%d\" />\n",
				services[i].video_stream.stream_type,
				services[i].video_stream.stream_pid,
				services[i].video_stream.ecm_pid);

			/// node:ts
			fprintf(file,"\t\t\t<ts tserviceID=\"%d\" netid=\"%d\" freq=\"%d\" symb=\"%d\" qam=\"%d\" />\n",
				services[i].ts.ts_id,services[i].ts.net_id,services[i].ts.tuning_param.freq,
				services[i].ts.tuning_param.symb,services[i].ts.tuning_param.qam);
			
			fprintf(file,"\t\t</Service>\n");
		}

		fprintf(file,"\t</DVBServices>\n");		
		fprintf(file,"</STBUXML>\n");
		fclose(file);
	}

	return bRet ; 
}

bool WriteDVBServiceToXML1(const char *strName,mapDVBServiceT& services)
{
	bool bRet = false;
	FILE *file = NULL;

	if(services.empty() || (strName ==NULL ) || (strName[0] == '\0'))
		return bRet;

	file = fopen(strName,"w+b");
	if(NULL != file)
	{
		fprintf(file,"<?xml version=\"1.0\" encoding=\"gb2312\"?>\n");
		fprintf(file,"<STBUXML>\n");
		fprintf(file,"\t<DVBServices>\n");		

		mapDVBServiceT::iterator it = services.begin();
		for (; it != services.end() ; it++)
		{
			fprintf(file,"\t\t<Service name=\"%s\" serviceID=\"%d\" channel_number=\"%d\" service_type=\"%d\" category=\"%d\" pcr_pid=\"%d\" pmt_pid=\"%d\">\n",
				it->second.name,it->second.serviceID,it->second.channel_number,it->second.service_type,it->second.category,it->second.pcr_pid,it->second.pmt_id);
			
			fprintf(file,"\t\t\t<volume volume_ratio=\"%d\" volume_reserve=\"%d\" />\n",it->second.volume_ratio,it->second.volume_reserve);

			/// node:audio_streams
			fprintf(file,"\t\t\t<audio_streams audio_channel_set=\"%d\" audio_format=\"%d\" audio_index=\"%d\">\n",
				it->second.audio_channel_set,it->second.audio_format,it->second.audio_index);
			int iAudioStreams = sizeof(it->second.audio_stream)/sizeof(DVBStream);
			for(int j = 0 ; j < iAudioStreams ; j++)
			{
				fprintf(file,"\t\t\t\t<audio_stream stream_type=\"%d\" stream_pid=\"%d\" ecm_pid=\"%d\" name=\"%s\" />\n",
					it->second.audio_stream[j].stream_type,
					it->second.audio_stream[j].stream_pid,
					it->second.audio_stream[j].ecm_pid,
					it->second.audio_stream[j].name);
			}
			fprintf(file,"\t\t\t</audio_streams>\n");

			/// node:video_stream
			fprintf(file,"\t\t\t<video_stream stream_type=\"%d\" stream_pid=\"%d\" ecm_pid=\"%d\" />\n",
				it->second.video_stream.stream_type,
				it->second.video_stream.stream_pid,
				it->second.video_stream.ecm_pid);

			/// node:ts
			fprintf(file,"\t\t\t<ts tsid=\"%d\" netid=\"%d\" freq=\"%d\" symb=\"%d\" qam=\"%d\" />\n",
				it->second.ts.ts_id,it->second.ts.net_id,it->second.ts.tuning_param.freq,
				it->second.ts.tuning_param.symb,it->second.ts.tuning_param.qam);
			
			fprintf(file,"\t\t</Service>\n");
		}

		fprintf(file,"\t</DVBServices>\n");		
		fprintf(file,"</STBUXML>\n");
		fclose(file);

		bRet = true ;
	}

	return bRet ; 
}

///  计算文件大小
long GetFileLength( FILE *fp )
{
	long len = NS__filelength(0);
	return len ;
}


/// 映射XMLNode至DVBService结构
void LoadService(const XmlNode *pService,DVBService &service)
{
	// 'Service'节点属性:
	for (int i = 0 ; i < (int)pService->properties.size() ; i++ )
	{
		if(pService->properties[i].name.compare("name") == 0 )
		{
			strcpy(service.name,pService->properties[i].value.c_str());
		}
		else if(pService->properties[i].name.compare("serviceID") == 0 )
		{
			service.serviceID = atoi(pService->properties[i].value.c_str());
		}
		else if(pService->properties[i].name.compare("channel_number") == 0 )
		{
			service.channel_number = atoi(pService->properties[i].value.c_str());
		}
		else if(pService->properties[i].name.compare("service_type") == 0 )
		{
			service.service_type = atoi(pService->properties[i].value.c_str());
		}
		else if(pService->properties[i].name.compare("pmt_pid") == 0 )
		{
			service.pmt_id = atoi(pService->properties[i].value.c_str());
		}
		else if(pService->properties[i].name.compare("category") == 0 )
		{
			service.category = atoi(pService->properties[i].value.c_str());
		}
		else if(pService->properties[i].name.compare("pcr_pid") == 0 )
		{
			service.pcr_pid = atoi(pService->properties[i].value.c_str());
		}
	}

	// 'Service' 子节点:
	XmlNode *pNode = pService->first_child_node_ptr;
	while(NULL != pNode )
	{
		if(pNode->name.compare("volume") == 0 )
		{
			for (int i = 0 ; i < (int)pNode->properties.size() ; i++)
			{
				if(pNode->properties[i].name.compare("volume_ratio") == 0 )
				{
					service.volume_ratio = atoi(pNode->properties[i].value.c_str());
				}
				else if(pNode->properties[i].name.compare("volume_reserve") == 0 )
				{
					service.volume_reserve = atoi(pNode->properties[i].value.c_str());
				}
			}
		}
		else if(pNode->name.compare("audio_streams") == 0 )
		{
			// 该节点的属性:
			for (int i = 0 ; i < (int)pNode->properties.size() ; i++)
			{
				if(pNode->properties[i].name.compare("audio_channel_set") == 0 )
				{
					service.audio_channel_set = atoi(pNode->properties[i].value.c_str());
				}
				else if(pNode->properties[i].name.compare("audio_format") == 0 )
				{
					service.audio_format = atoi(pNode->properties[i].value.c_str());
				}
				else if(pNode->properties[i].name.compare("audio_index") == 0 )
				{
					service.audio_index = atoi(pNode->properties[i].value.c_str());
				}
			}

			// 该节点的子节点:
			int j = 0;
			XmlNode *pAudioStreamNode = pNode->first_child_node_ptr ;
			while(NULL != pAudioStreamNode)
			{
				for(int i = 0 ; i < (int)pAudioStreamNode->properties.size() ;i++)
				{
					if(pAudioStreamNode->properties[i].name.compare("stream_type") == 0 )
					{
						service.audio_stream[j].stream_type = atoi(pAudioStreamNode->properties[i].value.c_str());
					}
					else if (pAudioStreamNode->properties[i].name.compare("stream_pid") == 0 )
					{
						service.audio_stream[j].stream_pid = atoi(pAudioStreamNode->properties[i].value.c_str());
					}
					else if (pAudioStreamNode->properties[i].name.compare("ecm_pid") == 0 )
					{
						service.audio_stream[j].ecm_pid = atoi(pAudioStreamNode->properties[i].value.c_str());
					}
					else if (pAudioStreamNode->properties[i].name.compare("name") == 0 )
					{
						strcpy(service.audio_stream[j].name,pAudioStreamNode->properties[i].value.c_str());
					}
				}

				pAudioStreamNode = pAudioStreamNode->brother_node_ptr;
				j++;
			}
		}
		else if(pNode->name.compare("video_stream") == 0 )
		{
			for(int i = 0 ; i < (int)pNode->properties.size() ;i++)
			{
				if(pNode->properties[i].name.compare("stream_type") == 0 )
					service.video_stream.stream_type = atoi(pNode->properties[i].value.c_str());
				else if (pNode->properties[i].name.compare("stream_pid") == 0 )
					service.video_stream.stream_pid = atoi(pNode->properties[i].value.c_str());
				else if (pNode->properties[i].name.compare("ecm_pid") == 0 )
					service.video_stream.ecm_pid = atoi(pNode->properties[i].value.c_str());
			}

		}
		else if(pNode->name.compare("ts") == 0 )
		{
			for(int i = 0 ; i < (int)pNode->properties.size() ;i++)
			{
				if(pNode->properties[i].name.compare("tsid") == 0 )
				{
					service.ts.ts_id = atoi(pNode->properties[i].value.c_str());
				}
				else if(pNode->properties[i].name.compare("netid") == 0 )
				{
					service.ts.net_id = atoi(pNode->properties[i].value.c_str());
				}
				else if(pNode->properties[i].name.compare("freq") == 0 )
				{
					service.ts.tuning_param.freq = atoi(pNode->properties[i].value.c_str());
				}
				else if(pNode->properties[i].name.compare("symb") == 0 )
				{
					service.ts.tuning_param.symb = atoi(pNode->properties[i].value.c_str());
				}
				else if(pNode->properties[i].name.compare("qam") == 0 )
				{
					service.ts.tuning_param.qam = atoi(pNode->properties[i].value.c_str());
				}
			}
		}

		pNode = pNode->brother_node_ptr;
	}

}


bool ReadEPGInfoFromXML(EPGDataBaseT& epgInfos)
{
	bool bRet = false;
	struct _stat st;
	intptr_t   hFile; 
	struct _finddata_t c_file; 
	std::string strName =CFG_FILE_PATH;
	strName+= "/epgdata";

	U16 serviceID = 0;
	if(0 == NS__stat(strName.c_str(),&st))
	{
		std::string strFind = strName;
		strFind +="\\*.*";

		hFile = NS__findfirst(strFind.c_str(), &c_file); 
		while(NS__findnext(hFile, &c_file) == 0 ) 
		{
			std::string xmlFileName = strName;
			if(0 != strcmp(".",c_file.name) && 
				0 != strcmp("..",c_file.name))
			{
				sscanf(c_file.name,"%d.xml",(int *)&serviceID);
				xmlFileName += "\\";
				xmlFileName += c_file.name;
				ReadProgramEpgFromXML(xmlFileName.c_str(),epgInfos[serviceID]);
			}
			xmlFileName.clear();
		}

		NS__findclose(hFile); 
		bRet = true ;
	}

	return bRet;
}

bool ReadProgramEpgFromXML(const char* fname,ProgramEpg& one)
{
	bool bRet = false;

	FILE *file = fopen(fname,"r+b");
	if(NULL != file)
	{
		size_t length = GetFileLength(file);
		char *pXMLBuffer = new char[length];
		if(NULL != pXMLBuffer)
		{
			if(length == fread(pXMLBuffer,sizeof(char),length,file))
			{
				MyParser parser;
				ParseXMLData(pXMLBuffer,(int)length,parser);
				if( !parser.sucess())
				{
					goto FREE_XMLBUFFER;
				}
				XmlNode* pRoot = parser.root_node();
				if((NULL == pRoot) || (NULL== pRoot->first_child_node_ptr))
				{
					goto FREE_XMLBUFFER;
				}
				
				std::vector<Property>::const_iterator it = pRoot->properties.begin();
				for (; it != pRoot->properties.end() ; it++)
				{
					if(it->name.compare("id") == 0 )
					{
						one.sid = atoi(it->value.c_str());
					}
					else if(it->name.compare("event_ver") == 0 )
					{
						one.EventsVer = atoi(it->value.c_str());
					}
				}
				
				XmlNode* pProgramEpg = pRoot->first_child_node_ptr;
				while(NULL != pProgramEpg)
				{
					EpgEvent evtInfo;
					std::vector<Property>::iterator it = pProgramEpg->properties.begin();
					if(pProgramEpg->name.compare("event") == 0 )
					{
						for (; it != pProgramEpg->properties.end() ; it++)
						{

							if(it->name.compare("id") == 0 )
							{
								evtInfo.id = atoi(it->value.c_str());
							}
							else if(it->name.compare("name") == 0 )
							{
								evtInfo.name = it->value;
							}
							else if(it->name.compare("start_time") == 0 )
							{
								evtInfo.start_time = (Time)atoi(it->value.c_str());
							}
							else if(it->name.compare("end_time") == 0 )
							{
								evtInfo.end_time = (Time)atoi(it->value.c_str());
							}
						}

						evtInfo.description = pProgramEpg->value; 
					}
					
					one.events.insert(evtInfo);
					pProgramEpg = pProgramEpg->brother_node_ptr;
				}
			}

FREE_XMLBUFFER:		
			delete[] pXMLBuffer;
			pXMLBuffer = NULL;
		}

		bRet = true ;
		fclose(file);
	}
	
	return bRet ;
}
#if 0
// 加载订阅信息
bool ReadSubscriptionFromXML(mapSubscriptionsT& subscription)
{
	bool bRet = false;
	FILE *file = NULL;
	std::string fname_ = CFG_FILE_PATH;
	fname_ += "subscription.xml";

	file = fopen(fname_.c_str(),"r+b");
	if(NULL != file)
	{
		size_t length = GetFileLength(file);
		char *pXMLBuffer = new char[length];
		if(NULL != pXMLBuffer)
		{
			if(length == fread(pXMLBuffer,sizeof(char),length,file))
			{
				MyParser parser;
				ParseXMLData(pXMLBuffer,(int)length,parser);
				if( !parser.sucess())
				{
					goto FREE_XMLBUFFER;
				}
				if(!parser.root_node() || !(parser.root_node())->first_child_node_ptr)
				{
					goto FREE_XMLBUFFER;
				}

				XmlNode *pSubscription = parser.root_node()->first_child_node_ptr;
				while(NULL != pSubscription)
				{
					TVSubscriptionT one_;
					memset(&one_,0,sizeof(one_));
					std::vector<Property>::iterator it = pSubscription->properties.begin();
					for (; it != pSubscription->properties.end() ; it++)
					{
						if(it->name.compare("id") == 0 )
						{
							one_.id = atoi(it->value.c_str());
						}
						else if(it->name.compare("serviceID") == 0 )
						{
							one_.sid = atoi(it->value.c_str());
						}
						else if(it->name.compare("channel_number") == 0 )
						{
							one_.channel_number = atoi(it->value.c_str());
						}
						else if(it->name.compare("ename") == 0 )
						{
							strcpy(one_.ename,it->value.c_str());
						}
						else if(it->name.compare("sname") == 0 )
						{
							strcpy(one_.sname,it->value.c_str());
						}
						else if(it->name.compare("starttime") == 0 )
						{
							one_.iStartTime = atoi(it->value.c_str());
						}
						else if(it->name.compare("endtime") == 0 )
						{
							one_.iEndTime = atoi(it->value.c_str());
						}
					}
					
					subscription[one_.id] = one_;
					pSubscription = pSubscription->brother_node_ptr;
				}

			}

FREE_XMLBUFFER:
			delete[] pXMLBuffer;
			pXMLBuffer = NULL;
		}

		fclose(file);
		bRet = true;
	}

	return bRet;
}

bool WriteEPGInfoToXML(const EPGDataBaseT *pEpgInfos)
{
	bool bRet = false;
	FILE *file = NULL;

	if((NULL == pEpgInfos) || (pEpgInfos->empty()))
	{
		return bRet;
	}

	std::string fname_;
	std::string strName = CFG_FILE_PATH ;
	struct _stat buf;

	strName+= "/epgdata";
	
	// 判断目录是否存在
	if(0 != NS__stat(strName.c_str(),&buf))
	{
		// 创建默认目录
		if( 0 != NS__mkdir(strName.c_str()))
		{
			bRet = true ;
		}
	}

	if(!bRet)
	{
		char ids[10]={0};
		EPGDataBaseT::const_iterator it = pEpgInfos->begin();
		for (; it != pEpgInfos->end() ; it++)
		{
			fname_= strName;
			sprintf(ids,"\\%d.xml",it->first);
			fname_ += ids ; 
			WriteProgramEpgToXML(fname_.c_str(),it->second);
		}
		
		bRet = true ;
	}

	return bRet ; 
}

bool WriteProgramEpgToXML(const char* fname,const ProgramEpg& one)
{
	bool bRet = false;
	
	if(0 < one.events.size())
	{

		FILE *file = NULL;
		file = fopen(fname,"w+b");
		if(NULL != file)
		{
			fprintf(file,"<?xml version=\"1.0\" encoding=\"gb2312\"?>\n");	

			//fprintf(file,"<TSStreamEpgInfos>\n");
			fprintf(file,"<Service id=\"%d\" event_ver=\"%d\">\n",one.sid,one.EventsVer);
			EpgEventSet::const_iterator it = one.events.begin();
			for (; it != one.events.end() ; it++)
			{
				fprintf(file,"\t\t<event id=\"%d\" name=\"%s\" start_time=\"%d\" end_time=\"%d\">%s</event>\n",
					it->id,it->name.c_str(),it->start_time,it->end_time,it->description.c_str());
			}

			fprintf(file,"</Service>\n");
			//fprintf(file,"</TSStreamEpgInfos>\n");

			fclose(file);
			bRet = true ;
		}
	}

	return bRet ; 
}

// 保存订阅信息
bool WriteSubscriptionToXML(const mapSubscriptionsT *pSubscription)
{
	bool bRet = false;
	FILE *file = NULL;
	std::string fname_ = CFG_FILE_PATH;
	fname_ += "subscription.xml";
	
	if(NULL == pSubscription)
		return bRet;

	file = fopen(fname_.c_str(),"w+b");
	if(NULL != file)
	{
		fprintf(file,"<?xml version=\"1.0\" encoding=\"gb2312\"?>\n");
		fprintf(file,"<TVSubscriptions>\n");
		
		mapSubscriptionsT::const_iterator it = pSubscription->begin();
		for (; it != pSubscription->end() ; it++)
		{
			fprintf(file,"\t<TVSubscription id=\"%d\" serviceID=\"%d\" channel_number=\"%d\" ename=\"%s\" sname=\"%s\" starttime=\"%u\" />\n",
				it->first,it->second.sid,it->second.channel_number,
				it->second.ename,it->second.sname,it->second.iStartTime);
		}

		fprintf(file,"</TVSubscriptions>\n");
		fclose(file);
	}

	return bRet ; 
}

U8 ReadNITVersionFromCFG()
{
	string fname_ = CFG_FILE_PATH;
	fname_ += CFG_FILE_NAME;
	
	return NS_GetPrivateProfileInt("Service","NITVersion",0,fname_.c_str());
}

bool WriteNITVersionToCFG(const U8 iNitVersion)
{
	string fname_  = CFG_FILE_PATH;
	fname_ += CFG_FILE_NAME;

	char temp[32]={0};
	sprintf(temp,"%d",iNitVersion);

	BOOL bRet = NS_WritePrivateProfileString("Service","NITVersion",temp,fname_.c_str());
	return (bRet ? true : false);
}

U16  ReadCHLFromCFG()
{
	string fname_  = CFG_FILE_PATH;
	fname_ += CFG_FILE_NAME;
	return NS_GetPrivateProfileInt("Service","Chl",1,fname_.c_str());
}

bool WriteCHLToCFG(const U16 iChannelNumber)
{
	string fname_  = CFG_FILE_PATH;
	fname_ += CFG_FILE_NAME;

	char temp[32]={0};
	sprintf(temp,"%d",iChannelNumber);
	BOOL bRet = NS_WritePrivateProfileString("Service","Chl",temp,fname_.c_str());
	return (bRet ? true : false);
}

bool StorePictureRect(const MixerRectT& rc)
{
	bool bRet = true;
	string fname_  = CFG_FILE_PATH;
	fname_ += CFG_FILE_NAME;

	char temp[32]={0};
	sprintf(temp,"%d,%d,%d,%d",rc.left,rc.top,rc.right,rc.bottom);
	
	BOOL b = NS_WritePrivateProfileString("Service","VideoRect",temp,fname_.c_str());
	
	bRet = (TRUE == b) ? true : false;
	return bRet ;
}

bool FetchPictureRect(MixerRectT& rc)
{
	string fname_  = CFG_FILE_PATH;
	fname_ += CFG_FILE_NAME;

	char temp[32]={0};
	NS_GetPrivateProfileString("Service","VideoRect",NULL,temp,32,fname_.c_str());
	
	sscanf(temp,"%d,%d,%d,%d",&rc.left,&rc.top,&rc.right,&rc.bottom); 
	
	return true;
}

// 丢包率阈值
float ReadDropThreshold()
{
	string fname_  = CFG_FILE_PATH;
	fname_ += CFG_FILE_NAME;

	char temp[32]={0};
	NS_GetPrivateProfileString("Service","DropThreshold","0.5",temp,32,fname_.c_str());
	float value=0;
	sscanf(temp,"%f",&value);
	return value;
}

// 丢包率统计周期
U8	  ReadCheckCycle()
{
	string fname_  = CFG_FILE_PATH;
	fname_ += CFG_FILE_NAME;
	
	return NS_GetPrivateProfileInt("Service","AutoCheckCycle",10,fname_.c_str());
}

// 读写主频点信息
void  ReadBasicFreq(TuningParam& tunning)
{
	string fname_  = CFG_FILE_PATH;
	fname_ += CFG_FILE_NAME;

	char temp[32]={0};
	NS_GetPrivateProfileString("Service","MainFreq",NULL,temp,32,fname_.c_str());

	sscanf(temp,"%d,%d,%d",&tunning.freq,&tunning.symb,&tunning.qam);
}

bool  WriteBasicFreq(const TuningParam& tunning)
{
	bool bRet = true;
	string fname_  = CFG_FILE_PATH;
	fname_ += CFG_FILE_NAME;

	char temp[32]={0};
	sprintf(temp,"%d,%d,%d",tunning.freq,tunning.symb,tunning.qam);

	BOOL b = NS_WritePrivateProfileString("Service","MainFreq",temp,fname_.c_str());

	bRet = (TRUE == b) ? true : false;
	return bRet ;
}

void  ReadAudioAtcTimingAccuracy(int& iEnable,int& iAhead,int& iBehind)
{
	string fname_  = CFG_FILE_PATH;
	fname_ += CFG_FILE_NAME;

	char temp[32]={0};
	NS_GetPrivateProfileString("Service","ATCAccuracy",NULL,temp,32,fname_.c_str());

	sscanf(temp,"%d,%d,%d",&iEnable,&iAhead,&iBehind);
}
#endif
