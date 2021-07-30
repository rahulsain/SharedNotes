package com.rahuls.sharednotes.group;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.rahuls.sharednotes.R;
import com.rahuls.sharednotes.auth.Login;
import com.rahuls.sharednotes.auth.Register;
import com.rahuls.sharednotes.model.Group;
import com.rahuls.sharednotes.model.Note;
import com.rahuls.sharednotes.ui.MainActivity;
import com.rahuls.sharednotes.ui.Splash;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SharedNote extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle toggle;
    NavigationView nav_view;
    RecyclerView noteLists;
    FirebaseFirestore fStore;
    FirestoreRecyclerAdapter<Note, NoteViewHolder> noteAdapter;
    FirebaseUser user;
    FirebaseAuth fAuth;
    Group group;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        user = fAuth.getCurrentUser();

        group = new Group();

        group.setGroupId(getIntent().getStringExtra("groupId"));


        Query query = fStore.collection("groups").document(group.getGroupId())
                .collection("ourNotes").orderBy("title", Query.Direction.DESCENDING);

        //query notes > gid > ourNotes

        FirestoreRecyclerOptions<Note> allNotes = new FirestoreRecyclerOptions.Builder<Note>()
                .setQuery(query, Note.class).build();

        noteAdapter = new FirestoreRecyclerAdapter<Note, NoteViewHolder>(allNotes) {
            @Override
            protected void onBindViewHolder(@NonNull NoteViewHolder holder, int position, @NonNull Note model) {
                holder.noteTitle.setText(model.getTitle());
                holder.noteContent.setText(model.getContent());
                final int colorCode = getRandomColor();
                holder.mCardView.setBackgroundColor(holder.view.getResources().getColor(colorCode, null));
                final String docId = noteAdapter.getSnapshots().getSnapshot(position).getId();

                holder.view.setOnClickListener(view -> {
                    Intent intent = new Intent(view.getContext(), GroupNoteDetails.class);
                    intent.putExtra("title", model.getTitle());
                    intent.putExtra("content", model.getContent());
                    intent.putExtra("code", colorCode);
                    intent.putExtra("noteId", docId);
                    intent.putExtra("groupId", group.getGroupId());
                    intent.putExtra("UserName",getIntent().getStringExtra("UserName"));
                    view.getContext().startActivity(intent);
                });
                ImageView menuIcon = holder.view.findViewById(R.id.menuIcon);
                menuIcon.setOnClickListener(v -> {
                    final String docId1 = noteAdapter.getSnapshots().getSnapshot(position).getId();
                    PopupMenu menu = new PopupMenu(v.getContext(), v);
                    menu.setGravity(Gravity.END);
                    menu.getMenu().add("Edit").setOnMenuItemClickListener(item -> {
                        Intent intent = new Intent(v.getContext(), EditGroupNote.class);
                        intent.putExtra("title", model.getTitle());
                        intent.putExtra("content", model.getContent());
                        intent.putExtra("noteId", docId1);
                        intent.putExtra("groupId", group.getGroupId());
                        intent.putExtra("UserName",getIntent().getStringExtra("UserName"));
                        startActivity(intent);
                        return false;
                    });
                    menu.getMenu().add("Delete").setOnMenuItemClickListener(item -> {
                        DocumentReference documentReference = fStore.collection("groups").document(group.getGroupId())
                                .collection("ourNotes").document(docId1);
                        documentReference.delete().addOnSuccessListener(aVoid -> {
                            //note deleted
                            Toast.makeText(getApplicationContext(), "Note Deleted", Toast.LENGTH_SHORT).show();
                        }).addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Error in deleting note", Toast.LENGTH_SHORT).show());
                        return false;
                    });
                    menu.show();
                });
            }

            @NonNull
            @Override
            public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_view_layout, parent, false);
                return new NoteViewHolder(view);
            }
        };

        noteLists = findViewById(R.id.notelist);

        drawerLayout = findViewById(R.id.drawer2);
        nav_view = findViewById(R.id.nav_view2);
        nav_view.setNavigationItemSelectedListener(this);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(true);
        toggle.syncState();

        noteLists.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        noteLists.setAdapter(noteAdapter);

        View headerView = nav_view.getHeaderView(0);
        TextView userName = headerView.findViewById(R.id.userDisplayName);
        TextView userEmail = headerView.findViewById(R.id.userDisplayEmail);
        nav_view.getMenu().findItem(R.id.groups).setTitle("Personal Notes");

        if (user.isAnonymous()) {
            userEmail.setVisibility(View.GONE);
            userName.setText(R.string.temp_user);
        } else {
            userName.setText(getIntent().getStringExtra("UserName"));
            userEmail.setText(user.getEmail());
        }

        FloatingActionButton fab = findViewById(R.id.addNoteFloat);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), AddGroupNote.class);
            intent.putExtra("groupId", group.getGroupId());
            startActivity(intent);
            overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
        });

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);
        switch (item.getItemId()) {
            case R.id.groups:
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                break;

            case R.id.addNote:
                Intent intent = new Intent(this, AddGroupNote.class);
                intent.putExtra("groupId", group.getGroupId());
                startActivity(intent);
                overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                break;

            case R.id.sync:
                if (user.isAnonymous()) {
                    startActivity(new Intent(this, Login.class));
                    overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                } else {
                    Toast.makeText(this, "You are Already Connected.", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.logout:
                checkUser();
                break;

            default:
                Toast.makeText(this, "Coming Soon", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private void checkUser() {
        //if user is real or not
        if (user.isAnonymous()) {
            displayAlert();
        } else {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getApplicationContext(), Splash.class));
            overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
            finish();
        }
    }

    private void displayAlert() {
        AlertDialog.Builder warning = new AlertDialog.Builder(this).setTitle("Are you sure?")
                .setMessage("You are logged in with temp Account. Logging out will permanently delete your data.")
                .setPositiveButton("Sync Note", (dialog, which) -> {
                    startActivity(new Intent(getApplicationContext(), Register.class));
                    overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                    finish();
                }).setNegativeButton("Logout", (dialog, which) -> {
                    //Fixme: delete data created by the Temp User [Not Feasible on Client Side]

                    DocumentReference documentReference = fStore.collection("users").document(user.getUid());
                    documentReference.delete().addOnSuccessListener(aVoid -> Toast.makeText(SharedNote.this, "User Deleted", Toast.LENGTH_SHORT).show()).addOnFailureListener(e -> Toast.makeText(SharedNote.this, "Error in deleting User", Toast.LENGTH_SHORT).show());

                    //delete the temp user

                    user.delete().addOnSuccessListener(aVoid -> {
                        startActivity(new Intent(getApplicationContext(), Splash.class));
                        overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                        finish();
                    });
                });
        warning.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.settings) {
            Toast.makeText(this, "Setting Menu is Clicked", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    private int getRandomColor() {
        List<Integer> colorCode = new ArrayList<>();
        colorCode.add(R.color.blue);
        colorCode.add(R.color.yellow);
        colorCode.add(R.color.skyblue);
        colorCode.add(R.color.lightPurple);
        colorCode.add(R.color.lightGreen);
        colorCode.add(R.color.gray);
        colorCode.add(R.color.pink);
        colorCode.add(R.color.red);
        colorCode.add(R.color.greenlight);
        colorCode.add(R.color.notgreen);

        Random randomColor = new Random();
        int number = randomColor.nextInt(colorCode.size());
        return colorCode.get(number);
    }

    @Override
    protected void onStart() {
        super.onStart();
        noteAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (noteAdapter != null) {
            noteAdapter.stopListening();
        }
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {

        TextView noteTitle, noteContent;
        View view;
        CardView mCardView;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);

            noteTitle = itemView.findViewById(R.id.titles);
            noteContent = itemView.findViewById(R.id.content);
            view = itemView;
            mCardView = itemView.findViewById(R.id.noteCard);
        }
    }
}