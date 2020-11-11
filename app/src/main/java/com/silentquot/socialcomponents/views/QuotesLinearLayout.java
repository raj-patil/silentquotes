/*
 * Copyright (c) 2020. Created By Raj Patil
 */

package com.silentquot.socialcomponents.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

public class QuotesLinearLayout extends LinearLayout {

    public QuotesLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public QuotesLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public QuotesLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public QuotesLinearLayout(Context context) {
        super(context);

    }

    int prevX, prevY;
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {

        final LinearLayout.LayoutParams par = (LinearLayout.LayoutParams) getLayoutParams();
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE: {
                par.topMargin += (int) event.getRawY() - prevY;

                prevY = (int) event.getRawY();
                par.leftMargin += (int) event.getRawX() - prevX;

                prevX = (int) event.getRawX();
                setLayoutParams(par);
                return true;
            }
            case MotionEvent.ACTION_UP: {
                par.topMargin += (int) event.getRawY() - prevY;

                par.leftMargin += (int) event.getRawX() - prevX;

                setLayoutParams(par);

                return true;
            }
            case MotionEvent.ACTION_DOWN: {
                prevX = (int) event.getRawX();
                prevY = (int) event.getRawY();
                par.bottomMargin = -2 * getHeight();
                par.rightMargin = -2 * getWidth();
                setLayoutParams(par);
                return true;
            }
        }
        return false;
    }
}
