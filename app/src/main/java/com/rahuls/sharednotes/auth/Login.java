package com.rahuls.sharednotes.auth;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.RuntimeExecutionException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.rahuls.sharednotes.R;
import com.rahuls.sharednotes.note.MainActivity;
import com.rahuls.sharednotes.ui.Splash;

import java.util.Objects;
import java.util.regex.Pattern;

public class Login extends AppCompatActivity {

    private static final String TAG = "Login";
    EditText lEmail, lPassword;
    Button lButton;
    TextView forgetPassword, createAccount;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    FirebaseUser user;
    ProgressBar spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Login to Shared Notes");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        lEmail = findViewById(R.id.email);
        lPassword = findViewById(R.id.lPassword);

        spinner = findViewById(R.id.progressBar3);

        lButton = findViewById(R.id.loginBtn);
        forgetPassword = findViewById(R.id.forgotPassword);
        createAccount = findViewById(R.id.createAccount);

        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();

        user = fAuth.getCurrentUser();

        showWarning();

        lButton.setOnClickListener(v -> {
            String mEmail = lEmail.getText().toString();
            String mPassword = lPassword.getText().toString();

            if (mEmail.isEmpty() || mPassword.isEmpty()) {
                Toast.makeText(Login.this, "All Fields are Required", Toast.LENGTH_SHORT).show();
                return;
            }

            //delete notes first

            spinner.setVisibility(View.VISIBLE);
            try {
                if (user.isAnonymous()) {
                    deleteTempDataBase();
                }
            } catch (NullPointerException e) {
                Toast.makeText(Login.this, "Some Error occurred, please restart the app", Toast.LENGTH_SHORT).show();
            }

            fAuth.signInWithEmailAndPassword(mEmail, mPassword).addOnSuccessListener(authResult -> {
                Toast.makeText(this, "Logged  in Successfully", Toast.LENGTH_SHORT).show();

                Intent i = new Intent(this, MainActivity.class);
//              set the new task and clear flags
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);

            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Login Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                spinner.setVisibility(View.GONE);
                startNewActivity(v.getContext(), Splash.class);

            });


        });

        createAccount.setOnClickListener(v -> startNewActivity(v.getContext(), Register.class));
        forgetPassword.setOnClickListener(v -> {

            final EditText emailID = new EditText(v.getContext());
            emailID.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

            final android.app.AlertDialog.Builder passwordResetDialog = new android.app.AlertDialog.Builder(v.getContext());
            passwordResetDialog.setTitle("Forgot Password");
            passwordResetDialog.setMessage("Enter Registered Email Id");
            passwordResetDialog.setView(emailID);

            passwordResetDialog.setPositiveButton("Forgot Password", (dialog, which) -> {
                // extract the email and send reset link
                String emailId = emailID.getText().toString();
                if(isValid(emailId)) {
                    fAuth.sendPasswordResetEmail(emailId)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(v.getContext(),"Password Reset mail sent. Check your inbox",Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(v.getContext(),"Make sure your email is registered first",Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(v.getContext(),"Email is not valid",Toast.LENGTH_SHORT).show();
                }
            });

            passwordResetDialog.setNegativeButton("Cancel", (dialog, which) -> {
                // close
            });

            passwordResetDialog.create().show();


        });
    }

    private void deleteTempDataBase() {
        String userID = user.getUid();

        //Fixme: delete data created by the Temp User [Not Feasible on Client Side - Permission]

        DocumentReference documentReference = fStore.collection("users").document(userID);

        Query queryN = documentReference.collection("myNotes");

        //delete user notes

        queryN.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                try {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String idDelete = document.getId();
                        Log.d(TAG, idDelete + " => " + document.getData());
                        documentReference.collection("myNotes").document(idDelete)
                                .delete()
                                .addOnSuccessListener(aVoid -> Log.d(TAG, "Notes created by User are successfully deleted!"))
                                .addOnFailureListener(e -> Log.w(TAG, "Error deleting user notes", e));
                    }
                } catch (NullPointerException | RuntimeExecutionException e) {
                    Toast.makeText(this, "You don't have enough permission to do this operation. Make sure you are logged in first", Toast.LENGTH_LONG).show();
                }
            } else {
                Log.d(TAG, "Error getting documents for notes: ", task.getException());
            }
        });

        //delete the user from fStore

        documentReference.delete().addOnSuccessListener(aVoid -> Toast.makeText(this, "User Deleted", Toast.LENGTH_SHORT).show()).addOnFailureListener(e -> Toast.makeText(this, "Error in deleting User", Toast.LENGTH_SHORT).show());

        //Fixme: delete groups created by temp user [Partially Working - Permission]

        Query queryG = fStore.collection("groups").whereEqualTo("CreatedBy", userID);
        queryG.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        try {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String documentId = document.getId();
                                Log.d(TAG, documentId + " => " + document.getData());

                                //notes present in this group

                                fStore.collection("groups").document(documentId).collection("ourNotes").get().addOnCompleteListener(taskT -> {
                                    if (task.isSuccessful()) {
                                        try {
                                            for (QueryDocumentSnapshot documentT : taskT.getResult()) {
                                                String idDelete = documentT.getId();
                                                Log.d(TAG, idDelete + " => " + documentT.getData());
                                                fStore.collection("groups").document(documentId).collection("ourNotes").document(idDelete)
                                                        .delete()
                                                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Notes created by this Group are successfully deleted!"))
                                                        .addOnFailureListener(e -> Log.w(TAG, "Error deleting group notes", e));
                                            }
                                        } catch (NullPointerException | RuntimeExecutionException e) {
                                            Toast.makeText(this, "You don't have enough permission to do this operation. Make sure you are logged in first", Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        Log.d(TAG, "Error getting documents for group notes: ", task.getException());
                                    }
                                });


                                //delete the group

                                fStore.collection("groups").document(documentId)
                                        .delete()
                                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Group created by user successfully deleted!"))
                                        .addOnFailureListener(e -> Log.w(TAG, "Error deleting group", e));
                            }
                        } catch (NullPointerException | RuntimeExecutionException e) {
                            Toast.makeText(this, "You don't have enough permission to do this operation. Make sure you are logged in first", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Log.d(TAG, "Error getting documents for groups: ", task.getException());
                    }
                });

        //delete the temp user from fAuth

        user.delete().addOnSuccessListener(aVoid -> Toast.makeText(this, "Temp User and its Data Deleted.", Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        startNewActivity(this,MainActivity.class);
        finish();
        return super.onOptionsItemSelected(item);
    }

    private void showWarning() {
        AlertDialog.Builder warning = new AlertDialog.Builder(this).setTitle("Are you sure?")
                .setMessage("Linking with Existing Account will delete the temp notes. Create new Account to save them.")
                .setPositiveButton("Save Note", (dialog, which) -> {
                    startNewActivity(getApplicationContext(), Register.class);
                    finish();
                }).setNegativeButton("It's Ok", (dialog, which) -> {
                    //do nothing
                });
        warning.show();
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
}