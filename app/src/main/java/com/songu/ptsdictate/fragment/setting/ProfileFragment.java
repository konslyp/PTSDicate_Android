package com.songu.ptsdictate.fragment.setting;

import android.app.Service;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.songu.ptsdictate.R;
import com.songu.ptsdictate.activity.MainActivity;
import com.songu.ptsdictate.doc.Globals;
import com.songu.ptsdictate.service.IServiceResult;
import com.songu.ptsdictate.service.ServiceManager;
import com.songu.ptsdictate.util.Utils;

/**
 * Created by Administrator on 3/23/2018.
 */

public class ProfileFragment extends Fragment implements IServiceResult,View.OnClickListener {


    private View mRootView;
    private ImageButton btnBack;
    private TextView txtDone,txtTitle,txtId;
    private EditText editPassword;
    private TextView txtName,txtEmail;
    private MaterialDialog noticeDialog;
    private MaterialDialog processDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mRootView == null)
            mRootView = inflater.inflate(R.layout.fragment_setting_profile, container, false);
        initView();
        setData();
        return mRootView;
    }

    public void initView()
    {
        btnBack = (ImageButton) mRootView.findViewById(R.id.header_back_BTN);
        txtDone = (TextView) mRootView.findViewById(R.id.inflate_header_done_TXT);
        txtTitle = (TextView)mRootView.findViewById(R.id.title_TXT);
        txtEmail = (TextView) mRootView.findViewById(R.id.screen_settings_profile_email);
        txtName = (TextView) mRootView.findViewById(R.id.screen_settings_profile_name);
        txtId = (TextView) mRootView.findViewById(R.id.screen_settings_profile_login_id);
        editPassword = (EditText) mRootView.findViewById(R.id.screen_settings_profile_password);

        processDialog = new MaterialDialog.Builder(this.getContext())
                .title("Please wait")
                .content("Update Processing...")
                .titleColor(Color.BLACK)
                .contentColor(Color.GRAY)
                .cancelable(false)
                .progress(true, 0).build();
        noticeDialog = new MaterialDialog.Builder(this.getContext())
                .contentColor(Color.GRAY)
                .content("Please input correct password")
                .positiveText("OK").build();


        txtDone.setVisibility(View.GONE);
        btnBack.setOnClickListener(this);
        txtDone.setOnClickListener(this);
    }
    public void setData()
    {
        txtTitle.setText("Profile");
        txtEmail.setText(Globals.mAccount.mEmail);
        txtName.setText(Globals.mAccount.mName);
        txtId.setText(Globals.mAccount.mID);
        editPassword.setText(Globals.mAccount.mPassword);
    }
    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.header_back_BTN:
                ((MainActivity)getActivity()).backFragment();
                break;
            case R.id.inflate_header_done_TXT:
                String pw = editPassword.getText().toString();
                if (pw.length() < 5) {

                    noticeDialog.show();
                    return;
                }
                Globals.mAccount.mPassword = pw;
                ServiceManager.serviceUpdateProfile(this);
                break;
        }
    }

    @Override
    public void onResponse(int code) {
        switch (code)
        {
            case 200:
                Utils.savePreference(this.getContext(),true);
                setData();
                break;
        }
    }
}
