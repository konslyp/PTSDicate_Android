package com.songu.ptsdictate.fragment.setting;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.songu.ptsdictate.R;
import com.songu.ptsdictate.activity.MainActivity;
import com.songu.ptsdictate.doc.Globals;
import com.songu.ptsdictate.service.IServiceResult;
import com.songu.ptsdictate.util.Utils;

/**
 * Created by Administrator on 3/23/2018.
 */

public class AudioQualityFragment extends Fragment implements View.OnClickListener {


    private View mRootView;
    private RadioButton radioOption1,radioOption2,radioOption3;
    private RelativeLayout btnOption1,btnOption2,btnOption3;
    private ImageButton btnBack;
    private TextView txtTitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mRootView == null)
            mRootView = inflater.inflate(R.layout.fragment_setting_quality, container, false);
        initView();
        setData();
        return mRootView;
    }

    public void initView()
    {
        radioOption3 = mRootView.findViewById(R.id.eightHzRRBTN);
        radioOption1 = mRootView.findViewById(R.id.elevenHzRRBTN);
        radioOption2 = mRootView.findViewById(R.id.twentytwoHzRBTN);
        btnOption1 = mRootView.findViewById(R.id.elevenHzLAY);
        btnOption2 = mRootView.findViewById(R.id.twentytwoHzLAY);
        btnOption3 = mRootView.findViewById(R.id.eightHzLAY);
        btnBack = mRootView.findViewById(R.id.header_back_BTN);
        txtTitle = mRootView.findViewById(R.id.title_TXT);
        txtTitle.setText("Audio Quality");

        btnOption1.setOnClickListener(this);
        btnOption2.setOnClickListener(this);
        btnOption3.setOnClickListener(this);
        btnBack.setOnClickListener(this);
    }
    public void setData()
    {
        radioOption1.setChecked(false);
        radioOption2.setChecked(false);
        radioOption3.setChecked(false);
        if (Globals.mSetting.mAudioQuality == 0)
        {
            radioOption3.setChecked(true);
        }
        else if (Globals.mSetting.mAudioQuality == 1)
        {
            radioOption1.setChecked(true);
        }
        else if (Globals.mSetting.mAudioQuality == 2)
        {
            radioOption2.setChecked(true);
        }

    }
    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.eightHzLAY:
                Globals.mSetting.mAudioQuality = 0;
                setData();
                Utils.saveSetting(this.getContext());
                break;
            case R.id.elevenHzLAY:
                Globals.mSetting.mAudioQuality = 1;
                setData();
                Utils.saveSetting(this.getContext());
                break;
            case R.id.twentytwoHzLAY:
                Globals.mSetting.mAudioQuality = 2;
                setData();
                Utils.saveSetting(this.getContext());
                break;
            case R.id.header_back_BTN:
                ((MainActivity)getActivity()).backFragment();
                break;
        }
    }
}
