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


package com.silentquot.socialcomponents.main.search.posts;

import android.content.Context;
import android.view.View;

import com.silentquot.R;
import com.silentquot.socialcomponents.main.base.BasePresenter;
import com.silentquot.socialcomponents.managers.PostManager;
import com.silentquot.socialcomponents.managers.listeners.OnPostChangedListener;
import com.silentquot.socialcomponents.model.Post;
import com.silentquot.socialcomponents.utils.LogUtil;

import java.util.List;

/**
 * Created by Alexey on 08.06.18.
 */
public class SearchPostsPresenter extends BasePresenter<SearchPostsView> {
    public static final int LIMIT_POSTS_FILTERED_BY_LIKES = 10;
    private Context context;
    private PostManager postManager;

    public SearchPostsPresenter(Context context) {
        super(context);
        this.context = context;
        postManager = PostManager.getInstance(context);
    }

    public void search() {
        search("");
    }

    public void search(String searchText) {
        if (checkInternetConnection()) {
            if (searchText.isEmpty()) {
                filterByLikes();
            } else {
                ifViewAttached(SearchPostsView::showLocalProgress);
                postManager.searchByTitle(searchText, this::handleSearchResult);
            }

        } else {
            ifViewAttached(SearchPostsView::hideLocalProgress);
        }
    }

    private void filterByLikes() {
        if (checkInternetConnection()) {
            ifViewAttached(SearchPostsView::showLocalProgress);
            postManager.filterByLikes(LIMIT_POSTS_FILTERED_BY_LIKES, this::handleSearchResult);
        } else {
            ifViewAttached(SearchPostsView::hideLocalProgress);
        }
    }

    private void handleSearchResult(List<Post> list) {
        ifViewAttached(view -> {
            view.hideLocalProgress();
            view.onSearchResultsReady(list);

            if (list.isEmpty()) {
                view.showEmptyListLayout();
            }

            LogUtil.logDebug(TAG, "found items count: " + list.size());
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
