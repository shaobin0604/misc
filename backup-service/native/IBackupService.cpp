#include "IBackupService.h"

namespace android {

enum {
    BACKUP_DATA = IBinder::FIRST_CALL_TRANSACTION,
    RESTORE_DATA,
    GET_DIR_OWNER_STR,
};

class BpBackupService: public BpInterface<IBackupService>
{
public:
    BpBackupService(const sp<IBinder>& impl)
        : BpInterface<IBackupService>(impl)
    {
    }
    
    status_t backupData(const char* packageName, const char* backupDir)
    {
        Parcel data, reply;
        data.writeInterfaceToken(IBackupService::getInterfaceDescriptor());
        data.writeCString(packageName);
        data.writeCString(backupDir);
        remote()->transact(BACKUP_DATA, data, &reply);
        return reply.readInt32();
    }
    
    status_t restoreData(const char* backupDir, const char* packageName, int packageUid, int systemUid, bool overwrite)
    {
        Parcel data, reply;
        data.writeInterfaceToken(IBackupService::getInterfaceDescriptor());
        data.writeCString(backupDir);
        data.writeCString(packageName);
        data.writeInt32(packageUid);
        data.writeInt32(systemUid);
        data.writeInt32(overwrite);
        remote()->transact(RESTORE_DATA, data, &reply);
        return reply.readInt32();
    }
        
    const char* getDirOwnerStr(const char* packageName)
    {
        Parcel data, reply;
        data.writeInterfaceToken(IBackupService::getInterfaceDescriptor());
        data.writeCString(packageName);
        remote()->transact(GET_DIR_OWNER_STR, data, &reply);
        return reply.readCString();
    }
};

IMPLEMENT_META_INTERFACE(BackupService, "IBackupService");

// ----------------------------------------------------------------------

status_t BnBackupService::onTransact(
    uint32_t code, const Parcel& data, Parcel* reply, uint32_t flags)
{
    switch(code) {
        case BACKUP_DATA: {
            CHECK_INTERFACE(IBackupService, data, reply);
            const char* packageName = data.readCString();
            const char* backupDir = data.readCString();
            reply->writeInt32(backupData(packageName, backupDir));
            return NO_ERROR;
        } break;
        case RESTORE_DATA: {
            CHECK_INTERFACE(IBackupService, data, reply);
            const char* backupDir = data.readCString();
            const char* packageName = data.readCString();
            int packageUid = data.readInt32();
            int systemUid = data.readInt32();
            bool overwrite = data.readInt32();
            reply->writeInt32(restoreData(backupDir, packageName, packageUid, systemUid, overwrite));
            return NO_ERROR;
        } break;
        case GET_DIR_OWNER_STR: {
            CHECK_INTERFACE(IBackupService, data, reply);
            const char* packageName = data.readCString();
            const char* dirOwnerStr = getDirOwnerStr(packageName);
            reply->writeCString(dirOwnerStr);
            free((void *)dirOwnerStr);
            return NO_ERROR;
        } break;
        default:
            return BBinder::onTransact(code, data, reply, flags);
    }
}

// ----------------------------------------------------------------------------

}; // namespace android
