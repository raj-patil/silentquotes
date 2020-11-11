

package com.silentquot.socialcomponents.main.profile;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.silentquot.R;
import com.silentquot.socialcomponents.adapters.PostsByUserAdapter;
import com.silentquot.socialcomponents.dialogs.UnfollowConfirmationDialog;
import com.silentquot.socialcomponents.enums.FollowState;
import com.silentquot.socialcomponents.main.base.BaseActivity;
import com.silentquot.socialcomponents.main.base.BaseFragment;
import com.silentquot.socialcomponents.main.editProfile.EditProfileActivity;
import com.silentquot.socialcomponents.main.login.LoginActivity;
import com.silentquot.socialcomponents.main.main.MainActivity;
import com.silentquot.socialcomponents.main.post.createPost.CreatePostActivity;
import com.silentquot.socialcomponents.main.postDetails.PostDetailsActivity;
import com.silentquot.socialcomponents.main.usersList.UsersListActivity;
import com.silentquot.socialcomponents.main.usersList.UsersListType;
import com.silentquot.socialcomponents.managers.FollowManager;
import com.silentquot.socialcomponents.managers.ProfileManager;
import com.silentquot.socialcomponents.model.Post;
import com.silentquot.socialcomponents.utils.GlideApp;
import com.silentquot.socialcomponents.utils.ImageUtil;
import com.silentquot.socialcomponents.utils.LogUtil;
import com.silentquot.socialcomponents.utils.LogoutHelper;
import com.silentquot.socialcomponents.views.FollowButton;

public class ProfileFragment extends BaseFragment<ProfileFragmentView, ProfileFragmentPresenter> implements ProfileFragmentView, GoogleApiClient.OnConnectionFailedListener, UnfollowConfirmationDialog.Callback {
    private static final String TAG = ProfileActivity.class.getSimpleName();
    public static final int CREATE_POST_FROM_PROFILE_REQUEST = 22;
    public static final String USER_ID_EXTRA_KEY = "ProfileActivity.USER_ID_EXTRA_KEY";

    // UI references.
    private TextView nameEditText;
    private ImageView imageView;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView postsCounterTextView;
    private ProgressBar postsProgressBar;

    private FirebaseAuth mAuth;
    private GoogleApiClient mGoogleApiClient;
    private String currentUserId;
    private String userID;

    private PostsByUserAdapter postsAdapter;
    private SwipeRefreshLayout swipeContainer;
    private TextView likesCountersTextView;
    private TextView followersCounterTextView;
    private TextView followingsCounterTextView;
    private FollowButton followButton;
    private CardView profileviewheadercardview;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        super.onCreate(savedInstanceState);

        View view = inflater.inflate(R.layout.activity_user_profile, container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        //view.setSupportActionBar(toolbar);
        //actionBar = getSupportActionBar();

//        if (actionBar != null) {
//            actionBar.setDisplayHomeAsUpEnabled(true);
//        }

        if (FirebaseAuth.getInstance().getCurrentUser()!=null)
        { userID = FirebaseAuth.getInstance().getUid(); }

        else {
            userID=FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            currentUserId = firebaseUser.getUid();
        }

        // Set up the login form.
        progressBar = view.findViewById(R.id.progressBar);
        imageView = view.findViewById(R.id.imageView);
        nameEditText = view.findViewById(R.id.nameEditText);
        postsCounterTextView = view.findViewById(R.id.postsCounterTextView);
        likesCountersTextView = view.findViewById(R.id.likesCountersTextView);
        followersCounterTextView = view.findViewById(R.id.followersCounterTextView);
        followingsCounterTextView =view.findViewById(R.id.followingsCounterTextView);
        postsProgressBar = view.findViewById(R.id.postsProgressBar);
        followButton = view.findViewById(R.id.followButton);
        swipeContainer = view.findViewById(R.id.swipeContainer);
        recyclerView = view.findViewById(R.id.recycler_view);

        initListeners();

        //presenter.checkFollowState(userID);

        loadPostsList();
        getActivity().supportPostponeEnterTransition();
        return  view;
    }

