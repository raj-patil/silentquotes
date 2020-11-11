package com.silentquot.Adapter;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.StrictMode;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.silentquot.Model.Post;
import com.silentquot.Model.User;
import com.silentquot.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;
import static com.theartofdev.edmodo.cropper.CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE;
import static com.theartofdev.edmodo.cropper.CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE;
import static com.theartofdev.edmodo.cropper.CropImage.getActivityResult;

public class PostAdapter  extends  RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private Context mContext;
    private List<Post> mPost;

    private StorageTask uploadTask;
    private String fuser;

    private Uri img_URI = null;
    Dialog dialog;

    String PostType;
    StorageReference storageReference;
    Fragment mActivity;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mauth;

    final int POST_TYPE_USERPOST = 0;
    final int POST_TYPE_COLLABPOST= 1;

    String collabText,collabImg , collabType;
    private DatabaseReference mRootDatabase;
    public String mCurrentUserId;
    private DatabaseReference mBookmarkDatabase;


    public PostAdapter(Context mContext, List<Post> mPost , Fragment mActivity){
        this.mPost = mPost;
        this.mContext = mContext;
        this.mActivity=mActivity;

    }

    @NonNull
    @Override
    public PostAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=null ;
        if (viewType == POST_TYPE_USERPOST) {
             view = LayoutInflater.from(mContext).inflate(R.layout.post_feed_item, parent, false);
        }else if(viewType == POST_TYPE_COLLABPOST)
        {
            view = LayoutInflater.from(mContext).inflate(R.layout.collab_post_item, parent, false);
        }

        return new PostAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final PostAdapter.ViewHolder holder, int position) {

        final Post postItem = mPost.get(position);

        String UserId=postItem.getUserId();
        final FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();

       mCurrentUserId = fuser.getUid();
        mRootDatabase =FirebaseDatabase.getInstance().getReference();

        if (UserId!=null) {
            DatabaseReference userreference = FirebaseDatabase.getInstance().getReference("Users").child(UserId);
            userreference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user!=null) {
                        holder.post_user_name.setText(user.getUsername());
                        if (user.getImageURL().equals("default")) {
                            holder.post_user_image.setImageResource(R.mipmap.ic_launcher);
                        } else {
                            Glide.with(mContext.getApplicationContext()).load(user.getImageURL()).into(holder.post_user_image);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }



        if (UserId.equals(fuser.getUid()))
        {
            holder.btn_post_delete.setEnabled(true);
            holder.btn_download_image.setEnabled(false);
            holder.btn_download_image.setVisibility(View.GONE);
            holder.btn_post_delete.setVisibility(View.VISIBLE);

            holder.btn_post_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case DialogInterface.BUTTON_POSITIVE:
                                    firebaseFirestore = FirebaseFirestore.getInstance();
                                    firebaseFirestore.collection("SilentQuotPost").document(postItem.getId()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            mPost.remove(position);

                                            if (position<2)
                                            {
                                                // mPost.clear();
                                            }
                                            notifyDataSetChanged();
                                        }
                                    });


                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    //No button clicked
                                    break;
                            }
                        }
                    };
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setMessage("Are you sure? ").setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();
                }
            });

        }
        else
        {

            holder.btn_post_delete.setEnabled(false);
            holder.btn_download_image.setEnabled(true);
            holder.btn_download_image.setVisibility(View.VISIBLE);
            holder.btn_post_delete.setVisibility(View.GONE);
            holder.btn_download_image.setVisibility(View.VISIBLE);
            holder.btn_download_image.setEnabled(true);

        }



        if(("USERPOST").equals(postItem.getType())) {

            if (postItem.getPostType().equals("TextImage")) {
                holder.post_text.setText(postItem.getDescription());
                holder.post_image.setVisibility(View.VISIBLE);
                Glide.with(mContext).load(postItem.getImageUrl()).into(holder.post_image);
                holder.btn_download_image.setVisibility(View.VISIBLE);
                holder.btn_download_image.setEnabled(true);
            }else if (postItem.getPostType().equals("Text"))
            {
                holder.post_image.setVisibility(View.GONE);
                holder.post_text.setText(postItem.getDescription());
                holder.btn_download_image.setVisibility(View.INVISIBLE);


            }else
                if (postItem.getPostType().equals("Image"))
                {
                    holder.post_text.setVisibility(View.INVISIBLE);
                    Glide.with(mContext.getApplicationContext()).load(postItem.getImageUrl()).into(holder.post_image);
                    holder.CopyText.setVisibility(View.INVISIBLE);
                    holder.btn_download_image.setVisibility(View.VISIBLE);
                    holder.btn_download_image.setEnabled(true);
                }
        }



                if (postItem.getType().equals("USERPOST") || postItem.getType().equals("COLLABPOST") && postItem.getPostType().equals("Text") || postItem.getPostType().equals("TextImage") )
                {

                    holder.CopyText.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("label", postItem.getDescription());
                            clipboard.setPrimaryClip(clip);
                            Toast.makeText(mContext, "Copy To The ClipBoard", Toast.LENGTH_SHORT).show();
                        }
                    });

                }

            holder.CollabtBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    collabType = postItem.getPostType();
                    collabImg = postItem.getImageUrl();
                    collabText = postItem.getDescription();
                    showCustomDialog(fuser.getUid(), mContext);

                }
            });

        holder.btn_download_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String randomName = UUID.randomUUID().toString();
                Date date = Calendar.getInstance().getTime();

                if(checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE  , 101))
                {
                    downloadfile(mContext, "SilentQuot_"+ date, ".jpg", "/SilentQuot/Images/", postItem.getImageUrl(), holder.download_progress, holder.btn_download_image);
                    Toast.makeText(mContext, "Post Downloaded at PICTURE/SilentQuot", Toast.LENGTH_SHORT).show();
                }
                else {
                    ActivityCompat
                            .requestPermissions(mActivity.getActivity(),
                                    new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE },
                                    101);
                }

            }
        });

                if (("COLLABPOST").equals(postItem.getType()))
                {
                    Toast.makeText(mContext,"CollabPost",Toast.LENGTH_LONG);
                    if (postItem.getPostType().equals("Text"))
                    {
                         if (postItem.getCollabPostType().equals("Text"))
                         {
                             holder.CollabText.setText(postItem.getCollabtext());
                             holder.post_text.setText(postItem.getDescription());
                             holder.btn_download_image.setVisibility(View.INVISIBLE);
                             Toast.makeText(mContext,postItem.getDescription(),Toast.LENGTH_LONG);
                         }
                         else if(postItem.getCollabPostType().equals("TextImage"))
                         {
                          holder.CollabImage.setEnabled(true);
                          holder.CollabImage.setVisibility(View.VISIBLE);
                             holder.btn_download_image.setVisibility(View.INVISIBLE);
                             Glide.with(mContext.getApplicationContext()).load(postItem.getCollabImage()).into(holder.CollabImage);
                             holder.CollabText.setText(postItem.getCollabtext());
                             holder.post_text.setText(postItem.getDescription());

                         }
                         else if (postItem.getCollabPostType().equals("Image"))
                         {
                             holder.CollabImage.setEnabled(true);
                             holder.CollabImage.setVisibility(View.VISIBLE);
                             holder.btn_download_image.setVisibility(View.INVISIBLE);
                             Glide.with(mContext.getApplicationContext()).load(postItem.getCollabImage()).into(holder.CollabImage);
                             holder.CollabText.setVisibility(View.INVISIBLE);
                         }
                    }
                }



            holder.SharePost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (postItem.getPostType().equals("TextImage")) {
                        Bitmap bitmap = getBitmapFromView(holder.post_image);
                        File file = new File(mContext.getExternalCacheDir(), "SilentQuot.png");
                        try {
                            FileOutputStream fout = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fout);
                            fout.flush();
                            fout.close();
                            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                            StrictMode.setVmPolicy(builder.build());
                            file.setReadable(true, false);
                            final Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra(Intent.EXTRA_TEXT, postItem.getDescription());
                            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                            intent.setType("image/png");
                            mContext.startActivity(Intent.createChooser(intent, "Share a Quot"));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else if (postItem.getPostType().equals("Text"))
                    {
                        final Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra(Intent.EXTRA_TEXT, postItem.getDescription());
                        intent.setType("Text");
                        mContext.startActivity(Intent.createChooser(intent, "Share a Quot"));

                    }
                }
            });

        mBookmarkDatabase =  FirebaseDatabase.getInstance().getReference().child("bookmark");
        mBookmarkDatabase.child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists())
                        {
                            if (postItem.getId()!=null)
                                if (dataSnapshot.hasChild(postItem.getId())) {
                                    String postId = dataSnapshot.child(postItem.getId()).child("postId").getValue().toString();
                                     if (postId.equals(postItem.getId()))
                                     {
                                         holder.btn_post_bookmark.setBackground(mContext.getResources().getDrawable(R.drawable.ic_bookmark));
                                     }
                                     else
                                     {
                                         holder.btn_post_bookmark.setBackground(mContext.getResources().getDrawable(R.drawable.ic_bookmark_border));
                                     }
                                }
                            else {
                                holder.btn_post_bookmark.setBackground(mContext.getResources().getDrawable(R.drawable.ic_bookmark_border));
                            }
                        }
                        else {
                            holder.btn_post_bookmark.setBackground(mContext.getResources().getDrawable(R.drawable.ic_bookmark_border));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        holder.btn_post_bookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mBookmarkDatabase.child(mCurrentUserId).child(postItem.getId()).addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists())
                                {
//                                    if (dataSnapshot.hasChild(postItem.getId())) {
//                                        String postId = dataSnapshot.child(postItem.getId()).child("postId").getValue().toString();
//                                        if (postId.equals(postItem.getId()))
//                                        {
                                            Map map = new HashMap();
                                            map.put("bookmark/"+ mCurrentUserId + "/"+postItem.getId()+"/" + "postId",null);
                                            map.put("bookmark/"+mCurrentUserId+"/"+postItem.getId() +"/"+"TimeStamp",  null);
                                            mRootDatabase.updateChildren(map, new DatabaseReference.CompletionListener() {
                                                @Override
                                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                                    holder.btn_post_bookmark.setBackground(mContext.getResources().getDrawable(R.drawable.ic_bookmark_border));
                                                    notifyDataSetChanged();
                                                }
                                            });

                                }
                                else {
                                    Map map = new HashMap();
                                    map.put("bookmark/"+ mCurrentUserId + "/"+postItem.getId()+"/" + "postId",postItem.getId());
                                    map.put("bookmark/"+mCurrentUserId+"/"+postItem.getId() +"/"+"TimeStamp",  ServerValue.TIMESTAMP);
                                    mRootDatabase.updateChildren(map, new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                            holder.btn_post_bookmark.setBackground(mContext.getResources().getDrawable(R.drawable.ic_bookmark));
                                            notifyDataSetChanged();
                                        }
                                    });

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

            }
        });

      }


    @Override
    public int getItemViewType(int position) {

       // final Post postItem = mPost.get(position);
       // String Type=postItem.getType();
        if(("USERPOST").equals(mPost.get(position).getType()))
             return POST_TYPE_USERPOST;
        else if(("COLLABPOST").equals(mPost.get(position).getType()))
            return POST_TYPE_COLLABPOST;

        return  -1;
    }

    public void showCustomDialog(String userId , final Context mContext ) {

      dialog = new Dialog(mContext);
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
                        CircleImageView userImg = dialog.findViewById(R.id.add_post_user_img);
                        Glide.with( mContext.getApplicationContext()).load(user.getImageURL()).into(userImg);

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


        mauth = FirebaseAuth.getInstance();
        fuser = mauth.getCurrentUser().getUid();
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
                               uploadPost(fuser , mUri , postText , PostType , collabText,collabImg,collabType );
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
                    uploadPost(fuser , null , postText , PostType , collabText,collabImg,collabType );

                }
                else if(img_URI != null)
                { PostType="Image";
                    uploadPost(fuser , null , postText , PostType ,collabText,collabImg,collabType);

                }

                Toast.makeText(mContext, "Post Submitted", Toast.LENGTH_SHORT).show();
            }
        });

        ((ImageButton) dialog.findViewById(R.id.bt_photo)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = CropImage.activity().setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512,512)
                        .setAspectRatio(1,1)
                        .getIntent(mContext);
               mActivity.startActivityForResult(intent, CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE);
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

    private Bitmap getBitmapFromView(View view)
    {
        Bitmap returnedBitmap=Bitmap.createBitmap(view.getWidth() , view.getHeight() ,Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(returnedBitmap);
        Drawable bgDrawable=view.getBackground();
        if(bgDrawable!=null)
        {
            bgDrawable.draw(canvas);
        }else {
            canvas.drawColor(Color.WHITE);
        }
        view.draw(canvas);
        return returnedBitmap;
    }

    public  void onActivityResult(int requestCode, int resultCode, Intent data) {

        img_URI=null;
        final ImageView imageView = dialog.findViewById(R.id.post_image_preview);
        imageView.setImageURI(null);
        if (requestCode == CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = getActivityResult(data);
            if (resultCode == RESULT_OK) {
                img_URI = result.getUri();

                imageView.setImageURI(img_URI);
//                String tct = main_uri.toString();
                Toast.makeText(mContext, img_URI.toString(), Toast.LENGTH_LONG).show();
            } else if (resultCode == CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(mContext, "Error" + error, Toast.LENGTH_LONG).show();
            }
        }

    }


    public void uploadPost(String userId, String PostImgUrl,String PostDescription , String PostType , String collabText , String collabImg , String collabType)
    {
        firebaseFirestore = FirebaseFirestore.getInstance();

        final Map<String,Object> postMap = new HashMap<>();
        postMap.put("ImageUrl",PostImgUrl);
        postMap.put("Description",PostDescription);
        postMap.put("UserId",userId);
        postMap.put("PostType",PostType);
        postMap.put("Type" , "COLLABPOST");
        postMap.put("CollabPostType" , collabType);
        postMap.put("CollabText" , collabText);
        postMap.put("CollabImage" , collabImg);
        postMap.put("TimeStamp", FieldValue.serverTimestamp());


         firebaseFirestore.collection("SilentQuotPost").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(mContext, "Successfully Posted", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(mContext, "Error" + task.getException().toString(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    // This function is called when user accept or decline the permission.
// Request Code is used to check which permission called this function.
// This request code is provided when user is prompt for permission.


    // Function to check and request permission
    public boolean checkPermission(String permission, int requestCode)
    {

        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(
                mContext,
                permission)
                == PackageManager.PERMISSION_DENIED) {

            return  false;
        }
        else {
          return  true;
        }
    }



    public  void downloadfile(Context context, String filename, String fileExt, String dest, String url, final ProgressBar download_progress, final ImageButton btn_download_image)
    {
        download_progress.setVisibility(View.VISIBLE);
        btn_download_image.setVisibility(View.INVISIBLE);
        btn_download_image.setEnabled(false);
        download_progress.setEnabled(true);
        File direct = new File( "/"+"SilentQuot"+"/"+"Images");

//        checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 101);
        if (!direct.exists()) {
            direct.mkdir();
            Toast.makeText(mContext,direct.toString(),Toast.LENGTH_SHORT).show();
        }
        final DownloadManager downloadManager=(DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri=Uri.parse(url);
        DownloadManager.Request request=new DownloadManager.Request(uri);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(direct.toString(),"/"+filename+fileExt);

//        if (ContextCompat.checkSelfPermission(
//                mContext,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                == PackageManager.PERMISSION_DENIED) {
//            ActivityCompat
//                    .requestPermissions(mActivity.getActivity(),
//                            new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE },
//                            101);
//        }
//        else {
            final long downloadId = downloadManager.enqueue(request);



        new Thread(new Runnable() {
            boolean downloading = true;
            @Override
            public void run() {

                while (downloading) {

                    DownloadManager.Query q = new DownloadManager.Query();
                    q.setFilterById(downloadId);
                    final Cursor cursor = downloadManager.query(q);
                    cursor.moveToFirst();
                    final int bytes_downloaded = cursor.getInt(cursor
                            .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                    final int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                    if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                        downloading = false;
                        download_progress.setEnabled(false);
                        download_progress.setVisibility(View.INVISIBLE);
                        btn_download_image.setEnabled(true);


                    }
                      final int dl_progress = (int) ((bytes_downloaded * 100l) / bytes_total);
                    ((Activity)mContext).runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            if (downloading==false)
                                btn_download_image.setVisibility(View.VISIBLE);
                            download_progress.setProgress((int) dl_progress);

                        }
                    });
                    cursor.close();
                }
            }
        }).start();
//        }
    }

    @Override
    public int getItemCount() {
        return mPost.size();
    }

    public  class ViewHolder extends RecyclerView.ViewHolder{

        public TextView post_text , post_user_name;
        public ImageView post_image , post_user_image;
        public Button join_btn;
        public ImageButton btn_download_image;
        public ImageButton CopyText;
        public  ImageButton SharePost;
        public ProgressBar download_progress;
        public  Button CollabtBtn;
        public  TextView CollabText ;
        public  ImageView CollabImage;
        public TextView CollabuserName;
        public  ImageButton btn_post_delete;
        public  Button btn_post_bookmark;

        public ViewHolder(View itemView) {
            super(itemView);

            post_text = itemView.findViewById(R.id.post_item_desc);
            post_image = itemView.findViewById(R.id.post_item_post_image);
            post_user_image = itemView.findViewById(R.id.post_item_user_img);
            post_user_name = itemView.findViewById(R.id.post_item_user_Name);
            btn_download_image=itemView.findViewById(R.id.post_item_btn_download);
            CopyText=itemView.findViewById(R.id.post_item_btn_copy);
            SharePost=itemView.findViewById(R.id.post_item_btn_share);
            download_progress=itemView.findViewById(R.id.post_item_download_progressbar);
            CollabtBtn=itemView.findViewById(R.id.post_item_btn_collab);
            CollabImage=itemView.findViewById(R.id.collab_post_img);
            CollabText=itemView.findViewById(R.id.collab_post_description);
            CollabuserName=itemView.findViewById(R.id.collab_post_userName);
            btn_post_delete=itemView.findViewById(R.id.post_item_btn_Delete_post);
            btn_post_bookmark=itemView.findViewById(R.id.post_item_btn_bookmark);

        }
    }

}
