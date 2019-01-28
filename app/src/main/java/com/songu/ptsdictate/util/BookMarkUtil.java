package com.songu.ptsdictate.util;

import android.content.Context;
import android.widget.Button;

import com.songu.ptsdictate.activity.MainActivity;
import com.songu.ptsdictate.doc.Globals;
import com.songu.ptsdictate.fragment.RecordFragment;
import com.songu.ptsdictate.view.BookmarkView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONStringer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 4/9/2018.
 */

public class BookMarkUtil {

    private Context mContext;
    private BookmarkView bookmarkView;
    private int bookMarkCount = 1;
    private List<String> lstBookMarks = new ArrayList<>();
    private int selectedBookMark = -1;
    private Button btnBookMarkClear;



    public void setClearButton(Button clear)
    {
        this.btnBookMarkClear = clear;
    }
    public BookMarkUtil(Context context)
    {
        mContext = context;
    }

    public int getSelectedBookMark()
    {
        return selectedBookMark;
    }
    public int getBookMarkCount()
    {
        return bookMarkCount;
    }

    public void setBookMarkView(BookmarkView bView)
    {
        this.bookmarkView = bView;
    }
    public void clearBookMarks()
    {
        lstBookMarks.clear();
        bookmarkView.resetAllViews();
        selectedBookMark = -1;
        bookMarkCount = 1;
    }
    public boolean isAlreadyIndex(long pos)
    {
        for (int i = 0;i < lstBookMarks.size();i++)
        {
            if (Utils.simpleTimeString(Long.parseLong(lstBookMarks.get(i))).equals(Utils.simpleTimeString(pos)))
            {
                return true;
            }
        }
        return false;
    }
    public void selectBookMark(long pos)
    {
        if (btnBookMarkClear != null)
            btnBookMarkClear.setEnabled(false);
        for (int i = 0;i < lstBookMarks.size();i++)
        {
            if (Utils.simpleTimeString(Long.parseLong(lstBookMarks.get(i))).equals(Utils.simpleTimeString(pos)))
            {
                this.selectedBookMark = i;
                if (((MainActivity)mContext).currentFragment instanceof RecordFragment)
                {
                    ((RecordFragment)(((MainActivity)mContext).currentFragment)).selectBookMark();
                }
                bookmarkView.setSelected(this.selectedBookMark + 1);
                if (btnBookMarkClear != null)
                    btnBookMarkClear.setEnabled(true);
            }
        }
    }
    public long selectNext()
    {
        if (selectedBookMark < bookMarkCount - 1)
            this.selectedBookMark++;
        bookmarkView.setSelected(this.selectedBookMark + 1);
        return Long.parseLong(lstBookMarks.get(selectedBookMark));
    }
    public long selectBack()
    {
        if (selectedBookMark > -1)
            this.selectedBookMark--;
        bookmarkView.setSelected(this.selectedBookMark + 1);
        if (selectedBookMark == -1)
            return 0;
        return Long.parseLong(lstBookMarks.get(selectedBookMark));
    }
    public void removeBookMark()
    {
        if (this.selectedBookMark == -1) return;
        bookmarkView.removeBookmark(this.selectedBookMark + 1);
        lstBookMarks.remove(this.selectedBookMark);
        this.bookMarkCount--;
        selectedBookMark = -1;
//        if (bookMarkCount - 1 <= this.selectedBookMark)
//        {
//            selectedBookMark--;
//        }
        bookmarkView.setSelected(this.selectedBookMark + 1);
    }
    public void deleteOverDurationMark(int duration)
    {
        int i = 0;
        while(i < lstBookMarks.size())
        {
            if ((Long.parseLong(lstBookMarks.get(i)) > duration))
            {
                this.selectedBookMark = i;
                removeBookMark();
                //lstBookMarks.remove(i);
                i = 0;
            }
            else
                i++;
        }
        this.selectedBookMark = -1;
        sortAndRedraw();
    }
    public void partialDeleteMark(int start,int end)
    {
        int i = 0;
        while(i < lstBookMarks.size())
        {
            if ((Long.parseLong(lstBookMarks.get(i)) >= start) && (Long.parseLong(lstBookMarks.get(i)) <= end))
            {
                this.selectedBookMark = i;
                removeBookMark();
                //lstBookMarks.remove(i);
                i = 0;
            }
            else i++;
        }
        for (i = 0;i < lstBookMarks.size();i++)
        {
            if (Long.parseLong(lstBookMarks.get(i)) > end)
            {
                long timeMillis = Long.parseLong(lstBookMarks.get(i)) - end + start;
                lstBookMarks.set(i, String.valueOf(timeMillis));
            }
        }
        this.selectedBookMark = -1;
        sortAndRedraw();
    }
    public String generateIndexJson()
    {
        JSONArray jsonIndex = new JSONArray();
        for (int i = 0;i < lstBookMarks.size();i++)
        {
            jsonIndex.put(lstBookMarks.get(i));
        }
        return jsonIndex.toString();
    }
    public List<String> getBookMarks()
    {
        return lstBookMarks;
    }
    public List<String> setJsonData(String data)
    {
        this.bookMarkCount = 1;
        try {
            lstBookMarks.clear();
            JSONArray jsonIndex = new JSONArray(data);
            for (int i = 0;i < jsonIndex.length();i++)
            {
                String time = jsonIndex.getString(i);
                lstBookMarks.add(time);
                this.bookMarkCount++;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return lstBookMarks;
    }
    public boolean addBookMark(long currentPos)
    {
        if (Utils.simpleTimeString(currentPos).equals("0s"))
        {
            return false;
        }
        if (isAlreadyIndex(currentPos))
        {
            return false;
        }
        lstBookMarks.add(String.valueOf(currentPos));
        sortAndRedraw();
        return true;
    }
    public void sortAndRedraw()
    {
        for (int i = 0;i < lstBookMarks.size() - 1;i++)
        {
            for (int j = i + 1;j < lstBookMarks.size();j++)
            {
                if (Long.parseLong(lstBookMarks.get(i)) > Long.parseLong(lstBookMarks.get(j)))
                {
                    String ss = lstBookMarks.get(i);
                    String tt = lstBookMarks.get(j);
                    lstBookMarks.set(i,tt);
                    lstBookMarks.set(j,ss);
                }
            }
        }
        bookmarkView.reset();
        bookMarkCount = 1;
        for (int i = 0;i < lstBookMarks.size();i++) {
            bookmarkView.addBookmark(i + 1,Utils.simpleTimeString(Long.parseLong(lstBookMarks.get(i))));
            bookMarkCount++;
        }
    }
}
