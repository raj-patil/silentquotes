/*
 * Copyright (c) 2020. Created By Raj Patil
 */

package com.silentquot.socialcomponents.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.silentquot.socialcomponents.managers.listeners.OnDownloadedCallback;

import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;


public  class DownloadChatImages extends AsyncTask<Object, Object, Object> {
        private String requestUrl, imagename_;
        private ImageView view;
        private Bitmap bitmap;
        private FileOutputStream fos;
        private  OnDownloadedCallback callback;
        public DownloadChatImages(String requestUrl, ImageView view, String _imagename_, OnDownloadedCallback onDownloadedCallback) {
            this.requestUrl = requestUrl;
            this.view = view;
            this.imagename_ = _imagename_;
            this.callback = onDownloadedCallback;
        }

        @Override
        protected Object doInBackground(Object... objects) {
            try {
                URL url = new URL(requestUrl);
                URLConnection conn = url.openConnection();
                bitmap = BitmapFactory.decodeStream(conn.getInputStream());
            } catch (Exception ex) {
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            if (!ImageUtil.checkifChatImageExists(imagename_)) {
                view.setImageBitmap(bitmap);

                ImageUtil.saveChatImages(bitmap, imagename_);
                callback.onSucussfullDownload(true);

            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            callback.onDownloadFail(false);
        }

        @Override
        protected void onProgressUpdate(Object... values) {
            super.onProgressUpdate(values);
            callback.onDownloadProgress();
        }
    }

