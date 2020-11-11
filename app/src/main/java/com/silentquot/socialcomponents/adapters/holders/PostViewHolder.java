/*
 *  Copyright 2017 Rozdoum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.silentquot.socialcomponents.adapters.holders;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.silentquot.Constants;
import com.silentquot.R;
import com.silentquot.socialcomponents.controllers.BookMarkController;
import com.silentquot.socialcomponents.controllers.LikeController;
import com.silentquot.socialcomponents.managers.PostManager;
import com.silentquot.socialcomponents.managers.ProfileManager;
import com.silentquot.socialcomponents.managers.listeners.OnObjectChangedListener;
import com.silentquot.socialcomponents.managers.listeners.OnObjectChangedListenerSimple;
import com.silentquot.socialcomponents.managers.listeners.OnObjectExistListener;
import com.silentquot.socialcomponents.managers.listeners.OnPostChangedListener;
import com.silentquot.socialcomponents.model.Like;
import com.silentquot.socialcomponents.model.Post;
import com.silentquot.socialcomponents.model.Profile;
import com.silentquot.socialcomponents.utils.FormatterUtil;
import com.silentquot.socialcomponents.utils.GlideApp;
import com.silentquot.socialcomponents.utils.ImageUtil;

/**
 * Created by alexey on 27.12.16.
 */

public class PostViewHolder extends RecyclerView.ViewHolder {
    public static final String TAG = PostViewHolder.class.getSimpleName();
    private final View profileviewheadercardview;


    protected Context context;
    private ImageView postImageView;
    private TextView titleTextView;
    private TextView detailsTextView;
    private TextView likeCounterTextView;
    private ImageView likesImageView;
    private TextView commentsCountTextView;
    private TextView watcherCounterTextView;
    private TextView dateTextView;
    private ImageView authorImageView;
    private ViewGroup likeViewGroup;
    private ViewGroup bookmarkViewGroup;
    private  ImageView CopyText;
    private  ImageView SharePost;
    private AppCompatButton CollabPost;
    private TextView collabText;
    private  ImageView collabAutherImageView;
    private CardView collabContainer;
    private  ImageView bookMarkImageView;
    private TextView bookMarkTextView;

    private ProfileManager profileManager;
    protected PostManager postManager;

    private LikeController likeController;
    private BookMarkController bookMarkController;
    private Activity baseActivity;

    public PostViewHolder(View view, final OnClickListener onClickListener, Activity activity) {
        this(view, onClickListener, activity, true , 2 );
    }

