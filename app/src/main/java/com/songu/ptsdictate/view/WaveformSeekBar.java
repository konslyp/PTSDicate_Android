package com.songu.ptsdictate.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.SeekBar;

import com.songu.ptsdictate.R;
import com.songu.ptsdictate.util.Utils;
import com.songu.ptsdictate.view.WavRecorder.WavFile;
import com.songu.ptsdictate.view.WavRecorder.WavFileException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

public class WaveformSeekBar extends SeekBar {

    public static final int READ_FRAME_COUNT = 100;

    private InputStream inputStream;
    private Vector<Float> yAxis = new Vector<Float>(0);
    public boolean firstDraw = true;
    private int activeColor;
    private int inactiveColor;
    private int thumbColor;
    private int thumbHideColor;
    private int transparencyAtrb;
    public int myStartPos = 0;
    private boolean hideDraw = false;
    private FragmentActivity myContext;
    public boolean isSetPoint = false;
    public boolean isEndPoint = false;
    public int markPointX;
    public int markEndPointX;

    public WaveformSeekBar(FragmentActivity context) {
        super(context);
        myContext = context;
        activeColor = Color.parseColor("#ff0f3ccc");
        inactiveColor = Color.parseColor("#ff7fadc6");
        thumbColor = Color.parseColor("#fff75527");
        thumbHideColor = Color.parseColor("#90ffffff");
        transparencyAtrb = 60;
        firstDraw = true;
        hideDraw = false;
    }

    public void WaveformSeekBar(Context context, int aVal) {
        hideDraw = true;
    }

