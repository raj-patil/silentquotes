/*
 * Copyright (c) 2020. Created By Raj Patil
 */

package com.silentquot.socialcomponents.main.connections;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.silentquot.R;
import com.silentquot.socialcomponents.adapters.viewPager.TabsPagerAdapter;
import com.silentquot.socialcomponents.main.base.BaseActivity;
import com.silentquot.socialcomponents.main.connections.requestconnections.RequestConnectionFragment;
import com.silentquot.socialcomponents.main.connections.yourconnections.YourConnectionFragment;
import com.silentquot.socialcomponents.main.search.Searchable;
import com.silentquot.socialcomponents.utils.LogUtil;


public class ConnectionActivity  extends BaseActivity<ConnectionView, ConnectionPresenter> implements ConnectionView {

    private TabsPagerAdapter tabsAdapter;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private androidx.appcompat.widget.SearchView searchView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


        initContentView();
    }

    @NonNull
    @Override
    public ConnectionPresenter createPresenter() {
        if (presenter == null) {
            return new ConnectionPresenter(this);
        }
        return presenter;
    }

    private void initContentView() {
        viewPager = findViewById(R.id.pager);
        tabLayout = findViewById(R.id.tabLayout);

        initTabs();
    }


    private void initTabs() {

        tabsAdapter = new TabsPagerAdapter(this, getSupportFragmentManager());


        Bundle argsPostsTab = new Bundle();
        tabsAdapter.addTab(YourConnectionFragment.class, argsPostsTab, getResources().getString(R.string.Connections_tab_title));


        Bundle argsUsersTab = new Bundle();
        tabsAdapter.addTab(RequestConnectionFragment.class, argsUsersTab, getResources().getString(R.string.Request_tab_title));



        viewPager.setAdapter(tabsAdapter);
        tabLayout.setupWithViewPager(viewPager);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                search(searchView.getQuery().toString());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }
    private void search(String searchText) {
        Fragment fragment = tabsAdapter.getItem(viewPager.getCurrentItem());
        ((Searchable)fragment).search(searchText);
        LogUtil.logDebug(TAG, "search text: " + searchText);
    }

    private void initSearch(MenuItem searchMenuItem) {

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (androidx.appcompat.widget.SearchView) searchMenuItem.getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        searchMenuItem.expandActionView();

        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                search(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                search(newText);
                return true;
            }
        });

        searchMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                finish();
                return false;
            }
        });
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);

        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        initSearch(searchMenuItem);

        return true;
    }


}
