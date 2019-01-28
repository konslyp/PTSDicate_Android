package com.songu.ptsdictate.util;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.songu.ptsdictate.doc.Enums;
import com.songu.ptsdictate.doc.Globals;
import com.songu.ptsdictate.fragment.RecordFragment;
import com.songu.ptsdictate.model.RecordModel;
import com.songu.ptsdictate.view.TimerTextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 3/23/2018.
 */

public class RecordUtil implements Runnable{


    private int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private String AUDIO_RECORDER_FOLDER = "PTSRecord";
    private String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";
    private String AUDIO_RECORDER_AUTOSAVE_FILE = "autosave.raw";
    private String AUDIO_SPLIT_HEADER_FILE = "_split_1.tmp";
    private String AUDIO_SPLIT_FOOTER_FILE = "_split_2.tmp";
    private String mAutoSavePath;
    private byte RECORDER_BPP = 16;
    private int SAMPLE_RATE = 44100;

    private Thread recordingThread;
    private String mCurrentFileName;
    public String mCurrentPath;
    private AudioRecord recorder = null;
    private int bufferSize = 0;
    public boolean isRecording;
    private long startTime = 0L;
    public long insertStartTime = 0L;
    private long timeInMillies = 0L;
    public long timeSwap = 0L;
    public long finalTime = 0L;
    private int indexTemp = 0;
    public long editTimeElapse = 0;
    public ArrayList<String> lstTempFiles;
    private long fileSize = 0;
    private RecordFragment recordFragment;
    private String endFilePath = null;
    private int durationOverwrite = 0;
//    private long LIMITFILESIZE = 83886080;
//    private long WARNINGSIZE = 78643200;
    private String WARNINGSIZELABEL = "75 MB";
    private String LIMITGSIZELABEL = "80 MB";

//    private String WARNINGSIZELABEL = "5 MB";
//    private String LIMITGSIZELABEL = "10 MB";

    private double SILENTVALUE = 0.25;
    private int SILENTTIMES = 30000;
    private long LIMITFILESIZE = 83886080;
    private long WARNINGSIZE = 4643200;
    //private long LIMITFILESIZE = 5242880;
    private int AMPTRESH = 500;
    public int cutPoint = 0;
    private long silentTimeStart = 0;
    private long originalAppendDuration = 0;
    private boolean isWarn = true;
    private boolean isPause = false;
    private boolean isAutoPause = false;
    private long baseAmp = 0;
    private double oldAmp = 0;
    private double newAmp = 0;
    public long overwriteOldFileSize;

