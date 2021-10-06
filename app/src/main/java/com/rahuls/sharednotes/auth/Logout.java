package com.rahuls.sharednotes.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.RuntimeExecutionException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.rahuls.sharednotes.R;

public class Logout extends AppCompatActivity {

    private static final String TAG = "Logout";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logout);

        FirebaseFirestore fStore = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userID = user.getUid();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference profileRef = storageReference.child("users/" + userID + "/profile.jpg");

        Log.d(TAG, "Current UserID is: " + userID);

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

        //delete user profile pic stored in firebase storage

        profileRef.delete().addOnSuccessListener(aVoid -> Log.d(TAG, "Profile Picture of User is successfully deleted!")).addOnFailureListener(aVoid -> Log.d(TAG, "Profile Picture of User is not deleted: " + aVoid.getMessage()));

        //delete user from fAuth

        user.delete().addOnSuccessListener(aVoid -> {
            startActivity(new Intent(this, Login.class));
            overridePendingTransition(0,0);
            finish();
        });
    }
}