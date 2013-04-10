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

	///���ʱʱ�䶨��
	const U32 TimeInfinite = (U32)(-1);

	class Thread;

	/**
	 * �߳�����ָ��.
	 *
	 * \tparam ThreadT �߳�����.
	 *
	 * \note �߳��౻���Ϊ���ɸ���,�κθ��Ʋ������ᵼ�±������.ͨ���߳�����ָ��,
	 ���Խ��̷߳����׼������,�����ܹ��Զ�������ֹͣ.
	 */
	template< typename ThreadT = Thread >
		struct ThreadPtr
		{
			/**
			 * �߳�����ָ�빹�캯��.
			 *
			 * \param[in] t ���߳�����ָ���������߳�ָ��.
			 */
			explicit ThreadPtr( ThreadT* t = 0 ) : t_( 0 ) { reset( t ); }

			/// �߳�����ָ�븴�ƹ��캯��.
			ThreadPtr( ThreadPtr const& other )
				: t_(other.t_)
			{
				if( t_ )
					t_->inc_ref();
			}

			/// �߳�����ָ����������.
			~ThreadPtr()
			{
				if( t_ && t_->dec_ref() == 0 ) 
				{
					t_->stop();
					delete t_;
				}
			}

			/// �߳�����ָ�����صĸ�ֵ������.
			ThreadPtr& operator = ( ThreadPtr const& other )
			{
				if( this != &other )
					reset( other.t_ );
				return *this;
			}

			/**
			 * �����߳�ָ��.
			 *
			 * \param[in] t ���߳�����ָ�����������߳�ָ��.
			 * \note �߳�����ָ������ԭ�߳��ཫ���Զ��رա�
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
			 * �߳�����ָ�����ص� * ������.
			 *
			 * \return  �̶߳��������.
			 * \note ͨ������*��������ʹ�߳�����ָ���������ͨ�߳�һ��ȡָ����̶߳���.
			 */
			ThreadT& operator * () const { return *t_; }

			/**
			 * �߳�����ָ�����ص� -> ������.
			 *
			 * \return  �̶߳����ָ��.
			 * \note ͨ������->��������ʹ�߳�����ָ���������ͨ�߳�ָ��һ�����ó�Ա����.
			 */
			ThreadT* operator -> () const { return t_; }

			private:
			ThreadT* t_;
		};

	/**
	 * ���������Ͷ���.
	 * 
	 * ���������ͱ������������������Դ���ڹ��캯���д��������壬������������ɾ��
	 * ������.
	 * \note �������౻���Ϊ���ɸ��ƣ��κθ��Ʋ������ᵼ�±������.
	 */
	class Mutex
	{
		public:	
			/// �����幹�캯��.
			Mutex()
			{
				h_ = NS_MutexCreate();
				if( h_ ) return;
				//			throw exception("Mutex::Mutex");
			}
			/// ��������������.
			~Mutex() { NS_MutexDelete(h_); }

			/// ���������.
			void lock() { NS_MutexLock(h_); }
			/// ���������.
			void unlock() { NS_MutexUnlock(h_); }

		private:
			HANDLE h_;
			// noncopyable:
			Mutex( Mutex & );
			void operator = ( Mutex );
	};

	/**
	 * �ź������Ͷ���.
	 * 
	 * �ź������ͱ������������������Դ���ڹ��캯���д����ź�����������������ɾ��
	 * �ź���.
	 * \note �ź����౻���Ϊ���ɸ��ƣ��κθ��Ʋ������ᵼ�±������.
	 */
#if 0
	struct Semaphore
	{
		/**
		 * �ź������캯��.
		 *
		 * \param[in] count �ź�����ʼֵ.
		 */
		Semaphore( U16 count,U16 maxCount )
		{
			h_ = NS_SemCreate( count,maxCount );
			if( h_ )
				return;
			// throw OsError( "Semaphore::Semaphore");
		}

		/// �ź�����������.
		~Semaphore() { NS_SemDelete( h_ ); }

		/**
		 * �ȴ��ź���,������ɹ���ʱ����.
		 *
		 * \param[in] timeout ��ʱʱ��,��λ:ms.
		 * \return  �ȴ��ź����Ƿ�ɹ�.
		 - true  �ȵ��ź���
		 - false û�ȵ��ź���.
		 */
		bool lock( U32 timeout ) { return NS_SemWait( h_, timeout ) == TRUE; }

		/**
		 * �ȴ��ź���.
		 *
		 * \note �ȵ��ź����������ŷ���.
		 */
		void lock() { lock( TimeInfinite ); }

		/// �ͷ��ź���.
		void unlock() { NS_SemSignal( h_ ); }

		private:
		HANDLE h_;
		// noncopyable:
		Semaphore( Semaphore & );
		void operator = ( Semaphore );
	};
