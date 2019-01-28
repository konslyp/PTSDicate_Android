package com.songu.ptsdictate.activity;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.rollbar.android.Rollbar;
import com.songu.ptsdictate.MainApplication;
import com.songu.ptsdictate.R;
import com.songu.ptsdictate.database.DBManager;
import com.songu.ptsdictate.doc.Config;
import com.songu.ptsdictate.doc.Enums;
import com.songu.ptsdictate.doc.Globals;
import com.songu.ptsdictate.fragment.LibraryFragment;
import com.songu.ptsdictate.fragment.RecordFragment;
import com.songu.ptsdictate.fragment.SettingFragment;
import com.songu.ptsdictate.fragment.UploadFragment;
import com.songu.ptsdictate.fragment.library.CommentFragment;
import com.songu.ptsdictate.fragment.library.RenameFragment;
import com.songu.ptsdictate.fragment.setting.AboutFragment;
import com.songu.ptsdictate.fragment.setting.AudioQualityFragment;
import com.songu.ptsdictate.fragment.setting.FilenameFormatFragment;
import com.songu.ptsdictate.fragment.setting.MicrophoneSensitivityFragment;
import com.songu.ptsdictate.fragment.setting.ProfileFragment;
import com.songu.ptsdictate.fragment.setting.about.ContactUsFragment;
import com.songu.ptsdictate.fragment.setting.about.PasswordGuideFragment;
import com.songu.ptsdictate.model.RecordModel;
import com.songu.ptsdictate.service.IServiceResult;
import com.songu.ptsdictate.service.OverlayService;
import com.songu.ptsdictate.service.UploadService;
import com.songu.ptsdictate.util.NotificationUtil;
import com.songu.ptsdictate.util.Utils;

import java.util.List;

/**
 * Created by Administrator on 3/22/2018.
 */

public class MainActivity extends FragmentActivity implements View.OnClickListener,IServiceResult{


    private RelativeLayout btnTab1,btnTab2,btnTab3,btnTab4,btnTab5;
    private ImageView imgTab1,imgTab2,imgTab3,imgTab4,imgTab5;
    private TextView txtTab1,txtTab2,txtTab3,txtTab4;
    public Fragment currentFragment;
    public LinearLayout layoutTabBar;
    public boolean isLogout = false;
    public NotificationUtil myNotificationMgr;




    private BroadcastReceiver callReceiver = new CallReceiver();

