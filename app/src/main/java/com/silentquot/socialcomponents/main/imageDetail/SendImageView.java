/*
 * Copyright (c) 2020. Created By Raj Patil
 */

package com.silentquot.socialcomponents.main.imageDetail;

import android.net.Uri;

import com.silentquot.socialcomponents.main.base.BaseView;

public interface SendImageView extends BaseView {


    Uri getImageUri();
    public String getReceiver();
}
