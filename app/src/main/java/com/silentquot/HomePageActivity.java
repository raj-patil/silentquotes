package com.silentquot;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.silentquot.socialcomponents.dialogs.NewPostDialog;
import com.silentquot.socialcomponents.main.base.BaseActivity;
import com.silentquot.socialcomponents.main.bookmark.BookMarkPostActivity;
import com.silentquot.socialcomponents.main.chatlist.ChatListFragment;
import com.silentquot.socialcomponents.main.connections.ConnectionActivity;
import com.silentquot.socialcomponents.main.followPosts.FollowingPostsActivity;
import com.silentquot.socialcomponents.main.followPosts.Refreshable;
import com.silentquot.socialcomponents.main.main.MainPresenter;
import com.silentquot.socialcomponents.main.main.MainView;
import com.silentquot.socialcomponents.main.post.createPost.CreatePostActivity;
import com.silentquot.socialcomponents.main.profile.ProfileFragment;
import com.silentquot.socialcomponents.main.search.SearchActivity;
import com.silentquot.socialcomponents.managers.listeners.OnPostCreatedListener;
import com.silentquot.socialcomponents.model.Post;

import java.util.ArrayList;
import java.util.HashMap;

public class HomePageActivity extends BaseActivity<MainView, MainPresenter> implements MainView , OnPostCreatedListener {


    FirebaseUser firebaseUser;
    DatabaseReference reference;
    private ActionBar actionBar;
    private Toolbar toolbar;
    private FirebaseAuth mAuth;
    private FloatingActionButton floatingActionButton;
    private final static int CONNECTION_FAB_BTN=0;
    private final static  int NEW_POST_FAB_BTN=1;
    private final static   int BOOKMARK_FAB_BTN=2;
    NewPostDialog newPostDialog;
     ViewPagerAdapter viewPagerAdapter;
     ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        initToolbar();


        floatingActionButton=findViewById(R.id.fab_btn);

        floatingActionButton.setOnClickListener(v -> presenter.onConnectionClickAction(floatingActionButton));

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//        FirebaseAuth.getInstance().signOut();

        final TabLayout tabLayout = findViewById(R.id.tab_layout);
         viewPager = findViewById(R.id.view_pager);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {

                switch (position) {
                    case CONNECTION_FAB_BTN:
                        int drawable = R.drawable.ic_person;
                        floatingActionButton.setImageResource(drawable);
                        floatingActionButton.setImageResource( R.drawable.ic_person);
                        floatingActionButton.setOnClickListener(v -> presenter.onConnectionClickAction(floatingActionButton));
                        floatingActionButton.show();
                        floatingActionButton.show();
                        break;

                    case NEW_POST_FAB_BTN:
                        floatingActionButton.setImageResource( R.drawable.ic_pensil_white);
                        floatingActionButton.setOnClickListener(v -> presenter.onCreatePostClickAction(floatingActionButton));
                        floatingActionButton.show();
                        break;

                    case BOOKMARK_FAB_BTN:
                        floatingActionButton.setImageResource( R.drawable.ic_bookmark_white);
                     floatingActionButton.setOnClickListener(v -> presenter.onBookMarkClickAction(floatingActionButton));
                        floatingActionButton.show();
                        break;


                    default:
                        floatingActionButton.hide();
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

         viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragment(new ChatListFragment(), " Chats");
        //tabsAdapter.addTab(SearchUsersFragment.class, argsUsersTab, getResources().getString(R.string.users_tab_title));
        viewPagerAdapter.addFragment(new FollowingPostsActivity(), "Post");
        viewPagerAdapter.addFragment(new ProfileFragment(), "Profile");
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);


//        reference = FirebaseDatabase.getInstance().getReference("Chats");
//        reference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                final ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
//                int unread = 0;
//                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
//                    Chat chat = snapshot.getValue(Chat.class);
//                    if (chat.getReceiver().equals(firebaseUser.getUid()) && !chat.isIsseen()){
//                        unread++;
//                    }
//                }
//
//                if (unread == 0){
//                    viewPagerAdapter.addFragment(new ChatsFragment(), "Chats");
//                } else {
//                    viewPagerAdapter.addFragment(new ChatsFragment(), "("+unread+") Chats");
//                }
//
//                viewPagerAdapter.addFragment(new PostFragment(), "Post");
//                viewPagerAdapter.addFragment(new ProfileFragment(), "Profile");
//                viewPager.setAdapter(viewPagerAdapter);
//                tabLayout.setupWithViewPager(viewPager);
////                viewPagerAdapter.notifyDataSetChanged();
//
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });

        

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CreatePostActivity.CREATE_NEW_POST_REQUEST:
                   onPostSaved(true);
                    break;

            }
        }
    }
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
       // toolbar.setNavigationIcon(R.drawable.ic_menu);
       setSupportActionBar(toolbar);
       //getSupportActionBar().setTitle("SilentQuot");
  //      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //Tools.setSystemBarColor(this, R.color.grey_1000);

    }
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_setting, menu);
    //    Tools.changeMenuIconColor(menu, Color.WHITE);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==R.id.action_search) {
                  Intent searchIntent = new Intent(this, SearchActivity.class);
                startActivity(searchIntent);
            Toast.makeText(getApplicationContext(), "SearchClick", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    public void openCreatePostActivity() {

             newPostDialog=new NewPostDialog(this);

            newPostDialog.showCustomDialog(FirebaseAuth.getInstance().getCurrentUser().getUid(), this, this, new OnPostCreatedListener() {
                @Override
                public void onPostSaved(boolean success) {

                    Fragment fragment = viewPagerAdapter.getItem(viewPager.getCurrentItem());
                    ((Refreshable)fragment).onRefreshPostsubmit();

                }
            });
    }


    @Override
    public void openBookMarkActivity() {
        Intent searchIntent = new Intent(this, BookMarkPostActivity.class);
        startActivity(searchIntent);
    }

    @Override
    public void hideCounterView() {

    }

    @Override
    public void openPostDetailsActivity(Post post, View v) {

    }

    @Override
    public void showFloatButtonRelatedSnackBar(int messageId) {

    }

    @Override
    public void openProfileActivity(String userId, View view) {

    }

    @Override
    public void refreshPostList() {

        Fragment fragment = viewPagerAdapter.getItem(viewPager.getCurrentItem());
        ((Refreshable)fragment).onRefreshPostsubmit();
    }

    @Override
    public void removePost() {

    }

    @Override
    public void updatePost() {

    }

    @Override
    public void showCounterView(int count) {

    }

    @Override
    public void openConnectionActivity() {
        Intent searchIntent = new Intent(this, ConnectionActivity.class);
        startActivity(searchIntent);
    }

    @Override
    public void onPostSaved(boolean success) {
            newPostDialog.dismissDialog();
            refreshPostList();

    }


    class ViewPagerAdapter extends FragmentPagerAdapter {

        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;

        ViewPagerAdapter(FragmentManager fm){
            super(fm);

            this.fragments = new ArrayList<>();
            this.fragments.clear();
            this.titles = new ArrayList<>();
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        public void addFragment(Fragment fragment, String title){
            fragments.add(fragment);
            titles.add(title);
        }

        // Ctrl + O

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }



    private void status(String status){
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        reference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @NonNull
    @Override
    public MainPresenter createPresenter() {
        if (presenter == null) {
            return new MainPresenter(this);
        }
        return presenter;
    }



    @Override
    protected void onPause() {
        super.onPause();
        status("offline");
    }



}
