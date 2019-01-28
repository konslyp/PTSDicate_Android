package com.songu.ptsdictate.doc;

import android.Manifest;

/**
 * Created by Administrator on 2/20/2017.
 */
public class Config {
    //public static String mBaseUrl = "http://192.168.1.107:88/rollgame/index.php/WebserviceController/";
    //public static String mImageBaseUrl = "http://192.168.1.107:88/rollgame/";

    public static String mBaseUrl = "https://www.etranscriptions.com.au/scripts/web_response.php?";

    public static String mLoginUrl = mBaseUrl + "Case=loginCheck";
    public static String mUploadUrl = mBaseUrl + "Case=UploadFile";

    public static int mDebug = 5;

    public static String mAppUpdateDate = "25 January 2017";
    public static String mContactEmail1 = "ptshelpdesk@outlook.com";
    public static String mContactEmail2 = "info@etranscriptions.com.au";
    public static String mContactPhone1 = "1300 768 476";
    public static String mContactPhone2 = "0800 884 323";

    public static String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,Manifest.permission.CALL_PHONE,Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.PROCESS_OUTGOING_CALLS};

    public static String[] STORAGE_PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};


    public static String[] MICPERMISSION = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,Manifest.permission.CALL_PHONE,Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.PROCESS_OUTGOING_CALLS};
}

