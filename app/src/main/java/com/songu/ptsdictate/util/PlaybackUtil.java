package com.songu.ptsdictate.util;

import android.Manifest;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.songu.ptsdictate.R;
import com.songu.ptsdictate.activity.MainActivity;
import com.songu.ptsdictate.doc.Enums;
import com.songu.ptsdictate.doc.Globals;
import com.songu.ptsdictate.fragment.LibraryFragment;
import com.songu.ptsdictate.fragment.RecordFragment;
import com.songu.ptsdictate.view.WaveformSeekBar;

import java.io.IOException;
import java.util.List;

/**
 * Created by Administrator on 3/27/2018.
 */

public class PlaybackUtil implements View.OnClickListener {


    private RecordFragment mParent;
    private Context mContext;
    private MediaPlayer player = null;
    public String mFilePath;
    private ImageButton btnControlPrev, btnControlBack, btnControlPlay, btnControlForward, btnControlNext;
    public TextView txtTotalDuration,txtCurrentTime;
    private WaveformSeekBar waveformSeekBar;
    private boolean isMovingBar = false;
    private int mPausePosition;
    private int duration;
    private boolean isControlOn = false;
    private List<String> bookMarks;
    private BookMarkUtil bookMarkUtil = null;

    private Handler mHandler = new Handler()
    {

        @Override
        public void handleMessage(Message inputMessage) {
            if (inputMessage.what == 0) {
                //Update Seekbar
                int curPos = player.getCurrentPosition();
                txtCurrentTime.setText(Utils.getTimeString(curPos));
                if (waveformSeekBar != null)
                    waveformSeekBar.setProgress(curPos);
                long bookMarkIndex = isBookMark(curPos);
                if (bookMarkIndex != -1)
                {
                    pauseFile();
                    if (bookMarkUtil != null)
                    {
                        bookMarkUtil.selectBookMark(bookMarkIndex);
                    }
                    mHandler.sendEmptyMessageDelayed(1,1000);
                    return;
                }
                if (player.isPlaying()) {
                    mHandler.sendEmptyMessageDelayed(0, 1000);
                }
            }
            else if (inputMessage.what == 1)
            {
                playFile();
            }
        }
    };
    public void setBookMarkUtil(BookMarkUtil util)
    {
        this.bookMarkUtil = util;
    }
    public long isBookMark(long pos)
    {
        if (this.bookMarks == null || !Globals.mSetting.isIndexing) return -1;
        for (int i = 0;i < bookMarks.size();i++)
        {
            if (Utils.getTimeString(pos).equals(Utils.getTimeString(Long.parseLong(bookMarks.get(i)))))
            {
                return Long.parseLong(bookMarks.get(i));
            }
        }
        return -1;
    }
    public PlaybackUtil(Context context)
    {
        this.mContext = context;
    }

    public void setBookMarks(List<String> bm)
    {
        this.bookMarks = bm;
    }
    public void setControls(ImageButton btnPrev,ImageButton btnBack,ImageButton btnPlay,ImageButton btnForward,ImageButton btnNext,TextView txtDuration,TextView time)
    {
        this.btnControlPlay = btnPlay;
        this.btnControlNext = btnNext;
        this.btnControlBack = btnBack;
        this.btnControlPrev = btnPrev;
        this.btnControlForward = btnForward;
        this.txtCurrentTime = time;
        this.txtTotalDuration = txtDuration;

        btnControlPlay.setOnClickListener(this);
        btnControlNext.setOnClickListener(this);
        btnControlForward.setOnClickListener(this);
        btnControlBack.setOnClickListener(this);
        btnControlPrev.setOnClickListener(this);

        initMediaPlayer();
    }
    public PlaybackUtil(Context context,View mRootView)
    {
        this.mContext = context;
        btnControlPrev = (ImageButton) mRootView.findViewById(R.id.screen_record_double_rew);
        btnControlBack = (ImageButton) mRootView.findViewById(R.id.screen_record_rew);
        btnControlPlay = (ImageButton) mRootView.findViewById(R.id.screen_record_play_IMGBTN);
        btnControlForward = (ImageButton) mRootView.findViewById(R.id.screen_record_fwd);
        btnControlNext = (ImageButton) mRootView.findViewById(R.id.screen_record_double_fwd);
        txtTotalDuration = (TextView) mRootView.findViewById(R.id.screen_record_mediaplayer_total_duration);
        txtCurrentTime = (TextView) mRootView.findViewById(R.id.screen_record_mediaplayer_currentposition);

        btnControlPlay.setOnClickListener(this);
        btnControlNext.setOnClickListener(this);
        btnControlForward.setOnClickListener(this);
        btnControlBack.setOnClickListener(this);
        btnControlPrev.setOnClickListener(this);

        initMediaPlayer();
    }

