/*
 * Copyright 2018 Rozdoum
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.silentquot.socialcomponents.main.main;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.silentquot.R;
import com.silentquot.socialcomponents.enums.PostStatus;
import com.silentquot.socialcomponents.main.base.BasePresenter;
import com.silentquot.socialcomponents.main.postDetails.PostDetailsActivity;
import com.silentquot.socialcomponents.managers.PostManager;
import com.silentquot.socialcomponents.model.Post;



public class MainPresenter extends BasePresenter<MainView> {

    private PostManager postManager;

    public MainPresenter(Context context) {
        super(context);
        postManager = PostManager.getInstance(context);
    }


    public void onBookMarkClickAction(View anchorview)
    {
        if (checkInternetConnection(anchorview)) {
            if (checkAuthorization()) {

                ifViewAttached(MainView::openBookMarkActivity);
            }
        }
    }
    public void onCreatePostClickAction(View anchorView) {
        if (checkInternetConnection(anchorView)) {
            if (checkAuthorization()) {

                ifViewAttached(MainView::openCreatePostActivity);
            }
        }
    }

    void onPostClicked(final Post post, final View postView) {
        postManager.isPostExistSingleValue(post.getId(), exist -> ifViewAttached(view -> {
            if (exist) {
                view.openPostDetailsActivity(post, postView);
            } else {
                view.showFloatButtonRelatedSnackBar(R.string.error_post_was_removed);
            }
        }));
    }

    void onProfileMenuActionClicked() {
        if (checkAuthorization()) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            ifViewAttached(view -> view.openProfileActivity(userId, null));
        }
    }

    void onPostCreated() {
        ifViewAttached(view -> {
            view.refreshPostList();
            view.showFloatButtonRelatedSnackBar(R.string.message_post_was_created);
        });
    }

    void onPostUpdated(Intent data) {
        if (data != null) {
            ifViewAttached(view -> {
                PostStatus postStatus = (PostStatus) data.getSerializableExtra(PostDetailsActivity.POST_STATUS_EXTRA_KEY);
                if (postStatus.equals(PostStatus.REMOVED)) {
                    view.removePost();
                    view.showFloatButtonRelatedSnackBar(R.string.message_post_was_removed);
                } else if (postStatus.equals(PostStatus.UPDATED)) {
                    view.updatePost();
                }
            });
        }
    }

    void updateNewPostCounter() {
        Handler mainHandler = new Handler(context.getMainLooper());
        mainHandler.post(() -> ifViewAttached(view -> {
            int newPostsQuantity = postManager.getNewPostsCounter();
            if (newPostsQuantity > 0) {
                view.showCounterView(newPostsQuantity);
            } else {
                view.hideCounterView();
            }
        }));
    }

    public void initPostCounter() {
        postManager.setPostCounterWatcher(newValue -> updateNewPostCounter());
    }

    public void onConnectionClickAction(FloatingActionButton floatingActionButton) {
        if (checkInternetConnection(floatingActionButton)) {
            if (checkAuthorization()) {

                ifViewAttached(MainView::openConnectionActivity);
            }
        }
    }
}
