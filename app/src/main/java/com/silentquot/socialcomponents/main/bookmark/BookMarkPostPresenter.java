package com.silentquot.socialcomponents.main.bookmark;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.silentquot.R;
import com.silentquot.socialcomponents.main.base.BasePresenter;
import com.silentquot.socialcomponents.managers.PostManager;
import com.silentquot.socialcomponents.managers.listeners.OnPostChangedListener;
import com.silentquot.socialcomponents.model.Post;

public class BookMarkPostPresenter  extends BasePresenter<BookMarkPostView> {

    private PostManager postManager;


    public BookMarkPostPresenter(Context context) {
        super(context);
        postManager = PostManager.getInstance(context);
    }


    void onCollabContainerClick(String postId , View postView){


        postManager.getSinglePostValue(postId, new OnPostChangedListener(){
            @Override
            public void onObjectChanged(Post obj) {

                postManager.isPostExistSingleValue(obj.getCollabPostId(), exist -> ifViewAttached(view -> {
                    if (exist) {

                        view.openPostDetailsActivity(obj.getCollabPostId(), postView);
                    } else {
                        view.showSnackBar(R.string.error_post_was_removed);
                    }
                }));
            }

            @Override
            public void onError(String errorText) {

            }
        });


    }

    void onPostClicked(final String postId, final View postView) {
        postManager.isPostExistSingleValue(postId, exist -> ifViewAttached(view -> {
            if (exist) {
                view.openPostDetailsActivity(postId, postView);
            } else {
                view.showSnackBar(R.string.error_post_was_removed);
            }
        }));
    }

    public void loadBookMarkPost() {
        if (checkInternetConnection()) {
            if (getCurrentUserId() != null) {
                ifViewAttached(BookMarkPostView::showLocalProgress);
                postManager.getBookMarkPost(getCurrentUserId(), list -> ifViewAttached(view -> {
                    view.hideLocalProgress();
                    view.onBookMarkPostsLoaded(list);
                    view.showEmptyListMessage(list.isEmpty());
                }));
            } else {
                ifViewAttached(view -> {
                    view.showEmptyListMessage(true);
                    view.hideLocalProgress();
                });
            }
        } else {
            ifViewAttached(BookMarkPostView::hideLocalProgress);
        }
    }
    void onCreatePostClickAction(View anchorView) {
        if (checkInternetConnection(anchorView)) {
            if (checkAuthorization()) {
                ifViewAttached(BookMarkPostView::openCreatePostActivity);
            }
        }
    }

    public void onRefresh() {
        loadBookMarkPost();
    }


    public  void  onCollabClick(String postid , View view)
    {
        postManager.CollabPost(postid , view.getContext());
//        postManager.getSinglePostValue(postid, new OnPostChangedListener(){
//
//            @Override
//            public void onObjectChanged(Post obj) {
//
//                postManager.CollabPost(obj,view.getContext());
//            }
//
//            @Override
//            public void onError(String errorText) {
//
//            }
//        });
    }
    public void onShareClick(String postid, ImageView view , View mainView)
    {
        postManager.getSinglePostValue(postid, new OnPostChangedListener(){

            @Override
            public void onObjectChanged(Post obj) {

                postManager.sharePost(obj , view, context);
            }

            @Override
            public void onError(String errorText) {

            }
        });

    }

    public  void onCopyClick(String postid )
    {
        postManager.getSinglePostValue(postid, new OnPostChangedListener(){

            @Override
            public void onObjectChanged(Post obj) {
                postManager.copyTextFromPost(obj);
            }

            @Override
            public void onError(String errorText) {

            }
        });

    }


    public void onAuthorClick(String postId, View authorView) {
        postManager.getSinglePostValue(postId, new OnPostChangedListener() {
            @Override
            public void onObjectChanged(Post obj) {
                ifViewAttached(view -> view.openProfileActivity(obj.getAuthorId(), authorView));
            }

            @Override
            public void onError(String errorText) {
                ifViewAttached(view -> view.showSnackBar(errorText));
            }
        });
    }

    public void onCollabAutherClick(String postId, View collabAutherview) {
        postManager.getSinglePostValue(postId, new OnPostChangedListener(){

            @Override
            public void onObjectChanged(Post obj) {

                postManager.getSinglePostValue(obj.getCollabPostId(), new OnPostChangedListener() {
                    @Override
                    public void onObjectChanged(Post obj) {
                        ifViewAttached(view -> view.openProfileActivity(obj.getAuthorId(), collabAutherview));
                    }

                    @Override
                    public void onError(String errorText) {

                    }
                });
            }

            @Override
            public void onError(String errorText) {

            }
        });
    }
}
