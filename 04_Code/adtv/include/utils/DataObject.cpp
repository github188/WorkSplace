#include "DataObject.h"
#include <stdexcept>

using namespace std;

// exception
// 
struct LogicError : std::logic_error
{
	LogicError(const std::string& strInfo)
		: std::logic_error(strInfo)
	{}
};

struct RuntimeError : std::runtime_error
{
	RuntimeError(const std::string& strInfo)
		: std::runtime_error(strInfo)
	{}
};

inline void ThrowLogicError(const std::string& strInfo)
{
	throw LogicError(strInfo);
}

inline void ThrowRuntimeError(const std::string& strInfo)
{
	throw RuntimeError(strInfo);
}

static J_S32 J_StringToS32(const char *str)
{
	if( (str[0] == '0') && (str[1]=='x' || str[1] == 'X'))
		return strtol(str,NULL,0);
	else
		return atol(str);
}

// CVariant_t implement
// 
CVariant_t::CVariant_t()
{
	this->vt = JVT_EMPTY;
}
	
CVariant_t::CVariant_t(const CVariant_t& varSrc)
{
	Clear();
	operator=(varSrc);
}

CVariant_t::CVariant_t(const J_Variant& varSrc)
{
	Clear();
	J_VariantCopy(this,const_cast<J_Variant*>(&varSrc));
}

CVariant_t::CVariant_t(J_Variant& varSrc, bool fCopy)
{
	Clear();
	if(fCopy)
		J_VariantCopy(this,const_cast<J_Variant*>(&varSrc));
	else
		Attach(varSrc);
}

// Destructor
// 
CVariant_t::~CVariant_t()
{
	J_VariantClear(this);
}

CVariant_t::CVariant_t(J_U8 value){
	Clear();
	operator=(value);
}
CVariant_t::CVariant_t(J_S8 value){
	Clear();
	operator=(value);
}
CVariant_t::CVariant_t(J_U16 value){
	Clear();
	operator=(value);
}
CVariant_t::CVariant_t(J_S16 value){
	Clear();
	operator=(value);
}
CVariant_t::CVariant_t(J_U32 value){
	Clear();
	operator=(value);
}
CVariant_t::CVariant_t(J_S32 value){
	Clear();
	operator=(value);
}

CVariant_t::CVariant_t(float value){
	Clear();
	operator=(value);
}
CVariant_t::CVariant_t(double value){
	Clear();
	operator=(value);
}
CVariant_t::CVariant_t(const std::string& strVal){
	Clear();
	operator=(strVal);
}

CVariant_t::CVariant_t(const char* strVal){
	Clear();
	operator=(strVal);
}

CVariant_t::CVariant_t(const J_Binary* pBulk){
	Clear();
	operator=(pBulk);
}

CVariant_t::CVariant_t(const J_BYTE* pByte,const J_U32 uSize)
{
	J_U32 uLength = 2*sizeof(J_U32) + sizeof(J_BYTE)*uSize;
	J_Binary *pBulk = (J_Binary*)new J_BYTE[uLength];
	if(pBulk){
		pBulk->uSize = uSize;
		memcpy((J_BYTE*)(pBulk->pByte),pByte,uSize);
		
		Clear();	
		operator=(pBulk);

		delete[] (J_BYTE*)pBulk;
		pBulk = 0;
	}
	else{
		ThrowRuntimeError("memory allocation failed!!!");
	}
}


// Extractors
//
CVariant_t::operator J_S8() const
{
	if(JVT_S8 != this->vt){
		ThrowRuntimeError("Type does not match!!!");
	}

	return this->s8Val ; 
}

CVariant_t::operator J_U8() const
{
	if(JVT_U8 != this->vt){
		ThrowRuntimeError("Type does not match!!!");
	}

	return this->u8Val ; 
}

CVariant_t::operator J_S16() const
{
	if(JVT_S16 != this->vt){
		ThrowRuntimeError("Type does not match!!!");
	}

	return this->s16Val ; 
}
CVariant_t::operator J_U16() const
{
	if(JVT_U16 != this->vt){
		ThrowRuntimeError("Type does not match!!!");
	}

	return this->u16Val ; 
}

CVariant_t::operator J_S32() const
{
	if(JVT_S32 != this->vt){
		ThrowRuntimeError("Type does not match!!!");
	}

	return this->s32Val ; 
}
CVariant_t::operator J_U32() const
{
	if(JVT_U32 != this->vt){
		ThrowRuntimeError("Type does not match!!!");
	}

	return this->u32Val ; 
}

CVariant_t::operator bool() const
{
	if(JVT_BOOL != this->vt){
		ThrowLogicError("Type does not match!!!");
	}

	return (J_TRUE == this->bVal) ? true : false;
}

