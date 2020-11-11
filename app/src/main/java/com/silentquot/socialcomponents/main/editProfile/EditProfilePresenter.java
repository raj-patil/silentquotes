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

package com.silentquot.socialcomponents.main.editProfile;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.silentquot.R;
import com.silentquot.socialcomponents.main.base.BaseView;
import com.silentquot.socialcomponents.main.pickImageBase.PickImagePresenter;
import com.silentquot.socialcomponents.managers.ProfileManager;
import com.silentquot.socialcomponents.managers.listeners.OnObjectChangedListenerSimple;
import com.silentquot.socialcomponents.model.Profile;
import com.silentquot.socialcomponents.utils.PreferencesUtil;
import com.silentquot.socialcomponents.utils.ValidationUtil;

/**
 * Created by Alexey on 03.05.18.
 */

public class EditProfilePresenter<V extends EditProfileView> extends PickImagePresenter<V> {

    protected Profile profile;
    protected ProfileManager profileManager;

    protected EditProfilePresenter(Context context) {
        super(context);
        profileManager = ProfileManager.getInstance(context.getApplicationContext());

    }

    public void loadProfile() {
        ifViewAttached(BaseView::showProgress);
        profileManager.getProfileSingleValue(getCurrentUserId(), new OnObjectChangedListenerSimple<Profile>() {
            @Override
            public void onObjectChanged(Profile obj) {
                profile = obj;
                ifViewAttached(view -> {
                    if (profile != null) {
                        view.setName(profile.getUsername());

                        if (profile.getPhotoUrl() != null) {
                            view.setProfilePhoto(profile.getPhotoUrl());
                        }
                    }


                    view.hideProgress();
                    view.setNameError(null);
                });
            }
        });
    }

    public void attemptCreateProfile(Uri  imageUri) {
        if (checkInternetConnection()) {
            ifViewAttached(view -> {
                view.setNameError(null);

                String name = view.getNameText().trim();
                boolean cancel = false;

                if (TextUtils.isEmpty(name)) {
                    view.setNameError(context.getString(R.string.error_field_required));
                    cancel = true;
                } else if (!ValidationUtil.isNameValid(name)) {
                    view.setNameError(context.getString(R.string.error_profile_name_length));
                    cancel = true;
                }

                if (!cancel) {


                    if (profile==null)
                    {
                        //view.startCreateProfileActivity();
                        view.showProgress();
                        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                        profile = new Profile(firebaseUser.getUid());
                        profile.setEmail(firebaseUser.getEmail());
                        profile.setId(firebaseUser.getUid());
                        profile.setUsername(name);
                        createOrUpdateProfile(imageUri);
                    }
                    else {
                        view.showProgress();
                        profile.setUsername(name);

                        createOrUpdateProfile(imageUri);
                    }
                }
            });
        }
    }

    private void createOrUpdateProfile(Uri imageUri) {
        profileManager.createOrUpdateProfile(profile, imageUri, success -> {
            ifViewAttached(view -> {
                view.hideProgress();
                if (success) {
                    onProfileUpdatedSuccessfully();
                } else {
                    view.showSnackBar(R.string.error_fail_create_profile);
                }
            });
        });
    }

    protected void onProfileUpdatedSuccessfully() {
        PreferencesUtil.setProfileCreated(context, true);
        profileManager.addRegistrationToken(FirebaseInstanceId.getInstance().getToken(), profile.getId());
       // ifViewAttached(BaseView::finish);
        ifViewAttached(view->{
            view.callmainactivity();
                });
    }

}
