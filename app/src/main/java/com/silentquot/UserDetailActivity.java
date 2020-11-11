package com.silentquot;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class UserDetailActivity extends AppCompatActivity {

    FirebaseAuth auth;
    DatabaseReference reference;
EditText userName;
Button Next;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);
        userName =findViewById(R.id.user_detail_Name);
        Next=findViewById(R.id.user_detail_btn_Next);
        auth = FirebaseAuth.getInstance();
        Next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String struserName =userName.getText().toString();

                FirebaseUser firebaseUser = auth.getCurrentUser();
                assert firebaseUser != null;
                String userid = firebaseUser.getUid();

                reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("id", userid);
                hashMap.put("username", struserName);
                hashMap.put("imageURL", "default");
                hashMap.put("status", "offline");
                hashMap.put("search", struserName.toLowerCase());

                reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Intent intent = new Intent(UserDetailActivity.this, HomePageActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                    }
                });

            }
        });

    }
}
