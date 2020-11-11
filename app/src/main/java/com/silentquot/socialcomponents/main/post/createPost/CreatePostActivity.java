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

package com.silentquot.socialcomponents.main.post.createPost;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;

import com.silentquot.R;
import com.silentquot.socialcomponents.main.post.BaseCreatePostActivity;

import java.io.ByteArrayOutputStream;

public class CreatePostActivity extends BaseCreatePostActivity<CreatePostView, CreatePostPresenter> implements CreatePostView {
    public static final int CREATE_NEW_POST_REQUEST = 11;


    @NonNull
    @Override
    public CreatePostPresenter createPresenter() {
        if (presenter == null) {
            return new CreatePostPresenter(this);
        }
        return presenter;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.create_post_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.post:
                setimageUri();
                presenter.doSavePost(imageUri);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setimageUri() {
        Bitmap bitmap =getBitmapFromView(imageView);
        imageUri=getImageUri(getApplicationContext() , bitmap);

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


}
