/*
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
 */

package com.silentquot.socialcomponents.main.postDetails;

import android.view.View;

import com.silentquot.socialcomponents.main.base.BaseView;
import com.silentquot.socialcomponents.model.Comment;
import com.silentquot.socialcomponents.model.Post;

import java.util.List;

/**
 * Created by Alexey on 03.05.18.
 */

public interface PostDetailsView extends BaseView  {

    void onPostRemoved();

    void openImageDetailScreen(String imagePath);

    void openProfileActivity(String authorId, View authorView);

    void setTitle(String title);

    void setDescription(String description);

    void loadPostDetailImage(String imagePath);

    void loadAuthorPhoto(String photoUrl);

    void setAuthorName(String username);

    void initLikeController(Post post);
    void initBookMarkController(Post post);

    void updateCounters(Post post);

    void initLikeButtonState(boolean exist);
    void  initBookMarkButtonState(boolean exist);

    void showComplainMenuAction(boolean show);

    void showEditMenuAction(boolean show);

    void showDeleteMenuAction(boolean show);

    String getCommentText();

    void clearCommentField();

    void scrollToFirstComment();

    void openEditPostActivity(Post post);

    void showCommentProgress(boolean show);

    void showCommentsWarning(boolean show);

    void showCommentsRecyclerView(boolean show);

    void onCommentsListChanged(List<Comment> list);

    void showCommentsLabel(boolean show);

    void openPostDetailsActivity(String collabPostId, View postView);

    void loadCollabText(String collabPostId);

    void loadTextPost(String authorId);
}
