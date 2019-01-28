package com.songu.ptsdictate.fragment.setting.about;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.songu.ptsdictate.R;
import com.songu.ptsdictate.activity.MainActivity;
import com.songu.ptsdictate.doc.Enums;
import com.songu.ptsdictate.doc.Globals;

/**
 * Created by Administrator on 3/23/2018.
 */

public class PasswordGuideFragment extends Fragment implements View.OnClickListener {

    private View mRootView;
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
            mRootView = inflater.inflate(R.layout.fragment_setting_about_password, container, false);
        initView();
        return mRootView;
    }

    public void initView()
    {
        txtTitle = (TextView) mRootView.findViewById(R.id.title_TXT);
        btnBack = (ImageButton) mRootView.findViewById(R.id.header_back_BTN);
        txtTitle.setText("Password Creation Guide");
        btnBack.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.header_back_BTN:
                Globals.e_mode = Enums.MODE.SETTING_ABOUT;
                ((MainActivity)getActivity()).setFragment();
                break;
        }
    }
}