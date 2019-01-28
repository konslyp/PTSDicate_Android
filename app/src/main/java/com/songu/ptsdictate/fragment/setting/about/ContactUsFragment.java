package com.songu.ptsdictate.fragment.setting.about;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.songu.ptsdictate.R;
import com.songu.ptsdictate.activity.MainActivity;
import com.songu.ptsdictate.doc.Config;
import com.songu.ptsdictate.doc.Enums;
import com.songu.ptsdictate.doc.Globals;
import com.songu.ptsdictate.service.IServiceResult;

/**
 * Created by Administrator on 3/23/2018.
 */

public class ContactUsFragment extends Fragment implements View.OnClickListener {

    private View mRootView;
    private RelativeLayout btnCall1,btnCall2,btnEmail1,btnEmail2;
    private MaterialDialog callDialog;
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
            mRootView = inflater.inflate(R.layout.fragment_setting_about_contact, container, false);
        initView();
        return mRootView;
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
    public void initView()
    {
        txtTitle = (TextView) mRootView.findViewById(R.id.title_TXT);
        btnBack = (ImageButton) mRootView.findViewById(R.id.header_back_BTN);
        txtTitle.setText("Contact Us");
        btnCall1 = mRootView.findViewById(R.id.screen_settings_contact_phone1);
        btnCall2 = mRootView.findViewById(R.id.screen_settings_contact_phone2);
        btnEmail1 = mRootView.findViewById(R.id.screen_settings_contact_email1);
        btnEmail2 = mRootView.findViewById(R.id.screen_settings_contact_email2);
        callDialog = new MaterialDialog.Builder(this.getContext())
                .title("Call")
                .titleColor(Color.BLACK)
                .contentColor(Color.GRAY)
                .positiveText("Call").negativeText("Cancel")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog dialog, DialogAction which) {
                        // TODO
                        if (hasPermissions(ContactUsFragment.this.getContext(), Manifest.permission.CALL_PHONE)) {
                            Intent callIntent = new Intent(Intent.ACTION_CALL);
                            callIntent.setData(Uri.parse("tel:" + dialog.getContentView().getText().toString()));
                            startActivity(callIntent);
                        }
                        else
                        {
                            new MaterialDialog.Builder(ContactUsFragment.this.getContext())
                                    .title("PTS Dictate")
                                    .content("Please allow phone permission in app settings.")
                                    .titleColor(Color.BLACK)
                                    .contentColor(Color.GRAY)
                                    .positiveText("OK")
                                    .show();
                        }
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog dialog, DialogAction which) {
                        // TODO
                        dialog.dismiss();
                    }
                }).build();


        btnCall1.setOnClickListener(this);
        btnCall2.setOnClickListener(this);
        btnEmail1.setOnClickListener(this);
        btnEmail2.setOnClickListener(this);
        btnBack.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.screen_settings_contact_phone1:
                callDialog.setContent(Config.mContactPhone1);
                callDialog.show();
                break;
            case R.id.screen_settings_contact_phone2:
                callDialog.setContent(Config.mContactPhone2);
                callDialog.show();
                break;
            case R.id.screen_settings_contact_email1:
                sendEmail(Config.mContactEmail1);
                break;
            case R.id.screen_settings_contact_email2:
                sendEmail(Config.mContactEmail1);
                break;
            case R.id.header_back_BTN:
                Globals.e_mode = Enums.MODE.SETTING_ABOUT;
                ((MainActivity)getActivity()).setFragment();
                break;
        }
    }
    public void sendEmail(String email)
    {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, new String[] { email });

        try {
            startActivity(Intent.createChooser(i, "Send email with...?"));
        } catch (android.content.ActivityNotFoundException exception) {

        }
    }
}