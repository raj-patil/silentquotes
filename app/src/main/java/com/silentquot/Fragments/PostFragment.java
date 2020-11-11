package com.silentquot.Fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.silentquot.Adapter.FirebasePostAdapter;
import com.silentquot.Adapter.PostAdapter;
import com.silentquot.Model.Post;
import com.silentquot.Model.User;
import com.silentquot.R;
import com.silentquot.socialcomponents.adapters.FollowPostsAdapter;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;
import static com.theartofdev.edmodo.cropper.CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE;
import static com.theartofdev.edmodo.cropper.CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE;
import static com.theartofdev.edmodo.cropper.CropImage.activity;
import static com.theartofdev.edmodo.cropper.CropImage.getActivityResult;


public class PostFragment extends Fragment {

    FloatingActionButton create_post_btn;
    RecyclerView post_list_recycler_view;
    List<Post> postList;
    PostAdapter postAdapter;
    FollowPostsAdapter  postsAdapter;


    private StorageTask uploadTask;
    private String fuser;

    private Uri img_URI = null;
    Dialog dialog;

    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;

    String PostType;
    StorageReference storageReference;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mauth;
    private boolean firstPageLoaded ;
    private DocumentSnapshot lastVisible;
    String postType=null;
    private boolean posted = false;

    FirebasePostAdapter postAdapter1;
    public PostFragment() {
        // Required empty public constructor

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        firstPageLoaded=true;

        firebaseFirestore = FirebaseFirestore.getInstance();
        mauth = FirebaseAuth.getInstance();
        fuser = mauth.getCurrentUser().getUid();
        storageReference= FirebaseStorage.getInstance().getReference();
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_post, container, false);
        create_post_btn=view.findViewById(R.id.fab_add_post);

