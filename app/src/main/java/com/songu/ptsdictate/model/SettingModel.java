package com.songu.ptsdictate.model;

/**
 * Created by Administrator on 3/23/2018.
 */

public class SettingModel {
    public int mAudioQuality = 0;
    public int mMicSensitivity = 0;
    public boolean isVoiceAutoPause = false;
    public boolean isEmailNotification = false;
    public boolean isCommentScreen = false;
    public boolean isCommentMandatory = true;
    public boolean isIndexing = false;
    public boolean isEditingScreen = false;
    public boolean isArchiveFile = false;
    public boolean isUploadviaWifi = false;
    public boolean isSleepMode = true;
    public int mAchiveDays = 1;

    public String mFilePrefix;
    public int mDateFormat;
}
