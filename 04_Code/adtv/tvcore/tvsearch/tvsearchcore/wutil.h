//------------------------------------------------------------------------------
// File: WXUtil.h
//
// Desc: DirectShow base classes - defines helper classes and functions for
//       building multimedia filters.
//
// Copyright (c) 1992-2002 Microsoft Corporation.  All rights reserved.
//------------------------------------------------------------------------------

#pragma once

// #pragma warning(disable: 4705)

class CritSec {

    CritSec(const CritSec &refCritSec);
    CritSec &operator=(const CritSec &refCritSec);

    CRITICAL_SECTION m_CritSec;
public:
    CritSec() {
        InitializeCriticalSection(&m_CritSec);
    };
    ~CritSec() {
        DeleteCriticalSection(&m_CritSec);
    };
	BOOL TryLock() {
		return TryEnterCriticalSection(&m_CritSec);
		return TRUE;
	}
    void Lock() {
        EnterCriticalSection(&m_CritSec);
    };
    void Unlock() {
        LeaveCriticalSection(&m_CritSec);
    };
	
};

class MyAutolock {

    MyAutolock(const MyAutolock &refAutoLock);
    MyAutolock &operator=(const MyAutolock &refAutoLock);
protected:
    CritSec * m_pLock;
	BOOL m_bLock;
public:
    MyAutolock(CritSec * plock,BOOL InitLock=TRUE,BOOL needSet=TRUE)
    {
        m_pLock = plock;
		if(InitLock){
			if(needSet)
				m_pLock->Lock();
			m_bLock=TRUE;
		}
		else
			m_bLock=FALSE;
    }
    ~MyAutolock() {
		if(m_bLock)
			m_pLock->Unlock();
    }
	BOOL IsLock() { return m_bLock; }
	BOOL TryLock() {
		if(m_bLock)
			return TRUE;
		return m_bLock=m_pLock->TryLock();
	}
	void Unlock() {
		if(m_bLock) {
			m_pLock->Unlock();
			m_bLock=FALSE;
		}
	}
};