    @Override
    public void onStart() {
        super.onStart();
        presenter.loadProfile(getActivity(), userID);
        presenter.getFollowersCount(userID);
        presenter.getFollowingsCount(userID);

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        FollowManager.getInstance(getContext()).closeListeners(getContext());
        ProfileManager.getInstance(getContext()).closeListeners(getContext());

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.stopAutoManage(getActivity());
            mGoogleApiClient.disconnect();
        }
    }

    @NonNull
    @Override
    public ProfileFragmentPresenter createPresenter() {
        if (presenter == null) {
            return new ProfileFragmentPresenter(getActivity());
        }
        return presenter;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode ==  getActivity().RESULT_OK) {
            switch (requestCode) {
                case CreatePostActivity.CREATE_NEW_POST_REQUEST:
                    postsAdapter.loadPosts();
                    showSnackBar(R.string.message_post_was_created);
                    getActivity().setResult(getActivity().RESULT_OK);
                    break;

                case PostDetailsActivity.UPDATE_POST_REQUEST:
                    presenter.checkPostChanges(data);
                    break;

                case LoginActivity.LOGIN_REQUEST_CODE:
                    presenter.checkFollowState(userID);
                    break;
            }
        }
    }

    private void initListeners() {
//        followButton.setOnClickListener(v -> {
//            presenter.onFollowButtonClick(followButton.getState(), userID);
//        });

        followingsCounterTextView.setOnClickListener(v -> {
            startUsersListActivity(UsersListType.FOLLOWINGS);
        });

        followersCounterTextView.setOnClickListener(v -> {
            startUsersListActivity(UsersListType.FOLLOWERS);
        });

        swipeContainer.setOnRefreshListener(this::onRefreshAction);
    }

    private void onRefreshAction() {
        postsAdapter.loadPosts();
    }

    private void startUsersListActivity(int usersListType) {
        Intent intent = new Intent(getContext(), UsersListActivity.class);
        intent.putExtra(UsersListActivity.USER_ID_EXTRA_KEY, userID);
        intent.putExtra(UsersListActivity.USER_LIST_TYPE, usersListType);
        startActivity(intent);
    }

    private void loadPostsList() {
//        if (recyclerView == null) {
            postsAdapter = new PostsByUserAdapter((BaseActivity)getActivity(), currentUserId);
            postsAdapter.setCallBack(new PostsByUserAdapter.CallBack() {
                @Override
                public void onItemClick(final Post post, final View view) {

                    presenter.onPostClick(post, view);
                }

                @Override
                public void onPostsListChanged(int postsCount) {
                    presenter.onPostListChanged(postsCount);
                }

                @Override
                public void onPostLoadingCanceled() {
                    hideLoadingPostsProgress();
                }

                @Override
                public void onCollabContainerClick(int position, View view) {

                    String postid=postsAdapter.getItemByPosition(position).getId();
                    presenter.onCollabContainerClick(postid, view);
                }
            });

            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
            recyclerView.setAdapter(postsAdapter);
            postsAdapter.loadPosts();
   //     }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void openPostDetailsActivity(String post, View v) {
        Intent intent = new Intent(getContext(), PostDetailsActivity.class);
        intent.putExtra(PostDetailsActivity.POST_ID_EXTRA_KEY, post);
        intent.putExtra(PostDetailsActivity.AUTHOR_ANIMATION_NEEDED_EXTRA_KEY, true);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            View imageView = v.findViewById(R.id.postImageView);

            if (imageView!=null) {
                ActivityOptions options = ActivityOptions.
                        makeSceneTransitionAnimation(getActivity(),
                                new android.util.Pair<>(imageView, getString(R.string.post_image_transition_name))
                        );
                startActivityForResult(intent, PostDetailsActivity.UPDATE_POST_REQUEST, options.toBundle());
            }
            else {
                startActivityForResult(intent, PostDetailsActivity.UPDATE_POST_REQUEST);
            }
        } else {
            startActivityForResult(intent, PostDetailsActivity.UPDATE_POST_REQUEST);
        }
    }

    private void scheduleStartPostponedTransition(final ImageView imageView) {
        imageView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                imageView.getViewTreeObserver().removeOnPreDrawListener(this);
                getActivity().supportStartPostponedEnterTransition();
                return true;
            }
        });
    }

    private void startMainActivity() {
        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public void startEditProfileActivity() {
        Intent intent = new Intent(getContext(), EditProfileActivity.class);
        startActivity(intent);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        LogUtil.logDebug(TAG, "onConnectionFailed:" + connectionResult);
    }

    @Override
    public void openCreatePostActivity() {
        Intent intent = new Intent(getContext(), CreatePostActivity.class);
        startActivityForResult(intent, CreatePostActivity.CREATE_NEW_POST_REQUEST);
    }

    @Override
    public void setProfileName(String username) {
        nameEditText.setText(username);
    }

    @Override
    public void setProfilePhoto(String photoUrl) {
        ImageUtil.loadImage(GlideApp.with(this), photoUrl, imageView, new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                scheduleStartPostponedTransition(imageView);
                progressBar.setVisibility(View.GONE);
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                scheduleStartPostponedTransition(imageView);
                progressBar.setVisibility(View.GONE);
                return false;
            }
        });
    }

    @Override
    public void setDefaultProfilePhoto() {
        progressBar.setVisibility(View.GONE);
        imageView.setImageResource(R.drawable.ic_stub);
    }

    @Override
    public void updateLikesCounter(Spannable text) {
        likesCountersTextView.setText(text);
    }

    @Override
    public void hideLoadingPostsProgress() {
        swipeContainer.setRefreshing(false);
        if (postsProgressBar.getVisibility() != View.GONE) {
            postsProgressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void showLikeCounter(boolean show) {
        likesCountersTextView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void updatePostsCounter(Spannable text) {
        postsCounterTextView.setText(text);
    }

    @Override
    public void showPostCounter(boolean show) {
        postsCounterTextView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onPostRemoved() {
        postsAdapter.removeSelectedPost();
    }

    @Override
    public void onPostUpdated() {
        postsAdapter.updateSelectedPost();
    }

//    @Override
//    public void showUnfollowConfirmation(@NonNull Profile profile) {
//        UnfollowConfirmationDialog unfollowConfirmationDialog = new UnfollowConfirmationDialog();
//        Bundle args = new Bundle();
//        args.putSerializable(UnfollowConfirmationDialog.PROFILE, profile);
//        unfollowConfirmationDialog.setArguments(args);
//        unfollowConfirmationDialog.show(getFragmentManager(), UnfollowConfirmationDialog.TAG);
//    }

    @Override
    public void updateFollowButtonState(FollowState followState) {
        followButton.setState(followState);
    }

    @Override
    public void updateFollowersCount(int count) {
        followersCounterTextView.setVisibility(View.VISIBLE);
        String followersLabel = getResources().getQuantityString(R.plurals.followers_counter_format, count, count);
        followersCounterTextView.setText(presenter.buildCounterSpannable(followersLabel, count));
    }

    @Override
    public void updateFollowingsCount(int count) {
        followingsCounterTextView.setVisibility(View.VISIBLE);
        String followingsLabel = getResources().getQuantityString(R.plurals.followings_counter_format, count, count);
        followingsCounterTextView.setText(presenter.buildCounterSpannable(followingsLabel, count));
    }

    @Override
    public void setFollowStateChangeResultOk() {
        getActivity().setResult(UsersListActivity.UPDATE_FOLLOWING_STATE_RESULT_OK);
    }

    @Override
    public void onUnfollowButtonClicked() {
        presenter.unfollowUser(userID);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        if (userID.equals(currentUserId)) {
//            MenuInflater inflater = getActivity().getMenuInflater();
//            inflater.inflate(R.menu.profile_menu, menu);
//            return true;
//        }
//
//        return super.onCreateOptionsMenu(menu);
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.editProfile:
                presenter.onEditProfileClick();
                return true;
            case R.id.signOut:
                LogoutHelper.signOut(mGoogleApiClient, getActivity());
                startMainActivity();
                return true;
            case R.id.createPost:
                presenter.onCreatePostClick();
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
