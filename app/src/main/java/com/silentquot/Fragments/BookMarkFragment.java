package com.silentquot.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.silentquot.Adapter.PostAdapter;
import com.silentquot.Model.Post;
import com.silentquot.R;

import java.util.ArrayList;
import java.util.List;

import static com.google.firebase.firestore.Query.Direction.DESCENDING;

public class BookMarkFragment extends Fragment {


    private RecyclerView bookamark_recycler_view;
    private List<Post> bookmarklist;
    PostAdapter postAdapter;


    private StorageTask uploadTask;
    private String fuser;

    StorageReference storageReference;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mauth;
    private boolean firstPageLoaded = true;
    private DocumentSnapshot lastVisible;
    String postType = null;

    public BookMarkFragment() {
        // Required empty public constructor

    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_book_mark, container, false);

        firebaseFirestore = FirebaseFirestore.getInstance();
        mauth = FirebaseAuth.getInstance();
        fuser = mauth.getCurrentUser().getUid();
        storageReference = FirebaseStorage.getInstance().getReference();


        bookamark_recycler_view = view.findViewById(R.id.bookmark_recycler_view);
        bookmarklist = new ArrayList<>();
        bookmarklist.clear();
        postAdapter = new PostAdapter(getContext(), bookmarklist, BookMarkFragment.this);
        bookamark_recycler_view.setLayoutManager(new LinearLayoutManager(getActivity()));
        bookamark_recycler_view.setAdapter(postAdapter);




     com.google.firebase.database.Query ref = FirebaseDatabase.getInstance().getReference("bookmark").child(fuser).orderByChild("TimeStamp" );
     ref.addValueEventListener(new ValueEventListener() {
         @Override
         public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
             for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                 String postId=snapshot.child("postId").getValue(String.class);
                 if (postId!=null)
                     loadBookmarkPost(postId);
             }
         }
         @Override
         public void onCancelled(@NonNull DatabaseError databaseError) {

         }
     });

        return view;
    }



    public  void loadBookmarkPost(String PostId)
    {
        Query firstQuery = firebaseFirestore.collection("SilentQuotPost").orderBy("TimeStamp", DESCENDING).whereEqualTo(FieldPath.documentId(),PostId)
                .limit(3);
        bookmarklist.clear();
        firstQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                //get lastVisibile iff first page not loaded at starting
                if (firstPageLoaded) {

                    // Get the last visible documentSnapshot
                    if (documentSnapshots!=null)
                        if (documentSnapshots.size()!=0)
                        lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                }

                if (documentSnapshots!=null)
                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED) {
                            String PostId = doc.getDocument().getId();
                            Post post = doc.getDocument().toObject(Post.class).withId(PostId);
                            post.setId(PostId);
                            if (firstPageLoaded) {
                                bookmarklist.add(post);
                            }
                            //Add new post to top
                            else {
                                bookmarklist.add(0, post);
                            }
                            postAdapter.notifyDataSetChanged();
                        }
                    }

            }


        });
    }

}