    public void setEnableControl(boolean enabled)
    {
        isControlOn = enabled;
        btnControlPrev.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.icn_fast_rewind_disable));
        btnControlBack.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.icn_backward_disable));
        btnControlForward.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.icn_forward_disable));
        btnControlNext.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.icn_fast_forward_disable));
        this.btnControlPlay.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.icn_media_play_disable));

        if (enabled)
        {
            this.btnControlPrev.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.icn_fast_rewind));
            this.btnControlBack.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.icn_backward));
            btnControlForward.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.icn_forward));
            btnControlNext.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.icn_fast_forward));
            this.btnControlPlay.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.icn_media_play));
        }
        this.btnControlPrev.setEnabled(enabled);
        this.btnControlBack.setEnabled(enabled);
        this.btnControlForward.setEnabled(enabled);
        this.btnControlNext.setEnabled(enabled);
        this.btnControlPlay.setEnabled(enabled);

        if (enabled)
            setEnableDisableSeekButton();
    }

    public void setEnableDisableSeekButton()
    {
        if (waveformSeekBar == null)
            return;
        int i = waveformSeekBar.getProgress();
        if (i == getDuration())
        {
            btnControlForward.setEnabled(false);
            btnControlNext.setEnabled(false);
            btnControlForward.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.icn_forward_disable));
            btnControlNext.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.icn_fast_forward_disable));
        }
        else
        {
            btnControlForward.setEnabled(true);
            btnControlNext.setEnabled(true);
            btnControlForward.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.icn_forward));
            btnControlNext.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.icn_fast_forward));
        }
        if (i == 0)
        {
            btnControlBack.setEnabled(false);
            btnControlPrev.setEnabled(false);

            btnControlPrev.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.icn_fast_rewind_disable));
            btnControlBack.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.icn_backward_disable));
        }
        else
        {
            btnControlBack.setEnabled(true);
            btnControlPrev.setEnabled(true);

            btnControlPrev.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.icn_fast_rewind));
            btnControlBack.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.icn_backward));
        }
    }
    public void setProgress(int position)
    {
        player.seekTo(position);
        waveformSeekBar.setProgress(position);
        txtCurrentTime.setText(Utils.getTimeString(position));
        if (isControlOn)
            setEnableDisableSeekButton();
    }
    public void seekToEndPosition()
    {
        player.seekTo(player.getDuration());
        setCurrentPos();
    }
    public void seekToFirstPosition()
    {
        player.seekTo(0);
        setCurrentPos();
    }
    public void setRecordFile(String filePath,WaveformSeekBar seekBar)
    {
        Log.e("RecordFile Path",filePath);
        this.mFilePath = filePath;
        waveformSeekBar = seekBar;
        try {
            player.reset();
            player.setDataSource(this.mFilePath);
            player.prepareAsync();
        } catch (IllegalArgumentException e1) {
            e1.printStackTrace();
        } catch (SecurityException e1) {
            e1.printStackTrace();
        } catch (IllegalStateException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        this.waveformSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (isMovingBar) {
                    player.seekTo(i);
                    txtCurrentTime.setText(Utils.getTimeString(i));
                } else {
                    mPausePosition = i;
                }
                if (isControlOn)
                    setEnableDisableSeekButton();
                if (bookMarkUtil != null)
                {
                    bookMarkUtil.selectBookMark(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isMovingBar = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isMovingBar = false;
                mPausePosition = seekBar.getProgress();
                player.seekTo(mPausePosition);
                waveformSeekBar.setProgress(mPausePosition);
            }
        });
    }
    public void stopLibraryItem()
    {
        if (((MainActivity)mContext).currentFragment instanceof LibraryFragment)
        {
            ((LibraryFragment)((MainActivity)mContext).currentFragment).stopPlayItem();
        }
    }
    public void initMediaPlayer()
    {
        player = new MediaPlayer();
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                int curPos = player.getCurrentPosition();
                txtCurrentTime.setText(Utils.getTimeString(getDuration()));
                if (waveformSeekBar != null && curPos != 0)
                    waveformSeekBar.setProgress(player.getDuration());

                if (isControlOn)
                    btnControlPlay.setBackground(mContext.getResources().getDrawable(R.drawable.icn_media_play));
                else
                    btnControlPlay.setBackground(mContext.getResources().getDrawable(R.drawable.icn_media_play_disable));
                mHandler.removeMessages(0);
                mHandler.removeMessages(1);
                stopLibraryItem();
                if (curPos == 0)
                    playFile();
            }
        });
        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                Log.e("Prepared File",String.valueOf(mediaPlayer.getDuration()));
                txtTotalDuration.setText(Utils.getTimeString(player.getDuration()));
                duration = player.getDuration();
                waveformSeekBar.setMax(getDuration());
                if (waveformSeekBar.getProgress() == getDuration())
                    txtCurrentTime.setText(Utils.getTimeString(getDuration()));
                if (isControlOn)
                    setEnableDisableSeekButton();

            }
        });
    }
    public boolean isPlaying()
    {
        return player.isPlaying();
    }
    public void playLibraryItem()
    {
        if (((MainActivity)mContext).currentFragment instanceof LibraryFragment)
        {
            ((LibraryFragment)((MainActivity)mContext).currentFragment).playItem();
        }
    }
    public void playFile()
    {
        if (player.isPlaying())
        {
            pauseFile();
        }
        else {

            if (((MainActivity)mContext).currentFragment instanceof RecordFragment)
            {
                ((RecordFragment)((MainActivity)mContext).currentFragment).setBookMarkIndexButtonStatus(true);
            }

            playLibraryItem();
            //setRecordFile(mFilePath,waveformSeekBar);
            if (waveformSeekBar.getProgress() == player.getDuration())
                player.seekTo(0);
            else player.seekTo(waveformSeekBar.getProgress());
            player.start();
            btnControlPlay.setBackground(mContext.getResources().getDrawable(R.drawable.icn_pause_media));
            mHandler.sendEmptyMessageDelayed(0,1000);
        }
    }
    public void pauseFile()
    {
        if (((MainActivity)mContext).currentFragment instanceof RecordFragment)
        {
            ((RecordFragment)((MainActivity)mContext).currentFragment).setBookMarkIndexButtonStatus(false);
        }
        if (player != null) {
            stopLibraryItem();
            if (player.isPlaying())
                player.pause();
            if (isControlOn)
                btnControlPlay.setBackground(mContext.getResources().getDrawable(R.drawable.icn_media_play));
            else
                btnControlPlay.setBackground(mContext.getResources().getDrawable(R.drawable.icn_media_play_disable));
            mHandler.removeMessages(0);
        }
    }
    public int getCurrentPosition()
    {
        if (player == null)
            return 0;
        return player.getCurrentPosition();
    }
    public void rewindFile()
    {
        int curPos = player.getCurrentPosition();
        if (curPos - 1000 > 0) {
            player.seekTo(curPos - 1000);
            setCurrentPos();
        }
        else {
            player.seekTo(0);
            setCurrentPos();
        }
    }
    public void setCurrentPos()
    {
        waveformSeekBar.setProgress(player.getCurrentPosition());
        txtCurrentTime.setText(Utils.getTimeString(player.getCurrentPosition()));
    }
    public void setCurrentPos(int pos)
    {
        waveformSeekBar.setProgress(pos);
        txtCurrentTime.setText(Utils.getTimeString(pos));
    }
    public void doubleRewindFile()
    {
        int curPos = player.getCurrentPosition();
        if (curPos - 3000 > 0) {
            player.seekTo(curPos - 3000);
            setCurrentPos();
        }
        else {
            player.seekTo(0);
            setCurrentPos();
        }
    }
    public void forwardFile()
    {
        int curPos = player.getCurrentPosition();
        int duration = player.getDuration();

        if (curPos + 1000 > duration) {
            player.seekTo(duration);
            setCurrentPos(duration);
        }
        else {
            player.seekTo(curPos + 1000);
            setCurrentPos(curPos + 1000);
        }
    }
    public void doubleForwardFile()
    {
        int curPos = player.getCurrentPosition();
        int duration = player.getDuration();

        if (curPos + 3000 > duration) {
            player.seekTo(duration);
            setCurrentPos(duration);
        }
        else {
            player.seekTo(curPos + 3000);
            setCurrentPos(curPos + 3000);
        }
    }
    public boolean checkStoragePermission()
    {
        return MainActivity.hasPermissions(mContext,  Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);
    }
    public int getDuration()
    {
        return duration;
    }
    @Override
    public void onClick(View view) {
        if (!checkStoragePermission()) return;
        if (view.equals(btnControlPlay))
        {
            if (!checkStoragePermission()) return;
            if (player.isPlaying()) {
                pauseFile();
            }
            else {

                playFile();
            }
        }
        if (view.equals(btnControlPrev))
        {
            doubleRewindFile();
        }
        if (view.equals(btnControlBack))
        {
            rewindFile();
        }
        if (view.equals(btnControlForward))
        {
            forwardFile();
        }
        if (view.equals(btnControlNext))
        {
            doubleForwardFile();
        }
    }
}
