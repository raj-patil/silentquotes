/*
 * Copyright (c) 2020. Created By Raj Patil
 */

package com.silentquot.socialcomponents.main.editProfile.createProfile;

import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.silentquot.socialcomponents.main.editProfile.EditProfilePresenter;


class CreateProfilePresenter extends EditProfilePresenter<CreateProfileView> {

    CreateProfilePresenter(Context context) {
        super(context);
    }

    public void buildProfile(String largeAvatarURL) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        profile = profileManager.buildProfile(firebaseUser, largeAvatarURL);

        ifViewAttached(view -> {
            view.setName(profile.getUsername());

            if (profile.getPhotoUrl() != null) {
                view.setProfilePhoto(profile.getPhotoUrl());
            } else {
                view.hideLocalProgress();
                view.setDefaultProfilePhoto();
            }
        });
    }

    @Override
    protected void onProfileUpdatedSuccessfully() {
        super.onProfileUpdatedSuccessfully();
//        PreferencesUtil.setProfileCreated(context, true);
//        profileManager.addRegistrationToken(FirebaseInstanceId.getInstance().getToken(), profile.getId());
    }
}
