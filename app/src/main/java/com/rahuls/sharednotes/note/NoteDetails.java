package com.rahuls.sharednotes.note;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.rahuls.sharednotes.R;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class NoteDetails extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_details);
        Toolbar toolbar = findViewById(R.id.toolbarTitle);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        Intent data = getIntent();

        TextView content = findViewById(R.id.noteDetailsContent);
        TextView title = findViewById(R.id.noteDetailsTitle);
        ImageView noteImageView = findViewById(R.id.showNoteImage);
        ImageView iv_share_note = findViewById(R.id.iv_share_note);
        Button documentNoteViewButton = findViewById(R.id.showNoteDocument);
        content.setMovementMethod(new ScrollingMovementMethod());

        content.setText(data.getStringExtra("content"));
        title.setText(data.getStringExtra("title"));
        content.setBackgroundColor(getResources().getColor(data.getIntExtra("code",0),null));

        StorageReference fileRef = FirebaseStorage.getInstance().getReference().child("users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/myNotes/" + data.getStringExtra("noteId") + "/imageNote.jpg");
        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Picasso.get().load(uri).into(noteImageView);
            noteImageView.setVisibility(View.VISIBLE);
            noteImageView.setBackgroundColor(getResources().getColor(data.getIntExtra("code",0),null));
        });

        noteImageView.setOnClickListener(v -> ImageViewPopUpHelper.enablePopUpOnClick(this,noteImageView));


        StorageReference documentRef = FirebaseStorage.getInstance().getReference().child("users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/myNotes/" + data.getStringExtra("noteId") + "/documentNote.pdf");
        documentRef.getDownloadUrl().addOnSuccessListener(uri -> {
            documentNoteViewButton.setVisibility(View.VISIBLE);
            documentNoteViewButton.setOnClickListener(v -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                browserIntent.setDataAndType(uri,"application/pdf");

                Intent chooser = Intent.createChooser(browserIntent, "View PDF File Using");
                chooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // optional

                startActivity(chooser);
            });
        }).addOnFailureListener(onFailure -> documentNoteViewButton.setVisibility(View.GONE));

        FloatingActionButton fab = findViewById(R.id.fab2);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(),EditNote.class);
            intent.putExtra("title",data.getStringExtra("title"));
            intent.putExtra("content",data.getStringExtra("content"));
            intent.putExtra("noteId",data.getStringExtra("noteId"));
            startActivity(intent);
        });

        iv_share_note.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, "Shared Note");
            intent.putExtra(Intent.EXTRA_TEXT, "Title: " + data.getStringExtra("title") + "\n\n" + data.getStringExtra("content"));
            startActivity(Intent.createChooser(intent, "Share Note"));
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}