#ifndef ANDROID_BACKUP_H
#define ANDROID_BACKUP_H

#include "IBackupService.h"

namespace android {

class Backup: public IBinder::DeathRecipient
{
public:
    virtual ~Backup();
    Backup();

    const char*     getDirOwnerStr(
        const char* packageName);

    status_t        backupData(
        const char* packageName,
        const char* backupDir);
        
    status_t        restoreData(
        const char* backupDir,
        const char* packageName,
        int packageUid,
        int systemUid,
        bool        overwrite);
private:
    virtual void binderDied(const wp<IBinder>& who);
    
    // helper function to obtain backup service handle
    static const sp<IBackupService>& getBackupService();

    static Mutex               sLock;
    static sp<IBackupService>  sBackupService;
    static sp<Backup> sBackup;
};
}; // namespace android

#endif // ANDROID_BACKUP_H