#endif

	/**
	 * �����Ͷ���
	 *
	 * \tparam ObjT ������.
	 */
	template< typename ObjT > struct Lock
	{
		/**
		 * �����캯��.
		 *
		 * \note �ڹ��캯���н��м�������.
		 */
		Lock( ObjT& obj ) : obj_(obj) { obj_.lock(); }
		/**
		 * ����������.
		 *
		 * \note �����������н��н�������.
		 */
		~Lock() { obj_.unlock(); }
		private:
		ObjT& obj_;
		// noncopyable:
		Lock( Lock& );
		void operator = ( Lock );
	};

	/// ������������
	typedef Lock<Mutex> LockMutex;
	/// �ź���������
	// typedef Lock<Semaphore> LockSemaphore;

	/**
	 * ��ʱ�����Ͷ���
	 *
	 * \tparam ObjT ������.
	 */
	template< typename ObjT >
		struct TryLock
		{
			/**
			 * �����캯��.
			 *
			 * \param[in] obj ������.
			 * \param[in] timeout ��ʱʱ�䣬��λ:ms.
			 */
			TryLock( ObjT& obj, U32 timeout ) : obj_(obj)
			{
				locked_ = obj_.lock( timeout );
			}

			/// ����������.
			~TryLock()
			{
				if( locked_ )
					obj_.unlock();
			}

			/**
			 * �Ƿ�����ɹ�.
			 *
			 * \return �����ɹ����.
			 - true �����ɹ�
			 - false ������ʱ.
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
	 * ���̶���
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

	///�̴߳��������ǳ�Ա����
	extern "C" inline unsigned int __stdcall stbos_threadproc( void* param );
	/**
	 * �߳����Ͷ���.
	 *
	 * �߳��ฺ������̣߳�ʵ�����̲߳����ӿڣ����������߳̽ӿں������߳��౻���Ϊ
	 * �����࣬���ܴ�������,Ҫ����������Ҫ���߳�������һ�����߳����ͣ���ʵ���߳�
	 * �ӿں�����
	 * \note �߳��౻���Ϊ���ɸ��ƣ��κθ��Ʋ������ᵼ�±������.
	 */
	class Thread
	{
		public:	
			/**
			 * �̹߳��캯��.
			 *
			 * \param[in] n �߳�����
			 * \param[in] s �߳�ջ��С.
			 * \param[in] p �߳����ȼ�.
			 * \note �߳��౻���Ϊ���ɸ��ƣ��κθ��Ʋ������ᵼ�±������.
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
			 * �߳���������.
			 */
			virtual ~Thread() { NS_ThreadDelete( h_ ); }

			/**
			 * ��ȡ�߳�����.
			 *
			 * \return �߳�����
			 */
			std::string const& name() const { return name_; }

			/**
			 * �����̵߳����ü���.
			 *
			 * \return �̵߳����ü���.
			 */
			U8 inc_ref() { return ++refcount_; }

			/**
			 * ��С�̵߳����ü���.
			 *
			 * \return �̵߳����ü���.
			 */
			U8 dec_ref() { return --refcount_; }

			/// �����������̣߳�һ���̶߳���ֻ�ܱ� start() һ�Ρ�
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
			 * ֪ͨ���ȴ��߳�ֹͣ.
			 *
			 * \param[in] timeout ��ʱʱ�䣬��λ:ms.
			 * \return �߳��Ƿ��˳�.
			 - true  �߳����˳�.
			 - false �߳�û�˳�.
			 */
			bool stop( U32 timeout = TimeInfinite )
			{
				signal();
				return join( timeout );
			}

			/// ֪ͨ�߳�ֹͣ��
			void signal()
			{
				if( h_ != 0 ) 
				{
					signalled_ = true;
					do_signal();
				}
			}

			/** 
			 * ֪ͨ���߳�ֹͣʱ����ɵĸ��Ӳ���.
			 * \note ������ͨ�����ظú���������߳��˳�ʱ�ĸ��Ӳ���.
			 */
			virtual void do_signal() {}

			/**
			 * ���߳������ж��Ƿ��ѱ�ֹ֪ͨͣ����
			 *
			 * \return �߳��Ƿ�֪ͨ�˳�.
			 - true �̱߳�֪ͨ�˳�.
			 - false �߳�û�б�֪ͨ�˳�.
			 */
			bool signalled() { return signalled_; }

			/**
			 * �ȴ��߳�ֹͣ.
			 *
			 * \param[in] timeout ��ʱʱ�䣬��λ:ms.
			 * \return �߳��Ƿ��˳�.
			 - true �߳����˳�.
			 - false �߳�û�˳�.
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
			 * �ж��߳��Ƿ���������.
			 *
			 * \return �߳�״̬
			 - true  �߳�������.
			 - false �߳����˳�.
			 */
			bool active() { return ! join( 0 ); }

			/**
			 * ��ȡ�߳��˳���.
			 *
			 * \return �߳��˳���.
			 * \note join() �ɹ���ſ��Ե���.
			 */
			U32 exitcode() { return exitcode_; }


			bool is_curthread() { return NS_IsCurrentThreadByID(threadid_)!=0; }

		protected:


			/**
			 * �̵߳����к���
			 *
			 * \return �߳����к���������.
			 * \note ������ͨ�����ظú����������߳����к���.
			 * \par ʾ������:
			 * \code
			 U32 do_run()
			 {
			 U32 time = 50;

			 while( !signalled() )
			 {
			//do_something;
			Sleep( time );//�ó�CPU����Ȩ,�Ա������̵߳õ����л���.
			}
			return 0;
			}
			\endcode
			*/
			virtual U32 do_run() = 0;

			/** 
			 * ����δ������쳣
			 * \param[in] what �쳣�����ı�.
			 * \note ������ͨ�����ظú���������δ������쳣.
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

