/*
 * Copyright (c) 2020. Created By Raj Patil
 */

package com.silentquot.socialcomponents.main.message.imagemessage;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.silentquot.socialcomponents.main.base.BasePresenter;

/**
 * Created by Alexey on 03.05.18.
 */

class ImageDetailPresenter extends BasePresenter<ImageDetailView> {

    ImageDetailPresenter(Context context) {
        super(context);
    }

    public int calcMaxImageSide() {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(displaymetrics);

        int width = displaymetrics.widthPixels;
        int height = displaymetrics.heightPixels;

        return width > height ? width : height;
    }
}
