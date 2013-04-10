#pragma once

#include "tvcomm.h"

int tvservice_init();
int tvservice_uninit();
int tvservice_play();
int tvservice_stop();
int tvservice_setService(DVBService const *service);
void tvservice_addTVNotify(TVNOTIFY callback);
void tvservice_delTVNotify(TVNOTIFY callback);
void tvservice_delAllTVNotify();
