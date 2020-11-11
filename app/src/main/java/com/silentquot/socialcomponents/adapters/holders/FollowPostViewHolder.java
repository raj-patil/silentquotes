package com.silentquot.socialcomponents.adapters.holders;

import android.view.View;

import com.silentquot.socialcomponents.main.base.BaseActivity;
import com.silentquot.socialcomponents.managers.listeners.OnPostChangedListener;
import com.silentquot.socialcomponents.model.FollowingPost;
import com.silentquot.socialcomponents.model.Post;
import com.silentquot.socialcomponents.utils.LogUtil;

/**
 * Created by Alexey on 22.05.18.
 */
public class FollowPostViewHolder extends PostViewHolder {

    private int TEXT_POST=1;
    private  int IMAGE_POST=2;

    public FollowPostViewHolder(View view, OnClickListener onClickListener, BaseActivity activity) {
        super(view, onClickListener, activity);
    }

    public FollowPostViewHolder(View view, OnClickListener onClickListener, BaseActivity activity, boolean isAuthorNeeded , int POST_TYPE) {
        super(view, onClickListener, activity, isAuthorNeeded , POST_TYPE);
    }

    public void bindData(FollowingPost followingPost) {
        postManager.getSinglePostValue(followingPost.getPostId(), new OnPostChangedListener() {
            @Override
            public void onObjectChanged(Post obj) {
                bindData(obj);
            }

            @Override
            public void onError(String errorText) {
                LogUtil.logError(TAG, "bindData", new RuntimeException(errorText));
            }
        });
    }

    public void bindDataWithText(FollowingPost followingPost) {
        postManager.getSinglePostValue(followingPost.getPostId(), new OnPostChangedListener() {
            @Override
            public void onObjectChanged(Post obj) {
                bindDataWithText(obj);
            }

            @Override
            public void onError(String errorText) {
                LogUtil.logError(TAG, "bindData", new RuntimeException(errorText));
            }
        });

    }

    public void bindDataWithCollab(FollowingPost followingPost){
        postManager.getSinglePostValue(followingPost.getPostId(), new OnPostChangedListener() {
            @Override
            public void onObjectChanged(Post obj) {
                bindDataWithCollab(obj);
            }

            @Override
            public void onError(String errorText) {
                LogUtil.logError(TAG, "bindData", new RuntimeException(errorText));
            }
        });

    }
}
