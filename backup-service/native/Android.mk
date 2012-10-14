LOCAL_PATH:= $(call my-dir)

#
# libbackup_client
#
include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
	IBackupService.cpp \
	Backup.cpp

LOCAL_SHARED_LIBRARIES:= \
    libutils \
    libbinder \
    libcutils \

LOCAL_MODULE:= libbackup_client
LOCAL_MODULE_TAGS := optional

include $(BUILD_STATIC_LIBRARY)

#
# libbackup_jni
#

include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
	Backup_jni.cpp \

LOCAL_C_INCLUDES += \
	$(JNI_H_INCLUDE) \
	
LOCAL_SHARED_LIBRARIES:= \
    libandroid_runtime \
    libnativehelper \
    libutils \
    libbinder \

LOCAL_STATIC_LIBRARIES:= \
	libbackup_client
	
LOCAL_MODULE:= libbackup_jni
LOCAL_MODULE_TAGS:= optional

include $(BUILD_SHARED_LIBRARY)

#
# backupserver
#

include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
    BackupService.cpp	\
    main_backupserver.cpp


LOCAL_SHARED_LIBRARIES:= \
    libutils \
    libbinder \
    libcutils \

LOCAL_STATIC_LIBRARIES:= \
    libbackup_client

LOCAL_MODULE:= backupserver
LOCAL_MODULE_TAGS:= optional

include $(BUILD_EXECUTABLE)



