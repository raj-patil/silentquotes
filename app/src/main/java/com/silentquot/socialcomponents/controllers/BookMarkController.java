package com.silentquot.socialcomponents.controllers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.silentquot.R;
import com.silentquot.socialcomponents.main.base.BaseActivity;
import com.silentquot.socialcomponents.main.interactors.PostInteractor;
import com.silentquot.socialcomponents.main.main.MainActivity;
import com.silentquot.socialcomponents.managers.PostManager;
import com.silentquot.socialcomponents.managers.listeners.OnObjectExistListener;
import com.silentquot.socialcomponents.managers.listeners.OnPostChangedListener;
import com.silentquot.socialcomponents.model.Post;

public class BookMarkController {



    private static final int ANIMATION_DURATION = 300;

    public enum AnimationType {
        COLOR_ANIM, BOUNCE_ANIM
    }

    private Context context;
    private String postId;
    private String postAuthorId;

    private LikeController.AnimationType bookMarkAnimationType = LikeController.AnimationType.BOUNCE_ANIM;

    private TextView likeCounterTextView;
    private ImageView bookmarkImageView;

    private boolean isListView = false;

    private boolean isLiked = false;
    private boolean updatingBookmarkCounter = true;

    public BookMarkController(Context context, Post post, TextView likeCounterTextView,
                          ImageView bookmarkImageView, boolean isListView) {
        this.context = context;
        this.postId = post.getId();
        this.postAuthorId = post.getAuthorId();
        this.likeCounterTextView = likeCounterTextView;
        this.bookmarkImageView = bookmarkImageView;
        this.isListView = isListView;
    }

    public void bookmarkClickAction(long prevValue) {
        if (!updatingBookmarkCounter) {
            startAnimationBookmarkButton(bookMarkAnimationType);

            if (!isLiked) {
                addBookMark(prevValue);
            } else {
                RemoveBookMark(prevValue);
            }
        }
    }

    public void bookmarkClickActionLocal(Post post) {
        setUpdatingLikeCounter(false);
        bookmarkClickAction(post.getBookmarkcount());
      updateLocalPostLikeCounter(post);
    }

    private void addBookMark(long prevValue) {
        updatingBookmarkCounter = true;
        isLiked = true;
        likeCounterTextView.setText(String.valueOf(prevValue + 1));
        PostInteractor.getInstance(context).createOrUpdateBookMark(postId, postAuthorId);
    }

    private void RemoveBookMark(long prevValue) {
        updatingBookmarkCounter = true;
        isLiked = false;
        likeCounterTextView.setText(String.valueOf(prevValue - 1));
        PostInteractor.getInstance(context).removeBookMark(postId);
    }

    private void startAnimationBookmarkButton(LikeController.AnimationType animationType) {
        switch (animationType) {
            case BOUNCE_ANIM:
                bounceAnimateImageView();
                break;
            case COLOR_ANIM:
                colorAnimateImageView();
                break;
        }
    }

