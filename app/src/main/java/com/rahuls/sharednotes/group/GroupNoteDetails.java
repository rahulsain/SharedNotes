package com.rahuls.sharednotes.group;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.rahuls.sharednotes.R;

import java.util.Objects;

public class GroupNoteDetails extends AppCompatActivity {

    Intent data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_details);
        Toolbar toolbar = findViewById(R.id.toolbarTitle);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        data = getIntent();

        TextView content = findViewById(R.id.noteDetailsContent);
        TextView title = findViewById(R.id.noteDetailsTitle);
        ImageView iv_share_note = findViewById(R.id.iv_share_note);
        content.setMovementMethod(new ScrollingMovementMethod());

        String contentS = data.getStringExtra("content");
        String titleS = data.getStringExtra("title");
        String userId = data.getStringExtra("UserId");
        String createdBy = data.getStringExtra("createdBy");

        content.setText(contentS);
        title.setText(titleS);
        content.setBackgroundColor(getResources().getColor(data.getIntExtra("code",0),null));

        FloatingActionButton fab = findViewById(R.id.fab2);
        fab.setOnClickListener(view -> {
            if(!userId.equals(createdBy)){
                Toast.makeText(view.getContext(),"You are not the author, you can't edit",Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(view.getContext(), EditGroupNote.class);
            intent.putExtra("title",titleS);
            intent.putExtra("content",contentS);
            intent.putExtra("noteId",data.getStringExtra("noteId"));
            intent.putExtra("groupId",data.getStringExtra("groupId"));
            intent.putExtra("UserName",data.getStringExtra("UserName"));
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