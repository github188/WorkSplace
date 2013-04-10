#ifndef NTF_Y4_TSHELP_H_
#define NTF_Y4_TSHELP_H_


#ifdef __cplusplus
extern "C"
{
#endif


#define TS_SECTION_HEADER_SIZE	5
#define TS_SECTION_LEADER_SIZE	3
#define TS_SECTION_CRC_SIZE	4
#define MAX_TS_SECTION_LENGTH	(1024) // max secion length
#define MAX_TS_EITSEC_LENGTH	(4096) // max EIT section length
#define MAX_TS_SECTION_COUNT	(255)
#define MAX_PID_COUNT			(8192)

#define TS_PACKET_SIZE			(188)

#define TS_INVALID_VER			((unsigned char)(-1))
#define TS_INVALID_SID			((unsigned short)(-1))
#define TS_INVALID_PID			((unsigned short)(-1))
#define TS_INVALID_TBID			((unsigned char)(-1))

typedef unsigned short TSPID;	// pid
typedef unsigned short CASYSID;	// ca system id
typedef unsigned short TSSID;	// service id
typedef unsigned char TSTBID;	// table id


// pid define

#define PID_PAT			0x00
#define PID_CAT			0x01
#define PID_NIT			0x10
#define PID_SDT			0x11
#define PID_BAT			0x11
#define PID_EIT			0x12
#define PID_RST			0x13
#define PID_TDT			0x14
#define PID_TOT			0x14

// table id define

#define TID_PAT			0x00
#define TID_CAT			0x01
#define TID_PMT			0x02
#define TID_NIT_ACTUAL	0x40
#define TID_NIT_OTHER	0x41
#define TID_SDT_ACTUAL	0x42
#define TID_SDT_OTHER	0x46
#define TID_BAT			0x4a
#define TID_EIT_ACTUAL	0x4e
#define TID_EIT_OTHER	0x4f
#define TID_EITS_ACTUAL	0x50
#define TID_EITS_OTHER	0x60
#define TID_TDT			0x70
#define TID_RST			0x71
#define TID_TOT			0x73

// table define

#define SDT_SERVICE_HEADER_LENGTH	5
#define EIT_EVENT_HEADER_LENGTH		12


// ts packet help function

__inline unsigned short getTSInt16(unsigned char *p)
{
	return (p[0]<<8) | p[1];
}

// ****000000000000 
__inline unsigned short getTSInt12(unsigned char *p)
{
	return ((p[0]&0x0f)<<8) | p[1];
}
// ***0000000000000 
__inline unsigned short getTSInt13(unsigned char *p)
{
	return ((p[0]&0x1f)<<8) | p[1];
}

// section help function

__inline unsigned char getSecTableID(unsigned char *SecData)
{
	return SecData[0];
}
__inline unsigned short getSecLength(unsigned char *SecData)
{
	return getTSInt12(SecData+1);
}
__inline unsigned short getSecPrivateID(unsigned char *SecData)
{
	return getTSInt16(SecData+3);
}
__inline unsigned char getSecVersion(unsigned char *SecData)
{
	return (SecData[5]>>1) & 0x1F;
}
__inline unsigned char getSecLastNumber(unsigned char *SecData)
{
	return SecData[7];
}
__inline unsigned char getSecNumber(unsigned char *SecData)
{
	return SecData[6];
}
__inline unsigned char getSecPayloadLen(unsigned char *SecData)
{
	return getSecLength(SecData)-TS_SECTION_HEADER_SIZE-TS_SECTION_CRC_SIZE; 
}
__inline unsigned char *getSecPayload(unsigned char *SecData)
{
	return SecData+(TS_SECTION_HEADER_SIZE+TS_SECTION_LEADER_SIZE);
}
__inline unsigned char *getSecPayloadEnd(unsigned char *SecData)
{
	return SecData+(TS_SECTION_LEADER_SIZE+getSecLength(SecData)-TS_SECTION_CRC_SIZE);
}
__inline unsigned char *getSecEnd(unsigned char *SecData)
{
	return SecData+(TS_SECTION_LEADER_SIZE+getSecLength(SecData));
}

__inline unsigned long getSecCrc(unsigned char *SecData)
{
	unsigned char *crc=SecData+(getSecLength(SecData)-1);
	return ( (crc[0]<<24) | (crc[1]<<16) | (crc[2]<<8) | crc[3] );
}

__inline unsigned char IsSecPosValid(unsigned char *SecData,unsigned char *pos,unsigned long off)
{
	return pos>=SecData && (pos+off)<=getSecPayloadEnd(SecData);
}

// desc help function

__inline unsigned char getDescriptorTag(unsigned char *p)
{
	return p[0];
}
__inline unsigned char getDescriptorLength(unsigned char *p)
{
	return p[1];
}

__inline unsigned char *firstDescriptor(unsigned char *pos,unsigned short DescLen)
{
	if(0 == pos||DescLen==0)
		return 0;
	if(DescLen<2)
		return 0;
	if(2+getDescriptorLength(pos)>DescLen)
		return 0;
	return pos;
}

__inline unsigned char *nextDescriptor(unsigned char *pos,unsigned short *len)
{
	if(pos==0 || len==0)
		return 0;
	// check current descriptor length
	if((*len)<2)
		return 0;
	if(2+getDescriptorLength(pos)>(*len))
		return 0;
	*len-=2+getDescriptorLength(pos);
	pos+=2+getDescriptorLength(pos);
	// check next descriptor length
	if((*len)<2)
		return 0;
	if(2+getDescriptorLength(pos)>(*len))
		return 0;

	return pos;
}


// loop object

__inline unsigned char *Loop12FirstObject(unsigned char *begin,unsigned short size,unsigned short ObjectOff,unsigned short LeaderLen) 
{
	unsigned short ExtLen=0;
	if(begin==0)
		return 0;
	if(ObjectOff+LeaderLen>size)
		return 0;
	ExtLen=getTSInt12(begin+ObjectOff+LeaderLen-2);
	if(ObjectOff+LeaderLen+ExtLen>size)
		return 0;
	return begin+ObjectOff;
}
__inline unsigned char *Loop12NextObject(unsigned char *begin,unsigned short size,unsigned char *pos,unsigned short LeaderLen) 
{
	unsigned short CurrentExtLen=0;
	unsigned char *next=0;
	unsigned short ExtLen=0;

	if(begin==0||pos==0||pos<begin)
		return 0;
	CurrentExtLen=getTSInt12(pos+LeaderLen-2);
	next=pos+LeaderLen+CurrentExtLen;
	if(next+LeaderLen>begin+size)
		return 0;
	ExtLen=getTSInt12(next+LeaderLen-2);
	if(next+LeaderLen+ExtLen>begin+size)
		return 0;
	return next;
}

// table parser

// SDT

__inline unsigned char *SDTServiceFirst(unsigned char *SecData)
{
	return Loop12FirstObject(
		SecData,
		getSecLength(SecData)+TS_SECTION_LEADER_SIZE,
		TS_SECTION_LEADER_SIZE+TS_SECTION_HEADER_SIZE+3,
		SDT_SERVICE_HEADER_LENGTH);
}
__inline unsigned char *SDTServiceNext(unsigned char *SecData,unsigned char *pos)
{
	return Loop12NextObject(
		SecData,
		getSecLength(SecData)+TS_SECTION_LEADER_SIZE,
		pos,
		SDT_SERVICE_HEADER_LENGTH);
}
__inline unsigned char *SDTServiceDescFirst(unsigned char *Servie,unsigned short DescLength)
{
	return firstDescriptor(Servie+SDT_SERVICE_HEADER_LENGTH,DescLength);
}

// EIT
__inline unsigned char *EITEventFirst(unsigned char *SecData)
{
	return Loop12FirstObject(
		SecData,
		getSecLength(SecData)+TS_SECTION_LEADER_SIZE,
		TS_SECTION_LEADER_SIZE+TS_SECTION_HEADER_SIZE+6,
		EIT_EVENT_HEADER_LENGTH);
}
__inline unsigned char *EITEventNext(unsigned char *SecData,unsigned char *pos)
{
	return Loop12NextObject(
		SecData,
		getSecLength(SecData)+TS_SECTION_LEADER_SIZE,
		pos,
		EIT_EVENT_HEADER_LENGTH);
}
__inline unsigned char *EITEventDescFirst(unsigned char *Event,unsigned short DescLength)
{
	return firstDescriptor(Event+EIT_EVENT_HEADER_LENGTH,DescLength);
}


// utility





#ifdef __cplusplus
}
#endif
#endif


