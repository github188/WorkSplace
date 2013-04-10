#ifndef _xywang__systick_h_
#define _xywang__systick_h_


struct Tick
{
	Tick() : tick_(0){}
	Tick(DWORD t) : tick_(t){}
	Tick(const Tick & t) : tick_(t.tick_){} 

	Tick & operator=(DWORD t) {
		tick_ = t;
		return *this;
	}
	Tick & operator=(const Tick & t) {
		tick_ = t.tick_;
		return *this;
	}
	operator DWORD() const { return tick_; }
	//bool operator==(const Tick & t) const { return tick_==t.tick_; }
	//bool operator!=(const Tick & t) const { return tick_!=t.tick_; }
	//bool operator> (const Tick & t) const { return tick_> t.tick_; }
	//bool operator>=(const Tick & t) const { return tick_>=t.tick_; }
	//bool operator< (const Tick & t) const { return tick_< t.tick_; }
	//bool operator<=(const Tick & t) const { return tick_<=t.tick_; }

	Tick operator-(const Tick & t) const { return Tick(tick_-t.tick_); }
	Tick operator+(const Tick & t) const { return Tick(tick_+t.tick_); }
	void operator-=(const Tick & t) { tick_-=t.tick_; }
	void operator+=(const Tick & t) { tick_+=t.tick_; }
	
	Tick pass() const {
		DWORD c = GetTickCount();
		if(c<tick_) return (0xffffffff-tick_)+c;
		return Tick(c-tick_);
	}

	static DWORD now() { return GetTickCount(); }

private:

	DWORD tick_;
};



#endif