        create_post_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              //  final PostUpload postUpload=new PostUpload(getContext() , getActivity());
                postType="userPost";
                showCustomDialog(fuser, getContext() , getActivity());

            }
        });



        post_list_recycler_view = view.findViewById(R.id.post_recycler_view);
        postList = new ArrayList<>();
        postList.clear();
        //postAdapter = new PostAdapter(getContext(), postList , PostFragment.this);
        post_list_recycler_view.setLayoutManager(new LinearLayoutManager(getActivity()));
        //post_list_recycler_view.setAdapter(postAdapter);

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


        /////////////////////**** FIreBAseRecycleAdapter*****////////

        Query firstQuery = firebaseFirestore.collection("SilentQuotPost").orderBy("TimeStamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Post> options = new FirestoreRecyclerOptions.Builder<Post>()
                .setQuery(firstQuery, Post.class )
                .build();
        postAdapter1 = new FirebasePostAdapter(options);
        post_list_recycler_view.setAdapter(postAdapter1);




        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
       // postAdapter1.startListening();

    }


    @Override
    public void onStop() {
        super.onStop();
      //  postAdapter1.stopListening();
    }


    private void loadMorePosts()
            {


                if (mauth.getCurrentUser() != null)
                {
                    Query nextQuery = null;

                    if (lastVisible!=null)
                         nextQuery = firebaseFirestore.collection("SilentQuotPost")
                                .orderBy("TimeStamp", Query.Direction.DESCENDING)
                                .startAfter(lastVisible)
                                .limit(3);

                    firstPageLoaded=false;
                    if (nextQuery!=null)
                    nextQuery.addSnapshotListener( new EventListener<QuerySnapshot>() {
                  @Override
                   public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                      //If no more posts than docSnaps will be empty leading to crash
                    if (!documentSnapshots.isEmpty()) {
                        lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                String PostId = doc.getDocument().getId();
                                Post post = doc.getDocument().toObject(Post.class).withId(PostId);
                                post.setId(PostId);
                                if (posted==false)
                                  postList.add(post);
                                postAdapter.notifyDataSetChanged();
                            }
                        }

                    }
                }
            });
        }
    }



    public void showCustomDialog(String userId , final Context mContext , final  Activity mActivity) {
        dialog= new Dialog(mContext);
        dialog.setContentView(R.layout.dialog_add_post);
        dialog.setCancelable(true);
        DatabaseReference userreference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        userreference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user =dataSnapshot.getValue(User.class);
                TextView userName= dialog.findViewById(R.id.add_post_user_name);
                userName.setText(user.getUsername());
                if (user.getImageURL().equals("default")) {
                    CircleImageView userImg= dialog.findViewById(R.id.add_post_user_img);
                //    Glide.with(getContext().getApplicationContext() ).load(R.drawable.b).into(userImg);
                    userName.setText(user.getUsername());
                } else {
                    CircleImageView userImg= dialog.findViewById(R.id.add_post_user_img);
                    Glide.with(mContext.getApplicationContext()).load(user.getImageURL()).into(userImg);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;

        final Button btn_submit_post = (Button) dialog.findViewById(R.id.bt_submit);
        ((EditText) dialog.findViewById(R.id.et_post)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btn_submit_post.setEnabled(!s.toString().trim().isEmpty());
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btn_submit_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String postText = ((EditText) dialog.findViewById(R.id.et_post)).getText().toString();
                if(img_URI != null && !TextUtils.isEmpty(postText)) {
                    PostType="TextImage";

                    final String randomName = UUID.randomUUID().toString();
                    final StorageReference fileReference = storageReference.child("SilentQuot_Post  /"+randomName+".jpg");
                    uploadTask = fileReference.putFile(img_URI);
                    uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()){
                                // throw  task.getException();
                            }

                            return  fileReference.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()){
                                Uri downloadUri = task.getResult();
                                String mUri = downloadUri.toString();
                                uploadPost(fuser , mUri , postText , PostType );
                            } else {
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                        }
                    });
                }
                else if(!TextUtils.isEmpty(postText))
                {
                    PostType="Text";
                    uploadPost(fuser , null , postText , PostType );

                }
                else if(img_URI != null)
                { PostType="Image";
                    uploadPost(fuser , null , postText , PostType );

                }
            }
        });

        ((ImageButton) dialog.findViewById(R.id.bt_photo)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = activity().setGuidelines(CropImageView.Guidelines.ON)
                       .setMinCropResultSize(512,512)
                        .setAspectRatio(1,1)
                        .getIntent(mContext);
                startActivityForResult(intent, CROP_IMAGE_ACTIVITY_REQUEST_CODE);


//                CropImage.activity().setGuidelines(CropImageView.Guidelines.ON)
//                        .setMinCropResultSize(512,512)
//                        .setAspectRatio(1,1)
//                        .start(getActivity());
//
                Toast.makeText(mContext, "Post Photo Clicked", Toast.LENGTH_SHORT).show();
            }
        });


        ((ImageButton) dialog.findViewById(R.id.bt_setting)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "Post Setting Clicked", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }


    public void uploadPost(String userId, String PostImgUrl,String PostDescription , String PostType)
    {
        final ProgressDialog pd = new ProgressDialog(getContext());
        pd.setMessage("Uploading");
        pd.show();

        final Map<String,Object> postMap = new HashMap<>();
        postMap.put("ImageUrl",PostImgUrl);
        postMap.put("Description",PostDescription);
        postMap.put("UserId",userId);
        postMap.put("PostType",PostType);
        postMap.put("Type" , "USERPOST");
        postMap.put("TimeStamp", FieldValue.serverTimestamp());
        posted=true;



        firebaseFirestore.collection("SilentQuotPost").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {

                if (task.isSuccessful()) {

                    postAdapter.notifyDataSetChanged();
                    postAdapter.notifyItemInserted(0);
                    Toast.makeText(getContext(), "Successfully Posted", Toast.LENGTH_LONG).show();
                    MyActivityFragment.postAdapter1.startListening();
                   // firstPageLoaded=true;
                    dialog.dismiss();
                    pd.dismiss();
                    postAdapter.notifyDataSetChanged();
                }
                else {
                    Toast.makeText(getContext(), "Error" + task.getException().toString(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }



    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // super.onActivityResult(requestCode, resultCode, data);
        if(!("userPost").equals(postType)) {
            postAdapter.onActivityResult(requestCode, resultCode, data);
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);


            if (requestCode == CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = getActivityResult(data);
                if (resultCode == RESULT_OK) {

                    img_URI = result.getUri();
                    final ImageView imageView = dialog.findViewById(R.id.post_image_preview);
                    imageView.setImageURI(null);
                    imageView.setImageURI(img_URI);
//                String tct = main_uri.toString();
                    Toast.makeText(getContext(), img_URI.toString(), Toast.LENGTH_LONG).show();
                } else if (resultCode == CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                    Toast.makeText(getContext(), "Error" + error, Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getContext(), "fail", Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode,
                permissions,
                grantResults);

        if (requestCode == CAMERA_PERMISSION_CODE) {

            // Checking whether user granted the permission or not.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // Showing the toast message
                Toast.makeText(getContext(),
                        "Camera Permission Granted",
                        Toast.LENGTH_SHORT)
                        .show();
            }
            else {
                Toast.makeText(getContext(),
                        "Camera Permission Denied",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }
        else if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(),
                        "Storage Permission Granted",
                        Toast.LENGTH_SHORT)
                        .show();
            }
            else {
                Toast.makeText(getContext(),
                        "Storage Permission Denied",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

}
