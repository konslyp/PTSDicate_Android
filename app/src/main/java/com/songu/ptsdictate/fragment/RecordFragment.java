package com.songu.ptsdictate.fragment;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.rollbar.android.Rollbar;
import com.songu.ptsdictate.R;
import com.songu.ptsdictate.activity.MainActivity;
import com.songu.ptsdictate.doc.Config;
import com.songu.ptsdictate.doc.Enums;
import com.songu.ptsdictate.doc.Globals;
import com.songu.ptsdictate.model.RecordModel;
import com.songu.ptsdictate.service.IServiceResult;
import com.songu.ptsdictate.util.BookMarkUtil;
import com.songu.ptsdictate.util.PlaybackUtil;
import com.songu.ptsdictate.util.Utils;
import com.songu.ptsdictate.view.BookmarkView;
import com.songu.ptsdictate.view.TimerTextView;
import com.songu.ptsdictate.view.VULedIndicatorBar;
import com.songu.ptsdictate.view.WaveformSeekBar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 3/22/2018.
 */

public class RecordFragment  extends Fragment implements IServiceResult,View.OnClickListener,RadioGroup.OnCheckedChangeListener {


    private View mRootView;
    private ImageView btnRecord, btnStop;
    private TextView txtFileName, txtFileSize;
    private TextView txtTitle;
    private ImageView imgTitle;
    private TimerTextView timerElapse;
    public TimerTextView timerEditElapse;
    public WaveformSeekBar waveformSeekBar;
    private LinearLayout layoutSeekBar;
    private LinearLayout layoutAmp;
    private LinearLayout layoutPlaybackTimebar;
    private TextView txtStatus;
    private VULedIndicatorBar ampBar;
    private LinearLayout layoutBottombar;
    private Button btnSave,btnDiscard,btnEdit;
    private RadioGroup editBar;
    private RadioButton btnAppend,btnInsert,btnOverwrite,btnParitalDelete;
    public PlaybackUtil playbackUtil;
    public BookMarkUtil bookMarkUtil;
    private Button btnPointInsert,btnPointOverwrite,btnPointDelete;
    public int isRecording = 0;
    private RecordModel currentRecordFile;
    private LinearLayout layoutBookmark;
    private BookmarkView bookmarkView;
    private RelativeLayout bookMarkControls;
    private Button btnClearBookmark;
    private ImageView btnBookMarkBack,btnBookMarkNext;
    private ImageView btnBookMarkIndex;
    public boolean isPhonecall = false;
    private boolean isBackup = false;
    private boolean isEditFile = false;
    private Rollbar rollbar = Rollbar.instance();

