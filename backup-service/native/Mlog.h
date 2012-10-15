#ifndef ANDROID_MLOG_H
#define ANDROID_MLOG_H

#include <utils/Log.h>

#define MLOGD(fmt, args...) 	LOGD("[%d: %s] _____ " fmt "\n", __LINE__, __FUNCTION__, ##args)
#define MLOGI(fmt, args...) 	LOGI("[%d: %s] _____ " fmt "\n", __LINE__, __FUNCTION__, ##args)
#define MLOGV(fmt, args...) 	LOGV("[%d: %s] _____ " fmt "\n", __LINE__, __FUNCTION__, ##args)
#define MLOGW(fmt, args...) 	LOGW("[%d: %s] _____ " fmt "\n", __LINE__, __FUNCTION__, ##args)
#define MLOGE(fmt, args...) 	LOGE("[%d: %s] _____ " fmt "\n", __LINE__, __FUNCTION__, ##args)

#endif /* ANDROID_MLOG_H */
