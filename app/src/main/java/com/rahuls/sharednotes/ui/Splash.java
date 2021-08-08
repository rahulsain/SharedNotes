package com.rahuls.sharednotes.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.rahuls.sharednotes.BuildConfig;
import com.rahuls.sharednotes.R;
import com.rahuls.sharednotes.note.MainActivity;

public class Splash extends AppCompatActivity {

    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        fAuth = FirebaseAuth.getInstance();

        TextView versionNumber = findViewById(R.id.versionNumber);
        versionNumber.setText(BuildConfig.VERSION_NAME);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Handler handler = new Handler();
        handler.postDelayed(() -> {

            //check if the user is logged in

            if(fAuth.getCurrentUser() != null){
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                //new anonymous account
                fAuth.signInAnonymously().addOnSuccessListener(authResult -> {
                    Toast.makeText(this, "Logged In with Temp Account", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this,MainActivity.class));
                    finish();
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "Error! " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
            }


        }, 100);
    }
}