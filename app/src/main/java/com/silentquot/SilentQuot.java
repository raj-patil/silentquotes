//package com.silentquot;
//
//import android.app.Application;
//
//import com.google.firebase.database.FirebaseDatabase;
//import com.silentquot.socialcomponents.main.interactors.PostInteractor;
//
//public class SilentQuot extends Application {
//    public static final String TAG = com.silentquot.Application.class.getSimpleName();
//    @Override
//    public void onCreate() {
//        super.onCreate();
//        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
//        ApplicationHelper.initDatabaseHelper(this);
//        PostInteractor.getInstance(this).subscribeToNewPosts();
//
//    }
//}
