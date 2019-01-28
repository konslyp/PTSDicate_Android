package com.songu.ptsdictate.fragment.setting;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.songu.ptsdictate.R;
import com.songu.ptsdictate.activity.MainActivity;
import com.songu.ptsdictate.doc.Config;
import com.songu.ptsdictate.doc.Enums;
import com.songu.ptsdictate.doc.Globals;
import com.songu.ptsdictate.service.IServiceResult;
import com.songu.ptsdictate.util.Utils;

/**
 * Created by Administrator on 3/23/2018.
 */

public class AboutFragment extends Fragment implements View.OnClickListener {


    private View mRootView;
    private TextView txtAppVersion,txtAppUpdate;
    private RelativeLayout btnPasswordGuide,btnContactus;
    private TextView txtTitle;
    private ImageButton btnBack;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mRootView == null)
            mRootView = inflater.inflate(R.layout.fragment_setting_about, container, false);
        initView();
        setData();
        return mRootView;
    }

    public void initView()
    {
        txtAppVersion = (TextView) mRootView.findViewById(R.id.screen_settings_about_app_version_TXT);
        btnBack = (ImageButton) mRootView.findViewById(R.id.header_back_BTN);
        txtAppUpdate = (TextView) mRootView.findViewById(R.id.screen_settings_about_app_LUPD_TXT);
        btnPasswordGuide = (RelativeLayout) mRootView.findViewById(R.id.screen_settings_about_password_RLAY);
        btnContactus = (RelativeLayout) mRootView.findViewById(R.id.screen_settings_about_contact_us_RLAY);

        txtTitle = mRootView.findViewById(R.id.title_TXT);
        txtTitle.setText("About");

        btnPasswordGuide.setOnClickListener(this);
        btnContactus.setOnClickListener(this);
        btnBack.setOnClickListener(this);
    }



    public void setData()
    {
        txtAppVersion.setText(Utils.getAppVersion(this.getContext()));
        txtAppUpdate.setText(Config.mAppUpdateDate);
    }
    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.screen_settings_about_password_RLAY:
                Globals.e_mode = Enums.MODE.SETTING_ABOUT_PGUIDE;
                ((MainActivity)getActivity()).setFragment();
                break;
            case R.id.screen_settings_about_contact_us_RLAY:
                Globals.e_mode = Enums.MODE.SETTING_ABOUT_CONTACTUS;
                ((MainActivity)getActivity()).setFragment();
                break;
            case R.id.header_back_BTN:
                ((MainActivity)getActivity()).backFragment();
                break;
        }
    }
}
