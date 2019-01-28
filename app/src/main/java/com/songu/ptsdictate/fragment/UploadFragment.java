package com.songu.ptsdictate.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.songu.ptsdictate.R;
import com.songu.ptsdictate.activity.MainActivity;
import com.songu.ptsdictate.adapter.AdapterUpload;
import com.songu.ptsdictate.doc.Enums;
import com.songu.ptsdictate.doc.Globals;
import com.songu.ptsdictate.model.RecordModel;
import com.songu.ptsdictate.service.IServiceResult;
import com.songu.ptsdictate.service.ServiceManager;
import com.songu.ptsdictate.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 3/22/2018.
 */

public class UploadFragment extends Fragment implements IServiceResult,View.OnClickListener {


    private View mRootView;
    private LinearLayout layoutUpload;
    private ListView lstUploads;
    private AdapterUpload adapterUpload;
    private LinearLayout layoutOverlay;
    private List<RecordModel> lstUploadedFiles;

    public int progressUpload = 0;
    public ProgressBar prsUpload;
    public TextView txtProgress;
    public TextView txtTitle;
    public ImageView imgTitle;
    private RelativeLayout relWelcome;
    private TextView txtWelcomeName;
    private TextView txtWaring;

    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message inputMessage) {
            switch (inputMessage.what)
            {
                case 0:
                    if (prsUpload != null)
                    {
                        if (txtProgress != null)
                        {
                            txtProgress.setText(String.format("Uploading Percent: %2d",progressUpload) + "%");
                            prsUpload.setProgress(progressUpload);
                        }
                    }
                    break;
                case 2:
                    if (getContext() != null) {
                        Animation slide_up = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up);
                        relWelcome.startAnimation(slide_up);
                    }
                    sendEmptyMessageDelayed(3,1300);
                    break;
                case 3:
                    relWelcome.setVisibility(View.GONE);
                    break;
            }
        }
    };


    private BroadcastReceiver uploadReceiver = new UploadReceiver();

    public class UploadReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("progress"))
            {
                adapterUpload.updateModels(Globals.g_lstUploads);
            }
            else if (intent.getAction().equals("update_progress"))
            {
                String percent = intent.getStringExtra("progress");
                progressUpload = Integer.parseInt(percent);
                uploadProgress();
                mHandler.sendEmptyMessageDelayed(0,100);
            }
            else if (intent.getAction().equals("disable_internet"))
            {
                disableNetworkAnimation();
            }
        }
    }

    public void disableNetworkAnimation()
    {
        //relWelcome.setBackgroundColor(0xffff0000);
        txtWelcomeName.setText("Network is not available");
        txtWaring.setText("Warning");
        Animation slide_down = AnimationUtils.loadAnimation(getContext(),R.anim.slide_down);
        relWelcome.setVisibility(View.VISIBLE);
        relWelcome.startAnimation(slide_down);
        mHandler.sendEmptyMessageDelayed(2,3500);
    }

    public boolean isUploading()
    {
        return Globals.g_isUpload;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentFilter filter = new IntentFilter("progress");
        filter.addAction("update_progress");
        filter.addAction("disable_internet");
        this.getActivity().registerReceiver(uploadReceiver, filter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mRootView == null)
            mRootView = inflater.inflate(R.layout.fragment_upload, container, false);
        initView();
        updateList();
        setData();
        return mRootView;
    }
    public void updateList()
    {

        for (int i = 0;i < Globals.g_lstUploads.size();i++)
        {
            if (Globals.g_lstUploads.get(i).mIsUpload == 0 || Globals.g_lstUploads.get(i).mIsUpload == 2)
            {
                return;
            }
        }
        Globals.g_lstUploads.clear();
    }

    public void initView()
    {
        lstUploadedFiles = new ArrayList<>();
        lstUploads = mRootView.findViewById(R.id.screen_uploads_LV);
        layoutOverlay = (LinearLayout) mRootView.findViewById(R.id.no_uploads_lay);
        layoutUpload = mRootView.findViewById(R.id.no_uploads_lay);
        txtTitle = (TextView)mRootView.findViewById(R.id.title_TXT);
        imgTitle = (ImageView) mRootView.findViewById(R.id.title_IMG);

        relWelcome = (RelativeLayout) mRootView.findViewById(R.id.screen_existing_user_details_LAY);
        txtWelcomeName = (TextView) mRootView.findViewById(R.id.screen_existing_dictations_user_name_TXT);
        txtWaring = (TextView) mRootView.findViewById(R.id.screen_existing_dictations_user_name_label);

        adapterUpload = new AdapterUpload(this.getActivity());
        lstUploads.setAdapter(adapterUpload);
        txtTitle.setText("Uploads");
        imgTitle.setImageDrawable(getResources().getDrawable(R.drawable.icn_uploads));
        imgTitle.setVisibility(View.VISIBLE);
        relWelcome.setVisibility(View.GONE);
    }
    public void setData()
    {
        layoutOverlay.setVisibility(View.GONE);
        lstUploads.setVisibility(View.GONE);
        if (Globals.g_lstUploads.size() > 0)
        {
            lstUploads.setVisibility(View.VISIBLE);
            adapterUpload.update(Globals.g_lstUploads);
        }
        else
        {
            layoutOverlay.setVisibility(View.VISIBLE);
            adapterUpload.update(Globals.g_lstUploads);
        }
    }
