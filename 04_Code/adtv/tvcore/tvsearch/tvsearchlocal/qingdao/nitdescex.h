#ifndef NOVELSUPER_PSISIPARSER_NITDESCEX_H
#define NOVELSUPER_PSISIPARSER_NITDESCEX_H


#include "imp/pre.h"



namespace novelsuper
{
	namespace psisi_parse 
	{

		struct DescloopBouquetList
		{
			U16 bouquetid() const{return  n2h16(bouquetID);}
			U8 regionid() const{return RegionID[1];}
			U8 classid() const{return classID[1];}
			U32 countrycode() const{return (country_code[0]*256 + country_code[1])*256 +country_code[2];}
			U8 length() const{ return 7; }
		private:
			U8 bouquetID[2];
			U8 RegionID[1];
			U8 classID[1];
			U8 country_code[3];
		};

		struct DescBouquetList : Descriptor<0xB4>
		{
			NOVEL_TF_PSISI_PARSE_GENERAL_LOOP_ITERATOR_BEGIN_END(DescloopBouquetList)
		protected:
			U16 loop_len() const{ return length(); }                     
			U8 const* loop_address() const{ return data(); }

		};
		typedef DescList_Iterator<DescBouquetList> BouquetList_I;

	}
}
#endif
