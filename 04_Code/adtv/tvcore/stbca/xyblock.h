//////////////////////////////////////////////////////////////////////////////
//
//                     (C) Novel-SuperTV 2008
//  All rights are reserved. Reproduction in whole or in part is prohibited
//  without the written consent of the copyright owner.
//
//  Novel-SuperTV  reserves the right to make changes without notice at any time.
//  Novel-SuperTV  makes no warranty, expressed, implied or statutory, including but
//  not limited to any implied warranty of merchantability or fitness for any
//  particular purpose, or that the use will not infringe any third party
//  patent, copyright or trademark. Novel-SuperTV  must not be liable for any loss
//  or damage arising from its use.
//
//////////////////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
//
// Modification History:
//
//  Date     By      Description
//  -------  ------  ---------------------------------------------------------
//  07Nov01  xyw      Created
//  08Jul02  mason      fixed ...
//
//////////////////////////////////////////////////////////////////////////////


#ifndef _XY_BLOCK_H_
#define _XY_BLOCK_H_

/************************************************************************/
/* 固定长度的块分配器                                                   */
/************************************************************************/
template <typename T,int MaxBlkCount> 
class BlockAllocator
{
public:
	BlockAllocator() {
		reset();
	}
	int Alloc()	{
		return StackCount_>0 ? Stack_[(StackCount_--)-1] : -1;
	}
	void free(int i) {
		Stack_[StackCount_++]=i;
	}
	void freeAll() {
		reset();
	}
	T *get(int i) { 
		return &buf_[i]; 
	}
private:
	int Stack_[MaxBlkCount];
	int StackCount_;
	T buf_[MaxBlkCount];
	void reset() {
		for(int i=0; i<MaxBlkCount; i++)
			Stack_[i]=i;
		StackCount_=MaxBlkCount;
	}
};


/************************************************************************/
/* 块数据队列                                                           */
/************************************************************************/

template<typename DATA, int SIZE  >
class BlockDataQueue 
{
private:
	typedef struct _NODE {
		int i;
		DATA data;
		struct _NODE *last;
		struct _NODE *next;
	} NODE;
public:
	BlockDataQueue() {
		head_=NULL; tail_=NULL; iNode_=0;
	}
	DATA *BeginPush()	{
		iNode_=Nodes_.Alloc();
		if(iNode_==-1) return NULL;
		NODE *pNode=Nodes_.get(iNode_);
		pNode->i=iNode_;
		return &pNode->data;
	}
	void EndPush() {
		if(iNode_==-1) return;
		NODE *pNode=Nodes_.get(iNode_);
		if(head_) {
			pNode->last=NULL; pNode->next=head_; head_->last=pNode;
		}
		else{
			pNode->next=NULL; pNode->last=NULL; tail_=pNode;
		}
		head_=pNode;
	}
	bool pop(DATA &data) {
		if(!tail_) return false;
		data=tail_->data;
		freeTail();
		return true;
	}
	DATA *GetTail()
	{
		if(!tail_) return NULL;
		return &tail_->data;
	}
	bool pop() {
		if(!tail_) return false;
		freeTail();
		return true;
	}
	
	void clear() {
		while(freeTail());
	}
private:
	BlockAllocator<NODE,SIZE> Nodes_;
	NODE *head_;
	NODE *tail_;
	int iNode_;
	bool freeTail()
	{
		if(!tail_)
			return false;
		Nodes_.free(tail_->i);
		if(tail_->last==NULL)
			head_=tail_=NULL;
		else {
			tail_=tail_->last;
			tail_->next=NULL;
		}
		return true;
	}
};


#endif
