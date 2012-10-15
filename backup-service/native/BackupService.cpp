#define LOG_NDEBUG 0
#define LOG_TAG "BackupService"

#include <stdlib.h>
#include <stdio.h>

#include <binder/IServiceManager.h>

#include "BackupService.h"
#include "Mlog.h"

#define MAX_CMD_LEN (1024)
#define MAX_FILE_PATH_LEN (1024)
#define MAX_LINE_LEN (1024)
#define BUSYBOX_PATH "/system/xbin/busybox"

namespace android {

/* static */
void BackupService::instantiate() {
    defaultServiceManager()->addService(
            String16("backupservice"), new BackupService());
}

status_t BackupService::backupData(const char* packageName, const char* backupDir) {
    MLOGV("backup E, packageName = %s, backupDir = %s", packageName, backupDir);
    
    char cmd[MAX_CMD_LEN] = {0};
    
    // use busybox tar to backup
    
    /*
      BusyBox v1.20.0 (2012-08-22 21:36:06 CDT) multi-call binary.

        Usage: tar -[cxtzjahmvO] [-X FILE] [-T FILE] [-f TARFILE] [-C DIR] [FILE]...

        Create, extract, or list files from a tar file

        Operation:
            c	Create
            x	Extract
            t	List
            f	Name of TARFILE ('-' for stdin/out)
            C	Change to DIR before operation
            v	Verbose
            z	(De)compress using gzip
            j	(De)compress using bzip2
            a	(De)compress using lzma
            O	Extract to stdout
            h	Follow symlinks
            m	Don't restore mtime
            exclude	File to exclude
            X	File with names to exclude
            T	File with names to include
    */
    
    // backup all data files except files in lib directory
    //
    // cmd: busybox tar -cvz --exclude lib -f {backup_dir}/{pacakge_name}.tar.gz -C /data/data {package_name}
    snprintf(cmd, MAX_CMD_LEN, "%s tar -cvz --exclude lib -f %s/%s.tar.gz -C /data/data %s", BUSYBOX_PATH, backupDir, packageName, packageName);
    int ret = system(cmd);
    MLOGV("exec: %s ret = %d", cmd, ret);
    
    MLOGV("backup X, ret = %d", ret);
    return ret;
}

const char* BackupService::getDirOwnerStr(const char* packageName) {
    MLOGV("getDirOwnerStr E, packageName = %s", packageName);
    
    // the shell cmd
    char cmd[MAX_CMD_LEN] = {0};
    
    // the shell cmd output line
    char* output = (char*) malloc(MAX_LINE_LEN);
    
    if (!output) {
        MLOGE("fail to malloc %d bytes", MAX_LINE_LEN);
        return "";
    }
    
    snprintf(cmd, MAX_CMD_LEN, "/system/bin/ls -l /data/data | %s grep %s", BUSYBOX_PATH, packageName);
    
    FILE* fis = popen(cmd, "r");
    
    fgets(output, MAX_LINE_LEN, fis);
    
    MLOGD("exec: %s output: \n%s", cmd, output);
    
    pclose(fis);
    
    return output;
}


status_t BackupService::restoreData(const char* backupDir, const char* packageName, int packageUid, int systemUid, bool overwrite) {
    MLOGV("restore E, backupDir = %s, packageName = %s, packageUid = %d, systemUid = %d, overwrite = %d", backupDir, packageName, packageUid, systemUid, overwrite);
    
    int ret;
    char cmd[MAX_CMD_LEN] = {0};
    char dataDir[MAX_FILE_PATH_LEN] = {0};

    snprintf(dataDir, MAX_FILE_PATH_LEN, "/data/data/%s", packageName);
    
    // check for existence
    bool fileExist = (access(dataDir, 0) == 0);
    
    MLOGV("dataDir = %s exist = %d", dataDir, fileExist);
    
    if (fileExist && !overwrite) {
        MLOGW("not overwrite, skip restore");
        return false;
    }
    
    // use busybox tar to restore
    // cmd: busybox tar -xvzm -f {backup_dir}/{package_name}.tar.gz -C /data/data
    snprintf(cmd, MAX_CMD_LEN, "%s tar -xvzm -f %s/%s.tar.gz -C /data/data/", BUSYBOX_PATH, backupDir, packageName);
    ret = system(cmd);
    MLOGV("exec %s ret = %d", cmd, ret);
    
    // chown uanem:gname for all non-lib directories
    snprintf(cmd, MAX_CMD_LEN, "%s chown -R %d.%d %s", BUSYBOX_PATH, packageUid, packageUid, dataDir);
    ret = system(cmd);
    MLOGV("exec %s ret = %d", cmd, ret);
    
    // chown system:system for lib directories
    snprintf(cmd, MAX_CMD_LEN, "%s chown -R %d.%d %s/lib", BUSYBOX_PATH, systemUid, systemUid, dataDir);
    ret = system(cmd);
    MLOGV("exec %s ret = %d", cmd, ret);
    
    MLOGV("restore X, ret = %d", ret);
    return ret;
}

BackupService::BackupService()
{
    MLOGV("BackupService created");
}

BackupService::~BackupService()
{
    MLOGV("BackupService destroyed");
}
} // namespace android

