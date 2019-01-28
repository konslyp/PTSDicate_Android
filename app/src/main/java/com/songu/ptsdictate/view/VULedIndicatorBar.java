// Copyright 2011 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.songu.ptsdictate.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.view.View;

/**
 * This class draws a colorful graphical level indicator similar to an LED VU
 * bar graph.
 * <p/>
 * This is a user defined View UI element that contains a ShapeDrawable, which
 * means it can be placed using in the XML UI configuration and updated
 * dynamically at runtime.
 * <p/>
 * To set the level, use setLevel(level). Level should be in the range [0.0 ;
 * 1.0].
 * <p/>
 * To change the number of segments or colors, change the segmentColors array.
 */
public final class VULedIndicatorBar extends View {

    private double mLevel = 0.0;

    int green = 0xfff0572b;

    int yellow = 0xff6f6663;

    int red = 0xffbdb4b1;

    int segmentOffColor = 0xfff6efed;

    FragmentActivity myContext;

    int[] segmentColors = {green, green, green, green, green, green, green,
            green, green, green, green, green, green, green, green, yellow,
            yellow, yellow, yellow, yellow, red, red, red, red, red};


    public VULedIndicatorBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initBarLevelDrawable();
    }

    public VULedIndicatorBar(Context context) {
        super(context);

        myContext = (FragmentActivity) context;

        initBarLevelDrawable();

    }

    /**
     * Set the bar level. The level should be in the range [0.0 ; 1.0], i.e. 0.0
     * gives no lit LEDs and 1.0 gives full scale.
     *
     * @param level the LED level in the range [0.0 ; 1.0].
     */
    public void setLevel(double level) {
        mLevel = level;
        invalidate();
    }

    public double getLevel() {
        return mLevel;
    }

    private void initBarLevelDrawable() {
        mLevel = 0.0;
    }

    private void drawBar(Canvas canvas) {
        int padding = 2; // Padding on both sides.
        int x = 0;
        int y = 0;

        int width = (int) (Math.floor(getWidth() / segmentColors.length))
                - (1 * padding);
        int height = 60;

        ShapeDrawable mDrawable = new ShapeDrawable(new RectShape());
        for (int i = 0; i < segmentColors.length; i++) {
            x = x + padding;
            if ((mLevel * segmentColors.length) > (i + 0.5)) {
                mDrawable.getPaint().setColor(segmentColors[i]);
            } else {
                mDrawable.getPaint().setColor(segmentOffColor);
            }
            mDrawable.setBounds(x, y, x + width, y + height);
            mDrawable.draw(canvas);
            x = x + width + padding;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawBar(canvas);
    }
}
