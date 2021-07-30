package com.rahuls.sharednotes.group;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rahuls.sharednotes.R;

import java.util.HashMap;
import java.util.Map;

public class AddGroupNote extends AppCompatActivity {

    FirebaseFirestore fStore;
    EditText noteTitle,noteContent;
    ProgressBar progressBarSave;
    FirebaseUser user;
    Intent data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fStore = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        noteTitle = findViewById(R.id.addNoteTitle);
        noteContent = findViewById(R.id.addNoteContent);

        progressBarSave = findViewById(R.id.progressBar);

        data = getIntent();


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {

            String nTitle = noteTitle.getText().toString();
            String nContent = noteContent.getText().toString();

            if(nTitle.isEmpty() || nContent.isEmpty()){
                Toast.makeText(AddGroupNote.this,"Title or Content cant be empty",Toast.LENGTH_SHORT).show();
                return;
            }

            progressBarSave.setVisibility(View.VISIBLE);

            String gID = data.getStringExtra("groupId");

            //save note
            DocumentReference documentReference = fStore.collection("groups").document(gID)
                    .collection("ourNotes").document();
            Map<String,Object> note = new HashMap<>();
            note.put("title",nTitle);
            note.put("content",nContent);
            note.put("createdOn", FieldValue.serverTimestamp());
            note.put("createdBy", user.getUid());

            documentReference.set(note).addOnSuccessListener(aVoid -> {
                Toast.makeText(AddGroupNote.this, "Note added", Toast.LENGTH_SHORT).show();
                onBackPressed();
            }).addOnFailureListener(e -> {
                Toast.makeText(AddGroupNote.this, "Error, try again", Toast.LENGTH_SHORT).show();
                progressBarSave.setVisibility(View.INVISIBLE);
            });
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.close_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.close){
            Toast.makeText(this, "Note is not saved",Toast.LENGTH_SHORT).show();
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
