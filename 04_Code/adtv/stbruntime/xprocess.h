/*****************************************************************************
Description: CPI OS interface

Copyright(c) 2010-2015 Novel-SuperTV, All rights reserved.

Date        Author           Modification
----------------------------------------------------------------
2010-12-06  Novel-SuperTV    Created
 *****************************************************************************/
#ifndef NOVELSUPERTV_STBRT_PROCESS_H
#define NOVELSUPERTV_STBRT_PROCESS_H

#include "typ.h"
#include "stbruntime.h"
//#include <stdexcept>
#include <string>
// #include "dxreport.h"
// #include "err.h"

namespace NovelSupertv
{
namespace stbruntime
{

	///最大超时时间定义
	const U32 TimeInfinite = (U32)(-1);

	class Thread;

	/**
	 * 线程智能指针.
	 *
	 * \tparam ThreadT 线程类型.
	 *
	 * \note 线程类被设计为不可复制,任何复制操作将会导致编译错误.通过线程智能指针,
	 可以将线程放入标准容器中,并且能够自动启动和停止.
	 */
	template< typename ThreadT = Thread >
		struct ThreadPtr
		{
			/**
			 * 线程智能指针构造函数.
			 *
			 * \param[in] t 被线程智能指针类管理的线程指针.
			 */
			explicit ThreadPtr( ThreadT* t = 0 ) : t_( 0 ) { reset( t ); }

			/// 线程智能指针复制构造函数.
			ThreadPtr( ThreadPtr const& other )
				: t_(other.t_)
			{
				if( t_ )
					t_->inc_ref();
			}

			/// 线程智能指针析构函数.
			~ThreadPtr()
			{
				if( t_ && t_->dec_ref() == 0 ) 
				{
					t_->stop();
					delete t_;
				}
			}

			/// 线程智能指针重载的赋值操作符.
			ThreadPtr& operator = ( ThreadPtr const& other )
			{
				if( this != &other )
					reset( other.t_ );
				return *this;
			}

			/**
			 * 重置线程指针.
			 *
			 * \param[in] t 被线程智能指针类管理的新线程指针.
			 * \note 线程智能指针管理的原线程类将会自动关闭。
			 */
			void reset( ThreadT* t = 0 )
			{
				if( t_ && t_->dec_ref() == 0 ) 
				{
					t_->stop();
					delete t_;
				}
				t_ = t;
				if( t_ ) 
				{
					t_->inc_ref();
					t_->start();
				}
			}

			/**
			 * 线程智能指针重载的 * 操作符.
			 *
			 * \return  线程对象的引用.
			 * \note 通过重载*操作符，使线程智能指针可以像普通线程一样取指向的线程对象.
			 */
			ThreadT& operator * () const { return *t_; }

			/**
			 * 线程智能指针重载的 -> 操作符.
			 *
			 * \return  线程对象的指针.
			 * \note 通过重载->操作符，使线程智能指针可以像普通线程指针一样调用成员函数.
			 */
			ThreadT* operator -> () const { return t_; }

			private:
			ThreadT* t_;
		};

	/**
	 * 互斥体类型定义.
	 * 
	 * 互斥体类型被设计用来管理互斥体资源，在构造函数中创建互斥体，在析够函数中删除
	 * 互斥体.
	 * \note 互斥体类被设计为不可复制，任何复制操作将会导致编译错误.
	 */
	class Mutex
	{
		public:	
			/// 互斥体构造函数.
			Mutex()
			{
				h_ = NS_MutexCreate();
				if( h_ ) return;
				//			throw exception("Mutex::Mutex");
			}
			/// 互斥体析构函数.
			~Mutex() { NS_MutexDelete(h_); }

			/// 互斥体加锁.
			void lock() { NS_MutexLock(h_); }
			/// 互斥体解锁.
			void unlock() { NS_MutexUnlock(h_); }

		private:
			HANDLE h_;
			// noncopyable:
			Mutex( Mutex & );
			void operator = ( Mutex );
	};

	/**
	 * 信号量类型定义.
	 * 
	 * 信号量类型被设计用来管理互斥体资源，在构造函数中创建信号量，在析够函数中删除
	 * 信号量.
	 * \note 信号量类被设计为不可复制，任何复制操作将会导致编译错误.
	 */
#if 0
	struct Semaphore
	{
		/**
		 * 信号量构造函数.
		 *
		 * \param[in] count 信号量初始值.
		 */
		Semaphore( U16 count,U16 maxCount )
		{
			h_ = NS_SemCreate( count,maxCount );
			if( h_ )
				return;
			// throw OsError( "Semaphore::Semaphore");
		}

		/// 信号量析构函数.
		~Semaphore() { NS_SemDelete( h_ ); }

		/**
		 * 等待信号量,如果不成功超时返回.
		 *
		 * \param[in] timeout 超时时间,单位:ms.
		 * \return  等待信号量是否成功.
		 - true  等到信号量
		 - false 没等到信号量.
		 */
		bool lock( U32 timeout ) { return NS_SemWait( h_, timeout ) == TRUE; }

