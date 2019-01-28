package com.songu.ptsdictate.adapter;

import android.Manifest;
import android.content.Context;
import android.os.Handler;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.songu.ptsdictate.R;
import com.songu.ptsdictate.activity.MainActivity;
import com.songu.ptsdictate.doc.Enums;
import com.songu.ptsdictate.doc.Globals;
import com.songu.ptsdictate.fragment.LibraryFragment;
import com.songu.ptsdictate.model.RecordModel;
import com.songu.ptsdictate.util.Utils;

import net.frakbot.blinktextview.BlinkTextView;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by Administrator on 3/26/2018.
 */

public class AdapterLibrary extends BaseAdapter {

    List<RecordModel> lstItems;
    public int sel = -1;
    public Context mContext;

    public AdapterLibrary(Context con) {
        super();
        mContext = con;
    }

    public int getCount() {
        if (this.lstItems == null)
            return 0;
        return this.lstItems.size();
    }

    public RecordModel getItem(int paramInt) {
        return this.lstItems.get(paramInt);
    }

    public long getItemId(int paramInt) {
        return 0L;
    }

    public View getView(final int paramInt, View paramView, ViewGroup paramViewGroup) {
        View localView = paramView;
        ViewHolder localViewHolder = null;

        if (localView == null) {
            localView = LayoutInflater.from(paramViewGroup.getContext()).inflate(R.layout.inflate_existing_dictations_list_item, null);
        } else {
            localViewHolder = (ViewHolder) localView.getTag();
        }
        if (localViewHolder == null) {
            localViewHolder = new ViewHolder();
            localViewHolder.btnPlay = ((CheckBox) localView.findViewById(R.id.existing_dictation_list_play_CHBX));
            localViewHolder.txtName = ((TextView) localView.findViewById(R.id.existing_dictation_list_file_name));
            localViewHolder.txtSize = ((TextView) localView.findViewById(R.id.existing_dictation_list_file_size));
            localViewHolder.txtElapse = (TextView) localView.findViewById(R.id.existing_dictation_list_file_duration);
            localViewHolder.txtAutoStatus = (BlinkTextView) localView.findViewById(R.id.existing_dictation_list_file_autosave_status);
            localViewHolder.txtUploadStatus = (TextView) localView.findViewById(R.id.existing_dictation_list_file_upload_status);
            localViewHolder.chkSelect = (ImageView) localView.findViewById(R.id.existing_dictation_list_file_select_CHBX);
            localViewHolder.imgComment = (ImageView) localView.findViewById(R.id.existing_dictation_list_file_comments_IMG);
            localViewHolder.imgEdit = (ImageView) localView.findViewById(R.id.existing_dictation_list_file_edit_IMG);
            localView.setTag(localViewHolder);
        }


        final RecordModel hItem = lstItems.get(paramInt);
        localViewHolder.txtName.setText(hItem.mName);
        if (hItem.mSize != null) {
            localViewHolder.txtSize.setText(Utils.readableFileSize(Long.parseLong(hItem.mSize)));
        }
        if (hItem.mElapse != null) {
            localViewHolder.txtElapse.setText(Utils.getTimeString(Long.parseLong(hItem.mElapse)));
        }
        if (hItem.isSelect)
            localViewHolder.chkSelect.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.icn_checked_chbx));
        else
            localViewHolder.chkSelect.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.icn_unchecked_chbx));
        //localViewHolder.chkSelect.setChecked(hItem.isSelect);
        localViewHolder.btnPlay.setChecked(hItem.isPlaying);


        localViewHolder.chkSelect.setVisibility(View.VISIBLE);
        for (int i = 0;i < Globals.g_lstUploads.size();i++)
        {
            if (Globals.g_lstUploads.get(i).mLocalNo == hItem.mLocalNo && (hItem.mIsUpload == 2 || hItem.mIsUpload == 0 || hItem.mIsUpload == 4))
            {
                localViewHolder.chkSelect.setVisibility(View.INVISIBLE);
            }
        }
        if (hItem.isComment == 0)
        {
            localViewHolder.imgComment.setVisibility(View.INVISIBLE);
        }
        else
        {
            localViewHolder.imgComment.setVisibility(View.VISIBLE);
            if (hItem.mComment.equals("")) {
                if (hItem.mUploaded == 1) {
                    localViewHolder.imgComment.setImageResource(R.drawable.icn_no_comments_disable);
                }
                else
                {
                    localViewHolder.imgComment.setImageResource(R.drawable.icn_no_comments);
                }
            }
            else {
                if (hItem.mUploaded == 1) {
                    localViewHolder.imgComment.setImageResource(R.drawable.icn_comments_disable);
                }
                else
                {
                    localViewHolder.imgComment.setImageResource(R.drawable.icn_comments);
                }
            }
        }

        localViewHolder.chkSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lstItems.get(paramInt).isSelect = !hItem.isSelect;
                notifyDataSetChanged();
            }
        });

        if (hItem.mIsAutoSave == 1 || hItem.mIsAutoSave == 2)
        {
            localViewHolder.txtAutoStatus.setText("Auto Saved File");
            localViewHolder.txtAutoStatus.setVisibility(View.VISIBLE);
            localViewHolder.txtUploadStatus.setVisibility(View.GONE);
        }
        else
        {
            localViewHolder.txtAutoStatus.setText("");
            localViewHolder.txtAutoStatus.setVisibility(View.GONE);
            localViewHolder.txtUploadStatus.setVisibility(View.GONE);
        }

        if (hItem.mIsUpload == 1)
        {
            localViewHolder.txtAutoStatus.setVisibility(View.GONE);
            localViewHolder.txtUploadStatus.setText("Uploaded");
            localViewHolder.txtUploadStatus.setVisibility(View.VISIBLE);
        }
        else if (hItem.mIsUpload == 3)
        {
            localViewHolder.txtAutoStatus.setVisibility(View.GONE);
            localViewHolder.txtUploadStatus.setText("Failed");
            localViewHolder.txtUploadStatus.setVisibility(View.VISIBLE);
        }
        else
        {
            localViewHolder.txtUploadStatus.setVisibility(View.GONE);
        }
        localViewHolder.imgEdit.setEnabled(true);
        localViewHolder.imgEdit.setImageDrawable(mContext.getResources().getDrawable(R.drawable.icn_edit_recording));
        if (hItem.mUploaded == 1)
        {
            localViewHolder.imgEdit.setEnabled(false);
            localViewHolder.imgEdit.setImageDrawable(mContext.getResources().getDrawable(R.drawable.icn_edit_recording_disable));
        }
        if (checkStoragePermission())
            localViewHolder.btnPlay.setEnabled(true);
        else
            localViewHolder.btnPlay.setEnabled(false);
        localViewHolder.btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((MainActivity)mContext).currentFragment instanceof LibraryFragment)
                {
                    ((LibraryFragment)((MainActivity)mContext).currentFragment).setRecordPlayFile(hItem,!hItem.isPlaying);
                }
            }
        });
        localViewHolder.imgEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i = 0;i < Globals.g_lstUploads.size();i++)
                {
                    if (Globals.g_lstUploads.get(i).mLocalNo == hItem.mLocalNo && (hItem.mIsUpload == 2 || hItem.mIsUpload == 0 || hItem.mIsUpload == 4))
                    {
                        return;
                    }
                }
                if (hItem.mUploaded == 1) return;
                Globals.g_isExistRecord = true;
                Globals.g_existFile = hItem;
                Globals.e_mode = Enums.MODE.RECORD;
                ((MainActivity)mContext).setFragment();
            }
        });
        localViewHolder.imgComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                for (int i = 0;i < Globals.g_lstUploads.size();i++)
//                {
//                    if (Globals.g_lstUploads.get(i).mLocalNo == hItem.mLocalNo && (hItem.mIsUpload == 2 || hItem.mIsUpload == 0 || hItem.mIsUpload == 4))
//                    {
//                        return;
//                    }
//                }
                if (hItem.isComment == 0) return;
//                if (hItem.mUploaded == 1) return;
                Globals.g_existFile = hItem;
                Globals.e_mode = Enums.MODE.LIBRARY_COMMENT;
                ((MainActivity)mContext).setFragment();
            }
        });
        return localView;
    }
    public boolean checkStoragePermission()
    {
        return MainActivity.hasPermissions(mContext,  Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);
    }
    public void update(List<RecordModel> paramList) {
        this.lstItems = paramList;
        this.notifyDataSetChanged();
    }

    public class ViewHolder {
        public CheckBox btnPlay;
        public TextView txtName;
        public TextView txtSize;
        public TextView txtElapse;
        public TextView txtUploadStatus;
        public BlinkTextView txtAutoStatus;
        public ImageView chkSelect;
        public ImageView imgComment;
        public ImageView imgEdit;
    }


}