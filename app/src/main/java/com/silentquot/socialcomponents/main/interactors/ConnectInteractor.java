package com.silentquot.socialcomponents.main.interactors;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.silentquot.ApplicationHelper;
import com.silentquot.socialcomponents.managers.DatabaseHelper;
import com.silentquot.socialcomponents.managers.listeners.OnCountChangedListener;
import com.silentquot.socialcomponents.managers.listeners.OnDataChangedListener;
import com.silentquot.socialcomponents.managers.listeners.OnObjectExistListener;
import com.silentquot.socialcomponents.managers.listeners.OnRequestComplete;
import com.silentquot.socialcomponents.model.Connection;
import com.silentquot.socialcomponents.model.Follower;
import com.silentquot.socialcomponents.model.Following;
import com.silentquot.socialcomponents.utils.LogUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConnectInteractor {

    private static final String TAG = ConnectInteractor.class.getSimpleName();
    private static ConnectInteractor instance;

    private DatabaseHelper databaseHelper;
    private Context context;

    public static ConnectInteractor getInstance(Context context) {
        if (instance == null) {
            instance = new ConnectInteractor(context);
        }

        return instance;
    }

    private ConnectInteractor(Context context) {
        this.context = context;
        databaseHelper = ApplicationHelper.getDatabaseHelper();
    }



    private DatabaseReference getFollowersRef(String userId) {
        return databaseHelper
                .getDatabaseReference()
                .child(DatabaseHelper.FOLLOW_DB_KEY)
                .child(userId)
                .child(DatabaseHelper.FOLLOWERS_DB_KEY);
    }

    private DatabaseReference getFollowingsRef(String userId) {
        return databaseHelper
                .getDatabaseReference()
                .child(DatabaseHelper.FOLLOW_DB_KEY)
                .child(userId)
                .child(DatabaseHelper.FOLLOWINGS_DB_KEY);
    }

    public void getFollowersList(String targetUserId, OnDataChangedListener<String> onDataChangedListener) {
        getFollowersRef(targetUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> list = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Follower follower = snapshot.getValue(Follower.class);

                    if (follower != null) {
                        String profileId = follower.getProfileId();
                        list.add(profileId);
                    }

                }
                onDataChangedListener.onListChanged(list);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logDebug(TAG, "getFollowersList, onCancelled");
            }
        });
    }

    public void getFollowingsList(String targetUserId, OnDataChangedListener<String> onDataChangedListener) {
        getFollowingsRef(targetUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> list = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Following following = snapshot.getValue(Following.class);

                    if (following != null) {
                        String profileId = following.getProfileId();
                        list.add(profileId);
                    }

                }
                onDataChangedListener.onListChanged(list);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logDebug(TAG, "getFollowingsList, onCancelled");
            }
        });
    }

    public ValueEventListener getFollowingsCount(String targetUserId, OnCountChangedListener onCountChangedListener) {
        return getFollowingsRef(targetUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                onCountChangedListener.onCountChanged(dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logDebug(TAG, "getFollowingsCount, onCancelled");
            }
        });
    }

    public ValueEventListener getFollowersCount(String targetUserId, OnCountChangedListener onCountChangedListener) {
        return getFollowersRef(targetUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                onCountChangedListener.onCountChanged(dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logDebug(TAG, "getFollowersCount, onCancelled");
            }
        });
    }

    public void isConnectionExit(String userId_1 , String userId_2 ,final OnObjectExistListener onObjectExistListener)
    {
        DatabaseReference connectionRef1 = databaseHelper
                .getDatabaseReference()
                .child(DatabaseHelper.CONNECTION_DB_KEY)
                .child(userId_1)
                .child(userId_2);


        DatabaseReference connectionRef2 = databaseHelper
                .getDatabaseReference()
                .child(DatabaseHelper.CONNECTION_DB_KEY)
                .child(userId_2)
                .child(userId_1);


        connectionRef1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                    connectionRef2.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists())
                            {
                                onObjectExistListener.onDataChanged(dataSnapshot.exists());
                            }
                            else
                            {
                                onObjectExistListener.onDataChanged(false);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                else
                {
                    onObjectExistListener.onDataChanged(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void isRequestExist(String connectUserId, String reQuestedUserId, final OnObjectExistListener onObjectExistListener) {
        DatabaseReference connectReqRef = databaseHelper
                .getDatabaseReference()
                .child(DatabaseHelper.REQUEST_DB_KEY)
                .child(connectUserId)
                .child(reQuestedUserId);


        connectReqRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String requestType=dataSnapshot.child("requestType").getValue().toString();
                           if (requestType!=null &&requestType.equals("sent")) {
                               onObjectExistListener.onDataChanged(dataSnapshot.exists());
                           }
                           else
                           {
                               onObjectExistListener.onDataChanged(false);
                           }

                } else {
                    onObjectExistListener.onDataChanged(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logDebug(TAG, "isFollowingExist, onCancelled");
            }
        });
    }

    private Task<Void> addConnection(String currentUSerId, String connectingUserId , String requestType)
    {
        Connection  connection=new Connection(connectingUserId ,requestType);
        return databaseHelper.getDatabaseReference()
                .child(DatabaseHelper.REQUEST_DB_KEY)
                .child(currentUSerId)
                .child(connectingUserId)
                .setValue(connection);
    }

    private Task<Void> removeFollowing(String followerUserId, String followingUserId) {
        return databaseHelper
                .getDatabaseReference()
                .child(DatabaseHelper.FOLLOW_DB_KEY)
                .child(followerUserId)
                .child(DatabaseHelper.FOLLOWINGS_DB_KEY)
                .child(followingUserId)
                .removeValue();
    }

    private Task<Void> removeFollower(String followerUserId, String followingUserId) {
        return databaseHelper
                .getDatabaseReference()
                .child(DatabaseHelper.FOLLOW_DB_KEY)
                .child(followingUserId)
                .child(DatabaseHelper.FOLLOWERS_DB_KEY)
                .child(followerUserId).removeValue();
    }

    public void unfollowUser(Activity activity, String followerUserId, String followingUserId, final OnRequestComplete onRequestComplete) {
        removeFollowing(followerUserId, followingUserId)
                .continueWithTask(task -> removeFollower(followerUserId, followingUserId))
                .addOnCompleteListener(activity, task -> {
                    onRequestComplete.onComplete(task.isSuccessful());
                    LogUtil.logDebug(TAG, "unfollowUser " + followingUserId + ", success: " + task.isSuccessful());
                });

    }

    public void connectUser(Activity activity, String currentUserId, String connectingUserId, final OnRequestComplete onRequestComplete) {
        addConnection(currentUserId, connectingUserId , "sent")
                .continueWithTask(task -> addConnection(connectingUserId, currentUserId , "received"))
                .addOnCompleteListener(activity, task -> {
                    onRequestComplete.onComplete(task.isSuccessful());
                    LogUtil.logDebug(TAG, "followUser " + connectingUserId + ", success: " + task.isSuccessful());
                });
    }


    public void acceptUser(Activity activity, String userName, String targetUserName, String currentUserId, String targetUserId, OnRequestComplete onRequestComplete) {
        acceptConnection(currentUserId, targetUserId,targetUserName.toLowerCase() )
                .continueWithTask(task -> acceptConnection(targetUserId, currentUserId , userName.toLowerCase()))
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful())
                    {
                        cancelUser(activity , currentUserId , targetUserId , onRequestComplete);
                    }
                    onRequestComplete.onComplete(task.isSuccessful());
                    LogUtil.logDebug(TAG, "followUser " + targetUserId + ", success: " + task.isSuccessful());
                });

    }

    private Task<Void> acceptConnection(String currentUserId, String targetUserId, String userName) {

        HashMap<String, Object> result = new HashMap<>();
        result.put("id", targetUserId);
        result.put("username" , userName);

        return databaseHelper.getDatabaseReference()
                .child(DatabaseHelper.CONNECTION_DB_KEY)
                .child(currentUserId)
                .child(targetUserId)
                .setValue(result);
    }

    public void cancelUser(Activity activity, String currentUserId, String targetUserId, OnRequestComplete onRequestComplete) {
        removeConnection(currentUserId, targetUserId)
                .continueWithTask(task -> removeConnection(targetUserId, currentUserId))
                .addOnCompleteListener(activity, task -> {
                    onRequestComplete.onComplete(task.isSuccessful());
                    LogUtil.logDebug(TAG, "unfollowUser " + targetUserId + ", success: " + task.isSuccessful());
                });


    }

    private Task<Void> removeConnection(String currentUserId, String targetUserId) {
        return databaseHelper
                .getDatabaseReference()
                .child(DatabaseHelper.REQUEST_DB_KEY)
                .child(currentUserId)
                .child(targetUserId)
                .removeValue();
    }
}
