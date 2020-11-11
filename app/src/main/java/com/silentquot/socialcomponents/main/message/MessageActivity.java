/*
 * Copyright (c) 2020. Created By Raj Patil
 */

package com.silentquot.socialcomponents.main.message;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.silentquot.Model.Chatlist;
import com.silentquot.R;
import com.silentquot.persistence.MessageDAOImplSQLiteOpenHelper;
import com.silentquot.socialcomponents.adapters.MessageAdapter;
import com.silentquot.socialcomponents.adapters.holders.MessageViewHolder;
import com.silentquot.socialcomponents.main.base.BaseActivity;
import com.silentquot.socialcomponents.main.imageDetail.ImageDetailActivity;
import com.silentquot.socialcomponents.main.imageDetail.SendImageActivity;
import com.silentquot.socialcomponents.managers.ChatManager;
import com.silentquot.socialcomponents.managers.listeners.OnDownloadedCallback;
import com.silentquot.socialcomponents.model.Message;
import com.silentquot.socialcomponents.utils.DownloadChatImages;
import com.silentquot.socialcomponents.utils.GlideApp;
import com.silentquot.socialcomponents.utils.ImageUtil;
import com.silentquot.socialcomponents.utils.LogUtil;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends BaseActivity<MessageView, MessagePresenter> implements MessageView {


    public static final String CHAT_EXTRA_KEY = "MessageActivity.CHAT_EXTRA_KEY";
    CircleImageView userimageview;
    TextView usernametextview;
    ImageButton sendMessageButton;
    EditText textToSendEditText;
    ImageView show_image;
    RecyclerView recyclerView;
    ImageView imgMessageButton;
    LinearLayout messageActivity_txt_box;
    Chatlist chatitem;
    MessageAdapter messageAdapter;
    Uri imageUri;
    private String IMAGE_URI_FROM_SEND_IMAGE_ACTIVITY ="imageuri";

    public final  static  int TEXT_MESSAGE=1;
    public final   static  int IMAGE_MESSAGE=2;
    private ScrollView scrollView;
    private String RECEIVER_ID = "receiverId";
    private ChatManager chatManager;
    private MessageDAOImplSQLiteOpenHelper messagesDAO;

    @NonNull
    @Override
    public MessagePresenter createPresenter() {
        if (presenter == null) {
            return new MessagePresenter(this);
        }
        return presenter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
       // Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
         chatitem = (Chatlist) getIntent().getSerializableExtra(CHAT_EXTRA_KEY);
        chatManager = ChatManager.getInstance(this);
        initContentView();
    }

    private void initContentView() {
        userimageview = findViewById(R.id.profile_image);
        usernametextview = findViewById(R.id.username);
        sendMessageButton = findViewById(R.id.btn_send);
        textToSendEditText = findViewById(R.id.text_send);
        recyclerView = findViewById(R.id.recycler_view);
        imgMessageButton = findViewById(R.id.imgmsg);
      //  scrollView = findViewById(R.id.scrollView);
        textToSendEditText.addTextChangedListener(messageTextWatcher);
        presenter.loaduserprofile(chatitem.getId());
        initMessageRecycleView();
        initListner();
    }

    private TextWatcher messageTextWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            imageUri=null;
            imgMessageButton.setVisibility(View.GONE);
            imgMessageButton.setEnabled(false);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (TextUtils.isEmpty(s))
            {
                imgMessageButton.setVisibility(View.VISIBLE);
                imgMessageButton.setEnabled(true);
            }
        }
    };

    private void initListner() {
//        sendMessageButton.setOnClickListener(v -> presenter.onSendButtonClick());
       LogUtil.logDebug(TAG, "initlistner");

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                presenter.onSendButtonClick();
            }
        });

        imgMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Intent intent = new Intent(MessageActivity.this, SendImageActivity.class);
                intent.putExtra(RECEIVER_ID , getReceiver());
                startActivityForResult(intent , SendImageActivity.SEND_IMAGE_REQUEST);
            }
        });


    }

    private void initMessageRecycleView() {
         messageAdapter = new MessageAdapter(this );

         messageAdapter.setCallBack(new MessageViewHolder.CallBack() {
             @Override
             public void onItemClick(Message message, View view) {
                 LogUtil.logDebug(TAG, "onitem Click");

                 if (message.getMsgtype().equals("Image")) {
                     if (!message.getStatus().equals("received")) {
                         Intent intent = new Intent(MessageActivity.this, com.silentquot.socialcomponents.main.message.imagemessage.ImageDetailActivity.class);
                         intent.putExtra(ImageDetailActivity.IMAGE_TITLE_EXTRA_KEY, message.getMessage());
                         startActivity(intent);
                     }
                 }

             }

             @Override
             public void onChatImageDownloadClick(Message message, ImageView view , OnDownloadedCallback onDownloadedCallback) {
                 messagesDAO = new MessageDAOImplSQLiteOpenHelper(getApplicationContext());
                 if(ImageUtil.checkifChatImageExists(message.getMsgid()))
                 {
                     String filename = ImageUtil.getFilename("SilentQuot" + "/images" , message.getMsgid());
                     ImageUtil.loadImageCenterCrop(GlideApp.with(getApplicationContext()), filename,view ,300 ,300, new RequestListener<Drawable>() {
                         @Override
                         public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {

                         onDownloadedCallback.onDownloadFail(false);
                             return false;
                         }

                         @Override
                         public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

                             message.setStatus("download");
                             messagesDAO.updateStatus(message, sucuss1 -> {
                                 onDownloadedCallback.onSucussfullDownload(true);
                             });

                            // onDownloadedCallback.onSucussfullDownload(true);
                             return false;
                         }
                     });
                 } else {
                     new DownloadChatImages(message.getMessage(), view, message.getMsgid(), new OnDownloadedCallback() {
                         @Override
                         public void onSucussfullDownload(boolean sucuss) {
                             message.setStatus("download");
                             messagesDAO.updateStatus(message, sucuss1 -> {
                                 onDownloadedCallback.onSucussfullDownload(true);
                             });

                         }

                         @Override
                         public void onDownloadProgress() {

                         }

                         @Override
                         public void onDownloadFail(boolean fail) {
                                    onDownloadedCallback.onDownloadFail(false);
                         }
                     }).execute() ;

                 }
             }




         });

        recyclerView.setAdapter(messageAdapter);

        loadMessages();

    }

    private void loadMessages() {

        if (checkWriteExternalPermission(getApplicationContext())) {
            presenter.loadmessages(chatitem);
        }
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        boolean canUseExternalStorage = false;

        switch (requestCode) {
            case 101: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    canUseExternalStorage = true;
                    presenter.loadmessages(chatitem);
                }

                if (!canUseExternalStorage) {
                    Toast.makeText(getApplicationContext(), "Cannot use this feature without requested permission", Toast.LENGTH_SHORT).show();
                } else {
                    // user now provided permission
                    // perform function for what you want to achieve
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.logDebug(TAG, "ResultCode" + resultCode);
        if (resultCode==RESULT_OK  && requestCode == SendImageActivity.SEND_IMAGE_REQUEST)
        {
//            LogUtil.logDebug(TAG, "runOnActivityResult" + resultCode);
//            Message sendingMessage = (Message) data.getSerializableExtra(SendImageActivity.SENDING_MESSAGE_EXTRA_KEY);
//            sendingMessage.setChat_key(chatitem.getKey());
//            sendingMessage.setMsgtype("Image");
//            sendingMessage.setSender(FirebaseAuth.getInstance().getCurrentUser().getUid());
//            LogUtil.logDebug(TAG, "sendingmsg" + sendingMessage.getMessage());
//            messageAdapter.addSedingMessage(sendingMessage);

            imageUri = Uri.parse(data.getStringExtra(IMAGE_URI_FROM_SEND_IMAGE_ACTIVITY));
            presenter.onImageSendClick();

        }

    }



    @Override
    public void setName(String username) {
        usernametextview.setText(username);
    }

    @Override
    public void setProfilePhoto(String photoUrl) {

        ImageUtil.loadImage(GlideApp.with(this), photoUrl, userimageview, new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
            //    avatarProgressBar.setVisibility(View.GONE);
                return false;
            }
            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
            //    avatarProgressBar.setVisibility(View.GONE);
                return false;
            }
        });

    }

    @Override
    public void openMessageActivity() {

    }

    @Override
    public void setMessageList(List<Message> message) {
        messageAdapter.setMessageList(message);
    }

    public void addNewMessageList(List<Message> messages){
        messageAdapter.addNewMessages(messages);
    }


    @Override
    public void clearMessageText() {
         textToSendEditText.setText("");
    }

    @Override
    public String getMessageText() {
       return textToSendEditText.getText().toString();
    }

    @Override
    public int getMessageType() {
        if (imageUri==null)
        {
            return TEXT_MESSAGE;
        }
        else
        {
            return IMAGE_MESSAGE;
        }
    }

    @Override
    public String getReceiver() {
        return chatitem.getId();
    }




    @Override
    public void refreshNewItem() {
  //    messageAdapter.updateItem();
//        messageAdapter.updateItem();
      recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
       // recyclerView.smoothScrollToPosition();
      //  messageAdapter.notifyDataSetChanged();
        LogUtil.logDebug(TAG, "refreshNewItem " +messageAdapter.getItemCount()+1);
    //   scrollView.smoothScrollTo(0 , sendMessageButton.getTop() );


    }

    @Override
    public void addnewIten(Message message) {
        messageAdapter.updateNewMesage(message);

    }

    @Override
    public Uri getImageUri() {
        return imageUri;
    }

    @Override
    public void setCacheSize(int size) {
        recyclerView.setItemViewCacheSize(size);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        chatManager.closeListeners(this);
    }
    

//
//    private void scrollToBottom() {
//        // scroll to last position
//        if (messageAdapter.getItemCount() > 0) {
//            int position = messageAdapter.getItemCount() - 1;
//            mLinearLayoutManager.scrollToPositionWithOffset(position, 0);
//        }
//    }


}