		/**
		 * 等待信号量.
		 *
		 * \note 等到信号量本函数才返回.
		 */
		void lock() { lock( TimeInfinite ); }

		/// 释放信号量.
		void unlock() { NS_SemSignal( h_ ); }

		private:
		HANDLE h_;
		// noncopyable:
		Semaphore( Semaphore & );
		void operator = ( Semaphore );
	};
#endif

	/**
	 * 锁类型定义
	 *
	 * \tparam ObjT 锁类型.
	 */
	template< typename ObjT > struct Lock
	{
		/**
		 * 锁构造函数.
		 *
		 * \note 在构造函数中进行加锁操作.
		 */
		Lock( ObjT& obj ) : obj_(obj) { obj_.lock(); }
		/**
		 * 锁析构函数.
		 *
		 * \note 在析构函数中进行解锁操作.
		 */
		~Lock() { obj_.unlock(); }
		private:
		ObjT& obj_;
		// noncopyable:
		Lock( Lock& );
		void operator = ( Lock );
	};

	/// 互斥体锁类型
	typedef Lock<Mutex> LockMutex;
	/// 信号量锁类型
	// typedef Lock<Semaphore> LockSemaphore;

	/**
	 * 超时锁类型定义
	 *
	 * \tparam ObjT 锁类型.
	 */
	template< typename ObjT >
		struct TryLock
		{
			/**
			 * 锁构造函数.
			 *
			 * \param[in] obj 锁对象.
			 * \param[in] timeout 超时时间，单位:ms.
			 */
			TryLock( ObjT& obj, U32 timeout ) : obj_(obj)
			{
				locked_ = obj_.lock( timeout );
			}

			/// 锁析构函数.
			~TryLock()
			{
				if( locked_ )
					obj_.unlock();
			}

			/**
			 * 是否加锁成功.
			 *
			 * \return 加锁成功与否.
			 - true 加锁成功
			 - false 加锁超时.
			 */
			bool locked() { return locked_; }
			private:
			ObjT& obj_;
			bool locked_;
			// noncopyable:
			TryLock( TryLock& );
			void operator = ( TryLock );
		};

	/**
	 * 进程定义
	 *
	 */
#if 0
	struct Process
	{
		Process(::std::string const &CmdLine,::std::string const &WorkDir)
		{
			CmdLine_=CmdLine;
			WorkDir_=WorkDir;
			handle_=0;
		}
		Process(::std::string const &CmdLine)
		{
			CmdLine_=CmdLine;
			handle_=0;
		}
		~Process()
		{
			if(handle_)
				Close();
		}
		bool Create(bool bShow=true)
		{
			if(WorkDir_.empty())
				handle_ = XNS_CreateProcess(CmdLine_.c_str(),0,static_cast<BOOL>(bShow));
			else
				handle_ = XNS_CreateProcess(CmdLine_.c_str(),WorkDir_.c_str(),static_cast<BOOL>(bShow));
			return handle_!=0;		
		}
		bool WaitForExit(U32 timeout=TimeInfinite) {
			NS_Process * pProcess = (NS_Process *)handle_;
			if(!pProcess)
				return false;
			DWORD res=NS_WaitForSingleObject( pProcess->pi.hProcess, timeout);
			return res==WAIT_OBJECT_0;		
		}
		void Terminate() {
			if(handle_)
				NS_TerminateProcess(handle_,0);		
		}
		void Close() {
			if(handle_) 
				NS_CloseProcess(handle_);
		}
		private:
		HANDLE handle_; 
		::std::string CmdLine_;
		::std::string WorkDir_;
	};
#endif
	struct Event
	{
		Event()
		{
			h_ = NS_CreateEvent( NULL, FALSE, FALSE, NULL );
			if( h_ ) return;
			//		throw OsError( "Event::Event");
		}

		~Event() { NS_DeleteEvent( h_ ); }

		bool lock( U32 timeout ) { return NS_WaitEvent( h_, timeout ) == TRUE; }
		void lock() { lock( TimeInfinite ); }
		void unlock() { NS_SetEvent( h_ ); }

		private:

		HANDLE h_;
		// noncopyable:
		Event( Event & );
		void operator = ( Event );
	};

	///线程处理函数，非成员函数
	extern "C" inline unsigned int __stdcall stbos_threadproc( void* param );
	/**
	 * 线程类型定义.
	 *
	 * 线程类负责管理线程，实现了线程操作接口，并定义了线程接口函数。线程类被设计为
	 * 抽象类，不能创建对象,要创建对象，需要从线程类派生一定制线程类型，并实现线程
	 * 接口函数。
	 * \note 线程类被设计为不可复制，任何复制操作将会导致编译错误.
	 */
	class Thread
	{
		public:	
			/**
			 * 线程构造函数.
			 *
			 * \param[in] n 线程名称
			 * \param[in] s 线程栈大小.
			 * \param[in] p 线程优先级.
			 * \note 线程类被设计为不可复制，任何复制操作将会导致编译错误.
			 */
			Thread( std::string const & n, U32 s /* Priority p = Priority_Normal */ )
			{
				name_= n ;
				stacksize_= s ;
				//			prio_= p ;
				refcount_= 0 ;
				h_= 0 ;
				exitcode_= U32( -1 ) ;
				threadid_=0;
			}

