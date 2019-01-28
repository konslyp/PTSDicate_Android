package com.songu.ptsdictate.fragment.library;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.songu.ptsdictate.R;
import com.songu.ptsdictate.activity.MainActivity;
import com.songu.ptsdictate.doc.Enums;
import com.songu.ptsdictate.doc.Globals;

import java.io.File;

/**
 * Created by Administrator on 3/29/2018.
 */

public class RenameFragment extends Fragment implements View.OnClickListener{


    public View mRootView;
    private TextView txtFilename;
    private TextView txtTitle,btnDone;
    private ImageButton btnCancel;
    private EditText editName;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mRootView == null)
            mRootView = inflater.inflate(R.layout.fragment_library_rename, container, false);
        initView();
        setData();
        return mRootView;
    }
    public void setData()
    {
        txtFilename.setText(Globals.g_existFile.mName);
    }
    public void initView()
    {
        txtFilename = mRootView.findViewById(R.id.screen_existing_dictations_rename_FileName_TXT);
        txtTitle = mRootView.findViewById(R.id.title_TXT);
        btnDone = mRootView.findViewById(R.id.inflate_header_done_TXT);
        btnCancel = mRootView.findViewById(R.id.inflate_header_cancel_btn);
        editName = mRootView.findViewById(R.id.screen_existing_rename_EDT);

        txtTitle.setText("Rename File");

        btnCancel.setOnClickListener(this);
        btnDone.setOnClickListener(this);
    }
    public void saveFileName()
    {
        MaterialDialog alert = new MaterialDialog.Builder(this.getContext())
                .title("PTS Dictate")
                .positiveText("OK")
                .titleColor(Color.BLACK)
                .contentColor(Color.GRAY)
                .build();
        String strName = editName.getText().toString().trim();
        if (strName.equals("") || strName.equals(" "))
        {
            alert.setContent("Please input file name.");
            alert.show();
            return;
        }
        if (Globals.g_database.isNameExist(strName))
        {
            alert.setContent("File already exist.");
            alert.show();
            return;
        }
        //Rename Path
        String newFilePath = Environment.getExternalStorageDirectory() + "/PTSRecord/" + strName + ".wav";
        File oldFile = new File(Globals.g_existFile.mPath);
        File newFile = new File(newFilePath);
        oldFile.renameTo(newFile);
        Globals.g_existFile.mPath = newFilePath;
        Globals.g_existFile.mName = strName + ".wav";
        Globals.g_database.updateRecordFile(Globals.g_existFile);
        Globals.e_mode = Enums.MODE.LIBRARY;
        ((MainActivity)RenameFragment.this.getActivity()).showHideTab(true);
        ((MainActivity)getActivity()).setFragment();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.inflate_header_cancel_btn:
                Globals.e_mode = Enums.MODE.LIBRARY;
                ((MainActivity)getActivity()).setFragment();
                ((MainActivity)RenameFragment.this.getActivity()).showHideTab(true);
                break;
            case R.id.inflate_header_done_TXT:
                saveFileName();
                break;
        }
    }
}
