package com.rahuls.sharednotes.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rahuls.sharednotes.R;
import com.rahuls.sharednotes.model.User;
import com.rahuls.sharednotes.ui.MainActivity;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class Register extends AppCompatActivity {

    public static final String TAG = "TAG";
    EditText rUserName,rUserEmail,rUserPassword,rUserConfirmPassword;
    Button syncAccount;
    TextView loginActivity;
    ProgressBar progressBar;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    private String userID;
    User userM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Connect to SharedNotes");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        rUserName = findViewById(R.id.userName);
        rUserEmail = findViewById(R.id.userEmail);
        rUserPassword = findViewById(R.id.password);
        rUserConfirmPassword = findViewById(R.id.passwordConfirm);

        syncAccount = findViewById(R.id.createAccount);
        loginActivity = findViewById(R.id.login);
        progressBar = findViewById(R.id.progressBar4);



        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userID = Objects.requireNonNull(fAuth.getCurrentUser()).getUid();

        loginActivity.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(),Login.class)));


        String gUserName = rUserName.getText().toString();
        String gUserEmail = rUserEmail.getText().toString();

        userM = new User(gUserName,gUserEmail,userID);

        syncAccount.setOnClickListener(v -> {

            String gUserPassword = rUserPassword.getText().toString();
            String gUserConfirmPassword = rUserConfirmPassword.getText().toString();

            if(gUserName.isEmpty() || gUserEmail.isEmpty() || gUserPassword.isEmpty() || gUserConfirmPassword.isEmpty()) {
                Toast.makeText(Register.this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            if(!isValid(gUserEmail)){
                rUserEmail.setError("Email is not valid");
            }

            if(!gUserPassword.equals(gUserConfirmPassword)){
                rUserConfirmPassword.setError("Password do not match.");
            }

            progressBar.setVisibility(View.VISIBLE);

            AuthCredential credential = EmailAuthProvider.getCredential(gUserEmail,gUserPassword);
            fAuth.getCurrentUser().linkWithCredential(credential).addOnSuccessListener(authResult -> {
                Toast.makeText(Register.this, "Notes are Synced.", Toast.LENGTH_SHORT).show();

                DocumentReference documentReference = fStore.collection("users").document(userID);
                Map<String,Object> user = new HashMap<>();
                user.put("UserName",userM.getName());
                user.put("UserEmail",userM.getUserEmail());
                user.put("UserId",userM.getUserId());

                documentReference.set(user).addOnSuccessListener(aVoid -> Log.d(TAG, "onSuccess: user profile is created for "+ userID)).addOnFailureListener(e -> Log.d(TAG, "onFailure: user profile is not created for "+ userID));

                FirebaseUser firebaseUser = fAuth.getCurrentUser();
                UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                        .setDisplayName(gUserName).build();

                Objects.requireNonNull(firebaseUser).updateProfile(request);

                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                overridePendingTransition(R.anim.slide_up,R.anim.slide_down);
                finish();

            }).addOnFailureListener(e -> {
                Toast.makeText(Register.this, "Failed to connect. Try Again.", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            });

        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        startActivity(new Intent(this, MainActivity.class));
        finish();
        return super.onOptionsItemSelected(item);
    }

    private boolean isValid(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." +
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if (email == null)
            return false;
        return pat.matcher(email).matches();
    }
}