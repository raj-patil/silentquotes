/*
 * Copyright 2017 Rozdoum
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.silentquot.socialcomponents.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.firebase.storage.StorageReference;
import com.silentquot.R;
import com.silentquot.socialcomponents.enums.UploadImagePrefix;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;


public class ImageUtil {

    public static final String TAG = ImageUtil.class.getSimpleName();

    public static String generateImageTitle(UploadImagePrefix prefix, String parentId) {
        if (parentId != null) {
            return prefix.toString() + parentId;
        }

        return prefix.toString() + new Date().getTime();
    }

    public static String generatePostImageTitle(String parentId) {
        return generateImageTitle(UploadImagePrefix.POST, parentId) + "_" + new Date().getTime();
    }

    public static void loadImage(GlideRequests glideRequests, String url, ImageView imageView) {
        loadImage(glideRequests, url, imageView, DiskCacheStrategy.ALL);
    }

    public static void loadImage(GlideRequests glideRequests, String url, ImageView imageView, DiskCacheStrategy diskCacheStrategy) {
        glideRequests.load(url)
                .diskCacheStrategy(diskCacheStrategy)
                .error(R.drawable.ic_stub)
                .into(imageView);
    }

    public static void loadImage(GlideRequests glideRequests, String url, ImageView imageView,
                                 RequestListener<Drawable> listener) {
        glideRequests.load(url)
                .error(R.drawable.ic_stub)
                .listener(listener)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(imageView);
    }

    public static void loadImageCenterCrop(GlideRequests glideRequests, String url, ImageView imageView,
                                           int width, int height) {
        glideRequests.load(url)
                .centerCrop()
                .override(width, height)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.drawable.ic_stub)
                .into(imageView);
    }

    public static void loadImageCenterCrop(GlideRequests glideRequests, StorageReference imageStorageRef, ImageView imageView,
                                           int width, int height) {
        glideRequests.load(imageStorageRef)
                .centerCrop()
                .override(width, height)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.drawable.ic_stub)
                .into(imageView);
    }

    public static void loadImageCenterCrop(GlideRequests glideRequests, StorageReference imageStorageRef, ImageView imageView,
                                           int width, int height, RequestListener<Drawable> listener) {
        glideRequests.load(imageStorageRef)
                .centerCrop()
                .override(width, height)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.drawable.ic_stub)
                .listener(listener)
                .into(imageView);
    }

    public static void loadMediumImageCenterCrop(GlideRequests glideRequests,
                                                 StorageReference imageStorageRefMedium,
                                                 StorageReference imageStorageRefOriginal,
                                                 ImageView imageView,
                                                 int width,
                                                 int height,
                                                 RequestListener<Drawable> listener) {

        glideRequests.load(imageStorageRefMedium)
                .centerCrop()
                .override(width, height)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.drawable.ic_stub)
                .listener(listener)
                .error(glideRequests.load(imageStorageRefOriginal))
                .into(imageView);
    }

    public static void loadImageCenterCrop(GlideRequests glideRequests, String url, ImageView imageView,
                                           int width, int height, RequestListener<Drawable> listener) {
        int thumbnailSize = 0;
        glideRequests.load(url)
                .centerCrop()
                .override(width, height)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.drawable.ic_stub)
//                .thumbnail(
//                        glideRequests
//                                .load(url)
//                                .override(thumbnailSize)).listener(listener)
                .listener(listener)
                .into(imageView);
    }

    public static void loadImageCenterCrop(GlideRequests glideRequests, StorageReference imageStorageRef, ImageView imageView,
                                           RequestListener<Drawable> listener) {
        glideRequests.load(imageStorageRef)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.drawable.ic_stub)
                .listener(listener)
                .into(imageView);
    }

    public static void loadImageCenterCrop(GlideRequests glideRequests, String url, ImageView imageView,
                                           RequestListener<Drawable> listener) {
        glideRequests.load(url)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.drawable.ic_stub)
                .listener(listener)
                .into(imageView);
    }


    @Nullable
    public static Bitmap loadBitmap(GlideRequests glideRequests, String url, int width, int height) {
        try {
            return glideRequests.asBitmap()
                    .load(url)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .submit(width, height)
                    .get();
        } catch (Exception e) {
            LogUtil.logError(TAG, "getBitmapfromUrl", e);
            return null;
        }
    }

    public static void loadImageWithSimpleTarget(GlideRequests glideRequests, String url, SimpleTarget<Bitmap> simpleTarget) {
        glideRequests.asBitmap()
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .fitCenter()
                .into(simpleTarget);
    }

    public static void loadImageWithSimpleTarget(GlideRequests glideRequests, StorageReference imageStorageRef, SimpleTarget<Bitmap> simpleTarget) {
        glideRequests.asBitmap()
                .load(imageStorageRef)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .fitCenter()
                .into(simpleTarget);
    }

    public static void loadLocalImage(GlideRequests glideRequests, Uri uri, ImageView imageView,
                                      RequestListener<Drawable> listener) {
        glideRequests.load(uri)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .fitCenter()
                .listener(listener)
                .into(imageView);
    }


    public static void loadThumbnailImage(GlideRequests glideRequest, Uri uri, ImageView imageView, RequestListener<Drawable> listener) {

        int thumbnailSize = 64;
        glideRequest
                .load(uri)
                .thumbnail(
                        glideRequest
                                .load(uri)
                                .override(thumbnailSize)).listener(listener)
                .into(imageView);
    }

    private static final float BITMAP_SCALE = 0.4f;
    private static final float BLUR_RADIUS = 7.5f;

    public static Bitmap blur(Context context, Bitmap image) {
        int width = Math.round(image.getWidth() * BITMAP_SCALE);
        int height = Math.round(image.getHeight() * BITMAP_SCALE);

        Bitmap inputBitmap = Bitmap.createScaledBitmap(image, width, height, false);
        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);

        RenderScript rs = RenderScript.create(context);
        ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
        Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);
        theIntrinsic.setRadius(BLUR_RADIUS);
        theIntrinsic.setInput(tmpIn);
        theIntrinsic.forEach(tmpOut);
        tmpOut.copyTo(outputBitmap);

        return outputBitmap;
    }


    public static Bitmap getThumbnail(Context context, Uri uri) throws FileNotFoundException, IOException {
        InputStream input = context.getContentResolver().openInputStream(uri);

        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inDither = true;//optional
        onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();

        if ((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1)) {
            return null;
        }

        int originalSize = (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) ? onlyBoundsOptions.outHeight : onlyBoundsOptions.outWidth;
        int THUMBNAIL_SIZE = 64;
        double ratio = (originalSize > THUMBNAIL_SIZE) ? (originalSize / THUMBNAIL_SIZE) : 1.0;

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
        bitmapOptions.inDither = true; //optional
        bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//
        input = context.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();
        return bitmap;
    }

    private static int getPowerOfTwoForSampleRatio(double ratio) {
        int k = Integer.highestOneBit((int) Math.floor(ratio));
        if (k == 0) return 1;
        else return k;
    }



    public static String getFilename(String folderPath, String imagename) {
//            File file = new File(Environment.getExternalStorageDirectory().getPath(), folderPath + "/images/sent");
        File file = new File(Environment.getExternalStorageDirectory().getPath(), folderPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        String uriSting = (file.getAbsolutePath() + "/" + imagename + ".jpg");
        //System.currentTimeMillis()
        return uriSting;
    }



    public static String saveThumbnail(Bitmap bitmap, String imagename) {

        String stored = null;

        File sdcard = Environment.getExternalStorageDirectory() ;

        String filename = getFilename("SilentQuot" + "/images/sent/.thumbnail" , imagename);
        File file = new File(filename) ;
        if (file.exists())
            return stored ;

        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            stored = "success";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stored;
    }

    public static File getImage(String imagename) {

        File mediaImage = null;
        try {
            String root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(root);
            if (!myDir.exists())
                return null;

            String filename = getFilename("SilentQuot" + "/images/sent/.thumbnail" , imagename);
            mediaImage = new File(filename);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return mediaImage;
    }
    public static boolean checkifImageExists(String imagename) {
        Bitmap b = null;
        File file = ImageUtil.getImage("/" + imagename);
        String path = file.getAbsolutePath();

        if (path != null)
            b = BitmapFactory.decodeFile(path);

        if (b == null || b.equals("")) {
            return false;
        }
        return true;
    }


    // Utils For Downloading chat images --
    public static boolean checkifChatImageExists(String imagename) {
        Bitmap b = null;
        File file = ImageUtil.getChatImage("/" + imagename);
        String path = file.getAbsolutePath();

        if (path != null)
            b = BitmapFactory.decodeFile(path);

        if (b == null || b.equals("")) {
            return false;
        }
        return true;
    }
    public static File getChatImage(String imagename) {

        File mediaImage = null;
        try {
            String root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(root);
            if (!myDir.exists())
                return null;

            String filename = getFilename("SilentQuot" + "/images" , imagename);
            mediaImage = new File(filename);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return mediaImage;
    }


    static String saveChatImages(Bitmap bitmap, String imagename_) {


        String stored = null;

        String filename = getFilename("SilentQuot" + "/images/" , imagename_);
        File file = new File(filename) ;
        if (file.exists())
            return stored ;

        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            stored = "success";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stored;
    }



}
