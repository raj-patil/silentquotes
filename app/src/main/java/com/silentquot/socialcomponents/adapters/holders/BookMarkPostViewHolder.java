package com.silentquot.socialcomponents.adapters.holders;

import android.app.Activity;
import android.view.View;

import com.silentquot.socialcomponents.main.interactors.PostInteractor;
import com.silentquot.socialcomponents.managers.listeners.OnPostChangedListener;
import com.silentquot.socialcomponents.model.BookMark;
import com.silentquot.socialcomponents.model.Post;
import com.silentquot.socialcomponents.utils.LogUtil;

public class BookMarkPostViewHolder  extends PostViewHolder {
    public BookMarkPostViewHolder(View view, OnClickListener onClickListener, Activity activity) {
        super(view, onClickListener, activity);
    }

    public BookMarkPostViewHolder(View view, OnClickListener onClickListener, Activity activity, boolean isAuthorNeeded, int POST_TYPE) {
        super(view, onClickListener, activity, isAuthorNeeded, POST_TYPE);
    }


    public void bindData(BookMark followingPost) {
        postManager.getSinglePostValue(followingPost.getId(), new OnPostChangedListener() {
            @Override
            public void onObjectChanged(Post obj) {
                bindData(obj);
            }

            @Override
            public void onError(String errorText) {
                PostInteractor.getInstance(context).removeBookMark(followingPost.getId());
                LogUtil.logError(TAG, "bindData", new RuntimeException(errorText));
            }
        });
    }

    public void bindDataWithText(BookMark followingPost) {
        postManager.getSinglePostValue(followingPost.getId(), new OnPostChangedListener() {
            @Override
            public void onObjectChanged(Post obj) {
                bindDataWithText(obj);
            }

            @Override
            public void onError(String errorText) {
                PostInteractor.getInstance(context).removeBookMark(followingPost.getId());

                LogUtil.logError(TAG, "bindData", new RuntimeException(errorText));
            }
        });

    }

    public void bindDataWithCollab(BookMark followingPost){
        postManager.getSinglePostValue(followingPost.getId(), new OnPostChangedListener() {
            @Override
            public void onObjectChanged(Post obj) {
                bindDataWithCollab(obj);
            }

            @Override
            public void onError(String errorText) {
                PostInteractor.getInstance(context).removeBookMark(followingPost.getId());
                LogUtil.logError(TAG, "bindData", new RuntimeException(errorText));
            }
        });

    }
    
    
}