    private void bounceAnimateImageView() {
        AnimatorSet animatorSet = new AnimatorSet();

        ObjectAnimator bounceAnimX = ObjectAnimator.ofFloat(bookmarkImageView, "scaleX", 0.2f, 1f);
        bounceAnimX.setDuration(ANIMATION_DURATION);
        bounceAnimX.setInterpolator(new BounceInterpolator());

        ObjectAnimator bounceAnimY = ObjectAnimator.ofFloat(bookmarkImageView, "scaleY", 0.2f, 1f);
        bounceAnimY.setDuration(ANIMATION_DURATION);
        bounceAnimY.setInterpolator(new BounceInterpolator());
        bounceAnimY.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                bookmarkImageView.setImageResource(!isLiked ? R.drawable.ic_bookmark
                        : R.drawable.ic_bookmark_border);
            }
        });

        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
            }
        });

        animatorSet.play(bounceAnimX).with(bounceAnimY);
        animatorSet.start();
    }

    private void colorAnimateImageView() {
        final int activatedColor = context.getResources().getColor(R.color.like_icon_activated);

        final ValueAnimator colorAnim = !isLiked ? ObjectAnimator.ofFloat(0f, 1f)
                : ObjectAnimator.ofFloat(1f, 0f);
        colorAnim.setDuration(ANIMATION_DURATION);
        colorAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float mul = (Float) animation.getAnimatedValue();
                int alpha = adjustAlpha(activatedColor, mul);
                bookmarkImageView.setColorFilter(alpha, PorterDuff.Mode.SRC_ATOP);
                if (mul == 0.0) {
                    bookmarkImageView.setColorFilter(null);
                }
            }
        });

        colorAnim.start();
    }

    private int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    public LikeController.AnimationType getLikeAnimationType() {
        return bookMarkAnimationType;
    }

    public void setLikeAnimationType(LikeController.AnimationType bookMarkAnimationType) {
        this.bookMarkAnimationType = bookMarkAnimationType;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    public boolean isUpdatingLikeCounter() {
        return updatingBookmarkCounter;
    }

    public void setUpdatingLikeCounter(boolean updatingBookmarkCounter) {
        this.updatingBookmarkCounter = updatingBookmarkCounter;
    }

    public void initBookMark(boolean isLiked) {
        bookmarkImageView.setImageResource(isLiked ? R.drawable.ic_bookmark : R.drawable.ic_bookmark_border);
        this.isLiked = isLiked;
    }

    private void updateLocalPostLikeCounter(Post post) {
        if (isLiked) {
            post.setBookmarkcount(post.getLikesCount() + 1);
        } else {
            post.setBookmarkcount(post.getLikesCount() - 1);
        }
    }

    public void handleBookmarkAction(final BaseActivity baseActivity, final Post post) {
        PostManager.getInstance(baseActivity.getApplicationContext()).isPostExistSingleValue(post.getId(), new OnObjectExistListener<Post>() {
            @Override
            public void onDataChanged(boolean exist) {
                if (exist) {
                    if (baseActivity.hasInternetConnection()) {
                        doHandleLikeClickAction(baseActivity, post);
                    } else {
                        showWarningMessage(baseActivity, R.string.internet_connection_failed);
                    }
                } else {
                    showWarningMessage(baseActivity, R.string.message_post_was_removed);
                }
            }
        });
    }

    public void handleBookmarkAction(final BaseActivity baseActivity, final String postId) {
        PostManager.getInstance(baseActivity.getApplicationContext()).getSinglePostValue(postId, new OnPostChangedListener() {
            @Override
            public void onObjectChanged(Post post) {
                if (baseActivity.hasInternetConnection()) {
                    doHandleLikeClickAction(baseActivity, post);
                } else {
                    showWarningMessage(baseActivity, R.string.internet_connection_failed);
                }
            }

            @Override
            public void onError(String errorText) {
                baseActivity.showSnackBar(errorText);
            }
        });
    }

    private void showWarningMessage(BaseActivity baseActivity, int messageId) {
        if (baseActivity instanceof MainActivity) {
            ((MainActivity) baseActivity).showFloatButtonRelatedSnackBar(messageId);
        } else {
            baseActivity.showSnackBar(messageId);
        }
    }

    private void doHandleLikeClickAction(BaseActivity baseActivity, Post post) {
        if (baseActivity.checkAuthorization()) {
            if (isListView) {
                bookmarkClickActionLocal(post);
            } else {
                bookmarkClickAction(post.getBookmarkcount());
            }
        }
    }

    public void changeAnimationType() {
        if (getLikeAnimationType() == LikeController.AnimationType.BOUNCE_ANIM) {
            setLikeAnimationType(LikeController.AnimationType.COLOR_ANIM);
        } else {
            setLikeAnimationType(LikeController.AnimationType.BOUNCE_ANIM);
        }

        Snackbar snackbar = Snackbar
                .make(bookmarkImageView, "Animation was changed", Snackbar.LENGTH_LONG);

        snackbar.show();
    }




}
