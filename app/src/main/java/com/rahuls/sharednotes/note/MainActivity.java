package com.rahuls.sharednotes.note;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
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
import androidx.core.splashscreen.SplashScreen;
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
import com.rahuls.sharednotes.auth.Logout;
import com.rahuls.sharednotes.auth.Register;
import com.rahuls.sharednotes.group.ListGroups;
import com.rahuls.sharednotes.model.Note;
import com.rahuls.sharednotes.ui.Splash;
import com.rahuls.sharednotes.ui.UserProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle toggle;
    NavigationView nav_view;
    RecyclerView noteLists;
    FirebaseFirestore fStore;
    FirestoreRecyclerAdapter<Note, NoteViewHolder> noteAdapter;
    FirebaseUser user;
    FirebaseAuth fAuth;
    TextView userName;
    TextView userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SplashScreen.installSplashScreen(this);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);

        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        user = fAuth.getCurrentUser();



        Query query = fStore.collection("users").document(user.getUid())
                .collection("myNotes").orderBy("createdOn", Query.Direction.DESCENDING);

        //query notes > uid > myNotes > notes

        FirestoreRecyclerOptions<Note> allNotes = new FirestoreRecyclerOptions.Builder<Note>()
                .setQuery(query, Note.class).build();

        noteAdapter = new FirestoreRecyclerAdapter<Note, NoteViewHolder>(allNotes) {
            @Override
            protected void onBindViewHolder(@NonNull NoteViewHolder holder, int position, @NonNull Note model) {
                holder.noteTitle.setText(model.getTitle());
                holder.noteContent.setText(model.getContent());
                holder.view.findViewById(R.id.userDetails).setVisibility(View.GONE);

                final int colorCode = getRandomColor();

                holder.mCardView.setBackgroundColor(holder.view.getResources().getColor(colorCode, null));
                final String docId = noteAdapter.getSnapshots().getSnapshot(position).getId();

                holder.view.setOnClickListener(view -> {
                    Intent intent = new Intent(view.getContext(), NoteDetails.class);
                    intent.putExtra("title", model.getTitle());
                    intent.putExtra("content", model.getContent());
                    intent.putExtra("code", colorCode);
                    intent.putExtra("noteId", docId);
                    view.getContext().startActivity(intent);
                });

                ImageView menuIcon = holder.view.findViewById(R.id.menuIcon);

                menuIcon.setOnClickListener(v -> {
                    final String docId1 = noteAdapter.getSnapshots().getSnapshot(position).getId();
                    PopupMenu menu = new PopupMenu(v.getContext(), v);
                    menu.setGravity(Gravity.END);
                    menu.getMenu().add("Edit").setOnMenuItemClickListener(item -> {
                        Intent intent = new Intent(v.getContext(), EditNote.class);
                        intent.putExtra("title", model.getTitle());
                        intent.putExtra("content", model.getContent());
                        intent.putExtra("noteId", docId1);
                        startActivity(intent);
                        return false;
                    });
                    menu.getMenu().add("Delete").setOnMenuItemClickListener(item -> {
                        DocumentReference documentReference = fStore.collection("users").document(user.getUid())
                                .collection("myNotes").document(docId1);
                        documentReference.delete().addOnSuccessListener(aVoid -> {
                            //note deleted
                            Toast.makeText(MainActivity.this, "Note Deleted", Toast.LENGTH_SHORT).show();
                        }).addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Error in deleting note", Toast.LENGTH_SHORT).show());
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

        noteLists = findViewById(R.id.noteList);

        drawerLayout = findViewById(R.id.drawer1);
        nav_view = findViewById(R.id.nav_view1);
        nav_view.setNavigationItemSelectedListener(this);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(true);
        toggle.syncState();

        noteLists.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        noteLists.setAdapter(noteAdapter);

        View headerView = nav_view.getHeaderView(0);
        userName = headerView.findViewById(R.id.userDisplayName);
        userEmail = headerView.findViewById(R.id.userDisplayEmail);

        if (user.isAnonymous()) {
            userEmail.setVisibility(View.GONE);
            userName.setText(R.string.temp_user);
        } else {
            DocumentReference docRef = fStore.collection("users").document(user.getUid());
            docRef.get().addOnSuccessListener(documentSnapshot -> userName.setText(documentSnapshot.getString("UserName")));

            userEmail.setText(user.getEmail());
        }

        FloatingActionButton fab = findViewById(R.id.addNoteFloat);
        fab.setOnClickListener(v -> startNewActivity(v.getContext(), AddNote.class));

    }

    private void startNewActivity(Context context, Class<?> actClass) {
        Intent intent = new Intent(context, actClass);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);
        int itemId = item.getItemId();
        if (itemId == R.id.groups) {
            Intent intent = new Intent(this, ListGroups.class);
            intent.putExtra("userName", userName.getText().toString());
            intent.putExtra("userEmail", userEmail.getText().toString());
            startActivity(intent);
        } else if (itemId == R.id.addNote) {
            startNewActivity(this, AddNote.class);
        } else if (itemId == R.id.sync) {
            if (user.isAnonymous()) {
                startNewActivity(this, Login.class);
            } else {
                Toast.makeText(this, "You are Already Connected.", Toast.LENGTH_SHORT).show();
            }
        } else if (itemId == R.id.logout) {
            checkUser();
        } else {
            Toast.makeText(this, "Coming Soon", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private void checkUser() {
        //if user is real or not
        if (user.isAnonymous()) {
            displayAlert();
        } else {
            fAuth.signOut();
            startNewActivity(this, Splash.class);
            finish();
        }
    }

    private void displayAlert() {
        AlertDialog.Builder warning = new AlertDialog.Builder(this).setTitle("Are you sure?")
                .setMessage("You are logged in with temp Account. Logging out will permanently delete your data.")
                .setPositiveButton("Sync Note", (dialog, which) -> {
                    startNewActivity(this, Register.class);
                    finish();
                }).setNegativeButton("Logout", (dialog, which) -> startNewActivity(this, Logout.class));
        warning.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.settings) {
            Toast.makeText(this, "Coming Soon", Toast.LENGTH_SHORT).show();
        } else if (item.getItemId() == R.id.userProfile) {
            Intent intent = new Intent(this, UserProfile.class);
            intent.putExtra("userName", userName.getText().toString());
            intent.putExtra("userEmail", userEmail.getText().toString());
            startActivity(intent);
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