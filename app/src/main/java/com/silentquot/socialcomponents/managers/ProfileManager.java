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

package com.silentquot.socialcomponents.managers;

import android.content.Context;
import android.net.Uri;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ValueEventListener;
import com.silentquot.Model.Brodcast;
import com.silentquot.socialcomponents.enums.ProfileStatus;
import com.silentquot.socialcomponents.main.interactors.ProfileInteractor;
import com.silentquot.socialcomponents.managers.listeners.OnDataChangedListener;
import com.silentquot.socialcomponents.managers.listeners.OnObjectChangedListener;
import com.silentquot.socialcomponents.managers.listeners.OnObjectExistListener;
import com.silentquot.socialcomponents.managers.listeners.OnProfileCreatedListener;
import com.silentquot.socialcomponents.model.Connection;
import com.silentquot.socialcomponents.model.Profile;
import com.silentquot.socialcomponents.utils.PreferencesUtil;



public class ProfileManager extends FirebaseListenersManager {

    private static final String TAG = ProfileManager.class.getSimpleName();
    private static ProfileManager instance;

    private Context context;
    private ProfileInteractor profileInteractor;


    public static ProfileManager getInstance(Context context) {
        if (instance == null) {
            instance = new ProfileManager(context);
        }

        return instance;
    }

    private ProfileManager(Context context) {
        this.context = context;
        profileInteractor = ProfileInteractor.getInstance(context);
    }

    public Profile buildProfile(FirebaseUser firebaseUser, String largeAvatarURL) {
        Profile profile = new Profile(firebaseUser.getUid());
        profile.setEmail(firebaseUser.getEmail());
        profile.setUsername(firebaseUser.getDisplayName());
        if (largeAvatarURL!=null) {
            profile.setPhotoUrl(largeAvatarURL);
        }
        else {
            profile.setPhotoUrl("default");
            // != null ? largeAvatarURL : firebaseUser.getPhotoUrl().toString()
        }
        return profile;
    }

    public void isProfileExist(String id, final OnObjectExistListener<Profile> onObjectExistListener) {
        profileInteractor.isProfileExist(id, onObjectExistListener);
    }

    public void createOrUpdateProfile(Profile profile, OnProfileCreatedListener onProfileCreatedListener) {
        createOrUpdateProfile(profile, null, onProfileCreatedListener);
    }

    public void createOrUpdateProfile(final Profile profile, Uri imageUri, final OnProfileCreatedListener onProfileCreatedListener) {
        if (imageUri == null) {
            if (profile.getPhotoUrl()!=null)
            {
                profile.setPhotoUrl(profile.getPhotoUrl());
            }
            else {
                profile.setPhotoUrl("default");
            }

            profileInteractor.createOrUpdateProfile(profile, onProfileCreatedListener);
        } else {
            profileInteractor.createOrUpdateProfileWithImage(profile, imageUri, onProfileCreatedListener);
        }
    }

    public void getProfileValue(Context activityContext, String id, final OnObjectChangedListener<Profile> listener) {
        ValueEventListener valueEventListener = profileInteractor.getProfile(id, listener);
        addListenerToMap(activityContext, valueEventListener);
    }

    public void getBrodCastSingleValue(String id, final OnObjectChangedListener<Brodcast> listener) {
        profileInteractor.getBrodCastSingleValue(id, listener);
    }

    public void getProfileSingleValue(String id, final OnObjectChangedListener<Profile> listener) {
        profileInteractor.getProfileSingleValue(id, listener);
    }

    public ProfileStatus checkProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            return ProfileStatus.NOT_AUTHORIZED;
        } else if (!PreferencesUtil.isProfileCreated(context)) {
            return ProfileStatus.NO_PROFILE;
        } else {
            return ProfileStatus.PROFILE_CREATED;
        }
    }


    public void searchRequestConnection(String searchText, OnDataChangedListener<Connection> onDataChangedListener) {
       // closeListeners(context);
        ValueEventListener valueEventListener = profileInteractor.searchRequestConnection(searchText, onDataChangedListener);
        addListenerToMap(context, valueEventListener);
    }

    public void searchConnection(String searchText, OnDataChangedListener<Connection> onDataChangedListener) {
        closeListeners(context);
        ValueEventListener valueEventListener = profileInteractor.searchConnections(searchText, onDataChangedListener);
        addListenerToMap(context, valueEventListener);
    }


    public void search(String searchText, OnDataChangedListener<Profile> onDataChangedListener) {
        closeListeners(context);
        ValueEventListener valueEventListener = profileInteractor.searchProfiles(searchText, onDataChangedListener);
        addListenerToMap(context, valueEventListener);
    }

    public void addRegistrationToken(String token, String userId) {
        profileInteractor.addRegistrationToken(token, userId);
    }
}
