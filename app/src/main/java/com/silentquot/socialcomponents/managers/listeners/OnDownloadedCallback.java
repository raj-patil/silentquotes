/*
 * Copyright (c) 2020. Created By Raj Patil
 */

package com.silentquot.socialcomponents.managers.listeners;

public interface OnDownloadedCallback {

    void onSucussfullDownload( boolean sucuss);

    void onDownloadProgress();

    void onDownloadFail(boolean fail);

}
