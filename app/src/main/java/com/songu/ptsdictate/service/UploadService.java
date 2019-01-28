package com.songu.ptsdictate.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.songu.ptsdictate.adapter.AdapterUpload;
import com.songu.ptsdictate.doc.Globals;
import com.songu.ptsdictate.model.RecordModel;
import com.songu.ptsdictate.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

/**
 * Created by Administrator on 5/2/2018.
 */

public class UploadService extends Service implements IServiceResult{



    private BroadcastReceiver uploadService = new UploadReceiver();
    public int progressUpload = 0;

    @Override
    public void onResponse(int code) {
        switch (code) {
            case 200:
                nextFile(1);
                break;
            case 400:
                Intent m = new Intent("disable_internet");
                sendBroadcast(m);
                nextFile(0);
                break;
        }
    }

    public class UploadReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("start_upload"))
            {
                Globals.g_isUpload = true;
                startUploading();
            }
        }
    }


    public void serviceUploadFile(final RecordModel model)
    {
        new Thread()
        {
            public void run()
            {
                Intent in = new Intent("update_progress");
                float aTotalBytes = 0, aProgressUpdate = 0;
                String aPercentage = "0";
                HttpsURLConnection conn = null;
                DataInputStream inStream = null;
                DataOutputStream dos = null;
                String lineEnd = "\r\n";
                String twoHyphens = "--";
                String boundary = "*****";
                int bytesRead, bytesAvailable, bufferSize;
                byte[] buffer;
                int maxBufferSize = 1024 * 1024;
                SSLContext context = null;
                try {
                    context = SSLContext.getInstance("TLS");
                    //TrustManager[] tmlist = {new IgnoreSSLTrustManager()};
                    //context.init(null, tmlist, new SecureRandom());
                    //HttpsURLConnection.setDefaultHostnameVerifier(new NullHostNameVerifier());
                    //HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
                    String email = "ON";
                    if (Globals.mSetting.isEmailNotification)
                        email = "OFF";
                    String urlString = "https://www.etranscriptions.com.au/scripts/web_response.php?Case=UploadFile&Login_Name="
                            + Globals.mAccount.mID + "& File_Desc=" + "&To_User=" + "pts" + "&Email_Notification=" + email;
                    if (model.mComment != null)
                        urlString = "https://www.etranscriptions.com.au/scripts/web_response.php?Case=UploadFile&Login_Name="
                                + Globals.mAccount.mID + "& File_Desc=" + URLEncoder.encode(model.mComment) + "&To_User=" + "pts" + "&Email_Notification=" + email;
                    urlString = urlString.replace(" ", "%20");
                    Log.e("URL", urlString);
                    // ------------------ CLIENT REQUEST
                    FileInputStream fileInputStream = new FileInputStream(new File(model.mPath));
                    URL url = new URL(urlString);
                    conn = (HttpsURLConnection) url.openConnection();
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setUseCaches(false);
                    conn.setRequestMethod("POST");
                    conn.setConnectTimeout(900000);
                    conn.setReadTimeout(900000);
                    //conn.setSSLSocketFactory(context.getSocketFactory());
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("Content-Type",
                            "multipart/form-data;boundary=" + boundary);
                    dos = new DataOutputStream(conn.getOutputStream());
                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                            + model.mName + "\"" + lineEnd);
                    dos.writeBytes(lineEnd);
                    // create a buffer of maximum size
                    bytesAvailable = fileInputStream.available();
                    bufferSize = (int) Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];
                    bytesRead = fileInputStream.read(buffer, 0, (int) bufferSize);
                    aTotalBytes = (float) bytesAvailable;
                    while (bytesRead > 0) {
                        if (!Utils.isUploadInternetOn(UploadService.this, Globals.mSetting.isUploadviaWifi)) {
                            Intent m = new Intent("disable_internet");
                            sendBroadcast(m);
                            nextFile(0);
                            return;
                        }
                        dos.write(buffer, 0, (int) bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = (int) Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                        aProgressUpdate = ((float) (aProgressUpdate + bufferSize));
                        aPercentage = String
                                .valueOf(new DecimalFormat("##")
                                        .format((aProgressUpdate / (aTotalBytes + (aTotalBytes / 30))) * 100));

                        Log.e("Percent Upload",aPercentage);
                        in.putExtra("progress",aPercentage);
                        sendBroadcast(in);

//                        Intent aProgress = new Intent();
//
//                        aProgress.setAction("ProgressUpdate");
//
//                        myContext.sendBroadcast(aProgress);

                    }
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                    // close streams
                    Log.e("Debug", "File is written");
                    fileInputStream.close();
                    dos.flush();
                    dos.close();
                    //
//                    ServiceManager.serviceUploadFile(model,UploadService.this);
                    try {
                        inStream = new DataInputStream(conn.getInputStream());
                        String str1;
                        JSONObject aJsonObject = null;
                        while ((str1 = inStream.readLine()) != null) {
                            try {
                                String aResponseCode = "";
                                String aResponseMsg = "";
                                aJsonObject = new JSONObject(str1);
                                aResponseCode = aJsonObject.getString("response_code");
                                aResponseMsg = aJsonObject.getString("response_msg");
                                Log.e("response_code ", aResponseCode);
                                Log.e("response_msg ", aResponseMsg);
                                if (aResponseCode.equals("1")) {
                                    nextFile(1);
                                } else {
                                    Intent m = new Intent("disable_internet");
                                    sendBroadcast(m);
                                    nextFile(0);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                nextFile(0);
                            }
                            Log.e("Debug", "Server Response " + str1);
                        }
                        inStream.close();
                        conn.disconnect();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e("Debug Exception 1", e.getMessage());
                        Intent m = new Intent("disable_internet");
                        sendBroadcast(m);
                        nextFile(0);
                    }
                } catch (MalformedURLException ex) {
                    Log.e("WebService", "MalformedURLException: " + ex.getMessage(), ex);
                    Intent m = new Intent("disable_internet");
                    sendBroadcast(m);
                    nextFile(0);
                } catch (IOException ioe) {
                    Log.e("WebService", "IOException: " + ioe.getMessage(), ioe);
                    Intent m = new Intent("disable_internet");
                    sendBroadcast(m);
                    nextFile(0);
                } catch (NoSuchAlgorithmException e1) {
                    e1.printStackTrace();
                    Log.e("WebService", "IOException: " + e1.getMessage(), e1);
                    Intent m = new Intent("disable_internet");
                    sendBroadcast(m);
                    nextFile(0);
                }
            }
        }.start();
    }

    public void startUploading()
    {
        if (Utils.isUploadInternetOn(this, Globals.mSetting.isUploadviaWifi)) {
            progressUpload = 0;
            if (Globals.g_lstUploads.size() > 0 && Globals.g_uploadIndex < Globals.g_lstUploads.size()) {
                Globals.g_uploadIndex++;
                Globals.g_lstUploads.get(Globals.g_uploadIndex).mIsUpload = 2;
                if (Globals.g_lstUploads.get(Globals.g_uploadIndex).mIsUpload == 1) {
                    Globals.g_lstUploads.get(Globals.g_uploadIndex).mIsUpload = 4;
                }
                serviceUploadFile(Globals.g_lstUploads.get(Globals.g_uploadIndex));
            }
            //uploadProgress();
        }
        else
        {
            Globals.g_uploadIndex++;
            nextFile(0);
        }
    }

