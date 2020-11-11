package com.silentquot.Fragments;


import androidx.annotation.NonNull;

import com.google.firebase.firestore.Exclude;

//Extender class
public class PostId {

    @Exclude
    public String PostId;

    public <T extends PostId> T withId(@NonNull final String id){
        this.PostId = id;
        return (T) this;
    }



}
