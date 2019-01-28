package com.songu.ptsdictate.doc;

import com.songu.ptsdictate.database.DBManager;
import com.songu.ptsdictate.model.RecordModel;
import com.songu.ptsdictate.model.SettingModel;
import com.songu.ptsdictate.model.UserModel;
import com.songu.ptsdictate.util.RecordUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 1/12/2018.
 */

public class Globals {

    public static DBManager g_database;
    public static UserModel mAccount = new UserModel();
    public static SettingModel mSetting = new SettingModel();
    public static Enums.MODE e_mode =  Enums.MODE.LIBRARY;
    public static Enums.EIDTMODE e_editMode = Enums.EIDTMODE.NONE;
    public static RecordUtil g_recordUtil = new RecordUtil();
    public static int g_fileIndex = 1;
    public static boolean g_isExistRecord = false;
    public static RecordModel g_existFile = new RecordModel();
    public static List<RecordModel> g_lstUploads = new ArrayList<>();
    public static boolean g_loginFirst = false;
    public static int g_selectLibraryItemNo;
    public static boolean g_isUpload = false;
    public static int g_uploadIndex = -1;
}
