package com.songu.ptsdictate.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.rollbar.android.Rollbar;
import com.songu.ptsdictate.R;
import com.songu.ptsdictate.doc.Globals;
import com.songu.ptsdictate.util.Utils;

/**
 * Created by Administrator on 3/22/2018.
 */

public class SplashActivity extends Activity {



    private Rollbar rollbar;
    public Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message inputMessage) {
            Intent mIntent;
            switch (inputMessage.what)
            {
                case 0:
                    mIntent = new Intent(SplashActivity.this,LoginActivity.class);
                    SplashActivity.this.startActivity(mIntent);
                    SplashActivity.this.finish();
                    break;
                case 1:
                    mIntent = new Intent(SplashActivity.this,MainActivity.class);
                    SplashActivity.this.startActivity(mIntent);
                    SplashActivity.this.finish();
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Utils.loadPreference(this);
        rollbar = Rollbar.instance();
        if (Utils.isLogin(this) == 0)
        {
            rollbar.debug("First Time to Login");
            mHandler.sendEmptyMessageDelayed(0,3000);
        }
        else
        {
            rollbar.debug("Already Has Login");
            mHandler.sendEmptyMessageDelayed(1,3000);
        }
    }
}
