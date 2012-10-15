// System headers required for setgroups, etc.

#include <binder/IPCThreadState.h>
#include <binder/ProcessState.h>
#include <binder/IServiceManager.h>

#include <private/android_filesystem_config.h>

#include "BackupService.h"
#include "Mlog.h"

using namespace android;

int main(int argc, char** argv)
{
    sp<ProcessState> proc(ProcessState::self());
    sp<IServiceManager> sm = defaultServiceManager();
    MLOGI("ServiceManager: %p", sm.get());
    BackupService::instantiate();
    ProcessState::self()->startThreadPool();
    IPCThreadState::self()->joinThreadPool();
}
