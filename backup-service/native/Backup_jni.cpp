#define LOG_NDEBUG 0
#define LOG_TAG "Backup-JNI"

#include <jni.h>
#include <JNIHelp.h>
#include <android_runtime/AndroidRuntime.h>

#include "Backup.h"
#include "Mlog.h"
// ----------------------------------------------------------------------------

using namespace android;

// ----------------------------------------------------------------------------

struct fields_t {
    jfieldID    context;
};
static fields_t fields;

static Mutex sLock;

// ----------------------------------------------------------------------------

static sp<Backup> getBackup(JNIEnv* env, jobject thiz)
{
    Mutex::Autolock l(sLock);
    Backup* const p = (Backup*)env->GetIntField(thiz, fields.context);
    return sp<Backup>(p);
}

static sp<Backup> setBackup(JNIEnv* env, jobject thiz, const sp<Backup>& backup)
{
    Mutex::Autolock l(sLock);
    sp<Backup> old = (Backup*)env->GetIntField(thiz, fields.context);
    if (backup.get()) {
        backup->incStrong(thiz);
    }
    if (old != 0) {
        old->decStrong(thiz);
    }
    env->SetIntField(thiz, fields.context, (int)backup.get());
    return old;
}

// ----------------------------------------------------------------------------

static jstring get_dir_owner_str(JNIEnv* env, jobject thiz, jstring jPackageName)
{
    sp<Backup> backup = getBackup(env, thiz);
    if (backup == NULL ) {
        jniThrowException(env, "java/lang/IllegalStateException", NULL);
        return NULL;
    }

    if (jPackageName == NULL) {
        jniThrowException(env, "java/lang/IllegalArgumentException", NULL);
        return NULL;
    }
    
    const char *packageName = env->GetStringUTFChars(jPackageName, NULL);
    if (packageName == NULL) {  // Out of memory
        return NULL;
    }
    
    MLOGV("get_dir_owner_str package %s", packageName);
    
    const char* ownerStr = backup->getDirOwnerStr(packageName);
    
    MLOGV("get_dir_owner_str ownerStr %s", ownerStr);
    
    env->ReleaseStringUTFChars(jPackageName, packageName);
    packageName = NULL;
    
    return env->NewStringUTF(ownerStr);
}

static jboolean backup_data(JNIEnv* env, jobject thiz, jstring jPackageName, jstring jBackupDir)
{
    sp<Backup> backup = getBackup(env, thiz);
    if (backup == NULL ) {
        jniThrowException(env, "java/lang/IllegalStateException", NULL);
        return false;
    }

    if (jPackageName == NULL) {
        jniThrowException(env, "java/lang/IllegalArgumentException", NULL);
        return false;
    }
    
    if (jBackupDir == NULL) {
        jniThrowException(env, "java/lang/IllegalArgumentException", NULL);
        return false;
    }

    const char *packageName = env->GetStringUTFChars(jPackageName, NULL);
    if (packageName == NULL) {  // Out of memory
        return false;
    }
    
    const char* backupDir = env->GetStringUTFChars(jBackupDir, NULL);
    if (backupDir == NULL) { // Out of memory
        env->ReleaseStringUTFChars(jPackageName, packageName);
        packageName = NULL;
        return false;
    }
    MLOGV("backupData: package %s, backupDir %s", packageName, backupDir);
    
    status_t opStatus = backup->backupData(packageName, backupDir);
    MLOGV("backupData return: %d", opStatus);

    env->ReleaseStringUTFChars(jPackageName, packageName);
    packageName = NULL;
    
    env->ReleaseStringUTFChars(jBackupDir, backupDir);
    backupDir = NULL;

    return opStatus == 0;
}

