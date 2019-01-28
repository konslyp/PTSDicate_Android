package com.songu.ptsdictate.fragment.setting;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.songu.ptsdictate.R;
import com.songu.ptsdictate.activity.MainActivity;
import com.songu.ptsdictate.doc.Globals;
import com.songu.ptsdictate.service.IServiceResult;
import com.songu.ptsdictate.util.Utils;

/**
 * Created by Administrator on 3/23/2018.
 */

public class MicrophoneSensitivityFragment extends Fragment implements View.OnClickListener {


    private View mRootView;
    private ImageButton btnBack;
    private SeekBar seekSensitivity;
    private TextView txtTitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mRootView == null)
            mRootView = inflater.inflate(R.layout.fragment_setting_sensitivity, container, false);
        initView();
        setData();
        return mRootView;
    }

    public void initView()
    {
        seekSensitivity = mRootView.findViewById(R.id.microphone_sensitivity_seekbar);
        btnBack = mRootView.findViewById(R.id.header_back_BTN);
        txtTitle = mRootView.findViewById(R.id.title_TXT);
        txtTitle.setText("Microphone Sensitivity");
        seekSensitivity.setMax(100);
        btnBack.setOnClickListener(this);
        seekSensitivity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                Globals.mSetting.mMicSensitivity = i;
                Utils.saveSetting(MicrophoneSensitivityFragment.this.getContext());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }
    public void setData()
    {
        seekSensitivity.setProgress(Globals.mSetting.mMicSensitivity);
    }
    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.header_back_BTN:
                ((MainActivity)getActivity()).backFragment();
                break;
        }
    }
}
