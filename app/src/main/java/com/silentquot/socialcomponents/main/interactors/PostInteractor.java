/*
 *
 * Copyright 2018 Rozdoum
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.silentquot.socialcomponents.main.interactors;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.silentquot.ApplicationHelper;
import com.silentquot.Constants;
import com.silentquot.R;
import com.silentquot.socialcomponents.dialogs.NewPostDialog;
import com.silentquot.socialcomponents.managers.DatabaseHelper;
import com.silentquot.socialcomponents.managers.listeners.OnDataChangedListener;
import com.silentquot.socialcomponents.managers.listeners.OnObjectExistListener;
import com.silentquot.socialcomponents.managers.listeners.OnPostChangedListener;
import com.silentquot.socialcomponents.managers.listeners.OnPostCreatedListener;
import com.silentquot.socialcomponents.managers.listeners.OnPostListChangedListener;
import com.silentquot.socialcomponents.managers.listeners.OnPostTypeChangedListner;
import com.silentquot.socialcomponents.managers.listeners.OnTaskCompleteListener;
import com.silentquot.socialcomponents.model.BookMark;
import com.silentquot.socialcomponents.model.Like;
import com.silentquot.socialcomponents.model.Post;
import com.silentquot.socialcomponents.model.PostListResult;
import com.silentquot.socialcomponents.utils.ImageUtil;
import com.silentquot.socialcomponents.utils.LogUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PostInteractor   extends Activity {

    private static final String TAG = PostInteractor.class.getSimpleName();
    private static PostInteractor instance;

    private DatabaseHelper databaseHelper;
    private Context context;

    public static PostInteractor getInstance(Context context) {
        if (instance == null) {
            instance = new PostInteractor(context);
        }

        return instance;
    }

    private PostInteractor(Context context) {
        this.context = context;
        databaseHelper = ApplicationHelper.getDatabaseHelper();
    }

    public String generatePostId() {
        return databaseHelper
                .getDatabaseReference()
                .child(DatabaseHelper.POSTS_DB_KEY)
                .push()
                .getKey();
    }

    public void createOrUpdatePost(Post post) {
        try {
            Map<String, Object> postValues = post.toMap();
            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put("/" + DatabaseHelper.POSTS_DB_KEY + "/" + post.getId(), postValues);

            databaseHelper.getDatabaseReference().updateChildren(childUpdates);

            childUpdates.put("/"+DatabaseHelper.FOLLOWINGS_POSTS_DB_KEY+"/"+FirebaseAuth.getInstance().getCurrentUser().getUid()+"/"+post.getId()+"/"+"postId" , post.getId());
            childUpdates.put("/"+DatabaseHelper.FOLLOWINGS_POSTS_DB_KEY+"/"+FirebaseAuth.getInstance().getCurrentUser().getUid()+"/"+post.getId()+"/"+"postType" , post.getPostType());

            databaseHelper.getDatabaseReference().updateChildren(childUpdates);

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }
    public void removePostFromAuthersFollowingId(String postId)
    {
        DatabaseReference postRef = databaseHelper.getDatabaseReference().child(DatabaseHelper.FOLLOWINGS_POSTS_DB_KEY).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(postId);
        postRef.removeValue();
    }

public void removeFromBookMark(String postId)
{
    Query postRef = databaseHelper.getDatabaseReference().child(DatabaseHelper.POST_BOOKMARK_DB_KEY).
            orderByKey().orderByChild("id").equalTo(postId);
            postRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    snapshot.getRef().removeValue();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
}

    public Task<Void> removePost(Post post) {
        DatabaseReference postRef = databaseHelper.getDatabaseReference().child(DatabaseHelper.POSTS_DB_KEY).child(post.getId());
        return postRef.removeValue();
    }

    public void incrementWatchersCount(String postId) {
        DatabaseReference postRef = databaseHelper.getDatabaseReference().child(DatabaseHelper.POSTS_DB_KEY + "/" + postId + "/watchersCount");
        postRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Integer currentValue = mutableData.getValue(Integer.class);
                if (currentValue == null) {
                    mutableData.setValue(1);
                } else {
                    mutableData.setValue(currentValue + 1);
                }

                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                LogUtil.logInfo(TAG, "Updating Watchers count transaction is completed.");
            }
        });
    }

    public void getPostList(final OnPostListChangedListener<Post> onDataChangedListener, long date) {
        DatabaseReference databaseReference = databaseHelper.getDatabaseReference().child(DatabaseHelper.POSTS_DB_KEY);
        Query postsQuery;
        if (date == 0) {
            postsQuery = databaseReference.limitToLast(Constants.Post.POST_AMOUNT_ON_PAGE).orderByChild("createdDate");
        } else {
            postsQuery = databaseReference.limitToLast(Constants.Post.POST_AMOUNT_ON_PAGE).endAt(date).orderByChild("createdDate");
        }

        postsQuery.keepSynced(true);
        postsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> objectMap = (Map<String, Object>) dataSnapshot.getValue();
                PostListResult result = parsePostList(objectMap);

                if (result.getPosts().isEmpty() && result.isMoreDataAvailable()) {
                    getPostList(onDataChangedListener, result.getLastItemCreatedDate() - 1);
                } else {
                    onDataChangedListener.onListChanged(parsePostList(objectMap));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "getPostList(), onCancelled", new Exception(databaseError.getMessage()));
                onDataChangedListener.onCanceled(context.getString(R.string.permission_denied_error));
            }
        });
    }

    public void getPostListByUser(final OnDataChangedListener<Post> onDataChangedListener, String userId) {
        DatabaseReference databaseReference = databaseHelper.getDatabaseReference().child(DatabaseHelper.POSTS_DB_KEY);
        Query postsQuery;
        postsQuery = databaseReference.orderByChild("authorId").equalTo(userId);

        postsQuery.keepSynced(true);
        postsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                PostListResult result = parsePostList((Map<String, Object>) dataSnapshot.getValue());
                onDataChangedListener.onListChanged(result.getPosts());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "getPostListByUser(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });
    }

    public  ValueEventListener getPostType(final String id , final OnPostTypeChangedListner listener)
    {
        DatabaseReference databaseReference = databaseHelper.getDatabaseReference().child(DatabaseHelper.POSTS_DB_KEY).child(id);
        ValueEventListener valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    if (isPostValid((Map<String, Object>) dataSnapshot.getValue())) {
                        Post post = dataSnapshot.getValue(Post.class);
                        if (post != null) {
                            post.setId(id);
                        }
                        listener.onObjectChanged(post.getPostType());
                    } else {
                        listener.onError(String.format(context.getString(R.string.error_general_post), id));
                    }
                } else {
                    listener.onObjectChanged(null);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "getPost(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });

        databaseHelper.addActiveListener(valueEventListener, databaseReference);
        return valueEventListener;
    }

    public ValueEventListener getPost(final String id, final OnPostChangedListener listener) {
        DatabaseReference databaseReference = databaseHelper.getDatabaseReference().child(DatabaseHelper.POSTS_DB_KEY).child(id);
        ValueEventListener valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    if (isPostValid((Map<String, Object>) dataSnapshot.getValue())) {
                        Post post = dataSnapshot.getValue(Post.class);
                        if (post != null) {
                            post.setId(id);
                        }
                        listener.onObjectChanged(post);
                    } else {
                        listener.onError(String.format(context.getString(R.string.error_general_post), id));
                    }
                } else {
                    listener.onObjectChanged(null);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "getPost(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });

        databaseHelper.addActiveListener(valueEventListener, databaseReference);
        return valueEventListener;
    }

    public void getSinglePost(final String id, final OnPostChangedListener listener) {
        DatabaseReference databaseReference = databaseHelper.getDatabaseReference().child(DatabaseHelper.POSTS_DB_KEY).child(id);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null && dataSnapshot.exists()) {
                    if (isPostValid((Map<String, Object>) dataSnapshot.getValue())) {
                        Post post = dataSnapshot.getValue(Post.class);
                        post.setId(id);
                        listener.onObjectChanged(post);
                    } else {
                        listener.onError(String.format(context.getString(R.string.error_general_post), id));
                    }
                } else {
                    listener.onError(context.getString(R.string.message_post_was_removed));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "getSinglePost(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });
    }

    private PostListResult parsePostList(Map<String, Object> objectMap) {
        PostListResult result = new PostListResult();
        List<Post> list = new ArrayList<Post>();
        boolean isMoreDataAvailable = true;
        long lastItemCreatedDate = 0;

        if (objectMap != null) {
            isMoreDataAvailable = Constants.Post.POST_AMOUNT_ON_PAGE == objectMap.size();

            for (String key : objectMap.keySet()) {
                Object obj = objectMap.get(key);
                if (obj instanceof Map) {
                    Map<String, Object> mapObj = (Map<String, Object>) obj;

                    if (!isPostValid(mapObj)) {
                        LogUtil.logDebug(TAG, "Invalid post, id: " + key);
                        continue;
                    }

                    boolean hasComplain = mapObj.containsKey("hasComplain") && (boolean) mapObj.get("hasComplain");
                    long createdDate = (long) mapObj.get("createdDate");

                    if (lastItemCreatedDate == 0 || lastItemCreatedDate > createdDate) {
                        lastItemCreatedDate = createdDate;
                    }

                    if (!hasComplain) {
                        Post post = new Post();
                        post.setId(key);
                        post.setTitle((String) mapObj.get("title"));
                        post.setDescription((String) mapObj.get("description"));
                        post.setImageTitle((String) mapObj.get("imageTitle"));
                        post.setAuthorId((String) mapObj.get("authorId"));
                        post.setCreatedDate(createdDate);
                        post.setPostType((String)mapObj.get("postType"));

                        post.setCollabPostId((String) mapObj.get("collabPostId"));

                        if (mapObj.containsKey("commentsCount")) {
                            post.setCommentsCount((long) mapObj.get("commentsCount"));
                        }
                        if (mapObj.containsKey("bookmarkcount")) {
                            post.setBookmarkcount((long) mapObj.get("bookmarkcount"));
                        }
                        if (mapObj.containsKey("likesCount")) {
                            post.setLikesCount((long) mapObj.get("likesCount"));
                        }
                        if (mapObj.containsKey("watchersCount")) {
                            post.setWatchersCount((long) mapObj.get("watchersCount"));
                        }
                        list.add(post);
                    }
                }
            }

            Collections.sort(list, (lhs, rhs) -> ((Long) rhs.getCreatedDate()).compareTo(lhs.getCreatedDate()));

            result.setPosts(list);
            result.setLastItemCreatedDate(lastItemCreatedDate);
            result.setMoreDataAvailable(isMoreDataAvailable);
        }

        return result;
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
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public  void SharePost(Post post , ImageView view , Context context)
    {
        if(!post.getImageTitle().equals("default")) {
            Bitmap bitmap = getBitmapFromView(view);
            File file = new File(context.getExternalCacheDir(), "SilentQuot.png");
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
                intent.putExtra(Intent.EXTRA_TEXT, post.getTitle() + '\n' + post.getDescription() + "\n Get More Quotes on SilentQuot - Download Now !");
                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                intent.setType("image/png");
                context.startActivity(Intent.createChooser(intent, "Share a Quot"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
        {
            String s = post.getTitle() + "\n "+ post.getDescription();
           final Intent sharingIntent = new Intent(Intent.ACTION_SEND);

            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "SilentQuot");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, s);
            sharingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(Intent.createChooser(sharingIntent, "Share a Quote"));
        }
    }

    public void CopyTextFromPost(Post post )
    {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", post.getTitle());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(context, "Copy To The ClipBoard", Toast.LENGTH_SHORT).show();
    }

    private boolean isPostValid(Map<String, Object> post) {
        return post.containsKey("title")
                && post.containsKey("description")
                && post.containsKey("imageTitle")
                && post.containsKey("authorId")
                && post.containsKey("description");
    }

    public void addComplainToPost(Post post) {
        databaseHelper.getDatabaseReference().child(DatabaseHelper.POSTS_DB_KEY).child(post.getId()).child("hasComplain").setValue(true);
    }

    public void isPostExistSingleValue(String postId, final OnObjectExistListener<Post> onObjectExistListener) {
        DatabaseReference databaseReference = databaseHelper.getDatabaseReference().child(DatabaseHelper.POSTS_DB_KEY).child(postId);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                onObjectExistListener.onDataChanged(dataSnapshot.exists());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "isPostExistSingleValue(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });
    }

    public void subscribeToNewPosts() {
        FirebaseMessaging.getInstance().subscribeToTopic("postsTopic");
    }

    public void removePost(final Post post, final OnTaskCompleteListener onTaskCompleteListener) {
        final DatabaseHelper databaseHelper = ApplicationHelper.getDatabaseHelper();
        if (!post.getImageTitle().equals("default")) {
            Task<Void> removeImageTask = databaseHelper.removeImage(post.getImageTitle());
            removeImageTask.addOnSuccessListener(aVoid -> {
                removePost(post).addOnCompleteListener(task -> {
                    onTaskCompleteListener.onTaskComplete(task.isSuccessful());
                    ProfileInteractor.getInstance(context).updateProfileLikeCountAfterRemovingPost(post);
                    removeObjectsRelatedToPost(post.getId());
                    removePostFromAuthersFollowingId(post.getId());
                    LogUtil.logDebug(TAG, "removePost(), is success: " + task.isSuccessful());
                });
                LogUtil.logDebug(TAG, "removeImage(): success");
            }).addOnFailureListener(exception -> {
                LogUtil.logError(TAG, "removeImage()", exception);
                onTaskCompleteListener.onTaskComplete(false);
            });
        }
        else {
            removePost(post).addOnCompleteListener(task -> {
                onTaskCompleteListener.onTaskComplete(task.isSuccessful());
                ProfileInteractor.getInstance(context).updateProfileLikeCountAfterRemovingPost(post);
                removeObjectsRelatedToPost(post.getId());
                removePostFromAuthersFollowingId(post.getId());
                LogUtil.logDebug(TAG, "removePost(), is success: " + task.isSuccessful());
            });
        }
    }

    private void removeObjectsRelatedToPost(final String postId) {
        CommentInteractor.getInstance(context).removeCommentsByPost(postId).addOnSuccessListener(aVoid -> LogUtil.logDebug(TAG, "Comments related to post with id: " + postId + " was removed")).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                LogUtil.logError(TAG, "Failed to remove comments related to post with id: " + postId, e);
            }
        });

        removeLikesByPost(postId).addOnSuccessListener(aVoid -> LogUtil.logDebug(TAG, "Likes related to post with id: " + postId + " was removed")).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                LogUtil.logError(TAG, "Failed to remove likes related to post with id: " + postId, e);
            }
        });
    }

    public void createOrUpdatePostWithImage(Uri imageUri, final OnPostCreatedListener onPostCreatedListener, final Post post) {
        // Register observers to listen for when the download is done or if it fails

        DatabaseHelper databaseHelper = ApplicationHelper.getDatabaseHelper();
        if (post.getId() == null) {
            post.setId(generatePostId());
        }
        if (imageUri != null) {
            final String imageTitle = ImageUtil.generatePostImageTitle(post.getId());
            UploadTask uploadTask = databaseHelper.uploadImage(imageUri, imageTitle);

            if (uploadTask != null) {
                uploadTask.addOnFailureListener(exception -> {
                    // Handle unsuccessful uploads
                    onPostCreatedListener.onPostSaved(false);

                }).addOnSuccessListener(taskSnapshot -> {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.

                    post.setImageTitle(imageTitle);
                    createOrUpdatePost(post);

                    onPostCreatedListener.onPostSaved(true);
                });
            }
        }
        else {
            post.setImageTitle("default");
            createOrUpdatePost(post);
            onPostCreatedListener.onPostSaved(true);
        }
    }

    public void createOrUpdateLike(final String postId, final String postAuthorId) {
        try {
            String authorId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference mLikesReference = databaseHelper
                    .getDatabaseReference()
                    .child(DatabaseHelper.POST_LIKES_DB_KEY)
                    .child(postId)
                    .child(authorId);
            mLikesReference.push();
            String id = mLikesReference.push().getKey();
            Like like = new Like(authorId);
            like.setId(id);

            mLikesReference.child(id).setValue(like, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError == null) {
                        DatabaseReference postRef = databaseHelper
                                .getDatabaseReference()
                                .child(DatabaseHelper.POSTS_DB_KEY + "/" + postId + "/likesCount");

                        incrementLikesCount(postRef);
                        DatabaseReference profileRef = databaseHelper
                                .getDatabaseReference()
                                .child(DatabaseHelper.PROFILES_DB_KEY + "/" + postAuthorId + "/likesCount");

                        incrementLikesCount(profileRef);
                    } else {
                        LogUtil.logError(TAG, databaseError.getMessage(), databaseError.toException());
                    }
                }

                private void incrementLikesCount(DatabaseReference postRef) {
                    postRef.runTransaction(new Transaction.Handler() {
                        @Override
                        public Transaction.Result doTransaction(MutableData mutableData) {
                            Integer currentValue = mutableData.getValue(Integer.class);
                            if (currentValue == null) {
                                mutableData.setValue(1);
                            } else {
                                mutableData.setValue(currentValue + 1);
                            }

                            return Transaction.success(mutableData);
                        }

                        @Override
                        public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                            LogUtil.logInfo(TAG, "Updating likes count transaction is completed.");
                        }
                    });
                }

            });
        } catch (Exception e) {
            LogUtil.logError(TAG, "createOrUpdateLike()", e);
        }

    }

    public void createOrUpdateBookMark(final String postId, final String postAuthorId ) {
        try {
            String authorId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference mLikesReference = databaseHelper
                    .getDatabaseReference()
                    .child(DatabaseHelper.POST_BOOKMARK_DB_KEY)
                    .child(authorId)
                    .child(postId);
            mLikesReference.push();
          //  String id = mLikesReference.push().getKey();

            getSinglePost(postId, new OnPostChangedListener() {
                @Override
                public void onObjectChanged(Post obj) {
                    BookMark like = new BookMark(authorId , obj.getPostType());
                    like.setId(postId);

                    mLikesReference.setValue(like, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                DatabaseReference postRef = databaseHelper
                                        .getDatabaseReference()
                                        .child(DatabaseHelper.POSTS_DB_KEY + "/" + postId + "/bookmarkcount");

                                incrementBookMarkCount(postRef);

                            } else {
                                LogUtil.logError(TAG, databaseError.getMessage(), databaseError.toException());
                            }
                        }

                        private void incrementBookMarkCount(DatabaseReference postRef) {
                            postRef.runTransaction(new Transaction.Handler() {
                                @Override
                                public Transaction.Result doTransaction(MutableData mutableData) {
                                    Integer currentValue = mutableData.getValue(Integer.class);
                                    if (currentValue == null) {
                                        mutableData.setValue(1);
                                    } else {
                                        mutableData.setValue(currentValue + 1);
                                    }

                                    return Transaction.success(mutableData);
                                }

                                @Override
                                public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                                    LogUtil.logInfo(TAG, "Updating BookMark count transaction is completed.");
                                }
                            });
                        }

                    });
                }

                @Override
                public void onError(String errorText) {

                }
            });

        } catch (Exception e) {
            LogUtil.logError(TAG, "createOrUpdateBookmark()", e);
        }

    }
    public void removeBookMark(final String postId) {
        String authorId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference mLikesReference = databaseHelper
                .getDatabaseReference()
                .child(DatabaseHelper.POST_BOOKMARK_DB_KEY)
                .child(authorId)
                .child(postId);
        mLikesReference.removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    DatabaseReference postRef = databaseHelper
                            .getDatabaseReference()
                            .child(DatabaseHelper.POSTS_DB_KEY + "/" + postId + "/bookmarkcount");
                    decrementBookmarkCount(postRef);

                } else {
                    LogUtil.logError(TAG, databaseError.getMessage(), databaseError.toException());
                }
            }

            private void decrementBookmarkCount(DatabaseReference postRef) {
                postRef.runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        Long currentValue = mutableData.getValue(Long.class);
                        if (currentValue == null) {
                            mutableData.setValue(0);
                        } else {
                            mutableData.setValue(currentValue - 1);
                        }

                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                        LogUtil.logInfo(TAG, "Updating BookMark count transaction is completed.");
                    }
                });
            }
        });
    }





    public void removeLike(final String postId, final String postAuthorId) {
        String authorId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference mLikesReference = databaseHelper
                .getDatabaseReference()
                .child(DatabaseHelper.POST_LIKES_DB_KEY)
                .child(postId)
                .child(authorId);
        mLikesReference.removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    DatabaseReference postRef = databaseHelper
                            .getDatabaseReference()
                            .child(DatabaseHelper.POSTS_DB_KEY + "/" + postId + "/likesCount");
                    decrementLikesCount(postRef);

                    DatabaseReference profileRef = databaseHelper
                            .getDatabaseReference()
                            .child(DatabaseHelper.PROFILES_DB_KEY + "/" + postAuthorId + "/likesCount");
                    decrementLikesCount(profileRef);
                } else {
                    LogUtil.logError(TAG, databaseError.getMessage(), databaseError.toException());
                }
            }

            private void decrementLikesCount(DatabaseReference postRef) {
                postRef.runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        Long currentValue = mutableData.getValue(Long.class);
                        if (currentValue == null) {
                            mutableData.setValue(0);
                        } else {
                            mutableData.setValue(currentValue - 1);
                        }

                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                        LogUtil.logInfo(TAG, "Updating likes count transaction is completed.");
                    }
                });
            }
        });
    }

    public ValueEventListener hasCurrentUserLike(String postId, String userId, final OnObjectExistListener<Like> onObjectExistListener) {
        DatabaseReference databaseReference = databaseHelper
                .getDatabaseReference()
                .child(DatabaseHelper.POST_LIKES_DB_KEY)
                .child(postId)
                .child(userId);
        ValueEventListener valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                onObjectExistListener.onDataChanged(dataSnapshot.exists());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "hasCurrentUserLike(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });

        databaseHelper.addActiveListener(valueEventListener, databaseReference);
        return valueEventListener;
    }

    public ValueEventListener hasCurrentUserBookMark(String postId, String userId, final OnObjectExistListener<Like> onObjectExistListener) {
        DatabaseReference databaseReference = databaseHelper
                .getDatabaseReference()
                .child(DatabaseHelper.POST_BOOKMARK_DB_KEY)
                .child(userId)
                .child(postId);
        ValueEventListener valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                onObjectExistListener.onDataChanged(dataSnapshot.exists());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "hasCurrentUserLike(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });

        databaseHelper.addActiveListener(valueEventListener, databaseReference);
        return valueEventListener;
    }


    public void hasCurrentUserLikeSingleValue(String postId, String userId, final OnObjectExistListener<Like> onObjectExistListener) {
        DatabaseReference databaseReference = databaseHelper
                .getDatabaseReference()
                .child(DatabaseHelper.POST_LIKES_DB_KEY)
                .child(postId)
                .child(userId);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                onObjectExistListener.onDataChanged(dataSnapshot.exists());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "hasCurrentUserLikeSingleValue(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });
    }

    public Task<Void> removeLikesByPost(String postId) {
        return databaseHelper
                .getDatabaseReference()
                .child(DatabaseHelper.POST_LIKES_DB_KEY)
                .child(postId)
                .removeValue();
    }

    public ValueEventListener searchPostsByTitle(String searchText, OnDataChangedListener<Post> onDataChangedListener) {
        DatabaseReference reference = databaseHelper.getDatabaseReference().child(DatabaseHelper.POSTS_DB_KEY);
        ValueEventListener valueEventListener = getSearchQuery(reference,"title", searchText).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                PostListResult result = parsePostList((Map<String, Object>) dataSnapshot.getValue());
                onDataChangedListener.onListChanged(result.getPosts());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "searchPostsByTitle(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });

        databaseHelper.addActiveListener(valueEventListener, reference);

        return valueEventListener;
    }

    public ValueEventListener filterPostsByLikes(int  limit, OnDataChangedListener<Post> onDataChangedListener) {
        DatabaseReference reference = databaseHelper.getDatabaseReference().child(DatabaseHelper.POSTS_DB_KEY);
        ValueEventListener valueEventListener = getFilteredQuery(reference,"likesCount", limit).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                PostListResult result = parsePostList((Map<String, Object>) dataSnapshot.getValue());
                onDataChangedListener.onListChanged(result.getPosts());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "filterPostsByLikes(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });

        databaseHelper.addActiveListener(valueEventListener, reference);

        return valueEventListener;
    }

    private Query getSearchQuery(DatabaseReference databaseReference, String childOrderBy, String searchText) {
        return databaseReference
                .orderByChild(childOrderBy)
                .startAt(searchText)
                .endAt(searchText + "\uf8ff");
    }

    private Query getFilteredQuery(DatabaseReference databaseReference, String childOrderBy, int limit) {
        return databaseReference
                .orderByChild(childOrderBy)
                .limitToLast(limit);
    }

    public StorageReference getMediumImageStorageRef(String imageTitle) {
        return databaseHelper.getMediumImageStorageRef(imageTitle);
    }

    public StorageReference getOriginImageStorageRef(String imageTitle) {
        return databaseHelper.getOriginImageStorageRef(imageTitle);
    }

    public StorageReference getSmallImageStorageRef(String imageTitle) {
        return databaseHelper.getSmallImageStorageRef(imageTitle);
    }


    public void collabPost(String postId , Context context) {
        NewPostDialog collabpost = new NewPostDialog(context);
        collabpost.showCollabDialog(FirebaseAuth.getInstance().getUid() , context , postId);

    }

    public void hasCurrentUserBookMarkSingleValue(String postId, String userId, OnObjectExistListener<Like> onObjectExistListener) {

        DatabaseReference databaseReference = databaseHelper
                .getDatabaseReference()
                .child(DatabaseHelper.POST_BOOKMARK_DB_KEY)
                .child(userId)
                .child(postId);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                onObjectExistListener.onDataChanged(dataSnapshot.exists());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogUtil.logError(TAG, "hasCurrentUserLikeSingleValue(), onCancelled", new Exception(databaseError.getMessage()));
            }
        });
    }
}
