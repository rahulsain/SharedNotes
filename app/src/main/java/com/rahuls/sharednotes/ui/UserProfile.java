package com.rahuls.sharednotes.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.rahuls.sharednotes.R;
import com.rahuls.sharednotes.auth.Login;
import com.rahuls.sharednotes.auth.Logout;
import com.rahuls.sharednotes.auth.Register;
import com.rahuls.sharednotes.group.ListGroups;
import com.rahuls.sharednotes.note.AddNote;
import com.squareup.picasso.Picasso;

public class UserProfile extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle toggle;
    NavigationView nav_view;
    FirebaseFirestore fStore;
    FirebaseUser user;
    FirebaseAuth fAuth;
    TextView fullName, email, phone, verifyMsg;
    String userId;
    Button resendCode;
    Button resetPassLocal, changeProfileImage;
    ImageView profileImage;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        Toolbar toolbar = findViewById(R.id.toolbar_fragment1);
        setSupportActionBar(toolbar);

        phone = findViewById(R.id.profilePhone);
        fullName = findViewById(R.id.profileName);
        email = findViewById(R.id.profileEmail);
        resetPassLocal = findViewById(R.id.resetPasswordLocal);

        profileImage = findViewById(R.id.profileImage);
        changeProfileImage = findViewById(R.id.changeProfile);

        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        user = fAuth.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();
        userId = user.getUid();


        drawerLayout = findViewById(R.id.drawer4);
        nav_view = findViewById(R.id.nav_view4);
        nav_view.setNavigationItemSelectedListener(this);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(true);
        toggle.syncState();


        View headerView = nav_view.getHeaderView(0);
        TextView userName = headerView.findViewById(R.id.userDisplayName);
        TextView userEmail = headerView.findViewById(R.id.userDisplayEmail);

        userName.setText(getIntent().getStringExtra("userName"));
        userEmail.setText(getIntent().getStringExtra("userEmail"));

        if (user.isAnonymous()) {
            userEmail.setVisibility(View.GONE);
        }


        StorageReference profileRef = storageReference.child("users/" + userId + "/profile.jpg");
        profileRef.getDownloadUrl().addOnSuccessListener(uri -> Picasso.get().load(uri).into(profileImage));

        resendCode = findViewById(R.id.resendCode);
        verifyMsg = findViewById(R.id.verifyMsg);

        if (!user.isEmailVerified()) {
            verifyMsg.setVisibility(View.VISIBLE);
            resendCode.setVisibility(View.VISIBLE);

            resendCode.setOnClickListener(v -> user.sendEmailVerification().addOnSuccessListener(aVoid -> Toast.makeText(v.getContext(), "Verification Email Has been Sent.", Toast.LENGTH_SHORT).show()).addOnFailureListener(e -> Toast.makeText(v.getContext(), "Verification Email Has failed: " + e, Toast.LENGTH_SHORT).show()));
        }


        DocumentReference documentReference = fStore.collection("users").document(userId);
        documentReference.addSnapshotListener(this, (documentSnapshot, e) -> {
            if(e == null) {
                if (documentSnapshot.exists()) {
                    phone.setText(documentSnapshot.getString("UserPhone"));
                    fullName.setText(documentSnapshot.getString("UserName"));
                    email.setText(documentSnapshot.getString("UserEmail"));
                } else {
                    Log.d("tag", "onEvent: Document do not exists");
                }
            }
        });


        resetPassLocal.setOnClickListener(v -> {

            final EditText resetPassword = new EditText(v.getContext());
            resetPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

            final AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext());
            passwordResetDialog.setTitle("Reset Password");
            passwordResetDialog.setMessage("Enter New Password more than 6 Characters long.");
            passwordResetDialog.setView(resetPassword);

            passwordResetDialog.setPositiveButton("Yes", (dialog, which) -> {
                // extract the email and send reset link
                String newPassword = resetPassword.getText().toString();
                if(newPassword.isEmpty()){
                    Toast.makeText(v.getContext(),"Password should be atleast of 6 characters.",Toast.LENGTH_SHORT).show();
                    return;
                }
                user.updatePassword(newPassword).addOnSuccessListener(aVoid -> Toast.makeText(v.getContext(), "Password Reset Successfully.", Toast.LENGTH_SHORT).show()).addOnFailureListener(e -> Toast.makeText(v.getContext(), "Password Reset Failed. Try Resigning In", Toast.LENGTH_SHORT).show());
            });

            passwordResetDialog.setNegativeButton("No", (dialog, which) -> {
                // close
            });

            passwordResetDialog.create().show();

        });

        changeProfileImage.setOnClickListener(v -> {
            // open gallery
            Intent i = new Intent(v.getContext(), EditProfile.class);
            i.putExtra("fullName", fullName.getText().toString());
            i.putExtra("email", email.getText().toString());
            i.putExtra("phone", phone.getText().toString());
            i.putExtra("userName", getIntent().getStringExtra("userName"));
            i.putExtra("userEmail", getIntent().getStringExtra("userEmail"));
            startActivity(i);
        });

    }

    private void startNewActivity(Context context, Class<?> actClass) {
        Intent intent = new Intent(context, actClass);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);
        int itemId = item.getItemId();
        if (itemId == R.id.groups) {
            Intent intent = new Intent(this, ListGroups.class);
            intent.putExtra("userName", getIntent().getStringExtra("userName"));
            intent.putExtra("userEmail", getIntent().getStringExtra("userEmail"));
            startActivity(intent);
        } else if (itemId == R.id.addNote) {
            startNewActivity(this, AddNote.class);
        } else if (itemId == R.id.sync) {
            if (user.isAnonymous()) {
                startNewActivity(this, Login.class);
            } else {
                Toast.makeText(this, "You are Already Connected.", Toast.LENGTH_SHORT).show();
            }
        } else if (itemId == R.id.logout) {
            checkUser();
        } else {
            Toast.makeText(this, "Coming Soon", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private void checkUser() {
        //if user is real or not
        if (user.isAnonymous()) {
            displayAlert();
        } else {
            fAuth.signOut();
//            startNewActivity(this, Splash.class);
//            finish();
        }
    }

    private void displayAlert() {
        androidx.appcompat.app.AlertDialog.Builder warning = new androidx.appcompat.app.AlertDialog.Builder(this).setTitle("Are you sure?")
                .setMessage("You are logged in with temp Account. Logging out will permanently delete your data.")
                .setPositiveButton("Sync Note", (dialog, which) -> {
                    startNewActivity(this, Register.class);
                    finish();
                }).setNegativeButton("Logout", (dialog, which) -> startNewActivity(this, Logout.class));
        warning.show();
    }

}