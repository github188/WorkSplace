#pragma once
#ifdef _WINDOWS
#pragma warning(disable:4312)
#endif
#include "stbruntime.h"

class simplethread
{
public:
	simplethread() : handle_(0),bStop_(false) {}
	~simplethread() { stop(); }
	bool start( unsigned int(__stdcall *start_address )(void *), void *arglist)
	{
		if(is_running()) 
			stop();
		unsigned threadID;
		bStop_=false;
		arglist_ = arglist;
		handle_ = (HANDLE)(NS__beginthreadex(NULL,0,start_address,this,0,&threadID));
		return handle_!=0;
	}
	void stop() {
		if(handle_) {
			bStop_ = true;
			NS_WaitThread(handle_);
			handle_=0;
		}
	}
	bool is_running() { return handle_!=0; }
	void * get_arglist() { return arglist_; }
	bool check_stop() { return bStop_; }
private:
	HANDLE handle_;
	void *arglist_;
	bool bStop_;
};
