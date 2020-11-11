package com.silentquot.socialcomponents.main.bookmark;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.silentquot.R;
import com.silentquot.socialcomponents.adapters.BookMarkPostAdapter;
import com.silentquot.socialcomponents.dialogs.NewPostDialog;
import com.silentquot.socialcomponents.main.base.BaseActivity;
import com.silentquot.socialcomponents.main.postDetails.PostDetailsActivity;
import com.silentquot.socialcomponents.main.profile.ProfileActivity;
import com.silentquot.socialcomponents.managers.listeners.OnPostCreatedListener;
import com.silentquot.socialcomponents.model.BookMark;

import java.util.List;

public class BookMarkPostActivity extends BaseActivity<BookMarkPostView, BookMarkPostPresenter> implements BookMarkPostView {

    private BookMarkPostAdapter postsAdapter;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeContainer;
    private TextView message_following_posts_empty;

    View view;
    private FloatingActionButton floatingActionButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmark_post);
        initContentView();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        presenter.loadBookMarkPost();
    }
  

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == PostDetailsActivity.UPDATE_POST_REQUEST) {
            postsAdapter.updateSelectedItem();
        }
    }

    @NonNull
    @Override
    public BookMarkPostPresenter createPresenter() {
        if (presenter == null) {
            return new BookMarkPostPresenter(this);
        }
        return presenter;
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

    private void initContentView() {
        if (recyclerView == null) {
            progressBar = findViewById(R.id.progressBar);
            message_following_posts_empty = findViewById(R.id.message_following_posts_empty);
            swipeContainer = findViewById(R.id.swipeContainer);
            swipeContainer.setOnRefreshListener(() -> presenter.onRefresh());


            initPostListRecyclerView();
        }
    }



    private void initPostListRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView);
        postsAdapter = new BookMarkPostAdapter((BaseActivity) this);
        postsAdapter.setCallBack(new BookMarkPostAdapter.CallBack() {
            @Override
            public void onItemClick(BookMark followingPost, View view) {
                presenter.onPostClicked(followingPost.getId(), view);
            }

            @Override
            public void onAuthorClick(int position, View view) {
                String postId = postsAdapter.getItemByPosition(position).getId();
                presenter.onAuthorClick(postId, view);
            }

            @Override
            public void onCopyClick(int position, View view) {
                String postid=postsAdapter.getItemByPosition(position).getId();
                presenter.onCopyClick(postid);
            }

            @Override
            public void onShareClick(int position , ImageView view , View mainView ) {
                String postid=postsAdapter.getItemByPosition(position).getId();
                presenter.onShareClick(postid , view,mainView);
            }

            @Override
            public void onCollabClick(int position, View view) {
                String postid=postsAdapter.getItemByPosition(position).getId();
                presenter.onCollabClick(postid , view);
            }

            @Override
            public void onCollabContainerClick(int position, View view) {

                String postid=postsAdapter.getItemByPosition(position).getId();
                presenter.onCollabContainerClick(postid, view);

//            FollowingPost    followingPost= postsAdapter.getItemByPosition(position);
//                presenter.onPostClicked(followingPost.getPostId(), view);

            }

            @Override
            public void onCollabAutherClick(int adapterPosition, View v) {
                String postId = postsAdapter.getItemByPosition(adapterPosition).getId();
                presenter.onCollabAutherClick(postId, view);
            }


        });
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        recyclerView.setAdapter(postsAdapter);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void openPostDetailsActivity(String postId, View v) {
        Toast.makeText(this,"CollabContainerClick_open",Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, PostDetailsActivity.class);
        intent.putExtra(PostDetailsActivity.POST_ID_EXTRA_KEY, postId);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            View imageView = v.findViewById(R.id.postImageView);
            View authorImageView = v.findViewById(R.id.authorImageView);
            View collabContainer=v.findViewById(R.id.collabcontainer);
            View collabauthorImageview = v.findViewById(R.id.collabauthorImageView1);

            if (imageView!=null) {
                ActivityOptions options = ActivityOptions.
                        makeSceneTransitionAnimation(this,
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
        NewPostDialog newPostDialog=new NewPostDialog(this);
        newPostDialog.showCustomDialog(FirebaseAuth.getInstance().getCurrentUser().getUid(), this, this, new OnPostCreatedListener() {
            @Override
            public void onPostSaved(boolean success) {

            }
        });

    }

    @SuppressLint("RestrictedApi")
    @Override
    public void openProfileActivity(String userId, View view) {
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra(ProfileActivity.USER_ID_EXTRA_KEY, userId);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && view != null) {

            View authorImageView = view.findViewById(R.id.authorImageView);

            ActivityOptions options = ActivityOptions.
                    makeSceneTransitionAnimation(this,
                            new android.util.Pair<>(authorImageView, getString(R.string.post_author_image_transition_name)));
            startActivityForResult(intent, ProfileActivity.CREATE_POST_FROM_PROFILE_REQUEST, options.toBundle());
        } else {
            startActivityForResult(intent, ProfileActivity.CREATE_POST_FROM_PROFILE_REQUEST);
        }
    }

    @Override
    public void onBookMarkPostsLoaded(List<BookMark> list) {
        postsAdapter.setList(list);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }


}
