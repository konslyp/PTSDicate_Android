package com.songu.ptsdictate;

import android.app.Application;

import com.rollbar.android.Rollbar;
import com.songu.ptsdictate.database.DBManager;
import com.songu.ptsdictate.doc.Globals;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

/**
 * Created by Administrator on 3/26/2018.
 */
@ReportsCrashes(
        mailTo = "pgyhw718@hotmail.com", // my email here
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.crash_report)
public class MainApplication extends Application {


    private Rollbar rollbar;

    @Override
    public void onCreate() {
        super.onCreate();
        Rollbar.init(this);
        rollbar = Rollbar.instance();
        rollbar.debug("MainApplication Create");
        ACRA.init(this);
    }
    public Rollbar getRollbar()
    {
        return rollbar;
    }

}
