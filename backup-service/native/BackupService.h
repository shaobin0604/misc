#ifndef ANDROID_BACKUPSERVICE_H
#define ANDROID_BACKUPSERVICE_H


#include <utils/Errors.h>
#include "IBackupService.h"

namespace android {

class BackupService : public BnBackupService
{
    class Client;
    
public:
    static  void                instantiate();

    // IBackupService interface
    virtual const char* getDirOwnerStr(const char* packageName);
    virtual status_t backupData(const char* packageName, const char* backupDir);
    virtual status_t restoreData(const char* backupDir, const char* packageName, int packageUid, int systemUid, bool overwrite);
    
            BackupService();
    virtual ~BackupService();
};

// ----------------------------------------------------------------------------

}; // namespace android

#endif // ANDROID_BACKUPSERVICE_H
