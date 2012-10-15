#define LOG_NDEBUG 0
#define LOG_TAG "Backup"

#include <utils/threads.h>
#include <binder/IServiceManager.h>

#include "Backup.h"
#include "Mlog.h"

namespace android {

// client singleton for backup service binder interface
Mutex Backup::sLock;
sp<IBackupService> Backup::sBackupService;
sp<Backup> Backup::sBackup;

// establish binder interface to backup service
/* static */
const sp<IBackupService>& Backup::getBackupService()
{
    Mutex::Autolock _l(sLock);
    if (sBackupService.get() == 0) {
        sp<IServiceManager> sm = defaultServiceManager();
        sp<IBinder> binder;
        do {
            binder = sm->getService(String16("backupservice"));
            if (binder != 0)
                break;
            MLOGW("BackupService not published, waiting...");
            usleep(500000); // 0.5 s
        } while(true);
        binder->linkToDeath(sBackup);
        sBackupService = interface_cast<IBackupService>(binder);
    }
    LOGE_IF(sBackupService==0, "no BackupService!?");
    return sBackupService;
}

// ---------------------------------------------------------------------------

Backup::Backup()
{
    sBackup = this;
    getBackupService();
}

Backup::~Backup() {
    sBackup = NULL;
}

const char* Backup::getDirOwnerStr(const char* packageName) {
    if (sBackupService == NULL) {
		getBackupService();
    }
    return sBackupService->getDirOwnerStr(packageName);
}


status_t Backup::backupData(const char* packageName, const char* backupDir) {
    if (sBackupService == NULL) {
		getBackupService();
    }
    return sBackupService->backupData(packageName, backupDir);
}

status_t Backup::restoreData(const char* backupDir, const char* packageName, int packageUid, int systemUid, bool overwrite) {
    if (sBackupService == NULL) {
		getBackupService();
    }
    return sBackupService->restoreData(backupDir, packageName, packageUid, systemUid, overwrite);
}

void Backup::binderDied(const wp<IBinder>& who) {
    MLOGW("[%s]%d > backup service died!", __FUNCTION__, __LINE__);
    if(sBackupService != 0) {
        Mutex::Autolock _l(sLock);
        sBackupService.clear();

        // TODO: notify jni part that backup service die
    }
	MLOGW("[%s]%d > start sleep", __FUNCTION__, __LINE__);
    sleep(2);	// 2s
	MLOGW("[%s]%d > start get backup service", __FUNCTION__, __LINE__);
    getBackupService();
}
}; // namespace android
