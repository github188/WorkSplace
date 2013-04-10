#include "tvservice.h"
#include "capture.h"
#include "tvplayer.h"


int tvservice_init()
{
	Ca_Init();
	tvplayer_init();
	return 0;
}
int tvservice_uninit()
{
	Ca_Uninit();
	return 0;
}
int tvservice_play()
{
	tvplayer_play();
	return 0;
}
int tvservice_stop()
{
	 tvplayer_stop();
	return 0;
}

int tvservice_setService(DVBService const *service)
{
	tvplayer_set_service(*service);
	return 0;
}
void tvservice_addTVNotify(TVNOTIFY callback)
{
}
void tvservice_delTVNotify(TVNOTIFY callback)
{

}
void tvservice_delAllTVNotify()
{

}
