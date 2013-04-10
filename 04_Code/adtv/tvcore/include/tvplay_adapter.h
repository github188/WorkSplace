#ifndef JOYSEE_TVPLAY_ADAPTER_H_
#define JOYSEE_TVPLAY_ADAPTER_H_

#include "typ.h"

#ifdef __cplusplus
extern "C" {
#endif 

	int tvplay_adapter_stop();
	int tvplay_adapter_play(IN U16 vpid,IN U8 vstype,IN U16 apid,IN U8 astype);
	int tvplay_adapter_putTSData(IN BYTE *pTSData,IN U32 iDataSize);
	int tvplay_setVideoWindow(int x,int y,int width,int height);
	int tvplay_setScreenMode(int mode);
	int tvplay_set_black_policy(int blackout);
	int tvplay_getVolume();
	int tvplay_setVolume(float volume);
	int tvplay_SetChannel(int index);
	int tvplay_SetMute(int sign);
	int tvplay_ClearVideoLayer(int sign); //true: hide
	int tvplay_adapter_clsBuffer();

#ifdef __cplusplus
}
#endif 

#endif // defined(JOYSEE_TVPLAY_ADAPTER_H_)

