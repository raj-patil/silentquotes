package com.silentquot;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.silentquot.Fragments.UserActivityProfileFragment;
import com.silentquot.Model.User;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    String UID;
    CircleImageView userProfileImage;
    TextView userProfileUserName;
    DatabaseReference reference;
    Button btn_Request_1,btn_Request_2;

    //
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mFriendRequestDatabase;
    private FirebaseUser currentUser;
    private DatabaseReference mRootDatabase;
    //constants
    private int mCurrentState;


    TabLayout tabLayout;
    ViewPager viewPager;
     ViewPagerAdapter viewPagerAdapter;
    String chat_key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        UID = getIntent().getExtras().getString("userid");
        Bundle bundle = new Bundle();
        bundle.putString("userId",UID);
        UserActivityProfileFragment fragobj = new UserActivityProfileFragment();
        fragobj.setArguments(bundle);

        initToolbar();

        userProfileUserName = findViewById(R.id.profile_user_name);
        userProfileImage = findViewById(R.id.profile_user_image);
        btn_Request_1=findViewById(R.id.profile_btn_request1);
        btn_Request_2=findViewById(R.id.profile_btn_request2);


        tabLayout = findViewById(R.id.profile_tab_layout);
        viewPager = findViewById(R.id.view_pager_profile);
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);


        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        viewPagerAdapter.addFragment(new UserActivityProfileFragment() , "Activity");
        viewPagerAdapter.notifyDataSetChanged();




        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        final String mCurrentUserId = currentUser.getUid();
        mRootDatabase =FirebaseDatabase.getInstance().getReference();
        mFriendsDatabase =  FirebaseDatabase.getInstance().getReference().child("friends");
        mFriendRequestDatabase =  FirebaseDatabase.getInstance().getReference().child("friend_req");
        mUsersDatabase = FirebaseDatabase.getInstance().getReference("Users").child(UID);

        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    User user = dataSnapshot.getValue(User.class);
                    userProfileUserName.setText(user.getUsername());
                    if (user.getImageURL().equals("default")) {
                        userProfileImage.setImageResource(R.mipmap.ic_launcher);
                    } else {
                        Glide.with(getApplicationContext()).load(user.getImageURL()).into(userProfileImage);
                    }


                mFriendRequestDatabase.child(mCurrentUserId).addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.hasChild(UID)){
                                    String req_type = dataSnapshot.child(UID).child("request_type").getValue().toString();
                                    if(req_type.equals("received")){
                                        btn_Request_2.setEnabled(true);
                                        btn_Request_2.setVisibility(View.VISIBLE);
                                        mCurrentState =2;
                                        btn_Request_1.setText("Accept Request");
                                    }else { if(req_type.equals("sent")){
                                        mCurrentState=1;
                                        btn_Request_1.setText("Cancel Request ");
                                    } }
                                }else {
                                    mFriendsDatabase.child(mCurrentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.hasChild(UID)){
                                                mCurrentState =3;
                                                btn_Request_1.setText("Unfriend ");
                                                btn_Request_2.setEnabled(true);
                                                btn_Request_2.setVisibility(View.VISIBLE);
                                                btn_Request_2.setText("Message");
                                            } }
                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                        }});
                                }
                              //  mProgressDialog.dismiss();
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) { //mProgressDialog.dismiss();//
                                 }
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        btn_Request_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (("Message").equals(btn_Request_2.getText())) {

                    final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                            .child(mCurrentUserId)
                            .child(UID).child("key");

                    chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (!dataSnapshot.exists()) {

                                 chat_key="";
                            }
                            else {
                                chat_key=dataSnapshot.getValue(String.class);
                            }


                            Intent messageeActivity = new Intent(getApplicationContext(), MessageActivity.class);
                            messageeActivity.putExtra("userid",UID);
                            messageeActivity.putExtra("chatType" , "userchat");
                            messageeActivity.putExtra("chat_key" , chat_key);
                            startActivity(messageeActivity);

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });




                } else {
                    Map map = new HashMap();
                    map.put("friend_req/" + mCurrentUserId + "/" + UID + "/id", null);
                    map.put("friend_req/" + mCurrentUserId + "/" + UID + "/request_type", null);
                    map.put("friend_req/" + UID + "/" + mCurrentUserId + "/id", null);
                    map.put("friend_req/" + UID + "/" + mCurrentUserId + "/request_type", null);
                    mRootDatabase.updateChildren(map, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            btn_Request_1.setEnabled(true);
                            mCurrentState = 0;
                            btn_Request_1.setText("Send Request");
                            btn_Request_2.setVisibility(View.INVISIBLE);
                            btn_Request_2.setEnabled(false);
                        }
                    });
                }
            }
        });

        btn_Request_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_Request_1.setEnabled(false);
                //0: send request state
                if(mCurrentState==0){ sendFriendRequest(mCurrentUserId,UID); }
                //1: cancel request state
                if(mCurrentState==1){ cancelFriendRequest(mCurrentUserId,UID); }
                //2: Accept Friend Request
                if(mCurrentState==2){ acceptFriendRequest(mCurrentUserId,UID); }
                //3: UnFriend This  person
                if(mCurrentState==3){ unFriendThisPerson(mCurrentUserId,UID); }
            }
        });

    }

    private void unFriendThisPerson(final String mCurrentUserId,final String uid) {
        Map map = new HashMap();
        map.put("friends/"+ mCurrentUserId + "/" + uid + "/date",null);
        map.put("friends/"+ mCurrentUserId + "/" + uid + "/id",null);
        map.put("friends/"+ uid + "/" + mCurrentUserId + "/date",null);
        map.put("friends/"+ uid + "/" + mCurrentUserId + "/id",null);
        mRootDatabase.updateChildren(map, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                btn_Request_1.setEnabled(true);
                mCurrentState=0;
                btn_Request_1.setText("Send  request");
            }
        });
    }

    private void acceptFriendRequest(final String mCurrentUserId,final String uid) {
        final String date = DateFormat.getDateTimeInstance().format(new Date());
        Map map = new HashMap();
        map.put("friends/"+ mCurrentUserId + "/" + uid + "/date",date);
        map.put("friends/"+ mCurrentUserId + "/" + uid + "/id",uid);
        map.put("friends/"+ uid + "/" + mCurrentUserId + "/date",date);
        map.put("friends/"+ uid + "/" + mCurrentUserId + "/id",mCurrentUserId);
        map.put("friend_req/"+mCurrentUserId + "/" + uid + "/id",null);
        map.put("friend_req/"+mCurrentUserId + "/" + uid + "/request_type",null);
        map.put("friend_req/"+uid + "/" + mCurrentUserId + "/id",null);
        map.put("friend_req/"+uid + "/" + mCurrentUserId + "/request_type",null);

        mRootDatabase.updateChildren(map, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                btn_Request_1.setEnabled(true);
                mCurrentState=3;
                btn_Request_1.setText("Unfriend ");
                btn_Request_2.setText("Message");
                btn_Request_2.setVisibility(View.VISIBLE);
                btn_Request_2.setEnabled(true);
            }
        });
    }

    private void cancelFriendRequest(final String mCurrentUserId,final String uid) {
        Map map = new HashMap();
        map.put("friend_req/"+mCurrentUserId + "/" + uid + "/id",null);
        map.put("friend_req/"+mCurrentUserId + "/" + uid + "/request_type",null);
        map.put("friend_req/"+uid + "/" + mCurrentUserId + "/id",null);
        map.put("friend_req/"+uid + "/" + mCurrentUserId + "/request_type",null);

        mRootDatabase.updateChildren(map, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                btn_Request_1.setEnabled(true);
                mCurrentState= 0;//1 for freindRequestSent
                btn_Request_1.setText("Send Request");
            }
        });
    }

    private void sendFriendRequest(final String mCurrentUserId,final String uid) {
        String key= mRootDatabase.child("notification").child(uid).push().getKey().toString();
        HashMap<String,String> notificationData = new HashMap<String, String>();
        notificationData.put("from",mCurrentUserId);
        notificationData.put("type","request");
        Map map = new HashMap();
        map.put("friend_req/"+ mCurrentUserId + "/" + uid + "/id",uid);
        map.put("friend_req/"+ mCurrentUserId + "/" + uid + "/request_type","sent");
        map.put("friend_req/"+ uid + "/" + mCurrentUserId + "/id",mCurrentUserId);
        map.put("friend_req/"+ uid + "/" + mCurrentUserId + "/request_type","received");
        //map.put("notification/" + uid +"/" + key ,notificationData);
        mRootDatabase.updateChildren(map, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                btn_Request_1.setEnabled(true);
                mCurrentState= 1;//1 for freindRequestSent
                btn_Request_1.setText("Cancel Request");
            }

        });
    }


    class ViewPagerAdapter extends FragmentPagerAdapter {

        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;

        ViewPagerAdapter(FragmentManager fm){
            super(fm);

            this.fragments = new ArrayList<>();
            this.fragments.clear();
            this.titles = new ArrayList<>();
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        public void addFragment(Fragment fragment, String title){
            fragments.add(fragment);
            titles.add(title);
        }

        // Ctrl + O

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }


    public  String getUserId()
    {
        return  UID;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // toolbar.setNavigationIcon(R.drawable.ic_menu);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setTitle("SilentQuot");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //Tools.setSystemBarColor(this, R.color.grey_1000);

    }


}