    public Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message inputMessage) {
            if (inputMessage.what == 0) {
                setUpdateRecordStatus(playbackUtil.getDuration(), Utils.readableFileSize(Globals.g_recordUtil.getFileSize()), 0, 0);
                Globals.g_recordUtil.setFinalTime(playbackUtil.getDuration());
                layoutBottombar.setVisibility(View.VISIBLE);
            }
            else if (inputMessage.what == 1)
            {
                playbackUtil.seekToEndPosition();
                setUpdateRecordStatus(playbackUtil.getDuration(), Utils.readableFileSize(Globals.g_recordUtil.getFileSize()), 0, Globals.g_recordUtil.editTimeElapse);
                Globals.g_recordUtil.setFinalTime(playbackUtil.getDuration());
                Globals.g_recordUtil.timeSwap = playbackUtil.getDuration();
                if (Globals.e_editMode == Enums.EIDTMODE.APPEND)
                    Globals.g_recordUtil.insertStartTime = playbackUtil.getDuration() - Globals.g_recordUtil.cutPoint;
            }
            else if (inputMessage.what == 2)
            {
                playbackUtil.seekToEndPosition();
                Globals.g_recordUtil.setFinalTime(playbackUtil.getDuration());
            }
            else if (inputMessage.what == 3)
            {

                playbackUtil.seekToEndPosition();
                setUpdateRecordStatus(playbackUtil.getDuration(), Utils.readableFileSize(Globals.g_recordUtil.getFileSize()), 0, 0);
                Globals.g_recordUtil.setFinalTime(playbackUtil.getDuration());
                layoutBottombar.setVisibility(View.VISIBLE);

                autoSaveFile();
            }
            else if (inputMessage.what ==4)
            {
                playbackUtil.seekToEndPosition();
                setUpdateRecordStatus(playbackUtil.getDuration(), Utils.readableFileSize(Globals.g_recordUtil.getFileSize()), 0, 0);
                Globals.g_recordUtil.setFinalTime(playbackUtil.getDuration());
                new MaterialDialog.Builder(RecordFragment.this.getContext())
                        .title("Warning")
                        .content("You are approaching your file size upload limit of 80Mb")
                        .titleColor(Color.BLACK)
                        .contentColor(Color.GRAY)
                        .positiveText("OK")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                startOrResumeRecord();
                            }
                        })
                        .show();
            }

        }
    };



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public void callAutosave()
    {
        if (isPhonecall) {
            new MaterialDialog.Builder(this.getContext())
                    .title("PTS Dictate")
                    .content("Due to a phone call, the recording has been auto saved. Please go back to Existing Dictation screen, and choose Edit/Append to continue recording.")
                    .positiveText("OK")
                    .titleColor(Color.BLACK)
                    .contentColor(Color.GRAY)
                    .cancelable(false)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            playbackUtil.pauseFile();
                            if (Globals.g_isExistRecord)
                            {
                                //Globals.g_existFile.mName = Globals.g_recordUtil.getCurrentFileName();
                                Globals.g_existFile.mSize = String.valueOf(Globals.g_recordUtil.getFileSize());
                                Globals.g_existFile.mElapse = String.valueOf(Globals.g_recordUtil.getElapsedTime());
                                Globals.g_existFile.mPath = Globals.g_recordUtil.getCurrentFilePath();
                                Globals.g_existFile.mIsAutoSave = 2;
                                Globals.g_existFile.mIndexData = bookMarkUtil.generateIndexJson();
                                Globals.g_database.updateRecordFile(Globals.g_existFile);
                                Globals.e_mode = Enums.MODE.LIBRARY;
                                ((MainActivity) RecordFragment.this.getActivity()).setFragment();
                                return;
                            }
                            Globals.g_fileIndex++;
                            Utils.saveIndex(RecordFragment.this.getContext());
                            //RecordModel rModel = new RecordModel();
                            currentRecordFile.mName = Globals.g_recordUtil.getCurrentFileName();
                            currentRecordFile.mSize = String.valueOf(Globals.g_recordUtil.getFileSize());
                            currentRecordFile.mElapse = String.valueOf(Globals.g_recordUtil.getElapsedTime());
                            currentRecordFile.mPath = Globals.g_recordUtil.getCurrentFilePath();
                            currentRecordFile.mComment = "";
                            currentRecordFile.mIsAutoSave = 2;
                            currentRecordFile.isComment = 0;
                            currentRecordFile.mIndexData = bookMarkUtil.generateIndexJson();
                            Globals.g_database.updateRecordFile(currentRecordFile);

                            Globals.e_mode = Enums.MODE.LIBRARY;
                            ((MainActivity) RecordFragment.this.getActivity()).setFragment();
                        }
                    })
                    .show();
            isPhonecall = false;
            return;
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mRootView == null)
            mRootView = inflater.inflate(R.layout.fragment_record, container, false);
        Globals.g_recordUtil.initRecord();
        Utils.loadSetting(this.getContext());
        initView();
        isEditFile = false;
        if (Globals.g_isExistRecord)
        {
            rollbar.debug("Existing Record");
            initSeekBar();
            initExistRecord();
            Globals.g_recordUtil.setSampleRate(Globals.g_existFile.mRate);
            editRecordfile();
            isEditFile = false;
            loadBookMark();
            setBookMarkIndexButtonStatus(false);
            timerElapse.setVisibility(View.VISIBLE);
            mRootView.findViewById(R.id.playback_timer).setVisibility(View.VISIBLE);
            if (!checkStoragePermission())
            {
                timerElapse.setVisibility(View.INVISIBLE);
                mRootView.findViewById(R.id.playback_timer).setVisibility(View.GONE);
            }
        }
        else {
            rollbar.debug("New Record");
            initRecord();
            initSeekBar();
            timerElapse.setVisibility(View.VISIBLE);
            if (!checkStoragePermission())
            {
                timerElapse.setVisibility(View.INVISIBLE);
            }
        }
        return mRootView;
    }
    public void loadBookMark()
    {
        bookMarkUtil.setJsonData(Globals.g_existFile.mIndexData);
        bookMarkUtil.sortAndRedraw();
        setBookMarkSetting();
    }
    public void initView()
    {
        ((MainActivity)getActivity()).showHideTab(true);
        if (playbackUtil == null)
            playbackUtil = new PlaybackUtil(this.getContext(),mRootView);
        bookMarkUtil = new BookMarkUtil(this.getContext());
        playbackUtil.setBookMarkUtil(bookMarkUtil);
        btnRecord = (ImageView) mRootView.findViewById(R.id.screen_record_record_pause_BTN);
        btnStop = (ImageView) mRootView.findViewById(R.id.screen_record_stoprecording_BTN);
        txtFileName = (TextView) mRootView.findViewById(R.id.screen_record_file_name_TXT);
        txtFileSize = (TextView) mRootView.findViewById(R.id.screen_record_file_size_TXT);
        txtTitle = (TextView) mRootView.findViewById(R.id.title_TXT);
        imgTitle = (ImageView) mRootView.findViewById(R.id.title_IMG);
        timerElapse = (TimerTextView) mRootView.findViewById(R.id.screen_record_recording_timer_CHRM);
        timerEditElapse = (TimerTextView) mRootView.findViewById(R.id.screen_record_overwriting_timer_CHRM);
        layoutSeekBar = (LinearLayout) mRootView.findViewById(R.id.screen_record_waveformViewLAY);
        layoutAmp = (LinearLayout) mRootView.findViewById(R.id.screen_record_LedIndicatorBar_LAY);
        txtStatus = (TextView) mRootView.findViewById(R.id.screen_record_recording_status_TXT);
        ampBar = (VULedIndicatorBar) mRootView.findViewById(R.id.screen_record_LedIndicatorBar);
        layoutBottombar = (LinearLayout) mRootView.findViewById(R.id.screen_record_save_discard_lay);
        btnSave = (Button) mRootView.findViewById(R.id.screen_record_recording_save_button);
        btnEdit = (Button) mRootView.findViewById(R.id.screen_record_recording_edit_button);
        btnDiscard = (Button) mRootView.findViewById(R.id.screen_record_recording_discard_button);
        editBar = (RadioGroup) mRootView.findViewById(R.id.screen_record_editing_options_RGRP);
        layoutPlaybackTimebar = (LinearLayout) mRootView.findViewById(R.id.playback_timer);
        layoutBookmark = (LinearLayout) mRootView.findViewById(R.id.screen_record_bookmarks_lay);
        bookmarkView = (BookmarkView) mRootView.findViewById(R.id.screen_record_bookmarks_bar);
        bookMarkControls = (RelativeLayout) mRootView.findViewById(R.id.screen_record_bookmark_controls);
        btnClearBookmark = (Button) mRootView.findViewById(R.id.screen_record_delete_bookmark_BTN);
        btnClearBookmark.setEnabled(false);
        bookMarkUtil.setClearButton(btnClearBookmark);
        btnBookMarkBack = (ImageView) mRootView.findViewById(R.id.screen_record_bookmark_previous);
        btnBookMarkIndex = (ImageView) mRootView.findViewById(R.id.screen_record_bookmark_btn);
        btnBookMarkNext  = (ImageView) mRootView.findViewById(R.id.screen_record_bookmark_next);

        btnPointInsert = (Button) mRootView.findViewById(R.id.screen_record_starts_insert_BTN);
        btnPointOverwrite = (Button) mRootView.findViewById(R.id.screen_record_startsBTN);
        btnPointDelete = (Button) mRootView.findViewById(R.id.screen_record_startEraseBTN);

        btnAppend = (RadioButton) mRootView.findViewById(R.id.screen_record_append_RBTN);
        btnInsert = (RadioButton) mRootView.findViewById(R.id.screen_record_insert_RBTN);
        btnOverwrite = (RadioButton) mRootView.findViewById(R.id.screen_record_overwrite_RBTN);
        btnParitalDelete = (RadioButton) mRootView.findViewById(R.id.screen_record_partial_delete_RBTN);
        bookMarkUtil.setBookMarkView(bookmarkView);
        editBar.setOnCheckedChangeListener(this);
        txtTitle.setText("Record");
        imgTitle.setVisibility(View.VISIBLE);
        imgTitle.setImageDrawable(getResources().getDrawable(R.drawable.icn_title_record));
        btnRecord.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        btnEdit.setOnClickListener(this);
        btnDiscard.setOnClickListener(this);

        btnBookMarkNext.setOnClickListener(this);
        btnBookMarkIndex.setOnClickListener(this);
        btnClearBookmark.setOnClickListener(this);
        btnBookMarkBack.setOnClickListener(this);


        btnPointInsert.setOnClickListener(this);
        btnPointOverwrite.setOnClickListener(this);
        btnPointDelete.setOnClickListener(this);

        txtFileSize.setText("0.0MB");
        timerElapse.setMsElapsed(0);

        if (Globals.mSetting.isIndexing)
        {
            layoutBookmark.setVisibility(View.VISIBLE);
            setBookMarkSetting();
        }
        else
        {
            layoutBookmark.setVisibility(View.GONE);
        }
    }
    public void selectBookMark()
    {
        setEnablebookMark(true);
        if (bookMarkUtil.getSelectedBookMark() == -1)
        {
//            btnBookMarkIndex.setEnabled(false);
//            btnBookMarkIndex.setImageDrawable(getResources().getDrawable(R.drawable.icn_bookmark_disable));
            btnBookMarkBack.setEnabled(false);
            btnBookMarkBack.setImageDrawable(getResources().getDrawable(R.drawable.icn_previous_disable));
            //btnClearBookmark.setEnabled(false);
        }
        if (bookMarkUtil.getSelectedBookMark() == bookMarkUtil.getBookMarkCount() - 2)
        {
//            btnBookMarkIndex.setEnabled(false);
//            btnBookMarkIndex.setImageDrawable(getResources().getDrawable(R.drawable.icn_bookmark_disable));
            btnBookMarkNext.setEnabled(false);
            btnBookMarkNext.setImageDrawable(getResources().getDrawable(R.drawable.icn_next_disable));
        }
        else
        {
//            btnBookMarkIndex.setEnabled(false);
//            btnBookMarkIndex.setImageDrawable(getResources().getDrawable(R.drawable.icn_bookmark_disable));
        }
    }
    public void setBookMarkSetting()
    {
        setEnablebookMark(false);
        if (isRecording == 0)
        {
            bookMarkUtil.clearBookMarks();
            setBookMarkIndexButtonStatus(false);
        }
        else if (isRecording == 1)
        {
            if (bookMarkUtil.getBookMarkCount() == 21)
            {
                setBookMarkIndexButtonStatus(false);
            }
            else {
                setBookMarkIndexButtonStatus(true);
            }
        }
        else if (isRecording == 2) // Paused
        {
            if (bookMarkUtil.getBookMarkCount() > 1) {
                selectBookMark();
            }

        }
        else if (isRecording == 3) // Stopped
        {
            if (bookMarkUtil.getBookMarkCount() > 1) {
                selectBookMark();
            }
//            if (bookMarkUtil.getBookMarkCount() == 21)
//            {
//                btnBookMarkIndex.setEnabled(false);
//                btnBookMarkIndex.setImageDrawable(getResources().getDrawable(R.drawable.icn_bookmark_disable));
//            }
//            if (bookMarkUtil.getBookMarkCount() == 1)
//            {
//                btnBookMarkIndex.setEnabled(true);
//                btnBookMarkIndex.setImageDrawable(getResources().getDrawable(R.drawable.icn_bookmark));
//            }
//
//            btnBookMarkIndex.setEnabled(false);
//            btnBookMarkIndex.setImageDrawable(getResources().getDrawable(R.drawable.icn_bookmark_disable));
        }
        else if (isRecording == 4) // Edit
        {
            if (bookMarkUtil.getBookMarkCount() > 0) {
                selectBookMark();
            }
//            if (bookMarkUtil.getBookMarkCount() == 21)
//            {
//                btnBookMarkIndex.setEnabled(false);
//                btnBookMarkIndex.setImageDrawable(getResources().getDrawable(R.drawable.icn_bookmark_disable));
//            }
//            if (bookMarkUtil.getBookMarkCount() == 1)
//            {
//                btnBookMarkIndex.setEnabled(true);
//                btnBookMarkIndex.setImageDrawable(getResources().getDrawable(R.drawable.icn_bookmark));
//            }
//            btnBookMarkIndex.setEnabled(false);
//            btnBookMarkIndex.setImageDrawable(getResources().getDrawable(R.drawable.icn_bookmark_disable));
        }
    }
    public void setBookMarkIndexButtonStatus(boolean b)
    {
        if (btnBookMarkIndex == null) return;
        if (b)
        {
            btnBookMarkIndex.setEnabled(true);
            btnBookMarkIndex.setClickable(true);
            btnBookMarkIndex.setImageDrawable(getResources().getDrawable(R.drawable.icn_bookmark));
        }
        else
        {
            btnBookMarkIndex.setEnabled(false);
            btnBookMarkIndex.setClickable(false);
            btnBookMarkIndex.setImageDrawable(getResources().getDrawable(R.drawable.icn_bookmark_disable));
        }
    }
    public void setEnablebookMark(boolean visible)
    {
        //btnClearBookmark.setEnabled(visible);
        btnBookMarkBack.setEnabled(visible);
        btnBookMarkNext.setEnabled(visible);
        btnBookMarkIndex.setEnabled(visible);

        if (!visible) {
            btnBookMarkBack.setImageDrawable(getResources().getDrawable(R.drawable.icn_previous_disable));
            btnBookMarkNext.setImageDrawable(getResources().getDrawable(R.drawable.icn_next_disable));
            btnBookMarkIndex.setImageDrawable(getResources().getDrawable(R.drawable.icn_bookmark_disable));
        }
        else
        {
            btnBookMarkBack.setImageDrawable(getResources().getDrawable(R.drawable.icn_previous));
            btnBookMarkNext.setImageDrawable(getResources().getDrawable(R.drawable.icn_next));
            btnBookMarkIndex.setImageDrawable(getResources().getDrawable(R.drawable.icn_bookmark));
        }
    }
    public void initExistRecord()
    {
        layoutBottombar.setVisibility(View.GONE);
        changeRecordButtonStatus();
        Globals.g_recordUtil.setAttachViews(this);
        txtFileName.setText(Globals.g_existFile.mName);

        Globals.g_recordUtil.mCurrentPath = Globals.g_existFile.mPath;
        Globals.g_recordUtil.setFileSize(Long.parseLong(Globals.g_existFile.mSize));
        try {
            waveformSeekBar.setAudio((InputStream) new FileInputStream(Globals.g_recordUtil.getCurrentFilePath()));
            playbackUtil.setRecordFile(Globals.g_recordUtil.getCurrentFilePath(),waveformSeekBar);
            playbackUtil.setBookMarks(bookMarkUtil.setJsonData(Globals.g_existFile.mIndexData));
            mHandler.sendEmptyMessageDelayed(1,500);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        layoutBottombar.setVisibility(View.VISIBLE);
    }
    public void initRecord()
    {
        layoutBottombar.setVisibility(View.GONE);
        changeRecordButtonStatus();
        Globals.g_recordUtil.setAttachViews(this);
        List<String> formats = Arrays.asList(getResources().getStringArray(R.array.filenameformat));
        String fileName = Globals.g_recordUtil.getNewFilename(Globals.mSetting.mFilePrefix,formats.get(Globals.mSetting.mDateFormat),Globals.g_fileIndex);
        txtFileName.setText(fileName);
    }
    public void initSeekBar()
    {
        if (waveformSeekBar == null) {
            waveformSeekBar = new WaveformSeekBar(getActivity());
            waveformSeekBar.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            layoutSeekBar.addView(waveformSeekBar);
        }
        waveformSeekBar.setProgress(0);
        mRootView.findViewById(R.id.relBarRecord).setVisibility(View.VISIBLE);
        if (!checkStoragePermission())
        {
            mRootView.findViewById(R.id.relBarRecord).setVisibility(View.GONE);
        }
    }
    public void changeRecordButtonStatus()
    {
        btnRecord.setImageDrawable(getResources().getDrawable(R.drawable.icn_record));
        btnStop.setImageDrawable(getResources().getDrawable(R.drawable.icn_stop));
        layoutAmp.setVisibility(View.GONE);
        layoutSeekBar.setVisibility(View.GONE);
        layoutPlaybackTimebar.setVisibility(View.GONE);
        txtStatus.setVisibility(View.VISIBLE);
        editBar.setVisibility(View.GONE);
        btnRecord.setEnabled(true);
        btnStop.setEnabled(true);
        switch (isRecording)
        {
            case 0: // Init
                playbackUtil.setEnableControl(false);
                layoutAmp.setVisibility(View.VISIBLE);
                txtStatus.setVisibility(View.GONE);
                btnStop.setImageDrawable(getResources().getDrawable(R.drawable.icn_stop_disable));
                btnStop.setEnabled(false);
                break;
            case 1: // Recording
                playbackUtil.setEnableControl(false);
                layoutAmp.setVisibility(View.VISIBLE);
                txtStatus.setText("Recording");
                btnRecord.setImageDrawable(getResources().getDrawable(R.drawable.icn_pause_recorder));
                btnStop.setImageDrawable(getResources().getDrawable(R.drawable.icn_stop));
                break;
            case 2: // Paused
                playbackUtil.setEnableControl(true);
                layoutSeekBar.setVisibility(View.VISIBLE);
                layoutPlaybackTimebar.setVisibility(View.VISIBLE);
                txtStatus.setText("Paused");
                btnRecord.setImageDrawable(getResources().getDrawable(R.drawable.icn_record));
                btnStop.setImageDrawable(getResources().getDrawable(R.drawable.icn_stop));
                break;
            case 3: // Stopped
                playbackUtil.setEnableControl(true);
                layoutSeekBar.setVisibility(View.VISIBLE);
                layoutPlaybackTimebar.setVisibility(View.VISIBLE);
                txtStatus.setText("Stopped");
                timerEditElapse.setVisibility(View.GONE);
                btnRecord.setImageDrawable(getResources().getDrawable(R.drawable.icn_record_disable));
                btnStop.setImageDrawable(getResources().getDrawable(R.drawable.icn_stop_disable));
                btnRecord.setEnabled(false);
                btnStop.setEnabled(false);

                timerEditElapse.setVisibility(View.GONE);
                btnPointInsert.setVisibility(View.GONE);
                btnPointOverwrite.setVisibility(View.GONE);
                btnPointDelete.setVisibility(View.GONE);

                waveformSeekBar.clearMarkPoint();
                break;
            case 4: // Edit
                txtStatus.setText("Stopped");
                layoutBottombar.setVisibility(View.GONE);
                layoutSeekBar.setVisibility(View.VISIBLE);
                layoutPlaybackTimebar.setVisibility(View.VISIBLE);
                playbackUtil.setEnableControl(true);
                editBar.setVisibility(View.VISIBLE);
                btnRecord.setImageDrawable(getResources().getDrawable(R.drawable.icn_record_disable));
                btnStop.setImageDrawable(getResources().getDrawable(R.drawable.icn_stop));
                btnRecord.setEnabled(false);
                btnStop.setEnabled(true);
                txtStatus.setVisibility(View.GONE);
                break;
        }
    }
    public void startEditRecording()
    {
        playbackUtil.pauseFile();
        if (Globals.g_existFile != null) {
            Globals.g_existFile.mIsAutoSave = 1;
            Globals.g_database.updateRecordFile(Globals.g_existFile);
            if (!isBackup) {
                Globals.g_recordUtil.backupOriginalFile(Globals.g_existFile.mPath);
                isBackup = true;
            }
        }
        switch (Globals.e_editMode)
        {
            case APPEND:
                txtStatus.setText("Recording");
                if (waveformSeekBar.getProgress() == playbackUtil.getDuration()) {
                    Globals.g_recordUtil.appendRecording();
                    timerEditElapse.setVisibility(View.GONE);
                    isRecording = 1;
                    changeRecordButtonStatus();
                }
                else
                {
                    Globals.g_recordUtil.appendRecordingBeforeEnd(waveformSeekBar.getProgress());
                    timerEditElapse.setVisibility(View.VISIBLE);
                    btnPointInsert.setVisibility(View.GONE);
                    waveformSeekBar.clearMarkPoint();
                    isRecording = 1;
                    changeRecordButtonStatus();
                }
                break;
            case INSERT:
                if (!waveformSeekBar.hasStartPoint())
                {
                    this.waveformSeekBar.setMarkPointX(playbackUtil.getCurrentPosition());
                    btnPointInsert.setBackground(getResources().getDrawable(R.drawable.btn_insert));
                }
                txtStatus.setText("Inserting");
                Globals.g_recordUtil.insertRecording(waveformSeekBar.getMarkPoint());
                timerEditElapse.setVisibility(View.VISIBLE);
                btnPointInsert.setVisibility(View.GONE);
                waveformSeekBar.clearMarkPoint();
                isRecording = 1;
                changeRecordButtonStatus();
                break;
            case OVERWRITE:
                txtStatus.setText("Overwriting");
                Globals.g_recordUtil.overwriteRecording(waveformSeekBar.getMarkPoint(),waveformSeekBar.getEndPoint());
                timerEditElapse.setVisibility(View.VISIBLE);
                btnPointOverwrite.setVisibility(View.GONE);
                waveformSeekBar.clearMarkPoint();
                isRecording = 1;
                changeRecordButtonStatus();
                break;
            case PARTIALDELETE:
                txtStatus.setText("Stopped");
                Globals.g_recordUtil.partialEraseRecording(waveformSeekBar.getMarkPoint(),waveformSeekBar.getEndPoint());
                timerEditElapse.setVisibility(View.GONE);
                btnPointDelete.setVisibility(View.GONE);
                //bookMarkUtil.partialDeleteMark(waveformSeekBar.getMarkPoint(),waveformSeekBar.getEndPoint());
                waveformSeekBar.clearMarkPoint();
                stopRecording();
                break;
        }
        setBookMarkSetting();
    }
    public void initAutosaveFile()
    {
        currentRecordFile = new RecordModel();
        currentRecordFile.mIsAutoSave = 1;
        currentRecordFile.mPath = Globals.g_recordUtil.getCurrentFilePath();
        currentRecordFile.mName = Globals.g_recordUtil.getCurrentFileName();
        currentRecordFile.mRate = Globals.g_recordUtil.getSampleRate();
        Globals.g_database.insertRecordFile(currentRecordFile);
        currentRecordFile = Globals.g_database.getLastInsertFile();
    }
    public void startOrResumeRecord()
    {
        playbackUtil.pauseFile();
        if (((MainActivity)getActivity()) == null) return;
        ((MainActivity)getActivity()).showHideTab(true);
        if (isRecording == 0)
        {
            Globals.e_editMode = Enums.EIDTMODE.NONE;
            Globals.g_recordUtil.startRecording();
            initAutosaveFile();
        }
        else if (isRecording == 1)
        {
            Globals.g_recordUtil.pauseRecording();
            setBookMarkIndexButtonStatus(false);
            try {
                waveformSeekBar.setAudio((InputStream) new FileInputStream(Globals.g_recordUtil.getAutoSaveFile()));
                playbackUtil.setRecordFile(Globals.g_recordUtil.getAutoSaveFile(),waveformSeekBar);
                ((TextView)mRootView.findViewById(R.id.screen_record_mediaplayer_currentposition)).setText(Utils.getTimeString(playbackUtil.getDuration()));
                playbackUtil.setBookMarks(bookMarkUtil.getBookMarks());


                //Globals.g_recordUtil.setFinalTime(playbackUtil.getDuration());
                //setUpdateRecordStatus(playbackUtil.getDuration(), Utils.readableFileSize(Globals.g_recordUtil.getFileSize()), 0, 0);
                mHandler.sendEmptyMessageDelayed(1,500);
                //waveformSeekBar.setProgress(playbackUtil.getDuration());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.e("Error-File",e.getMessage());
            }
        }
        else if (isRecording == 2)
        {
            setBookMarkIndexButtonStatus(true);
            Globals.g_recordUtil.resumeRecording();
        }
        if (isRecording == 0) isRecording = 1;
        else if (isRecording == 1) isRecording = 2;
        else if (isRecording == 2) isRecording = 1;
        changeRecordButtonStatus();
        setBookMarkSetting();
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
    public boolean isStopped()
    {
        if (isRecording == 3 || isRecording == 0)
        {
            return true;
        }
        else if (isRecording == 4)
        {
            if (isEditFile)
            {
                return false;
            }
            if (Globals.g_isExistRecord)
            {
                return true;
            }
            File file = new File(Globals.g_recordUtil.getCurrentFilePath());
            file.delete();
            Globals.g_database.deleteRecordFile(currentRecordFile.mLocalNo);
            return true;
        }
        return false;
    }
    public void showWarningMesasge()
    {
        startOrResumeRecord();
        new MaterialDialog.Builder(RecordFragment.this.getContext())
                .title("Warning")
                .content("You are approaching your file size upload limit of 80Mb")
                .titleColor(Color.BLACK)
                .contentColor(Color.GRAY)
                .positiveText("OK")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        startOrResumeRecord();
                    }
                })
                .show();