    public PostViewHolder(View view, final OnClickListener onClickListener, Activity activity, boolean isAuthorNeeded, int POST_TYPE) {
        super(view);
        this.context = view.getContext();
        this.baseActivity = activity;
        postImageView = view.findViewById(R.id.postImageView);
        likeCounterTextView = view.findViewById(R.id.likeCounterTextView);
        likesImageView = view.findViewById(R.id.likesImageView);
        bookmarkViewGroup=view.findViewById(R.id.bookmarkcontainer);
        commentsCountTextView = view.findViewById(R.id.commentsCountTextView);
        watcherCounterTextView = view.findViewById(R.id.watcherCounterTextView);
        dateTextView = view.findViewById(R.id.dateTextView);
        titleTextView = view.findViewById(R.id.titleTextView);
        detailsTextView = view.findViewById(R.id.detailsTextView);
        authorImageView = view.findViewById(R.id.authorImageView);
        likeViewGroup = view.findViewById(R.id.likesContainer);
        profileviewheadercardview=view.findViewById(R.id.profileviewheadercardview);
        if (POST_TYPE==1)
        profileviewheadercardview.setVisibility(isAuthorNeeded ? View.VISIBLE : View.GONE);
        authorImageView.setVisibility(isAuthorNeeded ? View.VISIBLE : View.GONE);
        profileManager = ProfileManager.getInstance(context.getApplicationContext());
        postManager = PostManager.getInstance(context.getApplicationContext());
        CopyText=view.findViewById(R.id.post_item_btn_copy);
        SharePost=view.findViewById(R.id.post_item_btn_share);
        CollabPost=view.findViewById(R.id.collab_post_btn);
        collabText=view.findViewById(R.id.collabText);
        collabAutherImageView=view.findViewById(R.id.collabauthorImageView1);
        collabContainer=view.findViewById(R.id.collabcontainer);
        bookMarkImageView=view.findViewById(R.id.bookmarkImageView);
        bookMarkTextView=view.findViewById(R.id.bookmarTextView);

        view.setOnClickListener(v -> {
            int position = getAdapterPosition();
            if (onClickListener != null && position != RecyclerView.NO_POSITION) {
                onClickListener.onItemClick(getAdapterPosition(), v);
            }
        });
        likeViewGroup.setOnClickListener(view1 -> {
            int position = getAdapterPosition();
            if (onClickListener != null && position != RecyclerView.NO_POSITION) {
                onClickListener.onLikeClick(likeController, position);
            }
        });
        bookmarkViewGroup.setOnClickListener(view1 -> {
            int position = getAdapterPosition();
            if (onClickListener != null && position != RecyclerView.NO_POSITION) {
                onClickListener.onBookMarkClick(bookMarkController, position);
            }
        });


        if (authorImageView!=null)
        authorImageView.setOnClickListener(v -> {
            int position = getAdapterPosition();
            if (onClickListener != null && position != RecyclerView.NO_POSITION) {
                onClickListener.onAuthorClick(getAdapterPosition(), v);
            }
        });

        if (collabAutherImageView!=null)
        collabAutherImageView.setOnClickListener( v ->{
            int position = getAdapterPosition();
            if (onClickListener != null && position != RecyclerView.NO_POSITION) {
                onClickListener.onCollabAutherClick(getAdapterPosition(), v);

            }
        });

        if (CopyText!=null)
        CopyText.setOnClickListener(v ->{
            int position = getAdapterPosition();
            if (onClickListener !=null && position!=RecyclerView.NO_POSITION)
            {

                onClickListener.onCopyClick(getAdapterPosition(),v);
            }
        });

        if (SharePost!=null)
            SharePost.setOnClickListener(v-> {
                int position = getAdapterPosition();
                if (onClickListener!=null && position != RecyclerView.NO_POSITION)
                {
                    onClickListener.onShareClick(getAdapterPosition() , postImageView , v);
                }
            });

        if (CollabPost!=null)
            CollabPost.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (onClickListener !=null && position!=RecyclerView.NO_POSITION)
                {
                    onClickListener.onCollabClick(getAdapterPosition() , v);
                }
            });
        if (collabContainer!=null)
            collabContainer.setOnClickListener( v -> {
                int position = getAdapterPosition();
                if (onClickListener!=null && position!=RecyclerView.NO_POSITION)
                {
                    onClickListener.onCollabContainerClick(getAdapterPosition() , v);
                }

            });

    }

    public void bindDataWithCollab(Post post)
    {
        bindDataWithText(post);
        if (post.getCollabPostId()!=null)
        postManager.getSinglePostValue(post.getCollabPostId(), new OnPostChangedListener(){

                    @Override
                    public void onObjectChanged(Post obj) {
                        collabText.setText(obj.getTitle());
                        profileManager.getProfileSingleValue(obj.getAuthorId(), createProfileChangeListener(collabAutherImageView));
                    }

                    @Override
                    public void onError(String errorText) {

                    }
                });

    }

    public void bindDataWithText(Post post)
    {

        likeController = new LikeController(context, post, likeCounterTextView, likesImageView, true);
        bookMarkController=new BookMarkController(context,post,bookMarkTextView,bookMarkImageView,true);

        //String title = removeNewLinesDividers(post.getTitle());
        String title= post.getTitle();
        titleTextView.setText(title);
        String description = removeNewLinesDividers(post.getDescription());
        detailsTextView.setText(description);
        likeCounterTextView.setText(String.valueOf(post.getLikesCount()));
        bookMarkTextView.setText(String.valueOf(post.getBookmarkcount()));
        commentsCountTextView.setText(String.valueOf(post.getCommentsCount()));
        watcherCounterTextView.setText(String.valueOf(post.getWatchersCount()));
        CharSequence date = FormatterUtil.getRelativeTimeSpanStringShort(context, post.getCreatedDate());
        dateTextView.setText(date);
            RelativeLayout.LayoutParams lparm= (RelativeLayout.LayoutParams) authorImageView.getLayoutParams();
            //RelativeLayout.LayoutParams lparm = new RelativeLayout.LayoutParams(authorImageView.getLayoutParams());
            lparm.topMargin=0;
            authorImageView.setLayoutParams(lparm);
            ViewGroup.MarginLayoutParams detailsTextViewLayoutParams=(ViewGroup.MarginLayoutParams) detailsTextView.getLayoutParams();
            //     RelativeLayout.LayoutParams buttonLayoutParams = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            detailsTextViewLayoutParams.topMargin=30;
            detailsTextView.setLayoutParams(detailsTextViewLayoutParams);

//            postImageView.setVisibility(View.GONE);
         //   postImageView.setEnabled(false);
            if (post.getAuthorId() != null) {

            profileManager.getProfileSingleValue(post.getAuthorId(), createProfileChangeListener(authorImageView));
        }

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            postManager.hasCurrentUserLikeSingleValue(post.getId(), firebaseUser.getUid(), createOnLikeObjectExistListener());
            postManager.hasCurrentUserBookMarkSingleValue(post.getId(), firebaseUser.getUid(), createOnBookMarkObjectExistListener());
        }
    }

    public void bindData(Post post) {


        likeController = new LikeController(context, post, likeCounterTextView, likesImageView, true);
        bookMarkController=new BookMarkController(context,post,bookMarkTextView,bookMarkImageView,true);
        //String title = removeNewLinesDividers(post.getTitle());
        String title= post.getTitle();
        titleTextView.setText(title);
        String description = removeNewLinesDividers(post.getDescription());
        detailsTextView.setText(description);
        likeCounterTextView.setText(String.valueOf(post.getLikesCount()));
        bookMarkTextView.setText(String.valueOf(post.getBookmarkcount()));
        commentsCountTextView.setText(String.valueOf(post.getCommentsCount()));
        watcherCounterTextView.setText(String.valueOf(post.getWatchersCount()));

        CharSequence date = FormatterUtil.getRelativeTimeSpanStringShort(context, post.getCreatedDate());
        dateTextView.setText(date);

        if(!post.getImageTitle().equals("default"))
        {
            postImageView.setVisibility(View.VISIBLE);
            postImageView.setEnabled(true);
            postManager.loadImageMediumSize(GlideApp.with(baseActivity), post.getImageTitle(), postImageView);
        }
        else {
         RelativeLayout.LayoutParams lparm= (RelativeLayout.LayoutParams) authorImageView.getLayoutParams();
           //RelativeLayout.LayoutParams lparm = new RelativeLayout.LayoutParams(authorImageView.getLayoutParams());
            lparm.topMargin=0;
            authorImageView.setLayoutParams(lparm);
            ViewGroup.MarginLayoutParams detailsTextViewLayoutParams=(ViewGroup.MarginLayoutParams) detailsTextView.getLayoutParams();
            //     RelativeLayout.LayoutParams buttonLayoutParams = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            detailsTextViewLayoutParams.topMargin=30;
            detailsTextView.setLayoutParams(detailsTextViewLayoutParams);
            postImageView.setVisibility(View.GONE);
            postImageView.setEnabled(false);

        }
        if (post.getAuthorId() != null) {

            profileManager.getProfileSingleValue(post.getAuthorId(), createProfileChangeListener(authorImageView));
        }

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            postManager.hasCurrentUserLikeSingleValue(post.getId(), firebaseUser.getUid(), createOnLikeObjectExistListener());
            postManager.hasCurrentUserBookMarkSingleValue(post.getId(), firebaseUser.getUid(), createOnBookMarkObjectExistListener());
        }
    }

    private String removeNewLinesDividers(String text) {
        int decoratedTextLength = text.length() < Constants.Post.MAX_TEXT_LENGTH_IN_LIST ?
                text.length() : Constants.Post.MAX_TEXT_LENGTH_IN_LIST;
        return text.substring(0, decoratedTextLength).replaceAll("\n", " ").trim();
    }

    private OnObjectChangedListener<Profile> createProfileChangeListener(final ImageView authorImageView) {
        return new OnObjectChangedListenerSimple<Profile>() {
            @Override
            public void onObjectChanged(Profile obj) {
                if (obj != null && obj.getPhotoUrl() != null) {
                    if (!baseActivity.isFinishing() && !baseActivity.isDestroyed()) {
                        ImageUtil.loadImage(GlideApp.with(baseActivity), obj.getPhotoUrl(), authorImageView);
                    }
                }
            }
        };
    }

    private OnObjectExistListener<Like> createOnLikeObjectExistListener() {
        return exist -> likeController.initLike(exist);
    }

    private OnObjectExistListener<Like> createOnBookMarkObjectExistListener() {
        return exist -> bookMarkController.initBookMark(exist);
    }

    public interface OnClickListener {
        void onItemClick(int position, View view);

        void onLikeClick(LikeController likeController, int position);

        void onAuthorClick(int position, View view);

        void onCopyClick(int position , View view);

        void  onShareClick(int position , ImageView view  , View mainView);

        void onCollabClick(int position , View view);

        void onCollabContainerClick(int position , View view);


        void onBookMarkClick(BookMarkController bookMarkController, int position);

        void onCollabAutherClick(int adapterPosition, View v);
    }
}