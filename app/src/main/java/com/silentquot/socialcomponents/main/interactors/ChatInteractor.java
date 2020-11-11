/*
 * Copyright (c) 2020. Created By Raj Patil
 */

package com.silentquot.socialcomponents.main.interactors;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.UploadTask;
import com.silentquot.ApplicationHelper;
import com.silentquot.Model.Chatlist;
import com.silentquot.persistence.ConversationDAOImplSQLiteOpenHelper;
import com.silentquot.persistence.MessageDAOImplSQLiteOpenHelper;
import com.silentquot.persistence.MessagesDAO;
import com.silentquot.persistence.OnUpdateComplete;
import com.silentquot.socialcomponents.enums.UploadImagePrefix;
import com.silentquot.socialcomponents.managers.DatabaseHelper;
import com.silentquot.socialcomponents.managers.listeners.OnChatListChangeListner;
import com.silentquot.socialcomponents.managers.listeners.OnGetChatKey;
import com.silentquot.socialcomponents.managers.listeners.OnMessageListChangesListner;
import com.silentquot.socialcomponents.managers.listeners.OnObjectChangedListener;
import com.silentquot.socialcomponents.managers.listeners.OnSendMessageListner;
import com.silentquot.socialcomponents.managers.listeners.OnTaskCompleteListener;
import com.silentquot.socialcomponents.managers.listeners.OnThumbnailCreated;
import com.silentquot.socialcomponents.model.Message;
import com.silentquot.socialcomponents.model.MessageListResult;
import com.silentquot.socialcomponents.utils.ImageCompressor;
import com.silentquot.socialcomponents.utils.ImageUtil;
import com.silentquot.socialcomponents.utils.LogUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChatInteractor {
    private static final String TAG = ChatInteractor.class.getSimpleName();

    private static ChatInteractor instance;
    private DatabaseHelper databaseHelper;
    private Context context;
    private ConversationDAOImplSQLiteOpenHelper conversationDAO;
    MessagesDAO messagesDAO;

    public static ChatInteractor getInstance(Context context) {
        if (instance == null) {
            instance = new ChatInteractor(context);
        }

        return instance;
    }

    private ChatInteractor(Context context) {
        this.context = context;
        databaseHelper = ApplicationHelper.getDatabaseHelper();
         messagesDAO = new MessageDAOImplSQLiteOpenHelper(context);

    }



    public  void getAllChats(String currentUserId, final OnChatListChangeListner onChatListChangeListner)
    {
        conversationDAO= new ConversationDAOImplSQLiteOpenHelper(context);

        List<Chatlist> chatlist = conversationDAO.getAll();
        LogUtil.logDebug(TAG, "chatlistset, getAllChats" + chatlist);

        onChatListChangeListner.onChatListChanged(chatlist);

    }


    public ValueEventListener getNewChats(String currentUserId, OnChatListChangeListner onChatListChangeListner) {

        conversationDAO= new ConversationDAOImplSQLiteOpenHelper(context);
        DatabaseReference databaseReference = databaseHelper
                .getDatabaseReference()
                .child(DatabaseHelper.CHATLIST_DB_KEY)
                .child(currentUserId);
        ValueEventListener valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Chatlist> list = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chatlist chatlist = snapshot.getValue(Chatlist.class);

                    Chatlist chat=conversationDAO.get(chatlist.getId());
                    if ( chat==null) {
                        conversationDAO.insert(chatlist);
                    }
                    else {

                    }
                }

                getAllChats(currentUserId,onChatListChangeListner);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "getCommentsList(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });

        databaseHelper.addActiveListener(valueEventListener, databaseReference);
        return valueEventListener;
    }

    public void getSingleChatItemOffline(String currentuserId , String  chatId , OnObjectChangedListener<Chatlist> onObjectChangedListener)
    {
        conversationDAO= new ConversationDAOImplSQLiteOpenHelper(context);
       Chatlist chatitem = conversationDAO.get(chatId);
       onObjectChangedListener.onObjectChanged(chatitem);
    }

    public void getSingleChatItem(String currentuserId, String chatId, OnObjectChangedListener<Chatlist> onObjectChangedListener) {

        DatabaseReference databaseReference = databaseHelper
                .getDatabaseReference()
                .child(DatabaseHelper.CHATLIST_DB_KEY)
                .child(currentuserId).child(chatId);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Chatlist chatItem = dataSnapshot.getValue(Chatlist.class);
                    onObjectChangedListener.onObjectChanged(chatItem);
                }
                else {
                    onObjectChangedListener.onObjectChanged(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    private void seenMessage(final String sender , String receiver , String msgid) {

        DatabaseReference databaseReference = databaseHelper
                .getDatabaseReference()
                .child(DatabaseHelper.MESSAGES_DB_KEY).child(msgid).child("isseen");

        databaseReference.setValue(true);

    }

    Query query;
    private  static ValueEventListener chatEventListener = null ;

    public  void removeChatListner()
    {
        if (query!=null && chatEventListener!=null)
        query.removeEventListener(chatEventListener);
    }


    public ValueEventListener getAllMessages(Chatlist chatitem, OnMessageListChangesListner onMessageListChangesListner) {

        DatabaseReference databaseReference = databaseHelper
                .getDatabaseReference()
                .child(DatabaseHelper.MESSAGES_DB_KEY);



      query  = databaseReference.orderByChild("chat_key").equalTo(chatitem.getKey());

         chatEventListener =   query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                MessageListResult messageListResult = new MessageListResult();
                List<Message> list = new ArrayList<Message>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message message = snapshot.getValue(Message.class);
                 //   list.add(message);
                    String  msgid = snapshot.getKey();
                    message.setMsgid(msgid);


                    //work at Receiver Side
                    if (message.getReceiver().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()) && message.getSender().equals(chatitem.getId()))
                    {
                        if (!message.isIsseen())
                        {
                          //  list.add(message);
                            message.setStatus("received");
                            messagesDAO.insert(message);
                            seenMessage(chatitem.getId() ,FirebaseAuth.getInstance().getCurrentUser().getUid(), msgid );
                            messageListResult.setMessage(list);
//                            if (message.getMsgtype().equals("Image"))
//                            {
//
//                            }

                            onMessageListChangesListner.onListChanged(messageListResult);
                        }


                    }

                    //Work At Sender Side
                    if (message.getSender().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())&& message.getReceiver().equals(chatitem.getId())){

                        if (messagesDAO.isMsgIdExist(msgid)) {
                            if (message.isIsseen()) {
                                LogUtil.logDebug("TAG" , msgid + "exist with seen true");
                                messagesDAO.updateIsseen(message, new OnUpdateComplete() {
                                    @Override
                                    public void onComplete(boolean sucuss) {
                                        databaseReference.child(msgid).removeValue();
                                        onMessageListChangesListner.onListChanged(messageListResult);

                                    }
                                });
                            }
                            else {
                                    messagesDAO.updateMessage(message, new OnUpdateComplete() {
                                        @Override
                                        public void onComplete(boolean sucuss) {
                                            messageListResult.setMessage(list);
                                            onMessageListChangesListner.onListChanged(messageListResult);
                                        }
                                    });
                               // list.add(message);
                            }
                        } else {
                            //list.add(message);
                           /// messagesDAO.insert(message);
                            //  databaseReference.child(msgid).removeValue();
                        }
                    }

                }

            //    messageListResult.setMessage(list);
              //  onMessageListChangesListner.onListChanged(messageListResult);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return  chatEventListener;
    }


    public  void getChatKey(String sender , String  receiver , OnGetChatKey onGetChatKey){
        getSingleChatItem(sender, receiver, new OnObjectChangedListener<Chatlist>() {

            @Override
            public void onObjectChanged(Chatlist obj) {
                if (obj!=null) {
                    onGetChatKey.onChatKeyExist(obj.getKey());
                }
                else {
                    onGetChatKey.onChatKeyDosentExist();
                }

            }

            @Override
            public void onError(String errorText) {
                onGetChatKey.onChatKeyDosentExist();
            }
        });



    }

     public void createChatKey(String sender , String receiver , OnGetChatKey onGetChatKey)
     {
         DatabaseReference chatListReference = databaseHelper.getDatabaseReference().child(DatabaseHelper.CHATLIST_DB_KEY).child(sender).child(receiver);
         DatabaseReference chatListReceiverReference = databaseHelper.getDatabaseReference().child(DatabaseHelper.CHATLIST_DB_KEY).child(receiver).child(sender);

         String ChatKey = chatListReference.push().getKey();

         Chatlist chatitem = new Chatlist();
         chatitem.setId(receiver);
         chatitem.setKey(ChatKey);
         chatitem.setChat_type("userchat");


         Chatlist chatitemreceiver = new Chatlist();
         chatitemreceiver.setId(sender);
         chatitemreceiver.setKey(ChatKey);
         chatitemreceiver.setChat_type("userchat");

         saveChatItem(chatListReference , chatitem ).addOnSuccessListener(aVoid -> {
             chatListReference.child("timestamp").setValue(ServerValue.TIMESTAMP);
             saveChatItem( chatListReceiverReference ,chatitemreceiver).addOnSuccessListener(aVoid1 -> {
                 chatListReceiverReference.child("timestamp").setValue(ServerValue.TIMESTAMP);
                 onGetChatKey.onChatKeyExist(ChatKey);
             }).addOnCanceledListener(() -> {
                 onGetChatKey.onChatKeyDosentExist();
             });
         });


     }



    public void sendTextMessage(Message message, OnSendMessageListner onSendMessageListner) {
        DatabaseReference messageReference = databaseHelper.getDatabaseReference().child(DatabaseHelper.MESSAGES_DB_KEY);
    //    DatabaseReference chatListReference = databaseHelper.getDatabaseReference().child(DatabaseHelper.CHATLIST_DB_KEY).child(sender).child(receiver);
      //  DatabaseReference chatListReceiverReference = databaseHelper.getDatabaseReference().child(DatabaseHelper.CHATLIST_DB_KEY).child(receiver).child(sender);
        LogUtil.logDebug(TAG, "chatinteractor send msg before callinh chat item ");

        message.setIsseen(false);
        message.setStatus("sending");


        getChatKey(message.getSender(), message.getReceiver(), new OnGetChatKey() {
            @Override
            public void onChatKeyExist(String chatkey) {

                LogUtil.logDebug(TAG, "chatKey is exist " + chatkey);
                message.setChat_key(chatkey);
                if (!message.getMsgtype().equals("Image")) {
                    String msgid = messageReference.push().getKey();
                    message.setMsgid(msgid);

                    onSendMessageListner.onBeforeMessageSent(message);
                    sendMessage(message, onSendMessageListner);
                } else {
                    sendMessage(message, onSendMessageListner);
                }

            }

            @Override
            public void onChatKeyDosentExist() {


                createChatKey(message.getSender(), message.getReceiver(), new OnGetChatKey() {
                    @Override
                    public void onChatKeyExist(String chatkey) {
                        LogUtil.logDebug(TAG, "chatkey is Created  " + chatkey);
                        message.setChat_key(chatkey);

                        if (!message.getMsgtype().equals("Image")) {
                            String msgid = messageReference.push().getKey();
                            message.setMsgid(msgid);

                            onSendMessageListner.onBeforeMessageSent(message);
                            sendMessage(message, onSendMessageListner);
                        } else {

                            sendMessage(message, onSendMessageListner);
                        }
                    }

                    @Override
                    public void onChatKeyDosentExist() {

                    }
                });


            }
        });
    }


        public void sendTextMessage(String sender, String receiver, String messageText, String messageType, String msgid , OnSendMessageListner onSendMessageListner) {
        DatabaseReference messageReference = databaseHelper.getDatabaseReference().child(DatabaseHelper.MESSAGES_DB_KEY);
        DatabaseReference chatListReference = databaseHelper.getDatabaseReference().child(DatabaseHelper.CHATLIST_DB_KEY).child(sender).child(receiver);
        DatabaseReference chatListReceiverReference = databaseHelper.getDatabaseReference().child(DatabaseHelper.CHATLIST_DB_KEY).child(receiver).child(sender);
        LogUtil.logDebug(TAG, "chatinteractor send msg before callinh chat item ");


        Message message = new Message();
        message.setMessage(messageText);
        message.setReceiver(receiver);
        message.setSender(sender);
        message.setMsgtype(messageType);
        message.setIsseen(false);
        message.setStatus("sending");




        getChatKey(sender, receiver, new OnGetChatKey() {
            @Override
            public void onChatKeyExist(String chatkey) {

                LogUtil.logDebug(TAG, "chatKey is exist " +chatkey);
                message.setChat_key(chatkey);
                if (!messageType.equals("Image")) {
                    String msgid = messageReference.push().getKey();
                    message.setMsgid(msgid);

                    onSendMessageListner.onBeforeMessageSent(message);
                    sendMessage(message, onSendMessageListner);
                }
                else {
                    message.setMsgid(msgid);
                    sendMessage(message, onSendMessageListner);
                }

            }

            @Override
            public void onChatKeyDosentExist() {


                createChatKey(sender, receiver, new OnGetChatKey() {
                    @Override
                    public void onChatKeyExist(String chatkey) {
                        LogUtil.logDebug(TAG, "chatkey is Created  " + chatkey);
                        message.setChat_key(chatkey);

                        if (!messageType.equals("Image")) {
                            String msgid = messageReference.push().getKey();
                            message.setMsgid(msgid);

                            onSendMessageListner.onBeforeMessageSent(message);
                            sendMessage(message, onSendMessageListner);
                        }
                        else {
                            message.setMsgid(msgid);
                            sendMessage(message, onSendMessageListner);
                        }
                    }

                    @Override
                    public void onChatKeyDosentExist() {

                    }
                });


            }
        });




//      getSingleChatItem(sender, receiver, new OnObjectChangedListener<Chatlist>() {
//          @Override
//          public void onObjectChanged(Chatlist obj) {
//              if (obj != null) {
//
//                  LogUtil.logDebug(TAG, "chatitem is exist " + obj.getId());
//
//                  Message message = new Message();
//                  message.setMessage(messageText);
//                  message.setReceiver(receiver);
//                  message.setSender(sender);
//                  message.setChat_key(obj.getKey());
//                  message.setMsgtype(messageType);
//                  message.setIsseen(false);
//                  message.setStatus("sending");
//                  if (!messageType.equals("Image")) {
//                      String msgid = messageReference.push().getKey();
//                      message.setMsgid(msgid);
//
//                      onSendMessageListner.onBeforeMessageSent(message);
//                      sendMessage(message, onSendMessageListner);
//                  }
//                  else {
//                      message.setMsgid(msgid);
//                      sendMessage(message, onSendMessageListner);
//                  }
//
//              } else
//              {
//                  String ChatKey = chatListReference.push().getKey();
//                  Message message = new Message();
//                  message.setMessage(messageText);
//                  message.setReceiver(receiver);
//                  message.setSender(sender);
//                  message.setChat_key(ChatKey);
//                  message.setMsgtype(messageType);
//                  message.setIsseen(false);
//                  message.setStatus("sending");
//                  String  msgid =  messageReference.push().getKey();
//                  message.setMsgid(msgid);
//
//                Chatlist chatitem = new Chatlist();
//                chatitem.setId(receiver);
//                chatitem.setKey(ChatKey);
//                chatitem.setChat_type("userchat");
//
//
//                  Chatlist chatitemreceiver = new Chatlist();
//                  chatitemreceiver.setId(sender);
//                  chatitemreceiver.setKey(ChatKey);
//                  chatitemreceiver.setChat_type("userchat");
//
//                  saveChatItem(chatListReference , chatitem ).addOnSuccessListener(aVoid -> {
//                      chatListReference.child("timestamp").setValue(ServerValue.TIMESTAMP);
//                      saveChatItem( chatListReceiverReference ,chatitemreceiver).addOnSuccessListener(aVoid1 -> {
//                          chatListReceiverReference.child("timestamp").setValue(ServerValue.TIMESTAMP);
//                          onSendMessageListner.onBeforeMessageSent(message);
//                          sendMessage(message , onSendMessageListner);
//                      });
//                  });
//
////                saveChatItem(chatListReference ,  chatitem , success ->
////                        saveChatItem(chatListReceiverReference , chatitemreceiver , success1 ->
////                                sendMessage(message , onTaskCompleteListener)));
//
//              }
//
//          }
//
//          @Override
//          public void onError(String errorText) {
//
//          }
//      });
//

    }

    public Task<Void> saveChatItem(DatabaseReference chatListReference , Chatlist chatitem) {
        return chatListReference.setValue(chatitem);
    }

    private void saveChatItem(DatabaseReference chatListReference, Chatlist chatitem, OnTaskCompleteListener onTaskCompleteListener) {

        chatListReference.setValue(chatitem).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                chatListReference.child("timestamp").setValue(ServerValue.TIMESTAMP);
                onTaskCompleteListener.onTaskComplete(true);
            }
        });
    }

    private void sendMessage(Message message, OnSendMessageListner onSendMessageListner) {
        DatabaseReference messageReference = databaseHelper.getDatabaseReference().child(DatabaseHelper.MESSAGES_DB_KEY);
        String messageId = message.getMsgid();
        message.setStatus("send");
        LogUtil.logDebug(TAG, "sending msg - "+messageId);
        messageReference.child(messageId).setValue(message, (databaseError, databaseReference) -> {
            if (databaseError==null)
            {
                messageReference.child(messageId).child("status").setValue("send");
                LogUtil.logDebug(TAG, "msg send sucusss ");
                message.setStatus("send");
                onSendMessageListner.onMessageSentComplete(message);
            }
        });

    }

    public void sendImageMessage(Message message, Uri imageUri, OnSendMessageListner onSendMessageListner) {
            DatabaseReference messageReference = databaseHelper.getDatabaseReference().child(DatabaseHelper.MESSAGES_DB_KEY);
            String messageId = messageReference.push().getKey();
            LogUtil.logDebug(TAG, "sendImageMessage in chatInterCtor");
            String imageTitle = ImageUtil.generateImageTitle(UploadImagePrefix.CHAT, messageId);


            //Image Compressor
        ImageCompressor.compress(context ,context.getContentResolver(), imageUri, new ImageCompressor.OnImageCompressListener() {
            @Override
            public void onImageCompressed(Uri imageUri) {
                LogUtil.logDebug(TAG, "Compress Imaage Uri : - " + imageUri);

    // createThumbnail(context , imageUri , messageId);

                //Upload Origional Imaage
        UploadTask uploadTask = databaseHelper.uploadChatImage(imageUri, imageTitle);
            if (uploadTask!=null){
                LogUtil.logDebug(TAG, "uploadTask NotNull, uploading");

                message.setMsgid(messageId);
                message.setStatus("sending");
                message.setMessage(imageUri.toString());

                getChatKey(message.getSender(), message.getReceiver(), new OnGetChatKey() {
                    @Override
                    public void onChatKeyExist(String chatkey) {
                        message.setChat_key(chatkey);
                        onSendMessageListner.onBeforeMessageSent(message);


                        uploadTask.addOnCompleteListener(task -> {
                   if (task.isSuccessful()){
                       LogUtil.logDebug(TAG, "upload Task Complete");
                       Objects.requireNonNull(task.getResult()).getStorage().getDownloadUrl().addOnCompleteListener(task1 -> {
                          if (task1.isSuccessful()){
                              Uri downloadUrl = task1.getResult();
                              LogUtil.logDebug(TAG, "successful upload image, image url: " + String.valueOf(downloadUrl));
                              assert downloadUrl != null;
                              message.setMessage(downloadUrl.toString());
                              message.setIsseen(false);


                              //upload Thumbnail
                              createThumbnail(context, imageUri, messageId, new OnThumbnailCreated() {
                                  @Override
                                  public void onThumbnailCreated(String path) {
                                       message.setThubmnail(path);

                                      sendTextMessage(message, new OnSendMessageListner() {
                                          @Override
                                          public void onBeforeMessageSent(Message message) {
                                              //   onSendMessageListner.onBeforeMessageSent(message);
                                          }

                                          @Override
                                          public void onMessageSentComplete(Message message) {
                                              message.setMessage(imageUri.toString());
                                              onSendMessageListner.onMessageSentComplete(message);
                                          }
                                      });

                                  }

                                  @Override
                                  public void onFailedThumbnailCreated(String err) {
                                      LogUtil.logDebug(TAG, "Failed, to create Thumbnail");
                                  }
                              });
                          }
                       });
                   }
                   else {
                       LogUtil.logDebug(TAG, "sendImageImage, fail to upload image");
                   }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                Log.d(TAG, "Upload is " + progress + "% done");
                                int currentProgress = (int) progress;
                              //  callback.onProgress(currentProgress);
                            }
                        });
                    }

                    @Override
                    public void onChatKeyDosentExist() {

                        createChatKey(message.getSender(), message.getReceiver(), new OnGetChatKey() {
                            @Override
                            public void onChatKeyExist(String chatkey) {

                                LogUtil.logDebug(TAG, "New Chat Key  Created" + chatkey);
                                message.setChat_key(chatkey);
                                onSendMessageListner.onBeforeMessageSent(message);

                                uploadTask.addOnCompleteListener(task -> {
                                    if (task.isSuccessful()){
                                        LogUtil.logDebug(TAG, "upload Task Complete");
                                        Objects.requireNonNull(task.getResult()).getStorage().getDownloadUrl().addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()){
                                                Uri downloadUrl = task1.getResult();
                                                LogUtil.logDebug(TAG, "successful upload image, image url: " + String.valueOf(downloadUrl));
                                                assert downloadUrl != null;
                                                message.setMessage(downloadUrl.toString());
                                                message.setIsseen(false);
                                                sendTextMessage(message.getSender(), message.getReceiver(), downloadUrl.toString(), message.getMsgtype(),messageId, new OnSendMessageListner() {
                                                    @Override
                                                    public void onBeforeMessageSent(Message message) {
                                                        //   onSendMessageListner.onBeforeMessageSent(message);
                                                    }

                                                    @Override
                                                    public void onMessageSentComplete(Message message) {
                                                        onSendMessageListner.onMessageSentComplete(message);
                                                    }
                                                });
                                            }
                                        });
                                    }
                                    else {
                                        LogUtil.logDebug(TAG, "sendImageImage, fail to upload image");
                                    }
                                });
                            }

                            @Override
                            public void onChatKeyDosentExist() {
                                LogUtil.logDebug(TAG, "ChatKey not created, fail to create a Chat Key");

                            }
                        });

                    }
                });

            }
            else {
                LogUtil.logDebug(TAG, "uploadTaskis NUll, UploadedFail");
            }
            }


        });

    }

    private static String getFilename(String folderPath) {
//            File file = new File(Environment.getExternalStorageDirectory().getPath(), folderPath + "/images/sent");
        File file = new File(Environment.getExternalStorageDirectory().getPath(), folderPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        String uriSting = (file.getAbsolutePath() + "/" + System.currentTimeMillis() + ".jpg");
        return uriSting;
    }


    private  void createThumbnail(final Context context, Uri file , String messageId , OnThumbnailCreated onThumbnailCreated ) {
//        ImageCompressor.compress(context, context.getContentResolver(), file, new ImageCompressor.OnImageCompressListener() {
//
//            @Override
//            public void onImageCompressed(Uri path) {
                try {
                    String imageTitle = ImageUtil.generateImageTitle(UploadImagePrefix.CHAT, messageId);
                    Bitmap thumbnail = ImageUtil.getThumbnail(context , file);
                    Bitmap blurThumbnail = ImageUtil.blur(context , thumbnail);

                    FileOutputStream out = null;
                    String filename = getFilename("SilentQuot" + "/images/sent/thumbnail");
                    try {
                        out = new FileOutputStream(filename);

                        // write the compressed bitmap at the destination specified by filename.
                        blurThumbnail.compress(Bitmap.CompressFormat.JPEG, 80, out);

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    // add scheme
                    filename = "file://" + filename;

                    UploadTask uploadTask = databaseHelper.uploadChatImageThumbnail(Uri.parse(filename), imageTitle);
                    if (uploadTask!=null){
                        uploadTask.addOnCompleteListener(task -> {
                            if (task.isSuccessful()){
                                LogUtil.logDebug(TAG, "upload Task Complete");
                                Objects.requireNonNull(task.getResult()).getStorage().getDownloadUrl().addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()){
                                        Uri downloadUrl = task1.getResult();
                                        LogUtil.logDebug(TAG, "successful upload Thumbnail, image url: " + String.valueOf(downloadUrl));
                                            onThumbnailCreated.onThumbnailCreated(downloadUrl.toString());
                                    }
                                });
                            }
                            else {
                                LogUtil.logDebug(TAG, "sendImageImage, fail to upload image");
                            }
                        });

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
//        });
//    }

}


//chat_key:
//        "-M8GoTulG6RORSpaOzVK"
//        isseen:
//        true
//        message:
//        "\"update"
//        msgtype:
//        "Text"
//        receiver:
//        "r1BQGmpgbbd9FxQivV8uwxzl0fF3"
//        sender:
//        "XU58Yf51FtNlKmcEVjtsUZYcdzb2"
