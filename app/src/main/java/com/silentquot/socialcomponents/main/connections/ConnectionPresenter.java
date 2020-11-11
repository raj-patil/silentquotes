/*
 * Copyright (c) 2020. Created By Raj Patil
 */

package com.silentquot.socialcomponents.main.connections;

import android.app.Activity;

import com.silentquot.socialcomponents.main.base.BasePresenter;

public class ConnectionPresenter extends BasePresenter<ConnectionView> {

    private Activity activity;

    ConnectionPresenter(Activity activity) {
        super(activity);
        this.activity = activity;
    }
}
