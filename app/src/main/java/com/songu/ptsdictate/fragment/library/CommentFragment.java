package com.songu.ptsdictate.fragment.library;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.songu.ptsdictate.R;
import com.songu.ptsdictate.activity.MainActivity;
import com.songu.ptsdictate.doc.Enums;
import com.songu.ptsdictate.doc.Globals;

/**
 * Created by Administrator on 3/29/2018.
 */

public class CommentFragment extends Fragment implements View.OnClickListener{


    private View mRootView;
    private TextView txtTitle;
    private EditText editComment;
    private Button btnSave,btnDiscard;
    private LinearLayout layoutBar;
    private TextView txtLabel;
    private TextView txtDone;


    public InputFilter EMOJI_FILTER = new InputFilter() {

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            for (int index = start; index < end; index++) {

                int type = Character.getType(source.charAt(index));

                if (type == Character.SURROGATE) {
                    return "";
                }
            }
            return null;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mRootView == null)
            mRootView = inflater.inflate(R.layout.fragment_library_comment, container, false);
        initView();
        setData();
        return mRootView;
    }
    public void setData()
    {
        editComment.setText(Globals.g_existFile.mComment);
    }
    public void initView()
    {
        txtTitle = mRootView.findViewById(R.id.title_TXT);
        txtDone = mRootView.findViewById(R.id.inflate_header_done_TXT);
        editComment = mRootView.findViewById(R.id.screen_existing_comments_EDT);
        btnSave = mRootView.findViewById(R.id.screen_existing_comments_save_button);
        btnDiscard = mRootView.findViewById(R.id.screen_existing_comments_discard_button);
        layoutBar = mRootView.findViewById(R.id.screen_record_save_discard_lay);
        txtLabel = mRootView.findViewById(R.id.txt_edit_comment_label);

        txtTitle.setText("Comments");

        btnSave.setOnClickListener(this);
        btnDiscard.setOnClickListener(this);

        if (Globals.g_existFile.mComment != null && Globals.g_existFile.mComment.equals(""))
        {
            txtLabel.setText("Add Comments");
        }
        else
            txtLabel.setText("Edit Comments");
        if (Globals.g_existFile.mUploaded == 1)
        {
            layoutBar.setVisibility(View.GONE);
            txtDone.setVisibility(View.VISIBLE);
            txtDone.setOnClickListener(this);
//            editComment.setFilters(new InputFilter[] {
//                    new InputFilter() {
//                        public CharSequence filter(CharSequence src, int start,
//                                                   int end, Spanned dst, int dstart, int dend) {
//                            return src.length() < 1 ? dst.subSequence(dstart, dend) : "";
//                        }
//                    }
//            });
            editComment.setEnabled(false);
        }
        else
        {
            for (int i = 0;i < Globals.g_lstUploads.size();i++)
            {
                if (Globals.g_lstUploads.get(i).mLocalNo == Globals.g_existFile.mLocalNo && (Globals.g_existFile.mIsUpload == 2 || Globals.g_existFile.mIsUpload == 0 || Globals.g_existFile.mIsUpload == 4))
                {
                    layoutBar.setVisibility(View.GONE);
                    editComment.setEnabled(false);
                    return;
                }
            }
            editComment.setEnabled(true);
            layoutBar.setVisibility(View.VISIBLE);
        }
    }

    public void saveComment()
    {
        if (editComment.getText().toString().trim().equals("") && Globals.mSetting.isCommentMandatory)
        {
            new MaterialDialog.Builder(this.getContext())
                    .title("PTS Dictate")
                    .content("Mandatory Comment Entry required")
                    .positiveText("OK")
                    .titleColor(Color.BLACK)
                    .contentColor(Color.GRAY)
                    .show();
            return;
        }
        Globals.g_existFile.mComment = editComment.getText().toString();
        Globals.g_existFile.isComment = 1;
        Globals.g_database.updateRecordFile(Globals.g_existFile);
        Globals.e_mode = Enums.MODE.LIBRARY;
        ((MainActivity)getActivity()).setFragment();
        ((MainActivity)CommentFragment.this.getActivity()).showHideTab(true);
    }
    public void backFragment()
    {
        if ((Globals.g_existFile.mComment.trim().equals("") && Globals.mSetting.isCommentMandatory))
        {
            new MaterialDialog.Builder(this.getContext())
                    .title("PTS Dictate")
                    .content("Mandatory Comment Entry required")
                    .positiveText("OK")
                    .titleColor(Color.BLACK)
                    .contentColor(Color.GRAY)
                    .show();
            return;
        }
        else if (!Globals.mSetting.isCommentMandatory)
        {
            Globals.g_existFile.isComment = 1;
            Globals.g_database.updateRecordFile(Globals.g_existFile);
        }
        Globals.e_mode = Enums.MODE.LIBRARY;
        ((MainActivity)getActivity()).setFragment();
        ((MainActivity)CommentFragment.this.getActivity()).showHideTab(true);
    }
    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.screen_existing_comments_save_button:
                saveComment();
                break;
            case R.id.screen_existing_comments_discard_button:
                backFragment();
                break;
            case R.id.inflate_header_done_TXT:
                backFragment();
                break;
        }
    }
}
