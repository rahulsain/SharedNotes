package com.rahuls.sharednotes.auth;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rahuls.sharednotes.MainActivity;
import com.rahuls.sharednotes.R;

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
        getSupportActionBar().setTitle("Login to SharedNotes");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        lEmail = findViewById(R.id.email);
        lPassword = findViewById(R.id.lPassword);

        spinner = findViewById(R.id.progressBar3);

        lButton = findViewById(R.id.loginBtn);
        forgetPassword = findViewById(R.id.forgotPasword);
        createAccount = findViewById(R.id.createAccount);

        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        showWarning();

        lButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mEmail = lEmail.getText().toString();
                String mPassword = lPassword.getText().toString();

                if(mEmail.isEmpty() || mPassword.isEmpty()) {
                    Toast.makeText(Login.this, "All Fields are Required", Toast.LENGTH_SHORT).show();
                    return;
                }

                //delete notes first

                spinner.setVisibility(View.VISIBLE);

                if(fAuth.getCurrentUser().isAnonymous()){
                    FirebaseUser user = fAuth.getCurrentUser();

                    fStore.collection("notes").document(user.getUid()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(Login.this, "All Temp Notes are Deleted.", Toast.LENGTH_SHORT).show();
                        }
                    });

                    //delete Temp user

                    user.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(Login.this, "Temp User Deleted.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                fAuth.signInWithEmailAndPassword(mEmail,mPassword).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Toast.makeText(Login.this, "Logged  in Successfully", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Login.this, "Login Failed! " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        spinner.setVisibility(View.GONE);
                    }
                });

            }
        });

    }

    private void showWarning() {
        AlertDialog.Builder warning = new AlertDialog.Builder(this).setTitle("Are you sure?")
                .setMessage("Linking with Existing Account will delete the temp notes. Create new Account to save them.")
                .setPositiveButton("Save Note", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(getApplicationContext(), Register.class));
                        finish();
                    }
                }).setNegativeButton("It's Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //do nothing
                    }
                });
        warning.show();
    }
}