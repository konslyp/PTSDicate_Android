package com.songu.ptsdictate.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.songu.ptsdictate.R;


/**
 *
 */
public class BookmarkView extends LinearLayout {

    private Context myContext;

    private View rootview;

    private LinearLayout myBookmarkLay, myBookmarkLay1;

    private HorizontalScrollView mScrollView;

    private int myCount;

    private LinearLayout[] myBookmarkLays;

    private String DEFAULT_COLOR = "#000000";

    private String HIGHLIGHTED = "#FFF72F0D";

    private int HiglightedPos = 0;

    TextView mySecTXT;

    public BookmarkView(Context context, AttributeSet attrs) {
        super(context, attrs);


        final TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.BookmarksView,
                0, 0);


        final int N = a.getIndexCount();
        for (int i = 0; i < N; ++i) {
            int attr = a.getIndex(i);
            switch (attr) {
                case R.styleable.BookmarksView_bookmarksCount: {
                    if (context.isRestricted()) {
                        throw new IllegalStateException("The " + getClass().getCanonicalName() + ":required attribute cannot "
                                + "be used within a restricted context");
                    }

                    int defaultValue = 0;
                    final int required = a.getInteger(attr, 0);
                    //DO SOMETHING

                    this.myCount = required + 1;
                }
                break;
            }

        }

        a.recycle();


        this.myContext = context;


        myBookmarkLays = new LinearLayout[myCount];

        ClassandWidgetInitialise();

        this.invalidate();

        this.addView(rootview);

    }

