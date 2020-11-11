/*
 * Copyright (c) 2020. Created By Raj Patil
 */

package com.silentquot.socialcomponents.main.imageDetail;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.silentquot.R;
import com.silentquot.socialcomponents.main.pickImageBase.PickImageActivity;
import com.silentquot.socialcomponents.main.pickImageBase.PickImageView;
import com.silentquot.socialcomponents.views.TouchImageView;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.Serializable;

public class SendImageActivity<V extends SendImageView & PickImageView, P extends SendImagePresenter<V>> extends PickImageActivity<V, P> implements SendImageView , Serializable {

    public static final int SEND_IMAGE_REQUEST =51 ;
    private static final String TAG = ImageDetailActivity.class.getSimpleName();

    public static final String IMAGE_TITLE_EXTRA_KEY = "ImageDetailActivity.IMAGE_TITLE_EXTRA_KEY";
    private ViewGroup viewGroup;
    private TouchImageView touchImageView;
    private ProgressBar progressBar;
    private FloatingActionButton imagesendbutton;
    private String RECEIVER_ID = "receiverId";
    public static  final String SENDING_MESSAGE_EXTRA_KEY="SendImageActivity.SendingMessageExtraKey";
    private String IMAGE_URI_FROM_SEND_IMAGE_ACTIVITY ="imageuri";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_image_message);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        touchImageView = findViewById(R.id.touchImageView);
        progressBar = findViewById(R.id.progressBar);
        viewGroup = findViewById(R.id.image_detail_container);
        imagesendbutton = findViewById(R.id.btn_send);
        initActionBar();

//        String imageTitle = getIntent().getStringExtra(IMAGE_TITLE_EXTRA_KEY);
        onSelectImageClick(touchImageView);

        touchImageView.setOnClickListener(v -> {
            final int vis = viewGroup.getSystemUiVisibility();
            if ((vis & View.SYSTEM_UI_FLAG_LOW_PROFILE) != 0) {
                viewGroup.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            } else {
                viewGroup.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
            }
        });

        initlistner();
    }

    private void initlistner() {
        imagesendbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            //    presenter.onImageSendClick();

                if (imageUri!=null)
                {
                    Intent intent = new Intent();
                    String uri = String.valueOf(getImageUri());
                    intent.putExtra(IMAGE_URI_FROM_SEND_IMAGE_ACTIVITY , uri);
                    setResult(RESULT_OK , intent);
                    finish();
                }
//                Intent intent = new Intent();
//                Message message = new Message();
//                message.setReceiver(getReceiver());
//                message.setMessage(getImageUri().toString());
//               // message.setLocalImageUri(getImageUri());
//                message.setMsgtype("Image");
//                message.setIsseen(false);
//                message.setStatus("sending");
//
//                setResult(RESULT_OK, intent.putExtra(SENDING_MESSAGE_EXTRA_KEY, message));
//                LogUtil.logDebug(TAG, "setResult " + message.getMessage());


            }
        });
    }

    @NonNull
    @Override
    public P createPresenter() {
        if (presenter == null) {
            return (P) new SendImagePresenter(this);
        }
        return presenter;
    }

    private void initActionBar() {
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);

            viewGroup.setOnSystemUiVisibilityChangeListener(
                    vis -> {
                        if ((vis & View.SYSTEM_UI_FLAG_LOW_PROFILE) != 0) {
                            actionBar.hide();
                        } else {
                            actionBar.show();
                        }
                    });

            // Start low profile mode and hide ActionBar
            viewGroup.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
            actionBar.hide();
        }
    }

    @SuppressLint("NewApi")
    public void onSelectImageClick(View view) {
        if (CropImage.isExplicitCameraPermissionRequired(this)) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE);
        } else {
            CropImage.startPickImageActivity(this);
        }
    }

    @Override
    @SuppressLint("NewApi")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // handle result of pick image chooser
        super.onActivityResult(requestCode, resultCode, data);

        handleCropImageResult(requestCode, resultCode, data);
    }

    @Override
    protected ProgressBar getProgressView() {
        return progressBar;
    }

    @Override
    protected ImageView getImageView() {
        return touchImageView;
    }

    @Override
    protected void onImagePikedAction() {

        loadImageToImageView(imageUri);

        //startCropImageActivity();  // to start crop activity here no need.
    }



    public Uri getImageUri()
    {
    return imageUri;
    }

    @Override
    public String getReceiver() {
        return getIntent().getStringExtra(RECEIVER_ID);
    }
}
