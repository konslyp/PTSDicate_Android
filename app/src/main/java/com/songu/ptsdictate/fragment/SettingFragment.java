package com.songu.ptsdictate.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.songu.ptsdictate.R;
import com.songu.ptsdictate.activity.LoginActivity;
import com.songu.ptsdictate.activity.MainActivity;
import com.songu.ptsdictate.adapter.AdapterUpload;
import com.songu.ptsdictate.doc.Enums;
import com.songu.ptsdictate.doc.Globals;
import com.songu.ptsdictate.service.IServiceResult;
import com.songu.ptsdictate.util.Utils;

/**
 * Created by Administrator on 3/22/2018.
 */

public class SettingFragment extends Fragment implements IServiceResult,View.OnClickListener {


    private View mRootView;

    private RelativeLayout menuQuality,menuSensitivy;
    private ToggleButton menuVoiceAutoPause,menuEmailNotification,menuComments,menuIndexing,menuEditingHelp,menuCommentMandatory;
    private RelativeLayout menuProfile,menuFileName,menuLogout,menuAbout;
    private ToggleButton menuArchive,menuWifi,menuSleep;
    private LinearLayout layoutCommentMandatory,layoutArchiveDays;
    private TextView txtTitle,txtArchive;
    private ImageView imgTitle;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mRootView == null)
            mRootView = inflater.inflate(R.layout.fragment_setting, container, false);
        initView();
        Utils.loadSetting(this.getContext());
        setSetting();
        return mRootView;
    }

    public void initView()
    {
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = this.getActivity().getCurrentFocus();
        if (view != null)
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        menuQuality = mRootView.findViewById(R.id.screen_settings_record_format_RLAY);
        menuSensitivy = mRootView.findViewById(R.id.screen_settings_microphone_sensitivity_RLAY);
        menuVoiceAutoPause = mRootView.findViewById(R.id.screen_settings_voice_activation_TBTN);
        menuEmailNotification = mRootView.findViewById(R.id.screen_settings_upload_email_notification_TBTN);
        menuComments = mRootView.findViewById(R.id.screen_settings_commentsdialog_TBTN);
        menuIndexing = mRootView.findViewById(R.id.screen_settings_indexing_TBTN);
        menuCommentMandatory = mRootView.findViewById(R.id.screen_settings_comments_mandatory_TBTN);
        menuEditingHelp = mRootView.findViewById(R.id.screen_settings_edit_TBTN);
        menuProfile = mRootView.findViewById(R.id.screen_settings_profile_RLAY);
        menuFileName = mRootView.findViewById(R.id.screen_settings_filenaming_RLAY);
        layoutCommentMandatory = mRootView.findViewById(R.id.screen_settings_comments_mandatory_LAY);
        layoutArchiveDays = mRootView.findViewById(R.id.screen_settings_archieve_LAY);
        txtArchive = (TextView) mRootView.findViewById(R.id.screen_settings_archieve_day_TXT);

        menuArchive = mRootView.findViewById(R.id.screen_settings_keep_file_TBTN);
        menuWifi = mRootView.findViewById(R.id.screen_settings_upload_via_wifi_TBTN);
        menuSleep = mRootView.findViewById(R.id.screen_settings_standbyMode_TBTN);
        menuAbout = mRootView.findViewById(R.id.screen_settings_about);
        menuLogout = mRootView.findViewById(R.id.screen_settings_logout_RLAY);

        txtTitle = (TextView)mRootView.findViewById(R.id.title_TXT);
        imgTitle = (ImageView) mRootView.findViewById(R.id.title_IMG);
        txtTitle.setText("Settings");
        imgTitle.setImageDrawable(getResources().getDrawable(R.drawable.icn_settings));
        imgTitle.setVisibility(View.VISIBLE);


        menuQuality.setOnClickListener(this);
        menuSensitivy.setOnClickListener(this);
        menuProfile.setOnClickListener(this);
        menuAbout.setOnClickListener(this);
        menuLogout.setOnClickListener(this);
        menuFileName.setOnClickListener(this);

        menuVoiceAutoPause.setOnClickListener(this);
        menuEmailNotification.setOnClickListener(this);
        menuComments.setOnClickListener(this);
        menuCommentMandatory.setOnClickListener(this);
        menuIndexing.setOnClickListener(this);
        menuEditingHelp.setOnClickListener(this);
        menuArchive.setOnClickListener(this);
        menuWifi.setOnClickListener(this);
        menuSleep.setOnClickListener(this);
        layoutArchiveDays.setOnClickListener(this);
    }

    public void setSetting()
    {
        this.menuVoiceAutoPause.setChecked(Globals.mSetting.isVoiceAutoPause);
        this.menuEmailNotification.setChecked(Globals.mSetting.isEmailNotification);
        this.menuComments.setChecked(Globals.mSetting.isCommentScreen);
        this.menuIndexing.setChecked(Globals.mSetting.isIndexing);
        this.menuEditingHelp.setChecked(Globals.mSetting.isEditingScreen);
        this.menuArchive.setChecked(Globals.mSetting.isArchiveFile);
        this.menuWifi.setChecked(Globals.mSetting.isUploadviaWifi);
        this.menuSleep.setChecked(Globals.mSetting.isSleepMode);
        this.menuCommentMandatory.setChecked(Globals.mSetting.isCommentMandatory);

        if (this.menuArchive.isChecked())
        {
            layoutArchiveDays.setVisibility(View.VISIBLE);
            txtArchive.setText(String.valueOf(Globals.mSetting.mAchiveDays));
        }
        else
        {
            layoutArchiveDays.setVisibility(View.GONE);
        }
        setCommentSetting();
    }
    public void setCommentSetting()
    {
        if (Globals.mSetting.isCommentScreen)
        {
            layoutCommentMandatory.setVisibility(View.VISIBLE);
        }
        else layoutCommentMandatory.setVisibility(View.GONE);
    }
    public void storeSetting()
    {
        Globals.mSetting.isVoiceAutoPause = this.menuVoiceAutoPause.isChecked();
        Globals.mSetting.isEmailNotification = this.menuEmailNotification.isChecked();
        Globals.mSetting.isCommentScreen = this.menuComments.isChecked();
        Globals.mSetting.isIndexing = this.menuIndexing.isChecked();
        Globals.mSetting.isEditingScreen = this.menuEditingHelp.isChecked();
        Globals.mSetting.isArchiveFile = this.menuArchive.isChecked();
        Globals.mSetting.isUploadviaWifi = this.menuWifi.isChecked();
        Globals.mSetting.isSleepMode = this.menuSleep.isChecked();
    }
    public void inputArchiveDays()
    {
        MaterialDialog dialog = new MaterialDialog.Builder(this.getContext())
                .title("PTS Dictate")
//                .content("Please Enter Archive Days")
                .inputType(InputType.TYPE_CLASS_NUMBER)
                .inputRange(1,3)
                .titleColor(Color.BLACK)
                .widgetColor(0xffeeeeee)
                .cancelable(false)
                .customView(R.layout.inflate_archive_pop,true)
                .autoDismiss(false)
                .positiveText("OK")
                .negativeText("Cancel")
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        EditText editText = dialog.getCustomView().findViewById(R.id.editArchiveDays);
                        if (editText.getText().toString().trim().equals(""))
                        {
                            return;
                        }
                        else
                        {
                            if (Integer.parseInt(editText.getText().toString()) >= 1) {
                                Globals.mSetting.mAchiveDays = Integer.parseInt(editText.getText().toString());
                                txtArchive.setText(String.valueOf(Globals.mSetting.mAchiveDays));
                                Utils.saveSetting(SettingFragment.this.getContext());
                                dialog.dismiss();
                            }
                            else
                            {
                                Globals.mSetting.mAchiveDays = 1;
                                txtArchive.setText(String.valueOf(Globals.mSetting.mAchiveDays));
                                Utils.saveSetting(SettingFragment.this.getContext());
                                dialog.dismiss();
                            }
                        }
                    }
                })
                .show();
        ((EditText)dialog.getCustomView().findViewById(R.id.editArchiveDays)).setText(String.valueOf(Globals.mSetting.mAchiveDays));

    }
    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.screen_settings_about:
                Globals.e_mode = Enums.MODE.SETTING_ABOUT;
                ((MainActivity)getActivity()).setFragment();
                break;
            case R.id.screen_settings_record_format_RLAY:
                Globals.e_mode = Enums.MODE.SETTING_QUALITY;
                ((MainActivity)getActivity()).setFragment();
                break;
            case R.id.screen_settings_microphone_sensitivity_RLAY:
                Globals.e_mode = Enums.MODE.SETTING_SENSITIVE;
                ((MainActivity)getActivity()).setFragment();
                break;
            case R.id.screen_settings_profile_RLAY:
                Globals.e_mode = Enums.MODE.SETTING_PROFILE;
                ((MainActivity)getActivity()).setFragment();
                break;
            case R.id.screen_settings_filenaming_RLAY:
                Globals.e_mode = Enums.MODE.SETTING_FILENAME;
                ((MainActivity)getActivity()).setFragment();
                break;
            case R.id.screen_settings_logout_RLAY:
                new MaterialDialog.Builder(this.getContext())
                        .title("PTS Dictate")
                        .content("Are you sure want to Log out?")
                        .positiveText("Yes")
                        .negativeText("No")
                        .titleColor(Color.BLACK)
                        .contentColor(Color.GRAY)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                Utils.savePreference(SettingFragment.this.getContext(),false);
                                Intent m = new Intent(SettingFragment.this.getContext(),LoginActivity.class);
                                SettingFragment.this.startActivity(m);
                                SettingFragment.this.getActivity().finish();
                            }
                        })
                        .show();
                break;
            case R.id.screen_settings_voice_activation_TBTN:
                Globals.mSetting.isVoiceAutoPause = !Globals.mSetting.isVoiceAutoPause;
                Utils.saveSetting(this.getContext());
                break;
            case R.id.screen_settings_upload_email_notification_TBTN:
                Globals.mSetting.isEmailNotification = !Globals.mSetting.isEmailNotification;
                Utils.saveSetting(this.getContext());
                break;
            case R.id.screen_settings_indexing_TBTN:
                Globals.mSetting.isIndexing = !Globals.mSetting.isIndexing;
                Utils.saveSetting(this.getContext());
                break;
            case R.id.screen_settings_commentsdialog_TBTN:
                Globals.mSetting.isCommentScreen = !Globals.mSetting.isCommentScreen;
                Utils.saveSetting(this.getContext());
                setCommentSetting();
                break;
            case R.id.screen_settings_edit_TBTN:
                Globals.mSetting.isEditingScreen = !Globals.mSetting.isEditingScreen;
                Utils.saveSetting(this.getContext());
                break;
            case R.id.screen_settings_archieve_LAY:
                inputArchiveDays();
                break;
            case R.id.screen_settings_keep_file_TBTN:
                Globals.mSetting.isArchiveFile = !Globals.mSetting.isArchiveFile;
                setSetting();
                if (Globals.mSetting.isArchiveFile)
                {
                    inputArchiveDays();
                }
                else
                {
                    Globals.mSetting.mAchiveDays = 1;
                }
                Utils.saveSetting(this.getContext());
                break;
            case R.id.screen_settings_upload_via_wifi_TBTN:
                Globals.mSetting.isUploadviaWifi = !Globals.mSetting.isUploadviaWifi;
                Utils.saveSetting(this.getContext());
                break;
            case R.id.screen_settings_standbyMode_TBTN:
                Globals.mSetting.isSleepMode = !Globals.mSetting.isSleepMode;
                if (Globals.mSetting.isSleepMode) {
                    getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//                    Intent m = new Intent("keep");
//                    getActivity().sendBroadcast(m);
                }
                else
                {
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//                    Intent m = new Intent("unkeep");
//                    getActivity().sendBroadcast(m);
                }
                Utils.saveSetting(this.getContext());
                break;
            case R.id.screen_settings_comments_mandatory_TBTN:
                Globals.mSetting.isCommentMandatory = !Globals.mSetting.isCommentMandatory;
                Utils.saveSetting(this.getContext());
                break;
        }
    }

    @Override
    public void onResponse(int code) {

    }
}

