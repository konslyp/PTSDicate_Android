package com.songu.ptsdictate.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.songu.ptsdictate.R;
import com.songu.ptsdictate.activity.MainActivity;
import com.songu.ptsdictate.doc.Enums;
import com.songu.ptsdictate.doc.Globals;
import com.songu.ptsdictate.fragment.LibraryFragment;
import com.songu.ptsdictate.model.RecordModel;
import com.songu.ptsdictate.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 3/30/2018.
 */

public class AdapterUpload extends BaseAdapter {

    List<RecordModel> lstItems;
    public int sel = -1;
    public Context mContext;
    public int progressUpload = 0;

//    private Handler mHandler = new Handler()
//    {
//        @Override
//        public void handleMessage(Message inputMessage) {
//            switch (inputMessage.what)
//            {
//                case 0:
//                    if (prsUpload != null)
//                    {
//                        if (progressUpload < 99)
//                            progressUpload++;
//                        if (txtProgress != null)
//                        {
//                            txtProgress.setText(String.format("Uploading Percent: %2d",progressUpload) + "%");
//                        }
//                        prsUpload.setProgress(progressUpload);
//                        mHandler.sendEmptyMessageDelayed(0,30);
//                    }
//                    break;
//            }
//        }
//    };


    public AdapterUpload(Context con) {
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
            localView = LayoutInflater.from(paramViewGroup.getContext()).inflate(R.layout.upload_status_list_item, null);
        } else {
            localViewHolder = (ViewHolder) localView.getTag();
        }
        if (localViewHolder == null) {
            localViewHolder = new ViewHolder();
            localViewHolder.txtName = ((TextView) localView.findViewById(R.id.upload_file_name));
            localViewHolder.txtSize = ((TextView) localView.findViewById(R.id.upload_file_size));
            localViewHolder.txtElapse = (TextView) localView.findViewById(R.id.upload_file_duration);
            localViewHolder.txtUploadStatus = (TextView) localView.findViewById(R.id.upload_file_status);
            localViewHolder.layoutProgress = (LinearLayout) localView.findViewById(R.id.uploading_file_LinearLayout);
            localViewHolder.layoutStatus = (LinearLayout) localView.findViewById(R.id.uploaded_file_details_layout);
            localViewHolder.progressBar = (ProgressBar) localView.findViewById(R.id.upload_file_progressBar);
            localViewHolder.txtProgress = (TextView) localView.findViewById(R.id.upload_file_progress_update_percent_TXT);

            localView.setTag(localViewHolder);
        }

        if (paramInt >= lstItems.size()) return null;

        final RecordModel hItem = lstItems.get(paramInt);
        localViewHolder.txtName.setText(hItem.mName);
        if (hItem.mIsAutoSave == 1)
        {
            String autoSaveName1 = hItem.mName.replace("_autosave.wav","");
            String autoSaveName = autoSaveName1.replace(".wav","_autosave.wav");
            localViewHolder.txtName.setText(autoSaveName);
        }

        localViewHolder.txtSize.setText(Utils.readableFileSize(Long.parseLong(hItem.mSize)));
        localViewHolder.txtElapse.setText(Utils.getTimeString(Long.parseLong(hItem.mElapse)));
//        prsUpload = null;
//        txtProgress = null;
//        if (hItem.mIsUpload == 2) // Uploading
//        {
//            localViewHolder.layoutStatus.setVisibility(View.GONE);
//            localViewHolder.layoutProgress.setVisibility(View.VISIBLE);
//            prsUpload = (ProgressBar) localView.findViewById(R.id.upload_file_progressBar);
//            txtProgress = localViewHolder.txtProgress;
//            progressUpload = 0;
//            mHandler.sendEmptyMessageDelayed(0,30);
//        }
//        else
//        {
//            localViewHolder.layoutStatus.setVisibility(View.VISIBLE);
//            localViewHolder.layoutProgress.setVisibility(View.GONE);
//            if (hItem.mIsUpload == 1) // Uploaded
//            {
//                localViewHolder.txtUploadStatus.setText("Uploaded");
//            }
//        }

//        localViewHolder.layoutStatus.setVisibility(View.VISIBLE);
//        localViewHolder.layoutProgress.setVisibility(View.GONE);
        if (hItem.mIsUpload == 1) // Uploaded
        {
            localViewHolder.layoutStatus.setVisibility(View.VISIBLE);
            localViewHolder.layoutProgress.setVisibility(View.GONE);
            localViewHolder.txtUploadStatus.setText("Uploaded");
        }
        else if (hItem.mIsUpload == 3)
        {
            localViewHolder.layoutStatus.setVisibility(View.VISIBLE);
            localViewHolder.layoutProgress.setVisibility(View.GONE);
            localViewHolder.txtUploadStatus.setText("Failed");
        }
        else if (hItem.mIsUpload == 2 || hItem.mIsUpload == 4)
        {
            localViewHolder.layoutStatus.setVisibility(View.GONE);
            localViewHolder.layoutProgress.setVisibility(View.VISIBLE);
            if (mContext != null) {
                final ViewHolder finalLocalViewHolder = localViewHolder;
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        finalLocalViewHolder.txtProgress.setText(String.format("Uploading Percent: %2d", hItem.uploadProgress) + "%");
                        finalLocalViewHolder.progressBar.setProgress(hItem.uploadProgress);
                    }
                });
            }

        }
        else
        {
            localViewHolder.layoutStatus.setVisibility(View.VISIBLE);
            localViewHolder.layoutProgress.setVisibility(View.GONE);
            localViewHolder.txtUploadStatus.setText("");
        }


        localView.setTag(localViewHolder);
        return localView;
    }

    public void updateModels(List<RecordModel> paramList)
    {
        this.lstItems = paramList;
        this.notifyDataSetChanged();
    }
    public void update(List<RecordModel> paramList) {
        this.lstItems = paramList;
        this.notifyDataSetChanged();
    }

    public class ViewHolder {
        public TextView txtName;
        public TextView txtSize;
        public TextView txtElapse;
        public TextView txtUploadStatus;
        public LinearLayout layoutStatus;
        public LinearLayout layoutProgress;
        public ProgressBar progressBar;
        public TextView txtProgress;
    }

}