//        Globals.g_recordUtil.pauseRecording();
//        try {
//            waveformSeekBar.setAudio((InputStream) new FileInputStream(Globals.g_recordUtil.getAutoSaveFile()));
//            playbackUtil.setRecordFile(Globals.g_recordUtil.getAutoSaveFile(),waveformSeekBar);
//            ((TextView)mRootView.findViewById(R.id.screen_record_mediaplayer_currentposition)).setText(Utils.getTimeString(playbackUtil.getDuration()));
//            playbackUtil.setBookMarks(bookMarkUtil.getBookMarks());
//            //Globals.g_recordUtil.setFinalTime(playbackUtil.getDuration());
//            //setUpdateRecordStatus(playbackUtil.getDuration(), Utils.readableFileSize(Globals.g_recordUtil.getFileSize()), 0, 0);
//            mHandler.sendEmptyMessageDelayed(4,500);
//            //waveformSeekBar.setProgress(playbackUtil.getDuration());
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//            Log.e("Error-File",e.getMessage());
//        }
        //mHandler.sendEmptyMessageDelayed(4,1000);
    }
    public void autoSaveRecording()
    {
        playbackUtil.pauseFile();
        isRecording = 3;
        ((MainActivity)getActivity()).showHideTab(false);
        setBookMarkSetting();
        changeRecordButtonStatus();

        if (Globals.g_recordUtil.lstTempFiles.size() > 0)
            showEditAlert();
        Globals.g_recordUtil.stopRecording();
        try {
            waveformSeekBar.setAudio((InputStream) new FileInputStream(Globals.g_recordUtil.getCurrentFilePath()));
            playbackUtil.setRecordFile(Globals.g_recordUtil.getCurrentFilePath(),waveformSeekBar);
            playbackUtil.setBookMarks(bookMarkUtil.getBookMarks());
            mHandler.sendEmptyMessageDelayed(3,500);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        //mHandler.sendEmptyMessageDelayed(3,1000);
    }
    public void stopRecording()
    {
        playbackUtil.pauseFile();
        isRecording = 3;
        ((MainActivity)getActivity()).showHideTab(false);
        setBookMarkSetting();
        changeRecordButtonStatus();
        setBookMarkIndexButtonStatus(false);
        if (checkPermission()) {

            if (Globals.g_recordUtil.lstTempFiles.size() > 0)
                showEditAlert();
            Globals.g_recordUtil.stopRecording();
            try {
                waveformSeekBar.firstDraw = true;
                waveformSeekBar.setAudio((InputStream) new FileInputStream(Globals.g_recordUtil.getCurrentFilePath()));
                playbackUtil.setRecordFile(Globals.g_recordUtil.getCurrentFilePath(), waveformSeekBar);
                playbackUtil.setBookMarks(bookMarkUtil.getBookMarks());
                mHandler.sendEmptyMessageDelayed(2, 500);
            } catch (FileNotFoundException e) {
                Log.e("Exception Stop----------",e.getMessage());
                e.printStackTrace();
            }
        }

        mHandler.sendEmptyMessageDelayed(0,1000);
    }
    public void showEditAlert()
    {
        MaterialDialog alert = new MaterialDialog.Builder(this.getContext())
                .title("PTS Dictate")
                .positiveText("OK")
                .titleColor(Color.BLACK)
                .contentColor(Color.GRAY)
                .build();
        switch (Globals.e_editMode)
        {
            case APPEND:
                bookMarkUtil.deleteOverDurationMark((int) Globals.g_recordUtil.finalTime);
                alert.setContent("Append complete");
                alert.show();
                break;
            case INSERT:
                alert.setContent("Insert complete");
                alert.show();
                break;
            case OVERWRITE:
                alert.setContent("Overwrite complete");
                alert.show();
                break;
            case PARTIALDELETE:
                bookMarkUtil.deleteOverDurationMark((int) Globals.g_recordUtil.finalTime);
                break;
        }
        Globals.e_editMode = Enums.EIDTMODE.NONE;
    }
    public void autoSaveFile()
    {
        playbackUtil.pauseFile();
        if (Globals.g_isExistRecord)
        {
            //Globals.g_existFile.mName = Globals.g_recordUtil.getCurrentFileName();
            Globals.g_existFile.mSize = String.valueOf(Globals.g_recordUtil.getFileSize());
            Globals.g_existFile.mElapse = String.valueOf(playbackUtil.getDuration());
            Globals.g_existFile.mPath = Globals.g_recordUtil.getCurrentFilePath();
            Globals.g_existFile.mIsAutoSave = 2;
            Globals.g_existFile.mIndexData = bookMarkUtil.generateIndexJson();
            Globals.g_database.updateRecordFile(Globals.g_existFile);
            Globals.e_mode = Enums.MODE.LIBRARY;
            ((MainActivity) getActivity()).showHideTab(true);
            ((MainActivity) RecordFragment.this.getActivity()).setFragment();
            return;
        }
        Globals.g_fileIndex++;
        Utils.saveIndex(RecordFragment.this.getContext());
        //RecordModel rModel = new RecordModel();
        currentRecordFile.mName = Globals.g_recordUtil.getCurrentFileName();
        currentRecordFile.mSize = String.valueOf(Globals.g_recordUtil.getFileSize());
        currentRecordFile.mElapse = String.valueOf(playbackUtil.getDuration());
        currentRecordFile.mPath = Globals.g_recordUtil.getCurrentFilePath();
        currentRecordFile.mComment = "";
        currentRecordFile.mIsAutoSave = 2;
        currentRecordFile.isComment = 0;
        currentRecordFile.mIndexData = bookMarkUtil.generateIndexJson();
        Globals.g_database.updateRecordFile(currentRecordFile);
        Globals.e_mode = Enums.MODE.LIBRARY;
        ((MainActivity) getActivity()).showHideTab(true);
        ((MainActivity) RecordFragment.this.getActivity()).setFragment();
    }
    public void saveRecordFile()
    {
        isEditFile = false;
        new MaterialDialog.Builder(this.getContext())
                .title("PTS Dictate")
                .content("Do you want to Save the current Recording?")
                .positiveText("Yes")
                .negativeText("No")
                .titleColor(Color.BLACK)
                .contentColor(Color.GRAY)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (!checkStoragePermission())
                        {
                            if (Globals.g_isExistRecord) {
                                Globals.g_existFile.mIsAutoSave = 0;
                                Globals.g_database.updateRecordFile(Globals.g_existFile);
                                Globals.e_mode = Enums.MODE.LIBRARY;
                                ((MainActivity) getActivity()).showHideTab(true);
                                ((MainActivity) RecordFragment.this.getActivity()).setFragment();
                                return;
                            }

                        }
                        playbackUtil.pauseFile();
                        if (Globals.g_isExistRecord)
                        {
                            //Globals.g_existFile.mName = Globals.g_recordUtil.getCurrentFileName();
                            Globals.g_existFile.mSize = String.valueOf(Globals.g_recordUtil.getFileSize());
                            Globals.g_existFile.mElapse = String.valueOf(Globals.g_recordUtil.getElapsedTime());
                            Globals.g_existFile.mPath = Globals.g_recordUtil.getCurrentFilePath();
                            Globals.g_existFile.mIsAutoSave = 0;
                            Globals.g_existFile.mIndexData = bookMarkUtil.generateIndexJson();
                            Globals.g_database.updateRecordFile(Globals.g_existFile);
                            if (Globals.mSetting.isCommentScreen)
                            {
                                Globals.e_mode = Enums.MODE.LIBRARY_COMMENT;
                                ((MainActivity) RecordFragment.this.getActivity()).setFragment();
                            }
                            else {
                                Globals.e_mode = Enums.MODE.LIBRARY;
                                ((MainActivity) getActivity()).showHideTab(true);
                                ((MainActivity) RecordFragment.this.getActivity()).setFragment();
                            }
                            return;
                        }
                        Globals.g_fileIndex++;
                        Utils.saveIndex(RecordFragment.this.getContext());
                        //RecordModel rModel = new RecordModel();
                        currentRecordFile.mName = Globals.g_recordUtil.getCurrentFileName();
                        currentRecordFile.mSize = String.valueOf(Globals.g_recordUtil.getFileSize());
                        currentRecordFile.mElapse = String.valueOf(Globals.g_recordUtil.getElapsedTime());
                        currentRecordFile.mPath = Globals.g_recordUtil.getCurrentFilePath();
                        currentRecordFile.mComment = "";
                        currentRecordFile.mIsAutoSave = 0;
                        currentRecordFile.isComment = 0;
                        currentRecordFile.mIndexData = bookMarkUtil.generateIndexJson();
                        Globals.g_database.updateRecordFile(currentRecordFile);
                        if (Globals.mSetting.isCommentScreen)
                        {
                            Globals.g_existFile = currentRecordFile;
                            Globals.e_mode = Enums.MODE.LIBRARY_COMMENT;
                            ((MainActivity) RecordFragment.this.getActivity()).setFragment();
                        }
                        else {
                            Globals.e_mode = Enums.MODE.LIBRARY;
                            ((MainActivity) getActivity()).showHideTab(true);
                            ((MainActivity) RecordFragment.this.getActivity()).setFragment();
                        }
                    }
                })
                .show();
    }
    public void editRecordfile()
    {
        isEditFile = true;
        playbackUtil.pauseFile();
        isRecording = 4; // Edit
        Globals.e_editMode = Enums.EIDTMODE.NONE;
        btnAppend.setChecked(false);
        btnInsert.setChecked(false);
        btnOverwrite.setChecked(false);
        btnParitalDelete.setChecked(false);
        editBar.clearCheck();
        changeRecordButtonStatus();
        setBookMarkSetting();
        ((MainActivity)getActivity()).showHideTab(true);

    }
    public void discardRecordFile()
    {
        isEditFile = false;
        //Delete Saved File
        new MaterialDialog.Builder(this.getContext())
                .title("PTS Dictate")
                .content("Do you want to discard the current Recording?")
                .positiveText("Yes")
                .negativeText("No")
                .titleColor(Color.BLACK)
                .contentColor(Color.GRAY)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        playbackUtil.pauseFile();
                        if (Globals.g_existFile != null) {
                            Globals.g_existFile.mIsAutoSave = 0;
                            Globals.g_database.updateRecordFile(Globals.g_existFile);
                            Globals.g_recordUtil.restoreFile(Globals.g_recordUtil.getCurrentFilePath());
                        }
                        if (Globals.g_isExistRecord)
                        {
                            Globals.e_mode = Enums.MODE.LIBRARY;
                            ((MainActivity)RecordFragment.this.getActivity()).setFragment();
                            return;
                        }
                        File file = new File(Globals.g_recordUtil.getCurrentFilePath());
                        file.delete();
                        Globals.g_database.deleteRecordFile(currentRecordFile.mLocalNo);
                        isRecording = 0;
                        initView();
                        initRecord();
                        //initSeekBar();
                    }
                })
                .show();
    }
    public boolean checkStoragePermission()
    {
        return MainActivity.hasPermissions(this.getContext(),  Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);
    }
    public boolean checkPermission()
    {
        return MainActivity.hasPermissions(this.getContext(),  Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO);
    }
    @Override
    public void onResume()
    {
        super.onResume();
        if (timerElapse.getVisibility() == View.INVISIBLE)
        {
            if (checkStoragePermission())
            {
                if (Globals.g_isExistRecord)
                {
                    initSeekBar();
                    //initExistRecord();
                    editRecordfile();
                    loadBookMark();
                    timerElapse.setVisibility(View.VISIBLE);
                    mRootView.findViewById(R.id.playback_timer).setVisibility(View.VISIBLE);
                    if (!checkStoragePermission())
                    {
                        timerElapse.setVisibility(View.INVISIBLE);
                        mRootView.findViewById(R.id.playback_timer).setVisibility(View.GONE);
                    }
                }
                else {
                    //initRecord();
                    initSeekBar();
                    timerElapse.setVisibility(View.VISIBLE);
                    if (!checkStoragePermission())
                    {
                        timerElapse.setVisibility(View.INVISIBLE);
                    }
                }
            }

        }
    }
    public void showAccessDenied()
    {
        new MaterialDialog.Builder(this.getContext())
                .title("Access Denied")
                .content("This app requires access to your device's Microphone & Storage. Please enable them in Settings/App/PTS Dictate/Permissions.")
                .positiveText("OK")
                .titleColor(Color.BLACK)
                .contentColor(Color.GRAY)
                .show();
    }
    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.screen_record_record_pause_BTN:
                if (checkPermission()) {
                    if (isRecording == 4) {
                        startEditRecording();
                    } else {
                        startOrResumeRecord();
                    }
                }
                else
                {
                    rollbar.debug("Access Denied");
                    showAccessDenied();
                }
                break;
            case R.id.screen_record_stoprecording_BTN:
                rollbar.debug("Stop Button Clicked");
                stopRecording();
                break;
            //Bottom Bar
            case R.id.screen_record_recording_save_button:
                rollbar.debug("Save Button Clicked");
                saveRecordFile();
                break;
            case R.id.screen_record_recording_edit_button:
                rollbar.debug("Edit Button Clicked");
                editRecordfile();
                break;
            case R.id.screen_record_recording_discard_button:
                rollbar.debug("Discard Button Clicked");
                discardRecordFile();
                break;
            case R.id.screen_record_starts_insert_BTN: // StartPoint Or Insert
                if (!waveformSeekBar.hasStartPoint()) {
                    if (waveformSeekBar.getProgress() == playbackUtil.getDuration())
                    {
                        new MaterialDialog.Builder(this.getContext())
                                .title("PTS Dictate")
                                .content("Start Point should not be end of the file.")
                                .positiveText("OK")
                                .titleColor(Color.BLACK)
                                .contentColor(Color.GRAY)
                                .show();
                        return;
                    }
                    this.waveformSeekBar.setMarkPointX(playbackUtil.getCurrentPosition());
                    btnPointInsert.setBackground(getResources().getDrawable(R.drawable.btn_insert));
                }
                else
                {
                    if (checkPermission()) {
                        startEditRecording();
                    }
                    else
                    {
                        new MaterialDialog.Builder(this.getContext())
                                .title("Access Denied")
                                .content("This app requires access to your device's Microphone & Storage. Please enable them in Settings/App/PTS Dictate/Permissions.")
                                .positiveText("OK")
                                .titleColor(Color.BLACK)
                                .contentColor(Color.GRAY)
                                .show();
                    }
                }
                break;
            case R.id.screen_record_startsBTN: //Overwrite
                if (!waveformSeekBar.hasStartPoint()) {
                    if (waveformSeekBar.getProgress() == playbackUtil.getDuration())
                    {
                        new MaterialDialog.Builder(this.getContext())
                                .title("PTS Dictate")
                                .content("Start Point should not be end of the file.")
                                .positiveText("OK")
                                .titleColor(Color.BLACK)
                                .contentColor(Color.GRAY)
                                .show();
                        return;
                    }
                    this.waveformSeekBar.setMarkPointX(playbackUtil.getCurrentPosition());
                    btnPointOverwrite.setBackground(getResources().getDrawable(R.drawable.btn_end_point));
                }
                else if (!waveformSeekBar.hasEndPoint())
                {
                    if (waveformSeekBar.getMarkPoint() >= waveformSeekBar.getProgress() || Utils.simpleTimeString(waveformSeekBar.getMarkPoint()).equals(Utils.simpleTimeString(waveformSeekBar.getProgress())))
                    {
                        new MaterialDialog.Builder(this.getContext())
                                .title("PTS Dictate")
                                .content("End Point should be greater than Start Point")
                                .positiveText("OK")
                                .titleColor(Color.BLACK)
                                .contentColor(Color.GRAY)
                                .show();
                        return;
                    }
                    this.waveformSeekBar.setMarkEndPointX(playbackUtil.getCurrentPosition());
                    btnPointOverwrite.setBackground(getResources().getDrawable(R.drawable.btn_start_overwrite));
                }
                else
                {
                    if (checkPermission()) {
                        startEditRecording();
                    }
                    else
                    {
                        new MaterialDialog.Builder(this.getContext())
                                .title("Access Denied")
                                .content("This app requires access to your device's Microphone & Storage. Please enable them in Settings/App/PTS Dictate/Permissions.")
                                .positiveText("OK")
                                .titleColor(Color.BLACK)
                                .contentColor(Color.GRAY)
                                .show();
                    }
                }
                break;
            case R.id.screen_record_startEraseBTN:
                if (!waveformSeekBar.hasStartPoint()) {
                    if (waveformSeekBar.getProgress() == playbackUtil.getDuration())
                    {
                        new MaterialDialog.Builder(this.getContext())
                                .title("PTS Dictate")
                                .content("Start Point should not be end of the file.")
                                .positiveText("OK")
                                .titleColor(Color.BLACK)
                                .contentColor(Color.GRAY)
                                .show();
                        return;
                    }
                    this.waveformSeekBar.setMarkPointX(playbackUtil.getCurrentPosition());
                    btnPointDelete.setBackground(getResources().getDrawable(R.drawable.btn_end_point));
                }
                else if (!waveformSeekBar.hasEndPoint())
                {
                    if (waveformSeekBar.getMarkPoint() > waveformSeekBar.getProgress() || Utils.simpleTimeString(waveformSeekBar.getMarkPoint()).equals(Utils.simpleTimeString(waveformSeekBar.getProgress())))
                    {
                        new MaterialDialog.Builder(this.getContext())
                                .title("PTS Dictate")
                                .content("End Point should be greater than Start Point")
                                .positiveText("OK")
                                .titleColor(Color.BLACK)
                                .contentColor(Color.GRAY)
                                .show();
                        return;
                    }
                    this.waveformSeekBar.setMarkEndPointX(playbackUtil.getCurrentPosition());
                    btnPointDelete.setBackground(getResources().getDrawable(R.drawable.btn_start_del));
                }
                else
                {
                    if (checkPermission()) {
                        startEditRecording();
                    }
                    else
                    {
                        new MaterialDialog.Builder(this.getContext())
                                .title("Access Denied")
                                .content("This app requires access to your device's Microphone & Storage. Please enable them in Settings/App/PTS Dictate/Permissions.")
                                .positiveText("OK")
                                .titleColor(Color.BLACK)
                                .contentColor(Color.GRAY)
                                .show();
                    }
                }
                break;
            //BookMark
            case R.id.screen_record_bookmark_btn:
                addBookMark();
                break;
            case R.id.screen_record_bookmark_next:
                int value = (int) bookMarkUtil.selectNext();
                //waveformSeekBar.setProgress(value);
                if (playbackUtil != null) {
                    playbackUtil.setProgress(value);
                    //playbackUtil.txtCurrentTime.setText(Utils.getTimeString(value));
                }
                setBookMarkSetting();
                break;
            case R.id.screen_record_bookmark_previous:
                value = (int) bookMarkUtil.selectBack();
                playbackUtil.setProgress(value);
                //waveformSeekBar.setProgress(value);
                //playbackUtil.txtCurrentTime.setText(Utils.getTimeString(value));
                setBookMarkSetting();
                break;
            case R.id.screen_record_delete_bookmark_BTN:
                bookMarkUtil.removeBookMark();
                setBookMarkSetting();
                bookMarkUtil.selectBookMark(waveformSeekBar.getProgress());
                break;

        }
    }
    public void addBookMark()
    {
        switch (Globals.e_editMode)
        {
            case APPEND:
                if (bookMarkUtil.getBookMarkCount() < 21) {
                    if (isRecording == 1) {
                        bookMarkUtil.addBookMark(Globals.g_recordUtil.finalTime);
                        setBookMarkSetting();
                    }
                    else
                    {
                        bookMarkUtil.addBookMark(waveformSeekBar.getProgress());
                        setBookMarkSetting();
                    }
                }
                break;
            case INSERT:
                if (bookMarkUtil.getBookMarkCount() < 21) {
                    if (isRecording == 1) {
                        bookMarkUtil.addBookMark(Globals.g_recordUtil.editTimeElapse);
                        setBookMarkSetting();
                    }
                    else
                    {
                        bookMarkUtil.addBookMark(waveformSeekBar.getProgress());
                        setBookMarkSetting();
                    }
                }
                break;
            case OVERWRITE:
                if (bookMarkUtil.getBookMarkCount() < 21) {
                    if (isRecording == 1) {
                        bookMarkUtil.addBookMark(Globals.g_recordUtil.editTimeElapse);
                        setBookMarkSetting();
                    }
                    else
                    {
                        bookMarkUtil.addBookMark(waveformSeekBar.getProgress());
                        setBookMarkSetting();
                    }
                }
                break;
            case PARTIALDELETE:
                if (bookMarkUtil.getBookMarkCount() < 21) {
                    if (isRecording != 1) {
                        bookMarkUtil.addBookMark(waveformSeekBar.getProgress());
                        setBookMarkSetting();
                    }
                }
                break;
            case NONE:
                if (isRecording == 1) {
                    if (bookMarkUtil.getBookMarkCount() < 21) {
                        bookMarkUtil.addBookMark(Globals.g_recordUtil.finalTime);
                        setBookMarkSetting();
                    }
                }
                else
                {
                    if (waveformSeekBar != null) {
                        if (bookMarkUtil.getBookMarkCount() < 21) {
                            bookMarkUtil.addBookMark(waveformSeekBar.getProgress());
                            setBookMarkSetting();
                        }
                    }
                }
                break;
        }
    }
    public void setUpdateRecordStatus(final long elapse, final String size,final float level,final long editTime)
    {
        if (this.getActivity() != null) {
            this.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    txtFileSize.setText(size);
                    timerElapse.setMsElapsed(elapse);
                    timerEditElapse.setMsElapsed(editTime);
                    ampBar.setLevel(level);
                }
            });
        }
    }
    @Override
    public void onResponse(int code) {

    }
    public void showEditAlertDialog(String title,String content)
    {
        new MaterialDialog.Builder(this.getContext())
                .title(title)
                .content(content)
                .positiveText("OK")
                .titleColor(Color.BLACK)
                .contentColor(Color.GRAY)
                .show();
    }
    public void setEditMode()
    {
        btnPointDelete.setVisibility(View.GONE);
        btnPointOverwrite.setVisibility(View.GONE);
        btnPointInsert.setVisibility(View.GONE);
        waveformSeekBar.clearMarkPoint();
        switch (Globals.e_editMode)
        {
            case APPEND:
                btnRecord.setEnabled(true);
                btnRecord.setImageDrawable(getResources().getDrawable(R.drawable.icn_record));
                break;
            case INSERT:
                btnPointInsert.setVisibility(View.VISIBLE);
                btnPointInsert.setBackground(getResources().getDrawable(R.drawable.btn_start_point));
                btnRecord.setEnabled(false);
                btnRecord.setImageDrawable(getResources().getDrawable(R.drawable.icn_record_disable));
                break;
            case OVERWRITE:
                btnPointOverwrite.setVisibility(View.VISIBLE);
                //btnPointOverwrite.setText("Start Overwriting");
                btnPointOverwrite.setBackground(getResources().getDrawable(R.drawable.btn_start_point));
                btnRecord.setEnabled(false);
                btnRecord.setImageDrawable(getResources().getDrawable(R.drawable.icn_record_disable));
                break;
            case PARTIALDELETE:
                btnPointDelete.setVisibility(View.VISIBLE);
                btnPointDelete.setBackground(getResources().getDrawable(R.drawable.btn_start_point));
                btnRecord.setEnabled(false);
                btnRecord.setImageDrawable(getResources().getDrawable(R.drawable.icn_record_disable));
                break;
        }
    }
    @Override
    public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
        playbackUtil.pauseFile();
        if (!checkPermission())
        {
            showAccessDenied();
            return;
        }
        switch (i)
        {
            case R.id.screen_record_append_RBTN:
                if (btnAppend.isChecked()) {
                    if (!Globals.mSetting.isEditingScreen)
                        showEditAlertDialog("Append", getContext().getResources().getString(
                                R.string.alert_for_edit_option_append));
                    Globals.e_editMode = Enums.EIDTMODE.APPEND;
                    setEditMode();
                    playbackUtil.seekToEndPosition();
                }
                break;
            case R.id.screen_record_insert_RBTN:
                if (btnInsert.isChecked()) {
                    if (!Globals.mSetting.isEditingScreen)
                        showEditAlertDialog("Insert", getContext().getResources().getString(
                                R.string.alert_for_edit_option_insert));
                    Globals.e_editMode = Enums.EIDTMODE.INSERT;
                    setEditMode();
                    playbackUtil.seekToFirstPosition();
                    playbackUtil.playFile();
                }
                break;
            case R.id.screen_record_overwrite_RBTN:
                if (btnOverwrite.isChecked()) {
                    if (!Globals.mSetting.isEditingScreen)
                        showEditAlertDialog("Overwrite", getContext().getResources().getString(
                                R.string.alert_for_edit_option_overwrite));
                    Globals.e_editMode = Enums.EIDTMODE.OVERWRITE;
                    setEditMode();
                    playbackUtil.seekToFirstPosition();
                    playbackUtil.playFile();
                }
                break;
            case R.id.screen_record_partial_delete_RBTN:
                if (btnParitalDelete.isChecked()) {
                    if (!Globals.mSetting.isEditingScreen)
                        showEditAlertDialog("Partial Delete", getContext().getResources().getString(
                                R.string.alert_for_edit_option_partial_del));
                    Globals.e_editMode = Enums.EIDTMODE.PARTIALDELETE;
                    playbackUtil.setProgress(0);
                    setEditMode();
                    playbackUtil.playFile();
                }
                break;
        }
    }
}