    double mGain = 2500.0 / Math.pow(10.0, 90.0 / 20.0);


    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message inputMessage) {
            if (inputMessage.what == 0)
            {
                timeInMillies = SystemClock.uptimeMillis() - startTime;
                editTimeElapse = SystemClock.uptimeMillis() - startTime + insertStartTime + cutPoint;
//                if (Globals.e_editMode == Enums.EIDTMODE.OVERWRITE && editTimeElapse >= durationOverwrite + cutPoint)
//                {
//                    recordFragment.stopRecording();
//                    return;
//                }
                if (Globals.e_editMode != Enums.EIDTMODE.OVERWRITE) {
                    finalTime = timeSwap + timeInMillies;
                }
                mHandler.sendEmptyMessageDelayed(0,500);
            }
            if (inputMessage.what == 1)
            {
                recordFragment.autoSaveRecording();
                return;
            }
            if (inputMessage.what == 2)
            {
                recordFragment.showWarningMesasge();
            }
            if (inputMessage.what == 3)
            {
                recordFragment.stopRecording();
                return;
            }

        }
    };


    public void setFinalTime(long finalTime)
    {
        this.finalTime = finalTime;
    }
    public RecordUtil()
    {

        if (Globals.mSetting.mAudioQuality == 0) SAMPLE_RATE = 8000;
        else if (Globals.mSetting.mAudioQuality == 1) SAMPLE_RATE = 11025;
        else if (Globals.mSetting.mAudioQuality == 2) SAMPLE_RATE = 22050;
        lstTempFiles = new ArrayList<String>();
        bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_8BIT);
        this.mAutoSavePath = Environment.getExternalStorageDirectory().getPath() + "/PTSRecord/" + this.AUDIO_RECORDER_AUTOSAVE_FILE;
        isWarn = true;
    }
    public void setSampleRate(int rate)
    {
        SAMPLE_RATE = rate;
        bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_8BIT);
    }
    public void initRecord()
    {
        if (Globals.mSetting.mAudioQuality == 0) SAMPLE_RATE = 8000;
        else if (Globals.mSetting.mAudioQuality == 1) SAMPLE_RATE = 11025;
        else if (Globals.mSetting.mAudioQuality == 2) SAMPLE_RATE = 22050;
        lstTempFiles = new ArrayList<String>();
        bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_8BIT);
        this.mGain *= Math.pow(10, 2 / 20.0);
        isWarn = true;
    }
    public void setAttachViews(RecordFragment record)
    {
        this.recordFragment = record;
    }
    public String getAutoSaveFile()
    {
        return mAutoSavePath;
    }
    public String getCurrentFilePath()
    {
        return mCurrentPath;
    }
    public String getCurrentFileName()
    {
        return mCurrentFileName;
    }
    public int getSampleRate()
    {
        return SAMPLE_RATE;
    }
    public void backupAutosaveFiles(String fileName)
    {
        //Clean tmp files
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File directoryPath = new File(filepath,AUDIO_RECORDER_FOLDER);
        String[] fileList = directoryPath.list();
        lstTempFiles.clear();
        if (fileList != null) {
            for (int i = 0; i < fileList.length; i++) {
                if (fileList[i].endsWith("_split_1.tmp")) {
                    String header = Environment.getExternalStorageDirectory().getPath() + "/PTSRecord/" + fileList[i];
                    lstTempFiles.add(header);
                    break;
                }
            }

            int tempNumber = 0;
            while (tempNumber < fileList.length) {
                int flag = 0;
                for (int k = 0; k < fileList.length; k++) {
                    if (!fileList[k].startsWith("record_temp")) continue;
                    if (fileList[k].endsWith(".raw" + String.valueOf(tempNumber))) {
                        String tempFile = Environment.getExternalStorageDirectory().getPath() + "/PTSRecord/" + fileList[k];
                        lstTempFiles.add(tempFile);
                        tempNumber++;
                        flag = 1;
                        break;
                    }
                }
                if (flag == 0) break;
            }
            for (int i = 0; i < fileList.length; i++) {
                if (fileList[i].endsWith("_split_2.tmp")) {
                    String header = Environment.getExternalStorageDirectory().getPath() + "/PTSRecord/" + fileList[i];
                    lstTempFiles.add(header);
                }
            }
        }
        if (lstTempFiles.size() > 0) {
            copyWaveFile(fileName);
        }
        else
        {
            File m = new File(fileName);
            this.fileSize = m.length();
        }
        deleteTempFile();
        deleteAutoSaveFile();
    }
    public String getNewFilename(String mUser,String dateFormat,int subNumber)
    {

        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath,AUDIO_RECORDER_FOLDER);
        if(!file.exists()){
            file.mkdirs();
        }

        SimpleDateFormat curFormater = new SimpleDateFormat(dateFormat);
        Date dateObj = new Date();
        String newDateStr = curFormater.format(dateObj);

        String subFix = String.format("%03d",subNumber);
        String fileName = mUser + "_" + newDateStr + "_File_" + subFix + ".wav";
        mCurrentFileName = fileName;
        this.mCurrentPath = file.getAbsolutePath() + "/" + mCurrentFileName;
        this.mAutoSavePath = file.getAbsolutePath() + "/" + this.AUDIO_RECORDER_AUTOSAVE_FILE;
        return fileName;
    }
    public void backupOriginalFile(String originalPath)
    {
        File originalFile = new File(originalPath);
        File backupFile = new File(originalPath + "_temp");
        copy(originalFile,backupFile);
    }
    public void restoreFile(String originalPath)
    {
        File originalFile = new File(originalPath);
        File backupFile = new File(originalPath + "_temp");
        copy(backupFile,originalFile);
        backupFile.delete();
    }

    public void copy(File src, File dst) {
        InputStream in = null;
        try {
            in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dst);
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            out.close();
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void appendRecording()
    {
        cutPoint = 0;
        lstTempFiles.clear();
        if (Utils.readableFileSize(fileSize).trim().equals(LIMITGSIZELABEL) || fileSize > LIMITFILESIZE)
        {
            mHandler.sendEmptyMessageDelayed(1, 500);
            return;
        }
        splitTempFileFromExisting(finalTime,finalTime,this.mCurrentPath);
        File fFile = new File(lstTempFiles.get(0));
        fileSize = fFile.length();
        cutPoint = (int) finalTime;
        originalAppendDuration = finalTime;
        startEditing();
    }
    public void appendRecordingBeforeEnd(int markPoint)
    {
        originalAppendDuration = finalTime;
        cutPoint = markPoint;
        lstTempFiles.clear();
        splitTempFileFromExisting(markPoint,finalTime,this.mCurrentPath);
        if (endFilePath != null)
        {
            File endFile = new File(endFilePath);
            endFile.delete();
            endFilePath = null;
        }
        File fFile = new File(lstTempFiles.get(0));
        fileSize = fFile.length();
        startEditing();
    }
    public void insertRecording(int markPoint)
    {
        cutPoint = markPoint;
        lstTempFiles.clear();
        splitTempFileFromExisting(markPoint,finalTime,this.mCurrentPath);
        File fFile = new File(this.mCurrentPath);
        fileSize = fFile.length();
        startEditing();
    }

    public void overwriteRecording(int startPoint,int endPoint)
    {
        cutPoint = startPoint;
        lstTempFiles.clear();
        if (this.mCurrentPath != null)
        {
            File fOFile = new File(this.mCurrentPath);
            overwriteOldFileSize = fOFile.length();
        }
        splitTempFileFromExisting(startPoint,finalTime,this.mCurrentPath);
        splitTempFileFromExisting(endPoint - startPoint,finalTime - startPoint,this.endFilePath);
        durationOverwrite = endPoint - startPoint;
        String fileName = lstTempFiles.get(lstTempFiles.size() - 1);

        File fFile = new File(lstTempFiles.get(0));
        fileSize = fFile.length();

        if (endFilePath != null)
        {
            File fEndFile = new File(endFilePath);
            fileSize += fEndFile.length();
        }
        File removeFile = new File(fileName);
        removeFile.delete();
        lstTempFiles.remove(lstTempFiles.size() - 1);
        startEditing();
    }
    public void partialEraseRecording(int startPoint, int endPoint)
    {
        int startSec = startPoint / 1000;
        if (startSec < 1)
            startSec = 1;
        int endSec = endPoint / 1000;
        lstTempFiles.clear();
        splitTempFileFromExisting(startSec * 1000,finalTime,this.mCurrentPath);
        splitTempFileFromExisting(endSec * 1000 - startSec * 1000,finalTime - startSec * 1000,this.endFilePath);
        durationOverwrite = endSec * 1000 - startSec * 1000;
        lstTempFiles.remove(lstTempFiles.size() - 1);
        finalTime -= durationOverwrite;
    }
    public void startEditing()
    {
        insertStartTime = 0;
        if (Globals.e_editMode == Enums.EIDTMODE.APPEND)
            timeSwap = cutPoint;
        else timeSwap = finalTime;
        indexTemp = 0;
        timeInMillies = 0;
        editTimeElapse = 0;
        isPause = false;
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,SAMPLE_RATE,RECORDER_CHANNELS,RECORDER_AUDIO_ENCODING, bufferSize);
        int i = recorder.getState();
        lstTempFiles.add(this.getTempFilename(indexTemp));
        if(recorder.getState() == AudioRecord.STATE_INITIALIZED)
            recorder.startRecording();
        isRecording = true;
        recordingThread = new Thread(this,"AudioRecorder Thread");
        recordingThread.start();
    }
    public void splitTempFileFromExisting(long cutPointSeconds,long duration,String filePath)
    {
        long time= System.currentTimeMillis();

        String headerFilePath = Environment.getExternalStorageDirectory().getPath() + "/PTSRecord/" + String.valueOf(time) + AUDIO_SPLIT_HEADER_FILE;
        String footerFilePath = Environment.getExternalStorageDirectory().getPath() + "/PTSRecord/" + String.valueOf(time) + AUDIO_SPLIT_FOOTER_FILE;

        File file = new File(filePath);
        long size = file.length();
        int bytesPerSecond = (int) ((size - 44) / duration);
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(headerFilePath);
            in = new FileInputStream(filePath);
            writeBlock(44 + bytesPerSecond * cutPointSeconds,in,out);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        lstTempFiles.add(headerFilePath);
        if (cutPointSeconds == duration)
        {
            try {
                this.endFilePath = null;
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        try {
            out = new FileOutputStream(footerFilePath);
            writeBlock(size - (bytesPerSecond * cutPointSeconds + 44),in,out);
            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.endFilePath = footerFilePath;
    }
    public void writeBlock(long totalSize,FileInputStream input,FileOutputStream out)
    {
        long tempTotalSize = totalSize;
        if (tempTotalSize > 30000)
        {
            byte [] data = new byte[30000];
            while(tempTotalSize < 30000)
            {
                try {
                    input.read(data);
                    out.write(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                tempTotalSize -= 30000;
            }
        }
        byte[] data = new byte [(int) tempTotalSize];
        try {
            input.read(data);
            out.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void startRecording()
    {
        timeSwap = 0;
        fileSize = 0;
        indexTemp = 0;
        timeInMillies = 0;
        editTimeElapse = 0;
        finalTime = 0;
        isPause = false;
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,SAMPLE_RATE,RECORDER_CHANNELS,RECORDER_AUDIO_ENCODING, bufferSize);
        lstTempFiles.add(this.getTempFilename(indexTemp));
        int i = recorder.getState();
        if(recorder.getState() == AudioRecord.STATE_INITIALIZED)
            recorder.startRecording();
        isRecording = true;
        recordingThread = new Thread(this,"AudioRecorder Thread");
        recordingThread.start();
    }

    @Override
    public void run() {
        startTime = SystemClock.uptimeMillis();
        mHandler.sendEmptyMessageDelayed(0,500);
        writeAudioDataToFile();
    }

    public void stopRecording(){
        //lstTempFiles.clear();
        isWarn = true;
        timeSwap += timeInMillies;
        isAutoPause = false;
        mHandler.removeMessages(0);

        if(recorder != null){
            isRecording = false;
            if(recorder.getState() == AudioRecord.STATE_INITIALIZED)
                recorder.stop();
            recorder.release();
            recorder = null;
            recordingThread = null;
        }
        if (endFilePath != null)
            lstTempFiles.add(endFilePath);
        if (lstTempFiles.size() > 0) {
            copyWaveFile(mCurrentPath);
        }
        deleteTempFile();
        deleteAutoSaveFile();
    }
    public void setFileSize(long size)
    {
        this.fileSize = size;
    }
    public long getFileSize()
    {
        return this.fileSize;
    }
    public long getElapsedTime()
    {
        return finalTime;
    }
    public void pauseRecording(){
        //lstTempFiles.clear();
        //lstTempFiles.add(this.getTempFilename(indexTemp));
        //indexTemp++;
        isPause = true;
        timeSwap += timeInMillies;
        insertStartTime += timeInMillies;
        timeInMillies = 0;
        mHandler.removeMessages(0);

//        if(null != recorder){
//            isRecording = false;
//            if(recorder.getState() == AudioRecord.STATE_INITIALIZED)
//                recorder.stop();
//            recorder.release();
//            recorder = null;
//            recordingThread = null;
//        }
        deleteAutoSaveFile();
        if (endFilePath != null)
            this.lstTempFiles.add(endFilePath);
        copyWaveFile(this.mAutoSavePath);
        this.lstTempFiles.remove(endFilePath);
    }
    private void deleteAutoSaveFile()
    {
        if (this.mAutoSavePath != null) {
            File file = new File(this.mAutoSavePath);
            file.delete();
        }
    }
    private void deleteTempFile() {
        for (int kk = 0;kk < lstTempFiles.size();kk++){
            File file = new File(lstTempFiles.get(kk));
            file.delete();
        }

        //Clean tmp files
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File directoryPath = new File(filepath,AUDIO_RECORDER_FOLDER);

        String[] fileList = directoryPath.list();
        if (fileList != null) {
            for (int i = 0; i < fileList.length; i++) {
                if (fileList[i].endsWith(".tmp")) {
                    File tempFile = new File(Environment.getExternalStorageDirectory().getPath() + "/" + AUDIO_RECORDER_FOLDER + "/" + fileList[i]);
                    tempFile.delete();
                }
            }
        }
        endFilePath = null;
        lstTempFiles.clear();
    }
    public void resumeRecording(){

//        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
//                SAMPLE_RATE, RECORDER_CHANNELS,RECORDER_AUDIO_ENCODING, bufferSize);
//        int i = recorder.getState();
//        if(i == 1)
//            recorder.startRecording();
//        isRecording = true;
//        recordingThread = new Thread(this);
//        recordingThread.start();
        isPause = false;
        startTime = SystemClock.uptimeMillis();
        mHandler.sendEmptyMessageDelayed(0,500);
    }
    private String getTempFilename(int i){
        String ind = String.valueOf(i);
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath,AUDIO_RECORDER_FOLDER);
        if(!file.exists()){
            file.mkdirs();
        }
        return (file.getAbsolutePath() + "/" + AUDIO_RECORDER_TEMP_FILE + ind);
    }

    private void writeAudioDataToFile(){
        silentTimeStart = 0;
        double mAlpha = 0.9;
        double mRmsSmoothed = 0;
        byte data[] = new byte[bufferSize];
        String filename = getTempFilename(indexTemp);
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        int read = 0;
        if(os != null){
            while(isRecording){
                read = recorder.read(data, 0, bufferSize);
                if(AudioRecord.ERROR_INVALID_OPERATION != read){
                    try {
                        short [] buffer20ms = new short[data.length / 2];
                        ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(buffer20ms);

                        double rms = 0;
                        for (int i = 0; i < buffer20ms.length; i++) {
                            rms += buffer20ms[i] * buffer20ms[i];
                        }
                        rms = Math.sqrt(rms / buffer20ms.length);
                        // Compute a smoothed version for less flickering of the display.
                        mRmsSmoothed = mRmsSmoothed * mAlpha + (1 - mAlpha) * rms;
                        final double rmsdB = 20.0 * Math.log10(mGain * mRmsSmoothed);
                        double amplitude = 0;
                        if (Double.isInfinite(rmsdB)) {

                        } else {
                            //amplitude = (rmsdB + 30) / 110.0f;
                            amplitude = (rmsdB) / 110.0f;
                        }
                        Log.e("RMSDB",String.valueOf(amplitude));
                        amplitude = amplitude - SILENTVALUE;
                        if (amplitude < 0)
                            amplitude = 0;
                        oldAmp = amplitude;
                        //Show Status
                        if (amplitude > 0 || silentTimeStart == 0)
                        {
                            silentTimeStart = SystemClock.uptimeMillis();
                            if (isPause && Globals.mSetting.isVoiceAutoPause && isAutoPause)
                            {
                                recordFragment.getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        isAutoPause = false;
                                        silentTimeStart = 0;
                                        recordFragment.startOrResumeRecord();
                                    }
                                });
                            }
                        }
                        else
                        {
                            //Log.e("Amplitude",String.valueOf(SystemClock.uptimeMillis() - silentTimeStart));
                            if (silentTimeStart != 0 && SystemClock.uptimeMillis() - silentTimeStart > SILENTTIMES && Globals.mSetting.isVoiceAutoPause && !isAutoPause)
                            {
                                recordFragment.getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //silentTimeStart = 0;
                                        isAutoPause = true;
                                        recordFragment.startOrResumeRecord();
                                    }
                                });
                            }
                        }
                        if (!isPause) {

                            os.write(data);
                            fileSize += data.length;

                            if (Globals.e_editMode == Enums.EIDTMODE.APPEND && editTimeElapse < originalAppendDuration) {
                                recordFragment.setUpdateRecordStatus(originalAppendDuration, Utils.readableFileSize(fileSize), (float) oldAmp, editTimeElapse);
                            }
                            else {
                                if (Globals.e_editMode == Enums.EIDTMODE.APPEND) {
                                    if (recordFragment.timerEditElapse.getVisibility() == View.VISIBLE) {
                                        recordFragment.getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                recordFragment.timerEditElapse.setVisibility(View.GONE);
                                            }
                                        });
                                    }
                                }//
                                recordFragment.setUpdateRecordStatus(finalTime, Utils.readableFileSize(fileSize + 44), (float) oldAmp, editTimeElapse);
                            }
                            if (Utils.readableFileSize(fileSize).trim().equals(Utils.readableFileSize(overwriteOldFileSize).trim()) && Globals.e_editMode == Enums.EIDTMODE.OVERWRITE)
                            {
                                mHandler.sendEmptyMessageDelayed(3, 500);
                                return;
                            }
                            if (Utils.readableFileSize(fileSize).trim().equals(WARNINGSIZELABEL) && isWarn)
                            {
                                isWarn = false;
                                mHandler.sendEmptyMessageDelayed(2, 0);
                            }
                            if (Utils.readableFileSize(fileSize).trim().equals(LIMITGSIZELABEL))
                            {
                                mHandler.sendEmptyMessageDelayed(1, 500);
                                return;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void copyWaveFile(String outFilename){
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 44;
        long longSampleRate = SAMPLE_RATE;
        int channels = 2;
        long byteRate = RECORDER_BPP * SAMPLE_RATE * channels/8;
        byte[] data = new byte[bufferSize];
        try {
            int len = 0;
            for (int t = 0;t < lstTempFiles.size();t++){
                File file = new File(lstTempFiles.get(t));
                len += file.length();
            }
            out = new FileOutputStream(outFilename);
            totalAudioLen = len;
            totalDataLen = totalAudioLen + 44;
            this.fileSize = totalAudioLen;
            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate);
            for (int t = 0;t < lstTempFiles.size();t++){
                FileInputStream tt = new FileInputStream(lstTempFiles.get(t));
                while(tt.read(data) != -1){
                    out.write(data);
                }
                tt.close();
            }
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void WriteWaveFileHeader(
            FileOutputStream out, long totalAudioLen,
            long totalDataLen, long longSampleRate, int channels,
            long byteRate) throws IOException {

        byte[] header = new byte[44];

        header[0] = 'R';  // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';  // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;  // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8);  // block align
        header[33] = 0;
        header[34] = RECORDER_BPP;  // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        out.write(header, 0, 44);
    }
}
