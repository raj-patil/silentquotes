package com.silentquot.socialcomponents.managers;

import android.app.Activity;
import android.content.Context;

import com.silentquot.socialcomponents.enums.ConnectionState;
import com.silentquot.socialcomponents.main.interactors.ConnectInteractor;
import com.silentquot.socialcomponents.managers.listeners.OnObjectExistListener;
import com.silentquot.socialcomponents.managers.listeners.OnRequestComplete;
import com.silentquot.socialcomponents.utils.LogUtil;

public class ConnecteManager extends  FirebaseListenersManager {

    private static final String TAG = FollowManager.class.getSimpleName();
    private static ConnecteManager instance;
    private ConnectInteractor connectInteractor;

    private Context context;

    public static ConnecteManager getInstance(Context context) {
        if (instance == null) {
            instance = new ConnecteManager(context);
        }

        return instance;
    }

    private ConnecteManager(Context context) {
        this.context = context;
        connectInteractor = ConnectInteractor.getInstance(context);
    }



    public void checkConnectState(String myId, String userId, CheckStateListener checkStateListener) {
        isConnectionExits(myId,userId,isConnected ->{

            if (isConnected)
            {   ConnectionState connectionState;
                connectionState = ConnectionState.CONNECTED_EACH_OTHER;
                checkStateListener.onStateReady(connectionState);
            }
            else
            {
                doseRequestedMe(myId, userId, userRequestedMe -> {

                    doIRequestedUser(myId, userId, iRequestedUser -> {
                        ConnectionState connectionState;
                        if (!userRequestedMe && !iRequestedUser) {
                            connectionState=ConnectionState.NO_ONE_CONNECTED;
                        } else if (userRequestedMe) {
                            connectionState=ConnectionState.USER_SEND_CONNECT_REQUEST_TO_ME;
                        } else if (iRequestedUser) {
                            connectionState=ConnectionState.I_SEND_CONNECT_REQUEST_TO_USER;
                        } else {
                            connectionState=ConnectionState.NO_ONE_CONNECTED;
                        }

                        checkStateListener.onStateReady(connectionState);

                        LogUtil.logDebug(TAG, "checkFollowState, state: " + connectionState);
                    });
                });
            }

        });

    }

    public void isConnectionExits(String myid , String userId , final  OnObjectExistListener onObjectExistListener)
    {
        connectInteractor.isConnectionExit(myid , userId , onObjectExistListener);
    }

    public void doseRequestedMe(String myId, String userId, final OnObjectExistListener onObjectExistListener) {
        connectInteractor.isRequestExist(userId, myId, onObjectExistListener);
    }

    public void doIRequestedUser(String myId, String userId, final OnObjectExistListener onObjectExistListener) {
        connectInteractor.isRequestExist(myId, userId, onObjectExistListener);
    }

    public void cancelUser(Activity activity, String currentUserId, String targetUserId, OnRequestComplete onRequestComplete)
    {
        connectInteractor.cancelUser(activity, currentUserId, targetUserId, onRequestComplete);
    }

    public void connectUser(Activity activity, String currentUserId, String targetUserId, OnRequestComplete onRequestComplete) {
        connectInteractor.connectUser(activity, currentUserId, targetUserId, onRequestComplete);
    }
    public void acceptUser(Activity activity,String userName ,String targetUserName, String currentUserId, String targetUserId, OnRequestComplete onRequestComplete) {
        connectInteractor.acceptUser(activity,userName, targetUserName, currentUserId, targetUserId, onRequestComplete);
    }

    public interface CheckStateListener {
        void onStateReady(ConnectionState connectionState);
    }


}
