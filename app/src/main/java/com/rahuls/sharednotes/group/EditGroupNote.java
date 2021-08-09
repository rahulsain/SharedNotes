package com.rahuls.sharednotes.group;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

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

public class EditGroupNote extends AppCompatActivity {
    Intent data;
    EditText editNoteTitle,editNoteContent;
    FirebaseFirestore fStore;
    ProgressBar spinner;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);
        Toolbar toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);

        fStore = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        spinner = findViewById(R.id.progressBar2);

        data = getIntent();

        editNoteTitle = findViewById(R.id.editNoteTitle);
        editNoteContent = findViewById(R.id.editNoteContent);

        String noteTitle = data.getStringExtra("title");
        String noteContent = data.getStringExtra("content");
        String groupId = data.getStringExtra("groupId");

        editNoteTitle.setText(noteTitle);
        editNoteContent.setText(noteContent);

        FloatingActionButton fab = findViewById(R.id.saveEditedNote);
        fab.setOnClickListener(v -> {
            String nTitle = editNoteTitle.getText().toString();
            String nContent = editNoteContent.getText().toString();

            if(nTitle.isEmpty() || nContent.isEmpty()){
                Toast.makeText(v.getContext(),"Title or Content cant be empty",Toast.LENGTH_SHORT).show();
                return;
            }

            spinner.setVisibility(View.VISIBLE);

            //save note
            DocumentReference documentReference = fStore.collection("groups").document(groupId)
                    .collection("ourNotes").document(data.getStringExtra("noteId"));
            Map<String,Object> note = new HashMap<>();
            note.put("title",nTitle);
            note.put("content",nContent);
            note.put("lastEditedOn", FieldValue.serverTimestamp());

            documentReference.update(note).addOnSuccessListener(aVoid -> {
                Toast.makeText(v.getContext(), "Note saved", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), SharedNote.class);
                intent.putExtra("groupId",data.getStringExtra("groupId"));
                intent.putExtra("UserName",data.getStringExtra("UserName"));
                intent.putExtra("UserEmail",data.getStringExtra("UserEmail"));
                startActivity(intent);
            }).addOnFailureListener(e -> {
                Toast.makeText(v.getContext(), "Error, try again", Toast.LENGTH_SHORT).show();
                spinner.setVisibility(View.INVISIBLE);
            });
        });
    }
}
