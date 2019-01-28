package com.songu.ptsdictate.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;


import com.songu.ptsdictate.BuildConfig;
import com.songu.ptsdictate.doc.Globals;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Administrator on 2/20/2017.
 */
public class Utils {
    public static String getResourceString(Context mContext, int res) {
        return mContext.getResources().getString(res);
    }

    public static String getPhotoFileName() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "'IMG'_yyyyMMdd_HHmmss");
        return dateFormat.format(date) + ".jpg";
    }

    public static String getRealPathFromURI(Activity act, Uri contentURI) {
        Cursor cursor = act.getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            return contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(idx);
        }
    }

    public static String getPriceValue(String value) {
        String result = "";
        while (value.length() > 3) {
            result = "," + value.substring(value.length() - 3) + result;
            value = value.substring(0, value.length() - 3);
        }
        result = value + result;
        return result;
    }

    public static boolean getAutoSaveSetting(Context mContext)
    {
        SharedPreferences sp = mContext.getSharedPreferences("autosave", Context.MODE_PRIVATE);
        return sp.getBoolean("autosave",false);
    }
    public static void saveAutoSaveSetting(Context mContext,boolean isAutosave)
    {
        SharedPreferences sp = mContext.getSharedPreferences("autosave", Context.MODE_PRIVATE);

        sp.edit().putBoolean("autosave", isAutosave).apply();
    }
    public static void saveSetting(Context mContext)
    {
        SharedPreferences sp = mContext.getSharedPreferences(Globals.mAccount.mID, Context.MODE_PRIVATE);

        sp.edit().putBoolean("voice", Globals.mSetting.isVoiceAutoPause).apply();
        sp.edit().putBoolean("email", Globals.mSetting.isEmailNotification).apply();
        sp.edit().putBoolean("comment", Globals.mSetting.isCommentScreen).apply();
        sp.edit().putBoolean("indexing", Globals.mSetting.isIndexing).apply();
        sp.edit().putBoolean("editscreen", Globals.mSetting.isEditingScreen).apply();
        sp.edit().putBoolean("archive", Globals.mSetting.isArchiveFile).apply();
        sp.edit().putBoolean("upload", Globals.mSetting.isUploadviaWifi).apply();
        sp.edit().putBoolean("sleep", Globals.mSetting.isSleepMode).apply();
        sp.edit().putInt("quality", Globals.mSetting.mAudioQuality).apply();
        sp.edit().putInt("sensitivity", Globals.mSetting.mMicSensitivity).apply();
        sp.edit().putString("filename", Globals.mSetting.mFilePrefix).apply();
        sp.edit().putInt("dateformat",Globals.mSetting.mDateFormat).apply();
        sp.edit().putBoolean("comment_mandatory",Globals.mSetting.isCommentMandatory).apply();
        sp.edit().putInt("adays",Globals.mSetting.mAchiveDays).apply();

    }
    public static void loadSetting(Context mContext)
    {
        Log.e("Load Setting","Done");
        SharedPreferences sp = mContext.getSharedPreferences(Globals.mAccount.mID, Context.MODE_PRIVATE);
        Globals.mSetting.isVoiceAutoPause = sp.getBoolean("voice",false);
        Globals.mSetting.isEmailNotification = sp.getBoolean("email",false);
        Globals.mSetting.isCommentScreen = sp.getBoolean("comment",false);
        Globals.mSetting.isIndexing = sp.getBoolean("indexing",false);
        Globals.mSetting.isEditingScreen = sp.getBoolean("editscreen",false);
        Globals.mSetting.isArchiveFile = sp.getBoolean("archive",false);
        Globals.mSetting.isUploadviaWifi = sp.getBoolean("upload",false);
        Globals.mSetting.isSleepMode = sp.getBoolean("sleep",true);
        Globals.mSetting.mAudioQuality = sp.getInt("quality",0);
        Globals.mSetting.mMicSensitivity = sp.getInt("sensitivity",100);
        Log.e("Test","11111");
        Globals.mSetting.mFilePrefix = sp.getString("filename",Utils.filterName(Globals.mAccount.mID));
        Log.e("Test","2222");
        Globals.mSetting.mDateFormat = sp.getInt("dateformat",0);
        Globals.mSetting.isCommentMandatory = sp.getBoolean("comment_mandatory",false);
        Globals.mSetting.mAchiveDays = sp.getInt("adays",1);
        Log.e("FileName",Globals.mSetting.mFilePrefix);

    }
    public static void saveIndex(Context mContext)
    {
        SharedPreferences sp = mContext.getSharedPreferences(Globals.mAccount.mID + "index", Context.MODE_PRIVATE);
        sp.edit().putInt("index", Globals.g_fileIndex).apply();
    }
    public static void loadIndex(Context mContext)
    {
        SharedPreferences sp = mContext.getSharedPreferences(Globals.mAccount.mID + "index", Context.MODE_PRIVATE);
        Globals.g_fileIndex = sp.getInt("index",1);
        Log.e("InDex-------------",String.valueOf(Globals.g_fileIndex));
    }

    public static String filterName(String name)
    {
        if (name == null) return "";
        String ss = name.replaceAll("[;\\/:*?\"<>|&$']","");
        if (ss.equals("")) return "";
        ss = ss.substring(0,1).toUpperCase() + ss.substring(1);
        return ss;
    }

    public static String getAppVersion(Context aContext) {
        PackageInfo pInfo = null;
        String aAppVersion = BuildConfig.VERSION_NAME;
        try {
            pInfo = aContext.getPackageManager().getPackageInfo(aContext.getPackageName(), 0);
            aAppVersion = "" + pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return aAppVersion;
    }
    public static void clearUsername(Context mContext)
    {
        SharedPreferences sp = mContext.getSharedPreferences("login", Context.MODE_PRIVATE);
        sp.edit().putString("id", "").apply();
        sp.edit().putString("pw", "").apply();
    }
    public static void savePreference(Context mContext,boolean isLogin){
        SharedPreferences sp = mContext.getSharedPreferences("login", Context.MODE_PRIVATE);
        if (isLogin)
            sp.edit().putInt("login",1).apply();
        else
            sp.edit().putInt("login",0).apply();
        sp.edit().putString("uid", Globals.mAccount.mNo).apply();
        sp.edit().putString("id", Globals.mAccount.mID).apply();
        sp.edit().putString("name", Globals.mAccount.mName).apply();
        sp.edit().putString("email", Globals.mAccount.mEmail).apply();
        sp.edit().putString("pw", Globals.mAccount.mPassword).apply();
        sp.edit().putBoolean("keep", Globals.mAccount.mIsKeepInfo).apply();
    }
    public static int isLogin(Context mContext)
    {
        SharedPreferences sp = mContext.getSharedPreferences("login", Context.MODE_PRIVATE);
        return sp.getInt("login",0);
    }
    public static void loadPreference(Context mContext)
    {
        SharedPreferences sp = mContext.getSharedPreferences("login", Context.MODE_PRIVATE);
        Globals.mAccount.mIsKeepInfo = sp.getBoolean("keep",false);
        Globals.mAccount.mNo = sp.getString("uid","");
        Globals.mAccount.mID = sp.getString("id","");
        Globals.mAccount.mPassword = sp.getString("pw","");
        Globals.mAccount.mName = sp.getString("name","");
        Globals.mAccount.mEmail = sp.getString("email","");


    }
    public static void clearPreference(Context mContext) {
        SharedPreferences sp = mContext.getSharedPreferences("login", Context.MODE_PRIVATE);
        sp.edit().putInt("login", 0).apply();
        sp.edit().putString("uid", "").apply();
        sp.edit().putString("phone", "").apply();
    }

    public static String simpleTimeString (long millis)
    {
        StringBuffer buf = new StringBuffer();

        int hours = (int) (millis / (1000 * 60 * 60));
        int minutes = (int) ((millis % (1000 * 60 * 60)) / (1000 * 60));
        int seconds = (int) (((millis % (1000 * 60 * 60)) % (1000 * 60)) / 1000);

        if (hours == 0 && minutes == 0)
        {
            if (seconds == 0)
                buf.append(String.valueOf(1) + "s");
            else buf.append(String.valueOf(seconds) + "s");
        }
        else if (hours == 0)
        {
            buf.append(String.valueOf(minutes) + "m " + String.valueOf(seconds) + "s");
        }
        else
        {
            buf.append(String.valueOf(hours) + "h " + String.valueOf(minutes) + "m " + String.valueOf(seconds) + "s");
        }
        return buf.toString();
    }
    public static String getTimeString(long millis) {
        StringBuffer buf = new StringBuffer();

        int hours = (int) (millis / (1000 * 60 * 60));
        int minutes = (int) ((millis % (1000 * 60 * 60)) / (1000 * 60));
        int seconds = (int) (((millis % (1000 * 60 * 60)) % (1000 * 60)) / 1000);

        buf.append(String.format("%02d", hours)).append(":")
                .append(String.format("%02d", minutes)).append(":")
                .append(String.format("%02d", seconds));

        return buf.toString();
    }

    public static String readableFileSize(long size)
    {
        float aFileLength;

        float aLength = 0.0f;

        aLength = (float) (size / 1024.00);

        aFileLength = (float) (aLength / 1024.00);

        return " " + new DecimalFormat("##.##").format(aFileLength) + " MB";
    }

    public static boolean isUploadInternetOn(Context aContext,boolean isUploadWifi) {

        if (aContext == null)
            return false;
        //
        boolean aResult = false;
        //
        ConnectivityManager aConnecMan = (ConnectivityManager) aContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (isUploadWifi) {

            NetworkInfo.State wifi = aConnecMan.getNetworkInfo(1).getState();

            if ((wifi == NetworkInfo.State.CONNECTED)
                    || (wifi == NetworkInfo.State.CONNECTING)) {
                aResult = true;
            } else if (wifi == NetworkInfo.State.DISCONNECTED) {
                aResult = false;
            }
        } else {

            aResult = isInternetOn(aContext);

        }
        return aResult;
    }

    public static boolean isInternetOn(Context aContext) {

        //
        boolean aResult = false;

        //
        ConnectivityManager aConnecMan = (ConnectivityManager) aContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        //
        if ((aConnecMan.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTED)
                || (aConnecMan.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTING)
                || (aConnecMan.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTING)
                || (aConnecMan.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTED)) {
            aResult = true;
        } else if ((aConnecMan.getNetworkInfo(0).getState() == NetworkInfo.State.DISCONNECTED)
                || (aConnecMan.getNetworkInfo(1).getState() == NetworkInfo.State.DISCONNECTED)) {
            aResult = false;
        }

        return aResult;
    }


}
