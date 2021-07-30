package com.rahuls.sharednotes.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rahuls.sharednotes.R;
import com.rahuls.sharednotes.ui.MainActivity;

import java.util.Objects;

public class Login extends AppCompatActivity {

    EditText lEmail,lPassword;
    Button lButton;
    TextView forgetPassword,createAccount;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    FirebaseUser user;
    ProgressBar spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Login to SharedNotes");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        lEmail = findViewById(R.id.email);
        lPassword = findViewById(R.id.lPassword);

        spinner = findViewById(R.id.progressBar3);

        lButton = findViewById(R.id.loginBtn);
        forgetPassword = findViewById(R.id.forgotPasword);
        createAccount = findViewById(R.id.createAccount);

        user = FirebaseAuth.getInstance().getCurrentUser();

        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();


        showWarning();

        lButton.setOnClickListener(v -> {
            String mEmail = lEmail.getText().toString();
            String mPassword = lPassword.getText().toString();

            if(mEmail.isEmpty() || mPassword.isEmpty()) {
                Toast.makeText(Login.this, "All Fields are Required", Toast.LENGTH_SHORT).show();
                return;
            }

            //delete notes first

            spinner.setVisibility(View.VISIBLE);

            if(Objects.requireNonNull(fAuth.getCurrentUser()).isAnonymous()){
                FirebaseUser user = fAuth.getCurrentUser();

                fStore.collection("users").document(user.getUid()).delete().addOnSuccessListener(aVoid -> Toast.makeText(Login.this, "All Temp Notes are Deleted.", Toast.LENGTH_SHORT).show());

                //delete Temp user

                user.delete().addOnSuccessListener(aVoid -> Toast.makeText(Login.this, "Temp User Deleted.", Toast.LENGTH_SHORT).show());
            }

            fAuth.signInWithEmailAndPassword(mEmail,mPassword).addOnSuccessListener(authResult -> {
                Toast.makeText(Login.this, "Logged  in Successfully", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }).addOnFailureListener(e -> {
                Toast.makeText(Login.this, "Login Failed! " + e.getMessage(), Toast.LENGTH_SHORT).show();
                spinner.setVisibility(View.GONE);
            });

        });

        createAccount.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(),Register.class)));
        forgetPassword.setOnClickListener(v -> Toast.makeText(this,"Coming Soon",Toast.LENGTH_SHORT).show());
    }

    private void showWarning() {
        AlertDialog.Builder warning = new AlertDialog.Builder(this).setTitle("Are you sure?")
                .setMessage("Linking with Existing Account will delete the temp notes. Create new Account to save them.")
                .setPositiveButton("Save Note", (dialog, which) -> {
                    startActivity(new Intent(getApplicationContext(), Register.class));
                    finish();
                }).setNegativeButton("It's Ok", (dialog, which) -> {
                    //do nothing
                });
        warning.show();
    }
}