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

package com.silentquot.socialcomponents.main.profile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;
import android.view.View;

import com.silentquot.R;
import com.silentquot.socialcomponents.enums.FollowState;
import com.silentquot.socialcomponents.enums.PostStatus;
import com.silentquot.socialcomponents.main.base.BasePresenter;
import com.silentquot.socialcomponents.main.base.BaseView;
import com.silentquot.socialcomponents.main.postDetails.PostDetailsActivity;
import com.silentquot.socialcomponents.managers.FollowManager;
import com.silentquot.socialcomponents.managers.PostManager;
import com.silentquot.socialcomponents.managers.ProfileManager;
import com.silentquot.socialcomponents.managers.listeners.OnObjectChangedListenerSimple;
import com.silentquot.socialcomponents.managers.listeners.OnPostChangedListener;
import com.silentquot.socialcomponents.model.Post;
import com.silentquot.socialcomponents.model.Profile;
import com.silentquot.socialcomponents.utils.LogUtil;

/**
 * Created by Alexey on 03.05.18.
 */

class ProfileFragmentPresenter extends BasePresenter<ProfileFragmentView> {

    private final FollowManager followManager;
    private Activity activity;
    private ProfileManager profileManager;
private PostManager postManager;
    private Profile profile;

    ProfileFragmentPresenter(Activity activity) {
        super(activity);
        this.activity = activity;
        profileManager = ProfileManager.getInstance(context);
        followManager = FollowManager.getInstance(context);
        postManager=PostManager.getInstance(context);
    }


    public void unfollowUser(String targetUserId) {
        ifViewAttached(BaseView::showProgress);
        followManager.unfollowUser(activity, getCurrentUserId(), targetUserId, success ->
                ifViewAttached(view -> {
                    if (success) {
                        view.setFollowStateChangeResultOk();
                        checkFollowState(targetUserId);
                    } else {
                        LogUtil.logDebug(TAG, "unfollowUser, success: " + false);
                    }
                }));
    }

    public void checkFollowState(String targetUserId) {
        String currentUserId = getCurrentUserId();

        if (currentUserId != null) {
            if (!targetUserId.equals(currentUserId)) {
                followManager.checkFollowState(currentUserId, targetUserId, followState -> {
                    ifViewAttached(view -> {
                        view.hideProgress();
                        view.updateFollowButtonState(followState);
                    });
                });
            } else {
                ifViewAttached(view -> view.updateFollowButtonState(FollowState.MY_PROFILE));
            }
        } else {
            ifViewAttached(view -> view.updateFollowButtonState(FollowState.NO_ONE_FOLLOW));
        }
    }

    public void getFollowersCount(String targetUserId) {
        followManager.getFollowersCount(context, targetUserId, count -> {
            ifViewAttached(view -> view.updateFollowersCount((int) count));
        });
    }

    public void getFollowingsCount(String targetUserId) {
        followManager.getFollowingsCount(context, targetUserId, count -> {
            ifViewAttached(view -> view.updateFollowingsCount((int) count));
        });
    }

    void onPostClick(Post post, View postItemView) {
        PostManager.getInstance(context).isPostExistSingleValue(post.getId(), exist -> {
            ifViewAttached(view -> {
                if (exist) {
                    view.openPostDetailsActivity(post.getId(), postItemView);
                } else {
                    view.showSnackBar(R.string.error_post_was_removed);
                }
            });
        });
    }

    public Spannable buildCounterSpannable(String label, int value) {
        SpannableStringBuilder contentString = new SpannableStringBuilder();
        contentString.append(String.valueOf(value));
        contentString.append("\n");
        int start = contentString.length();
        contentString.append(label);
        contentString.setSpan(new TextAppearanceSpan(context, R.style.TextAppearance_Second_Light), start, contentString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return contentString;
    }

    public void onEditProfileClick() {
        if (checkInternetConnection()) {
            ifViewAttached(ProfileFragmentView::startEditProfileActivity);
        }
    }

    public void onCreatePostClick() {
        if (checkInternetConnection()) {
            ifViewAttached(ProfileFragmentView::openCreatePostActivity);
        }
    }

    public void loadProfile(Context activityContext, String userID) {
        profileManager.getProfileValue(activityContext, userID, new OnObjectChangedListenerSimple<Profile>() {
            @Override
            public void onObjectChanged(Profile obj) {
                profile = obj;
                ifViewAttached(view -> {
                    view.setProfileName(profile.getUsername());

                    if (profile.getPhotoUrl() != null) {
                        view.setProfilePhoto(profile.getPhotoUrl());
                    } else {
                        view.setDefaultProfilePhoto();
                    }

                    int likesCount = (int) profile.getLikesCount();
                    String likesLabel = context.getResources().getQuantityString(R.plurals.likes_counter_format, likesCount, likesCount);
                    view.updateLikesCounter(buildCounterSpannable(likesLabel, likesCount));
                });
            }
        });
    }

    public void onPostListChanged(int postsCount) {
        ifViewAttached(view -> {
            String postsLabel = context.getResources().getQuantityString(R.plurals.posts_counter_format, postsCount, postsCount);
            view.updatePostsCounter(buildCounterSpannable(postsLabel, postsCount));
            view.showLikeCounter(true);
            view.showPostCounter(true);
            view.hideLoadingPostsProgress();


        });
    }

    public void checkPostChanges(Intent data) {
        ifViewAttached(view -> {
            if (data != null) {
                PostStatus postStatus = (PostStatus) data.getSerializableExtra(PostDetailsActivity.POST_STATUS_EXTRA_KEY);

                if (postStatus.equals(PostStatus.REMOVED)) {
                    view.onPostRemoved();
                } else if (postStatus.equals(PostStatus.UPDATED)) {
                    view.onPostUpdated();
                }
            }
        });
    }
    void onCollabContainerClick(String postId , View postView){


        postManager.getSinglePostValue(postId, new OnPostChangedListener() {

            @Override
            public void onObjectChanged(Post obj) {

                postManager.isPostExistSingleValue(obj.getCollabPostId(), exist -> ifViewAttached(view -> {
                    if (exist) {

                        view.openPostDetailsActivity(obj.getCollabPostId(), postView);
                    } else {
                        view.showSnackBar(R.string.error_post_was_removed);
                    }
                }));
            }

            @Override
            public void onError(String errorText) {

            }


        });
    }
}