CVariant_t::operator float() const
{	
	if(JVT_FLOAT != this->vt){
		ThrowLogicError("Type does not match!!!");
	}

	return this->fltVal ; 
}

CVariant_t::operator double() const
{
	if(JVT_DOUBLE != this->vt){
		ThrowLogicError("Type does not match!!!");
	}

	return this->dblVal ; 
}

CVariant_t::operator char *() const
{
	if(JVT_STRING != this->vt){
		ThrowLogicError("Type does not match!!!");
	}
	
	return this->pStrVal->strVal;
}

CVariant_t::operator std::string() const
{
	if(JVT_STRING != this->vt){
		ThrowLogicError("Type does not match!!!");
	}

	return this->pStrVal->strVal;
}

CVariant_t::operator J_Binary* () const
{
	if(JVT_BINARY != this->vt){
		ThrowLogicError("Type does not match!!!");
	}

	return this->pBinary;
}

J_VARTYPE CVariant_t::GetValueType()
{
	return this->vt;
}

void CVariant_t::Attach(const J_Variant& varSrc)
{
	J_VariantClear(this);
	memcpy(dynamic_cast<J_Variant*>(this),&varSrc,sizeof(J_Variant));
}

J_Variant CVariant_t::Detach()
{
	J_Variant var = *dynamic_cast<J_Variant*>(this);
	Clear();

	return var;
}


// Assignment operations
//
CVariant_t& CVariant_t::operator=(const _tagVariant& varSrc)
{
	J_VariantCopy(this,const_cast<_tagVariant *>(&varSrc));

	return *this;
}

CVariant_t& CVariant_t::operator=(const CVariant_t& varSrc)
{
	
	J_VariantCopy(this,const_cast<CVariant_t *>(dynamic_cast<CVariant_t const *>(&varSrc)));
	
	return *this;
}

CVariant_t& CVariant_t::operator=(const CVariant_t* pSrc) 
{
	J_VariantCopy(this,const_cast<CVariant_t *>(dynamic_cast<CVariant_t const *>(pSrc)));

	return *this;
}

CVariant_t& CVariant_t::operator=(const std::string& strVal)
{
	J_VariantClear(this);
	if(CopyStr(strVal.c_str())){
		this->vt = JVT_STRING;
	}

	return *this;
}

CVariant_t& CVariant_t::operator=(const char* pSrc) 
{
	J_VariantClear(this);
	if(CopyStr(pSrc)){
		this->vt = JVT_STRING;
	}
	
	return *this;
}

CVariant_t& CVariant_t::operator=(const J_Binary* pBulk)
{
	J_VariantClear(this);
	if(CopyBulk(pBulk->pByte,pBulk->uSize))
		this->vt = JVT_BINARY;

	return *this;
}

CVariant_t& CVariant_t::operator=(float value)
{
	J_VariantClear(this);

	this->vt = JVT_FLOAT;
	this->fltVal = value;
	
	return *this;
}

CVariant_t& CVariant_t::operator=(double value)
{
	J_VariantClear(this);

	this->vt = JVT_DOUBLE;
	this->dblVal = value;

	return *this;
}

CVariant_t& CVariant_t::operator=(J_S8 value)
{
	J_VariantClear(this);

	this->vt = JVT_S8;
	this->s8Val = value;

	return *this;
}

CVariant_t& CVariant_t::operator=(J_U8 value)
{
	J_VariantClear(this);

	this->vt = JVT_U8;
	this->u8Val = value;

	return *this;
}

CVariant_t& CVariant_t::operator=(J_S16 value)
{
	J_VariantClear(this);

	this->vt = JVT_S16;
	this->s16Val = value;

	return *this;
}

CVariant_t& CVariant_t::operator=(J_U16 value)
{
	J_VariantClear(this);

	this->vt = JVT_U16;
	this->u16Val = value;

	return *this;
}

CVariant_t& CVariant_t::operator=(J_U32 value)
{
	J_VariantClear(this);

	this->vt = JVT_U32;
	this->u32Val = value;

	return *this;
}

CVariant_t& CVariant_t::operator=(J_S32 value)
{
	J_VariantClear(this);

	this->vt = JVT_S32;
	this->s32Val = value;

	return *this;
}

// Comparison operations
//
bool CVariant_t::operator==(CVariant_t const &var)
{
	return J_VariantComp(
		const_cast<J_Variant *>(dynamic_cast<J_Variant const *>(this)),
		const_cast<J_Variant *>(dynamic_cast<J_Variant const *>(&var)));
}
bool CVariant_t::operator!=(CVariant_t const &var)
{
	return !J_VariantComp(
		const_cast<J_Variant *>(dynamic_cast<J_Variant const *>(this)),
		const_cast<J_Variant *>(dynamic_cast<J_Variant const *>(&var)));
}

