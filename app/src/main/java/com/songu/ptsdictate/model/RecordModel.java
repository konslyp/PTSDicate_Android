package com.songu.ptsdictate.model;

/**
 * Created by Administrator on 3/26/2018.
 */

public class RecordModel {
    public int mNo;
    public int mLocalNo;
    public String mName;
    public String mElapse;
    public int mIsAutoSave;
    public int mIsUpload;
    public String mSize;
    public String mPath;
    public String mComment;
    public int isComment;
    public String mUploadTime = "";
    public String mIndexData = "";
    public int mRate;
    public int mUploaded = 0;

    public boolean isSelect = false;
    public boolean isPlaying = false;

    public int uploadProgress;

}