static jboolean restore_data(JNIEnv* env, jobject thiz, jstring jBackupDir, jstring jPackageName, jint jPackageUid, jint jSystemUid, jboolean jOverwrite)
{
    sp<Backup> backup = getBackup(env, thiz);
    if (backup == NULL ) {
        jniThrowException(env, "java/lang/IllegalStateException", NULL);
        return false;
    }
    
    if (jBackupDir == NULL) {
        jniThrowException(env, "java/lang/IllegalArgumentException", NULL);
        return false;
    }

    if (jPackageName == NULL) {
        jniThrowException(env, "java/lang/IllegalArgumentException", NULL);
        return false;
    }
    
    if (jPackageUid == -1) {
        jniThrowException(env, "java/lang/IllegalArgumentException", NULL);
        return false;
    }
    
    if (jSystemUid == -1) {
        jniThrowException(env, "java/lang/IllegalArgumentException", NULL);
        return false;
    }
    
    status_t opStatus = UNKNOWN_ERROR; // not zero for error
    
    const char* packageName = NULL;
    const char* backupDir = NULL;

    packageName = env->GetStringUTFChars(jPackageName, NULL);
    if (packageName == NULL) {  // Out of memory
        goto bail;
    }
    
    backupDir = env->GetStringUTFChars(jBackupDir, NULL);
    if (backupDir == NULL) { // Out of memory
        goto bail;
    }
    
    MLOGV("restoreData: package = %s, backupDir = %s, packageUid = %d, systemUid = %d, overwrite = %d", packageName, backupDir, jPackageUid, jSystemUid, jOverwrite);
    
    opStatus = backup->restoreData(backupDir, packageName, jPackageUid, jSystemUid, jOverwrite);
    MLOGV("restoreData return: %d", opStatus);
    
/* Error */
bail:
    if (packageName) {
        env->ReleaseStringUTFChars(jPackageName, packageName);
        packageName = NULL;
    }
    
    if (backupDir) {
        env->ReleaseStringUTFChars(jBackupDir, backupDir);
        backupDir = NULL;
    }
    
    return opStatus == 0;
}

static void native_init(JNIEnv *env) {
    jclass clazz;

    clazz = env->FindClass("com/pekall/backup/Backup");
    if (clazz == NULL) {
        return;
    }

    fields.context = env->GetFieldID(clazz, "mNativeContext", "I");
    if (fields.context == NULL) {
        return;
    }
}

static void native_setup(JNIEnv* env, jobject thiz, jobject weak_this) {
    MLOGV("native_setup");
    sp<Backup> mp = new Backup();
    if (mp == NULL) {
        jniThrowException(env, "java/lang/RuntimeException", "Out of memory");
        return;
    }

    // Stow our new C++ Backup in an opaque field in the Java object.
    setBackup(env, thiz, mp);
}

static void native_release(JNIEnv* env, jobject thiz) {
    sp<Backup> mp = setBackup(env, thiz, 0);    
}

// ----------------------------------------------------------------------------

static JNINativeMethod gMethods[] = {
    {"native_init",             "()V",                                          (void *)native_init},
    {"native_setup",            "(Ljava/lang/Object;)V",                        (void *)native_setup},
    {"native_release",          "()V",                                          (void *)native_release},
    {"native_getDirOwnerStr",   "(Ljava/lang/String;)Ljava/lang/String;",       (void *)get_dir_owner_str},
    {"native_backupData",              "(Ljava/lang/String;Ljava/lang/String;)Z",      (void *)backup_data},
    {"native_restoreData",             "(Ljava/lang/String;Ljava/lang/String;IIZ)Z",   (void *)restore_data},
};

static const char* const kClassPathName = "com/pekall/backup/Backup";

// This function only registers the native methods
static int register_Backup(JNIEnv *env)
{
    return AndroidRuntime::registerNativeMethods(env,
                "com/pekall/backup/Backup", gMethods, NELEM(gMethods));
}

jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    JNIEnv* env = NULL;
    jint result = -1;

    if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        MLOGE("ERROR: GetEnv failed\n");
        goto bail;
    }
    assert(env != NULL);

    if (register_Backup(env) < 0) {
        MLOGE("ERROR: Backup native registration failed\n");
        goto bail;
    }

    /* success -- return valid version number */
    result = JNI_VERSION_1_4;

bail:
    return result;
}
