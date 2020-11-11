/*
 * Copyright (c) 2020. Created By Raj Patil
 */

package com.silentquot.socialcomponents.main.login;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.silentquot.R;
import com.silentquot.socialcomponents.main.base.BaseActivity;
import com.silentquot.socialcomponents.main.editProfile.EditProfileActivity;
import com.silentquot.socialcomponents.main.editProfile.createProfile.CreateProfileActivity;
import com.silentquot.socialcomponents.utils.GoogleApiHelper;
import com.silentquot.socialcomponents.utils.LogUtil;
import com.silentquot.socialcomponents.utils.LogoutHelper;

import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class VerificationCodeActivity extends BaseActivity<LoginView, LoginPresenter> implements LoginView, GoogleApiClient.OnConnectionFailedListener  {

    private static final String TAG = LoginActivity.class.getSimpleName();
    private static final int SIGN_IN_GOOGLE = 9001;
    public static final int LOGIN_REQUEST_CODE = 10001;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private GoogleApiClient mGoogleApiClient;

    private CallbackManager mCallbackManager;
    private String profilePhotoUrlLarge;


    EditText counttryCode,phoneNumber,otpCode_1,otpCode_2,otpCode_3,otpCode_4,otpCode_5,otpCode_6;
    Button verifyBtn;
    String strPhoneNumber,strCountryCode , strotpCode_1,strotpCode_2,strotpCode_3,strotpCode_4,strotpCode_5,strotpCode_6;
    String verificationCodeBySystem , code;

    private CountDownTimer countDownTimer;
    private TextView tv_coundown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification_code);

        counttryCode=findViewById(R.id.verification_code_country_code);
        phoneNumber=findViewById(R.id.verification_code_phone_number);
        otpCode_1=findViewById(R.id.verification_code_OTP_1);
        otpCode_2=findViewById(R.id.verification_code_OTP_2);
        otpCode_3=findViewById(R.id.verification_code_OTP_3);
        otpCode_4=findViewById(R.id.verification_code_OTP_4);
        otpCode_5=findViewById(R.id.verification_code_OTP_5);
        otpCode_6=findViewById(R.id.verification_code_OTP_6);
        verifyBtn=findViewById(R.id.verification_code_btn_verify);
        tv_coundown = (TextView) findViewById(R.id.tv_countdown);
        countDownTimer();
        otpCodeTextChange();

        //strPhoneNumber= getIntent().getStringExtra("phoneNo");
        strPhoneNumber = getIntent().getExtras().getString("phoneNo");
        strCountryCode= getIntent().getStringExtra("countryCode");
        Toast.makeText(VerificationCodeActivity.this,strPhoneNumber, Toast.LENGTH_SHORT).show();
        phoneNumber.setText(strPhoneNumber);
        counttryCode.setText(strCountryCode);

        sendVerificationCodeToUser(strPhoneNumber); //OTP SENDING AND VERIFICATION


        //Manual Verification OF OTP
        verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                strotpCode_1=otpCode_1.getText().toString().trim();
                strotpCode_2=otpCode_2.getText().toString().trim();
                strotpCode_3=otpCode_3.getText().toString().trim();
                strotpCode_4=otpCode_4.getText().toString().trim();
                strotpCode_5=otpCode_5.getText().toString().trim();
                strotpCode_6=otpCode_6.getText().toString().trim();

                code=strotpCode_1+strotpCode_2+strotpCode_3+strotpCode_4+strotpCode_5+strotpCode_6;
                if (code.isEmpty() || code.length() < 6) {
                    otpCode_6.setError("Wrong OTP...");
                    otpCode_1.requestFocus();
                    return;
                }
             //   progressBar.setVisibility(View.VISIBLE);
                verifyCode(code);
            }
        });

        FacebookSdk.sdkInitialize(getApplicationContext());
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        initGoogleSignIn();
        initFirebaseAuth();
   //     initFacebookSignIn();
    }


    //sendVerificationn_code_to_user
    private void sendVerificationCodeToUser(String phoneNo) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91" + phoneNo,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                TaskExecutors.MAIN_THREAD,   // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                @Override
                public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

                    String code = phoneAuthCredential.getSmsCode();
                    if (code != null) {
                       // progressBar.setVisibility(View.VISIBLE);
                        verifyCode(code);
                    }
                }

                @Override
                public void onVerificationFailed(FirebaseException e) {
                    Toast.makeText(VerificationCodeActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                    super.onCodeSent(s, forceResendingToken);
                    verificationCodeBySystem = s;
                }
            };
    private void verifyCode(String codeByUser) {

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCodeBySystem, codeByUser);
        signInTheUserByCredentials(credential);

    }


    private void countDownTimer() {
        countDownTimer = new CountDownTimer(1000 * 60 * 2, 1000) {
            @Override
            public void onTick(long l) {
                String text = String.format(Locale.getDefault(), "%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(l) % 60,
                        TimeUnit.MILLISECONDS.toSeconds(l) % 60);
                tv_coundown.setText(text);
            }

            @Override
            public void onFinish() {
                tv_coundown.setText("00:00");

                findViewById(R.id.googleSignInButton).setVisibility(View.VISIBLE);
                findViewById(R.id.googleSignInButton).setEnabled(true);
                findViewById(R.id.facebookSignInButton).setEnabled(true);
                findViewById(R.id.facebookSignInButton).setVisibility(View.VISIBLE);
                findViewById(R.id.otp_issue_message).setVisibility(View.VISIBLE);
                findViewById(R.id.otp_issue_message).setEnabled(true);
                findViewById(R.id.resend_otp_btn).setEnabled(true);
                findViewById(R.id.resend_otp_btn).setVisibility(View.VISIBLE);

            }
        };
        countDownTimer.start();
    }
    private void signInTheUserByCredentials(PhoneAuthCredential credential) {

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(VerificationCodeActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(VerificationCodeActivity.this, "Your Account has been created successfully!", Toast.LENGTH_SHORT).show();
                            //code for notification - Call NotifiMEthod-------------------
                         //   notificationManager.notify(1, builder.build());

                            //Adddata in session manager

                            //session.createLoginSession(strfirstname,strmiddlename,strlastname,strcontact,stremail,strrationcardnumber,stradharcardnumber,strpancardnumber);
                            finish();
                            //Perform Your required action here to either let the user sign In or do something required
                            Intent intent = new Intent(getApplicationContext(), EditProfileActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);

                        } else {
                            Toast.makeText(VerificationCodeActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }



    public  void otpCodeTextChange()
    {
        otpCode_1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                otpCode_2.requestFocus();
                otpCode_2.setCursorVisible(true);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        otpCode_2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                otpCode_3.requestFocus();
                otpCode_3.setCursorVisible(true);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        otpCode_3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                otpCode_4.requestFocus();
                otpCode_4.setCursorVisible(true);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        otpCode_4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                otpCode_5.requestFocus();
                otpCode_5.setCursorVisible(true);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        otpCode_5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                otpCode_6.requestFocus();
                otpCode_6.setCursorVisible(true);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


    }

    protected void initGoogleSignIn() {
        mGoogleApiClient = GoogleApiHelper.createGoogleApiClient(this);
        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.googleSignInButton).setOnClickListener(view -> presenter.onGoogleSignInClick());
    }

    private void initFirebaseAuth() {
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            LogoutHelper.signOut(mGoogleApiClient, this);
        }

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

    private void initFacebookSignIn() {
        mCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                LogUtil.logDebug(TAG, "facebook:onSuccess:" + loginResult);
                presenter.handleFacebookSignInResult(loginResult);
            }

            @Override
            public void onCancel() {
                LogUtil.logDebug(TAG, "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                LogUtil.logError(TAG, "facebook:onError", error);
                showSnackBar(error.getMessage());
            }
        });

        findViewById(R.id.facebookSignInButton).setOnClickListener(v -> presenter.onFacebookSignInClick());
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
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





}
