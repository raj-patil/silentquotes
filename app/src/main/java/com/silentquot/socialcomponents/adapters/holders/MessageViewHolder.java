/*
 * Copyright (c) 2020. Created By Raj Patil
 */

package com.silentquot.socialcomponents.adapters.holders;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.silentquot.R;
import com.silentquot.socialcomponents.managers.listeners.OnDownloadedCallback;
import com.silentquot.socialcomponents.model.Message;
import com.silentquot.socialcomponents.utils.DownloadChatImages;
import com.silentquot.socialcomponents.utils.GetThumbnail;
import com.silentquot.socialcomponents.utils.GlideApp;
import com.silentquot.socialcomponents.utils.ImageUtil;

public class MessageViewHolder extends RecyclerView.ViewHolder {


    public TextView show_message;
    public ImageView profile_image;
    public TextView txt_seen;
    public ImageView imageView;
    public RelativeLayout chat_right_rel_layout;
    private Activity activity;
    private ProgressBar mProgressbar , downloadchatimageprogressbar;
    ImageButton chatImageDownloadButton ;
     CardView chat_img_download_btn_container;
    private  static String TEXT_MSG_TYPE= "Text";
    CallBack callBack;
    public MessageViewHolder(@NonNull View itemView, CallBack callBack, Activity activity) {
        super(itemView);
        this.activity=activity;
        show_message = itemView.findViewById(R.id.show_message);
        //profile_image = itemView.findViewById(R.id.profile_image);
        txt_seen = itemView.findViewById(R.id.txt_seen);
        imageView=itemView.findViewById(R.id.show_image);
        mProgressbar= itemView.findViewById(R.id.chatimageprogressbar);
        chat_right_rel_layout=itemView.findViewById(R.id.chat_right_relative_layout);
        chatImageDownloadButton = itemView.findViewById(R.id.chat_img_download_btn);
        downloadchatimageprogressbar= itemView.findViewById(R.id.downloadchatimageprogressbar);
        chat_img_download_btn_container= itemView.findViewById(R.id.chat_img_download_btn_container);
        this.callBack=callBack;

    }

    public void bindMessage(Message message) {

           if (message!=null && message.getMsgtype().equals(TEXT_MSG_TYPE))
       {
              show_message.setText(message.getMessage());
               show_message.setOnLongClickListener(new View.OnLongClickListener() {
                   @Override
                   public boolean onLongClick(View v) {
                       return false;
                   }
               });

                  seenstatus(message);
       }
    }


    public  void bindSenderImageMessage(Message message)
    {
        imageView.setVisibility(View.VISIBLE);
        ImageUtil.loadImageCenterCrop(GlideApp.with(activity), message.getMessage(), imageView,300 ,300 , new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {

                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                mProgressbar.setVisibility(View.GONE);
                imageView.setOnClickListener(v -> {
                    callBack.onItemClick(message , imageView);
                } );

                return false;
            }
        });
        seenstatus(message);

    }
    public  void bindLocalImageMessage(Message message)
    {
        imageView.setVisibility(View.VISIBLE);

        ImageUtil.loadLocalImage(GlideApp.with(activity), Uri.parse(message.getMessage()), imageView, new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

                mProgressbar.setVisibility(View.GONE);
                return false;
            }
        });

        seenstatus(message);
    }

    private void seenstatus(Message message) {

        if (message.isIsseen()){
            txt_seen.setText("Seen");
        } else {

            if (message.getStatus()!=null && message.getStatus().equals("sending"))
            {
                txt_seen.setText("sending");
            }
            else if (message.getStatus()!=null && message.getStatus().equals("send"))
            {
                txt_seen.setText("send");
            }
            else {
                txt_seen.setText("Delivered");
            }
        }

    }



    OnDownloadedCallback onDownloadedCallback = new OnDownloadedCallback() {
        @Override
        public void onSucussfullDownload(boolean sucuss) {

                chatImageDownloadButton.setVisibility(View.GONE);
                chatImageDownloadButton.setEnabled(false);
                downloadchatimageprogressbar.setVisibility(View.GONE);
                chat_img_download_btn_container.setVisibility(View.GONE);


        }

        @Override
        public void onDownloadProgress() {

        }

        @Override
        public void onDownloadFail(boolean fail) {

        }
    };


    public void bindReceiverImageMessage(Message message) {
        imageView.setVisibility(View.VISIBLE);

        if (message.getStatus()!=null  && message.getStatus().equals("received"))
        {
            loadThumbnail(message);
            chatImageDownloadButton.setVisibility(View.VISIBLE);
            chatImageDownloadButton.setEnabled(true);

        }
        if (message.getStatus()!=null && message.getStatus().equals("download")) {
            loadDownload(message);
        }
    }


    public  void loadThumbnail(Message message)
    {
        chatImageDownloadButton.setVisibility(View.VISIBLE);
        chatImageDownloadButton.setEnabled(true);
        chatImageDownloadButton.setOnClickListener(v -> {
            downloadchatimageprogressbar.setVisibility(View.VISIBLE);
            chatImageDownloadButton.setVisibility(View.GONE);
            callBack.onChatImageDownloadClick(message, imageView, onDownloadedCallback);
        } );

        String  imagename = message.getMsgid();
        if(ImageUtil.checkifImageExists(imagename))
        {
            String filename = ImageUtil.getFilename("SilentQuot" + "/images/sent/.thumbnail" , imagename);
            ImageUtil.loadImageCenterCrop(GlideApp.with(activity), filename,imageView ,300 ,300, new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    mProgressbar.setVisibility(View.GONE);
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    mProgressbar.setVisibility(View.GONE);
                    return false;
                }
            });
        } else {
                 new GetThumbnail(message.getThubmnail(), imageView, imagename).execute();
            mProgressbar.setVisibility(View.GONE);
        }

    }


    public  void loadDownload(Message message)
    {
        chatImageDownloadButton.setVisibility(View.GONE);
        chatImageDownloadButton.setEnabled(false);
        downloadchatimageprogressbar.setVisibility(View.GONE);
        chat_img_download_btn_container.setVisibility(View.GONE);

        String  imagename = message.getMsgid();
        if (ImageUtil.checkifChatImageExists(message.getMsgid())) {
            String filename = ImageUtil.getFilename("SilentQuot" + "/images", message.getMsgid());


            ImageUtil.loadImageCenterCrop(GlideApp.with(activity),filename , imageView, 300, 300, new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    mProgressbar.setVisibility(View.GONE);
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

                    imageView.setOnClickListener(v -> {
                        callBack.onItemClick(message, imageView);
                    });
                    mProgressbar.setVisibility(View.GONE);
                    return false;
                }
            });
        }
        else {
            new DownloadChatImages(message.getMessage() , imageView , imagename , onDownloadedCallback);
        }
    }



    public interface CallBack {
        void onItemClick(Message message, View view);

        void onChatImageDownloadClick(Message message , ImageView view , OnDownloadedCallback onDownloadedCallback);

    }

}