//    public void uploadProgress()
//    {
//        if (Globals.g_uploadIndex > -1 && adapterUpload.lstViews.size() > 0) {
//            View itemView = adapterUpload.lstViews.get(Globals.g_uploadIndex);
//            AdapterUpload.ViewHolder holder = (AdapterUpload.ViewHolder) itemView.getTag();
//            holder.layoutStatus.setVisibility(View.GONE);
//            holder.layoutProgress.setVisibility(View.VISIBLE);
//            prsUpload = holder.progressBar;
//            txtProgress = holder.txtProgress;
//            mHandler.sendEmptyMessageDelayed(0, 30);
//        }
//    }
    public void nextFile(int status)
    {
        Intent in = new Intent("progress");
        Intent updateIn = new Intent("update");
//        if (Globals.g_lstUploads.size() <= Globals.g_uploadIndex) {
//            Globals.g_isUpload = false;
//            sendBroadcast(in);
//            sendBroadcast(updateIn);
//            return;
//        }
        if (status == 0) {
            if (Globals.g_lstUploads.get(Globals.g_uploadIndex).mIsUpload == 4)
                Globals.g_lstUploads.get(Globals.g_uploadIndex).mIsUpload = 1;
            else Globals.g_lstUploads.get(Globals.g_uploadIndex).mIsUpload = 3;
        }
        else {
            Globals.g_lstUploads.get(Globals.g_uploadIndex).mIsUpload = 1;
            Globals.g_lstUploads.get(Globals.g_uploadIndex).mUploaded = 1;
            if (Globals.mSetting.isArchiveFile) {
                Globals.g_lstUploads.get(Globals.g_uploadIndex).mUploadTime = String.valueOf(System.currentTimeMillis());
            }
        }
        Globals.g_database.updateRecordFile(Globals.g_lstUploads.get(Globals.g_uploadIndex));
        //adapterUpload.updateModels(Globals.g_lstUploads);

        sendBroadcast(updateIn);
        sendBroadcast(in);
        if (Globals.g_uploadIndex  >= Globals.g_lstUploads.size() - 1)
        {
            Globals.g_isUpload = false;
            sendBroadcast(in);
            sendBroadcast(updateIn);
            return;
        }
        startUploading();
    }


    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter("start_upload");
        this.registerReceiver(uploadService, filter);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy()
    {
        this.unregisterReceiver(this.uploadService);
        super.onDestroy();
    }

}
