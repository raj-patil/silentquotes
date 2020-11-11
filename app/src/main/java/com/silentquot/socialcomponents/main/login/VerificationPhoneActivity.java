/*
 * Copyright (c) 2020. Created By Raj Patil
 */

package com.silentquot.socialcomponents.main.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.silentquot.HomePageActivity;
import com.silentquot.R;
import com.silentquot.socialcomponents.main.base.BaseActivity;
import com.silentquot.socialcomponents.main.editProfile.EditProfileActivity;
import com.silentquot.socialcomponents.main.editProfile.createProfile.CreateProfileActivity;
import com.silentquot.socialcomponents.utils.GoogleApiHelper;
import com.silentquot.socialcomponents.utils.LogUtil;

import java.util.Arrays;

public class VerificationPhoneActivity  extends BaseActivity<LoginView, LoginPresenter> implements LoginView, GoogleApiClient.OnConnectionFailedListener {


    FirebaseUser firebaseUser;
    private static final int SIGN_IN_GOOGLE = 9001;
    public static final int LOGIN_REQUEST_CODE = 10001;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private GoogleApiClient mGoogleApiClient;

    private CallbackManager mCallbackManager;
    private String profilePhotoUrlLarge;



//    @Override
//    public void onStart() {
//        super.onStart();
//
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        if (currentUser != null) {
//            Intent intent = new Intent(VerificationPhoneActivity.this, HomePageActivity.class);
//            startActivity(intent);
//            finish();
//        }
//
//    }
//

    Button verification_phone_btn_continue;
    EditText phoneNumber , countryCode;
    String strPhoneNumber=null , strCountryCode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_verification_phone);

        verification_phone_btn_continue = findViewById(R.id.verification_phone_btn_continue);
        phoneNumber=(EditText)findViewById(R.id.verification_phone_txt_phone_number);
        countryCode=findViewById(R.id.verification_phone_txt_country_code);

        verification_phone_btn_continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strPhoneNumber=phoneNumber.getText().toString().trim();
                strCountryCode=countryCode.getText().toString().trim();
                Toast.makeText(VerificationPhoneActivity.this,strPhoneNumber, Toast.LENGTH_SHORT).show();

                Intent verificationCodeIntent = new Intent(VerificationPhoneActivity.this, VerificationCodeActivity.class);
                verificationCodeIntent.putExtra("phoneNo",strPhoneNumber );
                verificationCodeIntent.putExtra("countryCode",strCountryCode );
                startActivity(verificationCodeIntent);
            }
        });

        initGoogleSignIn();
        FacebookSdk.sdkInitialize(getApplicationContext());
        initFirebaseAuth();

    }



    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(VerificationPhoneActivity.this, HomePageActivity.class);
            startActivity(intent);
            finish();
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.stopAutoManage(this);
            mGoogleApiClient.disconnect();
        }

    }

    @NonNull
    @Override
    public LoginPresenter createPresenter() {
        if (presenter == null) {
            return new LoginPresenter(this);
        }
        return presenter;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//
//        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == SIGN_IN_GOOGLE) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            presenter.handleGoogleSignInResult(result);
        }
    }

    @Override
    public void startCreateProfileActivity() {
        Intent intent = new Intent(this, CreateProfileActivity.class);
        intent.putExtra(CreateProfileActivity.LARGE_IMAGE_URL_EXTRA_KEY, profilePhotoUrlLarge);
        startActivity(intent);
    }

    @Override
    public void firebaseAuthWithCredentials(AuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    LogUtil.logDebug(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());
                    Intent intent = new Intent(this, EditProfileActivity.class);
                    startActivity(intent);
                    // If sign in fails, display a message to the user. If sign in succeeds
                    // the auth state listener will be notified and logic to handle the
                    // signed in user can be handled in the listener.
                    if (!task.isSuccessful()) {
                        presenter.handleAuthError(task);
                    }
                });
    }

    @Override
    public void setProfilePhotoUrl(String url) {
        profilePhotoUrlLarge = url;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        LogUtil.logDebug(TAG, "onConnectionFailed:" + connectionResult);
        showSnackBar(R.string.error_google_play_services);
        hideProgress();
    }

    @Override
    public void signInWithGoogle() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, SIGN_IN_GOOGLE);
    }

    @Override
    public void signInWithFacebook() {
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
    }



    protected void initGoogleSignIn() {
        mGoogleApiClient = GoogleApiHelper.createGoogleApiClient(this);
        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.googleSignInButton).setOnClickListener(view -> presenter.onGoogleSignInClick());
    }

    private void initFirebaseAuth() {
        mAuth = FirebaseAuth.getInstance();

//        if (mAuth.getCurrentUser() != null) {
//            LogoutHelper.signOut(mGoogleApiClient, this);
//        }

        mAuthListener = firebaseAuth -> {
            final FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                // Profile is signed in
                LogUtil.logDebug(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                presenter.checkIsProfileExist(user.getUid());
                setResult(RESULT_OK);
            } else {
                // Profile is signed out
                LogUtil.logDebug(TAG, "onAuthStateChanged:signed_out");
            }
        };
    }


}
