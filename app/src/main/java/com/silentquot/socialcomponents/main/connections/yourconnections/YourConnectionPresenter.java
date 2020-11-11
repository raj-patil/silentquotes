/*
 * Copyright (c) 2020. Created By Raj Patil
 */

package com.silentquot.socialcomponents.main.connections.yourconnections;

import android.app.Activity;

import com.google.firebase.auth.FirebaseAuth;
import com.silentquot.socialcomponents.main.base.BasePresenter;
import com.silentquot.socialcomponents.managers.ConnecteManager;
import com.silentquot.socialcomponents.managers.ProfileManager;
import com.silentquot.socialcomponents.utils.LogUtil;

public class YourConnectionPresenter extends BasePresenter<YourConnectionView> {
    private final ConnecteManager connecteManager;
    private String currentUserId;
    private Activity activity;
    private ProfileManager profileManager;

    public YourConnectionPresenter(Activity activity) {
        super(activity);
        this.activity = activity;
        connecteManager = ConnecteManager.getInstance(context);
        currentUserId = FirebaseAuth.getInstance().getUid();
        profileManager = ProfileManager.getInstance(context.getApplicationContext());
    }


    public void loadUsersWithEmptySearch() {
        search("");
    }

    public void search(String searchText) {
        if (checkInternetConnection()) {
            ifViewAttached(YourConnectionView::showLocalProgress);
            profileManager.searchConnection(searchText, list -> {
                ifViewAttached(view -> {
                    view.hideLocalProgress();
                    view.onSearchResultsReady(list);

                    if (list.isEmpty()) {
                        view.showEmptyListLayout();
                    }
                });
                LogUtil.logDebug(TAG, "search text: " + searchText);
                LogUtil.logDebug(TAG, "found items count: " + list.size());
            });
        } else {
            ifViewAttached(YourConnectionView::hideLocalProgress);
        }
    }

}
