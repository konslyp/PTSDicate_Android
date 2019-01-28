package com.songu.ptsdictate.view;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.widget.Chronometer;

/**
 * Created by bedrocket on 1/28/15.
 */
public class TimerTextView extends Chronometer {

    public long msElapsed;
    public boolean isRunning = false;

    public TimerTextView(Context context) {
        super(context);
    }

    public TimerTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public long getMsElapsed() {
        return msElapsed;
    }

    public void setMsElapsed(long ms) {

        setBase(getBase() - ms);
        msElapsed = ms;

        // Code to display the time format as 00:00:00

        //long t = SystemClock.elapsedRealtime() - this.getBase();
        long t = ms;
        int h = (int) (t / 3600000);
        int m = (int) (t - h * 3600000) / 60000;
        int s = (int) (t - h * 3600000 - m * 60000) / 1000;
        String hh = h < 10 ? "0" + h : h + "";
        String mm = m < 10 ? "0" + m : m + "";
        String ss = s < 10 ? "0" + s : s + "";
        this.setText(hh + ":" + mm + ":" + ss);

    }

    @Override
    public void start() {
        super.start();
        setBase(SystemClock.elapsedRealtime() - msElapsed);
        // Code to display the time format as 00:00:00
        long t = SystemClock.elapsedRealtime() - this.getBase();
        int h = (int) (t / 3600000);
        int m = (int) (t - h * 3600000) / 60000;
        int s = (int) (t - h * 3600000 - m * 60000) / 1000;
        String hh = h < 10 ? "0" + h : h + "";
        String mm = m < 10 ? "0" + m : m + "";
        String ss = s < 10 ? "0" + s : s + "";
        this.setText(hh + ":" + mm + ":" + ss);
        isRunning = true;
    }

    @Override
    public void stop() {
        super.stop();
        if (isRunning) {
            msElapsed = (int) (SystemClock.elapsedRealtime() - this.getBase());
        }
        isRunning = false;
    }


    @Override
    public void setOnChronometerTickListener(OnChronometerTickListener listener) {
        super.setOnChronometerTickListener(listener);


        // Code to display the time format as 00:00:00

        long t = SystemClock.elapsedRealtime() - this.getBase();
        int h = (int) (t / 3600000);
        int m = (int) (t - h * 3600000) / 60000;
        int s = (int) (t - h * 3600000 - m * 60000) / 1000;
        String hh = h < 10 ? "0" + h : h + "";
        String mm = m < 10 ? "0" + m : m + "";
        String ss = s < 10 ? "0" + s : s + "";
        this.setText(hh + ":" + mm + ":" + ss);


    }
}