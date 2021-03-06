/*
 * Copyright (c) 2020. Created By Raj Patil
 */

package com.silentquot.socialcomponents.main.connections.yourconnections;

import android.annotation.SuppressLint;
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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.silentquot.Model.Chatlist;
import com.silentquot.R;
import com.silentquot.socialcomponents.adapters.ConnectionAdapter;
import com.silentquot.socialcomponents.adapters.holders.ConnectionViewHolder;
import com.silentquot.socialcomponents.main.base.BaseFragment;
import com.silentquot.socialcomponents.main.login.LoginActivity;
import com.silentquot.socialcomponents.main.message.MessageActivity;
import com.silentquot.socialcomponents.main.profile.ProfileActivity;
import com.silentquot.socialcomponents.main.search.Searchable;
import com.silentquot.socialcomponents.model.Connection;
import com.silentquot.socialcomponents.utils.AnimationUtils;
import com.silentquot.socialcomponents.views.ConnectButton;

import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.silentquot.socialcomponents.main.usersList.UsersListActivity.UPDATE_FOLLOWING_STATE_REQ;
import static com.silentquot.socialcomponents.main.usersList.UsersListActivity.UPDATE_FOLLOWING_STATE_RESULT_OK;


public class YourConnectionFragment extends BaseFragment<YourConnectionView, YourConnectionPresenter>
        implements YourConnectionView, Searchable {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private ConnectionAdapter usersAdapter;
    private TextView emptyListMessageTextView;
    private String lastSearchText = "";

    private boolean searchInProgress = false;

    private int selectedItemPosition = RecyclerView.NO_POSITION;

    public YourConnectionFragment() {
        // Required empty public constructor
    }

    @NonNull
    @Override
    public YourConnectionPresenter createPresenter() {
        if (presenter == null) {
            return new YourConnectionPresenter(getActivity());
        }
        return presenter;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search_results, container, false);

        progressBar = view.findViewById(R.id.progressBar);
        recyclerView = view.findViewById(R.id.recycler_view);
        emptyListMessageTextView = view.findViewById(R.id.emptyListMessageTextView);
        emptyListMessageTextView.setText(getResources().getString(R.string.empty_user_search_message));

        initRecyclerView();
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == UPDATE_FOLLOWING_STATE_REQ && resultCode == UPDATE_FOLLOWING_STATE_RESULT_OK) {
            updateSelectedItem();
        }

        if (requestCode == LoginActivity.LOGIN_REQUEST_CODE && resultCode == RESULT_OK) {
            presenter.search(lastSearchText);
        }
    }

    @Override
    public void updateSelectedItem() {
        if (selectedItemPosition != RecyclerView.NO_POSITION) {
            usersAdapter.updateItem(selectedItemPosition);
        }
    }

    private void initRecyclerView() {
        usersAdapter = new ConnectionAdapter(getActivity());
        usersAdapter.setCallback(new ConnectionViewHolder.Callback() {
            @Override
            public void onItemClick(int position, View view) {
                if (!searchInProgress) {
                    selectedItemPosition = position;
                    Connection profile = usersAdapter.getItemByPosition(position);
                    openProfileActivity(profile.getId(), view);
                }
            }

            @Override
            public void onAcceptButtonClick(int position, ConnectButton acceptButton) {

            }

            @Override
            public void onCancelButtonClick(int position, ConnectButton cancelButton) {

            }

            @Override
            public void onMessageButtonClick(int position, ConnectButton messageButton) {
                Intent intent = new Intent(getContext(), MessageActivity.class);
                Chatlist chatItem = new Chatlist();
                chatItem.setId(usersAdapter.getItemByPosition(position).getId());
                chatItem.setChat_type("userchat");
                intent.putExtra(MessageActivity.CHAT_EXTRA_KEY, chatItem);
                startActivity(intent);
            }
        });

        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        recyclerView.setAdapter(usersAdapter);

        presenter.loadUsersWithEmptySearch();
    }

    @SuppressLint("RestrictedApi")
    private void openProfileActivity(String userId, View view) {
        Intent intent = new Intent(getActivity(), ProfileActivity.class);
        intent.putExtra(ProfileActivity.USER_ID_EXTRA_KEY, userId);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && view != null) {

            ImageView imageView = view.findViewById(R.id.photoImageView);

            ActivityOptions options = ActivityOptions.
                    makeSceneTransitionAnimation(getActivity(),
                            new android.util.Pair<>(imageView, getString(R.string.post_author_image_transition_name)));
            startActivityForResult(intent, UPDATE_FOLLOWING_STATE_REQ, options.toBundle());
        } else {
            startActivityForResult(intent, UPDATE_FOLLOWING_STATE_REQ);
        }
    }

    @Override
    public void search(String searchText) {
        lastSearchText = searchText;
        presenter.search(searchText);
    }

    @Override
    public void onSearchResultsReady(List<Connection> profiles) {
        hideLocalProgress();
        emptyListMessageTextView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        usersAdapter.setList(profiles);
    }

    @Override
    public void showLocalProgress() {
        searchInProgress = true;
        AnimationUtils.showViewByScaleWithoutDelay(progressBar);
    }

    @Override
    public void hideLocalProgress() {
        searchInProgress = false;
        AnimationUtils.hideViewByScale(progressBar);
    }
    @Override
    public void showEmptyListLayout() {
        hideLocalProgress();
        recyclerView.setVisibility(View.GONE);
        emptyListMessageTextView.setVisibility(View.VISIBLE);
    }
}
