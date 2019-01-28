package com.songu.ptsdictate.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.TextInputEditText;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.afollestad.materialdialogs.MaterialDialog;
import com.songu.ptsdictate.R;
import com.songu.ptsdictate.doc.Globals;
import com.songu.ptsdictate.model.UserModel;
import com.songu.ptsdictate.service.IServiceResult;
import com.songu.ptsdictate.service.OverlayService;
import com.songu.ptsdictate.service.ServiceManager;
import com.songu.ptsdictate.util.Utils;

/**
 * Created by Administrator on 3/22/2018.
 */

public class LoginActivity extends Activity implements View.OnClickListener,IServiceResult{


    private EditText editUser;
    private TextInputEditText editPassword;
    private CheckBox chkRemember;
    private Button btnLogin;
    private MaterialDialog dlgProgress,dlgNotice;
    public static int OVERLAY_PERMISSION_REQ_CODE = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
        setUserInfo();

    }
    @Override
    public void onResume()
    {
        super.onResume();
        //initOverlayService();
    }
    public void initView()
    {
        editUser = (EditText) this.findViewById(R.id.screen_login_username);
        editPassword = (TextInputEditText) this.findViewById(R.id.screen_login_password);
        chkRemember = (CheckBox) this.findViewById(R.id.screen_login_rememeberme_chbx);
        btnLogin = (Button) this.findViewById(R.id.screen_login_loginbtn);
        btnLogin.setOnClickListener(this);
        dlgProgress = new MaterialDialog.Builder(this)
                .title("Please wait")
                .content("Login Processing...")
                .titleColor(Color.BLACK)
                .contentColor(Color.GRAY)
                .cancelable(false)
                .progress(true, 0).build();
        dlgNotice = new MaterialDialog.Builder(this)
                .title("PTS Dictate")
                .titleColor(Color.BLACK)
                .contentColor(Color.GRAY)
                .content("Username/Password are incorrect")
                .positiveText("OK").build();

        editPassword.setTransformationMethod(new PasswordTransformationMethod());


    }
    public void setUserInfo()
    {
        if (Globals.mAccount.mIsKeepInfo)
        {
            editUser.setText(Globals.mAccount.mID);
            editPassword.setText(Globals.mAccount.mPassword);
            chkRemember.setChecked(true);
        }
        else
        {
            chkRemember.setChecked(false);
            editUser.setText("");
            editPassword.setText("");
        }
//        editUser.setText("Mitda$01");
//        editPassword.setText("Mitda$02");
    }
    public void actionLogin()
    {
        String strUser = editUser.getText().toString();
        String strPassword = editPassword.getText().toString();

        if (strUser.equals(""))
        {
            dlgNotice.setContent("Username should not be empty");
            dlgNotice.show();
            return;
        }
        if (strPassword.equals(""))
        {
            dlgNotice.setContent("Password should not be empty");
            dlgNotice.show();
            return;
        }
        dlgProgress.show();
        UserModel uModel = new UserModel();
        uModel.mID = strUser;
        uModel.mPassword = strPassword;
        ServiceManager.serviceLoginNew(uModel,this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.screen_login_loginbtn:
                actionLogin();
                break;
        }
    }

    @Override
    public void onResponse(int code) {
        dlgProgress.dismiss();
        switch (code)
        {
            case 200:
                Globals.mAccount.mIsKeepInfo = chkRemember.isChecked();
                Utils.savePreference(this,true);
                Globals.g_loginFirst = true;
                Intent m = new Intent(this,MainActivity.class);
                this.finish();
                this.startActivity(m);
                break;
            case 400:
                this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dlgNotice.setContent("Username/Password are incorrect");
                        dlgNotice.show();
                    }
                });
                break;
            case 401:
                this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new MaterialDialog.Builder(LoginActivity.this)
                                .title("Login Fail")
                                .titleColor(Color.BLACK)
                                .contentColor(Color.GRAY)
                                .content("Internet Connection is not available.")
                                .positiveText("OK").show();
                    }
                });

                break;
            case 402:
                this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new MaterialDialog.Builder(LoginActivity.this)
                                .title("Login Fail")
                                .titleColor(Color.BLACK)
                                .contentColor(Color.GRAY)
                                .content("Internet Connection is not available.")
                                .positiveText("OK").show();
                    }
                });

                break;
        }
    }
}