void CVariant_t::Clear()
{
	J_VariantInit(this);
}

bool CVariant_t::CopyStr(const char* strVal)
{
	bool bRet = false;
	if(strVal) {
		size_t len = strlen(strVal);
		this->pStrVal = (J_String*)new char[sizeof(J_U32)+len+1];
		if(this->pStrVal->strVal) {
			memcpy(this->pStrVal->strVal,strVal,len);
			this->pStrVal->strVal[len]=0;

			this->pStrVal->uRefCount = 1;
			bRet = true;
		}
	}

	return bRet;
}

bool CVariant_t::CopyBulk(const J_BYTE* pBuf,const J_U32 uSize)
{
	bool bRet = false;
	if(pBuf) {
		J_U32 uLength = 2*sizeof(J_U32)+sizeof(J_BYTE)*uSize;
		this->pBinary = (J_Binary*)new J_BYTE[uLength];
		if(this->pBinary) {
			this->pBinary->uSize = uSize;
			memcpy((J_BYTE*)(this->pBinary->pByte),pBuf,uSize);

			this->pBinary->uRefcount = 1;
			bRet = true;
		}
	}

	return bRet;
}

void	J_VariantInit(J_Variant* val)
{
	memset(val,0,sizeof(J_Variant));
	val->vt = JVT_EMPTY;
}

void	J_VariantClear(J_Variant* val)
{
	if(JVT_STRING == val->vt)
	{
		if(val->pStrVal){
			val->pStrVal->uRefCount--;
			if(val->pStrVal->uRefCount <=0 )
			{
				delete[] (char*)val->pStrVal;
				val->pStrVal = 0;
			}
		}
	}
	if(JVT_BINARY == val->vt){
		if(val->pBinary){
			val->pBinary->uRefcount--;
			if(val->pBinary->uRefcount <=0 ){
				delete[] (J_BYTE*)val->pBinary;
				val->pBinary = 0;
			}
		}
	}

	memset(val,0,sizeof(J_Variant));
	val->vt = JVT_EMPTY;
}

void	J_VariantCopy(J_Variant* dest,J_Variant* src)
{
	J_VariantClear(dest);
	
	dest->vt = src->vt;

	switch(dest->vt)
	{
	case JVT_EMPTY:
		break;
	case JVT_U8:
		dest->u8Val = src->u8Val;
		break;
	case JVT_S8:
		dest->s8Val = src->s8Val;
		break;
	case JVT_U16:
		dest->u16Val = src->u16Val;
		break;
	case JVT_S16:
		dest->s16Val = src->s16Val;
		break;
	case JVT_U32:
		dest->u32Val = src->u32Val;
		break;
	case JVT_S32:
		dest->s32Val = src->s32Val;
		break;
	case JVT_BOOL:
		dest->bVal = src->bVal;
		break;
	case JVT_STRING:
		dest->pStrVal = src->pStrVal;
		dest->pStrVal->uRefCount++;
		break;
	case JVT_BINARY:
		dest->pBinary= src->pBinary;
		dest->pBinary->uRefcount++;
		break;
	case JVT_FLOAT:
		dest->fltVal = src->fltVal;
		break;
	case JVT_DOUBLE:
		dest->dblVal = src->dblVal;
		break;
	default:
		break;
	}
}

bool	J_VariantComp(J_Variant* dest,J_Variant* src)
{
	return true;
}

J_S32	J_VariantToS32(J_Variant* val)
{
	J_S32 nRet = 0;
	if(JVT_S32 != val->vt)
	{
		switch(val->vt)
		{
		case JVT_U32:
			nRet = static_cast<J_S32>(val->u32Val);
			break;
		case JVT_S32:
			nRet = static_cast<J_S32>(val->s32Val);
			break;
		case JVT_U16:
			nRet = static_cast<J_S32>(val->u16Val);
			break;
		case JVT_S16:
			nRet = static_cast<J_S32>(val->s16Val);
			break;
		case JVT_U8:
			nRet = static_cast<J_S32>(val->u8Val);
			break;
		case JVT_S8:
			nRet = static_cast<J_S32>(val->s8Val);
			break;
		case JVT_BOOL:
			nRet = static_cast<J_S32>(val->bVal);
			break;
		case JVT_STRING:
			if(val->pStrVal){
				nRet = J_StringToS32(val->pStrVal->strVal);
			}
			break;
		case JVT_BINARY:
			nRet = reinterpret_cast<J_S32>(val->pBinary);
			break;
		default:
			break;			
		}
	}	
	else{
		nRet = val->s32Val;
	}

	return nRet;
}