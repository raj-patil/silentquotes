/*
 * Copyright 2017 Rozdoum
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

package com.silentquot.socialcomponents.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.silentquot.R;
import com.silentquot.socialcomponents.adapters.holders.PostViewHolder;
import com.silentquot.socialcomponents.controllers.BookMarkController;
import com.silentquot.socialcomponents.controllers.LikeController;
import com.silentquot.socialcomponents.main.base.BaseActivity;
import com.silentquot.socialcomponents.managers.PostManager;
import com.silentquot.socialcomponents.model.Post;

import java.util.List;


public class SearchPostsAdapter extends BasePostsAdapter {
    public static final String TAG = SearchPostsAdapter.class.getSimpleName();

    private CallBack callBack;
    private int TEXT_POST=1;
    private int IMAGE_POST=2;
    private  int COLLAB_POST=3;

    public SearchPostsAdapter(final BaseActivity activity) {
        super(activity);
    }

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view;
        int POST_TYPE;
        if (viewType==IMAGE_POST) {
            view = inflater.inflate(R.layout.post_item_list_view, parent, false );
            POST_TYPE=IMAGE_POST;
        }else if (viewType==TEXT_POST){
            view = inflater.inflate(R.layout.text_post_item_list, parent, false);
            POST_TYPE=TEXT_POST;

        }
        else
        {
            view = inflater.inflate(R.layout.collab_post_item_list, parent, false);
            POST_TYPE=COLLAB_POST;
        }

      //  View view = inflater.inflate(R.layout.post_item_list_view, parent, false);

        return new PostViewHolder(view, createOnClickListener(), activity, true , POST_TYPE);
    }

    public int getItemViewType(int position) {
        Post post=getItemByPosition(position);
        String POST_TYPE=post.getPostType();
        //String Collbid=post.getCollabPostId();


        //String POST_TYPE= getItemByPosition(position).getPostType();
        if (POST_TYPE!=null && POST_TYPE.equals("TEXT")) {
            return TEXT_POST;
        }
        else if(POST_TYPE!=null && POST_TYPE.equals("IMAGE")) {
            return IMAGE_POST;
        }
        else if (POST_TYPE!=null && POST_TYPE.equals("COLLAB"))
        {
            return COLLAB_POST;
        }
        return IMAGE_POST;

    }


    private PostViewHolder.OnClickListener createOnClickListener() {
        return new PostViewHolder.OnClickListener() {
            @Override
            public void onItemClick(int position, View view) {
                if (callBack != null && callBack.enableClick()) {
                    selectedPostPosition = position;
                    callBack.onItemClick(getItemByPosition(position), view);
                }
            }

            @Override
            public void onLikeClick(LikeController likeController, int position) {
                if (callBack != null && callBack.enableClick()) {
                    Post post = getItemByPosition(position);
                    likeController.handleLikeClickAction(activity, post);
                }
            }

            @Override
            public void onAuthorClick(int position, View view) {
                if (callBack != null && callBack.enableClick()) {
                    callBack.onAuthorClick(getItemByPosition(position).getAuthorId(), view);
                }
            }

            @Override
            public void onCopyClick(int position, View view) {
                Post post = getItemByPosition(position);
                PostManager postManager = PostManager.getInstance(view.getContext());
                postManager.copyTextFromPost(post);
            }

            @Override
            public void onShareClick(int position, ImageView view , View mainView) {
                Post post = getItemByPosition(position);
                PostManager postManager= PostManager.getInstance(mainView.getContext());
                postManager.sharePost(post , view , mainView.getContext() );
            }

            @Override
            public void onCollabClick(int position, View view) {
                Post post = getItemByPosition(position);
                PostManager postManager= PostManager.getInstance(view.getContext());
                postManager.CollabPost(post.getId(),view.getContext());
            }

            @Override
            public void onCollabContainerClick(int position, View view) {
                if (callBack!=null) {
                    callBack.onCollabContainerClick(position, view);
                    selectedPostPosition=position;
                }
            }

            @Override
            public void onBookMarkClick(BookMarkController bookMarkController, int position) {
                Post post = getItemByPosition(position);
                bookMarkController.handleBookmarkAction(activity, post);
            }

            @Override
            public void onCollabAutherClick(int adapterPosition, View v) {
                if (callBack != null && callBack.enableClick()) {
                    callBack.onCollabAutherClick(getItemByPosition(adapterPosition).getCollabPostId(), v);
                }
            }
        };
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int POST_TYPE = getItemViewType(position);
        if (POST_TYPE == IMAGE_POST) {
            ((PostViewHolder) holder).bindData(postList.get(position));
        } else if(POST_TYPE == TEXT_POST)
        {
            ((PostViewHolder) holder).bindDataWithText(postList.get(position));
        }
        else {
            ((PostViewHolder) holder).bindDataWithCollab(postList.get(position));
        }
    }

    public void setList(List<Post> list) {
        cleanSelectedPostInformation();
        postList.clear();
        postList.addAll(list);
        notifyDataSetChanged();
    }

    public void removeSelectedPost() {
        if (selectedPostPosition != RecyclerView.NO_POSITION) {
            postList.remove(selectedPostPosition);
            notifyItemRemoved(selectedPostPosition);
        }
    }

    public interface CallBack {
        void onItemClick(Post post, View view);
        void onAuthorClick(String authorId, View view);
        boolean enableClick();
        void onCollabContainerClick(int position, View view);

        void onCollabAutherClick(String authorId, View v);
    }
}
