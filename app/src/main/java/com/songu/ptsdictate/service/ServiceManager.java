package com.songu.ptsdictate.service;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.rollbar.android.Rollbar;
import com.songu.ptsdictate.doc.Config;
import com.songu.ptsdictate.doc.Globals;
import com.songu.ptsdictate.model.RecordModel;
import com.songu.ptsdictate.model.UserModel;
import com.songu.ptsdictate.util.HttpUtil;
import com.songu.ptsdictate.util.IgnoreSSLTrustManager;
import com.songu.ptsdictate.util.NoSSLv3SocketFactory;
import com.songu.ptsdictate.util.NullHostNameVerifier;
import com.songu.ptsdictate.util.TLSSocketFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

/**
 * Created by Administrator on 2/20/2017.
 */
public class ServiceManager {


    public static Rollbar rollbar = Rollbar.instance();
    public static void serviceLoginNew(final UserModel model,final IServiceResult caller)
    {
        new Thread() {
            public void run() {
                HashMap<String, String> aResultList = new HashMap<String, String>();

                // Create a new HttpClient and Post Header
                String aUrlString = Config.mLoginUrl;

                Log.i("URL----->>>", aUrlString);
                try {

                    SSLContext context = null;
                    try {
                        context = SSLContext.getInstance("TLS");
                    } catch (NoSuchAlgorithmException e2) {
                        // TODO Auto-generated catch block
                        e2.printStackTrace();
                    }

                    TrustManager[] tmlist = {new IgnoreSSLTrustManager()};

                    try {
                        context.init(null, tmlist, new SecureRandom());

                    } catch (KeyManagementException e2) {
                        // TODO Auto-generated catch block
                        e2.printStackTrace();
                    }

                    HttpsURLConnection
                            .setDefaultHostnameVerifier(new NullHostNameVerifier());

                    HttpsURLConnection.setDefaultSSLSocketFactory(context
                            .getSocketFactory());

                    HashMap<String, String> aParam = new HashMap<String, String>();
                    aParam.put("Login_Name", model.mID);
                    aParam.put("Password", model.mPassword);
                    URL urlToRequest = new URL(aUrlString);

                    HttpsURLConnection urlConnection = null;
                    try {
                        urlConnection = (HttpsURLConnection) urlToRequest
                                .openConnection();
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                        caller.onResponse(402);
                        return;
                    }
                    urlConnection.setConnectTimeout(900000);
                    urlConnection.setReadTimeout(900000);
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setDoInput(true);
                    urlConnection.setDoOutput(true);

                    OutputStream os = urlConnection.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                            os, "UTF-8"));
                    try {
                        writer.write(getPostDataString(aParam));
                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        rollbar.error(e);
                        e.printStackTrace();
                        caller.onResponse(402);
                        return;
                    } catch (final IOException e) {
                        // TODO Auto-generated catch block
//                        ((Activity)caller).runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                Toast.makeText((Activity)caller,e.getMessage(),Toast.LENGTH_SHORT).show();
//                            }
//                        });
                        rollbar.error(e);
                        e.printStackTrace();
                        caller.onResponse(402);
                        return;
                    }

                    writer.flush();
                    writer.close();
                    os.close();

                    int statusCode = urlConnection.getResponseCode();

                    Log.e("code", "" + statusCode);

                    InputStream myInputStream = null;
                    myInputStream = new BufferedInputStream(
                            urlConnection.getInputStream());

                    String aResult = StringReader(myInputStream);

                    try {
                        JSONObject localJSONObject1 = new JSONObject(aResult);
                        if (localJSONObject1.has("response_msg")) {
                            String response = localJSONObject1.getString("response_msg");

                            if (response.equals("valid")) {
                                UserModel uModel = new UserModel();
                                uModel.mNo = localJSONObject1.getString("user_id");
                                uModel.mName = localJSONObject1.getString("user_name");
                                uModel.mEmail = localJSONObject1.getString("email");
                                uModel.mPrivilege = localJSONObject1.getString("privilege");
                                uModel.mPassword = model.mPassword;
                                uModel.mID = model.mID;
                                Globals.mAccount = uModel;
                                caller.onResponse(200);
                                rollbar.debug("Success Login");
                            } else {
                                caller.onResponse(400);
                                rollbar.debug("Fail to Login");
                            }
                        }

                    } catch (final Exception e) {
                        rollbar.error(e);
                        e.printStackTrace();
                        //Toast.makeText((Context) caller,e.getMessage(),Toast.LENGTH_SHORT).show();
//                        ((Activity)caller).runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                Toast.makeText((Activity)caller,e.getMessage(),Toast.LENGTH_SHORT).show();
//                            }
//                        });
                        caller.onResponse(402);
                    }
                } catch (final IOException e1) {
                    // TODO Auto-generated catch block
//                    ((Activity) caller).runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText((Activity) caller, e1.getMessage(), Toast.LENGTH_SHORT).show();
//                        }
//                    });
                    rollbar.error(e1);
                    e1.printStackTrace();
                    caller.onResponse(402);
                }
            }
        }.start();

    }

    private static String StringReader(InputStream myInputStream2) {
        // TODO Auto-generated method stub
        String aLine = null;
        String aResult = null;
        try {
            BufferedReader aReader = new BufferedReader(new InputStreamReader(
                    myInputStream2, "iso-8859-1"), 8);
            StringBuilder aStringBuilder = new StringBuilder();

            while ((aLine = aReader.readLine()) != null) {

                aStringBuilder.append(aLine + "\n");
            }

            myInputStream2.close();
            aResult = aStringBuilder.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return aResult;

    }
    private static String getPostDataString(HashMap<String, String> params)
            throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    public static void serviceLogin(final UserModel model, final IServiceResult caller)
    {
        String url = Config.mLoginUrl;
        RequestParams param = new RequestParams();
        param.put("Login_Name",model.mID);
        param.put("Password",model.mPassword);

        HttpUtil.post(url,param,new AsyncHttpResponseHandler() {
            public void onFailure(Throwable paramThrowable) {
                String s = "fail";
                Toast.makeText((Context) caller,paramThrowable.getMessage(),Toast.LENGTH_SHORT).show();
                caller.onResponse(401);
            }
            public void onFinish() {
                String s = "finish";
            }
            public void onSuccess(String paramString) {  //that is return when success..
                try {
                    JSONObject localJSONObject1 = new JSONObject(paramString);
                    if (localJSONObject1.has("response_msg")) {
                        String response = localJSONObject1.getString("response_msg");

                        if (response.equals("valid")) {
                            UserModel uModel = new UserModel();
                            uModel.mNo = localJSONObject1.getString("user_id");
                            uModel.mName = localJSONObject1.getString("user_name");
                            uModel.mEmail = localJSONObject1.getString("email");
                            uModel.mPrivilege = localJSONObject1.getString("privilege");
                            uModel.mPassword = model.mPassword;
                            uModel.mID = model.mID;
                            Globals.mAccount = uModel;
                            caller.onResponse(200);
                        } else {
                            caller.onResponse(400);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText((Context) caller,e.getMessage(),Toast.LENGTH_SHORT).show();
                    caller.onResponse(402);
                }

            }
        });
    }

    public static void serviceUpdateProfile(final IServiceResult caller)
    {
        String url = Config.mLoginUrl;
        RequestParams param = new RequestParams();
        param.put("Login_Name", Globals.mAccount.mID);
        param.put("Password", Globals.mAccount.mPassword);
        param.put("User_Name", Globals.mAccount.mName);
        param.put("Email", Globals.mAccount.mEmail);

        HttpUtil.post(url,param,new AsyncHttpResponseHandler() {
            public void onFailure(Throwable paramThrowable) {
                String s = "fail";
                caller.onResponse(400);
            }
            public void onFinish() {
                String s = "finish";
            }
            public void onSuccess(String paramString) {  //that is return when success..
                try {
                    JSONObject localJSONObject1 = new JSONObject(paramString);
                    if (localJSONObject1.has("response_msg")) {
                        String response = localJSONObject1.getString("response_msg");

                        if (response.equals("valid")) {
                            UserModel uModel = new UserModel();
                            uModel.mNo = localJSONObject1.getString("user_id");
                            uModel.mName = localJSONObject1.getString("user_name");
                            uModel.mEmail = localJSONObject1.getString("email");
                            uModel.mPrivilege = localJSONObject1.getString("privilege");
                            uModel.mPassword = Globals.mAccount.mPassword;
                            uModel.mID = Globals.mAccount.mID;
                            Globals.mAccount = uModel;
                            caller.onResponse(200);
                        } else {
                            caller.onResponse(400);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    caller.onResponse(400);
                }

            }
        });
    }


    public static void serviceUploadFile(final RecordModel model,final IServiceResult caller)
    {
        new Thread()
        {
            public void run()
            {
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
                int maxBufferSize = 1 * 1024;
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
                        dos.write(buffer, 0, (int) bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = (int) Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                        aProgressUpdate = ((float) (aProgressUpdate + bufferSize));
                        aPercentage = String
                                .valueOf(new DecimalFormat("##")
                                        .format((aProgressUpdate / (aTotalBytes + (aTotalBytes / 30))) * 100));

                        Log.e("Percent Upload",aPercentage);

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
                                    caller.onResponse(200);
                                } else {
                                    caller.onResponse(400);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                caller.onResponse(400);
                            }
                            Log.e("Debug", "Server Response " + str1);
                        }
                        inStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        caller.onResponse(400);
                    }
                } catch (MalformedURLException ex) {
                    Log.e("WebService", "MalformedURLException: " + ex.getMessage(), ex);
                    caller.onResponse(400);
                } catch (IOException ioe) {
                    Log.e("WebService", "IOException: " + ioe.getMessage(), ioe);
                    caller.onResponse(400);
                } catch (NoSuchAlgorithmException e1) {
                    e1.printStackTrace();
                }
            }
        }.start();
    }
//    public static void serviceUploadFile(RecordModel model, final IServiceResult caller)
//    {
//        String url = Config.mUploadUrl;
//        RequestParams param = new RequestParams();
//        param.put("Login_Name", Globals.mAccount.mID);
//        param.put("File_Desc", model.mComment);
//        param.put("To_User", "pts");
//        param.put("Email_Notification", "0");
//        File file = new File(model.mPath);
//        try {
//            param.put("name",file);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//
//        HttpUtil.post(url,param,new AsyncHttpResponseHandler() {
//            public void onFailure(Throwable paramThrowable) {
//                String s = "fail";
//                caller.onResponse(400);
//            }
//            public void onFinish() {
//                String s = "finish";
//            }
//            public void onSuccess(String paramString) {  //that is return when success..
//                try {
//                    JSONObject localJSONObject1 = new JSONObject(paramString);
//                    if (localJSONObject1.has("response_msg")) {
//                        String response = localJSONObject1.getString("response_msg");
//
//                        if (response.equals("Success")) {
//                            caller.onResponse(200);
//                        } else {
//                            caller.onResponse(400);
//                        }
//                    }
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    caller.onResponse(400);
//                }
//
//            }
//        });
//    }
}
