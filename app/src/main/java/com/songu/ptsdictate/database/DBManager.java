package com.songu.ptsdictate.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import com.songu.ptsdictate.doc.Globals;
import com.songu.ptsdictate.model.RecordModel;
import com.songu.ptsdictate.util.Utils;

import java.util.ArrayList;
import java.util.List;


public class DBManager {
    SQLiteDatabase database;

    String createRecord = "create table recordlist (id integer primary key AUTOINCREMENT ,autosave integer,name string,file_sz string,elapse string,path string,isupload integer,server_id integer,cmt string,iscomment integer,uploadtime string,idx string,rate integer,upload integer);";

    String dbname =  Utils.filterName(Globals.mAccount.mID);
    UsageSQLLite helper;
    SQLiteDatabase db;
    Context mContext;

    public DBManager() {

    }

    public DBManager(Context con) {
        this.mContext = con;
        helper = new UsageSQLLite(con, dbname, null, 1);
    }

    public boolean isNameExist(String name)
    {
        db = helper.getReadableDatabase();
        String[] query = new String[1];
        query[0] = name + ".wav";
        Cursor c = db.rawQuery("SELECT * FROM `recordlist` where name=? COLLATE NOCASE",query);

        c.moveToFirst();
        if (c.getCount() > 0) {
            return true;
        }
        else return false;
    }
    public RecordModel getLastInsertFile()
    {
        List<RecordModel> records = getAllRecordlist();
        if (records.size() > 0)
            return records.get(0);
        return null;
    }
    public void insertRecordFile(RecordModel model) {
        db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("autosave", model.mIsAutoSave);
        values.put("name", model.mName);
        values.put("file_sz", model.mSize);
        values.put("elapse", model.mElapse);
        values.put("path", model.mPath);
        values.put("server_id", model.mNo);
        values.put("isupload", model.mIsUpload);
        values.put("cmt", model.mComment);
        values.put("iscomment", model.isComment);
        values.put("uploadtime",model.mUploadTime);
        values.put("idx",model.mIndexData);
        values.put("rate",model.mRate);
        values.put("upload",model.mUploaded);
        db.insert("recordlist", null, values);

    }

    public void updateRecordFile(RecordModel model) {
        db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("autosave", model.mIsAutoSave);
        values.put("name", model.mName);
        values.put("file_sz", model.mSize);
        values.put("elapse", model.mElapse);
        values.put("path", model.mPath);
        values.put("server_id", model.mNo);
        values.put("isupload", model.mIsUpload);
        values.put("cmt", model.mComment);
        values.put("iscomment", model.isComment);
        values.put("uploadtime",model.mUploadTime);
        values.put("idx",model.mIndexData);
        values.put("rate",model.mRate);
        values.put("upload",model.mUploaded);
        String[] query = new String[1];
        query[0] = String.valueOf(model.mLocalNo);
        db.update("recordlist", values, "id=?", query);
    }

    public void deleteRecordFile(int id) {
        db = helper.getWritableDatabase();
        String[] values = new String[1];
        values[0] = String.valueOf(id);
        db.delete("recordlist", "id=?", values);
    }
    public List<RecordModel> getAllRecordlist()
    {
        db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM `recordlist` order by id desc",null);
        List<RecordModel> list = new ArrayList<RecordModel>();
        c.moveToFirst();
        if (c.getCount() > 0) {
            do {
                RecordModel temp = new RecordModel();
                temp.mLocalNo = c.getInt(c.getColumnIndex("id"));
                temp.mName = c.getString(c.getColumnIndex("name"));
                temp.mElapse = c.getString(c.getColumnIndex("elapse"));
                temp.mPath = c.getString(c.getColumnIndex("path"));
                temp.mSize = c.getString(c.getColumnIndex("file_sz"));
                temp.mIsAutoSave = c.getInt(c.getColumnIndex("autosave"));
                temp.mNo = c.getInt(c.getColumnIndex("server_id"));
                temp.mComment = c.getString(c.getColumnIndex("cmt"));
                temp.isComment = c.getInt(c.getColumnIndex("iscomment"));
                temp.mUploadTime = c.getString(c.getColumnIndex("uploadtime"));
                temp.mIndexData = c.getString(c.getColumnIndex("idx"));
                temp.mRate = c.getInt(c.getColumnIndex("rate"));
                temp.mUploaded = c.getInt(c.getColumnIndex("upload"));
                temp.isSelect = false;
                temp.mIsUpload = c.getInt(c.getColumnIndex("isupload"));

                list.add(temp);
            }while(c.moveToNext());
            c.close();
            return list;
        }
        else return list;
    }

    public class UsageSQLLite extends SQLiteOpenHelper
    {
        public UsageSQLLite(Context con,String name,CursorFactory factory,int version)
        {
            super(con,name,factory,version);
        }

        @Override
        public void onCreate(SQLiteDatabase arg0) {
            // TODO Auto-generated method stub
            arg0.execSQL(createRecord);
        }

        @Override
        public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub

        }
    }
}
