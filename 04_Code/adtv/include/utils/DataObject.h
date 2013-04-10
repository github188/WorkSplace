#ifndef JOYSEE_DATAOBJECT_H
#define JOYSEE_DATAOBJECT_H

#include "xinlin.hh"
#include <string>

typedef unsigned char	J_U8;
typedef	  signed char	J_S8;
typedef unsigned short	J_U16;
typedef   signed short  J_S16;
typedef unsigned int	J_U32;
typedef   signed int	J_S32;
typedef unsigned char   J_BOOL;
typedef J_U8			J_BYTE;
#define J_TRUE			((J_BOOL)1)
#define J_FALSE			((J_BOOL)0)

typedef unsigned short J_VARTYPE;

enum J_VARENUM
{	
	JVT_EMPTY = 0,
	JVT_U8,
	JVT_S8,
	JVT_U16,
	JVT_S16,
	JVT_U32,
	JVT_S32,
	JVT_BOOL,
	JVT_FLOAT,
	JVT_DOUBLE,
	JVT_STRING,
	JVT_BINARY
};

typedef struct _Binary{
	J_U32	uRefcount;
	J_U32	uSize;
	J_BYTE  pByte[1];
}J_Binary;

typedef struct _String{
	J_U32	uRefCount;
	char    strVal[1];
}J_String;

struct _tagVariant{
	J_VARTYPE vt;
    
	union 
	{
		J_U8	u8Val;
		J_S8	s8Val;
        J_U16	u16Val;
		J_S16	s16Val;
		J_U32 	u32Val;
        J_S32 	s32Val;
		J_BOOL  bVal;
		float	fltVal;
		double	dblVal;
		J_String* pStrVal;
		J_Binary* pBinary;
	};
};

typedef struct _tagVariant J_Variant;

struct CVariant_t : public J_Variant
{
	CVariant_t();
	
	CVariant_t(const CVariant_t& varSrc) ;
    CVariant_t(const J_Variant& varSrc) ;
	CVariant_t(J_Variant& varSrc, bool fCopy) ;
		
	CVariant_t(J_U8 value);
	CVariant_t(J_S8 value);
	CVariant_t(J_U16 value);
	CVariant_t(J_S16 value);
	CVariant_t(J_U32 value);
	CVariant_t(J_S32 value);
	CVariant_t(float value);
	CVariant_t(double value);
	CVariant_t(const std::string& strVal);
	CVariant_t(const char* strVal);
	CVariant_t(const J_Binary* pBulk);
	CVariant_t(const J_BYTE* pByte,const J_U32 uSize);
	
    // Destructor
    //
    ~CVariant_t();

    // Extractors
    //
	operator J_S8() const;
	operator J_U8() const;
	
	operator J_S16() const;
	operator J_U16() const;

	operator J_S32() const;
	operator J_U32() const;

	operator bool() const;
	operator float() const;
	operator double() const;
	operator char *() const;
	operator std::string() const;
	operator J_Binary* () const;
	
	J_VARTYPE GetValueType();
	
	void Attach(const J_Variant& varSrc) ;
	J_Variant Detach() ;
	
	// Assignment operations
    //
    CVariant_t& operator=(const J_Variant& varSrc) ;
	CVariant_t& operator=(const CVariant_t& varSrc) ;
    CVariant_t& operator=(const CVariant_t* pSrc) ;
    
	CVariant_t& operator=(const J_Binary* pBulk);
	CVariant_t& operator=(const std::string& strVal);
    CVariant_t& operator=(const char* pSrc);
	CVariant_t& operator=(float value);
	CVariant_t& operator=(double value);

	CVariant_t& operator=(J_S8 value);
	CVariant_t& operator=(J_U8 value);
	
	CVariant_t& operator=(J_S16 value);
	CVariant_t& operator=(J_U16 value);
	
	CVariant_t& operator=(J_U32 value);
	CVariant_t& operator=(J_S32 value);
	
	// Comparison operations
    //
	bool operator!=(const CVariant_t& var);
	bool operator==(const CVariant_t& var);

private:
	void Clear();
	bool CopyStr(const char* strVal);
	bool CopyBulk(const J_BYTE* pBuf,const J_U32 uSize);
};

template <typename T>
bool ConvertTo(const J_Variant& var,T& value)
{
	bool bRet = true;
	switch(var.vt)
	{
	case JVT_U32:
		value = static_cast<T>(var.u32Val);
		break;
	case JVT_S32:
		value = static_cast<T>(var.s32Val);
		break;
	case JVT_U16:
		value =  static_cast<T>(var.u16Val);
		break;
	case JVT_S16:
		value =  static_cast<T>(var.s16Val);
		break;
	case JVT_U8:
		value =  static_cast<T>(var.u8Val);
		break;
	case JVT_S8:
		value = static_cast<T>(var.s8Val);
		break;
	default:
		bRet = false;
		break;
	}

	return bRet;
}

void	J_VariantInit(J_Variant* val);
void	J_VariantClear(J_Variant* val);
void	J_VariantCopy(J_Variant* dest,J_Variant* src);
bool	J_VariantComp(J_Variant* dest,J_Variant* src);
J_S32	J_VariantToS32(J_Variant* val);

struct J_NVItem{
	std::string	name;
	CVariant_t 	value;
	
	J_NVItem(const J_NVItem& item){
		operator=(item);
	}
	J_NVItem& operator=(const J_NVItem& item){
		if(this != &item){
			this->name = item.name;
			this->value= item.value;
		}

		return *this;
	}
	J_NVItem():name(""){
	}
	~J_NVItem(){}
};
typedef tree<J_NVItem>  J_DataObject;

#endif // defined(DataObject_H)