//    public void startUploading()
//    {
//        if (Utils.isUploadInternetOn(this.getActivity(),Globals.mSetting.isUploadviaWifi)) {
//            progressUpload = 0;
//            if (Globals.g_lstUploads.size() > 0 && Globals.g_uploadIndex < Globals.g_lstUploads.size()) {
//                Globals.g_uploadIndex++;
//                Globals.g_lstUploads.get(Globals.g_uploadIndex).mIsUpload = 2;
//                ServiceManager.serviceUploadFile(Globals.g_lstUploads.get(Globals.g_uploadIndex), this);
//            }
//            uploadProgress();
//        }
//        else
//        {
//            Globals.g_uploadIndex++;
//            nextFile(0);
//        }
//
//    }
    public void uploadProgress()
    {
        if (Globals.g_uploadIndex > -1 && Globals.g_lstUploads.size() > 0) {
//            View itemView = adapterUpload.lstViews.get(Globals.g_uploadIndex);
//            AdapterUpload.ViewHolder holder = adapterUpload.lstHolders.get(Globals.g_uploadIndex);
//            holder.layoutStatus.setVisibility(View.GONE);
//            holder.layoutProgress.setVisibility(View.VISIBLE);
//            prsUpload = holder.progressBar;
//            txtProgress = holder.txtProgress;
//            mHandler.sendEmptyMessageDelayed(0, 30);

            Globals.g_lstUploads.get(Globals.g_uploadIndex).uploadProgress = progressUpload;
            adapterUpload.updateModels(Globals.g_lstUploads);


        }
    }
//    public void nextFile(int status)
//    {
//        if (Globals.g_lstUploads.size() <= Globals.g_uploadIndex) {
//            Globals.g_isUpload = false;
//            Globals.g_lstUploads.clear();
//            return;
//        }
//        if (status == 0)
//            Globals.g_lstUploads.get(Globals.g_uploadIndex).mIsUpload = 3;
//        else {
//            Globals.g_lstUploads.get(Globals.g_uploadIndex).mIsUpload = 1;
//            Globals.g_lstUploads.get(Globals.g_uploadIndex).mUploadTime = System.currentTimeMillis();
//        }
//        Globals.g_database.updateRecordFile(Globals.g_lstUploads.get(Globals.g_uploadIndex));
//        adapterUpload.updateModels(Globals.g_lstUploads);
//        if (Globals.g_uploadIndex  == Globals.g_lstUploads.size() - 1)
//        {
//            Globals.g_isUpload = false;
//            Globals.g_lstUploads.clear();
//            return;
//        }
//        startUploading();
//    }

    @Override
    public void onClick(View view) {

    }

    public void onDestroy() {
        super.onDestroy();
        this.getActivity().unregisterReceiver(this.uploadReceiver);
    }

    @Override
    public void onResponse(int code) {
//        switch (code)
//        {
//            case 200:
//                if (getActivity() != null) {
//                    getActivity().runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            nextFile(1);
//                        }
//                    });
//                }
//                break;
//            case 400:
//
//                if (getActivity() != null) {
//                    getActivity().runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            disableNetworkAnimation();
//                            nextFile(0);
//                        }
//                    });
//                }
//                break;
//        }
    }
}
