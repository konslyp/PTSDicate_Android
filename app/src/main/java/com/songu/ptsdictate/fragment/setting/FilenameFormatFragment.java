package com.songu.ptsdictate.fragment.setting;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.songu.ptsdictate.R;
import com.songu.ptsdictate.activity.MainActivity;
import com.songu.ptsdictate.doc.Globals;
import com.songu.ptsdictate.service.IServiceResult;
import com.songu.ptsdictate.util.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 3/23/2018.
 */

public class FilenameFormatFragment extends Fragment implements IServiceResult,View.OnClickListener {


    private View mRootView;
    private ImageButton btnBack;
    private TextView txtDone;
    private TextView txtPreview;
    private Spinner spDateFormat;
    private EditText editFilename;
    private TextView txtTitle;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mRootView == null)
            mRootView = inflater.inflate(R.layout.fragment_setting_fileformat, container, false);
        initView();
        setData();
        updatePreview();
        return mRootView;
    }

    public void initView()
    {
        btnBack = (ImageButton) mRootView.findViewById(R.id.header_back_BTN);
        txtDone = (TextView) mRootView.findViewById(R.id.inflate_header_done_TXT);
        spDateFormat = (Spinner) mRootView.findViewById(R.id.screen_file_naming_date_format_SPN);
        editFilename = (EditText) mRootView.findViewById(R.id.screen_file_naming_file_name_EDT);
        txtPreview = (TextView) mRootView.findViewById(R.id.screen_file_naming_file_name_model_TXT);

        txtTitle = mRootView.findViewById(R.id.title_TXT);
        txtTitle.setText("File Naming Date Format");


        editFilename.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                updatePreview();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        spDateFormat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                updatePreview();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
                btnBack.setOnClickListener(this);
        txtDone.setOnClickListener(this);
    }
    public void updatePreview()
    {
        List<String> Lines = Arrays.asList(getResources().getStringArray(R.array.filenameformat));

        SimpleDateFormat curFormater = new SimpleDateFormat(Lines.get(spDateFormat.getSelectedItemPosition()));
        Date dateObj = new Date();
        String newDateStr = curFormater.format(dateObj);

        String subFix = String.format("%03d",Globals.g_fileIndex);
        String fileName = editFilename.getText().toString().trim() + "_" + newDateStr + "_File_" + subFix + ".wav";
        txtPreview.setText(fileName);
    }
    public void setData()
    {
        editFilename.setText(Utils.filterName(Globals.mSetting.mFilePrefix));
        spDateFormat.setSelection(Globals.mSetting.mDateFormat);
    }
    public void clickDone()
    {
        if (editFilename.getText().toString().trim().equals("") || editFilename.getText().toString().trim().equals(" "))
        {
            new MaterialDialog.Builder(this.getContext())
                    .title("PTS Dictate")
                    .content("Filename should not be empty")
                    .positiveText("OK")
                    .titleColor(Color.BLACK)
                    .contentColor(Color.GRAY)
                    .show();
            return;
        }
        Globals.mSetting.mFilePrefix = editFilename.getText().toString().trim();
        Globals.mSetting.mDateFormat = spDateFormat.getSelectedItemPosition();
        Utils.saveSetting(this.getContext());
        ((MainActivity)getActivity()).backFragment();
    }
    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.header_back_BTN:
                ((MainActivity)getActivity()).backFragment();
                break;
            case R.id.inflate_header_done_TXT:
                clickDone();
                break;
        }
    }

    @Override
    public void onResponse(int code) {

    }
}
