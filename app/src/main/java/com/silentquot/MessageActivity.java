package com.silentquot;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.silentquot.Adapter.BrodcastAdapter;
import com.silentquot.Adapter.MessageAdapter;
import com.silentquot.Fragments.APIService;
import com.silentquot.Model.Brodcast;
import com.silentquot.Model.BrodcastChat;
import com.silentquot.Model.Chat;
import com.silentquot.Model.User;
import com.silentquot.Notifications.Client;
import com.silentquot.Notifications.Data;
import com.silentquot.Notifications.MyResponse;
import com.silentquot.Notifications.Sender;
import com.silentquot.Notifications.Token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {



    FirebaseUser fuser;
    DatabaseReference reference;

    CircleImageView profile_image;
    TextView username;
    ImageButton btn_send;
    EditText text_send;
    ImageView show_image;
    RecyclerView recyclerView;
    LinearLayout messageActivity_txt_box;

    MessageAdapter messageAdapter;
    BrodcastAdapter brodcastAdapter;
    List<Chat> mchat;
    List<BrodcastChat> bChat;



    Intent intent;

    ValueEventListener seenListener;
    private final int PICK_IMAGE_REQUEST = 71;
    StorageReference storageReference;
    private StorageTask uploadTask;
    Uri imgurl;
    String userid;

    APIService apiService;

    ImageView imgmsg;
    String msgtype;
    String chatType;
    boolean notify = false;
    private String ImageName;
    String key;
    String chat_key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // and this
                startActivity(new Intent(MessageActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        btn_send = findViewById(R.id.btn_send);
        text_send = findViewById(R.id.text_send);
        show_image = findViewById(R.id.show_image);
        imgmsg = findViewById(R.id.imgmsg);
        messageActivity_txt_box = findViewById(R.id.messageActivity_txt_box);

        initToolbar();

        intent = getIntent();
        userid = intent.getStringExtra("userid");
        chatType = intent.getStringExtra("chatType");
        chat_key = intent.getStringExtra("chat_key");

        fuser = FirebaseAuth.getInstance().getCurrentUser();

        imgmsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                msgtype = "Image";
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select File"), PICK_IMAGE_REQUEST);
            }
        });


        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notify = true;
                String msg = text_send.getText().toString();
                if (!msg.equals("") && msgtype != "Image") {
                    sendMessage(fuser.getUid(), userid, msg, "Text", chatType);
                } else if (msgtype == "Image") {
                    Toast.makeText(MessageActivity.this, "post Uploading...", Toast.LENGTH_SHORT).show();
                    uploadImage();

                } else {
                    Toast.makeText(MessageActivity.this, "You can't send empty message", Toast.LENGTH_SHORT).show();
                }
                text_send.setText("");
            }
        });


        if (("userchat").equals(chatType)) {
            reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);


                    username.setText(user.getUsername());
                    if (user.getImageURL().equals("default")) {
                        profile_image.setImageResource(R.mipmap.ic_launcher);
                    } else {
                        //and this
                        Glide.with(getApplicationContext()).load(user.getImageURL()).into(profile_image);
                    }

                    readMesagges(fuser.getUid(), userid, user.getImageURL(), chatType);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        if (("brodcast").equals(chatType)) {
//            messageActivity_txt_box.setVisibility(View.INVISIBLE);
//            messageActivity_txt_box.setEnabled(false);


            reference = FirebaseDatabase.getInstance().getReference("Brodcast").child(userid);
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Brodcast brodcast = dataSnapshot.getValue(Brodcast.class);
                    username.setText(brodcast.getId());
                    if (brodcast.getImageURL().equals("default")) {
                        profile_image.setImageResource(R.mipmap.ic_launcher);
                    } else {
                        //and this
                        Glide.with(getApplicationContext()).load(brodcast.getImageURL()).into(profile_image);
                    }

                    readMesagges(fuser.getUid(), userid, brodcast.getImageURL(), chatType);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }

        seenMessage(userid);
    }

    private void seenMessage(final String userid) {
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(fuser.getUid()) && chat.getSender().equals(userid)) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isseen", true);
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // toolbar.setNavigationIcon(R.drawable.ic_menu);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setTitle("SilentQuot");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        //Tools.setSystemBarColor(this, R.color.grey_1000);
    }

    private void uploadImage() {

        storageReference = FirebaseStorage.getInstance().getReference("Uploads");
        if (imgurl != null) {
            final StorageReference fileReference = storageReference.child("chatImage/" + ImageName);
            uploadTask = fileReference.putFile(imgurl);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        // throw  task.getException();
                    }
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        String mUri = downloadUri.toString();
                        sendMessage(fuser.getUid(), userid, mUri, "Image", chatType);
                    } else {
                        Toast.makeText(MessageActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MessageActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });
        } else {
            Toast.makeText(MessageActivity.this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendMessage(String sender, final String receiver, String message, String msgtype, final String chatType) {

        boolean isfrd=isFriend(fuser.getUid(), userid);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        if (("userchat").equals(chatType)) {
            final HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("sender", sender);
            hashMap.put("receiver", receiver);
            hashMap.put("message", message);
            hashMap.put("isseen", false);
            hashMap.put("msgtype", msgtype);


            // add user to chat fragment
            final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                    .child(fuser.getUid())
                    .child(userid);

            final DatabaseReference finalReference = reference;
            chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        final DatabaseReference isFrd = FirebaseDatabase.getInstance().getReference("friends")
                                .child(fuser.getUid())
                                .child(userid);

                        isFrd.addListenerForSingleValueEvent(new ValueEventListener() {

                        @Override
                        public void onDataChange(@NonNull DataSnapshot isFrdDatasnapShot) {
                            if(isFrdDatasnapShot.exists())
                            {
                                isFriend=true;

                                if (!dataSnapshot.exists())
                                {
                                    key = chatRef.push().getKey();
                                    chatRef.child("id").setValue(userid);
                                    chatRef.child("chat_type").setValue(chatType);
                                    chatRef.child("key").setValue(key);
                                    chatRef.child("timestamp").setValue(ServerValue.TIMESTAMP);
                                    hashMap.put("chat_key", key);

                                    final DatabaseReference chatRefReceiver = FirebaseDatabase.getInstance().getReference("Chatlist")
                                            .child(userid).child(fuser.getUid());

                                    chatRefReceiver.child("id").setValue(fuser.getUid());
                                    chatRefReceiver.child("chat_type").setValue(chatType);
                                    chatRefReceiver.child("key").setValue(key);
                                    chatRefReceiver.child("timestamp").setValue(ServerValue.TIMESTAMP);
                                    finalReference.child("Chats").push().setValue(hashMap);
                        } else {
                            hashMap.put("chat_key", chat_key);
                            finalReference.child("Chats").push().setValue(hashMap);
                            final DatabaseReference chatRefReceiver = FirebaseDatabase.getInstance().getReference("Chatlist")
                                    .child(userid)
                                    .child(fuser.getUid());

                            Map map = new HashMap();
                            map.put("timestamp", ServerValue.TIMESTAMP);

                            chatRef.updateChildren(map, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                                }
                            });
                            chatRefReceiver.updateChildren(map, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    //messageAdapter.notifyDataSetChanged();

                                }
                            });

//                chatRefReceiver.child("timestamp").setValue( ServerValue.TIMESTAMP);
                        }

                            }
                            else {
                               Toast.makeText(MessageActivity.this , "Sorry !  You can't Send message to This person  , Send a request To Start Communication"  , Toast.LENGTH_LONG);
                            }
                        }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(MessageActivity.this , "Sorry !  You can't Send message to This person  , Send a request To Start Communication .."  , Toast.LENGTH_LONG);
                            }
                        });


                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


            final String msg = message;
            reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    if (notify) {
                        sendNotifiaction(receiver, user.getUsername(), msg);
                    }
                    notify = false;
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });

        }
        if (("brodcast").equals(chatType)) {
//
            DatabaseReference finalReference1 = reference;
//
            boolean isadmin=isAdmin(fuser.getUid() , userid);

                        if ( isadmin){
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("message", message);
                            hashMap.put("msgtype", msgtype);

                            final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                                    .child(fuser.getUid())
                                    .child(userid);


                            chatRef.child("timestamp").setValue(ServerValue.TIMESTAMP);
                            finalReference1.child("BrodcastChats").child(userid).push().setValue(hashMap);
                        } else {
                            Toast.makeText(MessageActivity.this, "Sorry You Can Not Send Message To This BrodCast ! ", Toast.LENGTH_LONG).show();
                        }

                    }
