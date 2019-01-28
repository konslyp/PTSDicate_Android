package com.songu.ptsdictate.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.songu.ptsdictate.R;
import com.songu.ptsdictate.activity.MainActivity;
import com.songu.ptsdictate.fragment.RecordFragment;

/**
 * Created by Administrator on 4/11/2018.
 */

public class OverlayService extends Service {

    public View overlayView;
    public boolean isVisible;
    private WindowManager.LayoutParams params;
    private WindowManager windowManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private BroadcastReceiver recordReceiver = new RecordReceiver();

    public class RecordReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("show"))
            {
                showView(true);
            }
            else if (intent.getAction().equals("hide"))
            {
                showView(false);
            }
            else if (intent.getAction().equals("keep"))
            {
                addKeepScreen(true);
            }
            else if (intent.getAction().equals("unkeep"))
            {
                addKeepScreen(false);
            }

        }
    }



    public void addKeepScreen(boolean isKeep)
    {
        overlayView.setKeepScreenOn(isKeep);
    }
    @Override
    public void onCreate() {
        super.onCreate();
        if (overlayView == null)
            initOverlayView();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,

                PixelFormat.TRANSLUCENT);

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);


        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 0;

        Point size = new Point();
        windowManager.getDefaultDisplay().getSize(size);
        params.width = size.x;
        params.height = 100;
        IntentFilter filter = new IntentFilter("show");
        filter.addAction("hide");
        filter.addAction("keep");
        filter.addAction("unkeep");
        this.registerReceiver(recordReceiver, filter);
    }
    public void showView(boolean visible)
    {
        if (visible)
        {
            //overlayView.setVisibility(View.VISIBLE);
            windowManager.addView(overlayView, params);
        }
        else
        {
            if (overlayView.getParent() != null)
                windowManager.removeView(overlayView);
        }
    }
    public void initOverlayView()
    {
        if (overlayView == null) {
            overlayView = LayoutInflater.from(this).inflate(R.layout.inflate_overlay_status, null);
        }
        RelativeLayout layoutOverlay = overlayView.findViewById(R.id.layoutOverlay);
        layoutOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent m = new Intent(OverlayService.this,MainActivity.class);
                m.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_SINGLE_TOP);
                OverlayService.this.startActivity(m);
            }
        });
    }

    @Override
    public void onDestroy()
    {
        this.unregisterReceiver(this.recordReceiver);
        super.onDestroy();
    }

}