    public class CallReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("call"))
            {
                if (currentFragment instanceof RecordFragment) {
                    ((RecordFragment) currentFragment).callAutosave();
                }
            }
            else if (intent.getAction().equals("ring"))
            {
                if (currentFragment instanceof RecordFragment) {
                    if (((RecordFragment) currentFragment).isRecording == 1) {
                        ((RecordFragment) currentFragment).stopRecording();
                        ((RecordFragment) currentFragment).isPhonecall = true;
                    }
                }

            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initSetting();
        if (Globals.g_database == null)
            Globals.g_database = new DBManager(this);

        IntentFilter filter = new IntentFilter("call");
        filter.addAction("ring");
        this.registerReceiver(callReceiver, filter);
        ((MainApplication)getApplication()).getRollbar().debug("Starting MainActivity");
        grantPermission();
        initView();
        initOverlayService();
        Globals.e_mode = Enums.MODE.LIBRARY;
        Globals.g_existFile = null;
        Globals.g_isExistRecord = false;
        startService(new Intent(this, UploadService.class));

        setFragment();
        cleanArchiveDatas();
    }
    public void cleanArchiveDatas()
    {
        if (Globals.mSetting.isArchiveFile) {
            List<RecordModel> recordFiles = Globals.g_database.getAllRecordlist();
            long currentTime = System.currentTimeMillis();
            for (int i = 0; i < recordFiles.size(); i++) {
                if (recordFiles.get(i).mUploadTime == null || recordFiles.get(i).mUploadTime.equals("")) continue;
                long delta = ((long)Globals.mSetting.mAchiveDays) * 1000 * 60 * 60 * 24L;
                long time = (Long.parseLong(recordFiles.get(i).mUploadTime)) + delta;
                if (recordFiles.get(i).mUploaded == 1 && (time<= currentTime)) {
                    Globals.g_database.deleteRecordFile(recordFiles.get(i).mLocalNo);
                }
            }
        }
    }
    public void initSetting()
    {
        Utils.loadSetting(this);
        if (Globals.mSetting.isSleepMode) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//            Intent m = new Intent("keep");
//            sendBroadcast(m);
        }
        else
        {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//            Intent m = new Intent("unkeep");
//            sendBroadcast(m);
        }
    }
    public void initOverlayService()
    {
//        if(Build.VERSION.SDK_INT >= 23) {
//            if (!Settings.canDrawOverlays(MainActivity.this)) {
//                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
//                        Uri.parse("package:" + getPackageName()));
//                startActivityForResult(intent, 10);
//            }
//        }

    }
    @Override
    public void onResume()
    {
        super.onResume();
        Utils.loadPreference(this);
        Globals.g_database = new DBManager(this);
        isLogout = false;
        checkAutosave();
        myNotificationMgr.dismissNotification();
        initSetting();
    }

    public void checkAutosave()
    {
        if (Utils.getAutoSaveSetting(this))
        {
            Utils.saveAutoSaveSetting(this,false);
            new MaterialDialog.Builder(this)
                    .title("PTS Dictate")
                    .content("Record file is autosaved.")
                    .positiveText("Yes")
                    .titleColor(Color.BLACK)
                    .contentColor(Color.GRAY)
                    .show();
            return;
        }
        Utils.saveAutoSaveSetting(this,false);
    }
    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
    public void grantPermission()
    {
        if (Build.VERSION.SDK_INT >= 23) {
            if (!hasPermissions(this, Config.PERMISSIONS))
            {
                ActivityCompat.requestPermissions(this, Config.PERMISSIONS, 1);
            }
        }
    }


    public void initView()
    {
        myNotificationMgr = new NotificationUtil(this);


        btnTab1 = (RelativeLayout) this.findViewById(R.id.existing_dictations);
        btnTab2 = (RelativeLayout) this.findViewById(R.id.upload);
        btnTab3 = (RelativeLayout) this.findViewById(R.id.record);
        btnTab4 = (RelativeLayout) this.findViewById(R.id.settings);
        btnTab5 = (RelativeLayout) this.findViewById(R.id.logout);

        txtTab1 = (TextView) this.findViewById(R.id.existing_dictations_TXT);
        txtTab2 = (TextView) this.findViewById(R.id.upload_TXT);
        txtTab3 = (TextView) this.findViewById(R.id.record_TXT);
        txtTab4 = (TextView) this.findViewById(R.id.settings_TXT);

        imgTab1 = (ImageView) this.findViewById(R.id.existing_dictations_IMG);
        imgTab2 = (ImageView) this.findViewById(R.id.upload_IMG);
        imgTab4 = (ImageView) this.findViewById(R.id.settings_IMG);

        layoutTabBar = (LinearLayout) this.findViewById(R.id.footer_main);

        btnTab1.setOnClickListener(this);
        btnTab2.setOnClickListener(this);
        btnTab3.setOnClickListener(this);
        btnTab4.setOnClickListener(this);
        btnTab5.setOnClickListener(this);
    }

    public void initTabUI()
    {
        txtTab1.setTextColor(0xff808080);
        txtTab2.setTextColor(0xff808080);
        txtTab3.setTextColor(0xff808080);
        txtTab4.setTextColor(0xff808080);

        imgTab1.setImageDrawable(getResources().getDrawable(R.drawable.icn_existing_dictations_disable));
        imgTab2.setImageDrawable(getResources().getDrawable(R.drawable.icn_uploads_disable));
        imgTab4.setImageDrawable(getResources().getDrawable(R.drawable.icn_settings_disable));
    }
    public void showHideTab(boolean isShow)
    {
        if (isShow)
            layoutTabBar.setVisibility(View.VISIBLE);
        else
            layoutTabBar.setVisibility(View.GONE);
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //No call for super(). Bug on API Level > 11.
        outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
        super.onSaveInstanceState(outState);
    }
    public void setFragment()
    {
        initTabUI();
        try {
            switch (Globals.e_mode) {
                case LIBRARY:
                    showHideTab(true);
                    //Globals.g_lstUploads.clear();
                    txtTab1.setTextColor(getResources().getColor(R.color.app_theme_color));
                    imgTab1.setImageDrawable(getResources().getDrawable(R.drawable.icn_existing_dictations));
                    currentFragment = new LibraryFragment();
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame_main, currentFragment).commitAllowingStateLoss();
                    break;
                case LIBRARY_RENAME:
                    txtTab1.setTextColor(getResources().getColor(R.color.app_theme_color));
                    imgTab1.setImageDrawable(getResources().getDrawable(R.drawable.icn_existing_dictations));
                    currentFragment = new RenameFragment();
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame_main, currentFragment).commit();
                    break;
                case LIBRARY_COMMENT:
                    txtTab1.setTextColor(getResources().getColor(R.color.app_theme_color));
                    showHideTab(false);
                    imgTab1.setImageDrawable(getResources().getDrawable(R.drawable.icn_existing_dictations));
                    currentFragment = new CommentFragment();
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame_main, currentFragment).commit();
                    break;
                case UPLOAD:
                    txtTab2.setTextColor(getResources().getColor(R.color.app_theme_color));
                    imgTab2.setImageDrawable(getResources().getDrawable(R.drawable.icn_uploads));
                    currentFragment = new UploadFragment();
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame_main, currentFragment).commit();
                    break;
                case RECORD:
                    txtTab3.setTextColor(getResources().getColor(R.color.app_theme_color));
                    currentFragment = new RecordFragment();
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame_main, currentFragment).commit();
                    break;
                case SETTING:
                    txtTab4.setTextColor(getResources().getColor(R.color.app_theme_color));
                    imgTab4.setImageDrawable(getResources().getDrawable(R.drawable.icn_settings));
                    currentFragment = new SettingFragment();
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame_main, currentFragment).commit();
                    break;
                case SETTING_FILENAME:
                    txtTab4.setTextColor(getResources().getColor(R.color.app_theme_color));
                    imgTab4.setImageDrawable(getResources().getDrawable(R.drawable.icn_settings));
                    currentFragment = new FilenameFormatFragment();
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame_main, currentFragment).commit();
                    break;
                case SETTING_QUALITY:
                    txtTab4.setTextColor(getResources().getColor(R.color.app_theme_color));
                    imgTab4.setImageDrawable(getResources().getDrawable(R.drawable.icn_settings));
                    currentFragment = new AudioQualityFragment();
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame_main, currentFragment).commit();
                    break;
                case SETTING_SENSITIVE:
                    txtTab4.setTextColor(getResources().getColor(R.color.app_theme_color));
                    imgTab4.setImageDrawable(getResources().getDrawable(R.drawable.icn_settings));
                    currentFragment = new MicrophoneSensitivityFragment();
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame_main, currentFragment).commit();
                    break;
                case SETTING_PROFILE:
                    txtTab4.setTextColor(getResources().getColor(R.color.app_theme_color));
                    imgTab4.setImageDrawable(getResources().getDrawable(R.drawable.icn_settings));
                    currentFragment = new ProfileFragment();
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame_main, currentFragment).commit();
                    break;
                case SETTING_ABOUT:
                    txtTab4.setTextColor(getResources().getColor(R.color.app_theme_color));
                    imgTab4.setImageDrawable(getResources().getDrawable(R.drawable.icn_settings));
                    currentFragment = new AboutFragment();
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame_main, currentFragment).commit();
                    break;
                case SETTING_ABOUT_CONTACTUS:
                    txtTab4.setTextColor(getResources().getColor(R.color.app_theme_color));
                    imgTab4.setImageDrawable(getResources().getDrawable(R.drawable.icn_settings));
                    currentFragment = new ContactUsFragment();
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame_main, currentFragment).commit();
                    break;
                case SETTING_ABOUT_PGUIDE:
                    txtTab4.setTextColor(getResources().getColor(R.color.app_theme_color));
                    imgTab4.setImageDrawable(getResources().getDrawable(R.drawable.icn_settings));
                    currentFragment = new PasswordGuideFragment();
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame_main, currentFragment).commit();
                    break;
            }
        }
        catch (Exception e)
        {

        }
    }
    public void backFragment()
    {
        switch (Globals.e_mode)
        {
            case LIBRARY:
            case UPLOAD:
            case RECORD:
            case SETTING:
                finish();
                break;
            case SETTING_ABOUT:
            case SETTING_PROFILE:
            case SETTING_QUALITY:
            case SETTING_SENSITIVE:
            case SETTING_FILENAME:
                Globals.e_mode = Enums.MODE.SETTING;
                setFragment();
                break;
            case SETTING_ABOUT_CONTACTUS:
            case SETTING_ABOUT_PGUIDE:
                Globals.e_mode = Enums.MODE.SETTING_ABOUT;
                setFragment();
                break;
            case LIBRARY_COMMENT:
                ((CommentFragment)currentFragment).backFragment();
                break;
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            backFragment();

        }
        return false;
    }
    public void actionLogout()
    {
        if (Globals.g_isUpload)
        {
            new MaterialDialog.Builder(this)
                    .title("PTS Dictate")
                    .content("Uploading In progress")
                    .positiveText("OK")
                    .titleColor(Color.BLACK)
                    .contentColor(Color.GRAY)
                    .show();
            return;
        }
        new MaterialDialog.Builder(this)
                .title("PTS Dictate")
                .content("Are you sure want to Log out?")
                .positiveText("Yes")
                .negativeText("No")
                .titleColor(Color.BLACK)
                .contentColor(Color.GRAY)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        isLogout = true;
                        if (currentFragment instanceof LibraryFragment)
                        {
                            ((LibraryFragment) currentFragment).playbackUtil.pauseFile();
                        }
                        else if (currentFragment instanceof RecordFragment)
                        {
                            ((RecordFragment) currentFragment).playbackUtil.pauseFile();
                        }
                        Utils.savePreference(MainActivity.this,false);
                        Intent m = new Intent(MainActivity.this,LoginActivity.class);
                        MainActivity.this.startActivity(m);
                        finish();

                    }
                })
                .show();
    }
    public void showRecordingMessage()
    {
        new MaterialDialog.Builder(this)
                .title("PTS Dictate")
                .content("Do you want to stop the current recording?")
                .positiveText("Yes")
                .negativeText("No")
                .titleColor(Color.BLACK)
                .contentColor(Color.GRAY)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        ((RecordFragment)currentFragment).stopRecording();
                    }
                })
                .show();
    }
    public boolean isRecording()
    {
        if (currentFragment instanceof RecordFragment)
        {
            return !((RecordFragment) currentFragment).isStopped();
        }
        else return false;
    }
    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.existing_dictations:
                if (!isRecording()) {
                    Globals.e_mode = Enums.MODE.LIBRARY;
                    setFragment();
                }
                else
                    showRecordingMessage();
                break;
            case R.id.upload:
                if (!isRecording()) {
                    Globals.e_mode = Enums.MODE.UPLOAD;
                    setFragment();
                }
                else showRecordingMessage();
                break;
            case R.id.record:
                if (!(currentFragment instanceof RecordFragment)) {
                    Globals.e_mode = Enums.MODE.RECORD;
                    Globals.g_isExistRecord = false;
                    setFragment();
                }
                break;
            case R.id.settings:
                if (!isRecording()) {
                    Globals.e_mode = Enums.MODE.SETTING;
                    setFragment();
                }
                else showRecordingMessage();
                break;
            case R.id.logout:
                if (!isRecording()) {
                    actionLogout();
                }
                else
                    showRecordingMessage();
                break;
        }
    }

    @Override
    public void onResponse(int code) {

    }
    @Override
    public void onStop()
    {
        super.onStop();
        Log.e("PTS Recorder","Called Stopped");
        if (!isLogout)
        {
            if (currentFragment instanceof RecordFragment) {
                if (((RecordFragment)currentFragment).isRecording == 1) {
                    myNotificationMgr.ShowNotification("Recording");
//                    Intent in = new Intent("show");
//                    sendBroadcast(in);
                }
            }
        }
    }
    @Override
    public void onPause()
    {
        super.onPause();
        Log.e("PTS Recorder","Called Paused");
    }
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Log.e("PTS Recorder","Called MainActivity Destroy function");
        stopService(new Intent(MainActivity.this, UploadService.class));
        this.unregisterReceiver(this.callReceiver);
    }
}
