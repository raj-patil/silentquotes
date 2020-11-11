/*
 * Copyright (c) 2020. Created By Raj Patil
 */

package com.silentquot.socialcomponents.managers.listeners;

import android.net.Uri;


public interface OnUploadedCallback {
    void onUploadSuccess(String uid, Uri downloadUrl, String type);

    void onProgress(double progress);

    void onUploadFailed(Exception e);
}