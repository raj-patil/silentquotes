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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.silentquot.R;
import com.silentquot.socialcomponents.adapters.FollowPostsAdapter;
import com.silentquot.socialcomponents.dialogs.NewPostDialog;
import com.silentquot.socialcomponents.main.base.BaseActivity;
import com.silentquot.socialcomponents.main.base.BaseFragment;
import com.silentquot.socialcomponents.main.post.createPost.CreatePostActivity;
import com.silentquot.socialcomponents.main.postDetails.PostDetailsActivity;
import com.silentquot.socialcomponents.main.profile.ProfileActivity;
import com.silentquot.socialcomponents.managers.listeners.OnPostCreatedListener;
import com.silentquot.socialcomponents.model.FollowingPost;

import java.util.List;

import static android.app.Activity.RESULT_OK;

public class FollowingPostsActivity extends BaseFragment<FollowPostsView, FollowingPostsPresenter> implements FollowPostsView , Refreshable {

    private FollowPostsAdapter postsAdapter;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeContainer;
    private TextView message_following_posts_empty;

    View view;
    private FloatingActionButton floatingActionButton;


    @Override
    public View  onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         view = inflater.inflate(R.layout.activity_follow_posts, container, false);
         initContentView();
        presenter.loadFollowingPosts();
        return  view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CreatePostActivity.CREATE_NEW_POST_REQUEST:
                    postsAdapter.updateSelectedItem();
                   // callback.onPostSaved(true);

                case PostDetailsActivity.UPDATE_POST_REQUEST:
                    presenter.onPostUpdated(data);
                    break;
            }
        }
    }

    @NonNull
    @Override
    public FollowingPostsPresenter createPresenter() {
        if (presenter == null) {
            return new FollowingPostsPresenter(getContext());
        }

        return presenter;
    }
    OnPostCreatedListener callback;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        callback= (OnPostCreatedListener) getActivity();

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        callback= (OnPostCreatedListener) activity;
    }

    @Override
    public void onFollowingPostsLoaded(List<FollowingPost> list) {
        postsAdapter.setList(list);
    }

    @Override
    public void showLocalProgress() {
        if (!swipeContainer.isRefreshing()) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void hideLocalProgress() {
        swipeContainer.setRefreshing(false);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void showEmptyListMessage(boolean show) {
        message_following_posts_empty.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void removepost() {
            postsAdapter.removeSelectedPost();
    }

    @Override
    public void updatePost() {
        postsAdapter.updateSelectedItem();
    }

    private void initContentView() {
        if (recyclerView == null) {
            progressBar = view.findViewById(R.id.progressBar);
            message_following_posts_empty = view.findViewById(R.id.message_following_posts_empty);
            swipeContainer = view.findViewById(R.id.swipeContainer);
            swipeContainer.setOnRefreshListener(() -> presenter.onRefresh());


            initPostListRecyclerView();
        }
    }


    private void initPostListRecyclerView() {
        recyclerView = view.findViewById(R.id.recyclerView);
        postsAdapter = new FollowPostsAdapter((BaseActivity) getActivity());
        postsAdapter.setCallBack(new FollowPostsAdapter.CallBack() {
            @Override
            public void onItemClick(FollowingPost followingPost, View view) {
                presenter.onPostClicked(followingPost.getPostId(), view);
            }

            @Override
            public void onAuthorClick(int position, View view) {
                String postId = postsAdapter.getItemByPosition(position).getPostId();
                presenter.onAuthorClick(postId, view);
            }

            @Override
            public void onCollabAutherClick(int position, View view) {
                String postId = postsAdapter.getItemByPosition(position).getPostId();
                presenter.onCollabAutherClick(postId, view);
            }

            @Override
            public void onCopyClick(int position, View view) {
                String postid=postsAdapter.getItemByPosition(position).getPostId();
                presenter.onCopyClick(postid);
            }

            @Override
            public void onShareClick(int position , ImageView view , View mainView ) {
                String postid=postsAdapter.getItemByPosition(position).getPostId();
                presenter.onShareClick(postid , view,mainView);
            }

            @Override
            public void onCollabClick(int position, View view) {
                String postid=postsAdapter.getItemByPosition(position).getPostId();
                presenter.onCollabClick(postid , view);
            }

            @Override
            public void onCollabContainerClick(int position, View view) {

                String postid=postsAdapter.getItemByPosition(position).getPostId();
                presenter.onCollabContainerClick(postid, view);

//            FollowingPost    followingPost= postsAdapter.getItemByPosition(position);
//                presenter.onPostClicked(followingPost.getPostId(), view);

            }


        });
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        recyclerView.setAdapter(postsAdapter);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void openPostDetailsActivity(String postId, View v) {
        Toast.makeText(getContext(),"CollabContainerClick_open",Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getActivity(), PostDetailsActivity.class);
        intent.putExtra(PostDetailsActivity.POST_ID_EXTRA_KEY, postId);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            View imageView = v.findViewById(R.id.postImageView);
            View authorImageView = v.findViewById(R.id.authorImageView);
            View collabContainer=v.findViewById(R.id.collabcontainer);
            View collabauthorImageview = v.findViewById(R.id.collabauthorImageView1);

            if (imageView!=null && authorImageView!=null) {
                ActivityOptions options = ActivityOptions.
                        makeSceneTransitionAnimation(getActivity(),
                                new android.util.Pair<>(imageView, getString(R.string.post_image_transition_name)),
                                new android.util.Pair<>(authorImageView, getString(R.string.post_author_image_transition_name))
                        );
                startActivityForResult(intent, PostDetailsActivity.UPDATE_POST_REQUEST, options.toBundle());
            }
        else
            {
                startActivityForResult(intent, PostDetailsActivity.UPDATE_POST_REQUEST);
            }
        } else  {
            startActivityForResult(intent, PostDetailsActivity.UPDATE_POST_REQUEST);
        }
    }

    @Override
    public void openCreatePostActivity() {
        NewPostDialog newPostDialog=new NewPostDialog(getContext());
        newPostDialog.showCustomDialog(FirebaseAuth.getInstance().getCurrentUser().getUid(), getContext(), getActivity(), new OnPostCreatedListener() {
            @Override
            public void onPostSaved(boolean success) {
                presenter.onRefresh();
            }
        });

    }

    @SuppressLint("RestrictedApi")
    @Override
    public void openProfileActivity(String userId, View view) {
        Intent intent = new Intent(getActivity(), ProfileActivity.class);
        intent.putExtra(ProfileActivity.USER_ID_EXTRA_KEY, userId);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && view != null) {

            View authorImageView = view.findViewById(R.id.authorImageView);
            View collabautheImageview = view.findViewById(R.id.collabauthorImageView1);

            if (authorImageView!=null) {
                ActivityOptions options = ActivityOptions.
                        makeSceneTransitionAnimation(getActivity(),
                                new android.util.Pair<>(authorImageView, getString(R.string.post_author_image_transition_name)));
                startActivityForResult(intent, ProfileActivity.CREATE_POST_FROM_PROFILE_REQUEST, options.toBundle());
            }
            else {
                startActivityForResult(intent, ProfileActivity.CREATE_POST_FROM_PROFILE_REQUEST);
            }
        } else {
            startActivityForResult(intent, ProfileActivity.CREATE_POST_FROM_PROFILE_REQUEST);
        }




    }


    @Override
    public void onRefreshPostsubmit() {
        presenter.onRefresh();
    }
}
