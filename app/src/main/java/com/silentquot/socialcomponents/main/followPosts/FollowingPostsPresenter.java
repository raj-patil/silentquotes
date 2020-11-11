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

package com.silentquot.socialcomponents.main.followPosts;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;

import com.silentquot.R;
import com.silentquot.socialcomponents.enums.PostStatus;
import com.silentquot.socialcomponents.main.base.BasePresenter;
import com.silentquot.socialcomponents.main.postDetails.PostDetailsActivity;
import com.silentquot.socialcomponents.managers.PostManager;
import com.silentquot.socialcomponents.managers.listeners.OnPostChangedListener;
import com.silentquot.socialcomponents.model.Post;

/**
 * Created by Alexey on 03.05.18.
 */

public class FollowingPostsPresenter extends BasePresenter<FollowPostsView> {

    private PostManager postManager;

    public FollowingPostsPresenter(Context context) {
        super(context);
        postManager = PostManager.getInstance(context);
    }


    void onCollabContainerClick(String postId , View postView){


        postManager.getSinglePostValue(postId, new OnPostChangedListener(){
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

    void onPostClicked(final String postId, final View postView) {
        postManager.isPostExistSingleValue(postId, exist -> ifViewAttached(view -> {
            if (exist) {
                view.openPostDetailsActivity(postId, postView);
            } else {
                view.showSnackBar(R.string.error_post_was_removed);
            }
        }));
    }

    public void loadFollowingPosts() {
        if (checkInternetConnection()) {
            if (getCurrentUserId() != null) {
                ifViewAttached(FollowPostsView::showLocalProgress);
                postManager.getFollowingPosts(getCurrentUserId(), list -> ifViewAttached(view -> {
                    view.hideLocalProgress();
                    view.onFollowingPostsLoaded(list);
                    view.showEmptyListMessage(list.isEmpty());
                }));
            } else {
                ifViewAttached(view -> {
                    view.showEmptyListMessage(true);
                    view.hideLocalProgress();
                });
            }
        } else {
            ifViewAttached(FollowPostsView::hideLocalProgress);
        }
    }
    void onCreatePostClickAction(View anchorView) {
        if (checkInternetConnection(anchorView)) {
            if (checkAuthorization()) {
                ifViewAttached(FollowPostsView::openCreatePostActivity);
            }
        }
    }

    public void onRefresh() {
        loadFollowingPosts();
    }


    public  void  onCollabClick(String postid , View view)
    {
        postManager.CollabPost(postid , view.getContext());
//        postManager.getSinglePostValue(postid, new OnPostChangedListener(){
//
//            @Override
//            public void onObjectChanged(Post obj) {
//
//                postManager.CollabPost(obj,view.getContext());
//            }
//
//            @Override
//            public void onError(String errorText) {
//
//            }
//        });
    }
    public void onShareClick(String postid, ImageView view , View mainView)
    {
        postManager.getSinglePostValue(postid, new OnPostChangedListener(){

            @Override
            public void onObjectChanged(Post obj) {

                postManager.sharePost(obj , view, context);
            }

            @Override
            public void onError(String errorText) {

            }
        });

    }

    public  void onCopyClick(String postid )
    {
        postManager.getSinglePostValue(postid, new OnPostChangedListener(){

                    @Override
                    public void onObjectChanged(Post obj) {
                        postManager.copyTextFromPost(obj);
                    }

                    @Override
                    public void onError(String errorText) {

                    }
                });

    }


    public void onAuthorClick(String postId, View authorView) {
        postManager.getSinglePostValue(postId, new OnPostChangedListener() {
            @Override
            public void onObjectChanged(Post obj) {
                ifViewAttached(view -> view.openProfileActivity(obj.getAuthorId(), authorView));
            }

            @Override
            public void onError(String errorText) {
                ifViewAttached(view -> view.showSnackBar(errorText));
            }
        });
    }

    void onPostUpdated(Intent data) {
        if (data != null) {
            ifViewAttached(view -> {
                PostStatus postStatus = (PostStatus) data.getSerializableExtra(PostDetailsActivity.POST_STATUS_EXTRA_KEY);
                if (postStatus.equals(PostStatus.REMOVED)) {
                    view.removepost();
                    //view.showFloatButtonRelatedSnackBar(R.string.message_post_was_removed);
                } else if (postStatus.equals(PostStatus.UPDATED)) {
                   view.updatePost();
                }
            });
        }
    }

    public void onCollabAutherClick(String postId, View collabAutherview) {

            postManager.getSinglePostValue(postId, new OnPostChangedListener(){

                @Override
                public void onObjectChanged(Post obj) {

                    postManager.getSinglePostValue(obj.getCollabPostId(), new OnPostChangedListener() {
                        @Override
                        public void onObjectChanged(Post obj) {
                            ifViewAttached(view -> view.openProfileActivity(obj.getAuthorId(), collabAutherview));
                        }

                        @Override
                        public void onError(String errorText) {

                        }
                    });
                }

                @Override
                public void onError(String errorText) {

                }
            });

    }
}
