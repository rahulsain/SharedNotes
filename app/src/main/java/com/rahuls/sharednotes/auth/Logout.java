package com.rahuls.sharednotes.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.rahuls.sharednotes.R;
import com.rahuls.sharednotes.ui.Splash;

public class Logout extends AppCompatActivity {

    private static final String TAG = "Logout";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logout);

        FirebaseFirestore fStore = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userID = user.getUid();

        Log.d(TAG, "Current UserID is: " + userID);


        //Fixme: delete data created by the Temp User [Not Feasible on Client Side]

        DocumentReference documentReference = fStore.collection("users").document(userID);

        Query queryN = documentReference.collection("myNotes");

        queryN.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String idDelete = document.getId();
                    Log.d(TAG, idDelete + " => " + document.getData());
                    documentReference.collection("myNotes").document(idDelete)
                            .delete()
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Notes created by User are successfully deleted!"))
                            .addOnFailureListener(e -> Log.w(TAG, "Error deleting user notes", e));
                }
            } else {
                Log.d(TAG, "Error getting documents for notes: ", task.getException());
            }
        });

        documentReference.delete().addOnSuccessListener(aVoid -> Toast.makeText(this, "User Deleted", Toast.LENGTH_SHORT).show()).addOnFailureListener(e -> Toast.makeText(this, "Error in deleting User", Toast.LENGTH_SHORT).show());

        //Fixme: delete groups created by temp user [Partially Working]

        Query queryG = fStore.collection("groups").whereEqualTo("CreatedBy", userID);
        queryG.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String idDelete = document.getId();
                            Log.d(TAG, idDelete + " => " + document.getData());
                            fStore.collection("groups").document(idDelete)
                                    .delete()
                                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Group created by user successfully deleted!"))
                                    .addOnFailureListener(e -> Log.w(TAG, "Error deleting group", e));
                        }
                    } else {
                        Log.d(TAG, "Error getting documents for groups: ", task.getException());
                    }
                });

        //delete the temp user from fAuth

        user.delete().addOnSuccessListener(aVoid -> {
            startActivity(new Intent(this, Splash.class));
            overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
            finish();
        });
    }
}