//
//
        }



    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imgurl = data.getData();


            Cursor returnCursor =
                    getContentResolver().query(imgurl, null, null, null, null);
            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            returnCursor.moveToFirst();
            ImageName = returnCursor.getString(nameIndex);

            text_send.setText(ImageName);

        }
    }


    private void sendNotifiaction(String receiver, final String username, final String message) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(fuser.getUid(), R.mipmap.ic_launcher, username + ": " + message, "New Message",
                            userid);

                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200) {
                                        if (response.body().success != 1) {
                                            Toast.makeText(MessageActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readMesagges(final String myid, final String userid, final String imageurl, String chat_type) {
        if (("brodcast").equals(chat_type)) {
            bChat = new ArrayList<>();
            reference = FirebaseDatabase.getInstance().getReference("BrodcastChats").child(userid);
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    bChat.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        BrodcastChat chat = snapshot.getValue(BrodcastChat.class);
                        bChat.add(chat);
                        brodcastAdapter = new BrodcastAdapter(MessageActivity.this, bChat, imageurl);
                        recyclerView.setAdapter(brodcastAdapter);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }

        if (("userchat").equals(chat_type)) {

            mchat = new ArrayList<>();
            //Query query= FirebaseDatabase.getInstance().getReference("Chats");
            reference = FirebaseDatabase.getInstance().getReference("Chats");
            Query query = reference.orderByChild("chat_key").equalTo(chat_key);
            // Task query=reference.orderByChild("sender").equalTo(userid);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    mchat.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Chat chat = snapshot.getValue(Chat.class);
                        if (chat.getReceiver().equals(myid) && chat.getSender().equals(userid) ||
                                chat.getReceiver().equals(userid) && chat.getSender().equals(myid)) {
                            mchat.add(chat);
                        }
                        messageAdapter = new MessageAdapter(MessageActivity.this, mchat, imageurl);
                        recyclerView.setAdapter(messageAdapter);
                        messageAdapter.notifyDataSetChanged();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    boolean isAdmin=false;
    private boolean isAdmin(String userid, String brodcastId) {
        //CountDownLatch done =  new CountDownLatch(1);
        DatabaseReference brodcastAdmin = FirebaseDatabase.getInstance().getReference("Brodcast").child(brodcastId).child("admin");
        brodcastAdmin.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String admin = null;
                if (dataSnapshot.exists()) {
                    try {

                        admin = dataSnapshot.getValue(String.class);

                        if (admin != null && admin.equals(userid)) {
                            isAdmin=true;
                        }
                        else
                        {
                            isAdmin=false;
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return  isAdmin;
    }



            boolean isFriend = false;
    private  boolean isFriend(String userid_1 , String userid_2 )
    {

        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("friends")
                .child(userid_1)
                .child(userid_2);

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    isFriend=true;
                }
                else {
                    isFriend=false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        return  isFriend;
    }

    private void currentUser(String userid){
        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
        editor.putString("currentuser", userid);
        editor.apply();
    }

    private void status(String status){
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        reference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
        currentUser(userid);
    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListener);
        status("offline");
        currentUser("none");
    }
}