			/**
			 * 线程析够函数.
			 */
			virtual ~Thread() { NS_ThreadDelete( h_ ); }

			/**
			 * 获取线程名称.
			 *
			 * \return 线程名称
			 */
			std::string const& name() const { return name_; }

			/**
			 * 增加线程的引用计数.
			 *
			 * \return 线程的引用计数.
			 */
			U8 inc_ref() { return ++refcount_; }

			/**
			 * 减小线程的引用计数.
			 *
			 * \return 线程的引用计数.
			 */
			U8 dec_ref() { return --refcount_; }

			/// 创建并启动线程：一个线程对象只能被 start() 一次。
			void start()
			{
				signalled_ = false;
				h_ = NS_CreateThread(
						name_.c_str(),
						stbos_threadproc,
						this,
						stacksize_,
						false,
						&threadid_
						);

				//			dxreport("Class Thread: Start the '%s' thread \n",name_.c_str());
			}

			/**
			 * 通知并等待线程停止.
			 *
			 * \param[in] timeout 超时时间，单位:ms.
			 * \return 线程是否退出.
			 - true  线程已退出.
			 - false 线程没退出.
			 */
			bool stop( U32 timeout = TimeInfinite )
			{
				signal();
				return join( timeout );
			}

			/// 通知线程停止：
			void signal()
			{
				if( h_ != 0 ) 
				{
					signalled_ = true;
					do_signal();
				}
			}

			/** 
			 * 通知本线程停止时需完成的附加操作.
			 * \note 派生类通过重载该函数来完成线程退出时的附加操作.
			 */
			virtual void do_signal() {}

			/**
			 * 本线程用来判断是否已被通知停止运行
			 *
			 * \return 线程是否被通知退出.
			 - true 线程被通知退出.
			 - false 线程没有被通知退出.
			 */
			bool signalled() { return signalled_; }

			/**
			 * 等待线程停止.
			 *
			 * \param[in] timeout 超时时间，单位:ms.
			 * \return 线程是否退出.
			 - true 线程已退出.
			 - false 线程没退出.
			 */

			bool join( U32 timeout = TimeInfinite )
			{
				if( h_ == 0 )
					return true;
				//			dxreport("Class Thread: stop( the '%s' thread )\n",name_.c_str());
				BOOL rt = NS_ThreadJoin( h_, timeout ); 
				//			dxreport("Class Thread: stop( the '%s' thread ) return %d \n",name_.c_str(),rt);
				return rt == TRUE;
			}

			/**
			 * 判断线程是否还在运行中.
			 *
			 * \return 线程状态
			 - true  线程正运行.
			 - false 线程已退出.
			 */
			bool active() { return ! join( 0 ); }

			/**
			 * 获取线程退出码.
			 *
			 * \return 线程退出码.
			 * \note join() 成功后才可以调用.
			 */
			U32 exitcode() { return exitcode_; }


			bool is_curthread() { return NS_IsCurrentThreadByID(threadid_)!=0; }

		protected:


			/**
			 * 线程的运行函数
			 *
			 * \return 线程运行函数返回码.
			 * \note 派生类通过重载该函数来定制线程运行函数.
			 * \par 示例代码:
			 * \code
			 U32 do_run()
			 {
			 U32 time = 50;

			 while( !signalled() )
			 {
			//do_something;
			Sleep( time );//让出CPU控制权,以便其它线程得到运行机会.
			}
			return 0;
			}
			\endcode
			*/
			virtual U32 do_run() = 0;

			/** 
			 * 处理未捕获的异常
			 * \param[in] what 异常描述文本.
			 * \note 派生类通过重载该函数来处理未捕获的异常.
			 */
			virtual U32 do_unexpected( char const* what )
			{
				// TODO :  write 'what' to somewhere.
				(void )what;
				return U32(-1);
			}

		private:

			// noncopyable:
			Thread( Thread& );
			void operator = ( Thread& );

			// parameters:
			std::string name_;
			U32 stacksize_;
			//		Priority prio_;

			// states:
			U8 refcount_;
			HANDLE h_;
			U32 threadid_;
			bool volatile signalled_;
			U32 exitcode_;

			// the thread proc implementation:

			friend unsigned int __stdcall stbos_threadproc( void* param );

			U32 run()
			{
				//			try 
				{
					return do_run();
				} 
				/*
				   catch( std::exception& e ) 
				   {
				   return do_unexpected( e.what() );
				   } catch( ... ) 
				   {
				   return do_unexpected( "unknown exception occurred in a thread " );
				   }
				   */
			}
	};

	extern "C" inline unsigned int __stdcall stbos_threadproc( void* param )
	{
		return static_cast<Thread*>(param)->run();
	}
}
}

#if defined(WIN32)
#define MutexT	  NovelSupertv::stbruntime::Mutex
#define AutoLockT NovelSupertv::stbruntime::LockMutex 
#else
#include <utils/threads.h>
#define MutexT		android::Mutex
#define AutoLockT	android::Mutex::Autolock
#endif 

#endif

