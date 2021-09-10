package com.rahuls.sharednotes.auth;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.rahuls.sharednotes.R;
import com.rahuls.sharednotes.model.User;
import com.rahuls.sharednotes.note.MainActivity;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class Register extends AppCompatActivity {

    public static final String TAG = "Register";
    EditText rUserName,rUserEmail,rUserPassword,rUserConfirmPassword;
    Button syncAccount,signUpButton;
    TextView loginActivity;
    ProgressBar progressBar;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    private String userID;
    private SignInClient oneTapClient;
    private BeginSignInRequest signUpRequest;
    private static final int REQ_ONE_TAP = 1007;  // Can be any integer unique to the Activity.
    private boolean showOneTapUI = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Connect to Shared Notes");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        FirebaseUser user = fAuth.getCurrentUser();
        userID = user.getUid();

        oneTapClient = Identity.getSignInClient(this);
        signUpRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        // Your server's client ID, not your Android client ID.
                        .setServerClientId(getString(R.string.default_web_client_id))
                        // Show all accounts on the device.
                        .setFilterByAuthorizedAccounts(false)
                        .build())
                .build();

        rUserName = findViewById(R.id.userName);
        rUserEmail = findViewById(R.id.userEmail);
        rUserPassword = findViewById(R.id.password);
        rUserConfirmPassword = findViewById(R.id.passwordConfirm);

        syncAccount = findViewById(R.id.createAccount1);
        loginActivity = findViewById(R.id.login);
        progressBar = findViewById(R.id.progressBar4);
        signUpButton = findViewById(R.id.createAccount2);

        loginActivity.setOnClickListener(v -> startNewActivity(v.getContext(),Login.class));

        signUpButton.setOnClickListener(v -> oneTapClient.beginSignIn(signUpRequest)
                .addOnSuccessListener(Register.this, result -> {
                    try {
                        startIntentSenderForResult(
                                result.getPendingIntent().getIntentSender(), REQ_ONE_TAP,
                                null, 0, 0, 0);
                    } catch (IntentSender.SendIntentException e) {
                        Log.e(TAG, "Couldn't start One Tap UI: " + e.getLocalizedMessage());
                    }
                })
                .addOnFailureListener(Register.this, e -> {
                    // No Google Accounts found. Just continue presenting the signed-out UI.
                    Log.d(TAG, e.getLocalizedMessage());
                }));

        syncAccount.setOnClickListener(v -> {
            String gUserPassword = rUserPassword.getText().toString();
            String gUserConfirmPassword = rUserConfirmPassword.getText().toString();
            String gUserName = rUserName.getText().toString();
            String gUserEmail = rUserEmail.getText().toString();

            User userM = new User(gUserName,gUserEmail,userID);

            if(gUserName.isEmpty() || gUserEmail.isEmpty() || gUserPassword.isEmpty() || gUserConfirmPassword.isEmpty()) {
                Toast.makeText(Register.this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            if(!isValid(gUserEmail)){
                rUserEmail.setError("Email is not valid");
            }

            if(gUserPassword.length()<6){
                rUserPassword.setError("Password should be at least 6 character long");
            }

            if(!gUserPassword.equals(gUserConfirmPassword)){
                rUserConfirmPassword.setError("Password do not match.");
            }

            progressBar.setVisibility(View.VISIBLE);

            AuthCredential credential = EmailAuthProvider.getCredential(gUserEmail,gUserPassword);
            user.linkWithCredential(credential).addOnSuccessListener(authResult -> {
                Toast.makeText(Register.this, "Notes are Synced.", Toast.LENGTH_SHORT).show();

                DocumentReference documentReference = fStore.collection("users").document(userID);
                Map<String,Object> userD = new HashMap<>();
                userD.put("UserName",userM.getUserName());
                userD.put("UserEmail",userM.getUserEmail());
                userD.put("UserId",userM.getUserId());
                userD.put("RegisterOn", FieldValue.serverTimestamp());

                documentReference.set(userD).addOnSuccessListener(aVoid -> Log.d(TAG, "onSuccess: user profile is created for "+ userID)).addOnFailureListener(e -> Log.d(TAG, "onFailure: user profile is not created for "+ userID));

                startNewActivity(this,MainActivity.class);
                finish();

            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to connect. Try Again.", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            });

        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        startNewActivity(this,MainActivity.class);
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

    private void startNewActivity(Context context, Class<?> actClass) {
        Intent intent = new Intent(context, actClass);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_ONE_TAP) {
            try {
                SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(data);
                String idToken = credential.getGoogleIdToken();
                if (idToken != null) {
                    // Got an ID token from Google. Use it to authenticate
                    // with your backend.
                    Log.d(TAG, "Got ID token.");
                    firebaseAuthWithGoogle(idToken);
                }
            } catch (ApiException e) {
                switch (e.getStatusCode()) {
                    case CommonStatusCodes.CANCELED:
                        Log.d(TAG, "One-tap dialog was closed.");
                        // Don't re-prompt the user.
                        showOneTapUI = false;
                        break;
                    case CommonStatusCodes.NETWORK_ERROR:
                        Log.d(TAG, "One-tap encountered a network error.");
                        // Try again or just ignore.
                        break;
                    default:
                        Log.d(TAG, "Couldn't get credential from result."
                                + e.getLocalizedMessage());
                        break;
                }
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        fAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser user = fAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        updateUI(null);
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            // Name, email address, and profile photo Url
            String name = user.getDisplayName();
            String email = user.getEmail();
            Uri photoUrl = user.getPhotoUrl();

            // Check if user's email is verified
            boolean emailVerified = user.isEmailVerified();

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getIdToken() instead.
            String userID = user.getUid();
            Toast.makeText(this, "Notes are Synced.", Toast.LENGTH_SHORT).show();

            DocumentReference documentReference = fStore.collection("users").document(userID);
            Map<String, Object> userD = new HashMap<>();
            userD.put("UserName", name);
            userD.put("UserEmail", email);
            userD.put("UserId", userID);
            userD.put("RegisterOn", FieldValue.serverTimestamp());

            documentReference.set(userD).addOnSuccessListener(aVoid -> Log.d(TAG, "onSuccess: user profile is created for " + userID)).addOnFailureListener(e -> Log.d(TAG, "onFailure: user profile is not created for " + userID));

            if (photoUrl != null) {
                FirebaseStorage.getInstance().getReference().child("users/" + user.getUid() + "/profile.jpg").putFile(photoUrl);
            }

            startNewActivity(this, MainActivity.class);
            finish();

        }
    }
}