//    public PTSBookmarkView(Context context, int aCount) {
//        super(context);
//
//        this.myContext = context;
//
//        this.myCount = aCount + 1;
//
//        myBookmarkLays = new LinearLayout[myCount];
//
//        ClassandWidgetInitialise();
//
//        reset();
//
//        this.invalidate();
//
//
//    }

    private void ClassandWidgetInitialise() {

        LayoutInflater inflater = (LayoutInflater) myContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        rootview = inflater.inflate(R.layout.inflate_bookmark_bar, null);

        mScrollView = (HorizontalScrollView) rootview.findViewById(R.id.horizontal_scrollbar);

        myBookmarkLay = (LinearLayout) rootview.findViewById(R.id.bookmarks_LAY);

        LinearLayout LAY_0 = (LinearLayout) rootview.findViewById(R.id.Lay_0);

        LinearLayout LAY_1 = (LinearLayout) rootview.findViewById(R.id.Lay_1);

        mySecTXT = (TextView) rootview.findViewById(R.id.bookmark_TXT_1);

        myBookmarkLays[0] = LAY_0;

        // myBookmarkLays[1] = LAY_1;

        for (int i = 1; i < myCount; i++) {

            View aLinearLay = inflater.inflate(R.layout.inflate_bookmark, null);

            myBookmarkLays[i] = (LinearLayout) aLinearLay;

            myBookmarkLay.addView(aLinearLay);

        }

    }

    public void setMarkerSelection() {

        mySecTXT.setVisibility(VISIBLE);
    }

    public void setSelected(int pos) {

        if (pos > 0) {

            myBookmarkLays[0].setVisibility(View.INVISIBLE);

            resetAllViewSelection();

            this.HiglightedPos = pos;

            myBookmarkLays[pos].getChildAt(0).setBackgroundColor(Color
                    .parseColor(HIGHLIGHTED));

        } else {

            resetAllViewSelection();

            this.HiglightedPos = pos;

            myBookmarkLays[0].getChildAt(0).setBackgroundColor(Color
                    .parseColor(HIGHLIGHTED));

            myBookmarkLays[0].setVisibility(View.VISIBLE);
        }

        this.invalidate();

        CheckVisibilityOftheViewandMove(myBookmarkLays[getSelected()].getChildAt(0));


    }

    private void CheckVisibilityOftheViewandMove(View aView) {
        /**
         * If the view is not in the visible region of the scrollview programatically scroll left / right the scrollview
         */

        if (!isViewVisible(aView)) {
            int x = (int) myBookmarkLays[getSelected()].getX() + myBookmarkLays[getSelected()].getWidth();
            int y = mScrollView.getScrollY();


            Rect aRect = new Rect();
            mScrollView.getDrawingRect(aRect);
            if (x > (aRect.right)) {
                mScrollView.scrollTo(x - mScrollView.getWidth(), y);
            } else {
                if (x >= 50) {
                    mScrollView.scrollTo(x - myBookmarkLays[getSelected()].getWidth(), y);
                } else {
                    mScrollView.scrollTo(x, y);
                }
            }

        }

    }

    public void getHitRect(Rect outRect) {
        outRect.set(10, 10, 10, 10);
    }


    private boolean isViewVisible(View view) {
        Rect scrollBounds = new Rect();
        mScrollView.getHitRect(scrollBounds);


        float left = view.getX();
        float right = left + view.getWidth();

        if (view.getLocalVisibleRect(scrollBounds)) {
            return true;
        } else {
            return false;
        }
    }


    private void resetAllViewSelection() {

        for (int i = 0; i < myCount; i++) {

            myBookmarkLays[i].getChildAt(0).setBackgroundColor(Color
                    .parseColor(DEFAULT_COLOR));

        }

        this.invalidate();


    }

    public void addBookmark(final int pos, String aSec) {
        myBookmarkLays[pos].setVisibility(View.VISIBLE);
        TextView aSecsTXT = (TextView) myBookmarkLays[pos].getChildAt(1);
        aSecsTXT.setText(aSec);
        this.invalidate();
        CheckVisibilityOftheViewandMove(myBookmarkLays[pos].getChildAt(0));
    }

    /**
     * Remove a Bookmark
     *
     * @param pos
     */
    public void removeBookmark(int pos) {

        //myBookmarkLay.removeView(myBookmarkLays[pos]);
        int flag = 0;
        for (int i = pos; i < myBookmarkLays.length - 1;i++)
        {
            if (myBookmarkLays[i].getVisibility() == INVISIBLE)
            {
                flag = 1;
                myBookmarkLays[i - 1].setVisibility(View.INVISIBLE);
            }
            //myBookmarkLays[i] = myBookmarkLays[i + 1];

            TextView aSecTxt1 = (TextView) myBookmarkLays[i + 1].getChildAt(1);
            TextView aSecsTXT = (TextView) myBookmarkLays[i].getChildAt(1);
            aSecsTXT.setText(aSecTxt1.getText());
        }
        if (flag == 0)
        {
            if (myBookmarkLays[myBookmarkLays.length - 1].getVisibility() != View.INVISIBLE)
                myBookmarkLays[myBookmarkLays.length - 1].setVisibility(View.INVISIBLE);
            else
                myBookmarkLays[myBookmarkLays.length - 2].setVisibility(View.INVISIBLE);
        }
//        myBookmarkLays[pos].setVisibility(View.INVISIBLE);
//        TextView aSecsTXT = (TextView) myBookmarkLays[pos].getChildAt(1);
//        aSecsTXT.setText("");
        this.invalidate();

    }

    public int getSelected() {

        return HiglightedPos;
    }

    public void reset() {

        for (int i = 0;i < myBookmarkLays.length;i++)
        {
            myBookmarkLays[i].setVisibility(View.INVISIBLE);
        }
        myBookmarkLays[0].setVisibility(View.VISIBLE);

        resetAllViewSelection();

        setSelected(0);

    }

    public void resetAllViews() {
        // TODO Auto-generated method stub
        for (int i = 1; i < myCount; i++) {

            myBookmarkLays[i].getChildAt(0).setBackgroundColor(Color
                    .parseColor(DEFAULT_COLOR));

            TextView aSecsTXT = (TextView) myBookmarkLays[i].getChildAt(1);

            aSecsTXT.setText("");

            myBookmarkLays[i].setVisibility(View.INVISIBLE);

        }

        myBookmarkLays[0].setVisibility(VISIBLE);


        this.invalidate();
    }


}
