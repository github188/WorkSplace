#ifndef _BLOCK_RING
#define _BLOCK_RING
//////////////////////////////////////////////////////////////////////////////
//
// Modification History:
//
//  Date                       By         Description
//  --------------    ------  ---------------------------------------------------
//  06.01/壬辰年  hjj         Created

// 合理设置代码，可以实现不同线程之间
// 一个push, 一个pop, 不用加锁。

#define INCPTR(ptr) do{ \
	ptr++; \
	if(ptr==MAXSIZE) ptr=0; \
}while(0);

template<typename T, int MAXSIZE>
class BlockRing
{
	public:
		BlockRing():head_(0),tail_(0){}
		~BlockRing(){}
		T* BeginPush(){
			if(!IsFull())
				return &buf[head_];
			return NULL;
		}
		void EndPush(){INCPTR(head_)}
		T* BeginPop(){
			if(!IsEmpty())
				return &buf[tail_];
			return NULL;
		}
		void EndPop(){INCPTR(tail_)}
		void ClearRing(){head_=tail_/*=0*/;}
		void ClearHalfRing(){
			for(int i=0; i<MAXSIZE/2; i++)
				EndPop();
		}
		void GetPos(int *head, int *tail) const
		{
			*head = head_;
			*tail = tail_;
		}
		void PrintPos(const char *title) const
		{
			printf("%s, head:%d, tail:%d\n",title,head_,tail_);
		}
	private:
		T	buf[MAXSIZE];
		int	head_;
		int tail_;
	private:
		bool IsFull() const
		{
			int t=head_;
			INCPTR(t);
			if(t==tail_)
				return true;
			return false;
		}
		bool IsEmpty() const
		{
			if(head_==tail_)
				return true;
			return false;
		}
};
#endif
