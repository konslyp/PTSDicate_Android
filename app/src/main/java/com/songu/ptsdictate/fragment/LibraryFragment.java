package com.songu.ptsdictate.fragment;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.songu.ptsdictate.R;
import com.songu.ptsdictate.activity.MainActivity;
import com.songu.ptsdictate.adapter.AdapterLibrary;
import com.songu.ptsdictate.database.DBManager;
import com.songu.ptsdictate.doc.Config;
import com.songu.ptsdictate.doc.Enums;
import com.songu.ptsdictate.doc.Globals;
import com.songu.ptsdictate.model.RecordModel;
import com.songu.ptsdictate.service.IServiceResult;
import com.songu.ptsdictate.util.BookMarkUtil;
import com.songu.ptsdictate.util.PlaybackUtil;
import com.songu.ptsdictate.util.Utils;
import com.songu.ptsdictate.view.WaveformSeekBar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 3/22/2018.
 */

public class LibraryFragment extends Fragment implements IServiceResult,View.OnClickListener {


    private View mRootView;
    private TextView txtTitle;
    private ListView lstRecords;
    private AdapterLibrary adapterLibrary;
    private TextView txtNoFiles;
    private List<RecordModel> mRecordFiles;
    private LinearLayout layoutPlaybar;

    private ImageButton btnTopAdd;
    private CheckBox chkTopSelect;
    public PlaybackUtil playbackUtil;
    private ImageButton btnControlPrev,btnControlBack,btnControlPlay,btnControlForward,btnControlNext;
    private TextView txtDuration,txtElapse,txtFileName,txtWaring;
    private Button btnUpload,btnDelete;
    private LinearLayout layoutWaveForm;
    private WaveformSeekBar waveformSeekBar;
    private RelativeLayout relWelcome;
    private TextView txtWelcomeName;
    private BookMarkUtil bookMarkUtil;
    private RecordModel autoSaveModel = null;

    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message inputMessage) {
            if (inputMessage.what == 0) {
                if (getContext() != null) {
                    Animation slide_up = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up);
                    relWelcome.startAnimation(slide_up);
                }
                sendEmptyMessageDelayed(1,1300);
            }
            else if (inputMessage.what == 1)
            {
                relWelcome.setVisibility(View.GONE);
                Globals.g_loginFirst = false;
            }
            else if (inputMessage.what == 2){
                playbackUtil.seekToFirstPosition();
                playbackUtil.playFile();
            }
            else if (inputMessage.what == 3)
            {
                playbackUtil.seekToFirstPosition();
                if (playbackUtil.isPlaying())
                    playbackUtil.pauseFile();
            }
            else if (inputMessage.what == 4){
                playbackUtil.playFile();
            }
            else if (inputMessage.what == 5)
            {
                if (playbackUtil.isPlaying())
                    playbackUtil.pauseFile();
            }
            else if (inputMessage.what == 6)
            {
                if (autoSaveModel != null) {
                    autoSaveModel.mElapse = String.valueOf(playbackUtil.getDuration());
                    autoSaveModel.mIsAutoSave = 2;
                    Globals.g_database.updateRecordFile(autoSaveModel);
                    Globals.g_fileIndex++;
                    Utils.saveIndex(LibraryFragment.this.getContext());
                    setData();
                }
            }
            else if (inputMessage.what == 7)
            {

            }
        }
    };

    private BroadcastReceiver UpdateListReceiver = new UpdateListReceiver();

    public class UpdateListReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("update"))
            {
                setData();
            }
        }
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentFilter filter = new IntentFilter("update");
        this.getActivity().registerReceiver(UpdateListReceiver, filter);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        this.getActivity().unregisterReceiver(this.UpdateListReceiver);
    }
    @Override
    public void onResume()
    {
        super.onResume();
        Utils.loadIndex(this.getContext());
        ((MainActivity)getActivity()).cleanArchiveDatas();
        setData();
    }
    @Override
    public void onStop()
    {
        super.onStop();
        if (playbackUtil != null)
        {
            playbackUtil.pauseFile();
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null)
            mRootView = inflater.inflate(R.layout.fragment_library, container, false);
        initView();
        Utils.loadIndex(this.getContext());
        processAutosaveFile();
        welcomeAnimation();
        ((MainActivity)getActivity()).cleanArchiveDatas();
        //setData();
        return mRootView;
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

    public void processAutosaveFile()
    {
        if (Globals.g_recordUtil.isRecording)
        {
            Globals.g_recordUtil.stopRecording();
        }
        if (!hasPermissions(this.getContext(), Config.STORAGE_PERMISSIONS))
        {
            return;
        }
        List<RecordModel> recordFiles = null;
        try {
            recordFiles = Globals.g_database.getAllRecordlist();
        }
        catch (Exception e)
        {
            return;
        }
        for (int i = 0;i < recordFiles.size();i++)
        {
            if (recordFiles.get(i).mIsAutoSave == 1)
            {
                new MaterialDialog.Builder(this.getActivity())
                        .title("PTS Dictate")
                        .content("Record file is autosaved.")
                        .positiveText("OK")
                        .titleColor(Color.BLACK)
                        .contentColor(Color.GRAY)
                        .show();
                Globals.g_recordUtil.setSampleRate(recordFiles.get(i).mRate);
                Globals.g_recordUtil.backupAutosaveFiles(recordFiles.get(i).mPath);
                //File size and Duration
                autoSaveModel = recordFiles.get(i);
                autoSaveModel.mSize = String.valueOf(Globals.g_recordUtil.getFileSize());
//                playbackUtil.setRecordFile(autoSaveModel.mPath,waveformSeekBar);
//                playbackUtil.setBookMarks(bookMarkUtil.setJsonData(autoSaveModel.mIndexData));
                mHandler.sendEmptyMessageDelayed(6,500);
            }
        }
    }
    public void disableNetworkAnimation()
    {
        //relWelcome.setBackgroundColor(0xffff0000);
        txtWelcomeName.setText("Please enable WiFi to upload");
        txtWaring.setText("Warning");
        Animation slide_down = AnimationUtils.loadAnimation(getContext(),R.anim.slide_down);
        relWelcome.setVisibility(View.VISIBLE);
        relWelcome.startAnimation(slide_down);
        mHandler.sendEmptyMessageDelayed(0,3500);
    }
    public void disableNetworkAnimation1()
    {
        //relWelcome.setBackgroundColor(0xffff0000);
        txtWelcomeName.setText("Network is no longer available. Please turn on Mobile data or WiFi.");
        txtWaring.setText("Warning");
        Animation slide_down = AnimationUtils.loadAnimation(getContext(),R.anim.slide_down);
        relWelcome.setVisibility(View.VISIBLE);
        relWelcome.startAnimation(slide_down);
        mHandler.sendEmptyMessageDelayed(0,3500);
    }
    public void welcomeAnimation()
    {
        Animation slide_down = AnimationUtils.loadAnimation(getContext(),R.anim.slide_down);
        txtWelcomeName.setText(Globals.mAccount.mName);
        if (Globals.g_loginFirst) {
            relWelcome.startAnimation(slide_down);
            mHandler.sendEmptyMessageDelayed(0,3500);
        }
        else
        {
            relWelcome.setVisibility(View.GONE);
        }

    }
    public void initView()
    {
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = this.getActivity().getCurrentFocus();
        if (view != null)
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        bookMarkUtil = new BookMarkUtil(this.getContext());
        playbackUtil = new PlaybackUtil(this.getContext());
        adapterLibrary = new AdapterLibrary(this.getActivity());
        txtTitle = (TextView) mRootView.findViewById(R.id.title_TXT);
        txtNoFiles = (TextView) mRootView.findViewById(R.id.screen_existing_no_records_TXT);
        lstRecords = (ListView) mRootView.findViewById(R.id.existing_dictations_LV);
        txtWaring = (TextView) mRootView.findViewById(R.id.screen_existing_dictations_user_name_label);
        layoutPlaybar = (LinearLayout) mRootView.findViewById(R.id.main_lay);
        chkTopSelect = (CheckBox) mRootView.findViewById(R.id.header_select_all_CHBX);
        btnTopAdd = (ImageButton) mRootView.findViewById(R.id.header_add_BTN);
        relWelcome = (RelativeLayout) mRootView.findViewById(R.id.screen_existing_user_details_LAY);
        txtWelcomeName = (TextView) mRootView.findViewById(R.id.screen_existing_dictations_user_name_TXT);

        btnControlPrev = mRootView.findViewById(R.id.screen_existing_dictations_double_rew_IMGBTN);
        btnControlBack = mRootView.findViewById(R.id.screen_existing_dictations_rew_IMGBTN);
        btnControlPlay = mRootView.findViewById(R.id.screen_existing_dictation_play_IMGBTN);
        btnControlForward = mRootView.findViewById(R.id.screen_existing_dictations_fwd_IMGBTN);
        btnControlNext = mRootView.findViewById(R.id.screen_existing_dictations_double_fwd_IMGBTN);

        btnUpload = mRootView.findViewById(R.id.screen_existing_dictations_upload_BTN);
        btnDelete = mRootView.findViewById(R.id.screen_existing_dictations_delete_BTN);

        txtDuration = mRootView.findViewById(R.id.screen_existing_dictations_media_total_duration);
        txtElapse = mRootView.findViewById(R.id.screen_existing_dictations_media_elapsed_duration);
        txtFileName = mRootView.findViewById(R.id.screen_existing_dictations_file_name_TV);

        layoutWaveForm = mRootView.findViewById(R.id.screen_existing_dictations_waveformViewLAY);
        initSeekBar();
        playbackUtil.setControls(btnControlPrev,btnControlBack,btnControlPlay,btnControlForward,btnControlNext,txtDuration,txtElapse);
        playbackUtil.setEnableControl(true);
        lstRecords.setAdapter(adapterLibrary);
        txtTitle.setText("Existing Dictations");
        btnDelete.setOnClickListener(this);
        btnUpload.setOnClickListener(this);
        btnTopAdd.setOnClickListener(this);
        chkTopSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                for (int i = 0;i < mRecordFiles.size();i++)
                {
                    boolean isUploading = false;
                    for (int j = 0;j < Globals.g_lstUploads.size();j++)
                    {
                        if (mRecordFiles.get(i).mLocalNo == Globals.g_lstUploads.get(j).mLocalNo
                                && (Globals.g_lstUploads.get(j).mIsUpload == 2 || Globals.g_lstUploads.get(j).mIsUpload == 0 || Globals.g_lstUploads.get(j).mIsUpload == 4))
                        {
                            isUploading = true;
                        }
                    }
                    if (!isUploading)
                        mRecordFiles.get(i).isSelect = b;
                }
                adapterLibrary.update(mRecordFiles);
            }
        });
        lstRecords.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                for (int k = 0;k < Globals.g_lstUploads.size();k++)
                {
                    if (mRecordFiles.get(i).mLocalNo == Globals.g_lstUploads.get(k).mLocalNo && Globals.g_lstUploads.get(k).mIsUpload != 1)
                    {
                        new MaterialDialog.Builder(LibraryFragment.this.getContext())
                                .title("PTS Dictate")
                                .content("Uploading in progress, so cannot rename the file")
                                .positiveText("OK")
                                .titleColor(Color.BLACK)
                                .contentColor(Color.GRAY)
                                .show();
                        return false;
                    }
                }
                Globals.g_existFile = mRecordFiles.get(i);
                Globals.e_mode = Enums.MODE.LIBRARY_RENAME;
                ((MainActivity)LibraryFragment.this.getActivity()).showHideTab(false);
                ((MainActivity)LibraryFragment.this.getActivity()).setFragment();
                return false;
            }
        });
    }
    public void stopPlayItem()
    {
        if (mRecordFiles != null) {
            for (int i = 0; i < mRecordFiles.size(); i++) {
                mRecordFiles.get(i).isPlaying = false;
            }
            adapterLibrary.update(mRecordFiles);
        }
    }
    public boolean checkStoragePermission()
    {
        return MainActivity.hasPermissions(this.getContext(),  Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);
    }
    public void playItem()
    {
        if (!checkStoragePermission()) return;
        stopPlayItem();
        if (mRecordFiles == null) return;
        for (int i = 0;i < mRecordFiles.size();i++)
        {
            if (mRecordFiles.get(i).mLocalNo == Globals.g_selectLibraryItemNo) {
                mRecordFiles.get(i).isPlaying = true;
            }
        }
        adapterLibrary.update(mRecordFiles);
    }
    public void initSeekBar()
    {
        waveformSeekBar = new WaveformSeekBar(getActivity());
        waveformSeekBar.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        layoutWaveForm.addView(waveformSeekBar);
    }

    public void setRecordPlayFile(RecordModel rModel,boolean isPlay)
    {
        if (!checkStoragePermission()) return;
        Globals.g_selectLibraryItemNo = rModel.mLocalNo;
        txtFileName.setText(rModel.mName);
        for (int i = 0;i < mRecordFiles.size();i++)
        {
            mRecordFiles.get(i).isPlaying = false;
        }

        try {
            if (!rModel.mPath.equals(playbackUtil.mFilePath)) {
                playbackUtil.setRecordFile(rModel.mPath, waveformSeekBar);
                waveformSeekBar.firstDraw = true;
                waveformSeekBar.setAudio((InputStream) new FileInputStream(rModel.mPath));
                if (isPlay) {
                    mHandler.sendEmptyMessageDelayed(2, 500);
                    rModel.isPlaying = true;
                } else {
                    mHandler.sendEmptyMessageDelayed(3, 500);
                    rModel.isPlaying = false;
                }
            }
            else
            {
                if (isPlay) {
                    rModel.isPlaying = true;
                    mHandler.sendEmptyMessageDelayed(4, 500);
                } else {
                    mHandler.sendEmptyMessageDelayed(5, 500);
                    rModel.isPlaying = false;
                }
            }
            adapterLibrary.update(mRecordFiles);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    public void setData()
    {
        txtNoFiles.setVisibility(View.GONE);
        layoutPlaybar.setVisibility(View.GONE);
        btnTopAdd.setVisibility(View.GONE);
        chkTopSelect.setVisibility(View.GONE);

        if (!hasPermissions(this.getContext(), Config.STORAGE_PERMISSIONS))
        {
            txtNoFiles.setVisibility(View.VISIBLE);
            btnTopAdd.setVisibility(View.VISIBLE);
            return;
        }
        mRecordFiles = Globals.g_database.getAllRecordlist();
        adapterLibrary.update(mRecordFiles);


        if (mRecordFiles.size() > 0)
        {
            layoutPlaybar.setVisibility(View.VISIBLE);
            chkTopSelect.setVisibility(View.VISIBLE);
            //mHandler.sendEmptyMessageDelayed(7,500);
            setRecordPlayFile(mRecordFiles.get(0),false);

        }
        else
        {
            txtNoFiles.setVisibility(View.VISIBLE);
            btnTopAdd.setVisibility(View.VISIBLE);
        }
        //adapterLibrary.update(mRecordFiles);
    }
    public void uploadFiles()
    {
        final List<RecordModel> lstUploads = new ArrayList<>();
        int isUpload = 0;
        if (mRecordFiles == null) return;
        for (int i = 0;i < mRecordFiles.size();i++)
        {
            if (mRecordFiles.get(i).isSelect)
            {
                if (mRecordFiles.get(i).mUploaded == 1)
                {
                    isUpload++;
                }
                lstUploads.add(mRecordFiles.get(i));
            }
        }
        if (lstUploads.size() == 0)
        {
            new MaterialDialog.Builder(this.getContext())
                    .title("PTS Dictate")
                    .content("Please select atleast one file")
                    .positiveText("OK")
                    .titleColor(Color.BLACK)
                    .contentColor(Color.GRAY)
                    .show();
            return;
        }
        if (isUpload > 0)
        {
            new MaterialDialog.Builder(this.getContext())
                    .title("PTS Dictate")
                    .content("Are you sure you want to re-upload?")
                    .positiveText("Yes")
                    .negativeText("No")
                    .titleColor(Color.BLACK)
                    .contentColor(Color.GRAY)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                            if (Utils.isUploadInternetOn(LibraryFragment.this.getContext(),Globals.mSetting.isUploadviaWifi)) {
                                if (Globals.g_isUpload)
                                {
                                    for (int k = 0;k < lstUploads.size();k++) {
                                        lstUploads.get(k).mIsUpload = 0;
                                        Globals.g_database.updateRecordFile(lstUploads.get(k));
                                        Globals.g_lstUploads.add(lstUploads.get(k));
                                    }
                                    Globals.e_mode = Enums.MODE.UPLOAD;
                                    ((MainActivity)getActivity()).setFragment();
                                    return;
                                }
                                Globals.g_uploadIndex = -1;
                                uploadFilesService(lstUploads);
                            }
                            else {
                                if (!Globals.mSetting.isUploadviaWifi)
                                    disableNetworkAnimation1();
                                else {
                                    disableNetworkAnimation();
                                }
                            }
                        }
                    })
                    .show();
            return;
        }
        if (Utils.isUploadInternetOn(this.getContext(),Globals.mSetting.isUploadviaWifi)) {
            if (Globals.g_isUpload)
            {
                for (int k = 0;k < lstUploads.size();k++) {
                    lstUploads.get(k).mIsUpload = 0;
                    Globals.g_database.updateRecordFile(lstUploads.get(k));
                    Globals.g_lstUploads.add(lstUploads.get(k));
                }
                Globals.e_mode = Enums.MODE.UPLOAD;
                ((MainActivity)getActivity()).setFragment();
                return;
            }
            Globals.g_uploadIndex = -1;
            uploadFilesService(lstUploads);
        }
        else
        {
            if (!Globals.mSetting.isUploadviaWifi)
                disableNetworkAnimation1();
            else
                disableNetworkAnimation();
        }
    }
    public void uploadFilesService(List<RecordModel> uploadFiles)
    {
        for (int i = 0;i < uploadFiles.size();i++)
        {
            uploadFiles.get(i).mIsUpload = 0;
            Globals.g_database.updateRecordFile(uploadFiles.get(i));
        }
        Globals.g_lstUploads = uploadFiles;
        Globals.e_mode = Enums.MODE.UPLOAD;
        ((MainActivity)getActivity()).setFragment();
        Intent in = new Intent("start_upload");
        getActivity().sendBroadcast(in);
    }
    public void deleteFiles()
    {
        final List<RecordModel> lstDeletes = new ArrayList<>();
        int isUpload = 0;
        if (mRecordFiles == null) return;
        for (int i = 0;i < mRecordFiles.size();i++)
        {
            if (mRecordFiles.get(i).isSelect)
            {
                lstDeletes.add(mRecordFiles.get(i));
            }
        }
        if (lstDeletes.size() == 0)
        {
            new MaterialDialog.Builder(this.getContext())
                    .title("PTS Dictate")
                    .content("Please select atleast one file")
                    .positiveText("OK")
                    .titleColor(Color.BLACK)
                    .contentColor(Color.GRAY)
                    .show();
            chkTopSelect.setChecked(false);
            return;
        }
        new MaterialDialog.Builder(this.getContext())
                .title("PTS Dictate")
                .content("Are you sure want to delete?")
                .positiveText("Yes")
                .negativeText("No")
                .titleColor(Color.BLACK)
                .contentColor(Color.GRAY)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        for (int i = 0;i < mRecordFiles.size();i++)
                        {
                            if (mRecordFiles.get(i).isSelect)
                            {
                                if (mRecordFiles.get(i).mPath != null) {
                                    File file = new File(mRecordFiles.get(i).mPath);
                                    file.delete();
                                }
                                Globals.g_database.deleteRecordFile(mRecordFiles.get(i).mLocalNo);
                            }
                        }
                        setData();
                        chkTopSelect.setChecked(false);
                    }
                })
                .show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.screen_existing_dictations_delete_BTN:
                deleteFiles();
                break;
            case R.id.screen_existing_dictations_upload_BTN:
                uploadFiles();
                break;
            case R.id.header_add_BTN: //Add Record
                Globals.e_mode = Enums.MODE.RECORD;
                Globals.g_isExistRecord = false;
                ((MainActivity)getActivity()).setFragment();
                break;
        }
    }

    @Override
    public void onResponse(int code) {

    }
}
