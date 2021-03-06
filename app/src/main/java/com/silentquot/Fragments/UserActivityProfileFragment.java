package com.silentquot.Fragments;

import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.silentquot.Adapter.FirebasePostAdapter;
import com.silentquot.Model.Post;
import com.silentquot.ProfileActivity;
import com.silentquot.R;

import java.util.ArrayList;
import java.util.List;

public class UserActivityProfileFragment extends Fragment {

    FloatingActionButton create_post_btn;
    private RecyclerView post_list_recycler_view;
    List<Post> postList;
    FirebasePostAdapter postAdapter;


    private StorageTask uploadTask;
    private String fuser;

    private Uri img_URI = null;
    Dialog dialog;

    String PostType;
    StorageReference storageReference;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mauth;
    private boolean firstPageLoaded = true;
    private DocumentSnapshot lastVisible;
    String postType=null;
    String userId;

    public UserActivityProfileFragment() {
        // Required empty public constructor

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view= inflater.inflate(R.layout.fragment_user_activity_profile, container, false);

        firebaseFirestore = FirebaseFirestore.getInstance();

//        Bundle bundle = getArguments();
//        Intent intentReceived = getIntent();
//        Bundle data = getIn.getExtras();
   //     userId=getArguments().getString("userId");
ProfileActivity activity=(ProfileActivity)getActivity();
userId=activity.getUserId();
        storageReference= FirebaseStorage.getInstance().getReference();


        post_list_recycler_view = view.findViewById(R.id.my_Activity_recycler_view);
        postList = new ArrayList<>();
        postList.clear();
      //  postAdapter = new PostAdapter(getContext(), postList , UserActivityProfileFragment.this);
        post_list_recycler_view.setLayoutManager(new LinearLayoutManager(getActivity()));
      //  post_list_recycler_view.setAdapter(postAdapter);

//        post_list_recycler_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//
//                boolean lastItem = !recyclerView.canScrollVertically(1);
//
//                if (lastItem) {
//                    loadMorePosts();
//                }
//            }
//        });



        Query firstQuery = firebaseFirestore.collection("SilentQuotPost").orderBy("TimeStamp", Query.Direction.DESCENDING).whereEqualTo("UserId" , userId);

        FirestoreRecyclerOptions<Post> options = new FirestoreRecyclerOptions.Builder<Post>()
                .setQuery(firstQuery, Post.class )
                .build();
        postAdapter = new FirebasePostAdapter(options);
        post_list_recycler_view.setAdapter(postAdapter);



//        Query firstQuery = firebaseFirestore.collection("SilentQuotPost").orderBy("TimeStamp", Query.Direction.DESCENDING).whereEqualTo("UserId" , userId)
//                .limit(3);
//        postList.clear();
//        firstQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
//            @Override
//            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
//                //get lastVisibile iff first page not loaded at starting
//                if (firstPageLoaded) {
//
//                    // Get the last visible documentSnapshot
//                    if (documentSnapshots!=null)
//                        lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
//                }
//
//                if (documentSnapshots!=null)
//                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
//                        if (doc.getType() == DocumentChange.Type.ADDED) {
//                            String PostId = doc.getDocument().getId();
//                            Post post = doc.getDocument().toObject(Post.class).withId(PostId);
//                            post.setId(PostId);
//                            if (firstPageLoaded) {
//                                postList.add(post);
//                            }
//                            //Add new post to top
//                            else {
//                                postList.add(0, post);
//                            }
//                            postAdapter.notifyDataSetChanged();
//                        }
//                    }
//
//            }
//
//
//        });
//
//





        return view;
    }



    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {



        }
    }
    @Override
    public void onStart() {
        super.onStart();
        postAdapter.startListening();

    }


    @Override
    public void onStop() {
        super.onStop();
        postAdapter.stopListening();
    }




    private void loadMorePosts()
    {
        if (userId != null)
        {
            Query nextQuery = firebaseFirestore.collection("SilentQuotPost").whereEqualTo("UserId" , userId)
                    .orderBy("TimeStamp", Query.Direction.DESCENDING)
                    .startAfter(lastVisible)
                    .limit(3);

            nextQuery.addSnapshotListener( new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                    //If no more posts than docSnaps will be empty leading to crash
                    if (documentSnapshots!=null)
                        if (!documentSnapshots.isEmpty()) {
                            // Get the last visible documentSnapshot
                            lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                            for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    String PostId = doc.getDocument().getId();
                                    Post post = doc.getDocument().toObject(Post.class).withId(PostId);
                                    post.setId(PostId);
                                    postList.add(post);
                                    postAdapter.notifyDataSetChanged();
                                }
                            }

                        }
                }
            });
        }
    }

}
