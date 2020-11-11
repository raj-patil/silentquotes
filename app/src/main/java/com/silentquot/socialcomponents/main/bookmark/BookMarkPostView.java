package com.silentquot.socialcomponents.main.bookmark;

import android.view.View;

import com.silentquot.socialcomponents.main.base.BaseView;
import com.silentquot.socialcomponents.model.BookMark;

import java.util.List;

public interface BookMarkPostView  extends BaseView{
    void openPostDetailsActivity(String postId, View v);
    void openCreatePostActivity();

    void openProfileActivity(String userId, View view);

    void onBookMarkPostsLoaded(List<BookMark> list);

    void showLocalProgress();

    void hideLocalProgress();

    void showEmptyListMessage(boolean show);
}