    public WaveformSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttributes(context, attrs);

    }

    public WaveformSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initAttributes(context, attrs);
    }

    private void initAttributes(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.WaveformSeekBar, 0, 0);

        try {
            activeColor = a.getInteger(
                    R.styleable.WaveformSeekBar_activeLineColor, 0);
            inactiveColor = a.getInteger(
                    R.styleable.WaveformSeekBar_inactiveLineColor, 0);
            transparencyAtrb = a.getInteger(
                    R.styleable.WaveformSeekBar_inactiveLineColorAlpha, 0);

            if (transparencyAtrb > 100) {
                transparencyAtrb = 255;
            } else {
                transparencyAtrb = (int) ((transparencyAtrb / 100.0) * (255));
            }

        } finally {
            a.recycle();
        }

        if (inactiveColor == 0 && transparencyAtrb == 0) {
            inactiveColor = adjustAlpha(activeColor, 200);
        } else if (inactiveColor == 0 && transparencyAtrb != 0) {
            inactiveColor = adjustAlpha(activeColor, transparencyAtrb);
        } else if (inactiveColor != 0 && transparencyAtrb != 0) {
            inactiveColor = adjustAlpha(inactiveColor, transparencyAtrb);
        } else if (hideDraw) {
            // inactiveColor = adjustAlpha(thumbHideColor, 200);
        }
    }

    public int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    public void setAudio(InputStream intStream) {
        this.inputStream = intStream;
        invalidate();
    }

    private class CalculateYAxisPoints extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void[] aVoid) {
            myContext.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    try {
                        yAxis.clear();
                        readWavFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (WavFileException e) {
                        e.printStackTrace();
                    }


                }
            });

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // getting x-axis of the clicked point
        int clickedX = (int) (((double) getMeasuredWidth() * (double) getProgress()) / (double) getMax());

        if (firstDraw) {
            firstDraw = false;

            new CalculateYAxisPoints().execute();
        } else {

            Paint paintPath = new Paint();

            paintPath.setStyle(Paint.Style.STROKE);

            paintPath.setAntiAlias(true);
            // draw from 0 to clickedX
            drawPath(0, clickedX, activeColor, paintPath, canvas);

            // draw from clickedX to end
            drawPath(clickedX, getMeasuredWidth(), inactiveColor, paintPath,
                    canvas);

            if (hideDraw) {
                // To be hide thumb
            } else {
                // To show thumb
                drawthumb(clickedX, clickedX, thumbColor, paintPath, canvas);
            }

            drawMarkPoint(canvas);
            drawMarkEndPoint(canvas);
        } // end of onDraw
    }
    public void setMarkPointX(int point)
    {
        this.markPointX = point;
        this.isSetPoint = true;
        this.invalidate();
    }
    public void setMarkEndPointX(int point)
    {
        this.markEndPointX = point;
        this.isEndPoint = true;
        this.invalidate();
    }
    public void clearMarkPoint()
    {
        this.isSetPoint = false;
        this.markPointX = 0;

        this.isEndPoint = false;
        this.markEndPointX = 0;

        invalidate();
    }
    public void drawMarkPoint(Canvas canvas)
    {
        if (isSetPoint) {
            float pointX = (float)getMeasuredWidth() / getMax() * markPointX;
            Paint paintPath = new Paint();
            paintPath.setStyle(Paint.Style.STROKE);
            paintPath.setAntiAlias(true);
            paintPath.setColor(Color.BLACK);
            paintPath.setStrokeWidth(5);
            canvas.drawLine(pointX,20,pointX, this.getMeasuredHeight() - 20,paintPath);
            String pointString = Utils.simpleTimeString(markPointX);
            paintPath.setStyle(Paint.Style.FILL);
            paintPath.setTextSize(30);
            paintPath.setStrokeWidth(1);
            if (pointX < 25)
                canvas.drawText(pointString,0,this.getMeasuredHeight()- 5,paintPath);
            else
                canvas.drawText(pointString,pointX - 25,this.getMeasuredHeight()- 5,paintPath);

        }
    }
    public void drawMarkEndPoint(Canvas canvas)
    {
        if (isEndPoint) {
            float pointX = (float)getMeasuredWidth() / getMax() * markEndPointX;
            Paint paintPath = new Paint();
            paintPath.setStyle(Paint.Style.STROKE);
            paintPath.setAntiAlias(true);
            paintPath.setColor(Color.BLACK);
            paintPath.setStrokeWidth(5);
            canvas.drawLine(pointX,20,pointX, this.getMeasuredHeight() - 20,paintPath);
            String pointString = Utils.simpleTimeString(markEndPointX);
            paintPath.setStyle(Paint.Style.FILL);
            paintPath.setTextSize(30);
            paintPath.setStrokeWidth(1);
            if (pointX < 25)
                canvas.drawText(pointString,0,this.getMeasuredHeight() - 5,paintPath);
            else
                canvas.drawText(pointString,pointX - 25,this.getMeasuredHeight() - 5,paintPath);

        }
    }
    /**
     * Draw a path from start to end using yAxis array.
     *
     * @param start     starting point of the path
     * @param end       end point of the path
     * @param color     color of the path
     * @param paintPath paint which you want to draw the path with
     * @param canvas    canvas on which to draw
     */

    private void drawPath(int start, int end, int color, Paint paintPath,
                          Canvas canvas) {

        Path path = new Path();
        int viewHeightHalf = this.getMeasuredHeight() / 2;

        for (; start <= end; start += 1) {
            if (start >= yAxis.size())
                break;

            if (Math.floor(yAxis.get(start)) >= 1) {
                path.moveTo(start, viewHeightHalf - (yAxis.get(start)));
                path.lineTo(start, viewHeightHalf + (yAxis.get(start)));

            } else { // making it visible if very low value (thicker line)
                path.moveTo(start, (float) (viewHeightHalf * 1.01));
                path.lineTo(start, (float) (viewHeightHalf * 0.99));
            }
        }

        path.close();
        paintPath.setColor(color);
        canvas.drawPath(path, paintPath);
        path.reset();
    }

    public boolean hasEndPoint()
    {
        return isEndPoint;
    }
    public int getEndPoint()
    {
        return markEndPointX;
    }
    public boolean hasStartPoint()
    {
        return isSetPoint;
    }
    public int getMarkPoint()
    {
        return markPointX;
    }

    // Method to draw the seekbar thumb barfor every updation
    private void drawthumb(int start, int end, int color, Paint paintPath,
                           Canvas canvas) {
        myStartPos = start;
        int viewHeightHalf = this.getMeasuredHeight() / 2;
        paintPath.setColor(color);
        canvas.drawLine(start, viewHeightHalf - viewHeightHalf, start,
                viewHeightHalf + viewHeightHalf, paintPath);
        canvas.drawLine(start - 1, viewHeightHalf - viewHeightHalf, start - 1,
                viewHeightHalf + viewHeightHalf, paintPath);
        canvas.drawLine(start + 1, viewHeightHalf - viewHeightHalf, start + 1,
                viewHeightHalf + viewHeightHalf, paintPath);
        canvas.drawLine(start - 2, viewHeightHalf - viewHeightHalf, start - 2,
                viewHeightHalf + viewHeightHalf, paintPath);
        canvas.drawLine(start + 2, viewHeightHalf - viewHeightHalf, start + 2,
                viewHeightHalf + viewHeightHalf, paintPath);

    }


    public int getValues() {
        return myStartPos;
    }

    private void readWavFile() throws IOException, WavFileException {

        try {
            // Open the wav file specified as the first argument
            if (inputStream == null)
                return;

            WavFile wavFile = WavFile.openWavFile(inputStream);

            // Get the number of audio channels in the wav file
            int numChannels = wavFile.getNumChannels();

            // Create a buffer of 100 frames
            double[] buffer = new double[READ_FRAME_COUNT * numChannels];

            int framesRead;

            int samplesPerPixel = 0;

            if (getMeasuredWidth() != 0) {

                samplesPerPixel = (int) (wavFile.getNumFrames() * numChannels)
                        / (getMeasuredWidth());

            }

            double avrg = 0;
            int overallCounter = 0;
            int pointsInAvrg = 0;
            float yPoint = 0; // a single value on y-axis
            double max = Double.MIN_VALUE; // global max
            double maxLoc = Double.MIN_VALUE; // local max

            do {
                // Read frames into buffer
                framesRead = wavFile.readFrames(buffer, READ_FRAME_COUNT);

                // Loop through frames and store yAxis values
                if (framesRead != 1) {
                    for (int s = 0; s < framesRead * numChannels; s += 1) {
                        if (buffer[s] > maxLoc)
                            maxLoc = buffer[s];

                        // get max out of every 256
                        if (overallCounter % 512 == 0) {
                            avrg += Math.abs(maxLoc);
                            maxLoc = Double.MIN_VALUE;
                            pointsInAvrg++;
                        }
                        // average of maximums from samplesPerPixel
                        if (overallCounter % samplesPerPixel == 0) {

                            if (pointsInAvrg == 0)
                                yAxis.add((float) 0);
                            else {
                                yPoint = (float) (avrg / pointsInAvrg);
                                yAxis.add(yPoint);
                                if (yPoint > max)
                                    max = (yPoint);
                                pointsInAvrg = 0;
                                avrg = 0;
                            }
                        }
                        overallCounter++;
                    }
                }

            } while (framesRead != 0);

            // ratio relative to the screensize and padding
            double ratio = 0;

            if (max != 0) {

                ratio = ((this.getMeasuredHeight() / 2) - getPaddingTop() - getPaddingBottom())
                        / max;
            }


            // normalizing the values relative to the ratio
            for (overallCounter = 0; overallCounter < yAxis.size(); overallCounter++) {
                yAxis.set(overallCounter,
                        (float) (yAxis.get(overallCounter) * ratio));
            }

            // Close the wavFile
            wavFile.close();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.e("DrawBar",e.getMessage());
        }
    }

}
