package com.songu.ptsdictate.view.WavRecorder;

/**
 * Created by bedrocket on 2/19/15.
 */
public interface VULedListener {


    /**
     * processAudioFrame gets called periodically, e.g. every 20ms with PCM
     * audio samples.
     *
     * @param audioFrame this is an array of samples, e.g. if the sampling rate
     *                   is 8000 samples per second, then the array should contain 160 16 bit
     *                   samples.
     */
    public void processAudioFrame(short[] audioFrame, int a, byte data[]);

    public void detectVoiceLevel(int aVoiceLevel);

    // public void getAmplitude(int aValue);

}


