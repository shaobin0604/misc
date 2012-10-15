#ifndef ANDROID_IBACKUPSERVICE_H
#define ANDROID_IBACKUPSERVICE_H

#include <binder/IInterface.h>
#include <binder/Parcel.h>

namespace android {

class Parcel;
class Surface;
class IStreamSource;
class ISurfaceTexture;

class IBackupService: public IInterface
{
public:
    DECLARE_META_INTERFACE(BackupService);

    virtual const char* getDirOwnerStr(const char* packageName) = 0;
    virtual status_t backupData(const char* packageName, const char* backupDir) = 0;
    virtual status_t restoreData(const char* backupDir, const char* packageName, int packageUid, int systemUid, bool overwrite) = 0;
};

// ----------------------------------------------------------------------------

class BnBackupService: public BnInterface<IBackupService>
{
public:
    virtual status_t    onTransact( uint32_t code,
                                    const Parcel& data,
                                    Parcel* reply,
                                    uint32_t flags = 0);
};

}; // namespace android

#endif // ANDROID_IBACKUPSERVICE_H
