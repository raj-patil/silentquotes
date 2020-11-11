package com.silentquot;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.silentquot.socialcomponents.main.login.VerificationPhoneActivity;

public class MainActivity extends AppCompatActivity {

    Button clickme , socialcomponents;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        clickme=findViewById(R.id.click_me);
        clickme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent verificationPhonIntent = new Intent(getApplicationContext(), VerificationPhoneActivity.class);
                startActivity(verificationPhonIntent);
            }
        });

//        socialcomponents=findViewById(R.id.click_me_2);
//        socialcomponents.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent socialcompintent = new Intent(getApplicationContext(), com.silentquot.socialcomponents.main.main.MainActivity.class);
//                startActivity(socialcompintent);
//            }
//        });

    }
}
