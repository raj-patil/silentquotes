package com.silentquot.socialcomponents.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.silentquot.R;
import com.silentquot.socialcomponents.main.interactors.ProfileInteractor;
import com.silentquot.socialcomponents.main.post.BaseCreatePostPresenter;
import com.silentquot.socialcomponents.main.post.createPost.CreatePostActivity;
import com.silentquot.socialcomponents.main.post.createPost.CreatePostView;
import com.silentquot.socialcomponents.managers.PostManager;
import com.silentquot.socialcomponents.managers.listeners.OnObjectChangedListenerSimple;
import com.silentquot.socialcomponents.managers.listeners.OnPostCreatedListener;
import com.silentquot.socialcomponents.model.Post;
import com.silentquot.socialcomponents.model.Profile;
import com.silentquot.socialcomponents.utils.LogUtil;

import de.hdodenhof.circleimageview.CircleImageView;

public class NewPostDialog  extends BaseCreatePostPresenter<CreatePostView> implements OnPostCreatedListener {

    private Dialog dialog;
    private ProfileInteractor profileInteractor;
    private PostManager postManager;
    String autherName;
    private ProgressDialog progressDialog;
    String QUOTES ="quotes" ;

    public NewPostDialog(Context context) {
        super(context);
    }


    public void showCustomDialog(String userId, final Context mContext, final Activity mActivity , OnPostCreatedListener onPostCreatedListener) {
        profileInteractor = ProfileInteractor.getInstance(mContext);
        dialog = new Dialog(mContext);

        dialog.setContentView(R.layout.dialog_add_post);
        dialog.setCancelable(true);
        profileInteractor.getProfileSingleValue(userId, new OnObjectChangedListenerSimple<Profile>() {
            @Override
            public void onObjectChanged(Profile user) {
                TextView userName = dialog.findViewById(R.id.add_post_user_name);
                userName.setText(user.getUsername());
                autherName = user.getUsername();
                if (user.getPhotoUrl() != null) {
                    if (user.getPhotoUrl().equals("default")) {
                        CircleImageView userImg = dialog.findViewById(R.id.add_post_user_img);
                        Glide.with(mContext.getApplicationContext()).load(R.drawable.ic_stub).into(userImg);
                        userName.setText(user.getUsername());
                    } else {
                        CircleImageView userImg = dialog.findViewById(R.id.add_post_user_img);
                        Glide.with(mContext.getApplicationContext()).load(user.getPhotoUrl()).into(userImg);
                    }
                } else {
                    CircleImageView userImg = dialog.findViewById(R.id.add_post_user_img);
                    Glide.with(mContext.getApplicationContext()).load(R.drawable.ic_stub).into(userImg);
                }

                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(dialog.getWindow().getAttributes());
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.MATCH_PARENT;

                dialog.show();
                dialog.getWindow().setAttributes(lp);
            }
        });


        final Button btn_submit_post = (Button) dialog.findViewById(R.id.bt_submit);
        ((EditText) dialog.findViewById(R.id.et_post)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btn_submit_post.setEnabled(!s.toString().trim().isEmpty());
                Toast.makeText(mContext, "submitbtn", Toast.LENGTH_SHORT);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }

        });

        btn_submit_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog = new ProgressDialog(context);
                progressDialog.setMessage("posting");
                progressDialog.setCancelable(false);
                progressDialog.show();
                postManager = PostManager.getInstance(mContext);
                final String postText = ((EditText) dialog.findViewById(R.id.et_post)).getText().toString();
                savePost(autherName, postText, "TEXT" , onPostCreatedListener);
            }
        });

        ((ImageButton) dialog.findViewById(R.id.bt_photo)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String postText = ((EditText) dialog.findViewById(R.id.et_post)).getText().toString();
                if (!TextUtils.isEmpty(postText)) {
                    Intent intent = new Intent(mContext, CreatePostActivity.class);
                    intent.putExtra(QUOTES , postText);
                    mActivity.startActivityForResult(intent, CreatePostActivity.CREATE_NEW_POST_REQUEST);
                }

                Toast.makeText(mContext, "Write a Quote First", Toast.LENGTH_SHORT).show();

                ((ImageButton) dialog.findViewById(R.id.bt_setting)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(mContext, "Post Setting Clicked", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
    }


    @Override
    protected int getSaveFailMessage() {
        return 0;
    }

    @Override
    protected void savePost(String title, String description, String PostType) {

    }


    protected void savePost(String title, String description, String PostType , OnPostCreatedListener onPostCreatedListener) {
        //    ifViewAttached(view -> {
        //        view.showProgress(R.string.message_creating_post);
        Post post = new Post();
        post.setTitle(description);
        post.setDescription(title);
        post.setPostType(PostType);
        post.setCollabPostId("default");
        post.setAuthorId(FirebaseAuth.getInstance().getCurrentUser().getUid());

        postManager.createOrUpdatePostWithImage(null, new OnPostCreatedListener() {
            @Override
            public void onPostSaved(boolean success) {
                dismissDialog();
                progressDialog.dismiss();
                onPostCreatedListener.onPostSaved(true);

            }
        }, post);


        //      });

    }


    protected void savePost(String title, String description, String PostType, String CollabPostId) {
        //   ifViewAttached(view -> {
        //view.showProgress(R.string.message_creating_post);
        Post post = new Post();
        post.setTitle(description);
        post.setDescription(title);
        post.setPostType(PostType);
        post.setCollabPostId(CollabPostId);
        post.setAuthorId(FirebaseAuth.getInstance().getCurrentUser().getUid());

        postManager.createOrUpdatePostWithImage(null, this, post);

        //        });

    }


    @Override
    protected boolean isImageRequired() {
        return false;
    }

    public void showCollabDialog(String userId, Context mContext, String postId) {

        profileInteractor = ProfileInteractor.getInstance(context);
        dialog = new Dialog(mContext);
        dialog.setContentView(R.layout.dialog_add_post);
        dialog.setCancelable(true);
        profileInteractor.getProfileSingleValue(userId, new OnObjectChangedListenerSimple<Profile>() {
            @Override
            public void onObjectChanged(Profile user) {
                TextView userName = dialog.findViewById(R.id.add_post_user_name);
                userName.setText(user.getUsername());
                autherName = user.getUsername();
                if (user.getPhotoUrl() != null) {
                    if (user.getPhotoUrl().equals("default")) {
                        CircleImageView userImg = dialog.findViewById(R.id.add_post_user_img);
                        Glide.with(mContext.getApplicationContext()).load(R.drawable.ic_stub).into(userImg);
                        //  userName.setText(user.getUsername());
                    } else {
                        CircleImageView userImg = dialog.findViewById(R.id.add_post_user_img);
                        Glide.with(mContext.getApplicationContext()).load(user.getPhotoUrl()).into(userImg);
                    }
                } else {
                    CircleImageView userImg = dialog.findViewById(R.id.add_post_user_img);
                    Glide.with(mContext.getApplicationContext()).load(R.drawable.ic_stub).into(userImg);
                }

                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(dialog.getWindow().getAttributes());
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.MATCH_PARENT;

                dialog.show();
                dialog.getWindow().setAttributes(lp);
            }
        });


        final Button btn_submit_post = (Button) dialog.findViewById(R.id.bt_submit);
        ((EditText) dialog.findViewById(R.id.et_post)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btn_submit_post.setEnabled(!s.toString().trim().isEmpty());
                Toast.makeText(mContext, "submitbtn", Toast.LENGTH_SHORT);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btn_submit_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postManager = PostManager.getInstance(mContext);
                final String postText = ((EditText) dialog.findViewById(R.id.et_post)).getText().toString();
                Toast.makeText(mContext, "PostSubmitButton Click", Toast.LENGTH_SHORT).show();
                savePost(autherName, postText, "COLLAB", postId);
            }

        });

        ((ImageButton) dialog.findViewById(R.id.bt_photo)).setEnabled(false);
        ((ImageButton) dialog.findViewById(R.id.bt_photo)).setVisibility(View.GONE);

    }

    public void dismissDialog(){
        dialog.dismiss();
    }

    @Override
    public void onPostSaved(boolean success) {
        creatingPost = false;
            dialog.dismiss();
            Toast.makeText(context , "post save",Toast.LENGTH_SHORT).show();
            ifViewAttached(view -> {
            view.hideProgress();
            if (success) {
                view.onPostSavedSuccess();
                LogUtil.logDebug(TAG, "Post was saved");
            } else {
                view.showSnackBar(getSaveFailMessage());
                LogUtil.logDebug(TAG, "Failed to save a post");
            }
        });
    }
}