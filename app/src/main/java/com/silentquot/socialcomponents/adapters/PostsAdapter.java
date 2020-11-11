/*
 *  Copyright 2017 Rozdoum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.silentquot.socialcomponents.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.silentquot.R;
import com.silentquot.socialcomponents.adapters.holders.PostViewHolder;
import com.silentquot.socialcomponents.controllers.BookMarkController;
import com.silentquot.socialcomponents.controllers.LikeController;
import com.silentquot.socialcomponents.enums.ItemType;
import com.silentquot.socialcomponents.main.main.MainActivity;
import com.silentquot.socialcomponents.managers.PostManager;
import com.silentquot.socialcomponents.managers.listeners.OnPostListChangedListener;
import com.silentquot.socialcomponents.model.Post;
import com.silentquot.socialcomponents.model.PostListResult;
import com.silentquot.socialcomponents.utils.PreferencesUtil;

import java.util.List;


public class PostsAdapter extends BasePostsAdapter {
    public static final String TAG = PostsAdapter.class.getSimpleName();

    private Callback callback;
    private boolean isLoading = false;
    private boolean isMoreDataAvailable = true;
    private long lastLoadedItemCreatedDate;
    private SwipeRefreshLayout swipeContainer;
    private MainActivity mainActivity;

    public PostsAdapter(final MainActivity activity, SwipeRefreshLayout swipeContainer) {
        super(activity);
        this.mainActivity = activity;
        this.swipeContainer = swipeContainer;
        initRefreshLayout();
        setHasStableIds(true);
    }

    private void initRefreshLayout() {
        if (swipeContainer != null) {
            this.swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    onRefreshAction();
                }
            });
        }
    }


    private void onRefreshAction() {

        if (activity.hasInternetConnection()) {
            loadFirstPage();
            cleanSelectedPostInformation();
        } else {
            swipeContainer.setRefreshing(false);
            mainActivity.showFloatButtonRelatedSnackBar(R.string.internet_connection_failed);
        }
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
//        if (viewType == ItemType.ITEM.getTypeCode()) {
            return new PostViewHolder(inflater.inflate(R.layout.post_item_list_view, parent, false),
                    createOnClickListener(), activity);
//        } else {
//            return new LoadViewHolder(inflater.inflate(R.layout.loading_view, parent, false));
//        }
    }

    private PostViewHolder.OnClickListener createOnClickListener() {
        return new PostViewHolder.OnClickListener() {
            @Override
            public void onItemClick(int position, View view) {
                if (callback != null) {
                    selectedPostPosition = position;
                    callback.onItemClick(getItemByPosition(position), view);
                }
            }

            @Override
            public void onLikeClick(LikeController likeController, int position) {
                Post post = getItemByPosition(position);
                likeController.handleLikeClickAction(activity, post);
            }

            @Override
            public void onAuthorClick(int position, View view) {
                if (callback != null) {
                    callback.onAuthorClick(getItemByPosition(position).getAuthorId(), view);
                }
            }

            @Override
            public void onCopyClick(int position, View view) {
               Post post = getItemByPosition(position);
               PostManager postManager= PostManager.getInstance(view.getContext());
               postManager.copyTextFromPost(post);
            }


            @Override
            public void onShareClick(int position, ImageView view , View mainView) {

                Post post = getItemByPosition(position);
                PostManager postManager= PostManager.getInstance(mainView.getContext());
                postManager.sharePost(post , view ,mainView.getContext());
            }

            @Override
            public void onCollabClick(int position, View view) {
                Post post = getItemByPosition(position);
                PostManager postManager= PostManager.getInstance(view.getContext());
                postManager.CollabPost(post.getId(),view.getContext());
            }

            @Override
            public void onCollabContainerClick(int position, View view) {
                if (callback != null) {
                    selectedPostPosition = position;
                    callback.onCollabContainerClick(getItemByPosition(position), view);
                }
            }

            @Override
            public void onBookMarkClick(BookMarkController bookMarkController, int position) {
                Post post = getItemByPosition(position);
                bookMarkController.handleBookmarkAction(activity, post);
            }

            @Override
            public void onCollabAutherClick(int adapterPosition, View v) {
                if (callback != null) {
                    callback.onCollabAutherClick(getItemByPosition(adapterPosition).getCollabPostId(), v);
                }
            }
        };
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position >= getItemCount() - 1 && isMoreDataAvailable && !isLoading) {
            android.os.Handler mHandler = activity.getWindow().getDecorView().getHandler();
            mHandler.post(new Runnable() {
                public void run() {
                    //change adapter contents
                    if (activity.hasInternetConnection()) {
                        isLoading = true;
                        postList.add(new Post(ItemType.LOAD));
                        notifyItemInserted(postList.size());
                        loadNext(lastLoadedItemCreatedDate - 1);
                    } else {
                        mainActivity.showFloatButtonRelatedSnackBar(R.string.internet_connection_failed);
                    }
                }
            });


        }

        if (getItemViewType(position) != ItemType.LOAD.getTypeCode()) {
            ((PostViewHolder) holder).bindData(postList.get(position));
        }
    }

    private void addList(List<Post> list) {
        this.postList.addAll(list);
        notifyDataSetChanged();
        isLoading = false;
    }

    public void loadFirstPage() {
        loadNext(0);
        PostManager.getInstance(mainActivity.getApplicationContext()).clearNewPostsCounter();
    }

    private void loadNext(final long nextItemCreatedDate) {

        if (!PreferencesUtil.isPostWasLoadedAtLeastOnce(mainActivity) && !activity.hasInternetConnection()) {
            mainActivity.showFloatButtonRelatedSnackBar(R.string.internet_connection_failed);
            hideProgress();
            callback.onListLoadingFinished();
            return;
        }

        OnPostListChangedListener<Post> onPostsDataChangedListener = new OnPostListChangedListener<Post>() {
            @Override
            public void onListChanged(PostListResult result) {
                lastLoadedItemCreatedDate = result.getLastItemCreatedDate();
                isMoreDataAvailable = result.isMoreDataAvailable();
                List<Post> list = result.getPosts();

                if (nextItemCreatedDate == 0) {
                    postList.clear();
                    notifyDataSetChanged();
                    swipeContainer.setRefreshing(false);
                }

                hideProgress();

                if (!list.isEmpty()) {
                    addList(list);

                    if (!PreferencesUtil.isPostWasLoadedAtLeastOnce(mainActivity)) {
                        PreferencesUtil.setPostWasLoadedAtLeastOnce(mainActivity, true);
                    }
                } else {
                    isLoading = false;
                }

                callback.onListLoadingFinished();
            }

            @Override
            public void onCanceled(String message) {
                callback.onCanceled(message);
            }
        };

        PostManager.getInstance(activity).getPostsList(onPostsDataChangedListener, nextItemCreatedDate);
    }

    private void hideProgress() {
        if (!postList.isEmpty() && getItemViewType(postList.size() - 1) == ItemType.LOAD.getTypeCode()) {
            postList.remove(postList.size() - 1);
            notifyItemRemoved(postList.size() - 1);
        }
    }

    public void removeSelectedPost() {
        postList.remove(selectedPostPosition);
        notifyItemRemoved(selectedPostPosition);
    }

    @Override
    public long getItemId(int position) {
        return getItemByPosition(position).getId().hashCode();
    }

    public interface Callback {
        void onItemClick(Post post, View view);
        void onListLoadingFinished();
        void onAuthorClick(String authorId, View view);
        void onCanceled(String message);
        void onCollabContainerClick(Post itemByPosition, View view);

        void onCollabAutherClick(String collabPostId, View v);
    }
}
