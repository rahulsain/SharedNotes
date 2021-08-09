package com.rahuls.sharednotes.group;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.rahuls.sharednotes.R;
import com.rahuls.sharednotes.auth.Login;
import com.rahuls.sharednotes.auth.Logout;
import com.rahuls.sharednotes.auth.Register;
import com.rahuls.sharednotes.model.Group;
import com.rahuls.sharednotes.model.Note;
import com.rahuls.sharednotes.note.MainActivity;
import com.rahuls.sharednotes.ui.Splash;
import com.rahuls.sharednotes.ui.UserProfile;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class SharedNote extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "SharedNote";
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle toggle;
    NavigationView nav_view;
    RecyclerView noteLists;
    FirebaseFirestore fStore;
    FirestoreRecyclerAdapter<Note, NoteViewHolder> noteAdapter;
    FirebaseUser user;
    FirebaseAuth fAuth;
    Group group;
    TextView userName;
    TextView userEmail;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared_note);
        Toolbar toolbar = findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);

        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        user = fAuth.getCurrentUser();


        group = new Group();

        group.setGroupId(getIntent().getStringExtra("groupId"));


        Query query = fStore.collection("groups").document(group.getGroupId())
                .collection("ourNotes").orderBy("createdOn", Query.Direction.DESCENDING);

        //query notes > gid > ourNotes

        FirestoreRecyclerOptions<Note> allNotes = new FirestoreRecyclerOptions.Builder<Note>()
                .setQuery(query, Note.class).build();

        noteAdapter = new FirestoreRecyclerAdapter<Note, NoteViewHolder>(allNotes) {
            @Override
            protected void onBindViewHolder(@NonNull NoteViewHolder holder, int position, @NonNull Note model) {
                holder.noteTitle.setText(model.getTitle());
                holder.noteContent.setText(model.getContent());
                holder.userName.setText(model.getCreatedByName());

                StorageReference profileRef = FirebaseStorage.getInstance().getReference().child("users/" + model.getCreatedBy() + "/profile.jpg");
                profileRef.getDownloadUrl().addOnSuccessListener(uri -> Picasso.get().load(uri).into(holder.userPicture));

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
                    intent.putExtra("UserName", getIntent().getStringExtra("UserName"));
                    intent.putExtra("UserId", user.getUid());
                    intent.putExtra("createdBy", model.getCreatedBy());
                    view.getContext().startActivity(intent);
                });
                ImageView menuIcon = holder.view.findViewById(R.id.menuIcon);
                menuIcon.setOnClickListener(v -> {
                    final String docId1 = noteAdapter.getSnapshots().getSnapshot(position).getId();
                    PopupMenu menu = new PopupMenu(v.getContext(), v);
                    menu.setGravity(Gravity.END);
                    menu.getMenu().add("Edit").setOnMenuItemClickListener(item -> {
                        if (!user.getUid().equals(model.getCreatedBy())) {
                            Toast.makeText(getApplicationContext(), "You are not the author, you can't edit", Toast.LENGTH_SHORT).show();
                            return false;
                        }
                        Intent intent = new Intent(v.getContext(), EditGroupNote.class);
                        intent.putExtra("title", model.getTitle());
                        intent.putExtra("content", model.getContent());
                        intent.putExtra("noteId", docId1);
                        intent.putExtra("groupId", group.getGroupId());
                        intent.putExtra("UserName", getIntent().getStringExtra("UserName"));
                        intent.putExtra("UserEmail", getIntent().getStringExtra("UserEmail"));
                        startActivity(intent);
                        return false;
                    });
                    menu.getMenu().add("Delete").setOnMenuItemClickListener(item -> {
                        if (!user.getUid().equals(model.getCreatedBy())) {
                            Toast.makeText(getApplicationContext(), "You are not the author, you can't delete", Toast.LENGTH_SHORT).show();
                            return false;
                        }
                        DocumentReference documentReference = fStore.collection("groups").document(group.getGroupId())
                                .collection("ourNotes").document(docId1);
                        documentReference.delete().addOnSuccessListener(aVoid -> {
                            //note deleted
                            Toast.makeText(getApplicationContext(), "Note Deleted", Toast.LENGTH_SHORT).show();
                        }).addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Error in deleting note", Toast.LENGTH_SHORT).show());
                        return false;
                    });
                    menu.getMenu().add("Info").setOnMenuItemClickListener(item -> {
                        AlertDialog.Builder builder = new AlertDialog.Builder(SharedNote.this).setTitle("Note Information")
                                .setMessage(Html.fromHtml("<b>Created On: </b>" + new Date(model.getCreatedOn().getSeconds() *1000)
                                        + "<br><b>Last Edited On:</b> " + new Date(model.getLastEditedOn().getSeconds() *1000) + "<br><b>Created By:</b> " + model.getCreatedByEmail()))
                                .setPositiveButton("Ok", (dialog, which) -> {
                                    //nothing
                                });
                        builder.show();
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
        userName = headerView.findViewById(R.id.userDisplayName);
        userEmail = headerView.findViewById(R.id.userDisplayEmail);
        nav_view.getMenu().findItem(R.id.groups).setTitle("Personal Notes").setIcon(R.drawable.ic_event_note_black_24dp);
        nav_view.getMenu().findItem(R.id.addNote).setTitle("Add Group Note");

        userEmail.setText(getIntent().getStringExtra("UserEmail"));
        userName.setText(getIntent().getStringExtra("UserName"));

        if (user.isAnonymous()) {
            userEmail.setVisibility(View.GONE);
        }

        FloatingActionButton fab = findViewById(R.id.addNoteFloat);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), AddGroupNote.class);
            intent.putExtra("groupId", group.getGroupId());
            intent.putExtra("createdByName", getIntent().getStringExtra("UserName"));
            startActivity(intent);
            overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
        });

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);
        int itemId = item.getItemId();
        if (itemId == R.id.groups) {
            startNewActivity(this, MainActivity.class);
        } else if (itemId == R.id.addNote) {
            Intent intent = new Intent(this, AddGroupNote.class);
            intent.putExtra("groupId", group.getGroupId());
            startActivity(intent);
            overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
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

    private void startNewActivity(Context context, Class<?> actClass) {
        Intent intent = new Intent(context, actClass);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {

        TextView noteTitle, noteContent,userName;
        View view;
        CardView mCardView;
        ImageView userPicture;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);

            noteTitle = itemView.findViewById(R.id.titles);
            noteContent = itemView.findViewById(R.id.content);
            view = itemView;
            mCardView = itemView.findViewById(R.id.noteCard);
            userName = itemView.findViewById(R.id.displayUserName);
            userPicture = itemView.findViewById(R.id.userPicture);
        }
    }
}